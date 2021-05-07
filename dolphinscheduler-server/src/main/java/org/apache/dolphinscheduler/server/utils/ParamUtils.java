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

package org.apache.dolphinscheduler.server.utils;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.DataType;
import org.apache.dolphinscheduler.common.enums.Direct;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.common.utils.placeholder.BusinessTimeUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * param utils
 */
public class ParamUtils {

    /**
     * parameter conversion
     *
     * @param globalParams global params
     * @param globalParamsMap global params map
     * @param localParams local params
     * @param commandType command type
     * @param scheduleTime schedule time
     * @return global params
     */
    public static Map<String, Property> convert(Map<String, Property> globalParams,
                                                Map<String, String> globalParamsMap,
                                                Map<String, Property> localParams,
                                                CommandType commandType,
                                                Date scheduleTime) {
        if (globalParams == null
                && localParams == null) {
            return null;
        }
        // if it is a complement,
        // you need to pass in the task instance id to locate the time
        // of the process instance complement
        Map<String, String> timeParams = BusinessTimeUtils
                .getBusinessTime(commandType,
                        scheduleTime);

        if (globalParamsMap != null) {
            timeParams.putAll(globalParamsMap);
        }

        if (globalParams != null && localParams != null) {
            globalParams.putAll(localParams);
        } else if (globalParams == null && localParams != null) {
            globalParams = localParams;
        }
        Iterator<Map.Entry<String, Property>> iter = globalParams.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Property> en = iter.next();
            Property property = en.getValue();

            if (StringUtils.isNotEmpty(property.getValue())
                    && property.getValue().startsWith("$")) {
                /**
                 *  local parameter refers to global parameter with the same name
                 *  note: the global parameters of the process instance here are solidified parameters,
                 *  and there are no variables in them.
                 */
                String val = property.getValue();
                val = ParameterUtils.convertParameterPlaceholders(val, timeParams);
                property.setValue(val);
            }
        }

        return globalParams;
    }

    /**
     * format convert
     *
     * @param paramsMap params map
     * @return Map of converted
     */
    public static Map<String, String> convert(Map<String, Property> paramsMap) {
        if (paramsMap == null) {
            return null;
        }

        Map<String, String> map = new HashMap<>();
        Iterator<Map.Entry<String, Property>> iter = paramsMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Property> en = iter.next();
            map.put(en.getKey(), en.getValue().getValue());
        }
        return map;
    }

    /**
     * get parameters map
     *
     * @param definedParams definedParams
     * @return parameters map
     */
    public static Map<String, Property> getUserDefParamsMap(Map<String, String> definedParams) {
        if (definedParams != null) {
            Map<String, Property> userDefParamsMaps = new HashMap<>();
            Iterator<Map.Entry<String, String>> iter = definedParams.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> en = iter.next();
                Property property = new Property(en.getKey(), Direct.IN, DataType.VARCHAR, en.getValue());
                userDefParamsMaps.put(property.getProp(), property);
            }
            return userDefParamsMaps;
        }
        return null;
    }
}