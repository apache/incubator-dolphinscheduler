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
package org.apache.dolphinscheduler.dao.datasource;

import org.apache.dolphinscheduler.common.enums.DbConnectType;
import org.junit.Assert;
import org.junit.Test;

public class OracleDataSourceTest {

    @Test
    public void testGetOracleJdbcUrl() {
        OracleDataSource oracleDataSource = new OracleDataSource();
        oracleDataSource.setConnectType(DbConnectType.ORACLE_SERVICE_NAME);
        oracleDataSource.setAddress("jdbc:oracle:thin:@//127.0.0.1:1521");
        oracleDataSource.setDatabase("test");
        oracleDataSource.setPassword("123456");
        oracleDataSource.setUser("test");
        Assert.assertEquals("jdbc:oracle:thin:@//127.0.0.1:1521/test", oracleDataSource.getJdbcUrl());
        //set fake principal
        oracleDataSource.setPrincipal("fake principal");
        Assert.assertEquals("jdbc:oracle:thin:@//127.0.0.1:1521/test", oracleDataSource.getJdbcUrl());
        //set fake other
        oracleDataSource.setOther("charset=UTF-8");
        Assert.assertEquals("jdbc:oracle:thin:@//127.0.0.1:1521/test?charset=UTF-8", oracleDataSource.getJdbcUrl());

        OracleDataSource oracleDataSource2 = new OracleDataSource();
        oracleDataSource2.setAddress("jdbc:oracle:thin:@127.0.0.1:1521");
        oracleDataSource2.setDatabase("orcl");
        oracleDataSource2.setPassword("123456");
        oracleDataSource2.setUser("test");
        oracleDataSource2.setConnectType(DbConnectType.ORACLE_SID);
        Assert.assertEquals("jdbc:oracle:thin:@127.0.0.1:1521:orcl", oracleDataSource2.getJdbcUrl());
        //set fake principal
        oracleDataSource2.setPrincipal("fake principal");
        Assert.assertEquals("jdbc:oracle:thin:@127.0.0.1:1521:orcl", oracleDataSource2.getJdbcUrl());
        //set fake other
        oracleDataSource2.setOther("charset=UTF-8");
        Assert.assertEquals("jdbc:oracle:thin:@127.0.0.1:1521:orcl?charset=UTF-8", oracleDataSource2.getJdbcUrl());
    }

    @Test
    public void testAppendDatabase() {
        OracleDataSource oracleDataSource = new OracleDataSource();
        oracleDataSource.setAddress("jdbc:oracle:thin:@//127.0.0.1:1521");
        oracleDataSource.setDatabase("test");
        oracleDataSource.setConnectType(DbConnectType.ORACLE_SERVICE_NAME);
        StringBuilder jdbcUrl = new StringBuilder(oracleDataSource.getAddress());
        oracleDataSource.appendDatabase(jdbcUrl);
        Assert.assertEquals("jdbc:oracle:thin:@//127.0.0.1:1521/test", jdbcUrl.toString());

        OracleDataSource oracleDataSource2 = new OracleDataSource();
        oracleDataSource2.setAddress("jdbc:oracle:thin:@127.0.0.1:1521");
        oracleDataSource2.setDatabase("orcl");
        oracleDataSource2.setConnectType(DbConnectType.ORACLE_SID);
        StringBuilder jdbcUrl2 = new StringBuilder(oracleDataSource2.getAddress());
        oracleDataSource2.appendDatabase(jdbcUrl2);
        Assert.assertEquals("jdbc:oracle:thin:@127.0.0.1:1521:orcl", jdbcUrl2.toString());
    }
}