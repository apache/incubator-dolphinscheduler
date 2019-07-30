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
package cn.escheduler.server.rpc;

import cn.escheduler.common.Constants;
import cn.escheduler.rpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 *  log client
 */
public class LogClient {

    private static  final Logger logger = LoggerFactory.getLogger(LogClient.class);

    private final ManagedChannel channel;
    private final LogViewServiceGrpc.LogViewServiceBlockingStub blockingStub;

    /** Construct client connecting to HelloWorld server at {@code host:port}. */
    public LogClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext(true));
    }

    /** Construct client for accessing RouteGuide server using the existing channel. */
    LogClient(ManagedChannelBuilder<?> channelBuilder) {
        /**
         *  set max message read size
         */
        channelBuilder.maxInboundMessageSize(Integer.MAX_VALUE);
        channel = channelBuilder.build();
        blockingStub = LogViewServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     *  roll view log
     * @param path
     * @param skipLineNum
     * @param limit
     * @return
     */
    public String rollViewLog(String path,int skipLineNum,int limit) {
        logger.info("roll view log , path : {},skipLineNum : {} ,limit :{}", path, skipLineNum, limit);
        LogParameter pathParameter = LogParameter
                .newBuilder()
                .setPath(path)
                .setSkipLineNum(skipLineNum)
                .setLimit(limit)
                .build();
        RetStrInfo retStrInfo;
        try {
            retStrInfo = blockingStub.rollViewLog(pathParameter);
            return retStrInfo.getMsg();
        } catch (StatusRuntimeException e) {
            logger.error("roll view log failed : " + e.getMessage(), e);
            return null;
        }
    }

    /**
     *  view all log
     * @param path
     * @return
     */
    public String viewLog(String path) {
        logger.info("view log path : {}",path);

        PathParameter pathParameter = PathParameter.newBuilder().setPath(path).build();
        RetStrInfo retStrInfo;
        try {
            retStrInfo = blockingStub.viewLog(pathParameter);
            return retStrInfo.getMsg();
        } catch (StatusRuntimeException e) {
            logger.error("view log  failed : " + e.getMessage(), e);
            return null;
        }
    }

    /**
     *  get log bytes
     * @param path
     * @return
     */
    public byte[] getLogBytes(String path) {
        logger.info("get log bytes {}",path);

        PathParameter pathParameter = PathParameter.newBuilder().setPath(path).build();
        RetByteInfo retByteInfo;
        try {
            retByteInfo = blockingStub.getLogBytes(pathParameter);
            return retByteInfo.getData().toByteArray();
        } catch (StatusRuntimeException e) {
            logger.error("get log bytes failed : " + e.getMessage(), e);
            return null;
        }
    }
}