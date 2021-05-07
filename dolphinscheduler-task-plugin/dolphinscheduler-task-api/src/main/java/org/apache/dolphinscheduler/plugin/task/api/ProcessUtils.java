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

import org.apache.dolphinscheduler.spi.task.TaskConstants;
import org.apache.dolphinscheduler.spi.task.TaskRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessUtils {

    private ProcessUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(ProcessUtils.class);

    /**
     * Initialization regularization, solve the problem of pre-compilation performance,
     * avoid the thread safety problem of multi-thread operation
     */
    private static final Pattern MACPATTERN = Pattern.compile("-[+|-]-\\s(\\d+)");

    /**
     * Expression of PID recognition in Windows scene
     */
    private static final Pattern WINDOWSATTERN = Pattern.compile("(\\d+)");

    private static final String LOCAL_PROCESS_EXEC = "jdk.lang.Process.allowAmbiguousCommands";

    /**
     * verification cmd bat.
     */
    private static final int VERIFICATION_CMD_BAT = 0;

    /**
     * verification legacy.
     */
    private static final int VERIFICATION_LEGACY = 2;

    /**
     * escape verification.
     */
    private static final char[][] ESCAPE_VERIFICATION = {{' ', '\t', '<', '>', '&', '|', '^'}, {' ', '\t', '<', '>'}, {' ', '\t'}};

    /**
     * verification win32.
     */
    private static final int VERIFICATION_WIN32 = 1;

    /**
     * Lazy Pattern.
     */
    private static class LazyPattern {
        /**
         * Escape-support version:
         * "(\")((?:\\\\\\1|.)+?)\\1|([^\\s\"]+)";
         */
        private static final Pattern PATTERN = Pattern.compile("[^\\s\"]+|\"[^\"]*\"");
    }

    /**
     * build command line characters.
     *
     * @param commandList command list
     * @return command
     */
    public static String buildCommandStr(List<String> commandList) {
        String cmdstr;
        String[] cmd = commandList.toArray(new String[0]);
        SecurityManager security = System.getSecurityManager();
        boolean allowAmbiguousCommands = isAllowAmbiguousCommands(security);
        if (allowAmbiguousCommands) {

            String executablePath = new File(cmd[0]).getPath();

            if (needsEscaping(VERIFICATION_LEGACY, executablePath)) {
                executablePath = quoteString(executablePath);
            }

            cmdstr = createCommandLine(
                    VERIFICATION_LEGACY, executablePath, cmd);
        } else {
            String executablePath;
            try {
                executablePath = getExecutablePath(cmd[0]);
            } catch (IllegalArgumentException e) {

                StringBuilder join = new StringBuilder();
                for (String s : cmd) {
                    join.append(s).append(' ');
                }

                cmd = getTokensFromCommand(join.toString());
                executablePath = getExecutablePath(cmd[0]);

                // Check new executable name once more
                if (security != null) {
                    security.checkExec(executablePath);
                }
            }

            cmdstr = createCommandLine(

                    isShellFile(executablePath) ? VERIFICATION_CMD_BAT : VERIFICATION_WIN32, quoteString(executablePath), cmd);
        }
        return cmdstr;
    }

    /**
     * whether is shell file.
     *
     * @param executablePath executable path
     * @return true if endsWith .CMD or .BAT
     */
    private static boolean isShellFile(String executablePath) {
        String upPath = executablePath.toUpperCase();
        return (upPath.endsWith(".CMD") || upPath.endsWith(".BAT"));
    }

    /**
     * create command line.
     *
     * @param verificationType verification type
     * @param executablePath executable path
     * @param cmd cmd
     * @return command line
     */
    private static String createCommandLine(int verificationType, final String executablePath, final String[] cmd) {
        StringBuilder cmdbuf = new StringBuilder(80);

        cmdbuf.append(executablePath);

        for (int i = 1; i < cmd.length; ++i) {
            cmdbuf.append(' ');
            String s = cmd[i];
            if (needsEscaping(verificationType, s)) {
                cmdbuf.append('"').append(s);

                if ((verificationType != VERIFICATION_CMD_BAT) && s.endsWith("\\")) {
                    cmdbuf.append('\\');
                }
                cmdbuf.append('"');
            } else {
                cmdbuf.append(s);
            }
        }
        return cmdbuf.toString();
    }

    /**
     * check is allow ambiguous commands
     *
     * @param security security manager
     * @return allow ambiguous command flag
     */
    private static boolean isAllowAmbiguousCommands(SecurityManager security) {
        boolean allowAmbiguousCommands = false;
        if (security == null) {
            allowAmbiguousCommands = true;
            String value = System.getProperty(LOCAL_PROCESS_EXEC);
            if (value != null) {
                allowAmbiguousCommands = !TaskConstants.STRING_FALSE.equalsIgnoreCase(value);
            }
        }
        return allowAmbiguousCommands;
    }

    /**
     * whether needs escaping.
     *
     * @param verificationType verification type
     * @param arg arg
     * @return boolean
     */
    private static boolean needsEscaping(int verificationType, String arg) {

        boolean argIsQuoted = isQuoted((verificationType == VERIFICATION_CMD_BAT), arg, "Argument has embedded quote, use the explicit CMD.EXE call.");

        if (!argIsQuoted) {
            char[] testEscape = ESCAPE_VERIFICATION[verificationType];
            for (char c : testEscape) {
                if (arg.indexOf(c) >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * whether is quoted.
     *
     * @param noQuotesInside no quotes inside
     * @param arg arg
     * @param errorMessage error message
     * @return boolean
     */
    private static boolean isQuoted(boolean noQuotesInside, String arg, String errorMessage) {
        int lastPos = arg.length() - 1;
        if (lastPos >= 1 && arg.charAt(0) == '"' && arg.charAt(lastPos) == '"') {
            // The argument has already been quoted.
            if (noQuotesInside && arg.indexOf('"', 1) != lastPos) {
                // There is ["] inside.
                throw new IllegalArgumentException(errorMessage);
            }
            return true;
        }
        if (noQuotesInside && arg.indexOf('"') >= 0) {
            // There is ["] inside.
            throw new IllegalArgumentException(errorMessage);
        }
        return false;
    }

    /**
     * quote string.
     *
     * @param arg argument
     * @return format arg
     */
    private static String quoteString(String arg) {
        return '"' + arg + '"';
    }

    /**
     * get executable path.
     *
     * @param path path
     * @return executable path
     */
    private static String getExecutablePath(String path) {
        boolean pathIsQuoted = isQuoted(true, path, "Executable name has embedded quote, split the arguments");

        File fileToRun = new File(pathIsQuoted ? path.substring(1, path.length() - 1) : path);
        return fileToRun.getPath();
    }

    /**
     * get tokens from command.
     *
     * @param command command
     * @return token string array
     */
    private static String[] getTokensFromCommand(String command) {
        ArrayList<String> matchList = new ArrayList<>(8);
        Matcher regexMatcher = LazyPattern.PATTERN.matcher(command);
        while (regexMatcher.find()) {
            matchList.add(regexMatcher.group());
        }
        return matchList.toArray(new String[0]);
    }

    /**
     * kill tasks according to different task types.
     */
    public static void kill(TaskRequest request) {
        try {
            int processId = request.getProcessId();
            if (processId == 0) {
                logger.error("process kill failed, process id :{}, task id:{}",
                        processId, request.getTaskInstanceId());
                return;
            }

            String cmd = String.format("kill -9 %s", getPidsStr(processId));
            cmd = OSUtils.getSudoCmd(request.getTenantCode(), cmd);
            logger.info("process id:{}, cmd:{}", processId, cmd);

            OSUtils.exeCmd(cmd);

            // find log and kill yarn job
            //  killYarnJob(request);

        } catch (Exception e) {
            logger.error("kill task failed", e);
        }
    }

    /**
     * get pids str.
     *
     * @param processId process id
     * @return pids pid String
     * @throws Exception exception
     */
    public static String getPidsStr(int processId) throws Exception {
        StringBuilder sb = new StringBuilder();
        Matcher mat = null;
        // pstree pid get sub pids
        if (OSUtils.isMacOS()) {
            String pids = OSUtils.exeCmd(String.format("%s -sp %d", TaskConstants.PSTREE, processId));
            if (null != pids) {
                mat = MACPATTERN.matcher(pids);
            }
        } else {
            String pids = OSUtils.exeCmd(String.format("%s -p %d", TaskConstants.PSTREE, processId));
            mat = WINDOWSATTERN.matcher(pids);
        }

        if (null != mat) {
            while (mat.find()) {
                sb.append(mat.group(1)).append(" ");
            }
        }

        return sb.toString().trim();
    }

}
