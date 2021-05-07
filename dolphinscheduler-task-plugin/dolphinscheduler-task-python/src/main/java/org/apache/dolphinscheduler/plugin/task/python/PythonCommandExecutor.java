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

package org.apache.dolphinscheduler.plugin.task.python;

import org.apache.dolphinscheduler.plugin.task.api.AbstractCommandExecutor;
import org.apache.dolphinscheduler.spi.task.TaskRequest;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * python command executor
 */
public class PythonCommandExecutor extends AbstractCommandExecutor {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(PythonCommandExecutor.class);

    /**
     * python
     */
    public static final String PYTHON = "python";

    /**
     * constructor
     *
     * @param logHandler log handler
     * @param taskRequest TaskRequest
     * @param logger logger
     */
    public PythonCommandExecutor(Consumer<List<String>> logHandler,
                                 TaskRequest taskRequest,
                                 Logger logger) {
        super(logHandler, taskRequest, logger);
    }


    /**
     * build command file path
     *
     * @return command file path
     */
    @Override
    protected String buildCommandFilePath() {
        return String.format("%s/py_%s.command", taskRequest.getExecutePath(), taskRequest.getTaskAppId());
    }

    /**
     * create command file if not exists
     *
     * @param execCommand exec command
     * @param commandFile command file
     * @throws IOException io exception
     */
    @Override
    protected void createCommandFileIfNotExists(String execCommand, String commandFile) throws IOException {
        logger.info("tenantCode :{}, task dir:{}", taskRequest.getTenantCode(), taskRequest.getExecutePath());

        if (!Files.exists(Paths.get(commandFile))) {
            logger.info("generate command file:{}", commandFile);

            StringBuilder sb = new StringBuilder();
            sb.append("#-*- encoding=utf8 -*-\n");

            sb.append("\n\n");
            sb.append(execCommand);
            logger.info(sb.toString());

            // write data to file
            FileUtils.writeStringToFile(new File(commandFile),
                    sb.toString(),
                    StandardCharsets.UTF_8);
        }
    }

    /**
     * get command options
     *
     * @return command options list
     */
    @Override
    protected List<String> commandOptions() {
        // unbuffered binary stdout and stderr
        return Collections.singletonList("-u");
    }

    /**
     * get python home
     *
     * @return python home
     */
    @Override
    protected String commandInterpreter() {
        String pythonHome = getPythonHome(taskRequest.getEnvFile());
        if (StringUtils.isEmpty(pythonHome)) {
            return PYTHON;
        }
        return pythonHome;
    }

    /**
     * get the absolute path of the Python command
     * note :
     * common.properties
     * PYTHON_HOME configured under common.properties is Python absolute path, not PYTHON_HOME itself
     * <p>
     * for example :
     * your PYTHON_HOM is /opt/python3.7/
     * you must set PYTHON_HOME is /opt/python3.7/python under nder common.properties
     * dolphinscheduler.env.path file.
     *
     * @param envPath env path
     * @return python home
     */
    private static String getPythonHome(String envPath) {
        // BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(envPath)));) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(PythonConstants.PYTHON_HOME)) {
                    sb.append(line);
                    break;
                }
            }
            String result = sb.toString();
            if (StringUtils.isEmpty(result)) {
                return null;
            }
            String[] arrs = result.split(PythonConstants.EQUAL_SIGN);
            if (arrs.length == 2) {
                return arrs[1];
            }
        } catch (IOException e) {
            logger.error("read file failure", e);
        }
        return null;
    }

}
