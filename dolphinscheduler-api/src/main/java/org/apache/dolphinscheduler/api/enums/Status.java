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
 * status enum      // todo #4855 One category one interval
 */
public enum Status {

    SUCCESS(0, "success", "成功"),

    /**
     * Distinguish status codes by business: First category(2 digits)-Secondary category(2 digits)-Three-level category(3 digits)
     */

    //other-other(10-00)
    REQUEST_PARAMS_NOT_VALID_ERROR(1000000, "request parameter {0} is not valid", "请求参数[{0}]无效"),
    INTERNAL_SERVER_ERROR_ARGS(1000001, "Internal Server Error: {0}", "服务端异常: {0}"),
    LOGIN_SESSION_FAILED(1000002, "create session failed!", "创建session失败"),
    NAME_NULL(1000003, "name must be not null", "名称不能为空"),
    NAME_EXIST(1000004, "name {0} already exists", "名称[{0}]已存在"),
    SAVE_ERROR(1000005, "save error", "保存错误"),
    DATA_IS_NOT_VALID(1000006, "data {0} not valid", "数据[{0}]无效"),
    DATA_IS_NULL(1000007, "data {0} is null", "数据[{0}]不能为空"),
    START_TIME_BIGGER_THAN_END_TIME_ERROR(1000008, "start time bigger than end time error", "开始时间在结束时间之后错误"),
    NEGTIVE_SIZE_NUMBER_ERROR(1000009, "query size number error", "查询size错误"),
    QUERY_WORKFLOW_LINEAGE_ERROR(1000010, "query workflow lineage error", "查询血缘失败"),
    KERBEROS_STARTUP_STATE(1000011, "get kerberos startup state error", "获取kerberos启动状态错误"),
    //other-plugin(10-01)
    PLUGIN_NOT_A_UI_COMPONENT(1001000, "query plugin error, this plugin has no UI component", "查询插件错误，此插件无UI组件"),
    QUERY_PLUGINS_RESULT_IS_NULL(1001001, "query plugins result is null", "查询插件为空"),
    QUERY_PLUGINS_ERROR(1001002, "query plugins error", "查询插件错误"),
    QUERY_PLUGIN_DETAIL_RESULT_IS_NULL(1001003, "query plugin detail result is null", "查询插件详情结果为空"),
    //project-project(20-00)
    PROJECT_NOT_FOUNT(2000000, "project {0} not found ", "项目[{0}]不存在"),
    PROJECT_ALREADY_EXISTS(2000001, "project {0} already exists", "项目名称[{0}]已存在"),
    UPDATE_PROJECT_ERROR(2000002, "update project error", "更新项目信息错误"),
    QUERY_PROJECT_DETAILS_BY_ID_ERROR(2000003, "query project details by id error", "查询项目详细信息错误"),
    CREATE_PROJECT_ERROR(2000004, "create project error", "创建项目错误"),
    LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR(2000005, "login user query project list paging error", "分页查询项目列表错误"),
    DELETE_PROJECT_ERROR(2000006, "delete project error", "删除项目错误"),
    QUERY_UNAUTHORIZED_PROJECT_ERROR(2000007, "query unauthorized project error", "查询未授权项目错误"),
    QUERY_AUTHORIZED_PROJECT(2000008, "query authorized project", "查询授权项目错误"),
    DELETE_PROJECT_ERROR_DEFINES_NOT_NULL(2000009, "please delete the process definitions in project first!", "请先删除全部工作流定义"),
    QUERY_USER_CREATED_PROJECT_ERROR(2000010, "query user created project error error", "查询用户创建的项目错误"),
    //project-processDefinition(20-01)
    CREATE_PROCESS_DEFINITION(2001000, "create process definition", "创建工作流错误"),
    VERIFY_PROCESS_DEFINITION_NAME_UNIQUE_ERROR(2001001, "verify process definition name unique error", "工作流定义名称验证错误"),
    UPDATE_PROCESS_DEFINITION_ERROR(2001002, "update process definition error", "更新工作流定义错误"),
    RELEASE_PROCESS_DEFINITION_ERROR(2001003, "release process definition error", "上线工作流错误"),
    QUERY_DATAIL_OF_PROCESS_DEFINITION_ERROR(2001004, "query datail of process definition error", "查询工作流详细信息错误"),
    QUERY_PROCESS_DEFINITION_LIST(2001005, "query process definition list", "查询工作流列表错误"),
    ENCAPSULATION_TREEVIEW_STRUCTURE_ERROR(2001006, "encapsulation treeview structure error", "查询工作流树形图数据错误"),
    GET_TASKS_LIST_BY_PROCESS_DEFINITION_ID_ERROR(2001007, "get tasks list by process definition id error", "查询工作流定义节点信息错误"),
    QUERY_PROCESS_DEFINITION_LIST_PAGING_ERROR(2001008, "query process definition list paging error", "分页查询工作流定义列表错误"),
    COPY_PROCESS_DEFINITION_ERROR(2001009, "copy process definition from {0} to {1} error : {2}", "从{0}复制工作流到{1}错误 : {2}"),
    MOVE_PROCESS_DEFINITION_ERROR(2001010, "move process definition from {0} to {1} error : {2}", "从{0}移动工作流到{1}错误 : {2}"),
    SWITCH_PROCESS_DEFINITION_VERSION_ERROR(2001011, "Switch process definition version error", "切换工作流版本出错"),
    SWITCH_PROCESS_DEFINITION_VERSION_NOT_EXIST_PROCESS_DEFINITION_ERROR(2001012,
            "Switch process definition version error: not exists process definition, [process definition id {0}]", "切换工作流版本出错：工作流不存在，[工作流id {0}]"),
    SWITCH_PROCESS_DEFINITION_VERSION_NOT_EXIST_PROCESS_DEFINITION_VERSION_ERROR(2001013,
            "Switch process definition version error: not exists process definition version, [process definition id {0}] [version number {1}]", "切换工作流版本出错：工作流版本信息不存在，[工作流id {0}] [版本号 {1}]"),
    QUERY_PROCESS_DEFINITION_VERSIONS_ERROR(2001014, "query process definition versions error", "查询工作流历史版本信息出错"),
    QUERY_PROCESS_DEFINITION_VERSIONS_PAGE_NO_OR_PAGE_SIZE_LESS_THAN_1_ERROR(2001015,
            "query process definition versions error: [page number:{0}] < 1 or [page size:{1}] < 1", "查询工作流历史版本出错：[pageNo:{0}] < 1 或 [pageSize:{1}] < 1"),
    DELETE_PROCESS_DEFINITION_VERSION_ERROR(2001016, "delete process definition version error", "删除工作流历史版本出错"),
    PROCESS_DEFINITION_IDS_IS_EMPTY(2001017, "process definition ids is empty", "工作流IDS不能为空"),
    PROCESS_DEFINITION_NAME_EXIST(2001018, "process definition name {0} already exists", "工作流定义名称[{0}]已存在"),
    BATCH_COPY_PROCESS_DEFINITION_ERROR(2001019, "batch copy process definition error", "复制工作流错误"),
    BATCH_MOVE_PROCESS_DEFINITION_ERROR(2001020, "batch move process definition error", "移动工作流错误"),
    DELETE_PROCESS_DEFINITION_BY_ID_FAIL(2001021, "delete process definition by id fail, for there are {0} process instances in executing using it", "删除工作流定义失败，有[{0}]个运行中的工作流实例正在使用"),
    PROCESS_INSTANCE_NOT_EXIST(2001022, "process instance {0} does not exist", "工作流实例[{0}]不存在"),
    PROCESS_INSTANCE_EXIST(2001023, "process instance {0} already exists", "工作流实例[{0}]已存在"),
    PROCESS_DEFINE_NOT_EXIST(2001024, "process definition {0} does not exist", "工作流定义[{0}]不存在"),
    PROCESS_DEFINE_NOT_RELEASE(2001025, "process definition {0} not on line", "工作流定义[{0}]不是上线状态"),
    PROCESS_DEFINE_NOT_ALLOWED_EDIT(2001026, "process definition {0} does not allow edit", "工作流定义[{0}]不允许修改"),
    COUNT_PROCESS_DEFINITION_USER_ERROR(2001027, "count process definition user error", "查询各用户流程定义数错误"),
    CHECK_PROCESS_DEFINITION_ERROR(2001028, "check process definition error", "工作流定义错误"),
    PROCESS_DEFINE_STATE_ONLINE(2001029, "process definition {0} is already on line", "工作流定义[{0}]已上线"),
    DELETE_PROCESS_DEFINE_BY_ID_ERROR(2001030, "delete process definition by id error", "删除工作流定义错误"),
    BATCH_DELETE_PROCESS_DEFINE_ERROR(2001031, "batch delete process definition error", "批量删除工作流定义错误"),
    EXPORT_PROCESS_DEFINE_BY_ID_ERROR(2001032, "export process definition by id error", "导出工作流定义错误"),
    BATCH_EXPORT_PROCESS_DEFINE_BY_IDS_ERROR(2001033, "batch export process definition by ids error", "批量导出工作流定义错误"),
    IMPORT_PROCESS_DEFINE_ERROR(2001034, "import process definition error", "导入工作流定义错误"),
    PROCESS_NODE_HAS_CYCLE(2001035, "process node has cycle", "流程节点间存在循环依赖"),
    BATCH_DELETE_PROCESS_DEFINE_BY_IDS_ERROR(2001036, "batch delete process definition by ids {0} error", "批量删除工作流定义[{0}]错误"),
    PROCESS_NODE_S_PARAMETER_INVALID(2001037, "process node {0} parameter invalid", "流程节点[{0}]参数无效"),
    QUERY_RECIPIENTS_AND_COPYERS_BY_PROCESS_DEFINITION_ERROR(2001038, "query recipients and copyers by process definition error", "查询收件人和抄送人错误"),
    PROCESS_DEFINITION_VERSION_IS_USED(2001039, "this process definition version is used", "此工作流定义版本被使用"),
    //project-processInstance(20-02)
    QUERY_PROCESS_INSTANCE_LIST_PAGING_ERROR(2002000, "query process instance list paging error", "分页查询工作流实例列表错误"),
    UPDATE_PROCESS_INSTANCE_ERROR(2002001, "update process instance error", "更新工作流实例错误"),
    QUERY_PROCESS_INSTANCE_BY_ID_ERROR(2002002, "query process instance by id error", "查询工作流实例错误"),
    DELETE_PROCESS_INSTANCE_BY_ID_ERROR(2002003, "delete process instance by id error", "删除工作流实例错误"),
    QUERY_SUB_PROCESS_INSTANCE_DETAIL_INFO_BY_TASK_ID_ERROR(2002004, "query sub process instance detail info by task id error", "查询子流程任务实例错误"),
    QUERY_PARENT_PROCESS_INSTANCE_DETAIL_INFO_BY_SUB_PROCESS_INSTANCE_ID_ERROR(2002005, "query parent process instance detail info by sub process instance id error", "查询子流程该工作流实例错误"),
    QUERY_PROCESS_INSTANCE_ALL_VARIABLES_ERROR(2002006, "query process instance all variables error", "查询工作流自定义变量信息错误"),
    ENCAPSULATION_PROCESS_INSTANCE_GANTT_STRUCTURE_ERROR(2002007, "encapsulation process instance gantt structure error", "查询工作流实例甘特图数据错误"),
    PROCESS_INSTANCE_ALREADY_CHANGED(2002008, "the status of process instance {0} is already {1}", "工作流实例[{0}]的状态已经是[{1}]"),
    PROCESS_INSTANCE_STATE_OPERATION_ERROR(2002009, "the status of process instance {0} is {1},Cannot perform {2} operation", "工作流实例[{0}]的状态是[{1}]，无法执行[{2}]操作"),
    SUB_PROCESS_INSTANCE_NOT_EXIST(2002010, "the task belong to process instance does not exist", "子工作流实例不存在"),
    PROCESS_INSTANCE_EXECUTING_COMMAND(2002011, "process instance {0} is executing the command, please wait ...", "工作流实例[{0}]正在执行命令，请稍等..."),
    PROCESS_INSTANCE_NOT_SUB_PROCESS_INSTANCE(2002012, "process instance {0} is not sub process instance", "工作流实例[{0}]不是子工作流实例"),
    COUNT_PROCESS_INSTANCE_STATE_ERROR(2002013, "count process instance state error", "查询各状态流程实例数错误"),
    START_PROCESS_INSTANCE_ERROR(2002014, "start process instance error", "运行工作流实例错误"),
    EXECUTE_PROCESS_INSTANCE_ERROR(2002015, "execute process instance error", "操作工作流实例错误"),
    BATCH_DELETE_PROCESS_INSTANCE_BY_IDS_ERROR(2002016, "batch delete process instance by ids {0} error", "批量删除工作流实例错误"),
    //project-task(20-03)
    TASK_TIMEOUT_PARAMS_ERROR(2003000, "task timeout parameter is not valid", "任务超时参数无效"),
    QUERY_TASK_LIST_PAGING_ERROR(2003001, "query task list paging error", "分页查询任务列表错误"),
    TASK_DEFINE_NOT_EXIST(2003002, "task definition {0} does not exist", "任务定义[{0}]不存在"),
    DELETE_TASK_DEFINE_BY_CODE_ERROR(2003003, "delete task definition by code error", "删除任务定义错误"),
    DELETE_PROCESS_TASK_RELATION_ERROR(2003004, "delete process task relation error", "删除工作流任务关系错误"),
    PROCESS_TASK_RELATION_NOT_EXIST(2003005, "process task relation {0} does not exist", "工作流任务关系[{0}]不存在"),
    PROCESS_TASK_RELATION_EXIST(2003006, "process task relation is already exist, processCode:[{0}]", "工作流任务关系已存在, processCode:[{0}]"),
    QUERY_TASK_RECORD_LIST_PAGING_ERROR(2003007, "query task record list paging error", "分页查询任务记录错误"),
    //project-taskInstance(20-04)
    TASK_INSTANCE_NOT_FOUND(2004000, "task instance not found", "任务实例不存在"),
    TASK_INSTANCE_NOT_EXISTS(2004001, "task instance {0} does not exist", "任务实例[{0}]不存在"),
    TASK_INSTANCE_NOT_SUB_WORKFLOW_INSTANCE(2004002, "task instance {0} is not sub process instance", "任务实例[{0}]不是子流程实例"),
    QUERY_TASK_INSTANCE_LOG_ERROR(2004003, "view task instance log error", "查询任务实例日志错误"),
    DOWNLOAD_TASK_INSTANCE_LOG_FILE_ERROR(2004004, "download task instance log file error", "下载任务日志文件错误"),
    QUERY_TASK_LIST_BY_PROCESS_INSTANCE_ID_ERROR(2004005, "query task list by process instance id error", "查询任务实例列表错误"),
    TASK_INSTANCE_STATE_COUNT_ERROR(2004006, "task instance state count error", "查询各状态任务实例数错误"),
    COMMAND_STATE_COUNT_ERROR(2004007, "task instance state count error", "查询各状态任务实例数错误"),
    TASK_INSTANCE_STATE_OPERATION_ERROR(2004008, "the status of task instance {0} is {1},Cannot perform force success operation", "任务实例[{0}]的状态是[{1}]，无法执行强制成功操作"),
    FORCE_TASK_SUCCESS_ERROR(2004009, "force task success error", "强制成功任务实例错误"),
    //project-scheduler(20-05)
    SCHEDULE_CRON_NOT_EXISTS(2005000, "scheduler crontab {0} does not exist", "调度配置定时表达式[{0}]不存在"),
    SCHEDULE_CRON_ONLINE_FORBID_UPDATE(2005001, "online status does not allow update operations", "调度配置上线状态不允许修改"),
    SCHEDULE_CRON_CHECK_FAILED(2005002, "scheduler crontab expression validation failure: {0}", "调度配置定时表达式验证失败: {0}"),
    SCHEDULE_STATUS_UNKNOWN(2005003, "unknown status: {0}", "未知状态: {0}"),
    CREATE_SCHEDULE_ERROR(2005004, "create schedule error", "创建调度配置错误"),
    UPDATE_SCHEDULE_ERROR(2005005, "update schedule error", "更新调度配置错误"),
    PUBLISH_SCHEDULE_ONLINE_ERROR(2005006, "publish schedule online error", "上线调度配置错误"),
    OFFLINE_SCHEDULE_ERROR(2005007, "offline schedule error", "下线调度配置错误"),
    QUERY_SCHEDULE_LIST_PAGING_ERROR(2005008, "query schedule list paging error", "分页查询调度配置列表错误"),
    QUERY_SCHEDULE_LIST_ERROR(2005009, "query schedule list error", "查询调度配置列表错误"),
    SCHEDULE_CRON_REALEASE_NEED_NOT_CHANGE(2005010, "schedule release is already {0}", "调度配置上线错误[{0}]"),
    PREVIEW_SCHEDULE_ERROR(2005011, "preview schedule error", "预览调度配置错误"),
    PARSE_TO_CRON_EXPRESSION_ERROR(2005012, "parse cron to cron expression error", "解析调度表达式错误"),
    SCHEDULE_START_TIME_END_TIME_SAME(2005013, "The start time must not be the same as the end", "开始时间不能和结束时间一样"),
    SCHEDULE_CRON_STATE_ONLINE(2005014, "the status of schedule {0} is already on line", "调度配置[{0}]已上线"),
    DELETE_SCHEDULE_CRON_BY_ID_ERROR(2005015, "delete schedule by id error", "删除调度配置错误"),
    //resource-resource(30-00)
    CREATE_RESOURCE_ERROR(3000000, "create resource error", "创建资源错误"),
    UPDATE_RESOURCE_ERROR(3000001, "update resource error", "更新资源错误"),
    QUERY_RESOURCES_LIST_ERROR(3000002, "query resources list error", "查询资源列表错误"),
    QUERY_RESOURCES_LIST_PAGING(3000003, "query resources list paging", "分页查询资源列表错误"),
    DELETE_RESOURCE_ERROR(3000004, "delete resource error", "删除资源错误"),
    VERIFY_RESOURCE_BY_NAME_AND_TYPE_ERROR(3000005, "verify resource by name and type error", "资源名称或类型验证错误"),
    HDFS_NOT_STARTUP(3000006, "hdfs not startup", "hdfs未启用"),
    HDFS_OPERATION_ERROR(3000007, "hdfs operation error", "hdfs操作错误"),
    //resource-file(30-01)
    VIEW_RESOURCE_FILE_ON_LINE_ERROR(3001000, "view resource file online error", "查看资源文件错误"),
    CREATE_RESOURCE_FILE_ON_LINE_ERROR(3001001, "create resource file online error", "创建资源文件错误"),
    RESOURCE_FILE_IS_EMPTY(3001002, "resource file is empty", "资源文件内容不能为空"),
    EDIT_RESOURCE_FILE_ON_LINE_ERROR(3001003, "edit resource file online error", "更新资源文件错误"),
    DOWNLOAD_RESOURCE_FILE_ERROR(3001004, "download resource file error", "下载资源文件错误"),
    RESOURCE_NOT_EXIST(3001005, "resource not exist", "资源不存在"),
    RESOURCE_EXIST(3001006, "resource already exists", "资源已存在"),
    RESOURCE_SUFFIX_NOT_SUPPORT_VIEW(3001007, "resource suffix do not support online viewing", "资源文件后缀不支持查看"),
    RESOURCE_SIZE_EXCEED_LIMIT(3001008, "upload resource file size exceeds limit", "上传资源文件大小超过限制"),
    RESOURCE_SUFFIX_FORBID_CHANGE(3001009, "resource suffix not allowed to be modified", "资源文件后缀不支持修改"),
    RESOURCE_FILE_EXIST(3001010, "resource file {0} already exists in hdfs,please delete it or change name!", "资源文件[{0}]在hdfs中已存在，请删除或修改资源名"),
    RESOURCE_FILE_NOT_EXIST(3001011, "resource file {0} not exists in hdfs!", "资源文件[{0}]在hdfs中不存在"),
    RESOURCE_IS_USED(3001012, "resource file is used by process definition", "资源文件被上线的流程定义使用了"),
    RESOURCE_NOT_EXIST_OR_NO_PERMISSION(3001013, "resource not exist or no permission,please view the task node and remove error resource", "请检查任务节点并移除无权限或者已删除的资源"),
    PARENT_RESOURCE_NOT_EXIST(3001014, "parent resource not exist", "父资源文件不存在"),
    RESOURCE_IS_AUTHORIZED(3001015, "resource is authorized to user {0},suffix not allowed to be modified", "资源文件已授权其他用户[{0}],后缀不允许修改"),
    HDFS_COPY_FAIL(3001016, "hdfs copy {0} -> {1} fail", "hdfs复制失败：[{0}] -> [{1}]"),
    //resource-udf(30-02)
    CREATE_UDF_FUNCTION_ERROR(3002000, "create udf function error", "创建UDF函数错误"),
    VIEW_UDF_FUNCTION_ERROR(3002001, "view udf function error", "查询UDF函数错误"),
    UPDATE_UDF_FUNCTION_ERROR(3002002, "update udf function error", "更新UDF函数错误"),
    QUERY_UDF_FUNCTION_LIST_PAGING_ERROR(3002003, "query udf function list paging error", "分页查询UDF函数列表错误"),
    VERIFY_UDF_FUNCTION_NAME_ERROR(3002004, "verify udf function name error", "UDF函数名称验证错误"),
    DELETE_UDF_FUNCTION_ERROR(3002005, "delete udf function error", "删除UDF函数错误"),
    AUTHORIZED_FILE_RESOURCE_ERROR(3002006, "authorized file resource error", "授权资源文件错误"),
    AUTHORIZE_RESOURCE_TREE(3002007, "authorize resource tree display error", "授权资源目录树错误"),
    UNAUTHORIZED_UDF_FUNCTION_ERROR(3002008, "unauthorized udf function error", "查询未授权UDF函数错误"),
    AUTHORIZED_UDF_FUNCTION_ERROR(3002009, "authorized udf function error", "授权UDF函数错误"),
    UDF_FUNCTION_NOT_EXIST(3002010, "UDF function not found", "UDF函数不存在"),
    UDF_FUNCTION_EXISTS(3002011, "UDF function already exists", "UDF函数已存在"),
    UDF_RESOURCE_SUFFIX_NOT_JAR(3002012, "UDF resource suffix name must be jar", "UDF资源文件后缀名只支持[jar]"),
    UDF_RESOURCE_IS_BOUND(3002013, "udf resource file is bound by UDF functions:{0}", "udf函数绑定了资源文件[{0}]"),
    //datasource-datasource(40-00)
    DATASOURCE_EXIST(4000000, "data source name already exists", "数据源名称已存在"),
    DATASOURCE_CONNECT_FAILED(4000001, "data source connection failed", "建立数据源连接失败"),
    CREATE_DATASOURCE_ERROR(4000002, "create datasource error", "创建数据源错误"),
    UPDATE_DATASOURCE_ERROR(4000003, "update datasource error", "更新数据源错误"),
    QUERY_DATASOURCE_ERROR(4000004, "query datasource error", "查询数据源错误"),
    CONNECT_DATASOURCE_FAILURE(4000005, "connect datasource failure", "建立数据源连接失败"),
    CONNECTION_TEST_FAILURE(4000006, "connection test failure", "测试数据源连接失败"),
    DELETE_DATA_SOURCE_FAILURE(4000007, "delete data source failure", "删除数据源失败"),
    VERIFY_DATASOURCE_NAME_FAILURE(4000008, "verify datasource name failure", "验证数据源名称失败"),
    UNAUTHORIZED_DATASOURCE(4000009, "unauthorized datasource", "未经授权的数据源"),
    AUTHORIZED_DATA_SOURCE(4000010, "authorized data source", "授权数据源失败"),
    QUERY_DATASOURCE_BY_TYPE_ERROR(4000011, "query datasource by type error", "查询数据源信息错误"),
    IP_IS_EMPTY(4000012, "ip is empty", "IP地址不能为空"),
    DATASOURCE_TYPE_NOT_EXIST(4000013, "data source type not exist", "数据源类型不存在"),
    //monitor-master(50-00)
    MASTER_NOT_EXISTS(5000000, "master does not exist", "无可用master节点"),
    LIST_MASTERS_ERROR(5000001, "list masters error", "查询master列表错误"),
    //monitor-worker(50-01)
    LIST_WORKERS_ERROR(5001000, "list workers error", "查询worker列表错误"),
    //monitor-db(50-02)
    QUERY_DATABASE_STATE_ERROR(5002000, "query database state error", "查询数据库状态错误"),
    //monitor-zookeeper(50-03)
    QUERY_ZOOKEEPER_STATE_ERROR(5003000, "query zookeeper state error", "查询zookeeper状态错误"),
    //security-user(60-00)
    USER_NAME_EXIST(6000000, "user name already exists", "用户名已存在"),
    USER_NAME_NULL(6000001, "user name is null", "用户名不能为空"),
    USER_NAME_PASSWD_ERROR(6000002, "user name or password error", "用户名或密码错误"),
    USER_NOT_EXIST(6000003, "user {0} not exists", "用户[{0}]不存在"),
    LOGIN_SUCCESS(6000004, "login success", "登录成功"),
    SIGN_OUT_ERROR(6000005, "sign out error", "退出错误"),
    USER_LOGIN_FAILURE(6000006, "user login failure", "用户登录失败"),
    CREATE_USER_ERROR(6000007, "create user error", "创建用户错误"),
    QUERY_USER_LIST_PAGING_ERROR(6000008, "query user list paging error", "分页查询用户列表错误"),
    UPDATE_USER_ERROR(6000009, "update user error", "更新用户错误"),
    DELETE_USER_BY_ID_ERROR(6000010, "delete user by id error", "删除用户错误"),
    GET_USER_INFO_ERROR(6000011, "get user info error", "获取用户信息错误"),
    USER_LIST_ERROR(6000012, "user list error", "查询用户列表错误"),
    VERIFY_USERNAME_ERROR(6000013, "verify username error", "用户名验证错误"),
    UNAUTHORIZED_USER_ERROR(6000014, "unauthorized user error", "查询未授权用户错误"),
    AUTHORIZED_USER_ERROR(6000015, "authorized user error", "查询授权用户错误"),
    USER_DISABLED(6000016, "The current user is disabled", "当前用户已停用"),
    USER_NO_OPERATION_PERM(6000017, "user has no operation privilege", "当前用户没有操作权限"),
    USER_NO_OPERATION_PROJECT_PERM(6000018, "user {0} is not has project {1} permission", "当前用户[{0}]没有[{1}]项目的操作权限"),
    //security-tenant(60-01)
    TENANT_NOT_EXIST(6001000, "tenant not exists", "租户不存在"),
    OS_TENANT_CODE_EXIST(6001001, "os tenant code {0} already exists", "操作系统租户[{0}]已存在"),
    CREATE_TENANT_ERROR(6001002, "create tenant error", "创建租户错误"),
    QUERY_TENANT_LIST_PAGING_ERROR(6001003, "query tenant list paging error", "分页查询租户列表错误"),
    QUERY_TENANT_LIST_ERROR(6001004, "query tenant list error", "查询租户列表错误"),
    UPDATE_TENANT_ERROR(6001005, "update tenant error", "更新租户错误"),
    DELETE_TENANT_BY_ID_ERROR(6001006, "delete tenant by id error", "删除租户错误"),
    VERIFY_OS_TENANT_CODE_ERROR(6001007, "verify os tenant code error", "操作系统租户验证错误"),
    OS_TENANT_CODE_HAS_ALREADY_EXISTS(6001008, "os tenant code has already exists", "操作系统租户已存在"),
    DELETE_TENANT_BY_ID_FAIL(6001009, "delete tenant by id fail, for there are {0} process instances in executing using it", "删除租户失败，有[{0}]个运行中的工作流实例正在使用"),
    DELETE_TENANT_BY_ID_FAIL_DEFINES(6001010, "delete tenant by id fail, for there are {0} process definitions using it", "删除租户失败，有[{0}]个工作流定义正在使用"),
    DELETE_TENANT_BY_ID_FAIL_USERS(6001011, "delete tenant by id fail, for there are {0} users using it", "删除租户失败，有[{0}]个用户正在使用"),
    CHECK_OS_TENANT_CODE_ERROR(6001012, "Please enter the English os tenant code", "请输入英文操作系统租户"),
    TENANT_NOT_SUITABLE(6001013, "there is not any tenant suitable, please choose a tenant available.", "没有合适的租户，请选择可用的租户"),
    //security-alarm(60-02)
    ALERT_GROUP_NOT_EXIST(6002000, "alarm group not found", "告警组不存在"),
    ALERT_GROUP_EXIST(6002001, "alarm group already exists", "告警组名称已存在"),
    CREATE_ALERT_GROUP_ERROR(6002002, "create alert group error", "创建告警组错误"),
    QUERY_ALL_ALERTGROUP_ERROR(6002003, "query all alertgroup error", "查询告警组错误"),
    LIST_PAGING_ALERT_GROUP_ERROR(6002004, "list paging alert group error", "分页查询告警组错误"),
    UPDATE_ALERT_GROUP_ERROR(6002005, "update alert group error", "更新告警组错误"),
    DELETE_ALERT_GROUP_ERROR(6002006, "delete alert group error", "删除告警组错误"),
    ALERT_GROUP_GRANT_USER_ERROR(6002007, "alert group grant user error", "告警组授权用户错误"),
    UPDATE_ALERT_PLUGIN_INSTANCE_ERROR(6002008, "update alert plugin instance error", "更新告警组和告警组插件实例错误"),
    DELETE_ALERT_PLUGIN_INSTANCE_ERROR(6002009, "delete alert plugin instance error", "删除告警组和告警组插件实例错误"),
    GET_ALERT_PLUGIN_INSTANCE_ERROR(6002010, "get alert plugin instance error", "获取告警组和告警组插件实例错误"),
    CREATE_ALERT_PLUGIN_INSTANCE_ERROR(6002011, "create alert plugin instance error", "创建告警组和告警组插件实例错误"),
    QUERY_ALL_ALERT_PLUGIN_INSTANCE_ERROR(6002012, "query all alert plugin instance error", "查询所有告警实例失败"),
    PLUGIN_INSTANCE_ALREADY_EXIT(6002013, "plugin instance already exit", "该告警插件实例已存在"),
    LIST_PAGING_ALERT_PLUGIN_INSTANCE_ERROR(6002014, "query plugin instance page error", "分页查询告警实例失败"),
    DELETE_ALERT_PLUGIN_INSTANCE_ERROR_HAS_ALERT_GROUP_ASSOCIATED(6002015,
            "failed to delete the alert instance, there is an alarm group associated with this alert instance", "删除告警实例失败，存在与此告警实例关联的警报组"),
    //security-workerGroup(60-03)
    DELETE_WORKER_GROUP_BY_ID_FAIL(6003000, "delete worker group by id fail, for there are {0} process instances in executing using it", "删除Worker分组失败，有[{0}]个运行中的工作流实例正在使用"),
    QUERY_WORKER_GROUP_FAIL(6003001, "query worker group fail ", "查询worker分组失败"),
    DELETE_WORKER_GROUP_FAIL(6003002, "delete worker group fail ", "删除worker分组失败"),
    //security-grant(60-04)
    GRANT_PROJECT_ERROR(6004000, "grant project error", "授权项目错误"),
    GRANT_RESOURCE_ERROR(6004001, "grant resource error", "授权资源错误"),
    GRANT_UDF_FUNCTION_ERROR(6004002, "grant udf function error", "授权UDF函数错误"),
    GRANT_DATASOURCE_ERROR(6004003, "grant datasource error", "授权数据源错误"),
    QUERY_AUTHORIZED_AND_USER_CREATED_PROJECT_ERROR(6004004, "query authorized and user created project error error", "查询授权的和用户创建的项目错误"),
    //security-queue(60-05)
    QUERY_QUEUE_LIST_ERROR(6005000, "query queue list error", "查询队列列表错误"),
    CREATE_QUEUE_ERROR(6005001, "create queue error", "创建队列错误"),
    QUEUE_NOT_EXIST(6005002, "queue {0} not exists", "队列ID[{0}]不存在"),
    QUEUE_VALUE_EXIST(6005003, "queue value {0} already exists", "队列值[{0}]已存在"),
    QUEUE_NAME_EXIST(6005004, "queue name {0} already exists", "队列名称[{0}]已存在"),
    UPDATE_QUEUE_ERROR(6005005, "update queue error", "更新队列信息错误"),
    NEED_NOT_UPDATE_QUEUE(6005006, "no content changes, no updates are required", "数据未变更，不需要更新队列信息"),
    VERIFY_QUEUE_ERROR(6005007, "verify queue error", "验证队列信息错误"),
    QUEUE_COUNT_ERROR(6005008, "queue count error", "查询队列数据错误"),
    //security-token(60-06)
    CREATE_ACCESS_TOKEN_ERROR(6006000, "create access token error", "创建访问token错误"),
    GENERATE_TOKEN_ERROR(6006001, "generate token error", "生成token错误"),
    QUERY_ACCESSTOKEN_LIST_PAGING_ERROR(6006002, "query access token list paging error", "分页查询访问token列表错误"),
    UPDATE_ACCESS_TOKEN_ERROR(6006003, "update access token error", "更新访问token错误"),
    DELETE_ACCESS_TOKEN_ERROR(6006004, "delete access token error", "删除访问token错误"),
    ACCESS_TOKEN_NOT_EXIST(6006005, "access token not exist", "访问token不存在"),;

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
