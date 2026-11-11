//package com.serviceplus.UserManagement.identifieduserset;
//
//import com.serviceplus.UserManagement.Dto.ProvisionedUserResult;
//import com.serviceplus.UserManagement.Dto.RegisterSsoUserRequest;
//import com.serviceplus.UserManagement.Dto.RegisterSsoUserResponse;
//import com.serviceplus.UserManagement.Dto.UpdateOfficialProfileResponse;
//import com.serviceplus.UserManagement.Entity.LoginDetails;
//import com.serviceplus.UserManagement.Entity.UserProfile;
//import com.serviceplus.UserManagement.Repository.LoginDetailsRepository;
//import com.serviceplus.UserManagement.Repository.UserProfileRepository;
//import com.serviceplus.UserManagement.Service.SSOUserRegistrationService;
//import com.serviceplus.UserManagement.Service.UserOnboardingService;
//import com.serviceplus.UserManagement.Service.UserProfileService;
//import jakarta.servlet.http.HttpServletRequest;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class ProfileAndSsoProviderUserIdTest {
//
//    @Mock
//    private LoginDetailsRepository loginDetailsRepository;
//
//    @Mock
//    private UserProfileRepository userProfileRepository;
//
//    @Mock
//    private UserOnboardingService userOnboardingService;
//
//    @Mock
//    private HttpServletRequest httpRequest;
//
//    @Test
//    void testOfficialUpdateProfile_PersistsProviderUserId() {
//        UserProfileService userProfileService = new UserProfileService(userProfileRepository, loginDetailsRepository);
//
//        RegisterSsoUserRequest request = new RegisterSsoUserRequest();
//        request.setIssueUserId("SP_OFF_123");
//        request.setFullName("Ramesh Kumar");
//        request.setSsoProviderId("PARICHAY_PROV_999");
//
//        RegisterSsoUserRequest.CommonVerification verification = new RegisterSsoUserRequest.CommonVerification();
//        verification.setEmail("ramesh@serviceplus.gov.in");
//        verification.setMobile("9811223344");
//        request.setVerification(verification);
//
//        LoginDetails existingLogin = new LoginDetails();
//        existingLogin.setUserId(123L);
//        existingLogin.setSignNo("SP_OFF_123");
//        existingLogin.setSpdiStatus(1);
//        when(loginDetailsRepository.findBySignNo("SP_OFF_123")).thenReturn(Optional.of(existingLogin));
//
//        UserProfile existingProfile = new UserProfile();
//        existingProfile.setUserId(123L);
//        when(userProfileRepository.findByUserId(123L)).thenReturn(existingProfile);
//
//        UpdateOfficialProfileResponse response = userProfileService.updateProfile(request);
//
//        assertTrue(response.getSuccess());
//        assertEquals("PARICHAY_PROV_999", existingLogin.getSsoProviderUserId());
//        verify(loginDetailsRepository, times(1)).save(existingLogin);
//        verify(userProfileRepository, times(1)).save(existingProfile);
//    }
//
//    @Test
//    void testOfficialUpdateProfile_DoesNotOverwriteExistingWithNull() {
//        UserProfileService userProfileService = new UserProfileService(userProfileRepository, loginDetailsRepository);
//
//        RegisterSsoUserRequest request = new RegisterSsoUserRequest();
//        request.setIssueUserId("SP_OFF_123");
//        request.setFullName("Ramesh Kumar");
//        // ssoProviderId is not set (null)
//
//        RegisterSsoUserRequest.CommonVerification verification = new RegisterSsoUserRequest.CommonVerification();
//        verification.setEmail("ramesh@serviceplus.gov.in");
//        verification.setMobile("9811223344");
//        request.setVerification(verification);
//
//        LoginDetails existingLogin = new LoginDetails();
//        existingLogin.setUserId(123L);
//        existingLogin.setSignNo("SP_OFF_123");
//        existingLogin.setSsoProviderUserId("ORIGINAL_PROV_ID");
//        when(loginDetailsRepository.findBySignNo("SP_OFF_123")).thenReturn(Optional.of(existingLogin));
//
//        UserProfile existingProfile = new UserProfile();
//        existingProfile.setUserId(123L);
//        when(userProfileRepository.findByUserId(123L)).thenReturn(existingProfile);
//
//        UpdateOfficialProfileResponse response = userProfileService.updateProfile(request);
//
//        assertTrue(response.getSuccess());
//        // Must preserve existing valid value!
//        assertEquals("ORIGINAL_PROV_ID", existingLogin.getSsoProviderUserId());
//    }
//
//    @Test
//    void testOnboardSsoUser_ExistingUser_PersistsProviderUserId() {
//        SSOUserRegistrationService ssoService = new SSOUserRegistrationService(userOnboardingService, loginDetailsRepository);
//
//        RegisterSsoUserRequest request = new RegisterSsoUserRequest();
//        request.setIssueUserId("SSO_CITIZEN_01");
//        request.setSsoProviderId("NEW_PARICHAY_ID_888");
//
//        LoginDetails existingLogin = new LoginDetails();
//        existingLogin.setUserId(555L);
//        existingLogin.setSignNo("SSO_CITIZEN_01");
//        existingLogin.setPasswd("hash");
//        when(loginDetailsRepository.findBySignNo("SSO_CITIZEN_01")).thenReturn(Optional.of(existingLogin));
//
//        RegisterSsoUserResponse response = ssoService.onboardUser(request, httpRequest);
//
//        assertTrue(response.isSuccess());
//        assertTrue(response.getAlreadyRegistered());
//        assertEquals("NEW_PARICHAY_ID_888", existingLogin.getSsoProviderUserId());
//        verify(loginDetailsRepository, times(1)).save(existingLogin);
//    }
//
//    @Test
//    void testOnboardSsoUser_NewUser_PassesProviderUserIdToOnboarding() {
//        SSOUserRegistrationService ssoService = new SSOUserRegistrationService(userOnboardingService, loginDetailsRepository);
//
//        RegisterSsoUserRequest request = new RegisterSsoUserRequest();
//        request.setIssueUserId("NEW_CITIZEN_02");
//        request.setFullName("Priya Sharma");
//        request.setSsoProviderId("PARICHAY_USER_777");
//        request.setTenantId("tenant_uk");
//
//        RegisterSsoUserRequest.CommonVerification verification = new RegisterSsoUserRequest.CommonVerification();
//        verification.setEmail("priya@serviceplus.gov.in");
//        verification.setMobile("9988776655");
//        request.setVerification(verification);
//
//        when(loginDetailsRepository.findBySignNo("NEW_CITIZEN_02")).thenReturn(Optional.empty());
//
//        ProvisionedUserResult provisionedResult = new ProvisionedUserResult();
//        provisionedResult.setUserId(777L);
//        provisionedResult.setUsername("NEW_CITIZEN_02");
//        provisionedResult.setHashPassword("hashedPassword");
//        when(userOnboardingService.onboardUser(any(), any(), any())).thenReturn(provisionedResult);
//
//        RegisterSsoUserResponse response = ssoService.onboardUser(request, httpRequest);
//
//        assertTrue(response.isSuccess());
//        assertEquals(777L, response.getUserId());
//        verify(userOnboardingService, times(1)).onboardUser(argThat(req ->
//                "PARICHAY_USER_777".equals(req.getSsoProviderId())), any(), any());
//    }
//}
