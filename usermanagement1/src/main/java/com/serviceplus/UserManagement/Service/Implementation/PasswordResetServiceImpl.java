package com.serviceplus.UserManagement.Service.Implementation;

import com.serviceplus.UserManagement.Entity.UserSessionObject;
import com.serviceplus.UserManagement.Kafka.NotificationKafkaService;
import com.serviceplus.UserManagement.Kafka.UpdateUserKafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.serviceplus.UserManagement.Dto.CaptchaDto;
import com.serviceplus.UserManagement.Dto.PasswordResetRequestDto;
import com.serviceplus.UserManagement.Dto.PasswordResetResponseDto;
import com.serviceplus.UserManagement.Entity.LoginDetails;
import com.serviceplus.UserManagement.Entity.UserPswdResetLink;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.Repository.PasswordResetRepository;
import com.serviceplus.UserManagement.Repository.UserDetailsRepository;
import com.serviceplus.UserManagement.Service.CaptchaService;
import com.serviceplus.UserManagement.Service.PasswordResetService;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.Optional;

import static com.serviceplus.UserManagement.util.Utility.*;

@Service
public class PasswordResetServiceImpl implements PasswordResetService{
	
	@Value("${notification.service}")
	private String NOTIFICATION_SERVICE;
	
	@Value("${pwd.reset.url}")
	private String RESET_URL;
	
	@Value("${alert.activation.link}")
	private String ACTIVATION_EMAIL;
	
	@Value("${pwd.reset.link.access.count}")
	private Integer PWD_RESET_LINK_ACCESS_COUNT;

    private final PasswordResetRepository passwordResetRepository;
    private final CaptchaService captchaService;
    private final UserDetailsRepository userDetailsRepository;
    private final UpdateUserKafkaService updateUserKafkaService;
    private final NotificationKafkaService notificationKafkaService;

    @Autowired
    public PasswordResetServiceImpl(PasswordResetRepository passwordResetRepository, CaptchaService captchaService, UserDetailsRepository userDetailsRepository, UpdateUserKafkaService updateUserKafkaService, NotificationKafkaService notificationKafkaService) {
        this.passwordResetRepository = passwordResetRepository;
        this.captchaService = captchaService;
        this.userDetailsRepository = userDetailsRepository;
        this.updateUserKafkaService = updateUserKafkaService;
        this.notificationKafkaService = notificationKafkaService;
    }

    @Override
	public boolean generateLink(Long userId, String email, String subject, HttpServletRequest request, UserSessionObject loggedInUser) {
		try {

			UserPswdResetLink pswdResetModel = new UserPswdResetLink();
			pswdResetModel.setUserId(userId);
			pswdResetModel.setAccessCount(0);
			pswdResetModel.setAttemptCount(0);
			pswdResetModel.setCreationTime(new Date());
			pswdResetModel.setLastAccessTime(new Date());
			pswdResetModel.setIsActive(true);
            pswdResetModel.setTenantId(loggedInUser.getTenantId());

			String token = generateTokenForLinkVerification();

			pswdResetModel.setToken(token);
			
			passwordResetRepository.saveAndFlush(pswdResetModel);

            if(!isEmpty(getClientHost(request))){
                RESET_URL = getClientHost(request);
            }

			String resetLink = RESET_URL.endsWith("/") ? RESET_URL.concat("linkVerification/").concat(token)
                                            : RESET_URL.concat("/linkVerification/").concat(token);

            String content = ACTIVATION_EMAIL.formatted(resetLink,resetLink,resetLink);


            notificationKafkaService.sendNotification(request,loggedInUser.getTenantId(),email, subject, "", null, "", content, 0, 0, false, true, 0, 0);

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SPRuntimeError("Something went wrong [PW - 001]", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> checkPwdResetToken(String token, HttpServletRequest request) {
		try {		//XSS check	
			UserPswdResetLink link = findByToken(token,request);
			
			link.setAccessCount(link.getAccessCount() + 1);
			
			passwordResetRepository.saveAndFlush(link);
			
			PasswordResetResponseDto dto = new PasswordResetResponseDto();
			dto.setStatus(true);
			return ResponseEntity.ok(dto);
			
		}
		catch(Exception e) {
			if(e instanceof SPRuntimeError)
				throw e;
			
			e.printStackTrace();
			throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<?> updatePwd(PasswordResetRequestDto requestdto, HttpServletRequest request) {
		try {

			UserPswdResetLink link = findByToken(requestdto.getToken(),request);

			if (link.getAttemptCount() > PWD_RESET_LINK_ACCESS_COUNT || !link.getIsActive())
				throw new SPRuntimeError("Invalid URL", HttpStatus.BAD_REQUEST);

			String authString = requestdto.getAuthString();
			String[] auth = authString.split("!");

			String captcha = auth[0];
			String signNo = auth[1];
			String password = auth[2];
			String clientSecurityKey = auth[3];

			CaptchaDto fetchCaptcha = captchaService.fetchCaptcha(request);

			if (!fetchCaptcha.getCaptchaCode().equals(captcha)) {
				throw new SPRuntimeError("Invalid captcha", HttpStatus.BAD_REQUEST);
			}

			link.setAttemptCount(link.getAttemptCount() + 1);

			Optional<LoginDetails> ouser = userDetailsRepository.findById(link.getUserId());

            if(ouser.isEmpty()){
                throw new SPRuntimeError("Unauthorized Access", HttpStatus.UNAUTHORIZED);
            }

			LoginDetails user = ouser.get();
			
			String signNoDBHash = SHA256(user.getSignNo());
			
			if(!signNoDBHash.equalsIgnoreCase(signNo))
				throw new SPRuntimeError("Unauthorized Access", HttpStatus.UNAUTHORIZED);
			
			String seed = fetchCaptcha.getSeed();
			String serverSecurityKey = SHA256(password + seed);
			
			if(!serverSecurityKey.equalsIgnoreCase(clientSecurityKey))
				throw new SPRuntimeError("Unauthorized Access", HttpStatus.UNAUTHORIZED);
			
			user.setPasswd(password);
			user.setSpdiStatus(3);
			
			link.setLastAccessTime(new Date());
			link.setIsActive(false);
			passwordResetRepository.saveAndFlush(link);
			
			userDetailsRepository.saveAndFlush(user);

            updateUserKafkaService.updateUserCredential(user.getUserId(), user.getSpdiStatus(),
														user.getPasswd(), user.getSignNo(), null);


			
			PasswordResetResponseDto dto = new PasswordResetResponseDto();
			dto.setStatus(true);
			return ResponseEntity.ok(dto);

		} catch (Exception e) {
			
			if(e instanceof SPRuntimeError)
			   throw e;
		
			e.printStackTrace();
			throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private UserPswdResetLink findByToken(String token, HttpServletRequest request) {
		Optional<UserPswdResetLink> olink = passwordResetRepository.findByToken(token);
		
		if(olink.isEmpty() || olink.get().getAccessCount() > PWD_RESET_LINK_ACCESS_COUNT || !olink.get().getIsActive())
			throw new SPRuntimeError("Invalid URL", HttpStatus.BAD_REQUEST);
		
		
		String clientIP = getClientIpAddr(request);
		UserPswdResetLink userPswdResetLink = olink.get();
		userPswdResetLink.setLastHitIPAddress(clientIP);
		return userPswdResetLink;
	}
	
}
