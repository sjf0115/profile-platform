<template>
  <div class="export-detail-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="back-btn" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>
      <h2 class="page-title">投递详情</h2>
    </div>

    <el-card v-loading="loading" class="detail-card">
      <template v-if="exportData">
        <!-- 基础信息 -->
        <div class="detail-section">
          <h3 class="section-title">基本信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="投递名称">
              {{ exportData.export_name }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="exportData.status === 1 ? 'success' : 'danger'" size="small">
                {{ exportData.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="投递描述" :span="2">
              {{ exportData.export_desc || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(exportData.gmt_create) }}
            </el-descriptions-item>
            <el-descriptions-item label="修改时间">
              {{ formatDateTime(exportData.gmt_modified) }}
            </el-descriptions-item>
            <el-descriptions-item label="创建人">
              {{ exportData.creator || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="修改人">
              {{ exportData.modifier || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 投递配置 -->
        <div class="detail-section">
          <h3 class="section-title">投递配置</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="投递方式">
              <el-tag :type="exportData.export_mode === 2 ? 'success' : 'primary'" size="small">
                {{ exportData.export_mode === 2 ? '应用投递' : '数据源投递' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="目标类型">
              <el-tag :type="targetTypeTag" size="small">{{ targetTypeLabel }}</el-tag>
            </el-descriptions-item>

            <!-- 数据源投递配置 -->
            <template v-if="exportData.export_mode === 1">
              <el-descriptions-item label="数据源">
                {{ getDatasourceName(parsedConfig.datasource_id) }}
              </el-descriptions-item>

              <!-- table 类型 -->
              <template v-if="targetType === 'table'">
                <el-descriptions-item label="数据库">
                  {{ parsedConfig.database || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="数据表">
                  {{ parsedConfig.table_name || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="写入模式">
                  {{ parsedConfig.write_mode === 'append' ? '追加' : '覆盖' }}
                </el-descriptions-item>
              </template>

              <!-- file 类型 -->
              <template v-if="targetType === 'file'">
                <el-descriptions-item label="Bucket">
                  {{ parsedConfig.bucket || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="对象路径">
                  {{ parsedConfig.object_path || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="文件格式">
                  {{ parsedConfig.file_format || '-' }}
                </el-descriptions-item>
              </template>

              <!-- topic 类型 -->
              <template v-if="targetType === 'topic'">
                <el-descriptions-item label="Topic">
                  {{ parsedConfig.topic || '-' }}
                </el-descriptions-item>
              </template>

              <!-- index 类型 -->
              <template v-if="targetType === 'index'">
                <el-descriptions-item label="索引名">
                  {{ parsedConfig.index_name || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="写入模式">
                  {{ parsedConfig.write_mode === 'append' ? '追加' : '覆盖' }}
                </el-descriptions-item>
              </template>
            </template>

            <!-- 应用投递配置 -->
            <template v-if="exportData.export_mode === 2">
              <el-descriptions-item label="应用名称">
                {{ getApplicationName(parsedConfig.application_id) }}
              </el-descriptions-item>
            </template>
          </el-descriptions>
        </div>

        <!-- 调度配置 -->
        <div class="detail-section">
          <h3 class="section-title">调度配置</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="调度类型">
              {{ getSchedulerTypeText(exportData.scheduler_type) }}
            </el-descriptions-item>
            <el-descriptions-item v-if="exportData.scheduler_type === 3 || exportData.scheduler_type === 4" label="Cron 表达式">
              {{ exportData.scheduler_cron || '-' }}
            </el-descriptions-item>
            <el-descriptions-item v-if="exportData.scheduler_type === 2" label="触发 URL">
              {{ exportData.scheduler_url || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 执行历史 -->
        <div class="detail-section">
          <h3 class="section-title">最新执行</h3>
          <template v-if="exportData.latest_instance">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="执行状态">
                <el-tag :type="getInstanceStatusType(exportData.latest_instance.status)" size="small">
                  {{ getInstanceStatusText(exportData.latest_instance.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="执行时间">
                {{ formatDateTime(exportData.latest_instance.start_time) }}
              </el-descriptions-item>
              <el-descriptions-item label="耗时">
                {{ exportData.latest_instance.duration ? `${exportData.latest_instance.duration}ms` : '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="执行信息">
                {{ exportData.latest_instance.message || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </template>
          <div v-else class="empty-config">
            <el-empty description="暂无执行记录" :image-size="60" />
          </div>
        </div>
      </template>

      <el-empty v-else description="投递不存在或已被删除" />
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <el-button @click="handleBack">返回</el-button>
      <el-button type="primary" @click="handleEdit">编辑</el-button>
      <el-button type="success" @click="handleExecute">立即执行</el-button>
      <el-button
        :type="exportData?.status === 1 ? 'danger' : 'success'"
        @click="handleToggleStatus"
      >
        {{ exportData?.status === 1 ? '停用' : '启用' }}
      </el-button>
      <el-button type="danger" @click="handleDelete">删除</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import type { Export, ExportConfig, DataSource, Application } from '@/types'
import { exportApi } from '@/api/export'
import { dataSourceApi } from '@/api/datasource'
import { applicationApi } from '@/api/application'

const route = useRoute()
const router = useRouter()

// 投递 ID
const exportId = computed(() => route.params.id as string)

// 加载状态
const loading = ref(false)

// 投递详情
const exportData = ref<Export | null>(null)

// 数据源列表
const datasourceList = ref<DataSource[]>([])

// 应用列表
const applicationList = ref<Application[]>([])

// 解析 export_config
const parsedConfig = computed<ExportConfig>(() => {
  if (!exportData.value?.export_config) return {}
  try {
    return JSON.parse(exportData.value.export_config)
  } catch (e) {
    return {}
  }
})

// 目标类型
const targetType = computed(() => {
  const config = parsedConfig.value
  if (config.table_name) return 'table'
  if (config.bucket) return 'file'
  if (config.topic) return 'topic'
  if (config.index_name) return 'index'
  return 'table'
})

// 目标类型标签
const targetTypeTag = computed(() => {
  switch (targetType.value) {
    case 'table': return 'primary'
    case 'file': return 'success'
    case 'topic': return 'warning'
    case 'index': return 'danger'
    default: return 'info'
  }
})

const targetTypeLabel = computed(() => {
  switch (targetType.value) {
    case 'table': return '数据表'
    case 'file': return '文件'
    case 'topic': return '消息'
    case 'index': return '索引'
    default: return '-'
  }
})

// 获取数据源名称
const getDatasourceName = (datasourceId?: string): string => {
  if (!datasourceId) return '-'
  const ds = datasourceList.value.find(d => d.datasource_id === datasourceId)
  return ds ? ds.datasource_name : datasourceId
}

// 获取应用名称
const getApplicationName = (applicationId?: string): string => {
  if (!applicationId) return '-'
  const app = applicationList.value.find(a => String(a.id) === applicationId)
  return app ? app.app_name : applicationId
}

// 获取投递详情
const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await exportApi.getDetail(exportId.value)
    exportData.value = res.data.data || null
  } catch (error) {
    console.error('获取投递详情失败:', error)
    ElMessage.error('获取投递详情失败')
  } finally {
    loading.value = false
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

// 获取应用列表
const fetchApplicationList = async () => {
  try {
    const res = await applicationApi.getList({})
    applicationList.value = res.data.data || []
  } catch (error) {
    console.error('获取应用列表失败:', error)
  }
}

// 返回
const handleBack = () => {
  router.back()
}

// 编辑
const handleEdit = () => {
  router.push(`/group/export/edit/${exportId.value}`)
}

// 立即执行
const handleExecute = async () => {
  try {
    await ElMessageBox.confirm(
      `确认立即执行投递「${exportData.value?.export_name}」？`,
      '提示',
      { type: 'warning' }
    )
    const res = await exportApi.execute(exportId.value)
    if (res.data.code === 0) {
      ElMessage.success('投递任务已提交，请稍后查看执行结果')
      fetchDetail()
    } else {
      ElMessage.error(res.data.message || '执行失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('执行失败')
    }
  }
}

// 切换状态
const handleToggleStatus = async () => {
  if (!exportData.value) return
  const newStatus = exportData.value.status === 1 ? 2 : 1
  const action = newStatus === 1 ? '启用' : '停用'
  try {
    await ElMessageBox.confirm(`确认${action}投递？`, '提示', {
      type: 'warning'
    })
    const res = await exportApi.save({
      export_id: exportId.value,
      status: newStatus
    })
    if (res.data.code === 0) {
      ElMessage.success(`${action}成功`)
      fetchDetail()
    } else {
      ElMessage.error(res.data.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

// 删除
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确认删除该投递吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await exportApi.delete(exportId.value)
    ElMessage.success('删除成功')
    router.push('/group/export')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 工具函数
const formatDateTime = (dateStr?: string | number): string => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getSchedulerTypeText = (type?: number): string => {
  switch (type) {
    case 1: return '手动触发'
    case 2: return 'API 触发'
    case 3: return '日周期调度'
    case 4: return '小时周期调度'
    default: return '-'
  }
}

const getInstanceStatusType = (status?: number): string => {
  switch (status) {
    case 1: return 'info'
    case 2: return 'warning'
    case 3: return 'success'
    case 4: return 'danger'
    default: return 'info'
  }
}

const getInstanceStatusText = (status?: number): string => {
  switch (status) {
    case 1: return '未运行'
    case 2: return '运行中'
    case 3: return '运行失败'
    case 4: return '运行成功'
    default: return '-'
  }
}

onMounted(() => {
  fetchDetail()
  fetchDatasourceList()
  fetchApplicationList()
})
</script>

<style scoped lang="scss">
.export-detail-page {
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

  .detail-card {
    .detail-section {
      margin-bottom: 30px;

      .section-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 16px;
        padding-bottom: 10px;
        border-bottom: 1px solid #ebeef5;
      }

      .empty-config {
        padding: 20px 0;
      }
    }
  }

  .action-bar {
    margin-top: 20px;
    display: flex;
    gap: 12px;
  }
}
</style>
