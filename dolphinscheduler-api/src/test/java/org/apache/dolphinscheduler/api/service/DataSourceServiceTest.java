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
import org.apache.dolphinscheduler.api.service.impl.DataSourceServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DbConnectType;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.datasource.BaseDataSource;
import org.apache.dolphinscheduler.dao.datasource.DataSourceFactory;
import org.apache.dolphinscheduler.dao.datasource.MySQLDataSource;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceUserMapper;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * data source service test
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"sun.security.*", "javax.net.*"})
@PrepareForTest({DataSourceFactory.class, CommonUtils.class})
public class DataSourceServiceTest {

    @InjectMocks
    private DataSourceServiceImpl dataSourceService;

    @Mock
    private DataSourceMapper dataSourceMapper;

    @Mock
    private DataSourceUserMapper datasourceUserMapper;

    public void createDataSourceTest() {
        User loginUser = getAdminUser();

        String dataSourceName = "dataSource01";
        String dataSourceDesc = "test dataSource";
        DbType dataSourceType = DbType.POSTGRESQL;
        String parameter = dataSourceService.buildParameter(dataSourceType, "172.16.133.200", "5432", "dolphinscheduler", null, "postgres", "", null, null, null, null, null);

        // data source exits
        List<DataSource> dataSourceList = new ArrayList<>();
        DataSource dataSource = new DataSource();
        dataSource.setName(dataSourceName);
        dataSourceList.add(dataSource);
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(dataSourceList);
        Result dataSourceExitsResult = dataSourceService.createDataSource(loginUser, dataSourceName, dataSourceDesc, dataSourceType, parameter);
        Assert.assertEquals(Status.DATASOURCE_EXIST.getCode(), dataSourceExitsResult.getCode().intValue());

        // data source exits
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(null);
        Result connectionResult = new Result(Status.DATASOURCE_CONNECT_FAILED.getCode(), Status.DATASOURCE_CONNECT_FAILED.getMsg());
        //PowerMockito.when(dataSourceService.checkConnection(dataSourceType, parameter)).thenReturn(connectionResult);
        PowerMockito.doReturn(connectionResult).when(dataSourceService).checkConnection(dataSourceType, parameter);
        Result connectFailedResult = dataSourceService.createDataSource(loginUser, dataSourceName, dataSourceDesc, dataSourceType, parameter);
        Assert.assertEquals(Status.DATASOURCE_CONNECT_FAILED.getCode(), connectFailedResult.getCode().intValue());

        // data source exits
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(null);
        connectionResult = new Result(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg());
        PowerMockito.when(dataSourceService.checkConnection(dataSourceType, parameter)).thenReturn(connectionResult);
        PowerMockito.when(DataSourceFactory.getDatasource(dataSourceType, parameter)).thenReturn(null);
        Result notValidError = dataSourceService.createDataSource(loginUser, dataSourceName, dataSourceDesc, dataSourceType, parameter);
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode(), notValidError.getCode().intValue());

