package com.serviceplus.UserManagement.Dto;

import com.serviceplus.UserManagement.Dto.CreateUserDto.SpecificLocation;

import java.util.Date;

public class UserProvisioningRequest {

    private Integer clcId;

    private String clcName;

    private Long createdByUserId;

    private Integer categoryId;

    private Integer entityId;

    private String entityName;

    private Integer levelId;

    private String levelName;

    private Integer parentUnitId;

    private Integer roleId;

    private SpecificLocation location;

    private Integer designationId;

    private String kioskType;

    private Boolean approvalRequired;

    private Integer initialStatus;

    private String sigNoPrefix;

    private String signNo;

    private Boolean pushToSSO = Boolean.TRUE;

    private UserProfile userProfile;

    private String tenantId;

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public static class UserProfile {

        private String userName;

        private String firstName;

        private String middleName;

        private String lastName;

        private String emailId;

        private String mobileNo;

        private Date dob;

        private String address;

        private String postalCode;

        private Integer cugType;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getMiddleName() {
            return middleName;
        }

        public void setMiddleName(String middleName) {
            this.middleName = middleName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmailId() {
            return emailId;
        }

        public void setEmailId(String emailId) {
            this.emailId = emailId;
        }

        public String getMobileNo() {
            return mobileNo;
        }

        public void setMobileNo(String mobileNo) {
            this.mobileNo = mobileNo;
        }

        public Date getDob() {
            return dob;
        }

        public void setDob(Date dob) {
            this.dob = dob;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        public Integer getCugType() {
            return cugType;
        }

        public void setCugType(Integer cugType) {
            this.cugType = cugType;
        }
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Integer getClcId() {
        return clcId;
    }

    public void setClcId(Integer clcId) {
        this.clcId = clcId;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Integer getLevelId() {
        return levelId;
    }

    public void setLevelId(Integer levelId) {
        this.levelId = levelId;
    }

    public Integer getParentUnitId() {
        return parentUnitId;
    }

    public void setParentUnitId(Integer parentUnitId) {
        this.parentUnitId = parentUnitId;
    }

    public SpecificLocation getLocation() {
        return location;
    }

    public void setLocation(SpecificLocation location) {
        this.location = location;
    }

    public Integer getDesignationId() {
        return designationId;
    }

    public void setDesignationId(Integer designationId) {
        this.designationId = designationId;
    }

    public String getKioskType() {
        return kioskType;
    }

    public void setKioskType(String kioskType) {
        this.kioskType = kioskType;
    }

    public Boolean getApprovalRequired() {
        return approvalRequired;
    }

    public void setApprovalRequired(Boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }

    public Integer getInitialStatus() {
        return initialStatus;
    }

    public void setInitialStatus(Integer initialStatus) {
        this.initialStatus = initialStatus;
    }

    public String getClcName() {
        return clcName;
    }

    public void setClcName(String clcName) {
        this.clcName = clcName;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getSigNoPrefix() {
        return sigNoPrefix;
    }

    public void setSigNoPrefix(String sigNoPrefix) {
        this.sigNoPrefix = sigNoPrefix;
    }

    public String getSignNo() {
        return signNo;
    }

    public void setSignNo(String signNo) {
        this.signNo = signNo;
    }

    public Boolean getPushToSSO() {
        return pushToSSO;
    }

    public void setPushToSSO(Boolean pushToSSO) {
        this.pushToSSO = pushToSSO;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    private String ssoProviderId;

    public String getSsoProviderId() {
        return ssoProviderId;
    }

    public void setSsoProviderId(String ssoProviderId) {
        this.ssoProviderId = ssoProviderId;
    }

    public String getSsoProviderUserId() {
        return ssoProviderId;
    }

    public void setSsoProviderUserId(String ssoProviderUserId) {
        this.ssoProviderId = ssoProviderUserId;
    }
}
