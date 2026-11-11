package com.serviceplus.UserManagement.Entity;

import java.util.Date;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.serviceplus.UserManagement.util.ApplicationConstants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_profile", schema = ApplicationConstants.SP_SCHEMA_NAME)
public class UserProfile  extends BaseTenantEntity{

	@Id
	@Column(name = "user_id")
	private Long userId;
	
	@Column(name = "email_id")
	private String emailId;
	
	@Column(name = "mobile_no")
	private String mobileNo;
	
	@Column(name = "designation_id")
	private Integer designationId;
	
	@Column(name = "user_preference")
	private Integer userPreference;

	@Column(name = "clc_id")
	private Integer clcId;
	
	@Column(name = "user_name")
	private String userName;
	
	@Column(name = "clc_name")
	private String clcName;
	
	@Column(name = "cug_type")
	private Integer cugType;
	
	@Column(name = "approval_required")
	private Boolean approvalRequired;
	
	@Column(name = "middle_name")
	private String middleName;
	
	@Column(name = "last_name")
	private String lastName;
	
	@Column(name = "dob")
	private Date dob;
	
	@Column(name = "address")
	private String address;
	
	@Column(name = "postal_code")
	private String postalCode;
	
	@JdbcTypeCode(SqlTypes.VARBINARY)
	@Column(name = "profile_image")
	private byte[] profilePic;
	
	@Column(name = "c_date")
	private Date cDate;

	@Column(name = "u_date")
	private Date uDate;
	
	// Constructor Only used For Graph API response START
	public UserProfile(Long userId, String emailId, String mobileNo, Integer clcId, String userName, String middleName,
			String lastName, Date dob, String address, String postalCode, byte[] profilePic, Date cDate, Date uDate) {
        if (userId != null) this.userId = userId;
        if (emailId != null) this.emailId = emailId;
        if (mobileNo != null) this.mobileNo = mobileNo;
        if (clcId != null) this.clcId = clcId;
        if (userName != null) this.userName = userName;
        if (middleName != null) this.middleName = middleName;
        if (lastName != null) this.lastName = lastName;
        if (dob != null) this.dob = dob;
        if (address != null) this.address = address;
        if (postalCode != null) this.postalCode = postalCode;
        if (profilePic != null) this.profilePic = profilePic;
        if (cDate != null) this.cDate = cDate;
        if (uDate != null) this.uDate = uDate;
    }
	// Constructor Only used For Graph API response END
	
	public UserProfile(Long userId) {
		super();
		this.userId = userId;
	}

	public UserProfile(Long userId, String emailId, String mobileNo, Integer designationId, Integer userPreference) {
		super();
		this.userId = userId;
		this.emailId = emailId;
		this.mobileNo = mobileNo;
		this.designationId = designationId;
		this.userPreference = userPreference;
	}
	
	public UserProfile() {
		super();
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

	public Integer getDesignationId() {
		return designationId;
	}

	public void setDesignationId(Integer designationId) {
		this.designationId = designationId;
	}

	public Integer getUserPreference() {
		return userPreference;
	}

	public void setUserPreference(Integer userPreference) {
		this.userPreference = userPreference;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Boolean getApprovalRequired() {
		return approvalRequired;
	}

	public void setApprovalRequired(Boolean approvalRequired) {
		this.approvalRequired = approvalRequired;
	}

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getCugType() {
        return cugType;
    }

    public void setCugType(Integer cugType) {
        this.cugType = cugType;
    }

    public String getClcName() {
        return clcName;
    }

    public void setClcName(String clcName) {
        this.clcName = clcName;
    }

    public Integer getClcId() {
        return clcId;
    }

    public void setClcId(Integer clcId) {
        this.clcId = clcId;
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

	public byte[] getProfilePic() {
		return profilePic;
	}

	public void setProfilePic(byte[] profilePic) {
		this.profilePic = profilePic;
	}

	public Date getcDate() {
		return cDate;
	}

	public void setcDate(Date cDate) {
		this.cDate = cDate;
	}

	public Date getuDate() {
		return uDate;
	}

	public void setuDate(Date uDate) {
		this.uDate = uDate;
	}
    
}
