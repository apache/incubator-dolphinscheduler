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

package org.apache.dolphinscheduler.spi;

import static java.util.Collections.emptyList;

import org.apache.dolphinscheduler.spi.alert.AlertChannelFactory;
import org.apache.dolphinscheduler.spi.task.TaskChannelFactory;

/**
 * Dolphinscheduler plugin interface
 * All plugin need implements this interface.
 * Each plugin needs a factory. This factory has at least two methods.
 * one called <code>AlertChannelFactory#getId()</code>, used to return the name of the plugin implementation,
 * so that the 'PluginLoad' module can find the plugin implementation class by the name in the configuration file.
 * The other method is called <code>create(Map config)</code>. This method contains at least one parameter  <code>Map config</code>.
 * Config contains custom parameters read from the plug-in configuration file.
 */
public interface DolphinSchedulerPlugin {

    default Iterable<AlertChannelFactory> getAlertChannelFactorys() {
        return emptyList();
    }

    default Iterable<TaskChannelFactory> getTaskChannelFactorys() {
        return emptyList();
    }

}
