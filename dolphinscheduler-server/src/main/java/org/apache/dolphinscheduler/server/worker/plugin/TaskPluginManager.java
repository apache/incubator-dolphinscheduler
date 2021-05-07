package org.apache.dolphinscheduler.server.worker.plugin;/*
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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import static com.google.common.base.Preconditions.checkState;

import org.apache.dolphinscheduler.common.enums.PluginType;
import org.apache.dolphinscheduler.common.plugin.AbstractDolphinPluginManager;
import org.apache.dolphinscheduler.dao.DaoFactory;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.spi.DolphinSchedulerPlugin;
import org.apache.dolphinscheduler.spi.classloader.ThreadContextClassLoader;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.task.TaskChannel;
import org.apache.dolphinscheduler.spi.task.TaskChannelFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskPluginManager extends AbstractDolphinPluginManager {

    private static final Logger logger = LoggerFactory.getLogger(TaskPluginManager.class);

    private final Map<String, TaskChannelFactory> taskChannelFactoryMap = new ConcurrentHashMap<>();
    private final Map<String, TaskChannel> taskChannelMap = new ConcurrentHashMap<>();

    /**
     * k->pluginDefineId v->pluginDefineName
     */
    private final Map<Integer, String> pluginDefineMap = new HashMap<>();

    private void addTaskChannelFactory(TaskChannelFactory taskChannelFactory) {
        requireNonNull(taskChannelFactory, "taskChannelFactory is null");

        if (taskChannelFactoryMap.putIfAbsent(taskChannelFactory.getName(), taskChannelFactory) != null) {
            throw new IllegalArgumentException(format("Task Plugin '%s' is already registered", taskChannelFactory.getName()));
        }

        try {
            loadTaskChannel(taskChannelFactory.getName());
        } catch (Exception e) {
            throw new IllegalArgumentException(format("Task Plugin '%s' is can not load .", taskChannelFactory.getName()));
        }
    }

    private void loadTaskChannel(String name) {
        requireNonNull(name, "name is null");

        TaskChannelFactory taskChannelFactory = taskChannelFactoryMap.get(name);
        checkState(taskChannelFactory != null, "Task Plugin {} is not registered", name);

        try (ThreadContextClassLoader ignored = new ThreadContextClassLoader(taskChannelFactory.getClass().getClassLoader())) {
            TaskChannel taskChannel = taskChannelFactory.create();
            this.taskChannelMap.put(name, taskChannel);
        }

        logger.info("-- Loaded Task Plugin {} --", name);
    }


    private PluginDao pluginDao = DaoFactory.getDaoInstance(PluginDao.class);

    @Override
    public void installPlugin(DolphinSchedulerPlugin dolphinSchedulerPlugin) {
        for (TaskChannelFactory taskChannelFactory : dolphinSchedulerPlugin.getTaskChannelFactorys()) {
            logger.info("Registering Task Plugin '{}'", taskChannelFactory.getName());
            this.addTaskChannelFactory(taskChannelFactory);
            List<PluginParams> params = taskChannelFactory.getParams();
            String nameEn = taskChannelFactory.getName();
            String paramsJson = PluginParamsTransfer.transferParamsToJson(params);

            PluginDefine pluginDefine = new PluginDefine(nameEn, PluginType.TASK.getDesc(), paramsJson);
            int id = pluginDao.addOrUpdatePluginDefine(pluginDefine);
            pluginDefineMap.put(id, pluginDefine.getPluginName());
        }
    }
}
