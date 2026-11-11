package com.serviceplus.UserManagement.Entity;

import com.serviceplus.UserManagement.util.ApplicationConstants;

import jakarta.persistence.*;

@Entity
@Table(name = "user_location_designation", schema = ApplicationConstants.SP_SCHEMA_NAME)
public class UserLocationDesignation extends BaseTenantEntity{

	@Id
	@Column(name = "user_loc_desig_id", unique = true,   nullable = false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long userLocationDesignationId;
	
	@Column(name = "designation_id")
	private Integer designationId;
	
	@Column(name = "designation_name")
	private String designationName;
	
	@Column(name = "user_loc_id")
	private Long userLocationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_loc_id",
            referencedColumnName = "user_loc_id",
            insertable = false,
            updatable = false)
    private UserLocation userLocation;

	public Long getUserLocationDesignationId() {
		return userLocationDesignationId;
	}
	public void setUserLocationDesignationId(Long userLocationDesignationId) {
		this.userLocationDesignationId = userLocationDesignationId;
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
	public Long getUserLocationId() {
		return userLocationId;
	}
	public void setUserLocationId(Long userLocationId) {
		this.userLocationId = userLocationId;
	}

    public UserLocation getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }
}
