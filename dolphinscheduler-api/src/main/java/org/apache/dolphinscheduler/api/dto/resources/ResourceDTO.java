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
import java.util.Objects;

/**
 * Resource DTO for the front-end.
 */
public class ResourceDTO {
    /**
     * resource id
     */
    private int id;

    /**
     * parent directory id
     */
    private int pid;

    /**
     * resource alias name
     */
    private String alias;

    /**
     * resource fullname
     */
    private String fullName;

    /**
     * resource is directory or not
     */
    private boolean isDirectory;

    /**
     * resource description
     */
    private String description;

    /**
     * resource fileName
     */
    private String fileName;

    /**
     * file owner id
     */
    private int userId;

    /**
     * resource type like file or udf
     */
    private ResourceType type;

    /**
     * resource size
     */
    private long size;

    /**
     * resource create time
     */
    private Date createTime;

    /**
     * resource uptime time
     */
    private Date updateTime;

    /**
     * file owner name
     */
    private String userName;

    public ResourceDTO(Resource resource) {
        this.id = resource.getId();
        this.pid = resource.getPid();
        this.alias = resource.getAlias();
        this.fullName = resource.getFullName();
        this.isDirectory = resource.isDirectory();
        this.description = resource.getDescription();
        this.fileName = resource.getFileName();
        this.userId = resource.getUserId();
        this.type = resource.getType();
        this.size = resource.getSize();
        this.createTime = resource.getCreateTime();
        this.updateTime = resource.getUpdateTime();
    }

    public ResourceDTO(Resource resource, UserMapper userMapper) {
        this(resource);

        User user = userMapper.selectById(userId);
        Objects.requireNonNull(user);
        this.userName = user.getUserName();
    }

    public int getId() {
        return id;
    }

    public ResourceDTO id(int id) {
        this.id = id;
        return this;
    }

    public int getPid() {
        return pid;
    }

    public ResourceDTO pid(int pid) {
        this.pid = pid;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public ResourceDTO alias(String alias) {
        this.alias = alias;
        return this;
    }

    public String getFullName() {
        return fullName;
    }

    public ResourceDTO fullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public ResourceDTO directory(boolean directory) {
        isDirectory = directory;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ResourceDTO description(String description) {
        this.description = description;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public ResourceDTO fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public ResourceDTO userId(int userId) {
        this.userId = userId;
        return this;
    }

    public ResourceType getType() {
        return type;
    }

    public ResourceDTO type(ResourceType type) {
        this.type = type;
        return this;
    }

    public long getSize() {
        return size;
    }

    public ResourceDTO size(long size) {
        this.size = size;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public ResourceDTO createTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public ResourceDTO updateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public ResourceDTO userName(String userName) {
        this.userName = userName;
        return this;
    }
}
