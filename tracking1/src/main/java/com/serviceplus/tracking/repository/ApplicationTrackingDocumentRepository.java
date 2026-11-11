package com.serviceplus.tracking.repository;

import com.serviceplus.tracking.entity.ApplicationTrackingDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationTrackingDocumentRepository extends JpaRepository<ApplicationTrackingDocument, Long> {

    List<ApplicationTrackingDocument> findByTracking_ProcessId(String processId);

    List<ApplicationTrackingDocument> findByTrackingProcessId(String processId);
    
    @Query(value = """
            SELECT aptd.document_id
            FROM schm_sp.application_tracking_document aptd,
                 schm_sp.application_tracking apl_track
            WHERE aptd.process_id = apl_track.process_id
              AND apl_track.appl_ref_no = :applRefNo
              AND aptd.view_permission @> '["applicant"]'::jsonb
              AND apl_track.tenant_id = :tenantId
              AND aptd.tenant_id = :tenantId
              AND aptd.document_id IS NOT NULL
              AND aptd.document_id <> ''
            ORDER BY aptd.created_on DESC NULLS LAST, aptd.id DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<String> findLatestApplicantPermittedUploadId(
            @Param("applRefNo") String applRefNo,
            @Param("tenantId") String tenantId);
}
