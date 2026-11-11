package com.serviceplus.UserManagement.Dto;

import java.util.List;

public class LGDCommonDto {

	private String stateNameEnglish;
	private String stateNameLocal;
	private String orgLevelSpecName;
	private String orgUnitName;
	private List<LocalBodyData> localbody;
	private List<LineDepartmentData> ldData;

	public LGDCommonDto() {
		
	}
	
	public LGDCommonDto(List<LocalBodyData> localbody,List<LineDepartmentData> ldData) {
		this.localbody = localbody;
		this.ldData = ldData;
	}
	
	public static class LineDepartmentData{
		
		private Integer orgUnitCode;
		private String orgUnitName;
		private String parentOrgUnitNameEnglish;
		
		public Integer getOrgUnitCode() {
			return orgUnitCode;
		}
		public void setOrgUnitCode(Integer orgUnitCode) {
			this.orgUnitCode = orgUnitCode;
		}
		public String getOrgUnitName() {
			return orgUnitName;
		}
		public void setOrgUnitName(String orgUnitName) {
			this.orgUnitName = orgUnitName;
		}
		public String getParentOrgUnitNameEnglish() {
			return parentOrgUnitNameEnglish;
		}
		public void setParentOrgUnitNameEnglish(String parentOrgUnitNameEnglish) {
			this.parentOrgUnitNameEnglish = parentOrgUnitNameEnglish;
		}
		
	}
	
	public static class LocalBodyData{
		
		private Integer localBodyCode;
		private String localBodyNameEnglish;
		
		public Integer getLocalBodyCode() {
			return localBodyCode;
		}
		public void setLocalBodyCode(Integer localBodyCode) {
			this.localBodyCode = localBodyCode;
		}
		public String getLocalBodyNameEnglish() {
			return localBodyNameEnglish;
		}
		public void setLocalBodyNameEnglish(String localBodyNameEnglish) {
			this.localBodyNameEnglish = localBodyNameEnglish;
		}
		
	}

	public String getStateNameEnglish() {
		return stateNameEnglish;
	}

	public void setStateNameEnglish(String stateNameEnglish) {
		this.stateNameEnglish = stateNameEnglish;
	}

	public String getStateNameLocal() {
		return stateNameLocal;
	}

	public void setStateNameLocal(String stateNameLocal) {
		this.stateNameLocal = stateNameLocal;
	}


	public List<LineDepartmentData> getLdData() {
		return ldData;
	}

	public void setLdData(List<LineDepartmentData> ldData) {
		this.ldData = ldData;
	}

	public String getOrgLevelSpecName() {
		return orgLevelSpecName;
	}

	public void setOrgLevelSpecName(String orgLevelSpecName) {
		this.orgLevelSpecName = orgLevelSpecName;
	}

	public List<LocalBodyData> getLocalbody() {
		return localbody;
	}

	public void setLocalbody(List<LocalBodyData> localbody) {
		this.localbody = localbody;
	}

	public String getOrgUnitName() {
		return orgUnitName;
	}

	public void setOrgUnitName(String orgUnitName) {
		this.orgUnitName = orgUnitName;
	}
}
