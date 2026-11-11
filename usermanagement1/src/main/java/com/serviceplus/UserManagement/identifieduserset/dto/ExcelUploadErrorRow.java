package com.serviceplus.UserManagement.identifieduserset.dto;

public class ExcelUploadErrorRow {

    private int rowNumber;
    private String email;
    private String identifier;
    private String errorMessage;

    public ExcelUploadErrorRow() {
    }

    public ExcelUploadErrorRow(int rowNumber, String email, String identifier, String errorMessage) {
        this.rowNumber = rowNumber;
        this.email = email;
        this.identifier = identifier;
        this.errorMessage = errorMessage;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
