<template>
  <div class="group-rule-display">
    <!-- SQL 类型：只读展示 SQL -->
    <div v-if="groupType === 3" class="sql-display">
      <div class="sql-label">圈选 SQL：</div>
      <pre class="sql-code">{{ sqlText }}</pre>
    </div>

    <!-- 文件上传类型：展示文件信息（只读） -->
    <div v-else-if="groupType === 2" class="upload-display">
      <div class="uploaded-file-card">
        <el-icon class="success-icon"><CircleCheck /></el-icon>
        <span class="file-status">已完成</span>
        <span class="file-name">{{ uploadedFileName }}</span>
      </div>
      <div class="upload-tip">通过上传文件方式创建群组，文件中的 entity_id 列将作为群组用户</div>
    </div>

    <!-- 规则类型：显示 GroupRuleConfig -->
    <GroupRuleConfig
      v-else-if="groupType === 1 && groupRule"
      ref="ruleConfigRef"
      v-model="localRuleForm"
      :entity-identifier-id="entityIdentifierId"
      :readonly="readonly"
    />

    <el-empty v-else description="暂无规则数据" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Document, CircleCheck } from '@element-plus/icons-vue'
import GroupRuleConfig from './GroupRuleConfig.vue'

const props = withDefaults(defineProps<{
  groupType: number
  groupRule: any
  readonly?: boolean
  entityIdentifierId?: string
  ruleForm?: {
    expression: {
      logic: string
      rule_groups: any[]
    }
  }
}>(), {
  readonly: false,
  entityIdentifierId: '',
  ruleForm: undefined
})

const emit = defineEmits<{
  (e: 'update:ruleForm', value: any): void
}>()

const ruleConfigRef = ref()

// 本地规则表单（用于 v-model）
const localRuleForm = computed({
  get: () => props.ruleForm || { expression: { logic: 'AND', rule_groups: [] } },
  set: (value) => emit('update:ruleForm', value)
})

// SQL 文本
const sqlText = computed(() => {
  if (props.groupRule?.type === 'sql') {
    return props.groupRule.sql_text || ''
  }
  return ''
})

// 上传文件名
const uploadedFileName = computed(() => {
  if (props.groupRule?.type === 'upload') {
    return props.groupRule.file_list?.[0] || '未知文件'
  }
  return ''
})

// 暴露组件方法
const validate = () => {
  if (props.groupType === 1 && ruleConfigRef.value) {
    return ruleConfigRef.value.validate()
  }
  return true
}

const getGroupRule = () => {
  if (props.groupType === 1 && ruleConfigRef.value) {
    return ruleConfigRef.value.getGroupRule()
  }
  return props.groupRule
}

defineExpose({
  validate,
  getGroupRule
})
</script>

<style scoped lang="scss">
.sql-display {
  .sql-label {
    font-size: 14px;
    color: #606266;
    margin-bottom: 8px;
  }

  .sql-code {
    background: #1e1e1e;
    color: #d4d4d4;
    padding: 16px;
    border-radius: 4px;
    font-family: 'Fira Code', 'Consolas', monospace;
    font-size: 13px;
    overflow-x: auto;
    white-space: pre-wrap;
    margin: 0;
  }
}

.upload-display {
  .uploaded-file-card {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: #fff;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    max-width: 500px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    margin-bottom: 8px;

    .success-icon {
      color: #67c23a;
      font-size: 16px;
    }

    .file-status {
      font-size: 13px;
      color: #606266;
    }

    .file-name {
      font-size: 14px;
      color: #303133;
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .upload-tip {
    font-size: 13px;
    color: #909399;
  }
}
</style>
