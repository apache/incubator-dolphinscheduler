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

package org.apache.dolphinscheduler.api.enums;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;

/**
 * status enum
 */
public enum Status {

    SUCCESS(0, "success", "成功"),

    INTERNAL_SERVER_ERROR_ARGS(10000, "Internal Server Error: {0}", "服务端异常: {0}"),

    REQUEST_PARAMS_NOT_VALID_ERROR(10001, "request parameter {0} is not valid", "请求参数[{0}]无效"),
    TASK_TIMEOUT_PARAMS_ERROR(10002, "task timeout parameter is not valid", "任务超时参数无效"),
    USER_NAME_EXIST(10003, "user name already exists", "用户名已存在"),
    USER_NAME_NULL(10004, "user name is null", "用户名不能为空"),
    HDFS_OPERATION_ERROR(10006, "hdfs operation error", "hdfs操作错误"),
    TASK_INSTANCE_NOT_FOUND(10008, "task instance not found", "任务实例不存在"),
    OS_TENANT_CODE_EXIST(10009, "os tenant code {0} already exists", "操作系统租户[{0}]已存在"),
    USER_NOT_EXIST(10010, "user {0} not exists", "用户[{0}]不存在"),
    ALERT_GROUP_NOT_EXIST(10011, "alarm group not found", "告警组不存在"),
    ALERT_GROUP_EXIST(10012, "alarm group already exists", "告警组名称已存在"),
    USER_NAME_PASSWD_ERROR(10013, "user name or password error", "用户名或密码错误"),
    LOGIN_SESSION_FAILED(10014, "create session failed!", "创建session失败"),
    DATASOURCE_EXIST(10015, "data source name already exists", "数据源名称已存在"),
    DATASOURCE_CONNECT_FAILED(10016, "data source connection failed", "建立数据源连接失败"),
    TENANT_NOT_EXIST(10017, "tenant not exists", "租户不存在"),
    PROJECT_NOT_FOUNT(10018, "project {0} not found ", "项目[{0}]不存在"),
    PROJECT_ALREADY_EXISTS(10019, "project {0} already exists", "项目名称[{0}]已存在"),
    TASK_INSTANCE_NOT_EXISTS(10020, "task instance {0} does not exist", "任务实例[{0}]不存在"),
    TASK_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE(10021, "task instance {0} is not sub process instance", "任务实例[{0}]不是子流程实例"),
    SCHEDULE_CRON_NOT_EXISTS(10022, "scheduler crontab {0} does not exist", "调度配置定时表达式[{0}]不存在"),
    SCHEDULE_CRON_ONLINE_FORBID_UPDATE(10023, "online status does not allow update operations", "调度配置上线状态不允许修改"),
    SCHEDULE_CRON_CHECK_FAILED(10024, "scheduler crontab expression validation failure: {0}", "调度配置定时表达式验证失败: {0}"),
    MASTER_NOT_EXISTS(10025, "master does not exist", "无可用master节点"),
    SCHEDULE_STATUS_UNKNOWN(10026, "unknown status: {0}", "未知状态: {0}"),
    CREATE_ALERT_GROUP_ERROR(10027, "create alert group error", "创建告警组错误"),
    QUERY_ALL_ALERTGROUP_ERROR(10028, "query all alertgroup error", "查询告警组错误"),
    LIST_PAGING_ALERT_GROUP_ERROR(10029, "list paging alert group error", "分页查询告警组错误"),
    UPDATE_ALERT_GROUP_ERROR(10030, "update alert group error", "更新告警组错误"),
    DELETE_ALERT_GROUP_ERROR(10031, "delete alert group error", "删除告警组错误"),
    ALERT_GROUP_GRANT_USER_ERROR(10032, "alert group grant user error", "告警组授权用户错误"),
    CREATE_DATASOURCE_ERROR(10033, "create datasource error", "创建数据源错误"),
    UPDATE_DATASOURCE_ERROR(10034, "update datasource error", "更新数据源错误"),
    QUERY_DATASOURCE_ERROR(10035, "query datasource error", "查询数据源错误"),
    CONNECT_DATASOURCE_FAILURE(10036, "connect datasource failure", "建立数据源连接失败"),
    CONNECTION_TEST_FAILURE(10037, "connection test failure", "测试数据源连接失败"),
    DELETE_DATA_SOURCE_FAILURE(10038, "delete data source failure", "删除数据源失败"),
    VERIFY_DATASOURCE_NAME_FAILURE(10039, "verify datasource name failure", "验证数据源名称失败"),
    UNAUTHORIZED_DATASOURCE(10040, "unauthorized datasource", "未经授权的数据源"),
    AUTHORIZED_DATA_SOURCE(10041, "authorized data source", "授权数据源失败"),
    LOGIN_SUCCESS(10042, "login success", "登录成功"),
    USER_LOGIN_FAILURE(10043, "user login failure", "用户登录失败"),
    LIST_WORKERS_ERROR(10044, "list workers error", "查询worker列表错误"),
    LIST_MASTERS_ERROR(10045, "list masters error", "查询master列表错误"),
    UPDATE_PROJECT_ERROR(10046, "update project error", "更新项目信息错误"),
    QUERY_PROJECT_DETAILS_BY_ID_ERROR(10047, "query project details by id error", "查询项目详细信息错误"),
    CREATE_PROJECT_ERROR(10048, "create project error", "创建项目错误"),
    LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR(10049, "login user query project list paging error", "分页查询项目列表错误"),
    DELETE_PROJECT_ERROR(10050, "delete project error", "删除项目错误"),
    QUERY_UNAUTHORIZED_PROJECT_ERROR(10051, "query unauthorized project error", "查询未授权项目错误"),
    QUERY_AUTHORIZED_PROJECT(10052, "query authorized project", "查询授权项目错误"),
    QUERY_QUEUE_LIST_ERROR(10053, "query queue list error", "查询队列列表错误"),
    CREATE_RESOURCE_ERROR(10054, "create resource error", "创建资源错误"),
    UPDATE_RESOURCE_ERROR(10055, "update resource error", "更新资源错误"),
    QUERY_RESOURCES_LIST_ERROR(10056, "query resources list error", "查询资源列表错误"),
    QUERY_RESOURCES_LIST_PAGING(10057, "query resources list paging", "分页查询资源列表错误"),
    DELETE_RESOURCE_ERROR(10058, "delete resource error", "删除资源错误"),
    VERIFY_RESOURCE_BY_NAME_AND_TYPE_ERROR(10059, "verify resource by name and type error", "资源名称或类型验证错误"),
    VIEW_RESOURCE_FILE_ON_LINE_ERROR(10060, "view resource file online error", "查看资源文件错误"),
    CREATE_RESOURCE_FILE_ON_LINE_ERROR(10061, "create resource file online error", "创建资源文件错误"),
    RESOURCE_FILE_IS_EMPTY(10062, "resource file is empty", "资源文件内容不能为空"),
    EDIT_RESOURCE_FILE_ON_LINE_ERROR(10063, "edit resource file online error", "更新资源文件错误"),
    DOWNLOAD_RESOURCE_FILE_ERROR(10064, "download resource file error", "下载资源文件错误"),
    CREATE_UDF_FUNCTION_ERROR(10065, "create udf function error", "创建UDF函数错误"),
    VIEW_UDF_FUNCTION_ERROR(10066, "view udf function error", "查询UDF函数错误"),
    UPDATE_UDF_FUNCTION_ERROR(10067, "update udf function error", "更新UDF函数错误"),
    QUERY_UDF_FUNCTION_LIST_PAGING_ERROR(10068, "query udf function list paging error", "分页查询UDF函数列表错误"),
    QUERY_DATASOURCE_BY_TYPE_ERROR(10069, "query datasource by type error", "查询数据源信息错误"),
    VERIFY_UDF_FUNCTION_NAME_ERROR(10070, "verify udf function name error", "UDF函数名称验证错误"),
    DELETE_UDF_FUNCTION_ERROR(10071, "delete udf function error", "删除UDF函数错误"),
    AUTHORIZED_FILE_RESOURCE_ERROR(10072, "authorized file resource error", "授权资源文件错误"),
    AUTHORIZE_RESOURCE_TREE(10073, "authorize resource tree display error", "授权资源目录树错误"),
    UNAUTHORIZED_UDF_FUNCTION_ERROR(10074, "unauthorized udf function error", "查询未授权UDF函数错误"),
    AUTHORIZED_UDF_FUNCTION_ERROR(10075, "authorized udf function error", "授权UDF函数错误"),
    CREATE_SCHEDULE_ERROR(10076, "create schedule error", "创建调度配置错误"),
    UPDATE_SCHEDULE_ERROR(10077, "update schedule error", "更新调度配置错误"),
    PUBLISH_SCHEDULE_ONLINE_ERROR(10078, "publish schedule online error", "上线调度配置错误"),
    OFFLINE_SCHEDULE_ERROR(10079, "offline schedule error", "下线调度配置错误"),
    QUERY_SCHEDULE_LIST_PAGING_ERROR(10080, "query schedule list paging error", "分页查询调度配置列表错误"),
    QUERY_SCHEDULE_LIST_ERROR(10081, "query schedule list error", "查询调度配置列表错误"),
    QUERY_TASK_LIST_PAGING_ERROR(10082, "query task list paging error", "分页查询任务列表错误"),
    QUERY_TASK_RECORD_LIST_PAGING_ERROR(10083, "query task record list paging error", "分页查询任务记录错误"),
    CREATE_TENANT_ERROR(10084, "create tenant error", "创建租户错误"),
    QUERY_TENANT_LIST_PAGING_ERROR(10085, "query tenant list paging error", "分页查询租户列表错误"),
    QUERY_TENANT_LIST_ERROR(10086, "query tenant list error", "查询租户列表错误"),
    UPDATE_TENANT_ERROR(10087, "update tenant error", "更新租户错误"),
    DELETE_TENANT_BY_ID_ERROR(10088, "delete tenant by id error", "删除租户错误"),
    VERIFY_OS_TENANT_CODE_ERROR(10089, "verify os tenant code error", "操作系统租户验证错误"),
    CREATE_USER_ERROR(10090, "create user error", "创建用户错误"),
    QUERY_USER_LIST_PAGING_ERROR(10091, "query user list paging error", "分页查询用户列表错误"),
    UPDATE_USER_ERROR(10092, "update user error", "更新用户错误"),
    DELETE_USER_BY_ID_ERROR(10093, "delete user by id error", "删除用户错误"),
    GRANT_PROJECT_ERROR(10094, "grant project error", "授权项目错误"),
    GRANT_RESOURCE_ERROR(10095, "grant resource error", "授权资源错误"),
    GRANT_UDF_FUNCTION_ERROR(10096, "grant udf function error", "授权UDF函数错误"),
    GRANT_DATASOURCE_ERROR(10097, "grant datasource error", "授权数据源错误"),
    GET_USER_INFO_ERROR(10098, "get user info error", "获取用户信息错误"),
    USER_LIST_ERROR(10099, "user list error", "查询用户列表错误"),
    VERIFY_USERNAME_ERROR(10100, "verify username error", "用户名验证错误"),
    UNAUTHORIZED_USER_ERROR(10101, "unauthorized user error", "查询未授权用户错误"),
    AUTHORIZED_USER_ERROR(10102, "authorized user error", "查询授权用户错误"),
    QUERY_TASK_INSTANCE_LOG_ERROR(10103, "view task instance log error", "查询任务实例日志错误"),
    DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR(10104, "download task instance log file error", "下载任务日志文件错误"),
    CREATE_PROCESS_DEFINITION(10105, "create process definition", "创建工作流错误"),
    VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR(10106, "verify process definition name unique error", "工作流定义名称验证错误"),
    UPDATE_PROCESS_DEFINITION_ERROR(10107, "update process definition error", "更新工作流定义错误"),
    RELEASE_PROCESS_DEFINITION_ERROR(10108, "release process definition error", "上线工作流错误"),
    QUERY_DETAIL_OF_PROCESS_DEFINITION_ERROR(10109, "query detail of process definition error", "查询工作流详细信息错误"),
    QUERY_PROCESS_DEFINITION_LIST(10110, "query process definition list", "查询工作流列表错误"),
    ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR(10111, "encapsulation treeview structure error", "查询工作流树形图数据错误"),
    GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR(10112, "get tasks list by process definition id error", "查询工作流定义节点信息错误"),
    QUERY_PROCESS_INSTANCE_LIST_PAGING_ERROR(10113, "query process instance list paging error", "分页查询工作流实例列表错误"),
    QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_ERROR(10114, "query task list by process instance id error", "查询任务实例列表错误"),
    UPDATE_PROCESS_INSTANCE_ERROR(10115, "update process instance error", "更新工作流实例错误"),
    QUERY_PROCESS_INSTANCE_BY_ID_ERROR(10116, "query process instance by id error", "查询工作流实例错误"),
    DELETE_PROCESS_INSTANCE_BY_ID_ERROR(10117, "delete process instance by id error", "删除工作流实例错误"),
    QUERY_SUB_PROCESS_INSTANCE_DETAIL_INFO_BY_TASK_ID_ERROR(10118, "query sub process instance detail info by task id error", "查询子流程任务实例错误"),
    QUERY_PARENT_PROCESS_INSTANCE_DETAIL_INFO_BY_SUB_PROCESS_INSTANCE_ID_ERROR(10119, "query parent process instance detail info by sub process instance id error", "查询子流程该工作流实例错误"),
    QUERY_PROCESS_INSTANCE_ALL_VARIABLES_ERROR(10120, "query process instance all variables error", "查询工作流自定义变量信息错误"),
    ENCAPSULATION_PROCESS_INSTANCE_GANTT_STRUCTURE_ERROR(10121, "encapsulation process instance gantt structure error", "查询工作流实例甘特图数据错误"),
    QUERY_PROCESS_DEFINITION_LIST_PAGING_ERROR(10122, "query process definition list paging error", "分页查询工作流定义列表错误"),
    SIGN_OUT_ERROR(10123, "sign out error", "退出错误"),
    OS_TENANT_CODE_HAS_ALREADY_EXISTS(10124, "os tenant code has already exists", "操作系统租户已存在"),
    IP_IS_EMPTY(10125, "ip is empty", "IP地址不能为空"),
    SCHEDULE_CRON_RELEASE_NEED_NOT_CHANGE(10126, "schedule release is already {0}", "调度配置上线错误[{0}]"),
    CREATE_QUEUE_ERROR(10127, "create queue error", "创建队列错误"),
    QUEUE_NOT_EXIST(10128, "queue {0} not exists", "队列ID[{0}]不存在"),
    QUEUE_VALUE_EXIST(10129, "queue value {0} already exists", "队列值[{0}]已存在"),
    QUEUE_NAME_EXIST(10130, "queue name {0} already exists", "队列名称[{0}]已存在"),
    UPDATE_QUEUE_ERROR(10131, "update queue error", "更新队列信息错误"),
    NEED_NOT_UPDATE_QUEUE(10132, "no content changes, no updates are required", "数据未变更，不需要更新队列信息"),
    VERIFY_QUEUE_ERROR(10133, "verify queue error", "验证队列信息错误"),
    NAME_NULL(10134, "name must be not null", "名称不能为空"),
    NAME_EXIST(10135, "name {0} already exists", "名称[{0}]已存在"),
    SAVE_ERROR(10136, "save error", "保存错误"),
    DELETE_PROJECT_ERROR_DEFINES_NOT_NULL(10137, "please delete the process definitions in project first!", "请先删除全部工作流定义"),
    BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR(10117, "batch delete process instance by ids {0} error", "批量删除工作流实例错误"),
    PREVIEW_SCHEDULE_ERROR(10139, "preview schedule error", "预览调度配置错误"),
    PARSE_TO_CRON_EXPRESSION_ERROR(10140, "parse cron to cron expression error", "解析调度表达式错误"),
    SCHEDULE_START_TIME_END_TIME_SAME(10141, "The start time must not be the same as the end", "开始时间不能和结束时间一样"),
    DELETE_TENANT_BY_ID_FAIL(10142, "delete tenant by id fail, for there are {0} process instances in executing using it", "删除租户失败，有[{0}]个运行中的工作流实例正在使用"),
    DELETE_TENANT_BY_ID_FAIL_DEFINES(10143, "delete tenant by id fail, for there are {0} process definitions using it", "删除租户失败，有[{0}]个工作流定义正在使用"),
    DELETE_TENANT_BY_ID_FAIL_USERS(10144, "delete tenant by id fail, for there are {0} users using it", "删除租户失败，有[{0}]个用户正在使用"),
    DELETE_WORKER_GROUP_BY_ID_FAIL(10145, "delete worker group by id fail, for there are {0} process instances in executing using it", "删除Worker分组失败，有[{0}]个运行中的工作流实例正在使用"),
    QUERY_WORKER_GROUP_FAIL(10146, "query worker group fail ", "查询worker分组失败"),
    DELETE_WORKER_GROUP_FAIL(10147, "delete worker group fail ", "删除worker分组失败"),
    USER_DISABLED(10148, "The current user is disabled", "当前用户已停用"),
    COPY_PROCESS_DEFINITION_ERROR(10149, "copy process definition from {0} to {1} error : {2}", "从{0}复制工作流到{1}错误 : {2}"),
    MOVE_PROCESS_DEFINITION_ERROR(10150, "move process definition from {0} to {1} error : {2}", "从{0}移动工作流到{1}错误 : {2}"),
    SWITCH_PROCESS_DEFINITION_VERSION_ERROR(10151, "Switch process definition version error", "切换工作流版本出错"),
    SWITCH_PROCESS_DEFINITION_VERSION_NOT_EXIST_PROCESS_DEFINITION_ERROR(10152
            , "Switch process definition version error: not exists process definition, [process definition id {0}]", "切换工作流版本出错：工作流不存在，[工作流id {0}]"),
    SWITCH_PROCESS_DEFINITION_VERSION_NOT_EXIST_PROCESS_DEFINITION_VERSION_ERROR(10153
            , "Switch process definition version error: not exists process definition version, [process definition id {0}] [version number {1}]", "切换工作流版本出错：工作流版本信息不存在，[工作流id {0}] [版本号 {1}]"),
    QUERY_PROCESS_DEFINITION_VERSIONS_ERROR(10154, "query process definition versions error", "查询工作流历史版本信息出错"),
    QUERY_PROCESS_DEFINITION_VERSIONS_PAGE_NO_OR_PAGE_SIZE_LESS_THAN_1_ERROR(10155
            , "query process definition versions error: [page number:{0}] < 1 or [page size:{1}] < 1", "查询工作流历史版本出错：[pageNo:{0}] < 1 或 [pageSize:{1}] < 1"),
    DELETE_PROCESS_DEFINITION_VERSION_ERROR(10156, "delete process definition version error", "删除工作流历史版本出错"),

