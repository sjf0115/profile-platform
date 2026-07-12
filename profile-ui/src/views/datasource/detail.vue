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
        <!-- 基础信息 -->
        <div class="detail-section">
          <h3 class="section-title">基本信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="数据源名称">
              {{ dataSource.datasource_name }}
            </el-descriptions-item>
            <el-descriptions-item label="数据源类型">
              <el-tag size="small" type="info">{{ dataSource.datasource_type?.toUpperCase() }}</el-tag>
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
            <el-descriptions-item label="负责人">
              {{ dataSource.owner_name || dataSource.owner || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建人">
              {{ dataSource.creator_name || dataSource.creator || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="修改人">
              {{ dataSource.modifier_name || dataSource.modifier || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 连接信息 - 根据配置动态渲染 -->
        <div class="detail-section">
          <h3 class="section-title">连接信息</h3>
          <el-descriptions :column="2" border v-if="pluginParams.length > 0">
            <el-descriptions-item 
              v-for="param in pluginParams" 
              :key="param.field"
              :label="param.title"
            >
              {{ getConfigValue(param) }}
            </el-descriptions-item>
          </el-descriptions>
          <el-empty v-else-if="loadingConfig" description="加载配置中..." />
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
import type { DataSource, PluginParam } from '@/types'
import { dataSourceApi, dataSourceTypeApi } from '@/api/datasource'

const route = useRoute()
const router = useRouter()

// 数据源ID
const datasourceId = computed(() => route.params.id as string)

// 加载状态
const loading = ref(false)
const loadingConfig = ref(false)

// 数据源详情
const dataSource = ref<DataSource | null>(null)

// 插件参数列表（动态表单配置）
const pluginParams = ref<PluginParam[]>([])

// 解析后的配置对象
const parsedConfig = computed(() => {
  if (!dataSource.value?.config) return {}
  try {
    return typeof dataSource.value.config === 'string'
      ? JSON.parse(dataSource.value.config)
      : dataSource.value.config
  } catch (e) {
    console.error('解析 config 失败:', e)
    return {}
  }
})

// 获取配置项的显示值
const getConfigValue = (param: PluginParam): string => {
  const value = parsedConfig.value[param.field]
  if (value === undefined || value === null || value === '') {
    return '-'
  }
  // 密码字段脱敏显示
  if (isPasswordField(param)) {
    return '******'
  }
  return String(value)
}

// 判断是否为密码字段
const isPasswordField = (param: PluginParam): boolean => {
  // 根据 field 名称判断（后端 config 中 field=password 标识密码字段）
  return param.field === 'password'
}

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
    // 获取到数据源后，加载对应的配置
    if (dataSource.value?.datasource_type) {
      await fetchDataSourceConfig(dataSource.value.datasource_type)
    }
  } catch (error) {
    console.error('获取数据源详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取数据源类型配置（动态表单配置）
const fetchDataSourceConfig = async (type: string) => {
  loadingConfig.value = true
  try {
    const res = await dataSourceTypeApi.getConfig(type)
    let configData = res.data.data
    
    // 后端返回的是 JSON 字符串，需要解析
    if (typeof configData === 'string') {
      try {
        configData = JSON.parse(configData)
      } catch (e) {
        console.error('解析配置数据失败:', e)
        return
      }
    }
    
    if (configData && Array.isArray(configData)) {
      pluginParams.value = configData
    }
  } catch (error) {
    console.error('获取数据源配置失败:', error)
  } finally {
    loadingConfig.value = false
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