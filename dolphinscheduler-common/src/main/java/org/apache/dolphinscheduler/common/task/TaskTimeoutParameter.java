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

package org.apache.dolphinscheduler.common.task;

import org.apache.dolphinscheduler.common.enums.TaskTimeoutStrategy;

/**
 * task timeout parameter
 */
public class TaskTimeoutParameter {

    private boolean enable;
    /**
     * task timeout strategy
     */
    private TaskTimeoutStrategy strategy;
    /**
     * task timeout interval
     */
    private int interval;

    /**
     * The interval for checking whether an event has occurred.
     */
    private int checkInterval;

    public boolean getEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public TaskTimeoutStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(TaskTimeoutStrategy strategy) {
        this.strategy = strategy;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(int checkInterval) {
        this.checkInterval = checkInterval;
    }

    public TaskTimeoutParameter() {
    }

    public TaskTimeoutParameter(boolean enable) {
        this.enable = enable;
    }

    public TaskTimeoutParameter(boolean enable, TaskTimeoutStrategy strategy, int interval) {
        this.enable = enable;
        this.strategy = strategy;
        this.interval = interval;
    }

    public TaskTimeoutParameter(boolean enable, TaskTimeoutStrategy strategy, int interval, int checkInterval) {
        this.enable = enable;
        this.strategy = strategy;
        this.interval = interval;
        this.checkInterval = checkInterval;
    }

    @Override
    public String toString() {
        return "TaskTimeoutParameter{"
                + "enable=" + enable
                + ", strategy=" + strategy
                + ", interval=" + interval
                + ", checkInterval=" + checkInterval
                + '}';
    }
}
