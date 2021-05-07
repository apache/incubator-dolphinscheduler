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

package org.apache.dolphinscheduler.plugin.task.api;

import org.apache.dolphinscheduler.spi.task.TaskRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskExecutionContextCacheManager {

    private TaskExecutionContextCacheManager() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * taskInstance cache
     */
    private static Map<Integer, TaskRequest> taskRequestContextCache = new ConcurrentHashMap<>();

    /**
     * get taskInstance by taskInstance id
     *
     * @param taskInstanceId taskInstanceId
     * @return taskInstance
     */

    public static TaskRequest getByTaskInstanceId(Integer taskInstanceId) {
        return taskRequestContextCache.get(taskInstanceId);
    }

    /**
     * cache taskInstance
     *
     * @param request request
     */
    public static void cacheTaskExecutionContext(TaskRequest request) {
        taskRequestContextCache.put(request.getTaskInstanceId(), request);
    }

    /**
     * remove taskInstance by taskInstanceId
     *
     * @param taskInstanceId taskInstanceId
     */
    public static void removeByTaskInstanceId(Integer taskInstanceId) {
        taskRequestContextCache.remove(taskInstanceId);
    }

    public static boolean updateTaskExecutionContext(TaskRequest request) {
        taskRequestContextCache.computeIfPresent(request.getTaskInstanceId(), (k, v) -> request);
        return taskRequestContextCache.containsKey(request.getTaskInstanceId());
    }
}
