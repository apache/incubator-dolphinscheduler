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

package org.apache.dolphinscheduler.plugin.task.spark;

import org.apache.dolphinscheduler.spi.task.AbstractParameters;
import org.apache.dolphinscheduler.spi.task.ResourceInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * spark parameters
 */
public class SparkParameters extends AbstractParameters {

    /**
     * main jar
     */
    private ResourceInfo mainJar;

    /**
     * main class
     */
    private String mainClass;

    /**
     * deploy mode
     */
    private String deployMode;

    /**
     * arguments
     */
    private String mainArgs;

    /**
     * driver-cores Number of cores used by the driver, only in cluster mode
     */
    private int driverCores;

    /**
     * driver-memory Memory for driver
     */

    private String driverMemory;

    /**
     * num-executors Number of executors to launch
     */
    private int numExecutors;

    /**
     * executor-cores Number of cores per executor
     */
    private int executorCores;

    /**
     * Memory per executor
     */
    private String executorMemory;

    /**
     * app name
     */
    private String appName;

    /**
     * The YARN queue to submit to
     */
    private String queue;

    /**
     * other arguments
     */
    private String others;

    /**
     * program type
     * 0 JAVA,1 SCALA,2 PYTHON
     */
    private ProgramType programType;

    /**
     * spark version
     */
    private String sparkVersion;

    /**
     * resource list
     */
    private List<ResourceInfo> resourceList = new ArrayList<>();

    public ResourceInfo getMainJar() {
        return mainJar;
    }

    public void setMainJar(ResourceInfo mainJar) {
        this.mainJar = mainJar;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public String getDeployMode() {
        return deployMode;
    }

    public void setDeployMode(String deployMode) {
        this.deployMode = deployMode;
    }

    public String getMainArgs() {
        return mainArgs;
    }

    public void setMainArgs(String mainArgs) {
        this.mainArgs = mainArgs;
    }

    public int getDriverCores() {
        return driverCores;
    }

    public void setDriverCores(int driverCores) {
        this.driverCores = driverCores;
    }

    public String getDriverMemory() {
        return driverMemory;
    }

    public void setDriverMemory(String driverMemory) {
        this.driverMemory = driverMemory;
    }

    public int getNumExecutors() {
        return numExecutors;
    }

    public void setNumExecutors(int numExecutors) {
        this.numExecutors = numExecutors;
    }

    public int getExecutorCores() {
        return executorCores;
    }

    public void setExecutorCores(int executorCores) {
        this.executorCores = executorCores;
    }

    public String getExecutorMemory() {
        return executorMemory;
    }

    public void setExecutorMemory(String executorMemory) {
        this.executorMemory = executorMemory;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getOthers() {
        return others;
    }

    public void setOthers(String others) {
        this.others = others;
    }

    public List<ResourceInfo> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<ResourceInfo> resourceList) {
        this.resourceList = resourceList;
    }

    public ProgramType getProgramType() {
        return programType;
    }

    public void setProgramType(ProgramType programType) {
        this.programType = programType;
    }

    public String getSparkVersion() {
        return sparkVersion;
    }

    public void setSparkVersion(String sparkVersion) {
        this.sparkVersion = sparkVersion;
    }

    @Override
    public boolean checkParameters() {
        return mainJar != null && programType != null;
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        if (mainJar != null && !resourceList.contains(mainJar)) {
            resourceList.add(mainJar);
        }
        return resourceList;
    }

}
