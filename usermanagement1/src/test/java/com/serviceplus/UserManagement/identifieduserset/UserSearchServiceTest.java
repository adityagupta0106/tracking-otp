//package com.serviceplus.UserManagement.identifieduserset;
//
//import com.google.gson.Gson;
//import com.serviceplus.UserManagement.Entity.LoginDetails;
//import com.serviceplus.UserManagement.Entity.UserProfile;
//import com.serviceplus.UserManagement.Entity.UserSessionObject;
//import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
//import com.serviceplus.UserManagement.Repository.LoginDetailsRepository;
//import com.serviceplus.UserManagement.Repository.UserDetailsRepository;
//import com.serviceplus.UserManagement.Repository.UserProfileRepository;
//import com.serviceplus.UserManagement.identifieduserset.dto.UserSearchRequest;
//import com.serviceplus.UserManagement.identifieduserset.dto.UserSearchResultDto;
//import com.serviceplus.UserManagement.identifieduserset.entity.IdentifiedUserSet;
//import com.serviceplus.UserManagement.identifieduserset.entity.IdentifiedUserSetMember;
//import com.serviceplus.UserManagement.identifieduserset.enums.UserAccountStatus;
//import com.serviceplus.UserManagement.identifieduserset.repository.IdentifiedUserSetMemberRepository;
//import com.serviceplus.UserManagement.identifieduserset.repository.IdentifiedUserSetRepository;
//import com.serviceplus.UserManagement.identifieduserset.service.impl.UserSearchServiceImpl;
//import jakarta.servlet.http.HttpServletRequest;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class UserSearchServiceTest {
//
//    @Mock
//    private UserProfileRepository userProfileRepository;
//
//    @Mock
//    private LoginDetailsRepository loginDetailsRepository;
//
//    @Mock
//    private UserDetailsRepository userDetailsRepository;
//
//    @Mock
//    private IdentifiedUserSetMemberRepository memberRepository;
//
//    @Mock
//    private IdentifiedUserSetRepository userSetRepository;
//
//    @Mock
//    private HttpServletRequest httpRequest;
//
//    @InjectMocks
//    private UserSearchServiceImpl userSearchService;
//
//    private UserSessionObject callerSession;
//    private final Gson gson = new Gson();
//
//    @BeforeEach
//    void setUp() {
//        callerSession = new UserSessionObject();
//        callerSession.setUserID(100L);
//        callerSession.setTenantId("tenant_uk");
//        when(httpRequest.getHeader("USER-DETAILS")).thenReturn(gson.toJson(callerSession));
//    }
//
//    @Test
//    void testSearchByEmail_NotFoundInDatabase() {
//        UserSearchRequest request = new UserSearchRequest();
//        request.setQuery("absent@serviceplus.gov.in");
//
//        when(userProfileRepository.findByEmailId("absent@serviceplus.gov.in")).thenReturn(null);
//
//        List<UserSearchResultDto> results = userSearchService.searchUsers(request, httpRequest);
//
//        assertTrue(results.isEmpty());
//        verify(userProfileRepository, times(1)).findByEmailId("absent@serviceplus.gov.in");
//        verify(loginDetailsRepository, never()).findByUserId(anyLong());
//    }
//
//    @Test
//    void testSearchByEmail_FoundInDatabase() {
//        UserSearchRequest request = new UserSearchRequest();
//        request.setQuery("officer@serviceplus.gov.in");
//
//        UserProfile profile = new UserProfile();
//        profile.setUserId(205L);
//        profile.setUserName("Amit Singh");
//        profile.setEmailId("officer@serviceplus.gov.in");
//        profile.setMobileNo("9876543210");
//        when(userProfileRepository.findByEmailId("officer@serviceplus.gov.in")).thenReturn(profile);
//
//        LoginDetails login = new LoginDetails();
//        login.setUserId(205L);
//        login.setSignNo("SP_OFF_205");
//        login.setSsoProviderUserId("SSO_PROV_789");
//        login.setSpdiStatus(1);
//        login.setTenantId("tenant_uk");
//        when(loginDetailsRepository.findByUserId(205L)).thenReturn(Optional.of(login));
//
//        List<Object[]> rolesList = new ArrayList<>();
//        rolesList.add(new Object[]{20, "Tenant Admin"});
//        when(userDetailsRepository.fetchUserRoles(205)).thenReturn(rolesList);
//
//        IdentifiedUserSetMember member = new IdentifiedUserSetMember();
//        member.setUserSetId(1L);
//        when(memberRepository.findByUserIdAndTenantIdAndActive(205L, "tenant_uk", true)).thenReturn(List.of(member));
//
//        IdentifiedUserSet set = new IdentifiedUserSet();
//        set.setId(1L);
//        set.setName("District Collectors");
//        when(userSetRepository.findById(1L)).thenReturn(Optional.of(set));
//
//        List<UserSearchResultDto> results = userSearchService.searchUsers(request, httpRequest);
//
//        assertFalse(results.isEmpty());
//        UserSearchResultDto dto = results.get(0);
//        assertEquals(205L, dto.getUserId());
//        assertEquals("SP_OFF_205", dto.getSignNo());
//        assertEquals("SSO_PROV_789", dto.getSsoProviderUserId());
//        assertEquals("Amit Singh", dto.getUserName());
//        assertEquals("officer@serviceplus.gov.in", dto.getEmailId());
//        assertEquals("9876543210", dto.getMobileNo());
//        assertEquals(UserAccountStatus.ACTIVE, dto.getStatus());
//        assertTrue(dto.getRoles().contains("Tenant Admin"));
//        assertTrue(dto.getIdentifiedUserSets().contains("District Collectors"));
//    }
//
//    @Test
//    void testSearchByMobile_FoundInDatabase() {
//        UserSearchRequest request = new UserSearchRequest();
//        request.setQuery("9876543210");
//
//        UserProfile profile = new UserProfile();
//        profile.setUserId(205L);
//        profile.setUserName("Amit Singh");
//        profile.setEmailId("officer@serviceplus.gov.in");
//        profile.setMobileNo("9876543210");
//        when(userProfileRepository.findByMobileNo("9876543210")).thenReturn(profile);
//
//        LoginDetails login = new LoginDetails();
//        login.setUserId(205L);
//        login.setSignNo("SP_OFF_205");
//        login.setSpdiStatus(1);
//        login.setTenantId("tenant_uk");
//        when(loginDetailsRepository.findByUserId(205L)).thenReturn(Optional.of(login));
//
//        List<UserSearchResultDto> results = userSearchService.searchUsers(request, httpRequest);
//
//        assertFalse(results.isEmpty());
//        assertEquals("9876543210", results.get(0).getMobileNo());
//        assertEquals(205L, results.get(0).getUserId());
//    }
//
//    @Test
//    void testSearchBySignNo_NotSupported() {
//        UserSearchRequest request = new UserSearchRequest();
//        request.setQuery("SP_OFF_205");
//
//        List<UserSearchResultDto> results = userSearchService.searchUsers(request, httpRequest);
//
//        assertTrue(results.isEmpty());
//        verify(loginDetailsRepository, never()).findBySignNo(anyString());
//    }
//
//    @Test
//    void testSearchByProviderUserId_FoundInDatabase() {
//        UserSearchRequest request = new UserSearchRequest();
//        request.setQuery("SSO_PROV_789");
//
//        LoginDetails login = new LoginDetails();
//        login.setUserId(205L);
//        login.setSignNo("SP_OFF_205");
//        login.setSsoProviderUserId("SSO_PROV_789");
//        login.setSpdiStatus(1);
//        login.setTenantId("tenant_uk");
//        when(loginDetailsRepository.findBySsoProviderUserId("SSO_PROV_789")).thenReturn(Optional.of(login));
//
//        UserProfile profile = new UserProfile();
//        profile.setUserId(205L);
//        profile.setUserName("Amit Singh");
//        when(userProfileRepository.findByUserId(205L)).thenReturn(profile);
//
//        List<UserSearchResultDto> results = userSearchService.searchUsers(request, httpRequest);
//
//        assertFalse(results.isEmpty());
//        assertEquals("SSO_PROV_789", results.get(0).getSsoProviderUserId());
//        assertEquals(205L, results.get(0).getUserId());
//    }
//
//    @Test
//    void testSearchUser_SingleResultOrThrowsNotFound() {
//        UserSearchRequest request = new UserSearchRequest();
//        request.setQuery("unknown@sp.gov.in");
//        when(userProfileRepository.findByEmailId("unknown@sp.gov.in")).thenReturn(null);
//
//        SPRuntimeError ex = assertThrows(SPRuntimeError.class, () -> userSearchService.searchUser(request, httpRequest));
//        assertTrue(ex.getMessage().contains("User not found"));
//    }
//
//    @Test
//    void testSearchUser_CrossTenant_FilteredOut() {
//        UserSearchRequest request = new UserSearchRequest();
//        request.setQuery("other@state.gov.in");
//
//        UserProfile profile = new UserProfile();
//        profile.setUserId(999L);
//        when(userProfileRepository.findByEmailId("other@state.gov.in")).thenReturn(profile);
//
//        LoginDetails foreignLogin = new LoginDetails();
//        foreignLogin.setUserId(999L);
//        foreignLogin.setTenantId("different_tenant"); // Does NOT match caller's "tenant_uk"
//        when(loginDetailsRepository.findByUserId(999L)).thenReturn(Optional.of(foreignLogin));
//
//        List<UserSearchResultDto> results = userSearchService.searchUsers(request, httpRequest);
//
//        assertTrue(results.isEmpty()); // Cross-tenant isolated
//    }
//}
