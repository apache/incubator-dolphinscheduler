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
package org.apache.dolphinscheduler.api.enums;

/**
 * execute type
 */
public enum ExecuteType {


    /**
     * operation type
     * 1 repeat running
     * 2 resume pause
     * 3 resume failure
     * 4 stop
     * 5 pause
     */
    NONE, REPEAT_RUNNING, RECOVER_SUSPENDED_PROCESS, START_FAILURE_TASK_PROCESS, STOP, PAUSE,START_PROCESS_RANDOM_TASK;

    public static ExecuteType getEnum(int value) {
        for (ExecuteType e : ExecuteType.values()) {
            if (e.ordinal() == value) {
                return e;
            }
        }
        return null;
    }
}
