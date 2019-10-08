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
package org.apache.dolphinscheduler.server.worker.task.spark;

import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.spark.SparkParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.ProcessDao;
import org.apache.dolphinscheduler.server.utils.ParamUtils;
import org.apache.dolphinscheduler.server.utils.SparkArgsUtils;
import org.apache.dolphinscheduler.server.worker.task.AbstractYarnTask;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  spark task
 */
public class SparkTask extends AbstractYarnTask {

  /**
   *  spark command
   */
  private static final String SPARK_COMMAND = "spark-submit";

  /**
   *  spark parameters
   */
  private SparkParameters sparkParameters;

  public SparkTask(TaskProps props, Logger logger,ProcessDao processDao) {
    super(props, logger,processDao);
  }

  @Override
  public void init() {

    logger.info("spark task params {}", taskProps.getTaskParams());

    sparkParameters = JSONUtils.parseObject(taskProps.getTaskParams(), SparkParameters.class);

    if (!sparkParameters.checkParameters()) {
      throw new RuntimeException("spark task params is not valid");
    }
    sparkParameters.setQueue(taskProps.getQueue());

    if (StringUtils.isNotEmpty(sparkParameters.getMainArgs())) {
      String args = sparkParameters.getMainArgs();

      /**
       *  combining local and global parameters
       */
      Map<String, Property> paramsMap = ParamUtils.convert(taskProps.getUserDefParamsMap(),
              taskProps.getDefinedParams(),
              sparkParameters.getLocalParametersMap(),
              taskProps.getCmdTypeIfComplement(),
              taskProps.getScheduleTime());
      if (paramsMap != null ){
        args = ParameterUtils.convertParameterPlaceholders(args, ParamUtils.convert(paramsMap));
      }
      sparkParameters.setMainArgs(args);
    }
  }

  /**
   *  create command
   * @return
   */
  @Override
  protected String buildCommand() {
    List<String> args = new ArrayList<>();

    args.add(SPARK_COMMAND);

    // other parameters
    args.addAll(SparkArgsUtils.buildArgs(sparkParameters));

    String command = ParameterUtils
            .convertParameterPlaceholders(String.join(" ", args), taskProps.getDefinedParams());

    logger.info("spark task command : {}", command);

    return command;
  }

  @Override
  public AbstractParameters getParameters() {
    return sparkParameters;
  }
}
