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
package org.apache.dolphinscheduler.common.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.utils.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * job params related class
 */
public abstract class AbstractParameters implements IParameters {

  @Override
  public abstract boolean checkParameters();

  @Override
  public abstract List<ResourceInfo> getResourceFilesList();

  /**
   * local parameters
   */
  public List<Property> localParams;

  /**
   * check depend flag
   */
  private int checkDependFlag;

  /**
   * target node keys
   */
  private String targetNodeKeys;

  /**
   * depend node keys
   */
  private String dependNodeKeys;

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

  public int getCheckDependFlag() {
    return checkDependFlag;
  }

  public void setCheckDependFlag(int checkDependFlag) {
    this.checkDependFlag = checkDependFlag;
  }

  public String getTargetNodeKeys() {
    return targetNodeKeys;
  }

  public void setTargetNodeKeys(String targetNodeKeys) {
    this.targetNodeKeys = targetNodeKeys;
  }

  public String getDependNodeKeys() {
    return dependNodeKeys;
  }

  public void setDependNodeKeys(String dependNodeKeys) {
    this.dependNodeKeys = dependNodeKeys;
  }

  /**
   * get local parameters map
   * @return parameters map
   */
  @JsonIgnore
  public Map<String,Property> getLocalParametersMap() {
      if (localParams != null) {
        Map<String,Property> localParametersMaps = new LinkedHashMap<>();

        for (Property property : localParams) {
          localParametersMaps.put(property.getProp(),property);
        }
        return localParametersMaps;
      }
      return null;
  }

  public boolean isCheckDepend() {
    if (this.getCheckDependFlag() == Flag.NO.ordinal()
            || StringUtils.isEmpty(this.getDependNodeKeys())) {
      return false;
    }

    return true;
  }

}
