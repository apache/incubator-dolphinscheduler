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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.dolphinscheduler.common.enums.ResourceType;

import java.util.ArrayList;
import java.util.List;

/**
 * resource component
 */
@JsonPropertyOrder({"id","pid","name","fullName","description","isDirectory","children","type"})
public abstract class ResourceComponent {
    public ResourceComponent() {
    }

    public ResourceComponent(int id, int pid, String name, String fullName, String description, boolean isDirectory) {
        this.id = id;
        this.pid = pid;
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this.isDirectory = isDirectory;
        int directoryFlag = isDirectory ? 1:0;
        this.idValue = String.format("%s_%s",id,directoryFlag);
    }


    /**
     * id
     */
    protected int id;
    /**
     * parent id
     */
    protected int pid;
    /**
     * name
     */
    protected String name;
    /**
     * current directory
     */
    protected String currentDir;
    /**
     * full name
     */
    protected String fullName;
    /**
     * description
     */
    protected String description;
    /**
     * is directory
     */
    protected boolean isDirectory;
    /**
     * id value
     */
    protected String idValue;
    /**
     * resource type
     */
    protected ResourceType type;
    /**
     * children
     */
    protected List<ResourceComponent> children = new ArrayList<>();

    /**
     * add resource component
     * @param resourceComponent resource component
     */
    public void add(ResourceComponent resourceComponent){
        children.add(resourceComponent);
    }

    public String getName(){
        return this.name;
    }

    public String getDescription(){
        return this.description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public String getIdValue() {
        return idValue;
    }

    public void setIdValue(int id,boolean isDirectory) {
        int directoryFlag = isDirectory ? 1:0;
        this.idValue = String.format("%s_%s",id,directoryFlag);
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public List<ResourceComponent> getChildren() {
        return children;
    }

    public void setChildren(List<ResourceComponent> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "ResourceComponent{" +
                "id=" + id +
                ", pid=" + pid +
                ", name='" + name + '\'' +
                ", currentDir='" + currentDir + '\'' +
                ", fullName='" + fullName + '\'' +
                ", description='" + description + '\'' +
                ", isDirectory=" + isDirectory +
                ", idValue='" + idValue + '\'' +
                ", type=" + type +
                ", children=" + children +
                '}';
    }

}
