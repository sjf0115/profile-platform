<template>
  <div class="analysis-list-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">群组分析</h2>
        <el-tooltip content="管理已保存的群组分析，支持对群组标签分布进行对比分析">
          <el-icon><QuestionFilled /></el-icon>
        </el-tooltip>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          创建群组分析
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索分析名称"
        style="width: 300px"
        clearable
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      :data="analysisList"
      stripe
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column prop="analysis_name" label="分析名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="group_name" label="群组名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="group_count" label="群组规模" width="120" sortable>
        <template #default="{ row }">
          {{ row.group_count ?? '-' }}
        </template>
      </el-table-column>
      <el-table-column label="实体类型" width="180" show-overflow-tooltip>
        <template #default="{ row }">
          {{ formatEntityType(row) }}
        </template>
      </el-table-column>
      <el-table-column label="对比群组" width="100">
        <template #default="{ row }">
          {{ row.compare_group_ids?.length || 0 }} 个
        </template>
      </el-table-column>
      <el-table-column label="标签数" width="80">
        <template #default="{ row }">
          {{ row.label_ids?.length || 0 }}
        </template>
      </el-table-column>
      <el-table-column prop="creator" label="创建人" width="100" />
      <el-table-column prop="gmt_modified" label="更新时间" width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.gmt_modified) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
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
import { Search, QuestionFilled, Plus } from '@element-plus/icons-vue'
import { groupAnalysisApi } from '@/api/groupAnalysis'
import { checkLineageDeletable } from '@/api/lineage'
import type { GroupAnalysis } from '@/types'

const router = useRouter()

// 状态
const loading = ref(false)
const searchKeyword = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const analysisList = ref<GroupAnalysis[]>([])

// 获取群组分析列表
const fetchAnalysisList = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (searchKeyword.value) {
      params.analysis_name = searchKeyword.value
    }
    const res = await groupAnalysisApi.getAnalysisList(params)
    const allData = res.data.data || []
    total.value = allData.length
    // 前端分页
    const start = (pageNum.value - 1) * pageSize.value
    analysisList.value = allData.slice(start, start + pageSize.value)
  } catch (error) {
    console.error('获取群组分析列表失败:', error)
    ElMessage.error('获取群组分析列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pageNum.value = 1
  fetchAnalysisList()
}

// 重置
const handleReset = () => {
  searchKeyword.value = ''
  pageNum.value = 1
  fetchAnalysisList()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchAnalysisList()
}

const handlePageChange = (val: number) => {
  pageNum.value = val
  fetchAnalysisList()
}

// 创建
const handleCreate = () => {
  router.push('/group/analysis/create')
}

// 查看（进入详情页，直接分析）
const handleView = (row: GroupAnalysis) => {
  router.push(`/group/analysis/detail/${row.analysis_id}`)
}

// 编辑（进入创建页编辑模式）
const handleEdit = (row: GroupAnalysis) => {
  router.push(`/group/analysis/edit/${row.analysis_id}`)
}

// 删除
const handleDelete = async (row: GroupAnalysis) => {
  if (!await checkLineageDeletable('analysis', row.analysis_id!, row.analysis_name)) return
  try {
    await ElMessageBox.confirm(
      `确定要删除群组分析「${row.analysis_name}」吗？`,
      '确认删除',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
    await groupAnalysisApi.deleteAnalysis(row.analysis_id!)
    ElMessage.success('删除成功')
    fetchAnalysisList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 格式化实体类型
const formatEntityType = (row: GroupAnalysis): string => {
  if (!row.entity_identifier_name) return '-'
  if (row.entity_name && row.entity_identifier_name) {
    return `${row.entity_name} > ${row.entity_identifier_name}`
  }
  return row.entity_identifier_name
}

// 工具函数
const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  fetchAnalysisList()
})
</script>

<style scoped lang="scss">
.analysis-list-page {
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

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
