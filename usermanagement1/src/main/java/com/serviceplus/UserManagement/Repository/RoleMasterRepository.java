package com.serviceplus.UserManagement.Repository;

import com.serviceplus.UserManagement.Entity.RoleMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleMasterRepository  extends JpaRepository<RoleMaster, Integer> {
}
