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

package org.apache.dolphinscheduler.server.worker.task.sql;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.task.sql.SqlParameters;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.datasource.BaseDataSource;
import org.apache.dolphinscheduler.dao.datasource.MySQLDataSource;
import org.apache.dolphinscheduler.server.entity.SQLTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * sql task test
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(value = {SqlTask.class, DriverManager.class, SpringApplicationContext.class, ParameterUtils.class})
public class SqlTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(SqlTaskTest.class);

    private static final String CONNECTION_PARAMS = "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:mysql://127.0.0.1:3306\","
            + "\"database\":\"test\",\"jdbcUrl\":\"jdbc:mysql://127.0.0.1:3306/test\"}";

    private SqlTask sqlTask;

    private TaskExecutionContext taskExecutionContext;

    private AlertClientService alertClientService;

    @Before
    public void before() throws Exception {
        taskExecutionContext = new TaskExecutionContext();

        TaskProps props = new TaskProps();
        props.setExecutePath("/tmp");
        props.setTaskAppId(String.valueOf(System.currentTimeMillis()));
        props.setTaskInstanceId(1);
        props.setTenantCode("1");
        props.setEnvFile(".dolphinscheduler_env.sh");
        props.setTaskStartTime(new Date());
        props.setTaskTimeout(0);
        props.setTaskParams(
                "{\"localParams\":[{\"prop\":\"ret\", \"direct\":\"OUT\", \"type\":\"VARCHAR\", \"value\":\"\"}],"
                        + "\"type\":\"POSTGRESQL\",\"datasource\":1,\"sql\":\"insert into tb_1 values('1','2')\","
                        + "\"sqlType\":1}");

        taskExecutionContext = PowerMockito.mock(TaskExecutionContext.class);
        PowerMockito.when(taskExecutionContext.getTaskParams()).thenReturn(props.getTaskParams());
        PowerMockito.when(taskExecutionContext.getExecutePath()).thenReturn("/tmp");
        PowerMockito.when(taskExecutionContext.getTaskAppId()).thenReturn("1");
        PowerMockito.when(taskExecutionContext.getTenantCode()).thenReturn("root");
        PowerMockito.when(taskExecutionContext.getStartTime()).thenReturn(new Date());
        PowerMockito.when(taskExecutionContext.getTaskTimeout()).thenReturn(10000);
        PowerMockito.when(taskExecutionContext.getLogPath()).thenReturn("/tmp/dx");

        PowerMockito.mockStatic(SpringApplicationContext.class);
        PowerMockito.when(SpringApplicationContext.getBean(Mockito.any())).thenReturn(new AlertDao());

        SQLTaskExecutionContext sqlTaskExecutionContext = new SQLTaskExecutionContext();
        sqlTaskExecutionContext.setConnectionParams(CONNECTION_PARAMS);
        PowerMockito.when(taskExecutionContext.getSqlTaskExecutionContext()).thenReturn(sqlTaskExecutionContext);

        PowerMockito.mockStatic(SpringApplicationContext.class);
        PowerMockito.when(SpringApplicationContext.getBean(Mockito.any())).thenReturn(new AlertDao());
        alertClientService = PowerMockito.mock(AlertClientService.class);
        sqlTask = new SqlTask(taskExecutionContext, logger, alertClientService);
        sqlTask.init();
    }

    @Test
    public void testGetParameters() {
        Assert.assertNotNull(sqlTask.getParameters());
    }

    @Test
    public void testHandle() throws Exception {
        Connection connection = PowerMockito.mock(Connection.class);
        PowerMockito.mockStatic(DriverManager.class);
        PowerMockito.when(DriverManager.getConnection(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(connection);
        PreparedStatement preparedStatement = PowerMockito.mock(PreparedStatement.class);
        PowerMockito.when(connection.prepareStatement(Mockito.any())).thenReturn(preparedStatement);
        PowerMockito.mockStatic(ParameterUtils.class);
        PowerMockito.when(ParameterUtils.replaceScheduleTime(Mockito.any(), Mockito.any())).thenReturn("insert into tb_1 values('1','2')");

        sqlTask.handle();
        Assert.assertEquals(Constants.EXIT_CODE_SUCCESS, sqlTask.getExitStatusCode());
    }

    @Test(expected = Exception.class)
    public void testHandlePreQuerySql() throws Exception {
        Connection connection = PowerMockito.mock(Connection.class);
        PowerMockito.mockStatic(DriverManager.class);
        PowerMockito.when(DriverManager.getConnection(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(connection);

        BaseDataSource ds = PowerMockito.mock(MySQLDataSource.class);
        PowerMockito.when(ds.getConnection()).thenReturn(connection);

        PreparedStatement preparedStatement = PowerMockito.mock(PreparedStatement.class);
        PowerMockito.when(connection.prepareStatement(Mockito.any())).thenReturn(preparedStatement);
        PowerMockito.mockStatic(ParameterUtils.class);
        PowerMockito.when(ParameterUtils.replaceScheduleTime(Mockito.any(), Mockito.any())).thenReturn("insert into tb_1 values('1','2')");

        SqlParameters parameters = (SqlParameters) sqlTask.getParameters();
        parameters.setPreStatements(Arrays.asList("select aa1,aa2 from dual", "SELECT xy from dual"));
        //sqlTask.handle();
        sqlTask.handlePreQuerySql(parameters.getPreStatements());
        System.out.println(sqlTask.getParameters().getLocalParams().toString());
        Assert.assertTrue(sqlTask.getParameters().getLocalParametersMap()
                .keySet().containsAll(Arrays.asList("aa1", "aa2", "xy")));
    }

    @Test
    public void testIsQuerySql() {
        SqlParameters parameters = (SqlParameters) sqlTask.getParameters();
        parameters.setPreStatements(Arrays.asList("select aa1,aa2 from dual", "SELECT xy from dual"));
        Assert.assertTrue(sqlTask.isQuerySql(parameters.getPreStatements().get(0)));
        Assert.assertTrue(sqlTask.isQuerySql(parameters.getPreStatements().get(1)));
    }

    @Test
    public void testIsQuerySqlFail() {
        SqlParameters parameters = (SqlParameters) sqlTask.getParameters();
        parameters.setPreStatements(Arrays.asList("update xxaa1,aa2 from dual", "SELECT1 xy from dual"));
        Assert.assertTrue(!sqlTask.isQuerySql(parameters.getPreStatements().get(0)));
        Assert.assertTrue(!sqlTask.isQuerySql(parameters.getPreStatements().get(1)));
    }

    @Test
    public void testIsQuerySqlInputNotnull() {
        SqlParameters parameters = (SqlParameters) sqlTask.getParameters();
        parameters.setPreStatements(Arrays.asList("update xxaa1,aa2 from dual", "SELECT1 xy from dual"));
        Assert.assertTrue(sqlTask.isQuerySql("select x from dual"));
    }

    @Test
    public void testIsQuerySqlInputNull() {
        SqlParameters parameters = (SqlParameters) sqlTask.getParameters();
        parameters.setPreStatements(Arrays.asList("update xxaa1,aa2 from dual", "SELECT1 xy from dual"));
        Assert.assertTrue(!sqlTask.isQuerySql(""));
    }

    @Test
    public void testIsQuerySqlSelect() {
        SqlParameters parameters = (SqlParameters) sqlTask.getParameters();
        parameters.setPreStatements(Arrays.asList("update xxaa1,aa2 from dual", "SELECT xy from dual"));
        Assert.assertTrue(sqlTask.isQuerySql(parameters.getPreStatements().get(1)));
    }

    @Test
    public void testIsQuerySqlNot() {
        SqlParameters parameters = (SqlParameters) sqlTask.getParameters();
        parameters.setPreStatements(Arrays.asList("update xxaa1,aa2 from dual", "SELECT1 xy from dual"));
        Assert.assertTrue(!sqlTask.isQuerySql(parameters.getPreStatements().get(0)));
        Assert.assertTrue(!sqlTask.isQuerySql(parameters.getPreStatements().get(1)));
    }

    @Test
    public void testIsQuerySqlOne() {
        Assert.assertTrue(sqlTask.isQuerySql("select a,bc,d from dual"));
    }

    @Test
    public void testIsQuerySql2() {
        Assert.assertTrue(!sqlTask.isQuerySql("update a,bc,d from dual"));
    }

    @Test(expected = Exception.class)
    public void testExtractPreQuerySqlParams() throws Exception {
        SqlParameters parameters = (SqlParameters) sqlTask.getParameters();
        parameters.setPreStatements(Arrays.asList("select aa1,aa2,xy from dual"));
        Connection connection = PowerMockito.mock(Connection.class);
        PowerMockito.mockStatic(DriverManager.class);
        PowerMockito.when(DriverManager.getConnection(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(connection);

        PreparedStatement preparedStatement = PowerMockito.mock(PreparedStatement.class);
        PowerMockito.when(connection.prepareStatement(Mockito.any())).thenReturn(preparedStatement);

        sqlTask.extractPreQuerySqlParams(connection, sqlTask.getSqlAndSqlParamsMap(parameters.getPreStatements().get(0)));
        Assert.assertTrue(sqlTask.getParameters().getLocalParametersMap()
                .keySet().containsAll(Arrays.asList("aa1", "aa2", "xy")));
    }

}
