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
  <div class="dep-list-model">
    <div v-for="(el,$index) in dependItemList" :key='$index' @click="itemIndex = $index">
      <el-input
          type="textarea"
          v-model="el.condition"
          autocomplete="off"
          >
      </el-input>
      <div class="flow-item">
        <span class="text-b" style="padding-left: 0">{{$t('Branch flow')}}</span>
          <el-select style="width: 157px;" size="small" v-model="el.nextNode" clearable>
            <el-option v-for="item in rearList" :key="item.value" :value="item.value" :label="item.label">
            </el-option>
          </el-select>
          <span class="operation">
            <a href="javascript:" class="delete" @click="!isDetails && _remove($index)">
              <em class="iconfont el-icon-delete" :class="_isDetails" data-toggle="tooltip" data-container="body" :title="$t('Delete')" ></em>
            </a>
            <a href="javascript:" class="add" @click="!isDetails && _add()" v-if="$index === (dependItemList.length - 1)">
              <em class="iconfont el-icon-circle-plus-outline" :class="_isDetails" data-toggle="tooltip" data-container="body" :title="$t('Add')"></em>
            </a>
        </span>
      </div>

    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import { cycleList, nodeStatusList } from './commcon'
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'flow-status',
    data () {
      return {
        list: [],
        definitionList: [],
        projectList: [],
        cycleList: cycleList,
        isInstance: false,
        itemIndex: null,
        nodeStatusList: nodeStatusList
      }
    },
    mixins: [disabledState],
    props: {
      dependItemList: Array,
      index: Number,
      dependTaskList: Array,
      preNode: Array,
      rearList: Array
    },
    model: {
      prop: 'dependItemList',
      event: 'dependItemListEvent'
    },
    methods: {
      /**
       * add task
       */
      _add () {
        // btn loading
        this.isLoading = true
        this.$emit('dependItemListEvent', _.concat(this.dependItemList, this._rtNewParams()))
        // remove tooltip
        this._removeTip()
      },
      /**
       * remove task
       */
      _remove (i) {
        this.$emit('remove', i)
        this._removeTip()
      },
      _getProjectList () {
        return new Promise((resolve, reject) => {
          this.projectList = _.map(_.cloneDeep(this.store.state.dag.projectListS), v => {
            return {
              value: v.id,
              label: v.name
            }
          })
          resolve()
        })
      },
      _getProcessByProjectId (id) {
        return new Promise((resolve, reject) => {
          this.store.dispatch('dag/getProcessByProjectId', { projectId: id }).then(res => {
            this.definitionList = _.map(_.cloneDeep(res), v => {
              return {
                value: v.id,
                label: v.name
              }
            })
            resolve(res)
          })
        })
      },
      /**
       * get dependItemList
       */
      _getDependItemList (ids, is = true) {
        return new Promise((resolve, reject) => {
          if (is) {
            this.store.dispatch('dag/getProcessTasksList', { processDefinitionId: ids }).then(res => {
              resolve(['ALL'].concat(_.map(res, v => v.name)))
            })
          }
        })
      },
      _rtNewParams () {
        return {
          condition: '',
          nextNode: []
        }
      },
      _rtOldParams (value, depTasksList, item) {
        return {
          depTasks: '',
          status: ''
        }
      },
      /**
       * remove tip
       */
      _removeTip () {
        $('body').find('.tooltip.fade.top.in').remove()
      }
    },
    watch: {
    },
    beforeCreate () {
    },
    created () {
      // is type projects-instance-details
      this.isInstance = this.router.history.current.name === 'projects-instance-details'
      // get processlist
      this._getProjectList().then(() => {
        // let projectId = this.projectList[0].value
        if (!this.dependItemList && !this.dependItemList.length) {
          this.$emit('dependItemListEvent', _.concat(this.dependItemList, this._rtNewParams()))
        } else {
          // get definitionId ids
          let ids = _.map(this.dependItemList, v => v.definitionId).join(',')
          // get item list
          this._getDependItemList(ids, false).then(res => {
            _.map(this.dependItemList, (v, i) => {
              this._getProcessByProjectId(v.projectId).then(definitionList => {
                this.$set(this.dependItemList, i, this._rtOldParams(v.definitionId, ['ALL'].concat(_.map(res[v.definitionId] || [], v => v.name)), v))
              })
            })
          })
        }
      })
    },
    mounted () {},
    components: {}
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .dep-list-model {
    position: relative;
    min-height: 0px;
    .flow-item{
      margin-top:6px;
      margin-bottom: 6px;
    }
   .operation {
        padding-left: 4px;
        a {
          em {
            font-size: 18px;
            vertical-align: middle;
          }
        }
        .delete {
          color: #ff0000;
        }
        .add {
          color: #0097e0;
        }
      }
    .instance-state {
      display: inline-block;
      width: 24px;
      .iconfont {
        font-size: 20px;
        vertical-align: middle;
        cursor: pointer;
        margin-left: 6px;
        &.icon-SUCCESS {
          color: #33cc00;
        }
        &.icon-WAITING {
          color: #888888;
        }
        &.icon-FAILED {
          color: #F31322;
        }
      }
    }
  }
</style>
