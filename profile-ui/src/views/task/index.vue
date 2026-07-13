<template>
  <div class="task-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span class="title">任务管理</span>
        </div>
      </template>

      <!-- 搜索和操作区域 -->
      <div class="toolbar">
        <div class="right-filters">
          <el-input
            v-model="queryParams.task_name"
            placeholder="任务名称/任务ID"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select
            v-model="queryParams.task_type"
            placeholder="任务类型"
            clearable
            style="width: 140px"
            @change="handleSearch"
          >
            <el-option label="数据集同步" :value="3" />
            <el-option label="群组计算" :value="1" />
            <el-option label="群组投递" :value="2" />
          </el-select>
          <el-select
            v-model="queryParams.trigger_type"
            placeholder="调度类型"
            clearable
            style="width: 140px"
            @change="handleSearch"
          >
            <el-option label="手动触发" :value="1" />
            <el-option label="每日重复" :value="2" />
            <el-option label="小时重复" :value="3" />
          </el-select>
          <el-select
            v-model="queryParams.status"
            placeholder="状态"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="2" />
          </el-select>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            查询
          </el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="任务名称" min-width="200">
          <template #default="{ row }">
            <div class="task-name">
              <el-link type="primary" @click="handleViewDetail(row)">
                {{ row.task_name }}
              </el-link>
              <div class="task-meta">任务ID: {{ row.task_id }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="任务类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getTaskTypeType(row.task_type)">
              {{ getTaskTypeLabel(row.task_type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="任务状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="调度类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getTriggerTypeType(row.trigger_type)">
              {{ getTriggerTypeLabel(row.trigger_type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建人" width="100" align="center">
          <template #default="{ row }">
            {{ row.creator_name || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.gmt_create) }}
          </template>
        </el-table-column>
        <el-table-column label="修改时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.gmt_modified) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewDetail(row)">
              查看
            </el-button>
            <el-button link type="primary" @click="handleExecute(row)">
              手动执行
            </el-button>
            <el-button link type="primary" @click="handleViewInstance(row)">
              执行记录
            </el-button>
            <el-button 
              v-if="row.source_type !== 1" 
              link 
              type="primary" 
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-dropdown v-if="row.source_type !== 1" trigger="click">
              <el-button link type="primary">
                <el-icon><More /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item divided @click="handleDelete(row)">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.page_num"
          v-model:page-size="queryParams.page_size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, More } from '@element-plus/icons-vue'
import type { Task, TaskQueryParams } from '@/types'
import { taskApi } from '@/api/task'

const router = useRouter()

// 格式化日期时间
const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 加载状态
const loading = ref(false)

// 表格数据
const tableData = ref<Task[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive<TaskQueryParams>({
  page_num: 1,
  page_size: 10,
  task_name: '',
  task_type: undefined,
  trigger_type: undefined,
  status: undefined,
})

// 选中的数据
const selectedRows = ref<Task[]>([])

// 获取任务列表
const fetchData = async () => {
  loading.value = true
  try {
    const params: TaskQueryParams = {
      ...queryParams,
      task_name: queryParams.task_name || undefined,
    }
    const res = await taskApi.getList(params)
    tableData.value = res.data.data || []
    total.value = res.data.data?.length || 0
  } catch (error) {
    console.error('获取任务列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取任务类型标签
const getTaskTypeLabel = (type?: number) => {
  const map: Record<number, string> = { 1: '群组计算', 2: '群组投递', 3: '数据集同步' }
  return map[type || 0] || '未知'
}

const getTaskTypeType = (type?: number) => {
  const map: Record<number, any> = { 1: 'success', 2: 'warning', 3: 'primary' }
  return map[type || 0] || 'info'
}

// 获取调度类型标签
const getTriggerTypeLabel = (type?: number) => {
  const map: Record<number, string> = { 1: '手动触发', 2: '每日重复', 3: '小时重复' }
  return map[type || 0] || '未知'
}

const getTriggerTypeType = (type?: number) => {
  const map: Record<number, any> = { 1: 'info', 2: 'success', 3: 'warning' }
  return map[type || 0] || 'info'
}

// 搜索
const handleSearch = () => {
  queryParams.page_num = 1
  fetchData()
}

// 重置
const handleReset = () => {
  queryParams.task_name = undefined
  queryParams.task_type = undefined
  queryParams.trigger_type = undefined
  queryParams.status = undefined
  queryParams.page_num = 1
  fetchData()
}

// 查看任务详情
const handleViewDetail = (row: Task) => {
  router.push(`/task/detail/${row.task_id}`)
}

// 执行
const handleExecute = (row: Task) => {
  ElMessageBox.confirm(
    `确定要立即执行任务 "${row.task_name}" 吗？`,
    '确认执行',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  )
    .then(async () => {
      try {
        await taskApi.execute(row.task_id)
        ElMessage.success('任务已触发执行')
      } catch (error) {
        console.error('执行失败:', error)
      }
    })
    .catch(() => {})
}

// 查看实例
const handleViewInstance = (row: Task) => {
  router.push({
    path: '/task/instance',
    query: { task_id: row.task_id, task_name: row.task_name }
  })
}

// 启用/禁用
const handleToggleStatus = async (row: Task) => {
  const newStatus = row.status === 1 ? 2 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(
      `确定要${action}任务 "${row.task_name}" 吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await taskApi.updateStatus(row.task_id, newStatus)
    ElMessage.success(`${action}成功`)
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${action}失败:`, error)
    }
  }
}

// 删除
const handleDelete = (row: Task) => {
  ElMessageBox.confirm(
    `确定要删除任务 "${row.task_name}" 吗？`,
    '提示',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  )
    .then(async () => {
      try {
        await taskApi.delete(row.task_id)
        ElMessage.success('删除成功')
        fetchData()
      } catch (error) {
        console.error('删除失败:', error)
      }
    })
    .catch(() => {})
}

// 多选
const handleSelectionChange = (rows: Task[]) => {
  selectedRows.value = rows
}

// 分页
const handleSizeChange = (val: number) => {
  queryParams.page_size = val
  fetchData()
}
const handleCurrentChange = (val: number) => {
  queryParams.page_num = val
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.task-page {
  .page-card {
    min-height: calc(100vh - 104px);
  }

  .card-header {
    .title {
      font-size: 16px;
      font-weight: 600;
    }
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    flex-wrap: wrap;
    gap: 12px;

    .right-filters {
      display: flex;
      gap: 10px;
      align-items: center;
    }
  }

  .task-name {
    .task-meta {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
