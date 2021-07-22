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

-- ac_escheduler_T_t_escheduler_version
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_version;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_version()
   BEGIN
       drop table if exists t_escheduler_version;
       CREATE TABLE  IF NOT EXISTS  `t_escheduler_version` (
         `id` int(11) NOT NULL AUTO_INCREMENT,
         `version` varchar(200) NOT NULL,
         PRIMARY KEY (`id`),
         UNIQUE KEY `version_UNIQUE` (`version`)
       ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='version';

 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_version;
DROP PROCEDURE ac_escheduler_T_t_escheduler_version;

-- ac_escheduler_T_t_escheduler_user_C_queue
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_user_C_queue;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_user_C_queue()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_user'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='queue')
   THEN
         ALTER TABLE t_escheduler_user ADD COLUMN queue varchar(64) COMMENT 'queue' AFTER update_time;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_user_C_queue;
DROP PROCEDURE ac_escheduler_T_t_escheduler_user_C_queue;

-- ac_escheduler_T_t_escheduler_access_token
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_access_token;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_access_token()
   BEGIN
       drop table if exists t_escheduler_access_token;
       CREATE TABLE  IF NOT EXISTS  `t_escheduler_access_token` (
         `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
         `user_id` int(11) DEFAULT NULL COMMENT 'user id',
         `token` varchar(64) DEFAULT NULL COMMENT 'token',
         `expire_time` datetime DEFAULT NULL COMMENT 'end time of token ',
         `create_time` datetime DEFAULT NULL COMMENT 'create time',
         `update_time` datetime DEFAULT NULL COMMENT 'update time',
         PRIMARY KEY (`id`)
       ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_access_token;
DROP PROCEDURE ac_escheduler_T_t_escheduler_access_token;

-- ac_escheduler_T_t_escheduler_error_command
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_error_command;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_error_command()
   BEGIN
       drop table if exists t_escheduler_error_command;
       CREATE TABLE  IF NOT EXISTS  `t_escheduler_error_command` (
           `id` int(11) NOT NULL COMMENT 'key',
           `command_type` tinyint(4) NULL DEFAULT NULL COMMENT 'command type',
           `executor_id` int(11) NULL DEFAULT NULL COMMENT 'executor id',
           `process_definition_id` int(11) NULL DEFAULT NULL COMMENT 'process definition id',
           `command_param` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT 'json command parameters',
           `task_depend_type` tinyint(4) NULL DEFAULT NULL COMMENT 'task depend type',
           `failure_strategy` tinyint(4) NULL DEFAULT 0 COMMENT 'failure strategy',
           `warning_type` tinyint(4) NULL DEFAULT 0 COMMENT 'warning type',
           `warning_group_id` int(11) NULL DEFAULT NULL COMMENT 'warning group id',
           `schedule_time` datetime NULL DEFAULT NULL COMMENT 'scheduler time',
           `start_time` datetime NULL DEFAULT NULL COMMENT 'start time',
           `update_time` datetime NULL DEFAULT NULL COMMENT 'update time',
           `dependence` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT 'dependence',
           `process_instance_priority` int(11) NULL DEFAULT NULL COMMENT 'process instance priority, 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
           `worker_group_id` int(11) NULL DEFAULT -1 COMMENT 'worker group id',
           `message` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT 'message',
           PRIMARY KEY (`id`) USING BTREE
       ) ENGINE = InnoDB AUTO_INCREMENT=1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_error_command;
DROP PROCEDURE ac_escheduler_T_t_escheduler_error_command;

-- ac_escheduler_T_t_escheduler_worker_group
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_worker_group;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_worker_group()
   BEGIN
       drop table if exists t_escheduler_worker_group;
       CREATE TABLE  IF NOT EXISTS  `t_escheduler_worker_group` (
           `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
           `name` varchar(256)  NULL DEFAULT NULL COMMENT 'worker group name',
           `ip_list` varchar(256)  NULL DEFAULT NULL COMMENT 'worker ip list. split by [,] ',
           `create_time` datetime NULL DEFAULT NULL COMMENT 'create time',
           `update_time` datetime NULL DEFAULT NULL COMMENT 'update time',
           PRIMARY KEY (`id`) USING BTREE
       ) ENGINE = InnoDB AUTO_INCREMENT=1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_worker_group;
DROP PROCEDURE ac_escheduler_T_t_escheduler_worker_group;

-- ac_escheduler_T_t_escheduler_task_instance_C_worker_group_id
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_task_instance_C_worker_group_id;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_task_instance_C_worker_group_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='worker_group_id')
   THEN
         ALTER TABLE t_escheduler_task_instance ADD COLUMN `worker_group_id` int(11) NULL DEFAULT -1 COMMENT 'worker group id' AFTER `task_instance_priority`;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_task_instance_C_worker_group_id;
