-- DROP TABLE IF EXISTS schm_sp.admin_user_creation_access;

CREATE TABLE IF NOT EXISTS schm_sp.admin_user_creation_access
(
    id bigint NOT NULL DEFAULT nextval('schm_sp.admin_user_creation_access_id_seq'::regclass),
    logged_in_role integer,
    can_create_role integer,
    CONSTRAINT admin_user_creation_access_pkey PRIMARY KEY (id)
)


-- DROP TABLE IF EXISTS schm_sp.kiosk_types;

CREATE TABLE IF NOT EXISTS schm_sp.kiosk_types
(
    id bigint NOT NULL DEFAULT nextval('schm_sp.kiosk_types_id_seq'::regclass),
    name character varying(100) COLLATE pg_catalog."default" NOT NULL,
    state_id integer NOT NULL,
    created_by integer,
    created_date timestamp with time zone,
    active boolean,
    tenant_id character varying,
    CONSTRAINT kiosk_type_unique UNIQUE (name, state_id)
)

-- DROP TABLE IF EXISTS schm_sp.login_details;

CREATE TABLE IF NOT EXISTS schm_sp.login_details
(
    user_id bigint NOT NULL DEFAULT nextval('schm_sp.login_details_user_id_seq'::regclass),
    sign_no character varying COLLATE pg_catalog."default",
    passwd character varying COLLATE pg_catalog."default",
    sign_role character varying COLLATE pg_catalog."default",
    spdi_status integer,
    c_date timestamp without time zone,
    created_by integer,
    state_id integer,
    is_sga boolean,
    is_top_level_admin boolean,
    tenant_id character varying COLLATE pg_catalog."default",
    CONSTRAINT login_details_pkey PRIMARY KEY (user_id)
)

-- DROP TABLE IF EXISTS schm_sp.role_assignment;

CREATE TABLE IF NOT EXISTS schm_sp.role_assignment
(
    user_id integer NOT NULL,
    role_id integer NOT NULL,
    user_role_id bigint NOT NULL DEFAULT nextval('schm_sp.role_assignment_user_role_id_seq'::regclass),
    user_loc_id bigint,
    tenant_id character varying,
    CONSTRAINT pk_role_assignment PRIMARY KEY (user_role_id),
    CONSTRAINT uk_role_ass_loc UNIQUE (user_id, role_id, user_loc_id)
)

-- DROP TABLE IF EXISTS schm_sp.role_master;

CREATE TABLE IF NOT EXISTS schm_sp.role_master
(
    role_id integer NOT NULL,
    role_name character varying(100) COLLATE pg_catalog."default",
    sign_role character varying(10) COLLATE pg_catalog."default"
)


-- DROP TABLE IF EXISTS schm_sp.spe_user_sequence;

CREATE TABLE IF NOT EXISTS schm_sp.spe_user_sequence
(
    spdi_state_id integer NOT NULL,
    spdv_state_abbr character varying(2) COLLATE pg_catalog."default" NOT NULL,
    spdi_sequence_no integer,
    CONSTRAINT pk_user_seq PRIMARY KEY (spdi_state_id)
)

-- DROP TABLE IF EXISTS schm_sp.user_location;

CREATE TABLE IF NOT EXISTS schm_sp.user_location
(
    user_loc_id bigserial,
    user_id integer,
    location_id integer,
    location_type_id integer,
    department_id integer,
    department_level_id integer,
    department_level_name character varying(200) COLLATE pg_catalog."default",
    location_name character varying(300) COLLATE pg_catalog."default",
    CONSTRAINT pk_user_loc_id PRIMARY KEY (user_loc_id),
    CONSTRAINT uq_user_loc UNIQUE (user_id, location_id, location_type_id)
)

-- DROP TABLE IF EXISTS schm_sp.user_location_designation;

