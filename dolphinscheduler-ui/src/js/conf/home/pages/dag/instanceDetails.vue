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
  <div class="home-main index-model">
    <m-variable></m-variable>
    <m-starting-param></m-starting-param>
    <m-dag v-if="!isLoading" :type="'instance'"></m-dag>
    <m-spin :is-spin="isLoading"></m-spin>
  </div>
</template>
<script>
  import mDag from './_source/dag.vue'
  import { mapActions, mapMutations } from 'vuex'
  import mSpin from '@/module/components/spin/spin'
  import mVariable from './_source/variable'
  import mStartingParam from './_source/startingParam'
  import Affirm from './_source/jumpAffirm'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'instance-details',
    data () {
      return {
        // loading
        isLoading: true
      }
    },
    mixins: [disabledState],
    props: {},
    methods: {
      ...mapMutations('dag', ['setIsDetails', 'resetParams']),
      ...mapActions('dag', ['getProcessList', 'getProjectList', 'getResourcesList', 'getInstancedetail', 'getResourcesListJar']),
      ...mapActions('security', ['getTenantList', 'getWorkerGroupsAll', 'getAlarmGroupsAll']),
      /**
       * init
       */
      init () {
        this.isLoading = true
        // Initialization parameters
        this.resetParams()
        // Promise Get node needs data
        Promise.all([
          // Process instance details
          this.getInstancedetail(this.$route.params.id),
          // get process definition
          this.getProcessList(),
          // get project
          this.getProjectList(),
          // get resources
          this.getResourcesList(),
          // get jar
          this.getResourcesListJar(),
          // get worker group list
          this.getWorkerGroupsAll(),
          // get alarm group list
          this.getAlarmGroupsAll(),
          this.getTenantList()
        ]).then((data) => {
          let item = data[0]
          let flag = false
          if (item.state !== 'WAITING_THREAD' && item.state !== 'SUCCESS' && item.state !== 'PAUSE' && item.state !== 'FAILURE' && item.state !== 'STOP') {
            flag = true
          } else {
            flag = false
          }
          this.setIsDetails(flag)
          this.isLoading = false

          // Whether to pop up the box?
          Affirm.init(this.$root)
        }).catch(() => {
          this.isLoading = false
        })
      },
      /**
       * Redraw (refresh operation)
       */
      _reset () {
        this.getInstancedetail(this.$route.params.id).then(res => {
          let item = res
          let flag = false
          if (item.state !== 'WAITING_THREAD' && item.state !== 'SUCCESS' && item.state !== 'PAUSE' && item.state !== 'FAILURE' && item.state !== 'STOP') {
            flag = true
          } else {
            flag = false
          }
          this.setIsDetails(flag)
        })
      }
    },
    watch: {
      $route: {
        deep: true,
        handler () {
          this.init()
        }
      }
    },
    created () {
      this.init()
    },
    mounted () {
    },
    components: { mDag, mSpin, mVariable, mStartingParam }
  }
</script>
