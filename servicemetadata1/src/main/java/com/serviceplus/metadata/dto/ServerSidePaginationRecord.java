package com.serviceplus.metadata.dto;

import java.io.Serializable;
import java.util.List;

public class ServerSidePaginationRecord<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> data;

    private long totalRecords;

    private int currentPage;

    private int pageSize;

    private int totalPages;

    public ServerSidePaginationRecord() {
    }

    public ServerSidePaginationRecord(List<T> data, long totalRecords, int currentPage, int pageSize, int totalPages) {
        this.data = data;
        this.totalRecords = totalRecords;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
