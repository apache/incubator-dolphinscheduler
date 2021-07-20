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

# user data local directory path, please make sure the directory exists and have read write permissions
data.basedir.path=${DATA_BASEDIR_PATH}

# resource storage type: HDFS, S3, NONE
resource.storage.type=${RESOURCE_STORAGE_TYPE}

# resource store on HDFS/S3 path, resource file will store to this hadoop hdfs path, self configuration, please make sure the directory exists on hdfs and have read write permissions. "/dolphinscheduler" is recommended
resource.upload.path=${RESOURCE_UPLOAD_PATH}

# whether to startup kerberos
hadoop.security.authentication.startup.state=${HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE}

# java.security.krb5.conf path
java.security.krb5.conf.path=${JAVA_SECURITY_KRB5_CONF_PATH}

# login user from keytab username
login.user.keytab.username=${LOGIN_USER_KEYTAB_USERNAME}

# login user from keytab path
login.user.keytab.path=${LOGIN_USER_KEYTAB_PATH}

# kerberos expire time, the unit is hour
kerberos.expire.time=${KERBEROS_EXPIRE_TIME}

# resource view suffixs
#resource.view.suffixs=txt,log,sh,bat,conf,cfg,py,java,sql,xml,hql,properties,json,yml,yaml,ini,js

# if resource.storage.type=HDFS, the user must have the permission to create directories under the HDFS root path
hdfs.root.user=${HDFS_ROOT_USER}

# if resource.storage.type=S3, the value like: s3a://dolphinscheduler; if resource.storage.type=HDFS and namenode HA is enabled, you need to copy core-site.xml and hdfs-site.xml to conf dir
fs.defaultFS=${FS_DEFAULT_FS}

# if resource.storage.type=S3, s3 endpoint
fs.s3a.endpoint=${FS_S3A_ENDPOINT}

# if resource.storage.type=S3, s3 access key
fs.s3a.access.key=${FS_S3A_ACCESS_KEY}

# if resource.storage.type=S3, s3 secret key
fs.s3a.secret.key=${FS_S3A_SECRET_KEY}

# if resourcemanager HA is enabled, please set the HA IPs; if resourcemanager is single, keep this value empty
yarn.resourcemanager.ha.rm.ids=${YARN_RESOURCEMANAGER_HA_RM_IDS}

# if resourcemanager HA is enabled or not use resourcemanager, please keep the default value; If resourcemanager is single, you only need to replace ds1 to actual resourcemanager hostname
yarn.application.status.address=${YARN_APPLICATION_STATUS_ADDRESS}

# system env path
#dolphinscheduler.env.path=env/dolphinscheduler_env.sh

# network interface preferred like eth0, default: empty
#dolphin.scheduler.network.interface.preferred=

# network IP gets priority, default: inner outer
#dolphin.scheduler.network.priority.strategy=default

# development state
development.state=false
