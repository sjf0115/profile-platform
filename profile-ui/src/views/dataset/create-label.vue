<template>
  <div class="create-dataset-page">
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h2 class="page-title">创建标签数据集</h2>
      </div>
    </div>

    <div class="page-content">
      <el-steps :active="activeStep" finish-status="success" class="steps">
        <el-step title="选择来源表" />
        <el-step title="字段配置" />
      </el-steps>

      <!-- 第一步：选择来源表 -->
      <div v-if="activeStep === 0" class="step-content">
        <el-form :model="formData" label-width="120px" class="source-form">
          <div class="section-title">选择来源表</div>
          
          <el-form-item label="数据源" required>
            <el-select
              v-model="formData.datasource_id"
              placeholder="请选择数据源"
              clearable
              style="width: 300px"
              @change="handleDatasourceChange"
            >
              <el-option
                v-for="item in datasourceList"
                :key="item.datasource_id"
                :label="item.datasource_name"
                :value="item.datasource_id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="数据表" required>
            <el-select
              v-model="formData.table_name"
              placeholder="请选择数据表"
              clearable
              style="width: 300px"
              :disabled="!formData.datasource_id"
              @change="handleTableChange"
            >
              <el-option
                v-for="item in tableList"
                :key="item.name"
                :label="item.name"
                :value="item.name"
              />
            </el-select>
          </el-form-item>

          <div class="section-title">分区及更新方式设置</div>

          <el-form-item label="时间分区">
            <el-radio-group v-model="formData.has_partition">
              <el-radio :label="1">是</el-radio>
              <el-radio :label="0">否</el-radio>
            </el-radio-group>
          </el-form-item>

          <template v-if="formData.has_partition === 1">
            <el-form-item label="时间分区字段" required>
              <el-select
                v-model="formData.partition_field"
                placeholder="请选择分区字段"
                clearable
                style="width: 300px"
              >
                <el-option
                  v-for="field in partitionFieldOptions"
                  :key="field"
                  :label="field"
                  :value="field"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="分区值格式" required>
              <el-input
                v-model="formData.partition_format"
                placeholder="请输入分区值格式，如：${yyyyMMdd}"
                style="width: 300px"
              />
              <el-tooltip content="支持的时间格式：yyyy(年)、MM(月)、dd(日)、HH(时)、mm(分)、ss(秒)" placement="top">
                <el-icon class="help-icon"><QuestionFilled /></el-icon>
              </el-tooltip>
            </el-form-item>
          </template>

          <el-form-item>
            <el-button type="primary" @click="handleNext" :disabled="!canGoNext">下一步</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 第二步：字段配置 -->
      <div v-if="activeStep === 1" class="step-content">
        <el-form :model="formData" label-width="120px">
          <el-form-item label="数据集名称" required>
            <el-input
              v-model="formData.dataset_name"
              placeholder="请输入数据集名称"
              style="width: 400px"
            />
          </el-form-item>

          <el-form-item label="数据集描述">
            <el-input
              v-model="formData.dataset_desc"
              type="textarea"
              :rows="3"
              placeholder="请输入数据集描述"
              style="width: 400px"
            />
          </el-form-item>

          <div class="section-title">用户标识配置</div>

          <el-form-item label="用户标识字段" required>
            <el-select
              v-model="formData.entity_field"
              placeholder="请选择用户标识字段"
              clearable
              style="width: 300px"
            >
              <el-option
                v-for="field in fieldList"
                :key="field.field_name"
                :label="field.field_name"
                :value="field.field_name"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="用户标识类型" required>
            <el-select
              v-model="formData.entity_id"
              placeholder="请选择用户标识类型"
              clearable
              style="width: 300px"
            >
              <el-option label="OneID" value="one_id" />
              <el-option label="手机号" value="mobile" />
              <el-option label="邮箱" value="email" />
              <el-option label="设备ID" value="device_id" />
              <el-option label="会员ID" value="member_id" />
            </el-select>
          </el-form-item>

          <div class="section-title">
            字段列表
            <el-input
              v-model="fieldSearch"
              placeholder="请输入标签别名或字段名"
              clearable
              style="width: 250px; float: right;"
              :prefix-icon="Search"
            />
          </div>

          <el-table :data="filteredFieldList" border style="width: 100%">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="field_name" label="字段名" min-width="120" />
            <el-table-column prop="field_desc" label="字段描述" min-width="150">
              <template #default="{ row }">
                <el-input v-model="row.field_desc" placeholder="请输入字段描述" />
              </template>
            </el-table-column>
            <el-table-column prop="field_type" label="字段类型" width="100" />
            <el-table-column label="是否导入" width="80" align="center">
              <template #default="{ row }">
                <el-switch v-model="row.is_import" :active-value="1" :inactive-value="0" />
              </template>
            </el-table-column>
            <el-table-column label="实体ID类型" min-width="150">
              <template #default="{ row }">
                <el-select
                  v-model="row.id_type"
                  placeholder="请选择"
                  clearable
                  style="width: 100%"
                  :disabled="row.field_name !== formData.entity_field"
                >
                  <el-option label="OneID" value="one_id" />
                  <el-option label="手机号" value="mobile" />
                  <el-option label="邮箱" value="email" />
                  <el-option label="设备ID" value="device_id" />
                  <el-option label="会员ID" value="member_id" />
                </el-select>
              </template>
            </el-table-column>
          </el-table>

          <el-form-item class="form-actions">
            <el-button @click="handlePrev">上一步</el-button>
            <el-button type="primary" @click="handleSubmit">完成</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, QuestionFilled, Search } from '@element-plus/icons-vue'
