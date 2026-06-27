<template>
  <div class="create-sql-group-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h2 class="page-title">{{ pageTitle }}</h2>
    </div>

    <!-- 内容区域 -->
    <div class="page-content">
      <!-- 基本信息区域 -->
      <div class="section">
        <div class="section-header" @click="basicInfoExpanded = !basicInfoExpanded">
          <el-icon class="section-icon"><Menu /></el-icon>
          <span class="section-title">基本信息</span>
          <el-icon class="expand-icon" :class="{ collapsed: !basicInfoExpanded }">
            <ArrowDown />
          </el-icon>
        </div>
        <div v-show="basicInfoExpanded" class="section-content">
          <el-form
            ref="basicFormRef"
            :model="basicForm"
            :rules="basicRules"
            label-width="100px"
            class="basic-form"
          >
            <el-form-item label="群组名称" prop="group_name" required>
              <el-input
                v-model="basicForm.group_name"
                placeholder="请输入群组名称"
                maxlength="50"
                show-word-limit
                style="width: 500px"
              />
            </el-form-item>
            <el-form-item label="分析主体" prop="entity_identifier_id" required>
              <el-select
                v-model="basicForm.entity_identifier_id"
                placeholder="请选择分析主体"
                style="width: 500px"
                @change="handleEntityChange"
              >
                <el-option
                  v-for="item in entityIdentifierList"
                  :key="item.entity_identifier_id"
                  :label="`${item.entity_name} > ${item.entity_identifier_name}`"
                  :value="item.entity_identifier_id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="计算周期" prop="trigger_type" required>
              <el-radio-group v-model="basicForm.trigger_type">
                <el-radio :label="2">周期调度</el-radio>
                <el-radio :label="1">手动触发</el-radio>
              </el-radio-group>
            </el-form-item>

            <!-- 周期调度配置 -->
            <template v-if="basicForm.trigger_type === 2">
              <el-form-item label="计算时间" required>
                <el-time-picker
                  v-model="basicForm.calc_time"
                  format="HH:mm"
                  value-format="HH:mm"
                  placeholder="请选择计算时间"
                  style="width: 200px"
                  @change="updateCronExpression"
                />
              </el-form-item>
              <el-form-item label="Cron 表达式">
                <el-input v-model="basicForm.trigger_cron" readonly disabled style="width: 200px" />
              </el-form-item>
              <el-form-item label="生效日期" required>
                <el-radio-group v-model="basicForm.effective_type" @change="handleEffectiveTypeChange">
                  <el-radio :label="1">永久生效</el-radio>
                  <el-radio :label="2">指定日期</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item v-if="basicForm.effective_type === 2" label="时间区间">
                <div style="width: 250px;">
                  <el-date-picker
                    v-model="basicForm.effective_date_range"
                    type="daterange"
                    unlink-panels
                    range-separator="~"
                    start-placeholder="开始"
                    end-placeholder="结束"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%;"
                  />
                </div>
              </el-form-item>
            </template>

            <el-form-item label="群组描述">
              <el-input
                v-model="basicForm.group_desc"
                type="textarea"
                placeholder="请输入群组描述"
                :rows="3"
                style="width: 500px"
              />
            </el-form-item>
          </el-form>
        </div>
      </div>

      <!-- SQL 编辑区域 -->
      <div class="section sql-section">
        <div class="section-header sql-header">
          <el-icon class="section-icon"><Setting /></el-icon>
          <span class="section-title">SQL 编辑</span>
          <span class="sql-tip">结果集必须包含 entity_id 列</span>
        </div>
        <div class="section-content">
          <div class="sql-editor-container">
            <!-- 左侧：可用表树 -->
            <div class="tables-panel">
              <div class="tables-header">可用表</div>
              <div v-if="availableTablesLoading" class="tables-loading">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>加载中...</span>
              </div>
              <div v-else-if="availableTables.length === 0" class="tables-empty">
                <span>请先选择分析主体</span>
              </div>
              <div v-else class="tables-tree">
                <div
                  v-for="table in availableTables"
                  :key="table.dataset_id"
                  class="table-node"
                >
                  <!-- 表头：图标展开/折叠 + 表名插入 -->
                  <div class="table-header">
                    <el-icon class="table-expand-icon" :class="{ expanded: isTableExpanded(table.dataset_id) }" @click="toggleTable(table.dataset_id)">
                      <CaretRight />
                    </el-icon>
                    <el-icon class="table-grid-icon" @click="insertTableName(table.engine_table_name || '')">
                      <Grid />
                    </el-icon>
                    <div class="table-info" @click="insertTableName(table.engine_table_name || '')">
                      <span class="table-label">{{ table.dataset_name }}</span>
                      <span class="table-actual-name">{{ table.engine_table_name }}</span>
                    </div>
                  </div>
                  <!-- 字段列表：展开时显示 -->
                  <transition name="expand">
                    <div v-if="isTableExpanded(table.dataset_id)" class="fields-list">
                      <div
                        v-for="field in table.fields"
                        :key="field.field_name"
                        class="field-node"
                        :class="{ 'entity-field': field.entity_field }"
                        @click="handleFieldClick(table, field)"
                      >
                        <el-icon v-if="field.entity_field" class="entity-key-icon"><Key /></el-icon>
                        <span class="field-name">{{ field.field_name }}</span>
                        <el-tag size="small" type="info" class="field-type-tag">{{ field.field_type }}</el-tag>
                      </div>
                    </div>
                  </transition>
                </div>
              </div>
            </div>

            <!-- 右侧：CodeMirror SQL 编辑器 -->
            <div class="editor-panel">
              <codemirror
                v-model="sqlText"
                :style="{ height: '400px' }"
                :autofocus="true"
                :indent-with-tab="true"
                :tab-size="2"
                :extensions="extensions"
                placeholder="-- 请输入查询 SQL，结果集必须包含 entity_id 列&#10;-- 示例: SELECT user_id AS entity_id FROM profile_dataset_xxx WHERE age > 18"
              />
            </div>
          </div>

          <!-- 预估人数 -->
          <div class="sql-actions">
            <el-button @click="handleEstimate" :loading="estimating">预估人数</el-button>
            <span v-if="estimateResult !== null" class="estimate-result">
              预估人数：<strong>{{ estimateResult }}</strong> 人
            </span>
            <span v-if="estimateError" class="estimate-error">{{ estimateError }}</span>
          </div>
        </div>
      </div>

      <!-- 底部操作 -->
      <div class="page-actions">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowDown, Menu, Setting, Loading, Grid, CaretRight, Key } from '@element-plus/icons-vue'
