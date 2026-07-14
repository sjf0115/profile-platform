<template>
  <div class="instance-detail-page">
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h2 class="page-title">实例详情</h2>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleRerun">重跑</el-button>
        <el-button @click="handleViewLog">查看日志</el-button>
      </div>
    </div>

    <div class="page-content" v-loading="loading">
      <!-- 实例信息 -->
      <el-card class="detail-card">
        <template #header>
          <div class="card-header">
            <span>实例信息</span>
          </div>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="实例ID">{{ instanceInfo.instance_id }}</el-descriptions-item>
          <el-descriptions-item label="实例名称">{{ instanceInfo.instance_name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="实例状态">
            <el-tag size="small" :type="getStatusType(instanceInfo.status)">
              {{ getStatusLabel(instanceInfo.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="触发模式">
            <el-tag size="small" :type="getTriggerModeType(instanceInfo.trigger_mode)">
              {{ getTriggerModeLabel(instanceInfo.trigger_mode) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatTime(instanceInfo.start_time) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ formatTime(instanceInfo.end_time) }}</el-descriptions-item>
          <el-descriptions-item label="执行时长">{{ formatDuration(instanceInfo.duration) }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ instanceInfo.creator_name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(instanceInfo.gmt_create) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 任务信息 -->
      <el-card class="detail-card" v-if="instanceInfo.task">
        <template #header>
          <div class="card-header">
            <span>任务信息</span>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务ID">{{ instanceInfo.task.task_id }}</el-descriptions-item>
          <el-descriptions-item label="任务名称">{{ instanceInfo.task.task_name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="任务类型">
            <el-tag size="small" type="info">{{ getTaskTypeLabel(instanceInfo.task.task_type) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="任务描述">{{ instanceInfo.task.task_desc || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 执行日志 -->
      <el-card class="detail-card" v-if="instanceInfo.message">
        <template #header>
          <div class="card-header">
            <span>执行日志</span>
          </div>
        </template>
        <div class="log-content">
          <pre>{{ instanceInfo.message }}</pre>
        </div>
      </el-card>
    </div>

    <!-- 日志弹窗 -->
    <el-dialog
      v-model="logDialogVisible"
      title="执行日志"
      width="700px"
      destroy-on-close
    >
      <div class="log-content">
        <pre v-if="instanceInfo.message">{{ instanceInfo.message }}</pre>
        <el-empty v-else description="暂无日志" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import type { TaskInstance } from '@/types'
import { taskInstanceApi } from '@/api/taskInstance'
import { taskApi } from '@/api/task'

const route = useRoute()
const router = useRouter()
const instanceId = computed(() => route.params.id as string)

const loading = ref(false)
const instanceInfo = ref<Partial<TaskInstance>>({})
const logDialogVisible = ref(false)

// 格式化时间戳
const formatTime = (timestamp?: number) => {
  if (!timestamp || timestamp <= 0) return '-'
  const date = new Date(timestamp)
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).format(date).replace(/\//g, '-')
}

// 格式化日期时间
const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).format(date).replace(/\//g, '-')
}

// 获取状态标签
const getStatusLabel = (status?: number) => {
  const map: Record<number, string> = { 1: '未运行', 2: '运行中', 3: '运行失败', 4: '运行成功' }
  return map[status || 0] || '未知'
}

const getStatusType = (status?: number) => {
  const map: Record<number, any> = { 1: 'info', 2: 'primary', 3: 'danger', 4: 'success' }
  return map[status || 0] || 'info'
}

// 获取触发模式标签
const getTriggerModeLabel = (mode?: number) => {
  const map: Record<number, string> = { 1: '手动触发', 2: '定时调度', 3: 'API触发' }
  return map[mode || 0] || '未知'
}

const getTriggerModeType = (mode?: number) => {
  const map: Record<number, any> = { 1: 'info', 2: 'success', 3: 'warning' }
  return map[mode || 0] || 'info'
}

// 获取任务类型
const getTaskTypeLabel = (taskType?: number) => {
  const map: Record<number, string> = { 1: '群组圈选', 2: '群组投递', 3: '数据集同步' }
  return map[taskType || 0] || '未知'
}

// 格式化耗时（毫秒 → 秒）
const formatDuration = (ms?: number) => {
  if (!ms || ms <= 0) return '-'
  const seconds = (ms / 1000).toFixed(1)
  return `${seconds}s`
}

// 获取实例详情
const fetchInstanceDetail = async () => {
  if (!instanceId.value) {
    ElMessage.error('实例ID不能为空')
    return
  }
  loading.value = true
  try {
    const res = await taskInstanceApi.getDetail(instanceId.value)
    instanceInfo.value = res.data.data || {}
  } catch (error) {
    console.error('获取实例详情失败:', error)
    ElMessage.error('获取实例详情失败')
  } finally {
    loading.value = false
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 重跑
const handleRerun = () => {
  ElMessageBox.confirm(
    `确定要重跑实例 "${instanceInfo.value.instance_name}" 吗？`,
    '确认重跑',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  )
    .then(async () => {
      try {
        await taskApi.execute(instanceInfo.value.task_id!)
        ElMessage.success('已触发重跑')
        fetchInstanceDetail()
      } catch (error) {
        console.error('重跑失败:', error)
        ElMessage.error('重跑失败')
      }
    })
    .catch(() => {})
}

// 查看日志
const handleViewLog = () => {
  logDialogVisible.value = true
}

onMounted(() => {
  fetchInstanceDetail()
})
</script>

<style scoped lang="scss">
.instance-detail-page {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .page-title {
      margin: 0;
      font-size: 20px;
      font-weight: 500;
    }
  }
}

.page-content {
  flex: 1;
  overflow: auto;
}

.detail-card {
  margin-bottom: 20px;

  &:last-child {
    margin-bottom: 0;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 500;
  }
}

.log-content {
  max-height: 400px;
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
</style>
