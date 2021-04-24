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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.AlertPluginInstanceServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.AlertPluginInstanceVO;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * alert plugin instance service test
 */
@RunWith(MockitoJUnitRunner.class)
public class AlertPluginInstanceServiceTest {

    @InjectMocks
    AlertPluginInstanceServiceImpl alertPluginInstanceService;

    @Mock
    private AlertPluginInstanceMapper alertPluginInstanceMapper;

    @Mock
    private PluginDefineMapper pluginDefineMapper;

    @Mock
    private AlertGroupMapper alertGroupMapper;

    private List<AlertPluginInstance> alertPluginInstances;

    private User user;

    private String uiParams = "[\n"
            + "    {\n"
            + "        \"field\":\"userParams\",\n"
            + "        \"name\":\"user.params\",\n"
            + "        \"props\":{\n"
            + "            \"placeholder\":\"please enter your custom parameters, which will be passed to you when calling your script\",\n"
            + "            \"size\":\"small\"\n"
            + "        },\n"
            + "        \"type\":\"input\",\n"
            + "        \"title\":\"user.params\",\n"
            + "        \"value\":\"userParams\",\n"
            + "        \"validate\":[\n"
            + "            {\n"
            + "                \"required\":false,\n"
            + "                \"message\":null,\n"
            + "                \"type\":\"string\",\n"
            + "                \"trigger\":\"blur\",\n"
            + "                \"min\":null,\n"
            + "                \"max\":null\n"
            + "            }\n"
            + "        ]\n"
            + "    },\n"
            + "    {\n"
            + "        \"field\":\"path\",\n"
            + "        \"name\":\"path\",\n"
            + "        \"props\":{\n"
            + "            \"placeholder\":\"please upload the file to the disk directory of the alert server, and ensure that the path is absolute and has the corresponding access rights\",\n"
            + "            \"size\":\"small\"\n"
            + "        },\n"
            + "        \"type\":\"input\",\n"
            + "        \"title\":\"path\",\n"
            + "        \"value\":\"/kris/script/path\",\n"
            + "        \"validate\":[\n"
            + "            {\n"
            + "                \"required\":true,\n"
            + "                \"message\":null,\n"
            + "                \"type\":\"string\",\n"
            + "                \"trigger\":\"blur\",\n"
            + "                \"min\":null,\n"
            + "                \"max\":null\n"
            + "            }\n"
            + "        ]\n"
            + "    },\n"
            + "    {\n"
            + "        \"field\":\"type\",\n"
            + "        \"name\":\"type\",\n"
            + "        \"props\":{\n"
            + "            \"placeholder\":null,\n"
            + "            \"size\":\"small\"\n"
            + "        },\n"
            + "        \"type\":\"radio\",\n"
            + "        \"title\":\"type\",\n"
            + "        \"value\":0,\n"
            + "        \"validate\":[\n"
            + "            {\n"
            + "                \"required\":true,\n"
            + "                \"message\":null,\n"
            + "                \"type\":\"string\",\n"
            + "                \"trigger\":\"blur\",\n"
            + "                \"min\":null,\n"
            + "                \"max\":null\n"
            + "            }\n"
            + "        ],\n"
            + "        \"options\":[\n"
            + "            {\n"
            + "                \"label\":\"SHELL\",\n"
            + "                \"value\":0,\n"
            + "                \"disabled\":false\n"
            + "            }\n"
            + "        ]\n"
            + "    }\n"
            + "]\n"
            + "\n";

    private String paramsMap = "{\"path\":\"/kris/script/path\",\"userParams\":\"userParams\",\"type\":\"0\"}";

    @Before
    public void before() {
        user = new User();
        user.setUserType(UserType.ADMIN_USER);
        user.setId(1);
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance();
        alertPluginInstance.setPluginInstanceParams("test1");
        alertPluginInstance.setPluginDefineId(1);
        alertPluginInstance.setId(1);
        alertPluginInstance.setPluginInstanceParams("test");
        alertPluginInstances = new ArrayList<>();
        alertPluginInstances.add(alertPluginInstance);
    }

    @Test
    public void testCreate() {
        Mockito.when(alertPluginInstanceMapper.existInstanceName("test")).thenReturn(true);
        Result<Void> result = alertPluginInstanceService.create(user, 1, "test", uiParams);
        Assert.assertEquals(Status.PLUGIN_INSTANCE_ALREADY_EXIT.getCode(), (int) result.getCode());
        Mockito.when(alertPluginInstanceMapper.insert(Mockito.any())).thenReturn(1);
        result = alertPluginInstanceService.create(user, 1, "test1", uiParams);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

    @Test
    public void testDelete() {
        List<String> ids = Arrays.asList("11,2,3", null, "98,1");
        Mockito.when(alertGroupMapper.queryInstanceIdsList()).thenReturn(ids);
        Result<Void> result = alertPluginInstanceService.delete(user, 1);
        Assert.assertEquals(Status.DELETE_ALERT_PLUGIN_INSTANCE_ERROR_HAS_ALERT_GROUP_ASSOCIATED.getCode(), (int) result.getCode());
        Mockito.when(alertPluginInstanceMapper.deleteById(9)).thenReturn(1);
        result = alertPluginInstanceService.delete(user, 9);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());

    }

    @Test
    public void testUpdate() {
        Mockito.when(alertPluginInstanceMapper.updateById(Mockito.any())).thenReturn(0);
        Result<Void> result = alertPluginInstanceService.update(user, 1, "testUpdate", uiParams);
        Assert.assertEquals(Status.SAVE_ERROR.getCode(), (int) result.getCode());
        Mockito.when(alertPluginInstanceMapper.updateById(Mockito.any())).thenReturn(1);
        result = alertPluginInstanceService.update(user, 1, "testUpdate", uiParams);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

    @Test
    public void testQueryAll() {
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance();
        alertPluginInstance.setId(1);
        alertPluginInstance.setPluginDefineId(1);
        alertPluginInstance.setPluginInstanceParams(paramsMap);
        alertPluginInstance.setInstanceName("test");
        PluginDefine pluginDefine = new PluginDefine("script", "script", uiParams);
        pluginDefine.setId(1);
        List<PluginDefine> pluginDefines = Collections.singletonList(pluginDefine);
        List<AlertPluginInstance> pluginInstanceList = Collections.singletonList(alertPluginInstance);
        Mockito.when(alertPluginInstanceMapper.queryAllAlertPluginInstanceList()).thenReturn(pluginInstanceList);
        Mockito.when(pluginDefineMapper.queryAllPluginDefineList()).thenReturn(pluginDefines);
        Result<List<AlertPluginInstanceVO>> result = alertPluginInstanceService.queryAll();
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

}
