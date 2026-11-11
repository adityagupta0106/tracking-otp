package com.serviceplus.UserManagement.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.serviceplus.UserManagement.Entity.DigitalSignatureRegistration;
import com.serviceplus.UserManagement.Enum.DSCStatus;

public interface DSCManagementRepository extends JpaRepository<DigitalSignatureRegistration, Long> {

    @Query("""
            SELECT d
            FROM DigitalSignatureRegistration d
            WHERE d.userId = :userId
              AND d.status <> :status
            ORDER BY d.id DESC
            """)
    DigitalSignatureRegistration findActiveDSCByUserId(
            @Param("userId") Long userId,
            @Param("status") DSCStatus status);

    List<DigitalSignatureRegistration> findByStatus(
            DSCStatus status);

	List<DigitalSignatureRegistration> findByStatusAndUserIdIn(DSCStatus pendingApproval, List<Long> targetUserIds);

	DigitalSignatureRegistration findByUserIdAndStatus(Long userId,DSCStatus approved);

	Optional<DigitalSignatureRegistration> findByUserId(Long loggedInUserId);

	List<DigitalSignatureRegistration> findByUserIdIn(List<Long> targetUserIds);
}
