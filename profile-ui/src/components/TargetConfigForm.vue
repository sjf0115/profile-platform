<template>
  <div class="target-config-form">
    <template v-if="showTitle">
      <h3 class="section-title">
        投递目标配置
        <span v-if="optional" class="optional-tag">可选</span>
      </h3>
    </template>

    <el-form-item label="数据源">
      <el-select
        v-model="localDatasourceId"
        placeholder="请选择数据源"
        clearable
        filterable
        style="width: 300px"
        @change="handleDatasourceChange"
      >
        <el-option
          v-for="ds in datasourceList"
          :key="ds.datasource_id"
          :label="ds.datasource_name"
          :value="ds.datasource_id"
        />
      </el-select>
    </el-form-item>

    <!-- 动态表单区域 -->
    <template v-if="pluginParams.length > 0">
      <template v-for="param in pluginParams" :key="param.field">
        <!-- select 类型 -->
        <el-form-item
          v-if="param.type === 'select'"
          :label="param.title"
        >
          <el-select
            v-model="dynamicFormData[param.field]"
            :placeholder="getPlaceholder(param)"
            clearable
            filterable
            :loading="loadingFields[param.field]"
            style="width: 300px"
            @change="handleFieldChange(param.field)"
          >
            <el-option
              v-for="opt in fieldOptions[param.field]"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>

        <!-- radio 类型 -->
        <el-form-item
          v-else-if="param.type === 'radio'"
          :label="param.title"
        >
          <el-radio-group v-model="dynamicFormData[param.field]">
            <el-radio
              v-for="opt in param.options"
              :key="opt.value"
              :label="opt.value"
            >{{ opt.label }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- input 类型 -->
        <el-form-item
          v-else-if="param.type === 'input'"
          :label="param.title"
        >
          <el-input
            v-model="dynamicFormData[param.field]"
            :placeholder="getPlaceholder(param)"
            style="width: 400px"
          />
        </el-form-item>
      </template>
    </template>

    <!-- 无需配置提示 -->
    <template v-else-if="localDatasourceId && pluginParamsLoaded">
      <el-alert
        title="该数据源类型无需额外配置"
        type="info"
        :closable="false"
        style="margin-left: 120px; max-width: 400px"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { DataSource, PluginParam, TableInfo, TableColumnInfo } from '@/types'
import { dataSourceApi } from '@/api/datasource'

interface Props {
  modelValue: Record<string, any>
  showTitle?: boolean
  optional?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showTitle: false,
  optional: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, any>]
}>()

// 数据源列表
const datasourceList = ref<DataSource[]>([])

// 动态表单状态
const pluginParams = ref<PluginParam[]>([])
const dynamicFormData = reactive<Record<string, any>>({})
const fieldOptions = reactive<Record<string, Array<{ label: string; value: any }>>>({})
const loadingFields = reactive<Record<string, boolean>>({})
const pluginParamsLoaded = ref(false)

// 本地数据源 ID（与 modelValue.datasource_id 同步）
const localDatasourceId = ref(props.modelValue?.datasource_id || '')

// 字段名约定加载器
const fieldLoaders: Record<string, (dsId: string, form: Record<string, any>) => Promise<Array<{ label: string; value: any }>>> = {
  'table_name': async (dsId) => {
    const res = await dataSourceApi.getTablesByDatasource(dsId)
    const tables = res.data.data || []
    return tables.map((tb: TableInfo) => ({
      label: tb.name + (tb.comment ? ` (${tb.comment})` : ''),
      value: tb.name,
    }))
  },
  'target_column': async (dsId, form) => {
    const tableName = form['table_name']
    if (!tableName) return []
    const res = await dataSourceApi.getColumnsByDatasource(dsId, tableName)
    const colInfo = res.data.data as TableColumnInfo
    if (!colInfo || !colInfo.columns) return []
    return colInfo.columns.map((col: any) => ({
      label: col.name + (col.comment ? ` (${col.comment})` : ''),
      value: col.name,
    }))
  },
}

// 获取 placeholder
const getPlaceholder = (param: PluginParam): string => {
  const props = param.props as any
  return props?.placeholder || `请选择${param.title}`
}

// 发射配置变更
const emitUpdate = () => {
  const config: Record<string, any> = {}
  if (localDatasourceId.value) {
    config.datasource_id = localDatasourceId.value
    Object.keys(dynamicFormData).forEach(key => {
      if (dynamicFormData[key] !== '' && dynamicFormData[key] !== undefined && dynamicFormData[key] !== null) {
        config[key] = dynamicFormData[key]
      }
    })
  }
  emit('update:modelValue', config)
}

