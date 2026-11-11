package com.serviceplus.UserManagement.identifieduserset.dto;

public class ExcelUploadSuccessRow {

    private int rowNumber;
    private String email;
    private Long userId;
    private String signNo;
    private String status;

    public ExcelUploadSuccessRow() {
    }

    public ExcelUploadSuccessRow(int rowNumber, String email, Long userId, String signNo, String status) {
        this.rowNumber = rowNumber;
        this.email = email;
        this.userId = userId;
        this.signNo = signNo;
        this.status = status;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSignNo() {
        return signNo;
    }

    public void setSignNo(String signNo) {
        this.signNo = signNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
