#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#============================================================================
# Configure Main Scheduler Properties
#============================================================================
#org.quartz.jobStore.driverDelegateClass = org.quartz.impl.jdbcjobstore.StdJDBCDelegate
#org.quartz.jobStore.driverDelegateClass = org.quartz.impl.jdbcjobstore.PostgreSQLDelegate

#org.quartz.scheduler.instanceName = DolphinScheduler
#org.quartz.scheduler.instanceId = AUTO
#org.quartz.scheduler.makeSchedulerThreadDaemon = true
#org.quartz.jobStore.useProperties = false

#============================================================================
# Configure ThreadPool
#============================================================================

#org.quartz.threadPool.class = org.quartz.simpl.SimpleThreadPool
#org.quartz.threadPool.makeThreadsDaemons = true
#org.quartz.threadPool.threadCount = 25
#org.quartz.threadPool.threadPriority = 5

#============================================================================
# Configure JobStore
#============================================================================

#org.quartz.jobStore.class = org.quartz.impl.jdbcjobstore.JobStoreTX

#org.quartz.jobStore.tablePrefix = QRTZ_
#org.quartz.jobStore.isClustered = true
#org.quartz.jobStore.misfireThreshold = 60000
#org.quartz.jobStore.clusterCheckinInterval = 5000
#org.quartz.jobStore.acquireTriggersWithinLock=true
#org.quartz.jobStore.dataSource = myDs

#============================================================================
# Configure Datasources
#============================================================================
#org.quartz.dataSource.myDs.connectionProvider.class = org.apache.dolphinscheduler.service.quartz.DruidConnectionProvider
