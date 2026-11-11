package com.serviceplus.metadata.repository;

import com.serviceplus.metadata.entity.CopiedServiceTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ICopiedServiceTrackerRepository extends JpaRepository<CopiedServiceTracker, Long> {

    Optional<CopiedServiceTracker> findByServiceId(Integer serviceId);

    Optional<CopiedServiceTracker> findByServiceIdAndTargetTenantId(Integer serviceId, String targetTenantId);

    boolean existsByServiceIdAndTargetTenantId(Integer serviceId, String targetTenantId);
}
