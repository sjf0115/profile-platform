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
                v-for="(item, index) in datasourceList"
                :key="index"
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
                v-for="(item, index) in tableList"
                :key="index"
                :label="item.table_name + (item.table_comment ? ' (' + item.table_comment + ')' : '')"
                :value="item.table_name"
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
              <el-input
                v-model="formData.partition_field"
                placeholder="请输入时间分区字段"
                clearable
                style="width: 300px"
              />
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

          <div class="section-title">实体标识配置</div>

          <el-form-item label="实体字段" required>
            <el-select
              v-model="formData.entity_field"
              placeholder="请选择实体字段"
              clearable
              style="width: 300px"
            >
              <el-option
                v-for="(field, index) in fieldList"
                :key="index"
                :label="field.name"
                :value="field.name"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="实体标识" required>
            <el-select
              v-model="formData.entity_id"
              placeholder="请选择实体标识"
              clearable
              style="width: 300px"
            >
              <el-option
                v-for="item in entityIdentifierList"
                :key="item.entity_identifier_id"
                :label="item.entity_name + '>' + item.entity_identifier_name"
                :value="item.entity_identifier_id"
              />
            </el-select>
          </el-form-item>

          <div class="section-title">
            字段列表
            <span class="field-count">共 {{ filteredFieldList.length }} 个字段</span>
            <el-input
              v-model="fieldSearch"
              placeholder="请输入字段名"
              clearable
              style="width: 250px; float: right;"
              :prefix-icon="Search"
            />
          </div>

          <el-table 
            :data="filteredFieldList" 
            border 
            style="width: 100%"
            max-height="400"
          >
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="name" label="字段名" min-width="140" show-overflow-tooltip />
            <el-table-column prop="alias" label="字段描述" min-width="180">
              <template #default="{ row }">
                <el-input 
                  v-model="row.alias" 
                  placeholder="请输入字段描述"
                  size="small"
                />
              </template>
            </el-table-column>
            <el-table-column prop="column_type" label="字段类型" width="100" align="center" />
            <el-table-column label="是否导入" width="90" align="center">
              <template #default="{ row }">
                <el-switch 
                  v-model="row.import_status" 
                  :active-value="1" 
                  :inactive-value="2"
                  size="small"
                />
              </template>
            </el-table-column>
          </el-table>

          <div class="field-tips">
            <el-icon><InfoFilled /></el-icon>
            <span>提示：实体字段必须选择为导入状态，否则无法完成创建</span>
          </div>

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
import { ArrowLeft, QuestionFilled, Search, InfoFilled } from '@element-plus/icons-vue'
import type { Dataset, DatasetField } from '@/types'
import { datasetApi } from '@/api/dataset'
import { entityIdentifierApi, type EntityIdentifier } from '@/api/entity'

const router = useRouter()

// 当前步骤
const activeStep = ref(0)

// 数据源列表
const datasourceList = ref<any[]>([])

// 数据表列表
interface TableItem {
  table_name: string
  table_comment?: string
  is_partition_table?: boolean
  columns?: any[]  // 表字段列表
}
const tableList = ref<TableItem[]>([])

// 字段列表
const fieldList = ref<DatasetField[]>([])

// 字段搜索
const fieldSearch = ref('')

// 实体标识列表
const entityIdentifierList = ref<EntityIdentifier[]>([])

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

// 过滤后的字段列表
const filteredFieldList = computed(() => {
  if (!fieldSearch.value) return fieldList.value
  const keyword = fieldSearch.value.toLowerCase()
  return fieldList.value.filter(field => 
    field.name?.toLowerCase().includes(keyword) ||
    field.alias?.toLowerCase().includes(keyword)
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
    const res = await datasetApi.getDatasources(1) // dataset_type = 1 标签数据集
    console.log('数据源列表响应:', res)
    datasourceList.value = res.data.data || []
    console.log('数据源列表数据:', datasourceList.value)
  } catch (error) {
    console.error('获取数据源列表失败:', error)
    ElMessage.error('获取数据源列表失败')
  }
}

// 获取数据表列表
const fetchTableList = async (datasourceId: string) => {
  try {
    const res = await datasetApi.getTables(datasourceId)
    console.log('数据表列表响应:', res)
    tableList.value = res.data.data || []
  } catch (error) {
    console.error('获取数据表列表失败:', error)
    ElMessage.error('获取数据表列表失败')
  }
}

// 获取实体标识列表
const fetchEntityIdentifierList = async () => {
  try {
    const res = await entityIdentifierApi.list()
    console.log('实体标识列表响应:', res)
    entityIdentifierList.value = res.data.data || []
  } catch (error) {
    console.error('获取实体标识列表失败:', error)
  }
}

// 数据源变更
const handleDatasourceChange = (datasourceId: string) => {
  formData.table_name = ''
  fieldList.value = []
  tableList.value = []
  if (!datasourceId) return
  fetchTableList(datasourceId)
}

// 数据表变更
const handleTableChange = (tableName: string) => {
  if (!tableName || !formData.datasource_id) return
  
  // 从已加载的表列表中找到选中的表，获取其 columns 字段
  const selectedTable = tableList.value.find(t => t.table_name === tableName)
  if (selectedTable && selectedTable.columns) {
    fieldList.value = selectedTable.columns.map((col: any) => ({
      name: col.column_name,
      alias: col.column_comment || '',
      column_type: col.column_type,
      import_status: 1,
      category: undefined  // 实体ID类型默认为空，需要用户选择
    }))
    console.log('字段列表数据:', fieldList.value)
  } else {
    fieldList.value = []
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
    ElMessage.warning('请选择实体字段')
    return
  }
  if (!formData.entity_id) {
    ElMessage.warning('请选择实体类型')
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
      fields: fieldList.value.filter(f => f.import_status === 1)
    }

    await datasetApi.save(submitData as Dataset)
    ElMessage.success('创建成功')
    router.push('/project/dataset')
  } catch (error) {
    console.error('创建失败:', error)
    // 错误已在响应拦截器中处理，显示错误消息
  }
}

onMounted(() => {
  fetchDatasourceList()
  fetchEntityIdentifierList()
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
  
  .field-count {
    font-size: 13px;
    font-weight: normal;
    color: #909399;
    margin-left: 10px;
  }
}

.field-tips {
  margin-top: 12px;
  padding: 10px 15px;
  background-color: #f4f4f5;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
  
  .el-icon {
    color: #909399;
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
