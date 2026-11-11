-- Table: schm_sp.admin_user_creation_access

-- DROP TABLE IF EXISTS schm_sp.admin_user_creation_access;

CREATE TABLE IF NOT EXISTS schm_sp.admin_user_creation_access
(
    id bigint NOT NULL DEFAULT nextval('schm_sp.admin_user_creation_access_id_seq'::regclass),
    logged_in_role integer,
    can_create_role integer,
    tenant_id character varying COLLATE pg_catalog."default",
    CONSTRAINT admin_user_creation_access_pkey PRIMARY KEY (id)
)

-- Table: schm_sp.kafka_outbox_event

-- DROP TABLE IF EXISTS schm_sp.kafka_outbox_event;

CREATE TABLE IF NOT EXISTS schm_sp.kafka_outbox_event
(
    id character varying COLLATE pg_catalog."default" NOT NULL,
    topic character varying COLLATE pg_catalog."default" NOT NULL,
    event_key character varying COLLATE pg_catalog."default",
    payload text COLLATE pg_catalog."default" NOT NULL,
    status character varying COLLATE pg_catalog."default" NOT NULL DEFAULT 'PENDING'::character varying,
    retry_count integer NOT NULL DEFAULT 0,
    created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at timestamp with time zone,
    CONSTRAINT kafka_outbox_event_pkey PRIMARY KEY (id)
)

-- Table: schm_sp.kiosk_types

-- DROP TABLE IF EXISTS schm_sp.kiosk_types;

CREATE TABLE IF NOT EXISTS schm_sp.kiosk_types
(
    id bigint NOT NULL DEFAULT nextval('schm_sp.kiosk_types_id_seq'::regclass),
    name character varying(100) COLLATE pg_catalog."default" NOT NULL,
    clc_id integer NOT NULL,
    created_by bigint,
    created_date timestamp with time zone,
    active boolean,
    tenant_id character varying COLLATE pg_catalog."default",
    CONSTRAINT kiosk_type_unique UNIQUE (name, clc_id)
)

-- Table: schm_sp.login_details

-- DROP TABLE IF EXISTS schm_sp.login_details;

