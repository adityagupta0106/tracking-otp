package com.serviceplus.UserManagement.Dto;

import jakarta.validation.constraints.NotNull;

public class DSCApprovalRequest {

    @NotNull
    private Long dscId;

    private String remarks;

    public Long getDscId() {
        return dscId;
    }

    public void setDscId(Long dscId) {
        this.dscId = dscId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
