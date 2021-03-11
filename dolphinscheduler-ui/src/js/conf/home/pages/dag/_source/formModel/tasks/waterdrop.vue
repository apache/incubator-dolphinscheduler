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
  <div class="waterdrop-model">
    <!--deploy mode-->
    <m-list-box>
      <div slot="text">{{$t('Deploy Mode')}}</div>
      <div slot="content">
        <el-radio-group size="small" v-model="deployMode">
          <el-radio :label="'client'" :disabled="isDetails"></el-radio>
          <el-radio :label="'cluster'" :disabled="isDetails"></el-radio>
          <el-radio :label="'local'" :disabled="isDetails"></el-radio>
        </el-radio-group>
      </div>
    </m-list-box>
    <!--master-->
    <m-list-box v-if="deployMode !== 'local'">
      <div slot="text">{{$t('Master')}}</div>
      <div slot="content" class="display-flex">
        <el-select
          size="small"
          v-model="master"
          :disabled="isDetails">
          <el-option
            v-for="city in masterType"
            :key="city.code"
            :value="city.code"
            :label="city.code">
          </el-option>
        </el-select>
        <el-input
          :disabled="isDetails"
          type="input"
          size="small"
          v-model="masterUrl"
          :placeholder="$t('Please Enter Url')"
          class="cont-extra"
          v-if="masterUrlState">
        </el-input>
      </div>
    </m-list-box>
    <!--queue-->
    <m-list-box v-if="deployMode !== 'local' && master === 'yarn'">
      <div slot="text">{{$t('Queue')}}</div>
      <div slot="content">
        <el-input
          :disabled="isDetails"
          type="input"
          size="small"
          v-model="queue"
          :placeholder="$t('Please enter queue value')"
          style="width: 192px;">
        </el-input>
      </div>
    </m-list-box>
    <!--config file-->
    <m-list-box>
      <div slot="text">{{$t('Resources')}}</div>
      <div slot="content">
        <treeselect  v-model="resourceList" maxHeight="200" :disable-branch-nodes="true" :multiple="true" :options="options" :normalizer="normalizer" :disabled="isDetails" :value-consists-of="valueConsistsOf" :placeholder="$t('Please select resources')">
          <div slot="value-label" slot-scope="{ node }">{{ node.raw.fullName }}</div>
        </treeselect>
      </div>
    </m-list-box>
    <!--custom parameters-->
    <m-list-box>
      <div slot="text">{{$t('Custom Parameters')}}</div>
      <div slot="content">
        <m-local-params
          ref="refLocalParams"
          @on-local-params="_onLocalParams"
          :udp-list="localParams"
          :hide="false">
        </m-local-params>
      </div>
    </m-list-box>
  </div>
</template>

