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

package org.apache.dolphinscheduler.dao;

import static java.util.Objects.requireNonNull;

import org.apache.dolphinscheduler.dao.datasource.ConnectionFactory;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PluginDao extends AbstractBaseDao {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private PluginDefineMapper pluginDefineMapper;

    @Override
    protected void init() {
        pluginDefineMapper = ConnectionFactory.getInstance().getMapper(PluginDefineMapper.class);
    }

    /**
     * add pluginDefine
     *
     * @param pluginDefine plugin define entity
     * @return plugin define id
     */
    public int addPluginDefine(PluginDefine pluginDefine) {
        return pluginDefineMapper.insert(pluginDefine);
    }

    /**
     * add or update plugin define
     *
     * @param pluginDefine new pluginDefine
     */
    public int addOrUpdatePluginDefine(PluginDefine pluginDefine) {
        requireNonNull(pluginDefine, "pluginDefine is null");
        requireNonNull(pluginDefine.getPluginName(), "pluginName is null");
        requireNonNull(pluginDefine.getPluginType(), "pluginType is null");

        List<PluginDefine> pluginDefineList = pluginDefineMapper.queryByNameAndType(pluginDefine.getPluginName(), pluginDefine.getPluginType());
        if (pluginDefineList == null || pluginDefineList.size() == 0) {
            return pluginDefineMapper.insert(pluginDefine);
        }
        PluginDefine currPluginDefine = pluginDefineList.get(0);
        if (!currPluginDefine.getPluginParams().equals(pluginDefine.getPluginParams())) {
            currPluginDefine.setUpdateTime(pluginDefine.getUpdateTime());
            currPluginDefine.setPluginParams(pluginDefine.getPluginParams());
            pluginDefineMapper.updateById(currPluginDefine);
        }
        return currPluginDefine.getId();
    }

    /**
     * query plugin define by id
     *
     * @param pluginDefineId plugin define id
     * @return PluginDefine
     */
    public PluginDefine getPluginDefineById(int pluginDefineId) {
        return pluginDefineMapper.selectById(pluginDefineId);
    }

    public PluginDefineMapper getPluginDefineMapper() {
        return pluginDefineMapper;
    }

    public void setPluginDefineMapper(PluginDefineMapper pluginDefineMapper) {
        this.pluginDefineMapper = pluginDefineMapper;
    }
}
