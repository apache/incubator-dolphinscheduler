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
 * State code table
 */
const stateType = [
  {
    code: '',
    label: `${i18n.$t('none')}`
  }, {
    code: 'SUBMITTED_SUCCESS',
    label: `${i18n.$t('Submitted successfully')}`
  }, {
    code: 'RUNNING_EXECUTION',
    label: `${i18n.$t('Running')}`
  }, {
    code: 'READY_PAUSE',
    label: `${i18n.$t('Ready to pause')}`
  }, {
    code: 'PAUSE',
    label: `${i18n.$t('Pause')}`
  }, {
    code: 'READY_STOP',
    label: `${i18n.$t('Ready to stop')}`
  }, {
    code: 'STOP',
    label: `${i18n.$t('Stop')}`
  }, {
    code: 'FAILURE',
    label: `${i18n.$t('failed')}`
  }, {
    code: 'SUCCESS',
    label: `${i18n.$t('success')}`
  }, {
    code: 'NEED_FAULT_TOLERANCE',
    label: `${i18n.$t('Need fault tolerance')}`
  }, {
    code: 'KILL',
    label: `${i18n.$t('kill')}`
  }, {
    code: 'WAITING_THREAD',
    label: `${i18n.$t('Waiting for thread')}`
  }, {
    code: 'WAITING_DEPEND',
    label: `${i18n.$t('Waiting for dependency to complete')}`
  }, {
    code: 'DELAY_EXECUTION',
    label: `${i18n.$t('Delay execution')}`
  }, {
    code: 'FORCED_SUCCESS',
    label: `${i18n.$t('Forced success')}`
  }
]

export {
  stateType
}
