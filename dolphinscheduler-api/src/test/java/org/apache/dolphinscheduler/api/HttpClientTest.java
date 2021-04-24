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
package org.apache.dolphinscheduler.api;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientTest {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientTest.class);

    @Test
    public void doPOSTParam() throws Exception {
        // create HttpClient
        CloseableHttpClient httpclient = HttpClients.createDefault();

        // create http post request
        HttpPost httpPost = new HttpPost("http://localhost:12345/dolphinscheduler/projects/create");
        httpPost.setHeader("token", "123");
        // set parameters
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("projectName", "qzw"));
        parameters.add(new BasicNameValuePair("desc", "qzw"));

        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters);
        httpPost.setEntity(formEntity);


        CloseableHttpResponse response = null;
        try {
            // execute
            response = httpclient.execute(httpPost);
            // response status code 200
            if (response.getStatusLine().getStatusCode() == 200) {
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                logger.info(content);
            }
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
    }

    /**
     * do get param path variables chinese
     */
    @Test
    public void doGETParamPathVariableAndChinese() throws Exception {
        // create HttpClient
        CloseableHttpClient httpclient = HttpClients.createDefault();

        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        // parameters.add(new BasicNameValuePair("pageSize", "10"));

        // define the parameters of the request
        URI uri = new URIBuilder("http://localhost:12345/dolphinscheduler/projects/%E5%85%A8%E9%83%A8%E6%B5%81%E7%A8%8B%E6%B5%8B%E8%AF%95/process/list")
                .build();

        // create http GET request
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeader("token","10f5625a2a1cbf9aa710653796c5d764");
        //response object
        CloseableHttpResponse response = null;
        try {
            // execute http get request
            response = httpclient.execute(httpGet);
            // response status code 200
            if (response.getStatusLine().getStatusCode() == 200) {
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                logger.info("start--------------->");
                logger.info(content);
                logger.info("end----------------->");
            }
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
    }

    /**
     * do get param
     */
    @Test
    public void doGETParam() throws Exception {
        // create HttpClient
        CloseableHttpClient httpclient = HttpClients.createDefault();

        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("startDate", "2018-04-22 19:30:08"));
        parameters.add(new BasicNameValuePair("endDate", "2028-04-22 19:30:08"));
        parameters.add(new BasicNameValuePair("projectId", "0"));

        // define the parameters of the request
        URI uri = new URIBuilder("http://localhost:12345/dolphinscheduler/projects/analysis/queue-count")
                .setParameters(parameters)
                .build();

        // create http GET request
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeader("token","2aef24c052c212fab9eec78848c2258b");
        //response object
        CloseableHttpResponse response = null;
        try {
            // execute http get request
            response = httpclient.execute(httpGet);
            // response status code 200
            if (response.getStatusLine().getStatusCode() == 200) {
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                logger.info("start--------------->");
                logger.info(content);
                logger.info("end----------------->");
            }
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
    }

}
