<template>
  <div class="group-list-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">用户分群</h2>
        <el-tooltip content="用户分群是基于特定规则筛选出的用户集合">
          <el-icon><QuestionFilled /></el-icon>
        </el-tooltip>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          创建分群
        </el-button>
      </div>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="group-tabs">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="我创建的" name="mine" />
    </el-tabs>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索分群名称/创建人"
        style="width: 300px"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      :data="groupList"
      stripe
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="group_name" label="分群名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="group_count" label="人数" width="100" sortable>
        <template #default="{ row }">
          {{ row.group_count || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="刷新" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.group_type === 1" size="small">固定</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="creator" label="创建者" width="120" />
      <el-table-column prop="gmt_create" label="最近使用" width="150">
        <template #default="{ row }">
          {{ formatDate(row.gmt_create) }}
        </template>
      </el-table-column>
      <el-table-column label="主体" width="100">
        <template #default="{ row }">
          {{ getEntityName(row.entity_id) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleAnalyze(row)">分析</el-button>
          <el-button link type="primary" @click="handlePush(row)">推送</el-button>
          <el-button link type="primary" @click="handleUpdate(row)">更新</el-button>
          <el-button link type="primary" @click="handleDownload(row)">下载</el-button>
          <el-button link type="primary" @click="handleCopy(row)">复制</el-button>
          <el-dropdown trigger="click">
            <el-button link type="primary">
              <el-icon><More /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleViewUsers(row)">查看用户列表</el-dropdown-item>
                <el-dropdown-item @click="handleDelete(row)">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 创建方式选择弹窗 -->
    <CreateTypeDialog v-model:visible="showCreateDialog" @select="handleCreateTypeSelect" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, QuestionFilled, More } from '@element-plus/icons-vue'
import { groupApi } from '@/api/group'
import type { Group } from '@/types'
import CreateTypeDialog from './components/CreateTypeDialog.vue'

const router = useRouter()

// 状态
const loading = ref(false)
const activeTab = ref('all')
const searchKeyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const groupList = ref<Group[]>([])
const showCreateDialog = ref(false)

// 获取群组列表
const fetchGroupList = async () => {
  loading.value = true
  try {
    const params: any = {
      page_num: pageNum.value,
      page_size: pageSize.value
    }
    if (searchKeyword.value) {
      params.group_name = searchKeyword.value
    }
    if (activeTab.value === 'mine') {
      params.creator = 'current_user' // 实际需要获取当前用户ID
    }
    
    const res = await groupApi.getList(params)
    groupList.value = res.data.data || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取群组列表失败:', error)
    ElMessage.error('获取群组列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  fetchGroupList()
}

// 重置
const handleReset = () => {
  searchKeyword.value = ''
  pageNum.value = 1
  fetchGroupList()
}

// 分页
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchGroupList()
}

const handlePageChange = (val: number) => {
  pageNum.value = val
  fetchGroupList()
}

// 创建方式选择
const handleCreateTypeSelect = (type: string) => {
  showCreateDialog.value = false
  switch (type) {
    case 'rule':
      router.push('/group/create/rule')
      break
    case 'upload':
      ElMessage.info('上传文件创建功能开发中')
      break
    case 'sql':
      ElMessage.info('SQL创建功能开发中')
      break
  }
}

// 操作处理
const handleView = (row: Group) => {
  router.push(`/group/detail/${row.group_id}`)
}

const handleEdit = (row: Group) => {
  router.push(`/group/edit/${row.group_id}`)
}

const handleAnalyze = (row: Group) => {
  ElMessage.info('分析功能开发中')
}

const handlePush = (row: Group) => {
  ElMessage.info('推送功能开发中')
}

const handleUpdate = (row: Group) => {
  ElMessage.info('更新功能开发中')
}

const handleDownload = (row: Group) => {
  ElMessage.info('下载功能开发中')
}

const handleCopy = (row: Group) => {
  ElMessage.info('复制功能开发中')
}

const handleViewUsers = (row: Group) => {
  ElMessage.info('查看用户列表功能开发中')
}

const handleDelete = (row: Group) => {
  ElMessageBox.confirm('确认删除该分群吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await groupApi.delete(row.group_id)
      ElMessage.success('删除成功')
      fetchGroupList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 工具函数
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString()
}

const getEntityName = (entityId?: string) => {
  // 实际应该从实体列表中获取名称
  return entityId ? '主体1' : '-'
}

onMounted(() => {
  fetchGroupList()
})
</script>

<style scoped lang="scss">
.group-list-page {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 8px;

      .page-title {
        margin: 0;
        font-size: 20px;
        font-weight: 500;
      }
    }
  }

  .group-tabs {
    margin-bottom: 20px;
  }

  .search-bar {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
  }

  .el-table {
    flex: 1;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
