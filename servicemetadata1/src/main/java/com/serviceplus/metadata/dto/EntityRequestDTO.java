package com.serviceplus.metadata.dto;

public class EntityRequestDTO {

	private Long entityId;
	private Long entityCode;

	public EntityRequestDTO() {
	}

	public EntityRequestDTO(Long entityId) {
		this.entityId = entityId;
		this.entityCode = entityId;
	}

	public Long getEntityId() {
		return entityId != null ? entityId : entityCode;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
		if (this.entityCode == null) {
			this.entityCode = entityId;
		}
	}

	public Long getEntityCode() {
		return entityCode != null ? entityCode : entityId;
	}

	public void setEntityCode(Long entityCode) {
		this.entityCode = entityCode;
		if (this.entityId == null) {
			this.entityId = entityCode;
		}
	}
}
