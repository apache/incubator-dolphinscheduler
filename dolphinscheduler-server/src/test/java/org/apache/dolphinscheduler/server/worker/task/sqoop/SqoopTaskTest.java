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

package org.apache.dolphinscheduler.server.worker.task.sqoop;

import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.server.entity.SqoopTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.SqoopJobGenerator;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * sqoop task test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class SqoopTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(SqoopTaskTest.class);

    private SqoopTask sqoopTask;

    @Before
    public void before() {
        ProcessService processService = Mockito.mock(ProcessService.class);
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskAppId(String.valueOf(System.currentTimeMillis()));
        taskExecutionContext.setTenantCode("1");
        taskExecutionContext.setEnvFile(".dolphinscheduler_env.sh");
        taskExecutionContext.setStartTime(new Date());
        taskExecutionContext.setTaskTimeout(0);
        taskExecutionContext.setTaskParams("{\"jobName\":\"sqoop_import\",\"jobType\":\"TEMPLATE\",\"concurrency\":1,"
            + "\"modelType\":\"import\",\"sourceType\":\"MYSQL\",\"targetType\":\"HIVE\",\"sourceParams\":\"{\\\"srcDatasource\\\":2,\\\"srcTable\\\":\\\"person_2\\\","
            + "\\\"srcQueryType\\\":\\\"1\\\",\\\"srcQuerySql\\\":\\\"SELECT * FROM person_2\\\",\\\"srcColumnType\\\":\\\"0\\\","
            + "\\\"srcColumns\\\":\\\"\\\",\\\"srcConditionList\\\":[],\\\"mapColumnHive\\\":[],"
            + "\\\"mapColumnJava\\\":[{\\\"prop\\\":\\\"id\\\",\\\"direct\\\":\\\"IN\\\",\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"Integer\\\"}]}\""
            + ",\"targetParams\":\"{\\\"hiveDatabase\\\":\\\"stg\\\",\\\"hiveTable\\\":\\\"person_internal_2\\\",\\\"createHiveTable\\\":true,"
            + "\\\"dropDelimiter\\\":false,\\\"hiveOverWrite\\\":true,\\\"replaceDelimiter\\\":\\\"\\\",\\\"hivePartitionKey\\\":\\\"date\\\","
            + "\\\"hivePartitionValue\\\":\\\"2020-02-16\\\"}\",\"localParams\":[]}");

        sqoopTask = new SqoopTask(taskExecutionContext, logger);
        //test sqoop task init method
        sqoopTask.init();
    }

    /**
     * test SqoopJobGenerator
     */
    @Test
    public void testGenerator() {
        TaskExecutionContext mysqlTaskExecutionContext = getMysqlTaskExecutionContext();

        //sqoop TEMPLATE job
        //import mysql to HDFS with hadoop
        String mysqlToHdfs =
            "{\"jobName\":\"sqoop_import\",\"hadoopCustomParams\":[{\"prop\":\"mapreduce.map.memory.mb\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"4096\"}],"
                + "\"sqoopAdvancedParams\":[{\"prop\":\"--direct\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"\"}],\"jobType\":\"TEMPLATE\",\"concurrency\":1,"
                + "\"modelType\":\"import\",\"sourceType\":\"MYSQL\",\"targetType\":\"HDFS\","
                + "\"sourceParams\":\"{\\\"srcDatasource\\\":2,\\\"srcTable\\\":\\\"person_2\\\",\\\"srcQueryType\\\":\\\"0\\\",\\\"srcQuerySql\\\":\\\"\\\",\\\"srcColumnType\\\":\\\"0\\\","
                + "\\\"srcColumns\\\":\\\"\\\",\\\"srcConditionList\\\":[],\\\"mapColumnHive\\\":[],\\\"mapColumnJava\\\":[]}\",\"targetParams\":\"{\\\"targetPath\\\":\\\"/ods/tmp/test/person7\\\","
                + "\\\"deleteTargetDir\\\":true,\\\"fileType\\\":\\\"--as-textfile\\\",\\\"compressionCodec\\\":\\\"\\\",\\\"fieldsTerminated\\\":\\\"@\\\","
                + "\\\"linesTerminated\\\":\\\"\\\\\\\\n\\\"}\",\"localParams\":[]}";
        SqoopParameters mysqlToHdfsParams = JSONUtils.parseObject(mysqlToHdfs, SqoopParameters.class);
        SqoopJobGenerator generator = new SqoopJobGenerator();
        String mysqlToHdfsScript = generator.generateSqoopJob(mysqlToHdfsParams, mysqlTaskExecutionContext);
        String mysqlToHdfsExpected =
                "sqoop import -D mapred.job.name=sqoop_import -D mapreduce.map.memory.mb=4096 --direct  -m 1 --connect "
                        + "\"jdbc:mysql://192.168.0.111:3306/test?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false\" "
                        + "--username kylo --password \"123456\" --table person_2 --target-dir /ods/tmp/test/person7 --as-textfile "
                        + "--delete-target-dir --fields-terminated-by '@' --lines-terminated-by '\\n' --null-non-string 'NULL' --null-string 'NULL'";
        Assert.assertEquals(mysqlToHdfsExpected, mysqlToHdfsScript);

        //export hdfs to mysql using update mode
        String hdfsToMysql = "{\"jobName\":\"sqoop_import\",\"jobType\":\"TEMPLATE\",\"concurrency\":1,\"modelType\":\"export\",\"sourceType\":\"HDFS\","
            + "\"targetType\":\"MYSQL\",\"sourceParams\":\"{\\\"exportDir\\\":\\\"/ods/tmp/test/person7\\\"}\","
            + "\"targetParams\":\"{\\\"targetDatasource\\\":2,\\\"targetTable\\\":\\\"person_3\\\",\\\"targetColumns\\\":\\\"id,name,age,sex,create_time\\\","
            + "\\\"preQuery\\\":\\\"\\\",\\\"isUpdate\\\":true,\\\"targetUpdateKey\\\":\\\"id\\\",\\\"targetUpdateMode\\\":\\\"allowinsert\\\","
            + "\\\"fieldsTerminated\\\":\\\"@\\\",\\\"linesTerminated\\\":\\\"\\\\\\\\n\\\"}\",\"localParams\":[]}";
        SqoopParameters hdfsToMysqlParams = JSONUtils.parseObject(hdfsToMysql, SqoopParameters.class);
        String hdfsToMysqlScript = generator.generateSqoopJob(hdfsToMysqlParams, mysqlTaskExecutionContext);
        String hdfsToMysqlScriptExpected =
                "sqoop export -D mapred.job.name=sqoop_import -m 1 --export-dir /ods/tmp/test/person7 --connect "
                        + "\"jdbc:mysql://192.168.0.111:3306/test?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false\" "
                        + "--username kylo --password \"123456\" --table person_3 --columns id,name,age,sex,create_time --fields-terminated-by '@' "
                        + "--lines-terminated-by '\\n' --update-key id --update-mode allowinsert";
        Assert.assertEquals(hdfsToMysqlScriptExpected, hdfsToMysqlScript);

        //export hive to mysql
        String hiveToMysql =
            "{\"jobName\":\"sqoop_import\",\"jobType\":\"TEMPLATE\",\"concurrency\":1,\"modelType\":\"export\",\"sourceType\":\"HIVE\","
                + "\"targetType\":\"MYSQL\",\"sourceParams\":\"{\\\"hiveDatabase\\\":\\\"stg\\\",\\\"hiveTable\\\":\\\"person_internal\\\","
                + "\\\"hivePartitionKey\\\":\\\"date\\\",\\\"hivePartitionValue\\\":\\\"2020-02-17\\\"}\","
                + "\"targetParams\":\"{\\\"targetDatasource\\\":2,\\\"targetTable\\\":\\\"person_3\\\",\\\"targetColumns\\\":\\\"\\\",\\\"preQuery\\\":\\\"\\\","
                + "\\\"isUpdate\\\":false,\\\"targetUpdateKey\\\":\\\"\\\",\\\"targetUpdateMode\\\":\\\"allowinsert\\\",\\\"fieldsTerminated\\\":\\\"@\\\","
                + "\\\"linesTerminated\\\":\\\"\\\\\\\\n\\\"}\",\"localParams\":[]}";
        SqoopParameters hiveToMysqlParams = JSONUtils.parseObject(hiveToMysql, SqoopParameters.class);
        String hiveToMysqlScript = generator.generateSqoopJob(hiveToMysqlParams, mysqlTaskExecutionContext);
        String hiveToMysqlExpected =
                "sqoop export -D mapred.job.name=sqoop_import -m 1 --hcatalog-database stg --hcatalog-table person_internal --hcatalog-partition-keys date "
                        + "--hcatalog-partition-values 2020-02-17 --connect \"jdbc:mysql://192.168.0.111:3306/test?allowLoadLocalInfile="
                        + "false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false\" --username kylo --password \"123456\" --table person_3 "
                        + "--fields-terminated-by '@' --lines-terminated-by '\\n'";
        Assert.assertEquals(hiveToMysqlExpected, hiveToMysqlScript);

        //import mysql to hive
        String mysqlToHive =
            "{\"jobName\":\"sqoop_import\",\"jobType\":\"TEMPLATE\",\"concurrency\":1,\"modelType\":\"import\",\"sourceType\":\"MYSQL\",\"targetType\":\"HIVE\","
                + "\"sourceParams\":\"{\\\"srcDatasource\\\":2,\\\"srcTable\\\":\\\"person_2\\\",\\\"srcQueryType\\\":\\\"1\\\","
                + "\\\"srcQuerySql\\\":\\\"SELECT * FROM person_2\\\",\\\"srcColumnType\\\":\\\"0\\\",\\\"srcColumns\\\":\\\"\\\",\\\"srcConditionList\\\":[],"
                + "\\\"mapColumnHive\\\":[],\\\"mapColumnJava\\\":[{\\\"prop\\\":\\\"id\\\",\\\"direct\\\":\\\"IN\\\",\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"Integer\\\"}]}\","
                + "\"targetParams\":\"{\\\"hiveDatabase\\\":\\\"stg\\\",\\\"hiveTable\\\":\\\"person_internal_2\\\",\\\"createHiveTable\\\":true,\\\"dropDelimiter\\\":false,"
                + "\\\"hiveOverWrite\\\":true,\\\"replaceDelimiter\\\":\\\"\\\",\\\"hivePartitionKey\\\":\\\"date\\\",\\\"hivePartitionValue\\\":\\\"2020-02-16\\\"}\",\"localParams\":[]}";
        SqoopParameters mysqlToHiveParams = JSONUtils.parseObject(mysqlToHive, SqoopParameters.class);
        String mysqlToHiveScript = generator.generateSqoopJob(mysqlToHiveParams, mysqlTaskExecutionContext);
        String mysqlToHiveExpected =
            "sqoop import -D mapred.job.name=sqoop_import -m 1 --connect \"jdbc:mysql://192.168.0.111:3306/"
                    + "test?allowLoadLocalInfile=false&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false\" "
                    + "--username kylo --password \"123456\" "
                + "--query \"SELECT * FROM person_2 WHERE \\$CONDITIONS\" --map-column-java id=Integer --hive-import --hive-database stg --hive-table person_internal_2 "
                + "--create-hive-table --hive-overwrite --delete-target-dir --hive-partition-key date --hive-partition-value 2020-02-16";
                      Assert.assertEquals(mysqlToHiveExpected, mysqlToHiveScript);

        //sqoop CUSTOM job
        String sqoopCustomString = "{\"jobType\":\"CUSTOM\",\"localParams\":[],\"customShell\":\"sqoop import\"}";
        SqoopParameters sqoopCustomParams = JSONUtils.parseObject(sqoopCustomString, SqoopParameters.class);
        String sqoopCustomScript = generator.generateSqoopJob(sqoopCustomParams, new TaskExecutionContext());
        String sqoopCustomExpected = "sqoop import";
        Assert.assertEquals(sqoopCustomExpected, sqoopCustomScript);

    }

    /**
     * get taskExecutionContext include mysql
     *
     * @return TaskExecutionContext
     */
    private TaskExecutionContext getMysqlTaskExecutionContext() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        SqoopTaskExecutionContext sqoopTaskExecutionContext = new SqoopTaskExecutionContext();
        String mysqlSourceConnectionParams =
            "{\"address\":\"jdbc:mysql://192.168.0.111:3306\",\"database\":\"test\",\"jdbcUrl\":\"jdbc:mysql://192.168.0.111:3306/test\",\"user\":\"kylo\",\"password\":\"123456\"}";
        String mysqlTargetConnectionParams =
            "{\"address\":\"jdbc:mysql://192.168.0.111:3306\",\"database\":\"test\",\"jdbcUrl\":\"jdbc:mysql://192.168.0.111:3306/test\",\"user\":\"kylo\",\"password\":\"123456\"}";
        sqoopTaskExecutionContext.setDataSourceId(2);
        sqoopTaskExecutionContext.setDataTargetId(2);
        sqoopTaskExecutionContext.setSourcetype(0);
        sqoopTaskExecutionContext.setTargetConnectionParams(mysqlTargetConnectionParams);
        sqoopTaskExecutionContext.setSourceConnectionParams(mysqlSourceConnectionParams);
        sqoopTaskExecutionContext.setTargetType(0);
        taskExecutionContext.setSqoopTaskExecutionContext(sqoopTaskExecutionContext);
        return taskExecutionContext;
    }

    @Test
    public void testGetParameters() {
        Assert.assertNotNull(sqoopTask.getParameters());
    }

    /**
     * Method: init
     */
    @Test
    public void testInit() {
        try {
            sqoopTask.init();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testLogHandler() throws InterruptedException {
        LinkedBlockingQueue<String> loggerBuffer = new LinkedBlockingQueue<>();
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                loggerBuffer.add("test add log");
            }
        });
        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                sqoopTask.logHandle(loggerBuffer);
            }
        });
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        // if no exception throw, assert true
        Assert.assertTrue(true);
    }

}
