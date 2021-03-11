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

package org.apache.dolphinscheduler.server.worker.runner;

import org.apache.dolphinscheduler.common.thread.Stopper;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.server.worker.cache.ResponseCache;
import org.apache.dolphinscheduler.server.worker.processor.TaskCallbackService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Retry Report Task Status Thread
 */
@Component
public class RetryReportTaskStatusThread implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(RetryReportTaskStatusThread.class);

    /**
     * every 5 minutes
     */
    private static long RETRY_REPORT_TASK_STATUS_INTERVAL = 5 * 60 * 1000L;
    /**
     *  task callback service
     */
    private final TaskCallbackService taskCallbackService;

    public void start(){
        Thread thread = new Thread(this,"RetryReportTaskStatusThread");
        thread.start();
    }

    public RetryReportTaskStatusThread(){
        this.taskCallbackService = SpringApplicationContext.getBean(TaskCallbackService.class);
    }

    /**
     * retry ack/response
     */
    @Override
    public void run() {
        ResponseCache responseCache = ResponseCache.get();

        while (Stopper.isRunning()){

            // sleep 5 minutes
            ThreadUtils.sleep(RETRY_REPORT_TASK_STATUS_INTERVAL);

            try {
                if (!responseCache.getAckCache().isEmpty()){
                    Map<Integer,Command> ackCache =  responseCache.getAckCache();
                    for (Map.Entry<Integer, Command> entry : ackCache.entrySet()){
                        Integer taskInstanceId = entry.getKey();
                        Command ackCommand = entry.getValue();
                        taskCallbackService.sendAck(taskInstanceId,ackCommand);
                    }
                }

                if (!responseCache.getResponseCache().isEmpty()){
                    Map<Integer,Command> responseCache =  responseCache.getResponseCache();
                    for (Map.Entry<Integer, Command> entry : responseCache.entrySet()){
                        Integer taskInstanceId = entry.getKey();
                        Command responseCommand = entry.getValue();
                        taskCallbackService.sendResult(taskInstanceId,responseCommand);
                    }
                }
            }catch (Exception e){
                logger.warn("retry report task status error", e);
            }
        }
    }
}
