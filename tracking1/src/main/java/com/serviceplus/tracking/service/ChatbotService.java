
package com.serviceplus.tracking.service;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.serviceplus.tracking.ExceptionHandler.SPRuntimeError;
import com.serviceplus.tracking.dto.LatestDocumentDownloadResponse;
import com.serviceplus.tracking.dto.SignedUrlResponse;
import com.serviceplus.tracking.dto.UserSessionDTO;
import com.serviceplus.tracking.feignClient.FileManagementFeignClient;
import com.serviceplus.tracking.repository.ApplicationTrackingDocumentRepository;
import com.serviceplus.tracking.repository.ApplicationTrackingRepository;

import static com.serviceplus.tracking.utility.CommonUtil.entityToString;

@Service
public class ChatbotService {

    private final ApplicationTrackingRepository trackingRepository;

    private final ApplicationTrackingDocumentRepository documentRepository;

    private final FileManagementFeignClient fileManagementFeignClient;

    public ChatbotService(ApplicationTrackingRepository trackingRepository,ApplicationTrackingDocumentRepository documentRepository,FileManagementFeignClient fileManagementFeignClient) {

        this.trackingRepository = trackingRepository;
        this.documentRepository = documentRepository;
        this.fileManagementFeignClient = fileManagementFeignClient;
    }
    // Logged-in chatbot: Download latest permitted document
    public LatestDocumentDownloadResponse downloadLatest(String applRefNo,UserSessionDTO user) {

        // 1. Validate logged-in user
//        if (user == null|| user.getUserID() == null|| user.getUserID() <= 0|| user.getTenantId() == null|| user.getTenantId().isBlank()) {
//
//            throw new SPRuntimeError("Authentication required",HttpStatus.UNAUTHORIZED);
//        }

        // 2. Validate application reference number
        if (applRefNo == null || applRefNo.isBlank()) {

            throw new SPRuntimeError("Application reference number is required",HttpStatus.BAD_REQUEST);
            }
        String reference = applRefNo.trim();
        String tenantId = user.getTenantId();

        // 3. Verify application belongs to logged-in citizen
        boolean applicationExists = !trackingRepository.findByApplRefNoAndAppliedUserIdAndTenantIdOrderByProcessIdAsc(
                        reference,
                        user.getUserID(),
                        tenantId).isEmpty();

        if (!applicationExists) {

            throw new SPRuntimeError("Application not found",HttpStatus.NOT_FOUND);
        }
        // 4. Get latest document having applicant permission
        String uploadId = documentRepository.findLatestApplicantPermittedUploadId(
                        reference,
                        tenantId)
                .filter(id -> !id.isBlank())
                .orElseThrow(() -> new SPRuntimeError("Document not found",HttpStatus.NOT_FOUND));

        // 5. Call existing FileManagement download API
        SignedUrlResponse signedUrl = fileManagementFeignClient.download(uploadId,Map.of("USER-DETAILS", entityToString(user)));

        // 6. Validate FileManagement response
        if (signedUrl == null|| signedUrl.getUrl() == null|| signedUrl.getUrl().isBlank()) {
            throw new SPRuntimeError("Document download is unavailable",HttpStatus.BAD_GATEWAY);
        }
        // 7. Return signed download URL to chatbot
        return new LatestDocumentDownloadResponse(uploadId,signedUrl.getUrl(),signedUrl.getExpiresIn()
        );
    }
}
