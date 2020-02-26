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
package org.apache.dolphinscheduler.server.worker.task;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.server.utils.ProcessUtils;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.slf4j.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.dolphinscheduler.common.Constants.*;

/**
 * abstract command executor
 */
public abstract class AbstractCommandExecutor {
    /**
     * rules for extracting application ID
     */
    protected static final Pattern APPLICATION_REGEX = Pattern.compile(Constants.APPLICATION_REGEX);

    /**
     *  process
     */
    private Process process;

    /**
     *  log handler
     */
    protected Consumer<List<String>> logHandler;

    /**
     *  task dir
     */
    protected final String taskDir;

    /**
     *  task appId
     */
    protected final String taskAppId;

    /**
     *  task appId
     */
    protected final int taskInstanceId;

    /**
     *  tenant code , execute task linux user
     */
    protected final String tenantCode;

    /**
     *  env file
     */
    protected final String envFile;

    /**
     *  start time
     */
    protected final Date startTime;

    /**
     *  timeout
     */
    protected int timeout;

    /**
     *  logger
     */
    protected Logger logger;

    /**
     *  log list
     */
    protected final List<String> logBuffer;

    /**
     *  log path
     */
    private String logPath;

    /**
     *  execute path
     */
    private String executePath;

    public AbstractCommandExecutor(Consumer<List<String>> logHandler,
                                   String taskDir,
                                   String taskAppId,
                                   Integer taskInstanceId,
                                   String tenantCode, String envFile,
                                   Date startTime, int timeout, String logPath,String executePath,Logger logger){
        this.logHandler = logHandler;
        this.taskDir = taskDir;
        this.taskAppId = taskAppId;
        this.taskInstanceId = taskInstanceId;
        this.tenantCode = tenantCode;
        this.envFile = envFile;
        this.startTime = startTime;
        this.timeout = timeout;
        this.logPath = logPath;
        this.executePath = executePath;
        this.logger = logger;
        this.logBuffer = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * build process
     *
     * @param commandFile command file
     * @throws IOException IO Exception
     */
    private void buildProcess(String commandFile) throws IOException {
        //init process builder
        ProcessBuilder processBuilder = new ProcessBuilder();
        // setting up a working directory
        processBuilder.directory(new File(taskDir));
        // merge error information to standard output stream
        processBuilder.redirectErrorStream(true);
        // setting up user to run commands
        List<String> command = new LinkedList<>();
        command.add("sudo");
        command.add("-u");
        command.add(tenantCode);
        command.add(commandInterpreter());
        command.addAll(commandOptions());
        command.add(commandFile);
        processBuilder.command(command);

        process = processBuilder.start();

        // print command
        printCommand(processBuilder);
    }

    /**
     * task specific execution logic
     * @param execCommand execCommand
     * @return CommandExecuteResult
     * @throws Exception
     */
    public CommandExecuteResult run(String execCommand) throws Exception{

        CommandExecuteResult result = new CommandExecuteResult();


        if (StringUtils.isEmpty(execCommand)) {
            return result;
        }

        String commandFilePath = buildCommandFilePath();

        // create command file if not exists
        createCommandFileIfNotExists(execCommand, commandFilePath);

        //build process
        buildProcess(commandFilePath);

        // parse process output
        parseProcessOutput(process);


        Integer processId = getProcessId(process);

        result.setProcessId(processId);

        // print process id
        logger.info("process start, process id is: {}", processId);

        // if timeout occurs, exit directly
        long remainTime = getRemaintime();

        // waiting for the run to finish
        boolean status = process.waitFor(remainTime, TimeUnit.SECONDS);

        // SHELL task state
        result.setExitStatusCode(process.exitValue());

        logger.info("process has exited, execute path:{}, processId:{} ,exitStatusCode:{}",
                taskDir,
                processId
                , result.getExitStatusCode());

        // if SHELL task exit
        if (status) {
            // set appIds
            List<String> appIds = getAppIds(logPath);
            result.setAppIds(String.join(Constants.COMMA, appIds));

            // if yarn task , yarn state is final state
            result.setExitStatusCode(isSuccessOfYarnState(appIds) ? EXIT_CODE_SUCCESS : EXIT_CODE_FAILURE);
        } else {
            logger.error("process has failure , exitStatusCode : {} , ready to kill ...", result.getExitStatusCode());
            TaskInstance taskInstance = new TaskInstance();
            taskInstance.setPid(processId);
            taskInstance.setHost(OSUtils.getHost());
            taskInstance.setLogPath(logPath);
            taskInstance.setExecutePath(executePath);

            ProcessInstance processInstance = new ProcessInstance();
            processInstance.setTenantCode(tenantCode);
            taskInstance.setProcessInstance(processInstance);

            ProcessUtils.kill(taskInstance);
            result.setExitStatusCode(EXIT_CODE_FAILURE);
        }
        return result;
    }


    /**
     * cancel application
     * @throws Exception exception
     */
    public void cancelApplication() throws Exception {
        if (process == null) {
            return;
        }

        // clear log
        clear();

        int processId = getProcessId(process);

        logger.info("cancel process: {}", processId);

        // kill , waiting for completion
        boolean killed = softKill(processId);

        if (!killed) {
            // hard kill
            hardKill(processId);

            // destory
            process.destroy();

            process = null;
        }
    }

    /**
     * soft kill
     * @param processId process id
     * @return process is alive
     * @throws InterruptedException interrupted exception
     */
    private boolean softKill(int processId) {

        if (processId != 0 && process.isAlive()) {
            try {
                // sudo -u user command to run command
                String cmd = String.format("sudo kill %d", processId);

                logger.info("soft kill task:{}, process id:{}, cmd:{}", taskAppId, processId, cmd);

                Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                logger.info("kill attempt failed", e);
            }
        }

        return process.isAlive();
    }

    /**
     * hard kill
     * @param processId process id
     */
    private void hardKill(int processId) {
        if (processId != 0 && process.isAlive()) {
            try {
                String cmd = String.format("sudo kill -9 %d", processId);

                logger.info("hard kill task:{}, process id:{}, cmd:{}", taskAppId, processId, cmd);

                Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                logger.error("kill attempt failed ", e);
            }
        }
    }

    /**
     * print command
     * @param processBuilder process builder
     */
    private void printCommand(ProcessBuilder processBuilder) {
        String cmdStr;

        try {
            cmdStr = ProcessUtils.buildCommandStr(processBuilder.command());
            logger.info("task run command:\n{}", cmdStr);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * clear
     */
    private void clear() {
        if (!logBuffer.isEmpty()) {
            // log handle
            logHandler.accept(logBuffer);

            logBuffer.clear();
        }
    }

    /**
     * get the standard output of the process
     * @param process process
     */
    private void parseProcessOutput(Process process) {
        String threadLoggerInfoName = String.format(LoggerUtils.TASK_LOGGER_THREAD_NAME + "-%s", taskAppId);
        ExecutorService parseProcessOutputExecutorService = ThreadUtils.newDaemonSingleThreadExecutor(threadLoggerInfoName);
        parseProcessOutputExecutorService.submit(new Runnable(){
            @Override
            public void run() {
                BufferedReader inReader = null;

                try {
                    inReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;

                    long lastFlushTime = System.currentTimeMillis();

                    while ((line = inReader.readLine()) != null) {
                        logBuffer.add(line);
                        lastFlushTime = flush(lastFlushTime);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                } finally {
                    clear();
                    close(inReader);
                }
            }
        });
        parseProcessOutputExecutorService.shutdown();
    }

    /**
     * check yarn state
     *
     * @param appIds application id list
     * @return is success of yarn task state
     */
    public boolean isSuccessOfYarnState(List<String> appIds) {
        boolean result = true;
        try {
            for (String appId : appIds) {
                while(Stopper.isRunning()){
                    ExecutionStatus applicationStatus = HadoopUtils.getInstance().getApplicationStatus(appId);
                    logger.info("appId:{}, final state:{}",appId,applicationStatus.name());
                    if (applicationStatus.equals(ExecutionStatus.FAILURE) ||
                            applicationStatus.equals(ExecutionStatus.KILL)) {
                        return false;
                    }

                    if (applicationStatus.equals(ExecutionStatus.SUCCESS)){
                        break;
                    }
                    Thread.sleep(Constants.SLEEP_TIME_MILLIS);
                }
            }
        } catch (Exception e) {
            logger.error(String.format("yarn applications: %s  status failed ", appIds.toString()),e);
            result = false;
        }
        return result;

    }

    public int getProcessId() {
        return getProcessId(process);
    }

    /**
     * get app links
     *
     * @param logPath log path
     * @return app id list
     */
    private List<String> getAppIds(String logPath) {
        List<String> logs = convertFile2List(logPath);

        List<String> appIds = new ArrayList<>();
        /**
         * analysis log，get submited yarn application id
         */
        for (String log : logs) {
            String appId = findAppId(log);
            if (StringUtils.isNotEmpty(appId) && !appIds.contains(appId)) {
                logger.info("find app id: {}", appId);
                appIds.add(appId);
            }
        }
        return appIds;
    }

    /**
     * convert file to list
     * @param filename file name
     * @return line list
     */
    private List<String> convertFile2List(String filename) {
        List lineList = new ArrayList<String>(100);
        File file=new File(filename);

        if (!file.exists()){
            return lineList;
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8));
            String line = null;
            while ((line = br.readLine()) != null) {
                lineList.add(line);
            }
        } catch (Exception e) {
            logger.error(String.format("read file: %s failed : ",filename),e);
        } finally {
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(),e);
                }
            }

        }
        return lineList;
    }

    /**
     * find app id
     * @param line line
     * @return appid
     */
    private String findAppId(String line) {
        Matcher matcher = APPLICATION_REGEX.matcher(line);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }


    /**
     * get remain time（s）
     *
     * @return remain time
     */
    private long getRemaintime() {
        long usedTime = (System.currentTimeMillis() - startTime.getTime()) / 1000;
        long remainTime = timeout - usedTime;

        if (remainTime < 0) {
            throw new RuntimeException("task execution time out");
        }

        return remainTime;
    }

    /**
     * get process id
     *
     * @param process process
     * @return process id
     */
    private int getProcessId(Process process) {
        int processId = 0;

        try {
            Field f = process.getClass().getDeclaredField(Constants.PID);
            f.setAccessible(true);

            processId = f.getInt(process);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }

        return processId;
    }

    /**
     * when log buffer siz or flush time reach condition , then flush
     *
     * @param lastFlushTime  last flush time
     * @return last flush time
     */
    private long flush(long lastFlushTime) {
        long now = System.currentTimeMillis();

        /**
         * when log buffer siz or flush time reach condition , then flush
         */
        if (logBuffer.size() >= Constants.DEFAULT_LOG_ROWS_NUM || now - lastFlushTime > Constants.DEFAULT_LOG_FLUSH_INTERVAL) {
            lastFlushTime = now;
            /** log handle */
            logHandler.accept(logBuffer);

            logBuffer.clear();
        }
        return lastFlushTime;
    }

    /**
     * close buffer reader
     *
     * @param inReader in reader
     */
    private void close(BufferedReader inReader) {
        if (inReader != null) {
            try {
                inReader.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    protected List<String> commandOptions() {
        return Collections.emptyList();
    }
    protected abstract String buildCommandFilePath();
    protected abstract String commandInterpreter();
    protected abstract void createCommandFileIfNotExists(String execCommand, String commandFile) throws IOException;
}
