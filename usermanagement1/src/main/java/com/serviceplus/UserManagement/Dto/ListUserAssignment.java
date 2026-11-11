package com.serviceplus.UserManagement.Dto;

import java.io.Serializable;

public class ListUserAssignment implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String signNo;

    private String emailId;

    private Integer status;

    private Integer locationId;

    private String locationName;

    private Integer designationId;

    private String designationName;

    private String userName;

    public ListUserAssignment() {
    }

    public ListUserAssignment(
            Long userId,
            String signNo,
            String emailId,
            Integer status,
            Integer locationId,
            String locationName,
            Integer designationId,
            String designationName,
            String userName) {

        this.userId = userId;
        this.signNo = signNo;
        this.emailId = emailId;
        this.status = status;
        this.locationId = locationId;
        this.locationName = locationName;
        this.designationId = designationId;
        this.designationName = designationName;
        this.userName = userName;
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

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Integer getDesignationId() {
        return designationId;
    }

    public void setDesignationId(Integer designationId) {
        this.designationId = designationId;
    }

    public String getDesignationName() {
        return designationName;
    }

    public void setDesignationName(String designationName) {
        this.designationName = designationName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}