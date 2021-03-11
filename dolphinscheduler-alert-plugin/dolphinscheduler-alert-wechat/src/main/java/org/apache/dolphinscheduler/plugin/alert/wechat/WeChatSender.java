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

package org.apache.dolphinscheduler.plugin.alert.wechat;

import static java.util.Objects.requireNonNull;

import org.apache.dolphinscheduler.plugin.alert.wechat.exception.WeChatAlertException;
import org.apache.dolphinscheduler.spi.alert.AlertConstants;
import org.apache.dolphinscheduler.spi.alert.AlertResult;
import org.apache.dolphinscheduler.spi.alert.ShowType;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WeChatSender
 */
public class WeChatSender {

    private static Logger logger = LoggerFactory.getLogger(WeChatSender.class);

    private String weChatAgentId;

    private String weChatUsers;

    private String weChatUserSendMsg;

    private String weChatTokenUrlReplace;

    private String weChatToken;

    private String showType;


    private static final String MUST_NOT_NULL = " must not null";
    private static final String ALERT_STATUS = "false";
    private static final String AGENT_ID_REG_EXP = "{agentId}";
    private static final String MSG_REG_EXP = "{msg}";
    private static final String USER_REG_EXP = "{toUser}";
    private static final String CORP_ID_REGEX = "{corpId}";
    private static final String SECRET_REGEX = "{secret}";
    private static final String TOKEN_REGEX = "{token}";

