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
package org.apache.dolphinscheduler.alert.email;


import org.apache.dolphinscheduler.spi.alert.AlertResult;

import java.util.List;
import static java.util.Objects.requireNonNull;

/**
 * email send manager
 */
public class EmailManager {

    private MailUtils mailUtils;

    /**
     *
     * @param mailUtils
     */
    public EmailManager(MailUtils mailUtils){
        requireNonNull(mailUtils, "mailUtils must not null");
        this.mailUtils = mailUtils;
    }

    /**
     * email send
     * @param receviersList the receiver list
     * @param receviersCcList the cc List
     * @param title the title
     * @param content the content
     * @param showType the showType
     * @return the send result
     */
    public AlertResult send(List<String> receviersList, List<String> receviersCcList, String title, String content, String showType){

        return mailUtils.sendMails(receviersList, receviersCcList, title, content, showType);
    }

    /**
     * msg send
     * @param receviersList the receiver list
     * @param title the title
     * @param content the content
     * @param showType the showType
     * @return the send result
     */
    public AlertResult send(List<String> receviersList,String title,String content,String showType){

        return mailUtils.sendMails(receviersList,title, content, showType);
    }
}