    QUERY_USER_CREATED_PROJECT_ERROR(10157, "query user created project error error", "查询用户创建的项目错误"),
    PROCESS_DEFINITION_IDS_IS_EMPTY(10158, "process definition ids is empty", "工作流IDS不能为空"),
    BATCH_COPY_PROCESS_DEFINITION_ERROR(10159, "batch copy process definition error", "复制工作流错误"),
    BATCH_MOVE_PROCESS_DEFINITION_ERROR(10160, "batch move process definition error", "移动工作流错误"),
    QUERY_WORKFLOW_LINEAGE_ERROR(10161, "query workflow lineage error", "查询血缘失败"),
    QUERY_AUTHORIZED_AND_USER_CREATED_PROJECT_ERROR(10162, "query authorized and user created project error error", "查询授权的和用户创建的项目错误"),
    DELETE_PROCESS_DEFINITION_BY_ID_FAIL(10163,"delete process definition by id fail, for there are {0} process instances in executing using it", "删除工作流定义失败，有[{0}]个运行中的工作流实例正在使用"),
    CHECK_OS_TENANT_CODE_ERROR(10164, "Please enter the English os tenant code", "请输入英文操作系统租户"),
    FORCE_TASK_SUCCESS_ERROR(10165, "force task success error", "强制成功任务实例错误"),
    TASK_INSTANCE_STATE_OPERATION_ERROR(10166, "the status of task instance {0} is {1},Cannot perform force success operation", "任务实例[{0}]的状态是[{1}]，无法执行强制成功操作"),
    DATASOURCE_TYPE_NOT_EXIST(10167, "data source type not exist", "数据源类型不存在"),
    PROCESS_DEFINITION_NAME_EXIST(10168, "process definition name {0} already exists", "工作流定义名称[{0}]已存在"),

