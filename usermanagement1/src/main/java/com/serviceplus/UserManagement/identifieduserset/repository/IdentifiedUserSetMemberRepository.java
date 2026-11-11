package com.serviceplus.UserManagement.identifieduserset.repository;

import com.serviceplus.UserManagement.identifieduserset.entity.IdentifiedUserSetMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface IdentifiedUserSetMemberRepository extends JpaRepository<IdentifiedUserSetMember, Long> {

    List<IdentifiedUserSetMember> findByUserSetIdAndTenantIdAndActive(Long userSetId, String tenantId, Boolean active);

    List<IdentifiedUserSetMember> findByUserSetIdAndActive(Long userSetId, Boolean active);

    List<IdentifiedUserSetMember> findByUserIdAndTenantIdAndActive(Long userId, String tenantId, Boolean active);

    List<IdentifiedUserSetMember> findByUserIdAndTenantIdAndActiveAndUserSetIdNot(Long userId, String tenantId, Boolean active, Long userSetId);

    Optional<IdentifiedUserSetMember> findByUserSetIdAndUserIdAndTenantId(Long userSetId, Long userId, String tenantId);

    Optional<IdentifiedUserSetMember> findByUserSetIdAndUserIdAndTenantIdAndActive(Long userSetId, Long userId, String tenantId, Boolean active);

    boolean existsByUserSetIdAndUserIdAndTenantIdAndActive(Long userSetId, Long userId, String tenantId, Boolean active);

    boolean existsByUserIdAndTenantIdAndActive(Long userId, String tenantId, Boolean active);

    long countByUserSetIdAndTenantIdAndActive(Long userSetId, String tenantId, Boolean active);

    List<IdentifiedUserSetMember> findByUserIdAndActive(Long userId, boolean b);

    @Query("SELECT DISTINCT m.userSetId FROM IdentifiedUserSetMember m INNER JOIN IdentifiedUserSet s ON m.userSetId = s.id " +
           "WHERE m.userId = :userId AND m.active = true AND s.active = true " +
           "AND (:tenantId IS NULL OR m.tenantId = :tenantId)")
    List<Long> findActiveUserSetIdsByUserIdAndTenantId(@Param("userId") Long userId, @Param("tenantId") String tenantId);

    @Query("SELECT DISTINCT m.userSetId FROM IdentifiedUserSetMember m INNER JOIN IdentifiedUserSet s ON m.userSetId = s.id " +
            "WHERE m.userId = :userId AND m.active = true AND s.active = true ")
    List<Long> findActiveUserSetIdsByUserId(Long userId);
}
