<template>
  <div class="engine-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span class="title">计算引擎管理</span>
        </div>
      </template>

      <!-- 搜索和操作区域 -->
      <div class="toolbar">
        <div class="left-actions">
          <el-button type="primary" :icon="Plus" @click="handleAdd">
            新增引擎
          </el-button>
        </div>
        <div class="right-filters">
          <el-select
            v-model="queryParams.engine_type"
            placeholder="引擎类型"
            clearable
            style="width: 180px"
            @change="handleSearch"
          >
            <el-option
              v-for="item in engineTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-input
            v-model="queryParams.engine_name"
            placeholder="引擎名称"
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
        <el-table-column label="引擎信息" min-width="200">
          <template #default="{ row }">
            <div class="engine-info">
              <div class="engine-name">
                {{ row.engine_name }}
                <el-tag v-if="row.is_default === 1" size="small" type="success" style="margin-left: 8px;">
                  默认
                </el-tag>
              </div>
              <div class="engine-type">
                <el-tag size="small" type="info">{{ row.engine_type?.toUpperCase() }}</el-tag>
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
        <el-table-column prop="engine_desc" label="描述" min-width="150">
          <template #default="{ row }">
            <span class="desc-text">{{ row.engine_desc || '-' }}</span>
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
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button 
              v-if="row.is_default !== 1" 
              link 
              type="warning" 
              @click="handleSetDefault(row)"
            >
              设为默认
            </el-button>
            <el-button 
              v-if="row.source_type === 2" 
              link 
              type="danger" 
              @click="handleDelete(row)"
            >
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
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import type { Engine, EngineQueryParams } from '@/types'
import { engineApi } from '@/api/engine'

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
const tableData = ref<Engine[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive<EngineQueryParams>({
  page_num: 1,
  page_size: 10,
  engine_type: undefined,
  engine_name: '',
})

// 引擎类型选项
const engineTypeOptions = ref([
  { label: 'ClickHouse', value: 'clickhouse' },
  { label: 'Doris', value: 'doris' },
  { label: 'Spark', value: 'spark' },
  { label: 'Flink', value: 'flink' },
])

// 选中的数据
const selectedRows = ref<Engine[]>([])

// 获取引擎列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await engineApi.getList(queryParams)
    tableData.value = res.data.data || []
    total.value = res.data.data?.length || 0
  } catch (error) {
    console.error('获取引擎列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取连接信息展示（从 config JSON 字符串中解析）
const getConnectionInfo = (config: string) => {
  const keyMap: Record<string, string> = {
    host: '主机地址',
    port: '端口',
    database: '数据库名',
    username: '用户名',
  }
  try {
    const configObj = JSON.parse(config)
    return Object.entries(configObj)
      .filter(([key]) => ['host', 'port', 'database', 'username'].includes(key))
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
  queryParams.engine_type = undefined
  queryParams.engine_name = ''
  queryParams.page_num = 1
  fetchData()
}

// 新增
const handleAdd = () => {
  router.push('/settings/engine/create')
}

// 编辑
const handleEdit = (row: Engine) => {
  router.push({
    path: `/settings/engine/edit/${row.engine_id}`,
  })
}

// 设为默认
const handleSetDefault = (row: Engine) => {
  ElMessageBox.confirm(
    `确定要将引擎 "${row.engine_name}" 设为默认引擎吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await engineApi.setDefault(row.engine_id!)
        ElMessage.success('设置默认引擎成功')
        fetchData()
      } catch (error) {
        console.error('设置默认引擎失败:', error)
      }
    })
    .catch(() => {
      // 取消设置
    })
}

// 删除
const handleDelete = (row: Engine) => {
  ElMessageBox.confirm(
    `确定要删除引擎 "${row.engine_name}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await engineApi.delete(row.engine_id!)
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
const handleSelectionChange = (rows: Engine[]) => {
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
})
</script>

<style scoped lang="scss">
.engine-page {
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

  .engine-info {
    .engine-name {
      font-weight: 500;
      color: #303133;
      margin-bottom: 6px;
    }

    .engine-type {
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
