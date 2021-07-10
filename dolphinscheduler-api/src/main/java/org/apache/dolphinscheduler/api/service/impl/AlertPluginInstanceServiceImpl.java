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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.AlertPluginInstanceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.vo.AlertPluginInstanceVO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.BooleanUtils;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;

import org.apache.commons.collections.MapUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * alert plugin instance service impl
 */
@Service
@Lazy
public class AlertPluginInstanceServiceImpl extends BaseServiceImpl implements AlertPluginInstanceService {

    @Autowired
    private AlertPluginInstanceMapper alertPluginInstanceMapper;

    @Autowired
    private PluginDefineMapper pluginDefineMapper;

    @Autowired
    private AlertGroupMapper alertGroupMapper;

    /**
     * creat alert plugin instance
     *
     * @param loginUser login user
     * @param pluginDefineId plugin define id
     * @param instanceName instance name
     * @param pluginInstanceParams plugin instance params
     */
    @Override
    public Map<String, Object> create(User loginUser, int pluginDefineId, String instanceName, String pluginInstanceParams) {
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance();
        String paramsMapJson = parsePluginParamsMap(pluginInstanceParams);
        alertPluginInstance.setPluginInstanceParams(paramsMapJson);
        alertPluginInstance.setInstanceName(instanceName);
        alertPluginInstance.setPluginDefineId(pluginDefineId);

        Map<String, Object> result = new HashMap<>();

        if (BooleanUtils.isTrue(alertPluginInstanceMapper.existInstanceName(alertPluginInstance.getInstanceName()))) {
            putMsg(result, Status.PLUGIN_INSTANCE_ALREADY_EXIT);
            return result;
        }

        int i = alertPluginInstanceMapper.insert(alertPluginInstance);

        if (i > 0) {
            putMsg(result, Status.SUCCESS);
            return result;
        }
        putMsg(result, Status.SAVE_ERROR);
        return result;
    }

    /**
     * update alert plugin instance
     *
     * @param loginUser login user
     * @param pluginInstanceId plugin instance id
     * @param instanceName instance name
     * @param pluginInstanceParams plugin instance params
     */
    @Override
    public Map<String, Object> update(User loginUser, int pluginInstanceId, String instanceName, String pluginInstanceParams) {

        String paramsMapJson = parsePluginParamsMap(pluginInstanceParams);
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance(pluginInstanceId, paramsMapJson, instanceName, new Date());

        Map<String, Object> result = new HashMap<>();
        int i = alertPluginInstanceMapper.updateById(alertPluginInstance);

        if (i > 0) {
            putMsg(result, Status.SUCCESS);
            return result;
        }
        putMsg(result, Status.SAVE_ERROR);
        return result;
    }

    /**
     * delete alert plugin instance
     *
     * @param loginUser login user
     * @param id id
     * @return result
     */
    @Override
    public Map<String, Object> delete(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>();
        //check if there is an associated alert group
        boolean hasAssociatedAlertGroup = checkHasAssociatedAlertGroup(String.valueOf(id));
        if (hasAssociatedAlertGroup) {
            putMsg(result, Status.DELETE_ALERT_PLUGIN_INSTANCE_ERROR_HAS_ALERT_GROUP_ASSOCIATED);
            return result;
        }

        int i = alertPluginInstanceMapper.deleteById(id);
        if (i > 0) {
            putMsg(result, Status.SUCCESS);
        }

        return result;
    }

    /**
     * get alert plugin instance
     *
     * @param loginUser login user
     * @param id get id
     * @return alert plugin
     */
    @Override
    public Map<String, Object> get(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>();
        AlertPluginInstance alertPluginInstance = alertPluginInstanceMapper.selectById(id);

        if (null != alertPluginInstance) {
            putMsg(result, Status.SUCCESS);
            result.put(Constants.DATA_LIST, alertPluginInstance);
        }

        return result;
    }

    @Override
    public Map<String, Object> queryAll() {
        Map<String, Object> result = new HashMap<>();
        List<AlertPluginInstance> alertPluginInstances = alertPluginInstanceMapper.queryAllAlertPluginInstanceList();
        List<AlertPluginInstanceVO> alertPluginInstanceVOS = buildPluginInstanceVOList(alertPluginInstances);
        if (null != alertPluginInstances) {
            putMsg(result, Status.SUCCESS);
            result.put(Constants.DATA_LIST, alertPluginInstanceVOS);
        }
        return result;
    }