<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import mListBox from './_source/listBox'
  import mLocalParams from './_source/localParams'
  import disabledState from '@/module/mixin/disabledState'
  import Treeselect from '@riophae/vue-treeselect'
  import '@riophae/vue-treeselect/dist/vue-treeselect.css'

  export default {
    name: 'waterdrop',
    data () {
      return {
        valueConsistsOf: 'LEAF_PRIORITY',
        // script
        rawScript: '',
        // waterdrop script
        baseScript: 'sh ${WATERDROP_HOME}/bin/start-waterdrop.sh', // eslint-disable-line
        // resourceNameVal
        resourceNameVal: [],
        // Custom parameter
        localParams: [],
        // resource(list)
        resourceList: [],
        // Deployment method
        deployMode: 'client',
        // Deployment master
        queue: 'default',
        // Deployment master
        master: 'yarn',
        // Spark version(LIst)
        masterType: [{ code: 'yarn' }, { code: 'local' }, { code: 'spark://' }, { code: 'mesos://' }],
        // Deployment masterUrl state
        masterUrlState: false,
        // Deployment masterUrl
        masterUrl: '',
        // Cache ResourceList
        cacheResourceList: [],
        // define options
        options: [],
        normalizer (node) {
          return {
            label: node.name
          }
        },
        allNoResources: [],
        noRes: []
      }
    },
    mixins: [disabledState],
    props: {
      backfillItem: Object
    },
    methods: {
      /**
       * return localParams
       */
      _onLocalParams (a) {
        this.localParams = a
      },
      /**
       * return resourceList
       *
       */
      _onResourcesData (a) {
        this.resourceList = a
      },
      /**
       * cache resourceList
       */
      _onCacheResourcesData (a) {
        this.cacheResourceList = a
      },
      /**
       * verification
       */
      _verification () {
        // localParams Subcomponent verification
        if (!this.$refs.refLocalParams._verifProp()) {
          return false
        }
        // noRes
        if (this.noRes.length > 0) {
          this.$message.warning(`${i18n.$t('Please delete all non-existent resources')}`)
          return false
        }
        // noRes
        if (!this.resourceNameVal.resourceList) {
          this.$message.warning(`${i18n.$t('Please select the waterdrop resources')}`)
          return false
        }
        if (this.resourceNameVal.resourceList && this.resourceNameVal.resourceList.length === 0) {
          this.$message.warning(`${i18n.$t('Please select the waterdrop resources')}`)
          return false
        }
        // Process resourcelist
        let dataProcessing = _.map(this.resourceList, v => {
          return {
            id: v
          }
        })
        // verify deploy mode
        let deployMode = this.deployMode
        let master = this.master
        let masterUrl = this.masterUrl

        if (this.deployMode === 'local') {
          master = 'local'
          masterUrl = ''
          deployMode = 'client'
        }
        // get local params
        let locparams = ''
        this.localParams.forEach(v => {
          locparams = locparams + ' --variable ' + v.prop + '=' + v.value
        }
        )
        // get waterdrop script
        let tureScript = ''
        this.resourceNameVal.resourceList.forEach(v => {
          tureScript = tureScript + this.baseScript +
            ' --master ' + master + masterUrl +
            ' --deploy-mode ' + deployMode +
            ' --queue ' + this.queue +
            ' --config ' + v.res +
            locparams + ' \n'
        })

        // storage
        this.$emit('on-params', {
          resourceList: dataProcessing,
          localParams: this.localParams,
          rawScript: tureScript
        })

        return true
      },
      diGuiTree (item) { // Recursive convenience tree structure
        item.forEach(item => {
          item.children === '' || item.children === undefined || item.children === null || item.children.length === 0
            ? this.operationTree(item) : this.diGuiTree(item.children)
        })
      },
      operationTree (item) {
        if (item.directory) {
          item.isDisabled = true
        }
        delete item.children
      },
      searchTree (element, id) {
        // 根据id查找节点
        if (element.id === id) {
          return element
        } else if (element.children !== null) {
          let i
          let result = null
          for (i = 0; result === null && i < element.children.length; i++) {
            result = this.searchTree(element.children[i], id)
          }
          return result
        }
        return null
      },
      dataProcess (backResource) {
        let isResourceId = []
        let resourceIdArr = []
        if (this.resourceList.length > 0) {
          this.resourceList.forEach(v => {
            this.options.forEach(v1 => {
              if (this.searchTree(v1, v)) {
                isResourceId.push(this.searchTree(v1, v))
              }
            })
          })
          resourceIdArr = isResourceId.map(item => {
            return item.id
          })
          Array.prototype.diff = function (a) {
            return this.filter(function (i) { return a.indexOf(i) < 0 })
          }
          let diffSet = this.resourceList.diff(resourceIdArr)
          let optionsCmp = []
          if (diffSet.length > 0) {
            diffSet.forEach(item => {
              backResource.forEach(item1 => {
                if (item === item1.id || item === item1.res) {
                  optionsCmp.push(item1)
                }
              })
            })
          }
          let noResources = [{
            id: -1,
            name: $t('Unauthorized or deleted resources'),
            fullName: '/' + $t('Unauthorized or deleted resources'),
            children: []
          }]
          if (optionsCmp.length > 0) {
            this.allNoResources = optionsCmp
            optionsCmp = optionsCmp.map(item => {
              return { id: item.id, name: item.name, fullName: item.res }
            })
            optionsCmp.forEach(item => {
              item.isNew = true
            })
            noResources[0].children = optionsCmp
            this.options = this.options.concat(noResources)
          }
        }
      }
    },
    watch: {
      // Watch the cacheParams
      cacheParams (val) {
        this.resourceNameVal = val
        this.$emit('on-cache-params', val)
      },
      resourceIdArr (arr) {
        let result = []
        arr.forEach(item => {
          this.allNoResources.forEach(item1 => {
            if (item.id === item1.id) {
              // resultBool = true
              result.push(item1)
            }
          })
        })
        this.noRes = result
      },
      master: {
        handler (code) {
          if (code === 'spark://') {
            this.masterUrlState = true
          } else if (code === 'mesos://') {
            this.masterUrlState = true
          } else {
            this.masterUrlState = false
            this.masterUrl = ''
          }
        }
      }
    },
    computed: {
      resourceIdArr () {
        let isResourceId = []
        let resourceIdArr = []
        if (this.resourceList.length > 0) {
          this.resourceList.forEach(v => {
            this.options.forEach(v1 => {
              if (this.searchTree(v1, v)) {
                isResourceId.push(this.searchTree(v1, v))
              }
            })
          })
          resourceIdArr = isResourceId.map(item => {
            return { id: item.id, name: item.name, res: item.fullName }
          })
        }
        return resourceIdArr
      },
      cacheParams () {
        return {
          resourceList: this.resourceIdArr,
          localParams: this.localParams,
          deployMode: this.deployMode,
          master: this.master,
          masterUrl: this.masterUrl,
          queue: this.queue
        }
      }
    },
    created () {
      let item = this.store.state.dag.resourcesListS
      this.diGuiTree(item)
      this.options = item
      let o = this.backfillItem

      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        this.master = o.params.master || 'yarn'
        this.deployMode = o.params.deployMode || 'client'
        this.masterUrl = o.params.masterUrl || ''
        this.queue = o.params.queue || 'default'
        this.rawScript = o.params.rawScript || ''

        // backfill resourceList
        let backResource = o.params.resourceList || []
        let resourceList = o.params.resourceList || []
        if (resourceList.length) {
          _.map(resourceList, v => {
            if (!v.id) {
              this.store.dispatch('dag/getResourceId', {
                type: 'FILE',
                fullName: '/' + v.res
              }).then(res => {
                this.resourceList.push(res.id)
                this.dataProcess(backResource)
              }).catch(e => {
                this.resourceList.push(v.res)
                this.dataProcess(backResource)
              })
            } else {
              this.resourceList.push(v.id)
              this.dataProcess(backResource)
            }
          })
        }
        // backfill localParams
        let localParams = o.params.localParams || []
        if (localParams.length) {
          this.localParams = localParams
        }
      }
    },
    mounted () {
    },
    destroyed () {
    },
    components: { mLocalParams, mListBox, Treeselect }
  }
</script>
