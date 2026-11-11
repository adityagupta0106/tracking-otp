package com.serviceplus.UserManagement.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serviceplus.UserManagement.Entity.UserLocationDesignation;

@Repository
public interface UserLocationDesignationRepository   extends JpaRepository<UserLocationDesignation, Long>{

    List<UserLocationDesignation> findByUserLocationId(Long userLocId);

    List<UserLocationDesignation> findByUserLocationIdIn(List<Long> userLocationIds);
	
}
