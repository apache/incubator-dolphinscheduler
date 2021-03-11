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

package org.apache.dolphinscheduler.remote;

import io.netty.channel.Channel;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.Ping;
import org.apache.dolphinscheduler.remote.command.Pong;
import org.apache.dolphinscheduler.remote.config.NettyClientConfig;
import org.apache.dolphinscheduler.remote.config.NettyServerConfig;
import org.apache.dolphinscheduler.remote.future.InvokeCallback;
import org.apache.dolphinscheduler.remote.future.ResponseFuture;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 *  netty remote client test
 */
public class NettyRemotingClientTest {


    /**
     *  test send sync
     */
    @Test
    public void testSendSync(){
        NettyServerConfig serverConfig = new NettyServerConfig();

        NettyRemotingServer server = new NettyRemotingServer(serverConfig);
        server.registerProcessor(CommandType.PING, new NettyRequestProcessor() {
            @Override
            public void process(Channel channel, Command command) {
                channel.writeAndFlush(Pong.create(command.getOpaque()));
            }
        });


        server.start();
        //
        final NettyClientConfig clientConfig = new NettyClientConfig();
        NettyRemotingClient client = new NettyRemotingClient(clientConfig);
        Command commandPing = Ping.create();
        try {
            Command response = client.sendSync(new Host("127.0.0.1", serverConfig.getListenPort()), commandPing, 2000);
            Assert.assertEquals(commandPing.getOpaque(), response.getOpaque());
        } catch (Exception e) {
            e.printStackTrace();
        }
        server.close();
        client.close();
    }

    /**
     *  test send async
     */
    @Test
    public void testSendAsync(){
        NettyServerConfig serverConfig = new NettyServerConfig();

        NettyRemotingServer server = new NettyRemotingServer(serverConfig);
        server.registerProcessor(CommandType.PING, new NettyRequestProcessor() {
            @Override
            public void process(Channel channel, Command command) {
                channel.writeAndFlush(Pong.create(command.getOpaque()));
            }
        });
        server.start();
        //
        final NettyClientConfig clientConfig = new NettyClientConfig();
        NettyRemotingClient client = new NettyRemotingClient(clientConfig);
        CountDownLatch latch = new CountDownLatch(1);
        Command commandPing = Ping.create();
        try {
            final AtomicLong opaque = new AtomicLong(0);
            client.sendAsync(new Host("127.0.0.1", serverConfig.getListenPort()), commandPing, 2000, new InvokeCallback() {
                @Override
                public void operationComplete(ResponseFuture responseFuture) {
                    opaque.set(responseFuture.getOpaque());
                    latch.countDown();
                }
            });
            latch.await();
            Assert.assertEquals(commandPing.getOpaque(), opaque.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
        server.close();
        client.close();
    }
}
