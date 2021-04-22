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

import org.apache.dolphinscheduler.common.enums.DbType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * upgrade dao test
 */
public class UpgradeDaoTest {

    UpgradeDao upgradeDao;

    @Before
    public void before() {
        upgradeDao = PostgresqlUpgradeDao.getInstance();
    }

    @Test
    public void testGetDbType() {
        DbType dbType = UpgradeDao.getDbType();
        Assert.assertEquals(DbType.POSTGRESQL, dbType);
    }

    /**
     * init schema
     */
    @Ignore
    @Test
    public void testInitSchema() {
        String initSqlPath = "/../sql/create/release-1.2.0_schema/postgresql/";
        upgradeDao.initSchema(initSqlPath);
        Assert.assertNotNull(upgradeDao);
    }

    @Test
    public void testGetCurrentVersion() {
        String version = upgradeDao.getCurrentVersion("t_ds_version");
        Assert.assertNotNull(version);
    }

    @Test
    public void testUpgradeDolphinScheduler() {
        String schemaDir = "/../../../sql/upgrade/1.4.0_schema";
        upgradeDao.upgradeDolphinScheduler(schemaDir);
    }

}
