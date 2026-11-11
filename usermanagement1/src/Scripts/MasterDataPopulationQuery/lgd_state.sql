--
-- PostgreSQL database dump
--

-- Dumped from database version 16.11
-- Dumped by pg_dump version 17.1

-- Started on 2026-03-24 18:06:24

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
-- TOC entry 4877 (class 0 OID 24725)
-- Dependencies: 231
-- Data for Name: state; Type: TABLE DATA; Schema: lgd; Owner: postgres
--

INSERT INTO lgd.state VALUES (21, 1, 'ODISHA', 'ODISHA', 'OR', 'ODISHA', '', '21', '21', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 21, NULL, false);
INSERT INTO lgd.state VALUES (5, 1, 'UTTARAKHAND', 'UTTARAKHAND', 'UK', 'UTTARAKHAND', '', '05', '5 ', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 5, NULL, false);
INSERT INTO lgd.state VALUES (2, 1, 'HIMACHAL PRADESH', 'HIMACHAL PRADESH                                  ', 'HP', 'HIMACHALPRADESH', '', '02', '2 ', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 2, NULL, false);
INSERT INTO lgd.state VALUES (3, 1, 'PUNJAB', 'PUNJAB                                            ', 'PB', 'PUNJAB', '', '03', '3 ', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 3, NULL, false);
INSERT INTO lgd.state VALUES (4, 1, 'CHANDIGARH', 'CHANDIGARH                                        ', 'CD', 'CHANDIGARH', '', '04', '4 ', 'U', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 4, NULL, false);
INSERT INTO lgd.state VALUES (6, 1, 'HARYANA', 'HARYANA                                           ', 'HR', 'HARYANA', '', '06', '6 ', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 6, NULL, false);
INSERT INTO lgd.state VALUES (7, 1, 'DELHI', 'DELHI                                             ', 'DL', 'DELHI', '', '07', '7 ', 'U', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 7, NULL, false);
INSERT INTO lgd.state VALUES (8, 1, 'RAJASTHAN', 'RAJASTHAN                                         ', 'RJ', 'RAJASTHAN', '', '08', '8 ', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 8, NULL, false);
INSERT INTO lgd.state VALUES (9, 1, 'UTTAR PRADESH', 'UTTAR PRADESH                                     ', 'UP', 'UTTARPRADESH', '', '09', '9 ', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 9, NULL, false);
INSERT INTO lgd.state VALUES (10, 1, 'BIHAR', 'BIHAR                                             ', 'BI', 'BIHAR', '', '10', '10', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 10, NULL, false);
INSERT INTO lgd.state VALUES (11, 1, 'SIKKIM', 'SIKKIM                                            ', 'SK', 'SIKKIM', '', '11', '11', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 11, NULL, false);
INSERT INTO lgd.state VALUES (12, 1, 'ARUNACHAL PRADESH', 'ARUNACHAL PRADESH                                 ', 'AR', 'ARUNACHALPRADESH', '', '12', '12', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 12, NULL, false);
INSERT INTO lgd.state VALUES (13, 1, 'NAGALAND', 'NAGALAND                                          ', 'NA', 'NAGALAND', '', '13', '13', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 13, NULL, false);
INSERT INTO lgd.state VALUES (14, 1, 'MANIPUR', 'MANIPUR                                           ', 'MA', 'MANIPUR', '', '14', '14', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 14, NULL, false);
INSERT INTO lgd.state VALUES (15, 1, 'MIZORAM', 'MIZORAM                                           ', 'MZ', 'MIZORAM', '', '15', '15', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 15, NULL, false);
INSERT INTO lgd.state VALUES (16, 1, 'TRIPURA', 'TRIPURA                                           ', 'TR', 'TRIPURA', '', '16', '16', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 16, NULL, false);
INSERT INTO lgd.state VALUES (17, 1, 'MEGHALAYA', 'MEGHALAYA                                         ', 'ML', 'MEGHALAYA', '', '17', '17', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 17, NULL, false);
INSERT INTO lgd.state VALUES (18, 1, 'ASSAM', 'ASSAM                                             ', 'AS', 'ASSAM', '', '18', '18', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 18, NULL, false);
INSERT INTO lgd.state VALUES (19, 1, 'WEST BENGAL', 'WEST BENGAL                                       ', 'WB', 'WESTBENGAL', '', '19', '19', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 19, NULL, false);
INSERT INTO lgd.state VALUES (20, 1, 'JHARKHAND', 'JHARKHAND                                         ', 'JH', 'JHARKHAND', '', '20', '20', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 20, NULL, false);
INSERT INTO lgd.state VALUES (22, 1, 'CHHATTISGARH', 'CHHATTISGARH                                      ', 'CG', 'CHHATTISGARH', '', '22', '22', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 22, NULL, false);
INSERT INTO lgd.state VALUES (23, 1, 'MADHYA PRADESH', 'MADHYA PRADESH                                    ', 'MP', 'MADHYAPRADESH', '', '23', '23', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 23, NULL, false);
INSERT INTO lgd.state VALUES (24, 1, 'GUJARAT', 'GUJARAT                                           ', 'GU', 'GUJARAT', '', '24', '24', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 24, NULL, false);
INSERT INTO lgd.state VALUES (25, 1, 'DAMAN AND DIU', 'DAMAN AND DIU                                     ', 'DD', 'DAMANANDDIU', '', '25', '25', 'U', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 25, NULL, false);
INSERT INTO lgd.state VALUES (26, 1, 'DADRA AND NAGAR HAVELI', 'DADRA AND NAGAR HAVELI                            ', 'DN', 'DADRAANDNAGARHAVELI', '', '26', '26', 'U', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 26, NULL, false);
INSERT INTO lgd.state VALUES (29, 1, 'KARNATAKA', 'KARNATAKA                                         ', 'KA', 'KARNATAKA', '', '29', '29', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 29, NULL, false);
INSERT INTO lgd.state VALUES (30, 1, 'GOA', 'GOA                                               ', 'GA', 'GOA', '', '30', '30', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 30, NULL, false);
INSERT INTO lgd.state VALUES (31, 1, 'LAKSHADWEEP', 'LAKSHADWEEP                                       ', 'LD', 'LAKSHADWEEP', '', '31', '31', 'U', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 31, NULL, false);
INSERT INTO lgd.state VALUES (32, 1, 'KERALA', 'KERALA                                            ', 'KE', 'KERALA', '', '32', '32', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 32, NULL, false);
INSERT INTO lgd.state VALUES (33, 1, 'TAMIL NADU', 'TAMIL NADU                                        ', 'TN', 'TAMILNADU', '', '33', '33', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 33, NULL, false);
INSERT INTO lgd.state VALUES (34, 1, 'PUDUCHERRY', 'PUDUCHERRY', 'PO', 'PONDICHERRY', '', '34', '34', 'U', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 34, NULL, false);
INSERT INTO lgd.state VALUES (35, 1, 'ANDAMAN AND NICOBAR ISLANDS', 'ANDAMAN AND NICOBAR ISLANDS                       ', 'AN', 'ANDAMANANDNICOBARISLANDS', '', '35', '35', 'U', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 35, NULL, false);
INSERT INTO lgd.state VALUES (27, 1, 'MAHARASHTRA', 'MAHARASHTRA                                       ', 'MH', 'MAHARASHTRA', '', '27', '27', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2005-01-01 00:00:00+05:30', 0, '2005-01-01 00:00:00+05:30', 0, true, NULL, NULL, false, NULL, 27, NULL, false);
INSERT INTO lgd.state VALUES (36, 1, 'TELANGANA', ' ', 'TS', ' ', ' ', '  ', '0 ', 'S', 328, 0, '2014-06-17 01:19:31.259+05:30', '2014-06-17 01:19:31.259+05:30', 11617, '2014-06-17 01:19:31.259+05:30', 11617, true, 0, 1, false, 25519, 36, ' ', false);
INSERT INTO lgd.state VALUES (28, 1, 'ANDHRA PRADESH                                    ', 'ANDHRA PRADESH                                    ', 'AP', 'ANDHRAPRADESH', '', '28', '28', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2014-06-17 01:19:31.259+05:30', 11617, '2005-01-01 00:00:00+05:30', 0, false, 312, NULL, false, NULL, 28, NULL, false);
INSERT INTO lgd.state VALUES (28, 2, 'ANDHRA PRADESH', 'ANDHRA PRADESH', 'AP', 'ANDHRAPRADESH', '', '28', '28', 'S', 0, 0, '2014-06-17 01:19:31.259+05:30', '2014-06-17 01:19:31.259+05:30', 11617, '2014-06-17 01:19:31.259+05:30', 11617, true, 0, 73, false, 25519, 28, ' ', false);
INSERT INTO lgd.state VALUES (1, 1, 'JAMMU AND KASHMIR', 'JAMMU AND KASHMIR                                 ', 'JK', 'JAMMUANDKASHMIR', '', '01', '01', 'S', NULL, NULL, '2005-01-01 00:00:00+05:30', '2019-08-23 17:06:21+05:30', 11617, '2005-01-01 00:00:00+05:30', 0, false, 8616, NULL, false, NULL, 1, NULL, false);
INSERT INTO lgd.state VALUES (1, 2, 'JAMMU AND KASHMIR', 'JAMMU AND KASHMIR                                 ', 'JK', 'JAMMUANDKASHMIR', '', '01', '01', 'S', NULL, NULL, '2019-08-23 17:06:21+05:30', '2019-09-16 17:39:54+05:30', 131561, '2019-08-23 17:06:21+05:30', 11617, false, NULL, 73, false, 205172, 1, NULL, false);
INSERT INTO lgd.state VALUES (1, 3, 'JAMMU AND KASHMIR', 'JAMMU AND KASHMIR', 'JK', 'JAMMUANDKASHMIR', '', '01', '01', 'U', NULL, NULL, '2019-08-24 00:00:00+05:30', '2019-09-16 17:39:54+05:30', 131561, '2019-09-16 17:39:54+05:30', 131561, true, NULL, 48, false, 210278, 1, NULL, false);
INSERT INTO lgd.state VALUES (37, 1, 'LADAKH', NULL, 'LA', NULL, NULL, NULL, '00', 'U', 3339, NULL, '2019-08-23 17:06:21+05:30', '2019-08-23 17:06:21+05:30', 11617, '2019-08-23 17:06:21+05:30', 11617, true, NULL, 1, false, 205172, 37, NULL, false);
INSERT INTO lgd.state VALUES (38, 1, 'THE DADRA AND NAGAR HAVELI AND DAMAN AND DIU', 'THE DADRA AND NAGAR HAVELI AND DAMAN AND DIU', 'ND', NULL, NULL, '38', '38', 'U', 4256, NULL, '2019-12-09 00:00:00+05:30', '2020-05-09 16:21:22.464941+05:30', 0, '2020-05-09 16:21:22.464941+05:30', 0, true, NULL, 2, false, 266711, 38, NULL, false);


-- Completed on 2026-03-24 18:06:24

--
-- PostgreSQL database dump complete
--

