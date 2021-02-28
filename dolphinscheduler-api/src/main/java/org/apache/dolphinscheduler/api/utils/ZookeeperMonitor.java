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
package org.apache.dolphinscheduler.api.utils;

import org.apache.dolphinscheduler.common.enums.ZKNodeType;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.ZookeeperRecord;
import org.apache.dolphinscheduler.service.zk.AbstractZKClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 *	monitor zookeeper info
 */
@Component
public class ZookeeperMonitor extends AbstractZKClient {

	private static final Logger LOG = LoggerFactory.getLogger(ZookeeperMonitor.class);

	/**
	 *
	 * @return zookeeper info list
	 */
	public List<ZookeeperRecord> zookeeperInfoList(){
		String zookeeperServers = getZookeeperQuorum().replaceAll("[\\t\\n\\x0B\\f\\r]", "");
		try{
			return zookeeperInfoList(zookeeperServers);
		}catch(Exception e){
			LOG.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * get master servers
	 * @return master server information
	 */
	public List<Server> getMasterServers(){
	    return getServersList(ZKNodeType.MASTER);
	}

	/**
	 * master construct is the same with worker, use the master instead
	 * @return worker server informations
	 */
	public List<Server> getWorkerServers(){
	    return getServersList(ZKNodeType.WORKER);
	}

	private static List<ZookeeperRecord> zookeeperInfoList(String zookeeperServers) {

		List<ZookeeperRecord> list = new ArrayList<>(5);

		if(StringUtils.isNotBlank(zookeeperServers)){
			String[] zookeeperServersArray = zookeeperServers.split(",");

			for (String zookeeperServer : zookeeperServersArray) {
				ZooKeeperState state = new ZooKeeperState(zookeeperServer);
				boolean ok = state.ruok();
				if(ok){
					state.getZookeeperInfo();
				}

				int connections = state.getConnections();
				int watches = state.getWatches();
				long sent = state.getSent();
				long received = state.getReceived();
				String mode =  state.getMode();
				float minLatency =  state.getMinLatency();
				float avgLatency = state.getAvgLatency();
				float maxLatency = state.getMaxLatency();
				int nodeCount = state.getNodeCount();
				int status = ok ? 1 : 0;
				Date date = new Date();

				ZookeeperRecord zookeeperRecord = new ZookeeperRecord(zookeeperServer,connections,watches,sent,received,mode,minLatency,avgLatency,maxLatency,nodeCount,status,date);
				list.add(zookeeperRecord);

			}
		}

		return list;
	}
}
