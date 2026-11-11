package com.serviceplus.UserManagement.identifieduserset.dto;

import java.util.ArrayList;
import java.util.List;

public class ExcelUploadSummaryResponse {

    private int totalRecords;
    private int successfulRecords;
    private int failedRecords;
    private List<ExcelUploadSuccessRow> successfulList = new ArrayList<>();
    private List<ExcelUploadErrorRow> errors = new ArrayList<>();

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getSuccessfulRecords() {
        return successfulRecords;
    }

    public void setSuccessfulRecords(int successfulRecords) {
        this.successfulRecords = successfulRecords;
    }

    public int getFailedRecords() {
        return failedRecords;
    }

    public void setFailedRecords(int failedRecords) {
        this.failedRecords = failedRecords;
    }

    public List<ExcelUploadSuccessRow> getSuccessfulList() {
        return successfulList;
    }

    public void setSuccessfulList(List<ExcelUploadSuccessRow> successfulList) {
        this.successfulList = successfulList;
    }

    public List<ExcelUploadErrorRow> getErrors() {
        return errors;
    }

    public void setErrors(List<ExcelUploadErrorRow> errors) {
        this.errors = errors;
    }
}
