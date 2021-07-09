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

package org.apache.dolphinscheduler.server.worker.task;

/**
 * command execute result
 */
public class CommandExecuteResult {

    /**
     * command exit code
     */
    private Integer exitStatusCode;


    /**
     * process id
     */
    private Integer processId;


    public CommandExecuteResult(){
        this.exitStatusCode = 0;
    }


    public Integer getExitStatusCode() {
        return exitStatusCode;
    }

    public void setExitStatusCode(Integer exitStatusCode) {
        this.exitStatusCode = exitStatusCode;
    }


    public Integer getProcessId() {
        return processId;
    }

    public void setProcessId(Integer processId) {
        this.processId = processId;
    }
}
