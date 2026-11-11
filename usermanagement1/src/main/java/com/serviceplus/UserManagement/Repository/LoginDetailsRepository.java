package com.serviceplus.UserManagement.Repository;

import com.serviceplus.UserManagement.Entity.AdminUserCreationAccess;
import com.serviceplus.UserManagement.Entity.LoginDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginDetailsRepository  extends JpaRepository<LoginDetails, Long> {

    Optional<LoginDetails> findBySignNo(String signNo);

    Optional<LoginDetails> findBySignNoAndTenantId(String signNo, String tenantId);

    Optional<LoginDetails> findBySsoProviderUserId(String ssoProviderUserId);

    Optional<LoginDetails> findBySsoProviderUserIdAndTenantId(String ssoProviderUserId, String tenantId);

    Optional<LoginDetails> findByUserId(Long userId);

    Optional<LoginDetails> findByUserIdAndTenantId(Long userId, String tenantId);

    long countByTenantId(String tenantId);
}
