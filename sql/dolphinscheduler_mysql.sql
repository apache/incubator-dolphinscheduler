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

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for QRTZ_BLOB_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_BLOB_TRIGGERS`;
CREATE TABLE `QRTZ_BLOB_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `BLOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `SCHED_NAME` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_BLOB_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_CALENDARS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_CALENDARS`;
CREATE TABLE `QRTZ_CALENDARS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `CALENDAR_NAME` varchar(200) NOT NULL,
  `CALENDAR` blob NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`CALENDAR_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_CALENDARS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_CRON_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_CRON_TRIGGERS`;
CREATE TABLE `QRTZ_CRON_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `CRON_EXPRESSION` varchar(120) NOT NULL,
  `TIME_ZONE_ID` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_CRON_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_CRON_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_FIRED_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_FIRED_TRIGGERS`;
CREATE TABLE `QRTZ_FIRED_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `ENTRY_ID` varchar(95) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `INSTANCE_NAME` varchar(200) NOT NULL,
  `FIRED_TIME` bigint(13) NOT NULL,
  `SCHED_TIME` bigint(13) NOT NULL,
  `PRIORITY` int(11) NOT NULL,
  `STATE` varchar(16) NOT NULL,
  `JOB_NAME` varchar(200) DEFAULT NULL,
  `JOB_GROUP` varchar(200) DEFAULT NULL,
  `IS_NONCONCURRENT` varchar(1) DEFAULT NULL,
  `REQUESTS_RECOVERY` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`ENTRY_ID`),
  KEY `IDX_QRTZ_FT_TRIG_INST_NAME` (`SCHED_NAME`,`INSTANCE_NAME`),
  KEY `IDX_QRTZ_FT_INST_JOB_REQ_RCVRY` (`SCHED_NAME`,`INSTANCE_NAME`,`REQUESTS_RECOVERY`),
  KEY `IDX_QRTZ_FT_J_G` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_FT_JG` (`SCHED_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_FT_T_G` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `IDX_QRTZ_FT_TG` (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_FIRED_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_JOB_DETAILS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_JOB_DETAILS`;
CREATE TABLE `QRTZ_JOB_DETAILS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `JOB_NAME` varchar(200) NOT NULL,
  `JOB_GROUP` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `JOB_CLASS_NAME` varchar(250) NOT NULL,
  `IS_DURABLE` varchar(1) NOT NULL,
  `IS_NONCONCURRENT` varchar(1) NOT NULL,
  `IS_UPDATE_DATA` varchar(1) NOT NULL,
  `REQUESTS_RECOVERY` varchar(1) NOT NULL,
  `JOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_J_REQ_RECOVERY` (`SCHED_NAME`,`REQUESTS_RECOVERY`),
  KEY `IDX_QRTZ_J_GRP` (`SCHED_NAME`,`JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_JOB_DETAILS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_LOCKS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_LOCKS`;
CREATE TABLE `QRTZ_LOCKS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `LOCK_NAME` varchar(40) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`LOCK_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_LOCKS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_PAUSED_TRIGGER_GRPS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_PAUSED_TRIGGER_GRPS`;
CREATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_PAUSED_TRIGGER_GRPS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_SCHEDULER_STATE
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_SCHEDULER_STATE`;
CREATE TABLE `QRTZ_SCHEDULER_STATE` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `INSTANCE_NAME` varchar(200) NOT NULL,
  `LAST_CHECKIN_TIME` bigint(13) NOT NULL,
  `CHECKIN_INTERVAL` bigint(13) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`INSTANCE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_SCHEDULER_STATE
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_SIMPLE_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_SIMPLE_TRIGGERS`;
CREATE TABLE `QRTZ_SIMPLE_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `REPEAT_COUNT` bigint(7) NOT NULL,
  `REPEAT_INTERVAL` bigint(12) NOT NULL,
  `TIMES_TRIGGERED` bigint(10) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_SIMPLE_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_SIMPROP_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_SIMPROP_TRIGGERS`;
CREATE TABLE `QRTZ_SIMPROP_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `STR_PROP_1` varchar(512) DEFAULT NULL,
  `STR_PROP_2` varchar(512) DEFAULT NULL,
  `STR_PROP_3` varchar(512) DEFAULT NULL,
  `INT_PROP_1` int(11) DEFAULT NULL,
  `INT_PROP_2` int(11) DEFAULT NULL,
  `LONG_PROP_1` bigint(20) DEFAULT NULL,
  `LONG_PROP_2` bigint(20) DEFAULT NULL,
  `DEC_PROP_1` decimal(13,4) DEFAULT NULL,
  `DEC_PROP_2` decimal(13,4) DEFAULT NULL,
  `BOOL_PROP_1` varchar(1) DEFAULT NULL,
  `BOOL_PROP_2` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_SIMPROP_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS `QRTZ_TRIGGERS`;
