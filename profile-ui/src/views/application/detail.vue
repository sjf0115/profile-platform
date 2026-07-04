<template>
  <div class="application-detail-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="back-btn" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>
      <h2 class="page-title">应用详情</h2>
    </div>

    <el-card v-loading="loading" class="detail-card">
      <template v-if="application">
        <!-- 基础信息 -->
        <div class="detail-section">
          <h3 class="section-title">基本信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="应用名称">
              {{ application.app_name }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="application.status === 1 ? 'success' : 'danger'" size="small">
                {{ application.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="应用描述" :span="2">
              {{ application.app_desc || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(application.gmt_create) }}
            </el-descriptions-item>
            <el-descriptions-item label="修改时间">
              {{ formatDateTime(application.gmt_modified) }}
            </el-descriptions-item>
            <el-descriptions-item label="创建人">
              {{ application.creator || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="修改人">
              {{ application.modifier || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 凭证信息 -->
        <div class="detail-section">
          <h3 class="section-title">凭证信息</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="AppKey">
              <div class="credential-row">
                <span class="credential-value">{{ application.app_key }}</span>
                <el-button link type="primary" size="small" @click="copyText(application.app_key!)">
                  复制
                </el-button>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="AppSecret">
              <div class="credential-row">
                <span class="credential-value masked">••••••••••••••••</span>
                <el-button link type="warning" size="small" @click="handleResetSecret">
                  重置
                </el-button>
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 投递目标配置 -->
        <div class="detail-section">
          <h3 class="section-title">投递目标配置</h3>
          <template v-if="parsedTargetConfig.targetType">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="数据源">
                {{ getDatasourceName(parsedTargetConfig.datasourceId) }}
              </el-descriptions-item>
              <el-descriptions-item label="目标类型">
                <el-tag :type="targetTypeTag" size="small">{{ targetTypeLabel }}</el-tag>
              </el-descriptions-item>

              <!-- table 类型 -->
              <template v-if="parsedTargetConfig.targetType === 'table'">
                <el-descriptions-item label="数据库">
                  {{ parsedTargetConfig.database || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="数据表">
                  {{ parsedTargetConfig.tableName || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="写入模式">
                  {{ writeModeLabel }}
                </el-descriptions-item>
              </template>

              <!-- file 类型 -->
              <template v-if="parsedTargetConfig.targetType === 'file'">
                <el-descriptions-item label="Bucket">
                  {{ parsedTargetConfig.bucket || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="对象路径">
                  {{ parsedTargetConfig.objectPath || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="文件格式">
                  {{ parsedTargetConfig.fileFormat || '-' }}
                </el-descriptions-item>
              </template>

              <!-- topic 类型 -->
              <template v-if="parsedTargetConfig.targetType === 'topic'">
                <el-descriptions-item label="Topic">
                  {{ parsedTargetConfig.topic || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="消息格式">
                  {{ parsedTargetConfig.messageFormat || '-' }}
                </el-descriptions-item>
              </template>

              <!-- index 类型 -->
              <template v-if="parsedTargetConfig.targetType === 'index'">
                <el-descriptions-item label="索引名">
                  {{ parsedTargetConfig.indexName || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="写入模式">
                  {{ writeModeLabel }}
                </el-descriptions-item>
              </template>
            </el-descriptions>
          </template>
          <div v-else class="empty-config">
            <el-empty description="未配置投递目标">
              <el-button type="primary" size="small" @click="handleEdit">去配置</el-button>
            </el-empty>
          </div>
        </div>

        <!-- 高级配置 -->
        <div class="detail-section">
          <h3 class="section-title">高级配置</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="Webhook 地址">
              {{ application.webhook_url || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="频率限制">
              {{ application.rate_limit ? `${application.rate_limit} 次/分钟` : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="IP 白名单" :span="2">
              {{ application.ip_whitelist || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </template>

      <el-empty v-else description="应用不存在或已被删除" />
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <el-button @click="handleBack">返回</el-button>
      <el-button type="primary" @click="handleEdit">编辑</el-button>
      <el-button type="warning" @click="handleResetSecret">重置 Secret</el-button>
      <el-button
        :type="application?.status === 1 ? 'danger' : 'success'"
        @click="handleToggleStatus"
      >
        {{ application?.status === 1 ? '停用' : '启用' }}
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
import type { Application, DataSource } from '@/types'
import { applicationApi } from '@/api/application'
import { dataSourceApi } from '@/api/datasource'

const route = useRoute()
const router = useRouter()

// 应用 ID
const appId = computed(() => Number(route.params.id))

// 加载状态
const loading = ref(false)

// 应用详情
const application = ref<Application | null>(null)

// 数据源列表（用于显示数据源名称）
const datasourceList = ref<DataSource[]>([])

// 解析 target_config
const parsedTargetConfig = computed(() => {
  if (!application.value?.target_config) return {}
  try {
    return JSON.parse(application.value.target_config)
  } catch (e) {
    return {}
  }
})

// 目标类型标签
const targetTypeLabel = computed(() => {
  const map: Record<string, string> = {
    table: '数据表',
    file: '文件存储',
    topic: '消息队列',
    index: 'ES索引',
  }
  return map[parsedTargetConfig.value.targetType] || parsedTargetConfig.value.targetType || '-'
})

// 目标类型 Tag 颜色
const targetTypeTag = computed(() => {
  const map: Record<string, string> = {
    table: '',
    file: 'success',
    topic: 'warning',
    index: 'info',
  }
  return map[parsedTargetConfig.value.targetType] || 'info'
})

// 写入模式标签
const writeModeLabel = computed(() => {
  const map: Record<string, string> = {
    append: '追加',
    overwrite: '覆盖',
    upsert: 'Upsert',
  }
  return map[parsedTargetConfig.value.writeMode] || parsedTargetConfig.value.writeMode || '-'
})

// 格式化日期时间
const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 复制文本
const copyText = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch (e) {
    ElMessage.error('复制失败')
  }
}

// 获取数据源名称
const getDatasourceName = (datasourceId: string): string => {
  if (!datasourceId) return '-'
  const ds = datasourceList.value.find(d => d.datasource_id === datasourceId)
  return ds ? ds.datasource_name : datasourceId
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

// 获取应用详情
const fetchDetail = async () => {
  if (!appId.value) return
  loading.value = true
  try {
    const res = await applicationApi.getDetail(appId.value)
    application.value = res.data.data
  } catch (error) {
    console.error('获取应用详情失败:', error)
    ElMessage.error('获取应用详情失败')
  } finally {
    loading.value = false
  }
}

// 重置 Secret
const handleResetSecret = () => {
  ElMessageBox.confirm(
    `确定要重置应用 "${application.value?.app_name}" 的 AppSecret 吗？重置后旧 Secret 将立即失效。`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const res = await applicationApi.resetSecret(appId.value)
        const newSecret = res.data.data
        ElMessageBox.alert(
          `新的 AppSecret 为：<strong>${newSecret}</strong><br/><br/>请妥善保存，关闭后将无法再次查看。`,
          '重置成功',
          {
            dangerouslyUseHTMLString: true,
            confirmButtonText: '我知道了',
            type: 'success',
          }
        )
      } catch (error) {
        console.error('重置 Secret 失败:', error)
      }
    })
    .catch(() => {})
}

// 启用/停用
const handleToggleStatus = () => {
  const app = application.value
  if (!app) return
  const newStatus = app.status === 1 ? 2 : 1
  const action = newStatus === 1 ? '启用' : '停用'

  ElMessageBox.confirm(`确定要${action}应用 "${app.app_name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await applicationApi.save({ id: app.id, status: newStatus })
        ElMessage.success(`${action}成功`)
        fetchDetail()
      } catch (error) {
        console.error(`${action}失败:`, error)
      }
    })
    .catch(() => {})
}

// 删除
const handleDelete = () => {
  ElMessageBox.confirm(
    `确定要删除应用 "${application.value?.app_name}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await applicationApi.delete(appId.value)
        ElMessage.success('删除成功')
        router.push('/application')
      } catch (error: any) {
        ElMessage.error(error?.response?.data?.message || '删除失败')
      }
    })
    .catch(() => {})
}

// 返回
const handleBack = () => {
  router.back()
}

// 编辑
const handleEdit = () => {
  router.push(`/application/edit/${appId.value}`)
}

onMounted(() => {
  fetchDatasourceList()
  fetchDetail()
})
</script>

<style scoped lang="scss">
.application-detail-page {
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
    margin-bottom: 20px;
  }

  .detail-section {
    margin-bottom: 30px;

    &:last-child {
      margin-bottom: 0;
    }

    .section-title {
      font-size: 16px;
      font-weight: 500;
      margin: 0 0 16px 0;
      padding-left: 12px;
      border-left: 4px solid #409eff;
    }
  }

  .credential-row {
    display: flex;
    align-items: center;
    gap: 12px;

    .credential-value {
      font-family: monospace;
      color: #303133;
      font-size: 13px;

      &.masked {
        color: #909399;
        letter-spacing: 2px;
      }
    }
  }

  .empty-config {
    padding: 20px 0;
  }

  .action-bar {
    display: flex;
    justify-content: center;
    gap: 16px;
    padding: 20px 0;
  }
}
</style>
