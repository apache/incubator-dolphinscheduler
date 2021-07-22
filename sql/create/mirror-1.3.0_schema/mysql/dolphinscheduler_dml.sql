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
UPDATE t_ds_resources SET pid=-1,is_directory=false WHERE pid IS NULL;
UPDATE t_ds_resources SET full_name = concat('/',alias) WHERE pid=-1 and full_name IS NULL;
UPDATE QRTZ_JOB_DETAILS SET JOB_CLASS_NAME='org.apache.dolphinscheduler.service.quartz.ProcessScheduleJob' WHERE JOB_CLASS_NAME='org.apache.dolphinscheduler.server.quartz.ProcessScheduleJob';
UPDATE t_ds_process_instance instance SET `worker_group`=IFNULL((SELECT name from t_ds_worker_group WHERE instance.worker_group=CONCAT(id,'')),'default');
UPDATE t_ds_task_instance instance SET `worker_group`=IFNULL((SELECT name from t_ds_worker_group WHERE instance.worker_group=CONCAT(id,'')),'default');
UPDATE t_ds_schedules schedule SET `worker_group`=IFNULL((SELECT name from t_ds_worker_group WHERE schedule.worker_group=CONCAT(id,'')),'default');
UPDATE t_ds_command command SET `worker_group`=IFNULL((SELECT name from t_ds_worker_group WHERE command.worker_group=CONCAT(id,'')),'default');
UPDATE t_ds_error_command command SET `worker_group`=IFNULL((SELECT name from t_ds_worker_group WHERE command.worker_group=CONCAT(id,'')),'default');
UPDATE t_ds_user SET phone = '' WHERE phone = 'xx';