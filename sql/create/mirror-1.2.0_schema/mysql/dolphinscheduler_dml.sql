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
SET FOREIGN_KEY_CHECKS=0;
UPDATE QRTZ_CRON_TRIGGERS SET SCHED_NAME='DolphinScheduler' WHERE SCHED_NAME='EasyScheduler';
UPDATE QRTZ_TRIGGERS SET SCHED_NAME='DolphinScheduler' WHERE SCHED_NAME='EasyScheduler';
UPDATE QRTZ_FIRED_TRIGGERS SET SCHED_NAME='DolphinScheduler' WHERE SCHED_NAME='EasyScheduler';
UPDATE QRTZ_JOB_DETAILS SET SCHED_NAME='DolphinScheduler' WHERE SCHED_NAME='EasyScheduler';
UPDATE QRTZ_JOB_DETAILS SET JOB_CLASS_NAME='org.apache.dolphinscheduler.dao.quartz.ProcessScheduleJob' WHERE JOB_CLASS_NAME='cn.escheduler.server.quartz.ProcessScheduleJob';
UPDATE QRTZ_LOCKS SET SCHED_NAME='DolphinScheduler' WHERE SCHED_NAME='EasyScheduler';
UPDATE QRTZ_SCHEDULER_STATE SET SCHED_NAME='DolphinScheduler' WHERE SCHED_NAME='EasyScheduler';
UPDATE t_ds_user SET phone = '' WHERE phone = 'xx';