    UDF_FUNCTION_NOT_EXIST(20001, "UDF function not found", "UDF函数不存在"),
    UDF_FUNCTION_EXISTS(20002, "UDF function already exists", "UDF函数已存在"),
    RESOURCE_NOT_EXIST(20004, "resource not exist", "资源不存在"),
    RESOURCE_EXIST(20005, "resource already exists", "资源已存在"),
    RESOURCE_SUFFIX_NOT_SUPPORT_VIEW(20006, "resource suffix do not support online viewing", "资源文件后缀不支持查看"),
    RESOURCE_SIZE_EXCEED_LIMIT(20007, "upload resource file size exceeds limit", "上传资源文件大小超过限制"),
    RESOURCE_SUFFIX_FORBID_CHANGE(20008, "resource suffix not allowed to be modified", "资源文件后缀不支持修改"),
    UDF_RESOURCE_SUFFIX_NOT_JAR(20009, "UDF resource suffix name must be jar", "UDF资源文件后缀名只支持[jar]"),
    HDFS_COPY_FAIL(20010, "hdfs copy {0} -> {1} fail", "hdfs复制失败：[{0}] -> [{1}]"),
    RESOURCE_FILE_EXIST(20011, "resource file {0} already exists in hdfs,please delete it or change name!", "资源文件[{0}]在hdfs中已存在，请删除或修改资源名"),
    RESOURCE_FILE_NOT_EXIST(20012, "resource file {0} not exists in hdfs!", "资源文件[{0}]在hdfs中不存在"),
    UDF_RESOURCE_IS_BOUND(20013, "udf resource file is bound by UDF functions:{0}", "udf函数绑定了资源文件[{0}]"),
    RESOURCE_IS_USED(20014, "resource file is used by process definition", "资源文件被上线的流程定义使用了"),
    PARENT_RESOURCE_NOT_EXIST(20015, "parent resource not exist", "父资源文件不存在"),
    RESOURCE_NOT_EXIST_OR_NO_PERMISSION(20016, "resource not exist or no permission,please view the task node and remove error resource", "请检查任务节点并移除无权限或者已删除的资源"),
    RESOURCE_IS_AUTHORIZED(20017, "resource is authorized to user {0},suffix not allowed to be modified", "资源文件已授权其他用户[{0}],后缀不允许修改"),

