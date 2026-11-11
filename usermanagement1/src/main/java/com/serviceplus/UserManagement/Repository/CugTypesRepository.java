package com.serviceplus.UserManagement.Repository;

import com.serviceplus.UserManagement.Entity.CugTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CugTypesRepository extends JpaRepository<CugTypes, Integer> {


}
