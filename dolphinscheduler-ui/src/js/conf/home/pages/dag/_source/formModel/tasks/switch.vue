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
  <div class="dependence-model">
    <m-list-box>
      <div slot="text">条件</div>
      <div slot="content">
        <div class="dep-opt">
          <a href="javascript:"
             @click="!isDetails && _addDep()"
             class="add-dep">
            <em v-if="!isLoading" class="el-icon-circle-plus-outline" :class="_isDetails" data-toggle="tooltip" :title="$t('Add')"></em>
            <em v-if="isLoading" class="el-icon-loading as as-spin" data-toggle="tooltip" :title="$t('Add')"></em>
          </a>
        </div>
          <m-flow-status
            :dependTaskList='dependTaskList'
            v-model="dependTaskList"
            @on-delete-all="_onDeleteAll"
            @remove='_deleteDep'
            @getDependTaskList="getDependTaskList"
            :rear-list = "rearList"
            :pre-node = "preNode">
          </m-flow-status>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Branch flow')}}</div>
      <div slot="content">
        <el-select style="width: 157px;" size="small" v-model="nextNode" clearable >
          <el-option v-for="item in rearList" :key="item.value" :value="item.value" :label="item.label">
          </el-option>
        </el-select>
      </div>
    </m-list-box>

  </div>
</template>
<script>
  import _ from 'lodash'
  import mListBox from './_source/listBox'
  import mFlowStatus from './_source/flowStatus'
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'switchModel',
    data () {
      return {
        relation: 'AND',
        dependTaskList: [],
        isLoading: false,
        nextNode: []
      }
    },
    mixins: [disabledState],
    props: {
      backfillItem: Object,
      preNode: Array,
      rearList: Array
    },
    methods: {
      _addDep () {
        if (!this.isLoading) {
          this.isLoading = true
          this.dependTaskList.push({
            condition: '',
            nextNode: []
          })
        }
      },
      _deleteDep (i) {
        // remove index dependent
        this.dependTaskList.splice(i, 1)
        // remove tootip
        $('body').find('.tooltip.fade.top.in').remove()
      },
      _onDeleteAll (i) {
        this.dependTaskList = []
        // this._deleteDep(i)
      },
      getDependTaskList (i) {
        // console.log('getDependTaskList',i)
      },
      _setRelation (i) {
        this.dependTaskList[i].relation = this.dependTaskList[i].relation === 'AND' ? 'OR' : 'AND'
      },
      _verification () {
        this.$emit('on-dependent', {
          relation: this.relation,
          nextNode: this.nextNode,
          dependTaskList: _.map(this.dependTaskList, v => {
            return {
              condition: v.condition,
              nextNode: v.nextNode
            }
          })
        })
        return true
      }
    },
    watch: {
      dependTaskList (e) {
        setTimeout(() => {
          this.isLoading = false
        }, 600)
      },
      cacheDependence (val) {
        this.$emit('on-cache-dependent', val)
      }
    },
    beforeCreate () {
    },
    created () {
      let o = this.backfillItem
      // let dependentResult = $(`#${o.id}`).data('dependent-result') || {}

      // Does not represent an empty object backfill
      if (!_.isEmpty(o)) {
        this.relation = _.cloneDeep(o.dependence.relation) || 'AND'
        this.nextNode = _.cloneDeep(o.dependence.nextNode)
        this.dependTaskList = _.cloneDeep(o.dependence.dependTaskList) || []
        // let defaultState = this.isDetails ? 'WAITING' : ''
        // Process instance return status display matches by key
        // _.map(this.dependTaskList, v => _.map(v.dependItemList, v1 => {
        //   $(`#${o.id}`).siblings().each(function(){
        //     if(v1.depTasks == $(this).text()) {
        //       v1.state = $(this).attr('data-dependent-depstate')
        //     }
        //   });
        // }))
      }
    },
    mounted () {
    },
    destroyed () {
    },
    computed: {
      cacheDependence () {
        return {
          nextNode: this.nextNode,
          relation: this.relation,
          dependTaskList: _.map(this.dependTaskList, v => {
            return {
              condition: v.condition,
              nextNode: v.nextNode
            }
          })
        }
      }
    },
    components: { mListBox, mFlowStatus }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .dependence-model {
    margin-top: -10px;
    .dep-opt {
      margin-bottom: 10px;
      padding-top: 3px;
      line-height: 24px;
      .add-dep {
        color: #0097e0;
        margin-right: 10px;
        em {
          font-size: 18px;
          vertical-align: middle;
        }
      }
    }
    .dep-list {
      margin-bottom: 16px;
      position: relative;
      border-left: 1px solid #eee;
      padding-left: 16px;
      margin-left: -16px;
      transition: all 0.2s ease-out;
      padding-bottom: 20px;
      &:hover{
        border-left: 1px solid #0097e0;
        transition: all 0.2s ease-out;
        .dep-line-pie {
          transition: all 0.2s ease-out;
          border: 1px solid #0097e0;
          background: #0097e0;
          color: #fff;
        }
      }
      .dep-line-pie {
        transition: all 0.2s ease-out;
        position: absolute;
        width: 20px;
        height: 20px;
        border: 1px solid #e2e2e2;
        text-align: center;
        top: 50%;
        margin-top: -20px;
        z-index: 1;
        left: -10px;
        border-radius: 10px;
        background: #fff;
        font-size: 12px;
        cursor: pointer;
        &::selection {
          background:transparent;
        }
        &::-moz-selection {
          background:transparent;
        }
        &::-webkit-selection {
          background:transparent;
        }
      }
      .dep-delete {
        position: absolute;
        bottom: -6px;
        left: 14px;
        font-size: 18px;
        color: #ff0000;
        cursor: pointer;
      }
    }
    .ans-icon-increase{
      line-height: 2em;
    }
    .dep-box {
      border-left: 4px solid #eee;
      margin-left: -46px;
      padding-left: 42px;
      position: relative;
      .dep-relation {
        position: absolute;
        width: 20px;
        height: 20px;
        border: 1px solid #e2e2e2;
        text-align: center;
        top: 50%;
        margin-top: -10px;
        z-index: 1;
        left: -12px;
        border-radius: 10px;
        background: #fff;
        font-size: 12px;
        cursor: pointer;
        &::selection {
          background:transparent;
        }
        &::-moz-selection {
          background:transparent;
        }
        &::-webkit-selection {
          background:transparent;
        }
      }
    }
  }
</style>