CREATE TABLE IF NOT EXISTS schm_sp.login_details
(
    user_id bigint NOT NULL DEFAULT nextval('schm_sp.login_details_user_id_seq'::regclass),
    sign_no character varying COLLATE pg_catalog."default",
    passwd character varying COLLATE pg_catalog."default",
    spdi_status integer,
    c_date timestamp without time zone,
    created_by bigint,
    tenant_id character varying COLLATE pg_catalog."default",
    clc_id integer,
    CONSTRAINT login_details_pkey PRIMARY KEY (user_id),
    CONSTRAINT uq_login_details_sign_no UNIQUE (sign_no)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS schm_sp.login_details
    OWNER to postgres;


-- Table: schm_sp.role_assignment

-- DROP TABLE IF EXISTS schm_sp.role_assignment;

CREATE TABLE IF NOT EXISTS schm_sp.role_assignment
(
    user_id bigint NOT NULL,
    role_id integer NOT NULL,
    user_role_id bigint NOT NULL DEFAULT nextval('schm_sp.role_assignment_user_role_id_seq'::regclass),
    user_loc_id bigint,
    tenant_id character varying COLLATE pg_catalog."default",
    CONSTRAINT pk_role_assignment PRIMARY KEY (user_role_id),
    CONSTRAINT uk_role_ass_loc UNIQUE (user_id, role_id, user_loc_id)
)


-- Table: schm_sp.role_master

-- DROP TABLE IF EXISTS schm_sp.role_master;

CREATE TABLE IF NOT EXISTS schm_sp.role_master
(
    role_id integer NOT NULL,
    role_name character varying(100) COLLATE pg_catalog."default",
    sign_role character varying(10) COLLATE pg_catalog."default",
    tenant_id character varying COLLATE pg_catalog."default",
    CONSTRAINT role_master_pkey PRIMARY KEY (role_id)
)

-- Table: schm_sp.user_location

-- DROP TABLE IF EXISTS schm_sp.user_location;

CREATE TABLE IF NOT EXISTS schm_sp.user_location
(
    user_loc_id bigint NOT NULL DEFAULT nextval('schm_sp.user_location_user_loc_id_seq'::regclass),
    user_id bigint,
    location_id integer,
    category_id integer,
    entity_id integer,
    entity_level_id integer,
    entity_level_name character varying COLLATE pg_catalog."default",
    location_name character varying COLLATE pg_catalog."default",
    tenant_id character varying COLLATE pg_catalog."default",
    CONSTRAINT pk_user_loc_id PRIMARY KEY (user_loc_id),
    CONSTRAINT uq_user_loc UNIQUE (user_id, location_id, category_id)
)

-- Table: schm_sp.user_location_designation

-- DROP TABLE IF EXISTS schm_sp.user_location_designation;

CREATE TABLE IF NOT EXISTS schm_sp.user_location_designation
(
    user_loc_desig_id bigint NOT NULL DEFAULT nextval('schm_sp.user_location_designation_user_loc_desig_id_seq'::regclass),
    designation_id integer,
    designation_name character varying(200) COLLATE pg_catalog."default",
    user_loc_id bigint,
    tenant_id character varying COLLATE pg_catalog."default",
    CONSTRAINT pk_user_loc_desig PRIMARY KEY (user_loc_desig_id),
    CONSTRAINT uq_userloc_desg UNIQUE (user_loc_id, designation_id)
)

-- Table: schm_sp.user_profile

-- DROP TABLE IF EXISTS schm_sp.user_profile;

CREATE TABLE IF NOT EXISTS schm_sp.user_profile
(
    user_id bigint NOT NULL,
    email_id character varying COLLATE pg_catalog."default",
    mobile_no character varying COLLATE pg_catalog."default",
    designation_id integer,
    user_preference integer,
    clc_id integer,
    user_name character varying COLLATE pg_catalog."default",
    clc_name character varying COLLATE pg_catalog."default",
    cug_type character varying COLLATE pg_catalog."default",
    approval_required boolean,
    tenant_id character varying COLLATE pg_catalog."default",
    CONSTRAINT user_profile_pkey PRIMARY KEY (user_id)
)


-- Table: schm_sp.user_pwd_reset_link

-- DROP TABLE IF EXISTS schm_sp.user_pwd_reset_link;

CREATE TABLE IF NOT EXISTS schm_sp.user_pwd_reset_link
(
    token character varying COLLATE pg_catalog."default",
    user_id bigint NOT NULL,
    creation_time timestamp without time zone,
    last_acces_time timestamp without time zone,
    access_count integer,
    attempt_count integer,
    last_hit_ip_address character varying COLLATE pg_catalog."default",
    is_active boolean,
    tenant_id character varying COLLATE pg_catalog."default",
    CONSTRAINT user_pwd_reset_link_pkey PRIMARY KEY (user_id)
)

-------------------------------- SEQUENCES ----------------------------------------------

-- SEQUENCE: schm_sp.admin_user_creation_access_id_seq

-- DROP SEQUENCE IF EXISTS schm_sp.admin_user_creation_access_id_seq;

CREATE SEQUENCE IF NOT EXISTS schm_sp.admin_user_creation_access_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE schm_sp.admin_user_creation_access_id_seq
    OWNED BY schm_sp.admin_user_creation_access.id;

ALTER SEQUENCE schm_sp.admin_user_creation_access_id_seq
    OWNER TO postgres;


-- SEQUENCE: schm_sp.kiosk_types_id_seq

-- DROP SEQUENCE IF EXISTS schm_sp.kiosk_types_id_seq;

CREATE SEQUENCE IF NOT EXISTS schm_sp.kiosk_types_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE schm_sp.kiosk_types_id_seq
    OWNED BY schm_sp.kiosk_types.id;

ALTER SEQUENCE schm_sp.kiosk_types_id_seq
    OWNER TO postgres;


-- SEQUENCE: schm_sp.login_details_user_id_seq

-- DROP SEQUENCE IF EXISTS schm_sp.login_details_user_id_seq;

CREATE SEQUENCE IF NOT EXISTS schm_sp.login_details_user_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE schm_sp.login_details_user_id_seq
    OWNED BY schm_sp.login_details.user_id;

ALTER SEQUENCE schm_sp.login_details_user_id_seq
    OWNER TO postgres;


-- SEQUENCE: schm_sp.role_assignment_user_role_id_seq

-- DROP SEQUENCE IF EXISTS schm_sp.role_assignment_user_role_id_seq;

CREATE SEQUENCE IF NOT EXISTS schm_sp.role_assignment_user_role_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE schm_sp.role_assignment_user_role_id_seq
    OWNED BY schm_sp.role_assignment.user_role_id;

ALTER SEQUENCE schm_sp.role_assignment_user_role_id_seq
    OWNER TO postgres;



-- SEQUENCE: schm_sp.sign_no_seq

-- DROP SEQUENCE IF EXISTS schm_sp.sign_no_seq;

CREATE SEQUENCE IF NOT EXISTS schm_sp.sign_no_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 20;

ALTER SEQUENCE schm_sp.sign_no_seq
    OWNER TO postgres;


-- SEQUENCE: schm_sp.user_location_designation_user_loc_desig_id_seq

-- DROP SEQUENCE IF EXISTS schm_sp.user_location_designation_user_loc_desig_id_seq;

CREATE SEQUENCE IF NOT EXISTS schm_sp.user_location_designation_user_loc_desig_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE schm_sp.user_location_designation_user_loc_desig_id_seq
    OWNED BY schm_sp.user_location_designation.user_loc_desig_id;

ALTER SEQUENCE schm_sp.user_location_designation_user_loc_desig_id_seq
    OWNER TO postgres;


-- SEQUENCE: schm_sp.user_location_user_loc_id_seq

-- DROP SEQUENCE IF EXISTS schm_sp.user_location_user_loc_id_seq;

CREATE SEQUENCE IF NOT EXISTS schm_sp.user_location_user_loc_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE schm_sp.user_location_user_loc_id_seq
    OWNER TO postgres;



-- SEQUENCE: schm_sp.user_registration_id_seq

-- DROP SEQUENCE IF EXISTS schm_sp.user_registration_id_seq;

CREATE SEQUENCE IF NOT EXISTS schm_sp.user_registration_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE schm_sp.user_registration_id_seq
    OWNER TO postgres;


