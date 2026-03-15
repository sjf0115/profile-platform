<template>
  <div class="entity-list">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-left">
        <el-input
          v-model="searchForm.entity_name"
          placeholder="请输入实体名称"
          clearable
          style="width: 300px"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增实体
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-table 
      :data="tableData" 
      border 
      v-loading="loading" 
      style="width: 100%"
      :empty-text="loading ? '加载中...' : '暂无数据'"
    >
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="entity_name" label="实体名称" min-width="120" show-overflow-tooltip />
      <el-table-column prop="source_type" label="创建方式" min-width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.source_type === 1" type="info" size="small">系统预置</el-tag>
          <el-tag v-else type="success" size="small">自定义</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="creator" label="创建人" min-width="100" align="center" />
      <el-table-column label="操作" min-width="150" align="center" fixed="right">
        <template #default="{ row }">
          <div class="operation-btns">
            <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
            <el-button 
              v-if="row.source_type !== 1" 
              link 
              type="primary" 
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button 
              v-if="row.source_type !== 1" 
              link 
              type="danger" 
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="实体名称" prop="entity_name">
          <el-input v-model="formData.entity_name" placeholder="请输入实体名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="实体详情"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="实体ID">{{ detailData.entity_id }}</el-descriptions-item>
        <el-descriptions-item label="实体名称">{{ detailData.entity_name }}</el-descriptions-item>
        <el-descriptions-item label="创建方式">
          <el-tag v-if="detailData.source_type === 1" type="info">系统预置</el-tag>
          <el-tag v-else type="success">自定义</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detailData.creator || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(detailData.gmt_create) }}</el-descriptions-item>
        <el-descriptions-item label="修改时间">{{ formatDateTime(detailData.gmt_modified) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { entityApi, type Entity } from '@/api/entity'

// 搜索表单
const searchForm = reactive({
  entity_name: ''
})

// 表格数据
const tableData = ref<Entity[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增实体')
const formRef = ref<FormInstance>()
const isEdit = ref(false)

const formData = reactive({
  entity_id: '',
  entity_name: '',
  entity_desc: ''
})

const formRules = {
  entity_name: [{ required: true, message: '请输入实体名称', trigger: 'blur' }]
}

// 详情弹窗
const detailVisible = ref(false)
const detailData = ref<Partial<Entity>>({})

// 获取实体列表
const fetchList = async () => {
  loading.value = true
  try {
    const res = await entityApi.list({
      entity_name: searchForm.entity_name || undefined
    })
    tableData.value = res.data.data || []
    pagination.total = tableData.value.length
  } catch (error) {
    console.error('获取实体列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchList()
}

// 重置
const handleReset = () => {
  searchForm.entity_name = ''
  handleSearch()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增实体'
  formData.entity_id = ''
  formData.entity_name = ''
  formData.entity_desc = ''
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: Entity) => {
  isEdit.value = true
  dialogTitle.value = '编辑实体'
  formData.entity_id = row.entity_id
  formData.entity_name = row.entity_name
  formData.entity_desc = row.entity_desc || ''
  dialogVisible.value = true
}

// 详情
const handleDetail = (row: Entity) => {
  detailData.value = row
  detailVisible.value = true
}

// 删除
const handleDelete = (row: Entity) => {
  ElMessageBox.confirm('确认删除该实体吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await entityApi.delete(row.entity_id)
      ElMessage.success('删除成功')
      fetchList()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const submitData = {
          entity_id: formData.entity_id || undefined,
          entity_name: formData.entity_name,
          entity_desc: formData.entity_desc
        }
        await entityApi.save(submitData)
        ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
        dialogVisible.value = false
        fetchList()
      } catch (error) {
        console.error('保存失败:', error)
      }
    }
  })
}

// 分页
const handleSizeChange = (val: number) => {
  pagination.size = val
  fetchList()
}

const handlePageChange = (val: number) => {
  pagination.page = val
  fetchList()
}

// 格式化时间
const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.entity-list {
  .search-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .search-left {
      display: flex;
      gap: 12px;
      align-items: center;
    }
  }

  .operation-btns {
    display: flex;
    justify-content: center;
    gap: 8px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
