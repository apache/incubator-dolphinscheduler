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

SET sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));
-- ut_dolphin_T_t_ds_access_token
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_access_token;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_access_token()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_access_token'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_access_token RENAME t_ds_access_token;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_access_token;
DROP PROCEDURE ut_dolphin_T_t_ds_access_token;

-- ut_dolphin_T_t_ds_alert
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_alert;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_alert()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_alert'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_alert RENAME t_ds_alert;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_alert;
DROP PROCEDURE ut_dolphin_T_t_ds_alert;

-- ut_dolphin_T_t_ds_alertgroup
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_alertgroup;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_alertgroup()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_alertgroup'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_alertgroup RENAME t_ds_alertgroup;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_alertgroup;
DROP PROCEDURE ut_dolphin_T_t_ds_alertgroup;

-- ut_dolphin_T_t_ds_command
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_command;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_command()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_command'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_command RENAME t_ds_command;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_command;
DROP PROCEDURE ut_dolphin_T_t_ds_command;

-- ut_dolphin_T_t_ds_datasource
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_datasource;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_datasource()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_datasource'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_datasource RENAME t_ds_datasource;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_datasource;
DROP PROCEDURE ut_dolphin_T_t_ds_datasource;

-- ut_dolphin_T_t_ds_error_command
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_error_command;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_error_command()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_error_command'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_error_command RENAME t_ds_error_command;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_error_command;
DROP PROCEDURE ut_dolphin_T_t_ds_error_command;

-- ut_dolphin_T_t_ds_master_server
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_master_server;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_master_server()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_master_server'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_master_server RENAME t_ds_master_server;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_master_server;
DROP PROCEDURE ut_dolphin_T_t_ds_master_server;

-- ut_dolphin_T_t_ds_process_definition
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_process_definition;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_process_definition()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_process_definition'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_process_definition RENAME t_ds_process_definition;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_process_definition;
DROP PROCEDURE ut_dolphin_T_t_ds_process_definition;

-- ut_dolphin_T_t_ds_process_instance
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_process_instance;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_process_instance()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_process_instance'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_process_instance RENAME t_ds_process_instance;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_process_instance;
DROP PROCEDURE ut_dolphin_T_t_ds_process_instance;

-- ut_dolphin_T_t_ds_project
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_project;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_project()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_project'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_project RENAME t_ds_project;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_project;
DROP PROCEDURE ut_dolphin_T_t_ds_project;

-- ut_dolphin_T_t_ds_queue
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_queue;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_queue()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_queue'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_queue RENAME t_ds_queue;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_queue;
DROP PROCEDURE ut_dolphin_T_t_ds_queue;

-- ut_dolphin_T_t_ds_relation_datasource_user
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_relation_datasource_user;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_relation_datasource_user()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_relation_datasource_user'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_relation_datasource_user RENAME t_ds_relation_datasource_user;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_relation_datasource_user;
DROP PROCEDURE ut_dolphin_T_t_ds_relation_datasource_user;

-- ut_dolphin_T_t_ds_relation_process_instance
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_relation_process_instance;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_relation_process_instance()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_relation_process_instance'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_relation_process_instance RENAME t_ds_relation_process_instance;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_relation_process_instance;
DROP PROCEDURE ut_dolphin_T_t_ds_relation_process_instance;

-- ut_dolphin_T_t_ds_relation_project_user
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_relation_project_user;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_relation_project_user()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_relation_project_user'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_relation_project_user RENAME t_ds_relation_project_user;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_relation_project_user;
DROP PROCEDURE ut_dolphin_T_t_ds_relation_project_user;

-- ut_dolphin_T_t_ds_relation_resources_user
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_relation_resources_user;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_relation_resources_user()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_relation_resources_user'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_relation_resources_user RENAME t_ds_relation_resources_user;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_relation_resources_user;
DROP PROCEDURE ut_dolphin_T_t_ds_relation_resources_user;

-- ut_dolphin_T_t_ds_relation_udfs_user
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_relation_udfs_user;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_relation_udfs_user()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_relation_udfs_user'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_relation_udfs_user RENAME t_ds_relation_udfs_user;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_relation_udfs_user;
DROP PROCEDURE ut_dolphin_T_t_ds_relation_udfs_user;

-- ut_dolphin_T_t_ds_relation_user_alertgroup
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_relation_user_alertgroup;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_relation_user_alertgroup()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_relation_user_alertgroup'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_relation_user_alertgroup RENAME t_ds_relation_user_alertgroup;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_relation_user_alertgroup;
DROP PROCEDURE ut_dolphin_T_t_ds_relation_user_alertgroup;

