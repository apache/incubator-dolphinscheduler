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
package org.apache.dolphinscheduler.api.utils;


import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.utils.*;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.common.utils.TaskParametersUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * check utils
 */
public class CheckUtils {

  private CheckUtils() {
    throw new IllegalStateException("CheckUtils class");
  }
  /**
   * check username
   *
   * @param userName user name
   * @return true if user name regex valid,otherwise return false
   */
  public static boolean checkUserName(String userName) {
    return regexChecks(userName, Constants.REGEX_USER_NAME);
  }

  /**
   * check email
   *
   * @param email email
   * @return true if email regex valid, otherwise return false
   */
  public static boolean checkEmail(String email) {
    if (StringUtils.isEmpty(email)){
      return false;
    }

    return email.length() > 5 && email.length() <= 40 && regexChecks(email, Constants.REGEX_MAIL_NAME) ;
  }

  /**
   * check project description
   *
   * @param desc desc
   * @return true if description regex valid, otherwise return false
   */
  public static Map<String, Object> checkDesc(String desc) {
    Map<String, Object> result = new HashMap<>();
    if (StringUtils.isNotEmpty(desc) && desc.length() > 200) {
        result.put(Constants.STATUS, Status.REQUEST_PARAMS_NOT_VALID_ERROR);
        result.put(Constants.MSG, MessageFormat.format(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), "desc length"));
    }else{
      result.put(Constants.STATUS, Status.SUCCESS);
    }
    return result;
  }

  /**
   * check extra info
   *
   * @param otherParams other params
   * @return true if other parameters are valid, otherwise return false
   */
  public static boolean checkOtherParams(String otherParams) {
    return StringUtils.isNotEmpty(otherParams) && !JSONUtils.checkJsonValid(otherParams);
  }

  /**
   * check password
   *
   * @param password password
   * @return true if password regex valid, otherwise return false
   */
  public static boolean checkPassword(String password) {
    return StringUtils.isNotEmpty(password) && password.length() >= 2 && password.length() <= 20;
  }

  /**
   * check phone
   * phone can be empty.
   * @param phone phone
   * @return true if phone regex valid, otherwise return false
   */
  public static boolean checkPhone(String phone) {
    return StringUtils.isEmpty(phone) || phone.length() == 11;
  }


  /**
   * check task node parameter
   *
   * @param parameter parameter
   * @param taskType task type
   * @return true if task node parameters are valid, otherwise return false
   */
  public static boolean checkTaskNodeParameters(String parameter, String taskType) {
    AbstractParameters abstractParameters = TaskParametersUtils.getParameters(taskType, parameter);

    if (abstractParameters != null) {
      return abstractParameters.checkParameters();
    }

    return false;
  }

  /**
   * check params
   * @param userName user name
   * @param password password
   * @param email email
   * @param phone phone
   * @return true if user parameters are valid, other return false
   */
  public static boolean checkUserParams(String userName, String password, String email, String phone){
    return CheckUtils.checkUserName(userName) &&
            CheckUtils.checkEmail(email) &&
            CheckUtils.checkPassword(password) &&
            CheckUtils.checkPhone(phone);
  }

  /**
   * regex check
   *
   * @param str input string
   * @param pattern regex pattern
   * @return true if regex pattern is right, otherwise return false
   */
  private static boolean regexChecks(String str, Pattern pattern) {
    if (StringUtils.isEmpty(str)) {
      return false;
    }

    return pattern.matcher(str).matches();
  }
}
