<template>
  <div class="task-detail-page">
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h2 class="page-title">任务详情</h2>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleExecute">手动执行</el-button>
        <el-button @click="handleViewInstance">执行记录</el-button>
      </div>
    </div>

    <div class="page-content" v-loading="loading">
      <!-- 基本信息 -->
      <el-card class="detail-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
          </div>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="任务ID">{{ taskInfo.task_id }}</el-descriptions-item>
          <el-descriptions-item label="任务名称">{{ taskInfo.task_name }}</el-descriptions-item>
          <el-descriptions-item label="任务类型">
            <el-tag size="small" :type="getTaskTypeType(taskInfo.task_type)">
              {{ getTaskTypeLabel(taskInfo.task_type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="任务状态">
            <el-tag size="small" :type="taskInfo.status === 1 ? 'success' : 'danger'">
              {{ taskInfo.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="责任人">{{ taskInfo.owner || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建方式">
            <span v-if="taskInfo.source_type === 1">系统内置</span>
            <span v-else-if="taskInfo.source_type === 2">自定义</span>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="任务描述" :span="3">{{ taskInfo.task_desc || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 调度配置 -->
      <el-card class="detail-card">
        <template #header>
          <div class="card-header">
            <span>调度配置</span>
          </div>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="调度类型">
            <el-tag size="small" :type="getTriggerTypeType(taskInfo.trigger_type)">
              {{ getTriggerTypeLabel(taskInfo.trigger_type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="Cron表达式" v-if="taskInfo.trigger_type === 3">
            {{ taskInfo.trigger_cron || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="调度开始时间">{{ taskInfo.trigger_start_time || '-' }}</el-descriptions-item>
          <el-descriptions-item label="调度结束时间">{{ taskInfo.trigger_end_time || '-' }}</el-descriptions-item>
          <el-descriptions-item label="调度引擎ID">{{ taskInfo.schedule_id || '-' }}</el-descriptions-item>
          <el-descriptions-item label="触发URL">{{ taskInfo.trigger_url || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 关联信息 -->
      <el-card class="detail-card">
        <template #header>
          <div class="card-header">
            <span>关联信息</span>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="关联对象ID">{{ taskInfo.task_related_id || '-' }}</el-descriptions-item>
          <el-descriptions-item label="触发目标ID">{{ taskInfo.trigger_target_id || '-' }}</el-descriptions-item>
          <el-descriptions-item label="上游任务ID" :span="2">{{ taskInfo.upstream_task_ids || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 元数据信息 -->
      <el-card class="detail-card">
        <template #header>
          <div class="card-header">
            <span>元数据信息</span>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="创建人">{{ taskInfo.creator || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(taskInfo.gmt_create) }}</el-descriptions-item>
          <el-descriptions-item label="修改人">{{ taskInfo.modifier || '-' }}</el-descriptions-item>
          <el-descriptions-item label="修改时间">{{ formatDateTime(taskInfo.gmt_modified) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import type { Task } from '@/types'
import { taskApi } from '@/api/task'

const route = useRoute()
const router = useRouter()
const taskId = computed(() => route.params.id as string)

const loading = ref(false)
const taskInfo = ref<Partial<Task>>({})

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

// 获取任务详情
const fetchTaskDetail = async () => {
  if (!taskId.value) {
    ElMessage.error('任务ID不能为空')
    return
  }
  loading.value = true
  try {
    const res = await taskApi.detail(taskId.value)
    taskInfo.value = res.data.data || {}
  } catch (error) {
    console.error('获取任务详情失败:', error)
    ElMessage.error('获取任务详情失败')
  } finally {
    loading.value = false
  }
}

// 返回
const goBack = () => {
  router.push('/task')
}

// 手动执行
const handleExecute = () => {
  ElMessageBox.confirm(
    `确定要立即执行任务 "${taskInfo.value.task_name}" 吗？`,
    '确认执行',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  )
    .then(async () => {
      try {
        await taskApi.execute(taskInfo.value.task_id!)
        ElMessage.success('任务已触发执行')
      } catch (error) {
        console.error('执行失败:', error)
        ElMessage.error('执行失败')
      }
    })
    .catch(() => {})
}

// 查看执行记录
const handleViewInstance = () => {
  router.push({
    path: '/task/instance',
    query: { task_id: taskInfo.value.task_id, task_name: taskInfo.value.task_name }
  })
}

onMounted(() => {
  fetchTaskDetail()
})
</script>

<style scoped lang="scss">
.task-detail-page {
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
</style>
