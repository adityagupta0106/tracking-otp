package com.serviceplus.tracking.controller;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serviceplus.tracking.ExceptionHandler.SPRuntimeError;
import com.serviceplus.tracking.dto.ServerSidePaginationRecord;
import com.serviceplus.tracking.dto.ServiceDetailResponse;
import com.serviceplus.tracking.dto.TaskDetailResponse;
import com.serviceplus.tracking.dto.UserSessionDTO;
import com.serviceplus.tracking.dto.WorkflowInboxResponse;
import com.serviceplus.tracking.service.OutboxUserService;
import com.serviceplus.tracking.service.ServiceTaskFilterService;
import com.serviceplus.tracking.utility.CommonUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/a/outbox")
public class OutboxController {

	private static final Logger logger = LoggerFactory.getLogger("outboxLogger");

	@Autowired
	private OutboxUserService outboxService;

	@Autowired
	private ServiceTaskFilterService serviceTaskFilterService;

	@GetMapping("filter-list")
	public ResponseEntity<?> getFilterAttribute(@RequestParam Long serviceId, @RequestParam String taskId,
			HttpServletRequest request) {
		try {
			UserSessionDTO userSessionDetails = CommonUtil.getUserSessionDetails(request);
			if (userSessionDetails == null) {
				throw new SPRuntimeError("Unauthorized", HttpStatus.UNAUTHORIZED);
			}
			return serviceTaskFilterService.getFilterAttribute(serviceId, taskId, 'I', List.of('I', 'B'));
		} catch (Exception ex) {
			if (ex instanceof SPRuntimeError) {
				throw ex;
			} else {
				logger.error("Error get Filter Attributes for Outbox", ex);
				throw new SPRuntimeError("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

	@GetMapping("service-list")
	public ResponseEntity<?> getPendingServices(HttpServletRequest request) {

		List<ServiceDetailResponse> serviceList = new ArrayList<>();
		JSONObject responseJson = new JSONObject();

		try {
			UserSessionDTO userSessionDetails = CommonUtil.getUserSessionDetails(request);
			if (userSessionDetails == null) {
				responseJson.put("errorCode", "400");
				responseJson.put("errorMessage", "Bad Request");
				return new ResponseEntity<>(responseJson.toString(), HttpStatus.BAD_REQUEST);
			}
			serviceList = outboxService.getPendingServices(userSessionDetails);
			if (serviceList.isEmpty()) {
				responseJson.put("message", "No pending outbox services found");
				return new ResponseEntity<>(responseJson.toString(), HttpStatus.OK);
			}
			return ResponseEntity.ok(serviceList);
		} catch (Exception ex) {
			logger.error("Error while fetching pending outbox services", ex);
			responseJson.put("errorCode", "500");
			responseJson.put("errorMessage", "Internal Server Error");
			return new ResponseEntity<>(responseJson.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("task-list")
	public ResponseEntity<?> getPendingTaskList(@RequestParam Integer serviceId, HttpServletRequest request) {
		JSONObject responseJson = new JSONObject();
		try {
			UserSessionDTO userSessionDetails = CommonUtil.getUserSessionDetails(request);
			if (userSessionDetails == null) {
				responseJson.put("errorCode", HttpStatus.UNAUTHORIZED.value());
				responseJson.put("message", "Session Expired");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseJson.toString());
			}
			List<TaskDetailResponse> taskList = outboxService.getPendingTasks(serviceId / 10000, userSessionDetails);
			return ResponseEntity.ok(taskList);

		} catch (Exception e) {
			responseJson.put("errorCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
			responseJson.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseJson.toString());
		}
	}

	@RequestMapping(value = "applications", method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> getApplications(@RequestParam Integer serviceId, @RequestParam String taskId,
			@RequestParam(required = false) String applRefNo, @RequestParam(defaultValue = "0") Integer pageNo,
			@RequestParam(defaultValue = "20") Integer pageSize, HttpServletRequest request) {
		JSONObject responseJson = new JSONObject();
		try {
			UserSessionDTO userSessionDetails = null;
			try {
				userSessionDetails = CommonUtil.getUserSessionDetails(request);
			} catch (Exception ignored) {
			}

			Integer baseServiceId = (serviceId != null && serviceId > 10000) ? (serviceId / 10000) : serviceId;

			ServerSidePaginationRecord<WorkflowInboxResponse> applicationPage = outboxService.getApplications(
					baseServiceId, taskId, applRefNo, pageNo, pageSize, userSessionDetails);

			return ResponseEntity.ok(applicationPage);

		} catch (Exception e) {
			responseJson.put("errorCode", 500);
			responseJson.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseJson.toString());
		}
	}

}
