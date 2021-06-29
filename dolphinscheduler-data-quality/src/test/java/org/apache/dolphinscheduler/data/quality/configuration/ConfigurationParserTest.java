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

package org.apache.dolphinscheduler.data.quality.configuration;

import org.apache.dolphinscheduler.data.quality.config.DataQualityConfiguration;
import org.apache.dolphinscheduler.data.quality.utils.JsonUtils;

import org.junit.Assert;
import org.junit.Test;

/**
 * ConfigurationParserTest
 */
public class ConfigurationParserTest {

    @Test
    public void testConfigurationValidate() {
        Assert.assertEquals(1,verifyConfigurationValidate());
    }

    private int verifyConfigurationValidate() {
        int flag = 1;
        try {
            String parameterStr = "{\"name\":\"data quality test\",\"env\":{\"type\":\"batch\",\"config\":null},"
                    + "\"readers\":[{\"type\":\"JDBC\",\"config\":{\"database\":\"test\",\"password\":\"Test@123!\","
                    + "\"driver\":\"com.mysql.jdbc.Driver\",\"user\":\"test\",\"output_table\":\"test1\",\"table\":\"test1\","
                    + "\"url\":\"jdbc:mysql://172.16.100.199:3306/test\"} }],\"transformers\":[{\"type\":\"sql\",\"config\":"
                    + "{\"index\":1,\"output_table\":\"miss_count\",\"sql\":\"SELECT COUNT(*) AS miss FROM test1 WHERE (c1 is null or c1 = '') \"} },"
                    + "{\"type\":\"sql\",\"config\":{\"index\":2,\"output_table\":\"total_count\",\"sql\":\"SELECT COUNT(*) AS total FROM test1 \"} }],"
                    + "\"writers\":[{\"type\":\"JDBC\",\"config\":{\"database\":\"dolphinscheduler\",\"password\":\"test\","
                    + "\"driver\":\"org.postgresql.Driver\",\"user\":\"test\",\"table\":\"t_ds_dq_execute_result\","
                    + "\"url\":\"jdbc:postgresql://172.16.100.199:5432/dolphinscheduler?stringtype=unspecified\","
                    + "\"sql\":\"SELECT 0 as rule_type,'data quality test' as rule_name,7 as process_definition_id,80 as process_instance_id,"
                    + "80 as task_instance_id,miss_count.miss AS statistics_value, total_count.total AS comparison_value,2 as check_type,10 as"
                    + " threshold, 3 as operator, 0 as failure_strategy, '2021-06-29 10:18:59' as create_time,'2021-06-29 10:18:59' as update_time "
                    + "from miss_count FULL JOIN total_count\"} }]}";

            DataQualityConfiguration dataQualityConfiguration = JsonUtils.fromJson(parameterStr,DataQualityConfiguration.class);
            dataQualityConfiguration.validate();
        } catch (Exception e) {
            flag = 0;
            e.printStackTrace();
        }
        return flag;
    }
}
