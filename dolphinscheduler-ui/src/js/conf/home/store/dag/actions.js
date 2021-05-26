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

import _ from 'lodash'
import io from '@/module/io'
import { tasksState } from '@/conf/home/pages/dag/_source/config'

// delete 'definitionList' from tasks
const deleteDefinitionList = (tasks) => {
  const newTasks = []
  tasks.forEach(item => {
    const newItem = Object.assign({}, item)
    if (newItem.dependence && newItem.dependence.dependTaskList) {
      newItem.dependence.dependTaskList.forEach(dependTaskItem => {
        if (dependTaskItem.dependItemList) {
          dependTaskItem.dependItemList.forEach(dependItem => {
            Reflect.deleteProperty(dependItem, 'definitionList')
          })
        }
      })
    }
    newTasks.push(newItem)
  })
  return newTasks
}

export default {
  /**
   *  Task status acquisition
   */
  getTaskState ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/instance/task-list-by-process-id`, {
        processInstanceId: payload
      }, res => {
        const arr = _.map(res.data.taskList, v => {
          return _.cloneDeep(_.assign(tasksState[v.state], {
            name: v.name,
            stateId: v.id,
            dependentResult: v.dependentResult
          }))
        })
        resolve({
          list: arr,
          processInstanceState: res.data.processInstanceState,
          taskList: res.data.taskList
        })
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Update process definition status
   */
  editProcessState ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectName}/process/release`, {
        processId: payload.processId,
        releaseState: payload.releaseState
      }, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },

  /**
   * get process definition versions pagination info
   */
  getProcessDefinitionVersionsPage ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/process/versions`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },

  /**
   * switch process definition version
   */
  switchProcessDefinitionVersion ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/process/version/switch`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },

  /**
   * delete process definition version
   */
  deleteProcessDefinitionVersion ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/process/version/delete`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },

  /**
   * Update process instance status
   */
  editExecutorsState ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectName}/executors/execute`, {
        processInstanceId: payload.processInstanceId,
        executeType: payload.executeType
      }, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Verify that the DGA map name exists
   */
  verifDAGName ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/process/verify-name`, {
        name: payload
      }, res => {
        state.name = payload
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },

  /**
   * Get process definition DAG diagram details
   */
  getProcessDetails ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/process/select-by-id`, {
        processId: payload
      }, res => {
        // process definition code
        state.code = res.data.code
        // version
        state.version = res.data.version
        // name
        state.name = res.data.name
        // description
        state.description = res.data.description
        // connects
        state.connects = JSON.parse(res.data.connects)
        // locations
        state.locations = JSON.parse(res.data.locations)
        // Process definition
        const processDefinitionJson = JSON.parse(res.data.processDefinitionJson)
        // tasks info
        state.tasks = processDefinitionJson.tasks
        // tasks cache
        state.cacheTasks = {}
        processDefinitionJson.tasks.forEach(v => {
          state.cacheTasks[v.id] = v
        })
        // global params
        state.globalParams = processDefinitionJson.globalParams
        // timeout
        state.timeout = processDefinitionJson.timeout

        state.tenantId = processDefinitionJson.tenantId
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },

  /**
   * Get process definition DAG diagram details
   */
  copyProcess ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectName}/process/copy`, {
        processDefinitionIds: payload.processDefinitionIds,
        targetProjectId: payload.targetProjectId
      }, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },

  /**
   * Get process definition DAG diagram details
   */
  moveProcess ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectName}/process/move`, {
        processDefinitionIds: payload.processDefinitionIds,
        targetProjectId: payload.targetProjectId
      }, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },

  /**
   * Get all the items created by the logged in user
   */
  getAllItems ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('projects/created-and-authorized-project', {}, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },

  /**
   * Get the process instance DAG diagram details
   */
  getInstancedetail ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/instance/select-by-id`, {
        processInstanceId: payload
      }, res => {
        // code
        state.code = res.data.processDefinitionCode
        // version
        state.version = res.data.processDefinitionVersion
        // name
        state.name = res.data.name
        // desc
        state.description = res.data.description
        // connects
        state.connects = JSON.parse(res.data.connects)
        // locations
        state.locations = JSON.parse(res.data.locations)
        // process instance
        const processInstanceJson = JSON.parse(res.data.processInstanceJson)
        // tasks info
        state.tasks = processInstanceJson.tasks
        // tasks cache
        state.cacheTasks = {}
        processInstanceJson.tasks.forEach(v => {
          state.cacheTasks[v.id] = v
        })
        // global params
        state.globalParams = processInstanceJson.globalParams
        // timeout
        state.timeout = processInstanceJson.timeout

        state.tenantId = processInstanceJson.tenantId

        // startup parameters
        state.startup = _.assign(state.startup, _.pick(res.data, ['commandType', 'failureStrategy', 'processInstancePriority', 'workerGroup', 'warningType', 'warningGroupId', 'receivers', 'receiversCc']))
        state.startup.commandParam = JSON.parse(res.data.commandParam)

        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * Create process definition
   */
  saveDAGchart ({ state }, payload) {
    return new Promise((resolve, reject) => {
      const data = {
        globalParams: state.globalParams,
        tasks: deleteDefinitionList(state.tasks),
        tenantId: state.tenantId,
        timeout: state.timeout
      }
      io.post(`projects/${state.projectName}/process/save`, {
        processDefinitionJson: JSON.stringify(data),
        name: _.trim(state.name),
        description: _.trim(state.description),
        locations: JSON.stringify(state.locations),
        connects: JSON.stringify(state.connects)
      }, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Process definition update
   */
  updateDefinition ({ state }, payload) {
    return new Promise((resolve, reject) => {
      const data = {
        globalParams: state.globalParams,
        tasks: deleteDefinitionList(state.tasks),
        tenantId: state.tenantId,
        timeout: state.timeout
      }
      io.post(`projects/${state.projectName}/process/update`, {
        processDefinitionJson: JSON.stringify(data),
        locations: JSON.stringify(state.locations),
        connects: JSON.stringify(state.connects),
        name: _.trim(state.name),
        description: _.trim(state.description),
        id: payload,
        releaseState: state.releaseState
      }, res => {
        resolve(res)
        state.isEditDag = false
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Process instance update
   */
  updateInstance ({ state }, payload) {
    return new Promise((resolve, reject) => {
      const data = {
        globalParams: state.globalParams,
        tasks: state.tasks,
        tenantId: state.tenantId,
        timeout: state.timeout
      }
      io.post(`projects/${state.projectName}/instance/update`, {
        processInstanceJson: JSON.stringify(data),
        locations: JSON.stringify(state.locations),
        connects: JSON.stringify(state.connects),
        processInstanceId: payload,
        syncDefine: state.syncDefine
      }, res => {
        resolve(res)
        state.isEditDag = false
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Get a list of process definitions (sub-workflow usage is not paged)
   */
  getProcessList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      if (state.processListS.length) {
        resolve()
        return
      }
      io.get(`projects/${state.projectName}/process/list`, payload, res => {
        state.processListS = res.data
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * Get a list of process definitions (list page usage with pagination)
   */
  getProcessListP ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/process/list-paging`, payload, res => {
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * Get a list of project
   */
  getProjectList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      if (state.projectListS.length) {
        resolve()
        return
      }
      io.get('projects/query-project-list', payload, res => {
        state.projectListS = res.data
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * Get a list of process definitions by project id
   */
  getProcessByProjectId ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/process/queryProcessDefinitionAllByProjectId`, payload, res => {
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * get datasource
   */
  getDatasourceList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('datasources/list', {
        type: payload
      }, res => {
        resolve(res)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * get resources
   */
  getResourcesList ({ state }) {
    return new Promise((resolve, reject) => {
      if (state.resourcesListS.length) {
        resolve()
        return
      }
      io.get('resources/list', {
        type: 'FILE'
      }, res => {
        state.resourcesListS = res.data
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * get jar
   */
  getResourcesListJar ({ state }) {
    return new Promise((resolve, reject) => {
      if (state.resourcesListJar.length) {
        resolve()
        return
      }
      io.get('resources/list/jar', {
        type: 'FILE'
      }, res => {
        state.resourcesListJar = res.data
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * Get process instance
   */
  getProcessInstance ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/instance/list-paging`, payload, res => {
        state.instanceListS = res.data.totalList
        resolve(res.data)
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * Get alarm list
   */
  getNotifyGroupList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('alert-group/list', res => {
        state.notifyGroupListS = _.map(res.data, v => {
          return {
            id: v.id,
            code: v.groupName,
            disabled: false
          }
        })
        resolve(_.cloneDeep(state.notifyGroupListS))
      }).catch(res => {
        reject(res)
      })
    })
  },
  /**
   * Process definition startup interface
   */
  processStart ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectName}/executors/start-process-instance`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Instance startup interface
   */
  instanceStart ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectName}/executors/start-process-instance-random-node`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * View log
   */
  getLog ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('log/detail', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Get the process instance id according to the process definition id
   * @param taskId
   */
  getSubProcessId ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/instance/select-sub-process`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Called before the process definition starts
   */
  getStartCheck ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectName}/executors/start-check`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Create timing
   */
  createSchedule ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectName}/schedule/create`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Preview timing
   */
  previewSchedule ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectName}/schedule/preview`, payload, res => {
        resolve(res.data)
        // alert(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Timing list paging
   */
  getScheduleList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/schedule/list-paging`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Timing online
   */
  scheduleOffline ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectName}/schedule/offline`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Timed offline
   */
  scheduleOnline ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectName}/schedule/online`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Edit timing
   */
  updateSchedule ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectName}/schedule/update`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Delete process instance
   */
  deleteInstance ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/instance/delete`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Batch delete process instance
   */
  batchDeleteInstance ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/instance/batch-delete`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Delete definition
   */
  deleteDefinition ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/process/delete`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Batch delete definition
   */
  batchDeleteDefinition ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/process/batch-delete`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * export definition
   */
  exportDefinition ({ state }, payload) {
    const downloadBlob = (data, fileNameS = 'json') => {
      if (!data) {
        return
      }
      const blob = new Blob([data])
      const fileName = `${fileNameS}.json`
      if ('download' in document.createElement('a')) { // 不是IE浏览器
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.style.display = 'none'
        link.href = url
        link.setAttribute('download', fileName)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link) // 下载完成移除元素
        window.URL.revokeObjectURL(url) // 释放掉blob对象
      } else { // IE 10+
        window.navigator.msSaveBlob(blob, fileName)
      }
    }

    io.get(`projects/${state.projectName}/process/export`, { processDefinitionIds: payload.processDefinitionIds }, res => {
      downloadBlob(res, payload.fileName)
    }, e => {

    }, {
      responseType: 'blob'
    })
  },

  /**
   * Process instance get variable
   */
  getViewvariables ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/instance/view-variables`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Get udfs function based on data source
   */
  getUdfList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('resources/udf-func/list', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Query task instance list
   */
  getTaskInstanceList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/task-instance/list-paging`, payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Force fail/kill/need_fault_tolerance task success
   */
  forceTaskSuccess ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post(`projects/${state.projectName}/task-instance/force-success`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Query task record list
   */
  getTaskRecordList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('projects/task-record/list-paging', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Query history task record list
   */
  getHistoryTaskRecordList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('projects/task-record/history-list-paging', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * tree chart
   */
  getViewTree ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/process/view-tree`, payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * gantt chart
   */
  getViewGantt ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/instance/view-gantt`, payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * Query task node list
   */
  getProcessTasksList ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/process/gen-task-list`, payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  getTaskListDefIdAll ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/process/get-task-list`, payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * remove timing
   */
  deleteTiming ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get(`projects/${state.projectName}/schedule/delete`, payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  getResourceId ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('resources/queryResource', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  }
}
