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

import org.apache.dolphinscheduler.common.utils.IOUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.log.GetLogBytesRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.GetLogBytesResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.RemoveTaskLogRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.RemoveTaskLogResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.RollViewLogRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.RollViewLogResponseCommand;
import org.apache.dolphinscheduler.remote.command.log.ViewLogRequestCommand;
import org.apache.dolphinscheduler.remote.command.log.ViewLogResponseCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

/**
 *  logger request process logic
 */
public class LoggerRequestProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(LoggerRequestProcessor.class);

    private final ThreadPoolExecutor executor;

    public LoggerRequestProcessor() {
        this.executor = new ThreadPoolExecutor(4, 4, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100));
    }

    @Override
    public void process(Channel channel, Command command) {
        logger.info("received command : {}", command);

        /**
         * request task log command type
         */
        final CommandType commandType = command.getType();
        switch (commandType) {
            case GET_LOG_BYTES_REQUEST:
                GetLogBytesRequestCommand getLogRequest = JSONUtils.parseObject(
                        command.getBody(), GetLogBytesRequestCommand.class);
                byte[] bytes = getFileContentBytes(getLogRequest.getPath());
                GetLogBytesResponseCommand getLogResponse = new GetLogBytesResponseCommand(bytes);
                channel.writeAndFlush(getLogResponse.convert2Command(command.getOpaque()));
                break;
            case VIEW_WHOLE_LOG_REQUEST:
                ViewLogRequestCommand viewLogRequest = JSONUtils.parseObject(
                        command.getBody(), ViewLogRequestCommand.class);
                String msg = LoggerUtils.readWholeFileContent(viewLogRequest.getPath());
                ViewLogResponseCommand viewLogResponse = new ViewLogResponseCommand(msg);
                channel.writeAndFlush(viewLogResponse.convert2Command(command.getOpaque()));
                break;
            case ROLL_VIEW_LOG_REQUEST:
                RollViewLogRequestCommand rollViewLogRequest = JSONUtils.parseObject(
                        command.getBody(), RollViewLogRequestCommand.class);
                List<String> lines = readPartFileContent(rollViewLogRequest.getPath(),
                        rollViewLogRequest.getSkipLineNum(), rollViewLogRequest.getLimit());
                StringBuilder builder = new StringBuilder();
                for (String line : lines) {
                    builder.append(line + "\r\n");
                }
                RollViewLogResponseCommand rollViewLogRequestResponse = new RollViewLogResponseCommand(builder.toString());
                channel.writeAndFlush(rollViewLogRequestResponse.convert2Command(command.getOpaque()));
                break;
            case REMOVE_TAK_LOG_REQUEST:
                RemoveTaskLogRequestCommand removeTaskLogRequest = JSONUtils.parseObject(
                        command.getBody(), RemoveTaskLogRequestCommand.class);

                String taskLogPath = removeTaskLogRequest.getPath();

                File taskLogFile = new File(taskLogPath);
                Boolean status = true;
                try {
                    if (taskLogFile.exists()) {
                        status = taskLogFile.delete();
                    }
                } catch (Exception e) {
                    status = false;
                }

                RemoveTaskLogResponseCommand removeTaskLogResponse = new RemoveTaskLogResponseCommand(status);
                channel.writeAndFlush(removeTaskLogResponse.convert2Command(command.getOpaque()));
                break;
            default:
                throw new IllegalArgumentException("unknown commandType");
        }
    }

    public ExecutorService getExecutor() {
        return this.executor;
    }

    /**
     * get files content bytes，for down load file
     *
     * @param filePath file path
     * @return byte array of file
     * @throws Exception exception
     */
    private byte[] getFileContentBytes(String filePath) {
        InputStream in = null;
        ByteArrayOutputStream bos = null;
        try {
            in = new FileInputStream(filePath);
            bos  = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            logger.error("get file bytes error",e);
        } finally {
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(in);
        }
        return new byte[0];
    }

    /**
     * read part file content，can skip any line and read some lines
     *
     * @param filePath file path
     * @param skipLine skip line
     * @param limit read lines limit
     * @return part file content
     */
    private List<String> readPartFileContent(String filePath,
                                            int skipLine,
                                            int limit) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
                return stream.skip(skipLine).limit(limit).collect(Collectors.toList());
            } catch (IOException e) {
                logger.error("read file error",e);
            }
        } else {
            logger.info("file path: {} not exists", filePath);
        }
        return Collections.emptyList();
    }

}
