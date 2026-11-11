package com.serviceplus.UserManagement.identifieduserset.enums;

public enum JobStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED;

    public static JobStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            return PENDING;
        }
        for (JobStatus s : values()) {
            if (s.name().equalsIgnoreCase(status.trim())) {
                return s;
            }
        }
        return PENDING;
    }
}
