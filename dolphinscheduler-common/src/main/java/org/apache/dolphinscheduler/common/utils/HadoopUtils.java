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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.io.IOUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.client.cli.RMAdminCLI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.dolphinscheduler.common.Constants.RESOURCE_UPLOAD_PATH;

/**
 * hadoop utils
 * single instance
 */
public class HadoopUtils implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(HadoopUtils.class);

    private static String hdfsUser = PropertyUtils.getString(Constants.HDFS_ROOT_USER);
    public static final String resourceUploadPath = PropertyUtils.getString(RESOURCE_UPLOAD_PATH, "/dolphinscheduler");
    public static final String rmHaIds = PropertyUtils.getString(Constants.YARN_RESOURCEMANAGER_HA_RM_IDS);
    public static final String appAddress = PropertyUtils.getString(Constants.YARN_APPLICATION_STATUS_ADDRESS);
    public static final String jobHistoryAddress = PropertyUtils.getString(Constants.YARN_JOB_HISTORY_STATUS_ADDRESS);

    private static final String HADOOP_UTILS_KEY = "HADOOP_UTILS_KEY";

    private static final LoadingCache<String, HadoopUtils> cache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(PropertyUtils.getInt(Constants.KERBEROS_EXPIRE_TIME, 2), TimeUnit.HOURS)
            .build(new CacheLoader<String, HadoopUtils>() {
                @Override
                public HadoopUtils load(String key) throws Exception {
                    return new HadoopUtils();
                }
            });

    private static volatile boolean yarnEnabled = false;

    private Configuration configuration;
    private FileSystem fs;

    private HadoopUtils() {
        init();
        initHdfsPath();
    }

    public static HadoopUtils getInstance() {

        return cache.getUnchecked(HADOOP_UTILS_KEY);
    }

    /**
     * init dolphinscheduler root path in hdfs
     */

    private void initHdfsPath() {
        Path path = new Path(resourceUploadPath);

        try {
            if (!fs.exists(path)) {
                fs.mkdirs(path);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    /**
     * init hadoop configuration
     */
    private void init() {
        try {
            configuration = new Configuration();

            String resourceStorageType = PropertyUtils.getUpperCaseString(Constants.RESOURCE_STORAGE_TYPE);
            ResUploadType resUploadType = ResUploadType.valueOf(resourceStorageType);

            if (resUploadType == ResUploadType.HDFS) {
                if (PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false)) {
                    System.setProperty(Constants.JAVA_SECURITY_KRB5_CONF,
                            PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH));
                    configuration.set(Constants.HADOOP_SECURITY_AUTHENTICATION, "kerberos");
                    hdfsUser = "";
                    UserGroupInformation.setConfiguration(configuration);
                    UserGroupInformation.loginUserFromKeytab(PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME),
                            PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH));
                }

                String defaultFS = configuration.get(Constants.FS_DEFAULTFS);
                //first get key from core-site.xml hdfs-site.xml ,if null ,then try to get from properties file
                // the default is the local file system
                if (defaultFS.startsWith("file")) {
                    String defaultFSProp = PropertyUtils.getString(Constants.FS_DEFAULTFS);
                    if (StringUtils.isNotBlank(defaultFSProp)) {
                        Map<String, String> fsRelatedProps = PropertyUtils.getPrefixedProperties("fs.");
                        configuration.set(Constants.FS_DEFAULTFS, defaultFSProp);
                        fsRelatedProps.forEach((key, value) -> configuration.set(key, value));
                    } else {
                        logger.error("property:{} can not to be empty, please set!", Constants.FS_DEFAULTFS);
                        throw new RuntimeException(
                                String.format("property: %s can not to be empty, please set!", Constants.FS_DEFAULTFS)
                        );
                    }
                } else {
                    logger.info("get property:{} -> {}, from core-site.xml hdfs-site.xml ", Constants.FS_DEFAULTFS, defaultFS);
                }

                if (fs == null) {
                    if (StringUtils.isNotEmpty(hdfsUser)) {
                        UserGroupInformation ugi = UserGroupInformation.createRemoteUser(hdfsUser);
                        ugi.doAs(new PrivilegedExceptionAction<Boolean>() {
                            @Override
                            public Boolean run() throws Exception {
                                fs = FileSystem.get(configuration);
                                return true;
                            }
                        });
                    } else {
                        logger.warn("hdfs.root.user is not set value!");
                        fs = FileSystem.get(configuration);
                    }
                }
            } else if (resUploadType == ResUploadType.S3) {
                System.setProperty(Constants.AWS_S3_V4, Constants.STRING_TRUE);
                configuration.set(Constants.FS_DEFAULTFS, PropertyUtils.getString(Constants.FS_DEFAULTFS));
                configuration.set(Constants.FS_S3A_ENDPOINT, PropertyUtils.getString(Constants.FS_S3A_ENDPOINT));
                configuration.set(Constants.FS_S3A_ACCESS_KEY, PropertyUtils.getString(Constants.FS_S3A_ACCESS_KEY));
                configuration.set(Constants.FS_S3A_SECRET_KEY, PropertyUtils.getString(Constants.FS_S3A_SECRET_KEY));
                fs = FileSystem.get(configuration);
            } else if (resUploadType == ResUploadType.OSS) {
                configuration.set("fs.oss.impl", "org.apache.hadoop.fs.aliyun.oss.AliyunOSSFileSystem");
                configuration.set(Constants.FS_DEFAULTFS, PropertyUtils.getString(Constants.FS_DEFAULTFS));
                configuration.set(Constants.FS_OSS_ACCESS_KEY_ID, PropertyUtils.getString(Constants.FS_OSS_ACCESS_KEY_ID));
                configuration.set(Constants.FS_OSS_ACCESS_KEY_SECRET, PropertyUtils.getString(Constants.FS_OSS_ACCESS_KEY_SECRET));
                configuration.set(Constants.FS_OSS_ENDPOINT, PropertyUtils.getString(Constants.FS_OSS_ENDPOINT));
                fs = FileSystem.get(new URI("oss://" + PropertyUtils.getString(Constants.FS_OSS_BUCKET)), configuration);
            }


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @return Configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * get application url
     *
     * @param applicationId application id
     * @return url of application
     */
    public String getApplicationUrl(String applicationId) throws Exception {
        /**
         * if rmHaIds contains xx, it signs not use resourcemanager
         * otherwise:
         *  if rmHaIds is empty, single resourcemanager enabled
         *  if rmHaIds not empty: resourcemanager HA enabled
         */
        String appUrl = "";

        if (StringUtils.isEmpty(rmHaIds)) {
            //single resourcemanager enabled
            appUrl = appAddress;
            yarnEnabled = true;
        } else {
            //resourcemanager HA enabled
            appUrl = getAppAddress(appAddress, rmHaIds);
            yarnEnabled = true;
            logger.info("application url : {}", appUrl);
        }

        if (StringUtils.isBlank(appUrl)) {
            throw new Exception("application url is blank");
        }
        return String.format(appUrl, applicationId);
    }

    public String getJobHistoryUrl(String applicationId) {
        //eg:application_1587475402360_712719 -> job_1587475402360_712719
        String jobId = applicationId.replace("application", "job");
        return String.format(jobHistoryAddress, jobId);
    }

    /**
     * cat file on hdfs
     *
     * @param hdfsFilePath hdfs file path
     * @return byte[] byte array
     * @throws IOException errors
     */
    public byte[] catFile(String hdfsFilePath) throws IOException {

        if (StringUtils.isBlank(hdfsFilePath)) {
            logger.error("hdfs file path:{} is blank", hdfsFilePath);
            return new byte[0];
        }

        FSDataInputStream fsDataInputStream = fs.open(new Path(hdfsFilePath));
        return IOUtils.toByteArray(fsDataInputStream);
    }


    /**
     * cat file on hdfs
     *
     * @param hdfsFilePath hdfs file path
     * @param skipLineNums skip line numbers
     * @param limit        read how many lines
     * @return content of file
     * @throws IOException errors
     */
    public List<String> catFile(String hdfsFilePath, int skipLineNums, int limit) throws IOException {

        if (StringUtils.isBlank(hdfsFilePath)) {
            logger.error("hdfs file path:{} is blank", hdfsFilePath);
            return Collections.emptyList();
        }

        try (FSDataInputStream in = fs.open(new Path(hdfsFilePath))) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            Stream<String> stream = br.lines().skip(skipLineNums).limit(limit);
            return stream.collect(Collectors.toList());
        }

    }

    /**
     * make the given file and all non-existent parents into
     * directories. Has the semantics of Unix 'mkdir -p'.
     * Existence of the directory hierarchy is not an error.
     *
     * @param hdfsPath path to create
     * @return mkdir result
     * @throws IOException errors
     */
    public boolean mkdir(String hdfsPath) throws IOException {
        return fs.mkdirs(new Path(hdfsPath));
    }

    /**
     * copy files between FileSystems
     *
     * @param srcPath      source hdfs path
     * @param dstPath      destination hdfs path
     * @param deleteSource whether to delete the src
     * @param overwrite    whether to overwrite an existing file
     * @return if success or not
     * @throws IOException errors
     */
    public boolean copy(String srcPath, String dstPath, boolean deleteSource, boolean overwrite) throws IOException {
        return FileUtil.copy(fs, new Path(srcPath), fs, new Path(dstPath), deleteSource, overwrite, fs.getConf());
    }

    /**
     * the src file is on the local disk.  Add it to FS at
     * the given dst name.
     *
     * @param srcFile      local file
     * @param dstHdfsPath  destination hdfs path
     * @param deleteSource whether to delete the src
     * @param overwrite    whether to overwrite an existing file
     * @return if success or not
     * @throws IOException errors
     */
    public boolean copyLocalToHdfs(String srcFile, String dstHdfsPath, boolean deleteSource, boolean overwrite) throws IOException {
        Path srcPath = new Path(srcFile);
        Path dstPath = new Path(dstHdfsPath);

        fs.copyFromLocalFile(deleteSource, overwrite, srcPath, dstPath);

        return true;
    }

    /**
     * copy hdfs file to local
     *
     * @param srcHdfsFilePath source hdfs file path
     * @param dstFile         destination file
     * @param deleteSource    delete source
     * @param overwrite       overwrite
     * @return result of copy hdfs file to local
     * @throws IOException errors
     */
    public boolean copyHdfsToLocal(String srcHdfsFilePath, String dstFile, boolean deleteSource, boolean overwrite) throws IOException {
        Path srcPath = new Path(srcHdfsFilePath);
        File dstPath = new File(dstFile);

        if (dstPath.exists()) {
            if (dstPath.isFile()) {
                if (overwrite) {
                    Files.delete(dstPath.toPath());
                }
            } else {
                logger.error("destination file must be a file");
            }
        }

        if (!dstPath.getParentFile().exists()) {
            dstPath.getParentFile().mkdirs();
        }

        return FileUtil.copy(fs, srcPath, dstPath, deleteSource, fs.getConf());
    }

    /**
     * delete a file
     *
     * @param hdfsFilePath the path to delete.
     * @param recursive    if path is a directory and set to
     *                     true, the directory is deleted else throws an exception. In
     *                     case of a file the recursive can be set to either true or false.
     * @return true if delete is successful else false.
     * @throws IOException errors
     */
    public boolean delete(String hdfsFilePath, boolean recursive) throws IOException {
        return fs.delete(new Path(hdfsFilePath), recursive);
    }

    /**
     * check if exists
     *
     * @param hdfsFilePath source file path
     * @return result of exists or not
     * @throws IOException errors
     */
    public boolean exists(String hdfsFilePath) throws IOException {
        return fs.exists(new Path(hdfsFilePath));
    }

    /**
     * Gets a list of files in the directory
     *
     * @param filePath file path
     * @return {@link FileStatus} file status
     * @throws Exception errors
     */
    public FileStatus[] listFileStatus(String filePath) throws Exception {
        try {
            return fs.listStatus(new Path(filePath));
        } catch (IOException e) {
            logger.error("Get file list exception", e);
            throw new Exception("Get file list exception", e);
        }
    }

    /**
     * Renames Path src to Path dst.  Can take place on local fs
     * or remote DFS.
     *
     * @param src path to be renamed
     * @param dst new path after rename
     * @return true if rename is successful
     * @throws IOException on failure
     */
    public boolean rename(String src, String dst) throws IOException {
        return fs.rename(new Path(src), new Path(dst));
    }

    /**
     * hadoop resourcemanager enabled or not
     *
     * @return result
     */
    public boolean isYarnEnabled() {
        return yarnEnabled;
    }

    /**
     * get the state of an application
     *
     * @param applicationId application id
     * @return the return may be null or there may be other parse exceptions
     */
    public ExecutionStatus getApplicationStatus(String applicationId) throws Exception {
        if (StringUtils.isEmpty(applicationId)) {
            return null;
        }

        String result = Constants.FAILED;
        String applicationUrl = getApplicationUrl(applicationId);
        logger.info("applicationUrl={}", applicationUrl);

        String responseContent;
        if (PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false)) {
            responseContent = KerberosHttpClient.get(applicationUrl);
        } else {
            responseContent = HttpUtils.get(applicationUrl);
        }
        if (responseContent != null) {
            ObjectNode jsonObject = JSONUtils.parseObject(responseContent);
            if (!jsonObject.has("app")) {
                return ExecutionStatus.FAILURE;
            }
            result = jsonObject.path("app").path("finalStatus").asText();

        } else {
            //may be in job history
            String jobHistoryUrl = getJobHistoryUrl(applicationId);
            logger.info("jobHistoryUrl={}", jobHistoryUrl);
            responseContent = HttpUtils.get(jobHistoryUrl);
            if (null != responseContent) {
                ObjectNode jsonObject = JSONUtils.parseObject(responseContent);
                if (!jsonObject.has("job")) {
                    return ExecutionStatus.FAILURE;
                }
                result = jsonObject.path("job").path("state").asText();
            } else {
                return ExecutionStatus.FAILURE;
            }
        }

        switch (result) {
            case Constants.ACCEPTED:
                return ExecutionStatus.SUBMITTED_SUCCESS;
            case Constants.SUCCEEDED:
                return ExecutionStatus.SUCCESS;
            case Constants.NEW:
            case Constants.NEW_SAVING:
            case Constants.SUBMITTED:
            case Constants.FAILED:
                return ExecutionStatus.FAILURE;
            case Constants.KILLED:
                return ExecutionStatus.KILL;

            case Constants.RUNNING:
            default:
                return ExecutionStatus.RUNNING_EXECUTION;
        }
    }

    /**
     * get data hdfs path
     *
     * @return data hdfs path
     */
    public static String getHdfsDataBasePath() {
        if ("/".equals(resourceUploadPath)) {
            // if basepath is configured to /,  the generated url may be  //default/resources (with extra leading /)
            return "";
        } else {
            return resourceUploadPath;
        }
    }

    /**
     * hdfs resource dir
     *
     * @param tenantCode   tenant code
     * @param resourceType resource type
     * @return hdfs resource dir
     */
    public static String getHdfsDir(ResourceType resourceType, String tenantCode) {
        String hdfsDir = "";
        if (resourceType.equals(ResourceType.FILE)) {
            hdfsDir = getHdfsResDir(tenantCode);
        } else if (resourceType.equals(ResourceType.UDF)) {
            hdfsDir = getHdfsUdfDir(tenantCode);
        }
        return hdfsDir;
    }

    /**
     * hdfs resource dir
     *
     * @param tenantCode tenant code
     * @return hdfs resource dir
     */
    public static String getHdfsResDir(String tenantCode) {
        return String.format("%s/resources", getHdfsTenantDir(tenantCode));
    }

    /**
     * hdfs user dir
     *
     * @param tenantCode tenant code
     * @param userId     user id
     * @return hdfs resource dir
     */
    public static String getHdfsUserDir(String tenantCode, int userId) {
        return String.format("%s/home/%d", getHdfsTenantDir(tenantCode), userId);
    }

    /**
     * hdfs udf dir
     *
     * @param tenantCode tenant code
     * @return get udf dir on hdfs
     */
    public static String getHdfsUdfDir(String tenantCode) {
        return String.format("%s/udfs", getHdfsTenantDir(tenantCode));
    }


    /**
     * get hdfs file name
     *
     * @param resourceType resource type
     * @param tenantCode   tenant code
     * @param fileName     file name
     * @return hdfs file name
     */
    public static String getHdfsFileName(ResourceType resourceType, String tenantCode, String fileName) {
        if (fileName.startsWith("/")) {
            fileName = fileName.replaceFirst("/", "");
        }
        return String.format("%s/%s", getHdfsDir(resourceType, tenantCode), fileName);
    }

    /**
     * get absolute path and name for resource file on hdfs
     *
     * @param tenantCode tenant code
     * @param fileName   file name
     * @return get absolute path and name for file on hdfs
     */
    public static String getHdfsResourceFileName(String tenantCode, String fileName) {
        if (fileName.startsWith("/")) {
            fileName = fileName.replaceFirst("/", "");
        }
        return String.format("%s/%s", getHdfsResDir(tenantCode), fileName);
    }

    /**
     * get absolute path and name for udf file on hdfs
     *
     * @param tenantCode tenant code
     * @param fileName   file name
     * @return get absolute path and name for udf file on hdfs
     */
    public static String getHdfsUdfFileName(String tenantCode, String fileName) {
        if (fileName.startsWith("/")) {
            fileName = fileName.replaceFirst("/", "");
        }
        return String.format("%s/%s", getHdfsUdfDir(tenantCode), fileName);
    }

    /**
     * @param tenantCode tenant code
     * @return file directory of tenants on hdfs
     */
    public static String getHdfsTenantDir(String tenantCode) {
        return String.format("%s/%s", getHdfsDataBasePath(), tenantCode);
    }


    /**
     * getAppAddress
     *
     * @param appAddress app address
     * @param rmHa       resource manager ha
     * @return app address
     */
    public static String getAppAddress(String appAddress, String rmHa) {

        //get active ResourceManager
        String activeRM = YarnHAAdminUtils.getAcitveRMName(rmHa);

        String[] split1 = appAddress.split(Constants.DOUBLE_SLASH);

        if (split1.length != 2) {
            return null;
        }

        String start = split1[0] + Constants.DOUBLE_SLASH;
        String[] split2 = split1[1].split(Constants.COLON);

        if (split2.length != 2) {
            return null;
        }

        String end = Constants.COLON + split2[1];

        return start + activeRM + end;
    }


    @Override
    public void close() throws IOException {
        if (fs != null) {
            try {
                fs.close();
            } catch (IOException e) {
                logger.error("Close HadoopUtils instance failed", e);
                throw new IOException("Close HadoopUtils instance failed", e);
            }
        }
    }


    /**
     * yarn ha admin utils
     */
    private static final class YarnHAAdminUtils extends RMAdminCLI {

        /**
         * get active resourcemanager
         *
         * @param rmIds
         * @return
         */
        public static String getAcitveRMName(String rmIds) {

            String[] rmIdArr = rmIds.split(Constants.COMMA);

            int activeResourceManagerPort = PropertyUtils.getInt(Constants.HADOOP_RESOURCE_MANAGER_HTTPADDRESS_PORT, 8088);

            String yarnUrl = "http://%s:" + activeResourceManagerPort + "/ws/v1/cluster/info";

            String state = null;
            try {
                /**
                 * send http get request to rm1
                 */
                state = getRMState(String.format(yarnUrl, rmIdArr[0]));

                if (Constants.HADOOP_RM_STATE_ACTIVE.equals(state)) {
                    return rmIdArr[0];
                } else if (Constants.HADOOP_RM_STATE_STANDBY.equals(state)) {
                    state = getRMState(String.format(yarnUrl, rmIdArr[1]));
                    if (Constants.HADOOP_RM_STATE_ACTIVE.equals(state)) {
                        return rmIdArr[1];
                    }
                } else {
                    return null;
                }
            } catch (Exception e) {
                state = getRMState(String.format(yarnUrl, rmIdArr[1]));
                if (Constants.HADOOP_RM_STATE_ACTIVE.equals(state)) {
                    return rmIdArr[0];
                }
            }
            return null;
        }


        /**
         * get ResourceManager state
         *
         * @param url
         * @return
         */
        public static String getRMState(String url) {

            String retStr = HttpUtils.get(url);

            if (StringUtils.isEmpty(retStr)) {
                return null;
            }
            //to json
            ObjectNode jsonObject = JSONUtils.parseObject(retStr);

            //get ResourceManager state
            if (!jsonObject.has("clusterInfo")) {
                return null;
            }
            return jsonObject.get("clusterInfo").path("haState").asText();
        }

    }
}
