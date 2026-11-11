package com.serviceplus.UserManagement.Repository;

import com.serviceplus.UserManagement.Entity.RoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, Long> {

    List<RoleAssignment> findByRoleIdAndUserLocIdIn(Integer roleId, List<Long> userLocIds);

    void deleteByUserLocIdAndRoleId(Long userLocId, Integer roleId);

    boolean existsByUserLocIdAndRoleId(Long userLocId, Integer roleId);

    boolean existsByUserIdAndRoleId(Long userId, Integer roleId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RoleAssignment r WHERE r.userId = :userId AND r.roleId = :roleId")
    void deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Integer roleId);

    List<RoleAssignment> findByUserIdAndRoleId(Long userId, Integer roleId);
}
