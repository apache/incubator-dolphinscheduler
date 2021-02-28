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
package org.apache.dolphinscheduler.common.thread;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *  if the process closes, a signal is placed as true, and all threads get this flag to stop working
 */
public class Stopper {

	private static AtomicBoolean signal = new AtomicBoolean(false);

	public static final boolean isStopped(){
		return signal.get();
	}

	public static final boolean isRunning(){
		return !signal.get();
	}

	public static final void stop(){
		signal.set(true);
	}
}
