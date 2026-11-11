package com.serviceplus.UserManagement.Dto;

import java.util.List;

public class BatchApiRequestDto {

	private List<Integer> batch_ids;

	public BatchApiRequestDto(){
		
	}
	
	public BatchApiRequestDto(List<Integer> batch_ids){
		this.batch_ids = batch_ids;
	}
	
	public List<Integer> getBatch_ids() {
		return batch_ids;
	}

	public void setBatch_ids(List<Integer> batch_ids) {
		this.batch_ids = batch_ids;
	}
	
}
