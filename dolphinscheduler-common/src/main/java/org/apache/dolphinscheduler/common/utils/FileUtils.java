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

import static org.apache.dolphinscheduler.common.Constants.DATA_BASEDIR_PATH;
import static org.apache.dolphinscheduler.common.Constants.RESOURCE_VIEW_SUFFIXES;
import static org.apache.dolphinscheduler.common.Constants.RESOURCE_VIEW_SUFFIXES_DEFAULT_VALUE;
import static org.apache.dolphinscheduler.common.Constants.YYYYMMDDHHMMSS;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * file utils
 */
public class FileUtils {

    public static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static final String DATA_BASEDIR = PropertyUtils.getString(DATA_BASEDIR_PATH, "/tmp/dolphinscheduler");

    public static final ThreadLocal<Logger> taskLoggerThreadLocal = new ThreadLocal<>();

    private FileUtils() {
        throw new UnsupportedOperationException("Construct FileUtils");
    }

    /**
     * get file suffix
     *
     * @param filename file name
     * @return file suffix
     */
    public static String suffix(String filename) {

        String fileSuffix = "";
        if (StringUtils.isNotEmpty(filename)) {
            int lastIndex = filename.lastIndexOf('.');
            if (lastIndex > 0) {
                fileSuffix = filename.substring(lastIndex + 1);
            }
        }
        return fileSuffix;
    }

