/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

DROP TABLE IF EXISTS QRTZ_FIRED_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_PAUSED_TRIGGER_GRPS;
DROP TABLE IF EXISTS QRTZ_SCHEDULER_STATE;
DROP TABLE IF EXISTS QRTZ_LOCKS;
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_SIMPROP_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS;
DROP TABLE IF EXISTS QRTZ_CALENDARS;

CREATE TABLE QRTZ_JOB_DETAILS(
SCHED_NAME character varying(120) NOT NULL,
JOB_NAME character varying(200) NOT NULL,
JOB_GROUP character varying(200) NOT NULL,
DESCRIPTION character varying(250) NULL,
JOB_CLASS_NAME character varying(250) NOT NULL,
IS_DURABLE boolean NOT NULL,
IS_NONCONCURRENT boolean NOT NULL,
IS_UPDATE_DATA boolean NOT NULL,
REQUESTS_RECOVERY boolean NOT NULL,
JOB_DATA bytea NULL);
alter table QRTZ_JOB_DETAILS add primary key(SCHED_NAME,JOB_NAME,JOB_GROUP);

CREATE TABLE QRTZ_TRIGGERS (
SCHED_NAME character varying(120) NOT NULL,
TRIGGER_NAME character varying(200) NOT NULL,
TRIGGER_GROUP character varying(200) NOT NULL,
JOB_NAME character varying(200) NOT NULL,
JOB_GROUP character varying(200) NOT NULL,
DESCRIPTION character varying(250) NULL,
NEXT_FIRE_TIME BIGINT NULL,
PREV_FIRE_TIME BIGINT NULL,
PRIORITY INTEGER NULL,
TRIGGER_STATE character varying(16) NOT NULL,
TRIGGER_TYPE character varying(8) NOT NULL,
START_TIME BIGINT NOT NULL,
END_TIME BIGINT NULL,
CALENDAR_NAME character varying(200) NULL,
MISFIRE_INSTR SMALLINT NULL,
JOB_DATA bytea NULL)  ;
alter table QRTZ_TRIGGERS add primary key(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);

CREATE TABLE QRTZ_SIMPLE_TRIGGERS (
SCHED_NAME character varying(120) NOT NULL,
TRIGGER_NAME character varying(200) NOT NULL,
TRIGGER_GROUP character varying(200) NOT NULL,
REPEAT_COUNT BIGINT NOT NULL,
REPEAT_INTERVAL BIGINT NOT NULL,
TIMES_TRIGGERED BIGINT NOT NULL)  ;
alter table QRTZ_SIMPLE_TRIGGERS add primary key(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);

CREATE TABLE QRTZ_CRON_TRIGGERS (
SCHED_NAME character varying(120) NOT NULL,
TRIGGER_NAME character varying(200) NOT NULL,
TRIGGER_GROUP character varying(200) NOT NULL,
CRON_EXPRESSION character varying(120) NOT NULL,
TIME_ZONE_ID character varying(80))  ;
alter table QRTZ_CRON_TRIGGERS add primary key(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);

CREATE TABLE QRTZ_SIMPROP_TRIGGERS
  (
    SCHED_NAME character varying(120) NOT NULL,
    TRIGGER_NAME character varying(200) NOT NULL,
    TRIGGER_GROUP character varying(200) NOT NULL,
    STR_PROP_1 character varying(512) NULL,
    STR_PROP_2 character varying(512) NULL,
    STR_PROP_3 character varying(512) NULL,
    INT_PROP_1 INT NULL,
    INT_PROP_2 INT NULL,
    LONG_PROP_1 BIGINT NULL,
    LONG_PROP_2 BIGINT NULL,
    DEC_PROP_1 NUMERIC(13,4) NULL,
    DEC_PROP_2 NUMERIC(13,4) NULL,
    BOOL_PROP_1 boolean NULL,
    BOOL_PROP_2 boolean NULL) ;
alter table QRTZ_SIMPROP_TRIGGERS add primary key(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);

CREATE TABLE QRTZ_BLOB_TRIGGERS (
SCHED_NAME character varying(120) NOT NULL,
TRIGGER_NAME character varying(200) NOT NULL,
TRIGGER_GROUP character varying(200) NOT NULL,
BLOB_DATA bytea NULL) ;
alter table QRTZ_BLOB_TRIGGERS add primary key(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);

