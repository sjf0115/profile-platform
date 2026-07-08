<template>
  <div class="create-dataset-page">
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h2 class="page-title">{{ isEditMode ? '编辑标签数据集' : '创建标签数据集' }}</h2>
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

          <el-form-item label="数据库" required>
            <el-select
              v-model="formData.database"
              placeholder="请选择数据库"
              clearable
              style="width: 300px"
              :disabled="!formData.datasource_id"
              @change="handleDatabaseChange"
            >
              <el-option
                v-for="(item, index) in databaseList"
                :key="index"
                :label="item.name"
                :value="item.name"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="数据表" required>
            <el-select
              v-model="formData.table_name"
              placeholder="请选择数据表"
              clearable
              style="width: 300px"
              :disabled="!formData.database"
              @change="handleTableChange"
            >
              <el-option
                v-for="(item, index) in tableList"
                :key="index"
                :label="item.name + (item.comment ? ' (' + item.comment + ')' : '')"
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
                placeholder="请选择时间分区字段"
                clearable
                style="width: 300px"
              >
                <el-option
                  v-for="(field, index) in fieldList"
                  :key="index"
                  :label="field.field_name"
                  :value="field.field_name"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="分区值格式" required>
              <el-select
                v-model="formData.partition_format"
                placeholder="请选择分区值格式"
                style="width: 300px"
              >
                <el-option label="${yyyyMMdd}" value="${yyyyMMdd}" />
                <el-option label="${yyyy-MM-dd}" value="${yyyy-MM-dd}" />
                <el-option label="${yyyy/MM/dd}" value="${yyyy/MM/dd}" />
                <el-option label="${yyyyMM}" value="${yyyyMM}" />
                <el-option label="${yyyy-MM}" value="${yyyy-MM}" />
                <el-option label="${yyyy}" value="${yyyy}" />
              </el-select>
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

          <el-form-item required>
            <template #label>
              <span>实体配置</span>
            </template>
            <div class="entity-config-row">
              <el-select
                v-model="formData.entity_id"
                placeholder="请选择实体标识"
                clearable
                style="width: 220px"
                @change="handleEntityIdentifierChange"
              >
                <el-option
                  v-for="item in entityIdentifierList"
                  :key="item.entity_identifier_id"
                  :label="item.entity_name + '>' + item.entity_identifier_name"
                  :value="item.entity_identifier_id"
                />
              </el-select>
              <el-select
                v-model="formData.entity_field"
                placeholder="请选择实体字段"
                clearable
                style="width: 180px"
                :disabled="!formData.entity_id"
              >
                <el-option
                  v-for="(field, index) in fieldList"
                  :key="index"
                  :label="field.field_name"
                  :value="field.field_name"
                />
              </el-select>
            </div>
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
            <el-table-column prop="field_name" label="字段名" min-width="140" show-overflow-tooltip />
            <el-table-column prop="field_desc" label="字段描述" min-width="180">
              <template #default="{ row }">
                <el-input 
                  v-model="row.field_desc" 
                  placeholder="请输入字段描述"
                  size="small"
                />
              </template>
            </el-table-column>
            <el-table-column prop="field_type" label="字段类型" width="100" align="center" />
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
            <el-table-column label="关联标签" min-width="180">
              <template #default="{ row }">
                <el-select
                  v-model="row.related_id"
                  placeholder="请选择关联标签"
                  clearable
                  size="small"
                  style="width: 100%"
                  :disabled="!formData.entity_id || labelList.length === 0"
                >
                  <el-option
                    v-for="label in getAvailableLabels(row.related_id)"
                    :key="label.label_id"
                    :label="label.label_name"
                    :value="label.label_id"
                  />
                </el-select>
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
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, QuestionFilled, Search, InfoFilled } from '@element-plus/icons-vue'
import type { Dataset } from '@/types'
import { datasetApi } from '@/api/dataset'
import { dataSourceApi } from '@/api/datasource'
import { entityIdentifierApi, type EntityIdentifier } from '@/api/entity'
import { labelApi } from '@/api/label'

const route = useRoute()
const router = useRouter()

// 判断是否为编辑模式
const isEditMode = computed(() => !!route.params.id)
const datasetId = computed(() => route.params.id as string)

