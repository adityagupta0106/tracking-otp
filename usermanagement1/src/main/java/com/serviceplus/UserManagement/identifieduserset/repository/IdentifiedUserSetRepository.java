package com.serviceplus.UserManagement.identifieduserset.repository;

import com.serviceplus.UserManagement.identifieduserset.entity.IdentifiedUserSet;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IdentifiedUserSetRepository extends JpaRepository<IdentifiedUserSet, Long> {

    List<IdentifiedUserSet> findByTenantId(String tenantId, Sort sort);

    List<IdentifiedUserSet> findByTenantIdAndActive(String tenantId, Boolean active, Sort sort);

    Optional<IdentifiedUserSet> findByIdAndTenantId(Long id, String tenantId);

    boolean existsByNameAndTenantId(String name, String tenantId);

    boolean existsByNameAndTenantIdAndIdNot(String name, String tenantId, Long id);

    List<IdentifiedUserSet> findByCrByAndTenantId(Long userID, String tenantId, Sort name);

    Optional<IdentifiedUserSet> findByCrByAndIdAndTenantId(Long userID, Long id, String tenantId);
}
