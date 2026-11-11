--
-- PostgreSQL database dump
--

-- Dumped from database version 16.11
-- Dumped by pg_dump version 17.1

-- Started on 2026-03-24 18:07:33

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 4871 (class 0 OID 24874)
-- Dependencies: 234
-- Data for Name: role_master; Type: TABLE DATA; Schema: schm_sp; Owner: postgres
--

INSERT INTO schm_sp.role_master VALUES (23, 'CSC Payment Department', 'CSCPAY');
INSERT INTO schm_sp.role_master VALUES (22, 'Central Service Definer', 'CSD');
INSERT INTO schm_sp.role_master VALUES (0, 'Admin', 'ADMN');
INSERT INTO schm_sp.role_master VALUES (1, 'NIC State Admin', 'SADMN');
INSERT INTO schm_sp.role_master VALUES (2, 'NIC District Admin', 'DDMN');
INSERT INTO schm_sp.role_master VALUES (3, 'Designated Officer', 'GOVT');
INSERT INTO schm_sp.role_master VALUES (4, 'Master Data Operator', 'GOVT');
INSERT INTO schm_sp.role_master VALUES (6, 'Citizen', 'CTZN');
INSERT INTO schm_sp.role_master VALUES (7, 'Kiosk Operator', 'SAPR');
INSERT INTO schm_sp.role_master VALUES (8, 'Super User', 'SUSR');
INSERT INTO schm_sp.role_master VALUES (10, 'District Service Definer', 'DDEF');
INSERT INTO schm_sp.role_master VALUES (11, 'First Appellate Authority', 'FAA');
INSERT INTO schm_sp.role_master VALUES (12, 'Second Appellate Authority', 'SAA');
INSERT INTO schm_sp.role_master VALUES (13, 'Central Admin', 'CADM');
INSERT INTO schm_sp.role_master VALUES (14, 'Data Entry Operator', 'DEO');
INSERT INTO schm_sp.role_master VALUES (17, 'Dispatcher', 'DISP');
INSERT INTO schm_sp.role_master VALUES (19, 'SCA', 'SCA');
INSERT INTO schm_sp.role_master VALUES (24, 'Test Citizen', 'TESTCTZN');
INSERT INTO schm_sp.role_master VALUES (25, 'Test Kiosk', 'TESTKIOSK');
INSERT INTO schm_sp.role_master VALUES (26, 'Test Deo', 'TESTDEO');
INSERT INTO schm_sp.role_master VALUES (27, 'Reviewing Authority', 'RA');
INSERT INTO schm_sp.role_master VALUES (28, 'Menu CustomReport', 'DHEAD');
INSERT INTO schm_sp.role_master VALUES (29, 'External Service Applicant', 'ESA');
INSERT INTO schm_sp.role_master VALUES (9, 'Service Definer', 'SDEF');
INSERT INTO schm_sp.role_master VALUES (62, 'Grievance User', 'GRIEVUSER');
INSERT INTO schm_sp.role_master VALUES (91, 'Checker', 'CHCK');
INSERT INTO schm_sp.role_master VALUES (93, 'Credential Admin', 'CREDADMN');
INSERT INTO schm_sp.role_master VALUES (92, 'Credential Manager', 'CREDMAN');
INSERT INTO schm_sp.role_master VALUES (94, 'Help Desk', 'HELPDESK');
INSERT INTO schm_sp.role_master VALUES (95, 'Admin Module Manager', 'ADMINMAN');
INSERT INTO schm_sp.role_master VALUES (20, 'Instance Owner', 'SGA');
INSERT INTO schm_sp.role_master VALUES (18, 'Department Admin', 'DHEAD');
INSERT INTO schm_sp.role_master VALUES (5, 'Executive', 'GOVT');


-- Completed on 2026-03-24 18:07:33

--
-- PostgreSQL database dump complete
--

