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
package org.apache.dolphinscheduler.common.task.procedure;

import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * procedure parameter
 */
public class ProcedureParameters extends AbstractParameters {

  /**
   * data source type，eg  MYSQL, POSTGRES, HIVE ...
   */
  private String type;

  /**
   * data source id
   */
  private int datasource;

  /**
   * procedure name
   */
  private String method;


  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getDatasource() {
    return datasource;
  }

  public void setDatasource(int datasource) {
    this.datasource = datasource;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  @Override
  public boolean checkParameters() {
    return datasource != 0 && StringUtils.isNotEmpty(type) && StringUtils.isNotEmpty(method);
  }

  @Override
  public List<ResourceInfo> getResourceFilesList() {
    return new ArrayList<>();
  }

  @Override
  public String toString() {
    return "ProcedureParam{" +
            "type='" + type + '\'' +
            ", datasource=" + datasource +
            ", method='" + method + '\'' +
            '}';
  }
}
