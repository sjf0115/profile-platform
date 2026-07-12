<template>
  <div class="export-list-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span class="title">群组投递</span>
        </div>
      </template>

      <!-- 搜索和操作区域 -->
      <div class="toolbar">
        <div class="left-actions">
          <el-button type="primary" :icon="Plus" @click="handleCreate">
            创建投递
          </el-button>
        </div>
        <div class="right-filters">
          <el-select
            v-model="filterStatus"
            placeholder="状态"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="2" />
          </el-select>
          <el-input
            v-model="searchKeyword"
            placeholder="投递名称"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="exportList"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column prop="export_name" label="投递名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="投递方式" width="120">
          <template #default="{ row }">
            <el-tag :type="row.export_mode === 2 ? 'success' : 'primary'" size="small">
              {{ row.export_mode === 2 ? '应用投递' : '数据源投递' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="投递群组" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getGroupName(row) }}
          </template>
        </el-table-column>
        <el-table-column label="负责人" width="120">
          <template #default="{ row }">
            {{ row.owner_name || row.owner || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
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
        <el-table-column prop="gmt_create" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.gmt_create) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-dropdown trigger="click">
              <el-button link type="primary">
                <el-icon><More /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleExecute(row)">立即执行</el-dropdown-item>
                  <el-dropdown-item @click="handleViewTask(row)">执行任务</el-dropdown-item>
                  <el-dropdown-item @click="handleViewHistory(row)">执行历史</el-dropdown-item>
                  <el-dropdown-item divided @click="handleDelete(row)">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>

        <!-- 空状态 -->
        <template #empty>
          <el-empty description="暂无投递">
            <el-button type="primary" @click="handleCreate">创建投递</el-button>
          </el-empty>
        </template>
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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, More } from '@element-plus/icons-vue'
import { exportApi } from '@/api/export'
import { checkLineageDeletable } from '@/api/lineage'
import { groupApi } from '@/api/group'
import type { Export, ExportConfig, Group } from '@/types'

const router = useRouter()

// 状态
const loading = ref(false)
const searchKeyword = ref('')
const filterStatus = ref<number | undefined>(undefined)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const exportList = ref<Export[]>([])

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
    exportList.value = res.data.data || []
    total.value = exportList.value.length
  } catch (error) {
    console.error('获取投递列表失败:', error)
    ElMessage.error('获取投递列表失败')
  } finally {
    loading.value = false
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

// 执行任务
const handleViewTask = (row: Export) => {
  router.push(`/task?related_id=${row.export_id}`)
}

// 执行历史
const handleViewHistory = (row: Export) => {
  router.push(`/task/instance?related_id=${row.export_id}`)
}

// 切换状态
const handleToggleStatus = async (row: Export) => {
  const newStatus = row.status === 1 ? 2 : 1
  const action = newStatus === 1 ? '启用' : '停用'
  try {
    await ElMessageBox.confirm(`确认${action}投递「${row.export_name}」？`, '提示', {
      type: 'warning'
    })
    const res = await exportApi.updateStatus(row.export_id!, newStatus)
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
const handleDelete = async (row: Export) => {
  if (!await checkLineageDeletable('export', row.export_id!, row.export_name)) return
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
  if (isNaN(date.getTime())) return dateStr
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 关联群组
const getGroupName = (row: Export) => {
  const config = parseExportConfig(row.export_config)
  if (!config?.group_id) return '-'
  const group = groupMap.value.get(config.group_id)
  return group ? group.group_name : config.group_id
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
  fetchGroupList()
  fetchExportList()
})
</script>

<style scoped lang="scss">
.export-list-page {
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
