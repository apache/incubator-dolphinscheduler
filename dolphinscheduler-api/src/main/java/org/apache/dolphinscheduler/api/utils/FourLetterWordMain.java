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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Objects;

public class FourLetterWordMain {

    private static final int DEFAULT_SOCKET_TIMEOUT = 5000;
    protected static final Logger LOG = LoggerFactory.getLogger(FourLetterWordMain.class);

    private FourLetterWordMain() {
        throw new IllegalStateException("FourLetterWordMain class");
    }

    /**
     * Send the 4letterword
     * @param host the destination host
     * @param port the destination port
     * @param cmd the 4letterword
     * @return server response
     * @throws java.io.IOException io exceptions
     */
    public static String send4LetterWord(String host, int port, String cmd)
            throws IOException {
        return send4LetterWord(host, port, cmd, DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * Send the 4letterword
     * @param host the destination host
     * @param port the destination port
     * @param cmd the 4letterword
     * @param timeout in milliseconds, maximum time to wait while connecting/reading data
     * @return server response
     * @throws java.io.IOException io exceptions
     */
    public static String send4LetterWord(String host, int port, String cmd, int timeout)
            throws IOException {
        Objects.requireNonNull(cmd, "cmd must not be null");
        LOG.info("connecting to {} {}", host, port);
        InetSocketAddress hostaddress= host != null ? new InetSocketAddress(host, port) :
                new InetSocketAddress(InetAddress.getByName(null), port);

        try (Socket sock = new Socket()) {
            sock.setSoTimeout(timeout);
            sock.connect(hostaddress, timeout);
            OutputStream outstream = sock.getOutputStream();
            outstream.write(cmd.getBytes());
            outstream.flush();
            // this replicates NC - close the output stream before reading
            sock.shutdownOutput();

            try (BufferedReader reader =
                        new BufferedReader(
                            new InputStreamReader(sock.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                return sb.toString();
            }
        } catch (SocketTimeoutException e) {
            throw new IOException("Exception while executing four letter word: " + cmd, e);
        }
    }
}
