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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionVersion;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionVersionMapper;

import java.util.List;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;


/**
 * process definition controller test
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProcessDefinitionControllerTest extends AbstractControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionControllerTest.class);

    public static String projectName;
    public static String projectId;
    public static String definitionId;

    @Autowired
    private ProcessDefinitionVersionMapper processDefinitionVersionMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Test
    public void stage1_createProject() throws Exception {
        projectName = "project_test1";
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("projectName", projectName);
        paramsMap.add("description", "the test project");

        MvcResult mvcResult = mockMvc.perform(post("/projects/create")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), new TypeReference<Result<String>>() {
        });
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info("create project return result:{}", mvcResult.getResponse().getContentAsString());

        projectId = (String) result.getData();
    }


    @Test
    public void stage2_testCreateProcessDefinition() throws Exception {
        String json = "{\"globalParams\":[],\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-36196\",\"name\""
                + ":\"ssh_test1\",\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"aa=\\\"1234\\\"\\"
                + "necho ${aa}\"},\"desc\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\""
                + ",\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},"
                + "\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,\"preTasks\":[]}],\"tenantId\":-1,\"timeout\":0}";
        String locations = "{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}";
        String name = "dag_test";
        String description = "desc test";
        String connects = "[]";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("name", name);
        paramsMap.add("processDefinitionJson", json);
        paramsMap.add("locations", locations);
        paramsMap.add("connects", connects);
        paramsMap.add("description", description);

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/process/save", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());

        definitionId = result.getData().toString();


    }

    @Test
    public void stage2_testVerifyProcessDefinitionName() throws Exception {
        String name = "dag_test_1";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("name", name);

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/verify-name", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());

    }

    @Test
    public void stage2_updateProcessDefinition() throws Exception {

        String json = "{\"globalParams\":[],\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-36196\",\"name\":\"ssh_test1\""
                + ",\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"aa=\\\"1234\\\"\\necho ${aa}\"}"
                + ",\"desc\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\",\"retryInterval\""
                + ":\"1\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},\"taskInstancePriority\""
                + ":\"MEDIUM\",\"workerGroupId\":-1,\"preTasks\":[]}],\"tenantId\":-1,\"timeout\":0}";
        String locations = "{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}";
        String name = "dag_test";
        String description = "desc test";
        String connects = "[]";
        String id = definitionId;

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id", id);
        paramsMap.add("name", name);
        paramsMap.add("processDefinitionJson", json);
        paramsMap.add("locations", locations);
        paramsMap.add("connects", connects);
        paramsMap.add("description", description);
        paramsMap.add("releaseState", ReleaseState.OFFLINE.toString());

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/process/update", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void stage2_testReleaseProcessDefinition() throws Exception {
        String id = definitionId;

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processId", id);
        paramsMap.add("releaseState", ReleaseState.OFFLINE.toString());

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/process/release", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void stage2_testQueryProcessDefinitionById() throws Exception {
        String id = definitionId;

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processId", id);
        paramsMap.add("releaseState", ReleaseState.OFFLINE.toString());

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/select-by-id", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void stage3_testBatchCopyProcessDefinition() throws Exception {

        String targetProjectId = projectId;
        String id = definitionId;

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("targetProjectId", targetProjectId);
        paramsMap.add("processDefinitionIds", id);

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/process/copy", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void stage3_testBatchMoveProcessDefinition() throws Exception {

        String targetProjectId = projectId;
        String id = definitionId;

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("targetProjectId", targetProjectId);
        paramsMap.add("processDefinitionIds", id);

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/process/move", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void stage2_testQueryProcessDefinitionList() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/list", projectName)
                .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void stage2_testGetNodeListByDefinitionId() throws Exception {
        String id = definitionId;

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionId", id);

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/gen-task-list", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void stage2_testGetNodeListByDefinitionIdList() throws Exception {
        String idList = definitionId + "";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionIdList", idList);

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/get-task-list", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void stage2_testQueryProcessDefinitionAllByProjectId() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("projectId", projectId);

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/queryProcessDefinitionAllByProjectId", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void stage2_testViewTree() throws Exception {
        String processId = definitionId;
        String limit = "2";
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processId", processId);
        paramsMap.add("limit", limit);

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/view-tree", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void stage3_testQueryProcessDefinitionListPaging() throws Exception {
        String pageNo = "1";
        String pageSize = "10";
        String searchVal = "";
        String userId = "1";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pageNo", pageNo);
        paramsMap.add("pageSize", pageSize);
        paramsMap.add("searchVal", searchVal);
        paramsMap.add("userId", userId);

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/list-paging", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void stage3_testBatchExportProcessDefinitionByIds() throws Exception {
        String processDefinitionIds = "" + definitionId;
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionIds", processDefinitionIds);

        mockMvc.perform(get("/projects/{projectName}/process/export", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

    }

    @Test
    public void stage2_testQueryProcessDefinitionVersions() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionId", definitionId);
        paramsMap.set("pageNo", "1");
        paramsMap.set("pageSize", "10");

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/versions", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

    @Test
    public void stage3_testSwitchProcessDefinitionVersion() throws Exception {
        // query definition version
        Page<ProcessDefinitionVersion> page = new Page<>(1, 10);
        IPage<ProcessDefinitionVersion> processDefinitionVersionsPaging = processDefinitionVersionMapper.queryProcessDefinitionVersionsPaging(page, Integer.parseInt(definitionId));
        List<ProcessDefinitionVersion> records = processDefinitionVersionsPaging.getRecords();
        ProcessDefinitionVersion processDefinitionVersion = records.get(0);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionId", definitionId);
        paramsMap.add("version", String.valueOf(processDefinitionVersion.getVersion()));

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/version/switch", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void stage4_testDeleteProcessDefinitionVersion() throws Exception {
        // query definition version
        Page<ProcessDefinitionVersion> page = new Page<>(1, 10);
        IPage<ProcessDefinitionVersion> processDefinitionVersionsPaging = processDefinitionVersionMapper.queryProcessDefinitionVersionsPaging(page, Integer.parseInt(definitionId));
        List<ProcessDefinitionVersion> records = processDefinitionVersionsPaging.getRecords();

        for (ProcessDefinitionVersion record : records) {
            MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
            paramsMap.add("processDefinitionId", definitionId);
            paramsMap.add("version", String.valueOf(record.getVersion()));

            MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process//version/delete", projectName)
                    .header(SESSION_ID, sessionId)
                    .params(paramsMap))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn();

            Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
            Assert.assertNotNull(result);
            Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        }
    }

    @Test
    public void stage4_testDeleteProcessDefinitionById() throws Exception {
        String id = definitionId;

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionId", id);

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/process/delete", projectName)
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
    }

    @Test
    public void stage5_deleteProject() throws Exception {
        // delete all definition
        processDefinitionMapper.delete(new QueryWrapper<ProcessDefinition>().eq("project_id", Integer.parseInt(projectId)));

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("projectId", projectId);

        MvcResult mvcResult = mockMvc.perform(get("/projects/delete")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info("delete project return result:{}", mvcResult.getResponse().getContentAsString());
    }

}
