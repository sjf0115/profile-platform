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

            <el-form-item label="数据库" prop="database">
              <el-select
                v-model="formData.database"
                placeholder="请选择数据库"
                clearable
                style="width: 300px"
                :disabled="!formData.datasource_id"
                @change="handleDatabaseChange"
              >
                <el-option
                  v-for="db in databaseList"
                  :key="db.name"
                  :label="db.name"
                  :value="db.name"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="数据表" prop="table_name">
              <el-select
                v-model="formData.table_name"
                placeholder="请选择数据表"
                clearable
                style="width: 300px"
                :disabled="!formData.database"
              >
                <el-option
                  v-for="tb in tableList"
                  :key="tb.name"
                  :label="tb.name + (tb.comment ? ' (' + tb.comment + ')' : '')"
                  :value="tb.name"
                />
              </el-select>
            </el-form-item>

            <!-- 写入模式 -->
            <el-form-item
              v-if="inferredTargetType === 'table' || inferredTargetType === 'index'"
              label="写入模式"
            >
              <el-radio-group v-model="formData.write_mode">
                <el-radio label="append">追加</el-radio>
                <el-radio label="upsert">覆盖</el-radio>
              </el-radio-group>
            </el-form-item>

            <!-- Topic 配置 -->
            <template v-if="inferredTargetType === 'topic'">
              <el-form-item label="Topic">
                <el-input
                  v-model="formData.topic"
                  placeholder="请输入 Topic 名称"
                  style="width: 300px"
                />
              </el-form-item>
            </template>

            <!-- 文件存储配置 -->
            <template v-if="inferredTargetType === 'file'">
              <el-form-item label="Bucket">
                <el-input
                  v-model="formData.bucket"
                  placeholder="请输入 Bucket 名称"
                  style="width: 300px"
                />
              </el-form-item>
              <el-form-item label="对象路径">
                <el-input
                  v-model="formData.object_path"
                  placeholder="例如 /groups/{groupId}/{timestamp}.csv"
                  style="width: 400px"
                />
              </el-form-item>
              <el-form-item label="文件格式">
                <el-select v-model="formData.file_format" style="width: 300px">
                  <el-option label="CSV" value="csv" />
                  <el-option label="JSON" value="json" />
                  <el-option label="Parquet" value="parquet" />
                </el-select>
              </el-form-item>
            </template>

            <!-- ES 索引配置 -->
            <el-form-item v-if="inferredTargetType === 'index'" label="索引名">
              <el-input
                v-model="formData.index_name"
                placeholder="请输入 ES 索引名"
                style="width: 300px"
              />
            </el-form-item>
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
                  :key="app.id"
                  :label="app.app_name"
                  :value="String(app.id)"
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { Export, ExportConfig, DataSource, DatabaseInfo, TableInfo, Group, Application } from '@/types'
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
const databaseList = ref<DatabaseInfo[]>([])
const tableList = ref<TableInfo[]>([])
const groupList = ref<Group[]>([])
const applicationList = ref<Application[]>([])

// 表单数据
const formData = reactive<ExportConfig & {
  export_name: string
  export_desc: string
  group_id: string
  export_mode: number
  scheduler_type: number
  scheduler_cron: string
  scheduler_url: string
}>({
  export_name: '',
  export_desc: '',
  group_id: '',
  export_mode: 1,
  datasource_id: '',
  database: '',
  table_name: '',
  write_mode: 'append',
  application_id: '',
  bucket: '',
  object_path: '',
  file_format: 'csv',
  topic: '',
  index_name: '',
  scheduler_type: 1,
  scheduler_cron: '',
  scheduler_url: '',
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
  database: [
    { required: true, message: '请选择数据库', trigger: 'change' },
  ],
  table_name: [
    { required: true, message: '请选择数据表', trigger: 'change' },
  ],
  application_id: [
    { required: true, message: '请选择应用', trigger: 'change' },
  ],
})

// 当前选中数据源的类型
const currentDatasourceType = computed(() => {
  const ds = datasourceList.value.find(d => d.datasource_id === formData.datasource_id)
  return ds?.datasource_type || ''
})

// 根据数据源类型推断目标类型
const inferredTargetType = computed((): string => {
  const type = currentDatasourceType.value.toLowerCase()
  if (['mysql', 'clickhouse', 'postgresql', 'oracle', 'hive', 'doris', 'jdbc'].includes(type)) {
    return 'table'
  }
  if (['minio', 'hdfs', 'oss', 's3'].includes(type)) {
    return 'file'
  }
  if (['kafka', 'rabbitmq', 'rocketmq'].includes(type)) {
    return 'topic'
  }
  if (['elasticsearch', 'es'].includes(type)) {
    return 'index'
  }
  return ''
})

// 选中的应用
const selectedApplication = computed(() => {
  if (!formData.application_id) return null
  return applicationList.value.find(app => String(app.id) === formData.application_id) || null
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
    if (config.indexName) {
      return `Index: ${config.indexName}`
    }
    return '未配置'
  } catch {
    return '未配置'
  }
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

// 获取数据库列表
const fetchDatabaseList = async (datasourceId: string) => {
  try {
    const res = await dataSourceApi.getDatabases(datasourceId)
    databaseList.value = res.data.data || []
  } catch (error) {
    console.error('获取数据库列表失败:', error)
  }
}

// 获取数据表列表
const fetchTableList = async (datasourceId: string, database: string) => {
  try {
    const res = await dataSourceApi.getTables(datasourceId, database)
    tableList.value = res.data.data || []
  } catch (error) {
    console.error('获取数据表列表失败:', error)
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

// 数据源变更
const handleDatasourceChange = (datasourceId: string) => {
  formData.database = ''
  formData.table_name = ''
  databaseList.value = []
  tableList.value = []
  formData.write_mode = inferredTargetType.value === 'table' ? 'append' : 'upsert'
  if (!datasourceId) return
  fetchDatabaseList(datasourceId)
}

// 数据库变更
const handleDatabaseChange = (database: string) => {
  formData.table_name = ''
  tableList.value = []
  if (!database || !formData.datasource_id) return
  fetchTableList(formData.datasource_id, database)
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
        const config: ExportConfig = JSON.parse(data.export_config)
        formData.export_mode = data.export_mode || 1
        formData.datasource_id = config.datasource_id || ''
        formData.database = config.database || ''
        formData.table_name = config.table_name || ''
        formData.write_mode = config.write_mode || 'append'
        formData.application_id = config.application_id || ''
        formData.bucket = config.bucket || ''
        formData.object_path = config.object_path || ''
        formData.file_format = config.file_format || 'csv'
        formData.topic = config.topic || ''
        formData.index_name = config.index_name || ''

        // 级联加载
        if (formData.datasource_id) {
          await fetchDatabaseList(formData.datasource_id)
          if (formData.database) {
            await fetchTableList(formData.datasource_id, formData.database)
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
  const config: ExportConfig = {}

  if (formData.export_mode === 1) {
    config.datasource_id = formData.datasource_id
    if (inferredTargetType.value === 'table') {
      config.database = formData.database
      config.table_name = formData.table_name
      config.write_mode = formData.write_mode
    } else if (inferredTargetType.value === 'file') {
      config.bucket = formData.bucket
      config.object_path = formData.object_path
      config.file_format = formData.file_format
    } else if (inferredTargetType.value === 'topic') {
      config.topic = formData.topic
    } else if (inferredTargetType.value === 'index') {
      config.index_name = formData.index_name
      config.write_mode = formData.write_mode
    }
  } else {
    config.application_id = formData.application_id
  }

  return JSON.stringify(config)
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
