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
package org.apache.dolphinscheduler.api.exceptions;

import org.apache.dolphinscheduler.api.controller.AccessTokenController;
import org.apache.dolphinscheduler.api.controller.ProcessDefinitionController;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class ApiExceptionHandlerTest {

    @Test
    public void exceptionHandler() throws NoSuchMethodException {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        AccessTokenController controller = new AccessTokenController();
        Method method = controller.getClass().getMethod("createToken",int.class, String.class, String.class);
        HandlerMethod hm = new HandlerMethod(controller, method);
        Result result = handler.exceptionHandler(new RuntimeException("test exception"), hm);
        Assert.assertEquals(Status.CREATE_ACCESS_TOKEN_ERROR.getCode(),result.getCode().intValue());
    }

    @Test
    public void exceptionHandlerRuntime() throws NoSuchMethodException {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        ProcessDefinitionController controller = new ProcessDefinitionController();
        Method method = controller.getClass().getMethod("batchExportProcessDefinitionByIds", String.class, String.class, HttpServletResponse.class);
        HandlerMethod hm = new HandlerMethod(controller, method);
        Result result = handler.exceptionHandler(new RuntimeException("test exception"), hm);
        Assert.assertEquals(Status.INTERNAL_SERVER_ERROR_ARGS.getCode(),result.getCode().intValue());
    }
}