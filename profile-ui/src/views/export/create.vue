<template>
  <div class="export-create-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="back-btn" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>
      <h2 class="page-title">{{ isEdit ? '编辑投递' : '创建投递' }}</h2>
    </div>

    <el-card class="form-card">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="export-form"
      >
        <!-- 基础信息 -->
        <div class="form-section">
          <h3 class="section-title">基础信息</h3>

          <el-form-item label="投递名称" prop="export_name">
            <el-input
              v-model="formData.export_name"
              placeholder="请输入投递名称"
              maxlength="100"
              show-word-limit
              style="width: 400px"
            />
          </el-form-item>

          <el-form-item label="投递描述">
            <el-input
              v-model="formData.export_desc"
              type="textarea"
              :rows="3"
              placeholder="请输入投递描述"
              maxlength="200"
              show-word-limit
              style="width: 400px"
            />
          </el-form-item>

          <el-form-item label="关联群组" prop="group_id">
            <el-select
              v-model="formData.group_id"
              placeholder="请选择群组"
              filterable
              style="width: 400px"
            >
              <el-option
                v-for="group in groupList"
                :key="group.group_id"
                :label="group.group_name"
                :value="group.group_id"
              />
            </el-select>
          </el-form-item>
        </div>

        <!-- 投递配置 -->
        <div class="form-section">
          <h3 class="section-title">投递配置</h3>

          <el-form-item label="投递方式">
            <el-radio-group v-model="formData.export_mode">
              <el-radio :label="1">数据源投递</el-radio>
              <el-radio :label="2">应用投递</el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- 数据源投递 -->
          <template v-if="formData.export_mode === 1">
            <el-form-item label="数据源" prop="datasource_id">
              <el-select
                v-model="formData.datasource_id"
                placeholder="请选择数据源"
                clearable
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
            <template v-if="exportPluginParams.length > 0">
              <template v-for="param in exportPluginParams" :key="param.field">
                <!-- select 类型 -->
                <el-form-item
                  v-if="param.type === 'select'"
                  :label="param.title"
                  :prop="param.field"
                >
                  <el-select
                    v-model="exportFormData[param.field]"
                    :placeholder="getPlaceholder(param)"
                    clearable
                    filterable
                    :loading="loadingFields[param.field]"
                    style="width: 300px"
                    @change="handleExportFieldChange(param.field)"
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
                  <el-radio-group v-model="exportFormData[param.field]">
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
                  :prop="param.field"
                >
                  <el-input
                    v-model="exportFormData[param.field]"
                    :placeholder="getPlaceholder(param)"
                    style="width: 400px"
                  />
                </el-form-item>
              </template>
            </template>

            <!-- 无需配置提示 -->
            <template v-else-if="datasourceSelected && exportPluginParamsLoaded">
              <el-alert
                title="该数据源类型无需额外配置"
                type="info"
                :closable="false"
                style="margin-left: 120px; max-width: 400px"
              />
            </template>
          </template>

          <!-- 应用投递 -->
          <template v-if="formData.export_mode === 2">
            <el-form-item label="选择应用" prop="application_id">
              <el-select
                v-model="formData.application_id"
                placeholder="请选择应用"
                filterable
                style="width: 400px"
              >
                <el-option
                  v-for="app in applicationList"
                  :key="app.app_key"
                  :label="app.app_name"
                  :value="app.app_key"
                >
                  <span>{{ app.app_name }}</span>
                  <span style="color: #909399; font-size: 12px; margin-left: 8px;">{{ app.app_key }}</span>
                </el-option>
              </el-select>
            </el-form-item>
            <div class="form-tip" v-if="selectedApplication">
              <p>应用投递目标：{{ getApplicationTargetInfo(selectedApplication) }}</p>
            </div>
          </template>
        </div>

        <!-- 调度配置 -->
        <div class="form-section">
          <h3 class="section-title">
            调度配置
            <span class="optional-tag">可选</span>
          </h3>

          <el-form-item label="调度类型">
            <el-select v-model="formData.scheduler_type" style="width: 300px">
              <el-option label="手动触发" :value="1" />
              <el-option label="API 触发" :value="2" />
              <el-option label="日周期调度" :value="3" />
              <el-option label="小时周期调度" :value="4" />
            </el-select>
          </el-form-item>

          <el-form-item
            v-if="formData.scheduler_type === 3 || formData.scheduler_type === 4"
            label="Cron 表达式"
          >
            <el-input
              v-model="formData.scheduler_cron"
              placeholder="例如: 0 0 2 * * ?"
              style="width: 300px"
            />
          </el-form-item>

          <el-form-item v-if="formData.scheduler_type === 2" label="触发 URL">
            <el-input
              v-model="formData.scheduler_url"
              placeholder="API 触发地址"
              style="width: 400px"
            />
          </el-form-item>
        </div>
      </el-form>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button @click="handleBack">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { Export, DataSource, Group, Application, PluginParam, TableInfo, TableColumnInfo } from '@/types'
import { exportApi } from '@/api/export'
import { dataSourceApi } from '@/api/datasource'
import { groupApi } from '@/api/group'
import { applicationApi } from '@/api/application'

