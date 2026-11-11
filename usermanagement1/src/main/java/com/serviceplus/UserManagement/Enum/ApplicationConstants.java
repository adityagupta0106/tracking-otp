package com.serviceplus.UserManagement.Enum;


public enum ApplicationConstants {

	CENTRAL_STATE_ID(99),
	LGD_SCHEMA_NAME("lgd"),
	LINE_DEPARTMENT_TYPE_CODE(999),
	SP_SCHEMA_NAME("schm_sp"),
	PASS_LIST("abcdefghijklmnopqrstuvwxyz@#&%ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
	STATIC_AUTH_TOKEN("046a347d723b94c639fe19bbde2f1f5ce8b3ffd68c299fb75813bf766c508661")
	;
  
	
    private String action; 
    private Integer intV;
    
    public String getString() 
    { 
        return this.action; 
    } 
  
    private ApplicationConstants(String action) 
    { 
        this.action = action; 
    } 
    
    public Integer getInt() 
    { 
        return this.intV; 
    } 
  
    private ApplicationConstants(Integer intV) 
    { 
        this.intV = intV; 
    } 
    
}