CREATE TABLE IF NOT EXISTS schm_sp.user_location_designation
(
    user_loc_desig_id bigserial,
    designation_id integer,
    designation_name character varying(200) COLLATE pg_catalog."default",
    user_loc_id bigint,
    CONSTRAINT pk_user_loc_desig PRIMARY KEY (user_loc_desig_id),
    CONSTRAINT uq_userloc_desg UNIQUE (user_loc_id, designation_id)
)

-- DROP TABLE IF EXISTS schm_sp.user_profile;

CREATE TABLE IF NOT EXISTS schm_sp.user_profile
(
    user_id integer NOT NULL,
    email_id character varying COLLATE pg_catalog."default",
    mobile_no character varying COLLATE pg_catalog."default",
    designation_id integer,
    user_preference integer,
    state_id integer,
    user_name character varying COLLATE pg_catalog."default",
    state_name character varying(25) COLLATE pg_catalog."default",
    kiosk_type character varying(100) COLLATE pg_catalog."default",
    approval_required boolean,
    CONSTRAINT user_profile_pkey PRIMARY KEY (user_id)
)

-- DROP TABLE IF EXISTS schm_sp.user_pwd_reset_link;

CREATE TABLE IF NOT EXISTS schm_sp.user_pwd_reset_link
(
    token character varying COLLATE pg_catalog."default",
    user_id integer NOT NULL,
    creation_time timestamp without time zone,
    last_acces_time timestamp without time zone,
    access_count integer,
    attempt_count integer,
    last_hit_ip_address character varying COLLATE pg_catalog."default",
    is_active boolean,
    CONSTRAINT user_pwd_reset_link_pkey PRIMARY KEY (user_id)
);
--added semicolon
---------------------FUNCTIONS----------------------------------

-- DROP FUNCTION IF EXISTS schm_sp.fnc_generate_seq(integer);

CREATE OR REPLACE FUNCTION schm_sp.fnc_generate_seq(
	tabcode integer)
    RETURNS numeric
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$

declare
	lastid numeric;
	update_count numeric;
	db_link text:= '';
begin

	IF $1 = 11 THEN
		select nextval('schm_sp.appln_details') into lastid;
	ELSIF $1 = 77 THEN
		select nextval('schm_sp.appln_current_process') into lastid;
	ELSIF $1 = 80 THEN
		select nextval('schm_sp.appl_fieldset_detail') into lastid;
	ELSIF $1 = 73 THEN
		select nextval('schm_sp.off_param_detail') into lastid;
	ELSIF $1 = 109 THEN

	select 'dbname='||db_name||' port='||port||' host='||host||' user='||user_name||' password='||db_password from schm_sp.dblink_details a into db_link;

		SELECT * FROM dblink(db_link,'select nextval(''schm_sp.user_location_designation_user_loc_desig_id_seq'')') as cte(nextval bigint) into lastid;

	ELSIF $1 = 111 THEN
		select nextval('schm_sp.application_detail_pages_id_seq') into lastid;
	ELSIF $1 = 114 THEN
		select nextval('schm_sp.esign_seq') into lastid;
	ELSIF $1 = 115 THEN
		select nextval('schm_sp.uid_seq') into lastid;
	ELSIF $1 = 124 THEN
		select nextval('schm_sp.rem_notification_id_seq') into lastid;
	ELSIF $1 = 26 THEN
		select nextval('schm_sp.citizen_document_id_seq') into lastid;
	ELSIF $1 = 16 THEN
		select nextval('schm_sp.user_registration_id_seq') into lastid;

	ELSIF $1 = 102 THEN
		select nextval('schm_sp.cert_listbox_details_id_seq') into lastid;
	ELSIF $1 = 125 THEN
		select nextval('schm_sp.hibernate_sequence') into lastid;
	ELSIF $1 = 66 THEN

	select 'dbname='||db_name||' port='||port||' host='||host||' user='||user_name||' password='||db_password from schm_sp.dblink_details a into db_link;

		SELECT * FROM dblink(db_link,'select nextval(''schm_sp.mdds_address_id_seq'')') as cte(nextval bigint) into lastid;
	ELSE
		select "spdi_last_num" into lastid from "schm_sp"."spe_sequence_last_no" where "spdi_sequence_id"=tabcode for update of "spe_sequence_last_no";

		if lastid is null then
			lastid:=0;
		end if;

		lastid=lastid+1;

		update "schm_sp"."spe_sequence_last_no" set "spdi_last_num"=lastid where "spdi_sequence_id"=tabcode;

		GET DIAGNOSTICS update_count=ROW_COUNT;

		if update_count=0 then

			insert into "schm_sp"."spe_sequence_last_no" values (tabcode, 'System Generated', 1, 'system', current_date);

		end if;


	END IF;
        return lastid;
