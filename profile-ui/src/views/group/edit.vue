<template>
  <div class="edit-group-rule-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h2 class="page-title">编辑群组</h2>
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

            <el-form-item label="计算周期" prop="trigger_type" required>
              <el-radio-group v-model="basicForm.trigger_type">
                <el-radio :label="2">周期调度</el-radio>
                <el-radio :label="1">手动触发</el-radio>
              </el-radio-group>
              <el-tooltip content="计算周期说明" placement="top">
                <el-icon class="help-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </el-form-item>

            <!-- 周期调度配置 -->
            <template v-if="basicForm.trigger_type === 2">
              <el-form-item label="调度周期" required>
                <el-select v-model="basicForm.schedule_cycle" style="width: 200px">
                  <el-option label="日" value="day" />
                </el-select>
              </el-form-item>

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
                <el-input
                  v-model="basicForm.trigger_cron"
                  readonly
                  disabled
                  style="width: 200px"
                  placeholder="选择计算时间后自动生成"
                />
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

              <div class="form-tip schedule-tip">
                提示：30天以上未使用的周期调度分群将会自动调整为手动更新
              </div>
            </template>

            <el-form-item label="分析主体" prop="entity_identifier_id" required>
              <el-select
                v-model="basicForm.entity_identifier_id"
                placeholder="请选择分析主体"
                style="width: 200px"
                disabled
              >
                <el-option
                  v-for="item in entityIdentifierList"
                  :key="item.entity_identifier_id"
                  :label="`${item.entity_name} > ${item.entity_identifier_name}`"
                  :value="item.entity_identifier_id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="群组描述">
              <el-input
                v-model="basicForm.group_desc"
                placeholder="请输入群组描述"
                style="width: 500px"
              />
            </el-form-item>
          </el-form>
        </div>
      </div>

      <!-- 群组规则区域 -->
      <div class="section rule-section">
        <div class="section-header rule-header">
          <el-icon class="section-icon"><Setting /></el-icon>
          <span class="section-title">群组规则</span>
          <span class="rule-tip">符合下列条件的用户将创建分群，每条规则计算结果最长支持连续365天</span>
          <el-tooltip content="规则说明" placement="top">
            <el-icon class="help-icon"><QuestionFilled /></el-icon>
          </el-tooltip>
        </div>
        <div class="section-content">
          <GroupRuleConfig
            ref="ruleConfigRef"
            v-model="ruleForm"
            :entity-identifier-id="basicForm.entity_identifier_id"
          />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowDown, Menu, Setting, QuestionFilled } from '@element-plus/icons-vue'
import { groupApi } from '@/api/group'
import { entityIdentifierApi } from '@/api/entity'
import type { FormInstance } from 'element-plus'
import GroupRuleConfig from './components/GroupRuleConfig.vue'

const router = useRouter()
const route = useRoute()

const groupId = route.params.id as string

// 区域展开状态
const basicInfoExpanded = ref(true)
const basicFormRef = ref<FormInstance>()
const ruleConfigRef = ref()
const saving = ref(false)

// 规则表单
const ruleForm = reactive<{ rule_groups?: any[] }>({
  rule_groups: []
})

// 实体标识列表
const entityIdentifierList = ref<any[]>([])

// 基本信息表单
const basicForm = reactive({
  group_id: '',
  group_name: '',
  group_desc: '',
  group_type: 1,  // 1-规则筛选, 2-文件上传, 3-SQL创建
  trigger_type: 1,
  schedule_cycle: 'day',
  calc_time: '',
  trigger_cron: '',
  effective_type: 1,
  effective_date_range: [] as string[],
  entity_identifier_id: ''
})

// 群组规则（用于回填）
const groupRule = ref<any>(null)

// 是否可编辑规则（只有规则筛选类型可编辑）
const canEditRule = computed(() => {
  return basicForm.group_type === 1 // 1-规则筛选
})

// 基本信息验证规则
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

// 获取实体标识列表
const fetchEntityIdentifierList = async () => {
  try {
    const res = await entityIdentifierApi.list()
    entityIdentifierList.value = res.data.data || []
  } catch (error) {
    console.error('获取实体标识列表失败:', error)
  }
}

// 获取群组详情
const fetchGroupDetail = async () => {
  try {
    const res = await groupApi.getDetail(groupId)
    const data = res.data.data
    if (data) {
      // 填充基本信息
      basicForm.group_id = data.group_id
      basicForm.group_name = data.group_name
      basicForm.group_desc = data.group_desc || ''
      basicForm.trigger_type = data.trigger_type || 1
      basicForm.entity_identifier_id = data.entity_identifier_id || ''
      basicForm.trigger_cron = data.trigger_cron || ''
      basicForm.group_type = data.group_type || 1
      
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
      
      // 解析群组规则
      if (data.group_rule) {
        const parsedRule = typeof data.group_rule === 'string' 
          ? JSON.parse(data.group_rule) 
          : data.group_rule
        groupRule.value = parsedRule
        
        // 将后端 GroupRule 格式转换为前端 rule_groups 格式
        // 注意：这里假设后端存储的是前端原始格式
        // 如果后端存储的是 SelectorExpression 格式，需要另行转换
        if (parsedRule?.expression) {
          // 后端 SelectorExpression 格式，需要转换
          ruleForm.rule_groups = convertSelectorExpressionToRuleGroups(parsedRule.expression)
        } else if (Array.isArray(parsedRule?.rule_groups)) {
          // 前端原始格式，直接使用
          ruleForm.rule_groups = parsedRule.rule_groups
        }
      }
    }
  } catch (error) {
    console.error('获取群组详情失败:', error)
    ElMessage.error('获取群组详情失败')
  }
}

