<template>
  <div class="create-group-rule-page">
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
            <el-form-item label="分群名称" prop="group_name" required>
              <el-input
                v-model="basicForm.group_name"
                placeholder="请输入分群名称"
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
              >
                <el-option
                  v-for="item in entityIdentifierList"
                  :key="item.entity_identifier_id"
                  :label="`${item.entity_name} > ${item.entity_identifier_name}`"
                  :value="item.entity_identifier_id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="分群描述">
              <el-input
                v-model="basicForm.group_desc"
                placeholder="请输入分群描述"
                style="width: 500px"
              />
            </el-form-item>
          </el-form>
        </div>
      </div>

      <!-- 创建规则区域 -->
      <div class="section rule-section">
        <div class="section-header rule-header">
          <el-icon class="section-icon"><Setting /></el-icon>
          <span class="section-title">创建规则</span>
          <span class="rule-tip">符合下列条件的用户将创建分群，每条规则计算结果最长支持连续365天</span>
          <el-tooltip content="规则说明" placement="top">
            <el-icon class="help-icon"><QuestionFilled /></el-icon>
          </el-tooltip>
          <div class="estimate-area">
            <el-button link type="primary" @click="handleEstimate" :loading="estimating">
              <el-icon><Refresh /></el-icon>
              预估人数
            </el-button>
            <span class="estimate-count">{{ estimatedCount }}</span>
          </div>
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
        <el-button type="primary" @click="handleSave">保存</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowDown, Refresh, Menu, Setting, QuestionFilled } from '@element-plus/icons-vue'
import { groupApi } from '@/api/group'
import { entityIdentifierApi } from '@/api/entity'
import type { FormInstance } from 'element-plus'
import GroupRuleConfig from './components/GroupRuleConfig.vue'

const router = useRouter()
const route = useRoute()

// 编辑模式判断
const groupId = computed(() => route.params.id as string | undefined)
const isEdit = computed(() => !!groupId.value)
const pageTitle = computed(() => isEdit.value ? '编辑群组' : '规则创建')

// 区域展开状态
const basicInfoExpanded = ref(true)
const basicFormRef = ref<FormInstance>()
const ruleConfigRef = ref()

// 预估人数
const estimatedCount = ref(0)
const estimating = ref(false)

// 预估人数方法
const handleEstimate = async () => {
  estimating.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    estimatedCount.value = Math.floor(Math.random() * 10000)
  } finally {
    estimating.value = false
  }
}

// 实体标识列表
const entityIdentifierList = ref<any[]>([])

// 基本信息表单
const basicForm = reactive({
  group_id: '',
  group_name: '',
  group_desc: '',
  trigger_type: 1,  // 1-手动触发调度, 2-周期调度, 3-API触发调度
  schedule_cycle: 'day',  // 调度周期：day
  calc_time: '',  // 计算时间 HH:mm（不传递后端，用于生成 Cron）
  trigger_cron: '',  // Cron 表达式（对应后端 triggerCron）
  effective_type: 1,  // 1-永久生效, 2-指定日期
  effective_date_range: [] as string[],  // 生效日期范围
  entity_identifier_id: ''
})

// 更新 Cron 表达式
const updateCronExpression = () => {
  if (basicForm.calc_time) {
    const [hour, minute] = basicForm.calc_time.split(':')
    // 日调度的 Cron 表达式: 秒 分 时 日 月 周
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
    // 永久生效：当前日期
    return formatDate(new Date())
  } else {
    // 指定日期：用户选择的开始日期，默认当天
    return basicForm.effective_date_range?.[0] || formatDate(new Date())
  }
}

// 获取触发结束时间
const getTriggerEndTime = () => {
  if (basicForm.effective_type === 1) {
    // 永久生效：9999-12-31
    return '9999-12-31'
  } else {
    // 指定日期：用户选择的结束日期，默认当天
    return basicForm.effective_date_range?.[1] || formatDate(new Date())
  }
}

// 格式化日期为 YYYY-MM-DD
const formatDate = (date: Date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 基本信息验证规则
const basicRules = {
  group_name: [
    { required: true, message: '请输入分群名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  trigger_type: [
    { required: true, message: '请选择计算周期', trigger: 'change' }
  ],
  entity_identifier_id: [
    { required: true, message: '请选择分析主体', trigger: 'change' }
  ]
}

// 规则表单
const ruleForm = reactive<{ rule_groups?: any[] }>({
  rule_groups: []
})

// 是否显示预估人数（有规则时才显示）
const showEstimate = computed(() => {
  const groups = ruleForm.rule_groups || []
  return groups.some(group => group.rules && group.rules.length > 0)
})

// 获取实体标识列表
const fetchEntityIdentifierList = async () => {
  try {
    const res = await entityIdentifierApi.list()
    entityIdentifierList.value = res.data.data || []
  } catch (error) {
    console.error('获取实体标识列表失败:', error)
  }
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

  // 验证规则配置
  const ruleValid = ruleConfigRef.value?.validate()
  if (!ruleValid) {
    ElMessage.warning('请完善群组规则')
    return
  }

  try {
    // 获取后端格式的 RuleExpression 数据
    const groupRule = ruleConfigRef.value?.getGroupRule()

    const submitData: any = {
      group_name: basicForm.group_name,
      group_desc: basicForm.group_desc,
      entity_identifier_id: basicForm.entity_identifier_id,
      trigger_type: basicForm.trigger_type,
      trigger_cron: basicForm.trigger_type === 2 ? basicForm.trigger_cron : undefined,
      trigger_start_time: getTriggerStartTime(),
      trigger_end_time: getTriggerEndTime(),
      group_rule: groupRule,
      group_type: 1,  // 1-规则筛选
      source_type: 2
    }

    // 编辑模式需要传递 group_id
    if (isEdit.value) {
      submitData.group_id = basicForm.group_id
    }

    await groupApi.save(submitData)
    ElMessage.success(isEdit.value ? '保存成功' : '创建成功')
    router.push('/group/filter')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
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

      // 解析群组规则，填充规则配置
      if (data.group_rule) {
        const parsedRule = typeof data.group_rule === 'string'
          ? JSON.parse(data.group_rule)
          : data.group_rule
        if (parsedRule?.expression?.rule_groups) {
          ruleForm.rule_groups = parsedRule.expression.rule_groups
        }
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
.create-group-rule-page {
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
    
    .estimate-area {
      margin-left: auto;
      display: flex;
      align-items: center;
      gap: 8px;
      
      .estimate-count {
        font-size: 14px;
        color: #409eff;
        font-weight: 500;
      }
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