end;
$BODY$;

ALTER FUNCTION schm_sp.fnc_generate_seq(integer)
    OWNER TO postgres;



-- DROP FUNCTION IF EXISTS schm_sp.fnc_generate_user_id(integer);

CREATE OR REPLACE FUNCTION schm_sp.fnc_generate_user_id(
	state_id integer)
    RETURNS text
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
DECLARE
lastNo Text;
stateAbbr Text;
update_count numeric;
BEGIN

   IF state_id = 99 THEN
stateAbbr = 'CN' ;
   ELSE
stateAbbr = (select short_name from lgd.state where state_code = $1 and isactive='true');
   END IF;

        select max(cast (spdi_sequence_no as numeric)) +1 into
lastNo from schm_sp.spe_user_sequence where spdi_state_id = $1 and

spdv_state_abbr=stateAbbr;

if lastNo is null then
lastNo:='1';
END IF;

update schm_sp.spe_user_sequence set

spdi_sequence_no=cast (lastNo as numeric) where spdi_state_id = $1 and

spdv_state_abbr=stateAbbr;

GET DIAGNOSTICS update_count=ROW_COUNT;

if update_count=0 then

insert into schm_sp.spe_user_sequence values (state_id, stateAbbr, cast (lastNo as numeric));

end if;
IF lastNo:: integer <= 999999 THEN
        RETURN  stateAbbr || lpad(lastNo,6,'0');
ELSE
RETURN  stateAbbr || lastNo;
END IF;
END;
$BODY$;

ALTER FUNCTION schm_sp.fnc_generate_user_id(integer)
    OWNER TO postgres;


---------------------------SEQUENCES-----------------------------------------

-- DROP SEQUENCE IF EXISTS schm_sp.user_location_user_loc_id_seq;

CREATE SEQUENCE IF NOT EXISTS schm_sp.user_location_user_loc_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;


-- DROP SEQUENCE IF EXISTS schm_sp.user_registration_id_seq;

CREATE SEQUENCE IF NOT EXISTS schm_sp.user_registration_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;


------------------------TEMPORARY TO BE REMOVED---------------------------------

-- DROP TABLE IF EXISTS lgd.state;

CREATE TABLE IF NOT EXISTS lgd.state
(
    state_code integer NOT NULL,
    state_version integer NOT NULL,
    state_name_english character varying(50) COLLATE pg_catalog."default" NOT NULL,
    state_name_local character varying(50) COLLATE pg_catalog."default",
    short_name character(2) COLLATE pg_catalog."default",
    alias_english character varying(50) COLLATE pg_catalog."default",
    alias_local character varying(50) COLLATE pg_catalog."default",
    census_2001_code character(2) COLLATE pg_catalog."default",
    census_2011_code character(2) COLLATE pg_catalog."default",
    state_or_ut character(1) COLLATE pg_catalog."default" NOT NULL,
    lr_replaces integer,
    map_attachment_code integer,
    effective_date timestamp with time zone NOT NULL,
    lastupdated timestamp with time zone,
    lastupdatedby bigint,
    createdon timestamp with time zone NOT NULL,
    createdby bigint NOT NULL,
    isactive boolean NOT NULL DEFAULT true,
    lr_replacedby integer,
    flag_code integer,
    islocked boolean NOT NULL DEFAULT false,
    transaction_id integer,
    slc integer,
    coordinates text COLLATE pg_catalog."default",
    warningflag boolean NOT NULL DEFAULT false,
    CONSTRAINT state_pkey PRIMARY KEY (state_code, state_version)
)