// 数据源变更
const handleDatasourceChange = (datasourceId: string) => {
  // 清空动态表单
  Object.keys(dynamicFormData).forEach(key => delete dynamicFormData[key])
  Object.keys(fieldOptions).forEach(key => delete fieldOptions[key])
  pluginParams.value = []
  pluginParamsLoaded.value = false

  if (!datasourceId) {
    pluginParamsLoaded.value = true
    emitUpdate()
    return
  }

  loadPluginParams(datasourceId)
}

// 加载动态表单
const loadPluginParams = async (datasourceId: string) => {
  pluginParams.value = []
  pluginParamsLoaded.value = false
  Object.keys(dynamicFormData).forEach(key => delete dynamicFormData[key])
  Object.keys(fieldOptions).forEach(key => delete fieldOptions[key])

  try {
    const res = await dataSourceApi.getExportConfig(datasourceId)
    const jsonStr = res.data.data
    if (!jsonStr || jsonStr === '[]') {
      pluginParams.value = []
      pluginParamsLoaded.value = true
      emitUpdate()
      return
    }
    const params: PluginParam[] = JSON.parse(jsonStr)
    pluginParams.value = params

    // 初始化表单数据和默认值
    params.forEach(param => {
      if (param.value !== undefined && param.value !== null) {
        dynamicFormData[param.field] = param.value
      } else {
        dynamicFormData[param.field] = ''
      }
    })

    // 自动加载有 loader 的字段选项
    for (const param of params) {
      if (fieldLoaders[param.field]) {
        await loadFieldOptions(param.field)
      }
    }

    pluginParamsLoaded.value = true
    emitUpdate()
  } catch (error) {
    console.error('获取投递配置表单失败:', error)
    pluginParamsLoaded.value = true
    emitUpdate()
  }
}

// 加载字段选项
const loadFieldOptions = async (fieldName: string) => {
  const loader = fieldLoaders[fieldName]
  if (!loader || !localDatasourceId.value) return

  loadingFields[fieldName] = true
  try {
    const options = await loader(localDatasourceId.value, dynamicFormData)
    fieldOptions[fieldName] = options
  } catch (error) {
    console.error(`加载字段 ${fieldName} 选项失败:`, error)
    fieldOptions[fieldName] = []
  } finally {
    loadingFields[fieldName] = false
  }
}

// 动态表单字段变更
const handleFieldChange = (fieldName: string) => {
  // 检查是否有其他字段依赖当前字段
  pluginParams.value.forEach((param: PluginParam) => {
    // 约定：target_column 依赖 table_name
    if (param.field === 'target_column' && fieldName === 'table_name') {
      dynamicFormData['target_column'] = ''
      loadFieldOptions('target_column')
    }
  })
  emitUpdate()
}

// 加载数据源列表
const fetchDatasourceList = async () => {
  try {
    const res = await dataSourceApi.getList()
    datasourceList.value = res.data.data || []
  } catch (error) {
    console.error('获取数据源列表失败:', error)
  }
}

// 暴露给父组件的方法：编辑回填
const loadConfig = async (config: Record<string, any>) => {
  if (!config) return

  const datasourceId = config.datasource_id || ''
  localDatasourceId.value = datasourceId

  if (!datasourceId) {
    pluginParamsLoaded.value = true
    return
  }

  // 加载动态表单
  await loadPluginParams(datasourceId)

  // 回填动态表单数据
  Object.keys(config).forEach(key => {
    if (key !== 'datasource_id' && key in dynamicFormData) {
      dynamicFormData[key] = config[key]
    }
  })

  // 级联加载选项（如 target_column 依赖 table_name）
  if (dynamicFormData['table_name']) {
    await loadFieldOptions('target_column')
  }

  emitUpdate()
}

defineExpose({ loadConfig })

onMounted(() => {
  fetchDatasourceList()
})
</script>

<style scoped>
.target-config-form {
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid #ebeef5;
    display: flex;
    align-items: center;

    .optional-tag {
      font-size: 12px;
      font-weight: 400;
      color: #909399;
      margin-left: 8px;
      background: #f5f7fa;
      padding: 2px 8px;
      border-radius: 4px;
    }
  }
}
</style>
