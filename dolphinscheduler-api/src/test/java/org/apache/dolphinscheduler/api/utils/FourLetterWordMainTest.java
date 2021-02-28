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

package org.apache.dolphinscheduler.api.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FourLetterWordMain.class, Socket.class})
public class FourLetterWordMainTest {

    private static final Logger logger =
            LoggerFactory.getLogger(FourLetterWordMainTest.class);
    private static final String NEW_LINE = "\n";

    @InjectMocks
    private FourLetterWordMain fourLetterWord;
    @Mock
    private Socket socket;
    @Mock
    private InetSocketAddress socketAddress;

    private final String localHost = "127.0.0.1";
    private final int zkPort = 2181;
    private ByteArrayOutputStream byteArrayOutputStream;
    private InputStream inputStream;

    private String cmd;
    private String testResult;
    private String expectedStr;

    @Before
    public void setUp() {
        // mock socket class
        PowerMockito.mockStatic(Socket.class);
        try {
            PowerMockito.whenNew(Socket.class).withNoArguments()
                    .thenReturn(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * None mock test method, just to check zookeeper status.
     * Comment @Before notation to run this test.
     * Zookeeper status will be as:
     * Zookeeper version: 3.4.11 ...
     * Received: 6739707
     * Sent: 6739773
     * Connections: 20
     * Outstanding: 0
     * Zxid: 0x9ba
     * Mode: standalone
     * Node count: 263
     */
    public void testCmd() {
        // "192.168.64.11"
        // final String zkHost = localHost;
        final String zkHost = "192.168.64.11";
        cmd = "srvr";
        try {
            // Change localhost to right zk host ip.
            final String result = FourLetterWordMain
                    .send4LetterWord(zkHost, zkPort, cmd);
            logger.info(cmd + ": " + result + "<<<");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEmptyCmd() {
        cmd = "";
        expectedStr = "";
        testSend4LetterWord(cmd, expectedStr);
    }

    @Test
    public void testNullCmd() {
        cmd = null;

        try {
            testResult = FourLetterWordMain
                    .send4LetterWord(localHost, zkPort, cmd);
        } catch (Exception e) {
            testResult = e.getMessage();
        }

        logger.info("testNullCmd result: " + testResult);
        assertEquals("cmd must not be null", testResult);
    }

    @Test
    public void testNullSocketOutput() {
        cmd = "test null socket output";
        expectedStr = null;
        testSend4LetterWord(cmd, expectedStr);
    }

    @Test
    public void testOneLineOutput() {
        cmd = "line 1";

        // line end without \n
        expectedStr = "line 1" + NEW_LINE;
        testSend4LetterWord(cmd, expectedStr);

        // line end with \n
        expectedStr = "line 1\n" + NEW_LINE;
        testSend4LetterWord(cmd, expectedStr);
    }

    @Test
    public void testMultiline() {
        cmd = "line 1 " + NEW_LINE +
                "line 2 " + NEW_LINE +
                "line 3 " + NEW_LINE;

        expectedStr = cmd + NEW_LINE;
        testSend4LetterWord(cmd, expectedStr);

        expectedStr = NEW_LINE + NEW_LINE + NEW_LINE;
        testSend4LetterWord(cmd, expectedStr);
    }

    @Test
    public void testSocketTimeOut() {
        cmd = "test socket time out";

        try {
            doThrow(new SocketTimeoutException())
                .when(socket)
                .connect(any(InetSocketAddress.class), Mockito.anyInt());
            testResult = FourLetterWordMain
                .send4LetterWord(localHost, zkPort, cmd);
        } catch (Exception e) {
            testResult = e.getMessage();
        }

        logger.info("testSocketTimeOut result: " + testResult);
        assertEquals(
                "Exception while executing four letter word: " + cmd,
                testResult
        );
    }

    /**
     * Test FourLetterWordMain.send4LetterWord() with input cmd and output
     * string.
     * @param cmd
     * @param expectedStr
     */
    public void testSend4LetterWord(String cmd, String expectedStr) {
        try {
            final byte[] strBytes = cmd.getBytes();
            byteArrayOutputStream = new ByteArrayOutputStream(strBytes.length);
            byteArrayOutputStream.write(strBytes, 0, strBytes.length);

            inputStream = new ByteArrayInputStream(expectedStr.getBytes());

            when(socket.getOutputStream())
                    .thenReturn(byteArrayOutputStream);
            when(socket.getInputStream()).thenReturn(inputStream);

            final String result = FourLetterWordMain
                    .send4LetterWord(localHost, zkPort, cmd);
            logger.info(
                    "testSend4LetterWord: " +
                    "cmd: " + cmd +
                    ", expectedStr: " + expectedStr +
                    ", result: " + result + "."
            );
            Assert.assertEquals(expectedStr, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
