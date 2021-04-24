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
import org.apache.dolphinscheduler.api.service.impl.WorkerGroupServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.utils.ZookeeperMonitor;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.ZKNodeType;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;
import org.apache.dolphinscheduler.service.zk.ZookeeperConfig;

import java.util.ArrayList;
import java.util.HashMap;
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
 * worker group service test
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkerGroupServiceTest {

    @InjectMocks
    private WorkerGroupServiceImpl workerGroupService;

    @Mock
    private WorkerGroupMapper workerGroupMapper;

    @Mock
    private ProcessInstanceMapper processInstanceMapper;

    @Mock
    private ZookeeperCachedOperator zookeeperCachedOperator;

    @Mock
    private ZookeeperMonitor zookeeperMonitor;

    private String groupName = "groupName000001";

    @Before
    public void init() {
        ZookeeperConfig zookeeperConfig = new ZookeeperConfig();
        zookeeperConfig.setDsRoot("/dolphinscheduler_qzw");
        Mockito.when(zookeeperCachedOperator.getZookeeperConfig()).thenReturn(zookeeperConfig);

        String workerPath = zookeeperCachedOperator.getZookeeperConfig().getDsRoot() + Constants.ZOOKEEPER_DOLPHINSCHEDULER_WORKERS;

        List<String> workerGroupStrList = new ArrayList<>();
        workerGroupStrList.add("default");
        workerGroupStrList.add("test");
        Mockito.when(zookeeperCachedOperator.getChildrenKeys(workerPath)).thenReturn(workerGroupStrList);

        List<String> defaultAddressList = new ArrayList<>();
        defaultAddressList.add("192.168.220.188:1234");
        defaultAddressList.add("192.168.220.189:1234");

        Mockito.when(zookeeperCachedOperator.getChildrenKeys(workerPath + "/default")).thenReturn(defaultAddressList);

        Mockito.when(zookeeperCachedOperator.get(workerPath + "/default" + "/" + defaultAddressList.get(0))).thenReturn("0.01,0.17,0.03,25.83,8.0,1.0,2020-07-21 11:17:59,2020-07-21 14:39:20,0,13238");
    }

    /**
     *  create or update a worker group
     */
    @Test
    public void testSaveWorkerGroup() {
        // worker server maps
        Map<String, String> serverMaps = new HashMap<>();
        serverMaps.put("127.0.0.1:1234", "0.3,0.07,4.4,7.42,16.0,0.3,2021-03-19 20:17:58,2021-03-19 20:25:29,0,79214");
        Mockito.when(zookeeperMonitor.getServerMaps(ZKNodeType.WORKER, true)).thenReturn(serverMaps);

        User user = new User();
        // general user add
        user.setUserType(UserType.GENERAL_USER);
        Result<Void> result = workerGroupService.saveWorkerGroup(user, 0, groupName, "127.0.0.1:1234");
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getMsg(), result.getMsg());

        // success
        user.setUserType(UserType.ADMIN_USER);
        result = workerGroupService.saveWorkerGroup(user, 0, groupName, "127.0.0.1:1234");
        Assert.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
        // group name exist
        Mockito.when(workerGroupMapper.selectById(2)).thenReturn(getWorkerGroup(2));
        Mockito.when(workerGroupMapper.queryWorkerGroupByName(groupName)).thenReturn(getList());
        result = workerGroupService.saveWorkerGroup(user, 2, groupName, "127.0.0.1:1234");
        Assert.assertEquals(Status.NAME_EXIST.getCode(), (int) result.getCode());
    }

    /**
     * query worker group paging
     */
    @Test
    public void testQueryAllGroupPaging() {
        User user = new User();
        // general user add
        user.setUserType(UserType.ADMIN_USER);
        Result<PageListVO<WorkerGroup>> result = workerGroupService.queryAllGroupPaging(user, 1, 10, null);
        PageListVO<WorkerGroup> pageInfo = result.getData();
        Assert.assertEquals(pageInfo.getTotalList().size(), 1);
    }

    @Test
    public void testQueryAllGroup() {
        Result<List<String>> result = workerGroupService.queryAllGroup();
        List<String> workerGroups = result.getData();
        Assert.assertEquals(workerGroups.size(), 1);
    }

    /**
     * delete group by id
     */
    @Test
    public  void testDeleteWorkerGroupById() {
        User user = new User();
        user.setUserType(UserType.ADMIN_USER);
        WorkerGroup wg2 = getWorkerGroup(2);
        Mockito.when(workerGroupMapper.selectById(2)).thenReturn(wg2);
        Mockito.when(processInstanceMapper.queryByWorkerGroupNameAndStatus(wg2.getName(), Constants.NOT_TERMINATED_STATES)).thenReturn(getProcessInstanceList());
        Result<Void> result = workerGroupService.deleteWorkerGroupById(user, 1);
        Assert.assertEquals(Status.DELETE_WORKER_GROUP_NOT_EXIST.getCode(), (int) result.getCode());
        result = workerGroupService.deleteWorkerGroupById(user, 2);
        Assert.assertEquals(Status.DELETE_WORKER_GROUP_BY_ID_FAIL.getCode(), (int) result.getCode());
        // correct
        WorkerGroup wg3 = getWorkerGroup(3);
        Mockito.when(workerGroupMapper.selectById(3)).thenReturn(wg3);
        Mockito.when(processInstanceMapper.queryByWorkerGroupNameAndStatus(wg3.getName(), Constants.NOT_TERMINATED_STATES)).thenReturn(new ArrayList<>());
        result = workerGroupService.deleteWorkerGroupById(user, 3);
        Assert.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
    }

    /**
     * get processInstances
     */
    private List<ProcessInstance> getProcessInstanceList() {
        List<ProcessInstance> processInstances = new ArrayList<>();
        processInstances.add(new ProcessInstance());
        return processInstances;
    }

    @Test
    public void testQueryAllGroupWithDefault() {
        Result<List<String>> result = workerGroupService.queryAllGroup();
        List<String> workerGroups = result.getData();
        Assert.assertEquals(1, workerGroups.size());
        Assert.assertEquals("default", workerGroups.toArray()[0]);
    }

    /**
     * get Group
     * @return
     */
    private WorkerGroup getWorkerGroup(int id) {
        WorkerGroup workerGroup = new WorkerGroup();
        workerGroup.setName(groupName);
        workerGroup.setId(id);
        return workerGroup;
    }

    private WorkerGroup getWorkerGroup() {
        return getWorkerGroup(1);
    }

    private List<WorkerGroup> getList() {
        List<WorkerGroup> list = new ArrayList<>();
        list.add(getWorkerGroup());
        return list;
    }

}
