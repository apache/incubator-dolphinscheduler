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

package org.apache.dolphinscheduler.api.service;

import java.util.Map;

/**
 * task record service
 */
public interface TaskRecordService {

    /**
     * query task record list paging
     *
     * @param taskName task name
     * @param state state
     * @param sourceTable source table
     * @param destTable destination table
     * @param taskDate task date
     * @param startDate start time
     * @param endDate end time
     * @param pageNo page number
     * @param pageSize page size
     * @param isHistory is history
     * @return task record list
     */
    Map<String,Object> queryTaskRecordListPaging(boolean isHistory, String taskName, String startDate,
                                                 String taskDate, String sourceTable,
                                                 String destTable, String endDate,
                                                 String state, Integer pageNo, Integer pageSize);
}
