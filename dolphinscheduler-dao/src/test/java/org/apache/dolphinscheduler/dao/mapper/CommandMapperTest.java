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
package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.CommandCount;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.common.enums.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *  command mapper test
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class CommandMapperTest {


    @Autowired
    CommandMapper commandMapper;

    @Autowired
    ProcessDefinitionMapper processDefinitionMapper;


    /**
     * test insert
     */
    @Test
    public void testInsert(){
        Command command = createCommand();
        assertThat(command.getId(),greaterThan(0));
    }


    /**
     * test select by id
     */
    @Test
    public void testSelectById() {
        Command expectedCommand = createCommand();
        //query
        Command actualCommand = commandMapper.selectById(expectedCommand.getId());

        assertNotNull(actualCommand);
        assertEquals(expectedCommand.getProcessDefinitionId(), actualCommand.getProcessDefinitionId());
    }

    /**
     * test update
     */
    @Test
    public void testUpdate(){

        Command expectedCommand = createCommand();

        // update the command time if current command if recover from waiting
        expectedCommand.setUpdateTime(DateUtils.getCurrentDate());

        commandMapper.updateById(expectedCommand);

        Command actualCommand = commandMapper.selectById(expectedCommand.getId());

        assertNotNull(actualCommand);
        assertEquals(expectedCommand.getUpdateTime(),actualCommand.getUpdateTime());

    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){
        Command expectedCommand = createCommand();

        commandMapper.deleteById(expectedCommand.getId());

        Command actualCommand = commandMapper.selectById(expectedCommand.getId());

        assertNull(actualCommand);
    }



    /**
     * test query all
     */
    @Test
    public void testGetAll() {
        Integer count = 10;

        Map<Integer, Command> commandMap = createCommandMap(count);


        List<Command> actualCommands = commandMapper.selectList(null);

        assertThat(actualCommands.size(), greaterThanOrEqualTo(count));
    }

    /**
     * test get on command to run
     */
    @Test
    public void testGetOneToRun() {

        ProcessDefinition processDefinition = createProcessDefinition();

        Command expectedCommand = createCommand(CommandType.START_PROCESS,processDefinition.getId());

        Command actualCommand = commandMapper.getOneToRun();

        assertNotNull(actualCommand);
    }

    /**
     * test count command state
     */
    @Test
    public void testCountCommandState() {
        Integer count = 10;

        ProcessDefinition processDefinition = createProcessDefinition();

        CommandCount expectedCommandCount = createCommandMap(count, CommandType.START_PROCESS, processDefinition.getId());

        Integer[] projectIdArray = {processDefinition.getProjectId()};

        Date startTime = DateUtils.stringToDate("2019-12-29 00:10:00");

        Date endTime = DateUtils.stringToDate("2019-12-29 23:59:59");

        List<CommandCount> actualCommandCounts = commandMapper.countCommandState(0, startTime, endTime, projectIdArray);

        assertThat(actualCommandCounts.size(),greaterThanOrEqualTo(1));
    }


    /**
     * create command map
     * @param count map count
     * @param commandType command type
     * @param processDefinitionId process definition id
     * @return command map
     */
    private CommandCount createCommandMap(
            Integer count,
            CommandType commandType,
            Integer processDefinitionId){

        CommandCount commandCount = new CommandCount();

        for (int i = 0 ;i < count ;i++){
            createCommand(commandType,processDefinitionId);
        }
        commandCount.setCommandType(commandType);
        commandCount.setCount(count);

        return commandCount;
    }

    /**
     *  create process definition
     * @return process definition
     */
    private ProcessDefinition createProcessDefinition(){
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        processDefinition.setName("ut test");
        processDefinition.setProjectId(1);
        processDefinition.setFlag(Flag.YES);

        processDefinitionMapper.insert(processDefinition);

        return processDefinition;
    }

    /**
     * create command map
     * @param count map count
     * @return command map
     */
    private Map<Integer,Command> createCommandMap(Integer count){
        Map<Integer,Command> commandMap = new HashMap<>();

        for (int i = 0; i < count ;i++){
            Command command = createCommand();
            commandMap.put(command.getId(),command);
        }
        return commandMap;
    }


    /**
     * create command
     * @return
     */
    private Command createCommand(){
        return createCommand(CommandType.START_PROCESS,1);
    }

    /**
     * create command
     * @return Command
     */
    private Command createCommand(CommandType commandType,Integer processDefinitionId){

        Command command = new Command();
        command.setCommandType(commandType);
        command.setProcessDefinitionId(processDefinitionId);
        command.setExecutorId(4);
        command.setCommandParam("test command param");
        command.setTaskDependType(TaskDependType.TASK_ONLY);
        command.setFailureStrategy(FailureStrategy.CONTINUE);
        command.setWarningType(WarningType.ALL);
        command.setWarningGroupId(1);
        command.setScheduleTime(DateUtils.stringToDate("2019-12-29 12:10:00"));
        command.setProcessInstancePriority(Priority.MEDIUM);
        command.setStartTime(DateUtils.stringToDate("2019-12-29 10:10:00"));
        command.setUpdateTime(DateUtils.stringToDate("2019-12-29 10:10:00"));
        command.setWorkerGroup(Constants.DEFAULT_WORKER_GROUP);
        commandMapper.insert(command);

        return command;
    }



}