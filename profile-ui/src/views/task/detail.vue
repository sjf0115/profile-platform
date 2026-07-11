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

      <!-- 告警配置 -->
      <el-card class="detail-card">
        <template #header>
          <div class="card-header">
            <span>告警配置</span>
            <el-button type="primary" link @click="openAlertDialog">
              <el-icon><Edit /></el-icon>
              <span style="margin-left: 4px">编辑</span>
            </el-button>
          </div>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="触发条件">
            {{ getConditionLabel(alertConfig.alert_condition) }}
          </el-descriptions-item>
          <el-descriptions-item label="报警方式">
            <template v-if="alertConfig.alert_channels">
              <el-tag v-for="ch in parseChannels(alertConfig.alert_channels)" :key="ch" size="small" type="success" style="margin-right: 4px">
                {{ getChannelLabel(ch) }}
              </el-tag>
            </template>
            <span v-else>未配置</span>
          </el-descriptions-item>
          <el-descriptions-item label="接收人">
            <span v-if="alertConfig.alert_receivers">{{ formatReceivers(alertConfig.alert_receivers) }}</span>
            <span v-else>未配置</span>
          </el-descriptions-item>
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

    <!-- 告警配置编辑对话框 -->
    <el-dialog v-model="alertDialogVisible" title="告警配置" width="600px" :close-on-click-modal="false">
      <el-form :model="alertForm" label-width="100px">
        <el-divider content-position="left">触发方式</el-divider>
        <el-form-item label="触发条件">
          <el-select v-model="alertForm.alert_condition" placeholder="选择触发条件" style="width: 100%">
            <el-option label="执行失败" value="failure" />
            <el-option label="执行成功" value="success" />
            <el-option label="执行完成" value="finished" />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">报警行为</el-divider>
        <el-alert type="info" :closable="false" show-icon
          title="如未收到告警信息，请参考文档进行排查。" style="margin-bottom: 16px" />

        <el-form-item label="报警方式">
          <el-checkbox-group v-model="alertForm.alert_channels">
            <el-checkbox value="sms" disabled>短信</el-checkbox>
            <el-checkbox value="email">邮件</el-checkbox>
            <el-checkbox value="phone" disabled>电话</el-checkbox>
            <el-tooltip content="敬请期待" placement="top">
              <el-checkbox value="dingtalk" disabled>钉钉群机器人</el-checkbox>
            </el-tooltip>
            <el-tooltip content="敬请期待" placement="top">
              <el-checkbox value="webhook" disabled>WebHook</el-checkbox>
            </el-tooltip>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="接收人">
          <div style="width: 100%">
            <el-checkbox v-model="alertForm.notify_owner">任务责任人</el-checkbox>
            <div style="display: flex; align-items: center; margin-top: 8px">
              <el-checkbox v-model="alertForm.enable_other">其他</el-checkbox>
              <el-select v-model="alertForm.other_receivers" multiple filterable
                :disabled="!alertForm.enable_other" placeholder="请输入接收人名字/ID"
                style="flex: 1; margin-left: 8px">
                <el-option v-for="u in userOptions" :key="u.user_id"
                  :label="u.user_name" :value="u.user_id" />
              </el-select>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="alertDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAlert" :loading="alertSaving">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Edit } from '@element-plus/icons-vue'
import type { Task } from '@/types'
import { taskApi } from '@/api/task'
import { alertApi, type AlertReceiver } from '@/api/alert'
import { userApi } from '@/api/user'

const route = useRoute()
const router = useRouter()
const taskId = computed(() => route.params.id as string)

const loading = ref(false)
const taskInfo = ref<Partial<Task>>({})

// 告警配置
const alertConfig = ref({
  alert_condition: '',
  alert_channels: '',
  alert_receivers: ''
})
const alertDialogVisible = ref(false)
const alertSaving = ref(false)
const alertForm = ref({
  alert_condition: '' as string,
  alert_channels: [] as string[],
  notify_owner: false,
  enable_other: false,
  other_receivers: [] as string[]
})
const userOptions = ref<any[]>([])

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

