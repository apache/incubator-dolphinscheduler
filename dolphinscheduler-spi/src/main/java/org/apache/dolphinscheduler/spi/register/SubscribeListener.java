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

package org.apache.dolphinscheduler.spi.register;

/**
 * Registration center subscription. All listeners must implement this interface
 */
public interface SubscribeListener extends Comparable<SubscribeListener> {

    /**
     * Processing logic when the subscription node changes
     */
    void notify(String path, DataChangeEvent dataChangeEvent);

    /**
     * When multiple listeners listen to one event, the high order will be execute first.
     * The default order is 0
     *
     * @return order
     */
    default int getOrder() {
        return 0;
    }

    default int compareTo(SubscribeListener subscribeListener) {
        return this.getOrder() - subscribeListener.getOrder();
    }
}