import { groupApi } from '@/api/group'
import { entityIdentifierApi } from '@/api/entity'
import type { FormInstance } from 'element-plus'
import type { Dataset, DatasetField } from '@/types'
import { Codemirror } from 'vue-codemirror'
import { sql } from '@codemirror/lang-sql'
import { oneDark } from '@codemirror/theme-one-dark'

const router = useRouter()
const route = useRoute()

// 编辑模式判断
const groupId = computed(() => route.params.id as string | undefined)
const isEdit = computed(() => !!groupId.value)
const pageTitle = computed(() => isEdit.value ? '编辑群组' : 'SQL 创建')

// 展开状态
const basicInfoExpanded = ref(true)
const basicFormRef = ref<FormInstance>()
const saving = ref(false)
const estimating = ref(false)
const estimateResult = ref<number | null>(null)
const estimateError = ref('')

// SQL 文本
const sqlText = ref('')

// 可用数据集表
const availableTables = ref<Dataset[]>([])
const availableTablesLoading = ref(false)

// 表展开状态（默认折叠）
const expandedTables = ref<Set<string>>(new Set())

const toggleTable = (datasetId: string) => {
  if (expandedTables.value.has(datasetId)) {
    expandedTables.value.delete(datasetId)
  } else {
    expandedTables.value.add(datasetId)
  }
  // 触发响应式更新
  expandedTables.value = new Set(expandedTables.value)
}

