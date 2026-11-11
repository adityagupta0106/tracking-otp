package com.serviceplus.tracking.service;

import static com.serviceplus.tracking.utility.CommonUtil.getUserSessionDetails;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.serviceplus.tracking.dto.ServerSidePaginationRecord;
import com.serviceplus.tracking.dto.UserSessionDTO;
import com.serviceplus.tracking.dto.WorkflowInboxResponse;
import com.serviceplus.tracking.entity.UserOutbox;
import com.serviceplus.tracking.repository.UserOutboxRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class WorkflowOutboxService {

    private static final Logger logger = LoggerFactory.getLogger("workflowOutboxService");

    private static final int MAX_PER_PAGE_COUNT = 50;

    private final WorkflowInboxRedisService workflowInboxRedisService;
    private final UserOutboxRepository userOutboxRepository;

    @Autowired
    public WorkflowOutboxService(WorkflowInboxRedisService workflowInboxRedisService,
            UserOutboxRepository userOutboxRepository) {
        this.workflowInboxRedisService = workflowInboxRedisService;
        this.userOutboxRepository = userOutboxRepository;
    }

    public ServerSidePaginationRecord<?> getOutboxApplications(HttpServletRequest request, int page, int size) {

        UserSessionDTO userSessionDetails = null;
        try {
            userSessionDetails = getUserSessionDetails(request);
        } catch (Exception ex) {
            logger.warn("Could not extract user session details for outbox: {}", ex.getMessage());
        }

        Pageable pageable = PageRequest.of(
                page,
                Math.min(size, MAX_PER_PAGE_COUNT),
                Sort.by(Sort.Direction.DESC, "applRecievedOn"));

        Page<UserOutbox> outboxPage;
        if (userSessionDetails != null) {
            Long userId = userSessionDetails.getUserID();
            String locationId = String.valueOf(userSessionDetails.getLocationId());

            Set<Object> tokens = null;
            try {
                tokens = workflowInboxRedisService.getTokensByUserAndLocation(userId, locationId);
            } catch (Exception ex) {
                logger.warn("Could not fetch redis tokens for outbox: {}", ex.getMessage());
            }

            List<String> tokenKeys = (tokens != null && !tokens.isEmpty())
                    ? tokens.stream().map(Object::toString).toList()
                    : Collections.emptyList();

            if (!tokenKeys.isEmpty()) {
                outboxPage = userOutboxRepository.findByUserTokenInOrderByIsPriorityDescLastActionOnDesc(tokenKeys, pageable);
            } else {
                outboxPage = userOutboxRepository.findAll(pageable);
            }
        } else {
            outboxPage = userOutboxRepository.findAll(pageable);
        }

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
