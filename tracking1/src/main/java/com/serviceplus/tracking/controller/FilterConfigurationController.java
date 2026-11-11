package com.serviceplus.tracking.controller;

import static com.serviceplus.tracking.utility.CommonUtil.validateRequest;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.serviceplus.tracking.ExceptionHandler.SPRuntimeError;
import com.serviceplus.tracking.dto.FilterKafkaMessage;
import com.serviceplus.tracking.service.ServiceFilterConfigService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class FilterConfigurationController {
	private static final Logger logger = LogManager.getLogger(FilterConfigurationController.class);

	private final ServiceFilterConfigService filterConfigService;

	public FilterConfigurationController(ServiceFilterConfigService filterConfigService) {
		super();
		this.filterConfigService = filterConfigService;
	}

	@PostMapping("/o/move-to-prod")
	public ResponseEntity<?> upsert(@RequestBody List<FilterKafkaMessage> filterConfigs, HttpServletRequest request,
			HttpServletResponse response) {
		logger.info("Received request to move-to-prod filter-configuration");
		if (!validateRequest(request)) {
			logger.error("Unauthorized access attempt to move-to-prod filter-configuration");
			throw new SPRuntimeError("Request Denied [ERR: 001]", HttpStatus.UNAUTHORIZED);
		}

		try {
			for (FilterKafkaMessage filterConfig : filterConfigs) {
				filterConfigService.persistMessage(filterConfig);
			}
			logger.info("Filter Configuration move-to-prod Successfully.");
			return ResponseEntity.ok(Map.of("message", "Notification Configuration Save/Update Successfully."));
		} catch (Exception e) {
			logger.error("Error move-to-prod filter-configuration.", e);
			e.printStackTrace();
			throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
