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

package org.apache.dolphinscheduler.data.quality.flow.reader;

import static org.apache.dolphinscheduler.data.quality.Constants.DATABASE;
import static org.apache.dolphinscheduler.data.quality.Constants.DRIVER;
import static org.apache.dolphinscheduler.data.quality.Constants.PASSWORD;
import static org.apache.dolphinscheduler.data.quality.Constants.TABLE;
import static org.apache.dolphinscheduler.data.quality.Constants.URL;
import static org.apache.dolphinscheduler.data.quality.Constants.USER;

import org.apache.dolphinscheduler.data.quality.config.Config;
import org.apache.dolphinscheduler.data.quality.flow.FlowTestBase;
import org.apache.dolphinscheduler.data.quality.flow.batch.reader.JdbcReader;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * JdbcConnectorTest
 */
public class JdbcReaderTest extends FlowTestBase {

    @Before
    public void before() {
        super.before();
        createConnectorTable();
    }

    @Test
    public void testJdbcConnectorExecute() {
        JdbcReader jdbcConnector = new JdbcReader(buildReaderConfig());
        jdbcConnector.read(sparkRuntimeEnvironment);
    }

    private Config buildReaderConfig() {
        Map<String,Object> config = new HashMap<>();
        config.put(DATABASE,"test");
        config.put(TABLE,"test.test1");
        config.put(URL,url);
        config.put(USER,"test");
        config.put(PASSWORD,"123456");
        config.put(DRIVER,driver);
        return new Config(config);
    }

    private void createConnectorTable() {
        try {
            Connection connection = getConnection();
            connection.prepareStatement("create schema if not exists test").executeUpdate();

            connection.prepareStatement("drop table if exists test.test1").executeUpdate();
            connection
                    .prepareStatement(
                            "CREATE TABLE test.test1 (\n"
                                    + "  `id` int(11) NOT NULL AUTO_INCREMENT,\n"
                                    + "  `company` varchar(255) DEFAULT NULL,\n"
                                    + "  `date` varchar(255) DEFAULT NULL,\n"
                                    + "  `c1` varchar(255) DEFAULT NULL,\n"
                                    + "  `c2` varchar(255) DEFAULT NULL,\n"
                                    + "  `c3` varchar(255) DEFAULT NULL,\n"
                                    + "  `c4` int(11) DEFAULT NULL,\n"
                                    + "  PRIMARY KEY (`id`)\n"
                                    + ")")
                    .executeUpdate();
            connection.prepareStatement("INSERT INTO test.test1 (company,`date`,c1,c2,c3,c4) VALUES\n"
                    + "\t ('1','2019-03-01','11','12','13',1),\n"
                    + "\t ('2','2019-06-01','21','22','23',1),\n"
                    + "\t ('3','2019-09-01','31','32','33',1),\n"
                    + "\t ('4','2019-12-01','41','42','43',1),\n"
                    + "\t ('5','2013','42','43','54',1),\n"
                    + "\t ('6','2020','42','43','54',1);").executeUpdate();
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
