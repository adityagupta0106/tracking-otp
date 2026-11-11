package com.serviceplus.tracking.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.serviceplus.tracking.dto.ServerSidePaginationRecord;
import com.serviceplus.tracking.dto.ServiceDetailResponse;
import com.serviceplus.tracking.dto.TaskDetailResponse;
import com.serviceplus.tracking.dto.UserSessionDTO;
import com.serviceplus.tracking.dto.WorkflowInboxResponse;
import com.serviceplus.tracking.entity.TaskMaster;
import com.serviceplus.tracking.entity.UserOutbox;
import com.serviceplus.tracking.repository.UserOutboxRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

@Service
public class OutboxUserService {

	private static final Logger logger = LoggerFactory.getLogger("outboxLogger");

	@PersistenceContext
	private EntityManager entityManager;

	private final UserOutboxRepository userOutboxRepository;
	private final WorkflowInboxRedisService workflowInboxRedisService;

	public OutboxUserService(UserOutboxRepository userOutboxRepository,
			WorkflowInboxRedisService workflowInboxRedisService) {
		this.userOutboxRepository = userOutboxRepository;
		this.workflowInboxRedisService = workflowInboxRedisService;
	}

	public List<ServiceDetailResponse> getPendingServices(UserSessionDTO session) {
		logger.info("Fetching pending outbox services. UserId: {}, TenantId: {}, LocationId: {}",
				session.getUserID(), session.getTenantId(), session.getLocationId());

		Set<String> holderIds = Collections.emptySet();
		try {
			Set<Object> redisTokens = workflowInboxRedisService
					.getTokensByUserAndLocation(session.getUserID(), String.valueOf(session.getLocationId()));
			if (redisTokens != null) {
				holderIds = redisTokens.stream().map(String::valueOf).collect(Collectors.toSet());
			}
		} catch (Exception ex) {
			logger.warn("Could not fetch redis tokens for user: {}", session.getUserID(), ex);
		}

		logger.info("Holder tokens fetched for outbox: {}", holderIds.size());

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = cb.createTupleQuery();

		Root<UserOutbox> root = cq.from(UserOutbox.class);
		cq.distinct(true);

		Predicate tokenFilter = (holderIds != null && !holderIds.isEmpty())
				? cb.or(root.get("userToken").in(holderIds), root.get("userToken").isNull())
				: cb.conjunction();

		Subquery<Integer> maxServiceIdSubquery = cq.subquery(Integer.class);
		Root<UserOutbox> maxRoot = maxServiceIdSubquery.from(UserOutbox.class);

		Predicate maxTokenFilter = (holderIds != null && !holderIds.isEmpty())
				? cb.or(maxRoot.get("userToken").in(holderIds), maxRoot.get("userToken").isNull())
				: cb.conjunction();

		maxServiceIdSubquery.select(cb.max(maxRoot.get("serviceId")));
		maxServiceIdSubquery.where(cb.and(cb.equal(maxRoot.get("baseServiceId"), root.get("baseServiceId")),
				cb.equal(maxRoot.get("tenantId"), session.getTenantId()), maxTokenFilter));

		Subquery<Long> countSubquery = cq.subquery(Long.class);
		Root<UserOutbox> countRoot = countSubquery.from(UserOutbox.class);

		Predicate countTokenFilter = (holderIds != null && !holderIds.isEmpty())
				? cb.or(countRoot.get("userToken").in(holderIds), countRoot.get("userToken").isNull())
				: cb.conjunction();

		countSubquery.select(cb.count(countRoot));
		countSubquery.where(cb.and(cb.equal(countRoot.get("baseServiceId"), root.get("baseServiceId")),
				cb.equal(countRoot.get("tenantId"), session.getTenantId()), countTokenFilter));

		cq.multiselect(root.get("serviceId").alias("serviceId"), root.get("serviceName").alias("serviceName"),
				root.get("baseServiceId").alias("baseServiceId"), countSubquery.alias("pendingCount"));

		cq.where(cb.and(cb.equal(root.get("tenantId"), session.getTenantId()), tokenFilter,
				cb.equal(root.get("serviceId"), maxServiceIdSubquery)));

		cq.orderBy(cb.asc(root.get("serviceName")));

		List<Tuple> result = entityManager.createQuery(cq).getResultList();
		logger.info("Found {} pending outbox services.", result.size());
		return result.stream().map(tuple -> {
			ServiceDetailResponse response = new ServiceDetailResponse();
			response.setServiceId(tuple.get("serviceId", Integer.class));
			response.setServiceName(tuple.get("serviceName", String.class));
			response.setPendingCount(tuple.get("pendingCount", Long.class));
			return response;
		}).collect(Collectors.toList());
	}

