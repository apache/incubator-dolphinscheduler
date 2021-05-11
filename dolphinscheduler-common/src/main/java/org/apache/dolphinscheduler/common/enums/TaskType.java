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

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * task node type
 */
public enum TaskType {
    /**
     * 0 SHELL
     * 1 SQL
     * 2 SUB_PROCESS
     * 3 PROCEDURE
     * 4 MR
     * 5 SPARK
     * 6 PYTHON
     * 7 DEPENDENT
     * 8 FLINK
     * 9 HTTP
     * 10 DATAX
     * 11 CONDITIONS
     * 12 SQOOP
     * 13 WATERDROP
     */
    SHELL(0, "shell"),
    SQL(1, "sql"),
    SUB_PROCESS(2, "sub_process"),
    PROCEDURE(3, "procedure"),
    MR(4, "mr"),
    SPARK(5, "spark"),
    PYTHON(6, "python"),
    DEPENDENT(7, "dependent"),
    FLINK(8, "flink"),
    HTTP(9, "http"),
    DATAX(10, "datax"),
    CONDITIONS(11, "conditions"),
    SQOOP(12, "sqoop"),
    WATERDROP(13, "waterdrop"),
    SWITCH(14, "switch"),
    ;

    TaskType(int code, String descp){
        this.code = code;
        this.descp = descp;
    }

    @EnumValue
    private final int code;
    private final String descp;

    public static boolean typeIsNormalTask(String typeName) {
        TaskType taskType = TaskType.valueOf(typeName);
        return !(taskType == TaskType.SUB_PROCESS || taskType == TaskType.DEPENDENT);
    }

    public int getCode() {
        return code;
    }

    public String getDescp() {
        return descp;
    }
}
