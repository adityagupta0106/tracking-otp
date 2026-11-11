package com.serviceplus.UserManagement.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.serviceplus.UserManagement.Entity.UserPswdResetLink;

@Repository
public interface PasswordResetRepository  extends JpaRepository<UserPswdResetLink, Integer>{

	Optional<UserPswdResetLink> findByToken(String token);

}