CREATE TABLE QRTZ_CALENDARS (
SCHED_NAME character varying(120) NOT NULL,
CALENDAR_NAME character varying(200) NOT NULL,
CALENDAR bytea NOT NULL)  ;
alter table QRTZ_CALENDARS add primary key(SCHED_NAME,CALENDAR_NAME);

CREATE TABLE QRTZ_PAUSED_TRIGGER_GRPS (
SCHED_NAME character varying(120) NOT NULL,
TRIGGER_GROUP character varying(200) NOT NULL)  ;
alter table QRTZ_PAUSED_TRIGGER_GRPS add primary key(SCHED_NAME,TRIGGER_GROUP);

CREATE TABLE QRTZ_FIRED_TRIGGERS (
SCHED_NAME character varying(120) NOT NULL,
ENTRY_ID character varying(200) NOT NULL,
TRIGGER_NAME character varying(200) NOT NULL,
TRIGGER_GROUP character varying(200) NOT NULL,
INSTANCE_NAME character varying(200) NOT NULL,
FIRED_TIME BIGINT NOT NULL,
SCHED_TIME BIGINT NOT NULL,
PRIORITY INTEGER NOT NULL,
STATE character varying(16) NOT NULL,
JOB_NAME character varying(200) NULL,
JOB_GROUP character varying(200) NULL,
IS_NONCONCURRENT boolean NULL,
REQUESTS_RECOVERY boolean NULL)  ;
alter table QRTZ_FIRED_TRIGGERS add primary key(SCHED_NAME,ENTRY_ID);

CREATE TABLE QRTZ_SCHEDULER_STATE (
SCHED_NAME character varying(120) NOT NULL,
INSTANCE_NAME character varying(200) NOT NULL,
LAST_CHECKIN_TIME BIGINT NOT NULL,
CHECKIN_INTERVAL BIGINT NOT NULL)  ;
alter table QRTZ_SCHEDULER_STATE add primary key(SCHED_NAME,INSTANCE_NAME);

CREATE TABLE QRTZ_LOCKS (
SCHED_NAME character varying(120) NOT NULL,
LOCK_NAME character varying(40) NOT NULL)  ;
alter table QRTZ_LOCKS add primary key(SCHED_NAME,LOCK_NAME);

CREATE INDEX IDX_QRTZ_J_REQ_RECOVERY ON QRTZ_JOB_DETAILS(SCHED_NAME,REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ_J_GRP ON QRTZ_JOB_DETAILS(SCHED_NAME,JOB_GROUP);

CREATE INDEX IDX_QRTZ_T_J ON QRTZ_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_JG ON QRTZ_TRIGGERS(SCHED_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_C ON QRTZ_TRIGGERS(SCHED_NAME,CALENDAR_NAME);
CREATE INDEX IDX_QRTZ_T_G ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_T_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_N_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_N_G_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_NEXT_FIRE_TIME ON QRTZ_TRIGGERS(SCHED_NAME,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_ST ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_MISFIRE ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE_GRP ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);

CREATE INDEX IDX_QRTZ_FT_TRIG_INST_NAME ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME);
CREATE INDEX IDX_QRTZ_FT_INST_JOB_REQ_RCVRY ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ_FT_J_G ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_FT_JG ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_FT_T_G ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_FT_TG ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);


--
-- Table structure for table t_ds_access_token
--