const route = useRoute()
const router = useRouter()

// 是否编辑模式
const isEdit = computed(() => !!route.params.id)

// 表单引用
const formRef = ref<FormInstance>()

// 提交状态
const submitting = ref(false)

// 列表数据
const datasourceList = ref<DataSource[]>([])
const groupList = ref<Group[]>([])
const applicationList = ref<Application[]>([])

// 动态表单状态
const exportPluginParams = ref<PluginParam[]>([])
const exportFormData = reactive<Record<string, any>>({})
const fieldOptions = reactive<Record<string, Array<{ label: string; value: any }>>>({})
const loadingFields = reactive<Record<string, boolean>>({})
const exportPluginParamsLoaded = ref(false)

// 表单数据
const formData = reactive<{
  export_name: string
  export_desc: string
  group_id: string
  export_mode: number
  datasource_id: string
  application_id: string
  scheduler_type: number
  scheduler_cron: string
  scheduler_url: string
}>({
  export_name: '',
  export_desc: '',
  group_id: '',
  export_mode: 1,
  datasource_id: '',
  application_id: '',
  scheduler_type: 1,
  scheduler_cron: '',
  scheduler_url: '',
})

const datasourceSelected = computed(() => !!formData.datasource_id)

// 监听投递方式切换，清空另一模式的字段
watch(() => formData.export_mode, (newMode) => {
  if (newMode === 1) {
    // 切换到数据源投递，清空应用投递字段
    formData.application_id = ''
  } else if (newMode === 2) {
    // 切换到应用投递，清空数据源投递字段
    formData.datasource_id = ''
    exportPluginParams.value = []
    Object.keys(exportFormData).forEach(key => delete exportFormData[key])
    Object.keys(fieldOptions).forEach(key => delete fieldOptions[key])
    exportPluginParamsLoaded.value = true
  }
})

// 表单校验规则
const formRules = reactive<FormRules>({
  export_name: [
    { required: true, message: '请输入投递名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' },
  ],
  group_id: [
    { required: true, message: '请选择群组', trigger: 'change' },
  ],
  datasource_id: [
    { required: true, message: '请选择数据源', trigger: 'change' },
  ],
  application_id: [
    { required: true, message: '请选择应用', trigger: 'change' },
  ],
})

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

// 选中的应用
const selectedApplication = computed(() => {
  if (!formData.application_id) return null
  return applicationList.value.find(app => app.app_key === formData.application_id) || null
})

// 获取应用投递目标信息
const getApplicationTargetInfo = (app: Application): string => {
  if (!app.target_config) return '未配置'
  try {
    const config = JSON.parse(app.target_config)
    if (config.tableName) {
      return `${config.database}.${config.tableName}`
    }
    if (config.bucket) {
      return `${config.bucket}/${config.objectPath || ''}`
    }
    if (config.topic) {
      return `Topic: ${config.topic}`
    }
    return '未配置'
  } catch {
    return '未配置'
  }
}

// 获取 placeholder
const getPlaceholder = (param: PluginParam): string => {
  const props = param.props as any
  return props?.placeholder || `请选择${param.title}`
}

// 获取数据源列表
const fetchDatasourceList = async () => {
  try {
    const res = await dataSourceApi.getList()
    datasourceList.value = res.data.data || []
  } catch (error) {
    console.error('获取数据源列表失败:', error)
  }
}

// 获取群组列表
const fetchGroupList = async () => {
  try {
    const res = await groupApi.getList({})
    groupList.value = res.data.data || []
  } catch (error) {
    console.error('获取群组列表失败:', error)
  }
}

// 获取应用列表
const fetchApplicationList = async () => {
  try {
    const res = await applicationApi.getList({})
    applicationList.value = res.data.data || []
  } catch (error) {
    console.error('获取应用列表失败:', error)
  }
}

// 加载动态表单配置
const loadExportPluginParams = async (datasourceId: string) => {
  exportPluginParams.value = []
  exportPluginParamsLoaded.value = false
  Object.keys(exportFormData).forEach(key => delete exportFormData[key])
  Object.keys(fieldOptions).forEach(key => delete fieldOptions[key])

  if (!datasourceId) {
    exportPluginParamsLoaded.value = true
    return
  }

  try {
    const res = await dataSourceApi.getExportConfig(datasourceId)
    const jsonStr = res.data.data
    if (!jsonStr || jsonStr === '[]') {
      exportPluginParams.value = []
      exportPluginParamsLoaded.value = true
      return
    }
    const params: PluginParam[] = JSON.parse(jsonStr)
    exportPluginParams.value = params

    // 初始化表单数据和默认值
    params.forEach(param => {
      if (param.value !== undefined && param.value !== null) {
        exportFormData[param.field] = param.value
      } else {
        exportFormData[param.field] = ''
      }
    })

    // 自动加载有 loader 的字段选项
    for (const param of params) {
      if (fieldLoaders[param.field]) {
        await loadFieldOptions(param.field)
      }
    }

    exportPluginParamsLoaded.value = true
  } catch (error) {
    console.error('获取投递配置表单失败:', error)
    exportPluginParamsLoaded.value = true
  }
}

