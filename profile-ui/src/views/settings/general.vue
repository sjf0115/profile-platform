<template>
  <div class="general-settings-page">
    <div class="page-header">
      <h2 class="page-title">通用配置</h2>
      <p class="page-desc">管理系统级基础配置</p>
    </div>

    <!-- 基础信息（预留） -->
    <el-card class="config-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>基础信息</span>
          <el-tag type="info" size="small">待实现</el-tag>
        </div>
      </template>
      <el-empty description="平台名称、Logo 等基础信息配置（后续实现）" :image-size="60" />
    </el-card>

    <!-- 邮件服务 SMTP -->
    <el-card class="config-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>邮件服务（SMTP）</span>
        </div>
      </template>

      <!-- 加载中 -->
      <el-skeleton v-if="loadingForm" :rows="6" animated />

      <!-- 空状态 -->
      <el-empty v-else-if="smtpParams.length === 0" description="加载 SMTP 配置失败" :image-size="60" />

      <!-- 配置列表 -->
      <div v-else class="config-list">
        <div
          v-for="param in smtpParams"
          :key="param.field"
          class="config-row"
        >
          <!-- 左侧标签 -->
          <div class="config-label">
            <span v-if="isRequired(param)" class="required-mark">*</span>
            {{ param.title }}
          </div>

          <!-- 右侧内容区 -->
          <div class="config-value">
            <!-- ===== 编辑模式 ===== -->
            <template v-if="editingField === param.field">
              <div class="edit-control">
                <!-- 输入框 -->
                <el-input
                  v-if="param.type === 'input'"
                  v-model="editValue"
                  :placeholder="getPlaceholder(param)"
                  :type="getInputType(param)"
                  :show-password="isPasswordField(param)"
                  clearable
                  size="small"
                  class="edit-input"
                  @keyup.enter="handleConfirmEdit(param)"
                />
                <!-- 选择框 -->
                <el-select
                  v-else-if="param.type === 'select'"
                  v-model="editValue"
                  :placeholder="getPlaceholder(param)"
                  clearable
                  size="small"
                  class="edit-input"
                >
                  <el-option
                    v-for="option in param.options"
                    :key="String(option.value)"
                    :label="option.label"
                    :value="String(option.value)"
                  />
                </el-select>
                <!-- 单选框 -->
                <el-radio-group
                  v-else-if="param.type === 'radio'"
                  v-model="editValue"
                  size="small"
                >
                  <el-radio
                    v-for="option in param.options"
                    :key="String(option.value)"
                    :label="String(option.value)"
                  >
                    {{ option.label }}
                  </el-radio>
                </el-radio-group>
                <!-- 文本域 -->
                <el-input
                  v-else-if="param.type === 'textarea'"
                  v-model="editValue"
                  type="textarea"
                  :rows="2"
                  :placeholder="getPlaceholder(param)"
                  clearable
                  size="small"
                  class="edit-input"
                />
                <!-- 默认输入框 -->
                <el-input
                  v-else
                  v-model="editValue"
                  :placeholder="getPlaceholder(param)"
                  clearable
                  size="small"
                  class="edit-input"
                />
              </div>
              <div class="edit-actions">
                <el-button
                  type="primary"
                  size="small"
                  :loading="savingField === param.field"
                  @click="handleConfirmEdit(param)"
                >
                  保存
                </el-button>
                <el-button size="small" @click="handleCancelEdit">取消</el-button>
              </div>
            </template>

            <!-- ===== 展示模式 ===== -->
            <template v-else>
              <!-- 开关类型：直接展示 switch -->
              <template v-if="param.type === 'checkbox'">
                <el-switch
                  :model-value="toBool(smtpFormData[param.field])"
                  @change="(val: boolean) => handleSwitchChange(param, val)"
                />
              </template>

              <!-- 其他类型：文本 + 编辑图标 -->
              <template v-else>
                <span class="display-text" :class="{ 'is-empty': !smtpFormData[param.field] }">
                  {{ getDisplayText(param) }}
                </span>
                <el-icon
                  class="edit-icon"
                  :class="{ 'is-saving': savingField === param.field }"
                  @click="handleStartEdit(param)"
                >
                  <Edit />
                </el-icon>
              </template>
            </template>
          </div>
        </div>

        <!-- 分割线 + 测试发送 -->
        <div class="config-row test-row">
          <div class="config-label">连通性测试</div>
          <div class="config-value">
            <el-input
              v-model="testReceiver"
              placeholder="测试收件人邮箱（可选，默认使用发件人邮箱）"
              clearable
              size="small"
              class="test-input"
            />
            <el-button
              type="success"
              size="small"
              :loading="testing"
              @click="handleTest"
            >
              发送测试邮件
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import type { PluginParam } from '@/types'
import { systemConfigApi } from '@/api/systemConfig'

// 状态
const loadingForm = ref(false)
const testing = ref(false)
const savingField = ref<string | null>(null)
const testReceiver = ref('')

// 编辑状态
const editingField = ref<string | null>(null)
const editValue = ref<any>('')

// SMTP 表单 Schema
const smtpParams = ref<PluginParam[]>([])

// 当前表单数据
const smtpFormData = reactive<Record<string, any>>({})

// 初始化
onMounted(async () => {
  await Promise.all([fetchSmtpForm(), fetchSmtpConfig()])
})

