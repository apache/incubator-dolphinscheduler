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

package org.apache.dolphinscheduler.plugin.task.api;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * thread utils
 */
public class ThreadUtils {

    private ThreadUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private static final int STACK_DEPTH = 20;

    /**
     * Wrapper over newCachedThreadPool. Thread names are formatted as prefix-ID, where ID is a
     * unique, sequentially assigned integer.
     *
     * @param prefix prefix
     * @return ThreadPoolExecutor
     */
    public static ThreadPoolExecutor newDaemonCachedThreadPool(String prefix) {
        ThreadFactory threadFactory = namedThreadFactory(prefix);
        return ((ThreadPoolExecutor) Executors.newCachedThreadPool(threadFactory));
    }

    /**
     * Create a thread factory that names threads with a prefix and also sets the threads to daemon.
     *
     * @param prefix prefix
     * @return ThreadFactory
     */
    private static ThreadFactory namedThreadFactory(String prefix) {
        return new ThreadFactoryBuilder().setDaemon(true).setNameFormat(prefix + "-%d").build();
    }

    /**
     * Create a cached thread pool whose max number of threads is `maxThreadNumber`. Thread names
     * are formatted as prefix-ID, where ID is a unique, sequentially assigned integer.
     *
     * @param prefix prefix
     * @param maxThreadNumber maxThreadNumber
     * @param keepAliveSeconds keepAliveSeconds
     * @return ThreadPoolExecutor
     */
    public static ThreadPoolExecutor newDaemonCachedThreadPool(String prefix,
                                                               int maxThreadNumber,
                                                               int keepAliveSeconds) {
        ThreadFactory threadFactory = namedThreadFactory(prefix);
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                // corePoolSize: the max number of threads to create before queuing the tasks
                maxThreadNumber,
                // maximumPoolSize: because we use LinkedBlockingDeque, this one is not used
                maxThreadNumber,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory);
        threadPool.allowCoreThreadTimeOut(true);
        return threadPool;
    }

    /**
     * Wrapper over newFixedThreadPool. Thread names are formatted as prefix-ID, where ID is a
     * unique, sequentially assigned integer.
     *
     * @param nThreads nThreads
     * @param prefix prefix
     * @return ThreadPoolExecutor
     */
    public static ThreadPoolExecutor newDaemonFixedThreadPool(int nThreads, String prefix) {
        ThreadFactory threadFactory = namedThreadFactory(prefix);
        return ((ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads, threadFactory));
    }

    /**
     * Wrapper over newSingleThreadExecutor.
     *
     * @param threadName threadName
     * @return ExecutorService
     */
    public static ExecutorService newDaemonSingleThreadExecutor(String threadName) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat(threadName)
                .build();
        return Executors.newSingleThreadExecutor(threadFactory);
    }

    /**
     * Wrapper over newDaemonFixedThreadExecutor.
     *
     * @param threadName threadName
     * @param threadsNum threadsNum
     * @return ExecutorService
     */
    public static ExecutorService newDaemonFixedThreadExecutor(String threadName, int threadsNum) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat(threadName)
                .build();
        return Executors.newFixedThreadPool(threadsNum, threadFactory);
    }

    /**
     * Wrapper over ScheduledThreadPoolExecutor
     *
     * @param threadName threadName
     * @param corePoolSize corePoolSize
     * @return ScheduledExecutorService
     */
    public static ScheduledExecutorService newDaemonThreadScheduledExecutor(String threadName, int corePoolSize) {
        return newThreadScheduledExecutor(threadName, corePoolSize, true);
    }

    /**
     * Wrapper over ScheduledThreadPoolExecutor
     *
     * @param threadName threadName
     * @param corePoolSize corePoolSize
     * @param isDaemon isDaemon
     * @return ScheduledThreadPoolExecutor
     */
    public static ScheduledExecutorService newThreadScheduledExecutor(String threadName, int corePoolSize, boolean isDaemon) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setDaemon(isDaemon)
                .setNameFormat(threadName)
                .build();
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
        // By default, a cancelled task is not automatically removed from the work queue until its delay
        // elapses. We have to enable it manually.
        executor.setRemoveOnCancelPolicy(true);
        return executor;
    }

    /**
     * get thread info
     *
     * @param t t
     * @return thread info
     */
    public static ThreadInfo getThreadInfo(Thread t) {
        long tid = t.getId();
        return threadBean.getThreadInfo(tid, STACK_DEPTH);
    }

    /**
     * Format the given ThreadInfo object as a String.
     *
     * @param threadInfo threadInfo
     * @param indent indent
     * @return threadInfo
     */
    public static String formatThreadInfo(ThreadInfo threadInfo, String indent) {
        StringBuilder sb = new StringBuilder();
        appendThreadInfo(sb, threadInfo, indent);
        return sb.toString();
    }

    /**
     * Print all of the thread's information and stack traces.
     *
     * @param sb StringBuilder
     * @param info ThreadInfo
     * @param indent indent
     */
    public static void appendThreadInfo(StringBuilder sb,
                                        ThreadInfo info,
                                        String indent) {
        boolean contention = threadBean.isThreadContentionMonitoringEnabled();

        if (info == null) {
            sb.append(indent).append("Inactive (perhaps exited while monitoring was done)\n");
            return;
        }
        String taskName = getTaskName(info.getThreadId(), info.getThreadName());
        sb.append(indent).append("Thread ").append(taskName).append(":\n");

        Thread.State state = info.getThreadState();
        sb.append(indent).append("  State: ").append(state).append("\n");
        sb.append(indent).append("  Blocked count: ").append(info.getBlockedCount()).append("\n");
        sb.append(indent).append("  Waited count: ").append(info.getWaitedCount()).append("\n");
        if (contention) {
            sb.append(indent).append("  Blocked time: ").append(info.getBlockedTime()).append("\n");
            sb.append(indent).append("  Waited time: ").append(info.getWaitedTime()).append("\n");
        }
        if (state == Thread.State.WAITING) {
            sb.append(indent).append("  Waiting on ").append(info.getLockName()).append("\n");
        } else if (state == Thread.State.BLOCKED) {
            sb.append(indent).append("  Blocked on ").append(info.getLockName()).append("\n");
            sb.append(indent).append("  Blocked by ").append(
                    getTaskName(info.getLockOwnerId(), info.getLockOwnerName())).append("\n");
        }
        sb.append(indent).append("  Stack:").append("\n");
        for (StackTraceElement frame : info.getStackTrace()) {
            sb.append(indent).append("    ").append(frame.toString()).append("\n");
        }
    }

    /**
     * getTaskName
     *
     * @param id id
     * @param name name
     * @return task name
     */
    private static String getTaskName(long id, String name) {
        if (name == null) {
            return Long.toString(id);
        }
        return id + " (" + name + ")";
    }

    /**
     * sleep
     *
     * @param millis millis
     */
    public static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
    }
}
