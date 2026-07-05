<template>
  <div class="export-list-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">群组投递</h2>
        <el-tooltip content="将群组数据投递到外部数据源或应用">
          <el-icon><QuestionFilled /></el-icon>
        </el-tooltip>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          创建投递
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索投递名称"
        style="width: 300px"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="filterDeliveryType" placeholder="投递方式" style="width: 150px" clearable>
        <el-option label="数据源投递" value="datasource" />
        <el-option label="应用投递" value="application" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="状态" style="width: 120px" clearable>
        <el-option label="启用" :value="1" />
        <el-option label="停用" :value="2" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      :data="exportList"
      stripe
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column prop="export_name" label="投递名称" min-width="150" show-overflow-tooltip />
      <el-table-column label="投递方式" width="120">
        <template #default="{ row }">
          <el-tag :type="getDeliveryTypeTag(row)" size="small">
            {{ getDeliveryTypeText(row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="目标类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getTargetTypeTag(row)" size="small">
            {{ getTargetTypeText(row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="关联群组" width="150" show-overflow-tooltip>
        <template #default="{ row }">
          {{ getGroupName(row) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最新执行" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.latest_instance" :type="getInstanceStatusType(row.latest_instance.status)" size="small">
            {{ getInstanceStatusText(row.latest_instance.status) }}
          </el-tag>
          <span v-else class="empty-text">未执行</span>
        </template>
      </el-table-column>
      <el-table-column prop="gmt_create" label="创建时间" width="150">
        <template #default="{ row }">
          {{ formatDateTime(row.gmt_create) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">详情</el-button>
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleExecute(row)">执行</el-button>
          <el-dropdown trigger="click">
            <el-button link type="primary">
              <el-icon><More /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleToggleStatus(row)">
                  {{ row.status === 1 ? '停用' : '启用' }}
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleDelete(row)">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, QuestionFilled, More } from '@element-plus/icons-vue'
import { exportApi } from '@/api/export'
import { dataSourceApi } from '@/api/datasource'
import { groupApi } from '@/api/group'
import type { Export, ExportConfig, DataSource, Group } from '@/types'

const router = useRouter()

// 状态
const loading = ref(false)
const searchKeyword = ref('')
const filterDeliveryType = ref('')
const filterStatus = ref<number | undefined>(undefined)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const exportList = ref<Export[]>([])

// 数据源列表（用于目标类型推断）
const datasourceMap = ref<Map<string, DataSource>>(new Map())

// 群组列表（用于群组名称显示）
const groupMap = ref<Map<string, Group>>(new Map())

// 获取投递列表
const fetchExportList = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (searchKeyword.value) {
      params.export_name = searchKeyword.value
    }
    if (filterStatus.value !== undefined) {
      params.status = filterStatus.value
    }

    const res = await exportApi.getList(params)
    let list = res.data.data || []

    // 前端过滤投递方式
    if (filterDeliveryType.value) {
      list = list.filter(item => item.export_mode === (filterDeliveryType.value === 'application' ? 2 : 1))
    }

    exportList.value = list
    total.value = list.length
  } catch (error) {
    console.error('获取投递列表失败:', error)
    ElMessage.error('获取投递列表失败')
  } finally {
    loading.value = false
  }
}

// 加载数据源列表（构建 ID→数据源 映射）
const fetchDatasourceList = async () => {
  try {
    const res = await dataSourceApi.getList()
    const list = res.data.data || []
    const map = new Map<string, DataSource>()
    list.forEach((ds: DataSource) => {
      if (ds.datasource_id) map.set(ds.datasource_id, ds)
    })
    datasourceMap.value = map
  } catch (error) {
    console.error('获取数据源列表失败:', error)
  }
}

// 加载群组列表（构建 ID→群组 映射）
const fetchGroupList = async () => {
  try {
    const res = await groupApi.getList({})
    const list = res.data.data || []
    const map = new Map<string, Group>()
    list.forEach((g: Group) => map.set(g.group_id, g))
    groupMap.value = map
  } catch (error) {
    console.error('获取群组列表失败:', error)
  }
}

// 解析投递配置
const parseExportConfig = (configStr?: string): ExportConfig | null => {
  if (!configStr) return null
  try {
    return JSON.parse(configStr)
  } catch {
    return null
  }
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  fetchExportList()
}

// 重置
const handleReset = () => {
  searchKeyword.value = ''
  filterDeliveryType.value = ''
  filterStatus.value = undefined
  pageNum.value = 1
  fetchExportList()
}

// 分页
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchExportList()
}

const handlePageChange = (val: number) => {
  pageNum.value = val
  fetchExportList()
}

// 创建投递
const handleCreate = () => {
  router.push('/group/export/create')
}

// 查看详情
const handleView = (row: Export) => {
  router.push(`/group/export/detail/${row.export_id}`)
}

// 编辑
const handleEdit = (row: Export) => {
  router.push(`/group/export/edit/${row.export_id}`)
}

// 立即执行
const handleExecute = async (row: Export) => {
  try {
    await ElMessageBox.confirm(
      `确认立即执行投递「${row.export_name}」？`,
      '提示',
      { type: 'warning' }
    )
    const res = await exportApi.execute(row.export_id!)
    if (res.data.code === 0) {
      ElMessage.success('投递任务已提交，请稍后查看执行结果')
      fetchExportList()
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
const handleToggleStatus = async (row: Export) => {
  const newStatus = row.status === 1 ? 2 : 1
  const action = newStatus === 1 ? '启用' : '停用'
  try {
    await ElMessageBox.confirm(`确认${action}投递「${row.export_name}」？`, '提示', {
      type: 'warning'
    })
    const res = await exportApi.save({
      export_id: row.export_id,
      status: newStatus
    })
    if (res.data.code === 0) {
      ElMessage.success(`${action}成功`)
      fetchExportList()
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
const handleDelete = (row: Export) => {
  ElMessageBox.confirm('确认删除该投递吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await exportApi.delete(row.export_id!)
      ElMessage.success('删除成功')
      fetchExportList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 工具函数
const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 投递方式
const getDeliveryTypeTag = (row: Export) => {
  return row.export_mode === 2 ? 'success' : 'primary'
}

const getDeliveryTypeText = (row: Export) => {
  return row.export_mode === 2 ? '应用投递' : '数据源投递'
}

// 目标类型（基于数据源类型推断）
const getTargetTypeTag = (row: Export) => {
  if (row.export_mode === 2) return 'success' // 应用投递
  const config = parseExportConfig(row.export_config)
  if (!config?.datasource_id) return 'info'
  const ds = datasourceMap.value.get(config.datasource_id)
  if (!ds) return 'info'
  const type = ds.datasource_type?.toLowerCase() || ''
  if (['mysql', 'clickhouse', 'postgresql', 'oracle', 'hive', 'doris', 'jdbc'].includes(type)) return 'primary'
  if (['minio', 'hdfs', 'oss', 's3'].includes(type)) return 'success'
  if (['kafka', 'rabbitmq', 'rocketmq'].includes(type)) return 'warning'
  if (['elasticsearch', 'es'].includes(type)) return 'danger'
  return 'info'
}

const getTargetTypeText = (row: Export) => {
  if (row.export_mode === 2) return '应用'
  const config = parseExportConfig(row.export_config)
  if (!config?.datasource_id) return '-'
  const ds = datasourceMap.value.get(config.datasource_id)
  if (!ds) return '-'
  const type = ds.datasource_type?.toLowerCase() || ''
  if (['mysql', 'clickhouse', 'postgresql', 'oracle', 'hive', 'doris', 'jdbc'].includes(type)) return '数据表'
  if (['minio', 'hdfs', 'oss', 's3'].includes(type)) return '文件'
  if (['kafka', 'rabbitmq', 'rocketmq'].includes(type)) return '消息'
  if (['elasticsearch', 'es'].includes(type)) return '索引'
  return '-'
}

// 关联群组
const getGroupName = (row: Export) => {
  const config = parseExportConfig(row.export_config)
  if (!config?.group_id) return '-'
  const group = groupMap.value.get(config.group_id)
  return group ? group.group_name : config.group_id
}

// 状态
const getStatusType = (status?: number) => {
  return status === 1 ? 'success' : 'danger'
}

const getStatusText = (status?: number) => {
  return status === 1 ? '启用' : '停用'
}

// 执行状态
const getInstanceStatusType = (status?: number) => {
  switch (status) {
    case 1: return 'info'
    case 2: return 'warning'
    case 3: return 'success'
    case 4: return 'danger'
    default: return 'info'
  }
}

const getInstanceStatusText = (status?: number) => {
  switch (status) {
    case 1: return '未运行'
    case 2: return '运行中'
    case 3: return '运行失败'
    case 4: return '运行成功'
    default: return '-'
  }
}

onMounted(() => {
  fetchDatasourceList()
  fetchGroupList()
  fetchExportList()
})
</script>

<style scoped lang="scss">
.export-list-page {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 8px;

      .page-title {
        margin: 0;
        font-size: 20px;
        font-weight: 500;
      }
    }
  }

  .search-bar {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
  }

  .el-table {
    flex: 1;
  }

  .empty-text {
    color: #999;
    font-size: 13px;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