import type { Dataset, DatasetField } from '@/types'
import { datasetApi } from '@/api/dataset'
import { dataSourceApi } from '@/api/datasource'

const router = useRouter()

// 当前步骤
const activeStep = ref(0)

// 数据源列表
const datasourceList = ref<any[]>([])

// 数据表列表
const tableList = ref<any[]>([])

// 字段列表
const fieldList = ref<DatasetField[]>([])

// 字段搜索
const fieldSearch = ref('')

// 表单数据
const formData = reactive({
  datasource_id: '',
  table_name: '',
  dataset_name: '',
  dataset_desc: '',
  has_partition: 0,
  partition_field: '',
  partition_format: '${yyyyMMdd}',
  entity_field: '',
  entity_id: '',
  fields: [] as DatasetField[]
})

// 分区字段选项（通常是日期/时间类型的字段）
const partitionFieldOptions = computed(() => {
  return fieldList.value
    .filter(f => {
      const type = f.field_type?.toLowerCase() || ''
      return type.includes('date') || type.includes('time') || f.field_name?.toLowerCase().includes('date') || f.field_name?.toLowerCase().includes('time')
    })
    .map(f => f.field_name)
})

// 过滤后的字段列表
const filteredFieldList = computed(() => {
  if (!fieldSearch.value) return fieldList.value
  const keyword = fieldSearch.value.toLowerCase()
  return fieldList.value.filter(field => 
    field.field_name?.toLowerCase().includes(keyword) ||
    field.field_desc?.toLowerCase().includes(keyword)
  )
})

// 是否可以进入下一步
const canGoNext = computed(() => {
  if (!formData.datasource_id || !formData.table_name) return false
  if (formData.has_partition === 1) {
    return !!formData.partition_field && !!formData.partition_format
  }
  return true
})

// 获取数据源列表
const fetchDatasourceList = async () => {
  try {
    const res = await dataSourceApi.getList({
      page_num: 1,
      page_size: 1000
    })
    if (res.data.code === 200) {
      datasourceList.value = res.data.data || []
    }
  } catch (error) {
    console.error('获取数据源列表失败:', error)
  }
}

// 数据源变更
const handleDatasourceChange = async (datasourceId: string) => {
  formData.table_name = ''
  tableList.value = []
  if (!datasourceId) return
  
  try {
    const res = await datasetApi.getTables(datasourceId)
    if (res.data.code === 200) {
      tableList.value = res.data.data || []
    }
  } catch (error) {
    console.error('获取数据表列表失败:', error)
  }
}

// 数据表变更
const handleTableChange = async (tableName: string) => {
  if (!tableName || !formData.datasource_id) return
  
  try {
    const res = await datasetApi.getFields(formData.datasource_id, tableName)
    if (res.data.code === 200) {
      // 初始化字段列表
      fieldList.value = (res.data.data || []).map((field: any) => ({
        ...field,
        is_import: 1,
        field_desc: field.field_desc || '',
        id_type: ''
      }))
    }
  } catch (error) {
    console.error('获取字段列表失败:', error)
  }
}

// 下一步
const handleNext = () => {
  if (activeStep.value < 1) {
    activeStep.value++
  }
}

// 上一步
const handlePrev = () => {
  if (activeStep.value > 0) {
    activeStep.value--
  }
}

// 返回
const goBack = () => {
  router.push('/project/dataset')
}

// 提交
const handleSubmit = async () => {
  if (!formData.dataset_name) {
    ElMessage.warning('请输入数据集名称')
    return
  }
  if (!formData.entity_field) {
    ElMessage.warning('请选择用户标识字段')
    return
  }
  if (!formData.entity_id) {
    ElMessage.warning('请选择用户标识类型')
    return
  }

  try {
    const submitData: Partial<Dataset> = {
      dataset_name: formData.dataset_name,
      dataset_desc: formData.dataset_desc,
      dataset_type: 1, // 标签数据集
      datasource_id: formData.datasource_id,
      table_name: formData.table_name,
      partition_field: formData.has_partition === 1 ? formData.partition_field : undefined,
      partition_format: formData.has_partition === 1 ? formData.partition_format : undefined,
      entity_field: formData.entity_field,
      entity_id: formData.entity_id,
      fields: fieldList.value.filter(f => f.is_import === 1)
    }

    const res = await datasetApi.save(submitData as Dataset)
    if (res.data.code === 200) {
      ElMessage.success('创建成功')
      router.push('/project/dataset')
    }
  } catch (error) {
    console.error('创建失败:', error)
  }
}

onMounted(() => {
  fetchDatasourceList()
})
</script>

<style scoped lang="scss">
.create-dataset-page {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
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
  background-color: #fff;
  border-radius: 8px;
  padding: 30px;
  flex: 1;
  overflow: auto;
}

.steps {
  max-width: 600px;
  margin: 0 auto 40px;
}

.step-content {
  max-width: 800px;
  margin: 0 auto;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin: 30px 0 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
  
  &:first-child {
    margin-top: 0;
  }
}

.source-form {
  .el-form-item {
    margin-bottom: 24px;
  }
}

.help-icon {
  margin-left: 8px;
  color: #909399;
  cursor: help;
}

.form-actions {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style>
