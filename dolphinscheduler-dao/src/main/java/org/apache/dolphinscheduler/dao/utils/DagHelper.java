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
package org.apache.dolphinscheduler.dao.utils;


import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.process.ProcessDag;
import org.apache.dolphinscheduler.common.task.conditions.ConditionsParameters;
import org.apache.dolphinscheduler.common.task.conditions.SwitchParameters;
import org.apache.dolphinscheduler.common.task.conditions.SwitchResultVo;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessData;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * dag tools
 */
public class DagHelper {


    private static final Logger logger = LoggerFactory.getLogger(DagHelper.class);


    /**
     * generate flow node relation list by task node list;
     * Edges that are not in the task Node List will not be added to the result
     *
     * @param taskNodeList taskNodeList
     * @return task node relation list
     */
    public static List<TaskNodeRelation> generateRelationListByFlowNodes(List<TaskNode> taskNodeList) {
        List<TaskNodeRelation> nodeRelationList = new ArrayList<>();
        for (TaskNode taskNode : taskNodeList) {
            String preTasks = taskNode.getPreTasks();
            List<String> preTaskList = JSONUtils.toList(preTasks, String.class);
            if (preTaskList != null) {
                for (String depNodeName : preTaskList) {
                    if (null != findNodeByName(taskNodeList, depNodeName)) {
                        nodeRelationList.add(new TaskNodeRelation(depNodeName, taskNode.getName()));
                    }
                }
            }
        }
        return nodeRelationList;
    }

    /**
     * generate task nodes needed by dag
     *
     * @param taskNodeList         taskNodeList
     * @param startNodeNameList    startNodeNameList
     * @param recoveryNodeNameList recoveryNodeNameList
     * @param taskDependType       taskDependType
     * @return task node list
     */
    public static List<TaskNode> generateFlowNodeListByStartNode(List<TaskNode> taskNodeList, List<String> startNodeNameList,
                                                                 List<String> recoveryNodeNameList, TaskDependType taskDependType) {
        List<TaskNode> destFlowNodeList = new ArrayList<>();
        List<String> startNodeList = startNodeNameList;

        if (taskDependType != TaskDependType.TASK_POST
                && CollectionUtils.isEmpty(startNodeList)) {
            logger.error("start node list is empty! cannot continue run the process ");
            return destFlowNodeList;
        }
        List<TaskNode> destTaskNodeList = new ArrayList<>();
        List<TaskNode> tmpTaskNodeList = new ArrayList<>();
        if (taskDependType == TaskDependType.TASK_POST
                && CollectionUtils.isNotEmpty(recoveryNodeNameList)) {
            startNodeList = recoveryNodeNameList;
        }
        if (CollectionUtils.isEmpty(startNodeList)) {
            // no special designation start nodes
            tmpTaskNodeList = taskNodeList;
        } else {
            // specified start nodes or resume execution
            for (String startNodeName : startNodeList) {
                TaskNode startNode = findNodeByName(taskNodeList, startNodeName);
                List<TaskNode> childNodeList = new ArrayList<>();
                if (startNode == null) {
                    logger.error("start node name [{}] is not in task node list [{}] ",
                            startNodeName,
                            taskNodeList
                    );
                    continue;
                } else if (TaskDependType.TASK_POST == taskDependType) {
                    List<String> visitedNodeNameList = new ArrayList<>();
                    childNodeList = getFlowNodeListPost(startNode, taskNodeList, visitedNodeNameList);
                } else if (TaskDependType.TASK_PRE == taskDependType) {
                    List<String> visitedNodeNameList = new ArrayList<>();
                    childNodeList = getFlowNodeListPre(startNode, recoveryNodeNameList, taskNodeList, visitedNodeNameList);
                } else {
                    childNodeList.add(startNode);
                }
                tmpTaskNodeList.addAll(childNodeList);
            }
        }

        for (TaskNode taskNode : tmpTaskNodeList) {
            if (null == findNodeByName(destTaskNodeList, taskNode.getName())) {
                destTaskNodeList.add(taskNode);
            }
        }
        return destTaskNodeList;
    }


