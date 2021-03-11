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

package org.apache.dolphinscheduler.service.alert;

import org.apache.dolphinscheduler.remote.NettyRemotingClient;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendRequestCommand;
import org.apache.dolphinscheduler.remote.command.alert.AlertSendResponseCommand;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.remote.utils.JsonSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertClientService {

    private static final Logger logger = LoggerFactory.getLogger(AlertClientService.class);
    /**
     * request time out
     */
    private static final long ALERT_REQUEST_TIMEOUT = 10 * 1000L;
    private final NettyClientConfig clientConfig;
    private final NettyRemotingClient client;
    private volatile boolean isRunning;
    private String host;
    private int port;

    /**
     * alert client
     */
    public AlertClientService() {
        this.clientConfig = new NettyClientConfig();
        this.client = new NettyRemotingClient(clientConfig);
        this.isRunning = true;
    }

    /**
     * alert client
     */
    public AlertClientService(String host, int port) {
        this.clientConfig = new NettyClientConfig();
        this.client = new NettyRemotingClient(clientConfig);
        this.isRunning = true;
        this.host = host;
        this.port = port;
    }

    /**
     * close
     */
    public void close() {
        this.client.close();
        this.isRunning = false;
        logger.info("alter client closed");
    }

    /**
     * alert sync send data
     */
    public AlertSendResponseCommand sendAlert(int groupId, String title, String content) {
        return this.sendAlert(this.host, this.port, groupId, title, content);
    }

    /**
     * alert sync send data
     *
     * @param host host
     * @param port port
     * @param groupId groupId
     * @param title title
     * @param content content
     * @return AlertSendResponseCommand
     */
    public AlertSendResponseCommand sendAlert(String host, int port, int groupId, String title, String content) {
        logger.info("sync alert send, host : {}, port : {}, groupId : {}, title : {} ", host, port, groupId, title);
        AlertSendRequestCommand request = new AlertSendRequestCommand(groupId, title, content);
        AlertSendResponseCommand alertSendResponseCommand = null;
        String[] hosts = host.split(",");
        for (String hostName : hosts) {
            final Host address = new Host(hostName, port);
            try {
                Command command = request.convert2Command();
                Command response = this.client.sendSync(address, command, ALERT_REQUEST_TIMEOUT);
                if (response != null) {
                    alertSendResponseCommand = JsonSerializer.deserialize(response.getBody(), AlertSendResponseCommand.class);
                    if (alertSendResponseCommand.getResStatus()) {
                        return alertSendResponseCommand;
                    }
                }
            } catch (Exception e) {
                logger.error("sync alert send error", e);
            } finally {
                this.client.closeChannel(address);
            }

        }
        //Multiple alert services were sent without success. The latest result is returned
        return alertSendResponseCommand;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