// 当前步骤
const activeStep = ref(0)

// 数据源列表
const datasourceList = ref<any[]>([])

// 数据库列表
const databaseList = ref<any[]>([])

// 数据表列表
const tableList = ref<any[]>([])

// 字段列表（使用后端字段命名 - 下划线命名）
interface DatasetFieldItem {
  field_name: string
  field_desc?: string
  field_type?: string
  import_status?: number
  field_status?: number
  related_id?: string
}
const fieldList = ref<DatasetFieldItem[]>([])

// 字段搜索
const fieldSearch = ref('')

// 实体标识列表
const entityIdentifierList = ref<EntityIdentifier[]>([])

// 标签列表（可用标签 - 未被其他数据集绑定）
const labelList = ref<any[]>([])

// 已选择的标签ID列表（用于过滤下拉选项，防止重复选择）
const selectedLabelIds = computed(() => {
  return fieldList.value
    .filter(field => field.related_id)
    .map(field => field.related_id as string)
})

// 过滤后的可用标签列表（排除当前行已选择的标签）
const getAvailableLabels = (currentRowRelatedId?: string) => {
  return labelList.value.filter(label => {
    // 当前行已选择的标签保留（允许保持原选择）
    if (currentRowRelatedId && label.label_id === currentRowRelatedId) {
      return true
    }
    // 排除已被其他字段选择的标签
    return !selectedLabelIds.value.includes(label.label_id)
  })
}

