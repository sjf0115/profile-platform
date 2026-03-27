<template>
  <div class="select-type-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="back-btn" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>
      <h2 class="page-title">选择数据源类型</h2>
    </div>

    <el-card class="page-card">
      <!-- 加载状态 -->
      <el-skeleton v-if="loading" :rows="6" animated />

      <!-- 数据源类型网格 -->
      <template v-else>
        <div class="datasource-type-grid">
          <div
            v-for="item in dataSourceTypes"
            :key="item.key"
            class="type-card"
            @click="handleSelect(item)"
          >
            <div class="type-icon" :style="{ backgroundColor: getIconColor(item.key) }">
              <component :is="getIcon(item.key)" />
            </div>
            <div class="type-info">
              <div class="type-name">{{ item.value }}</div>
              <div class="type-key">{{ item.key }}</div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <el-empty v-if="dataSourceTypes.length === 0" description="暂无可用数据源类型" />
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import type { DataSourceTypeItem } from '@/types'
import { dataSourceTypeApi } from '@/api/datasource'

const router = useRouter()

// 数据源类型列表
const dataSourceTypes = ref<DataSourceTypeItem[]>([])

// 加载状态
const loading = ref(false)

// 获取数据源类型列表
const fetchDataSourceTypes = async () => {
  loading.value = true
  try {
    const res = await dataSourceTypeApi.getList()
    dataSourceTypes.value = res.data.data || []
  } catch (error) {
    console.error('获取数据源类型列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 根据数据源类型 key 获取图标颜色
const getIconColor = (key: string) => {
  const colorMap: Record<string, string> = {
    clickhouse: '#FFCC00',
    mysql: '#4479A1',
    postgresql: '#336791',
    hologres: '#FF6A00',
    maxcompute: '#FF6A00',
    hbase: '#6D4C9A',
    hive: '#F5D300',
    elasticsearch: '#00A4E0',
    redis: '#DC382D',
    mongodb: '#47A248',
    kafka: '#231F20',
    oracle: '#F80000',
    ftp: '#0066CC',
    jdbc: '#909399',
  }
  return colorMap[key.toLowerCase()] || '#409EFF'
}

// 根据数据源类型 key 获取图标组件
const getIcon = (key: string) => {
  const iconMap: Record<string, string> = {
    clickhouse: 'TrendCharts',
    mysql: 'Coin',
    postgresql: 'DataAnalysis',
    hologres: 'DataLine',
    maxcompute: 'DataLine',
    hbase: 'Grid',
    hive: 'Box',
    elasticsearch: 'Search',
    redis: 'Stopwatch',
    mongodb: 'Collection',
    kafka: 'Message',
    oracle: 'Coin',
    ftp: 'Folder',
    jdbc: 'Connection',
  }
  return iconMap[key.toLowerCase()] || 'Connection'
}

// 选择数据源类型
const handleSelect = (item: DataSourceTypeItem) => {
  router.push({
    path: '/datasource/create',
    query: { type: item.key },
  })
}

// 返回
const handleBack = () => {
  router.back()
}

onMounted(() => {
  fetchDataSourceTypes()
})
</script>

<style scoped lang="scss">
.select-type-page {
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

  .page-card {
    min-height: calc(100vh - 140px);
  }

  .datasource-type-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    padding: 8px;
  }

  .type-card {
    display: flex;
    align-items: center;
    padding: 20px;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    background-color: #fff;

    &:hover {
      border-color: #409eff;
      box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.15);
      transform: translateY(-2px);
    }

    .type-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      color: #fff;
      font-size: 24px;
      flex-shrink: 0;
    }

    .type-info {
      flex: 1;
      min-width: 0;

      .type-name {
        font-size: 15px;
        font-weight: 500;
        color: #303133;
        margin-bottom: 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .type-key {
        font-size: 12px;
        color: #909399;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}
</style>
