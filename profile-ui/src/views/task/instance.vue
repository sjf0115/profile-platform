<template>
  <div class="instance-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">任务实例</span>
            <el-tag v-if="currentTaskName" size="small" type="info" style="margin-left: 12px;">
              {{ currentTaskName }}
            </el-tag>
          </div>
          <div class="header-right">
            <el-button :icon="ArrowLeft" @click="handleBack">返回任务列表</el-button>
            <el-button type="primary" :icon="Refresh" @click="fetchData">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索和操作区域 -->
      <div class="toolbar">
        <div class="left-actions">
          <el-input
            v-model="queryParams.instance_name"
            placeholder="任务名称/任务ID/实例ID"
            clearable
            style="width: 260px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="right-filters">
          <el-select
            v-model="queryParams.status"
            placeholder="实例状态"
            clearable
            style="width: 140px"
            @change="handleSearch"
          >
            <el-option label="未运行" :value="1" />
            <el-option label="运行中" :value="2" />
            <el-option label="运行失败" :value="3" />
            <el-option label="运行成功" :value="4" />
          </el-select>
          <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            查询
          </el-button>
        </div>
      </div>

      <!-- 筛选标签 -->
      <div class="filter-tags">
        <el-check-tag
          v-for="tag in filterTags"
          :key="tag.value"
          :checked="activeFilterTag === tag.value"
          @change="handleFilterTagChange(tag.value)"
        >
          {{ tag.label }}
        </el-check-tag>
      </div>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="基本信息" min-width="220">
          <template #default="{ row }">
            <div class="instance-info">
              <div class="instance-name">
                <el-icon v-if="row.status === 4" color="#67C23A" size="16"><CircleCheck /></el-icon>
                <el-icon v-else-if="row.status === 3" color="#F56C6C" size="16"><CircleClose /></el-icon>
                <el-icon v-else-if="row.status === 2" color="#409EFF" size="16"><Loading /></el-icon>
                <el-icon v-else color="#909399" size="16"><Timer /></el-icon>
                <span class="name-text">{{ row.instance_name }}</span>
              </div>
              <div class="instance-meta">ID: {{ row.instance_id }}</div>
              <div class="instance-time" v-if="row.start_time && row.end_time">
                {{ formatTime(row.start_time) }} ~ {{ formatTime(row.end_time) }}
                <span v-if="row.duration" class="duration">(dur {{ row.duration }}ms)</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="实例状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag
              size="small"
              :type="getStatusType(row.status)"
              effect="light"
            >
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" type="info">
              {{ getTaskTypeLabel(row.instance_related_id) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creator" label="责任人" width="100" />
        <el-table-column label="定时时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.gmt_create) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleRerun(row)">
              <el-icon><RefreshRight /></el-icon>
              重跑
            </el-button>
            <el-button link type="primary" @click="handleViewLog(row)">
              <el-icon><Document /></el-icon>
              日志
            </el-button>
            <el-dropdown trigger="click" @command="(cmd: string) => handleMoreCommand(cmd, row)">
              <el-button link type="primary">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="detail">查看详情</el-dropdown-item>
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

    <!-- 日志弹窗 -->
    <el-dialog
      v-model="logDialogVisible"
      title="执行日志"
      width="700px"
      destroy-on-close
    >
      <div class="log-content">
        <pre v-if="currentLog">{{ currentLog }}</pre>
        <el-empty v-else description="暂无日志" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, RefreshRight, ArrowLeft, ArrowDown,
  CircleCheck, CircleClose, Loading, Timer, Document
} from '@element-plus/icons-vue'
import type { TaskInstance, TaskInstanceQueryParams } from '@/types'
import { taskInstanceApi } from '@/api/taskInstance'
import { taskApi } from '@/api/task'

const route = useRoute()
const router = useRouter()

const currentTaskId = computed(() => route.query.task_id as string)
const currentTaskName = computed(() => route.query.task_name as string)

// 格式化时间戳
const formatTime = (timestamp?: number) => {
  if (!timestamp || timestamp <= 0) return '-'
  const date = new Date(timestamp)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}:${seconds}`
}

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
const tableData = ref<TaskInstance[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive<TaskInstanceQueryParams>({
  page_num: 1,
  page_size: 10,
  instance_name: '',
  status: undefined,
  task_id: currentTaskId.value,
})

// 筛选标签
const filterTags = [
  { label: '全部', value: 'all' },
  { label: '我的', value: 'mine' },
  { label: '运行失败', value: 'failed' },
  { label: '运行成功', value: 'success' },
]
const activeFilterTag = ref('all')

// 日志弹窗
const logDialogVisible = ref(false)
const currentLog = ref('')

// 获取状态标签
const getStatusLabel = (status?: number) => {
  const map: Record<number, string> = { 1: '未运行', 2: '运行中', 3: '运行失败', 4: '运行成功' }
  return map[status || 0] || '未知'
}

const getStatusType = (status?: number) => {
  const map: Record<number, any> = { 1: 'info', 2: 'primary', 3: 'danger', 4: 'success' }
  return map[status || 0] || 'info'
}

// 获取任务类型（简化：从 related_id 推断，实际应查 task 表）
const getTaskTypeLabel = (relatedId?: string) => {
  return relatedId ? '数据集同步' : '未知'
}

// 获取实例列表
const fetchData = async () => {
  loading.value = true
  try {
    let res
    if (currentTaskId.value) {
      res = await taskInstanceApi.listByTaskId(currentTaskId.value)
    } else {
      res = await taskInstanceApi.getList(queryParams)
    }
    tableData.value = res.data.data || []
    total.value = res.data.data?.length || 0
  } catch (error) {
    console.error('获取实例列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page_num = 1
  fetchData()
}

// 重置
const handleReset = () => {
  queryParams.instance_name = ''
  queryParams.status = undefined
  queryParams.page_num = 1
  activeFilterTag.value = 'all'
  fetchData()
}

// 筛选标签切换
const handleFilterTagChange = (value: string) => {
  activeFilterTag.value = value
  if (value === 'failed') {
    queryParams.status = 3
  } else if (value === 'success') {
    queryParams.status = 4
  } else {
    queryParams.status = undefined
  }
  handleSearch()
}

// 返回
const handleBack = () => {
  router.push('/task')
}

// 重跑
const handleRerun = (row: TaskInstance) => {
  ElMessageBox.confirm(
    `确定要重跑实例 "${row.instance_name}" 吗？`,
    '确认重跑',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  )
    .then(async () => {
      try {
        await taskApi.execute(row.task_id)
        ElMessage.success('已触发重跑')
        fetchData()
      } catch (error) {
        console.error('重跑失败:', error)
      }
    })
    .catch(() => {})
}

// 查看日志
const handleViewLog = (row: TaskInstance) => {
  currentLog.value = row.message || '暂无日志信息'
  logDialogVisible.value = true
}

// 更多操作
const handleMoreCommand = (command: string, row: TaskInstance) => {
  if (command === 'detail') {
    handleViewLog(row)
  }
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
.instance-page {
  .page-card {
    min-height: calc(100vh - 104px);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .title {
      font-size: 16px;
      font-weight: 600;
    }

    .header-left {
      display: flex;
      align-items: center;
    }

    .header-right {
      display: flex;
      gap: 10px;
    }
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    flex-wrap: wrap;
    gap: 12px;

    .left-actions {
      display: flex;
      gap: 10px;
    }

    .right-filters {
      display: flex;
      gap: 10px;
      align-items: center;
    }
  }

  .filter-tags {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
    flex-wrap: wrap;
  }

  .instance-info {
    .instance-name {
      display: flex;
      align-items: center;
      gap: 6px;

      .name-text {
        font-weight: 500;
        color: #303133;
      }
    }

    .instance-meta {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }

    .instance-time {
      font-size: 12px;
      color: #606266;
      margin-top: 4px;

      .duration {
        color: #909399;
      }
    }
  }

  .log-content {
    max-height: 500px;
    overflow: auto;
    background-color: #1e1e1e;
    color: #d4d4d4;
    padding: 16px;
    border-radius: 4px;

    pre {
      margin: 0;
      white-space: pre-wrap;
      word-break: break-all;
      font-family: 'Consolas', 'Monaco', monospace;
      font-size: 13px;
      line-height: 1.6;
    }
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
