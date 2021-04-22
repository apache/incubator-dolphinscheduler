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

import static org.apache.dolphinscheduler.dao.utils.Constants.TABLE_VERSION_V1;
import static org.apache.dolphinscheduler.dao.utils.Constants.TABLE_VERSION_V2;
import static org.apache.dolphinscheduler.dao.utils.Constants.TABLE_QUEUE_V1;

import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.SchemaUtils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * upgrade manager
 */
public class DolphinSchedulerManager {
    private static final Logger logger = LoggerFactory.getLogger(DolphinSchedulerManager.class);
    UpgradeDao upgradeDao;

    /**
     * init upgrade dao
     */
    private void initUpgradeDao() {
        DbType dbType = UpgradeDao.getDbType();
        if (dbType != null) {
            switch (dbType) {
                case MYSQL:
                    upgradeDao = MysqlUpgradeDao.getInstance();
                    break;
                case POSTGRESQL:
                    upgradeDao = PostgresqlUpgradeDao.getInstance();
                    break;
                default:
                    logger.error("not support sql type: {},can't upgrade", dbType);
                    throw new IllegalArgumentException("not support sql type,can't upgrade");
            }
        }
    }

    /**
     * constructor init
     */
    public DolphinSchedulerManager() {
        initUpgradeDao();
    }

    /**
     * init DolphinScheduler
     */
    public void initDolphinScheduler() {
        // Determines whether the dolphinscheduler table structure has been init
        if (upgradeDao.isExistsTable(TABLE_VERSION_V1)
                || upgradeDao.isExistsTable(TABLE_VERSION_V2)
                ||  upgradeDao.isExistsTable(TABLE_QUEUE_V1)) {
            logger.info("The database has been initialized. Skip the initialization step");
            return;
        }
        this.initDolphinSchedulerSchema();
    }

    /**
     * init DolphinScheduler Schema
     */
    public void initDolphinSchedulerSchema() {

        logger.info("Start initializing the DolphinScheduler manager table structure");
        upgradeDao.initSchema();
    }

    /**
     * upgrade DolphinScheduler
     * @throws Exception if error throws Exception
     */
    public void upgradeDolphinScheduler() throws Exception {

        // Gets a list of all upgrades
        List<String> schemaList = SchemaUtils.getAllSchemaList();
        if (schemaList == null || schemaList.size() == 0) {
            logger.info("There is no schema to upgrade!");
        } else {

            String version = "";
            // Gets the version of the current system
            if (upgradeDao.isExistsTable(TABLE_VERSION_V1)) {
                version = upgradeDao.getCurrentVersion(TABLE_VERSION_V1);
            } else if (upgradeDao.isExistsTable(TABLE_VERSION_V2)) {
                version = upgradeDao.getCurrentVersion(TABLE_VERSION_V2);
            } else if (upgradeDao.isExistsColumn(TABLE_QUEUE_V1,"create_time")) {
                version = "1.0.1";
            } else if (upgradeDao.isExistsTable(TABLE_QUEUE_V1)) {
                version = "1.0.0";
            } else {
                logger.error("Unable to determine current software version, so cannot upgrade");
                throw new RuntimeException("Unable to determine current software version, so cannot upgrade");
            }
            // The target version of the upgrade
            String schemaVersion = "";
            for (String schemaDir : schemaList) {
                schemaVersion = schemaDir.split("_")[0];
                if (SchemaUtils.isAGreatVersion(schemaVersion, version)) {
                    logger.info("upgrade DolphinScheduler metadata version from {} to {}", version, schemaVersion);
                    logger.info("Begin upgrading DolphinScheduler's table structure");
                    upgradeDao.upgradeDolphinScheduler(schemaDir);
                    if ("1.3.0".equals(schemaVersion)) {
                        upgradeDao.upgradeDolphinSchedulerWorkerGroup();
                    } else if ("1.3.2".equals(schemaVersion)) {
                        upgradeDao.upgradeDolphinSchedulerResourceList();
                    }
                    version = schemaVersion;
                }

            }
        }

        // Assign the value of the version field in the version table to the version of the product
        upgradeDao.updateVersion(SchemaUtils.getSoftVersion());
    }
}
