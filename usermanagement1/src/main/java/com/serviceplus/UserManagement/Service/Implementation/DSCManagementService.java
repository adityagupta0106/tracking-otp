package com.serviceplus.UserManagement.Service.Implementation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serviceplus.UserManagement.Dto.DSCApprovalRequest;
import com.serviceplus.UserManagement.Dto.DSCRegistrationRequest;
import com.serviceplus.UserManagement.Dto.DSCResponse;
import com.serviceplus.UserManagement.Dto.DSCStatusResponse;
import com.serviceplus.UserManagement.Dto.DSCVerificationDetails;
import com.serviceplus.UserManagement.Dto.DSCVerificationRequest;
import com.serviceplus.UserManagement.Dto.LocationDetailsDTO;
import com.serviceplus.UserManagement.Entity.DigitalSignatureRegistration;
import com.serviceplus.UserManagement.Entity.UserSessionObject;
import com.serviceplus.UserManagement.Enum.DSCStatus;
import com.serviceplus.UserManagement.ExceptionHandler.SPRuntimeError;
import com.serviceplus.UserManagement.Repository.DSCManagementRepository;
import com.serviceplus.UserManagement.Repository.UserLocationRepository;
import com.serviceplus.UserManagement.Service.LGDService;

@Service
public class DSCManagementService {

	private final DSCManagementRepository dscManagementRepository;
	
	private final UserLocationRepository userLocationRepository;
	
	private final LGDService lgdService;
	
	@Value("${dsc.download.path}")
	private String dscDownloadPath;

	public DSCManagementService(DSCManagementRepository dscManagementRepository,UserLocationRepository userLocationRepository, LGDService lgdService) {
		this.dscManagementRepository = dscManagementRepository;
		this.userLocationRepository = userLocationRepository;
		this.lgdService = lgdService;
	}

	public DSCStatusResponse getDSCStatus(UserSessionObject userSessionDetails) {
		Long userId = userSessionDetails.getUserID();
		DigitalSignatureRegistration dsc = dscManagementRepository.findActiveDSCByUserId(userId,
				DSCStatus.DEREGISTERED);
		if (dsc == null) {
			DSCStatusResponse response = new DSCStatusResponse();
			response.setStatus(DSCStatus.DEREGISTERED.name());
			return response;
		}
		return mapToStatusResponse(dsc);
	}

	@Transactional
	public DSCResponse registerDSC(DSCRegistrationRequest registrationRequest, UserSessionObject userSessionDetails) {
		Long userId = userSessionDetails.getUserID();
		DigitalSignatureRegistration existingDSC = dscManagementRepository.findActiveDSCByUserId(userId,
				DSCStatus.DEREGISTERED);
		if (existingDSC != null) {
			return buildResponse(false, "You are already registered", existingDSC.getId(),
					existingDSC.getStatus().name());
		}

		try {

			X509Certificate certificate = getX509Certificate(registrationRequest.getCertificate());
			//certificate.checkValidity();
			String serialNumber = certificate.getSerialNumber().toString();
			String subjectName = certificate.getSubjectX500Principal().getName();
			String thumbprint = getCertificateThumbprint(certificate);
			Date validFrom = certificate.getNotBefore();
			Date validTo = certificate.getNotAfter();

			DigitalSignatureRegistration dsc = new DigitalSignatureRegistration();

			dsc.setUserId(userId);
			byte[] certificateBytes = Base64.getDecoder().decode(registrationRequest.getCertificate());
			dsc.setCertificate(certificateBytes);
			dsc.setSerialNumber(serialNumber);
			dsc.setSubjectName(subjectName);
			dsc.setThumbprint(thumbprint);
			dsc.setValidFrom(validFrom);
			dsc.setValidTo(validTo);
			dsc.setStatus(DSCStatus.PENDING_APPROVAL);
			dsc.setCreatedBy(userId);
			dsc.setCreatedOn(new Date());
			dsc.setTenantId(userSessionDetails.getTenantId());
			dsc = dscManagementRepository.save(dsc);

			return buildResponse(true, "DSC registered successfully", dsc.getId(), dsc.getStatus().name());

		} catch (CertificateExpiredException ex) {
			return buildResponse(false, "DSC certificate has expired", null, DSCStatus.EXPIRED.name());

		} catch (CertificateNotYetValidException ex) {
			return buildResponse(false, "DSC certificate is not yet valid", null, DSCStatus.REJECTED.name());

		} catch (CertificateException ex) {
			return buildResponse(false, "Invalid DSC certificate", null, DSCStatus.REJECTED.name());

		} catch (Exception ex) {
			ex.printStackTrace();
			return buildResponse(false, "Unable to process DSC certificate", null, DSCStatus.REJECTED.name());
		}
	}

