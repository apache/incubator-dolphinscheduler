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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is Regex expression utils.
 */
public class RegexUtils {

    /**
     * check number regex expression
     */
    private static final String CHECK_NUMBER = "^-?\\d+(\\.\\d+)?$";

    private static final String LINUX_USERNAME_PATTERN = "[a-z_][a-z\\d_]{0,30}";

    private static final String WINDOWS_USERNAME_PATTERN = "[A-Z_][A-Z\\s\\d_]{0,30}";

    private RegexUtils() {
    }

    /**
     * check if the input is number
     *
     * @param str input
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile(CHECK_NUMBER);
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    /**
     * check if the input is a valid linux username
     * @param str input
     * @return boolean
     */
    public static boolean isValidLinuxUserName(String str) {
        Pattern pattern = Pattern.compile(LINUX_USERNAME_PATTERN);
        return pattern.matcher(str).matches();
    }

    /**
     * check if the input is a valid Windows username
     * @param str input
     * @return boolean
     */
    public static boolean isValidWindowUserName(String str) {
        Pattern pattern = Pattern.compile(WINDOWS_USERNAME_PATTERN);
        return pattern.matcher(str).matches();
    }

    public static String escapeNRT(String str) {
        // Logging should not be vulnerable to injection attacks: Replace pattern-breaking characters
        if (str != null && !str.isEmpty()) {
            return str.replaceAll("[\n|\r|\t]", "_");
        }
        return null;
    }

}
