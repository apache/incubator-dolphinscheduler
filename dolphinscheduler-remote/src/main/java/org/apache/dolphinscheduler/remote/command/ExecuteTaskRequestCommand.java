/* * Licensed to the Apache Software Foundation (ASF) under one or more * contributor license agreements.  See the NOTICE file distributed with * this work for additional information regarding copyright ownership. * The ASF licenses this file to You under the Apache License, Version 2.0 * (the "License"); you may not use this file except in compliance with * the License.  You may obtain a copy of the License at * *    http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law or agreed to in writing, software * distributed under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. * See the License for the specific language governing permissions and * limitations under the License. */package org.apache.dolphinscheduler.remote.command;import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;import java.io.Serializable;import java.util.List;/** *  execute task request command */public class ExecuteTaskRequestCommand implements Serializable {    /**     *  task id     */    private String taskId;    /**     *  application name     */    private String applicationName;    /**     *  group name     */    private String groupName;    /**     *  task name     */    private String taskName;    /**     *  connector port     */    private int connectorPort;    /**     *  description info     */    private String description;    /**     *  class name     */    private String className;    /**     *  method name     */    private String methodName;    /**     *  parameters     */    private String params;    /**     *  shard itemds     */    private List<Integer> shardItems;    public List<Integer> getShardItems() {        return shardItems;    }    public void setShardItems(List<Integer> shardItems) {        this.shardItems = shardItems;    }    public String getParams() {        return params;    }    public void setParams(String params) {        this.params = params;    }    public String getTaskId() {        return taskId;    }    public void setTaskId(String taskId) {        this.taskId = taskId;    }    public String getApplicationName() {        return applicationName;    }    public void setApplicationName(String applicationName) {        this.applicationName = applicationName;    }    public String getGroupName() {        return groupName;    }    public void setGroupName(String groupName) {        this.groupName = groupName;    }    public String getTaskName() {        return taskName;    }    public void setTaskName(String taskName) {        this.taskName = taskName;    }    public int getConnectorPort() {        return connectorPort;    }    public void setConnectorPort(int connectorPort) {        this.connectorPort = connectorPort;    }    public String getDescription() {        return description;    }    public void setDescription(String description) {        this.description = description;    }    public String getClassName() {        return className;    }    public void setClassName(String className) {        this.className = className;    }    public String getMethodName() {        return methodName;    }    public void setMethodName(String methodName) {        this.methodName = methodName;    }    /**     *  package request command     *     * @return command     */    public Command convert2Command(){        Command command = new Command();        command.setType(CommandType.EXECUTE_TASK_REQUEST);        byte[] body = FastJsonSerializer.serialize(this);        command.setBody(body);        return command;    }}