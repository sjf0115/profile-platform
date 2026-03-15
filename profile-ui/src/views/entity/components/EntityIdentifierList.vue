<template>
  <div class="entity-identifier-list">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-left">
        <el-input
          v-model="searchForm.entity_identifier_name"
          placeholder="请输入实体标识名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="searchForm.entity_id"
          placeholder="请选择实体"
          clearable
          style="width: 200px"
        >
          <el-option
            v-for="item in entityList"
            :key="item.entity_id"
            :label="item.entity_name"
            :value="item.entity_id"
          />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增实体标识
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
      <el-table-column prop="entity_identifier_name" label="实体标识名称" min-width="120" show-overflow-tooltip />
      <el-table-column prop="entity_name" label="实体" min-width="100" align="center" />
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
      <el-table-column prop="creator" label="创建人" min-width="100" align="center" />
      <el-table-column label="操作" min-width="180" align="center" fixed="right">
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
              :type="row.status === 1 ? 'warning' : 'success'" 
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
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
      width="560px"
      destroy-on-close
      class="entity-identifier-dialog"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="110px"
        class="dialog-form"
      >
        <el-form-item label="实体标识名称" prop="entity_identifier_name">
          <el-input 
            v-model="formData.entity_identifier_name" 
            placeholder="请输入实体标识名称"
            clearable
          />
        </el-form-item>
        <el-form-item label="所属实体" prop="entity_id">
          <el-select
            v-model="formData.entity_id"
            placeholder="请选择所属实体"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="item in entityList"
              :key="item.entity_id"
              :label="item.entity_name"
              :value="item.entity_id"
            />
          </el-select>
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
      title="实体标识详情"
      width="600px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="实体标识ID">{{ detailData.entity_identifier_id }}</el-descriptions-item>
        <el-descriptions-item label="实体标识名称">{{ detailData.entity_identifier_name }}</el-descriptions-item>
        <el-descriptions-item label="所属实体">{{ detailData.entity_name }}</el-descriptions-item>
        <el-descriptions-item label="实体ID">{{ detailData.entity_id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="detailData.status === 1" type="success">启用</el-tag>
          <el-tag v-else type="danger">禁用</el-tag>
        </el-descriptions-item>
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
import { entityIdentifierApi, type EntityIdentifier, entityApi, type Entity } from '@/api/entity'

// 搜索表单
const searchForm = reactive({
  entity_identifier_name: '',
  entity_id: ''
})

// 表格数据
const tableData = ref<EntityIdentifier[]>([])
const loading = ref(false)
const entityList = ref<Entity[]>([])

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增实体标识')
const formRef = ref<FormInstance>()
const isEdit = ref(false)

const formData = reactive({
  entity_identifier_id: '',
  entity_identifier_name: '',
  entity_identifier_desc: '',
  entity_id: ''
})

const formRules = {
  entity_identifier_name: [{ required: true, message: '请输入实体标识名称', trigger: 'blur' }],
  entity_id: [{ required: true, message: '请选择所属实体', trigger: 'change' }]
}

// 详情弹窗
const detailVisible = ref(false)
const detailData = ref<Partial<EntityIdentifier>>({})

// 获取实体标识列表
const fetchList = async () => {
  loading.value = true
  try {
    const res = await entityIdentifierApi.list({
      entity_identifier_name: searchForm.entity_identifier_name || undefined,
      entity_id: searchForm.entity_id || undefined
    })
    tableData.value = res.data.data || []
    pagination.total = tableData.value.length
  } catch (error) {
    console.error('获取实体标识列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取实体列表（用于下拉选择）
const fetchEntityList = async () => {
  try {
    const res = await entityApi.list()
    entityList.value = res.data.data || []
  } catch (error) {
    console.error('获取实体列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchList()
}

// 重置
const handleReset = () => {
  searchForm.entity_identifier_name = ''
  searchForm.entity_id = ''
  handleSearch()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增实体标识'
  formData.entity_identifier_id = ''
  formData.entity_identifier_name = ''
  formData.entity_identifier_desc = ''
  formData.entity_id = ''
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: EntityIdentifier) => {
  isEdit.value = true
  dialogTitle.value = '编辑实体标识'
  formData.entity_identifier_id = row.entity_identifier_id
  formData.entity_identifier_name = row.entity_identifier_name
  formData.entity_identifier_desc = row.entity_identifier_desc || ''
  formData.entity_id = row.entity_id
  dialogVisible.value = true
}

// 详情
const handleDetail = (row: EntityIdentifier) => {
  detailData.value = row
  detailVisible.value = true
}

// 启用/禁用
const handleToggleStatus = (row: EntityIdentifier) => {
  const action = row.status === 1 ? '禁用' : '启用'
  const newStatus = row.status === 1 ? 0 : 1
  ElMessageBox.confirm(`确认${action}该实体标识吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await entityIdentifierApi.save({
        entity_identifier_id: row.entity_identifier_id,
        status: newStatus
      })
      ElMessage.success(`${action}成功`)
      fetchList()
    } catch (error) {
      console.error(`${action}失败:`, error)
    }
  }).catch(() => {})
}

// 删除
const handleDelete = (row: EntityIdentifier) => {
  ElMessageBox.confirm('确认删除该实体标识吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await entityIdentifierApi.delete(row.entity_identifier_id)
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
          entity_identifier_id: formData.entity_identifier_id || undefined,
          entity_identifier_name: formData.entity_identifier_name,
          entity_identifier_desc: formData.entity_identifier_desc,
          entity_id: formData.entity_id
        }
        await entityIdentifierApi.save(submitData)
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
  fetchEntityList()
})
</script>

<style scoped lang="scss">
.entity-identifier-list {
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

// 弹窗样式优化
:deep(.entity-identifier-dialog) {
  .el-dialog__body {
    padding: 20px 24px 10px;
  }
  
  .dialog-form {
    .el-form-item {
      margin-bottom: 20px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .el-form-item__label {
        font-weight: 500;
      }
      
      .el-input__wrapper,
      .el-select {
        width: 100%;
      }
    }
  }
  
  .el-dialog__footer {
    padding: 16px 24px;
    border-top: 1px solid #e4e7ed;
  }
}
</style>
