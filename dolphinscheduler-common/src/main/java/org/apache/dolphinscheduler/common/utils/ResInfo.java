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
package org.apache.dolphinscheduler.common.utils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.model.Server;

/**
 *  heartbeat for ZK register res info
 */
public class ResInfo {

    /**
     *  cpuUsage
     */
    private double cpuUsage;

    /**
     *  memoryUsage
     */
    private double memoryUsage;

    /**
     * loadAverage
     */
    private double loadAverage;

    public ResInfo(){}

    public ResInfo(double cpuUsage , double memoryUsage){
        this.cpuUsage = cpuUsage ;
        this.memoryUsage = memoryUsage;
    }

    public ResInfo(double cpuUsage, double memoryUsage, double loadAverage) {
        this(cpuUsage,memoryUsage);
        this.loadAverage = loadAverage;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public double getLoadAverage() {
        return loadAverage;
    }

    public void setLoadAverage(double loadAverage) {
        this.loadAverage = loadAverage;
    }

    /**
     * get CPU and memory usage
     * @param cpuUsage cpu usage
     * @param memoryUsage memory usage
     * @param loadAverage load average
     * @return cpu and memory usage
     */
    public static String getResInfoJson(double cpuUsage , double memoryUsage,double loadAverage){
        ResInfo resInfo = new ResInfo(cpuUsage,memoryUsage,loadAverage);
        return JSONUtils.toJsonString(resInfo);
    }


    /**
     * parse heartbeat info for zk
     * @param heartBeatInfo heartbeat info
     * @return heartbeat info to Server
     */
    public static Server parseHeartbeatForZKInfo(String heartBeatInfo){
        if (StringUtils.isEmpty(heartBeatInfo)) {
            return null;
        }
        String[] masterArray = heartBeatInfo.split(Constants.COMMA);
        if(masterArray.length != Constants.HEARTBEAT_FOR_ZOOKEEPER_INFO_LENGTH){
            return null;

        }
        Server masterServer = new Server();
        masterServer.setResInfo(getResInfoJson(Double.parseDouble(masterArray[0]),
                Double.parseDouble(masterArray[1]),
                Double.parseDouble(masterArray[2])));
        masterServer.setCreateTime(DateUtils.stringToDate(masterArray[6]));
        masterServer.setLastHeartbeatTime(DateUtils.stringToDate(masterArray[7]));
        //set process id
        masterServer.setId(Integer.parseInt(masterArray[9]));
        return masterServer;
    }

}
