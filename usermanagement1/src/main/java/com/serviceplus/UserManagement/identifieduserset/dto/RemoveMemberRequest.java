package com.serviceplus.UserManagement.identifieduserset.dto;

public class RemoveMemberRequest {

    private Long userId;

    public RemoveMemberRequest() {}

    public RemoveMemberRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
