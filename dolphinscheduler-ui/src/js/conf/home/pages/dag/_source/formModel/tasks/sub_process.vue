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
  <div class="sub_process-model">
    <m-list-box>
      <div slot="text">{{$t('Child Node')}}</div>
      <div slot="content">
        <el-select
                style="width: 100%;"
                size="small"
                filterable
                v-model="wdiCurr"
                :disabled="isDetails"
                @change="_handleWdiChanged">
          <el-option
                v-for="city in processDefinitionList"
                :key="city.code"
                :value="city.id"
                :label="city.code">
          </el-option>
        </el-select>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Custom Parameters')}}</div>
      <div slot="content">
        <m-local-params
                ref="refLocalParams"
                @on-local-params="_onLocalParams"
                :udp-list="localParams"
                :hide="true">
        </m-local-params>
      </div>
    </m-list-box>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import disabledState from '@/module/mixin/disabledState'
  import mListBox from './_source/listBox'
  import mLocalParams from './_source/localParams'

  export default {
    name: 'sub_process',
    data () {
      return {
        // Process definition(List)
        processDefinitionList: [],
        // Process definition
        wdiCurr: null,
        // Custom parameter
        localParams: []
      }
    },
    mixins: [disabledState],
    props: {
      backfillItem: Object
    },
    computed: {
      cacheParams () {
        return {
          processDefinitionId: this.wdiCurr,
          localParams: this.localParams
        }
      }
    },
    methods: {
      _onLocalParams (a) {
        this.localParams = a
      },
      /**
       * Node unified authentication parameters
       */
      _verification () {
        if (!this.wdiCurr) {
          this.$message.warning(`${i18n.$t('Please select a sub-Process')}`)
          return false
        }
        // localParams Subcomponent verification
        if (!this.$refs.refLocalParams._verifProp()) {
          return false
        }
        this.$emit('on-params', {
          processDefinitionId: this.wdiCurr,
          localParams: this.localParams
        })
        return true
      },
      /**
       * The selected process defines the upper component name padding
       */
      _handleWdiChanged (o) {
        this.$emit('on-set-process-name', this._handleName(o))
      },
      /**
       * Return the name according to the process definition id
       */
      _handleName (id) {
        return _.filter(this.processDefinitionList, v => id === v.id)[0].code
      }
    },
    watch: {
      cacheParams (val) {
        this.$emit('on-cache-params', val)
      }
    },
    created () {
      let processListS = _.cloneDeep(this.store.state.dag.processListS)
      let id = null
      if (this.router.history.current.name === 'projects-instance-details') {
        id = this.router.history.current.query.id || null
      } else {
        id = this.router.history.current.params.id || null
      }
      this.processDefinitionList = (() => {
        let a = _.map(processListS, v => {
          return {
            id: v.id,
            code: v.name,
            disabled: false
          }
        })
        return _.filter(a, v => +v.id !== +id)
      })()

      let o = this.backfillItem
      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        this.wdiCurr = o.params.processDefinitionId
        // backfill localParams
        let localParams = o.params.localParams || []
        if (localParams.length) {
          this.localParams = localParams
        }
      } else {
        if (this.processDefinitionList.length) {
          this.wdiCurr = this.processDefinitionList[0].id
          this.$emit('on-set-process-name', this._handleName(this.wdiCurr))
        }
      }
    },
    mounted () {
    },
    components: { mLocalParams, mListBox }
  }
</script>
