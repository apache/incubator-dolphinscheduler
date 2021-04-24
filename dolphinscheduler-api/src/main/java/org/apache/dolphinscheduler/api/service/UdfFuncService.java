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
import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;

/**
 * udf func service
 */
public interface UdfFuncService {

    /**
     * create udf function
     *
     * @param loginUser login user
     * @param type udf type
     * @param funcName function name
     * @param argTypes argument types
     * @param database database
     * @param desc description
     * @param resourceId resource id
     * @param className class name
     * @return create result code
     */
    Result<Object> createUdfFunction(User loginUser,
                                     String funcName,
                                     String className,
                                     String argTypes,
                                     String database,
                                     String desc,
                                     UdfType type,
                                     int resourceId);

    /**
     * query udf function
     *
     * @param id  udf function id
     * @return udf function detail
     */
    Result<UdfFunc> queryUdfFuncDetail(int id);

    /**
     * updateProcessInstance udf function
     *
     * @param udfFuncId udf function id
     * @param type  resource type
     * @param funcName function name
     * @param argTypes argument types
     * @param database data base
     * @param desc description
     * @param resourceId resource id
     * @param className class name
     * @return update result code
     */
    Result<Void> updateUdfFunc(int udfFuncId,
                                      String funcName,
                                      String className,
                                      String argTypes,
                                      String database,
                                      String desc,
                                      UdfType type,
                                      int resourceId);

    /**
     * query udf function list paging
     *
     * @param loginUser login user
     * @param pageNo page number
     * @param pageSize page size
     * @param searchVal search value
     * @return udf function list page
     */
    Result<PageListVO<UdfFunc>> queryUdfFuncListPaging(User loginUser, String searchVal, Integer pageNo, Integer pageSize);

    /**
     * query udf list
     *
     * @param loginUser login user
     * @param type  udf type
     * @return udf func list
     */
    Result<List<UdfFunc>> queryUdfFuncList(User loginUser, Integer type);

    /**
     * delete udf function
     *
     * @param id udf function id
     * @return delete result code
     */
    Result<Void> delete(int id);

    /**
     * verify udf function by name
     *
     * @param name name
     * @return true if the name can user, otherwise return false
     */
    Result<Void> verifyUdfFuncByName(String name);

}