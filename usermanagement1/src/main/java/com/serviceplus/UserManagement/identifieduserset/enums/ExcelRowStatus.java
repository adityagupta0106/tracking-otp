package com.serviceplus.UserManagement.identifieduserset.enums;

public enum ExcelRowStatus {
    SUCCESS,
    WARNING,
    FAILED;

    public static ExcelRowStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            return FAILED;
        }
        for (ExcelRowStatus s : values()) {
            if (s.name().equalsIgnoreCase(status.trim())) {
                return s;
            }
        }
        return FAILED;
    }
}
