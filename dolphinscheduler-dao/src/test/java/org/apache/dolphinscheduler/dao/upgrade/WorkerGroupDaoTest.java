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
package org.apache.dolphinscheduler.dao.upgrade;

import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.dolphinscheduler.dao.upgrade.UpgradeDao.getDataSource;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

public class WorkerGroupDaoTest {
    protected  final DataSource dataSource = getDataSource();

    @Test
    public void testQueryQueryAllOldWorkerGroup() throws Exception{
        WorkerGroupDao workerGroupDao = new WorkerGroupDao();

        Map<Integer, String> workerGroupMap = workerGroupDao.queryAllOldWorkerGroup(dataSource.getConnection());

        assertThat(workerGroupMap.size(),greaterThanOrEqualTo(0));
    }

    @Test(expected = Exception.class)
    public void testQueryQueryAllOldWorkerGroupException() throws Exception{
        WorkerGroupDao workerGroupDao = new WorkerGroupDao();

        workerGroupDao.queryAllOldWorkerGroup(null);

    }

}
