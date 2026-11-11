package com.serviceplus.UserManagement.Dto;

import jakarta.persistence.*;

import java.time.Instant;

import static com.serviceplus.UserManagement.util.ApplicationConstants.SP_SCHEMA_NAME;

@Entity
@Table(name = "user_creation_history", schema = SP_SCHEMA_NAME)
public class UserCreationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_by_name")
    private String createdByName;

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "role_name")
    private String roleName;

    @Column(name = "entity_id")
    private Integer entityId;

    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "entity_level_id")
    private Integer entityLevelId;

    @Column(name = "entity_level_name")
    private String entityLevelName;

    @Column(name = "total_users")
    private Integer totalUsers;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_content", columnDefinition = "bytea")
    private byte[] fileContent;

    @Column(name = "created_on")
    private Instant createdOn;

    @Column(name = "tenant_id")
    private String tenantId;

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
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

    public Integer getEntityLevelId() {
        return entityLevelId;
    }

    public void setEntityLevelId(Integer entityLevelId) {
        this.entityLevelId = entityLevelId;
    }

    public String getEntityLevelName() {
        return entityLevelName;
    }

    public void setEntityLevelName(String entityLevelName) {
        this.entityLevelName = entityLevelName;
    }

    public Integer getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Integer totalUsers) {
        this.totalUsers = totalUsers;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
