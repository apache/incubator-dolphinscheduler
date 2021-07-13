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

import org.junit.Assert;
import org.junit.Test;

/**
 * RegexUtils test case
 */
public class RegexUtilsTest {

    @Test
    public void testIsNumeric() {
        String num1 = "123467854678";
        boolean numeric = RegexUtils.isNumeric(num1);
        Assert.assertTrue(numeric);

        String num2 = "0.0.01";
        boolean numeric2 = RegexUtils.isNumeric(num2);
        Assert.assertFalse(numeric2);
    }

    @Test
    public void testIsValidLinuxUserName() {
        String name1 = "10000";
        Assert.assertFalse(RegexUtils.isValidLinuxUserName(name1));

        String name2 = "00hayden";
        Assert.assertFalse(RegexUtils.isValidLinuxUserName(name2));

        String name3 = "hayde123456789123456789123456789";
        Assert.assertFalse(RegexUtils.isValidLinuxUserName(name3));

        String name4 = "hayd123456789123456789123456789";
        Assert.assertTrue(RegexUtils.isValidLinuxUserName(name4));

        String name5 = "h";
        Assert.assertTrue(RegexUtils.isValidLinuxUserName(name5));

        String name6 = "hayden";
        Assert.assertTrue(RegexUtils.isValidLinuxUserName(name6));
    }

    @Test
    public void testIsValidWindowsUserName() {
        String name1 = "10000";
        Assert.assertFalse(RegexUtils.isValidWindowUserName(name1));

        String name2 = "00HAyden";
        Assert.assertFalse(RegexUtils.isValidWindowUserName(name2));

        String name3 = "HAYS1234567891289123456789";
        Assert.assertTrue(RegexUtils.isValidWindowUserName(name3));

        String name4 = "HAHAHA12345678912345hjh6789123456789";
        Assert.assertFalse(RegexUtils.isValidWindowUserName(name4));

        String name5 = "A  H";
        Assert.assertTrue(RegexUtils.isValidWindowUserName(name5));

        String name6 = "    ";
        Assert.assertFalse(RegexUtils.isValidWindowUserName(name6));

    }

    @Test
    public void testEscapeNRT() {
        String result1 = RegexUtils.escapeNRT("abc\n");
        Assert.assertEquals("abc_", result1);

        String result2 = RegexUtils.escapeNRT("abc\r");
        Assert.assertEquals("abc_", result2);

        String result3 = RegexUtils.escapeNRT("abc\t");
        Assert.assertEquals("abc_", result3);

        String result4 = RegexUtils.escapeNRT(null);
        Assert.assertNull(result4);
    }

}