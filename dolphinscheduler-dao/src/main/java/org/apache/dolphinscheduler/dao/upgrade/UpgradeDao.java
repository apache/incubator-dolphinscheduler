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

import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.ConnectionUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.SchemaUtils;
import org.apache.dolphinscheduler.common.utils.ScriptRunner;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.AbstractBaseDao;
import org.apache.dolphinscheduler.dao.datasource.ConnectionFactory;

import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 *  upgrade sql script
 */
public abstract class UpgradeDao extends AbstractBaseDao {

    public static final Logger logger = LoggerFactory.getLogger(UpgradeDao.class);
    private static final String rootDir = System.getProperty("user.dir");
    protected static final DataSource dataSource = getDataSource();
    private static final DbType dbType = getCurrentDbType();

    @Override
    protected void init() {

    }

    /**
     * get datasource
     * @return DruidDataSource
     */
    public static DataSource getDataSource() {
        return ConnectionFactory.getInstance().getDataSource();
    }

    /**
     * get db type
     * @return dbType
     */
    public static DbType getDbType() {
        return dbType;
    }

    /**
     * get current dbType
     * @return
     */
    private static DbType getCurrentDbType() {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            String name = conn.getMetaData().getDatabaseProductName().toUpperCase();
            return DbType.valueOf(name);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return null;
        } finally {
            ConnectionUtils.releaseResource(conn);
        }
    }

    /**
     * init schema
     */
    public void initSchema() {
        DbType dbType = getDbType();
        String initSqlPath = "";
        if (dbType != null) {
            switch (dbType) {
                case MYSQL:
                    initSqlPath = "/sql/create/release-1.0.0_schema/mysql/";
                    initSchema(initSqlPath);
                    break;
                case POSTGRESQL:
                    initSqlPath = "/sql/create/release-1.2.0_schema/postgresql/";
                    initSchema(initSqlPath);
                    break;
                default:
                    logger.error("not support sql type: {},can't upgrade", dbType);
                    throw new IllegalArgumentException("not support sql type,can't upgrade");
            }
        }
    }

    /**
     * init scheam
     *
     * @param initSqlPath initSqlPath
     */
    public void initSchema(String initSqlPath) {

        // Execute the dolphinscheduler DDL, it cannot be rolled back
        runInitDDL(initSqlPath);

        // Execute the dolphinscheduler DML, it can be rolled back
        runInitDML(initSqlPath);

    }

    /**
     * run DML
     *
     * @param initSqlPath initSqlPath
     */
    private void runInitDML(String initSqlPath) {
        this.runSqlScript(initSqlPath + "dolphinscheduler_dml.sql", false, true);
    }

    /**
     * run DDL
     *
     * @param initSqlPath initSqlPath
     */
    private void runInitDDL(String initSqlPath) {
        this.runSqlScript(initSqlPath + "dolphinscheduler_ddl.sql", false, true);
    }

    /**
     *  run sql script
     *
     * @param initSqlFile initSqlFile
     * @param autoCommit autoCommit
     * @param stopOnError stopOnError
     */
    private void runSqlScript(String initSqlFile, boolean autoCommit, boolean stopOnError) {
        if (StringUtils.isEmpty(rootDir)) {
            throw new RuntimeException("Environment variable user.dir not found");
        }
        String mysqlSQLFilePath = rootDir + initSqlFile;

        try (Connection conn = dataSource.getConnection();
             Reader initSqlReader = new FileReader(mysqlSQLFilePath)) {
            this.runSqlScript(initSqlReader, conn, autoCommit, stopOnError);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * run sql script
     *
     * @param initSqlReader Reader
     * @param conn Connection
     * @param autoCommit autoCommit
     * @param stopOnError stopOnError
     */
    private void runSqlScript(Reader initSqlReader, Connection conn, boolean autoCommit, boolean stopOnError) {
        try {
            conn.setAutoCommit(autoCommit);
            // Execute the DolphinScheduler sql script to import related data of DolphinScheduler
            ScriptRunner initScriptRunner = new ScriptRunner(conn, autoCommit, stopOnError);
            initScriptRunner.runScript(initSqlReader);
        } catch (Exception e) {
            ConnectionUtils.rollback(conn);
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * determines whether a table exists
     *
     * @param tableName tableName
     * @return if table exist return true，else return false
     */
    public abstract boolean isExistsTable(String tableName);

    /**
     * determines whether a field exists in the specified table
     *
     * @param tableName  tableName
     * @param columnName columnName
     * @return if column name exist return true，else return false
     */
    public abstract boolean isExistsColumn(String tableName, String columnName);

    /**
     * get current version
     *
     * @param versionName versionName
     * @return version
     */
    public String getCurrentVersion(String versionName) {
        String sql = String.format("select version from %s", versionName);
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        String version = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                version = rs.getString(1);
            }

            return version;

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("sql: " + sql, e);
        } finally {
            ConnectionUtils.releaseResource(rs, pstmt, conn);
        }
    }

    /**
     * upgrade DolphinScheduler
     *
     * @param schemaDir schema dir
     */
    public void upgradeDolphinScheduler(String schemaDir) {

        upgradeDolphinSchedulerDDL(schemaDir);

        upgradeDolphinSchedulerDML(schemaDir);
    }

    /**
     * upgrade DolphinScheduler worker group
     * ds-1.3.0 modify the worker group for process definition json
     */
    public void upgradeDolphinSchedulerWorkerGroup() {
        updateProcessDefinitionJsonWorkerGroup();
    }

    /**
     * upgrade DolphinScheduler resource list
     * ds-1.3.2 modify the resource list for process definition json
     */
    public void upgradeDolphinSchedulerResourceList() {
        updateProcessDefinitionJsonResourceList();
    }

    /**
     * updateProcessDefinitionJsonWorkerGroup
     */
    protected void updateProcessDefinitionJsonWorkerGroup() {
        WorkerGroupDao workerGroupDao = new WorkerGroupDao();
        ProcessDefinitionDao processDefinitionDao = new ProcessDefinitionDao();
        Map<Integer, String> replaceProcessDefinitionMap = new HashMap<>();
        try {
            Map<Integer, String> oldWorkerGroupMap = workerGroupDao.queryAllOldWorkerGroup(dataSource.getConnection());
            Map<Integer, String> processDefinitionJsonMap = processDefinitionDao.queryAllProcessDefinition(dataSource.getConnection());

            for (Map.Entry<Integer, String> entry : processDefinitionJsonMap.entrySet()) {
                ObjectNode jsonObject = JSONUtils.parseObject(entry.getValue());
                ArrayNode tasks = JSONUtils.parseArray(jsonObject.get("tasks").toString());

                for (int i = 0; i < tasks.size(); i++) {
                    ObjectNode task = (ObjectNode) tasks.path(i);
                    ObjectNode workerGroupNode = (ObjectNode) task.path("workerGroupId");
                    Integer workerGroupId = -1;
                    if (workerGroupNode != null && workerGroupNode.canConvertToInt()) {
                        workerGroupId = workerGroupNode.asInt(-1);
                    }
                    if (workerGroupId == -1) {
                        task.put("workerGroup", "default");
                    } else {
                        task.put("workerGroup", oldWorkerGroupMap.get(workerGroupId));
                    }
                }

                jsonObject.remove("task");

                jsonObject.put("tasks", tasks);

                replaceProcessDefinitionMap.put(entry.getKey(), jsonObject.toString());
            }
            if (replaceProcessDefinitionMap.size() > 0) {
                processDefinitionDao.updateProcessDefinitionJson(dataSource.getConnection(), replaceProcessDefinitionMap);
            }
        } catch (Exception e) {
            logger.error("update process definition json workergroup error", e);
        }
    }

    /**
     * updateProcessDefinitionJsonResourceList
     */
    protected void updateProcessDefinitionJsonResourceList() {
        ResourceDao resourceDao = new ResourceDao();
        ProcessDefinitionDao processDefinitionDao = new ProcessDefinitionDao();
        Map<Integer, String> replaceProcessDefinitionMap = new HashMap<>();
        try {
            Map<String, Integer> resourcesMap = resourceDao.listAllResources(dataSource.getConnection());
            Map<Integer, String> processDefinitionJsonMap = processDefinitionDao.queryAllProcessDefinition(dataSource.getConnection());

            for (Map.Entry<Integer, String> entry : processDefinitionJsonMap.entrySet()) {
                ObjectNode jsonObject = JSONUtils.parseObject(entry.getValue());
                ArrayNode tasks = JSONUtils.parseArray(jsonObject.get("tasks").toString());

                for (int i = 0; i < tasks.size(); i++) {
                    ObjectNode task = (ObjectNode) tasks.get(i);
                    ObjectNode param = (ObjectNode) task.get("params");
                    if (param != null) {

                        List<ResourceInfo> resourceList = JSONUtils.toList(param.get("resourceList").toString(), ResourceInfo.class);
                        ResourceInfo mainJar = JSONUtils.parseObject(param.get("mainJar").toString(), ResourceInfo.class);
                        if (mainJar != null && mainJar.getId() == 0) {
                            String fullName = mainJar.getRes().startsWith("/") ? mainJar.getRes() : String.format("/%s", mainJar.getRes());
                            if (resourcesMap.containsKey(fullName)) {
                                mainJar.setId(resourcesMap.get(fullName));
                                param.put("mainJar", JSONUtils.parseObject(JSONUtils.toJsonString(mainJar)));
                            }
                        }

                        if (CollectionUtils.isNotEmpty(resourceList)) {
                            List<ResourceInfo> newResourceList = resourceList.stream().map(resInfo -> {
                                String fullName = resInfo.getRes().startsWith("/") ? resInfo.getRes() : String.format("/%s", resInfo.getRes());
                                if (resInfo.getId() == 0 && resourcesMap.containsKey(fullName)) {
                                    resInfo.setId(resourcesMap.get(fullName));
                                }
                                return resInfo;
                            }).collect(Collectors.toList());
                            param.put("resourceList", JSONUtils.parseObject(JSONUtils.toJsonString(newResourceList)));
                        }
                    }
                    task.put("params", param);

                }

                jsonObject.remove("tasks");

                jsonObject.put("tasks", tasks);

                replaceProcessDefinitionMap.put(entry.getKey(), jsonObject.toString());
            }
            if (replaceProcessDefinitionMap.size() > 0) {
                processDefinitionDao.updateProcessDefinitionJson(dataSource.getConnection(), replaceProcessDefinitionMap);
            }
        } catch (Exception e) {
            logger.error("update process definition json resource list error", e);
        }

    }

    /**
     * upgradeDolphinScheduler DML
     *
     * @param schemaDir schemaDir
     */
    private void upgradeDolphinSchedulerDML(String schemaDir) {
        String schemaVersion = schemaDir.split("_")[0];
        if (StringUtils.isEmpty(rootDir)) {
            throw new RuntimeException("Environment variable user.dir not found");
        }
        String sqlFilePath = MessageFormat.format("{0}/sql/upgrade/{1}/{2}/dolphinscheduler_dml.sql", rootDir, schemaDir, getDbType().name().toLowerCase());
        logger.info("sqlSQLFilePath" + sqlFilePath);

        try (Connection conn = dataSource.getConnection();
             Reader sqlReader = new FileReader((sqlFilePath))) {
            try {
                conn.setAutoCommit(false);
                // Execute the upgraded dolphinscheduler dml
                ScriptRunner scriptRunner = new ScriptRunner(conn, false, true);
                scriptRunner.runScript(sqlReader);
                String versionTable;
                if (isExistsTable(TABLE_VERSION_V1)) {
                    versionTable = TABLE_VERSION_V1;
                } else if (isExistsTable(TABLE_VERSION_V2)) {
                    versionTable = TABLE_VERSION_V2;
                } else {
                    return;
                }

                // Change version in the version table to the new version
                String upgradeSQL = String.format("update %s set version = ?", versionTable);
                try (PreparedStatement pstmt = conn.prepareStatement(upgradeSQL)) {
                    pstmt.setString(1, schemaVersion);
                    pstmt.executeUpdate();
                }
                conn.commit();
            } catch (Exception e) {
                ConnectionUtils.rollback(conn);
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e.getMessage(), e);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * upgradeDolphinScheduler DDL
     *
     * @param schemaDir schemaDir
     */
    private void upgradeDolphinSchedulerDDL(String schemaDir) {
        String sqlFilePath = MessageFormat.format("/sql/upgrade/{0}/{1}/dolphinscheduler_ddl.sql", schemaDir, getDbType().name().toLowerCase());
        this.runSqlScript(sqlFilePath, true, true);
    }

    /**
     * update version
     *
     * @param version version
     */
    public void updateVersion(String version) {
        // Change version in the version table to the new version
        String versionName = TABLE_VERSION_V1;
        if (!SchemaUtils.isAGreatVersion("1.2.0", version)) {
            versionName = TABLE_VERSION_V2;
        }
        String upgradeSQL = String.format("update %s set version = ?", versionName);
        PreparedStatement pstmt = null;
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(upgradeSQL);
            pstmt.setString(1, version);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("sql: " + upgradeSQL, e);
        } finally {
            ConnectionUtils.releaseResource(pstmt, conn);
        }

    }

}
