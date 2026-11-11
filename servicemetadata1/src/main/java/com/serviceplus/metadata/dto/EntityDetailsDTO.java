package com.serviceplus.metadata.dto;

public class EntityDetailsDTO {

	private Long entityCode;
	private String entityName;
	private String entityLocalName;
	private Integer clc;
	private Integer entityTypeCode;
	

	public EntityDetailsDTO() {
	}

	public Long getEntityCode() {
		return entityCode;
	}


	public void setEntityCode(Long entityCode) {
		this.entityCode = entityCode;
	}

	public String getEntityName() {
		return entityName;
	}


	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}


	public String getEntityLocalName() {
		return entityLocalName;
	}


	public void setEntityLocalName(String entityLocalName) {
		this.entityLocalName = entityLocalName;
	}


	public Integer getClc() {
		return clc;
	}


	public void setClc(Integer clc) {
		this.clc = clc;
	}


	public Integer getEntityTypeCode() {
		return entityTypeCode;
	}


	public void setEntityTypeCode(Integer entityTypeCode) {
		this.entityTypeCode = entityTypeCode;
	}

	
}
