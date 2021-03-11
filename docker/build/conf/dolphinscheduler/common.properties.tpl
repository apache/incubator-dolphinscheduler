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

# resource storage type : HDFS, S3, NONE
resource.storage.type=${RESOURCE_STORAGE_TYPE}

# resource store on HDFS/S3 path, resource file will store to this hadoop hdfs path, self configuration, please make sure the directory exists on hdfs and have read write permissions。"/dolphinscheduler" is recommended
resource.upload.path=${RESOURCE_UPLOAD_PATH}

# user data local directory path, please make sure the directory exists and have read write permissions
data.basedir.path=${DOLPHINSCHEDULER_DATA_BASEDIR_PATH}

# whether kerberos starts
hadoop.security.authentication.startup.state=false

# java.security.krb5.conf path
java.security.krb5.conf.path=/opt/krb5.conf

# login user from keytab username
login.user.keytab.username=hdfs-mycluster@ESZ.COM

# login user from keytab path
login.user.keytab.path=/opt/hdfs.headless.keytab

#resource.view.suffixes
#resource.view.suffixes=txt,log,sh,bat,conf,cfg,py,java,sql,xml,hql,properties,json,yml,yaml,ini,js

# if resource.storage.type=HDFS, the user need to have permission to create directories under the HDFS root path
hdfs.root.user=hdfs

# if resource.storage.type=S3, the value like: s3a://dolphinscheduler; if resource.storage.type=HDFS, When namenode HA is enabled, you need to copy core-site.xml and hdfs-site.xml to conf dir
fs.defaultFS=${FS_DEFAULT_FS}

# if resource.storage.type=S3, s3 endpoint
fs.s3a.endpoint=${FS_S3A_ENDPOINT}

# if resource.storage.type=S3, s3 access key
fs.s3a.access.key=${FS_S3A_ACCESS_KEY}

# if resource.storage.type=S3, s3 secret key
fs.s3a.secret.key=${FS_S3A_SECRET_KEY}

# if resourcemanager HA enable, please type the HA ips ; if resourcemanager is single, make this value empty
yarn.resourcemanager.ha.rm.ids=192.168.xx.xx,192.168.xx.xx

# if resourcemanager HA enable or not use resourcemanager, please keep the default value; If resourcemanager is single, you only need to replace ds1 to actual resourcemanager hostname.
yarn.application.status.address=http://ds1:8088/ws/v1/cluster/apps/%s

# job history status url when application number threshold is reached(default 10000,maybe it was set to 1000)
yarn.job.history.status.address=http://ds1:19888/ws/v1/history/mapreduce/jobs/%s

# system env path, If you want to set your own path, you need to set this env file to an absolute path
#dolphinscheduler.env.path=env/dolphinscheduler_env.sh
development.state=false

# kerberos tgt expire time, unit is hours
kerberos.expire.time=2

# datasource encryption salt
datasource.encryption.enable=false
datasource.encryption.salt=!@#$%^&*

# Network IP gets priority, default inner outer
#dolphin.scheduler.network.priority.strategy=default
