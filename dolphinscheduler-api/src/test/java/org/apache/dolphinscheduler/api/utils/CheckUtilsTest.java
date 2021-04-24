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

package org.apache.dolphinscheduler.api.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.dolphinscheduler.api.dto.CheckParamResult;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.datax.DataxParameters;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.task.flink.FlinkParameters;
import org.apache.dolphinscheduler.common.task.http.HttpParameters;
import org.apache.dolphinscheduler.common.task.mr.MapReduceParameters;
import org.apache.dolphinscheduler.common.task.procedure.ProcedureParameters;
import org.apache.dolphinscheduler.common.task.python.PythonParameters;
import org.apache.dolphinscheduler.common.task.shell.ShellParameters;
import org.apache.dolphinscheduler.common.task.spark.SparkParameters;
import org.apache.dolphinscheduler.common.task.sql.SqlParameters;
import org.apache.dolphinscheduler.common.task.subprocess.SubProcessParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(CheckUtilsTest.class);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * check username
     */
    @Test
    public void testCheckUserName() {

        assertTrue(CheckUtils.checkUserName("test01"));

        assertFalse(CheckUtils.checkUserName(null));

        assertFalse(CheckUtils.checkUserName("test01@abc"));
    }

    /**
     * check email
     */
    @Test
    public void testCheckEmail() {

        assertTrue(CheckUtils.checkEmail("test01@gmail.com"));

        assertFalse(CheckUtils.checkEmail("test01@gmail"));
    }

    /**
     * check desc
     */
    @Test
    public void testCheckDesc() {

        CheckParamResult checkResult = CheckUtils.checkDesc("I am desc");
        Status status = checkResult.getStatus();

        assertEquals(status.getCode(), Status.SUCCESS.getCode());

    }

    @Test
    public void testCheckOtherParams() {
        assertFalse(CheckUtils.checkOtherParams(null));
        assertFalse(CheckUtils.checkOtherParams(""));
        assertTrue(CheckUtils.checkOtherParams("xxx"));
        assertFalse(CheckUtils.checkOtherParams("{}"));
        assertFalse(CheckUtils.checkOtherParams("{\"key1\":111}"));
    }
    /**
     * check passwd
     */
    @Test
    public void testCheckPassword() {

        assertFalse(CheckUtils.checkPassword(null));

        assertFalse(CheckUtils.checkPassword("a"));

        assertFalse(CheckUtils.checkPassword("1234567890abcderfasdf2"));

        assertTrue(CheckUtils.checkPassword("123456"));
    }

    /**
     * check phone
     */
    @Test
    public void testCheckPhone() {

        // phone can be null
        assertTrue(CheckUtils.checkPhone(null));

        assertFalse(CheckUtils.checkPhone("14567134578654"));

        assertTrue(CheckUtils.checkPhone("17362537263"));
    }
    @Test
    public void testCheckTaskNodeParameters() {

        assertFalse(CheckUtils.checkTaskNodeParameters(null,null));
        assertFalse(CheckUtils.checkTaskNodeParameters(null,"unKnown"));
        assertFalse(CheckUtils.checkTaskNodeParameters("unKnown","unKnown"));
        assertFalse(CheckUtils.checkTaskNodeParameters("unKnown",null));

        // sub SubProcessParameters
        SubProcessParameters subProcessParameters = new SubProcessParameters();
        assertFalse(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(subProcessParameters), TaskType.SUB_PROCESS.toString()));
        subProcessParameters.setProcessDefinitionId(1234);
        assertTrue(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(subProcessParameters), TaskType.SUB_PROCESS.toString()));

        // ShellParameters
        ShellParameters shellParameters = new ShellParameters();
        assertFalse(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(shellParameters), TaskType.SHELL.toString()));
        shellParameters.setRawScript("");
        assertFalse(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(shellParameters), TaskType.SHELL.toString()));
        shellParameters.setRawScript("sss");
        assertTrue(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(shellParameters), TaskType.SHELL.toString()));

        // ProcedureParameters
        ProcedureParameters procedureParameters = new ProcedureParameters();
        assertFalse(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(procedureParameters), TaskType.PROCEDURE.toString()));
        procedureParameters.setDatasource(1);
        procedureParameters.setType("xx");
        procedureParameters.setMethod("yy");
        assertTrue(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(procedureParameters), TaskType.PROCEDURE.toString()));

        // SqlParameters
        SqlParameters sqlParameters = new SqlParameters();
        assertFalse(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(sqlParameters), TaskType.SQL.toString()));
        sqlParameters.setDatasource(1);
        sqlParameters.setType("xx");
        sqlParameters.setSql("yy");
        assertTrue(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(sqlParameters), TaskType.SQL.toString()));

        // MapReduceParameters
        MapReduceParameters mapreduceParameters = new MapReduceParameters();
        assertFalse(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(mapreduceParameters), TaskType.MR.toString()));

        ResourceInfo resourceInfoMapreduce = new ResourceInfo();
        resourceInfoMapreduce.setId(1);
        resourceInfoMapreduce.setRes("");
        mapreduceParameters.setMainJar(resourceInfoMapreduce);
        mapreduceParameters.setProgramType(ProgramType.JAVA);
        assertTrue(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(mapreduceParameters), TaskType.MR.toString()));

        // SparkParameters
        SparkParameters sparkParameters = new SparkParameters();
        assertFalse(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(sparkParameters), TaskType.SPARK.toString()));
        sparkParameters.setMainJar(new ResourceInfo());
        sparkParameters.setProgramType(ProgramType.SCALA);
        sparkParameters.setSparkVersion("1.1.1");
        assertTrue(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(sparkParameters), TaskType.SPARK.toString()));

        // PythonParameters
        PythonParameters pythonParameters = new PythonParameters();
        assertFalse(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(pythonParameters), TaskType.PYTHON.toString()));
        pythonParameters.setRawScript("ss");
        assertTrue(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(pythonParameters), TaskType.PYTHON.toString()));

        // DependentParameters
        DependentParameters dependentParameters = new DependentParameters();
        assertTrue(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(dependentParameters), TaskType.DEPENDENT.toString()));

        // FlinkParameters
        FlinkParameters flinkParameters = new FlinkParameters();
        assertFalse(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(flinkParameters), TaskType.FLINK.toString()));
        flinkParameters.setMainJar(new ResourceInfo());
        flinkParameters.setProgramType(ProgramType.JAVA);
        assertTrue(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(flinkParameters), TaskType.FLINK.toString()));

        // HTTP
        HttpParameters httpParameters = new HttpParameters();
        assertFalse(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(httpParameters), TaskType.HTTP.toString()));
        httpParameters.setUrl("httpUrl");
        assertTrue(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(httpParameters), TaskType.HTTP.toString()));

        // DataxParameters
        DataxParameters dataxParameters = new DataxParameters();
        assertFalse(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(dataxParameters), TaskType.DATAX.toString()));
        dataxParameters.setCustomConfig(0);
        dataxParameters.setDataSource(111);
        dataxParameters.setDataTarget(333);
        dataxParameters.setSql("sql");
        dataxParameters.setTargetTable("tar");
        assertTrue(CheckUtils.checkTaskNodeParameters(JSONUtils.toJsonString(dataxParameters), TaskType.DATAX.toString()));
    }

}