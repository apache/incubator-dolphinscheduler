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

package org.apache.dolphinscheduler.api.vo;

import org.apache.dolphinscheduler.api.utils.PageInfo;

import java.io.Serializable;
import java.util.List;

public class PageListVO<T> implements Serializable {
    private List<T> totalList;
    private Integer currentPage;
    private Integer totalPage;
    private Integer total;

    public PageListVO(PageInfo<T> pageInfo) {
        this.totalList = pageInfo.getLists();
        this.currentPage = pageInfo.getCurrentPage();
        this.totalPage = pageInfo.getTotalPage();
        this.total = pageInfo.getTotalCount();
    }

    public PageListVO() {

    }

    public List<T> getTotalList() {
        return totalList;
    }

    public void setTotalList(List<T> totalList) {
        this.totalList = totalList;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "PageListVO{"
                + "totalList=" + totalList
                + ", currentPage=" + currentPage
                + ", totalPage=" + totalPage
                + ", total=" + total + '}';
    }
}
