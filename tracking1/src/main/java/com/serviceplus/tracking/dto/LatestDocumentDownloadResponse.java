package com.serviceplus.tracking.dto;

/** The browser/client downloads the document using the expiring signed URL. */
public class LatestDocumentDownloadResponse {
    private final String documentId;  // FileManagement uploadId (fd_...), not tracking_document.id.
    private final String downloadUrl;
    private final Integer expiresIn;

    public LatestDocumentDownloadResponse(String documentId, String downloadUrl, Integer expiresIn) {
        this.documentId = documentId;
        this.downloadUrl = downloadUrl;
        this.expiresIn = expiresIn;
    }

    public String getDocumentId() { return documentId; }
    public String getDownloadUrl() { return downloadUrl; }
    public Integer getExpiresIn() { return expiresIn; }
}
