package com.serviceplus.UserManagement.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serviceplus.UserManagement.Entity.UserProfile;


@Repository
public interface UserProfileRepository  extends JpaRepository<UserProfile, Long>{

    UserProfile findByEmailIdAndUserIdNot(String email, Long uid);

    UserProfile findByMobileNoAndUserIdNot(String mobileNo, Long uid);

    UserProfile findByUserId(Long userId);

    UserProfile findByEmailId(String emailId);

    UserProfile findByEmailIdAndTenantId(String emailId, String tenantId);

    UserProfile findByMobileNo(String mobileNo);

    UserProfile findByMobileNoAndTenantId(String mobileNo, String tenantId);

    java.util.List<UserProfile> findAllByTenantId(String tenantId);

}