TABLESPACE pg_default;


------------------- POST NEW LGD CHANGES -------------------------


truncate schm_sp.role_master

INSERT INTO schm_sp.role_master (
    role_id,
    role_name,
    sign_role
)
VALUES
    (0, 'Instance Admin', NULL),
    (20, 'Tenant Admin', NULL),
    (93, 'Credential Admin', NULL),
    (18, 'Department Admin', NULL),
    (101, 'Executive', NULL),
    (19, 'SCA', NULL),
    (9, 'Service Definer', NULL),
    (102, 'SPGD Admin', NULL),
    (92, 'Credential Manager', NULL),
    (14, 'DEO', NULL),
    (3, 'DO', NULL),
    (5, 'WFP', NULL),
	(24, 'Citizen', NULL);



-- Instance Admin can create Tenant Admin
INSERT INTO schm_sp.admin_user_creation_access
(id, logged_in_role, can_create_role)
VALUES
(1, 0, 20);

-- Tenant Admin can create Credential Admin, Department Admin, Service Definer, SPGD Admin
INSERT INTO schm_sp.admin_user_creation_access
(id, logged_in_role, can_create_role)
VALUES
(3, 20, 18),
(5, 20, 102);

-- Department Admin can create Executive and SD
INSERT INTO schm_sp.admin_user_creation_access
(id, logged_in_role, can_create_role)
VALUES
(6, 18, 101),
(8, 18, 9),
(9,18,18),
(10,18,93),
(11,18,29)


alter table schm_sp.login_details drop column is_sga
alter table schm_sp.login_details add column if not exists tenant_id character varying;
alter table schm_sp.role_assignment add column if not exists tenant_id character varying;
alter table schm_sp.role_master add column if not exists tenant_id character varying;
alter table schm_sp.user_location add column if not exists tenant_id character varying;
alter table schm_sp.user_location_designation add column if not exists tenant_id character varying;
alter table schm_sp.admin_user_creation_access add column if not exists tenant_id character varying;
alter table schm_sp.user_profile add column if not exists tenant_id character varying;
alter table schm_sp.user_pwd_reset_link add column if not exists tenant_id character varying;
alter table schm_sp.kiosk_types add column if not exists tenant_id character varying;
ALTER TABLE lgd.state ALTER COLUMN short_name TYPE character varying(3);
ALTER TABLE schm_sp.spe_user_sequence ALTER COLUMN spdv_state_abbr TYPE character varying(3);


ALTER TABLE schm_sp.login_details DROP COLUMN IF EXISTS sign_role;
ALTER TABLE schm_sp.login_details DROP COLUMN IF EXISTS state_id;
ALTER TABLE schm_sp.login_details DROP COLUMN IF EXISTS is_top_level_admin;
ALTER TABLE schm_sp.login_details ADD COLUMN IF NOT EXISTS clc_id INTEGER;
ALTER TABLE schm_sp.login_details ALTER COLUMN created_by TYPE BIGINT;
ALTER TABLE schm_sp.login_details ADD CONSTRAINT uq_login_details_sign_no UNIQUE (sign_no);


CREATE SEQUENCE schm_sp.sign_no_seq
START WITH 1
INCREMENT BY 1
MINVALUE 1
CACHE 20;


ALTER TABLE schm_sp.user_profile RENAME COLUMN state_id TO clc_id;
ALTER TABLE schm_sp.user_profile RENAME COLUMN state_name TO clc_name;
ALTER TABLE schm_sp.user_profile RENAME COLUMN kiosk_type TO cug_type;
ALTER TABLE schm_sp.user_profile ALTER COLUMN user_id TYPE BIGINT;