    /**
     * find all the nodes that depended on the start node
     *
     * @param startNode    startNode
     * @param taskNodeList taskNodeList
     * @return task node list
     */
    private static List<TaskNode> getFlowNodeListPost(TaskNode startNode, List<TaskNode> taskNodeList, List<String> visitedNodeNameList) {
        List<TaskNode> resultList = new ArrayList<>();
        for (TaskNode taskNode : taskNodeList) {
            List<String> depList = taskNode.getDepList();
            if (null != depList && null != startNode && depList.contains(startNode.getName()) && !visitedNodeNameList.contains(taskNode.getName())) {
                resultList.addAll(getFlowNodeListPost(taskNode, taskNodeList, visitedNodeNameList));
            }
        }
        // why add (startNode != null) condition? for SonarCloud Quality Gate passed
        if (null != startNode) {
            visitedNodeNameList.add(startNode.getName());
        }

        resultList.add(startNode);
        return resultList;
    }


    /**
     * find all nodes that start nodes depend on.
     *
     * @param startNode            startNode
     * @param recoveryNodeNameList recoveryNodeNameList
     * @param taskNodeList         taskNodeList
     * @return task node list
     */
    private static List<TaskNode> getFlowNodeListPre(TaskNode startNode, List<String> recoveryNodeNameList, List<TaskNode> taskNodeList, List<String> visitedNodeNameList) {

        List<TaskNode> resultList = new ArrayList<>();

        List<String> depList = new ArrayList<>();
        if (null != startNode) {
            depList = startNode.getDepList();
            resultList.add(startNode);
        }
        if (CollectionUtils.isEmpty(depList)) {
            return resultList;
        }
        for (String depNodeName : depList) {
            TaskNode start = findNodeByName(taskNodeList, depNodeName);
            if (recoveryNodeNameList.contains(depNodeName)) {
                resultList.add(start);
            } else if (!visitedNodeNameList.contains(depNodeName)) {
                resultList.addAll(getFlowNodeListPre(start, recoveryNodeNameList, taskNodeList, visitedNodeNameList));
            }
        }
        // why add (startNode != null) condition? for SonarCloud Quality Gate passed
        if (null != startNode) {
            visitedNodeNameList.add(startNode.getName());
        }
        return resultList;
    }

    /**
     * generate dag by start nodes and recovery nodes
     *
     * @param processDefinitionJson processDefinitionJson
     * @param startNodeNameList     startNodeNameList
     * @param recoveryNodeNameList  recoveryNodeNameList
     * @param depNodeType           depNodeType
     * @return process dag
     * @throws Exception if error throws Exception
     */
    public static ProcessDag generateFlowDag(String processDefinitionJson,
                                             List<String> startNodeNameList,
                                             List<String> recoveryNodeNameList,
                                             TaskDependType depNodeType) throws Exception {
        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);

