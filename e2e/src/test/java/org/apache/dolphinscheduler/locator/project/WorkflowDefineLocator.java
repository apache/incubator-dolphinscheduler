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

package org.apache.dolphinscheduler.locator.project;

import org.openqa.selenium.By;

public class WorkflowDefineLocator {

    /**
     * jump workflow define page
     */
    // click project name
    public static final By CLICK_PROJECT_NAME = By.xpath("//span/a");

    // click workflow define
    public static final By CLICK_WORKFLOW_DEFINE = By.xpath("//li/span");

    /**
     * create workflow
     */
    // click create workflow button
    public static final By CLICK_CREATE_WORKFLOW_BUTTON = By.xpath("//button/span");

    //mouse down at shell
    public static final By MOUSE_DOWN_AT_SHELL = By.xpath("//div[@id='SHELL']/div/div");

    //mouse down at spark
    public static final By MOUSE_DOWN_AT_SPARK = By.xpath("//div[5]/div/div");

    //mouse move at DAG
    public static final By MOUSE_MOVE_SHELL_AT_DAG = By.xpath("//div[@id='canvas']");

    //input shell task _name
    public static final By INPUT_SHELL_TASK_NAME = By.xpath("//input");

    //click stop run type
    public static final By CLICK_STOP_RUN_TYPE = By.xpath("//label[2]/span[1]/input");

    //click normal run type
    public static final By CLICK_NORMAL_RUN_TYPE = By.xpath("//label[1]/span[1]/input");

    //input shell task description
    public static final By INPUT_SHELL_TASK_DESCRIPTION = By.xpath("//label/div/textarea");

    //click task priority
    public static final By CLICK_TASK_PRIORITY = By.xpath("//span/div/div/div/div/div");

    //select task priority
    public static final By SELECT_TASK_PRIORITY = By.xpath("//li[2]/li/span");

    //click work group
    public static final By CLICK_WORK_GROUP = By.xpath("//div/div/input");

    //select work group
    public static final By SELECT_WORK_GROUP = By.xpath("//div[4]/div[2]/div/div[1]/div/input");

    //select number of failed retries
    public static final By SELECT_FAIL_RETRIES_NUMBER = By.xpath("//div[5]/div[2]/div[1]/div[1]/div/input");

    //select failed retry interval
    public static final By SELECT_FAIL_RETRIES_INTERVAL = By.xpath("//div[5]/div[2]/div[2]/div[1]/div/input");

    //click timeout alarm
    public static final By CLICK_TIMEOUT_ALARM = By.xpath("//label/div/span/span");

    //select timeout fail
    public static final By SELECT_TIMEOUT_FAIL = By.xpath("//div/div/label[2]/span/input");

    //cancel timeout alarm
    public static final By CANCEL_TIMEOUT_ALARM = By.xpath("//div/div/label/span/input");

    //select timeout alarm
    public static final By SELECT_TIMEOUT_ALARM = By.xpath("//div/div/label/span/input");

    //input timeout
    public static final By SELECT_TIMEOUT = By.xpath("//div[3]/div[2]/label/div/input");

    //click codeMirror
    public static final By CLICK_CODE_MIRROR = By.xpath("//div[5]/div/pre");

    //input script
    public static final By INPUT_SCRIPT = By.xpath("//div[2]/div/div/div/div/div/textarea");

    //click custom parameters
    public static final By CLICK_CUSTOM_PARAMETERS = By.xpath("//span/a/em");

    //input custom parameters
    public static final By INPUT_CUSTOM_PARAMETERS = By.xpath("//div[2]/div/div/div/div/div/input");

    //input custom parameters value
    public static final By INPUT_CUSTOM_PARAMETERS_VALUE = By.xpath("//div[2]/div/div/div/div[1]/div[2]/input");

    //click to add custom parameters
    public static final By CLICK_ADD_CUSTOM_PARAMETERS = By.xpath("//div[2]/div/div/div/div/span[2]/a/em");

    //input to add custom parameters
    public static final By INPUT_ADD_CUSTOM_PARAMETERS = By.xpath("//div[2]/div/div/div/div[2]/div[1]/input");

    //input to add custom parameters value
    public static final By INPUT_ADD_CUSTOM_PARAMETERS_VALUE = By.xpath("//div[2]/div[2]/input");

