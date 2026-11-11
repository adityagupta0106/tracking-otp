package com.serviceplus.UserManagement.Enum;

public enum UserStatus {

    INITIAL_CREATION(0),

    PROFILE_UPDATED(1),

    PASSWORD_RESET_REQUIRED(2),

    ACTIVE(3);

    private final Integer code;

    UserStatus(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