	public List<TaskDetailResponse> getPendingTasks(Integer baseServiceId, UserSessionDTO session) {
		logger.info("Fetching pending outbox tasks. BaseServiceId: {}, UserId: {}, TenantId: {}, LocationId: {}",
				baseServiceId, session.getUserID(), session.getTenantId(), session.getLocationId());

		Set<String> holderIds = Collections.emptySet();
		try {
			Set<Object> redisTokens = workflowInboxRedisService
					.getTokensByUserAndLocation(session.getUserID(), String.valueOf(session.getLocationId()));
			if (redisTokens != null) {
				holderIds = redisTokens.stream().map(String::valueOf).collect(Collectors.toSet());
			}
		} catch (Exception ex) {
			logger.warn("Could not fetch redis tokens for user: {}", session.getUserID(), ex);
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = cb.createTupleQuery();
		cq.distinct(true);

		Root<UserOutbox> root = cq.from(UserOutbox.class);
		Root<TaskMaster> taskMaster = cq.from(TaskMaster.class);

		cq.multiselect(root.get("taskId").alias("taskId"), root.get("taskName").alias("taskName"),
				cb.count(root).alias("pendingCount"), taskMaster.get("taskProperty").alias("taskProperty"));

		Predicate tokenPred = (holderIds != null && !holderIds.isEmpty())
				? cb.or(root.get("userToken").in(holderIds), root.get("userToken").isNull())
				: cb.conjunction();

		cq.where(cb.and(cb.equal(root.get("tenantId"), session.getTenantId()),
				cb.equal(root.get("baseServiceId"), baseServiceId), tokenPred,
				cb.equal(root.get("taskId"), taskMaster.get("taskId")),
				cb.equal(root.get("tenantId"), taskMaster.get("tenantId")),
				cb.equal(root.get("baseServiceId"), taskMaster.get("baseServiceId"))));

		cq.groupBy(root.get("taskId"), root.get("taskName"), taskMaster.get("taskProperty"));
		cq.orderBy(cb.asc(root.get("taskName")));

		List<Tuple> result = entityManager.createQuery(cq).getResultList();
		logger.info("Found {} pending outbox tasks.", result.size());
		return result.stream().map(tuple -> {
			TaskDetailResponse response = new TaskDetailResponse();
			response.setTaskId(tuple.get("taskId", String.class));
			response.setTaskName(tuple.get("taskName", String.class));
			response.setPendingCount(tuple.get("pendingCount", Long.class));
			response.setTaskProperty(tuple.get("taskProperty", JsonNode.class));
			return response;
		}).collect(Collectors.toList());
	}

	public ServerSidePaginationRecord<WorkflowInboxResponse> getApplications(Integer baseServiceId, String taskId,
			String applRefNo, Integer pageNo, Integer pageSize, UserSessionDTO userSessionDetails) {
		String tenantId = null;
		Long userId = null;
		String locationId = null;

		if (userSessionDetails != null) {
			userId = userSessionDetails.getUserID();
			locationId = String.valueOf(userSessionDetails.getLocationId());
			tenantId = userSessionDetails.getTenantId();
			logger.info("Fetching outbox applications. UserId: {}, BaseServiceId: {}, TaskId: {}, PageNo: {}, PageSize: {}, ApplRefNo: {}",
					userId, baseServiceId, taskId, pageNo, pageSize, applRefNo);
		} else {
			logger.info("Fetching outbox applications without session. BaseServiceId: {}, TaskId: {}, PageNo: {}, PageSize: {}, ApplRefNo: {}",
					baseServiceId, taskId, pageNo, pageSize, applRefNo);
		}

		Set<Object> tokens = null;
		if (userId != null) {
			try {
				tokens = workflowInboxRedisService.getTokensByUserAndLocation(userId, locationId);
			} catch (Exception ex) {
				logger.warn("Could not fetch tokens for outbox applications: {}", ex.getMessage());
			}
		}

		boolean hasTokens = (tokens != null && !tokens.isEmpty());
		List<String> tokenKeys = hasTokens
				? tokens.stream().map(String::valueOf).collect(Collectors.toList())
				: Collections.emptyList();

		Pageable pageable = PageRequest.of(pageNo == null ? 0 : pageNo, pageSize == null ? 20 : pageSize,
				Sort.by(Sort.Direction.DESC, "applRecievedOn"));

		Page<UserOutbox> outboxPage;
		if (StringUtils.hasText(applRefNo)) {
			outboxPage = userOutboxRepository.findOutboxApplicationsWithSearch(tenantId,
					baseServiceId, taskId, tokenKeys, hasTokens, applRefNo.trim(), pageable);
		} else {
			outboxPage = userOutboxRepository.findOutboxApplicationsWithoutSearch(tenantId,
					baseServiceId, taskId, tokenKeys, hasTokens, pageable);
		}

		logger.info("Fetched {} outbox records. TotalRecords: {}, TotalPages: {}", outboxPage.getNumberOfElements(),
				outboxPage.getTotalElements(), outboxPage.getTotalPages());

		ServerSidePaginationRecord<WorkflowInboxResponse> response = new ServerSidePaginationRecord<>();
		response.setData(outboxPage.getContent().stream().map(this::buildOutboxResponse).toList());
		response.setTotalRecords(outboxPage.getTotalElements());
		response.setCurrentPage(outboxPage.getNumber());
		response.setPageSize(outboxPage.getSize());
		response.setTotalPages(outboxPage.getTotalPages());
		return response;
	}

	private WorkflowInboxResponse buildOutboxResponse(UserOutbox outbox) {
		WorkflowInboxResponse response = new WorkflowInboxResponse();
		response.setApplId(outbox.getApplId());
		response.setApplRefNo(outbox.getApplRefNo());
		response.setServiceId(outbox.getServiceId());
		response.setServiceName(outbox.getServiceName());
		response.setTaskId(outbox.getTaskId());
		response.setTaskName(outbox.getTaskName());
		response.setFormId(outbox.getFormId());
		response.setCurrentProcessId(outbox.getCurrentProcessId());
		response.setApplRecievedOn(outbox.getApplRecievedOn());
		response.setLocationId(outbox.getLocationId());
		return response;
	}
}