const isTableExpanded = (datasetId: string) => expandedTables.value.has(datasetId)

// 实体标识列表
const entityIdentifierList = ref<any[]>([])

// 基本信息表单
const basicForm = reactive({
  group_id: '',
  group_name: '',
  group_desc: '',
  group_type: 3, // SQL 创建
  trigger_type: 1,
  calc_time: '',
  trigger_cron: '',
  effective_type: 1,
  effective_date_range: [] as string[],
  entity_identifier_id: '',
})

// 验证规则
const basicRules = {
  group_name: [
    { required: true, message: '请输入群组名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  trigger_type: [
    { required: true, message: '请选择计算周期', trigger: 'change' }
  ],
  entity_identifier_id: [
    { required: true, message: '请选择分析主体', trigger: 'change' }
  ]
}

// CodeMirror SQL schema 自动补全
const sqlSchema = computed(() => {
  return availableTables.value.reduce((acc: Record<string, string[]>, t: Dataset) => {
    acc[t.engine_table_name || ''] = (t.fields || []).map((f) => f.field_name)
    return acc
  }, {})
})

const extensions = computed(() => [
  sql({ schema: sqlSchema.value }),
  oneDark,
])

// 获取实体标识列表
const fetchEntityIdentifierList = async () => {
  try {
    const res = await entityIdentifierApi.list()
    entityIdentifierList.value = res.data.data || []
  } catch (error) {
    console.error('获取实体标识列表失败:', error)
  }
}

// 分析主体变化
const handleEntityChange = async (entityIdentifierId: string) => {
  if (!entityIdentifierId) {
    availableTables.value = []
    return
  }
  availableTablesLoading.value = true
  try {
    const res = await groupApi.getAvailableTables(entityIdentifierId)
    availableTables.value = res.data.data || []
  } catch (error) {
    console.error('获取可用表失败:', error)
    availableTables.value = []
  } finally {
    availableTablesLoading.value = false
  }
}

// 插入表名到编辑器
const insertTableName = (tableName: string) => {
  insertTextAtCursor(tableName)
}

// 点击字段
const handleFieldClick = (table: Dataset, field: DatasetField) => {
  if (field.entity_field) {
    insertTextAtCursor(`${field.field_name} AS entity_id`)
  } else {
    insertTextAtCursor(field.field_name)
  }
}

// 在光标位置插入文本
const insertTextAtCursor = (text: string) => {
  const currentSql = sqlText.value
  // 简单追加到末尾，带空格分隔
  if (currentSql.trim()) {
    sqlText.value = currentSql + ' ' + text
  } else {
    sqlText.value = text
  }
}

// SQL 前端校验
const validateSql = (sql: string): string | null => {
  if (!sql.trim()) return 'SQL 不能为空'
  const forbidden = ['DROP ', 'DELETE ', 'ALTER ', 'TRUNCATE ', 'INSERT ', 'UPDATE ', 'CREATE ']
  const upper = sql.toUpperCase()
  for (const kw of forbidden) {
    if (upper.includes(kw)) return `SQL 中不允许包含: ${kw.trim()}`
  }
  if (!upper.trimStart().startsWith('SELECT')) return 'SQL 必须以 SELECT 开头'
  return null
}

// 预估人数
const handleEstimate = async () => {
  const error = validateSql(sqlText.value)
  if (error) {
    ElMessage.warning(error)
    return
  }
  if (!basicForm.entity_identifier_id) {
    ElMessage.warning('请先选择分析主体')
    return
  }
  estimating.value = true
  estimateResult.value = null
  estimateError.value = ''
  try {
    const res = await groupApi.estimate({
      entity_identifier_id: basicForm.entity_identifier_id,
      group_rule: { type: 'sql', sql_text: sqlText.value },
    })
    estimateResult.value = res.data.data ?? 0
  } catch (e: any) {
    estimateError.value = e?.response?.data?.msg || e.message || '预估失败'
  } finally {
    estimating.value = false
  }
}

// 更新 Cron 表达式
const updateCronExpression = () => {
  if (basicForm.calc_time) {
    const [hour, minute] = basicForm.calc_time.split(':')
    basicForm.trigger_cron = `0 ${minute} ${hour} * * ?`
  } else {
    basicForm.trigger_cron = ''
  }
}

// 生效日期变化
const handleEffectiveTypeChange = () => {
  if (basicForm.effective_type === 1) {
    basicForm.effective_date_range = []
  }
}

// 日期格式化
const formatDate = (date: Date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const getTriggerStartTime = () => {
  if (basicForm.effective_type === 1) return formatDate(new Date())
  return basicForm.effective_date_range?.[0] || formatDate(new Date())
}

const getTriggerEndTime = () => {
  if (basicForm.effective_type === 1) return '9999-12-31'
  return basicForm.effective_date_range?.[1] || formatDate(new Date())
}

// 保存
const handleSave = async () => {
  if (!basicFormRef.value) return
  const basicValid = await basicFormRef.value.validate().catch(() => false)
  if (!basicValid) {
    basicInfoExpanded.value = true
    return
  }

  const sqlError = validateSql(sqlText.value)
  if (sqlError) {
    ElMessage.warning(sqlError)
    return
  }

  saving.value = true
  try {
    const submitData: any = {
      group_name: basicForm.group_name,
      group_desc: basicForm.group_desc,
      entity_identifier_id: basicForm.entity_identifier_id,
      group_type: 3,
      trigger_type: basicForm.trigger_type,
      trigger_cron: basicForm.trigger_type === 2 ? basicForm.trigger_cron : undefined,
      trigger_start_time: getTriggerStartTime(),
      trigger_end_time: getTriggerEndTime(),
      source_type: 2,
      group_rule: {
        type: 'sql',
        sql_text: sqlText.value,
      },
    }

    // 编辑模式需要传递 group_id
    if (isEdit.value) {
      submitData.group_id = basicForm.group_id
    }

    await groupApi.save(submitData)
    ElMessage.success(isEdit.value ? '保存成功' : '保存成功')
    router.push('/group/filter')
  } catch (error: any) {
    console.error('保存失败:', error)
    ElMessage.error(error?.response?.data?.msg || '保存失败')
  } finally {
    saving.value = false
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 获取群组详情（编辑模式）
const fetchGroupDetail = async () => {
  if (!isEdit.value || !groupId.value) return
  try {
    const res = await groupApi.getDetail(groupId.value)
    const data = res.data.data
    if (data) {
      basicForm.group_id = data.group_id
      basicForm.group_name = data.group_name
      basicForm.group_desc = data.group_desc || ''
      basicForm.entity_identifier_id = data.entity_identifier_id || ''
      basicForm.trigger_type = data.trigger_type || 1
      basicForm.trigger_cron = data.trigger_cron || ''

      // 解析生效日期
      if (data.trigger_start_time && data.trigger_end_time) {
        if (data.trigger_end_time === '9999-12-31') {
          basicForm.effective_type = 1
        } else {
          basicForm.effective_type = 2
          basicForm.effective_date_range = [data.trigger_start_time, data.trigger_end_time]
        }
      }

      // 从 Cron 表达式解析计算时间
      if (data.trigger_cron) {
        const parts = data.trigger_cron.split(' ')
        if (parts.length >= 3) {
          basicForm.calc_time = `${parts[2].padStart(2, '0')}:${parts[1].padStart(2, '0')}`
        }
      }

      // 解析群组规则，填充 SQL 文本
      if (data.group_rule) {
        const parsedRule = typeof data.group_rule === 'string'
          ? JSON.parse(data.group_rule)
          : data.group_rule
        if (parsedRule?.type === 'sql') {
          sqlText.value = parsedRule.sql_text || ''
        }
      }

      // 加载可用表
      if (basicForm.entity_identifier_id) {
        await handleEntityChange(basicForm.entity_identifier_id)
      }
    }
  } catch (error) {
    console.error('获取群组详情失败:', error)
    ElMessage.error('获取群组详情失败')
  }
}

onMounted(() => {
  fetchEntityIdentifierList()
  fetchGroupDetail()
})
</script>

<style scoped lang="scss">
.create-sql-group-page {
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
    cursor: pointer;
    user-select: none;

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

    .expand-icon {
      margin-left: 8px;
      color: #909399;
      transition: transform 0.3s;
      &.collapsed { transform: rotate(-90deg); }
    }
  }

  .section-content {
    padding: 0 20px 20px;
  }
}

.sql-section {
  .sql-header {
    cursor: default;

    .sql-tip {
      margin-left: 16px;
      font-size: 13px;
      color: #e6a23c;
    }
  }
}

.sql-editor-container {
  display: flex;
  gap: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;

  .tables-panel {
    width: 260px;
    min-width: 260px;
    border-right: 1px solid #e4e7ed;
    background: #fafafa;
    overflow-y: auto;
    max-height: 420px;

    .tables-header {
      padding: 12px 16px;
      font-size: 14px;
      font-weight: 500;
      color: #303133;
      border-bottom: 1px solid #e4e7ed;
      background: #f5f7fa;
    }

    .tables-loading,
    .tables-empty {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      padding: 40px 16px;
      color: #909399;
      font-size: 13px;
    }

    .tables-tree {
      padding: 8px 0;
    }

    // 展开/折叠过渡动画
    .expand-enter-active,
    .expand-leave-active {
      transition: all 0.2s ease;
      overflow: hidden;
    }

    .expand-enter-from,
    .expand-leave-to {
      opacity: 0;
      max-height: 0;
    }

    .expand-enter-to,
    .expand-leave-from {
      opacity: 1;
      max-height: 500px;
    }

    .table-node {
      margin-bottom: 4px;

      .table-header {
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 6px 16px;
        cursor: default;

        .table-expand-icon {
          font-size: 14px;
          color: #909399;
          cursor: pointer;
          transition: transform 0.2s;
          flex-shrink: 0;

          &:hover {
            color: #409eff;
          }

          &.expanded {
            transform: rotate(90deg);
          }
        }

        .table-grid-icon {
          font-size: 16px;
          color: #409eff;
          cursor: pointer;
          flex-shrink: 0;

          &:hover {
            color: #66b1ff;
          }
        }

        .table-info {
          display: flex;
          flex-direction: column;
          overflow: hidden;
          cursor: pointer;
          flex: 1;

          &:hover .table-label {
            color: #409eff;
          }

          .table-label {
            font-weight: 500;
            font-size: 13px;
            color: #303133;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            transition: color 0.2s;
          }

          .table-actual-name {
            font-size: 11px;
            color: #909399;
          }
        }
      }

      .fields-list {
        .field-node {
          display: flex;
          align-items: center;
          gap: 6px;
          padding: 4px 16px 4px 40px;
          cursor: pointer;
          font-size: 12px;
          color: #606266;

          &:hover {
            background: #ecf5ff;
          }

          &.entity-field {
            background: #fdf6ec;

            .field-name {
              font-weight: 600;
              color: #e6a23c;
            }

            .entity-key-icon {
              color: #e6a23c;
              font-size: 12px;
            }
          }

          .entity-key-icon {
            color: #909399;
            font-size: 12px;
            flex-shrink: 0;
          }

          .field-name {
            flex: 1;
          }

          .field-type-tag {
            transform: scale(0.85);
          }
        }
      }
    }
  }

  .editor-panel {
    flex: 1;
    min-width: 0;
  }
}

.sql-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 12px;

  .estimate-result {
    font-size: 14px;
    color: #67c23a;

    strong {
      font-size: 18px;
    }
  }

  .estimate-error {
    font-size: 13px;
    color: #f56c6c;
  }
}

.basic-form {
  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
}

.page-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
}
</style>
