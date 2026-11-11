package com.serviceplus.UserManagement.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.serviceplus.UserManagement.Entity.AdminUserCreationAccess;

@Repository
public interface AdminUserCreationAccessRepository  extends JpaRepository<AdminUserCreationAccess, Integer>{

	List<AdminUserCreationAccess> findByLoggedInRoleIn(List<Integer> roles);
}
