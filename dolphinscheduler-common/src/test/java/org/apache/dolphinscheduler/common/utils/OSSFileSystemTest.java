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
package org.apache.dolphinscheduler.common.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *  please config oss properties on {@code common.properties} if want to run test
 *  <pre>
 *   resource.storage.type=OSS
 *   fs.oss.bucket=xxxxx
 *   fs.oss.endpoint=http://oss-cn-zhangjiakou.aliyuncs.com
 *   fs.oss.accessKeyId=xxxxx
 *   fs.oss.accessKeySecret=xxxx
 *  </pre>
 */
public class OSSFileSystemTest {
    private HadoopUtils hadoopUtils = HadoopUtils.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(OSSFileSystemTest.class);

    //@Test
    public void test() throws Exception {
        Path base = Paths.get(OSSFileSystemTest.class.getResource("/").toURI());
        String dataFile = base.resolve("data.txt").toString();
        String dataLogFile = base.resolve("data.log").toString();
        String content = "oss test";

        logger.info("write data to file(path={})", dataFile);
        if (!FileUtils.writeContent2File(content, dataFile)) {
            throw new IllegalStateException("write data to file error");
        }

        Assert.assertTrue(
                hadoopUtils.copyLocalToHdfs(dataFile, "/data.txt", true, true));

        Assert.assertTrue(
                hadoopUtils.exists("/data.txt"));

        hadoopUtils.copyHdfsToLocal(
                "/data.txt",
                base.resolve("data.log").toString(),
                true,
                true);

        try(FileInputStream is = new FileInputStream(base.resolve("data.log").toFile())) {
            Assert.assertEquals(content, FileUtils.readFile2Str(is));
        }

        FileUtils.deleteFile(dataLogFile);
        hadoopUtils.delete("/data.txt", false);
    }
}
