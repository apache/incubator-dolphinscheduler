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

import java.util.HashMap;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * PluginType
 */
public enum PluginType {

    ALERT(1, "alert", true),
    REGISTER(2, "register", false),
    TASK(3,"task",true);

    PluginType(int code, String desc, boolean hasUi) {
        this.code = code;
        this.desc = desc;
        this.hasUi = hasUi;
    }

    @EnumValue
    private final int code;
    private final String desc;
    private final boolean hasUi;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public boolean getHasUi() {
        return hasUi;
    }


    private static HashMap<Integer, PluginType> PLUGIN_TYPE_MAP = new HashMap<>();

    static {
        for (PluginType pluginType : PluginType.values()) {
            PLUGIN_TYPE_MAP.put(pluginType.getCode(), pluginType);
        }
    }

    public static PluginType of(int type) {
        if (PLUGIN_TYPE_MAP.containsKey(type)) {
            return PLUGIN_TYPE_MAP.get(type);
        }
        throw new IllegalArgumentException("invalid type : " + type);
    }
}
