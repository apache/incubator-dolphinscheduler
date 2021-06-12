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

import i18n from '@/module/i18n'

/**
 * Operation bar config
 * @code code
 * @icon icon
 * @disable disable
 * @desc tooltip
 */
const toolOper = (dagThis) => {
  const disabled = !!dagThis.$store.state.dag.isDetails// Permissions.getAuth() === false ? false : !dagThis.$store.state.dag.isDetails
  return [
    {
      code: 'pointer',
      icon: 'el-icon-thumb',
      disable: disabled,
      desc: `${i18n.$t('Drag Nodes and Selected Items')}`
    },
    {
      code: 'line',
      icon: 'el-icon-top-right',
      disable: disabled,
      desc: `${i18n.$t('Select Line Connection')}`
    },
    {
      code: 'remove',
      icon: 'el-icon-delete',
      disable: disabled,
      desc: `${i18n.$t('Delete selected lines or nodes')}`
    },
    {
      code: 'download',
      icon: 'el-icon-download',
      disable: !dagThis.type,
      desc: `${i18n.$t('Download')}`
    },
    {
      code: 'screen',
      icon: 'el-icon-full-screen',
      disable: false,
      desc: `${i18n.$t('Full Screen')}`
    }
  ]
}

/**
 * Post status
 * @id Front end definition id
 * @desc tooltip
 * @code Backend definition identifier
 */
const publishStatus = [
  {
    id: 0,
    desc: `${i18n.$t('Unpublished')}`,
    code: 'NOT_RELEASE'
  },
  {
    id: 1,
    desc: `${i18n.$t('online')}`,
    code: 'ONLINE'
  },
  {
    id: 2,
    desc: `${i18n.$t('offline')}`,
    code: 'OFFLINE'
  }
]

/**
 * Operation type
 * @desc tooltip
 * @code identifier
 */
const runningType = [
  {
    desc: `${i18n.$t('Start Process')}`,
    code: 'START_PROCESS'
  },
  {
    desc: `${i18n.$t('Execute from the current node')}`,
    code: 'START_CURRENT_TASK_PROCESS'
  },
  {
    desc: `${i18n.$t('Recover tolerance fault process')}`,
    code: 'RECOVER_TOLERANCE_FAULT_PROCESS'
  },
  {
    desc: `${i18n.$t('Resume the suspension process')}`,
    code: 'RECOVER_SUSPENDED_PROCESS'
  },
  {
    desc: `${i18n.$t('Execute from the failed nodes')}`,
    code: 'START_FAILURE_TASK_PROCESS'
  },
  {
    desc: `${i18n.$t('Complement Data')}`,
    code: 'COMPLEMENT_DATA'
  },
  {
    desc: `${i18n.$t('Scheduling execution')}`,
    code: 'SCHEDULER'
  },
  {
    desc: `${i18n.$t('Rerun')}`,
    code: 'REPEAT_RUNNING'
  },
  {
    desc: `${i18n.$t('Pause')}`,
    code: 'PAUSE'
  },
  {
    desc: `${i18n.$t('Stop')}`,
    code: 'STOP'
  },
  {
    desc: `${i18n.$t('Recovery waiting thread')}`,
    code: 'RECOVER_WAITTING_THREAD'
  },
  {
    desc: `${i18n.$t('Start random task process')}`,
    code: 'START_RANDOM_TASK_PROCESS'
  }
]

/**
 * Task status
 * @key key
 * @id id
 * @desc tooltip
 * @color color
 * @icoUnicode iconfont
 * @isSpin is loading (Need to execute the code block to write if judgment)
 */
