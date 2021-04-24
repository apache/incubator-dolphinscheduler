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
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;

import java.util.List;

/**
 * worker group service
 */
public interface WorkerGroupService {

    /**
     * create or update a worker group
     *
     * @param loginUser login user
     * @param id worker group id
     * @param name worker group name
     * @param addrList addr list
     * @return create or update result code
     */
    Result<Void> saveWorkerGroup(User loginUser, int id, String name, String addrList);

    /**
     * query worker group paging
     *
     * @param loginUser login user
     * @param pageNo page number
     * @param searchVal search value
     * @param pageSize page size
     * @return worker group list page
     */
    Result<PageListVO<WorkerGroup>> queryAllGroupPaging(User loginUser, Integer pageNo, Integer pageSize, String searchVal);

    /**
     * query all worker group
     *
     * @return all worker group list
     */
    Result<List<String>> queryAllGroup();

    /**
     * delete worker group by id
     * @param id worker group id
     * @return delete result code
     */
    Result<Void> deleteWorkerGroupById(User loginUser, Integer id);

    /**
     * query all worker address list
     *
     * @return all worker address list
     */
    Result<List<String>> getWorkerAddressList();

}
