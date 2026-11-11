package com.serviceplus.UserManagement.identifieduserset.enums;

public enum UserAccountStatus {
    ACTIVE,
    INACTIVE;

    public static UserAccountStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            return INACTIVE;
        }
        for (UserAccountStatus s : values()) {
            if (s.name().equalsIgnoreCase(status.trim())) {
                return s;
            }
        }
        return INACTIVE;
    }
}
