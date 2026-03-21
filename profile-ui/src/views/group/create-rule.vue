<template>
  <div class="create-group-rule-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h2 class="page-title">规则创建</h2>
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

            <el-form-item label="计算周期" prop="calc_period" required>
              <el-radio-group v-model="basicForm.calc_period">
                <el-radio :label="1">每日例行</el-radio>
                <el-radio :label="2">手动更新</el-radio>
              </el-radio-group>
              <el-tooltip content="计算周期说明" placement="top">
                <el-icon class="help-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
              <div v-if="basicForm.calc_period === 1" class="form-tip">
                提示：30天以上未使用的例行分群将会自动调整为手动更新
              </div>
            </el-form-item>

            <el-form-item label="分析主体" prop="entity_identifier_id" required>
              <el-select
                v-model="basicForm.entity_identifier_id"
                placeholder="请选择分析主体"
                style="width: 500px"
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowDown, Refresh, Menu, Setting, QuestionFilled } from '@element-plus/icons-vue'
import { groupApi } from '@/api/group'
import { entityIdentifierApi } from '@/api/entity'
import type { FormInstance } from 'element-plus'
import GroupRuleConfig from './components/GroupRuleConfig.vue'

const router = useRouter()

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
  group_name: '',
  group_desc: '',
  calc_period: 2,  // 1-每日例行, 2-手动更新
  calc_time_type: 1,
  calc_hour: 0,
  calc_minute: 0,
  entity_identifier_id: ''
})

// 基本信息验证规则
const basicRules = {
  group_name: [
    { required: true, message: '请输入分群名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  calc_period: [
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
    const submitData = {
      group_name: basicForm.group_name,
      group_desc: basicForm.group_desc,
      entity_identifier_id: basicForm.entity_identifier_id,
      calc_period: basicForm.calc_period,
      calc_time_type: basicForm.calc_time_type,
      calc_hour: basicForm.calc_hour,
      calc_minute: basicForm.calc_minute,
      group_rule: JSON.stringify(ruleForm.rule_groups),
      group_type: 3,
      source_type: 2
    }

    await groupApi.save(submitData)
    ElMessage.success('创建成功')
    router.push('/group/detail')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchEntityIdentifierList()
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
