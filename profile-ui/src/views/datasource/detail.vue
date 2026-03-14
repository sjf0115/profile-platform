<template>
  <div class="datasource-detail-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="back-btn" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>
      <h2 class="page-title">数据源详情</h2>
    </div>

    <el-card v-loading="loading" class="detail-card">
      <template v-if="dataSource">
        <!-- 基本信息 -->
        <div class="detail-section">
          <h3 class="section-title">基本信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="数据源名称">
              {{ dataSource.datasource_name }}
            </el-descriptions-item>
            <el-descriptions-item label="数据源类型">
              <el-tag size="small" type="info">{{ dataSource.schema_name }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="数据源ID">
              {{ dataSource.datasource_id }}
            </el-descriptions-item>
            <el-descriptions-item label="数据源描述">
              {{ dataSource.datasource_desc || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(dataSource.gmt_create) }}
            </el-descriptions-item>
            <el-descriptions-item label="修改时间">
              {{ formatDateTime(dataSource.gmt_modified) }}
            </el-descriptions-item>
            <el-descriptions-item label="创建人">
              {{ dataSource.creator || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="修改人">
              {{ dataSource.modifier || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 连接信息 -->
        <div class="detail-section">
          <h3 class="section-title">连接信息</h3>
          <el-descriptions :column="2" border v-if="configData.length > 0">
            <el-descriptions-item 
              v-for="item in configData" 
              :key="item.key"
              :label="item.label"
            >
              {{ item.value }}
            </el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="暂无连接信息" />
        </div>
      </template>

      <el-empty v-else description="数据源不存在或已被删除" />
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <el-button @click="handleBack">返回</el-button>
      <el-button type="primary" @click="handleEdit">编辑</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import type { DataSource } from '@/types'
import { dataSourceApi } from '@/api/datasource'

const route = useRoute()
const router = useRouter()

// 数据源ID
const datasourceId = computed(() => route.params.id as string)
const schemaId = computed(() => route.query.schema_id as string)

// 加载状态
const loading = ref(false)

// 数据源详情
const dataSource = ref<DataSource | null>(null)

// 配置数据
const configData = computed(() => {
  if (!dataSource.value?.config) return []
  const keyMap: Record<string, string> = {
    host: '主机地址',
    port: '端口',
    database: '数据库名',
    user_name: '用户名',
    instance: '实例名',
    endpoint: 'Endpoint',
    ak: 'Access Key',
    sk: 'Secret Key',
    bucket: 'Bucket',
  }
  return Object.entries(dataSource.value.config).map(([key, value]) => ({
    key,
    label: keyMap[key] || key,
    value: String(value),
  }))
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

// 获取数据源详情
const fetchDataSourceDetail = async () => {
  if (!datasourceId.value) return
  loading.value = true
  try {
    const res = await dataSourceApi.getDetail(datasourceId.value)
    dataSource.value = res.data.data
  } catch (error) {
    console.error('获取数据源详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 返回
const handleBack = () => {
  router.back()
}

// 编辑
const handleEdit = () => {
  router.push({
    path: `/datasource/edit/${datasourceId.value}`,
    query: { schema_id: schemaId.value },
  })
}

onMounted(() => {
  fetchDataSourceDetail()
})
</script>

<style scoped lang="scss">
.datasource-detail-page {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;

  .back-btn {
    display: flex;
    align-items: center;
    cursor: pointer;
    color: #606266;
    font-size: 14px;
    margin-right: 16px;

    &:hover {
      color: #409eff;
    }

    .el-icon {
      margin-right: 4px;
    }
  }

  .page-title {
    margin: 0;
    font-size: 20px;
    font-weight: 500;
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

.action-bar {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px 0;
}
</style>