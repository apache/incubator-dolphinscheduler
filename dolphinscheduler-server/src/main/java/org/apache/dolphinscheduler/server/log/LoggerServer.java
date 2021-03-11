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

package org.apache.dolphinscheduler.server.log;


import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.remote.NettyRemotingServer;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  logger server
 */
public class LoggerServer {

    private static  final Logger logger = LoggerFactory.getLogger(LoggerServer.class);

    /**
     *  netty server
     */
    private final NettyRemotingServer server;

    /**
     *  netty server config
     */
    private final NettyServerConfig serverConfig;

    /**
     *  logger request processor
     */
    private final LoggerRequestProcessor requestProcessor;

    public LoggerServer(){
        this.serverConfig = new NettyServerConfig();
        this.serverConfig.setListenPort(Constants.RPC_PORT);
        this.server = new NettyRemotingServer(serverConfig);
        this.requestProcessor = new LoggerRequestProcessor();
        this.server.registerProcessor(CommandType.GET_LOG_BYTES_REQUEST, requestProcessor, requestProcessor.getExecutor());
        this.server.registerProcessor(CommandType.ROLL_VIEW_LOG_REQUEST, requestProcessor, requestProcessor.getExecutor());
        this.server.registerProcessor(CommandType.VIEW_WHOLE_LOG_REQUEST, requestProcessor, requestProcessor.getExecutor());
        this.server.registerProcessor(CommandType.REMOVE_TAK_LOG_REQUEST, requestProcessor, requestProcessor.getExecutor());
    }

    /**
     * main launches the server from the command line.
     * @param args arguments
     */
    public static void main(String[] args)  {
        final LoggerServer server = new LoggerServer();
        server.start();
    }

    /**
     * server start
     */
    public void start()  {
        this.server.start();
        logger.info("logger server started, listening on port : {}" , Constants.RPC_PORT);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LoggerServer.this.stop();
            }
        });
    }

    /**
     * stop
     */
    public void stop() {
        this.server.close();
        logger.info("logger server shut down");
    }

}
