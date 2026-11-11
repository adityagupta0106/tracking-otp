package com.serviceplus.UserManagement.identifieduserset.dto;

public class UserSearchRequest {

    private String query;

    public UserSearchRequest() {
    }

    public UserSearchRequest(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
