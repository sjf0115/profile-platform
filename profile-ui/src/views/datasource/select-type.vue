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

      <!-- 按分类展示 -->
      <template v-else>
        <div
          v-for="category in categories"
          :key="category.id"
          class="schema-group"
        >
          <div class="group-title">{{ category.name }} ({{ category.schemas.length }})</div>
          <div class="schema-grid">
            <div
              v-for="item in category.schemas"
              :key="item.schema_id"
              class="schema-card"
              @click="handleSelect(item)"
            >
              <div class="schema-icon" :style="{ backgroundColor: getIconColor(item.schema_name) }">
                <component :is="getIcon(item.schema_name)" />
              </div>
              <div class="schema-info">
                <div class="schema-name">{{ item.schema_name }}</div>
                <div class="schema-desc">{{ category.name }}</div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import type { DataSourceSchema, SchemaCategory } from '@/types'
import { dataSourceSchemaApi } from '@/api/datasource'

const router = useRouter()

// 分类列表（带 schemas）
interface CategoryWithSchemas extends SchemaCategory {
  schemas: DataSourceSchema[]
}

const categories = ref<CategoryWithSchemas[]>([])

// 加载状态
const loading = ref(false)

// 获取所有分类及其下的 Schema
const fetchCategoriesAndSchemas = async () => {
  loading.value = true
  try {
    // 第一步：获取所有分类
    const categoryRes = await dataSourceSchemaApi.getCategories()
    const categoryList = categoryRes.data.data || []

    // 第二步：为每个分类获取 schema 列表
    const categoriesWithSchemas: CategoryWithSchemas[] = []

    for (const category of categoryList) {
      try {
        const schemaRes = await dataSourceSchemaApi.getList({ schema_type: category.id })
        categoriesWithSchemas.push({
          ...category,
          schemas: schemaRes.data.data || []
        })
      } catch (error) {
        console.error(`获取分类 ${category.name} 的 schemas 失败:`, error)
        categoriesWithSchemas.push({
          ...category,
          schemas: []
        })
      }
    }

    categories.value = categoriesWithSchemas
  } catch (error) {
    console.error('获取分类列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 根据数据源名称获取图标颜色
const getIconColor = (name: string) => {
  const colorMap: Record<string, string> = {
    MySQL: '#4479A1',
    PostgreSQL: '#336791',
    ClickHouse: '#FFCC00',
    Hologres: '#FF6A00',
    MaxCompute: '#FF6A00',
    HBase: '#6D4C9A',
    Hive: '#F5D300',
    Elasticsearch: '#00A4E0',
    Redis: '#DC382D',
    MongoDB: '#47A248',
    Kafka: '#231F20',
    Oracle: '#F80000',
    FTP: '#0066CC',
  }
  for (const key of Object.keys(colorMap)) {
    if (name.toLowerCase().includes(key.toLowerCase())) {
      return colorMap[key]
    }
  }
  return '#409EFF'
}

// 根据数据源名称获取图标组件
const getIcon = (name: string) => {
  const iconMap: Record<string, string> = {
    mysql: 'Coin',
    postgresql: 'DataAnalysis',
    clickhouse: 'TrendCharts',
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
  }
  for (const key of Object.keys(iconMap)) {
    if (name.toLowerCase().includes(key)) {
      return iconMap[key]
    }
  }
  return 'Connection'
}

// 选择 Schema
const handleSelect = (schema: DataSourceSchema) => {
  router.push({
    path: '/datasource/create',
    query: { schema_id: schema.schema_id },
  })
}

// 返回
const handleBack = () => {
  router.back()
}

onMounted(() => {
  fetchCategoriesAndSchemas()
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

  .schema-group {
    margin-bottom: 24px;

    .group-title {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
      margin-bottom: 12px;
      padding-left: 8px;
      border-left: 3px solid #409eff;
    }
  }

  .schema-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }

  .schema-card {
    display: flex;
    align-items: center;
    padding: 16px;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    background-color: #fff;

    &:hover {
      border-color: #409eff;
      box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.15);
    }

    .schema-icon {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 12px;
      color: #fff;
      font-size: 20px;
      flex-shrink: 0;
    }

    .schema-info {
      flex: 1;
      min-width: 0;

      .schema-name {
        font-size: 14px;
        font-weight: 500;
        color: #303133;
        margin-bottom: 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .schema-desc {
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
