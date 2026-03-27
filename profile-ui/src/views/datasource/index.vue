<template>
  <div class="datasource-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span class="title">数据源管理</span>
        </div>
      </template>

      <!-- 搜索和操作区域 -->
      <div class="toolbar">
        <div class="left-actions">
          <el-button type="primary" :icon="Plus" @click="handleAdd">
            新增数据源
          </el-button>
          <el-button :icon="Upload">批量新增数据源</el-button>
        </div>
        <div class="right-filters">
          <el-select
            v-model="queryParams.datasource_type"
            placeholder="数据源类型"
            clearable
            style="width: 180px"
            @change="handleSearch"
          >
            <el-option
              v-for="item in dataSourceTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-input
            v-model="queryParams.datasource_name"
            placeholder="数据源名称"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            查询
          </el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="数据源信息" min-width="200">
          <template #default="{ row }">
            <div class="datasource-info">
              <div class="datasource-name">{{ row.datasource_name }}</div>
              <div class="datasource-type">
                <el-tag size="small" type="info">{{ row.datasource_type?.toUpperCase() }}</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="连接信息" min-width="280">
          <template #default="{ row }">
            <div class="connection-info" v-if="row.config">
              <div
                v-for="(item, index) in getConnectionInfo(row.config)"
                :key="index"
                class="info-item"
              >
                <span class="info-label">{{ item.label }}：</span>
                <span class="info-value">{{ item.value }}</span>
              </div>
            </div>
            <span v-else class="empty-text">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="datasource_desc" label="描述" min-width="150">
          <template #default="{ row }">
            <span class="desc-text">{{ row.datasource_desc || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.gmt_create) }}
          </template>
        </el-table-column>
        <el-table-column label="修改时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.gmt_modified) }}
          </template>
        </el-table-column>
        <el-table-column prop="modifier" label="修改人" width="100" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              查看
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.page_num"
          v-model:page-size="queryParams.page_size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, Search, Refresh } from '@element-plus/icons-vue'
import type { DataSource, DataSourceQueryParams } from '@/types'
import { dataSourceApi, dataSourceTypeApi } from '@/api/datasource'

const router = useRouter()

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

// 加载状态
const loading = ref(false)

// 表格数据
const tableData = ref<DataSource[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive<DataSourceQueryParams>({
  page_num: 1,
  page_size: 10,
  datasource_type: undefined,
  datasource_name: '',
})

// 数据源类型选项
const dataSourceTypeOptions = ref<{label: string; value: string}[]>([])

// 选中的数据
const selectedRows = ref<DataSource[]>([])

// 获取数据源列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await dataSourceApi.getList(queryParams)
    tableData.value = res.data.data || []
    total.value = res.data.data?.length || 0
  } catch (error) {
    console.error('获取数据源列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取数据源类型列表
const fetchDataSourceTypeList = async () => {
  try {
    const res = await dataSourceTypeApi.getList()
    dataSourceTypeOptions.value = (res.data.data || []).map((item: {key: string; value: string}) => ({
      label: item.value,
      value: item.key
    }))
  } catch (error) {
    console.error('获取数据源类型列表失败:', error)
  }
}

// 获取连接信息展示（从 config JSON 字符串中解析）
const getConnectionInfo = (config: string) => {
  const keyMap: Record<string, string> = {
    host: '主机地址',
    port: '端口',
    database: '数据库名',
    username: '用户名',
    instance: '实例名',
  }
  try {
    const configObj = JSON.parse(config)
    return Object.entries(configObj)
      .filter(([key]) => ['host', 'port', 'database', 'user_name', 'instance'].includes(key))
      .slice(0, 4)
      .map(([key, value]) => ({
        label: keyMap[key] || key,
        value: String(value),
      }))
  } catch (e) {
    return []
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page_num = 1
  fetchData()
}

// 重置
const handleReset = () => {
  queryParams.datasource_type = undefined
  queryParams.datasource_name = ''
  queryParams.page_num = 1
  fetchData()
}

// 新增 - 跳转到数据源类型选择页面
const handleAdd = () => {
  router.push('/datasource/select-type')
}

// 查看
const handleView = (row: DataSource) => {
  router.push({
    path: `/datasource/detail/${row.datasource_id}`,
  })
}

// 编辑
const handleEdit = (row: DataSource) => {
  router.push({
    path: `/datasource/edit/${row.datasource_id}`,
  })
}

// 删除
const handleDelete = (row: DataSource) => {
  ElMessageBox.confirm(
    `确定要删除数据源 "${row.datasource_name}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await dataSourceApi.delete(row.datasource_id)
        ElMessage.success('删除成功')
        fetchData()
      } catch (error) {
        console.error('删除失败:', error)
      }
    })
    .catch(() => {
      // 取消删除
    })
}

// 多选
const handleSelectionChange = (rows: DataSource[]) => {
  selectedRows.value = rows
}

// 分页大小变化
const handleSizeChange = (val: number) => {
  queryParams.page_size = val
  fetchData()
}

// 页码变化
const handleCurrentChange = (val: number) => {
  queryParams.page_num = val
  fetchData()
}

onMounted(() => {
  fetchData()
  fetchDataSourceTypeList()
})
</script>

<style scoped lang="scss">
.datasource-page {
  .page-card {
    min-height: calc(100vh - 104px);
  }

  .card-header {
    .title {
      font-size: 16px;
      font-weight: 600;
    }
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    flex-wrap: wrap;
    gap: 12px;

    .left-actions {
      display: flex;
      gap: 10px;
    }

    .right-filters {
      display: flex;
      gap: 10px;
      align-items: center;
    }
  }

  .datasource-info {
    .datasource-name {
      font-weight: 500;
      color: #303133;
      margin-bottom: 6px;
    }

    .datasource-type {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .connection-info {
    .info-item {
      display: flex;
      margin-bottom: 4px;
      font-size: 13px;

      .info-label {
        color: #909399;
        min-width: 70px;
      }

      .info-value {
        color: #606266;
        flex: 1;
      }
    }
  }

  .desc-text {
    color: #606266;
  }

  .empty-text {
    color: #909399;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
