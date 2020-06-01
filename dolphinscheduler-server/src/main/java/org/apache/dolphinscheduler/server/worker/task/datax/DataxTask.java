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
package org.apache.dolphinscheduler.server.worker.task.datax;


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.datax.DataxParameters;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.datasource.BaseDataSource;
import org.apache.dolphinscheduler.dao.datasource.DataSourceFactory;
import org.apache.dolphinscheduler.server.entity.DataxTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.DataxUtils;
import org.apache.dolphinscheduler.server.utils.ParamUtils;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.apache.dolphinscheduler.server.worker.task.CommandExecuteResult;
import org.apache.dolphinscheduler.server.worker.task.ShellCommandExecutor;
import org.slf4j.Logger;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.parser.SQLStatementParser;


/**
 * DataX task
 */
public class DataxTask extends AbstractTask {

    /**
     * python process(datax only supports version 2.7 by default)
     */
    private static final String DATAX_PYTHON = "python2.7";

    /**
     * datax home path
     */
    private static final String DATAX_HOME_EVN = "${DATAX_HOME}";

    /**
     * datax channel count
     */
    private static final int DATAX_CHANNEL_COUNT = 1;

    /**
     * datax parameters
     */
    private DataxParameters dataXParameters;

    /**
     * shell command executor
     */
    private ShellCommandExecutor shellCommandExecutor;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    /**
     * constructor
     * @param taskExecutionContext taskExecutionContext
     * @param logger logger
     */
    public DataxTask(TaskExecutionContext taskExecutionContext, Logger logger) {
        super(taskExecutionContext, logger);
        this.taskExecutionContext = taskExecutionContext;


        this.shellCommandExecutor = new ShellCommandExecutor(this::logHandle,
                taskExecutionContext,logger);
    }

