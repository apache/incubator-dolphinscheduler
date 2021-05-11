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

package org.apache.dolphinscheduler.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.TaskInstanceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * task instance controller test
 */
public class TaskInstanceControllerTest extends AbstractControllerTest {

    @InjectMocks
    private TaskInstanceController taskInstanceController;

    @Mock
    private TaskInstanceService taskInstanceService;

    @Test
    public void testQueryTaskListPaging() {

        Map<String,Object> result = new HashMap<>();
        Integer pageNo = 1;
        Integer pageSize = 20;
        PageInfo pageInfo = new PageInfo<TaskInstance>(pageNo, pageSize);
        result.put(Constants.DATA_LIST, pageInfo);
        result.put(Constants.STATUS, Status.SUCCESS);

        when(taskInstanceService.queryTaskListPaging(any(), eq(""),  eq(1), eq(""), eq(""), eq(""),any(), any(),
                eq(""), Mockito.any(), eq("192.168.xx.xx"), any(), any())).thenReturn(result);
        Result taskResult = taskInstanceController.queryTaskListPaging("", 1, "", "",
                "", "", ExecutionStatus.SUCCESS,"192.168.xx.xx", "2020-01-01 00:00:00", "2020-01-02 00:00:00",pageNo, pageSize);
        Assert.assertEquals(Integer.valueOf(Status.SUCCESS.getCode()), taskResult.getCode());
    }

    @Ignore
    @Test
    public void testForceTaskSuccess() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("taskInstanceId", "104");

        Map<String, Object> mockResult = new HashMap<>(5);
        mockResult.put(Constants.STATUS, Status.SUCCESS);
        mockResult.put(Constants.MSG, Status.SUCCESS.getMsg());
        when(taskInstanceService.forceTaskSuccess(any(User.class), anyString(), anyInt())).thenReturn(mockResult);

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/task-instance/force-success", "cxc_1113")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

}
