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
  <div class="udp-model">
    <div class="scrollbar contpi-boxt">
      <div>
        <el-input
                type="text"
                size="small"
                v-model="name"
                :disabled="router.history.current.name === 'projects-instance-details'"
                :placeholder="$t('Please enter name (required)')">
        </el-input>
      </div>

      <template v-if="router.history.current.name !== 'projects-instance-details'">
        <div style="padding-top: 12px;">
          <el-input
                  type="textarea"
                  size="small"
                  v-model="description"
                  :autosize="{minRows:2}"
                  :placeholder="$t('Please enter description(optional)')"
                  autocomplete="off">
          </el-input>
        </div>
      </template>

      <div class="title" style="padding-top: 6px;">
        <span class="text-b">{{$t('select tenant')}}</span>
        <form-tenant v-model="tenantId"></form-tenant>
      </div>
      <div class="title" style="padding-top: 6px;">
        <span class="text-b">{{$t('warning of timeout')}}</span>
        <span style="padding-left: 6px;">
          <el-switch v-model="checkedTimeout" size="small"></el-switch>
        </span>
      </div>
      <div class="content" style="padding-bottom: 10px;" v-if="checkedTimeout">
        <span>
          <el-input v-model="timeout" style="width: 160px;" maxlength="9" size="small">
            <span slot="append">{{$t('Minute')}}</span>
          </el-input>
        </span>
      </div>

      <div class="title" style="padding-top: 6px;">
        <span>{{$t('Set global')}}</span>
      </div>
      <div class="content">
        <div>
          <m-local-params
                  ref="refLocalParams"
                  @on-local-params="_onLocalParams"
                  :udp-list="udpList"
                  :hide="false">
          </m-local-params>
        </div>
      </div>
    </div>
    <div class="bottom">
      <div class="submit">
        <template v-if="router.history.current.name === 'projects-definition-details'">
          <div class="lint-pt">
            <el-checkbox v-model="releaseState" size="small" :false-label="'OFFLINE'" :true-label="'ONLINE'">{{$t('Whether to go online the process definition')}}</el-checkbox>
          </div>
        </template>
        <template v-if="router.history.current.name === 'projects-instance-details'">
          <div class="lint-pt">
            <el-checkbox v-model="syncDefine" size="small">{{$t('Whether to update the process definition')}}</el-checkbox>
          </div>
        </template>
        <el-button type="text" size="small" @click="close()"> {{$t('Cancel')}} </el-button>
        <el-button type="primary" size="small" round :disabled="isDetails" @click="ok()">{{$t('Add')}}</el-button>
      </div>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import mLocalParams from '../formModel/tasks/_source/localParams'
  import disabledState from '@/module/mixin/disabledState'
  import Affirm from '../jumpAffirm'
  import FormTenant from './_source/selectTenant'

  export default {
    name: 'udp',
    data () {
      return {
        originalName: '',
        // dag name
        name: '',
        // dag description
        description: '',
        // Global custom parameters
        udpList: [],
        // Global custom parameters
        udpListCache: [],
        // Whether to go online the process definition
        releaseState: 'ONLINE',
        // Whether to update the process definition
        syncDefine: true,
        // Timeout alarm
        timeout: 0,

        tenantId: -1,
        // checked Timeout alarm
        checkedTimeout: true
      }
    },
    mixins: [disabledState],
    props: {
    },
    methods: {
      /**
       * udp data
       */
      _onLocalParams (a) {
        this.udpList = a
      },
      _verifTimeout () {
        const reg = /^[1-9]\d*$/
        if (!reg.test(this.timeout)) {
          this.$message.warning(`${i18n.$t('Please enter a positive integer greater than 0')}`)
          return false
        }
        return true
      },
      _accuStore () {
        const udp = _.cloneDeep(this.udpList)
        udp.forEach(u => {
          delete u.ifFixed
        })
        this.store.commit('dag/setGlobalParams', udp)

        this.store.commit('dag/setName', _.cloneDeep(this.name))
        this.store.commit('dag/setTimeout', _.cloneDeep(this.timeout))
        this.store.commit('dag/setTenantId', _.cloneDeep(this.tenantId))
        this.store.commit('dag/setDesc', _.cloneDeep(this.description))
        this.store.commit('dag/setSyncDefine', this.syncDefine)
        this.store.commit('dag/setReleaseState', this.releaseState)
      },
      /**
       * submit
       */
      ok () {
        if (!this.name) {
          this.$message.warning(`${i18n.$t('DAG graph name cannot be empty')}`)
          return
        }

        let _verif = () => {
          // verification udf
          if (!this.$refs.refLocalParams._verifProp()) {
            return
          }
          // verification timeout
          if (this.checkedTimeout && !this._verifTimeout()) {
            return
          }

          // Storage global globalParams
          this._accuStore()

          Affirm.setIsPop(false)
          this.$emit('onUdp')
        }

        if (this.originalName !== this.name) {
          this.store.dispatch('dag/verifDAGName', this.name).then(res => {
            _verif()
          }).catch(e => {
            this.$message.error(e.msg || '')
          })
        } else {
          _verif()
        }
      },
      /**
       * Close the popup
       */
      close () {
        this.$emit('close')
      },
      /**
       * reload localParam
       */
      reloadParam () {
        const dag = _.cloneDeep(this.store.state.dag)
        let globalParams = _.cloneDeep(dag.globalParams)
        let udpList = [...globalParams]
        this.udpList = udpList
        this.udpListCache = udpList
      }
    },
    watch: {
      checkedTimeout (val) {
        if (!val) {
          this.timeout = 0
          this.store.commit('dag/setTimeout', _.cloneDeep(this.timeout))
        }
      }
    },
    created () {
      const dag = _.cloneDeep(this.store.state.dag)

      this.name = dag.name
      this.originalName = dag.name
      this.description = dag.description
      this.syncDefine = dag.syncDefine
      this.releaseState = dag.releaseState
      this.timeout = dag.timeout || 0
      this.checkedTimeout = this.timeout !== 0
      this.$nextTick(() => {
        if (dag.tenantId > -1) {
          this.tenantId = dag.tenantId
        } else if (this.store.state.user.userInfo.tenantId) {
          this.tenantId = this.store.state.user.userInfo.tenantId
        }
      })
    },
    mounted () {},
    components: { FormTenant, mLocalParams }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .udp-model {
    width: 624px;
    min-height: 420px;
    background: #fff;
    border-radius: 3px;
    padding:20px 0 ;
    position: relative;
    .contpi-boxt {
      max-height: 600px;
      overflow-y: scroll;
      padding:0 20px;

      ::selection {
        background: #409EFF ;
        color: white;
      }
      ::-moz-selection {
        background: #409EFF;
        color: white;
      }
      ::-webkit-selection {
        background: #409EFF;
        color: white;
      }
    }
    .title {
      line-height: 36px;
      padding-bottom: 10px;
      span {
        font-size: 16px;
        color: #333;
      }
    }
    .bottom{
      position: absolute;
      bottom: 0;
      left: 0;
      width: 100%;
      text-align: right;
      height: 56px;
      line-height: 56px;
      border-top: 1px solid #DCDEDC;
      background: #fff;
      .submit {
        padding-right: 20px;
        margin-top: -4px;
      }
      .lint-pt {
        position: absolute;
        left: 20px;
        top: -2px;
        >label {
          font-weight: normal;
        }
      }
    }
    .content {
      padding-bottom: 50px;
      .user-def-params-model {
        .add {
          a {
            color: #0097e0;
          }
        }
      }
    }

  }
</style>