/**
 * 将后端 SelectorExpression 转换为前端 RuleGroups 格式
 * 这是一个简化的转换，实际使用时可能需要根据具体业务逻辑调整
 */
const convertSelectorExpressionToRuleGroups = (expression: any): any[] => {
  if (!expression?.expression || !Array.isArray(expression.expression)) {
    return []
  }
  
  return expression.expression.map((group: any, index: number) => ({
    id: Date.now() + index,
    expanded: true,
    inner_logic: group.logic || 'AND',
    logic: index === 0 ? 'AND' : (expression.logic || 'OR'),
    rules: (group.conditions || []).map((condition: any, rIndex: number) => ({
      id: Date.now() + index * 1000 + rIndex,
      rule_type: mapConditionTypeToRuleType(condition.type),
      // 其他字段根据具体业务需求填充
      ...extractConditionData(condition)
    }))
  }))
}

/**
 * 将后端 condition type 映射为前端 rule_type
 */
const mapConditionTypeToRuleType = (type?: string): string => {
  switch (type) {
    case 'profile':
    case 'not_profile':
      return 'tag'
    case 'event':
    case 'not_event':
      return 'event'
    case 'event_sequence':
    case 'not_event_sequence':
      return 'sequence'
    default:
      return 'tag'
  }
}

/**
 * 提取 condition 中的数据
 */
const extractConditionData = (condition: any): any => {
  const data: any = {}
  
  // 根据 condition 类型提取不同字段
  if (condition.measure) {
    // 标签规则
    data.tag_id = condition.measure.id
    data.operator = condition.measure.op
    data.value = condition.measure.values?.[0] || ''
  }
  
  if (condition.event) {
    data.event_code = condition.event.eventId
  }
  
  if (condition.period?.timestamp) {
    data.time_range = [
      new Date(condition.period.timestamp[0]),
      new Date(condition.period.timestamp[1])
    ]
  }
  
  return data
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

// 生效日期类型变化处理
const handleEffectiveTypeChange = () => {
  if (basicForm.effective_type === 1) {
    basicForm.effective_date_range = []
  }
}

// 获取触发开始时间
const getTriggerStartTime = () => {
  if (basicForm.effective_type === 1) {
    return formatDate(new Date())
  } else {
    return basicForm.effective_date_range?.[0] || formatDate(new Date())
  }
}

// 获取触发结束时间
const getTriggerEndTime = () => {
  if (basicForm.effective_type === 1) {
    return '9999-12-31'
  } else {
    return basicForm.effective_date_range?.[1] || formatDate(new Date())
  }
}

// 格式化日期
const formatDate = (date: Date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 保存
const handleSave = async () => {
  // 验证基本信息
  if (!basicFormRef.value) return
  const basicValid = await basicFormRef.value.validate().catch(() => false)
  if (!basicValid) {
    basicInfoExpanded.value = true
    return
  }

  // 验证规则配置（只有规则筛选类型需要验证）
  if (canEditRule.value) {
    const ruleValid = ruleConfigRef.value?.validate()
    if (!ruleValid) {
      ElMessage.warning('请完善群组规则')
      return
    }
  }

  saving.value = true
  try {
    // 获取后端格式的 GroupRule 数据
    const groupRuleData = canEditRule.value 
      ? ruleConfigRef.value?.getGroupRule() 
      : groupRule.value

    const submitData = {
      group_id: basicForm.group_id,
      group_name: basicForm.group_name,
      group_desc: basicForm.group_desc,
      entity_identifier_id: basicForm.entity_identifier_id,
      trigger_type: basicForm.trigger_type,
      trigger_cron: basicForm.trigger_type === 2 ? basicForm.trigger_cron : undefined,
      trigger_start_time: getTriggerStartTime(),
      trigger_end_time: getTriggerEndTime(),
      group_rule: groupRuleData,
      group_type: basicForm.group_type,
      source_type: 2
    }

    await groupApi.save(submitData)
    ElMessage.success('保存成功')
    router.push('/group/filter')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchEntityIdentifierList()
  fetchGroupDetail()
})
</script>

<style scoped lang="scss">
.edit-group-rule-page {
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

      &.collapsed {
        transform: rotate(-90deg);
      }
    }

    .help-icon {
      margin-left: 8px;
      color: #909399;
      cursor: pointer;

      &:hover {
        color: #606266;
      }
    }
  }

  .section-content {
    padding: 0 20px 20px;
  }
}

.rule-section {
  .rule-header {
    cursor: default;

    .rule-tip {
      margin-left: 16px;
      font-size: 13px;
      color: #909399;
    }
  }
}

.basic-form {
  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }

  .schedule-tip {
    margin-left: 100px;
    margin-top: 8px;
    padding: 8px 12px;
    background-color: #fdf6ec;
    border-radius: 4px;
    color: #e6a23c;
  }

  .help-icon {
    margin-left: 8px;
    color: #909399;
    cursor: pointer;
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