    /**
     * init DataX config
     */
    @Override
    public void init() {
        logger.info("datax task params {}", taskExecutionContext.getTaskParams());
        dataXParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), DataxParameters.class);

        if (!dataXParameters.checkParameters()) {
            throw new RuntimeException("datax task params is not valid");
        }
    }

    /**
     * run DataX process
     *
     * @throws Exception if error throws Exception
     */
    @Override
    public void handle() throws Exception {
        try {
            // set the name of the current thread
            String threadLoggerInfoName = String.format("TaskLogInfo-%s", taskExecutionContext.getTaskAppId());
            Thread.currentThread().setName(threadLoggerInfoName);

            // run datax process
            String jsonFilePath = buildDataxJsonFile();
            String shellCommandFilePath = buildShellCommandFile(jsonFilePath);
            CommandExecuteResult commandExecuteResult = shellCommandExecutor.run(shellCommandFilePath);

            setExitStatusCode(commandExecuteResult.getExitStatusCode());
            setAppIds(commandExecuteResult.getAppIds());
            setProcessId(commandExecuteResult.getProcessId());
        }
        catch (Exception e) {
            logger.error("datax task failure", e);
            setExitStatusCode(Constants.EXIT_CODE_FAILURE);
            throw e;
        }
    }

    /**
     * cancel DataX process
     *
     * @param cancelApplication cancelApplication
     * @throws Exception if error throws Exception
     */
    @Override
    public void cancelApplication(boolean cancelApplication)
        throws Exception {
        // cancel process
        shellCommandExecutor.cancelApplication();
    }

    /**
     * build datax configuration file
     *
     * @return datax json file name
     * @throws Exception if error throws Exception
     */
    private String buildDataxJsonFile()
        throws Exception {
        // generate json
        String fileName = String.format("%s/%s_job.json",
                taskExecutionContext.getExecutePath(),
                taskExecutionContext.getTaskAppId());
        String json;

        Path path = new File(fileName).toPath();
        if (Files.exists(path)) {
            return fileName;
        }



        if (dataXParameters.getCustomConfig() == 1){

            json = dataXParameters.getJson().replaceAll("\\r\\n", "\n");

            /**
             *  combining local and global parameters
             */
            Map<String, Property> paramsMap = ParamUtils.convert(ParamUtils.getUserDefParamsMap(taskExecutionContext.getDefinedParams()),
                    taskExecutionContext.getDefinedParams(),
                    dataXParameters.getLocalParametersMap(),
                    CommandType.of(taskExecutionContext.getCmdTypeIfComplement()),
                    taskExecutionContext.getScheduleTime());
            if (paramsMap != null){
                json = ParameterUtils.convertParameterPlaceholders(json, ParamUtils.convert(paramsMap));
            }

        }else {

            ObjectNode job = JSONUtils.createObjectNode();
            job.put("content", JSONUtils.toJsonNode(buildDataxJobContentJson()));
            job.put("setting", JSONUtils.toJsonNode(buildDataxJobSettingJson()));

            ObjectNode root = JSONUtils.createObjectNode();
            root.set("job", job);
            root.set("core", JSONUtils.toJsonNode(buildDataxCoreJson()));
            json = root.toString();
        }

        logger.debug("datax job json : {}", json);

        // create datax json file
        FileUtils.writeStringToFile(new File(fileName), json, StandardCharsets.UTF_8);
        return fileName;
    }

    /**
     * build datax job config
     *
     * @return collection of datax job config JSONObject
     * @throws SQLException if error throws SQLException
     */
    private List<ObjectNode> buildDataxJobContentJson() throws SQLException {
        DataxTaskExecutionContext dataxTaskExecutionContext = taskExecutionContext.getDataxTaskExecutionContext();


        BaseDataSource dataSourceCfg = DataSourceFactory.getDatasource(DbType.of(dataxTaskExecutionContext.getSourcetype()),
                dataxTaskExecutionContext.getSourceConnectionParams());

        BaseDataSource dataTargetCfg = DataSourceFactory.getDatasource(DbType.of(dataxTaskExecutionContext.getTargetType()),
                dataxTaskExecutionContext.getTargetConnectionParams());

        List<ObjectNode> readerConnArr = new ArrayList<>();
        ObjectNode readerConn = JSONUtils.createObjectNode();
        readerConn.set("querySql", JSONUtils.toJsonNode(new String[] {dataXParameters.getSql()}));
        readerConn.set("jdbcUrl", JSONUtils.toJsonNode(new String[] {dataSourceCfg.getJdbcUrl()}));
        readerConnArr.add(readerConn);

        ObjectNode readerParam = JSONUtils.createObjectNode();
        readerParam.put("username", dataSourceCfg.getUser());
        readerParam.put("password", dataSourceCfg.getPassword());
        readerParam.set("connection", JSONUtils.toJsonNode(readerConnArr));

        ObjectNode reader = JSONUtils.createObjectNode();
        reader.put("name", DataxUtils.getReaderPluginName(DbType.of(dataxTaskExecutionContext.getSourcetype())));
        reader.set("parameter", readerParam);

        List<ObjectNode> writerConnArr = new ArrayList<>();
        ObjectNode writerConn = JSONUtils.createObjectNode();
        writerConn.set("table", JSONUtils.toJsonNode(new String[] {dataXParameters.getTargetTable()}));
        writerConn.put("jdbcUrl", dataTargetCfg.getJdbcUrl());
        writerConnArr.add(writerConn);

        ObjectNode writerParam = JSONUtils.createObjectNode();
        writerParam.put("username", dataTargetCfg.getUser());
        writerParam.put("password", dataTargetCfg.getPassword());
        writerParam.set("column",
                JSONUtils.toJsonNode(parsingSqlColumnNames(DbType.of(dataxTaskExecutionContext.getSourcetype()),
                    DbType.of(dataxTaskExecutionContext.getTargetType()),
                    dataSourceCfg, dataXParameters.getSql())));
        writerParam.set("connection", JSONUtils.toJsonNode(writerConnArr));

        if (CollectionUtils.isNotEmpty(dataXParameters.getPreStatements())) {
            writerParam.set("preSql", JSONUtils.toJsonNode(dataXParameters.getPreStatements()));
        }

        if (CollectionUtils.isNotEmpty(dataXParameters.getPostStatements())) {
            writerParam.set("postSql", JSONUtils.toJsonNode(dataXParameters.getPostStatements()));
        }

        ObjectNode writer = JSONUtils.createObjectNode();
        writer.put("name", DataxUtils.getWriterPluginName(DbType.of(dataxTaskExecutionContext.getTargetType())));
        writer.put("parameter", writerParam);

        List<ObjectNode> contentList = new ArrayList<>();
        ObjectNode content = JSONUtils.createObjectNode();
        content.put("reader", reader);
        content.put("writer", writer);
        contentList.add(content);

        return contentList;
    }

    /**
     * build datax setting config
     *
     * @return datax setting config JSONObject
     */
    private ObjectNode buildDataxJobSettingJson() {
        ObjectNode speed = JSONUtils.createObjectNode();
        speed.put("channel", DATAX_CHANNEL_COUNT);

        if (dataXParameters.getJobSpeedByte() > 0) {
            speed.put("byte", dataXParameters.getJobSpeedByte());
        }

        if (dataXParameters.getJobSpeedRecord() > 0) {
            speed.put("record", dataXParameters.getJobSpeedRecord());
        }

        ObjectNode errorLimit = JSONUtils.createObjectNode();
        errorLimit.put("record", 0);
        errorLimit.put("percentage", 0);

        ObjectNode setting = JSONUtils.createObjectNode();
        setting.put("speed", speed);
        setting.put("errorLimit", errorLimit);

        return setting;
    }

    private ObjectNode buildDataxCoreJson() {
        ObjectNode speed = JSONUtils.createObjectNode();
        speed.put("channel", DATAX_CHANNEL_COUNT);

        if (dataXParameters.getJobSpeedByte() > 0) {
            speed.put("byte", dataXParameters.getJobSpeedByte());
        }

        if (dataXParameters.getJobSpeedRecord() > 0) {
            speed.put("record", dataXParameters.getJobSpeedRecord());
        }

        ObjectNode channel = JSONUtils.createObjectNode();
        channel.set("speed", speed);

        ObjectNode transport = JSONUtils.createObjectNode();
        transport.set("channel", channel);

        ObjectNode core = JSONUtils.createObjectNode();
        core.set("transport", transport);

        return core;
    }

    /**
     * create command
     *
     * @return shell command file name
     * @throws Exception if error throws Exception
     */
    private String buildShellCommandFile(String jobConfigFilePath)
        throws Exception {
        // generate scripts
        String fileName = String.format("%s/%s_node.sh",
                taskExecutionContext.getExecutePath(),
                taskExecutionContext.getTaskAppId());

        Path path = new File(fileName).toPath();

        if (Files.exists(path)) {
            return fileName;
        }

        // datax python command
        StringBuilder sbr = new StringBuilder();
        sbr.append(DATAX_PYTHON);
        sbr.append(" ");
        sbr.append(DATAX_HOME_EVN);
        sbr.append(" ");
        sbr.append(jobConfigFilePath);
        String dataxCommand = sbr.toString();

        // combining local and global parameters
        // replace placeholder
        Map<String, Property> paramsMap = ParamUtils.convert(ParamUtils.getUserDefParamsMap(taskExecutionContext.getDefinedParams()),
                taskExecutionContext.getDefinedParams(),
                dataXParameters.getLocalParametersMap(),
                CommandType.of(taskExecutionContext.getCmdTypeIfComplement()),
                taskExecutionContext.getScheduleTime());
        if (paramsMap != null) {
            dataxCommand = ParameterUtils.convertParameterPlaceholders(dataxCommand, ParamUtils.convert(paramsMap));
        }

        logger.debug("raw script : {}", dataxCommand);

        // create shell command file
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString(Constants.RWXR_XR_X);
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);

        if (OSUtils.isWindows()) {
            Files.createFile(path);
        } else {
            Files.createFile(path, attr);
        }

        Files.write(path, dataxCommand.getBytes(), StandardOpenOption.APPEND);

        return fileName;
    }

    /**
     * parsing synchronized column names in SQL statements
     *
     * @param dsType
     *            the database type of the data source
     * @param dtType
     *            the database type of the data target
     * @param dataSourceCfg
     *            the database connection parameters of the data source
     * @param sql
     *            sql for data synchronization
     * @return Keyword converted column names
     */
    private String[] parsingSqlColumnNames(DbType dsType, DbType dtType, BaseDataSource dataSourceCfg, String sql) {
        String[] columnNames = tryGrammaticalAnalysisSqlColumnNames(dsType, sql);

        if (columnNames == null || columnNames.length == 0) {
            logger.info("try to execute sql analysis query column name");
            columnNames = tryExecuteSqlResolveColumnNames(dataSourceCfg, sql);
        }

        notNull(columnNames, String.format("parsing sql columns failed : %s", sql));

        return DataxUtils.convertKeywordsColumns(dtType, columnNames);
    }

    /**
     * try grammatical parsing column
     *
     * @param dbType
     *            database type
     * @param sql
     *            sql for data synchronization
     * @return column name array
     * @throws RuntimeException if error throws RuntimeException
     */
    private String[] tryGrammaticalAnalysisSqlColumnNames(DbType dbType, String sql) {
        String[] columnNames;

        try {
            SQLStatementParser parser = DataxUtils.getSqlStatementParser(dbType, sql);
            notNull(parser, String.format("database driver [%s] is not support", dbType.toString()));

            SQLStatement sqlStatement = parser.parseStatement();
            SQLSelectStatement sqlSelectStatement = (SQLSelectStatement)sqlStatement;
            SQLSelect sqlSelect = sqlSelectStatement.getSelect();

            List<SQLSelectItem> selectItemList = null;
            if (sqlSelect.getQuery() instanceof SQLSelectQueryBlock) {
                SQLSelectQueryBlock block = (SQLSelectQueryBlock)sqlSelect.getQuery();
                selectItemList = block.getSelectList();
            } else if (sqlSelect.getQuery() instanceof SQLUnionQuery) {
                SQLUnionQuery unionQuery = (SQLUnionQuery)sqlSelect.getQuery();
                SQLSelectQueryBlock block = (SQLSelectQueryBlock)unionQuery.getRight();
                selectItemList = block.getSelectList();
            }

            notNull(selectItemList,
                String.format("select query type [%s] is not support", sqlSelect.getQuery().toString()));

            columnNames = new String[selectItemList.size()];
            for (int i = 0; i < selectItemList.size(); i++ ) {
                SQLSelectItem item = selectItemList.get(i);

                String columnName = null;

                if (item.getAlias() != null) {
                    columnName = item.getAlias();
                } else if (item.getExpr() != null) {
                    if (item.getExpr() instanceof SQLPropertyExpr) {
                        SQLPropertyExpr expr = (SQLPropertyExpr)item.getExpr();
                        columnName = expr.getName();
                    } else if (item.getExpr() instanceof SQLIdentifierExpr) {
                        SQLIdentifierExpr expr = (SQLIdentifierExpr)item.getExpr();
                        columnName = expr.getName();
                    }
                } else {
                    throw new RuntimeException(
                        String.format("grammatical analysis sql column [ %s ] failed", item.toString()));
                }

                if (columnName == null) {
                    throw new RuntimeException(
                        String.format("grammatical analysis sql column [ %s ] failed", item.toString()));
                }

                columnNames[i] = columnName;
            }
        }
        catch (Exception e) {
            logger.warn(e.getMessage(), e);
            return null;
        }

        return columnNames;
    }

    /**
     * try to execute sql to resolve column names
     *
     * @param baseDataSource
     *            the database connection parameters
     * @param sql
     *            sql for data synchronization
     * @return column name array
     */
    public String[] tryExecuteSqlResolveColumnNames(BaseDataSource baseDataSource, String sql) {
        String[] columnNames;
        sql = String.format("SELECT t.* FROM ( %s ) t WHERE 0 = 1", sql);
        sql = sql.replace(";", "");

        try (
            Connection connection = DriverManager.getConnection(baseDataSource.getJdbcUrl(), baseDataSource.getUser(),
                baseDataSource.getPassword());
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet resultSet = stmt.executeQuery()) {

            ResultSetMetaData md = resultSet.getMetaData();
            int num = md.getColumnCount();
            columnNames = new String[num];
            for (int i = 1; i <= num; i++ ) {
                columnNames[i - 1] = md.getColumnName(i);
            }
        }
        catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            return null;
        }

        return columnNames;
    }

    @Override
    public AbstractParameters getParameters() {
        return dataXParameters;
    }

    private void notNull(Object obj, String message) {
        if (obj == null) {
            throw new RuntimeException(message);
        }
    }

}
