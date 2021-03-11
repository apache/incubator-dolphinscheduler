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

package org.apache.dolphinscheduler.server.entity;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class SQLTaskExecutionContextTest {

  /**
   * test parse  json String to TaskExecutionContext
   */
  @Test
  public void testTaskExecutionContext() {
    String contextJson = "{\n"
        + "    \"taskInstanceId\":32,\n"
        + "    \"taskName\":\"test-hive-func\",\n"
        + "    \"startTime\":\"2020-07-19 16:45:46\",\n"
        + "    \"taskType\":\"SQL\",\n"
        + "    \"host\":null,\n"
        + "    \"executePath\":\"/tmp/dolphinscheduler/exec/process/1/5/14/32\",\n"
        + "    \"logPath\":null,\n"
        + "    \"taskJson\":\"{\\\"id\\\":\\\"tasks-70999\\\",\\\"name\\\":\\\"test-hive-func\\\""
        + ",\\\"desc\\\":null,\\\"type\\\":\\\"SQL\\\",\\\"runFlag\\\":\\\"NORMAL\\\","
        + "\\\"loc\\\":null,\\\"maxRetryTimes\\\":0,\\\"retryInterval\\\":1,"
        + "\\\"params\\\":{\\\"type\\\":\\\"HIVE\\\",\\\"datasource\\\":2,"
        + "\\\"sql\\\":\\\"select mid_id, user_id,"
        + " version_code, version_name, lang, source, os, area, model, "
        + "brand, sdk_version, gmail, height_width, app_time, network,"
        + " lng, lat, dt,\\\\n       Lower(model)\\\\nfrom dws_uv_detail_day limit 5;"
        + "\\\",\\\"udfs\\\":\\\"1\\\",\\\"sqlType\\\":\\\"0\\\",\\\"title\\\":\\\""
        + "test-hive-user-func\\\",\\\"receivers\\\":\\\"534634799@qq.com\\\","
        + "\\\"receiversCc\\\":\\\"\\\",\\\"showType\\\":\\\"TABLE\\\",\\\"localParams\\\":[],"
        + "\\\"connParams\\\":\\\"\\\",\\\"preStatements\\\":[],\\\"postStatements\\\":[]},"
        + "\\\"preTasks\\\":[],\\\"extras\\\":null,\\\"depList\\\":[],\\\"dependence\\\":{},"
        + "\\\"conditionResult\\\":{\\\"successNode\\\":[\\\"\\\"],\\\"failedNode\\\":[\\\"\\\"]},"
        + "\\\"taskInstancePriority\\\":\\\"MEDIUM\\\",\\\"workerGroup\\\":\\\"default\\\","
        + "\\\"workerGroupId\\\":null,\\\"timeout\\\":{\\\"strategy\\\":\\\"\\\",\\\"interval\\\":null,"
        + "\\\"enable\\\":false},\\\"conditionsTask\\\":false,\\\"forbidden\\\":false,"
        + "\\\"taskTimeoutParameter\\\":{\\\"enable\\\":false,\\\"strategy\\\":null,"
        + "\\\"interval\\\":0}}\",\n"
        + "    \"processId\":0,\n"
        + "    \"appIds\":null,\n"
        + "    \"processInstanceId\":14,\n"
        + "    \"scheduleTime\":null,\n"
        + "    \"globalParams\":null,\n"
        + "    \"executorId\":2,\n"
        + "    \"cmdTypeIfComplement\":2,\n"
        + "    \"tenantCode\":\"sl\",\n"
        + "    \"queue\":\"sl\",\n"
        + "    \"processDefineId\":5,\n"
        + "    \"projectId\":1,\n"
        + "    \"taskParams\":null,\n"
        + "    \"envFile\":null,\n"
        + "    \"definedParams\":null,\n"
        + "    \"taskAppId\":null,\n"
        + "    \"taskTimeoutStrategy\":0,\n"
        + "    \"taskTimeout\":0,\n"
        + "    \"workerGroup\":\"default\",\n"
        + "    \"resources\":{\n"
        + "    },\n"
        + "    \"sqlTaskExecutionContext\":{\n"
        + "        \"warningGroupId\":0,\n"
        + "        \"connectionParams\":\"{\\\"type\\\":null,\\\"address\\\":"
        + "\\\"jdbc:hive2://localhost:10000\\\",\\\"database\\\":\\\"gmall\\\","
        + "\\\"jdbcUrl\\\":\\\"jdbc:hive2://localhost:10000/gmall\\\","
        + "\\\"user\\\":\\\"sl-test\\\",\\\"password\\\":\\\"123456sl\\\"}\",\n"
        + "        \"udfFuncTenantCodeMap\": null"
        + "    },\n"
        + "    \"dataxTaskExecutionContext\":{\n"
        + "        \"dataSourceId\":0,\n"
        + "        \"sourcetype\":0,\n"
        + "        \"sourceConnectionParams\":null,\n"
        + "        \"dataTargetId\":0,\n"
        + "        \"targetType\":0,\n"
        + "        \"targetConnectionParams\":null\n"
        + "    },\n"
        + "    \"dependenceTaskExecutionContext\":null,\n"
        + "    \"sqoopTaskExecutionContext\":{\n"
        + "        \"dataSourceId\":0,\n"
        + "        \"sourcetype\":0,\n"
        + "        \"sourceConnectionParams\":null,\n"
        + "        \"dataTargetId\":0,\n"
        + "        \"targetType\":0,\n"
        + "        \"targetConnectionParams\":null\n"
        + "    },\n"
        + "    \"procedureTaskExecutionContext\":{\n"
        + "        \"connectionParams\":null\n"
        + "    }\n"
        + "}\n";

    TaskExecutionContext taskExecutionContext = JSONUtils.parseObject(contextJson, TaskExecutionContext.class);

    assertNotNull(taskExecutionContext);
  }

  @Test
  public void testSqlTaskExecutionContext() {

    SQLTaskExecutionContext sqlTaskExecutionContext = new SQLTaskExecutionContext();
    sqlTaskExecutionContext.setWarningGroupId(0);

    Map<UdfFunc, String> udfmap = new HashMap<>();

    UdfFunc udfFunc = new UdfFunc();
    udfFunc.setArgTypes("1");
    udfFunc.setId(1);
    udfFunc.setResourceName("name1");
    udfmap.put(udfFunc, "map1");

    UdfFunc udfFunc2 = new UdfFunc();
    udfFunc2.setArgTypes("2");
    udfFunc2.setId(2);
    udfFunc2.setResourceName("name2");
    udfmap.put(udfFunc2, "map2");

    sqlTaskExecutionContext.setUdfFuncTenantCodeMap(udfmap);

    String contextJson = JSONUtils.toJsonString(sqlTaskExecutionContext);
    SQLTaskExecutionContext parseSqlTask = JSONUtils.parseObject(contextJson, SQLTaskExecutionContext.class);

    assertNotNull(parseSqlTask);
    assertEquals(sqlTaskExecutionContext.getWarningGroupId(), parseSqlTask.getWarningGroupId());
    assertEquals(sqlTaskExecutionContext.getUdfFuncTenantCodeMap().size(), parseSqlTask.getUdfFuncTenantCodeMap().size());
  }

  /**
   * test the SQLTaskExecutionContext
   */
  @Test
  public void testSqlTaskExecutionContextParse() {

    // SQLTaskExecutionContext.udfFuncTenantCodeMap is null
    String contextJson = "{\n"
        + "    \"warningGroupId\":0,\n"
        + "    \"connectionParams\":null,\n"
        + "    \"udfFuncTenantCodeMap\":null"
        + "}\n}";
    SQLTaskExecutionContext parseSqlTask = JSONUtils.parseObject(contextJson, SQLTaskExecutionContext.class);

    assertNotNull(parseSqlTask);
    assertEquals(0,parseSqlTask.getWarningGroupId());
    assertNull(parseSqlTask.getUdfFuncTenantCodeMap());

    // SQLTaskExecutionContext.udfFuncTenantCodeMap is not null
    contextJson = "{\"warningGroupId\":0,"
        + "\"connectionParams\":null,"
        + "\"udfFuncTenantCodeMap\":{\""
        + "{\\\"id\\\":2,\\\"userId\\\":0,"
        + "\\\"funcName\\\":null,\\\"className\\\":null,\\\"argTypes\\\":\\\"2\\\",\\\"database\\\":null,"
        + "\\\"description\\\":null,\\\"resourceId\\\":0,\\\"resourceName\\\":\\\"name2\\\",\\\"type\\\":null,"
        + "\\\"createTime\\\":null,\\\"updateTime\\\":null}\":\"map2\","
        + "\"{\\\"id\\\":1,\\\"userId\\\":0,\\\"funcName\\\":null,"
        + "\\\"className\\\":null,\\\"argTypes\\\":\\\"1\\\","
        + "\\\"database\\\":null,\\\"description\\\":null,"
        + "\\\"resourceId\\\":0,\\\"resourceName\\\":\\\"name1\\\","
        + "\\\"type\\\":null,\\\"createTime\\\":null,\\\"updateTime\\\":null}\":\"map1\"}}\n";

    SQLTaskExecutionContext parseSqlTask2 = JSONUtils.parseObject(contextJson, SQLTaskExecutionContext.class);

    assertNotNull(parseSqlTask2);
    assertEquals(0,parseSqlTask2.getWarningGroupId());
    assertEquals(2,parseSqlTask2.getUdfFuncTenantCodeMap().size());
  }

}