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
package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.entity.Resource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * resource mapper interface
 */
public interface ResourceMapper extends BaseMapper<Resource> {

    /**
     * query resource list
     * @param fullName full name
     * @param userId userId
     * @param type type
     * @return resource list
     */
    List<Resource> queryResourceList(@Param("fullName") String fullName,
                                     @Param("userId") int userId,
                                     @Param("type") int type);

    /**
     * query resource list
     * @param userId userId
     * @param type type
     * @param perm perm
     * @return resource list
     */
    List<Resource> queryResourceListAuthored(
                                     @Param("userId") int userId,
                                     @Param("type") int type,
                                     @Param("perm") int perm);


    /**
     * resource page
     * @param page page
     * @param userId userId
     * @param id id
     * @param type type
     * @param searchVal searchVal
     * @return resource page
     */
    IPage<Resource> queryResourcePaging(IPage<Resource> page,
                                        @Param("userId") int userId,
                                        @Param("id") int id,
                                        @Param("type") int type,
                                        @Param("searchVal") String searchVal);

    /**
     * query Authed resource list
     * @param userId userId
     * @return resource list
     */
    List<Resource> queryAuthorizedResourceList(@Param("userId") int userId);

    /**
     *  query resource except userId
     * @param userId userId
     * @return resource list
     */
    List<Resource> queryResourceExceptUserId(@Param("userId") int userId);

    /**
     * query tenant code by name
     * @param resName resource name
     * @param resType resource type
     * @return tenant code
     */
    String queryTenantCodeByResourceName(@Param("resName") String resName,@Param("resType") int resType);

    /**
     * list authorized resource
     * @param userId userId
     * @param resNames resNames
     * @param <T> T
     * @return resource list
     */
    <T> List<Resource> listAuthorizedResource(@Param("userId") int userId,@Param("resNames")T[] resNames);



    /**
     * list authorized resource
     * @param userId userId
     * @param resIds resIds
     * @param <T> T
     * @return resource list
     */
    <T> List<Resource> listAuthorizedResourceById(@Param("userId") int userId,@Param("resIds")T[] resIds);

    /**
     * delete resource by id array
     * @param resIds resource id array
     * @return delete num
     */
    int deleteIds(@Param("resIds")Integer[] resIds);

    /**
     * list children
     * @param directoryId directory id
     * @return resource id array
     */
    List<Integer> listChildren(@Param("directoryId") int directoryId);

    /**
     * query resource by full name or pid
     * @param fullName  full name
     * @param type      resource type
     * @return resource
     */
    List<Resource> queryResource(@Param("fullName") String fullName,@Param("type") int type);

    /**
     * list resource by id array
     * @param resIds resource id array
     * @return resource list
     */
    List<Resource> listResourceByIds(@Param("resIds")Integer[] resIds);

    /**
     * update resource
     * @param resourceList  resource list
     * @return update num
     */
    int batchUpdateResource(@Param("resourceList") List<Resource> resourceList);
}