ALTER TABLE schm_sp.user_location ALTER COLUMN user_id TYPE BIGINT;
ALTER TABLE schm_sp.user_location RENAME COLUMN location_type_id TO category_id;
ALTER TABLE schm_sp.user_location RENAME COLUMN department_id TO entity_id;
ALTER TABLE schm_sp.user_location RENAME COLUMN department_level_id TO entity_level_id;
ALTER TABLE schm_sp.user_location RENAME COLUMN department_level_name TO entity_level_name;

ALTER TABLE schm_sp.role_assignment ALTER COLUMN user_id TYPE BIGINT;

ALTER TABLE schm_sp.user_pwd_reset_link ALTER COLUMN user_id TYPE BIGINT;



ALTER TABLE schm_sp.kiosk_types RENAME COLUMN state_id TO clc_id;
ALTER TABLE schm_sp.kiosk_types ALTER COLUMN created_by TYPE BIGINT;


CREATE TABLE schm_sp.kafka_outbox_event (
    id CHARACTER VARYING PRIMARY KEY,
    topic CHARACTER VARYING NOT NULL,
    event_key CHARACTER VARYING,
    payload TEXT NOT NULL,
    status CHARACTER VARYING NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMPTZ
);


-- Start New Functionality : Modify Profile
ALTER TABLE IF EXISTS schm_sp.user_profile
    ADD COLUMN middle_name character varying;

ALTER TABLE IF EXISTS schm_sp.user_profile
    ADD COLUMN last_name character varying;

ALTER TABLE IF EXISTS schm_sp.user_profile
    ADD COLUMN dob timestamp without time zone;

ALTER TABLE IF EXISTS schm_sp.user_profile
    ADD COLUMN address character varying;

ALTER TABLE IF EXISTS schm_sp.user_profile
    ADD COLUMN postal_code character varying;

ALTER TABLE IF EXISTS schm_sp.user_profile
    ADD COLUMN profile_image character varying;

ALTER TABLE IF EXISTS schm_sp.user_profile
    ADD COLUMN c_date timestamp with time zone;

ALTER TABLE IF EXISTS schm_sp.user_profile
    ADD COLUMN u_date time with time zone;
    
-- End New Functionality : Modify Profile


----------------NEXT JULY-----------------

CREATE TABLE IF NOT EXISTS schm_sp.user_creation_history
(
    history_id BIGSERIAL PRIMARY KEY,
    created_by BIGINT NOT NULL,
    created_by_name CHARACTER VARYING,
    role_id INTEGER,
    role_name CHARACTER VARYING,
    entity_id INTEGER,
    entity_name CHARACTER VARYING,
    entity_level_id INTEGER,
    entity_level_name CHARACTER VARYING,
    total_users INTEGER,
    file_name CHARACTER VARYING,
    file_content BYTEA,
    created_on TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id CHARACTER VARYING NOT NULL
);

insert into schm_sp.role_master values(7,'Closed User Group')


CREATE TABLE schm_sp.cug_types
(
    id                  bigserial PRIMARY KEY,
    name                character varying NOT NULL,
    type                character varying NOT NULL,
    created_by          BIGINT NOT NULL,
    active              BOOLEAN,
    created_date        TIMESTAMP WITH TIME ZONE,
    tenant_id           character varying NOT NULL
);


INSERT INTO schm_sp.cug_types
(
    tenant_id,    name,    type,    created_date,    created_by,    active
)
VALUES
(
    '',    'CSC Kiosk',    'KIOSK',    CURRENT_TIMESTAMP,    0,    TRUE
);


UPDATE  schm_sp.user_profile set cug_type = null
ALTER TABLE schm_sp.user_profile ALTER COLUMN cug_type TYPE INTEGER USING cug_type::INTEGER;

CREATE TABLE IF NOT EXISTS schm_sp.digital_signature
(
    id bigserial,
    user_id bigint NOT NULL,
    certificate bytea,
    serial_number character varying COLLATE pg_catalog."default",
    subject_name character varying COLLATE pg_catalog."default",
    thumbprint character varying COLLATE pg_catalog."default",
    valid_from timestamp with time zone,
    valid_to timestamp with time zone,
    status character varying COLLATE pg_catalog."default" NOT NULL,
    cr_by bigint,
    cr_date timestamp with time zone,
    up_by bigint,
    up_date timestamp with time zone,
    tenant_id character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT digital_signature_pkey PRIMARY KEY (id)
);

