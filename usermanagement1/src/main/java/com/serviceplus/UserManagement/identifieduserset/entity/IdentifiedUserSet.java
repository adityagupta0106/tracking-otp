package com.serviceplus.UserManagement.identifieduserset.entity;

import com.serviceplus.UserManagement.Entity.BaseTenantEntity;
import com.serviceplus.UserManagement.util.ApplicationConstants;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "identified_user_sets", schema = ApplicationConstants.SP_SCHEMA_NAME)
public class IdentifiedUserSet extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
