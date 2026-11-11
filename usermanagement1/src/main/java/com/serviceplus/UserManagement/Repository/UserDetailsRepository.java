package com.serviceplus.UserManagement.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serviceplus.UserManagement.Entity.LoginDetails;

@Repository
public interface UserDetailsRepository  extends JpaRepository<LoginDetails, Long>{

	@Query(value = "SELECT a.roleId,m.roleName  FROM RoleAssignment a,RoleMaster m  WHERE a.userId=?1 and a.roleId=m.roleId",nativeQuery = false)
	public List<Object[]> fetchUserRoles(Integer userId);

    Optional<LoginDetails> findByUserId(Long userId);
}