// ---------- 告警配置 ----------

const getConditionLabel = (code: string) => {
  const map: Record<string, string> = { failure: '执行失败', success: '执行成功', finished: '执行完成' }
  return map[code] || '未配置'
}

const parseChannels = (channels: string) => {
  if (!channels) return []
  return channels.split(',').filter(c => c.trim())
}

const getChannelLabel = (code: string) => {
  const map: Record<string, string> = {
    sms: '短信', email: '邮件', phone: '电话', dingtalk: '钉钉群机器人', webhook: 'WebHook'
  }
  return map[code] || code
}

const formatReceivers = (json: string) => {
  if (!json) return '未配置'
  try {
    const receivers: AlertReceiver[] = JSON.parse(json)
    if (!receivers || receivers.length === 0) return '未配置'
    return receivers.map(r => {
      if (r.type === 'owner') return '任务责任人'
      if (r.type === 'user') return `用户(${r.value})`
      return r.type
    }).join(', ')
  } catch {
    return json
  }
}

/** 获取告警配置 */
const fetchAlertConfig = async () => {
  if (!taskId.value) return
  try {
    const res = await alertApi.getAlertConfig(taskId.value)
    const data = res.data.data
    if (data) {
      alertConfig.value = {
        alert_condition: data.alert_condition || '',
        alert_channels: data.alert_channels || '',
        alert_receivers: data.alert_receivers || ''
      }
    }
  } catch (e) {
    // 未配置或接口异常，忽略
  }
}

/** 获取用户列表（用于接收人下拉） */
const fetchUserOptions = async () => {
  try {
    const res = await userApi.getList()
    userOptions.value = res.data.data || []
  } catch {
    userOptions.value = []
  }
}

/** 打开编辑对话框（回显当前配置） */
const openAlertDialog = () => {
  const cfg = alertConfig.value
  alertForm.value.alert_condition = cfg.alert_condition || ''
  alertForm.value.alert_channels = cfg.alert_channels ? cfg.alert_channels.split(',').filter(c => c.trim()) : []

  // 解析接收人
  alertForm.value.notify_owner = false
  alertForm.value.enable_other = false
  alertForm.value.other_receivers = []
  if (cfg.alert_receivers) {
    try {
      const receivers: AlertReceiver[] = JSON.parse(cfg.alert_receivers)
      for (const r of receivers) {
        if (r.type === 'owner') alertForm.value.notify_owner = true
        if (r.type === 'user' && r.value) {
          alertForm.value.enable_other = true
          alertForm.value.other_receivers.push(r.value)
        }
      }
    } catch { /* ignore */ }
  }
  alertDialogVisible.value = true
}

/** 保存告警配置 */
const handleSaveAlert = async () => {
  const form = alertForm.value
  if (!form.alert_condition) {
    ElMessage.warning('请选择触发条件')
    return
  }
  if (form.alert_channels.length === 0) {
    ElMessage.warning('请选择至少一种报警方式')
    return
  }

  // 组装接收人
  const receivers: AlertReceiver[] = []
  if (form.notify_owner) receivers.push({ type: 'owner' })
  if (form.enable_other) {
    for (const uid of form.other_receivers) {
      receivers.push({ type: 'user', value: uid })
    }
  }

  const data = {
    alert_condition: form.alert_condition,
    alert_channels: form.alert_channels.join(','),
    alert_receivers: JSON.stringify(receivers)
  }

  alertSaving.value = true
  try {
    await alertApi.saveAlertConfig(taskId.value, data)
    ElMessage.success('告警配置已保存')
    alertConfig.value = data
    alertDialogVisible.value = false
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    alertSaving.value = false
  }
}

onMounted(() => {
  fetchTaskDetail()
  fetchAlertConfig()
  fetchUserOptions()
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
