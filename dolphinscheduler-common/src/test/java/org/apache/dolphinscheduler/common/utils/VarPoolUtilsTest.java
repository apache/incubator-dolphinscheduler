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

package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.model.TaskNode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VarPoolUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(VarPoolUtilsTest.class);

    @Test
    public void testConvertVarPoolToMap() throws Exception {
        String varPool = "p1,66$VarPool$p2,69$VarPool$";
        ConcurrentHashMap<String, Object> propToValue = new ConcurrentHashMap<String, Object>();
        VarPoolUtils.convertVarPoolToMap(propToValue, varPool);
        Assert.assertEquals((String)propToValue.get("p1"), "66");
        Assert.assertEquals((String)propToValue.get("p2"), "69");
        logger.info(propToValue.toString());
    }

    @Test
    public void testConvertPythonScriptPlaceholders() throws Exception {
        String rawScript = "print(${p1});\n${setShareVar(${p1},3)};\n${setShareVar(${p2},4)};";
        rawScript = VarPoolUtils.convertPythonScriptPlaceholders(rawScript);
        Assert.assertEquals(rawScript, "print(${p1});\n"
            + "print(\"${{setValue({},{})}}\".format(\"p1\",3));\n"
            + "print(\"${{setValue({},{})}}\".format(\"p2\",4));");
        logger.info(rawScript);
    }

    @Test
    public void testSetTaskNodeLocalParams() throws Exception {
        String taskJson = "{\"id\":\"tasks-66199\",\"name\":\"file-shell\",\"desc\":null,\"type\":\"SHELL\","
                        + "\"runFlag\":\"NORMAL\",\"loc\":null,\"maxRetryTimes\":0,\"retryInterval\":1,\""
                        +  "params\":{\"rawScript\":\"sh n-1/n-1-1/run.sh\",\""
                        + "localParams\":[{\"prop\":\"k1\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"v1\"},{\"prop\":\"k2\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"v2\"},"
                        + "{\"prop\":\"k3\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"v3\"}],\""
                        + "resourceList\":[{\"id\":\"dolphinschedule-code\",\"res\":\"n-1/n-1-1/dolphinscheduler-api-server.log\"},"
                        + "{\"id\":\"mr-code\",\"res\":\"n-1/n-1-1/hadoop-mapreduce-examples-2.7.4.jar\"},"
                        + "{\"id\":\"run\",\"res\":\"n-1/n-1-1/run.sh\"}]},\"preTasks\":[],\"extras\":null,\"depList\":[],\""
                        + "dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"taskInstancePriority\":\"MEDIUM\",\""
                        + "workerGroup\":\"default\",\"workerGroupId\":null,\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},\"delayTime\":0}";
        String changeTaskJson = "{\"id\":\"tasks-66199\",\"name\":\"file-shell\",\"desc\":null,\"type\":\"SHELL\","
                        + "\"runFlag\":\"NORMAL\",\"loc\":null,\"maxRetryTimes\":0,\"retryInterval\":1,\""
                        +  "params\":{\"rawScript\":\"sh n-1/n-1-1/run.sh\",\""
                        + "localParams\":[{\"prop\":\"k1\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"k1-value-change\"},"
                        + "{\"prop\":\"k2\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"k2-value-change\"},"
                        + "{\"prop\":\"k3\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"v3\"}],\""
                        + "resourceList\":[{\"id\":\"dolphinschedule-code\",\"res\":\"n-1/n-1-1/dolphinscheduler-api-server.log\"},"
                        + "{\"id\":\"mr-code\",\"res\":\"n-1/n-1-1/hadoop-mapreduce-examples-2.7.4.jar\"},"
                        + "{\"id\":\"run\",\"res\":\"n-1/n-1-1/run.sh\"}]},\"preTasks\":[],\"extras\":null,\"depList\":[],\""
                        + "dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"taskInstancePriority\":\"MEDIUM\",\""
                        + "workerGroup\":\"default\",\"workerGroupId\":null,\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},\"delayTime\":0}";
        Map<String, Object> propToValue = new HashMap<String, Object>();
        propToValue.put("k1","k1-value-change");
        propToValue.put("k2","k2-value-change");

        TaskNode taskNode = JSONUtils.parseObject(taskJson,TaskNode.class);

        VarPoolUtils.setTaskNodeLocalParams(taskNode,propToValue);

        Assert.assertEquals(changeTaskJson,JSONUtils.toJsonString(taskNode));

    }

}