DROP TABLE IF EXISTS t_ds_access_token;
CREATE TABLE t_ds_access_token (
  id int NOT NULL  ,
  user_id int DEFAULT NULL ,
  token varchar(64) DEFAULT NULL ,
  expire_time timestamp DEFAULT NULL ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;

--
-- Table structure for table t_ds_alert
--

DROP TABLE IF EXISTS t_ds_alert;
CREATE TABLE t_ds_alert (
  id int NOT NULL  ,
  title varchar(64) DEFAULT NULL ,
  content text ,
  alert_status int DEFAULT '0' ,
  log text ,
  alertgroup_id int DEFAULT NULL ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;
--
-- Table structure for table t_ds_alertgroup
--

DROP TABLE IF EXISTS t_ds_alertgroup;
CREATE TABLE t_ds_alertgroup(
  id             int NOT NULL,
  alert_instance_ids varchar (255) DEFAULT NULL,
  create_user_id int4         DEFAULT NULL,
  group_name     varchar(255) DEFAULT NULL,
  description    varchar(255) DEFAULT NULL,
  create_time    timestamp    DEFAULT NULL,
  update_time    timestamp    DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT t_ds_alertgroup_name_UN UNIQUE (group_name)
) ;

--
-- Table structure for table t_ds_command
--

DROP TABLE IF EXISTS t_ds_command;
CREATE TABLE t_ds_command (
  id int NOT NULL  ,
  command_type int DEFAULT NULL ,
  process_definition_id int DEFAULT NULL ,
  command_param text ,
  task_depend_type int DEFAULT NULL ,
  failure_strategy int DEFAULT '0' ,
  warning_type int DEFAULT '0' ,
  warning_group_id int DEFAULT NULL ,
  schedule_time timestamp DEFAULT NULL ,
  start_time timestamp DEFAULT NULL ,
  executor_id int DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  process_instance_priority int DEFAULT NULL ,
  worker_group varchar(64),
  PRIMARY KEY (id)
) ;

--
-- Table structure for table t_ds_datasource
--

DROP TABLE IF EXISTS t_ds_datasource;
CREATE TABLE t_ds_datasource (
  id int NOT NULL  ,
  name varchar(64) NOT NULL ,
  note varchar(256) DEFAULT NULL ,
  type int NOT NULL ,
  user_id int NOT NULL ,
  connection_params text NOT NULL ,
  create_time timestamp NOT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id),
  CONSTRAINT t_ds_datasource_name_UN UNIQUE (name, type)
) ;

--
-- Table structure for table t_ds_error_command
--

DROP TABLE IF EXISTS t_ds_error_command;
CREATE TABLE t_ds_error_command (
  id int NOT NULL ,
  command_type int DEFAULT NULL ,
  executor_id int DEFAULT NULL ,
  process_definition_id int DEFAULT NULL ,
  command_param text ,
  task_depend_type int DEFAULT NULL ,
  failure_strategy int DEFAULT '0' ,
  warning_type int DEFAULT '0' ,
  warning_group_id int DEFAULT NULL ,
  schedule_time timestamp DEFAULT NULL ,
  start_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  process_instance_priority int DEFAULT NULL ,
  worker_group varchar(64),
  message text ,
  PRIMARY KEY (id)
);
--
-- Table structure for table t_ds_master_server
--

--
-- Table structure for table t_ds_process_definition
--

DROP TABLE IF EXISTS t_ds_process_definition;
CREATE TABLE t_ds_process_definition (
  id int NOT NULL  ,
  name varchar(255) DEFAULT NULL ,
  version int DEFAULT NULL ,
  release_state int DEFAULT NULL ,
  project_id int DEFAULT NULL ,
  user_id int DEFAULT NULL ,
  process_definition_json text ,
  description text ,
  global_params text ,
  flag int DEFAULT NULL ,
  locations text ,
  connects text ,
  warning_group_id int4 DEFAULT NULL ,
  create_time timestamp DEFAULT NULL ,
  timeout int DEFAULT '0' ,
  tenant_id int NOT NULL DEFAULT '-1' ,
  update_time timestamp DEFAULT NULL ,
  modify_by varchar(36) DEFAULT '' ,
  resource_ids varchar(64),
  PRIMARY KEY (id),
  CONSTRAINT process_definition_unique UNIQUE (name, project_id)
) ;

create index process_definition_index on t_ds_process_definition (project_id,id);

--
-- Table structure for table t_ds_process_definition_version
--

DROP TABLE IF EXISTS t_ds_process_definition_version;
CREATE TABLE t_ds_process_definition_version (
  id int NOT NULL  ,
  process_definition_id int NOT NULL  ,
  version int DEFAULT NULL ,
  process_definition_json text ,
  description text ,
  global_params text ,
  locations text ,
  connects text ,
  warning_group_id int4 DEFAULT NULL,
  create_time timestamp DEFAULT NULL ,
  timeout int DEFAULT '0' ,
  resource_ids varchar(64),
  PRIMARY KEY (id)
) ;

create index process_definition_id_and_version on t_ds_process_definition_version (process_definition_id,version);

--
-- Table structure for table t_ds_process_instance
--

DROP TABLE IF EXISTS t_ds_process_instance;
CREATE TABLE t_ds_process_instance (
  id int NOT NULL  ,
  name varchar(255) DEFAULT NULL ,
  process_definition_id int DEFAULT NULL ,
  state int DEFAULT NULL ,
  recovery int DEFAULT NULL ,
  start_time timestamp DEFAULT NULL ,
  end_time timestamp DEFAULT NULL ,
  run_times int DEFAULT NULL ,
  host varchar(135) DEFAULT NULL ,
  command_type int DEFAULT NULL ,
  command_param text ,
  task_depend_type int DEFAULT NULL ,
  max_try_times int DEFAULT '0' ,
  failure_strategy int DEFAULT '0' ,
  warning_type int DEFAULT '0' ,
  warning_group_id int DEFAULT NULL ,
  schedule_time timestamp DEFAULT NULL ,
  command_start_time timestamp DEFAULT NULL ,
  global_params text ,
  process_instance_json text ,
  flag int DEFAULT '1' ,
  update_time timestamp NULL ,
  is_sub_process int DEFAULT '0' ,
  executor_id int NOT NULL ,
  locations text ,
  connects text ,
  history_cmd text ,
  dependence_schedule_times text ,
  process_instance_priority int DEFAULT NULL ,
  worker_group varchar(64) ,
  timeout int DEFAULT '0' ,
  tenant_id int NOT NULL DEFAULT '-1' ,
  var_pool text ,
  PRIMARY KEY (id)
) ;
  create index process_instance_index on t_ds_process_instance (process_definition_id,id);
  create index start_time_index on t_ds_process_instance (start_time);

--
-- Table structure for table t_ds_project
--

DROP TABLE IF EXISTS t_ds_project;
CREATE TABLE t_ds_project (
  id int NOT NULL  ,
  name varchar(100) DEFAULT NULL ,
  description varchar(200) DEFAULT NULL ,
  user_id int DEFAULT NULL ,
  flag int DEFAULT '1' ,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP ,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (id)
) ;
  create index user_id_index on t_ds_project (user_id);

--
-- Table structure for table t_ds_queue
--

DROP TABLE IF EXISTS t_ds_queue;
CREATE TABLE t_ds_queue (
  id int NOT NULL  ,
  queue_name varchar(64) DEFAULT NULL ,
  queue varchar(64) DEFAULT NULL ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
);


--
-- Table structure for table t_ds_relation_datasource_user
--

DROP TABLE IF EXISTS t_ds_relation_datasource_user;
CREATE TABLE t_ds_relation_datasource_user (
  id int NOT NULL  ,
  user_id int NOT NULL ,
  datasource_id int DEFAULT NULL ,
  perm int DEFAULT '1' ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;
;

--
-- Table structure for table t_ds_relation_process_instance
--

DROP TABLE IF EXISTS t_ds_relation_process_instance;
CREATE TABLE t_ds_relation_process_instance (
  id int NOT NULL  ,
  parent_process_instance_id int DEFAULT NULL ,
  parent_task_instance_id int DEFAULT NULL ,
  process_instance_id int DEFAULT NULL ,
  PRIMARY KEY (id)
) ;


--
-- Table structure for table t_ds_relation_project_user
--

DROP TABLE IF EXISTS t_ds_relation_project_user;
CREATE TABLE t_ds_relation_project_user (
  id int NOT NULL  ,
  user_id int NOT NULL ,
  project_id int DEFAULT NULL ,
  perm int DEFAULT '1' ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;
create index relation_project_user_id_index on t_ds_relation_project_user (user_id);

--
-- Table structure for table t_ds_relation_resources_user
--

DROP TABLE IF EXISTS t_ds_relation_resources_user;
CREATE TABLE t_ds_relation_resources_user (
  id int NOT NULL ,
  user_id int NOT NULL ,
  resources_id int DEFAULT NULL ,
  perm int DEFAULT '1' ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;

--
-- Table structure for table t_ds_relation_udfs_user
--

DROP TABLE IF EXISTS t_ds_relation_udfs_user;
CREATE TABLE t_ds_relation_udfs_user (
  id int NOT NULL  ,
  user_id int NOT NULL ,
  udf_id int DEFAULT NULL ,
  perm int DEFAULT '1' ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;
;

--
-- Table structure for table t_ds_resources
--

DROP TABLE IF EXISTS t_ds_resources;
CREATE TABLE t_ds_resources (
  id int NOT NULL  ,
  alias varchar(64) DEFAULT NULL ,
  file_name varchar(64) DEFAULT NULL ,
  description varchar(256) DEFAULT NULL ,
  user_id int DEFAULT NULL ,
  type int DEFAULT NULL ,
  size bigint DEFAULT NULL ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  pid int,
  full_name varchar(64),
  is_directory int,
  PRIMARY KEY (id),
  CONSTRAINT t_ds_resources_un UNIQUE (full_name, type)
) ;


--
-- Table structure for table t_ds_schedules
--

DROP TABLE IF EXISTS t_ds_schedules;
CREATE TABLE t_ds_schedules (
  id int NOT NULL  ,
  process_definition_id int NOT NULL ,
  start_time timestamp NOT NULL ,
  end_time timestamp NOT NULL ,
  crontab varchar(256) NOT NULL ,
  failure_strategy int NOT NULL ,
  user_id int NOT NULL ,
  release_state int NOT NULL ,
  warning_type int NOT NULL ,
  warning_group_id int DEFAULT NULL ,
  process_instance_priority int DEFAULT NULL ,
  worker_group varchar(64),
  create_time timestamp NOT NULL ,
  update_time timestamp NOT NULL ,
  PRIMARY KEY (id)
);

--
-- Table structure for table t_ds_session
--

DROP TABLE IF EXISTS t_ds_session;
CREATE TABLE t_ds_session (
  id varchar(64) NOT NULL ,
  user_id int DEFAULT NULL ,
  ip varchar(45) DEFAULT NULL ,
  last_login_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
);

--
-- Table structure for table t_ds_task_instance
--

DROP TABLE IF EXISTS t_ds_task_instance;
CREATE TABLE t_ds_task_instance (
  id int NOT NULL  ,
  name varchar(255) DEFAULT NULL ,
  task_type varchar(64) DEFAULT NULL ,
  process_definition_id int DEFAULT NULL ,
  process_instance_id int DEFAULT NULL ,
  task_json text ,
  state int DEFAULT NULL ,
  submit_time timestamp DEFAULT NULL ,
  start_time timestamp DEFAULT NULL ,
  end_time timestamp DEFAULT NULL ,
  host varchar(135) DEFAULT NULL ,
  execute_path varchar(200) DEFAULT NULL ,
  log_path varchar(200) DEFAULT NULL ,
  alert_flag int DEFAULT NULL ,
  retry_times int DEFAULT '0' ,
  pid int DEFAULT NULL ,
  app_link text ,
  flag int DEFAULT '1' ,
  retry_interval int DEFAULT NULL ,
  max_retry_times int DEFAULT NULL ,
  task_instance_priority int DEFAULT NULL ,
  worker_group varchar(64),
  executor_id int DEFAULT NULL ,
  first_submit_time timestamp DEFAULT NULL ,
  delay_time int DEFAULT '0' ,
  var_pool text ,
  PRIMARY KEY (id),
  CONSTRAINT foreign_key_instance_id FOREIGN KEY(process_instance_id) REFERENCES t_ds_process_instance(id) ON DELETE CASCADE
) ;

--
-- Table structure for table t_ds_tenant
--

DROP TABLE IF EXISTS t_ds_tenant;
CREATE TABLE t_ds_tenant (
  id int NOT NULL  ,
  tenant_code varchar(64) DEFAULT NULL ,
  description varchar(256) DEFAULT NULL ,
  queue_id int DEFAULT NULL ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;

--
-- Table structure for table t_ds_udfs
--

DROP TABLE IF EXISTS t_ds_udfs;
CREATE TABLE t_ds_udfs (
  id int NOT NULL  ,
  user_id int NOT NULL ,
  func_name varchar(100) NOT NULL ,
  class_name varchar(255) NOT NULL ,
  type int NOT NULL ,
  arg_types varchar(255) DEFAULT NULL ,
  database varchar(255) DEFAULT NULL ,
  description varchar(255) DEFAULT NULL ,
  resource_id int NOT NULL ,
  resource_name varchar(255) NOT NULL ,
  create_time timestamp NOT NULL ,
  update_time timestamp NOT NULL ,
  PRIMARY KEY (id)
) ;

--
-- Table structure for table t_ds_user
--

DROP TABLE IF EXISTS t_ds_user;
CREATE TABLE t_ds_user (
  id int NOT NULL  ,
  user_name varchar(64) DEFAULT NULL ,
  user_password varchar(64) DEFAULT NULL ,
  user_type int DEFAULT NULL ,
  email varchar(64) DEFAULT NULL ,
  phone varchar(11) DEFAULT NULL ,
  tenant_id int DEFAULT NULL ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  queue varchar(64) DEFAULT NULL ,
  state int DEFAULT 1 ,
  PRIMARY KEY (id)
);
comment on column t_ds_user.state is 'state 0:disable 1:enable';

--
-- Table structure for table t_ds_version
--

DROP TABLE IF EXISTS t_ds_version;
CREATE TABLE t_ds_version (
  id int NOT NULL ,
  version varchar(200) NOT NULL,
  PRIMARY KEY (id)
) ;
create index version_index on t_ds_version(version);

--
-- Table structure for table t_ds_worker_group
--

DROP TABLE IF EXISTS t_ds_worker_group;
CREATE TABLE t_ds_worker_group (
  id bigint NOT NULL  ,
  name varchar(256) NOT NULL ,
  addr_list text DEFAULT NULL ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id) ,
  CONSTRAINT name_unique UNIQUE (name)
) ;

--
-- Table structure for table t_ds_worker_server
--

DROP TABLE IF EXISTS t_ds_worker_server;
CREATE TABLE t_ds_worker_server (
  id int NOT NULL  ,
  host varchar(45) DEFAULT NULL ,
  port int DEFAULT NULL ,
  zk_directory varchar(64)   DEFAULT NULL ,
  res_info varchar(255) DEFAULT NULL ,
  create_time timestamp DEFAULT NULL ,
  last_heartbeat_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;


DROP SEQUENCE IF EXISTS t_ds_access_token_id_sequence;
CREATE SEQUENCE  t_ds_access_token_id_sequence;
ALTER TABLE t_ds_access_token ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_access_token_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_alert_id_sequence;
CREATE SEQUENCE  t_ds_alert_id_sequence;
ALTER TABLE t_ds_alert ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_alert_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_alertgroup_id_sequence;
CREATE SEQUENCE  t_ds_alertgroup_id_sequence;
ALTER TABLE t_ds_alertgroup ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_alertgroup_id_sequence');

DROP SEQUENCE IF EXISTS t_ds_command_id_sequence;
CREATE SEQUENCE  t_ds_command_id_sequence;
ALTER TABLE t_ds_command ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_command_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_datasource_id_sequence;
CREATE SEQUENCE  t_ds_datasource_id_sequence;
ALTER TABLE t_ds_datasource ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_datasource_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_process_definition_id_sequence;
CREATE SEQUENCE  t_ds_process_definition_id_sequence;
ALTER TABLE t_ds_process_definition ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_process_definition_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_process_definition_version_id_sequence;
CREATE SEQUENCE  t_ds_process_definition_version_id_sequence;
ALTER TABLE t_ds_process_definition_version ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_process_definition_version_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_process_instance_id_sequence;
CREATE SEQUENCE  t_ds_process_instance_id_sequence;
ALTER TABLE t_ds_process_instance ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_process_instance_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_project_id_sequence;
CREATE SEQUENCE  t_ds_project_id_sequence;
ALTER TABLE t_ds_project ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_project_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_queue_id_sequence;
CREATE SEQUENCE  t_ds_queue_id_sequence;
ALTER TABLE t_ds_queue ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_queue_id_sequence');

DROP SEQUENCE IF EXISTS t_ds_relation_datasource_user_id_sequence;
CREATE SEQUENCE  t_ds_relation_datasource_user_id_sequence;
ALTER TABLE t_ds_relation_datasource_user ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_relation_datasource_user_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_relation_process_instance_id_sequence;
CREATE SEQUENCE  t_ds_relation_process_instance_id_sequence;
ALTER TABLE t_ds_relation_process_instance ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_relation_process_instance_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_relation_project_user_id_sequence;
CREATE SEQUENCE  t_ds_relation_project_user_id_sequence;
ALTER TABLE t_ds_relation_project_user ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_relation_project_user_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_relation_resources_user_id_sequence;
CREATE SEQUENCE  t_ds_relation_resources_user_id_sequence;
ALTER TABLE t_ds_relation_resources_user ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_relation_resources_user_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_relation_udfs_user_id_sequence;
CREATE SEQUENCE  t_ds_relation_udfs_user_id_sequence;
ALTER TABLE t_ds_relation_udfs_user ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_relation_udfs_user_id_sequence');

DROP SEQUENCE IF EXISTS t_ds_resources_id_sequence;
CREATE SEQUENCE  t_ds_resources_id_sequence;
ALTER TABLE t_ds_resources ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_resources_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_schedules_id_sequence;
CREATE SEQUENCE  t_ds_schedules_id_sequence;
ALTER TABLE t_ds_schedules ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_schedules_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_task_instance_id_sequence;
CREATE SEQUENCE  t_ds_task_instance_id_sequence;
ALTER TABLE t_ds_task_instance ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_task_instance_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_tenant_id_sequence;
CREATE SEQUENCE  t_ds_tenant_id_sequence;
ALTER TABLE t_ds_tenant ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_tenant_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_udfs_id_sequence;
CREATE SEQUENCE  t_ds_udfs_id_sequence;
ALTER TABLE t_ds_udfs ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_udfs_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_user_id_sequence;
CREATE SEQUENCE  t_ds_user_id_sequence;
ALTER TABLE t_ds_user ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_user_id_sequence');

DROP SEQUENCE IF EXISTS t_ds_version_id_sequence;
CREATE SEQUENCE  t_ds_version_id_sequence;
ALTER TABLE t_ds_version ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_version_id_sequence');

DROP SEQUENCE IF EXISTS t_ds_worker_group_id_sequence;
CREATE SEQUENCE  t_ds_worker_group_id_sequence;
ALTER TABLE t_ds_worker_group ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_worker_group_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_worker_server_id_sequence;
CREATE SEQUENCE t_ds_worker_server_id_sequence;
ALTER TABLE t_ds_worker_server ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_worker_server_id_sequence');


-- Records of t_ds_user?user : admin , password : dolphinscheduler123
INSERT INTO t_ds_user(user_name, user_password, user_type, email, phone, tenant_id, state, create_time, update_time)
VALUES ('admin', '7ad2410b2f4c074479a8937a28a22b8f', '0', 'xxx@qq.com', '', '0', 1, '2018-03-27 15:48:50', '2018-10-24 17:40:22');

-- Records of t_ds_alertgroup，dolphinscheduler warning group
INSERT INTO t_ds_alertgroup(id,alert_instance_ids, create_user_id, group_name, description, create_time, update_time)
VALUES (1,'1,2', 1, 'default admin warning group', 'default admin warning group', '2018-11-29 10:20:39', '2018-11-29 10:20:39');

-- Records of t_ds_queue,default queue name : default
INSERT INTO t_ds_queue(queue_name, queue, create_time, update_time)
VALUES ('default', 'default', '2018-11-29 10:22:33', '2018-11-29 10:22:33');

-- Records of t_ds_queue,default queue name : default
INSERT INTO t_ds_version(version) VALUES ('1.4.0');

--
-- Table structure for table t_ds_plugin_define
--
DROP TABLE IF EXISTS t_ds_plugin_define;
CREATE TABLE t_ds_plugin_define (
  id serial NOT NULL,
  plugin_name varchar(100) NOT NULL,
  plugin_type varchar(100) NOT NULL,
  plugin_params text NULL,
  create_time timestamp NULL,
  update_time timestamp NULL,
  CONSTRAINT t_ds_plugin_define_pk PRIMARY KEY (id),
  CONSTRAINT t_ds_plugin_define_un UNIQUE (plugin_name, plugin_type)
);

--
-- Table structure for table t_ds_alert_plugin_instance
--
DROP TABLE IF EXISTS t_ds_alert_plugin_instance;
CREATE TABLE t_ds_alert_plugin_instance (
  id serial NOT NULL,
  plugin_define_id int4 NOT NULL,
  plugin_instance_params text NULL,
  create_time timestamp NULL,
  update_time timestamp NULL,
  instance_name varchar(200) NULL,
  CONSTRAINT t_ds_alert_plugin_instance_pk PRIMARY KEY (id)
);