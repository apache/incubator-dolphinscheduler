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

package org.apache.dolphinscheduler.api.dto.resources;

import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * resource dto  test
 */
@RunWith(PowerMockRunner.class)
public class ResourceDTOTest {

    @Mock
    private UserMapper userMapper;

    private User getOwner() {
        User user = new User();
        user.setId(1);
        user.setTenantId(1);
        user.setUserName("test");
        return user;
    }

    @Test
    public void testInitResourceDTO() throws Exception {
        Mockito.when(userMapper.selectById(1)).thenReturn(getOwner());

        Resource resource = new Resource(8, "resource.jar", "/a/b/resource.jar", false,
                "spark resource", "resource.jar", 1, ResourceType.FILE, 17182,
                new Date(), new Date());

        ResourceDTO resourceDTO = new ResourceDTO(resource, userMapper);

        Assert.assertEquals(resource.getId(), resourceDTO.getId());
        Assert.assertEquals(resourceDTO.getPid(), resourceDTO.getPid());
        Assert.assertEquals(resource.getAlias(), resourceDTO.getAlias());
        Assert.assertEquals(resource.getDescription(), resourceDTO.getDescription());
        Assert.assertEquals(resource.getFileName(), resourceDTO.getFileName());
        Assert.assertEquals(resource.getUserId(), resourceDTO.getUserId());
        Assert.assertEquals(resource.getType(), resourceDTO.getType());
        Assert.assertEquals(resource.getSize(), resourceDTO.getSize());
        Assert.assertEquals(resource.getFullName(), resourceDTO.getFullName());
        Assert.assertEquals(resource.isDirectory(), resourceDTO.isDirectory());
        Assert.assertEquals(resource.getCreateTime(), resourceDTO.getCreateTime());
        Assert.assertEquals(resource.getUpdateTime(), resourceDTO.getUpdateTime());
        Assert.assertEquals(getOwner().getUserName(), resourceDTO.getUserName());
    }

}