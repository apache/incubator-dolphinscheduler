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
package org.apache.dolphinscheduler.server.worker.task.http;


import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.Charsets;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.HttpMethod;
import org.apache.dolphinscheduler.common.enums.HttpParametersType;
import org.apache.dolphinscheduler.common.process.HttpProperty;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.http.HttpParameters;
import org.apache.dolphinscheduler.common.utils.*;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.ParamUtils;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * http task
 */
public class HttpTask extends AbstractTask {

    /**
     * http parameters
     */
    private HttpParameters httpParameters;

    /**
     * application json
     */
    protected static final String APPLICATION_JSON = "application/json";

    /**
     * output
     */
    protected String output;


    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    /**
     * constructor
     * @param taskExecutionContext     taskExecutionContext
     * @param logger    logger
     */
    public HttpTask(TaskExecutionContext taskExecutionContext, Logger logger) {
        super(taskExecutionContext, logger);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {
        logger.info("http task params {}", taskExecutionContext.getTaskParams());
        this.httpParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), HttpParameters.class);

        if (!httpParameters.checkParameters()) {
            throw new RuntimeException("http task params is not valid");
        }
    }

    @Override
    public void handle() throws Exception {
        String threadLoggerInfoName = String.format(Constants.TASK_LOG_INFO_FORMAT, taskExecutionContext.getTaskAppId());
        Thread.currentThread().setName(threadLoggerInfoName);

        long startTime = System.currentTimeMillis();
        String statusCode = null;
        String body = null;

        try(CloseableHttpClient client = createHttpClient();
            CloseableHttpResponse response = sendRequest(client)) {
            statusCode = String.valueOf(getStatusCode(response));
            body = getResponseBody(response);
            exitStatusCode = validResponse(body, statusCode);
            long costTime = System.currentTimeMillis() - startTime;
            logger.info("startTime: {}, httpUrl: {}, httpMethod: {}, costTime : {}Millisecond, statusCode : {}, body : {}, log : {}",
                    DateUtils.format2Readable(startTime), httpParameters.getUrl(),httpParameters.getHttpMethod(), costTime, statusCode, body, output);
        }catch (Exception e){
            appendMessage(e.toString());
            exitStatusCode = -1;
            logger.error("httpUrl[" + httpParameters.getUrl() + "] connection failed："+output, e);
            throw e;
        }
    }

    /**
     * send request
     * @param client client
     * @return CloseableHttpResponse
     * @throws IOException io exception
     */
    protected CloseableHttpResponse sendRequest(CloseableHttpClient client) throws IOException {
        RequestBuilder builder = createRequestBuilder();

        // replace placeholder
        Map<String, Property> paramsMap = ParamUtils.convert(ParamUtils.getUserDefParamsMap(taskExecutionContext.getDefinedParams()),
                taskExecutionContext.getDefinedParams(),
                httpParameters.getLocalParametersMap(),
                CommandType.of(taskExecutionContext.getCmdTypeIfComplement()),
                taskExecutionContext.getScheduleTime());
        List<HttpProperty> httpPropertyList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(httpParameters.getHttpParams() )){
            for (HttpProperty httpProperty: httpParameters.getHttpParams()) {
                String jsonObject = JSONUtils.toJsonString(httpProperty);
                String params = ParameterUtils.convertParameterPlaceholders(jsonObject,ParamUtils.convert(paramsMap));
                logger.info("http request params：{}",params);
                httpPropertyList.add(JSONUtils.parseObject(params,HttpProperty.class));
            }
        }
        addRequestParams(builder,httpPropertyList);
        String requestUrl = ParameterUtils.convertParameterPlaceholders(httpParameters.getUrl(),ParamUtils.convert(paramsMap));
        HttpUriRequest request = builder.setUri(requestUrl).build();
        setHeaders(request,httpPropertyList);
        return client.execute(request);
    }

    /**
     * get response body
     * @param httpResponse http response
     * @return response body
     * @throws ParseException parse exception
     * @throws IOException io exception
     */
    protected String getResponseBody(CloseableHttpResponse httpResponse) throws ParseException, IOException {
        if (httpResponse == null) {
            return null;
        }
        HttpEntity entity = httpResponse.getEntity();
        if (entity == null) {
            return null;
        }
        return EntityUtils.toString(entity, StandardCharsets.UTF_8.name());
    }

    /**
     * get status code
     * @param httpResponse http response
     * @return status code
     */
    protected int getStatusCode(CloseableHttpResponse httpResponse) {
        return httpResponse.getStatusLine().getStatusCode();
    }

    /**
     * valid response
     * @param body          body
     * @param statusCode    status code
     * @return exit status code
     */
    protected int validResponse(String body, String statusCode){
        int exitStatusCode = 0;
        switch (httpParameters.getHttpCheckCondition()) {
            case BODY_CONTAINS:
                if (StringUtils.isEmpty(body) || !body.contains(httpParameters.getCondition())) {
                    appendMessage(httpParameters.getUrl() + " doesn't contain "
                            + httpParameters.getCondition());
                    exitStatusCode = -1;
                }
                break;
            case BODY_NOT_CONTAINS:
                if (StringUtils.isEmpty(body) || body.contains(httpParameters.getCondition())) {
                    appendMessage(httpParameters.getUrl() + " contains "
                            + httpParameters.getCondition());
                    exitStatusCode = -1;
                }
                break;
            case STATUS_CODE_CUSTOM:
                if (!statusCode.equals(httpParameters.getCondition())) {
                    appendMessage(httpParameters.getUrl() + " statuscode: " + statusCode + ", Must be: " + httpParameters.getCondition());
                    exitStatusCode = -1;
                }
                break;
            default:
                if (!"200".equals(statusCode)) {
                    appendMessage(httpParameters.getUrl() + " statuscode: " + statusCode + ", Must be: 200");
                    exitStatusCode = -1;
                }
                break;
        }
        return exitStatusCode;
    }

    public String getOutput() {
        return output;
    }

    /**
     * append message
     * @param message message
     */
    protected void appendMessage(String message) {
        if (output == null) {
            output = "";
        }
        if (message != null && !message.trim().isEmpty()) {
            output += message;
        }
    }

    /**
     * add request params
     * @param builder           builder
     * @param httpPropertyList  http property list
     */
    protected void addRequestParams(RequestBuilder builder,List<HttpProperty> httpPropertyList) {
        if(CollectionUtils.isNotEmpty(httpPropertyList)){
            ObjectNode jsonParam = JSONUtils.createObjectNode();
            for (HttpProperty property: httpPropertyList){
                if(property.getHttpParametersType() != null){
                    if (property.getHttpParametersType().equals(HttpParametersType.PARAMETER)){
                        builder.addParameter(property.getProp(), property.getValue());
                    }else if(property.getHttpParametersType().equals(HttpParametersType.BODY)){
                        jsonParam.put(property.getProp(), property.getValue());
                    }
                }
            }
            StringEntity postingString = new StringEntity(jsonParam.toString(), Charsets.UTF_8);
            postingString.setContentEncoding(StandardCharsets.UTF_8.name());
            postingString.setContentType(APPLICATION_JSON);
            builder.setEntity(postingString);
        }
    }

    /**
     * set headers
     * @param request           request
     * @param httpPropertyList  http property list
     */
    protected void setHeaders(HttpUriRequest request,List<HttpProperty> httpPropertyList) {
        if(CollectionUtils.isNotEmpty(httpPropertyList)){
            for (HttpProperty property: httpPropertyList) {
                if (HttpParametersType.HEADERS.equals(property.getHttpParametersType())) {
                    request.addHeader(property.getProp(), property.getValue());
                }
            }
        }
    }

    /**
     * create http client
     * @return CloseableHttpClient
     */
    protected CloseableHttpClient createHttpClient() {
        final RequestConfig requestConfig = requestConfig();
        HttpClientBuilder httpClientBuilder;
        httpClientBuilder = HttpClients.custom().setDefaultRequestConfig(requestConfig);
        return httpClientBuilder.build();
    }

    /**
     * request config
     * @return RequestConfig
     */
    private RequestConfig requestConfig() {
        return RequestConfig.custom().setSocketTimeout(httpParameters.getSocketTimeout()).setConnectTimeout(httpParameters.getConnectTimeout()).build();
    }

    /**
     * create request builder
     * @return RequestBuilder
     */
    protected RequestBuilder createRequestBuilder() {
        if (httpParameters.getHttpMethod().equals(HttpMethod.GET)) {
            return RequestBuilder.get();
        } else if (httpParameters.getHttpMethod().equals(HttpMethod.POST)) {
            return RequestBuilder.post();
        } else if (httpParameters.getHttpMethod().equals(HttpMethod.HEAD)) {
            return RequestBuilder.head();
        } else if (httpParameters.getHttpMethod().equals(HttpMethod.PUT)) {
            return RequestBuilder.put();
        } else if (httpParameters.getHttpMethod().equals(HttpMethod.DELETE)) {
            return RequestBuilder.delete();
        } else {
            return null;
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return this.httpParameters;
    }
}