const fetchSmtpForm = async () => {
  loadingForm.value = true
  try {
    const res = await systemConfigApi.getSmtpForm()
    let data = res.data.data
    if (typeof data === 'string') {
      try { data = JSON.parse(data) } catch (e) {
        ElMessage.error('SMTP 表单配置格式错误')
        return
      }
    }
    if (data && Array.isArray(data)) {
      smtpParams.value = data
      data.forEach((param: PluginParam) => {
        if (smtpFormData[param.field] === undefined) {
          smtpFormData[param.field] = param.value ?? ''
        }
      })
    }
  } catch (error) {
    console.error('获取 SMTP 表单失败:', error)
  } finally {
    loadingForm.value = false
  }
}

const fetchSmtpConfig = async () => {
  try {
    const res = await systemConfigApi.getConfigByGroup('smtp')
    const config = res.data.data
    if (config && typeof config === 'object') {
      Object.entries(config).forEach(([key, value]) => {
        smtpFormData[key] = value
      })
    }
  } catch (error) {
    console.error('获取 SMTP 配置失败:', error)
  }
}

// ---- 展示辅助 ----

const isRequired = (param: PluginParam) => {
  return param.validate?.some((v: any) => v.required)
}

const getPlaceholder = (param: PluginParam) => {
  if (param.props && 'placeholder' in param.props) {
    return (param.props as any).placeholder
  }
  return `请输入${param.title}`
}

const getInputType = (param: PluginParam) => {
  if (param.props && 'type' in param.props) return (param.props as any).type
  if (param.field.toLowerCase().includes('passwd') || param.field.toLowerCase().includes('password')) return 'password'
  return 'text'
}

const isPasswordField = (param: PluginParam) => getInputType(param) === 'password'

const toBool = (val: any) => val === true || val === 'true'

/** 获取展示文本：radio 显示选中的 label，input 显示值或占位 */
const getDisplayText = (param: PluginParam) => {
  const val = smtpFormData[param.field]
  if (!val && val !== 0) return '未配置'

  // radio / select：找到对应 option 的 label
  if ((param.type === 'radio' || param.type === 'select') && param.options) {
    const matched = param.options.find((o: any) => String(o.value) === String(val))
    return matched ? matched.label : String(val)
  }

  // 密码字段：遮掩
  if (isPasswordField(param)) return '••••••••'

  return String(val)
}

// ---- 编辑交互 ----

const handleStartEdit = (param: PluginParam) => {
  editingField.value = param.field
  editValue.value = smtpFormData[param.field] ?? ''
}

const handleCancelEdit = () => {
  editingField.value = null
  editValue.value = ''
}

const handleConfirmEdit = async (param: PluginParam) => {
  savingField.value = param.field
  try {
    await systemConfigApi.saveConfig('smtp', { [param.field]: String(editValue.value ?? '') })
    smtpFormData[param.field] = editValue.value
    handleCancelEdit()
    ElMessage.success(`${param.title} 保存成功`)
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  } finally {
    savingField.value = null
  }
}

/** 开关切换：直接保存 */
const handleSwitchChange = async (param: PluginParam, val: boolean) => {
  const strVal = String(val)
  savingField.value = param.field
  try {
    await systemConfigApi.saveConfig('smtp', { [param.field]: strVal })
    smtpFormData[param.field] = strVal
    ElMessage.success(`${param.title} 已${val ? '开启' : '关闭'}`)
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    savingField.value = null
  }
}

// ---- 测试发送 ----

const handleTest = async () => {
  testing.value = true
  try {
    await systemConfigApi.testSmtp(testReceiver.value || undefined)
    ElMessage.success('测试邮件发送成功，请检查收件箱')
  } catch (error) {
    console.error('SMTP 测试失败:', error)
  } finally {
    testing.value = false
  }
}
</script>

<style scoped>
.general-settings-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 600;
}

.page-desc {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.config-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 600;
}

/* ---- 配置列表 ---- */
.config-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 32px;
}

.config-row {
  display: flex;
  align-items: center;
  min-height: 48px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.config-row:last-child {
  border-bottom: none;
}

/* 测试行跨满两列 */
.test-row {
  grid-column: 1 / -1;
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  border-bottom: none;
}

.config-label {
  flex: 0 0 120px;
  font-size: 14px;
  color: #606266;
  line-height: 32px;
}

.required-mark {
  color: #f56c6c;
  margin-right: 2px;
}

.config-value {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 32px;
  min-width: 0;
}

/* ---- 展示模式 ---- */
.display-text {
  font-size: 14px;
  color: #303133;
  line-height: 32px;
}

.display-text.is-empty {
  color: #c0c4cc;
}

.edit-icon {
  cursor: pointer;
  color: #909399;
  font-size: 14px;
  transition: color 0.2s;
  flex-shrink: 0;
}

.edit-icon:hover {
  color: #409eff;
}

.edit-icon.is-saving {
  color: #c0c4cc;
  pointer-events: none;
}

/* ---- 编辑模式 ---- */
.edit-control {
  display: flex;
  align-items: center;
  gap: 8px;
}

.edit-input {
  width: 100%;
  max-width: 280px;
}

.edit-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

/* ---- 测试行 ---- */

.test-input {
  width: 360px;
}
</style>
