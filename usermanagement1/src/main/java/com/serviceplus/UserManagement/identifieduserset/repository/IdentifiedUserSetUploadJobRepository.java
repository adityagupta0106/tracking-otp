package com.serviceplus.UserManagement.identifieduserset.repository;

import com.serviceplus.UserManagement.identifieduserset.entity.IdentifiedUserSetUploadJob;
import com.serviceplus.UserManagement.identifieduserset.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface IdentifiedUserSetUploadJobRepository extends JpaRepository<IdentifiedUserSetUploadJob, Long> {

    @Query("SELECT j FROM IdentifiedUserSetUploadJob j WHERE j.status = :status ORDER BY j.cDate ASC")
    List<IdentifiedUserSetUploadJob> findByStatusOrderByCDateAsc(@Param("status") JobStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE IdentifiedUserSetUploadJob j SET j.status = :newStatus, j.uDate = :now WHERE j.id = :id AND j.status = :expectedStatus")
    int claimJob(@Param("id") Long id,
                 @Param("newStatus") JobStatus newStatus,
                 @Param("expectedStatus") JobStatus expectedStatus,
                 @Param("now") Date now);

    Optional<IdentifiedUserSetUploadJob> findByIdAndTenantId(Long id, String tenantId);

    List<IdentifiedUserSetUploadJob> findByUserSetIdAndTenantId(Long userSetId, String tenantId);

    List<IdentifiedUserSetUploadJob> findByCrByAndTenantId(Long userID, String tenantId);
}
