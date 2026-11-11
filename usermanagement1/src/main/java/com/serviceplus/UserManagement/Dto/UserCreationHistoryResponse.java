package com.serviceplus.UserManagement.Dto;

import java.time.Instant;

public class UserCreationHistoryResponse {

    private Long historyId;

    private String createdByName;

    private Instant createdOn;

    private String roleName;

    private String entityName;

    private String entityLevelName;

    private Integer totalUsers;

    public UserCreationHistoryResponse(){

    }

    public UserCreationHistoryResponse(Long historyId,
                                       String createdByName,
                                       Instant createdOn,
                                       String roleName,
                                       String entityName,
                                       String entityLevelName,
                                       Integer totalUsers) {

        this.historyId = historyId;
        this.createdByName = createdByName;
        this.createdOn = createdOn;
        this.roleName = roleName;
        this.entityName = entityName;
        this.entityLevelName = entityLevelName;
        this.totalUsers = totalUsers;
    }

    public String getEntityLevelName() {
        return entityLevelName;
    }

    public void setEntityLevelName(String entityLevelName) {
        this.entityLevelName = entityLevelName;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Integer getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Integer totalUsers) {
        this.totalUsers = totalUsers;
    }
}
