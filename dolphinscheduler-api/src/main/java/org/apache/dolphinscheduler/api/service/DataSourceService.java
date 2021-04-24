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

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.enums.DbConnectType;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;
import java.util.Map;

/**
 * data source service
 */
public interface DataSourceService {

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
    Result<Void> createDataSource(User loginUser, String name, String desc, DbType type, String parameter);

    /**
     * updateProcessInstance datasource
     *
     * @param loginUser login user
     * @param name      data source name
     * @param desc      data source description
     * @param type      data source type
     * @param parameter datasource parameters
     * @param id        data source id
     * @return update result code
     */
    Result<Void> updateDataSource(int id, User loginUser, String name, String desc, DbType type, String parameter);

    /**
     * updateProcessInstance datasource
     *
     * @param id datasource id
     * @return data source detail
     */
    Result<Map<String, Object>> queryDataSource(int id);

    /**
     * query datasource list by keyword
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return data source list page
     */
    Result<PageListVO<DataSource>> queryDataSourceListPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize);

    /**
     * query data resource list
     *
     * @param loginUser login user
     * @param type data source type
     * @return data source list page
     */
    Result<List<DataSource>> queryDataSourceList(User loginUser, Integer type);

    /**
     * verify datasource exists
     *
     * @param name      datasource name
     * @return true if data datasource not exists, otherwise return false
     */
    Result<Object> verifyDataSourceName(String name);

    /**
     * check connection
     *
     * @param type      data source type
     * @param parameter data source parameters
     * @return true if connect successfully, otherwise false
     */
    Result<Void> checkConnection(DbType type, String parameter);

    /**
     * test connection
     *
     * @param id datasource id
     * @return connect result code
     */
    Result<Void> connectionTest(int id);

    /**
     * build paramters
     *
     * @param type      data source  type
     * @param host      data source  host
     * @param port      data source port
     * @param database  data source database name
     * @param userName  user name
     * @param password  password
     * @param other     other parameters
     * @param principal principal
     * @return datasource parameter
     */
    String buildParameter(DbType type, String host,
                          String port, String database, String principal, String userName,
                          String password, DbConnectType connectType, String other,
                          String javaSecurityKrb5Conf, String loginUserKeytabUsername, String loginUserKeytabPath);

    /**
     * delete datasource
     *
     * @param loginUser    login user
     * @param datasourceId data source id
     * @return delete result code
     */
    Result<Object> delete(User loginUser, int datasourceId);

    /**
     * unauthorized datasource
     *
     * @param loginUser login user
     * @param userId    user id
     * @return unauthed data source result code
     */
    Result<List<DataSource>> unauthDatasource(User loginUser, Integer userId);

    /**
     * authorized datasource
     *
     * @param loginUser login user
     * @param userId    user id
     * @return authorized result code
     */
    Result<List<DataSource>> authedDatasource(User loginUser, Integer userId);
}
