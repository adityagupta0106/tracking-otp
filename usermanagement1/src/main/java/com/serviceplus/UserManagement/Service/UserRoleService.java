package com.serviceplus.UserManagement.Service;

import com.serviceplus.UserManagement.Dto.UserRoleRequest;
import com.serviceplus.UserManagement.Entity.LoginDetails;
import com.serviceplus.UserManagement.Entity.RoleAssignment;
import com.serviceplus.UserManagement.Entity.UserLocation;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.Repository.LoginDetailsRepository;
import com.serviceplus.UserManagement.Repository.RoleAssignmentRepository;
import com.serviceplus.UserManagement.Repository.UserLocationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserRoleService {

    private final LoginDetailsRepository loginDetailsRepository;
    private final UserLocationRepository userLocationRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public UserRoleService(LoginDetailsRepository loginDetailsRepository, UserLocationRepository userLocationRepository, RoleAssignmentRepository roleAssignmentRepository) {

        this.loginDetailsRepository = loginDetailsRepository;
        this.userLocationRepository = userLocationRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
    }

    @Transactional
    public void addRole(UserRoleRequest request) {

        Long userId = request.getUserId();

        LoginDetails login = loginDetailsRepository.findById(userId)
                                                   .orElseThrow(() ->
                                                            new SPRuntimeError(
                                                                    "Invalid user",
                                                                    HttpStatus.BAD_REQUEST)
                                                   );

        List<UserLocation> locations = userLocationRepository.findByUserId(userId);

        if (locations.isEmpty()) {
            throw new SPRuntimeError("User has no location assigned", HttpStatus.BAD_REQUEST);
        }

        for (UserLocation location : locations) {

            boolean exists = roleAssignmentRepository.existsByUserLocIdAndRoleId(location.getUserLocId(), request.getRoleId());

            if (exists) {
                continue;
            }

            RoleAssignment assignment = new RoleAssignment();

            assignment.setUser_role_id(generateRoleAssignmentId());
            assignment.setUserId(userId);
            assignment.setUserLocId(location.getUserLocId());
            assignment.setRoleId(request.getRoleId());
            assignment.setTenantId(login.getTenantId());

            roleAssignmentRepository.save(assignment);
        }
    }

    @Transactional
    public void removeRole(UserRoleRequest request) {

        Long userId = request.getUserId();

        List<UserLocation> locations = userLocationRepository.findByUserId(userId);

        for (UserLocation location : locations) {
            roleAssignmentRepository.deleteByUserLocIdAndRoleId(location.getUserLocId(), request.getRoleId());
        }
    }

    private Long generateRoleAssignmentId() {
        Query query = em.createNativeQuery("SELECT nextval('schm_sp.role_assignment_user_role_id_seq')");
        return ((Number) query.getSingleResult()).longValue();
    }
}
