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

import org.apache.dolphinscheduler.api.dto.resources.ResourceComponent;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

/**
 * resources service
 */
public interface ResourcesService {

    /**
     * create directory
     *
     * @param loginUser login user
     * @param name alias
     * @param description description
     * @param type type
     * @param pid parent id
     * @param currentDir current directory
     * @return create directory result
     */
    Result<Object> createDirectory(User loginUser,
                                   String name,
                                   String description,
                                   ResourceType type,
                                   int pid,
                                   String currentDir);

    /**
     * create resource
     *
     * @param loginUser login user
     * @param name alias
     * @param desc description
     * @param file file
     * @param type type
     * @param pid parent id
     * @param currentDir current directory
     * @return create result code
     */
    Result<Object> createResource(User loginUser,
                                  String name,
                                  String desc,
                                  ResourceType type,
                                  MultipartFile file,
                                  int pid,
                                  String currentDir);

    /**
     * update resource
     * @param loginUser     login user
     * @param resourceId    resource id
     * @param name          name
     * @param desc          description
     * @param type          resource type
     * @param file          resource file
     * @return  update result code
     */
    Result<Object> updateResource(User loginUser,
                                  int resourceId,
                                  String name,
                                  String desc,
                                  ResourceType type,
                                  MultipartFile file);

    /**
     * query resources list paging
     *
     * @param loginUser login user
     * @param type resource type
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return resource list page
     */
    Result<PageListVO<Resource>> queryResourceListPaging(User loginUser, int directoryId, ResourceType type, String searchVal, Integer pageNo, Integer pageSize);

    /**
     * query resource list
     *
     * @param loginUser login user
     * @param type resource type
     * @return resource list
     */
    Result<List<ResourceComponent>> queryResourceList(User loginUser, ResourceType type);

    /**
     * query resource list by program type
     *
     * @param loginUser login user
     * @param type resource type
     * @return resource list
     */
    Result<List<ResourceComponent>> queryResourceByProgramType(User loginUser, ResourceType type, ProgramType programType);

    /**
     * delete resource
     *
     * @param loginUser login user
     * @param resourceId resource id
     * @return delete result code
     * @throws IOException exception
     */
    Result<Object> delete(User loginUser, int resourceId) throws IOException;

    /**
     * verify resource by name and type
     * @param loginUser login user
     * @param fullName  resource full name
     * @param type      resource type
     * @return true if the resource name not exists, otherwise return false
     */
    Result<Object> verifyResourceName(String fullName, ResourceType type,User loginUser);

    /**
     * verify resource by full name or pid and type
     * @param fullName  resource full name
     * @param id        resource id
     * @param type      resource type
     * @return true if the resource full name or pid not exists, otherwise return false
     */
    Result<Object> queryResource(String fullName,Integer id,ResourceType type);

    /**
     * view resource file online
     *
     * @param resourceId resource id
     * @param skipLineNum skip line number
     * @param limit limit
     * @return resource content
     */
    Result<Object> readResource(int resourceId, int skipLineNum, int limit);

    /**
     * create resource file online
     *
     * @param loginUser login user
     * @param type resource type
     * @param fileName file name
     * @param fileSuffix file suffix
     * @param desc description
     * @param content content
     * @return create result code
     */
    Result<Object> onlineCreateResource(User loginUser, ResourceType type, String fileName, String fileSuffix, String desc, String content,int pid,String currentDirectory);

    /**
     * updateProcessInstance resource
     *
     * @param resourceId resource id
     * @param content content
     * @return update result cod
     */
    Result<Object> updateResourceContent(int resourceId, String content);

    /**
     * download file
     *
     * @param resourceId resource id
     * @return resource content
     * @throws IOException exception
     */
    org.springframework.core.io.Resource downloadResource(int resourceId) throws IOException;

    /**
     * list all file
     *
     * @param loginUser login user
     * @param userId user id
     * @return unauthorized result code
     */
    Result<List<ResourceComponent>> authorizeResourceTree(User loginUser, Integer userId);

    /**
     * unauthorized file
     *
     * @param loginUser login user
     * @param userId user id
     * @return unauthorized result code
     */
    Result<List<ResourceComponent>> unauthorizedFile(User loginUser, Integer userId);

    /**
     * unauthorized udf function
     *
     * @param loginUser login user
     * @param userId user id
     * @return unauthorized result code
     */
    Result<List<UdfFunc>> unauthorizedUDFFunction(User loginUser, Integer userId);

    /**
     * authorized udf function
     *
     * @param loginUser login user
     * @param userId user id
     * @return authorized result code
     */
    Result<List<UdfFunc>> authorizedUDFFunction(User loginUser, Integer userId);

    /**
     * authorized file
     *
     * @param loginUser login user
     * @param userId user id
     * @return authorized result
     */
    Result<List<ResourceComponent>> authorizedFile(User loginUser, Integer userId);

}
