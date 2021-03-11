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
package org.apache.dolphinscheduler.server.worker.task.processdure;

import com.cronutils.utils.StringUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.*;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.procedure.ProcedureParameters;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.*;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.datasource.BaseDataSource;
import org.apache.dolphinscheduler.dao.datasource.DataSourceFactory;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.ParamUtils;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.slf4j.Logger;

import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.apache.dolphinscheduler.common.enums.DataType.*;

/**
 *  procedure task
 */
public class ProcedureTask extends AbstractTask {

    /**
     * procedure parameters
     */
    private ProcedureParameters procedureParameters;

    /**
     * base datasource
     */
    private BaseDataSource baseDataSource;


    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    /**
     * constructor
     * @param taskExecutionContext taskExecutionContext
     * @param logger    logger
     */
    public ProcedureTask(TaskExecutionContext taskExecutionContext, Logger logger) {
        super(taskExecutionContext, logger);

        this.taskExecutionContext = taskExecutionContext;

        logger.info("procedure task params {}", taskExecutionContext.getTaskParams());

        this.procedureParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), ProcedureParameters.class);


        // check parameters
        if (!procedureParameters.checkParameters()) {
            throw new RuntimeException("procedure task params is not valid");
        }
    }

    @Override
    public void handle() throws Exception {
        // set the name of the current thread
        String threadLoggerInfoName = String.format(Constants.TASK_LOG_INFO_FORMAT, taskExecutionContext.getTaskAppId());
        Thread.currentThread().setName(threadLoggerInfoName);

        logger.info("procedure type : {}, datasource : {}, method : {} , localParams : {}",
                procedureParameters.getType(),
                procedureParameters.getDatasource(),
                procedureParameters.getMethod(),
                procedureParameters.getLocalParams());

        Connection connection = null;
        CallableStatement stmt = null;
        try {
            // load class
            DataSourceFactory.loadClass(DbType.valueOf(procedureParameters.getType()));

            // get datasource
            baseDataSource = DataSourceFactory.getDatasource(DbType.valueOf(procedureParameters.getType()),
                    taskExecutionContext.getProcedureTaskExecutionContext().getConnectionParams());


            // get jdbc connection
            connection = DriverManager.getConnection(baseDataSource.getJdbcUrl(),
                    baseDataSource.getUser(),
                    baseDataSource.getPassword());



            // combining local and global parameters
            Map<String, Property> paramsMap = ParamUtils.convert(ParamUtils.getUserDefParamsMap(taskExecutionContext.getDefinedParams()),
                    taskExecutionContext.getDefinedParams(),
                    procedureParameters.getLocalParametersMap(),
                    CommandType.of(taskExecutionContext.getCmdTypeIfComplement()),
                    taskExecutionContext.getScheduleTime());


            Collection<Property> userDefParamsList = null;

            if (procedureParameters.getLocalParametersMap() != null){
                userDefParamsList = procedureParameters.getLocalParametersMap().values();
            }

            String method = getCallMethod(userDefParamsList);

            logger.info("call method : {}",method);

            // call method
            stmt = connection.prepareCall(method);

            // set timeout
            setTimeout(stmt);

            // outParameterMap
            Map<Integer, Property> outParameterMap = getOutParameterMap(stmt, paramsMap, userDefParamsList);


            stmt.executeUpdate();

            /**
             *  print the output parameters to the log
             */
            printOutParameter(stmt, outParameterMap);

            setExitStatusCode(Constants.EXIT_CODE_SUCCESS);
        }catch (Exception e){
            setExitStatusCode(Constants.EXIT_CODE_FAILURE);
            logger.error("procedure task error",e);
            throw e;
        }
        finally {
            close(stmt,connection);
        }
    }

    /**
     * get call method
     * @param userDefParamsList userDefParamsList
     * @return method
     */
    private String getCallMethod(Collection<Property> userDefParamsList) {
        String method;// no parameters
        if (CollectionUtils.isEmpty(userDefParamsList)){
            method = "{call " + procedureParameters.getMethod() + "}";
        }else { // exists parameters
            int size = userDefParamsList.size();
            StringBuilder parameter = new StringBuilder();
            parameter.append("(");
            for (int i = 0 ;i < size - 1; i++){
                parameter.append("?,");
            }
            parameter.append("?)");
            method = "{call " + procedureParameters.getMethod() + parameter.toString()+ "}";
        }
        return method;
    }

    /**
     * print outParameter
     * @param stmt CallableStatement
     * @param outParameterMap outParameterMap
     * @throws SQLException
     */
    private void printOutParameter(CallableStatement stmt,
                                   Map<Integer, Property> outParameterMap) throws SQLException {
        Iterator<Map.Entry<Integer, Property>> iter = outParameterMap.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry<Integer, Property> en = iter.next();

            int index = en.getKey();
            Property property = en.getValue();
            String prop = property.getProp();
            DataType dataType = property.getType();
            // get output parameter
            getOutputParameter(stmt, index, prop, dataType);
        }
    }

    /**
     * get output parameter
     *
     * @param stmt CallableStatement
     * @param paramsMap paramsMap
     * @param userDefParamsList userDefParamsList
     * @return outParameterMap
     * @throws Exception
     */
    private Map<Integer, Property> getOutParameterMap(CallableStatement stmt,
                                                      Map<String, Property> paramsMap,
                                                      Collection<Property> userDefParamsList) throws Exception {
        Map<Integer,Property> outParameterMap = new HashMap<>();
        if (userDefParamsList != null && userDefParamsList.size() > 0){
            int index = 1;
            for (Property property : userDefParamsList){
                logger.info("localParams : prop : {} , dirct : {} , type : {} , value : {}"
                        ,property.getProp(),
                        property.getDirect(),
                        property.getType(),
                        property.getValue());
                // set parameters
                if (property.getDirect().equals(Direct.IN)){
                    ParameterUtils.setInParameter(index, stmt, property.getType(), paramsMap.get(property.getProp()).getValue());
                }else if (property.getDirect().equals(Direct.OUT)){
                    setOutParameter(index,stmt,property.getType(),paramsMap.get(property.getProp()).getValue());
                    property.setValue(paramsMap.get(property.getProp()).getValue());
                    outParameterMap.put(index,property);
                }
                index++;
            }
        }
        return outParameterMap;
    }

    /**
     * set timeout
     * @param stmt CallableStatement
     * @throws SQLException
     */
    private void setTimeout(CallableStatement stmt) throws SQLException {
        Boolean failed = TaskTimeoutStrategy.of(taskExecutionContext.getTaskTimeoutStrategy()) == TaskTimeoutStrategy.FAILED;
        Boolean warnfailed = TaskTimeoutStrategy.of(taskExecutionContext.getTaskTimeoutStrategy()) == TaskTimeoutStrategy.WARNFAILED;
        if(failed || warnfailed){
            stmt.setQueryTimeout(taskExecutionContext.getTaskTimeout());
        }
    }

    /**
     * close jdbc resource
     *
     * @param stmt
     * @param connection
     */
    private void close(PreparedStatement stmt,
                       Connection connection){
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {

            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {

            }
        }
    }

    /**
     * get output parameter
     * @param stmt
     * @param index
     * @param prop
     * @param dataType
     * @throws SQLException
     */
    private void getOutputParameter(CallableStatement stmt, int index, String prop, DataType dataType) throws SQLException {
        switch (dataType){
            case VARCHAR:
                logger.info("out parameter varchar key : {} , value : {}",prop,stmt.getString(index));
                break;
            case INTEGER:
                logger.info("out parameter integer key : {} , value : {}", prop, stmt.getInt(index));
                break;
            case LONG:
                logger.info("out parameter long key : {} , value : {}",prop,stmt.getLong(index));
                break;
            case FLOAT:
                logger.info("out parameter float key : {} , value : {}",prop,stmt.getFloat(index));
                break;
            case DOUBLE:
                logger.info("out parameter double key : {} , value : {}",prop,stmt.getDouble(index));
                break;
            case DATE:
                logger.info("out parameter date key : {} , value : {}",prop,stmt.getDate(index));
                break;
            case TIME:
                logger.info("out parameter time key : {} , value : {}",prop,stmt.getTime(index));
                break;
            case TIMESTAMP:
                logger.info("out parameter timestamp key : {} , value : {}",prop,stmt.getTimestamp(index));
                break;
            case BOOLEAN:
                logger.info("out parameter boolean key : {} , value : {}",prop, stmt.getBoolean(index));
                break;
            default:
                break;
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return procedureParameters;
    }

    /**
     * set out parameter
     * @param index     index
     * @param stmt      stmt
     * @param dataType  dataType
     * @param value     value
     * @throws Exception exception
     */
    private void setOutParameter(int index,CallableStatement stmt,DataType dataType,String value)throws Exception{
        if (dataType.equals(VARCHAR)){
            if (StringUtils.isEmpty(value)){
                stmt.registerOutParameter(index, Types.VARCHAR);
            }else {
                stmt.registerOutParameter(index, Types.VARCHAR, value);
            }

        }else if (dataType.equals(INTEGER)){
            if (StringUtils.isEmpty(value)){
                stmt.registerOutParameter(index, Types.INTEGER);
            }else {
                stmt.registerOutParameter(index, Types.INTEGER, value);
            }

        }else if (dataType.equals(LONG)){
            if (StringUtils.isEmpty(value)){
                stmt.registerOutParameter(index,Types.INTEGER);
            }else {
                stmt.registerOutParameter(index,Types.INTEGER ,value);
            }
        }else if (dataType.equals(FLOAT)){
            if (StringUtils.isEmpty(value)){
                stmt.registerOutParameter(index, Types.FLOAT);
            }else {
                stmt.registerOutParameter(index, Types.FLOAT,value);
            }
        }else if (dataType.equals(DOUBLE)){
            if (StringUtils.isEmpty(value)){
                stmt.registerOutParameter(index, Types.DOUBLE);
            }else {
                stmt.registerOutParameter(index, Types.DOUBLE , value);
            }

        }else if (dataType.equals(DATE)){
            if (StringUtils.isEmpty(value)){
                stmt.registerOutParameter(index, Types.DATE);
            }else {
                stmt.registerOutParameter(index, Types.DATE , value);
            }

        }else if (dataType.equals(TIME)){
            if (StringUtils.isEmpty(value)){
                stmt.registerOutParameter(index, Types.TIME);
            }else {
                stmt.registerOutParameter(index, Types.TIME , value);
            }

        }else if (dataType.equals(TIMESTAMP)){
            if (StringUtils.isEmpty(value)){
                stmt.registerOutParameter(index, Types.TIMESTAMP);
            }else {
                stmt.registerOutParameter(index, Types.TIMESTAMP , value);
            }

        }else if (dataType.equals(BOOLEAN)){
            if (StringUtils.isEmpty(value)){
                stmt.registerOutParameter(index, Types.BOOLEAN);
            }else {
                stmt.registerOutParameter(index, Types.BOOLEAN , value);
            }
        }
    }
}