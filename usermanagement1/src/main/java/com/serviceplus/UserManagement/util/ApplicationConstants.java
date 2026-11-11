package com.serviceplus.UserManagement.util;

public class ApplicationConstants {

	public static final String LGD_SCHEMA_NAME = "lgd";
	public static final String SP_SCHEMA_NAME = "schm_sp";	
	public static final String SP_Custom_NAME = "sp_custom";
	
	public static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
	
	//accepts 10 digit mobile phone no. with code eg.1234567898
	public static final String PHONE_PATTERN = "\\d{10}";
	public static final int DEFINER_ROLEID = 9;
	public static final String DEFINER_ROLENAME = "Service Definer";
	public static final int DEPARTMENT_ADMIN = 18;
	public static final String DEPARTMENT_ADMIN_ROLENAME = "Department Admin";
	public static final int STATE_GOVERNMENT_ADMIN = 20;
	public static final String STATE_GOVERNMENT_ADMIN_ROLENAME = "State Government Admin";
	public static final String AUTH_SESSION_HEADER = "Atk";
	public static final String PRESID_HEADER = "Presid";
	
	public static final Integer LINE_DEPARTMENT_TYPE_CODE=999;
	public static final Integer LOCATION_TYPE_CODE_STATE=91;
	public static final Integer CONSTITUENCY_TYPE_CODE=998;
	//public static final Integer ROLE_SGA=20;
	public static final int ROLE_DA=18;
	public static final int ROLE_DEFINER=9;
	public static final int ROLE_WORK_FLOW_PLAYER=5;
	public static final int ROLE_SCA=19;
    public static final int ROLE_CREDENTIAL_ADMIN=93;
	public static final Integer TYPE_CENTRAL_USER=6;
	public static final Integer FALLBACK_CLC_CODE =0;
	public static final Integer REDIS_KEY_EXIPRATION_DURATION_IN_MINUTES = 360;
	public static final int ENTITY_LEVEL_TOP = 1;
    final public static String HOST_HEADER = "SP-Client-Domain";
    public static final int ROLE_TENANT_ADMIN=20;
    public static final int ROLE_INSTANCE_ADMIN=0;
    public static final int ROLE_SPGD_ADMIN=102;
    public static final Integer LOCATION_TYPE_CODE_TOP=91;
    public static final int ROLE_EXTERNAL_APP_DEO=29;
    public static final int ROLE_EXECUTIVE = 101;
    public static final int ROLE_CITIZEN = 24;
    public static final int ROLE_CUG = 7;
    public static final int ROLE_IDENTIFIED_USER = 7;
    public static final String IDENTIFIED_USER_ROLENAME = "Identified User";
    public static final int CUG_TYPE_CSC_KIOSK = 1;



    public static final String OUTBOX_TOKEN_PREFIX="OB";
    public static final String OUTBOX_STATUS_PENDING="PENDING";
    public static final String OUTBOX_STATUS_PROCESSED="PROCESSED";
    public static final String TENANT_USER_SIGN_PREFIX="TN";
    public static final String DEFAULT_USER_SIGN_PREFIX="SP";
}
