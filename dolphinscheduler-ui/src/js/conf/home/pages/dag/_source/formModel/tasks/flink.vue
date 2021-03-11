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
  <div class="flink-model">
    <m-list-box>
      <div slot="text">{{$t('Program Type')}}</div>
      <div slot="content">
        <el-select
                style="width: 130px;"
                size="small"
                v-model="programType"
                :disabled="isDetails">
          <el-option
                  v-for="city in programTypeList"
                  :key="city.code"
                  :value="city.code"
                  :label="city.code">
          </el-option>
        </el-select>
      </div>
    </m-list-box>
    <m-list-box v-if="programType !== 'PYTHON'">
      <div slot="text">{{$t('Main Class')}}</div>
      <div slot="content">
        <el-input
          :disabled="isDetails"
          type="input"
          size="small"
          v-model="mainClass"
          :placeholder="$t('Please enter main class')">
        </el-input>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Main Jar Package')}}</div>
      <div slot="content">
        <treeselect v-model="mainJar" maxHeight="200" :options="mainJarLists" :disable-branch-nodes="true" :normalizer="normalizer" :disabled="isDetails" :placeholder="$t('Please enter main jar package')">
          <div slot="value-label" slot-scope="{ node }">{{ node.raw.fullName }}</div>
        </treeselect>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Deploy Mode')}}</div>
      <div slot="content">
        <el-radio-group v-model="deployMode" size="small">
          <el-radio :label="'cluster'" :disabled="isDetails"></el-radio>
          <el-radio :label="'local'" :disabled="isDetails"></el-radio>
        </el-radio-group>
      </div>
    </m-list-box>
    <m-list-box v-if="deployMode === 'cluster'">
      <div slot="text">{{$t('Flink Version')}}</div>
      <div slot="content">
        <el-select
          style="width: 100px;"
          size="small"
          v-model="flinkVersion"
          :disabled="isDetails">
          <el-option
            v-for="version in flinkVersionList"
            :key="version.code"
            :value="version.code"
            :label="version.code">
          </el-option>
        </el-select>
      </div>
    </m-list-box>
    <m-list-box v-if="deployMode === 'cluster'">
      <div slot="text">{{$t('App Name')}}</div>
      <div slot="content">
        <el-input
          :disabled="isDetails"
          type="input"
          size="small"
          v-model="appName"
          :placeholder="$t('Please enter app name(optional)')">
        </el-input>
      </div>
    </m-list-box>
    <m-list-4-box v-if="deployMode === 'cluster'">
      <div slot="text">{{$t('JobManager Memory')}}</div>
      <div slot="content">
        <el-input
          :disabled="isDetails"
          type="input"
          size="small"
          v-model="jobManagerMemory"
          :placeholder="$t('Please enter JobManager memory')">
        </el-input>
      </div>
      <div slot="text-2">{{$t('TaskManager Memory')}}</div>
      <div slot="content-2">
        <el-input
          :disabled="isDetails"
          type="input"
          size="small"
          v-model="taskManagerMemory"
          :placeholder="$t('Please enter TaskManager memory')">
        </el-input>
      </div>
    </m-list-4-box>
    <m-list-4-box v-if="deployMode === 'cluster'">
      <div slot="text">{{$t('Slot Number')}}</div>
      <div slot="content">
        <el-input
          :disabled="isDetails"
          type="input"
          size="small"
          v-model="slot"
          :placeholder="$t('Please enter Slot number')">
        </el-input>
      </div>
      <div slot="text-2" v-if="flinkVersion === '<1.10'">{{$t('TaskManager Number')}}</div>
      <div slot="content-2" v-if="flinkVersion === '<1.10'">
        <el-input
          :disabled="isDetails"
          type="input"
          size="small"
          v-model="taskManager"
          :placeholder="$t('Please enter TaskManager number')">
        </el-input>
      </div>
    </m-list-4-box>
    <m-list-4-box>
      <div slot="text">{{$t('Parallelism')}}</div>
      <div slot="content">
        <el-input
          :disabled="isDetails"
          type="input"
          size="small"
          v-model="parallelism"
          :placeholder="$t('Please enter Parallelism')">
        </el-input>
      </div>
    </m-list-4-box>
    <m-list-box>
      <div slot="text">{{$t('Main Arguments')}}</div>
      <div slot="content">
        <el-input
                :autosize="{minRows:2}"
                :disabled="isDetails"
                type="textarea"
                size="small"
                v-model="mainArgs"
                :placeholder="$t('Please enter main arguments')">
        </el-input>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Option Parameters')}}</div>
      <div slot="content">
        <el-input
                :disabled="isDetails"
                :autosize="{minRows:2}"
                type="textarea"
                size="small"
                v-model="others"
                :placeholder="$t('Please enter option parameters')">
        </el-input>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Resources')}}</div>
      <div slot="content">
        <treeselect v-model="resourceList" :multiple="true" maxHeight="200" :options="mainJarList" :normalizer="normalizer" :disabled="isDetails" :value-consists-of="valueConsistsOf" :placeholder="$t('Please select resources')">
          <div slot="value-label" slot-scope="{ node }">{{ node.raw.fullName }}</div>
        </treeselect>
      </div>
    </m-list-box>
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
  import mLocalParams from './_source/localParams'
  import mListBox from './_source/listBox'
  import mList4Box from './_source/list4Box'
  import Treeselect from '@riophae/vue-treeselect'
  import '@riophae/vue-treeselect/dist/vue-treeselect.css'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'flink',
    data () {
      return {
        valueConsistsOf: 'LEAF_PRIORITY',
        // Main function class
        mainClass: '',
        // Master jar package
        mainJar: null,
        // Master jar package(List)
        mainJarLists: [],
        mainJarList: [],
        // Deployment method
        deployMode: 'cluster',
        // Resource(list)
        resourceList: [],
        // Cache ResourceList
        cacheResourceList: [],
        // Custom function
        localParams: [],
        // Slot number
        slot: 1,
        // Parallelism
        parallelism: 1,
        // TaskManager mumber
        taskManager: '2',
        // JobManager memory
        jobManagerMemory: '1G',
        // TaskManager memory
        taskManagerMemory: '2G',
        // Flink app name
        appName: '',
        // Main arguments
        mainArgs: '',
        // Option parameters
        others: '',
        // Program type
        programType: 'SCALA',
        // Program type(List)
        programTypeList: [{ code: 'JAVA' }, { code: 'SCALA' }, { code: 'PYTHON' }],

        flinkVersion: '<1.10',
        // Flink Versions(List)
        flinkVersionList: [{ code: '<1.10' }, { code: '>=1.10' }],

        normalizer (node) {
          return {
            label: node.name
          }
        },
        allNoResources: [],
        noRes: []
      }
    },
    props: {
      backfillItem: Object
    },
    mixins: [disabledState],
    methods: {
      /**
       * getResourceId
       */
      marjarId (name) {
        this.store.dispatch('dag/getResourceId', {
          type: 'FILE',
          fullName: '/' + name
        }).then(res => {
          this.mainJar = res.id
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      /**
       * return localParams
       */
      _onLocalParams (a) {
        this.localParams = a
      },
      /**
       * return resourceList
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
        if (this.programType !== 'PYTHON' && !this.mainClass) {
          this.$message.warning(`${i18n.$t('Please enter main class')}`)
          return false
        }

        if (!this.mainJar) {
          this.$message.warning(`${i18n.$t('Please enter main jar package')}`)
          return false
        }

        if (!this.jobManagerMemory) {
          this.$message.warning(`${i18n.$t('Please enter JobManager memory')}`)
          return false
        }

        if (!Number.isInteger(parseInt(this.jobManagerMemory))) {
          this.$message.warning(`${i18n.$t('Memory should be a positive integer')}`)
          return false
        }

        if (!this.taskManagerMemory) {
          this.$message.warning(`${i18n.$t('Please enter TaskManager memory')}`)
          return false
        }

        if (!Number.isInteger(parseInt(this.taskManagerMemory))) {
          this.$message.warning(`${i18n.$t('Memory should be a positive integer')}`)
          return false
        }

        if (!Number.isInteger(parseInt(this.slot))) {
          this.$message.warning(`${i18n.$t('Please enter Slot number')}`)
          return false
        }

        if (!Number.isInteger(parseInt(this.parallelism))) {
          this.$message.warning(`${i18n.$t('Please enter Parallelism')}`)
          return false
        }

        if (this.flinkVersion === '<1.10' && !Number.isInteger(parseInt(this.taskManager))) {
          this.$message.warning(`${i18n.$t('Please enter TaskManager number')}`)
          return false
        }

        // noRes
        if (this.noRes.length > 0) {
          this.$message.warning(`${i18n.$t('Please delete all non-existent resources')}`)
          return false
        }

        // localParams Subcomponent verification
        if (!this.$refs.refLocalParams._verifProp()) {
          return false
        }

        // storage
        this.$emit('on-params', {
          mainClass: this.mainClass,
          mainJar: {
            id: this.mainJar
          },
          deployMode: this.deployMode,
          resourceList: _.map(this.resourceList, v => {
            return { id: v }
          }),
          localParams: this.localParams,
          flinkVersion: this.flinkVersion,
          slot: this.slot,
          parallelism: this.parallelism,
          taskManager: this.taskManager,
          jobManagerMemory: this.jobManagerMemory,
          taskManagerMemory: this.taskManagerMemory,
          appName: this.appName,
          mainArgs: this.mainArgs,
          others: this.others,
          programType: this.programType
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
            this.mainJarList.forEach(v1 => {
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
            this.mainJarList = this.mainJarList.concat(noResources)
          }
        }
      }
    },
    watch: {
      // Listening type
      programType (type) {
        if (type === 'PYTHON') {
          this.mainClass = ''
        }
      },
      // Watch the cacheParams
      cacheParams (val) {
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
      }
    },
    computed: {
      resourceIdArr () {
        let isResourceId = []
        let resourceIdArr = []
        if (this.resourceList.length > 0) {
          this.resourceList.forEach(v => {
            this.mainJarList.forEach(v1 => {
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
          mainClass: this.mainClass,
          mainJar: {
            id: this.mainJar
          },
          deployMode: this.deployMode,
          resourceList: this.resourceIdArr,
          localParams: this.localParams,
          slot: this.slot,
          parallelism: this.parallelism,
          taskManager: this.taskManager,
          jobManagerMemory: this.jobManagerMemory,
          taskManagerMemory: this.taskManagerMemory,
          appName: this.appName,
          mainArgs: this.mainArgs,
          others: this.others,
          programType: this.programType
        }
      }
    },
    created () {
      let item = this.store.state.dag.resourcesListS
      let items = this.store.state.dag.resourcesListJar
      this.diGuiTree(item)
      this.diGuiTree(items)
      this.mainJarList = item
      this.mainJarLists = items
      let o = this.backfillItem
      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        this.mainClass = o.params.mainClass || ''
        if (o.params.mainJar.res) {
          this.marjarId(o.params.mainJar.res)
        } else if (o.params.mainJar.res === '') {
          this.mainJar = ''
        } else {
          this.mainJar = o.params.mainJar.id || ''
        }
        this.deployMode = o.params.deployMode || ''
        this.flinkVersion = o.params.flinkVersion || '<1.10'
        this.slot = o.params.slot || 1
        this.parallelism = o.params.parallelism || 1
        this.taskManager = o.params.taskManager || '2'
        this.jobManagerMemory = o.params.jobManagerMemory || '1G'
        this.taskManagerMemory = o.params.taskManagerMemory || '2G'
        this.appName = o.params.appName || ''
        this.mainArgs = o.params.mainArgs || ''
        this.others = o.params.others
        this.programType = o.params.programType || 'SCALA'

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
          this.cacheResourceList = resourceList
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
    components: { mLocalParams, mListBox, mList4Box, Treeselect }
  }
</script>
