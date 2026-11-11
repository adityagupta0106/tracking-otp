package com.serviceplus.UserManagement.Dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class UserCreationHistoryRequest {

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate fromDate;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate toDate;

    private Integer pageNo = 0;

    private Integer pageSize = 20;

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}