-- ====================================================================
-- Identified User Set Management Module Tables
-- -- ====================================================================

ALTER TABLE schm_sp.login_details
ADD COLUMN IF NOT EXISTS sso_provider_user_id character varying;

CREATE INDEX IF NOT EXISTS idx_login_details_sso_provider_user_id
ON schm_sp.login_details (sso_provider_user_id);


CREATE TABLE IF NOT EXISTS schm_sp.identified_user_sets
(
    id bigserial NOT NULL,
    name character varying NOT NULL,
    description character varying,
    active boolean NOT NULL DEFAULT true,
    cr_by bigint NOT NULL,
    u_by bigint,
    c_date timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    u_date timestamp with time zone,
    tenant_id character varying NOT NULL,

    CONSTRAINT identified_user_sets_pkey PRIMARY KEY (id),
    CONSTRAINT uk_user_set_name_tenant UNIQUE (name, tenant_id)
);

CREATE INDEX IF NOT EXISTS idx_identified_user_sets_tenant
ON schm_sp.identified_user_sets (tenant_id);


CREATE TABLE IF NOT EXISTS schm_sp.identified_user_set_members
(
    id bigserial NOT NULL,
    user_set_id bigint NOT NULL,
    user_id bigint NOT NULL,
    sign_no character varying,
    sso_provider_user_id character varying,
    membership_role character varying,
    active boolean NOT NULL DEFAULT true,
    cr_by bigint NOT NULL,
    u_by bigint,
    c_date timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    u_date timestamp with time zone,
    tenant_id character varying NOT NULL,

    CONSTRAINT identified_user_set_members_pkey PRIMARY KEY (id),
    CONSTRAINT fk_member_user_set
        FOREIGN KEY (user_set_id)
        REFERENCES schm_sp.identified_user_sets(id),
    CONSTRAINT fk_member_user
        FOREIGN KEY (user_id)
        REFERENCES schm_sp.login_details(user_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_active_user_set_membership
ON schm_sp.identified_user_set_members (user_set_id, user_id)
WHERE active = true;

CREATE INDEX IF NOT EXISTS idx_identified_user_set_members_tenant
ON schm_sp.identified_user_set_members (tenant_id);

CREATE INDEX IF NOT EXISTS idx_identified_user_set_members_user
ON schm_sp.identified_user_set_members (user_id);

CREATE INDEX IF NOT EXISTS idx_identified_user_set_members_set
ON schm_sp.identified_user_set_members (user_set_id);


CREATE TABLE IF NOT EXISTS schm_sp.identified_user_set_upload_jobs
(
    id bigserial NOT NULL,
    user_set_id bigint NOT NULL,
    file_name character varying,
    file_path character varying,
    file_data bytea,
    job_type character varying NOT NULL DEFAULT 'UPLOAD',
    status character varying NOT NULL DEFAULT 'PENDING',
    admin_email character varying,
    total_records integer DEFAULT 0,
    added_count integer DEFAULT 0,
    failed_count integer DEFAULT 0,
    warning_count integer DEFAULT 0,
    result_file_path character varying,
    error_message text,
    cr_by bigint NOT NULL,
    u_by bigint,
    c_date timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    u_date timestamp with time zone,
    tenant_id character varying NOT NULL,

    CONSTRAINT identified_user_set_upload_jobs_pkey PRIMARY KEY (id),
    CONSTRAINT fk_job_user_set
        FOREIGN KEY (user_set_id)
        REFERENCES schm_sp.identified_user_sets(id)
);

CREATE INDEX IF NOT EXISTS idx_upload_jobs_status
ON schm_sp.identified_user_set_upload_jobs (status);

CREATE INDEX IF NOT EXISTS idx_upload_jobs_tenant
ON schm_sp.identified_user_set_upload_jobs (tenant_id);
