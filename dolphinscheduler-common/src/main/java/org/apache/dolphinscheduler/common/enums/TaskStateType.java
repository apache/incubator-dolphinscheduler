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
package org.apache.dolphinscheduler.common.enums;

/**
 * type of task state
 */
public enum TaskStateType {
    /**
     * 0 waiting running
     * 1 running
     * 2 finish
     * 3 failed
     * 4 success
     */
    WAITING, RUNNING, FINISH, FAILED, SUCCESS;

    /**
     * convert task state to execute status integer array ;
     *
     * @param taskStateType task state type
     * @return result of execution status
     */
    public static int[] convert2ExecuteStatusIntArray(TaskStateType taskStateType) {

        switch (taskStateType) {
            case SUCCESS:
                return new int[]{ExecutionStatus.SUCCESS.ordinal()};
            case FAILED:
                return new int[]{
                        ExecutionStatus.FAILURE.ordinal(),
                        ExecutionStatus.NEED_FAULT_TOLERANCE.ordinal()};
            case FINISH:
                return new int[]{
                        ExecutionStatus.PAUSE.ordinal(),
                        ExecutionStatus.STOP.ordinal()
                };
            case RUNNING:
                return new int[]{ExecutionStatus.SUBMITTED_SUCCESS.ordinal(),
                        ExecutionStatus.RUNNING_EXECUTION.ordinal(),
                        ExecutionStatus.DELAY_EXECUTION.ordinal(),
                        ExecutionStatus.READY_PAUSE.ordinal(),
                        ExecutionStatus.READY_STOP.ordinal()};
            case WAITING:
                return new int[]{
                        ExecutionStatus.SUBMITTED_SUCCESS.ordinal()
                };
            default:
                break;
        }
        return new int[0];
    }

}
