package com.serviceplus.UserManagement.Entity;

import com.serviceplus.UserManagement.util.ApplicationConstants;

import jakarta.persistence.*;

@Entity
@Table(name = "user_location",  schema = ApplicationConstants.SP_SCHEMA_NAME)
public class UserLocation extends BaseTenantEntity{

	@Id
	@Column(name = "user_loc_id", unique = true, nullable = false)
	private Long userLocId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "user_id",
            insertable = false,
            updatable = false)
    private LoginDetails user;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "location_id")
	private Integer locationId;

	@Column(name = "category_id")
	private Integer categoryId;

	@Column(name = "entity_id")
	private Integer entityId;

    @Column(name = "entity_level_id")
    private Integer entityLevelId;

	@Column(name = "entity_level_name")
	private String entityLevelName;
	
	@Column(name = "location_name")
	private String locationName;

	public Long getUserLocId() {
		return userLocId;
	}

	public void setUserLocId(Long userLocId) {
		this.userLocId = userLocId;
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

    public LoginDetails getUser() {
        return user;
    }

    public void setUser(LoginDetails user) {
        this.user = user;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
}