        // success
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName.trim())).thenReturn(null);
        PowerMockito.when(dataSourceService.checkConnection(dataSourceType, parameter)).thenReturn(connectionResult);
        PowerMockito.when(DataSourceFactory.getDatasource(dataSourceType, parameter)).thenReturn(JSONUtils.parseObject(parameter, MySQLDataSource.class));
        Result success = dataSourceService.createDataSource(loginUser, dataSourceName, dataSourceDesc, dataSourceType, parameter);
        Assert.assertEquals(Status.SUCCESS.getCode(), success.getCode().intValue());
    }

    public void updateDataSourceTest() {
        User loginUser = getAdminUser();

        int dataSourceId = 12;
        String dataSourceName = "dataSource01";
        String dataSourceDesc = "test dataSource";
        DbType dataSourceType = DbType.POSTGRESQL;
        String parameter = dataSourceService.buildParameter(dataSourceType, "172.16.133.200", "5432", "dolphinscheduler", null, "postgres", "", null, null, null, null, null);

        // data source not exits
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(null);
        Result resourceNotExits = dataSourceService.updateDataSource(dataSourceId, loginUser, dataSourceName, dataSourceDesc, dataSourceType, parameter);
        Assert.assertEquals(Status.RESOURCE_NOT_EXIST.getCode(), resourceNotExits.getCode().intValue());
        // user no operation perm
        DataSource dataSource = new DataSource();
        dataSource.setUserId(0);
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        Result userNoOperationPerm = dataSourceService.updateDataSource(dataSourceId, loginUser, dataSourceName, dataSourceDesc, dataSourceType, parameter);
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), userNoOperationPerm.getCode().intValue());

        // data source name exits
        dataSource.setUserId(-1);
        List<DataSource> dataSourceList = new ArrayList<>();
        dataSourceList.add(dataSource);
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName)).thenReturn(dataSourceList);
        Result dataSourceNameExist = dataSourceService.updateDataSource(dataSourceId, loginUser, dataSourceName, dataSourceDesc, dataSourceType, parameter);
        Assert.assertEquals(Status.DATASOURCE_EXIST.getCode(), dataSourceNameExist.getCode().intValue());

        // data source connect failed
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName)).thenReturn(null);
        Result connectionResult = new Result(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg());
        PowerMockito.when(dataSourceService.checkConnection(dataSourceType, parameter)).thenReturn(connectionResult);
        Result connectFailed = dataSourceService.updateDataSource(dataSourceId, loginUser, dataSourceName, dataSourceDesc, dataSourceType, parameter);
        Assert.assertEquals(Status.DATASOURCE_CONNECT_FAILED.getCode(), connectFailed.getCode().intValue());

        //success
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName)).thenReturn(null);
        connectionResult = new Result(Status.DATASOURCE_CONNECT_FAILED.getCode(), Status.DATASOURCE_CONNECT_FAILED.getMsg());
        PowerMockito.when(dataSourceService.checkConnection(dataSourceType, parameter)).thenReturn(connectionResult);
        Result success = dataSourceService.updateDataSource(dataSourceId, loginUser, dataSourceName, dataSourceDesc, dataSourceType, parameter);
        Assert.assertEquals(Status.SUCCESS.getCode(), success.getCode().intValue());

    }

    @Test
    public void queryDataSourceListPagingTest() {
        User loginUser = getAdminUser();
        String searchVal = "";
        int pageNo = 1;
        int pageSize = 10;
        Result<PageListVO<DataSource>> pageListVOResult = dataSourceService.queryDataSourceListPaging(loginUser, searchVal, pageNo, pageSize);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) pageListVOResult.getCode());
    }

    @Test
    public void connectionTest() {
        int dataSourceId = -1;
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(null);
        Result result = dataSourceService.connectionTest(dataSourceId);
        Assert.assertEquals(Status.RESOURCE_NOT_EXIST.getCode(), result.getCode().intValue());
    }

    @Test
    public void deleteTest() {
        User loginUser = getAdminUser();
        int dataSourceId = 1;
        Result result = new Result();

        //resource not exist
        dataSourceService.putMsg(result, Status.RESOURCE_NOT_EXIST);
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(null);
        Assert.assertEquals(result.getCode(), dataSourceService.delete(loginUser, dataSourceId).getCode());

        // user no operation perm
        dataSourceService.putMsg(result, Status.USER_NO_OPERATION_PERM);
        DataSource dataSource = new DataSource();
        dataSource.setUserId(0);
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        Assert.assertEquals(result.getCode(), dataSourceService.delete(loginUser, dataSourceId).getCode());

        // success
        dataSourceService.putMsg(result, Status.SUCCESS);
        dataSource.setUserId(-1);
        PowerMockito.when(dataSourceMapper.selectById(dataSourceId)).thenReturn(dataSource);
        Assert.assertEquals(result.getCode(), dataSourceService.delete(loginUser, dataSourceId).getCode());

    }

    @Test
    public void unauthDatasourceTest() {
        User loginUser = getAdminUser();
        int userId = -1;

        //user no operation perm
        Result<List<DataSource>> result = dataSourceService.unauthDatasource(loginUser, userId);
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) result.getCode());

        //success
        loginUser.setUserType(UserType.ADMIN_USER);
        Result<List<DataSource>> success = dataSourceService.unauthDatasource(loginUser, userId);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) success.getCode());
    }

    @Test
    public void authedDatasourceTest() {
        User loginUser = getAdminUser();
        int userId = -1;

        //user no operation perm
        Result<List<DataSource>> noOperationPerm = dataSourceService.authedDatasource(loginUser, userId);
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM.getCode(), (int) noOperationPerm.getCode());

        //success
        loginUser.setUserType(UserType.ADMIN_USER);
        Result<List<DataSource>> success = dataSourceService.authedDatasource(loginUser, userId);
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) success.getCode());
    }

    @Test
    public void queryDataSourceListTest() {
        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        Result<List<DataSource>> result = dataSourceService.queryDataSourceList(loginUser, DbType.MYSQL.ordinal());
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

    @Test
    public void verifyDataSourceNameTest() {
        User loginUser = new User();
        loginUser.setUserType(UserType.GENERAL_USER);
        String dataSourceName = "dataSource1";
        PowerMockito.when(dataSourceMapper.queryDataSourceByName(dataSourceName)).thenReturn(getDataSourceList());
        Result result = dataSourceService.verifyDataSourceName(dataSourceName);
        Assert.assertEquals(Status.DATASOURCE_EXIST.getMsg(), result.getMsg());
    }

    @Test
    public void queryDataSourceTest() {
        PowerMockito.when(dataSourceMapper.selectById(Mockito.anyInt())).thenReturn(null);
        Result<Map<String, Object>> result = dataSourceService.queryDataSource(Mockito.anyInt());
        Assert.assertEquals(Status.RESOURCE_NOT_EXIST.getCode(), (int) result.getCode());

        PowerMockito.when(dataSourceMapper.selectById(Mockito.anyInt())).thenReturn(getOracleDataSource());
        result = dataSourceService.queryDataSource(Mockito.anyInt());
        Assert.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

    private List<DataSource> getDataSourceList() {

        List<DataSource> dataSources = new ArrayList<>();
        dataSources.add(getOracleDataSource());
        return dataSources;
    }

    private DataSource getOracleDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setName("test");
        dataSource.setNote("Note");
        dataSource.setType(DbType.ORACLE);
        dataSource.setConnectionParams("{\"connectType\":\"ORACLE_SID\",\"address\":\"jdbc:oracle:thin:@192.168.xx.xx:49161\",\"database\":\"XE\","
                + "\"jdbcUrl\":\"jdbc:oracle:thin:@192.168.xx.xx:49161/XE\",\"user\":\"system\",\"password\":\"oracle\"}");

        return dataSource;
    }

    @Test
    public void buildParameter() {
        String param = dataSourceService.buildParameter(DbType.ORACLE, "192.168.9.1", "1521", "im"
                , "", "test", "test", DbConnectType.ORACLE_SERVICE_NAME, "", "", "", "");
        String expected = "{\"connectType\":\"ORACLE_SERVICE_NAME\",\"address\":\"jdbc:oracle:thin:@//192.168.9.1:1521\",\"database\":\"im\","
                + "\"jdbcUrl\":\"jdbc:oracle:thin:@//192.168.9.1:1521/im\",\"user\":\"test\",\"password\":\"test\"}";
        Assert.assertEquals(expected, param);

        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.getKerberosStartupState()).thenReturn(true);
        PowerMockito.when(CommonUtils.encodePassword(Mockito.anyString())).thenReturn("test");
        param = dataSourceService.buildParameter(DbType.HIVE, "192.168.9.1", "10000", "im"
                , "hive/hdfs-mycluster@ESZ.COM", "test", "test", null, "", "/opt/krb5.conf", "test2/hdfs-mycluster@ESZ.COM", "/opt/hdfs.headless.keytab");
        expected = "{\"address\":\"jdbc:hive2://192.168.9.1:10000\",\"database\":\"im\",\"jdbcUrl\":\"jdbc:hive2://192.168.9.1:10000/im;principal=hive/hdfs-mycluster@ESZ.COM\","
                + "\"user\":\"test\",\"password\":\"test\",\"principal\":\"hive/hdfs-mycluster@ESZ.COM\",\"javaSecurityKrb5Conf\":\"/opt/krb5.conf\","
                + "\"loginUserKeytabUsername\":\"test2/hdfs-mycluster@ESZ.COM\",\"loginUserKeytabPath\":\"/opt/hdfs.headless.keytab\"}";
        Assert.assertEquals(expected, param);

    }

    @Test
    public void buildParameterWithDecodePassword() {
        PropertyUtils.setValue(Constants.DATASOURCE_ENCRYPTION_ENABLE, "true");
        String other = "{\"autoDeserialize\":\"yes\",\"allowUrlInLocalInfile\":\"true\"}";
        String param = dataSourceService.buildParameter(DbType.MYSQL, "192.168.9.1", "1521", "im"
                , "", "test", "123456", null, other, "", "", "");
        String expected = "{\"address\":\"jdbc:mysql://192.168.9.1:1521\",\"database\":\"im\",\"jdbcUrl\":\"jdbc:mysql://192.168.9.1:1521/im\","
                + "\"user\":\"test\",\"password\":\"IUAjJCVeJipNVEl6TkRVMg==\"}";
        Assert.assertEquals(expected, param);

        PropertyUtils.setValue(Constants.DATASOURCE_ENCRYPTION_ENABLE, "false");
        param = dataSourceService.buildParameter(DbType.MYSQL, "192.168.9.1", "1521", "im"
                , "", "test", "123456", null, "", "", "", "");
        expected = "{\"address\":\"jdbc:mysql://192.168.9.1:1521\",\"database\":\"im\",\"jdbcUrl\":\"jdbc:mysql://192.168.9.1:1521/im\","
                + "\"user\":\"test\",\"password\":\"123456\"}";
        Assert.assertEquals(expected, param);
    }

    /**
     * get Mock Admin User
     *
     * @return admin user
     */
    private User getAdminUser() {
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserName("admin");
        loginUser.setUserType(UserType.GENERAL_USER);
        return loginUser;
    }

    /**
     * test check connection
     */
    @Test
    public void testCheckConnection() throws Exception {
        DbType dataSourceType = DbType.POSTGRESQL;
        String parameter = dataSourceService.buildParameter(dataSourceType, "172.16.133.200", "5432", "dolphinscheduler", null, "postgres", "", null, null, null, null, null);

        PowerMockito.mockStatic(DataSourceFactory.class);
        PowerMockito.when(DataSourceFactory.getDatasource(Mockito.any(), Mockito.anyString())).thenReturn(null);
        Result result = dataSourceService.checkConnection(dataSourceType, parameter);
        Assert.assertEquals(Status.DATASOURCE_TYPE_NOT_EXIST.getCode(), result.getCode().intValue());

        BaseDataSource dataSource = PowerMockito.mock(BaseDataSource.class);
        PowerMockito.when(DataSourceFactory.getDatasource(Mockito.any(), Mockito.anyString())).thenReturn(dataSource);
        PowerMockito.when(dataSource.getConnection()).thenReturn(null);
        result = dataSourceService.checkConnection(dataSourceType, parameter);
        Assert.assertEquals(Status.CONNECTION_TEST_FAILURE.getCode(), result.getCode().intValue());

        Connection connection = PowerMockito.mock(Connection.class);
        PowerMockito.when(dataSource.getConnection()).thenReturn(connection);
        result = dataSourceService.checkConnection(dataSourceType, parameter);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());

    }

}
