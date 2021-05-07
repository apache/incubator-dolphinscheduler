package org.apache.dolphinscheduler.spi.task;/*
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * job params related class
 */
public abstract class AbstractParameters implements IParameters {

    /**
     * local parameters
     */
    private  List<Property> localParams;

    /**
     * get local parameters list
     * @return Property list
     */
    public List<Property> getLocalParams() {
        return localParams;
    }

    public void setLocalParams(List<Property> localParams) {
        this.localParams = localParams;
    }

    /**
     * get local parameters map
     * @return parameters map
     */
    public Map<String, Property> getLocalParametersMap() {
        if (localParams != null) {
            Map<String, Property> localParametersMaps = new LinkedHashMap<>();

            for (Property property : localParams) {
                localParametersMaps.put(property.getProp(),property);
            }
            return localParametersMaps;
        }
        return null;
    }

}
