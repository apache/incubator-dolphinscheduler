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
import org.apache.dolphinscheduler.common.model.MasterServer;

import java.util.Date;

/**
 *  heartbeat for ZK reigster res info
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
     * @return
     */
    public static String getResInfoJson(double cpuUsage , double memoryUsage,double loadAverage){
        ResInfo resInfo = new ResInfo(cpuUsage,memoryUsage,loadAverage);
        return JSONUtils.toJson(resInfo);
    }


    /**
     * get heart beat info
     * @param now
     * @return
     */
    public static String getHeartBeatInfo(Date now){
        return buildHeartbeatForZKInfo(OSUtils.getHost(),
                OSUtils.getProcessID(),
                OSUtils.cpuUsage(),
                OSUtils.memoryUsage(),
                OSUtils.loadAverage(),
                DateUtils.dateToString(now),
                DateUtils.dateToString(now));

    }

    /**
     * build heartbeat info for zk
     * @param host
     * @param port
     * @param cpuUsage
     * @param memoryUsage
     * @param loadAverage
     * @param createTime
     * @param lastHeartbeatTime
     * @return
     */
    public static String buildHeartbeatForZKInfo(String host , int port ,
                                         double cpuUsage , double memoryUsage,double loadAverage,
                                         String createTime,String lastHeartbeatTime){

        return host + Constants.COMMA + port + Constants.COMMA
                + cpuUsage + Constants.COMMA
                + memoryUsage + Constants.COMMA
                + loadAverage + Constants.COMMA
                + createTime + Constants.COMMA
                + lastHeartbeatTime;
    }

    /**
     * parse heartbeat info for zk
     * @param heartBeatInfo
     * @return
     */
    public static MasterServer parseHeartbeatForZKInfo(String heartBeatInfo){
        MasterServer masterServer =  null;
        String[] masterArray = heartBeatInfo.split(Constants.COMMA);
        if(masterArray == null ||
                masterArray.length != Constants.HEARTBEAT_FOR_ZOOKEEPER_INFO_LENGTH){
            return masterServer;

        }
        masterServer = new MasterServer();
        masterServer.setHost(masterArray[0]);
        masterServer.setPort(Integer.parseInt(masterArray[1]));
        masterServer.setResInfo(getResInfoJson(Double.parseDouble(masterArray[2]),
                Double.parseDouble(masterArray[3]),
                Double.parseDouble(masterArray[4])));
        masterServer.setCreateTime(DateUtils.stringToDate(masterArray[5]));
        masterServer.setLastHeartbeatTime(DateUtils.stringToDate(masterArray[6]));
        return masterServer;
    }

}