    /**
     * get download file absolute path and name
     *
     * @param filename file name
     * @return download file name
     */
    public static String getDownloadFilename(String filename) {
        String fileName = String.format("%s/download/%s/%s", DATA_BASEDIR, DateUtils.getCurrentTime(YYYYMMDDHHMMSS), filename);

        File file = new File(fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        return fileName;
    }

    /**
     * get upload file absolute path and name
     *
     * @param tenantCode tenant code
     * @param filename file name
     * @return local file path
     */
    public static String getUploadFilename(String tenantCode, String filename) {
        String fileName = String.format("%s/%s/resources/%s", DATA_BASEDIR, tenantCode, filename);
        File file = new File(fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        return fileName;
    }

    /**
     * directory of process execution
     *
     * @param projectId project id
     * @param processDefineId process definition id
     * @param processInstanceId process instance id
     * @param taskInstanceId task instance id
     * @return directory of process execution
     */
    public static String getProcessExecDir(int projectId, int processDefineId, int processInstanceId, int taskInstanceId) {
        String fileName = String.format("%s/exec/process/%s/%s/%s/%s", DATA_BASEDIR, Integer.toString(projectId),
                Integer.toString(processDefineId), Integer.toString(processInstanceId), Integer.toString(taskInstanceId));
        File file = new File(fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        return fileName;
    }

    /**
     * directory of process instances
     *
     * @param projectId project id
     * @param processDefineId process definition id
     * @param processInstanceId process instance id
     * @return directory of process instances
     */
    public static String getProcessExecDir(int projectId, int processDefineId, int processInstanceId) {
        String fileName = String.format("%s/exec/process/%s/%s/%s", DATA_BASEDIR, Integer.toString(projectId),
                Integer.toString(processDefineId), Integer.toString(processInstanceId));
        File file = new File(fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        return fileName;
    }

    /**
     * @return get suffixes for resource files that support online viewing
     */
    public static String getResourceViewSuffixes() {
        return PropertyUtils.getString(RESOURCE_VIEW_SUFFIXES, RESOURCE_VIEW_SUFFIXES_DEFAULT_VALUE);
    }

    /**
     * create directory if absent
     *
     * @param execLocalPath execute local path
     * @throws IOException errors
     */
    public static void createWorkDirIfAbsent(String execLocalPath) throws IOException {
        //if work dir exists, first delete
        File execLocalPathFile = new File(execLocalPath);

        if (execLocalPathFile.exists()) {
            org.apache.commons.io.FileUtils.forceDelete(execLocalPathFile);
        }

        //create work dir
        org.apache.commons.io.FileUtils.forceMkdir(execLocalPathFile);
        String mkdirLog = "create dir success " + execLocalPath;
        LoggerUtils.logInfo(Optional.ofNullable(logger), mkdirLog);
        LoggerUtils.logInfo(Optional.ofNullable(taskLoggerThreadLocal.get()), mkdirLog);
    }

    /**
     * write content to file ,if parent path not exists, it will do one's utmost to mkdir
     *
     * @param content content
     * @param filePath target file path
     * @return true if write success
     */
    public static boolean writeContent2File(String content, String filePath) {
        boolean flag = true;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        try {
            File distFile = new File(filePath);
            if (!distFile.getParentFile().exists() && !distFile.getParentFile().mkdirs()) {
                FileUtils.logger.error("mkdir parent failed");
                return false;
            }
            bufferedReader = new BufferedReader(new StringReader(content));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(distFile), StandardCharsets.UTF_8));
            char[] buf = new char[1024];
            int len;
            while ((len = bufferedReader.read(buf)) != -1) {
                bufferedWriter.write(buf, 0, len);
            }
            bufferedWriter.flush();
            bufferedReader.close();
            bufferedWriter.close();
        } catch (IOException e) {
            FileUtils.logger.error(e.getMessage(), e);
            flag = false;
            return flag;
        } finally {
            IOUtils.closeQuietly(bufferedWriter);
            IOUtils.closeQuietly(bufferedReader);
        }
        return flag;
    }

    /**
     * Writes a String to a file creating the file if it does not exist.
     * <p>
     * NOTE: As from v1.3, the parent directories of the file will be created
     * if they do not exist.
     *
     * @param file the file to write
     * @param data the content to write to the file
     * @param encoding the encoding to use, {@code null} means platform default
     * @throws IOException in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     * @since 2.4
     */
    public static void writeStringToFile(File file, String data, Charset encoding) throws IOException {
        writeStringToFile(file, data, encoding, false);
    }

    /**
     * Writes a String to a file creating the file if it does not exist.
     * <p>
     * NOTE: As from v1.3, the parent directories of the file will be created
     * if they do not exist.
     *
     * @param file the file to write
     * @param data the content to write to the file
     * @param encoding the encoding to use, {@code null} means platform default
     * @throws IOException in case of an I/O error
     * @throws java.io.UnsupportedEncodingException if the encoding is not supported by the VM
     */
    public static void writeStringToFile(File file, String data, String encoding) throws IOException {
        writeStringToFile(file, data, encoding, false);
    }

    /**
     * Writes a String to a file creating the file if it does not exist.
     *
     * @param file the file to write
     * @param data the content to write to the file
     * @param encoding the encoding to use, {@code null} means platform default
     * @param append if {@code true}, then the String will be added to the
     * end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.3
     */
    public static void writeStringToFile(File file, String data, Charset encoding, boolean append) throws IOException {
        OutputStream out = null;
        try {
            out = openOutputStream(file, append);
            IOUtils.write(data, out, encoding);
            out.close(); // don't swallow close Exception if copy completes normally
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * Writes a String to a file creating the file if it does not exist.
     *
     * @param file the file to write
     * @param data the content to write to the file
     * @param encoding the encoding to use, {@code null} means platform default
     * @param append if {@code true}, then the String will be added to the
     * end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @throws UnsupportedCharsetException thrown instead of {@link UnsupportedEncodingException} in version 2.2 if the encoding is not
     * supported by the VM
     * @since 2.1
     */
    public static void writeStringToFile(File file, String data, String encoding, boolean append) throws IOException {
        writeStringToFile(file, data, Charsets.toCharset(encoding), append);
    }

    /**
     * Writes a String to a file creating the file if it does not exist using the default encoding for the VM.
     *
     * @param file the file to write
     * @param data the content to write to the file
     * @throws IOException in case of an I/O error
     */
    public static void writeStringToFile(File file, String data) throws IOException {
        writeStringToFile(file, data, Charset.defaultCharset(), false);
    }

    /**
     * Writes a String to a file creating the file if it does not exist using the default encoding for the VM.
     *
     * @param file the file to write
     * @param data the content to write to the file
     * @param append if {@code true}, then the String will be added to the
     * end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since 2.1
     */
    public static void writeStringToFile(File file, String data, boolean append) throws IOException {
        writeStringToFile(file, data, Charset.defaultCharset(), append);
    }

    /**
     * Opens a {@link FileOutputStream} for the specified file, checking and
     * creating the parent directory if it does not exist.
     * <p>
     * At the end of the method either the stream will be successfully opened,
     * or an exception will have been thrown.
     * <p>
     * The parent directory will be created if it does not exist.
     * The file will be created if it does not exist.
     * An exception is thrown if the file object exists but is a directory.
     * An exception is thrown if the file exists but cannot be written to.
     * An exception is thrown if the parent directory cannot be created.
     *
     * @param file the file to open for output, must not be {@code null}
     * @return a new {@link FileOutputStream} for the specified file
     * @throws IOException if the file object is a directory
     * @throws IOException if the file cannot be written to
     * @throws IOException if a parent directory needs creating but that fails
     * @since 1.3
     */
    public static FileOutputStream openOutputStream(File file) throws IOException {
        return openOutputStream(file, false);
    }

    /**
     * Opens a {@link FileOutputStream} for the specified file, checking and
     * creating the parent directory if it does not exist.
     * <p>
     * At the end of the method either the stream will be successfully opened,
     * or an exception will have been thrown.
     * <p>
     * The parent directory will be created if it does not exist.
     * The file will be created if it does not exist.
     * An exception is thrown if the file object exists but is a directory.
     * An exception is thrown if the file exists but cannot be written to.
     * An exception is thrown if the parent directory cannot be created.
     *
     * @param file the file to open for output, must not be {@code null}
     * @param append if {@code true}, then bytes will be added to the
     * end of the file rather than overwriting
     * @return a new {@link FileOutputStream} for the specified file
     * @throws IOException if the file object is a directory
     * @throws IOException if the file cannot be written to
     * @throws IOException if a parent directory needs creating but that fails
     * @since 2.1
     */
    public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null && !parent.mkdirs() && !parent.isDirectory()) {
                throw new IOException("Directory '" + parent + "' could not be created");
            }
        }
        return new FileOutputStream(file, append);
    }

    /**
     * deletes a directory recursively
     *
     * @param dir directory
     * @throws IOException in case deletion is unsuccessful
     */

    public static void deleteDir(String dir) throws IOException {
        org.apache.commons.io.FileUtils.deleteDirectory(new File(dir));
    }

    /**
     * Deletes a file. If file is a directory, delete it and all sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted.
     *      (java.io.File methods returns a boolean)</li>
     * </ul>
     *
     * @param filename file name
     * @throws IOException in case deletion is unsuccessful
     */
    public static void deleteFile(String filename) throws IOException {
        org.apache.commons.io.FileUtils.forceDelete(new File(filename));
    }

    /**
     * Gets all the parent subdirectories of the parentDir directory
     *
     * @param parentDir parent dir
     * @return all dirs
     */
    public static File[] getAllDir(String parentDir) {
        if (parentDir == null || "".equals(parentDir)) {
            throw new RuntimeException("parentDir can not be empty");
        }

        File file = new File(parentDir);
        if (!file.exists() || !file.isDirectory()) {
            throw new RuntimeException("parentDir not exist, or is not a directory:" + parentDir);
        }

        return file.listFiles(File::isDirectory);
    }

    /**
     * Get Content
     *
     * @param inputStream input stream
     * @return string of input stream
     */
    public static String readFile2Str(InputStream inputStream) {

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
            return output.toString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
