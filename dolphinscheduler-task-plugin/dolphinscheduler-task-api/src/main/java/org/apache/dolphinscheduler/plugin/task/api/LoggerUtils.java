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

package org.apache.dolphinscheduler.plugin.task.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

/**
 * logger utils
 */
public class LoggerUtils {

    private static final String APPLICATION_REGEX_NAME = "application_\\d+_\\d+";

    private LoggerUtils() {
        throw new UnsupportedOperationException("Construct LoggerUtils");
    }

    /**
     * rules for extracting application ID
     */
    private static final Pattern APPLICATION_REGEX = Pattern.compile(APPLICATION_REGEX_NAME);

    /**
     * Task Logger's prefix
     */
    public static final String TASK_LOGGER_INFO_PREFIX = "TASK";

    /**
     * Task Logger Thread's name
     */
    public static final String TASK_LOGGER_THREAD_NAME = "TaskLogInfo";

    /**
     * Task Logger Thread's name
     */
    public static final String TASK_APPID_LOG_FORMAT = "[taskAppId=";

    /**
     * build job id
     *
     * @param affix Task Logger's prefix
     * @param processDefId process define id
     * @param processInstId process instance id
     * @param taskId task id
     * @return task id format
     */
    public static String buildTaskId(String affix,
                                     int processDefId,
                                     int processInstId,
                                     int taskId) {
        // - [taskAppId=TASK_79_4084_15210]
        return String.format(" - %s%s-%s-%s-%s]", TASK_APPID_LOG_FORMAT, affix,
                processDefId,
                processInstId,
                taskId);
    }

    /**
     * processing log
     * get yarn application id list
     *
     * @param log log content
     * @param logger logger
     * @return app id list
     */
    public static List<String> getAppIds(String log, Logger logger) {

        List<String> appIds = new ArrayList<>();

        Matcher matcher = APPLICATION_REGEX.matcher(log);

        // analyse logs to get all submit yarn application id
        while (matcher.find()) {
            String appId = matcher.group();
            if (!appIds.contains(appId)) {
                logger.info("find app id: {}", appId);
                appIds.add(appId);
            }
        }
        return appIds;
    }

    public static void logError(Optional<Logger> optionalLogger
            , String error) {
        optionalLogger.ifPresent((Logger logger) -> logger.error(error));
    }

    public static void logError(Optional<Logger> optionalLogger
            , Throwable e) {
        optionalLogger.ifPresent((Logger logger) -> logger.error(e.getMessage(), e));
    }

    public static void logError(Optional<Logger> optionalLogger
            , String error, Throwable e) {
        optionalLogger.ifPresent((Logger logger) -> logger.error(error, e));
    }

    public static void logInfo(Optional<Logger> optionalLogger
            , String info) {
        optionalLogger.ifPresent((Logger logger) -> logger.info(info));
    }
}