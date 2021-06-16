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
<template>
  <div class="form-model-wrapper" v-clickoutside="_handleClose">
    <div class="title-box">
      <span class="name">{{$t('Current node settings')}}</span>
      <span class="go-subtask">
        <!-- Component can't pop up box to do component processing -->
        <m-log :item="backfillItem">
          <template slot="history"><a href="javascript:" @click="_seeHistory" ><em class="ansicon el-icon-alarm-clock"></em><em>{{$t('View history')}}</em></a></template>
          <template slot="log"><a href="javascript:"><em class="ansicon el-icon-document"></em><em>{{$t('View log')}}</em></a></template>
        </m-log>
        <a href="javascript:" @click="_goSubProcess" v-if="_isGoSubProcess"><em class="ansicon ri-node-tree"></em><em>{{$t('Enter this child node')}}</em></a>
      </span>
    </div>
    <div class="content-box" v-if="isContentBox">
      <div class="form-model">
        <!-- Node name -->
        <m-list-box>
          <div slot="text">{{$t('Node name')}}</div>
          <div slot="content">
            <el-input
              type="text"
              v-model="name"
              size="small"
              :disabled="isDetails"
              :placeholder="$t('Please enter name (required)')"
              maxlength="100"
              @blur="_verifName()">
            </el-input>
          </div>
        </m-list-box>

        <!-- Running sign -->
        <m-list-box>
          <div slot="text">{{$t('Run flag')}}</div>
          <div slot="content">
            <el-radio-group v-model="runFlag" size="small">
              <el-radio :label="'NORMAL'" :disabled="isDetails">{{$t('Normal')}}</el-radio>
              <el-radio :label="'FORBIDDEN'" :disabled="isDetails">{{$t('Prohibition execution')}}</el-radio>
            </el-radio-group>
          </div>
        </m-list-box>

        <!-- description -->
        <m-list-box>
          <div slot="text">{{$t('Description')}}</div>
          <div slot="content">
            <el-input
              :rows="2"
              type="textarea"
              :disabled="isDetails"
              v-model="desc"
              :placeholder="$t('Please enter description')">
            </el-input>
          </div>
        </m-list-box>

        <!-- Task priority -->
        <m-list-box>
          <div slot="text">{{$t('Task priority')}}</div>
          <div slot="content">
            <span class="label-box" style="width: 193px;display: inline-block;">
              <m-priority v-model="taskInstancePriority"></m-priority>
            </span>
            <span class="text-b">{{$t('Worker group')}}</span>
            <m-worker-groups v-model="workerGroup"></m-worker-groups>
          </div>
        </m-list-box>

        <!-- Number of failed retries -->
        <m-list-box v-if="nodeData.taskType !== 'SUB_PROCESS'">
          <div slot="text">{{$t('Number of failed retries')}}</div>
          <div slot="content">
            <m-select-input v-model="maxRetryTimes" :list="[0,1,2,3,4]"></m-select-input>
            <span>({{$t('Times')}})</span>
            <span class="text-b">{{$t('Failed retry interval')}}</span>
            <m-select-input v-model="retryInterval" :list="[1,10,30,60,120]"></m-select-input>
            <span>({{$t('Minute')}})</span>
          </div>
        </m-list-box>

        <!-- Delay execution time -->
        <m-list-box v-if="nodeData.taskType !== 'SUB_PROCESS' && nodeData.taskType !== 'CONDITIONS' && nodeData.taskType !== 'DEPENDENT' && nodeData.taskType !== 'SWITCH'">
          <div slot="text">{{$t('Delay execution time')}}</div>
          <div slot="content">
            <m-select-input v-model="delayTime" :list="[0,1,5,10]"></m-select-input>
            <span>({{$t('Minute')}})</span>
          </div>
        </m-list-box>

        <!-- Branch flow -->
        <m-list-box v-if="nodeData.taskType === 'CONDITIONS'">
          <div slot="text">{{$t('State')}}</div>
          <div slot="content">
            <span class="label-box" style="width: 193px;display: inline-block;">
              <el-select style="width: 157px;" size="small" v-model="successNode" :disabled="true">
                <el-option v-for="item in stateList" :key="item.value" :value="item.value" :label="item.label"></el-option>
              </el-select>
            </span>
            <span class="text-b" style="padding-left: 38px">{{$t('Branch flow')}}</span>
            <el-select style="width: 157px;" size="small" v-model="successBranch" clearable>
              <el-option v-for="item in nodeData.rearList" :key="item.value" :value="item.value" :label="item.label"></el-option>
            </el-select>
          </div>
        </m-list-box>
        <m-list-box v-if="nodeData.taskType === 'CONDITIONS'">
          <div slot="text">{{$t('State')}}</div>
          <div slot="content">
            <span class="label-box" style="width: 193px;display: inline-block;">
              <el-select style="width: 157px;" size="small" v-model="failedNode" :disabled="true">
                <el-option v-for="item in stateList" :key="item.value" :value="item.value" :label="item.label"></el-option>
              </el-select>
            </span>
            <span class="text-b" style="padding-left: 38px">{{$t('Branch flow')}}</span>
            <el-select style="width: 157px;" size="small" v-model="failedBranch" clearable>
              <el-option v-for="item in nodeData.rearList" :key="item.value" :value="item.value" :label="item.label"></el-option>
            </el-select>
          </div>
        </m-list-box>

        <!-- Task timeout alarm -->
        <m-timeout-alarm
          v-if="nodeData.taskType !== 'DEPENDENT' && nodeData.taskType !== 'SWITCH'"
          ref="timeout"
          :backfill-item="backfillItem"
          @on-timeout="_onTimeout">
        </m-timeout-alarm>
        <!-- Dependent timeout alarm -->
        <m-dependent-timeout
          v-if="nodeData.taskType === 'DEPENDENT'"
          ref="dependentTimeout"
          :backfill-item="backfillItem"
          @on-timeout="_onDependentTimeout">
        </m-dependent-timeout>

        <!-- shell node -->
        <m-shell
          v-if="nodeData.taskType === 'SHELL'"
          @on-params="_onParams"
          @on-cache-params="_onCacheParams"
          ref="SHELL"
          :backfill-item="backfillItem">
        </m-shell>
        <!-- waterdrop node -->
        <m-waterdrop
          v-if="nodeData.taskType === 'WATERDROP'"
          @on-params="_onParams"
          @on-cache-params="_onCacheParams"
          ref="WATERDROP"
          :backfill-item="backfillItem">
        </m-waterdrop>
        <!-- sub_process node -->
        <m-sub-process
          v-if="nodeData.taskType === 'SUB_PROCESS'"
          @on-params="_onParams"
          @on-cache-params="_onCacheParams"
          @on-set-process-name="_onSetProcessName"
          ref="SUB_PROCESS"
          :backfill-item="backfillItem">
        </m-sub-process>
        <!-- procedure node -->
        <m-procedure
          v-if="nodeData.taskType === 'PROCEDURE'"
          @on-params="_onParams"
          @on-cache-params="_onCacheParams"
          ref="PROCEDURE"
          :backfill-item="backfillItem">
        </m-procedure>
        <!-- sql node -->
        <m-sql
          v-if="nodeData.taskType === 'SQL'"
          @on-params="_onParams"
          @on-cache-params="_onCacheParams"
          ref="SQL"
          :create-node-id="nodeData.id"
          :backfill-item="backfillItem">
        </m-sql>
        <!-- spark node -->
        <m-spark
          v-if="nodeData.taskType === 'SPARK'"
          @on-params="_onParams"
          @on-cache-params="_onCacheParams"
          ref="SPARK"
          :backfill-item="backfillItem">
        </m-spark>
        <m-flink
          v-if="nodeData.taskType === 'FLINK'"
          @on-params="_onParams"
          @on-cache-params="_onCacheParams"
          ref="FLINK"
          :backfill-item="backfillItem">
        </m-flink>
        <!-- mr node -->
        <m-mr
          v-if="nodeData.taskType === 'MR'"
          @on-params="_onParams"
          @on-cache-params="_onCacheParams"
          ref="MR"
          :backfill-item="backfillItem">
        </m-mr>
        <!-- python node -->
        <m-python
          v-if="nodeData.taskType === 'PYTHON'"
          @on-params="_onParams"
          @on-cache-params="_onCacheParams"
          ref="PYTHON"
          :backfill-item="backfillItem">
        </m-python>
        <!-- dependent node -->
        <m-dependent
          v-if="nodeData.taskType === 'DEPENDENT'"
          @on-dependent="_onDependent"
          @on-cache-dependent="_onCacheDependent"
          ref="DEPENDENT"
          :backfill-item="backfillItem">
        </m-dependent>
        <m-http
          v-if="nodeData.taskType === 'HTTP'"
          @on-params="_onParams"
          @on-cache-params="_onCacheParams"
          ref="HTTP"
          :backfill-item="backfillItem">
        </m-http>
        <m-datax
          v-if="nodeData.taskType === 'DATAX'"
          @on-params="_onParams"
          @on-cache-params="_onCacheParams"
          ref="DATAX"
          :backfill-item="backfillItem">
        </m-datax>
        <m-sqoop
          v-if="nodeData.taskType === 'SQOOP'"
          @on-params="_onParams"
          @on-cache-params="_onCacheParams"
          ref="SQOOP"
          :backfill-item="backfillItem">
        </m-sqoop>
        <m-conditions
          v-if="nodeData.taskType === 'CONDITIONS'"
          ref="CONDITIONS"
          @on-dependent="_onDependent"
          @on-cache-dependent="_onCacheDependent"
          :backfill-item="backfillItem"
          :pre-node="nodeData.preNode">
        </m-conditions>
        <m-switch
          v-if="nodeData.taskType === 'SWITCH'"
          ref="SWITCH"
          @on-dependent="_onDependent"
          @on-cache-dependent="_onCacheDependent"
          :backfill-item="backfillItem"
          :rear-list="nodeData.rearList"
          :pre-node="nodeData.preNode">
        </m-switch>
        <!-- Pre-tasks in workflow -->
        <m-pre-tasks
          v-if="['SHELL', 'SUB_PROCESS'].indexOf(nodeData.taskType) > -1"
          @on-pre-tasks="_onPreTasks"
          ref="PRE_TASK"
          :backfill-item="backfillItem"></m-pre-tasks>
      </div>
    </div>
    <div class="bottom-box">
      <div class="submit" style="background: #fff;">
        <el-button type="text" size="small" id="cancelBtn"> {{$t('Cancel')}} </el-button>
        <el-button type="primary" size="small" round :loading="spinnerLoading" @click="ok()" :disabled="isDetails">{{spinnerLoading ? 'Loading...' : $t('Confirm add')}} </el-button>
      </div>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import mLog from './log'
  import mMr from './tasks/mr'
  import mSql from './tasks/sql'
  import i18n from '@/module/i18n'
  import mListBox from './tasks/_source/listBox'
  import mShell from './tasks/shell'
  import mWaterdrop from './tasks/waterdrop'
  import mSpark from './tasks/spark'
  import mFlink from './tasks/flink'
  import mPython from './tasks/python'
  import JSP from './../plugIn/jsPlumbHandle'
  import mProcedure from './tasks/procedure'
  import mDependent from './tasks/dependent'
  import mHttp from './tasks/http'
  import mDatax from './tasks/datax'
  import mConditions from './tasks/conditions'
  import mSwitch from './tasks/switch'
  import mSqoop from './tasks/sqoop'
  import mSubProcess from './tasks/sub_process'
  import mSelectInput from './_source/selectInput'
  import mTimeoutAlarm from './_source/timeoutAlarm'
  import mDependentTimeout from './_source/dependentTimeout'
  import mWorkerGroups from './_source/workerGroups'
  import mPreTasks from './tasks/pre_tasks'
  import clickoutside from '@/module/util/clickoutside'
  import disabledState from '@/module/mixin/disabledState'
  import { isNameExDag, rtBantpl } from './../plugIn/util'
  import mPriority from '@/module/components/priority/priority'

  export default {
    name: 'form-model',
    data () {
      return {
        // loading
        spinnerLoading: false,
        // node name
        name: '',
        // description
        desc: '',
        // Node echo data
        backfillItem: {},
        cacheBackfillItem: {},
        // Resource(list)
        resourcesList: [],
        successNode: 'success',
        failedNode: 'failed',
        successBranch: '',
        failedBranch: '',
        conditionResult: {
          successNode: [],
          failedNode: []
        },
        // dependence
        dependence: {},
        // cache dependence
        cacheDependence: {},
        // task code
        code: '',
        // Current node params data
        params: {},
        // Running sign
        runFlag: 'NORMAL',
        // The second echo problem caused by the node data is specifically which node hook caused the unfinished special treatment
        isContentBox: false,
        // Number of failed retries
        maxRetryTimes: '0',
        // Failure retry interval
        retryInterval: '1',
        // Delay execution time
        delayTime: '0',
        // Task timeout alarm
        timeout: {},
        // (For Dependent nodes) Wait start timeout alarm
        waitStartTimeout: {},
        // Task priority
        taskInstancePriority: 'MEDIUM',
        // worker group id
        workerGroup: 'default',
        stateList: [
          {
            value: 'success',
            label: `${i18n.$t('Success')}`
          },
          {
            value: 'failed',
            label: `${i18n.$t('Failed')}`
          }
        ],
        // preTasks
        preTaskIdsInWorkflow: [],
        preTasksToAdd: [], // pre-taskIds to add, used in jsplumb connects
        preTasksToDelete: [] // pre-taskIds to delete, used in jsplumb connects
      }
    },
    /**
     * Click on events that are not generated internally by the component
     */
    directives: { clickoutside },
    mixins: [disabledState],
    props: {
      nodeData: Object
    },
    methods: {
      ...mapActions('dag', ['getTaskInstanceList']),
      /**
       * depend
       */
      _onDependent (o) {
        this.dependence = Object.assign(this.dependence, {}, o)
      },
      /**
       * Pre-tasks in workflow
       */
      _onPreTasks (o) {
        this.preTaskIdsInWorkflow = o.preTasks
        this.preTasksToAdd = o.preTasksToAdd
        this.preTasksToDelete = o.preTasksToDelete
      },
      /**
       * cache dependent
       */
      _onCacheDependent (o) {
        this.cacheDependence = Object.assign(this.cacheDependence, {}, o)
      },
      /**
       * Task timeout alarm
       */
      _onTimeout (o) {
        this.timeout = Object.assign(this.timeout, {}, o)
      },
      /**
       * Dependent timeout alarm
       */
      _onDependentTimeout (o) {
        this.timeout = Object.assign(this.timeout, {}, o.waitCompleteTimeout)
        this.waitStartTimeout = Object.assign(this.waitStartTimeout, {}, o.waitStartTimeout)
      },
      /**
       * Click external to close the current component
       */
      _handleClose () {
        // this.close()
      },
      /**
       * Jump to task instance
       */
      _seeHistory () {
        this.$emit('seeHistory', this.backfillItem.name)
      },
      /**
       * Enter the child node to judge the process instance or the process definition
       * @param  type = instance
       */
      _goSubProcess () {
        if (_.isEmpty(this.backfillItem)) {
          this.$message.warning(`${i18n.$t('The newly created sub-Process has not yet been executed and cannot enter the sub-Process')}`)
          return
        }
        if (this.router.history.current.name === 'projects-instance-details') {
          let stateId = $(`#${this.nodeData.id}`).attr('data-state-id') || null
          if (!stateId) {
            this.$message.warning(`${i18n.$t('The task has not been executed and cannot enter the sub-Process')}`)
            return
          }
          this.store.dispatch('dag/getSubProcessId', { taskId: stateId }).then(res => {
            this.$emit('onSubProcess', {
              subProcessId: res.data.subProcessInstanceId,
              fromThis: this
            })
          }).catch(e => {
            this.$message.error(e.msg || '')
          })
        } else {
          this.$emit('onSubProcess', {
            subProcessId: this.backfillItem.params.processDefinitionId,
            fromThis: this
          })
        }
      },
      /**
       * return params
       */
      _onParams (o) {
        this.params = Object.assign({}, o)
      },

      _onCacheParams (o) {
        this.params = Object.assign(this.params, {}, o)
        this._cacheItem()
      },

      _cacheItem () {
        this.conditionResult.successNode[0] = this.successBranch
        this.conditionResult.failedNode[0] = this.failedBranch
        this.$emit('cacheTaskInfo', {
          item: {
            type: this.nodeData.taskType,
            id: this.nodeData.id,
            name: this.name,
            code: this.code,
            params: this.params,
            desc: this.desc,
            runFlag: this.runFlag,
            conditionResult: this.conditionResult,
            dependence: this.cacheDependence,
            maxRetryTimes: this.maxRetryTimes,
            retryInterval: this.retryInterval,
            delayTime: this.delayTime,
            timeout: this.timeout,
            waitStartTimeout: this.waitStartTimeout,
            taskInstancePriority: this.taskInstancePriority,
            workerGroup: this.workerGroup,
            status: this.status,
            branch: this.branch
          },
          fromThis: this
        })
      },
      /**
       * verification name
       */
      _verifName () {
        if (!_.trim(this.name)) {
          this.$message.warning(`${i18n.$t('Please enter name (required)')}`)
          return false
        }
        if (this.successBranch !== '' && this.successBranch !== null && this.successBranch === this.failedBranch) {
          this.$message.warning(`${i18n.$t('Cannot select the same node for successful branch flow and failed branch flow')}`)
          return false
        }
        if (this.name === this.backfillItem.name) {
          return true
        }
        // Name repeat depends on dom backfill dependent store
        if (isNameExDag(this.name, _.isEmpty(this.backfillItem) ? 'dom' : 'backfill')) {
          this.$message.warning(`${i18n.$t('Name already exists')}`)
          return false
        }
        return true
      },
      _verifWorkGroup () {
        let item = this.store.state.security.workerGroupsListAll.find(item => {
          return item.id === this.workerGroup
        })
        if (item === undefined) {
          this.$message.warning(`${i18n.$t('The Worker group no longer exists, please select the correct Worker group!')}`)
          return false
        }
        return true
      },
      /**
       * Global verification procedure
       */
      _verification () {
        // Verify name
        if (!this._verifName()) {
          return
        }
        // verif workGroup
        if (!this._verifWorkGroup()) {
          return
        }
        // Verify task alarm parameters
        if (this.nodeData.taskType === 'DEPENDENT') {
          if (!this.$refs.dependentTimeout._verification()) {
            return
          }
        } else if (this.nodeData.taskType !== 'SWITCH') {
          if (!this.$refs.timeout._verification()) {
            return
          }
        }

        // Verify node parameters
        if (!this.$refs[this.nodeData.taskType]._verification()) {
          return
        }
        // Verify preTasks and update dag-things
        if (this.$refs.PRE_TASK) {
          if (!this.$refs.PRE_TASK._verification()) {
            return
          } else {
            // Sync data-targetarr
            $(`#${this.nodeData.id}`).attr(
              'data-targetarr', this.preTaskIdsInWorkflow ? this.preTaskIdsInWorkflow.join(',') : '')

            // Update JSP connections
            let plumbIns = JSP.JspInstance
            let targetId = this.nodeData.id

            // Update new connections
            this.preTasksToAdd.map(sourceId => {
              plumbIns.connect({
                source: sourceId,
                target: targetId,
                type: 'basic',
                paintStyle: { strokeWidth: 2, stroke: '#2d8cf0' },
                HoverPaintStyle: { stroke: '#ccc', strokeWidth: 3 }
              })
            })

            // Update remove connections
            let currentConnects = plumbIns.getAllConnections()
            let len = currentConnects.length
            for (let i = 0; i < len; i++) {
              if (this.preTasksToDelete.indexOf(currentConnects[i].sourceId) > -1 && currentConnects[i].targetId === targetId) {
                plumbIns.deleteConnection(currentConnects[i])
                i -= 1
                len -= 1
              }
            }
          }
        }

        $(`#${this.nodeData.id}`).find('span').text(this.name)
        this.conditionResult.successNode[0] = this.successBranch
        this.conditionResult.failedNode[0] = this.failedBranch
        // Store the corresponding node data structure
        this.$emit('addTaskInfo', {
          item: {
            type: this.nodeData.taskType,
            id: this.nodeData.id,
            name: this.name,
            code: this.code,
            params: this.params,
            desc: this.desc,
            runFlag: this.runFlag,
            conditionResult: this.conditionResult,
            dependence: this.dependence,
            maxRetryTimes: this.maxRetryTimes,
            retryInterval: this.retryInterval,
            delayTime: this.delayTime,
            timeout: this.timeout,
            waitStartTimeout: this.waitStartTimeout,
            taskInstancePriority: this.taskInstancePriority,
            workerGroup: this.workerGroup,
            status: this.status,
            branch: this.branch
          },
          fromThis: this
        })

        // set run flag
        this._setRunFlag()
      },
      /**
       * Sub-workflow selected node echo name
       */
      _onSetProcessName (name) {
        this.name = name
      },
      /**
       *  set run flag
       */
      _setRunFlag () {
        let dom = $(`#${this.nodeData.id}`).find('.ban-p')
        dom.html('')
        if (this.runFlag === 'FORBIDDEN') {
          dom.append(rtBantpl())
        }
      },
      /**
       * Submit verification
       */
      ok () {
        this._verification()
      },
      /**
       * Close and destroy component and component internal events
       */
      close () {
        let flag = false
        // Delete node without storage
        if (!this.backfillItem.name) {
          flag = true
        }
        this.isContentBox = false
        // flag Whether to delete a node this.$destroy()
        this.$emit('close', {
          item: this.cacheBackfillItem,
          flag: flag,
          fromThis: this
        })
      }
    },
    watch: {
      /**
       * Watch the item change, cache the value it changes
       **/
      _item (val) {
        // this._cacheItem()
      }
    },
    created () {
      // Unbind copy and paste events
      JSP.removePaste()
      // Backfill data
      let taskList = this.store.state.dag.tasks

      // fillback use cacheTasks
      let cacheTasks = this.store.state.dag.cacheTasks
      let o = {}
      if (cacheTasks[this.nodeData.id]) {
        o = cacheTasks[this.nodeData.id]
        this.backfillItem = cacheTasks[this.nodeData.id]
      } else {
        if (taskList.length) {
          taskList.forEach(v => {
            if (v.id === this.nodeData.id) {
              o = v
              this.backfillItem = v
            }
          })
        }
      }
      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        this.code = o.code
        this.name = o.name
        this.taskInstancePriority = o.taskInstancePriority
        this.runFlag = o.runFlag || 'NORMAL'
        this.desc = o.desc
        this.maxRetryTimes = o.maxRetryTimes
        this.retryInterval = o.retryInterval
        this.delayTime = o.delayTime
        if (o.conditionResult) {
          this.successBranch = o.conditionResult.successNode[0]
          this.failedBranch = o.conditionResult.failedNode[0]
        }
        // If the workergroup has been deleted, set the default workergroup
        for (let i = 0; i < this.store.state.security.workerGroupsListAll.length; i++) {
          let workerGroup = this.store.state.security.workerGroupsListAll[i].id
          if (o.workerGroup === workerGroup) {
            break
          }
        }
        if (o.workerGroup === undefined) {
          this.store.dispatch('dag/getTaskInstanceList', {
            pageSize: 10, pageNo: 1, processInstanceId: this.nodeData.instanceId, name: o.name
          }).then(res => {
            this.workerGroup = res.totalList[0].workerGroup
          })
        } else {
          this.workerGroup = o.workerGroup
        }

        this.params = o.params || {}
        this.dependence = o.dependence || {}
        this.cacheDependence = o.dependence || {}
      } else {
        this.workerGroup = this.store.state.security.workerGroupsListAll[0].id
      }
      this.cacheBackfillItem = JSON.parse(JSON.stringify(o))
      this.isContentBox = true

      // Init value of preTask selector
      let preTaskIds = $(`#${this.nodeData.id}`).attr('data-targetarr')
      if (!_.isEmpty(this.backfillItem)) {
        if (preTaskIds && preTaskIds.length) {
          this.backfillItem.preTasks = preTaskIds.split(',')
        } else {
          this.backfillItem.preTasks = []
        }
      }
    },
    mounted () {
      let self = this
      $('#cancelBtn').mousedown(function (event) {
        event.preventDefault()
        self.close()
      })
    },
    updated () {
    },
    beforeDestroy () {
    },
    destroyed () {
    },
    computed: {
      /**
       * Child workflow entry show/hide
       */
      _isGoSubProcess () {
        return this.nodeData.taskType === 'SUB_PROCESS' && this.name
      },

      // Define the item model
      _item () {
        return {
          type: this.nodeData.taskType,
          id: this.nodeData.id,
          code: this.code,
          name: this.name,
          desc: this.desc,
          runFlag: this.runFlag,
          dependence: this.cacheDependence,
          maxRetryTimes: this.maxRetryTimes,
          retryInterval: this.retryInterval,
          delayTime: this.delayTime,
          timeout: this.timeout,
          waitStartTimeout: this.waitStartTimeout,
          taskInstancePriority: this.taskInstancePriority,
          workerGroup: this.workerGroup,
          successBranch: this.successBranch,
          failedBranch: this.failedBranch
        }
      }
    },
    components: {
      mListBox,
      mMr,
      mShell,
      mWaterdrop,
      mSubProcess,
      mProcedure,
      mSql,
      mLog,
      mSpark,
      mFlink,
      mPython,
      mDependent,
      mHttp,
      mDatax,
      mSqoop,
      mConditions,
      mSwitch,
      mSelectInput,
      mTimeoutAlarm,
      mDependentTimeout,
      mPriority,
      mWorkerGroups,
      mPreTasks
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  @import "./formModel";
  .ans-radio-disabled {
    .ans-radio-inner:after {
      background-color: #6F8391
    }
  }
</style>
