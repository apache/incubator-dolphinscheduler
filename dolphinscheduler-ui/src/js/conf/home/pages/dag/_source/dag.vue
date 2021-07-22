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
  <div class="clearfix dag-model" >
    <div class="toolbar">
      <div class="title"><span>{{$t('Toolbar')}}</span></div>
      <div class="toolbar-btn">
        <div class="bar-box roundedRect jtk-draggable jtk-droppable jtk-endpoint-anchor jtk-connected"
             :class="v === dagBarId ? 'active' : ''"
             :id="v"
             :key="v"
             v-for="(item,v) in tasksTypeList"
             @mousedown="_getDagId(v)">
          <div data-toggle="tooltip" :title="item.desc">
            <div class="icos" :class="'icos-' + v" ></div>
          </div>
        </div>
      </div>
    </div>
    <div class="dag-contect">
      <div class="dag-toolbar">
        <div class="assist-btn">
          <el-tooltip :content="$t('View variables')" placement="top" :enterable="false">
           <span>
            <el-button
              style="vertical-align: middle;"
              type="primary"
              size="mini"
              :disabled="$route.name !== 'projects-instance-details'"
              @click="_toggleView"
              icon="el-icon-c-scale-to-original">
            </el-button>
           </span>
          </el-tooltip>
          <el-tooltip :content="$t('Startup parameter')" placement="top" :enterable="false">
            <span>
              <el-button
                style="vertical-align: middle;"
                type="primary"
                size="mini"
                :disabled="$route.name !== 'projects-instance-details'"
                @click="_toggleParam"
                icon="el-icon-arrow-right">
              </el-button>
            </span>
          </el-tooltip>
          <span class="name">{{name}}</span>
          &nbsp;
          <span v-if="name"  class="copy-name" @click="_copyName" :data-clipboard-text="name"><em class="el-icon-copy-document" data-container="body"  data-toggle="tooltip" :title="$t('Copy name')" ></em></span>
        </div>
        <div class="save-btn">
          <div class="operation" style="vertical-align: middle;">
            <a href="javascript:"
               v-for="(item,$index) in toolOperList"
               :class="_operationClass(item)"
               :id="item.code"
               :key="$index"
               @click="_ckOperation(item,$event)">
              <el-tooltip :content="item.desc" placement="top" :enterable="false">
                <span><el-button type="text" class="operBtn" :icon="item.icon"></el-button></span>
              </el-tooltip>
            </a>
          </div>
          <el-tooltip :content="$t('Format DAG')" placement="top" :enterable="false">
            <span>
              <el-button
                type="primary"
                icon="el-icon-caret-right"
                size="mini"
                v-if="(type === 'instance' || 'definition') && urlParam.id !=undefined"
                style="vertical-align: middle;"
                @click="dagAutomaticLayout">
              </el-button>
            </span>
          </el-tooltip>
          <el-tooltip :content="$t('Refresh DAG status')" placement="top" :enterable="false">
            <span>
              <el-button
                style="vertical-align: middle;"
                icon="el-icon-refresh"
                type="primary"
                :loading="isRefresh"
                v-if="type === 'instance'"
                @click="!isRefresh && _refresh()"
                size="mini" >
              </el-button>
            </span>
          </el-tooltip>
          <el-button
                  v-if="isRtTasks"
                  style="vertical-align: middle;"
                  type="primary"
                  size="mini"
                  icon="el-icon-back"
                  @click="_rtNodesDag" >
            {{$t('Return_1')}}
          </el-button>
          <span>
            <el-button
              type="primary"
              icon="el-icon-switch-button"
              size="mini"
              v-if="(type === 'instance' || 'definition') "
              style="vertical-align: middle;"
              @click="_closeDAG">
              {{$t('Close')}}
            </el-button>
          </span>
          <el-button
                  style="vertical-align: middle;"
                  type="primary"
                  size="mini"
                  :loading="spinnerLoading"
                  @click="_saveChart"
                  icon="el-icon-document-checked"
                  >
            {{spinnerLoading ? 'Loading...' : $t('Save')}}
          </el-button>
          <span>
            <el-button
              style="vertical-align: middle;"
              type="primary"
              size="mini"
              :loading="spinnerLoading"
              @click="_version"
              :disabled="$route.params.id == null"
              icon="el-icon-info">
              {{spinnerLoading ? 'Loading...' : $t('Version Info')}}
            </el-button>
          </span>
        </div>
      </div>
      <div class="scrollbar dag-container">
        <div class="jtk-demo" id="jtk-demo">
          <div class="jtk-demo-canvas canvas-wide statemachine-demo jtk-surface jtk-surface-nopan jtk-draggable" id="canvas" ></div>
        </div>
      </div>
      <el-drawer
        :visible.sync="drawer"
        size=""
        :with-header="false">
        <m-versions :versionData = versionData :isInstance="type === 'instance'" @mVersionSwitchProcessDefinitionVersion="mVersionSwitchProcessDefinitionVersion" @mVersionGetProcessDefinitionVersionsPage="mVersionGetProcessDefinitionVersionsPage" @mVersionDeleteProcessDefinitionVersion="mVersionDeleteProcessDefinitionVersion" @closeVersion="closeVersion"></m-versions>
      </el-drawer>
      <el-drawer
        :visible.sync="nodeDrawer"
        size=""
        :with-header="false">
        <m-form-model v-if="nodeDrawer" :nodeData=nodeData @seeHistory="seeHistory" @addTaskInfo="addTaskInfo" @cacheTaskInfo="cacheTaskInfo" @close="close" @onSubProcess="onSubProcess"></m-form-model>
      </el-drawer>
      <el-drawer
        :visible.sync="lineDrawer"
        size=""
        :wrapperClosable="false"
        :with-header="false">
        <m-form-line-model :lineData = lineData @addLineInfo="addLineInfo" @cancel="cancel"></m-form-line-model>
      </el-drawer>
      <el-drawer
        :visible.sync="udpDrawer"
        size=""
        :wrapperClosable="false"
        :with-header="false">
        <m-udp></m-udp>
      </el-drawer>
      <el-dialog
        :title="$t('Set the DAG diagram name')"
        :visible.sync="dialogVisible"
        width="auto">
        <m-udp ref="mUdp" @onUdp="onUdpDialog" @close="closeDialog"></m-udp>
      </el-dialog>
      <el-dialog
        :title="$t('Please set the parameters before starting')"
        :visible.sync="startDialog"
        width="auto">
        <m-start :startData= "startData" :startNodeList="startNodeList" :sourceType="sourceType" @onUpdateStart="onUpdateStart" @closeStart="closeStart"></m-start>
      </el-dialog>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import Dag from './dag'
  import mUdp from './udp/udp'
  import i18n from '@/module/i18n'
  import { jsPlumb } from 'jsplumb'
  import Clipboard from 'clipboard'
  import { allNodesId } from './plugIn/util'
  import { toolOper, tasksType } from './config'
  import mFormModel from './formModel/formModel'
  import mFormLineModel from './formModel/formLineModel'
  import { formatDate } from '@/module/filter/filter'
  import { findComponentDownward } from '@/module/util/'
  import disabledState from '@/module/mixin/disabledState'
  import { mapActions, mapState, mapMutations } from 'vuex'
  import mStart from '../../projects/pages/definition/pages/list/_source/start'
  import mVersions from '../../projects/pages/definition/pages/list/_source/versions'

  let eventModel

  export default {
    name: 'dag-chart',
    data () {
      return {
        tasksTypeList: tasksType,
        toolOperList: toolOper(this),
        dagBarId: null,
        toolOperCode: '',
        spinnerLoading: false,
        urlParam: {
          id: this.$route.params.id || null
        },
        isRtTasks: false,
        isRefresh: false,
        isLoading: false,
        taskId: null,
        arg: false,
        versionData: {
          processDefinition: {
            id: null,
            version: '',
            releaseState: ''
          },
          processDefinitionVersions: [],
          total: null,
          pageNo: null,
          pageSize: null
        },
        drawer: false,
        nodeData: {
          id: null,
          taskType: '',
          self: {},
          preNode: [],
          rearList: [],
          instanceId: null
        },
        nodeDrawer: false,
        lineData: {
          id: null,
          sourceId: '',
          targetId: ''
        },
        lineDrawer: false,
        udpDrawer: false,
        dialogVisible: false,
        startDialog: false,
        startData: {},
        startNodeList: '',
        sourceType: ''
      }
    },
    mixins: [disabledState],
    props: {
      type: String,
      releaseState: String
    },
    methods: {
      ...mapActions('dag', ['saveDAGchart', 'updateInstance', 'updateDefinition', 'getTaskState', 'switchProcessDefinitionVersion', 'getProcessDefinitionVersionsPage', 'deleteProcessDefinitionVersion']),
      ...mapMutations('dag', ['addTasks', 'cacheTasks', 'resetParams', 'setIsEditDag', 'setName', 'addConnects', 'resetLocalParam']),
      startRunning (item, startNodeList, sourceType) {
        this.startData = item
        this.startNodeList = startNodeList
        this.sourceType = sourceType
        this.startDialog = true
      },
      onUpdateStart () {
        this.startDialog = false
      },
      closeStart () {
        this.startDialog = false
      },
      // DAG automatic layout
      dagAutomaticLayout () {
        if (this.store.state.dag.isEditDag) {
          this.$message.warning(`${i18n.$t('Please save the DAG before formatting')}`)
          return false
        }
        $('#canvas').html('')

        // Destroy round robin
        Dag.init({
          dag: this,
          instance: jsPlumb.getInstance({
            Endpoint: [
              'Dot', { radius: 1, cssClass: 'dot-style' }
            ],
            Connector: 'Bezier',
            PaintStyle: { lineWidth: 2, stroke: '#456' }, // Connection style
            ConnectionOverlays: [
              [
                'Arrow',
                {
                  location: 1,
                  id: 'arrow',
                  length: 12,
                  foldback: 0.8
                }
              ],
              ['Label', {
                location: 0.5,
                id: 'label'
              }]
            ],
            Container: 'canvas',
            ConnectionsDetachable: true
          })
        })
        if (this.tasks.length) {
          Dag.backfill(true)
          if (this.type === 'instance') {
            this._getTaskState(false).then(res => {})
          }
        } else {
          Dag.create()
        }
      },

      init (args) {
        if (this.tasks.length) {
          Dag.backfill(args)
          // Process instances can view status
          if (this.type === 'instance') {
            this._getTaskState(false).then(res => {})
            // Round robin acquisition status
            this.setIntervalP = setInterval(() => {
              this._getTaskState(true).then(res => {})
            }, 90000)
          }
        } else {
          Dag.create()
        }
      },
      /**
       * copy name
       */
      _copyName () {
        let clipboard = new Clipboard('.copy-name')
        clipboard.on('success', e => {
          this.$message.success(`${i18n.$t('Copy success')}`)
          // Free memory
          clipboard.destroy()
        })
        clipboard.on('error', e => {
          // Copy is not supported
          this.$message.warning(`${i18n.$t('The browser does not support automatic copying')}`)
          // Free memory
          clipboard.destroy()
        })
      },
      /**
       * Get state interface
       * @param isReset Whether to manually refresh
       */
      _getTaskState (isReset) {
        return new Promise((resolve, reject) => {
          this.getTaskState(this.urlParam.id).then(res => {
            let data = res.list
            let state = res.processInstanceState
            let taskList = res.taskList
            let idArr = allNodesId()
            const titleTpl = (item, desc) => {
              let $item = _.filter(taskList, v => v.name === item.name)[0]
              return `<div style="text-align: left">${i18n.$t('Name')}：${$item.name}</br>${i18n.$t('State')}：${desc}</br>${i18n.$t('type')}：${$item.taskType}</br>${i18n.$t('host')}：${$item.host || '-'}</br>${i18n.$t('Retry Count')}：${$item.retryTimes}</br>${i18n.$t('Submit Time')}：${formatDate($item.submitTime)}</br>${i18n.$t('Start Time')}：${formatDate($item.startTime)}</br>${i18n.$t('End Time')}：${$item.endTime ? formatDate($item.endTime) : '-'}</br></div>`
            }

            // remove tip state dom
            $('.w').find('.state-p').html('')
            const newTask = []
            data.forEach(v1 => {
              idArr.forEach(v2 => {
                if (v2.name === v1.name) {
                  let dom = $(`#${v2.id}`)
                  let state = dom.find('.state-p')
                  let depState = ''
                  taskList.forEach(item => {
                    if (item.name === v1.name) {
                      depState = item.state
                      const params = item.taskParams ? JSON.parse(item.taskParams) : ''
                      let localParam = params.localParams || []
                      newTask.push({
                        id: v2.id,
                        localParam
                      })
                    }
                  })
                  dom.attr('data-state-id', v1.stateId)
                  dom.attr('data-dependent-result', v1.dependentResult || '')
                  dom.attr('data-dependent-depState', depState)
                  state.append(`<strong class="${v1.icoUnicode} ${v1.isSpin ? 'as as-spin' : ''}" style="color:${v1.color}" data-toggle="tooltip" data-html="true" data-container="body"></strong>`)
                  state.find('strong').attr('title', titleTpl(v2, v1.desc))
                }
              })
            })
            if (state === 'PAUSE' || state === 'STOP' || state === 'FAILURE' || this.state === 'SUCCESS') {
              // Manual refresh does not regain large json
              if (isReset) {
                findComponentDownward(this.$root, `${this.type}-details`)._reset()
              }
            }
            if (!isReset) {
              this.resetLocalParam(newTask)
            }
            resolve()
          })
        })
      },
      /**
       * Get the action bar id
       * @param item
       */
      _getDagId (v) {
        // if (this.isDetails) {
        //   return
        // }
        this.dagBarId = v
      },
      /**
       * operating
       */
      _ckOperation (item) {
        let is = true
        let code = ''

        if (item.disable) {
          return
        }

        if (this.toolOperCode === item.code) {
          this.toolOperCode = ''
          code = item.code
          is = false
        } else {
          this.toolOperCode = item.code
          code = this.toolOperCode
          is = true
        }

        // event type
        Dag.toolbarEvent({
          item: item,
          code: code,
          is: is
        })
      },
      _operationClass (item) {
        return this.toolOperCode === item.code ? 'active' : ''
        // if (item.disable) {
        //   return this.toolOperCode === item.code ? 'active' : ''
        // } else {
        //   return 'disable'
        // }
      },
      /**
       * Storage interface
       */
      _save (sourceType) {
        return new Promise((resolve, reject) => {
          this.spinnerLoading = true
          // Storage store
          Dag.saveStore().then(res => {
            if (this._verifConditions(res.tasks)) {
              if (this.urlParam.id) {
                /**
                 * Edit
                 * @param saveInstanceEditDAGChart => Process instance editing
                 * @param saveEditDAGChart => Process definition editing
                 */
                this[this.type === 'instance' ? 'updateInstance' : 'updateDefinition'](this.urlParam.id).then(res => {
                  // this.$message.success(res.msg)
                  this.$message({
                    message: res.msg,
                    type: 'success',
                    offset: 80
                  })
                  this.spinnerLoading = false
                  // Jump process definition
                  if (this.type === 'instance') {
                    this.$router.push({ path: `/projects/${this.projectId}/instance/list/${this.urlParam.id}` })
                  } else {
                    this.$router.push({ path: `/projects/${this.projectId}/definition/list/${this.urlParam.id}` })
                  }
                  resolve()
                }).catch(e => {
                  this.$message.error(e.msg || '')
                  this.spinnerLoading = false
                  reject(e)
                })
              } else {
                // New
                this.saveDAGchart().then(res => {
                  this.$message.success(res.msg)
                  this.spinnerLoading = false
                  // source @/conf/home/pages/dag/_source/editAffirmModel/index.js
                  if (sourceType !== 'affirm') {
                    // Jump process definition
                    this.$router.push({ name: 'projects-definition-list' })
                  }
                  resolve()
                }).catch(e => {
                  this.$message.error(e.msg || '')
                  this.setName('')
                  this.spinnerLoading = false
                  reject(e)
                })
              }
            }
          })
        })
      },
      _closeDAG () {
        let $name = this.$route.name
        if ($name && $name.indexOf('definition') !== -1) {
          this.$router.push({ name: 'projects-definition-list' })
        } else {
          this.$router.push({ name: 'projects-instance-list' })
        }
      },
      _verifConditions (value) {
        let locations = this.store.state.dag.locations

        let locationsArr = Object.keys(locations).map(key => {
          return locations[key]
        })

        let tasks = value
        let bool = true
        let bool2 = false
        tasks.map(v => {
          if (v.type === 'CONDITIONS' && (v.conditionResult.successNode[0] === '' || v.conditionResult.successNode[0] === null || v.conditionResult.failedNode[0] === '' || v.conditionResult.failedNode[0] === null)) {
            bool = false
            return false
          }

          locationsArr.map(item => {
            if (v.type === 'CONDITIONS') {
              let successNode = v.conditionResult.successNode[0]
              if (successNode === item.name) {
                bool2 = true
              }

              let failNode = v.conditionResult.failedNode[0]
              if (failNode === item.name) {
                bool2 = true
              }
            }
          })
        })

        if (!bool) {
          this.$message.warning(`${i18n.$t('Successful branch flow and failed branch flow are required')}`)
          this.spinnerLoading = false
          return false
        }

        if (!bool2) {
          this.$message.warning(`${i18n.$t('Error branch')}`)
          this.spinnerLoading = false
          return false
        }
        return true
      },
      onUdpDialog () {
        this._save()
        this.dialogVisible = false
      },
      closeDialog () {
        this.dialogVisible = false
      },
      /**
       * Save chart
       */
      _saveChart () {
        // Verify node
        if (!this.tasks.length) {
          this.$message.warning(`${i18n.$t('Failed to create node to save')}`)
          return
        }

        this.dialogVisible = true
        this.$nextTick(() => {
          this.$refs.mUdp.reloadParam()
        })
      },
      /**
       * Return to the previous child node
       */
      _rtNodesDag () {
        let getIds = this.$route.query.subProcessIds
        let idsArr = getIds.split(',')
        let ids = idsArr.slice(0, idsArr.length - 1)
        let id = idsArr[idsArr.length - 1]
        let query = {}

        if (id !== idsArr[0]) {
          query = { subProcessIds: ids.join(',') }
        }
        let $name = this.$route.name.split('-')
        this.$router.push({ path: `/${$name[0]}/${$name[1]}/list/${id}`, query: query })
      },
      /**
       * Subprocess processing
       * @param subProcessId Subprocess ID
       */
      _subProcessHandle (subProcessId) {
        let subProcessIds = []
        let getIds = this.$route.query.subProcessIds
        if (getIds) {
          let newId = getIds.split(',')
          newId.push(this.urlParam.id)
          subProcessIds = newId
        } else {
          subProcessIds.push(this.urlParam.id)
        }
        let $name = this.$route.name.split('-')
        this.$router.push({ path: `/${$name[0]}/${$name[1]}/list/${subProcessId}`, query: { subProcessIds: subProcessIds.join(',') } })
      },
      /**
       * Refresh data
       */
      _refresh () {
        this.isRefresh = true
        this._getTaskState(false).then(res => {
          setTimeout(() => {
            this.isRefresh = false
            this.$message.success(`${i18n.$t('Refresh status succeeded')}`)
          }, 2200)
        })
      },
      /**
       * View variables
       */
      _toggleView () {
        findComponentDownward(this.$root, 'assist-dag-index')._toggleView()
      },

      /**
       * Starting parameters
       */
      _toggleParam () {
        findComponentDownward(this.$root, 'starting-params-dag-index')._toggleParam()
      },
      addLineInfo ({ item, fromThis }) {
        this.addConnects(item)
        this.lineDrawer = false
      },
      cancel ({ fromThis }) {
        this.lineDrawer = false
      },

      /**
       * Create a node popup layer
       * @param Object id
       */
      _createLineLabel ({ id, sourceId, targetId }) {
        this.lineData.id = id
        this.lineData.sourceId = sourceId
        this.lineData.targetId = targetId
        this.lineDrawer = true
      },

      seeHistory (taskName) {
        this.nodeData.self.$router.push({
          name: 'task-instance',
          query: {
            processInstanceId: this.nodeData.self.$route.params.id,
            taskName: taskName
          }
        })
      },

      addTaskInfo ({ item, fromThis }) {
        this.addTasks(item)
        this.nodeDrawer = false
      },

      cacheTaskInfo ({ item, fromThis }) {
        this.cacheTasks(item)
      },

      close ({ item, flag, fromThis }) {
        this.addTasks(item)
        // Edit status does not allow deletion of nodes
        if (flag) {
          jsPlumb.remove(this.nodeData.id)
        }
        this.nodeDrawer = false
      },
      onSubProcess ({ subProcessId, fromThis }) {
        this._subProcessHandle(subProcessId)
      },

      _createNodes ({ id, type }) {
        let self = this
        let preNode = []
        let rearNode = []
        let rearList = []
        $('div[data-targetarr*="' + id + '"]').each(function () {
          rearNode.push($(this).attr('id'))
        })

        if (rearNode.length > 0) {
          rearNode.forEach(v => {
            let rearobj = {}
            rearobj.value = $(`#${v}`).find('.name-p').text()
            rearobj.label = $(`#${v}`).find('.name-p').text()
            rearList.push(rearobj)
          })
        } else {
          rearList = []
        }
        let targetarr = $(`#${id}`).attr('data-targetarr')
        if (targetarr) {
          let nodearr = targetarr.split(',')
          nodearr.forEach(v => {
            let nodeobj = {}
            nodeobj.value = $(`#${v}`).find('.name-p').text()
            nodeobj.label = $(`#${v}`).find('.name-p').text()
            preNode.push(nodeobj)
          })
        } else {
          preNode = []
        }

        this.taskId = id
        type = type || self.dagBarId

        this.nodeData.id = id
        this.nodeData.taskType = type
        this.nodeData.self = self
        this.nodeData.preNode = preNode
        this.nodeData.rearList = rearList
        this.nodeData.instanceId = this.$route.params.id

        this.nodeDrawer = true
      },
      removeEventModelById ($id) {
        if (eventModel && this.taskId === $id) {
          eventModel.remove()
        }
      },

      /**
        * switch version in process definition version list
        *
        * @param version the version user want to change
        * @param processDefinitionId the process definition id
        * @param fromThis fromThis
      */
      mVersionSwitchProcessDefinitionVersion ({ version, processDefinitionId, fromThis }) {
        this.$store.state.dag.isSwitchVersion = true
        this.switchProcessDefinitionVersion({
          version: version,
          processDefinitionId: processDefinitionId
        }).then(res => {
          this.$message.success($t('Switch Version Successfully'))
          this.$router.push({ path: `/projects/${this.projectId}/definition/list/${processDefinitionId}` })
        }).catch(e => {
          this.$store.state.dag.isSwitchVersion = false
          this.$message.error(e.msg || '')
        })
      },

      /**
        * Paging event of process definition versions
        *
        * @param pageNo page number
        * @param pageSize page size
        * @param processDefinitionId the process definition id of page version
        * @param fromThis fromThis
      */
      mVersionGetProcessDefinitionVersionsPage ({ pageNo, pageSize, processDefinitionCode, fromThis }) {
        this.getProcessDefinitionVersionsPage({
          pageNo: pageNo,
          pageSize: pageSize,
          processDefinitionCode: processDefinitionCode
        }).then(res => {
          this.versionData.processDefinitionVersions = res.data.lists
          this.versionData.total = res.data.totalCount
          this.versionData.pageSize = res.data.pageSize
          this.versionData.pageNo = res.data.currentPage
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },

      /**
       * delete one version of process definition
       *
       * @param version the version need to delete
       * @param processDefinitionId the process definition id user want to delete
       * @param fromThis fromThis
       */
      mVersionDeleteProcessDefinitionVersion ({ version, processDefinitionId, processDefinitionCode, fromThis }) {
        this.deleteProcessDefinitionVersion({
          version: version,
          processDefinitionId: processDefinitionId
        }).then(res => {
          this.$message.success(res.msg || '')
          this.mVersionGetProcessDefinitionVersionsPage({
            pageNo: 1,
            pageSize: 10,
            processDefinitionCode: processDefinitionCode,
            fromThis: fromThis
          })
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      /**
       * query the process definition pagination version
       */
      _version (item) {
        this.getProcessDefinitionVersionsPage({
          pageNo: 1,
          pageSize: 10,
          processDefinitionCode: this.store.state.dag.code
        }).then(res => {
          let processDefinitionVersions = res.data.lists
          let total = res.data.totalCount
          let pageSize = res.data.pageSize
          let pageNo = res.data.currentPage
          this.versionData.processDefinition.id = this.urlParam.id
          this.versionData.processDefinition.code = this.store.state.dag.code
          this.versionData.processDefinition.version = this.$store.state.dag.version
          this.versionData.processDefinition.releaseState = this.releaseState
          this.versionData.processDefinitionVersions = processDefinitionVersions
          this.versionData.total = total
          this.versionData.pageNo = pageNo
          this.versionData.pageSize = pageSize
          this.drawer = true
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },

      closeVersion () {
        this.drawer = false
      }
    },
    watch: {
      tasks: {
        deep: true,
        handler (o) {
          // Edit state does not allow deletion of node a...
          this.setIsEditDag(true)
        }
      }
    },
    created () {
      // Edit state does not allow deletion of node a...
      this.setIsEditDag(false)

      if (this.$route.query.subProcessIds) {
        this.isRtTasks = true
      }

      Dag.init({
        dag: this,
        instance: jsPlumb.getInstance({
          Endpoint: [
            'Dot', { radius: 1, cssClass: 'dot-style' }
          ],
          Connector: 'Bezier',
          PaintStyle: { lineWidth: 2, stroke: '#456' }, // Connection style
          ConnectionOverlays: [
            [
              'Arrow',
              {
                location: 1,
                id: 'arrow',
                length: 12,
                foldback: 0.8
              }
            ],
            ['Label', {
              location: 0.5,
              id: 'label'
            }]
          ],
          Container: 'canvas',
          ConnectionsDetachable: true
        })
      })
    },
    mounted () {
      this.init(this.arg)
    },
    beforeDestroy () {
      this.resetParams()

      // Destroy round robin
      clearInterval(this.setIntervalP)
    },
    destroyed () {
      if (eventModel) {
        eventModel.remove()
      }
    },
    computed: {
      ...mapState('dag', ['tasks', 'locations', 'connects', 'isEditDag', 'name', 'projectId'])
    },
    components: { mVersions, mFormModel, mFormLineModel, mUdp, mStart }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  @import "./dag";
  .operBtn {
    padding: 8px 6px;
  }
</style>