-- ut_dolphin_T_t_ds_resources
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_resources;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_resources()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_resources'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_resources RENAME t_ds_resources;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_resources;
DROP PROCEDURE ut_dolphin_T_t_ds_resources;

-- ut_dolphin_T_t_ds_schedules
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_schedules;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_schedules()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_schedules'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_schedules RENAME t_ds_schedules;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_schedules;
DROP PROCEDURE ut_dolphin_T_t_ds_schedules;

-- ut_dolphin_T_t_ds_session
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_session;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_session()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_session'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_session RENAME t_ds_session;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_session;
DROP PROCEDURE ut_dolphin_T_t_ds_session;

-- ut_dolphin_T_t_ds_task_instance
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_task_instance;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_task_instance()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_task_instance'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_task_instance RENAME t_ds_task_instance;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_task_instance;
DROP PROCEDURE ut_dolphin_T_t_ds_task_instance;

-- ut_dolphin_T_t_ds_tenant
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_tenant;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_tenant()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_tenant'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_tenant RENAME t_ds_tenant;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_tenant;
DROP PROCEDURE ut_dolphin_T_t_ds_tenant;

-- ut_dolphin_T_t_ds_udfs
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_udfs;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_udfs()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_udfs'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_udfs RENAME t_ds_udfs;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_udfs;
DROP PROCEDURE ut_dolphin_T_t_ds_udfs;

-- ut_dolphin_T_t_ds_user
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_user;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_user()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_user'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_user RENAME t_ds_user;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_user;
DROP PROCEDURE ut_dolphin_T_t_ds_user;

-- ut_dolphin_T_t_ds_version
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_version;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_version()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_version'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_version RENAME t_ds_version;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_version;
DROP PROCEDURE ut_dolphin_T_t_ds_version;

-- ut_dolphin_T_t_ds_worker_group
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_worker_group;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_worker_group()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_worker_group'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_worker_group RENAME t_ds_worker_group;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_worker_group;
DROP PROCEDURE ut_dolphin_T_t_ds_worker_group;

-- ut_dolphin_T_t_ds_worker_server
drop PROCEDURE if EXISTS ut_dolphin_T_t_ds_worker_server;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_worker_server()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_worker_server'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_worker_server RENAME t_ds_worker_server;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_worker_server;
DROP PROCEDURE ut_dolphin_T_t_ds_worker_server;

-- uc_dolphin_T_t_ds_alertgroup_C_desc
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_alertgroup_C_desc;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_alertgroup_C_desc()
   BEGIN
       IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_alertgroup'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='desc')
   THEN
         ALTER TABLE t_ds_alertgroup CHANGE COLUMN `desc` description varchar(255);
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_alertgroup_C_desc;
DROP PROCEDURE uc_dolphin_T_t_ds_alertgroup_C_desc;

-- uc_dolphin_T_t_ds_process_definition_C_desc
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_process_definition_C_desc;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_process_definition_C_desc()
   BEGIN
       IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_process_definition'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='desc')
   THEN
         ALTER TABLE t_ds_process_definition CHANGE COLUMN `desc` description text;
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_process_definition_C_desc;
DROP PROCEDURE uc_dolphin_T_t_ds_process_definition_C_desc;

-- uc_dolphin_T_t_ds_project_C_desc
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_project_C_desc;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_project_C_desc()
   BEGIN
       IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_project'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='desc')
   THEN
         ALTER TABLE t_ds_project CHANGE COLUMN `desc` description varchar(200);
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_project_C_desc;
DROP PROCEDURE uc_dolphin_T_t_ds_project_C_desc;

-- uc_dolphin_T_t_ds_resources_C_desc
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_resources_C_desc;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_resources_C_desc()
   BEGIN
       IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_resources'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='desc')
   THEN
         ALTER TABLE t_ds_resources CHANGE COLUMN `desc` description varchar(256);
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_resources_C_desc;
DROP PROCEDURE uc_dolphin_T_t_ds_resources_C_desc;

-- uc_dolphin_T_t_ds_tenant_C_desc
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_tenant_C_desc;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_tenant_C_desc()
   BEGIN
       IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_tenant'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='desc')
   THEN
         ALTER TABLE t_ds_tenant CHANGE COLUMN `desc` description varchar(256);
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_tenant_C_desc;
DROP PROCEDURE uc_dolphin_T_t_ds_tenant_C_desc;

-- uc_dolphin_T_t_ds_udfs_C_desc
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_udfs_C_desc;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_udfs_C_desc()
   BEGIN
       IF EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_udfs'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='desc')
   THEN
         ALTER TABLE t_ds_udfs CHANGE COLUMN `desc` description varchar(255);
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_udfs_C_desc;
DROP PROCEDURE uc_dolphin_T_t_ds_udfs_C_desc;