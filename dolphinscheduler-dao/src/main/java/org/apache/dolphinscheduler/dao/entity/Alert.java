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
package org.apache.dolphinscheduler.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.ShowType;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@TableName("t_ds_alert")
public class Alert {
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    @TableField(value = "title")
    private String title;
    @TableField(value = "show_type")
    private ShowType showType;
    @TableField(value = "content")
    private String content;
    @TableField(value = "alert_type")
    private AlertType alertType;
    @TableField(value = "alert_status")
    private AlertStatus alertStatus;
    @TableField(value = "log")
    private String log;
    @TableField("alertgroup_id")
    private int alertGroupId;
    @TableField("receivers")
    private String receivers;
    @TableField("receivers_cc")
    private String receiversCc;
    @TableField("create_time")
    private Date createTime;
    @TableField("update_time")
    private Date updateTime;
    @TableField(exist = false)
    private Map<String, Object> info = new HashMap<>();

    public Alert() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ShowType getShowType() {
        return showType;
    }

    public void setShowType(ShowType showType) {
        this.showType = showType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public AlertStatus getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(AlertStatus alertStatus) {
        this.alertStatus = alertStatus;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public int getAlertGroupId() {
        return alertGroupId;
    }

    public void setAlertGroupId(int alertGroupId) {
        this.alertGroupId = alertGroupId;
    }

    public String getReceivers() {
        return receivers;
    }

    public void setReceivers(String receivers) {
        this.receivers = receivers;
    }

    public String getReceiversCc() {
        return receiversCc;
    }

    public void setReceiversCc(String receiversCc) {
        this.receiversCc = receiversCc;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Map<String, Object> getInfo() {
        return info;
    }

    public void setInfo(Map<String, Object> info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", showType=" + showType +
                ", content='" + content + '\'' +
                ", alertType=" + alertType +
                ", alertStatus=" + alertStatus +
                ", log='" + log + '\'' +
                ", alertGroupId=" + alertGroupId +
                ", receivers='" + receivers + '\'' +
                ", receiversCc='" + receiversCc + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", info=" + info +
                '}';
    }
}
