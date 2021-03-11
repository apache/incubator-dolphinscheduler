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
package org.apache.dolphinscheduler.page;

import org.apache.dolphinscheduler.common.PageCommon;
import org.apache.dolphinscheduler.data.LoginData;
import org.apache.dolphinscheduler.data.security.TenantManageData;
import org.apache.dolphinscheduler.locator.LoginLocator;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

public class LoginPage extends PageCommon {
    TenantManageData tenantManageData = new TenantManageData();

    /**
     * Unique constructor
     * @param driver driver
     */
    public LoginPage(WebDriver driver) {
        super(driver);
    }

    /**
     * jump page
     */
    public void jumpPageEnglish() {
        System.out.println("jump to English login page");
        super.jumpPage(LoginData.URL);
        Cookie cookie = new Cookie("language", "en_US", "/", null);
        driver.manage().addCookie(cookie);
    }

    public void jumpPageChinese() {
        super.jumpPage(LoginData.URL);
        Cookie cookie = new Cookie("language", "zh_CN", "/", null);
        driver.manage().addCookie(cookie);
    }
    /**
     * login
     *
     * @return Whether to enter the specified page after searching
     */
    public boolean login() throws InterruptedException {
        // login data
        sendInput(LoginLocator.LOGIN_INPUT_USER, LoginData.USER);
        sendInput(LoginLocator.LOGIN_INPUT_PASSWORD, LoginData.PASSWORD);

        // click login button
        clickButton(LoginLocator.LOGIN_BUTTON);

        moveToElement(LoginLocator.LOGIN_BUTTON_MOVE);

        // Whether to enter the specified page after login
        return ifTitleContains(tenantManageData.getTenantData("tenantTitle"));
    }
}
