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

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.DataSourceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbConnectType;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.datasource.BaseDataSource;
import org.apache.dolphinscheduler.dao.datasource.DataSourceFactory;
import org.apache.dolphinscheduler.dao.datasource.MySQLDataSource;
import org.apache.dolphinscheduler.dao.datasource.OracleDataSource;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceUserMapper;

import org.apache.commons.collections4.MapUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * data source service impl
 */
@Service
public class DataSourceServiceImpl extends BaseServiceImpl implements DataSourceService {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceServiceImpl.class);

    public static final String NAME = "name";
    public static final String NOTE = "note";
    public static final String TYPE = "type";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String PRINCIPAL = "principal";
    public static final String DATABASE = "database";
    public static final String USER_NAME = "userName";
    public static final String OTHER = "other";

    private static final Pattern IPV4_PATTERN = Pattern.compile("^[a-zA-Z0-9\\_\\-\\.]+$");

    private static final Pattern IPV6_PATTERN = Pattern.compile("^[a-zA-Z0-9\\_\\-\\.\\:\\[\\]]+$");

    private static final Pattern DATABASE_PATTER = Pattern.compile("^[a-zA-Z0-9\\_\\-\\.]+$");

    private static final Pattern PARAMS_PATTER = Pattern.compile("^[a-zA-Z0-9]+$");


    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private DataSourceUserMapper datasourceUserMapper;

    /**
     * create data source
     *
     * @param loginUser login user
     * @param name data source name
     * @param desc data source description
     * @param type data source type
     * @param parameter datasource parameters
     * @return create result code
     */
    @Override
    public Result<Void> createDataSource(User loginUser, String name, String desc, DbType type, String parameter) {

        // check name can use or not
        if (checkName(name)) {
            return Result.error(Status.DATASOURCE_EXIST);
        }
        Result<Void> isConnection = checkConnection(type, parameter);
        if (Status.SUCCESS.getCode() != isConnection.getCode()) {
            return isConnection;
        }

        // build datasource
        DataSource dataSource = new DataSource();
        Date now = new Date();

        dataSource.setName(name.trim());
        dataSource.setNote(desc);
        dataSource.setUserId(loginUser.getId());
        dataSource.setUserName(loginUser.getUserName());
        dataSource.setType(type);
        dataSource.setConnectionParams(parameter);
        dataSource.setCreateTime(now);
        dataSource.setUpdateTime(now);
        dataSourceMapper.insert(dataSource);

        return Result.success(null);
    }

    /**
     * updateProcessInstance datasource
     *
     * @param loginUser login user
     * @param name data source name
     * @param desc data source description
     * @param type data source type
     * @param parameter datasource parameters
     * @param id data source id
     * @return update result code
     */
    @Override
    public Result<Void> updateDataSource(int id, User loginUser, String name, String desc, DbType type, String parameter) {

        // determine whether the data source exists
        DataSource dataSource = dataSourceMapper.selectById(id);
        if (dataSource == null) {
            return Result.error(Status.RESOURCE_NOT_EXIST);
        }

        if (!hasPerm(loginUser, dataSource.getUserId())) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        //check name can use or not
        if (!name.trim().equals(dataSource.getName()) && checkName(name)) {
            return Result.error(Status.DATASOURCE_EXIST);
        }
        //check password，if the password is not updated, set to the old password.
        ObjectNode paramObject = JSONUtils.parseObject(parameter);
        String password = paramObject.path(Constants.PASSWORD).asText();
        if (StringUtils.isBlank(password)) {
            String oldConnectionParams = dataSource.getConnectionParams();
            ObjectNode oldParams = JSONUtils.parseObject(oldConnectionParams);
            paramObject.put(Constants.PASSWORD, oldParams.path(Constants.PASSWORD).asText());
        }
        // connectionParams json
        String connectionParams = paramObject.toString();

        Result<Void> isConnection = checkConnection(type, parameter);
        if (Status.SUCCESS.getCode() != isConnection.getCode()) {
            return isConnection;
        }

        Date now = new Date();

        dataSource.setName(name.trim());
        dataSource.setNote(desc);
        dataSource.setUserName(loginUser.getUserName());
        dataSource.setType(type);
        dataSource.setConnectionParams(connectionParams);
        dataSource.setUpdateTime(now);
        dataSourceMapper.updateById(dataSource);
        return Result.success(null);
    }

    private boolean checkName(String name) {
        List<DataSource> queryDataSource = dataSourceMapper.queryDataSourceByName(name.trim());
        return queryDataSource != null && !queryDataSource.isEmpty();
    }

    /**
     * updateProcessInstance datasource
     *
     * @param id datasource id
     * @return data source detail
     */
    @Override
    public Result<Map<String, Object>> queryDataSource(int id) {

        DataSource dataSource = dataSourceMapper.selectById(id);
        if (dataSource == null) {
            return Result.error(Status.RESOURCE_NOT_EXIST);
        }
        // type
        String dataSourceType = dataSource.getType().toString();
        // name
        String dataSourceName = dataSource.getName();
        // desc
        String desc = dataSource.getNote();
        // parameter
        String parameter = dataSource.getConnectionParams();

        BaseDataSource datasourceForm = DataSourceFactory.getDatasource(dataSource.getType(), parameter);
        DbConnectType connectType = null;
        String hostSeperator = Constants.DOUBLE_SLASH;
        if (DbType.ORACLE.equals(dataSource.getType())) {
            connectType = ((OracleDataSource) datasourceForm).getConnectType();
            if (DbConnectType.ORACLE_SID.equals(connectType)) {
                hostSeperator = Constants.AT_SIGN;
            }
        }
        String database = datasourceForm.getDatabase();
        // jdbc connection params
        String other = datasourceForm.getOther();
        String address = datasourceForm.getAddress();

        String[] hostsPorts = getHostsAndPort(address, hostSeperator);
        // ip host
        String host = hostsPorts[0];
        // prot
        String port = hostsPorts[1];
        String separator = "";

        switch (dataSource.getType()) {
            case HIVE:
            case SQLSERVER:
                separator = ";";
                break;
            case MYSQL:
            case POSTGRESQL:
            case CLICKHOUSE:
            case ORACLE:
            case PRESTO:
                separator = "&";
                break;
            default:
                separator = "&";
                break;
        }

        Map<String, String> otherMap = new LinkedHashMap<>();
        if (other != null) {
            String[] configs = other.split(separator);
            for (String config : configs) {
                otherMap.put(config.split("=")[0], config.split("=")[1]);
            }

        }

        Map<String, Object> map = new HashMap<>();
        map.put(NAME, dataSourceName);
        map.put(NOTE, desc);
        map.put(TYPE, dataSourceType);
        if (connectType != null) {
            map.put(Constants.ORACLE_DB_CONNECT_TYPE, connectType);
        }

        map.put(HOST, host);
        map.put(PORT, port);
        map.put(PRINCIPAL, datasourceForm.getPrincipal());
        map.put(Constants.KERBEROS_KRB5_CONF_PATH, datasourceForm.getJavaSecurityKrb5Conf());
        map.put(Constants.KERBEROS_KEY_TAB_USERNAME, datasourceForm.getLoginUserKeytabUsername());
        map.put(Constants.KERBEROS_KEY_TAB_PATH, datasourceForm.getLoginUserKeytabPath());
        map.put(DATABASE, database);
        map.put(USER_NAME, datasourceForm.getUser());
        map.put(OTHER, otherMap);
        return Result.success(map);
    }

    /**
     * query datasource list by keyword
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return data source list page
     */
    @Override
    public Result<PageListVO<DataSource>> queryDataSourceListPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        IPage<DataSource> dataSourceList;
        Page<DataSource> dataSourcePage = new Page<>(pageNo, pageSize);

        if (isAdmin(loginUser)) {
            dataSourceList = dataSourceMapper.selectPaging(dataSourcePage, 0, searchVal);
        } else {
            dataSourceList = dataSourceMapper.selectPaging(dataSourcePage, loginUser.getId(), searchVal);
        }

        List<DataSource> dataSources = dataSourceList != null ? dataSourceList.getRecords() : new ArrayList<>();
        handlePasswd(dataSources);
        PageInfo<DataSource> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotalCount((int) (dataSourceList != null ? dataSourceList.getTotal() : 0L));
        pageInfo.setLists(dataSources);

        return Result.success(new PageListVO<>(pageInfo));
    }

    /**
     * handle datasource connection password for safety
     */
    private void handlePasswd(List<DataSource> dataSourceList) {
        for (DataSource dataSource : dataSourceList) {
            String connectionParams = dataSource.getConnectionParams();
            ObjectNode object = JSONUtils.parseObject(connectionParams);
            object.put(Constants.PASSWORD, getHiddenPassword());
            dataSource.setConnectionParams(object.toString());
        }
    }

    /**
     * get hidden password (resolve the security hotspot)
     *
     * @return hidden password
     */
    private String getHiddenPassword() {
        return Constants.XXXXXX;
    }

    /**
     * query data resource list
     *
     * @param loginUser login user
     * @param type data source type
     * @return data source list page
     */
    @Override
    public Result<List<DataSource>> queryDataSourceList(User loginUser, Integer type) {
        List<DataSource> datasourceList;

        if (isAdmin(loginUser)) {
            datasourceList = dataSourceMapper.listAllDataSourceByType(type);
        } else {
            datasourceList = dataSourceMapper.queryDataSourceByType(loginUser.getId(), type);
        }

        return Result.success(datasourceList);
    }

    /**
     * verify datasource exists
     *
     * @param name datasource name
     * @return true if data datasource not exists, otherwise return false
     */
    @Override
    public Result<Object> verifyDataSourceName(String name) {
        Result<Object> result = new Result<>();
        List<DataSource> dataSourceList = dataSourceMapper.queryDataSourceByName(name);
        if (dataSourceList != null && !dataSourceList.isEmpty()) {
            putMsg(result, Status.DATASOURCE_EXIST);
        } else {
            putMsg(result, Status.SUCCESS);
        }

        return result;
    }

    /**
     * check connection
     *
     * @param type data source type
     * @param parameter data source parameters
     * @return true if connect successfully, otherwise false
     */
    @Override
    public Result<Void> checkConnection(DbType type, String parameter) {
        BaseDataSource datasource = DataSourceFactory.getDatasource(type, parameter);
        if (datasource == null) {
            return Result.errorWithArgs(Status.DATASOURCE_TYPE_NOT_EXIST, type);
        }
        try (Connection connection = datasource.getConnection()) {
            if (connection == null) {
                return Result.error(Status.CONNECTION_TEST_FAILURE);
            }
            return Result.success(null);
        } catch (Exception e) {
            logger.error("datasource test connection error, dbType:{}, jdbcUrl:{}, message:{}.", type, datasource.getJdbcUrl(), e.getMessage());
            return new Result<>(Status.CONNECTION_TEST_FAILURE.getCode(), e.getMessage());
        }
    }

    /**
     * test connection
     *
     * @param id datasource id
     * @return connect result code
     */
    @Override
    public Result<Void> connectionTest(int id) {
        DataSource dataSource = dataSourceMapper.selectById(id);
        if (dataSource == null) {
            return Result.error(Status.RESOURCE_NOT_EXIST);
        }
        return checkConnection(dataSource.getType(), dataSource.getConnectionParams());
    }

    /**
     * build paramters
     *
     * @param type data source  type
     * @param host data source  host
     * @param port data source port
     * @param database data source database name
     * @param userName user name
     * @param password password
     * @param other other parameters
     * @param principal principal
     * @return datasource parameter
     */
    @Override
    public String buildParameter(DbType type, String host,
                                 String port, String database, String principal, String userName,
                                 String password, DbConnectType connectType, String other,
                                 String javaSecurityKrb5Conf, String loginUserKeytabUsername, String loginUserKeytabPath) {
        checkParams(type, port, host, database, other);
        String address = buildAddress(type, host, port, connectType);
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        String jdbcUrl;
        if (DbType.SQLSERVER == type) {
            jdbcUrl = address + ";databaseName=" + database;
        } else {
            jdbcUrl = address + "/" + database;
        }

        if (Constants.ORACLE.equals(type.name())) {
            parameterMap.put(Constants.ORACLE_DB_CONNECT_TYPE, connectType);
        }

        if (CommonUtils.getKerberosStartupState()
                && (type == DbType.HIVE || type == DbType.SPARK)) {
            jdbcUrl += ";principal=" + principal;
        }

        String separator = "";
        if (Constants.MYSQL.equals(type.name())
                || Constants.POSTGRESQL.equals(type.name())
                || Constants.CLICKHOUSE.equals(type.name())
                || Constants.ORACLE.equals(type.name())
                || Constants.PRESTO.equals(type.name())) {
            separator = "&";
        } else if (Constants.HIVE.equals(type.name())
                || Constants.SPARK.equals(type.name())
                || Constants.DB2.equals(type.name())
                || Constants.SQLSERVER.equals(type.name())) {
            separator = ";";
        }

        parameterMap.put(Constants.ADDRESS, address);
        parameterMap.put(Constants.DATABASE, database);
        parameterMap.put(Constants.JDBC_URL, jdbcUrl);
        parameterMap.put(Constants.USER, userName);
        parameterMap.put(Constants.PASSWORD, CommonUtils.encodePassword(password));
        if (CommonUtils.getKerberosStartupState()
                && (type == DbType.HIVE || type == DbType.SPARK)) {
            parameterMap.put(Constants.PRINCIPAL, principal);
            parameterMap.put(Constants.KERBEROS_KRB5_CONF_PATH, javaSecurityKrb5Conf);
            parameterMap.put(Constants.KERBEROS_KEY_TAB_USERNAME, loginUserKeytabUsername);
            parameterMap.put(Constants.KERBEROS_KEY_TAB_PATH, loginUserKeytabPath);
        }

        Map<String, String> map = JSONUtils.toMap(other);
        if (type == DbType.MYSQL) {
            map = MySQLDataSource.buildOtherParams(other);
        }

        if (MapUtils.isNotEmpty(map)) {
            StringBuilder otherSb = new StringBuilder();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                otherSb.append(String.format("%s=%s%s", entry.getKey(), entry.getValue(), separator));
            }
            if (!Constants.DB2.equals(type.name())) {
                otherSb.deleteCharAt(otherSb.length() - 1);
            }
            parameterMap.put(Constants.OTHER, otherSb);
        }

        if (logger.isDebugEnabled()) {
            logger.info("parameters map:{}", JSONUtils.toJsonString(parameterMap));
        }
        return JSONUtils.toJsonString(parameterMap);

    }

    private String buildAddress(DbType type, String host, String port, DbConnectType connectType) {
        StringBuilder sb = new StringBuilder();
        if (Constants.MYSQL.equals(type.name())) {
            sb.append(Constants.JDBC_MYSQL);
            sb.append(host).append(":").append(port);
        } else if (Constants.POSTGRESQL.equals(type.name())) {
            sb.append(Constants.JDBC_POSTGRESQL);
            sb.append(host).append(":").append(port);
        } else if (Constants.HIVE.equals(type.name()) || Constants.SPARK.equals(type.name())) {
            sb.append(Constants.JDBC_HIVE_2);
            String[] hostArray = host.split(",");
            if (hostArray.length > 0) {
                for (String zkHost : hostArray) {
                    sb.append(String.format("%s:%s,", zkHost, port));
                }
                sb.deleteCharAt(sb.length() - 1);
            }
        } else if (Constants.CLICKHOUSE.equals(type.name())) {
            sb.append(Constants.JDBC_CLICKHOUSE);
            sb.append(host).append(":").append(port);
        } else if (Constants.ORACLE.equals(type.name())) {
            if (connectType == DbConnectType.ORACLE_SID) {
                sb.append(Constants.JDBC_ORACLE_SID);
            } else {
                sb.append(Constants.JDBC_ORACLE_SERVICE_NAME);
            }
            sb.append(host).append(":").append(port);
        } else if (Constants.SQLSERVER.equals(type.name())) {
            sb.append(Constants.JDBC_SQLSERVER);
            sb.append(host).append(":").append(port);
        } else if (Constants.DB2.equals(type.name())) {
            sb.append(Constants.JDBC_DB2);
            sb.append(host).append(":").append(port);
        } else if (Constants.PRESTO.equals(type.name())) {
            sb.append(Constants.JDBC_PRESTO);
            sb.append(host).append(":").append(port);
        }

        return sb.toString();
    }

    /**
     * delete datasource
     *
     * @param loginUser login user
     * @param datasourceId data source id
     * @return delete result code
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public Result<Object> delete(User loginUser, int datasourceId) {
        Result<Object> result = new Result<>();
        try {
            //query datasource by id
            DataSource dataSource = dataSourceMapper.selectById(datasourceId);
            if (dataSource == null) {
                logger.error("resource id {} not exist", datasourceId);
                putMsg(result, Status.RESOURCE_NOT_EXIST);
                return result;
            }
            if (!hasPerm(loginUser, dataSource.getUserId())) {
                putMsg(result, Status.USER_NO_OPERATION_PERM);
                return result;
            }
            dataSourceMapper.deleteById(datasourceId);
            datasourceUserMapper.deleteByDatasourceId(datasourceId);
            putMsg(result, Status.SUCCESS);
        } catch (Exception e) {
            logger.error("delete datasource error", e);
            throw new RuntimeException("delete datasource error");
        }
        return result;
    }

    /**
     * unauthorized datasource
     *
     * @param loginUser login user
     * @param userId user id
     * @return unauthed data source result code
     */
    @Override
    public Result<List<DataSource>> unauthDatasource(User loginUser, Integer userId) {

        //only admin operate
        if (!isAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        /**
         * query all data sources except userId
         */
        List<DataSource> resultList = new ArrayList<>();
        List<DataSource> datasourceList = dataSourceMapper.queryDatasourceExceptUserId(userId);
        Set<DataSource> datasourceSet = null;
        if (datasourceList != null && !datasourceList.isEmpty()) {
            datasourceSet = new HashSet<>(datasourceList);

            List<DataSource> authedDataSourceList = dataSourceMapper.queryAuthedDatasource(userId);

            Set<DataSource> authedDataSourceSet = null;
            if (authedDataSourceList != null && !authedDataSourceList.isEmpty()) {
                authedDataSourceSet = new HashSet<>(authedDataSourceList);
                datasourceSet.removeAll(authedDataSourceSet);

            }
            resultList = new ArrayList<>(datasourceSet);
        }
        return Result.success(resultList);
    }

    /**
     * authorized datasource
     *
     * @param loginUser login user
     * @param userId user id
     * @return authorized result code
     */
    @Override
    public Result<List<DataSource>> authedDatasource(User loginUser, Integer userId) {

        if (!isAdmin(loginUser)) {
            return Result.error(Status.USER_NO_OPERATION_PERM);
        }

        List<DataSource> authedDatasourceList = dataSourceMapper.queryAuthedDatasource(userId);
        return Result.success(authedDatasourceList);
    }

    /**
     * get host and port by address
     *
     * @param address address
     * @param separator separator
     * @return sting array: [host,port]
     */
    private String[] getHostsAndPort(String address, String separator) {
        String[] result = new String[2];
        String[] tmpArray = address.split(separator);
        String hostsAndPorts = tmpArray[tmpArray.length - 1];
        StringBuilder hosts = new StringBuilder();
        String[] hostPortArray = hostsAndPorts.split(Constants.COMMA);
        String port = hostPortArray[0].split(Constants.COLON)[1];
        for (String hostPort : hostPortArray) {
            hosts.append(hostPort.split(Constants.COLON)[0]).append(Constants.COMMA);
        }
        hosts.deleteCharAt(hosts.length() - 1);
        result[0] = hosts.toString();
        result[1] = port;
        return result;
    }

    private void checkParams(DbType type, String port, String host, String database, String other) {
        if (null == DbType.of(type.getCode())) {
            throw new ServiceException(Status.DATASOURCE_DB_TYPE_ILLEGAL);
        }
        if (!isNumeric(port)) {
            throw new ServiceException(Status.DATASOURCE_PORT_ILLEGAL);
        }
        if (!IPV4_PATTERN.matcher(host).matches() || !IPV6_PATTERN.matcher(host).matches()) {
            throw new ServiceException(Status.DATASOURCE_HOST_ILLEGAL);
        }
        if (!DATABASE_PATTER.matcher(database).matches()) {
            throw new ServiceException(Status.DATASOURCE_NAME_ILLEGAL);
        }
        if (StringUtils.isBlank(other)) {
            return;
        }
        Map<String, String> map = JSONUtils.toMap(other);
        if (MapUtils.isEmpty(map)) {
            return;
        }
        boolean paramsCheck = map.entrySet().stream().allMatch(p -> PARAMS_PATTER.matcher(p.getValue()).matches());
        if (!paramsCheck) {
            throw new ServiceException(Status.DATASOURCE_OTHER_PARAMS_ILLEGAL);
        }
    }

    private static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
