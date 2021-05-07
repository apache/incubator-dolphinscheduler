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
import java.util.Map;

/**
 * shell command executor.
 *
 * <code>ShellExecutor</code> should be used in cases where the output
 * of the command needs no explicit parsing and where the command, working
 * directory and the environment remains unchanged. The output of the command
 * is stored as-is and is expected to be small.
 */
public class ShellExecutor extends AbstractShell {

    private String[] command;
    private StringBuilder output;

    public ShellExecutor(String... execString) {
        this(execString, null);
    }

    public ShellExecutor(String[] execString, File dir) {
        this(execString, dir, null);
    }

    public ShellExecutor(String[] execString, File dir,
                         Map<String, String> env) {
        this(execString, dir, env, 0L);
    }

    /**
     * Create a new instance of the ShellExecutor to execute a command.
     *
     * @param execString The command to execute with arguments
     * @param dir If not-null, specifies the directory which should be set
     * as the current working directory for the command.
     * If null, the current working directory is not modified.
     * @param env If not-null, environment of the command will include the
     * key-value pairs specified in the map. If null, the current
     * environment is not modified.
     * @param timeout Specifies the time in milliseconds, after which the
     * command will be killed and the status marked as timedout.
     * If 0, the command will not be timed out.
     */
    public ShellExecutor(String[] execString, File dir,
                         Map<String, String> env, long timeout) {
        command = execString.clone();
        if (dir != null) {
            setWorkingDirectory(dir);
        }
        if (env != null) {
            setEnvironment(env);
        }
        timeOutInterval = timeout;
    }

    /**
     * Static method to execute a shell command.
     * Covers most of the simple cases without requiring the user to implement
     * the <code>AbstractShell</code> interface.
     *
     * @param cmd shell command to execute.
     * @return the output of the executed command.
     * @throws IOException errors
     */
    public static String execCommand(String... cmd) throws IOException {
        return execCommand(null, cmd, 0L);
    }

    /**
     * Static method to execute a shell command.
     * Covers most of the simple cases without requiring the user to implement
     * the <code>AbstractShell</code> interface.
     *
     * @param env the map of environment key=value
     * @param cmd shell command to execute.
     * @param timeout time in milliseconds after which script should be marked timeout
     * @return the output of the executed command.
     * @throws IOException errors
     */
    public static String execCommand(Map<String, String> env, String[] cmd,
                                     long timeout) throws IOException {
        ShellExecutor exec = new ShellExecutor(cmd, null, env,
                timeout);
        exec.execute();
        return exec.getOutput();
    }

    /**
     * Static method to execute a shell command.
     * Covers most of the simple cases without requiring the user to implement
     * the <code>AbstractShell</code> interface.
     *
     * @param env the map of environment key=value
     * @param cmd shell command to execute.
     * @return the output of the executed command.
     * @throws IOException errors
     */
    public static String execCommand(Map<String, String> env, String... cmd)
            throws IOException {
        return execCommand(env, cmd, 0L);
    }

    /**
     * Execute the shell command
     *
     * @throws IOException errors
     */
    public void execute() throws IOException {
        this.run();
    }

    @Override
    protected String[] getExecString() {
        return command;
    }

    @Override
    protected void parseExecResult(BufferedReader lines) throws IOException {
        output = new StringBuilder();
        char[] buf = new char[1024];
        int nRead;
        String line = "";
        while ((nRead = lines.read(buf, 0, buf.length)) > 0) {
            line = new String(buf, 0, nRead);
            output.append(line);
        }
    }

    /**
     * @return the output of the shell command
     */
    public String getOutput() {
        return (output == null) ? "" : output.toString();
    }


    /**
     * Returns the commands of this instance.
     * Arguments with spaces in are presented with quotes round; other
     * arguments are presented raw
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String[] args = getExecString();
        for (String s : args) {
            if (s.indexOf(' ') >= 0) {
                builder.append('"').append(s).append('"');
            } else {
                builder.append(s);
            }
            builder.append(' ');
        }
        return builder.toString();
    }
}
