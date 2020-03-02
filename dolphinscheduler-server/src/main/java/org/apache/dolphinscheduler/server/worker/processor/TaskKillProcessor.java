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

package org.apache.dolphinscheduler.server.worker.processor;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.Preconditions;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.KillTaskRequestCommand;
import org.apache.dolphinscheduler.remote.command.KillTaskResponseCommand;
import org.apache.dolphinscheduler.remote.processor.NettyRequestProcessor;
import org.apache.dolphinscheduler.remote.utils.FastJsonSerializer;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.ProcessUtils;
import org.apache.dolphinscheduler.server.worker.cache.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.server.worker.cache.impl.TaskExecutionContextCacheManagerImpl;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.log.LogClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *  task kill processor
 */
public class TaskKillProcessor implements NettyRequestProcessor {

    private final Logger logger = LoggerFactory.getLogger(TaskKillProcessor.class);

    /**
     *  worker config
     */
    private final WorkerConfig workerConfig;

    /**
     *  task callback service
     */
    private final KillTaskCallbackService killTaskCallbackService;

    /**
     * taskExecutionContextCacheManager
     */
    private TaskExecutionContextCacheManager taskExecutionContextCacheManager;

    /**
     * appIds
     */
    private List<String> appIds;


    public TaskKillProcessor(){
        this.killTaskCallbackService = new KillTaskCallbackService();
        this.workerConfig = SpringApplicationContext.getBean(WorkerConfig.class);
        this.taskExecutionContextCacheManager = SpringApplicationContext.getBean(TaskExecutionContextCacheManagerImpl.class);
    }

    /**
     * kill task logic
     *
     * @param context context
     * @return execute result
     */
    private Boolean doKill(TaskExecutionContext context){
        try {
            TaskExecutionContext taskExecutionContext = taskExecutionContextCacheManager.getByTaskInstanceId(context.getTaskInstanceId());

            Integer processId = taskExecutionContext.getProcessId();

            if (processId == null || processId.equals(0)){
                logger.error("process kill failed, process id :{}, task id:{}", processId, taskExecutionContext.getTaskInstanceId());
                return false;
            }


            String cmd = String.format("sudo kill -9 %s", ProcessUtils.getPidsStr(taskExecutionContext.getProcessId()));

            logger.info("process id:{}, cmd:{}", taskExecutionContext.getProcessId(), cmd);

            OSUtils.exeCmd(cmd);

            // find log and kill yarn job
            killYarnJob(taskExecutionContext.getHost(), taskExecutionContext.getLogPath(), taskExecutionContext.getExecutePath(), taskExecutionContext.getTenantCode());

            return true;
        } catch (Exception e) {
            logger.error("kill task failed", e);
            return false;
        }
    }

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandType.KILL_TASK_REQUEST == command.getType(), String.format("invalid command type : %s", command.getType()));
        KillTaskRequestCommand killTaskRequestCommand = FastJsonSerializer.deserialize(command.getBody(), KillTaskRequestCommand.class);
        logger.info("received command : {}", killTaskRequestCommand);


        String contextJson = killTaskRequestCommand.getTaskExecutionContext();

        TaskExecutionContext taskExecutionContext = JSONObject.parseObject(contextJson, TaskExecutionContext.class);

        Boolean killStatus = doKill(taskExecutionContext);

        KillTaskResponseCommand killTaskResponseCommand = buildKillTaskResponseCommand(taskExecutionContext,killStatus);
        killTaskCallbackService.sendKillResult(killTaskResponseCommand.getTaskInstanceId(),killTaskResponseCommand);
    }

    /**
     * build KillTaskResponseCommand
     *
     * @param taskExecutionContext taskExecutionContext
     * @param killStatus killStatus
     * @return build KillTaskResponseCommand
     */
    private KillTaskResponseCommand buildKillTaskResponseCommand(TaskExecutionContext taskExecutionContext,
                                                                 Boolean killStatus) {
        KillTaskResponseCommand killTaskResponseCommand = new KillTaskResponseCommand();
        killTaskResponseCommand.setTaskInstanceId(taskExecutionContext.getTaskInstanceId());
        killTaskResponseCommand.setHost(taskExecutionContext.getHost());
        killTaskResponseCommand.setStatus(killStatus ? ExecutionStatus.SUCCESS.getCode() : ExecutionStatus.FAILURE.getCode());
        killTaskResponseCommand.setProcessId(taskExecutionContext.getProcessId());
        killTaskResponseCommand.setAppIds(appIds);

        return killTaskResponseCommand;
    }

    /**
     *  kill yarn job
     *
     * @param host host
     * @param logPath logPath
     * @param executePath executePath
     * @param tenantCode tenantCode
     */
    public void killYarnJob(String host, String logPath, String executePath, String tenantCode) {
        List<String> appIds = null;
        try {
            Thread.sleep(Constants.SLEEP_TIME_MILLIS);
            LogClientService logClient = null;
            String log = null;
            try {
                logClient = new LogClientService();
                log = logClient.viewLog(host, Constants.RPC_PORT, logPath);
            } finally {
                if(logClient != null){
                    logClient.close();
                }
            }
            if (StringUtils.isNotEmpty(log)) {
                appIds = LoggerUtils.getAppIds(log, logger);
                if (StringUtils.isEmpty(executePath)) {
                    logger.error("task instance work dir is empty");
                    throw new RuntimeException("task instance work dir is empty");
                }
                if (appIds.size() > 0) {
                    ProcessUtils.cancelApplication(appIds, logger, tenantCode, executePath);
                }
            }

        } catch (Exception e) {
            logger.error("kill yarn job failure",e);
        }
    }

}