    USER_NO_OPERATION_PERM(30001, "user has no operation privilege", "当前用户没有操作权限"),
    USER_NO_OPERATION_PROJECT_PERM(30002, "user {0} is not has project {1} permission", "当前用户[{0}]没有[{1}]项目的操作权限"),


    PROCESS_INSTANCE_NOT_EXIST(50001, "process instance {0} does not exist", "工作流实例[{0}]不存在"),
    PROCESS_INSTANCE_EXIST(50002, "process instance {0} already exists", "工作流实例[{0}]已存在"),
    PROCESS_DEFINE_NOT_EXIST(50003, "process definition {0} does not exist", "工作流定义[{0}]不存在"),
    PROCESS_DEFINE_NOT_RELEASE(50004, "process definition {0} not on line", "工作流定义[{0}]不是上线状态"),
    PROCESS_INSTANCE_ALREADY_CHANGED(50005, "the status of process instance {0} is already {1}", "工作流实例[{0}]的状态已经是[{1}]"),
    PROCESS_INSTANCE_STATE_OPERATION_ERROR(50006, "the status of process instance {0} is {1},Cannot perform {2} operation", "工作流实例[{0}]的状态是[{1}]，无法执行[{2}]操作"),
    SUB_PROCESS_INSTANCE_NOT_EXIST(50007, "the task belong to process instance does not exist", "子工作流实例不存在"),
    PROCESS_DEFINE_NOT_ALLOWED_EDIT(50008, "process definition {0} does not allow edit", "工作流定义[{0}]不允许修改"),
    PROCESS_INSTANCE_EXECUTING_COMMAND(50009, "process instance {0} is executing the command, please wait ...", "工作流实例[{0}]正在执行命令，请稍等..."),
    PROCESS_INSTANCE_NOT_SUB_PROCESS_INSTANCE(50010, "process instance {0} is not sub process instance", "工作流实例[{0}]不是子工作流实例"),
    TASK_INSTANCE_STATE_COUNT_ERROR(50011, "task instance state count error", "查询各状态任务实例数错误"),
    COUNT_PROCESS_INSTANCE_STATE_ERROR(50012, "count process instance state error", "查询各状态流程实例数错误"),
    COUNT_PROCESS_DEFINITION_USER_ERROR(50013, "count process definition user error", "查询各用户流程定义数错误"),
    START_PROCESS_INSTANCE_ERROR(50014, "start process instance error", "运行工作流实例错误"),
    EXECUTE_PROCESS_INSTANCE_ERROR(50015, "execute process instance error", "操作工作流实例错误"),
    CHECK_PROCESS_DEFINITION_ERROR(50016, "check process definition error", "工作流定义错误"),
    QUERY_RECIPIENTS_AND_COPYERS_BY_PROCESS_DEFINITION_ERROR(50017, "query recipients and copyers by process definition error", "查询收件人和抄送人错误"),
    DATA_IS_NOT_VALID(50017, "data {0} not valid", "数据[{0}]无效"),
    DATA_IS_NULL(50018, "data {0} is null", "数据[{0}]不能为空"),
    PROCESS_NODE_HAS_CYCLE(50019, "process node has cycle", "流程节点间存在循环依赖"),
    PROCESS_NODE_S_PARAMETER_INVALID(50020, "process node {0} parameter invalid", "流程节点[{0}]参数无效"),
    PROCESS_DEFINE_STATE_ONLINE(50021, "process definition {0} is already on line", "工作流定义[{0}]已上线"),
    DELETE_PROCESS_DEFINE_BY_ID_ERROR(50022, "delete process definition by id error", "删除工作流定义错误"),
    SCHEDULE_CRON_STATE_ONLINE(50023, "the status of schedule {0} is already on line", "调度配置[{0}]已上线"),
    DELETE_SCHEDULE_CRON_BY_ID_ERROR(50024, "delete schedule by id error", "删除调度配置错误"),
    BATCH_DELETE_PROCESS_DEFINE_ERROR(50025, "batch delete process definition error", "批量删除工作流定义错误"),
    BATCH_DELETE_PROCESS_DEFINE_BY_IDS_ERROR(50026, "batch delete process definition by ids {0} error", "批量删除工作流定义[{0}]错误"),
    TENANT_NOT_SUITABLE(50027, "there is not any tenant suitable, please choose a tenant available.", "没有合适的租户，请选择可用的租户"),
    EXPORT_PROCESS_DEFINE_BY_ID_ERROR(50028, "export process definition by id error", "导出工作流定义错误"),
    BATCH_EXPORT_PROCESS_DEFINE_BY_IDS_ERROR(50028,"batch export process definition by ids error", "批量导出工作流定义错误"),
    IMPORT_PROCESS_DEFINE_ERROR(50029, "import process definition error", "导入工作流定义错误"),

