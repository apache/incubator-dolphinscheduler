package org.apache.dolphinscheduler.spi.common;/*
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

import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.List;

public interface UiChannelFactory {

    /**
     * plugin name
     * Must be UNIQUE .
     * This alert plugin name eg: email , message ...
     * Name can often be displayed on the page ui eg : email , message , MR , spark , hive ...
     *
     * @return this alert plugin name
     */
    String getName();

    /**
     * Returns the configurable parameters that this plugin needs to display on the web ui
     *
     * @return this alert plugin params
     */
    List<PluginParams> getParams();

}
