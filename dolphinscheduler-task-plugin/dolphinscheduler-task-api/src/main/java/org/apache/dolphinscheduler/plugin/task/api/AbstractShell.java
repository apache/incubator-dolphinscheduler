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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for running a Unix command.
 *
 * <code>AbstractShell</code> can be used to run unix commands like <code>du</code> or
 * <code>df</code>. It also offers facilities to gate commands by
 * time-intervals.
 */
public abstract class AbstractShell {

    private static final Logger logger = LoggerFactory.getLogger(AbstractShell.class);

    /**
     * Time after which the executing script would be timedout
     */
    protected long timeOutInterval = 0L;
    /**
     * If or not script timed out
     */
    private AtomicBoolean timedOut;

    /**
     * refresh interval in msec
     */
    private long interval;

    /**
     * last time the command was performed
     */
    private long lastTime;

    /**
     * env for the command execution
     */
    private Map<String, String> environment;

    private File dir;

    /**
     * sub process used to execute the command
     */
    private Process process;
    private int exitCode;

    /**
     * If or not script finished executing
     */
    private AtomicBoolean completed;

    AbstractShell() {
        this(0L);
    }

    /**
     * @param interval the minimum duration to wait before re-executing the
     * command.
     */
    AbstractShell(long interval) {
        this.interval = interval;
        this.lastTime = (interval < 0) ? 0 : -interval;
    }

    /**
     * set the environment for the command
     *
     * @param env Mapping of environment variables
     */
    protected void setEnvironment(Map<String, String> env) {
        this.environment = env;
    }

    /**
     * set the working directory
     *
     * @param dir The directory where the command would be executed
     */
    protected void setWorkingDirectory(File dir) {
        this.dir = dir;
    }

    /**
     * check to see if a command needs to be executed and execute if needed
     *
     * @throws IOException errors
     */
    protected void run() throws IOException {
        if (lastTime + interval > System.currentTimeMillis()) {
            return;
        }
        // reset for next run
        exitCode = 0;
        runCommand();
    }

    /**
     * Run a command   actual work
     */
    private void runCommand() throws IOException {
        ProcessBuilder builder = new ProcessBuilder(getExecString());
        Timer timeOutTimer = null;
        ShellTimeoutTimerTask timeoutTimerTask;
        timedOut = new AtomicBoolean(false);
        completed = new AtomicBoolean(false);

        if (environment != null) {
            builder.environment().putAll(this.environment);
        }
        if (dir != null) {
            builder.directory(this.dir);
        }

        process = builder.start();
        ProcessContainer.putProcess(process);

        if (timeOutInterval > 0) {
            timeOutTimer = new Timer();
            timeoutTimerTask = new ShellTimeoutTimerTask(
                    this);
            //One time scheduling.
            timeOutTimer.schedule(timeoutTimerTask, timeOutInterval);
        }
        final BufferedReader errReader =
                new BufferedReader(
                        new InputStreamReader(process.getErrorStream()));
        BufferedReader inReader =
                new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
        final StringBuilder errMsg = new StringBuilder();

        // read error and input streams as this would free up the buffers
        // free the error stream buffer
        Thread errThread = new Thread() {
            @Override
            public void run() {
                try {
                    String line = errReader.readLine();
                    while ((line != null) && !isInterrupted()) {
                        errMsg.append(line);
                        errMsg.append(System.getProperty("line.separator"));
                        line = errReader.readLine();
                    }
                } catch (IOException ioe) {
                    logger.warn("Error reading the error stream", ioe);
                }
            }
        };
        Thread inThread = new Thread() {
            @Override
            public void run() {
                try {
                    parseExecResult(inReader);
                } catch (IOException ioe) {
                    logger.warn("Error reading the in stream", ioe);
                }
                super.run();
            }
        };
        try {
            errThread.start();
            inThread.start();
        } catch (IllegalStateException e) {
            logger.error(" read error and input streams start error", e);
        }
        try {
            // parse the output
            exitCode = process.waitFor();
            try {
                // make sure that the error and in thread exits
                errThread.join();
                inThread.join();
            } catch (InterruptedException ie) {
                logger.warn("Interrupted while reading the error and in stream", ie);
            }
            completed.compareAndSet(false, true);
            //the timeout thread handling
            //taken care in finally block
            if (exitCode != 0 || errMsg.length() > 0) {
                throw new ExitCodeException(exitCode, errMsg.toString());
            }
        } catch (InterruptedException ie) {
            throw new IOException(ie.toString());
        } finally {
            if ((timeOutTimer != null) && !timedOut.get()) {
                timeOutTimer.cancel();
            }
            // close the input stream
            try {
                inReader.close();
            } catch (IOException ioe) {
                logger.warn("Error while closing the input stream", ioe);
            }
            if (!completed.get()) {
                errThread.interrupt();
            }
            try {
                errReader.close();
            } catch (IOException ioe) {
                logger.warn("Error while closing the error stream", ioe);
            }
            ProcessContainer.removeProcess(process);
            process.destroy();
            lastTime = System.currentTimeMillis();
        }
    }

    /**
     * @return an array containing the command name and its parameters
     */
    protected abstract String[] getExecString();

    /**
     * Parse the execution result
     *
     * @param lines lines
     * @throws IOException errors
     */
    protected abstract void parseExecResult(BufferedReader lines)
            throws IOException;

    /**
     * get the current sub-process executing the given command
     *
     * @return process executing the command
     */
    public Process getProcess() {
        return process;
    }

    /**
     * get the exit code
     *
     * @return the exit code of the process
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Set if the command has timed out.
     */
    private void setTimedOut() {
        this.timedOut.set(true);
    }

    /**
     * Timer which is used to timeout scripts spawned off by shell.
     */
    private static class ShellTimeoutTimerTask extends TimerTask {

        private AbstractShell shell;

        public ShellTimeoutTimerTask(AbstractShell shell) {
            this.shell = shell;
        }

        @Override
        public void run() {
            Process p = shell.getProcess();
            try {
                p.exitValue();
            } catch (Exception e) {
                //Process has not terminated.
                //So check if it has completed
                //if not just destroy it.
                if (p != null && !shell.completed.get()) {
                    shell.setTimedOut();
                    p.destroy();
                }
            }
        }
    }

    /**
     * This is an IOException with exit code added.
     */
    public static class ExitCodeException extends IOException {
        private final int exitCode;

        public ExitCodeException(int exitCode, String message) {
            super(message);
            this.exitCode = exitCode;
        }

        public int getExitCode() {
            return exitCode;
        }
    }

    /**
     * process manage container
     */
    public static class ProcessContainer extends ConcurrentHashMap<Integer, Process> {
        private static final ProcessContainer container = new ProcessContainer();

        private ProcessContainer() {
            super();
        }

        public static final ProcessContainer getInstance() {
            return container;
        }

        public static void putProcess(Process process) {
            getInstance().put(process.hashCode(), process);
        }

        public static int processSize() {
            return getInstance().size();
        }

        public static void removeProcess(Process process) {
            getInstance().remove(process.hashCode());
        }

        public static void destroyAllProcess() {
            Set<Entry<Integer, Process>> set = getInstance().entrySet();
            for (Entry<Integer, Process> entry : set) {
                try {
                    entry.getValue().destroy();
                } catch (Exception e) {
                    logger.error("Destroy All Processes error", e);
                }
            }

            logger.info("close " + set.size() + " executing process tasks");
        }
    }
}