    @Override
    public boolean checkExistPluginInstanceName(String pluginInstanceName) {
        return BooleanUtils.isTrue(alertPluginInstanceMapper.existInstanceName(pluginInstanceName));
    }

    @Override
    public Map<String, Object> queryPluginPage(int pageIndex, int pageSize) {
        IPage<AlertPluginInstance> pluginInstanceIPage = new Page<>(pageIndex, pageSize);
        pluginInstanceIPage = alertPluginInstanceMapper.selectPage(pluginInstanceIPage, null);

        PageInfo<AlertPluginInstanceVO> pageInfo = new PageInfo<>(pageIndex, pageSize);
        pageInfo.setTotalCount((int) pluginInstanceIPage.getTotal());
        pageInfo.setLists(buildPluginInstanceVOList(pluginInstanceIPage.getRecords()));
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private List<AlertPluginInstanceVO> buildPluginInstanceVOList(List<AlertPluginInstance> alertPluginInstances) {
        if (CollectionUtils.isEmpty(alertPluginInstances)) {
            return null;
        }
        List<PluginDefine> pluginDefineList = pluginDefineMapper.queryAllPluginDefineList();
        if (CollectionUtils.isEmpty(pluginDefineList)) {
            return null;
        }
        Map<Integer, PluginDefine> pluginDefineMap = pluginDefineList.stream().collect(Collectors.toMap(PluginDefine::getId, Function.identity()));
        List<AlertPluginInstanceVO> alertPluginInstanceVOS = new ArrayList<>();
        alertPluginInstances.forEach(alertPluginInstance -> {
            AlertPluginInstanceVO alertPluginInstanceVO = new AlertPluginInstanceVO();

            alertPluginInstanceVO.setCreateTime(alertPluginInstance.getCreateTime());
            alertPluginInstanceVO.setUpdateTime(alertPluginInstance.getUpdateTime());
            alertPluginInstanceVO.setPluginDefineId(alertPluginInstance.getPluginDefineId());
            alertPluginInstanceVO.setInstanceName(alertPluginInstance.getInstanceName());
            alertPluginInstanceVO.setId(alertPluginInstance.getId());
            PluginDefine pluginDefine = pluginDefineMap.get(alertPluginInstance.getPluginDefineId());
            //FIXME When the user removes the plug-in, this will happen. At this time, maybe we should add a new field to indicate that the plug-in has expired?
            if (null == pluginDefine) {
                return;
            }
            alertPluginInstanceVO.setAlertPluginName(pluginDefine.getPluginName());
            //todo List pages do not recommend returning this parameter
            String pluginParamsMapString = alertPluginInstance.getPluginInstanceParams();
            String uiPluginParams = parseToPluginUiParams(pluginParamsMapString, pluginDefine.getPluginParams());
            alertPluginInstanceVO.setPluginInstanceParams(uiPluginParams);
            alertPluginInstanceVOS.add(alertPluginInstanceVO);
        });
        return alertPluginInstanceVOS;

    }

    /**
     * Get the parameters actually needed by the plugin
     *
     * @param pluginParams Complete parameters(include ui)
     * @return k, v(json string)
     */
    private String parsePluginParamsMap(String pluginParams) {
        Map<String, String> paramsMap = PluginParamsTransfer.getPluginParamsMap(pluginParams);
        return JSONUtils.toJsonString(paramsMap);
    }

    /**
     * parse To Plugin Ui Params
     *
     * @param pluginParamsMapString k-v data
     * @param pluginUiParams Complete parameters(include ui)
     * @return Complete parameters list(include ui)
     */
    private String parseToPluginUiParams(String pluginParamsMapString, String pluginUiParams) {
        // todo: serialize to PluginParams list
        List<Map<String, Object>> pluginParamsList = PluginParamsTransfer.generatePluginParams(pluginParamsMapString, pluginUiParams);
        pluginParamsList.forEach(pluginParamMap -> {
            Map<String, String> props = (Map<String, String>) pluginParamMap.get("props");
            if (MapUtils.isNotEmpty(props) && "password".equals(props.get("type"))) {
                // hide the password
                pluginParamMap.remove("value");
            }
        });
        return JSONUtils.toJsonString(pluginParamsList);
    }

    private boolean checkHasAssociatedAlertGroup(String id) {
        List<String> idsList = alertGroupMapper.queryInstanceIdsList();
        if (CollectionUtils.isEmpty(idsList)) {
            return false;
        }
        Optional<String> first = idsList.stream().filter(k -> null != k && Arrays.asList(k.split(",")).contains(id)).findFirst();
        return first.isPresent();
    }

}