	private X509Certificate getX509Certificate(String certificateData) throws CertificateException {

		if (certificateData == null || certificateData.isBlank()) {
			throw new CertificateException("Certificate is required");
		}

		try {
			String certificate = certificateData.trim();
			if (certificate.contains("-----BEGIN CERTIFICATE-----")) {
				certificate = certificate.replace("-----BEGIN CERTIFICATE-----", "")
						.replace("-----END CERTIFICATE-----", "").replaceAll("\\s", "");
			}

			byte[] certificateBytes = Base64.getDecoder().decode(certificate);

			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

			return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certificateBytes));

		} catch (IllegalArgumentException ex) {

			throw new CertificateException("Invalid Base64 certificate", ex);
		}
	}

	private String getCertificateThumbprint(X509Certificate certificate) throws Exception {

		byte[] encodedCertificate = certificate.getEncoded();

		MessageDigest digest = MessageDigest.getInstance("SHA-256");

		byte[] hash = digest.digest(encodedCertificate);

		StringBuilder thumbprint = new StringBuilder();

		for (byte value : hash) {

			thumbprint.append(String.format("%02X", value));
		}

		return thumbprint.toString();
	}

	@Transactional
	public DSCResponse deregisterDSC(UserSessionObject userSessionDetails) {

		Long userId = userSessionDetails.getUserID();

		DigitalSignatureRegistration dsc = dscManagementRepository.findActiveDSCByUserId(userId,
				DSCStatus.DEREGISTERED);

		if (dsc == null) {

			return buildResponse(false, "No DSC found to un-register", null, DSCStatus.DEREGISTERED.name());
		}

		dsc.setStatus(DSCStatus.DEREGISTERED);

		dsc.setModifiedBy(userId);
		dsc.setModifiedOn(new Date());

		dscManagementRepository.save(dsc);

		return buildResponse(true, "Your DSC has been successfully unregistered", dsc.getId(), dsc.getStatus().name());
	}

	public List<DSCStatusResponse> getApprovalRequests(UserSessionObject userSessionDetails) {
		
		boolean hasRoleDA = userSessionDetails.getRoles() != null && userSessionDetails.getRoles().stream().anyMatch(role -> role.getRoleId() == 18); 
		if (!hasRoleDA) { 
			return List.of();
		}
		Integer sourceEntityId = userSessionDetails.getEntityId();
		Integer sourceLocationId = userSessionDetails.getLocationId();
		Integer sourceEntityLevelId = userSessionDetails.getEntityLevelId();		
		List<Long> targetUserIds=new ArrayList<>();
		List<LocationDetailsDTO>  childLocationsByParentLocationId=lgdService.findChildLocationDetails(sourceEntityId,sourceLocationId,sourceEntityLevelId);
		List<Integer> locationIds=childLocationsByParentLocationId.stream().map(LocationDetailsDTO::getLocationId).collect(Collectors.toList());
		targetUserIds = userLocationRepository.findDistinctUserIdByLocationIdIn(locationIds);
		List<DigitalSignatureRegistration> dscList = dscManagementRepository.findByUserIdIn(targetUserIds);
		if (dscList == null || dscList.isEmpty()) {
			return List.of();
		}
		return dscList.stream().map(this::mapToStatusResponse).toList();
	}

	@Transactional
	public DSCResponse approveDSC(DSCApprovalRequest approvalRequest, UserSessionObject userSessionDetails) {
		DigitalSignatureRegistration dsc = dscManagementRepository.findById(approvalRequest.getDscId()).orElse(null);
		if (dsc == null) {
			return buildResponse(false, "DSC not found", null, null);
		}

		if (dsc.getStatus() != DSCStatus.PENDING_APPROVAL) {
			return buildResponse(false, "DSC is not pending for approval", dsc.getId(), dsc.getStatus().name());
		}

		Long sourceUserId = userSessionDetails.getUserID();
		Long targetUserId = dsc.getUserId();
		validateDSCAuthorization(userSessionDetails, sourceUserId, targetUserId, "approve");
		dsc.setStatus(DSCStatus.APPROVED);
		dsc.setModifiedBy(sourceUserId);
		dsc.setModifiedOn(new Date());

		dscManagementRepository.save(dsc);

		return buildResponse(true, "DSC approved successfully", dsc.getId(), dsc.getStatus().name());
	}

	@Transactional
	public DSCResponse rejectDSC(DSCApprovalRequest approvalRequest, UserSessionObject userSessionDetails) {

		DigitalSignatureRegistration dsc = dscManagementRepository.findById(approvalRequest.getDscId()).orElse(null);

		if (dsc == null) {
			return buildResponse(false, "DSC not found", null, null);
		}

		if (dsc.getStatus() != DSCStatus.PENDING_APPROVAL) {
			return buildResponse(false, "DSC is not pending for approval", dsc.getId(), dsc.getStatus().name());
		}

		Long sourceUserId = userSessionDetails.getUserID();
		Long targetUserId = dsc.getUserId();

		validateDSCAuthorization(userSessionDetails, sourceUserId, targetUserId, "reject");

		dsc.setStatus(DSCStatus.REJECTED);
		dsc.setModifiedBy(sourceUserId);
		dsc.setModifiedOn(new Date());

		dscManagementRepository.save(dsc);

		return buildResponse(true, "DSC rejected successfully", dsc.getId(), dsc.getStatus().name());
	}
	
	private void validateDSCAuthorization(UserSessionObject userSessionDetails, Long sourceUserId, Long targetUserId,
			String action) {

		Integer sourceEntityId = userSessionDetails.getEntityId();
		Integer sourceLocationId = userSessionDetails.getLocationId();
		Integer sourceEntityLevelId = userSessionDetails.getEntityLevelId();

		List<LocationDetailsDTO> childLocations = lgdService.findChildLocationDetails(sourceEntityId, sourceLocationId,
				sourceEntityLevelId);

		if (childLocations == null || childLocations.isEmpty()) {
			throw new SPRuntimeError("You are not authorized to " + action + " this DSC", HttpStatus.UNAUTHORIZED);
		}

		List<Integer> locationIds = childLocations.stream().map(LocationDetailsDTO::getLocationId)
				.filter(Objects::nonNull).toList();

		if (locationIds.isEmpty()) {
			throw new SPRuntimeError("You are not authorized to " + action + " this DSC", HttpStatus.UNAUTHORIZED);
		}

		List<Long> targetUserIds = userLocationRepository.findDistinctUserIdByLocationIdIn(locationIds);

		if (targetUserIds == null || !targetUserIds.contains(targetUserId)) {
			throw new SPRuntimeError("You are not authorized to " + action + " this DSC", HttpStatus.UNAUTHORIZED);
		}
	}

	private DSCStatusResponse mapToStatusResponse(DigitalSignatureRegistration dsc) {

		DSCStatusResponse response = new DSCStatusResponse();

		response.setDscId(dsc.getId());
		response.setSubjectName(dsc.getSubjectName());
		response.setSerialNumber(dsc.getSerialNumber());
		if (dsc.getCertificate() != null) {
			response.setCertificateNumber(Base64.getEncoder().encodeToString(dsc.getCertificate()));
		}
		response.setThumbprint(dsc.getThumbprint());
		response.setValidFrom(dsc.getValidFrom());
		response.setValidTo(dsc.getValidTo());
		response.setStatus(dsc.getStatus().name());

		return response;
	}

	private DSCResponse buildResponse(boolean success, String message, Long dscId, String status) {
		DSCResponse response = new DSCResponse();
		
		response.setSuccess(success);
		response.setMessage(message);
		response.setDscId(dscId);
		response.setStatus(status);
		
		return response;
	}
	
	public DSCVerificationDetails getDSCVerificationDetails(DSCVerificationRequest request) {

		if (request == null || request.getUserId() == null) {

			throw new SPRuntimeError("User ID is required", HttpStatus.BAD_REQUEST);
		}

		DigitalSignatureRegistration dsc = dscManagementRepository.findByUserIdAndStatus(request.getUserId(),DSCStatus.APPROVED);

		if (dsc == null) {

			throw new SPRuntimeError("DSC is not registered", HttpStatus.BAD_REQUEST);
		}

		DSCVerificationDetails response = new DSCVerificationDetails();

		response.setUserId(dsc.getUserId());

		response.setStatus(dsc.getStatus() != null ? dsc.getStatus().name() : null);

		if (dsc.getCertificate() != null) {
			response.setCertificate(Base64.getEncoder().encodeToString(dsc.getCertificate()));
		}

		return response;
	}

	public DSCStatusResponse getDSCDetails(UserSessionObject userSessionDetails) {
		
		Long loggedInUserId = userSessionDetails.getUserID();
		DigitalSignatureRegistration dsc = dscManagementRepository.findByUserId(loggedInUserId).orElse(null);
		
		if (dsc == null) {
			throw new SPRuntimeError("DSC is not registered", HttpStatus.BAD_REQUEST);
		}
		return mapToStatusResponse(dsc);
	}

	public byte[] downloadDSCApi(
	        String type,
	        UserSessionObject userSessionDetails) {

	    try {

	        Path directory = Paths.get(dscDownloadPath);

	        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
	            throw new SPRuntimeError(
	                    "DSC download directory not found",
	                    HttpStatus.INTERNAL_SERVER_ERROR
	            );
	        }

	        List<String> fileNames = new ArrayList<>();

	        switch (type) {

	        case "1":
	            // Windows
	            fileNames.add("NICDSign.exe");
	            break;

	        case "2":
	            // Mac
	            fileNames.add("MacDscApi.zip");
	            break;

	        case "3":
	            // Linux
	            fileNames.add("LinuxDscApi.zip");
	            break;

	        default:
	            // Download all
	            fileNames.add("NICDSign.exe");
	            fileNames.add("MacDscApi.zip");
	            fileNames.add("LinuxDscApi.zip");
	            break;
	        }

	        ByteArrayOutputStream baos =
	                new ByteArrayOutputStream();

	        try (ZipOutputStream zos =
	                     new ZipOutputStream(baos)) {

	            for (String fileName : fileNames) {

	                Path filePath = directory.resolve(fileName);

	                if (!Files.exists(filePath)
	                        || !Files.isRegularFile(filePath)) {

	                    throw new SPRuntimeError(
	                            "DSC API file not found: " + fileName,
	                            HttpStatus.INTERNAL_SERVER_ERROR
	                    );
	                }

	                ZipEntry zipEntry =
	                        new ZipEntry(fileName);

	                zos.putNextEntry(zipEntry);

	                Files.copy(filePath, zos);

	                zos.closeEntry();
	            }
	        }

	        return baos.toByteArray();

	    } catch (SPRuntimeError ex) {

	        throw ex;

	    } catch (IOException ex) {

	        ex.printStackTrace();

	        throw new SPRuntimeError(
	                "Error while creating DSC ZIP",
	                HttpStatus.INTERNAL_SERVER_ERROR
	        );
	    }
	}
	
}