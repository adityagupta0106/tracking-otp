package com.serviceplus.tracking.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.serviceplus.tracking.entity.UserOutbox;

@Repository
public interface UserOutboxRepository extends JpaRepository<UserOutbox, Long> {

	List<UserOutbox> findByUserToken(String userToken);

	List<UserOutbox> findByUserTokenIn(List<String> tokenKeys);

	List<UserOutbox> findByUserTokenInAndServiceId(List<String> tokenKeys, Integer serviceId);

	List<UserOutbox> findByUserTokenInAndBaseServiceId(List<String> tokenKeys, Integer baseServiceId);

	Page<UserOutbox> findByUserTokenInAndBaseServiceIdAndTaskIdAndApplRefNoContainingIgnoreCase(List<String> tokenKeys,
			Integer baseServiceId, String taskId, String trim, Pageable pageable);

	Page<UserOutbox> findByUserTokenInAndBaseServiceIdAndTaskId(List<String> tokenKeys, Integer baseServiceId, String taskId,
			Pageable pageable);

	Page<UserOutbox> findByBaseServiceIdAndTaskIdAndApplRefNoContainingIgnoreCase(Integer baseServiceId, String taskId,
			String trim, Pageable pageable);

	Page<UserOutbox> findByBaseServiceIdAndTaskId(Integer baseServiceId, String taskId, Pageable pageable);

	Page<UserOutbox> findByUserTokenInOrderByIsPriorityDescLastActionOnDesc(List<String> userTokens, Pageable pageable);

	void deleteByCurrentProcessIdAndTenantId(String processId, String tenantId);

	Optional<UserOutbox> findByCurrentProcessIdAndTenantId(String processId, String tenantId);

	List<UserOutbox> findByCurrentProcessIdAndUserTokenInAndTenantId(String processId, Set<String> holderIdStrings, String tenantId);

	List<UserOutbox> findByCurrentProcessId(String processId);

	@Modifying
	@Query("""
		delete from UserOutbox u
		where u.applId = :applicationId
		  and u.tenantId = :tenantId
		  and u.baseServiceId = :baseServiceId
	""")
	void deleteByApplIdAndTenantIdAndBaseServiceId(@Param("applicationId") String applicationId,
			@Param("tenantId") String tenantId, @Param("baseServiceId") Integer baseServiceId);

	@Query("""
		SELECT u FROM UserOutbox u
		WHERE (CAST(:tenantId AS string) IS NULL OR u.tenantId = :tenantId)
		  AND u.baseServiceId = :baseServiceId
		  AND u.taskId = :taskId
		  AND (:hasTokens = false OR u.userToken IN :tokenKeys OR u.userToken IS NULL)
	""")
	Page<UserOutbox> findOutboxApplicationsWithoutSearch(
			@Param("tenantId") String tenantId,
			@Param("baseServiceId") Integer baseServiceId,
			@Param("taskId") String taskId,
			@Param("tokenKeys") List<String> tokenKeys,
			@Param("hasTokens") boolean hasTokens,
			Pageable pageable);

	@Query("""
		SELECT u FROM UserOutbox u
		WHERE (CAST(:tenantId AS string) IS NULL OR u.tenantId = :tenantId)
		  AND u.baseServiceId = :baseServiceId
		  AND u.taskId = :taskId
		  AND (:hasTokens = false OR u.userToken IN :tokenKeys OR u.userToken IS NULL)
		  AND LOWER(u.applRefNo) LIKE LOWER(CONCAT('%', :applRefNo, '%'))
	""")
	Page<UserOutbox> findOutboxApplicationsWithSearch(
			@Param("tenantId") String tenantId,
			@Param("baseServiceId") Integer baseServiceId,
			@Param("taskId") String taskId,
			@Param("tokenKeys") List<String> tokenKeys,
			@Param("hasTokens") boolean hasTokens,
			@Param("applRefNo") String applRefNo,
			Pageable pageable);

	@Query("""
		SELECT u FROM UserOutbox u
		WHERE (CAST(:tenantId AS string) IS NULL OR u.tenantId = :tenantId)
		  AND u.baseServiceId = :baseServiceId
		  AND u.taskId = :taskId
		  AND (:hasTokens = false OR u.userToken IN :tokenKeys OR u.userToken IS NULL)
		  AND (CAST(:applRefNo AS string) IS NULL OR LOWER(u.applRefNo) LIKE LOWER(CONCAT('%', CAST(:applRefNo AS string), '%')))
	""")
	Page<UserOutbox> findOutboxApplications(
			@Param("tenantId") String tenantId,
			@Param("baseServiceId") Integer baseServiceId,
			@Param("taskId") String taskId,
			@Param("tokenKeys") List<String> tokenKeys,
			@Param("hasTokens") boolean hasTokens,
			@Param("applRefNo") String applRefNo,
			Pageable pageable);
}
