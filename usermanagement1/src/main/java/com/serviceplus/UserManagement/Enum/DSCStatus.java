package com.serviceplus.UserManagement.Enum;

public enum DSCStatus {

    PENDING_APPROVAL("Approval Pending"),

    APPROVED("Approved"),

    REJECTED("Rejected"),

    DEREGISTERED("Deregistered"),

    EXPIRED("Expired");

	private final String label;

    DSCStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