    HDFS_NOT_STARTUP(60001, "hdfs not startup", "hdfs未启用"),

    /**
     * for monitor
     */
    QUERY_DATABASE_STATE_ERROR(70001, "query database state error", "查询数据库状态错误"),
    QUERY_ZOOKEEPER_STATE_ERROR(70002, "query zookeeper state error", "查询zookeeper状态错误"),


    CREATE_ACCESS_TOKEN_ERROR(70010, "create access token error", "创建访问token错误"),
    GENERATE_TOKEN_ERROR(70011, "generate token error", "生成token错误"),
    QUERY_ACCESSTOKEN_LIST_PAGING_ERROR(70012, "query access token list paging error", "分页查询访问token列表错误"),
    UPDATE_ACCESS_TOKEN_ERROR(70013, "update access token error", "更新访问token错误"),
    DELETE_ACCESS_TOKEN_ERROR(70014, "delete access token error", "删除访问token错误"),
    ACCESS_TOKEN_NOT_EXIST(70015, "access token not exist", "访问token不存在"),


    COMMAND_STATE_COUNT_ERROR(80001, "task instance state count error", "查询各状态任务实例数错误"),
    NEGATIVE_SIZE_NUMBER_ERROR(80002, "query size number error", "查询size错误"),
    START_TIME_BIGGER_THAN_END_TIME_ERROR(80003, "start time bigger than end time error", "开始时间在结束时间之后错误"),
    QUEUE_COUNT_ERROR(90001, "queue count error", "查询队列数据错误"),

