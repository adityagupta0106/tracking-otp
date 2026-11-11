package com.serviceplus.UserManagement.util;

import static com.serviceplus.UserManagement.util.ApplicationConstants.HOST_HEADER;
import static com.serviceplus.UserManagement.util.SnowflakeIdGenerator.createUniqueId;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import com.google.gson.Gson;
import com.serviceplus.UserManagement.Dto.AttachmentDTO;
import com.serviceplus.UserManagement.Dto.EmailSMSNotificationDto;
import com.serviceplus.UserManagement.Dto.NotificationRequestDTO;
import com.serviceplus.UserManagement.Entity.UserSessionObject;
import com.serviceplus.UserManagement.Enum.ApplicationConstants;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class Utility {
	
	@Value("${aesAuthKey}")
	private String aesAuthKey;

    @Value("${user.excel.path}")
    private String userExcelPath;
	
	private static String AES_AUTH_KEY;
    private static String USER_CREATION_EXCEL_PATH;
	
	@PostConstruct
	public void initalize() {
		AES_AUTH_KEY = this.aesAuthKey;
        USER_CREATION_EXCEL_PATH = this.userExcelPath;
	}
	
	public static SimpleDateFormat uniqueNumber = new SimpleDateFormat("ddMMyyyyhhmmss");

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final String UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";

    private static final String NUMBERS = "0123456789";

    private static final String SPECIAL_CHARACTERS = "@#$%&*!";

    private static final String ALLOWED_CHARACTERS = UPPER_CASE + LOWER_CASE + NUMBERS + SPECIAL_CHARACTERS;

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	public static boolean isValidEmail(String email) {
		return email != null && !email.trim().isEmpty() && EMAIL_PATTERN.matcher(email.trim()).matches();
	}
    
    public static String generateSecurePassword(int length) {

        if (length < 8) {
            throw new IllegalArgumentException("Password length must be at least 8");
        }

        StringBuilder password = new StringBuilder(length);

        password.append(UPPER_CASE.charAt(SECURE_RANDOM.nextInt(UPPER_CASE.length())));
        password.append(LOWER_CASE.charAt(SECURE_RANDOM.nextInt(LOWER_CASE.length())));
        password.append(NUMBERS.charAt(SECURE_RANDOM.nextInt(NUMBERS.length())));
        password.append(SPECIAL_CHARACTERS.charAt(SECURE_RANDOM.nextInt(SPECIAL_CHARACTERS.length())));

        for (int i = 4; i < length; i++) {
            password.append(ALLOWED_CHARACTERS.charAt(SECURE_RANDOM.nextInt(ALLOWED_CHARACTERS.length())));
        }

        return shuffle(password.toString());
    }

    private static String shuffle(String input) {

        List<Character> characters = new ArrayList<>();

        for (char c : input.toCharArray()) {
            characters.add(c);
        }

        Collections.shuffle(characters, SECURE_RANDOM);
        StringBuilder shuffled = new StringBuilder();

        for (Character c : characters) {
            shuffled.append(c);
        }

        return shuffled.toString();
    }

	public static String AESEncrypt(String content) {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(AES_AUTH_KEY.getBytes("UTF-8"), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec,new IvParameterSpec(new byte[16]));
			byte[] encrypted = cipher.doFinal(content.getBytes("UTF-8"));
			String finalString = org.apache.commons.codec.binary.Base64.encodeBase64String(encrypted);
			
			return finalString;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String AESDecrypt(String content) {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(AES_AUTH_KEY.getBytes("UTF-8"), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec,new IvParameterSpec(new byte[16]));
			byte[] original = cipher.doFinal(org.apache.commons.codec.binary.Base64.decodeBase64(content));

			return new String(original);
			
		} catch(Exception e) {
			e.printStackTrace();
            throw new SPRuntimeError("Unable to decrypt", HttpStatus.BAD_REQUEST);
		}
	}
	
	public static Cookie createCookie(String name,String Value,boolean httpflag,boolean secureflag,String sameSite,String path) {
		Cookie cookie = new Cookie(name, Value); 
		cookie.setSecure(secureflag);
		cookie.setAttribute("SameSite", sameSite); 
		cookie.setHttpOnly(httpflag);		
		cookie.setPath(path);
		return cookie;
	}
	
	public static boolean isEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}
	
	public static String generateRandomString(String randomString, Integer length) {

		StringBuffer randStr = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = getRandomNumber(randomString);
			char ch = randomString.charAt(number);
			randStr.append(ch);
		}
		return randStr.toString();
	}
	
	private static int getRandomNumber(String randomString) {
		int randomInt = 0;
		Random randomGenerator = new Random();
		randomInt = randomGenerator.nextInt(randomString.length());
		if (randomInt - 1 == -1) {
			return randomInt;
		} else {
			return randomInt - 1;
		}
	}
	
	public static String getUniqueNumber() {
		Date obCur = new Date();
		return uniqueNumber.format(obCur);
	}
	
	public static String getClientIpAddr(HttpServletRequest request) {
		String ip = "";
		if (request != null) {
			ip = request.getHeader("X-Forwarded-For");
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("WL-Proxy-Client-IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_CLIENT_IP");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_X_FORWARDED_FOR");
			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getRemoteAddr();
			}
			if (isEmpty(ip) || ip.equalsIgnoreCase("127.0.0.1")) {
				try {
					InetAddress ownIP = InetAddress.getLocalHost();
					ip = ownIP.getHostAddress();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return ip;
	}
	
	public static String SHA256(String plaintext)   {
	    MessageDigest md = null;
	    try {
	        md = MessageDigest.getInstance("SHA-256"); 
	        md.update(plaintext.getBytes("UTF-8")); 
	    } catch (Exception e) {
	        md=null;
	    }

	    StringBuffer ls_sb=new StringBuffer();
	    
	    if(md != null){
	    	 byte raw[] = md.digest(); 
	    	    for(int i=0;i<raw.length;i++)
	    	        ls_sb.append(char2hex(raw[i]));
	    }
	   
	    return ls_sb.toString(); 
	    }
	
		public static String char2hex(byte x)
	
		{
		    char arr[]={
		                  '0','1','2','3',
		                  '4','5','6','7',
		                  '8','9','A','B',
		                  'C','D','E','F'
		                };
	
		    char c[] = {arr[(x & 0xF0)>>4],arr[x & 0x0F]};
		    return (new String(c));
		}
		
		/**
		 * Fetch logged in user details
		 * @param request
		 * @return UserSessionObject
		 */
		public static UserSessionObject getUserSessionDetails(HttpServletRequest request) {
			String user = request.getHeader("USER-DETAILS");
			if(isEmpty(user))
				return null;
				
			return (UserSessionObject) stringToEntity(user,UserSessionObject.class);
		}
		
		/**
		 * Checks if the request is from an internal service or not
		 * 
		 * <ul><li>
		 * Example request from notification service for user details where user session will not be there
		 * </li></ul>
		 * @param request
		 * @return boolean
		 */
		public static boolean isServicePlusInternalCall(HttpServletRequest request) {
			final String _servicepluscall = request.getHeader("_scall");	
			final String _uuid = request.getHeader("Atk");
			return "true".equals(_servicepluscall) && _uuid.equals(ApplicationConstants.STATIC_AUTH_TOKEN.getString());
		}
		
		public static Map<String,String> getServicePlusInternalRequestAuth(){
			Map<String,String> headers = new HashMap<>();
			headers.put("_scall", "true");
			headers.put("Atk", ApplicationConstants.STATIC_AUTH_TOKEN.getString());
			return headers;
		}

		public static NotificationRequestDTO getNotificationRequestEntity(String tenantId, String channel,
				String provider, String beneficiary, String subject, String copyTo, String filePath,
				String attachmentType, String content, Integer toMobileNo, Integer countryCode,
				Map<String, Object> configuration) {

			NotificationRequestDTO request = new NotificationRequestDTO();

			request.setTenantId(tenantId);
			request.setChannel(channel);
			request.setProvider(provider);
			request.setConfiguration(configuration);
			request.setCorrelationId(createUniqueId());
			Map<String, Object> recipient = new HashMap<>();
			Map<String, Object> payload = new HashMap<>();

			if ("EMAIL".equalsIgnoreCase(channel)) {

				recipient.put("to", List.of(beneficiary));

				if (copyTo != null && !copyTo.isBlank()) {
					recipient.put("cc", List.of(copyTo.split(";")));
				}

				payload.put("subject", subject);
				payload.put("body", content);

				if (filePath != null && !filePath.isBlank()) {
					AttachmentDTO attachment = new AttachmentDTO();
					attachment.setFileName(new File(filePath).getName());
					attachment.setContentType(attachmentType);
					attachment.setBase64Content(fileToBase64(filePath));
					request.setAttachments(List.of(attachment));
				}

			} else if ("SMS".equalsIgnoreCase(channel)) {
				recipient.put("mobile", toMobileNo);
				if (countryCode != null) {
					recipient.put("countryCode", countryCode);
				}
				payload.put("message", content);
			}

			request.setRecipient(recipient);
			request.setPayload(payload);

			return request;
		}

		public static EmailSMSNotificationDto getNotificationRequestEntity(String beneficiary,String subject,String copyTo,
								String filePath,String attachementType,String content,Integer applId,Integer serviceId
								,boolean isSMS,boolean isMail,Integer toMobileNo,Integer countryCode) {
			EmailSMSNotificationDto notfiDetails = new EmailSMSNotificationDto();
			notfiDetails.setApplicationId((applId != null ? applId : 0));
			notfiDetails.setServiceId((serviceId != null ? serviceId : 0));
			
			if(isMail) {
				EmailSMSNotificationDto.EmailDetails emailDetails = new EmailSMSNotificationDto.EmailDetails();
				emailDetails.setBeneficiary(beneficiary);
				emailDetails.setSubject(subject);
				emailDetails.setContent(content);
				emailDetails.setAttachementType(attachementType);
				if(filePath != null) {
					emailDetails.setAttachement(fileToBase64(filePath));
				}
				notfiDetails.setEmail(emailDetails);
			}
			
			if(isSMS) {
				EmailSMSNotificationDto.SMSDetails smsDetails = new EmailSMSNotificationDto.SMSDetails();
				smsDetails.setBeneficiary(toMobileNo);
				smsDetails.setCountryCode(countryCode);
				smsDetails.setContent(content);
				notfiDetails.setSms(smsDetails);
			}
			
			return notfiDetails;
		}
		
		public static String fileToBase64(String path) {
			File file = new File(path);
			
			try (FileInputStream fileInputStream = new FileInputStream(file)) {
				
	            byte[] fileBytes = new byte[(int) file.length()];
	            fileInputStream.read(fileBytes);
	            return Base64.getEncoder().encodeToString(fileBytes);
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	            return "";
	        }
		}
		
		public static String entityToString(Object data) {
			return new Gson().toJson(data);
		}
		
		public static Object stringToEntity(String data,Class<?> classs) {
			return new Gson().fromJson(data, classs);
		}
		
		public static Object stringToEntityUsingType(String data, Type typeOfT) {
			    return new Gson().fromJson(data, typeOfT);
		}
		
		public static JSONObject stringToJson(String data) {
			return new Gson().fromJson(data, JSONObject.class);
		}
		
		public static String generateTokenForLinkVerification() throws NoSuchAlgorithmException {
	            byte[] randomBytes = new byte[32];
	            SecureRandom secureRandom = new SecureRandom();
	            secureRandom.nextBytes(randomBytes);

	            MessageDigest digest = MessageDigest.getInstance("SHA-256");
	            byte[] hashedBytes = digest.digest(randomBytes);

	            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashedBytes);

		}
		
		public static boolean validateUserId(String encUserId,Long sessionUserId) {
			Long userId = Long.parseLong(AESDecrypt(encUserId));
			return sessionUserId.equals(userId);
		}
		
		public static String extractServiceName(String url) {
	        if (url != null && url.startsWith("lb://")) {
	            int firstSlash = url.indexOf('/', 5);
	            if (firstSlash != -1) {
	                return url.substring(5, firstSlash);
	            }
	            return url.substring(5);
	        }
	        return "unknown-service";
	    }

        public static String getClientHost(HttpServletRequest request){
            //User user = getUserSessionDetails(request);
            String host =  request.getHeader(HOST_HEADER);
            if(isEmpty(host))
                return host;

            host = host.endsWith("/") ? host : host.concat("/");
            return host;
        }

        public static String buildExcelFilePath(Long loggedInUser) {
            String uniqueNo = getUniqueNumber();
            return USER_CREATION_EXCEL_PATH + loggedInUser + "_" + uniqueNo + ".xlsx";
        }


        public static boolean hasRole(UserSessionObject user, Integer roleId) {

            List<UserSessionObject.Roles> roles = user.getRoles();
            return roles != null && roles.stream().anyMatch(r -> roleId.equals(r.getRoleId()));
        }

        public static String generateUniqueToken(String PREFIX){
            return PREFIX.concat(createUniqueId());
        }
}