    //delete custom parameters
    public static final By CLICK_DELETE_CUSTOM_PARAMETERS = By.xpath("//div[2]/span/a/em");

    //click submit button
    public static final By CLICK_SUBMIT_BUTTON = By.xpath("//div[3]/div/button[2]/span");

    //copy task
    public static final By MOUSE_RIGHT_CLICK = By.xpath("//div[2]/div[2]/div/div/div/div/div[2]");
    public static final By COPY_TASK = By.xpath("//a[3]/span");

    //click line
    public static final By CLICK_LINE = By.xpath("//a[@id='line']/button/i");

    public static final By LINE_SOURCES_TASK = By.xpath("//div[@id='canvas']/div[1]/div[2]");

    public static final By LINE_TARGET_TASK = By.xpath("//div[@id='canvas']/div[2]/div[2]");

    /**
     * save workflow
     */
    //click save workflow button
    public static final By CLICK_SAVE_WORKFLOW_BUTTON = By.xpath("//div[2]/div[1]/div[2]/button[2]/span");

    //input  workflow name
    public static final By INPUT_WORKFLOW_NAME = By.xpath("//input");

    //input  workflow description
    public static final By INPUT_WORKFLOW_DESCRIPTION = By.xpath("//textarea");

    //select tenant
    public static final By CLICK_TENANT = By.xpath("//div[4]/div/div/div/input");
    public static final By SELECT_TENANT = By.xpath("//div[4]/div/div[2]/div/div/div/ul/li[2]/span");

    //click workflow timeout alarm
    public static final By CLICK_WORKFLOW_TIMEOUT_ALARM = By.xpath("//span[2]/span/span");

    //input workflow timeout
    public static final By INPUT_WORKFLOW_TIMEOUT = By.xpath("//span/div/input");

    //click workflow  global parameters
    public static final By CLICK_WORKFLOW_GLOBAL_PARAMETERS = By.xpath("//span/a/em");

    //input workflow  global parameters
    public static final By INPUT_WORKFLOW_GLOBAL_PARAMETERS = By.xpath("//div[8]/div/div/div/div/input");

    //input workflow  global parameters value
    public static final By INPUT_WORKFLOW_GLOBAL_PARAMETERS_VALUES = By.xpath("//div[2]/input");

    //click to add workflow  global parameters
    public static final By CLICK_ADD_WORKFLOW_GLOBAL_PARAMETERS = By.xpath("//span[2]/a/em");

    //input to  add workflow  global parameters
    public static final By INPUT_ADD_WORKFLOW_GLOBAL_PARAMETERS = By.xpath("//div[8]/div/div/div[2]/div/input");

    //input to add workflow  global parameters value
    public static final By INPUT_ADD_WORKFLOW_GLOBAL_PARAMETERS_VALUES = By.xpath("//div[2]/div[2]/input");

    //delete workflow  global parameters value
    public static final By CLICK_DELETE_WORKFLOW_GLOBAL_PARAMETERS = By.xpath("//div[2]/span/a/em");

    //click add button
    public static final By CLICK_ADD_BUTTON = By.xpath("//div[2]/div/button[2]/span");
    //scroll to element bottom
    public static final By SCROLL_BOTTOM = By.xpath("//span/a/em");

    public static final By WORKFLOW_NAME = By.xpath("//table/tr[2]/td[3]/span/a");

    /**
     * online workflow
     */
    public static final By WORKFLOW_STATE = By.xpath("//table/tr[2]/td[4]/span");


    // click online workflow button
    public static final By CLICK_ONLINE_WORKFLOW_BUTTON = By.xpath("//button[@title['data-original-title']='上线']");

    /**
     * offline workflow
     */
    // click offline workflow button
    public static final By CLICK_OFFLINE_WORKFLOW_BUTTON = By.xpath("//button[@title['data-original-title']='下线']");

    /**
     * delete workflow
     */
    //click delete workflow button
    public static final By DELETE_WORKFLOW_BOTTOM = By.xpath("//table/tr[2]/td[10]/span/button");

    //click confirm delete workflow button
    public static final By CONFIRM_DELETE_WORKFLOW_BOTTOM = By.xpath("//div[2]/div/button[2]/span");
}