DROP PROCEDURE ac_escheduler_T_t_escheduler_task_instance_C_worker_group_id;


-- ac_escheduler_T_t_escheduler_command_C_worker_group_id
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_command_C_worker_group_id;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_command_C_worker_group_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_command'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='worker_group_id')
   THEN
         ALTER TABLE t_escheduler_command ADD COLUMN `worker_group_id` int(11) NULL DEFAULT -1 COMMENT 'worker group id' AFTER `process_instance_priority`;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_command_C_worker_group_id;
DROP PROCEDURE ac_escheduler_T_t_escheduler_command_C_worker_group_id;

-- ac_escheduler_T_t_escheduler_schedules_C_worker_group_id
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_schedules_C_worker_group_id;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_schedules_C_worker_group_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_schedules'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='worker_group_id')
   THEN
         ALTER TABLE t_escheduler_schedules ADD COLUMN `worker_group_id` int(11) NULL DEFAULT -1 COMMENT 'worker group id' AFTER `process_instance_priority`;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_schedules_C_worker_group_id;
DROP PROCEDURE ac_escheduler_T_t_escheduler_schedules_C_worker_group_id;

-- ac_escheduler_T_t_escheduler_process_instance_C_worker_group_id
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_process_instance_C_worker_group_id;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_process_instance_C_worker_group_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_process_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='worker_group_id')
   THEN
         ALTER TABLE t_escheduler_process_instance ADD COLUMN `worker_group_id` int(11) NULL DEFAULT -1 COMMENT 'worker group id' AFTER `process_instance_priority`;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_process_instance_C_worker_group_id;
DROP PROCEDURE ac_escheduler_T_t_escheduler_process_instance_C_worker_group_id;

-- ac_escheduler_T_t_escheduler_process_instance_C_timeout
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_process_instance_C_timeout;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_process_instance_C_timeout()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_process_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='timeout')
   THEN
         ALTER TABLE `t_escheduler_process_instance` ADD COLUMN `timeout` int(11) NULL DEFAULT 0  COMMENT 'time out' AFTER `worker_group_id`;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_process_instance_C_timeout;
DROP PROCEDURE ac_escheduler_T_t_escheduler_process_instance_C_timeout;

-- ac_escheduler_T_t_escheduler_process_definition_C_timeout
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_process_definition_C_timeout;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_process_definition_C_timeout()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_process_definition'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='timeout')
   THEN
         ALTER TABLE `t_escheduler_process_definition` ADD COLUMN `timeout` int(11) NULL DEFAULT 0 COMMENT 'time out' AFTER `create_time`;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_process_definition_C_timeout;
DROP PROCEDURE ac_escheduler_T_t_escheduler_process_definition_C_timeout;

-- ac_escheduler_T_t_escheduler_process_definition_C_tenant_id
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_process_definition_C_tenant_id;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_process_definition_C_tenant_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_process_definition'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='tenant_id')
   THEN
         ALTER TABLE `t_escheduler_process_definition` ADD COLUMN `tenant_id` int(11) NOT NULL DEFAULT -1 COMMENT 'tenant id' AFTER `timeout`;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_process_definition_C_tenant_id;
DROP PROCEDURE ac_escheduler_T_t_escheduler_process_definition_C_tenant_id;

-- ac_escheduler_T_t_escheduler_process_instance_C_tenant_id
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_process_instance_C_tenant_id;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_process_instance_C_tenant_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_process_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='tenant_id')
   THEN
         ALTER TABLE `t_escheduler_process_instance` ADD COLUMN `tenant_id` int(11) NOT NULL DEFAULT -1 COMMENT 'tenant id' AFTER `timeout`;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_process_instance_C_tenant_id;
DROP PROCEDURE ac_escheduler_T_t_escheduler_process_instance_C_tenant_id;
