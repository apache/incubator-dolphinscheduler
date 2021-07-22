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
  <div class="list-model">
    <div class="table-box">
      <el-table :data="list" size="mini" style="width: 100%">
        <el-table-column type="index" :label="$t('#')" width="50"></el-table-column>
        <el-table-column :label="$t('Name')">
          <template slot-scope="scope">
            <el-popover trigger="hover" placement="top">
              <p>{{ scope.row.alias }}</p>
              <div slot="reference" class="name-wrapper">
                <a href="javascript:" class="links" @click="_go(scope.row)">{{ scope.row.alias }}</a>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="userName" :label="$t('Resource userName')"></el-table-column>
        <el-table-column :label="$t('Whether directory')" width="100">
          <template slot-scope="scope">
            {{scope.row.directory? $t('Yes') : $t('No')}}
          </template>
        </el-table-column>
        <el-table-column prop="fileName" :label="$t('File Name')"></el-table-column>
        <el-table-column :label="$t('Description')" width="200">
          <template slot-scope="scope">
            <span>{{scope.row.description | filterNull}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Size')">
          <template slot-scope="scope">
            {{_rtSize(scope.row.size)}}
          </template>
        </el-table-column>
        <el-table-column :label="$t('Update Time')" min-width="120">
          <template slot-scope="scope">
            <span>{{scope.row.updateTime | formatDate}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Operation')" width="150">
          <template slot-scope="scope">
            <el-tooltip :content="$t('Edit')" placement="top" :enterable="false">
              <span><el-button type="primary" size="mini" icon="el-icon-edit-outline" @click="_edit(scope.row)" :disabled="_rtDisb(scope.row)" circle></el-button></span>
            </el-tooltip>
            <el-tooltip :content="$t('Rename')" placement="top" :enterable="false">
              <span><el-button type="primary" size="mini" icon="el-icon-edit" @click="_rename(scope.row,scope.$index)" circle></el-button></span>
            </el-tooltip>
            <el-tooltip :content="$t('Download')" placement="top" :enterable="false">
              <span><el-button type="primary" size="mini" icon="el-icon-download" @click="_downloadFile(scope.row)" :disabled="scope.row.directory? true: false" circle></el-button></span>
            </el-tooltip>
            <el-tooltip :content="$t('Delete')" placement="top" :enterable="false">
              <el-popconfirm
                :confirmButtonText="$t('Confirm')"
                :cancelButtonText="$t('Cancel')"
                icon="el-icon-info"
                iconColor="red"
                :title="$t('Delete?')"
                @onConfirm="_delete(scope.row,scope.row.id)"
              >
                <el-button type="danger" size="mini" icon="el-icon-delete" circle slot="reference"></el-button>
              </el-popconfirm>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog
      :visible.sync="renameDialog"
      width="auto">
      <m-rename :item="item" @onUpDate="onUpDate" @close="close"></m-rename>
    </el-dialog>
  </div>
</template>
<script>
  import _ from 'lodash'
  import mRename from './rename'
  import { mapActions } from 'vuex'
  import { filtTypeArr } from '../../_source/common'
  import { bytesToSize } from '@/module/util/util'
  import { downloadFile } from '@/module/download'
  import localStore from '@/module/util/localStorage'

  export default {
    name: 'file-manage-list',
    data () {
      return {
        list: [],
        renameDialog: false
      }
    },
    props: {
      fileResourcesList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('resource', ['deleteResource']),
      _edit (item) {
        localStore.setItem('file', `${item.alias}|${item.size}`)
        this.$router.push({ path: `/resource/file/edit/${item.id}` })
      },
      _go (item) {
        localStore.setItem('file', `${item.alias}|${item.size}`)
        if (item.directory) {
          localStore.setItem('pid', `${item.id}`)
          localStore.setItem('currentDir', `${item.fullName}`)
          this.$router.push({ path: `/resource/file/subdirectory/${item.id}` })
        } else {
          this.$router.push({ path: `/resource/file/list/${item.id}` })
        }
      },
      _downloadFile (item) {
        downloadFile('resources/download', {
          id: item.id
        })
      },
      _rtSize (val) {
        return bytesToSize(parseInt(val))
      },
      _delete (item, i) {
        this.deleteResource({
          id: item.id
        }).then(res => {
          this.$emit('on-update')
          this.$message.success(res.msg)
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      _rename (item, i) {
        this.item = item
        this.index = i
        this.renameDialog = true
      },

      onUpDate (item) {
        this.$set(this.list, this.index, item)
        this.renameDialog = false
      },

      close () {
        this.renameDialog = false
      },

      _rtDisb ({ alias, size }) {
        let i = alias.lastIndexOf('.')
        let a = alias.substring(i, alias.length)
        let flag = _.includes(filtTypeArr, _.trimStart(a, '.'))
        if (flag && (size < 1000000)) {
          flag = true
        } else {
          flag = false
        }
        return !flag
      }
    },
    watch: {
      fileResourcesList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
      // Listening for routing changes
      // '$route': {
      //   deep: false,
      //   handler () {
      //     this.$emit('on-update',this.$route.params.id)
      //   }
      // }
    },
    beforeRouteUpdate (to, from, next) {
      next() // 一定要有next
    },
    created () {
    },
    mounted () {
      this.list = this.fileResourcesList
    },
    components: { mRename }
  }
</script>