// 加载字段选项
const loadFieldOptions = async (fieldName: string) => {
  const loader = fieldLoaders[fieldName]
  if (!loader || !formData.datasource_id) return

  loadingFields[fieldName] = true
  try {
    const options = await loader(formData.datasource_id, exportFormData)
    fieldOptions[fieldName] = options
  } catch (error) {
    console.error(`加载字段 ${fieldName} 选项失败:`, error)
    fieldOptions[fieldName] = []
  } finally {
    loadingFields[fieldName] = false
  }
}

// 数据源变更
const handleDatasourceChange = (datasourceId: string) => {
  loadExportPluginParams(datasourceId)
}

// 动态表单字段变更
const handleExportFieldChange = (fieldName: string) => {
  // 检查是否有其他字段依赖当前字段
  exportPluginParams.value.forEach((param: PluginParam) => {
    // 简单约定：target_column 依赖 table_name
    if (param.field === 'target_column' && fieldName === 'table_name') {
      exportFormData['target_column'] = ''
      loadFieldOptions('target_column')
    }
  })
}

// 获取投递详情（编辑模式）
const fetchDetail = async () => {
  if (!isEdit.value) return
  const exportId = route.params.id as string
  if (!exportId) return

  try {
    const res = await exportApi.getDetail(exportId)
    const data = res.data.data
    if (!data) {
      ElMessage.error('投递不存在')
      return
    }

    // 回填基础信息
    formData.export_name = data.export_name
    formData.export_desc = data.export_desc || ''
    formData.scheduler_type = data.scheduler_type || 1
    formData.scheduler_cron = data.scheduler_cron || ''
    formData.scheduler_url = data.scheduler_url || ''

    // 解析 export_config
    if (data.export_config) {
      try {
        const config = JSON.parse(data.export_config)
        formData.export_mode = data.export_mode || 1
        formData.group_id = config.group_id || ''
        formData.datasource_id = config.datasource_id || ''
        formData.application_id = config.application_id || ''

        // 加载动态表单配置
        if (formData.datasource_id && formData.export_mode === 1) {
          await loadExportPluginParams(formData.datasource_id)

          // 回填动态表单数据
          Object.keys(config).forEach(key => {
            if (key !== 'datasource_id' && key in exportFormData) {
              exportFormData[key] = config[key]
            }
          })

          // 级联加载选项
          if (exportFormData['table_name']) {
            await loadFieldOptions('target_column')
          }
        }
      } catch (e) {
        console.error('解析 export_config 失败:', e)
      }
    }
  } catch (error) {
    console.error('获取投递详情失败:', error)
    ElMessage.error('获取投递信息失败')
  }
}

// 返回
const handleBack = () => {
  router.back()
}

// 构建 export_config
const buildExportConfig = (): string => {
  if (formData.export_mode === 1) {
    return JSON.stringify({
      group_id: formData.group_id,
      datasource_id: formData.datasource_id,
      ...exportFormData,
    })
  } else {
    return JSON.stringify({
      group_id: formData.group_id,
      application_id: formData.application_id,
    })
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const params: Partial<Export> = {
        export_name: formData.export_name,
        export_desc: formData.export_desc || undefined,
        export_type: 1, // 群组投递
        export_mode: formData.export_mode,
        export_config: buildExportConfig(),
        scheduler_type: formData.scheduler_type,
        scheduler_cron: formData.scheduler_cron || undefined,
        scheduler_url: formData.scheduler_url || undefined,
      }

      // 编辑模式
      if (isEdit.value) {
        params.export_id = route.params.id as string
      }

      const res = await exportApi.save(params)

      if (res.data.code === 0) {
        ElMessage.success(isEdit.value ? '保存成功' : '创建成功')
        router.push('/group/export')
      } else {
        ElMessage.error(res.data.message || '操作失败')
      }
    } catch (error) {
      console.error('保存失败:', error)
      ElMessage.error('保存失败')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  fetchDatasourceList()
  fetchGroupList()
  fetchApplicationList()
  if (isEdit.value) {
    fetchDetail()
  }
})
</script>

<style scoped lang="scss">
.export-create-page {
  .page-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;

    .back-btn {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #606266;
      cursor: pointer;
      margin-right: 16px;
      padding: 6px 12px;
      border-radius: 4px;
      transition: all 0.3s;

      &:hover {
        background-color: #f5f7fa;
        color: #409eff;
      }
    }

    .page-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      margin: 0;
    }
  }

  .form-card {
    .export-form {
      max-width: 800px;
    }

    .form-section {
      margin-bottom: 30px;

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

    .form-tip {
      margin-left: 120px;
      padding: 10px 16px;
      background: #f0f9eb;
      border-radius: 4px;
      color: #67c23a;
      font-size: 13px;

      p {
        margin: 0;
      }
    }

    .form-actions {
      margin-top: 30px;
      padding-top: 20px;
      border-top: 1px solid #ebeef5;
      display: flex;
      justify-content: center;
      gap: 16px;
    }
  }
}
</style>
