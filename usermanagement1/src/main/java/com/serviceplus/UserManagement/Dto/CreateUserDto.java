package com.serviceplus.UserManagement.Dto;

import java.io.Serializable;
import java.util.List;

public class CreateUserDto implements Serializable{

    private Integer createdRole;
    private Integer userCount;
    private Boolean approvalRequired;
    private Integer categoryId;
    private Integer clcId;
    private Integer entityId;
    private Integer levelId;
    private List<SpecificLocation> unitId;
    private Integer parentUnitId;
    private Integer allOrSpecific;
    private Integer designationId;
    private String cugTypes;
    transient private String entityName;
    transient private String entityLevelName;
    private String referenceId;

    public static class SpecificLocation{
        private Integer entityUnitCode;
        private String entityUnitName;
        private String parentEntityUnitName;

        public Integer getEntityUnitCode() {
            return entityUnitCode;
        }

        public void setEntityUnitCode(Integer entityUnitCode) {
            this.entityUnitCode = entityUnitCode;
        }

        public String getEntityUnitName() {
            return entityUnitName;
        }

        public void setEntityUnitName(String entityUnitName) {
            this.entityUnitName = entityUnitName;
        }

        public String getParentEntityUnitName() {
            return parentEntityUnitName;
        }

        public void setParentEntityUnitName(String parentEntityUnitName) {
            this.parentEntityUnitName = parentEntityUnitName;
        }
    }



    public Integer getCreatedRole() {
        return createdRole;
    }

    public void setCreatedRole(Integer createdRole) {
        this.createdRole = createdRole;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public Boolean getApprovalRequired() {
        return approvalRequired;
    }

    public void setApprovalRequired(Boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getClcId() {
        return clcId;
    }

    public void setClcId(Integer clcId) {
        this.clcId = clcId;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public Integer getLevelId() {
        return levelId;
    }

    public void setLevelId(Integer levelId) {
        this.levelId = levelId;
    }

    public List<SpecificLocation> getUnitId() {
        return unitId;
    }

    public void setUnitId(List<SpecificLocation> unitId) {
        this.unitId = unitId;
    }

    public Integer getParentUnitId() {
        return parentUnitId;
    }

    public void setParentUnitId(Integer parentUnitId) {
        this.parentUnitId = parentUnitId;
    }

    public Integer getAllOrSpecific() {
        return allOrSpecific;
    }

    public void setAllOrSpecific(Integer allOrSpecific) {
        this.allOrSpecific = allOrSpecific;
    }

    public Integer getDesignationId() {
        return designationId;
    }

    public void setDesignationId(Integer designationId) {
        this.designationId = designationId;
    }

    public String getCugTypes() {
        return cugTypes;
    }

    public void setCugTypes(String cugTypes) {
        this.cugTypes = cugTypes;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityLevelName() {
        return entityLevelName;
    }

    public void setEntityLevelName(String entityLevelName) {
        this.entityLevelName = entityLevelName;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
}