package com.serviceplus.UserManagement.identifieduserset.enums;

public enum MembershipRole {
    MEMBER,
    ADMIN,
    OFFICER;

    public static MembershipRole fromString(String role) {
        if (role == null || role.trim().isEmpty()) {
            return MEMBER;
        }
        for (MembershipRole r : values()) {
            if (r.name().equalsIgnoreCase(role.trim())) {
                return r;
            }
        }
        return MEMBER;
    }
}