// 表单数据
const formData = reactive({
  datasource_id: '',
  database: '',
  table_name: '',
  dataset_name: '',
  dataset_desc: '',
  has_partition: 0,
  partition_field: '',
  partition_format: '${yyyyMMdd}',
  entity_field: '',
  entity_id: '',
  fields: [] as DatasetFieldItem[]
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
  if (!formData.datasource_id || !formData.database || !formData.table_name) return false
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

// 获取数据库列表
const fetchDatabaseList = async (datasourceId: string) => {
  try {
    const res = await dataSourceApi.getDatabases(datasourceId)
    console.log('数据库列表响应:', res)
    databaseList.value = res.data.data || []
  } catch (error) {
    console.error('获取数据库列表失败:', error)
    ElMessage.error('获取数据库列表失败')
  }
}

// 获取数据表列表
const fetchTableList = async (datasourceId: string, database: string) => {
  try {
    const res = await dataSourceApi.getTables(datasourceId, database)
    console.log('数据表列表响应:', res)
    tableList.value = res.data.data || []
  } catch (error) {
    console.error('获取数据表列表失败:', error)
    ElMessage.error('获取数据表列表失败')
  }
}

// 获取数据表字段列表
const fetchColumnList = async (datasourceId: string, database: string, table: string) => {
  try {
    const res = await dataSourceApi.getColumns(datasourceId, database, table)
    console.log('字段列表响应:', res)
    const columnInfo = res.data.data
    if (columnInfo && columnInfo.columns) {
      fieldList.value = columnInfo.columns.map((col: any) => ({
        field_name: col.name,
        field_desc: col.comment || '',
        field_type: col.type,
        import_status: 1,
        field_status: 1,  // 1-新增字段
        related_id: undefined  // 关联标签ID
      }))
      console.log('字段列表数据:', fieldList.value)
    } else {
      fieldList.value = []
    }
  } catch (error) {
    console.error('获取字段列表失败:', error)
    ElMessage.error('获取字段列表失败')
    fieldList.value = []
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

// 获取未绑定数据集的标签
const fetchLabelList = async (entityIdentifierId: string) => {
  try {
    // 编辑模式传入 datasetId，创建模式传 undefined
    const res = await labelApi.getUnbound(entityIdentifierId, isEditMode.value ? datasetId.value : undefined)
    console.log('未绑定标签列表响应:', res)
    labelList.value = res.data.data || []
  } catch (error) {
    console.error('获取未绑定标签列表失败:', error)
    labelList.value = []
  }
}

// 实体标识变更
const handleEntityIdentifierChange = (entityIdentifierId: string) => {
  // 清空已选择的实体字段和字段中的关联标签
  formData.entity_field = ''
  fieldList.value.forEach(field => {
    field.related_id = undefined
  })
  // 如果选择了实体标识，获取对应的标签列表
  if (entityIdentifierId) {
    fetchLabelList(entityIdentifierId)
  } else {
    labelList.value = []
  }
}

// 数据源变更
const handleDatasourceChange = (datasourceId: string) => {
  formData.database = ''
  formData.table_name = ''
  fieldList.value = []
  databaseList.value = []
  tableList.value = []
  if (!datasourceId) return
  fetchDatabaseList(datasourceId)
}

// 数据库变更
const handleDatabaseChange = (database: string) => {
  formData.table_name = ''
  fieldList.value = []
  tableList.value = []
  if (!database || !formData.datasource_id) return
  fetchTableList(formData.datasource_id, database)
}

// 数据表变更
const handleTableChange = (tableName: string) => {
  fieldList.value = []
  if (!tableName || !formData.datasource_id || !formData.database) return
  
  // 调用接口获取字段列表
  fetchColumnList(formData.datasource_id, formData.database, tableName)
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
    // 组合 database 和 table_name
    const fullTableName = formData.database ? `${formData.database}.${formData.table_name}` : formData.table_name
    
    const submitData: Partial<Dataset> = {
      dataset_name: formData.dataset_name,
      dataset_desc: formData.dataset_desc,
      dataset_type: 1, // 标签数据集
      datasource_id: formData.datasource_id,
      table_name: fullTableName,
      partition_field: formData.has_partition === 1 ? formData.partition_field : undefined,
      partition_format: formData.has_partition === 1 ? formData.partition_format : undefined,
      entity_field: formData.entity_field,
      entity_id: formData.entity_id,
      fields: fieldList.value as any
    }
    
    // 编辑模式添加 dataset_id
    if (isEditMode.value) {
      submitData.dataset_id = datasetId.value
    }

    await datasetApi.save(submitData as Dataset)
    ElMessage.success(isEditMode.value ? '编辑成功' : '创建成功')
    router.push('/project/dataset')
  } catch (error) {
    console.error('创建失败:', error)
    // 错误已在响应拦截器中处理，显示错误消息
  }
}

// 加载数据集详情（编辑模式）
const fetchDatasetDetail = async () => {
  if (!isEditMode.value) return
  
  try {
    const res = await datasetApi.detail(datasetId.value)
    console.log('数据集详情:', res)
    const dataset = res.data.data
    
    if (dataset) {
      // 填充表单数据
      formData.datasource_id = dataset.datasource_id || ''
      
      // 解析 table_name，分离 database 和 table
      const tableName = dataset.table_name || ''
      if (tableName.includes('.')) {
        const parts = tableName.split('.')
        formData.database = parts[0]
        formData.table_name = parts[1]
      } else {
        formData.database = ''
        formData.table_name = tableName
      }
      
      formData.dataset_name = dataset.dataset_name || ''
      formData.dataset_desc = dataset.dataset_desc || ''
      formData.has_partition = dataset.partition_field ? 1 : 0
      formData.partition_field = dataset.partition_field || ''
      formData.partition_format = dataset.partition_format || '${yyyyMMdd}'
      formData.entity_field = dataset.entity_field || ''
      formData.entity_id = dataset.entity_id || ''
      
      // 先获取标签列表（确保下拉选项在字段填充前可用）
      if (formData.entity_id) {
        await fetchLabelList(formData.entity_id)
      }
      
      // 填充字段列表
      if (dataset.fields && dataset.fields.length > 0) {
        fieldList.value = dataset.fields.map((field: any) => ({
          field_name: field.field_name,
          field_desc: field.field_desc || '',
          field_type: field.field_type,
          import_status: field.import_status,
          field_status: field.field_status,
          related_id: field.related_id
        }))
      }
      
      // 编辑模式直接跳到第二步
      activeStep.value = 1
    }
  } catch (error) {
    console.error('获取数据集详情失败:', error)
    ElMessage.error('获取数据集详情失败')
  }
}

onMounted(() => {
  fetchDatasourceList()
  fetchEntityIdentifierList()
  fetchDatasetDetail()
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

.entity-config-row {
  display: flex;
  gap: 12px;
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
