<template>
  <div class="application-create-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="back-btn" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>
      <h2 class="page-title">{{ isEdit ? '编辑应用' : '创建应用' }}</h2>
    </div>

    <el-card class="form-card">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="application-form"
      >
        <!-- 基础信息 -->
        <div class="form-section">
          <h3 class="section-title">基础信息</h3>

          <el-form-item label="应用名称" prop="app_name">
            <el-input
              v-model="formData.app_name"
              placeholder="请输入应用名称"
              maxlength="100"
              show-word-limit
              style="width: 400px"
            />
          </el-form-item>

          <el-form-item label="应用描述">
            <el-input
              v-model="formData.app_desc"
              type="textarea"
              :rows="3"
              placeholder="请输入应用描述"
              maxlength="200"
              show-word-limit
              style="width: 400px"
            />
          </el-form-item>
        </div>

        <!-- 投递目标配置 -->
        <div class="form-section">
          <h3 class="section-title">
            投递目标配置
            <span class="optional-tag">可选</span>
          </h3>

          <el-form-item label="数据源">
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

          <el-form-item label="数据库">
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

          <el-form-item label="数据表">
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

          <!-- 写入模式（table/index 类型显示） -->
          <el-form-item
            v-if="inferredTargetType === 'table' || inferredTargetType === 'index'"
            label="写入模式"
          >
            <el-radio-group v-model="formData.writeMode">
              <el-radio v-if="inferredTargetType === 'table'" label="append">追加</el-radio>
              <el-radio v-if="inferredTargetType === 'table'" label="overwrite">覆盖</el-radio>
              <el-radio label="upsert">Upsert</el-radio>
            </el-radio-group>
          </el-form-item>

          <!-- Topic 配置（topic 类型显示） -->
          <template v-if="inferredTargetType === 'topic'">
            <el-form-item label="Topic">
              <el-input
                v-model="formData.topic"
                placeholder="请输入 Topic 名称"
                style="width: 300px"
              />
            </el-form-item>
            <el-form-item label="消息格式">
              <el-select v-model="formData.messageFormat" style="width: 300px">
                <el-option label="JSON" value="json" />
                <el-option label="Avro" value="avro" />
                <el-option label="Protobuf" value="protobuf" />
              </el-select>
            </el-form-item>
          </template>

          <!-- 文件存储配置（file 类型显示） -->
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
                v-model="formData.objectPath"
                placeholder="例如 /groups/{groupId}/{timestamp}.csv"
                style="width: 400px"
              />
            </el-form-item>
            <el-form-item label="文件格式">
              <el-select v-model="formData.fileFormat" style="width: 300px">
                <el-option label="CSV" value="csv" />
                <el-option label="JSON" value="json" />
                <el-option label="Parquet" value="parquet" />
              </el-select>
            </el-form-item>
          </template>

          <!-- ES 索引配置（index 类型显示） -->
          <el-form-item v-if="inferredTargetType === 'index'" label="索引名">
            <el-input
              v-model="formData.indexName"
              placeholder="请输入 ES 索引名"
              style="width: 300px"
            />
          </el-form-item>
        </div>

        <!-- 高级配置 -->
        <div class="form-section">
          <h3 class="section-title">
            高级配置
            <span class="optional-tag">可选</span>
          </h3>

          <el-form-item label="Webhook 地址">
            <el-input
              v-model="formData.webhook_url"
              placeholder="请输入回调通知地址"
              clearable
              style="width: 400px"
            />
          </el-form-item>

          <el-form-item label="频率限制">
            <el-input-number
              v-model="formData.rate_limit"
              :min="1"
              :max="10000"
              placeholder="次/分钟"
              style="width: 200px"
            />
            <span class="form-help">次/分钟，默认 100</span>
          </el-form-item>

          <el-form-item label="IP 白名单">
            <el-input
              v-model="formData.ip_whitelist"
              type="textarea"
              :rows="2"
              placeholder="多个 IP 用逗号分隔，为空则不限制"
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
import type { Application, DataSource, DatabaseInfo, TableInfo } from '@/types'
import { applicationApi } from '@/api/application'
import { dataSourceApi } from '@/api/datasource'

const route = useRoute()
const router = useRouter()

// 是否编辑模式
const isEdit = computed(() => !!route.params.id)

// 表单引用
const formRef = ref<FormInstance>()

// 提交状态
const submitting = ref(false)

// 数据源列表
const datasourceList = ref<DataSource[]>([])

// 数据库列表
const databaseList = ref<DatabaseInfo[]>([])

// 数据表列表
const tableList = ref<TableInfo[]>([])

// 表单数据
const formData = reactive<{
  app_name: string
  app_desc: string
  datasource_id: string
  database: string
  table_name: string
  writeMode: string
  topic: string
  messageFormat: string
  bucket: string
  objectPath: string
  fileFormat: string
  indexName: string
  webhook_url: string
  rate_limit: number
  ip_whitelist: string
}>({
  app_name: '',
  app_desc: '',
  datasource_id: '',
  database: '',
  table_name: '',
  writeMode: 'upsert',
  topic: '',
  messageFormat: 'json',
  bucket: '',
  objectPath: '',
  fileFormat: 'csv',
  indexName: '',
  webhook_url: '',
  rate_limit: 100,
  ip_whitelist: '',
})