    WeChatSender(Map<String, String> config) {
        weChatAgentId = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_AGENT_ID);
        weChatUsers = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_USERS);
        String weChatCorpId = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_CORP_ID);
        String weChatSecret = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_SECRET);
        String weChatTokenUrl = WeChatAlertConstants.WE_CHAT_TOKEN_URL;
        weChatUserSendMsg = config.get(WeChatAlertParamsConstants.NAME_ENTERPRISE_WE_CHAT_USER_SEND_MSG);
        showType = config.get(AlertConstants.SHOW_TYPE);
        requireNonNull(showType, AlertConstants.SHOW_TYPE + MUST_NOT_NULL);
        weChatTokenUrlReplace = weChatTokenUrl
            .replace(CORP_ID_REGEX, weChatCorpId)
            .replace(SECRET_REGEX, weChatSecret);
        weChatToken = getToken();
    }

    /**
     * make user multi user message
     *
     * @param toUser the toUser
     * @param agentId the agentId
     * @param msg the msg
     * @return Enterprise WeChat send message
     */
    private String makeUserSendMsg(Collection<String> toUser, String agentId, String msg) {
        String listUser = mkString(toUser);
        return weChatUserSendMsg.replace(USER_REG_EXP, listUser)
            .replace(AGENT_ID_REG_EXP, agentId)
            .replace(MSG_REG_EXP, msg);
    }

    /**
     * send Enterprise WeChat
     *
     * @return Enterprise WeChat resp, demo: {"errcode":0,"errmsg":"ok","invaliduser":""}
     */
    public AlertResult sendEnterpriseWeChat(String title, String content) {
        AlertResult alertResult;
        List<String> userList = Arrays.asList(weChatUsers.split(","));
        String data = markdownByAlert(title, content);
        String msg = makeUserSendMsg(userList, weChatAgentId, data);
        if (null == weChatToken) {
            alertResult = new AlertResult();
            alertResult.setMessage("send we chat alert fail,get weChat token error");
            alertResult.setStatus(ALERT_STATUS);
            return alertResult;
        }
        String enterpriseWeChatPushUrlReplace = WeChatAlertConstants.WE_CHAT_PUSH_URL.replace(TOKEN_REGEX, weChatToken);

        try {
            return checkWeChatSendMsgResult(post(enterpriseWeChatPushUrlReplace, msg));
        } catch (Exception e) {
            logger.info("send we chat alert msg  exception : {}", e.getMessage());
            alertResult = new AlertResult();
            alertResult.setMessage("send we chat alert fail");
            alertResult.setStatus(ALERT_STATUS);
        }
        return alertResult;
    }

    private static String post(String url, String data) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new StringEntity(data, WeChatAlertConstants.CHARSET));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String resp;
            try {
                HttpEntity entity = response.getEntity();
                resp = EntityUtils.toString(entity, WeChatAlertConstants.CHARSET);
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
            logger.info("Enterprise WeChat send [{}], param:{}, resp:{}",
                url, data, resp);
            return resp;
        }
    }

    /**
     * convert table to markdown style
     *
     * @param title the title
     * @param content the content
     * @return markdown table content
     */
    private static String markdownTable(String title, String content) {
        List<LinkedHashMap> mapItemsList = JSONUtils.toList(content, LinkedHashMap.class);
        if (null == mapItemsList || mapItemsList.isEmpty()) {
            logger.error("itemsList is null");
            throw new WeChatAlertException("itemsList is null");
        }
        StringBuilder contents = new StringBuilder(200);
        for (LinkedHashMap mapItems : mapItemsList) {
            Set<Entry<String, Object>> entries = mapItems.entrySet();
            Iterator<Entry<String, Object>> iterator = entries.iterator();
            StringBuilder t = new StringBuilder(String.format("`%s`%s", title, WeChatAlertConstants.MARKDOWN_ENTER));

            while (iterator.hasNext()) {

                Map.Entry<String, Object> entry = iterator.next();
                t.append(WeChatAlertConstants.MARKDOWN_QUOTE);
                t.append(entry.getKey()).append(":").append(entry.getValue());
                t.append(WeChatAlertConstants.MARKDOWN_ENTER);
            }
            contents.append(t);
        }

        return contents.toString();
    }

    /**
     * convert text to markdown style
     *
     * @param title the title
     * @param content the content
     * @return markdown text
     */
    private static String markdownText(String title, String content) {
        if (StringUtils.isNotEmpty(content)) {
            List<LinkedHashMap> mapItemsList = JSONUtils.toList(content, LinkedHashMap.class);
            if (null == mapItemsList || mapItemsList.isEmpty()) {
                logger.error("itemsList is null");
                throw new RuntimeException("itemsList is null");
            }

            StringBuilder contents = new StringBuilder(100);
            contents.append(String.format("`%s`%n", title));
            for (LinkedHashMap mapItems : mapItemsList) {

                Set<Map.Entry<String, Object>> entries = mapItems.entrySet();
                for (Entry<String, Object> entry : entries) {
                    contents.append(WeChatAlertConstants.MARKDOWN_QUOTE);
                    contents.append(entry.getKey()).append(":").append(entry.getValue());
                    contents.append(WeChatAlertConstants.MARKDOWN_ENTER);
                }

            }
            return contents.toString();
        }
        return null;
    }

    /**
     * Determine the markdown style based on the show type of the alert
     *
     * @return the markdown alert table/text
     */
    private String markdownByAlert(String title, String content) {
        String result = "";
        if (showType.equals(ShowType.TABLE.getDescp())) {
            result = markdownTable(title, content);
        } else if (showType.equals(ShowType.TEXT.getDescp())) {
            result = markdownText(title, content);
        }
        return result;

    }

    private String getToken() {
        try {
            return get(weChatTokenUrlReplace);
        } catch (IOException e) {
            logger.info("we chat alert get token error{}", e.getMessage());
        }
        return null;
    }

    private static String get(String url) throws IOException {
        String resp;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                resp = EntityUtils.toString(entity, WeChatAlertConstants.CHARSET);
                EntityUtils.consume(entity);
            }

            HashMap<String, Object> map = JSONUtils.parseObject(resp, HashMap.class);
            if (map != null && null != map.get("access_token")) {
                return map.get("access_token").toString();
            } else {
                return null;
            }
        }
    }

    private static String mkString(Iterable<String> list) {

        if (null == list || StringUtils.isEmpty("|")) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : list) {
            if (first) {
                first = false;
            } else {
                sb.append("|");
            }
            sb.append(item);
        }
        return sb.toString();
    }

    public static class WeChatSendMsgResponse {
        private Integer errcode;
        private String errmsg;

        public Integer getErrcode() {
            return errcode;
        }

        public void setErrcode(Integer errcode) {
            this.errcode = errcode;
        }

        public String getErrmsg() {
            return errmsg;
        }

        public void setErrmsg(String errmsg) {
            this.errmsg = errmsg;
        }
    }

    private static AlertResult checkWeChatSendMsgResult(String result) {
        AlertResult alertResult = new AlertResult();
        alertResult.setStatus(ALERT_STATUS);

        if (null == result) {
            alertResult.setMessage("we chat send fail");
            logger.info("send we chat msg error,resp is null");
            return alertResult;
        }
        WeChatSendMsgResponse sendMsgResponse = JSONUtils.parseObject(result, WeChatSendMsgResponse.class);
        if (null == sendMsgResponse) {
            alertResult.setMessage("we chat send fail");
            logger.info("send we chat msg error,resp error");
            return alertResult;
        }
        if (sendMsgResponse.errcode == 0) {
            alertResult.setStatus("true");
            alertResult.setMessage("we chat alert send success");
            return alertResult;
        }
        alertResult.setStatus(ALERT_STATUS);
        alertResult.setMessage(sendMsgResponse.getErrmsg());
        return alertResult;
    }
}
