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
package org.apache.dolphinscheduler.dao.upgrade.shell;

import org.apache.dolphinscheduler.dao.upgrade.DolphinSchedulerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * upgrade DolphinScheduler
 */
public class UpgradeDolphinScheduler {
	private static final Logger logger = LoggerFactory.getLogger(UpgradeDolphinScheduler.class);

	/**
	 * upgrade dolphin scheduler db
	 * @param args args
	 */
	public static void main(String[] args) {
		DolphinSchedulerManager dolphinSchedulerManager = new DolphinSchedulerManager();
		try {
			dolphinSchedulerManager.upgradeDolphinScheduler();
			logger.info("upgrade DolphinScheduler success");
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			logger.info("Upgrade DolphinScheduler failed");
			throw new RuntimeException(e);
		}
	}



}
