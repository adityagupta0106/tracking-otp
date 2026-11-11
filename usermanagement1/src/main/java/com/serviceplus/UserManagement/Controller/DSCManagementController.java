package com.serviceplus.UserManagement.Controller;

import static com.serviceplus.UserManagement.util.Utility.getUserSessionDetails;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.serviceplus.UserManagement.Dto.DSCApprovalRequest;
import com.serviceplus.UserManagement.Dto.DSCRegistrationRequest;
import com.serviceplus.UserManagement.Dto.DSCResponse;
import com.serviceplus.UserManagement.Dto.DSCStatusResponse;
import com.serviceplus.UserManagement.Entity.UserSessionObject;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.Service.Implementation.DSCManagementService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/a/dsc")
public class DSCManagementController {

	private final DSCManagementService dscManagementService;

	public DSCManagementController(DSCManagementService dscManagementService) {

		this.dscManagementService = dscManagementService;
	}

	@GetMapping("/status")
	public ResponseEntity<DSCStatusResponse> getDSCStatus(HttpServletRequest request) {

		try {
			UserSessionObject userSessionDetails = getUserSessionDetails(request);
			if (userSessionDetails == null) {
				throw new SPRuntimeError("Invalid session", HttpStatus.UNAUTHORIZED);
			}
			return ResponseEntity.ok(dscManagementService.getDSCStatus(userSessionDetails));
		} catch (Exception ex) {
			if (ex instanceof SPRuntimeError) {
				throw (SPRuntimeError) ex;
			}
			ex.printStackTrace();
			throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/register")
	public ResponseEntity<DSCResponse> registerDSC(@RequestBody DSCRegistrationRequest registrationRequest,
			HttpServletRequest request) {

		try {
			UserSessionObject userSessionDetails = getUserSessionDetails(request);
			if (userSessionDetails == null) {
				throw new SPRuntimeError("Invalid session", HttpStatus.UNAUTHORIZED);
			}
			return ResponseEntity.ok(dscManagementService.registerDSC(registrationRequest, userSessionDetails));

		} catch (Exception ex) {
			if (ex instanceof SPRuntimeError) {
				throw (SPRuntimeError) ex;
			}
			ex.printStackTrace();
			throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/deregister")
	public ResponseEntity<DSCResponse> deregisterDSC(HttpServletRequest request) {

		try {
			UserSessionObject userSessionDetails = getUserSessionDetails(request);
			if (userSessionDetails == null) {
				throw new SPRuntimeError("Invalid session", HttpStatus.UNAUTHORIZED);
			}
			return ResponseEntity.ok(dscManagementService.deregisterDSC(userSessionDetails));
		} catch (Exception ex) {
			if (ex instanceof SPRuntimeError) {
				throw (SPRuntimeError) ex;
			}
			ex.printStackTrace();
			throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = "/details")
	public ResponseEntity<DSCStatusResponse> getDSCDetails(HttpServletRequest httpRequest) {
		try {
			UserSessionObject userSessionDetails = getUserSessionDetails(httpRequest);
			if (userSessionDetails == null) {
				throw new SPRuntimeError("Invalid session", HttpStatus.UNAUTHORIZED);
			}
			return ResponseEntity.ok(dscManagementService.getDSCDetails(userSessionDetails));
		} catch (Exception ex) {
			if (ex instanceof SPRuntimeError) {
				throw (SPRuntimeError) ex;
			}
			ex.printStackTrace();
			throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/approval-requests")
	public ResponseEntity<List<DSCStatusResponse>> getApprovalRequests(HttpServletRequest request) {

		try {
			UserSessionObject userSessionDetails = getUserSessionDetails(request);
			if (userSessionDetails == null) {
				throw new SPRuntimeError("Invalid session", HttpStatus.UNAUTHORIZED);
			}
			return ResponseEntity.ok(dscManagementService.getApprovalRequests(userSessionDetails));
		} catch (Exception ex) {
			if (ex instanceof SPRuntimeError) {
				throw (SPRuntimeError) ex;
			}
			ex.printStackTrace();
			throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/approve")
	public ResponseEntity<DSCResponse> approveDSC(@RequestBody DSCApprovalRequest approvalRequest,
			HttpServletRequest request) {

		try {
			UserSessionObject userSessionDetails = getUserSessionDetails(request);
			if (userSessionDetails == null) {
				throw new SPRuntimeError("Invalid session", HttpStatus.UNAUTHORIZED);
			}
			return ResponseEntity.ok(dscManagementService.approveDSC(approvalRequest, userSessionDetails));

		} catch (Exception ex) {
			if (ex instanceof SPRuntimeError) {
				throw (SPRuntimeError) ex;
			}
			ex.printStackTrace();
			throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/reject")
	public ResponseEntity<DSCResponse> rejectDSC(@RequestBody DSCApprovalRequest approvalRequest,
			HttpServletRequest request) {

		try {
			UserSessionObject userSessionDetails = getUserSessionDetails(request);
			if (userSessionDetails == null) {
				throw new SPRuntimeError("Invalid session", HttpStatus.UNAUTHORIZED);
			}
			return ResponseEntity.ok(dscManagementService.rejectDSC(approvalRequest, userSessionDetails));
		} catch (Exception ex) {
			if (ex instanceof SPRuntimeError) {
				throw (SPRuntimeError) ex;
			}
			ex.printStackTrace();
			throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/download")
	public ResponseEntity<byte[]> downloadDSCApi(@RequestParam(required = false, defaultValue = "") String type,
			HttpServletRequest request) {

		try {

			UserSessionObject userSessionDetails = getUserSessionDetails(request);

			if (userSessionDetails == null) {
				throw new SPRuntimeError("Invalid session", HttpStatus.UNAUTHORIZED);
			}

			byte[] zipBytes = dscManagementService.downloadDSCApi(type, userSessionDetails);

			String fileName = switch (type) {
			case "1" -> "WindowsDscApi.zip";
			case "2" -> "MacDscApi.zip";
			case "3" -> "LinuxDscApi.zip";
			default -> "DscApi.zip";
			};

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
					.contentType(MediaType.APPLICATION_OCTET_STREAM).contentLength(zipBytes.length).body(zipBytes);

		} catch (Exception ex) {
			if (ex instanceof SPRuntimeError) {
				throw (SPRuntimeError) ex;
			}
			ex.printStackTrace();
			throw new SPRuntimeError("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}