        List<TaskNode> taskNodeList = new ArrayList<>();
        if (null != processData) {
            taskNodeList = processData.getTasks();
        }
        List<TaskNode> destTaskNodeList = generateFlowNodeListByStartNode(taskNodeList, startNodeNameList, recoveryNodeNameList, depNodeType);
        if (destTaskNodeList.isEmpty()) {
            return null;
        }
        List<TaskNodeRelation> taskNodeRelations = generateRelationListByFlowNodes(destTaskNodeList);
        ProcessDag processDag = new ProcessDag();
        processDag.setEdges(taskNodeRelations);
        processDag.setNodes(destTaskNodeList);
        return processDag;
    }

    /**
     * parse the forbidden task nodes in process definition.
     *
     * @param processDefinitionJson processDefinitionJson
     * @return task node map
     */
    public static Map<String, TaskNode> getForbiddenTaskNodeMaps(String processDefinitionJson) {
        Map<String, TaskNode> forbidTaskNodeMap = new ConcurrentHashMap<>();
        ProcessData processData = JSONUtils.parseObject(processDefinitionJson, ProcessData.class);

        List<TaskNode> taskNodeList = new ArrayList<>();
        if (null != processData) {
            taskNodeList = processData.getTasks();
        }
        for (TaskNode node : taskNodeList) {
            if (node.isForbidden()) {
                forbidTaskNodeMap.putIfAbsent(node.getName(), node);
            }
        }
        return forbidTaskNodeMap;
    }


    /**
     * find node by node name
     *
     * @param nodeDetails nodeDetails
     * @param nodeName    nodeName
     * @return task node
     */
    public static TaskNode findNodeByName(List<TaskNode> nodeDetails, String nodeName) {
        for (TaskNode taskNode : nodeDetails) {
            if (taskNode.getName().equals(nodeName)) {
                return taskNode;
            }
        }
        return null;
    }

    /**
     * the task can be submit when  all the depends nodes are forbidden or complete
     *
     * @param taskNode         taskNode
     * @param dag              dag
     * @param completeTaskList completeTaskList
     * @return can submit
     */
    public static boolean allDependsForbiddenOrEnd(TaskNode taskNode,
                                                   DAG<String, TaskNode, TaskNodeRelation> dag,
                                                   Map<String, TaskNode> skipTaskNodeList,
                                                   Map<String, TaskInstance> completeTaskList) {
        List<String> dependList = taskNode.getDepList();
        if (dependList == null) {
            return true;
        }
        for (String dependNodeName : dependList) {
            TaskNode dependNode = dag.getNode(dependNodeName);
            if (dependNode == null || completeTaskList.containsKey(dependNodeName)
                    || dependNode.isForbidden()
                    || skipTaskNodeList.containsKey(dependNodeName)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * parse the successor nodes of previous node.
     * this function parse the condition node to find the right branch.
     * also check all the depends nodes forbidden or complete
     *
     * @param preNodeName
     * @return successor nodes
     */
    public static Set<String> parsePostNodes(String preNodeName,
                                             Map<String, TaskNode> skipTaskNodeList,
                                             DAG<String, TaskNode, TaskNodeRelation> dag,
                                             Map<String, TaskInstance> completeTaskList) {
        Set<String> postNodeList = new HashSet<>();
        Collection<String> startVertexes = new ArrayList<>();
        if (preNodeName == null) {
            startVertexes = dag.getBeginNode();
        } else if (dag.getNode(preNodeName).isConditionsTask()) {
            List<String> conditionTaskList = parseConditionTask(preNodeName, skipTaskNodeList, dag, completeTaskList);
            startVertexes.addAll(conditionTaskList);
        } else if (dag.getNode(preNodeName).isSwitchTask()) {
            List<String> conditionTaskList = parseSwitchTask(preNodeName, skipTaskNodeList, dag, completeTaskList);
            startVertexes.addAll(conditionTaskList);
        } else {
            startVertexes = dag.getSubsequentNodes(preNodeName);
        }
        for (String subsequent : startVertexes) {
            TaskNode taskNode = dag.getNode(subsequent);
            if (isTaskNodeNeedSkip(taskNode, skipTaskNodeList)) {
                setTaskNodeSkip(subsequent, dag, completeTaskList, skipTaskNodeList);
                continue;
            }
            if (!DagHelper.allDependsForbiddenOrEnd(taskNode, dag, skipTaskNodeList, completeTaskList)) {
                continue;
            }
            if (taskNode.isForbidden() || completeTaskList.containsKey(subsequent)) {
                postNodeList.addAll(parsePostNodes(subsequent, skipTaskNodeList, dag, completeTaskList));
                continue;
            }
            postNodeList.add(subsequent);
        }
        return postNodeList;
    }

    /**
     * if all of the task dependence are skipped, skip it too.
     *
     * @param taskNode
     * @return
     */
    private static boolean isTaskNodeNeedSkip(TaskNode taskNode,
                                              Map<String, TaskNode> skipTaskNodeList
    ) {
        if (CollectionUtils.isEmpty(taskNode.getDepList())) {
            return false;
        }
        for (String depNode : taskNode.getDepList()) {
            if (!skipTaskNodeList.containsKey(depNode)) {
                return false;
            }
        }
        return true;
    }


    /**
     * parse condition task find the branch process
     * set skip flag for another one.
     *
     * @param nodeName
     * @return
     */
    public static List<String> parseConditionTask(String nodeName,
                                                  Map<String, TaskNode> skipTaskNodeList,
                                                  DAG<String, TaskNode, TaskNodeRelation> dag,
                                                  Map<String, TaskInstance> completeTaskList) {
        List<String> conditionTaskList = new ArrayList<>();
        TaskNode taskNode = dag.getNode(nodeName);
        if (!taskNode.isConditionsTask()) {
            return conditionTaskList;
        }
        if (!completeTaskList.containsKey(nodeName)) {
            return conditionTaskList;
        }
        TaskInstance taskInstance = completeTaskList.get(nodeName);
        ConditionsParameters conditionsParameters =
                JSONUtils.parseObject(taskNode.getConditionResult(), ConditionsParameters.class);
        List<String> skipNodeList = new ArrayList<>();
        if (taskInstance.getState().typeIsSuccess()) {
            conditionTaskList = conditionsParameters.getSuccessNode();
            skipNodeList = conditionsParameters.getFailedNode();
        } else if (taskInstance.getState().typeIsFailure()) {
            conditionTaskList = conditionsParameters.getFailedNode();
            skipNodeList = conditionsParameters.getSuccessNode();
        } else {
            conditionTaskList.add(nodeName);
        }
        for (String failedNode : skipNodeList) {
            setTaskNodeSkip(failedNode, dag, completeTaskList, skipTaskNodeList);
        }
        return conditionTaskList;
    }

    /**
     * parse condition task find the branch process
     * set skip flag for another one.
     *
     * @param nodeName
     * @return
     */
    public static List<String> parseSwitchTask(String nodeName,
                                               Map<String, TaskNode> skipTaskNodeList,
                                               DAG<String, TaskNode, TaskNodeRelation> dag,
                                               Map<String, TaskInstance> completeTaskList) {
        List<String> conditionTaskList = new ArrayList<>();
        TaskNode taskNode = dag.getNode(nodeName);
        if (!taskNode.isSwitchTask()) {
            return conditionTaskList;
        }
        if (!completeTaskList.containsKey(nodeName)) {
            return conditionTaskList;
        }
        TaskInstance taskInstance = completeTaskList.get(nodeName);
        TaskNode taskNodeNew = JSONUtils.parseObject(taskInstance.getTaskJson(), TaskNode.class);
        String nextNde = skipTaskNode4Condition(taskNodeNew, skipTaskNodeList, completeTaskList, dag);
        conditionTaskList.add(nextNde);
        return conditionTaskList;
    }

    private static String skipTaskNode4Condition(TaskNode taskNode, Map<String, TaskNode> skipTaskNodeList, Map<String, TaskInstance> completeTaskList, DAG<String, TaskNode, TaskNodeRelation> dag) {
        SwitchParameters switchParameters =
                JSONUtils.parseObject(taskNode.getDependence(), SwitchParameters.class);
        int resultConditionLocation = switchParameters.getResultConditionLocation();
        List<SwitchResultVo> conditionResultVoList = switchParameters.getDependTaskList();
        String switchTask = conditionResultVoList.get(resultConditionLocation).getNextNode();

        conditionResultVoList.remove(resultConditionLocation);
        for (SwitchResultVo info : conditionResultVoList) {
            if (StringUtils.isEmpty(info.getNextNode())) {
                continue;
            }
            setTaskNodeSkip(info.getNextNode(), dag, completeTaskList, skipTaskNodeList);
        }
        return switchTask;
    }

    /**
     * set task node and the post nodes skip flag
     *
     * @param skipNodeName
     * @param dag
     * @param completeTaskList
     * @param skipTaskNodeList
     */
    private static void setTaskNodeSkip(String skipNodeName,
                                        DAG<String, TaskNode, TaskNodeRelation> dag,
                                        Map<String, TaskInstance> completeTaskList,
                                        Map<String, TaskNode> skipTaskNodeList) {
        if (!dag.containsNode(skipNodeName)) {
            return;
        }
        skipTaskNodeList.putIfAbsent(skipNodeName, dag.getNode(skipNodeName));
        Collection<String> postNodeList = dag.getSubsequentNodes(skipNodeName);
        for (String post : postNodeList) {
            TaskNode postNode = dag.getNode(post);
            if (isTaskNodeNeedSkip(postNode, skipTaskNodeList)) {
                setTaskNodeSkip(post, dag, completeTaskList, skipTaskNodeList);
            }
        }
    }


    /***
     * build dag graph
     * @param processDag processDag
     * @return dag
     */
    public static DAG<String, TaskNode, TaskNodeRelation> buildDagGraph(ProcessDag processDag) {

        DAG<String, TaskNode, TaskNodeRelation> dag = new DAG<>();

        //add vertex
        if (CollectionUtils.isNotEmpty(processDag.getNodes())) {
            for (TaskNode node : processDag.getNodes()) {
                dag.addNode(node.getName(), node);
            }
        }

        //add edge
        if (CollectionUtils.isNotEmpty(processDag.getEdges())) {
            for (TaskNodeRelation edge : processDag.getEdges()) {
                dag.addEdge(edge.getStartNode(), edge.getEndNode());
            }
        }
        return dag;
    }

    /**
     * get process dag
     *
     * @param taskNodeList task node list
     * @return Process dag
     */
    public static ProcessDag getProcessDag(List<TaskNode> taskNodeList) {
        List<TaskNodeRelation> taskNodeRelations = new ArrayList<>();

        // Traverse node information and build relationships
        for (TaskNode taskNode : taskNodeList) {
            String preTasks = taskNode.getPreTasks();
            List<String> preTasksList = JSONUtils.toList(preTasks, String.class);

            // If the dependency is not empty
            if (preTasksList != null) {
                for (String depNode : preTasksList) {
                    taskNodeRelations.add(new TaskNodeRelation(depNode, taskNode.getName()));
                }
            }
        }

        ProcessDag processDag = new ProcessDag();
        processDag.setEdges(taskNodeRelations);
        processDag.setNodes(taskNodeList);
        return processDag;
    }

    /**
     * is there have conditions after the parent node
     *
     * @param parentNodeName
     * @return
     */
    public static boolean haveConditionsAfterNode(String parentNodeName,
                                                  DAG<String, TaskNode, TaskNodeRelation> dag
    ) {
        boolean result = false;
        Set<String> subsequentNodes = dag.getSubsequentNodes(parentNodeName);
        if (CollectionUtils.isEmpty(subsequentNodes)) {
            return result;
        }
        for (String nodeName : subsequentNodes) {
            TaskNode taskNode = dag.getNode(nodeName);
            List<String> preTasksList = JSONUtils.toList(taskNode.getPreTasks(), String.class);
            if (preTasksList.contains(parentNodeName) && taskNode.isConditionsTask()) {
                return true;
            }
        }
        return result;
    }

    /**
     * is there have conditions after the parent node
     *
     * @param parentNodeName
     * @return
     */
    public static boolean haveConditionsAfterNode(String parentNodeName,
                                                  List<TaskNode> taskNodes
    ) {
        boolean result = false;
        if (CollectionUtils.isEmpty(taskNodes)) {
            return result;
        }
        for (TaskNode taskNode : taskNodes) {
            List<String> preTasksList = JSONUtils.toList(taskNode.getPreTasks(), String.class);
            if (preTasksList.contains(parentNodeName) && taskNode.isConditionsTask()) {
                return true;
            }
        }
        return result;
    }
}
