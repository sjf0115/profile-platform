<template>
  <div class="group-detail-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h2 class="page-title">群组详情</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleEdit">编辑</el-button>
        <el-button @click="handleAnalyze">分析</el-button>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="page-content">
      <!-- 基本信息 -->
      <div class="section">
        <div class="section-header">
          <el-icon class="section-icon"><Menu /></el-icon>
          <span class="section-title">基本信息</span>
        </div>
        <div class="section-content">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="群组名称">{{ groupInfo.group_name }}</el-descriptions-item>
            <el-descriptions-item label="群组ID">{{ groupInfo.group_id }}</el-descriptions-item>
            <el-descriptions-item label="群组状态">
              <el-tag :type="getStatusType(groupInfo.group_status)" size="small">
                {{ getStatusText(groupInfo.group_status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建方式">{{ getGroupTypeText(groupInfo.group_type) }}</el-descriptions-item>
            <el-descriptions-item label="群组规模">{{ groupInfo.group_count || 0 }} 人</el-descriptions-item>
            <el-descriptions-item label="实体类型">{{ formatEntityType(groupInfo) }}</el-descriptions-item>
            <el-descriptions-item label="创建人">{{ groupInfo.creator }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(groupInfo.gmt_create) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDateTime(groupInfo.gmt_modified) }}</el-descriptions-item>
            <el-descriptions-item label="计算周期" :span="1">
              {{ getTriggerTypeText(groupInfo.trigger_type) }}
            </el-descriptions-item>
            <el-descriptions-item v-if="groupInfo.trigger_type === 2" label="Cron表达式" :span="2">
              {{ groupInfo.trigger_cron || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="生效时间" :span="3">
              {{ formatDateTime(groupInfo.trigger_start_time) }} 至 {{ formatDateTime(groupInfo.trigger_end_time) }}
            </el-descriptions-item>
            <el-descriptions-item label="群组描述" :span="3">
              {{ groupInfo.group_desc || '无' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <!-- 执行信息 -->
      <div class="section">
        <div class="section-header">
          <el-icon class="section-icon"><Timer /></el-icon>
          <span class="section-title">执行信息</span>
        </div>
        <div class="section-content">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="执行状态">
              <el-tag :type="getInstanceStatusType(groupInfo.instance_status)" size="small">
                {{ getInstanceStatusText(groupInfo.instance_status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="实例ID">{{ groupInfo.instance_id || '-' }}</el-descriptions-item>
            <el-descriptions-item label="执行信息">{{ groupInfo.instance_msg || '-' }}</el-descriptions-item>
            <el-descriptions-item label="开始时间">{{ formatDateTime(groupInfo.instance_start_time) }}</el-descriptions-item>
            <el-descriptions-item label="结束时间">{{ formatDateTime(groupInfo.instance_end_time) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <!-- 群组规则 -->
      <div class="section">
        <div class="section-header">
          <el-icon class="section-icon"><Setting /></el-icon>
          <span class="section-title">{{ ruleSectionTitle }}</span>
          <span class="rule-tip">{{ ruleSectionTip }}</span>
          <el-tooltip content="规则说明" placement="top">
            <el-icon class="help-icon"><QuestionFilled /></el-icon>
          </el-tooltip>
        </div>
        <div class="section-content">
          <GroupRuleDisplay
            ref="ruleDisplayRef"
            :group-type="groupInfo.group_type || 1"
            :group-rule="groupRule"
            :entity-identifier-id="groupInfo.entity_identifier_id || ''"
            :rule-form="ruleForm"
            readonly
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Menu, Setting, Timer, QuestionFilled } from '@element-plus/icons-vue'
import { groupApi } from '@/api/group'
import type { Group } from '@/types'
import GroupRuleDisplay from './components/GroupRuleDisplay.vue'

const router = useRouter()
const route = useRoute()

const groupId = route.params.id as string
const groupInfo = ref<Partial<Group>>({})
const groupRule = ref<any>(null)
const loading = ref(false)
const ruleDisplayRef = ref()

// 规则区域标题
const ruleSectionTitle = computed(() => {
  switch (groupInfo.value.group_type) {
    case 1: return '群组规则'
    case 2: return '上传文件'
    case 3: return 'SQL 语句'
    default: return '群组规则'
  }
})

// 规则区域提示
const ruleSectionTip = computed(() => {
  switch (groupInfo.value.group_type) {
    case 1: return '符合下列条件的用户将创建分群，每条规则计算结果最长支持连续365天'
    case 2: return '通过上传文件方式创建群组'
    case 3: return 'SQL 创建的群组，结果集必须包含 entity_id 列'
    default: return ''
  }
})

// 规则表单
const ruleForm = reactive<{
  expression: {
    logic: string
    rule_groups: any[]
  }
}>({
  expression: {
    logic: 'AND',
    rule_groups: []
  }
})

// 格式化实体类型
const formatEntityType = (info?: Partial<Group>): string => {
  if (!info?.entity_identifier_id) return '-'
  if (info.entity_name && info.entity_identifier_name) {
    return `${info.entity_name} > ${info.entity_identifier_name}`
  }
  return info.entity_identifier_id
}

// 获取群组详情
const fetchGroupDetail = async () => {
  loading.value = true
  try {
    const res = await groupApi.getDetail(groupId)
    groupInfo.value = res.data.data || {}
    // 解析群组规则
    if (groupInfo.value.group_rule) {
      groupRule.value = typeof groupInfo.value.group_rule === 'string' 
        ? JSON.parse(groupInfo.value.group_rule) 
        : groupInfo.value.group_rule
      // 解析规则到表单
      if (groupRule.value?.expression) {
        ruleForm.expression.logic = groupRule.value.expression.logic || 'AND'
        ruleForm.expression.rule_groups = groupRule.value.expression.rule_groups || []
      }
    }
  } catch (error) {
    console.error('获取群组详情失败:', error)
    ElMessage.error('获取群组详情失败')
  } finally {
    loading.value = false
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 编辑
const handleEdit = () => {
  router.push(`/group/edit/${groupId}`)
}

// 分析
const handleAnalyze = () => {
  ElMessage.info('分析功能开发中')
}

// 工具函数
const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getStatusType = (status?: number) => {
  switch (status) {
    case 1: return 'success'
    case 2: return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status?: number) => {
  switch (status) {
    case 1: return '启用'
    case 2: return '停用'
    default: return '未知'
  }
}

const getInstanceStatusType = (status?: number) => {
  switch (status) {
    case 1: return 'info'
    case 2: return 'warning'
    case 3: return 'success'
    case 4: return 'danger'
    default: return 'info'
  }
}

const getInstanceStatusText = (status?: number) => {
  switch (status) {
    case 1: return '未运行'
    case 2: return '运行中'
    case 3: return '成功'
    case 4: return '失败'
    default: return '-'
  }
}

const getGroupTypeText = (type?: number) => {
  switch (type) {
    case 1: return '规则筛选'
    case 2: return '文件上传'
    case 3: return 'SQL创建'
    default: return '未知'
  }
}

const getTriggerTypeText = (type?: number) => {
  switch (type) {
    case 1: return '手动触发'
    case 2: return '周期调度'
    case 3: return 'API触发'
    default: return '-'
  }
}

onMounted(() => {
  fetchGroupDetail()
})
</script>

<style scoped lang="scss">
.group-detail-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;

  .page-title {
    margin: 0;
    font-size: 18px;
    font-weight: 500;
    flex: 1;
  }

  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.page-content {
  flex: 1;
  padding: 20px;
  overflow: auto;
}

.section {
  background-color: #fff;
  border-radius: 4px;
  margin-bottom: 16px;

  .section-header {
    display: flex;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #ebeef5;

    .section-icon {
      color: #909399;
      margin-right: 8px;
      font-size: 18px;
    }

    .section-title {
      font-size: 16px;
      font-weight: 500;
      color: #303133;
    }

    .rule-tip {
      font-size: 13px;
      color: #909399;
      margin-left: 12px;
    }

    .help-icon {
      font-size: 14px;
      color: #909399;
      margin-left: 8px;
      cursor: pointer;

      &:hover {
        color: #409eff;
      }
    }
  }

  .section-content {
    padding: 20px;
  }
}
</style>