CREATE TABLE `QRTZ_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `JOB_NAME` varchar(200) NOT NULL,
  `JOB_GROUP` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `NEXT_FIRE_TIME` bigint(13) DEFAULT NULL,
  `PREV_FIRE_TIME` bigint(13) DEFAULT NULL,
  `PRIORITY` int(11) DEFAULT NULL,
  `TRIGGER_STATE` varchar(16) NOT NULL,
  `TRIGGER_TYPE` varchar(8) NOT NULL,
  `START_TIME` bigint(13) NOT NULL,
  `END_TIME` bigint(13) DEFAULT NULL,
  `CALENDAR_NAME` varchar(200) DEFAULT NULL,
  `MISFIRE_INSTR` smallint(2) DEFAULT NULL,
  `JOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `IDX_QRTZ_T_J` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_T_JG` (`SCHED_NAME`,`JOB_GROUP`),
  KEY `IDX_QRTZ_T_C` (`SCHED_NAME`,`CALENDAR_NAME`),
  KEY `IDX_QRTZ_T_G` (`SCHED_NAME`,`TRIGGER_GROUP`),
  KEY `IDX_QRTZ_T_STATE` (`SCHED_NAME`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_N_STATE` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_N_G_STATE` (`SCHED_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_NEXT_FIRE_TIME` (`SCHED_NAME`,`NEXT_FIRE_TIME`),
  KEY `IDX_QRTZ_T_NFT_ST` (`SCHED_NAME`,`TRIGGER_STATE`,`NEXT_FIRE_TIME`),
  KEY `IDX_QRTZ_T_NFT_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`),
  KEY `IDX_QRTZ_T_NFT_ST_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_STATE`),
  KEY `IDX_QRTZ_T_NFT_ST_MISFIRE_GRP` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
  CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) REFERENCES `QRTZ_JOB_DETAILS` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of QRTZ_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_access_token
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_access_token`;
CREATE TABLE `t_ds_access_token` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `user_id` int(11) DEFAULT NULL COMMENT 'user id',
  `token` varchar(64) DEFAULT NULL COMMENT 'token',
  `expire_time` datetime DEFAULT NULL COMMENT 'end time of token ',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_access_token
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_alert
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_alert`;
CREATE TABLE `t_ds_alert` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `title` varchar(64) DEFAULT NULL COMMENT 'title',
  `content` text COMMENT 'Message content (can be email, can be SMS. Mail is stored in JSON map, and SMS is string)',
  `alert_status` tinyint(4) DEFAULT '0' COMMENT '0:wait running,1:success,2:failed',
  `log` text COMMENT 'log',
  `alertgroup_id` int(11) DEFAULT NULL COMMENT 'alert group id',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_alert
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_alertgroup
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_alertgroup`;
CREATE TABLE `t_ds_alertgroup`(
                                  `id`             int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
                                  `alert_instance_ids` varchar (255) DEFAULT NULL COMMENT 'alert instance ids',
                                  `create_user_id` int(11) DEFAULT NULL COMMENT 'create user id',
                                  `group_name`     varchar(255) DEFAULT NULL COMMENT 'group name',
                                  `description`    varchar(255) DEFAULT NULL,
                                  `create_time`    datetime     DEFAULT NULL COMMENT 'create time',
                                  `update_time`    datetime     DEFAULT NULL COMMENT 'update time',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_alertgroup
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_command
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_command`;
CREATE TABLE `t_ds_command` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `command_type` tinyint(4) DEFAULT NULL COMMENT 'Command type: 0 start workflow, 1 start execution from current node, 2 resume fault-tolerant workflow, 3 resume pause process, 4 start execution from failed node, 5 complement, 6 schedule, 7 rerun, 8 pause, 9 stop, 10 resume waiting thread',
  `process_definition_id` int(11) DEFAULT NULL COMMENT 'process definition id',
  `command_param` text COMMENT 'json command parameters',
  `task_depend_type` tinyint(4) DEFAULT NULL COMMENT 'Node dependency type: 0 current node, 1 forward, 2 backward',
  `failure_strategy` tinyint(4) DEFAULT '0' COMMENT 'Failed policy: 0 end, 1 continue',
  `warning_type` tinyint(4) DEFAULT '0' COMMENT 'Alarm type: 0 is not sent, 1 process is sent successfully, 2 process is sent failed, 3 process is sent successfully and all failures are sent',
  `warning_group_id` int(11) DEFAULT NULL COMMENT 'warning group',
  `schedule_time` datetime DEFAULT NULL COMMENT 'schedule time',
  `start_time` datetime DEFAULT NULL COMMENT 'start time',
  `executor_id` int(11) DEFAULT NULL COMMENT 'executor id',
  `dependence` varchar(255) DEFAULT NULL COMMENT 'dependence',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  `process_instance_priority` int(11) DEFAULT NULL COMMENT 'process instance priority: 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group` varchar(64)  COMMENT 'worker group',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_command
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_datasource
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_datasource`;
CREATE TABLE `t_ds_datasource` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` varchar(64) NOT NULL COMMENT 'data source name',
  `note` varchar(256) DEFAULT NULL COMMENT 'description',
  `type` tinyint(4) NOT NULL COMMENT 'data source type: 0:mysql,1:postgresql,2:hive,3:spark',
  `user_id` int(11) NOT NULL COMMENT 'the creator id',
  `connection_params` text NOT NULL COMMENT 'json connection params',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_datasource
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_error_command
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_error_command`;
CREATE TABLE `t_ds_error_command` (
  `id` int(11) NOT NULL COMMENT 'key',
  `command_type` tinyint(4) DEFAULT NULL COMMENT 'command type',
  `executor_id` int(11) DEFAULT NULL COMMENT 'executor id',
  `process_definition_id` int(11) DEFAULT NULL COMMENT 'process definition id',
  `command_param` text COMMENT 'json command parameters',
  `task_depend_type` tinyint(4) DEFAULT NULL COMMENT 'task depend type',
  `failure_strategy` tinyint(4) DEFAULT '0' COMMENT 'failure strategy',
  `warning_type` tinyint(4) DEFAULT '0' COMMENT 'warning type',
  `warning_group_id` int(11) DEFAULT NULL COMMENT 'warning group id',
  `schedule_time` datetime DEFAULT NULL COMMENT 'scheduler time',
  `start_time` datetime DEFAULT NULL COMMENT 'start time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  `dependence` text COMMENT 'dependence',
  `process_instance_priority` int(11) DEFAULT NULL COMMENT 'process instance priority, 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group` varchar(64)  COMMENT 'worker group',
  `message` text COMMENT 'message',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of t_ds_error_command
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_process_definition
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_process_definition`;
CREATE TABLE `t_ds_process_definition` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` varchar(255) DEFAULT NULL COMMENT 'process definition name',
  `version` int(11) DEFAULT NULL COMMENT 'process definition version',
  `release_state` tinyint(4) DEFAULT NULL COMMENT 'process definition release state：0:offline,1:online',
  `project_id` int(11) DEFAULT NULL COMMENT 'project id',
  `user_id` int(11) DEFAULT NULL COMMENT 'process definition creator id',
  `process_definition_json` longtext COMMENT 'process definition json content',
  `description` text,
  `global_params` text COMMENT 'global parameters',
  `flag` tinyint(4) DEFAULT NULL COMMENT '0 not available, 1 available',
  `locations` text COMMENT 'Node location information',
  `connects` text COMMENT 'Node connection information',
  `warning_group_id` int(11) DEFAULT NULL COMMENT 'alert group id',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `timeout` int(11) DEFAULT '0' COMMENT 'time out',
  `tenant_id` int(11) NOT NULL DEFAULT '-1' COMMENT 'tenant id',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  `modify_by` varchar(255) DEFAULT NULL,
  `resource_ids` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `process_definition_unique` (`name`,`project_id`),
  KEY `process_definition_index` (`project_id`,`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_process_definition
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_process_definition_version
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_process_definition_version`;
CREATE TABLE `t_ds_process_definition_version` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `process_definition_id` int(11) NOT NULL COMMENT 'process definition id',
  `version` int(11) DEFAULT NULL COMMENT 'process definition version',
  `process_definition_json` longtext COMMENT 'process definition json content',
  `description` text,
  `global_params` text COMMENT 'global parameters',
  `locations` text COMMENT 'Node location information',
  `connects` text COMMENT 'Node connection information',
  `warning_group_id` int(11) DEFAULT NULL COMMENT 'alert group id',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `timeout` int(11) DEFAULT '0' COMMENT 'time out',
  `resource_ids` varchar(255) DEFAULT NULL COMMENT 'resource ids',
  PRIMARY KEY (`id`),
  UNIQUE KEY `process_definition_id_and_version` (`process_definition_id`,`version`) USING BTREE,
  KEY `process_definition_index` (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=84 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_process_definition
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_process_instance
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_process_instance`;
CREATE TABLE `t_ds_process_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` varchar(255) DEFAULT NULL COMMENT 'process instance name',
  `process_definition_id` int(11) DEFAULT NULL COMMENT 'process definition id',
  `state` tinyint(4) DEFAULT NULL COMMENT 'process instance Status: 0 commit succeeded, 1 running, 2 prepare to pause, 3 pause, 4 prepare to stop, 5 stop, 6 fail, 7 succeed, 8 need fault tolerance, 9 kill, 10 wait for thread, 11 wait for dependency to complete',
  `recovery` tinyint(4) DEFAULT NULL COMMENT 'process instance failover flag：0:normal,1:failover instance',
  `start_time` datetime DEFAULT NULL COMMENT 'process instance start time',
  `end_time` datetime DEFAULT NULL COMMENT 'process instance end time',
  `run_times` int(11) DEFAULT NULL COMMENT 'process instance run times',
  `host` varchar(45) DEFAULT NULL COMMENT 'process instance host',
  `command_type` tinyint(4) DEFAULT NULL COMMENT 'command type',
  `command_param` text COMMENT 'json command parameters',
  `task_depend_type` tinyint(4) DEFAULT NULL COMMENT 'task depend type. 0: only current node,1:before the node,2:later nodes',
  `max_try_times` tinyint(4) DEFAULT '0' COMMENT 'max try times',
  `failure_strategy` tinyint(4) DEFAULT '0' COMMENT 'failure strategy. 0:end the process when node failed,1:continue running the other nodes when node failed',
  `warning_type` tinyint(4) DEFAULT '0' COMMENT 'warning type. 0:no warning,1:warning if process success,2:warning if process failed,3:warning if success',
  `warning_group_id` int(11) DEFAULT NULL COMMENT 'warning group id',
  `schedule_time` datetime DEFAULT NULL COMMENT 'schedule time',
  `command_start_time` datetime DEFAULT NULL COMMENT 'command start time',
  `global_params` text COMMENT 'global parameters',
  `process_instance_json` longtext COMMENT 'process instance json(copy的process definition 的json)',
  `flag` tinyint(4) DEFAULT '1' COMMENT 'flag',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_sub_process` int(11) DEFAULT '0' COMMENT 'flag, whether the process is sub process',
  `executor_id` int(11) NOT NULL COMMENT 'executor id',
  `locations` text COMMENT 'Node location information',
  `connects` text COMMENT 'Node connection information',
  `history_cmd` text COMMENT 'history commands of process instance operation',
  `dependence_schedule_times` text COMMENT 'depend schedule fire time',
  `process_instance_priority` int(11) DEFAULT NULL COMMENT 'process instance priority. 0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group` varchar(64) DEFAULT NULL COMMENT 'worker group id',
  `timeout` int(11) DEFAULT '0' COMMENT 'time out',
  `tenant_id` int(11) NOT NULL DEFAULT '-1' COMMENT 'tenant id',
  `var_pool` longtext COMMENT 'var_pool',
  PRIMARY KEY (`id`),
  KEY `process_instance_index` (`process_definition_id`,`id`) USING BTREE,
  KEY `start_time_index` (`start_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_process_instance
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_project
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_project`;
CREATE TABLE `t_ds_project` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` varchar(100) DEFAULT NULL COMMENT 'project name',
  `description` varchar(200) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL COMMENT 'creator id',
  `flag` tinyint(4) DEFAULT '1' COMMENT '0 not available, 1 available',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  KEY `user_id_index` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_project
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_queue
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_queue`;
CREATE TABLE `t_ds_queue` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `queue_name` varchar(64) DEFAULT NULL COMMENT 'queue name',
  `queue` varchar(64) DEFAULT NULL COMMENT 'yarn queue name',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_queue
-- ----------------------------
INSERT INTO `t_ds_queue` VALUES ('1', 'default', 'default', null, null);

-- ----------------------------
-- Table structure for t_ds_relation_datasource_user
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_relation_datasource_user`;
CREATE TABLE `t_ds_relation_datasource_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `user_id` int(11) NOT NULL COMMENT 'user id',
  `datasource_id` int(11) DEFAULT NULL COMMENT 'data source id',
  `perm` int(11) DEFAULT '1' COMMENT 'limits of authority',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_relation_datasource_user
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_relation_process_instance
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_relation_process_instance`;
CREATE TABLE `t_ds_relation_process_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `parent_process_instance_id` int(11) DEFAULT NULL COMMENT 'parent process instance id',
  `parent_task_instance_id` int(11) DEFAULT NULL COMMENT 'parent process instance id',
  `process_instance_id` int(11) DEFAULT NULL COMMENT 'child process instance id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_relation_process_instance
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_relation_project_user
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_relation_project_user`;
CREATE TABLE `t_ds_relation_project_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `user_id` int(11) NOT NULL COMMENT 'user id',
  `project_id` int(11) DEFAULT NULL COMMENT 'project id',
  `perm` int(11) DEFAULT '1' COMMENT 'limits of authority',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`),
  KEY `user_id_index` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_relation_project_user
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_relation_resources_user
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_relation_resources_user`;
CREATE TABLE `t_ds_relation_resources_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT 'user id',
  `resources_id` int(11) DEFAULT NULL COMMENT 'resource id',
  `perm` int(11) DEFAULT '1' COMMENT 'limits of authority',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_relation_resources_user
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_relation_udfs_user
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_relation_udfs_user`;
CREATE TABLE `t_ds_relation_udfs_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `user_id` int(11) NOT NULL COMMENT 'userid',
  `udf_id` int(11) DEFAULT NULL COMMENT 'udf id',
  `perm` int(11) DEFAULT '1' COMMENT 'limits of authority',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_ds_resources
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_resources`;
CREATE TABLE `t_ds_resources` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `alias` varchar(64) DEFAULT NULL COMMENT 'alias',
  `file_name` varchar(64) DEFAULT NULL COMMENT 'file name',
  `description` varchar(256) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL COMMENT 'user id',
  `type` tinyint(4) DEFAULT NULL COMMENT 'resource type,0:FILE，1:UDF',
  `size` bigint(20) DEFAULT NULL COMMENT 'resource size',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  `pid` int(11) DEFAULT NULL,
  `full_name` varchar(64) DEFAULT NULL,
  `is_directory` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `t_ds_resources_un` (`full_name`,`type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_resources
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_schedules
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_schedules`;
CREATE TABLE `t_ds_schedules` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `process_definition_id` int(11) NOT NULL COMMENT 'process definition id',
  `start_time` datetime NOT NULL COMMENT 'start time',
  `end_time` datetime NOT NULL COMMENT 'end time',
  `crontab` varchar(256) NOT NULL COMMENT 'crontab description',
  `failure_strategy` tinyint(4) NOT NULL COMMENT 'failure strategy. 0:end,1:continue',
  `user_id` int(11) NOT NULL COMMENT 'user id',
  `release_state` tinyint(4) NOT NULL COMMENT 'release state. 0:offline,1:online ',
  `warning_type` tinyint(4) NOT NULL COMMENT 'Alarm type: 0 is not sent, 1 process is sent successfully, 2 process is sent failed, 3 process is sent successfully and all failures are sent',
  `warning_group_id` int(11) DEFAULT NULL COMMENT 'alert group id',
  `process_instance_priority` int(11) DEFAULT NULL COMMENT 'process instance priority：0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group` varchar(256) DEFAULT '' COMMENT 'worker group id',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_schedules
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_session
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_session`;
CREATE TABLE `t_ds_session` (
  `id` varchar(64) NOT NULL COMMENT 'key',
  `user_id` int(11) DEFAULT NULL COMMENT 'user id',
  `ip` varchar(45) DEFAULT NULL COMMENT 'ip',
  `last_login_time` datetime DEFAULT NULL COMMENT 'last login time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_session
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_task_instance
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_task_instance`;
CREATE TABLE `t_ds_task_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` varchar(255) DEFAULT NULL COMMENT 'task name',
  `task_type` varchar(64) DEFAULT NULL COMMENT 'task type',
  `process_definition_id` int(11) DEFAULT NULL COMMENT 'process definition id',
  `process_instance_id` int(11) DEFAULT NULL COMMENT 'process instance id',
  `task_json` longtext COMMENT 'task content json',
  `state` tinyint(4) DEFAULT NULL COMMENT 'Status: 0 commit succeeded, 1 running, 2 prepare to pause, 3 pause, 4 prepare to stop, 5 stop, 6 fail, 7 succeed, 8 need fault tolerance, 9 kill, 10 wait for thread, 11 wait for dependency to complete',
  `submit_time` datetime DEFAULT NULL COMMENT 'task submit time',
  `start_time` datetime DEFAULT NULL COMMENT 'task start time',
  `end_time` datetime DEFAULT NULL COMMENT 'task end time',
  `host` varchar(45) DEFAULT NULL COMMENT 'host of task running on',
  `execute_path` varchar(200) DEFAULT NULL COMMENT 'task execute path in the host',
  `log_path` varchar(200) DEFAULT NULL COMMENT 'task log path',
  `alert_flag` tinyint(4) DEFAULT NULL COMMENT 'whether alert',
  `retry_times` int(4) DEFAULT '0' COMMENT 'task retry times',
  `pid` int(4) DEFAULT NULL COMMENT 'pid of task',
  `app_link` text COMMENT 'yarn app id',
  `flag` tinyint(4) DEFAULT '1' COMMENT '0 not available, 1 available',
  `retry_interval` int(4) DEFAULT NULL COMMENT 'retry interval when task failed ',
  `max_retry_times` int(2) DEFAULT NULL COMMENT 'max retry times',
  `task_instance_priority` int(11) DEFAULT NULL COMMENT 'task instance priority:0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `worker_group` varchar(64) DEFAULT NULL COMMENT 'worker group id',
  `executor_id` int(11) DEFAULT NULL,
  `first_submit_time` datetime DEFAULT NULL COMMENT 'task first submit time',
  `delay_time` int(4) DEFAULT '0' COMMENT 'task delay execution time',
  `var_pool` longtext COMMENT 'var_pool',
  PRIMARY KEY (`id`),
  KEY `process_instance_id` (`process_instance_id`) USING BTREE,
  KEY `task_instance_index` (`process_definition_id`,`process_instance_id`) USING BTREE,
  CONSTRAINT `foreign_key_instance_id` FOREIGN KEY (`process_instance_id`) REFERENCES `t_ds_process_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_task_instance
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_tenant
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_tenant`;
CREATE TABLE `t_ds_tenant` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `tenant_code` varchar(64) DEFAULT NULL COMMENT 'tenant code',
  `description` varchar(256) DEFAULT NULL,
  `queue_id` int(11) DEFAULT NULL COMMENT 'queue id',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_tenant
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_udfs
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_udfs`;
CREATE TABLE `t_ds_udfs` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `user_id` int(11) NOT NULL COMMENT 'user id',
  `func_name` varchar(100) NOT NULL COMMENT 'UDF function name',
  `class_name` varchar(255) NOT NULL COMMENT 'class of udf',
  `type` tinyint(4) NOT NULL COMMENT 'Udf function type',
  `arg_types` varchar(255) DEFAULT NULL COMMENT 'arguments types',
  `database` varchar(255) DEFAULT NULL COMMENT 'data base',
  `description` varchar(255) DEFAULT NULL,
  `resource_id` int(11) NOT NULL COMMENT 'resource id',
  `resource_name` varchar(255) NOT NULL COMMENT 'resource name',
  `create_time` datetime NOT NULL COMMENT 'create time',
  `update_time` datetime NOT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_udfs
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_user
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_user`;
CREATE TABLE `t_ds_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'user id',
  `user_name` varchar(64) DEFAULT NULL COMMENT 'user name',
  `user_password` varchar(64) DEFAULT NULL COMMENT 'user password',
  `user_type` tinyint(4) DEFAULT NULL COMMENT 'user type, 0:administrator，1:ordinary user',
  `email` varchar(64) DEFAULT NULL COMMENT 'email',
  `phone` varchar(11) DEFAULT NULL COMMENT 'phone',
  `tenant_id` int(11) DEFAULT NULL COMMENT 'tenant id',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  `queue` varchar(64) DEFAULT NULL COMMENT 'queue',
  `state` int(1) DEFAULT 1 COMMENT 'state 0:disable 1:enable',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_name_unique` (`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_user
-- ----------------------------


-- ----------------------------
-- Table structure for t_ds_version
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_version`;
CREATE TABLE `t_ds_version` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `version` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `version_UNIQUE` (`version`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='version';

-- ----------------------------
-- Records of t_ds_version
-- ----------------------------
INSERT INTO `t_ds_version`
VALUES ('1', '1.3.0');


-- ----------------------------
-- Records of t_ds_alertgroup
-- ----------------------------
INSERT INTO `t_ds_alertgroup`
VALUES (1,'1,2', 1, 'default admin warning group', 'default admin warning group', '2018-11-29 10:20:39',
        '2018-11-29 10:20:39');

-- ----------------------------
-- Records of t_ds_user
-- ----------------------------
INSERT INTO `t_ds_user`
VALUES ('1', 'admin', '7ad2410b2f4c074479a8937a28a22b8f', '0', 'xxx@qq.com', '', '0', '2018-03-27 15:48:50',
        '2018-10-24 17:40:22', null, 1);

-- ----------------------------
-- Table structure for t_ds_plugin_define
-- ----------------------------
SET
sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));
DROP TABLE IF EXISTS `t_ds_plugin_define`;
CREATE TABLE `t_ds_plugin_define` (
  `id` int NOT NULL AUTO_INCREMENT,
  `plugin_name` varchar(100) NOT NULL COMMENT 'the name of plugin eg: email',
  `plugin_type` varchar(100) NOT NULL COMMENT 'plugin type . alert=alert plugin, job=job plugin',
  `plugin_params` text COMMENT 'plugin params',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `t_ds_plugin_define_UN` (`plugin_name`,`plugin_type`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_ds_alert_plugin_instance
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_alert_plugin_instance`;
CREATE TABLE `t_ds_alert_plugin_instance` (
  `id` int NOT NULL AUTO_INCREMENT,
  `plugin_define_id` int NOT NULL,
  `plugin_instance_params` text COMMENT 'plugin instance params. Also contain the params value which user input in web ui.',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `instance_name` varchar(200) DEFAULT NULL COMMENT 'alert instance name',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_ds_dq_execute_result
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_dq_execute_result`;
CREATE TABLE `t_ds_dq_execute_result` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `process_definition_id` int(11) DEFAULT NULL COMMENT 'task id',
  `process_instance_id` int(11) DEFAULT NULL COMMENT 'process instance id',
  `task_instance_id` int(11) DEFAULT NULL COMMENT 'task instance id',
  `rule_type` int(11) DEFAULT NULL COMMENT 'rule type',
  `rule_name` varchar(255) DEFAULT NULL COMMENT 'rule name',
  `statistics_value` double DEFAULT NULL COMMENT 'statistics value',
  `comparison_value` double DEFAULT NULL COMMENT 'comparison value',
  `check_type` int(11) DEFAULT NULL COMMENT 'check type',
  `threshold` double DEFAULT NULL COMMENT 'threshold',
  `operator` int(11) DEFAULT NULL COMMENT 'operator',
  `failure_strategy` int(11) DEFAULT NULL COMMENT 'dqs job failure strategy',
  `state` int(11) DEFAULT NULL COMMENT 'task state',
  `user_id` int(11) DEFAULT NULL COMMENT 'create user id',
  `create_time` datetime DEFAULT NULL COMMENT 'create time',
  `update_time` datetime DEFAULT NULL COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='rule calculate result';

-- ----------------------------
-- Table structure for t_ds_dq_rule
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_dq_rule`;
CREATE TABLE `t_ds_dq_rule` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `name` varchar(100) DEFAULT NULL COMMENT 'rule name',
    `type` int(11) DEFAULT NULL COMMENT 'rule type',
    `user_id` int(11) DEFAULT NULL COMMENT 'create user id',
    `create_time` datetime DEFAULT NULL COMMENT 'create time',
    `update_time` datetime DEFAULT NULL COMMENT 'update_time',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='rule info';

INSERT INTO `t_ds_dq_rule`
(id, name, `type`, user_id, create_time, update_time)
VALUES(1, '空值检测', 0, 1, '2020-01-12 00:00:00.0', '2020-01-12 00:00:00.0');
INSERT INTO `t_ds_dq_rule`
(id, name, `type`, user_id, create_time, update_time)
VALUES(2, '自定义SQL', 1, 1, '2020-01-12 00:00:00.0', '2020-01-12 00:00:00.0');
INSERT INTO `t_ds_dq_rule`
(id, name, `type`, user_id, create_time, update_time)
VALUES(3, '跨表准确性', 2, 1, '2020-01-12 00:00:00.0', '2020-01-12 00:00:00.0');
INSERT INTO `t_ds_dq_rule`
(id, name, `type`, user_id, create_time, update_time)
VALUES(4, '跨表值比对', 3, 1, '2020-01-12 00:00:00.0', '2020-01-12 00:00:00.0');

-- ----------------------------
-- Table structure for t_ds_dq_rule_execute_sql
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_dq_rule_execute_sql`;
CREATE TABLE `t_ds_dq_rule_execute_sql` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `index` int(11) DEFAULT NULL COMMENT 'sql execute index',
    `sql` text COMMENT 'sql',
    `table_alias` varchar(255) DEFAULT NULL COMMENT 'table alias',
    `type` int(11) DEFAULT NULL COMMENT 'execute sql type : middle,statistics,comparison',
    `create_time` datetime DEFAULT NULL COMMENT 'create time',
    `update_time` datetime DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='data quality execute sql definition in rule';

INSERT INTO `t_ds_dq_rule_execute_sql`
(id, `index`, `sql`, table_alias, `type`, create_time, update_time)
VALUES(1, 1, 'SELECT COUNT(*) AS miss FROM ${src_table} WHERE (${src_field} is null or ${src_field} = '''') AND (${src_filter})', 'miss_count', 1, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
INSERT INTO `t_ds_dq_rule_execute_sql`
(id, `index`, `sql`, table_alias, `type`, create_time, update_time)
VALUES(2, 1, 'SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})', 'total_count', 2, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
INSERT INTO `t_ds_dq_rule_execute_sql`
(id, `index`, `sql`, table_alias, `type`, create_time, update_time)
VALUES(3, 1, 'SELECT * FROM (SELECT * FROM ${src_table} WHERE (${src_filter})) ${src_table} LEFT JOIN (SELECT * FROM ${target_table} WHERE (${target_filter})) ${target_table} ON ${on_clause} WHERE ${where_clause}', 'miss_items', 0, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
INSERT INTO `t_ds_dq_rule_execute_sql`
(id, `index`, `sql`, table_alias, `type`, create_time, update_time)
VALUES(4, 1, 'SELECT COUNT(*) AS miss FROM miss_items', 'miss_count', 1, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
INSERT INTO `t_ds_dq_rule_execute_sql`
(id, `index`, `sql`, table_alias, `type`, create_time, update_time)
VALUES(5, 1, 'SELECT COUNT(*) AS total FROM ${target_table} WHERE (${target_filter})', 'total_count', 2, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');

-- ----------------------------
-- Table structure for t_ds_dq_rule_input_entry
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_dq_rule_input_entry`;
CREATE TABLE `t_ds_dq_rule_input_entry` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `field` varchar(255) DEFAULT NULL COMMENT 'form filed name',
    `type` int(11) DEFAULT NULL COMMENT 'form type',
    `title` varchar(255) DEFAULT NULL COMMENT 'form title',
    `value` varchar(255) DEFAULT NULL COMMENT 'default value, can be null',
    `options` text DEFAULT NULL COMMENT 'default options, can be null, like [{label:"",value:""}]',
    `placeholder` varchar(255) DEFAULT NULL COMMENT 'placeholder',
    `option_source_type` int(11) DEFAULT NULL COMMENT 'he source type of options, use default options or other',
    `value_type` int(11) DEFAULT NULL COMMENT 'input entry value type: string,array,number .etc',
    `input_type` int(11) DEFAULT NULL COMMENT 'input entry type: default,statistics,comparison,check',
    `is_show` tinyint(1) DEFAULT '1' COMMENT 'whether to display on the front end',
    `can_edit` tinyint(1) DEFAULT '1' COMMENT 'whether to edit on the front end',
    `is_emit` tinyint(1) DEFAULT '0' COMMENT 'is emit event',
    `is_validate` tinyint(1) DEFAULT '0' COMMENT 'is validate',
    `create_time` datetime DEFAULT NULL COMMENT 'create time',
    `update_time` datetime DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8 COMMENT='data quality rule input entry';

INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(1, 'src_connector_type', 2, '源数据类型', '', '[{"label":"HIVE","value":"HIVE"},{"label":"JDBC","value":"JDBC"}]', 'please select source connector type', 2, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(2, 'src_datasource_id', 2, '源数据源', '', NULL, 'please select source datasource id', 1, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(3, 'src_table', 2, '源数据表', NULL, NULL, 'Please enter source table name', 0, 0, 0, 1, 1, 1, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(4, 'src_filter', 0, '源表过滤条件', NULL, NULL, 'Please enter filter expression', 0, 3, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(5, 'src_field', 2, '源表检测列', NULL, NULL, 'Please enter column, only single column is supported', 0, 0, 0, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(6, 'statistics_name', 0, '统计值名', NULL, NULL, 'Please enter statistics name, the alias in statistics execute sql', 0, 0, 1, 0, 0, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(7, 'check_type', 2, '检测方式', '0', '[{"label":"统计值与固定值比较","value":"0"},{"label":"统计值与比对值比较","value":"1"},{"label":"统计值占比对值百分比","value":"2"}]', 'please select check type', 0, 0, 3, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(8, 'operator', 2, '操作符', '0', '[{"label":"=","value":"0"},{"label":"<","value":"1"},{"label":"<=","value":"2"},{"label":">","value":"3"},{"label":">=","value":"4"},{"label":"!=","value":"5"}]', 'please select operator', 0, 0, 3, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(9, 'threshold', 0, '阈值', NULL, NULL, 'Please enter threshold, number is needed', 0, 2, 3, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(10, 'failure_strategy', 2, '失败策略', '0', '[{"label":"结束","value":"0"},{"label":"继续","value":"1"},{"label":"结束并告警","value":"2"},{"label":"继续并告警","value":"3"}]', 'please select failure strategy', 0, 0, 3, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(11, 'target_connector_type', 2, '目标数据类型', '', '[{"label":"HIVE","value":"HIVE"},{"label":"JDBC","value":"JDBC"}]', 'Please select target connector type', 2, 0, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(12, 'target_datasource_id', 2, '目标数据源', '', NULL, 'Please select target datasource', 1, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(13, 'target_table', 2, '目标数据表', NULL, NULL, 'Please enter target table', 0, 0, 0, 1, 1, 1, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(14, 'target_filter', 0, '目标表过滤条件', NULL, NULL, 'Please enter target filter expression', 0, 3, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(15, 'mapping_columns', 6, 'ON语句', NULL, '[{"field":"src_field","props":{"placeholder":"Please input src field","rows":0,"disabled":false,"size":"small"},"type":"input","title":"源数据列"},{"field":"operator","props":{"placeholder":"Please input operator","rows":0,"disabled":false,"size":"small"},"type":"input","title":"操作符"},{"field":"target_field","props":{"placeholder":"Please input target field","rows":0,"disabled":false,"size":"small"},"type":"input","title":"目标数据列"}]', 'please enter mapping columns', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(16, 'statistics_execute_sql', 5, '统计值计算SQL', NULL, NULL, 'Please enter statistics execute sql', 0, 3, 0, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(17, 'comparison_name', 0, '比对值名', NULL, NULL, 'Please enter comparison name, the alias in comparison execute sql', 0, 0, 0, 0, 0, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(18, 'comparison_execute_sql', 5, '比对值计算SQL', NULL, NULL, 'Please enter comparison execute sql', 0, 3, 0, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(19, 'comparison_title', 0, '比对值', '表总行数', NULL, 'Please enter comparison title', 0, 0, 2, 1, 0, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(20, 'writer_connector_type', 2, '输出数据类型', '', '[{"label":"MYSQL","value":"0"},{"label":"POSTGRESQL","value":"1"}]', 'please select writer connector type', 0, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(21, 'writer_datasource_id', 2, '输出数据源', '', NULL, 'please select writer datasource id', 1, 2, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(22, 'target_field', 2, '目标表检测列', NULL, NULL, 'Please enter column, only single column is supported', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');

-- ----------------------------
-- Table structure for t_ds_relation_rule_execute_sql
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_relation_rule_execute_sql`;
CREATE TABLE `t_ds_relation_rule_execute_sql` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
    `rule_id` int(11) DEFAULT NULL COMMENT 'rule id',
    `execute_sql_id` int(11) DEFAULT NULL COMMENT 'execute sql id',
    `create_time` datetime DEFAULT NULL COMMENT 'create time',
    `update_time` datetime DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `t_ds_relation_rule_execute_sql`
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(1, 1, 1, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
INSERT INTO `t_ds_relation_rule_execute_sql`
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(2, 1, 2, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
INSERT INTO `t_ds_relation_rule_execute_sql`
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(3, 2, 2, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
INSERT INTO `t_ds_relation_rule_execute_sql`
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(4, 3, 3, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
INSERT INTO `t_ds_relation_rule_execute_sql`
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(5, 3, 4, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
INSERT INTO `t_ds_relation_rule_execute_sql`
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(6, 3, 5, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');

-- ----------------------------
-- Table structure for t_ds_relation_rule_input_entry
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_relation_rule_input_entry`;
CREATE TABLE `t_ds_relation_rule_input_entry` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
    `rule_id` int(11) DEFAULT NULL COMMENT 'rule id',
    `rule_input_entry_id` int(11) DEFAULT NULL COMMENT 'rule input entry id',
    `values_map` text COMMENT 'input entry value map',
    `index` int(11) DEFAULT NULL COMMENT 'index',
    `create_time` datetime DEFAULT NULL COMMENT 'create time',
    `update_time` datetime DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8;
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(1, 1, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(2, 1, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(3, 1, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(4, 1, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(5, 1, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(6, 1, 6, '{"statistics_name":"miss_count.miss"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(7, 1, 7, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(8, 1, 8, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(9, 1, 9, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(10, 1, 10, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(11, 1, 17, '{"comparison_name":"total_count.total"}', 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(12, 1, 19, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(13, 2, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(14, 2, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(15, 2, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(16, 2, 6, '{"is_show":"true","can_edit":"true"}', 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(17, 2, 16, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(18, 2, 4, NULL, 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(19, 2, 7, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(20, 2, 8, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(21, 2, 9, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(22, 2, 10, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(23, 2, 17, '{"comparison_name":"total_count.total"}', 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(24, 2, 19, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(25, 3, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(26, 3, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(27, 3, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(28, 3, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(29, 3, 11, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(30, 3, 12, NULL, 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(31, 3, 13, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(32, 3, 14, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(33, 3, 15, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(34, 3, 7, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(35, 3, 8, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(36, 3, 9, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(37, 3, 10, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(38, 3, 17, '{"comparison_name":"total_count.total"}', 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(39, 3, 19, NULL, 15, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(40, 4, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(41, 4, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(42, 4, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(43, 4, 6, '{"is_show":"true","can_edit":"true"}', 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(44, 4, 16, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(45, 4, 11, NULL, 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(46, 4, 12, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(47, 4, 13, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(48, 4, 17, '{"is_show":"true","can_edit":"true"}', 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(49, 4, 18, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(50, 4, 7, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(51, 4, 8, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(52, 4, 9, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(53, 4, 10, NULL, 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(54, 1, 20, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(55, 1, 21, NULL, 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(56, 2, 20, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(57, 2, 21, NULL, 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(58, 3, 20, NULL, 16, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(59, 3, 21, NULL, 17, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(60, 4, 20, NULL, 15, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(61, 4, 21, NULL, 16, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(62, 3, 6, '{"statistics_name":"miss_count.miss"}', 18, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
