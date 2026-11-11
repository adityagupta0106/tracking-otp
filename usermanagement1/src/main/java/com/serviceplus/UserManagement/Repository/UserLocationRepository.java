package com.serviceplus.UserManagement.Repository;

import java.util.List;

import com.serviceplus.UserManagement.Dto.ListUserAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.serviceplus.UserManagement.Entity.UserLocation;

@Repository
public interface UserLocationRepository   extends JpaRepository<UserLocation, Long>{

    List<UserLocation> findByUserId(Long userId);

    Page<UserLocation> findByLocationId(Integer locationId, Pageable pageable);

    Page<UserLocation> findByLocationIdAndUser_SignNoContainingIgnoreCase(Integer locationId, String signNo, Pageable pageable);
    
	@Query("""
			SELECT new com.serviceplus.UserManagement.Dto.ListUserAssignment(
			    ld.userId,
			    ld.signNo,
			    up.emailId,
			    ld.spdiStatus,
			    ul.locationId,
			    ul.locationName,
			    uld.designationId,
			    uld.designationName,
			    up.userName
			)
			FROM UserLocation ul
			INNER JOIN LoginDetails ld
			    ON ld.userId = ul.userId
			INNER JOIN RoleAssignment ra
			    ON ra.userLocId = ul.userLocId
			JOIN UserProfile up
			    ON up.userId = ul.userId
			LEFT JOIN UserLocationDesignation uld
			    ON uld.userLocationId = ul.userLocId
			WHERE ul.locationId = :locationId
			AND ra.roleId IN :roleIds
			AND (
			    :applySignFilter = false
			    OR ld.signNo IN :signNos
			)
			""")
	Page<ListUserAssignment> findUsersByRoles(Integer locationId, List<Integer> roleIds, List<String> signNos,
			boolean applySignFilter, Pageable pageable);
	
	@Query("""
			SELECT DISTINCT ul.userId
			FROM UserLocation ul
			WHERE ul.locationId IN :locationIds
			""")
	List<Long> findDistinctUserIdByLocationIdIn(@Param("locationIds") List<Integer> locationIds);
}