    KERBEROS_STARTUP_STATE(100001, "get kerberos startup state error", "获取kerberos启动状态错误"),

    //plugin
    PLUGIN_NOT_A_UI_COMPONENT(110001, "query plugin error, this plugin has no UI component", "查询插件错误，此插件无UI组件"),
    QUERY_PLUGINS_RESULT_IS_NULL(110002, "query plugins result is null", "查询插件为空"),
    QUERY_PLUGINS_ERROR(110003, "query plugins error", "查询插件错误"),
    QUERY_PLUGIN_DETAIL_RESULT_IS_NULL(110004, "query plugin detail result is null", "查询插件详情结果为空"),

    UPDATE_ALERT_PLUGIN_INSTANCE_ERROR(110005, "update alert plugin instance error", "更新告警组和告警组插件实例错误"),
    DELETE_ALERT_PLUGIN_INSTANCE_ERROR(110006, "delete alert plugin instance error", "删除告警组和告警组插件实例错误"),
    GET_ALERT_PLUGIN_INSTANCE_ERROR(110007, "get alert plugin instance error", "获取告警组和告警组插件实例错误"),
    CREATE_ALERT_PLUGIN_INSTANCE_ERROR(110008, "create alert plugin instance error", "创建告警组和告警组插件实例错误"),
    QUERY_ALL_ALERT_PLUGIN_INSTANCE_ERROR(110009, "query all alert plugin instance error", "查询所有告警实例失败"),
    PLUGIN_INSTANCE_ALREADY_EXIT(110010,"plugin instance already exit","该告警插件实例已存在"),
    LIST_PAGING_ALERT_PLUGIN_INSTANCE_ERROR(110011,"query plugin instance page error","分页查询告警实例失败"),
    DELETE_ALERT_PLUGIN_INSTANCE_ERROR_HAS_ALERT_GROUP_ASSOCIATED(110012,"failed to delete the alert instance, there is an alarm group associated with this alert instance","删除告警实例失败，存在与此告警实例关联的警报组")

    ;

    private final int code;
    private final String enMsg;
    private final String zhMsg;

    Status(int code, String enMsg, String zhMsg) {
        this.code = code;
        this.enMsg = enMsg;
        this.zhMsg = zhMsg;
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(LocaleContextHolder.getLocale().getLanguage())) {
            return this.zhMsg;
        } else {
            return this.enMsg;
        }
    }
}