// 表单校验规则
const formRules = reactive<FormRules>({
  app_name: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' },
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
  // 数据库类型 → table
  if (['mysql', 'clickhouse', 'postgresql', 'oracle', 'hive', 'doris', 'jdbc'].includes(type)) {
    return 'table'
  }
  // 文件存储类型 → file
  if (['minio', 'hdfs', 'oss', 's3'].includes(type)) {
    return 'file'
  }
  // 消息队列类型 → topic
  if (['kafka', 'rabbitmq', 'rocketmq'].includes(type)) {
    return 'topic'
  }
  // ES 类型 → index
  if (['elasticsearch', 'es'].includes(type)) {
    return 'index'
  }
  return ''
})

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

// 数据源变更
const handleDatasourceChange = (datasourceId: string) => {
  formData.database = ''
  formData.table_name = ''
  databaseList.value = []
  tableList.value = []
  // 重置写入模式默认值
  formData.writeMode = inferredTargetType.value === 'table' ? 'append' : 'upsert'
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

// 获取应用详情（编辑模式）
const fetchDetail = async () => {
  if (!isEdit.value) return
  const id = Number(route.params.id)
  if (!id) return

  try {
    const res = await applicationApi.getDetail(id)
    const data = res.data.data
    if (!data) {
      ElMessage.error('应用不存在')
      return
    }

    // 回填基础信息
    formData.app_name = data.app_name
    formData.app_desc = data.app_desc || ''
    formData.webhook_url = data.webhook_url || ''
    formData.rate_limit = data.rate_limit ?? 100
    formData.ip_whitelist = data.ip_whitelist || ''

    // 解析 target_config
    if (data.target_config) {
      try {
        const config = JSON.parse(data.target_config)
        formData.datasource_id = config.datasourceId || ''
        formData.database = config.database || ''
        formData.table_name = config.tableName || ''
        formData.writeMode = config.writeMode || 'upsert'
        formData.topic = config.topic || ''
        formData.messageFormat = config.messageFormat || 'json'
        formData.bucket = config.bucket || ''
        formData.objectPath = config.objectPath || ''
        formData.fileFormat = config.fileFormat || 'csv'
        formData.indexName = config.indexName || ''

        // 级联加载：先加载数据库列表，再加载数据表列表
        if (formData.datasource_id) {
          await fetchDatabaseList(formData.datasource_id)
          if (formData.database) {
            await fetchTableList(formData.datasource_id, formData.database)
          }
        }
      } catch (e) {
        console.error('解析 target_config 失败:', e)
      }
    }
  } catch (error) {
    console.error('获取应用详情失败:', error)
    ElMessage.error('获取应用信息失败')
  }
}

// 返回
const handleBack = () => {
  router.back()
}

// 构建 target_config
const buildTargetConfig = (): string | undefined => {
  if (!formData.datasource_id) return undefined

  const config: Record<string, any> = {
    targetType: inferredTargetType.value,
    datasourceId: formData.datasource_id,
  }

  if (inferredTargetType.value === 'table') {
    if (formData.database) config.database = formData.database
    if (formData.table_name) config.tableName = formData.table_name
    config.writeMode = formData.writeMode
  } else if (inferredTargetType.value === 'file') {
    if (formData.bucket) config.bucket = formData.bucket
    if (formData.objectPath) config.objectPath = formData.objectPath
    config.fileFormat = formData.fileFormat
  } else if (inferredTargetType.value === 'topic') {
    if (formData.topic) config.topic = formData.topic
    config.messageFormat = formData.messageFormat
  } else if (inferredTargetType.value === 'index') {
    if (formData.indexName) config.indexName = formData.indexName
    config.writeMode = formData.writeMode
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
      const params: Partial<Application> = {
        app_name: formData.app_name,
        app_desc: formData.app_desc || undefined,
        webhook_url: formData.webhook_url || undefined,
        rate_limit: formData.rate_limit,
        ip_whitelist: formData.ip_whitelist || undefined,
        target_config: buildTargetConfig(),
      }

      // 编辑模式传入 id
      if (isEdit.value) {
        params.id = Number(route.params.id)
      }

      const res = await applicationApi.save(params)

      if (isEdit.value) {
        ElMessage.success('保存成功')
        router.push('/application')
      } else {
        // 创建成功，传递凭证信息到列表页弹窗
        const savedApp = res.data.data
        router.push({
          path: '/application',
          state: {
            createdApp: {
              app_key: savedApp?.app_key,
              app_secret: savedApp?.app_secret,
            },
          },
        })
      }
    } catch (error) {
      console.error('保存失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  fetchDatasourceList()
  if (isEdit.value) {
    fetchDetail()
  }
})
</script>

<style scoped lang="scss">
.application-create-page {
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
    .application-form {
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

    .form-help {
      margin-left: 12px;
      color: #909399;
      font-size: 13px;
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
