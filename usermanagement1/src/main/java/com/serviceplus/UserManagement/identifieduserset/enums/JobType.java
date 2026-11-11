package com.serviceplus.UserManagement.identifieduserset.enums;

public enum JobType {
    UPLOAD,
    EXPORT;

    public static JobType fromString(String type) {
        if (type == null || type.trim().isEmpty()) {
            return UPLOAD;
        }
        for (JobType t : values()) {
            if (t.name().equalsIgnoreCase(type.trim())) {
                return t;
            }
        }
        return UPLOAD;
    }
}
