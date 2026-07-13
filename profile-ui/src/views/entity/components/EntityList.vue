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
      <el-table-column prop="status" label="状态" min-width="80" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success" size="small">启用</el-tag>
          <el-tag v-else type="danger" size="small">禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="source_type" label="创建方式" min-width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.source_type === 1" type="info" size="small">系统预置</el-tag>
          <el-tag v-else type="success" size="small">自定义</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="creator_name" label="创建人" min-width="100" align="center">
        <template #default="{ row }">
          {{ row.creator_name || row.creator || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="gmt_create" label="创建时间" min-width="160" align="center">
        <template #default="{ row }">
          {{ formatDateTime(row.gmt_create) }}
        </template>
      </el-table-column>
      <el-table-column prop="gmt_modified" label="修改时间" min-width="160" align="center">
        <template #default="{ row }">
          {{ formatDateTime(row.gmt_modified) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleView(row)">查看</el-button>
          <el-button 
            v-if="row.source_type !== 1" 
            link 
            type="primary" 
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button 
            v-if="row.source_type !== 1" 
            link 
            type="primary" 
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-dropdown v-if="row.source_type !== 1" trigger="click">
            <el-button link type="primary">
              <el-icon><More /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item divided @click="handleDelete(row)">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, More } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { entityApi, type Entity } from '@/api/entity'

const router = useRouter()

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
const editingEntityId = ref('')

const formData = reactive({
  entity_name: ''
})

const formRules = {
  entity_name: [{ required: true, message: '请输入实体名称', trigger: 'blur' }]
}

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
  formData.entity_name = ''
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: Entity) => {
  isEdit.value = true
  dialogTitle.value = '编辑实体'
  editingEntityId.value = row.entity_id
  formData.entity_name = row.entity_name
  dialogVisible.value = true
}

// 查看（跳转详情页）
const handleView = (row: Entity) => {
  router.push(`/entity/detail/${row.entity_id}`)
}

// 启用/禁用
const handleToggleStatus = (row: Entity) => {
  const action = row.status === 1 ? '禁用' : '启用'
  const newStatus = row.status === 1 ? 2 : 1
  ElMessageBox.confirm(`确认${action}该实体吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await entityApi.updateStatus(row.entity_id, newStatus)
      ElMessage.success(`${action}成功`)
      fetchList()
    } catch (error) {
      console.error(`${action}失败:`, error)
    }
  }).catch(() => {})
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
        if (isEdit.value) {
          await entityApi.update(editingEntityId.value, {
            entity_name: formData.entity_name
          })
          ElMessage.success('编辑成功')
        } else {
          await entityApi.create({
            entity_name: formData.entity_name
          })
          ElMessage.success('新增成功')
        }
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
  if (isNaN(date.getTime())) return dateStr
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
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

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
