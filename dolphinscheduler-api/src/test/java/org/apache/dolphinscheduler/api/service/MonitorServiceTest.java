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
import org.apache.dolphinscheduler.api.service.impl.MonitorServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.MonitorDBDao;
import org.apache.dolphinscheduler.dao.entity.MonitorRecord;

import java.util.ArrayList;
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

/**
 * monitor service test
 */
@RunWith(MockitoJUnitRunner.class)
public class MonitorServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(MonitorServiceTest.class);

    @InjectMocks
    private MonitorServiceImpl monitorService;

    @Mock
    private MonitorDBDao monitorDBDao;

    @Test
    public  void testQueryDatabaseState(){

        Mockito.when(monitorDBDao.queryDatabaseState()).thenReturn(getList());
        Result<List<MonitorRecord>> result = monitorService.queryDatabaseState(null);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
        List<MonitorRecord> monitorRecordList = result.getData();
        Assert.assertTrue(CollectionUtils.isNotEmpty(monitorRecordList));
    }
    @Test
    public  void testQueryMaster(){
        //TODO need zk
//        Map<String,Object> result = monitorService.queryMaster(null);
//        logger.info(result.toString());
//        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
    }
    @Test
    public  void testQueryZookeeperState(){
        //TODO need zk
//        Map<String,Object> result = monitorService.queryZookeeperState(null);
//        logger.info(result.toString());
//        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
    }

    @Test
    public  void testGetServerListFromZK(){
        //TODO need zk
//        List<Server> serverList = monitorService.getServerListFromZK(true);
//        logger.info(serverList.toString());
    }

    private List<MonitorRecord> getList(){
        List<MonitorRecord> monitorRecordList = new ArrayList<>();
        monitorRecordList.add(getEntity());
        return monitorRecordList;
    }

    private MonitorRecord getEntity(){
        MonitorRecord monitorRecord = new  MonitorRecord();
        monitorRecord.setDbType(DbType.MYSQL);
        return monitorRecord;
    }

    private List<Server> getServerList(){
        List<Server> servers = new ArrayList<>();
        servers.add(new Server());
        return servers;
    }

}
