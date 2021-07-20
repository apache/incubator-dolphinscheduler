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
  <div class="datasource-model">
    <div class="select-listpp">
      <x-select v-model="type"
                style="width: 160px;"
                @on-change="_handleTypeChanged"
                :disabled="isDetails">
        <x-option
                v-for="city in typeList"
                :key="city.code"
                :value="city.code"
                :label="city.code">
        </x-option>
      </x-select>
      <x-select :placeholder="$t('Please select the datasource')"
                v-model="datasource"
                filterable
                style="width: 288px;"
                :disabled="isDetails">
        <x-option
                v-for="city in datasourceList"
                :key="city.id"
                :value="city.id"
                :label="city.code">
        </x-option>
      </x-select>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'datasource',
    data () {
      return {
        // Data source type
        type: '',
        // Data source type(List)
        typeList: [],
        // data source
        datasource: '',
        // data source(List)
        datasourceList: []
      }
    },
    mixins: [disabledState],
    props: {
      data: Object,
      supportType: Array
    },
    methods: {
      /**
       * Verify data source
       */
      _verifDatasource () {
        if (!this.datasource) {
          this.$message.warning(`${i18n.$t('Please select the datasource')}`)
          return false
        }
        this.$emit('on-dsData', {
          type: this.type,
          datasource: this.datasource
        })
        return true
      },
      /**
       * Get the corresponding datasource data according to type
       */
      _getDatasourceData () {
        return new Promise((resolve, reject) => {
          this.store.dispatch('dag/getDatasourceList', this.type).then(res => {
            this.datasourceList = _.map(res.data, v => {
              return {
                id: v.id,
                code: v.name,
                disabled: false
              }
            })
            resolve()
          })
        })
      },
      /**
       * Brush type
       */
      _handleTypeChanged ({ value }) {
        this.type = value
        this._getDatasourceData().then(res => {
          this.datasource = this.datasourceList.length && this.datasourceList[0].id || ''
          this.$emit('on-dsData', {
            type: this.type,
            datasource: this.datasource
          })
        })
      }
    },
    computed: {
      cacheParams () {
        return {
          type: this.type,
          datasource: this.datasource
        }
      }
    },
    // Watch the cacheParams
    watch: {
      datasource (val) {
        this.$emit('on-dsData', {
          type: this.type,
          datasource: val
        });
      }
    },
    created () {
      let supportType = this.supportType || []
      this.typeList = this.data.typeList || _.cloneDeep(this.store.state.dag.dsTypeListS)
      // Have a specified data source
      if (supportType.length) {
        let is = (type) => {
          return !!_.filter(supportType, v => v === type).length
        }
        this.typeList = _.filter(this.typeList, v => is(v.code))
      }

      this.type = _.cloneDeep(this.data.type) || this.typeList[0].code
      // init data
      this._getDatasourceData().then(res => {
        if (_.isEmpty(this.data)) {
          this.$nextTick(() => {
            this.datasource = this.datasourceList[0].id
          })
        } else {
          this.$nextTick(() => {
            this.datasource = this.data.datasource
          })
        }
        this.$emit('on-dsData', {
          type: this.type,
          datasource: this.datasource
        })
      })
    },
    mounted () {

    },
    components: { }
  }
</script>