const tasksState = {
  SUBMITTED_SUCCESS: {
    id: 0,
    desc: `${i18n.$t('Submitted successfully')}`,
    color: '#A9A9A9',
    icoUnicode: 'ri-record-circle-fill',
    isSpin: false
  },
  RUNNING_EXECUTION: {
    id: 1,
    desc: `${i18n.$t('Executing')}`,
    color: '#0097e0',
    icoUnicode: 'el-icon-s-tools',
    isSpin: true
  },
  READY_PAUSE: {
    id: 2,
    desc: `${i18n.$t('Ready to pause')}`,
    color: '#07b1a3',
    icoUnicode: 'ri-settings-3-line',
    isSpin: false
  },
  PAUSE: {
    id: 3,
    desc: `${i18n.$t('Pause')}`,
    color: '#057c72',
    icoUnicode: 'el-icon-video-pause',
    isSpin: false
  },
  READY_STOP: {
    id: 4,
    desc: `${i18n.$t('Ready to stop')}`,
    color: '#FE0402',
    icoUnicode: 'ri-stop-circle-fill',
    isSpin: false
  },
  STOP: {
    id: 5,
    desc: `${i18n.$t('Stop')}`,
    color: '#e90101',
    icoUnicode: 'ri-stop-circle-line',
    isSpin: false
  },
  FAILURE: {
    id: 6,
    desc: `${i18n.$t('Failed')}`,
    color: '#000000',
    icoUnicode: 'el-icon-circle-close',
    isSpin: false
  },
  SUCCESS: {
    id: 7,
    desc: `${i18n.$t('Success')}`,
    color: '#33cc00',
    icoUnicode: 'el-icon-circle-check',
    isSpin: false
  },
  NEED_FAULT_TOLERANCE: {
    id: 8,
    desc: `${i18n.$t('Need fault tolerance')}`,
    color: '#FF8C00',
    icoUnicode: 'el-icon-edit',
    isSpin: false
  },
  KILL: {
    id: 9,
    desc: `${i18n.$t('Kill')}`,
    color: '#a70202',
    icoUnicode: 'el-icon-remove-outline',
    isSpin: false
  },
  WAITTING_THREAD: {
    id: 10,
    desc: `${i18n.$t('Waiting for thread')}`,
    color: '#912eed',
    icoUnicode: 'ri-time-line',
    isSpin: false
  },
  WAITTING_DEPEND: {
    id: 11,
    desc: `${i18n.$t('Waiting for dependence')}`,
    color: '#5101be',
    icoUnicode: 'ri-send-to-back',
    isSpin: false
  },
  DELAY_EXECUTION: {
    id: 12,
    desc: `${i18n.$t('Delay execution')}`,
    color: '#5102ce',
    icoUnicode: 'ri-pause-circle-fill',
    isSpin: false
  },
  FORCED_SUCCESS: {
    id: 13,
    desc: `${i18n.$t('Forced success')}`,
    color: '#5102ce',
    icoUnicode: 'el-icon-success',
    isSpin: false
  }
}

/**
 * Node type
 * @key key
 * @desc tooltip
 * @color color (tree and gantt)
 */
const tasksType = {
  SHELL: {
    desc: 'SHELL',
    color: '#646464'
  },
  WATERDROP: {
    desc: 'WATERDROP',
    color: '#646465'
  },
  SUB_PROCESS: {
    desc: 'SUB_PROCESS',
    color: '#0097e0'
  },
  PROCEDURE: {
    desc: 'PROCEDURE',
    color: '#525CCD'
  },
  SQL: {
    desc: 'SQL',
    color: '#7A98A1'
  },
  SPARK: {
    desc: 'SPARK',
    color: '#E46F13'
  },
  FLINK: {
    desc: 'FLINK',
    color: '#E46F13'
  },
  MR: {
    desc: 'MapReduce',
    color: '#A0A5CC'
  },
  PYTHON: {
    desc: 'PYTHON',
    color: '#FED52D'
  },
  DEPENDENT: {
    desc: 'DEPENDENT',
    color: '#2FBFD8'
  },
  HTTP: {
    desc: 'HTTP',
    color: '#E46F13'
  },
  DATAX: {
    desc: 'DataX',
    color: '#1fc747'
  },
  SQOOP: {
    desc: 'SQOOP',
    color: '#E46F13'
  },
  CONDITIONS: {
    desc: 'CONDITIONS',
    color: '#E46F13'
  }
}

export {
  toolOper,
  publishStatus,
  runningType,
  tasksState,
  tasksType
}
