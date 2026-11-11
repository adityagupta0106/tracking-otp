package com.serviceplus.UserManagement.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.serviceplus.UserManagement.util.ApplicationConstants;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "cug_types", schema = ApplicationConstants.SP_SCHEMA_NAME)
public class CugTypes  extends BaseTenantEntity{

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    @Column(name="name")
    private String  name;

    @Column(name = "created_date")
    private Date createdOn;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "type")
    private String type;

    @Column(name = "active")
    private Boolean active;

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
