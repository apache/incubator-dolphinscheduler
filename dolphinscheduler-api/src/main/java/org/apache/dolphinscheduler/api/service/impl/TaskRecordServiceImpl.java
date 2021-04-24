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

package org.apache.dolphinscheduler.api.service.impl;

import static org.apache.dolphinscheduler.common.Constants.TASK_RECORD_TABLE_HISTORY_HIVE_LOG;
import static org.apache.dolphinscheduler.common.Constants.TASK_RECORD_TABLE_HIVE_LOG;

import org.apache.dolphinscheduler.api.service.TaskRecordService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.vo.PageListVO;
import org.apache.dolphinscheduler.dao.TaskRecordDao;
import org.apache.dolphinscheduler.dao.entity.TaskRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * task record service impl
 */
@Service
public class TaskRecordServiceImpl extends BaseServiceImpl implements TaskRecordService {

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
     * @param pageNo page numbere
     * @param pageSize page size
     * @param isHistory is history
     * @return task record list
     */
    @Override
    public Result<PageListVO<TaskRecord>> queryTaskRecordListPaging(boolean isHistory, String taskName, String startDate,
                                                        String taskDate, String sourceTable,
                                                        String destTable, String endDate,
                                                        String state, Integer pageNo, Integer pageSize) {
        PageInfo<TaskRecord> pageInfo = new PageInfo<>(pageNo, pageSize);

        Map<String, String> map = new HashMap<>();
        map.put("taskName", taskName);
        map.put("taskDate", taskDate);
        map.put("state", state);
        map.put("sourceTable", sourceTable);
        map.put("targetTable", destTable);
        map.put("startTime", startDate);
        map.put("endTime", endDate);
        map.put("offset", pageInfo.getStart().toString());
        map.put("pageSize", pageInfo.getPageSize().toString());

        String table = isHistory ? TASK_RECORD_TABLE_HISTORY_HIVE_LOG : TASK_RECORD_TABLE_HIVE_LOG;
        int count = TaskRecordDao.countTaskRecord(map, table);
        List<TaskRecord> recordList = TaskRecordDao.queryAllTaskRecord(map, table);
        pageInfo.setTotalCount(count);
        pageInfo.setLists(recordList);

        return Result.success(new PageListVO<>(pageInfo));
    }
}
