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

package org.apache.dolphinscheduler.service.process;

import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_RECOVER_PROCESS_ID_STRING;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_START_PARAMS;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_SUB_PROCESS_DEFINE_ID;

import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.task.conditions.ConditionsParameters;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessData;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessInstanceMap;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ErrorCommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.service.quartz.cron.CronUtilsTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * process service test
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(CronUtilsTest.class);

    @InjectMocks
    private ProcessService processService;

    @Mock
    TenantMapper tenantMapper;

    @Mock
    private CommandMapper commandMapper;

    @Mock
    private ErrorCommandMapper errorCommandMapper;

    @Mock
    private ProcessDefinitionMapper processDefineMapper;
    @Mock
    private ProcessInstanceMapper processInstanceMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    TaskInstanceMapper taskInstanceMapper;

    @Test
    public void testCreateSubCommand() {
        ProcessService processService = new ProcessService();
        ProcessInstance parentInstance = new ProcessInstance();
        parentInstance.setProcessDefinitionId(1);
        parentInstance.setWarningType(WarningType.SUCCESS);
        parentInstance.setWarningGroupId(0);

        TaskInstance task = new TaskInstance();
        task.setTaskJson("{\"params\":{\"processDefinitionId\":100}}");
        task.setId(10);

        ProcessInstance childInstance = null;
        ProcessInstanceMap instanceMap = new ProcessInstanceMap();
        instanceMap.setParentProcessInstanceId(1);
        instanceMap.setParentTaskInstanceId(10);
        Command command = null;

        //father history: start; child null == command type: start
        parentInstance.setHistoryCmd("START_PROCESS");
        parentInstance.setCommandType(CommandType.START_PROCESS);
        command = processService.createSubProcessCommand(
                parentInstance, childInstance, instanceMap, task
        );
        Assert.assertEquals(CommandType.START_PROCESS, command.getCommandType());

        //father history: start,start failure; child null == command type: start
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("START_PROCESS,START_FAILURE_TASK_PROCESS");
        command = processService.createSubProcessCommand(
                parentInstance, childInstance, instanceMap, task
        );
        Assert.assertEquals(CommandType.START_PROCESS, command.getCommandType());

        //father history: scheduler,start failure; child null == command type: scheduler
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("SCHEDULER,START_FAILURE_TASK_PROCESS");
        command = processService.createSubProcessCommand(
                parentInstance, childInstance, instanceMap, task
        );
        Assert.assertEquals(CommandType.SCHEDULER, command.getCommandType());

        //father history: complement,start failure; child null == command type: complement

        String startString = "2020-01-01 00:00:00";
        String endString = "2020-01-10 00:00:00";
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("COMPLEMENT_DATA,START_FAILURE_TASK_PROCESS");
        Map<String, String> complementMap = new HashMap<>();
        complementMap.put(Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE, startString);
        complementMap.put(Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE, endString);
        parentInstance.setCommandParam(JSONUtils.toJsonString(complementMap));
        command = processService.createSubProcessCommand(
                parentInstance, childInstance, instanceMap, task
        );
        Assert.assertEquals(CommandType.COMPLEMENT_DATA, command.getCommandType());

        JsonNode complementDate = JSONUtils.parseObject(command.getCommandParam());
        Date start = DateUtils.stringToDate(complementDate.get(Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE).asText());
        Date end = DateUtils.stringToDate(complementDate.get(Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE).asText());
        Assert.assertEquals(startString, DateUtils.dateToString(start));
        Assert.assertEquals(endString, DateUtils.dateToString(end));

        //father history: start,failure,start failure; child not null == command type: start failure
        childInstance = new ProcessInstance();
        parentInstance.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        parentInstance.setHistoryCmd("START_PROCESS,START_FAILURE_TASK_PROCESS");
        command = processService.createSubProcessCommand(
                parentInstance, childInstance, instanceMap, task
        );
        Assert.assertEquals(CommandType.START_FAILURE_TASK_PROCESS, command.getCommandType());
    }

    @Test
    public void testVerifyIsNeedCreateCommand() {

        List<Command> commands = new ArrayList<>();

        Command command = new Command();
        command.setCommandType(CommandType.REPEAT_RUNNING);
        command.setCommandParam("{\"" + CMD_PARAM_RECOVER_PROCESS_ID_STRING + "\":\"111\"}");
        commands.add(command);
        Mockito.when(commandMapper.selectList(null)).thenReturn(commands);
        Assert.assertFalse(processService.verifyIsNeedCreateCommand(command));

        Command command1 = new Command();
        command1.setCommandType(CommandType.REPEAT_RUNNING);
        command1.setCommandParam("{\"" + CMD_PARAM_RECOVER_PROCESS_ID_STRING + "\":\"222\"}");
        Assert.assertTrue(processService.verifyIsNeedCreateCommand(command1));

        Command command2 = new Command();
        command2.setCommandType(CommandType.PAUSE);
        Assert.assertTrue(processService.verifyIsNeedCreateCommand(command2));
    }

    @Test
    public void testCreateRecoveryWaitingThreadCommand() {

        int id = 123;
        Mockito.when(commandMapper.deleteById(id)).thenReturn(1);
        ProcessInstance subProcessInstance = new ProcessInstance();
        subProcessInstance.setIsSubProcess(Flag.YES);
        Command originCommand = new Command();
        originCommand.setId(id);
        processService.createRecoveryWaitingThreadCommand(originCommand, subProcessInstance);

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(111);
        processService.createRecoveryWaitingThreadCommand(null, subProcessInstance);

        Command recoverCommand = new Command();
        recoverCommand.setCommandType(CommandType.RECOVER_WAITTING_THREAD);
        processService.createRecoveryWaitingThreadCommand(recoverCommand, subProcessInstance);

        Command repeatRunningCommand = new Command();
        recoverCommand.setCommandType(CommandType.REPEAT_RUNNING);
        processService.createRecoveryWaitingThreadCommand(repeatRunningCommand, subProcessInstance);

        ProcessInstance subProcessInstance2 = new ProcessInstance();
        subProcessInstance2.setId(111);
        subProcessInstance2.setIsSubProcess(Flag.NO);
        processService.createRecoveryWaitingThreadCommand(repeatRunningCommand, subProcessInstance2);

    }

    @Test
    public void testHandleCommand() {

        //cannot construct process instance, return null;
        String host = "127.0.0.1";
        int validThreadNum = 1;
        Command command = new Command();
        command.setProcessDefinitionId(222);
        command.setCommandType(CommandType.REPEAT_RUNNING);
        command.setCommandParam("{\"" + CMD_PARAM_RECOVER_PROCESS_ID_STRING + "\":\"111\",\""
                + CMD_PARAM_SUB_PROCESS_DEFINE_ID + "\":\"222\"}");
        Mockito.when(processDefineMapper.selectById(command.getProcessDefinitionId())).thenReturn(null);
        Assert.assertNull(processService.handleCommand(logger, host, validThreadNum, command));

        //there is not enough thread for this command
        Command command1 = new Command();
        command1.setProcessDefinitionId(123);
        command1.setCommandParam("{\"ProcessInstanceId\":222}");
        command1.setCommandType(CommandType.START_PROCESS);
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(123);
        processDefinition.setName("test");
        processDefinition.setVersion(1);
        processDefinition.setProcessDefinitionJson("{\"globalParams\":[{\"prop\":\"startParam1\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"\"}],\"tasks\":[{\"conditionResult\":"
                + "{\"failedNode\":[\"\"],\"successNode\":[\"\"]},\"delayTime\":\"0\",\"dependence\":{}"
                + ",\"description\":\"\",\"id\":\"tasks-3011\",\"maxRetryTimes\":\"0\",\"name\":\"tsssss\""
                + ",\"params\":{\"localParams\":[],\"rawScript\":\"echo \\\"123123\\\"\",\"resourceList\":[]}"
                + ",\"preTasks\":[],\"retryInterval\":\"1\",\"runFlag\":\"NORMAL\",\"taskInstancePriority\":\"MEDIUM\""
                + ",\"timeout\":{\"enable\":false,\"interval\":null,\"strategy\":\"\"},\"type\":\"SHELL\""
                + ",\"waitStartTimeout\":{},\"workerGroup\":\"default\"}],\"tenantId\":4,\"timeout\":0}");
        processDefinition.setGlobalParams("[{\"prop\":\"startParam1\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"\"}]");
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(222);
        Mockito.when(processDefineMapper.selectById(command1.getProcessDefinitionId())).thenReturn(processDefinition);
        Mockito.when(processInstanceMapper.queryDetailById(222)).thenReturn(processInstance);
        Assert.assertNotNull(processService.handleCommand(logger, host, validThreadNum, command1));

        Command command2 = new Command();
        command2.setCommandParam("{\"ProcessInstanceId\":222,\"StartNodeIdList\":\"n1,n2\"}");
        command2.setProcessDefinitionId(123);
        command2.setCommandType(CommandType.RECOVER_SUSPENDED_PROCESS);

        Assert.assertNotNull(processService.handleCommand(logger, host, validThreadNum, command2));

        Command command3 = new Command();
        command3.setProcessDefinitionId(123);
        command3.setCommandParam("{\"WaitingThreadInstanceId\":222}");
        command3.setCommandType(CommandType.START_FAILURE_TASK_PROCESS);
        Assert.assertNotNull(processService.handleCommand(logger, host, validThreadNum, command3));

        Command command4 = new Command();
        command4.setProcessDefinitionId(123);
        command4.setCommandParam("{\"WaitingThreadInstanceId\":222,\"StartNodeIdList\":\"n1,n2\"}");
        command4.setCommandType(CommandType.REPEAT_RUNNING);
        Assert.assertNotNull(processService.handleCommand(logger, host, validThreadNum, command4));

        Command command5 = new Command();
        command5.setProcessDefinitionId(123);
        HashMap<String, String> startParams = new HashMap<>();
        startParams.put("startParam1", "testStartParam1");
        HashMap<String, String> commandParams = new HashMap<>();
        commandParams.put(CMD_PARAM_START_PARAMS, JSONUtils.toJsonString(startParams));
        command5.setCommandParam(JSONUtils.toJsonString(commandParams));
        command5.setCommandType(CommandType.START_PROCESS);
        ProcessInstance processInstance1 = processService.handleCommand(logger, host, validThreadNum, command5);
        Assert.assertTrue(processInstance1.getGlobalParams().contains("\"testStartParam1\""));

        Command command6 = new Command();
        command6.setProcessDefinitionId(123);
        HashMap<String, String> commandParams4Random = new HashMap<>();
        commandParams4Random.put(Constants.CMD_PARAM_RECOVERY_START_NODE_STRING, "1");
        command6.setCommandParam(JSONUtils.toJsonString(commandParams4Random));
        command6.setCommandType(CommandType.START_RANDOM_TASK_PROCESS);
        Assert.assertNotNull(processService.handleCommand(logger, host, validThreadNum, command6));
    }

    @Test
    public void testGetUserById() {
        User user = new User();
        user.setId(123);
        Mockito.when(userMapper.selectById(123)).thenReturn(user);
        Assert.assertEquals(user, processService.getUserById(123));
    }

    @Test
    public void testFormatTaskAppId() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(333);
        taskInstance.setProcessDefinitionId(111);
        taskInstance.setProcessInstanceId(222);
        Mockito.when(processService.findProcessDefineById(taskInstance.getProcessDefinitionId())).thenReturn(null);
        Mockito.when(processService.findProcessInstanceById(taskInstance.getProcessInstanceId())).thenReturn(null);
        Assert.assertEquals("", processService.formatTaskAppId(taskInstance));

        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(111);
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(222);
        Mockito.when(processService.findProcessDefineById(taskInstance.getProcessDefinitionId())).thenReturn(processDefinition);
        Mockito.when(processService.findProcessInstanceById(taskInstance.getProcessInstanceId())).thenReturn(processInstance);
        Assert.assertEquals("111_222_333", processService.formatTaskAppId(taskInstance));

    }

    @Test
    public void testRecurseFindSubProcessId() {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setProcessDefinitionJson("{\"globalParams\":[],\"tasks\":[{\"conditionResult\":"
                + "{\"failedNode\":[\"\"],\"successNode\":[\"\"]},\"delayTime\":\"0\""
                + ",\"dependence\":{},\"description\":\"\",\"id\":\"tasks-76544\""
                + ",\"maxRetryTimes\":\"0\",\"name\":\"test\",\"params\":{\"localParams\":[],"
                + "\"rawScript\":\"echo \\\"123123\\\"\",\"resourceList\":[],\"processDefinitionId\""
                + ":\"222\"},\"preTasks\":[],\"retryInterval\":\"1\",\"runFlag\":\"NORMAL\","
                + "\"taskInstancePriority\":\"MEDIUM\",\"timeout\":{\"enable\":false,\"interval\":"
                + "null,\"strategy\":\"\"},\"type\":\"SHELL\",\"waitStartTimeout\":{},\"workerGroup\":\"default\"}],"
                + "\"tenantId\":4,\"timeout\":0}");
        int parentId = 111;
        List<Integer> ids = new ArrayList<>();
        ProcessDefinition processDefinition2 = new ProcessDefinition();
        processDefinition2.setProcessDefinitionJson("{\"globalParams\":[],\"tasks\":[{\"conditionResult\""
                + ":{\"failedNode\":[\"\"],\"successNode\":[\"\"]},\"delayTime\":\"0\",\"dependence\":{},"
                + "\"description\":\"\",\"id\":\"tasks-76544\",\"maxRetryTimes\":\"0\",\"name\":\"test\","
                + "\"params\":{\"localParams\":[],\"rawScript\":\"echo \\\"123123\\\"\",\"resourceList\":[]},"
                + "\"preTasks\":[],\"retryInterval\":\"1\",\"runFlag\":\"NORMAL\",\"taskInstancePriority\":"
                + "\"MEDIUM\",\"timeout\":{\"enable\":false,\"interval\":null,\"strategy\":\"\"},\"type\":"
                + "\"SHELL\",\"waitStartTimeout\":{},\"workerGroup\":\"default\"}],\"tenantId\":4,\"timeout\":0}");
        Mockito.when(processDefineMapper.selectById(parentId)).thenReturn(processDefinition);
        Mockito.when(processDefineMapper.selectById(222)).thenReturn(processDefinition2);
        processService.recurseFindSubProcessId(parentId, ids);

    }

    @Test
    public void testChangeJson() {

        ProcessData oldProcessData = new ProcessData();
        ConditionsParameters conditionsParameters = new ConditionsParameters();
        ArrayList<TaskNode> tasks = new ArrayList<>();
        TaskNode taskNode = new TaskNode();
        TaskNode taskNode11 = new TaskNode();
        TaskNode taskNode111 = new TaskNode();
        ArrayList<String> successNode = new ArrayList<>();
        ArrayList<String> faildNode = new ArrayList<>();

        taskNode.setName("bbb");
        taskNode.setType("SHELL");
        taskNode.setId("222");

        taskNode11.setName("vvv");
        taskNode11.setType("CONDITIONS");
        taskNode11.setId("444");
        successNode.add("bbb");
        faildNode.add("ccc");

        taskNode111.setName("ccc");
        taskNode111.setType("SHELL");
        taskNode111.setId("333");

        conditionsParameters.setSuccessNode(successNode);
        conditionsParameters.setFailedNode(faildNode);
        taskNode11.setConditionResult(conditionsParameters.getConditionResult());
        tasks.add(taskNode);
        tasks.add(taskNode11);
        tasks.add(taskNode111);
        oldProcessData.setTasks(tasks);

        ProcessData newProcessData = new ProcessData();
        ConditionsParameters conditionsParameters2 = new ConditionsParameters();
        TaskNode taskNode2 = new TaskNode();
        TaskNode taskNode22 = new TaskNode();
        TaskNode taskNode222 = new TaskNode();
        ArrayList<TaskNode> tasks2 = new ArrayList<>();
        ArrayList<String> successNode2 = new ArrayList<>();
        ArrayList<String> faildNode2 = new ArrayList<>();

        taskNode2.setName("bbbchange");
        taskNode2.setType("SHELL");
        taskNode2.setId("222");

        taskNode22.setName("vv");
        taskNode22.setType("CONDITIONS");
        taskNode22.setId("444");
        successNode2.add("bbb");
        faildNode2.add("ccc");

        taskNode222.setName("ccc");
        taskNode222.setType("SHELL");
        taskNode222.setId("333");

        conditionsParameters2.setSuccessNode(successNode2);
        conditionsParameters2.setFailedNode(faildNode2);
        taskNode22.setConditionResult(conditionsParameters2.getConditionResult());
        tasks2.add(taskNode2);
        tasks2.add(taskNode22);
        tasks2.add(taskNode222);

        newProcessData.setTasks(tasks2);

        ProcessData exceptProcessData = new ProcessData();
        ConditionsParameters conditionsParameters3 = new ConditionsParameters();
        TaskNode taskNode3 = new TaskNode();
        TaskNode taskNode33 = new TaskNode();
        TaskNode taskNode333 = new TaskNode();
        ArrayList<TaskNode> tasks3 = new ArrayList<>();
        ArrayList<String> successNode3 = new ArrayList<>();
        ArrayList<String> faildNode3 = new ArrayList<>();

        taskNode3.setName("bbbchange");
        taskNode3.setType("SHELL");
        taskNode3.setId("222");

        taskNode33.setName("vv");
        taskNode33.setType("CONDITIONS");
        taskNode33.setId("444");
        successNode3.add("bbbchange");
        faildNode3.add("ccc");

        taskNode333.setName("ccc");
        taskNode333.setType("SHELL");
        taskNode333.setId("333");

        conditionsParameters3.setSuccessNode(successNode3);
        conditionsParameters3.setFailedNode(faildNode3);
        taskNode33.setConditionResult(conditionsParameters3.getConditionResult());
        tasks3.add(taskNode3);
        tasks3.add(taskNode33);
        tasks3.add(taskNode333);
        exceptProcessData.setTasks(tasks3);

        String expect = JSONUtils.toJsonString(exceptProcessData);
        String oldJson = JSONUtils.toJsonString(oldProcessData);

        Assert.assertEquals(expect, processService.changeJson(newProcessData,oldJson));

    }

    @Test
    public void testChangeOutParam() {
        String result = "[{\"d\":\"20210203\"}]";
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setProcessInstanceId(62);
        taskInstance.setTaskJson("{\"id\":\"tasks-86175\",\"name\":\"wew\",\"desc\":null,\"type\":\"SHELL\",\"runFlag\":\"NORMAL\",\"loc\":null,\"maxRetryTimes\":0,"
                + "\"retryInterval\":1,\"params\":{\"rawScript\":\"echo 20210203\",\"localParams\":[{\"prop\":\"d\",\"direct\":\"OUT\",\"type\":\"VARCHAR\",\"value\":\"\"}],"
                + "\"resourceList\":[]},\"preTasks\":[],\"extras\":null,\"depList\":[],\"dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},"
                + "\"taskInstancePriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"workerGroupId\":null,"
                + "\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},\"delayTime\":0}");
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(62);
        processInstance.setGlobalParams("[{\"prop\":\"sql2\",\"direct\":null,\"type\":null,\"value\":\"\"},{\"prop\":\"out\",\"direct\":null,\"type\":null,\"value\":\"\"},"
                + "{\"prop\":\"d\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"\"}]");
        String params4ProcessString = "[{\"prop\":\"sql2\",\"direct\":null,\"type\":null,\"value\":\"\"},{\"prop\":\"out\",\"direct\":null,\"type\":null,\"value\":\"\"},"
                + "{\"prop\":\"d\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"20210203\"}]";
        Mockito.when(processInstanceMapper.queryDetailById(taskInstance.getProcessInstanceId())).thenReturn(processInstance);
        Mockito.when(this.processInstanceMapper.updateGlobalParamsById(params4ProcessString, processInstance.getId())).thenReturn(1);
        processService.changeOutParam(result,taskInstance);
    }

    @Test
    public void testCreateCommand() {
        Command command = new Command();
        command.setProcessDefinitionId(123);
        command.setCommandParam("{\"ProcessInstanceId\":222}");
        command.setCommandType(CommandType.START_PROCESS);
        int mockResult = 1;
        Mockito.when(commandMapper.insert(command)).thenReturn(mockResult);
        int exeMethodResult = processService.createCommand(command);
        Assert.assertEquals(mockResult, exeMethodResult);
        Mockito.verify(commandMapper, Mockito.times(1)).insert(command);
    }

    @Test
    public void testTenantForProcess() {
        User user = getAdminUser();
        user.setTenantId(2);
        when(tenantMapper.queryById(1)).thenReturn(null);
        when(userMapper.selectById(1)).thenReturn(user);
        when(tenantMapper.queryById(2)).thenReturn(new Tenant());
        Tenant tenant = processService.getTenantForProcess(1,1);
        Assert.assertNotNull(tenant);
    }

    private User getAdminUser() {
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserName("admin");
        loginUser.setUserType(UserType.GENERAL_USER);
        return loginUser;
    }

}
