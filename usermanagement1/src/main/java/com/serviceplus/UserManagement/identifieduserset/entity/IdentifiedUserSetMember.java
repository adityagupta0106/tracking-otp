package com.serviceplus.UserManagement.identifieduserset.entity;

import com.serviceplus.UserManagement.Entity.BaseTenantEntity;
import com.serviceplus.UserManagement.util.ApplicationConstants;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "identified_user_set_members", schema = ApplicationConstants.SP_SCHEMA_NAME)
public class IdentifiedUserSetMember extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_set_id", nullable = false)
    private Long userSetId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "sign_no")
    private String signNo;

    @Column(name = "sso_provider_user_id")
    private String ssoProviderUserId;

    @Column(name = "membership_role")
    private String membershipRole;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "cr_by", nullable = false)
    private Long crBy;

    @Column(name = "u_by")
    private Long uBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "c_date")
    private Date cDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "u_date")
    private Date uDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserSetId() {
        return userSetId;
    }

    public void setUserSetId(Long userSetId) {
        this.userSetId = userSetId;
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

    public String getSsoProviderUserId() {
        return ssoProviderUserId;
    }

    public void setSsoProviderUserId(String ssoProviderUserId) {
        this.ssoProviderUserId = ssoProviderUserId;
    }

    public String getMembershipRole() {
        return membershipRole;
    }

    public void setMembershipRole(String membershipRole) {
        this.membershipRole = membershipRole;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getCrBy() {
        return crBy;
    }

    public void setCrBy(Long crBy) {
        this.crBy = crBy;
    }

    public Long getUBy() {
        return uBy;
    }

    public void setUBy(Long uBy) {
        this.uBy = uBy;
    }

    public Date getCDate() {
        return cDate;
    }

    public void setCDate(Date cDate) {
        this.cDate = cDate;
    }

    public Date getUDate() {
        return uDate;
    }

    public void setUDate(Date uDate) {
        this.uDate = uDate;
    }

    // Helper delegation methods
    public Long getCreatedBy() {
        return crBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.crBy = createdBy;
    }

    public Long getUpdatedBy() {
        return uBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.uBy = updatedBy;
    }

    public Date getCreatedOn() {
        return cDate;
    }

    public void setCreatedOn(Date createdOn) {
        this.cDate = createdOn;
    }

    public Date getUpdatedOn() {
        return uDate;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.uDate = updatedOn;
    }
}
