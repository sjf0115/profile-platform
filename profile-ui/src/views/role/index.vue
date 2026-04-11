<template>
  <div class="role-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span class="title">角色管理</span>
        </div>
      </template>

      <!-- 搜索和操作区域 -->
      <div class="toolbar">
        <div class="left-actions">
          <el-button type="primary" :icon="Plus" @click="handleAdd">
            添加新角色
          </el-button>
        </div>
        <div class="right-filters">
          <el-select
            v-model="queryParams.role_type"
            placeholder="角色类型"
            clearable
            style="width: 180px"
            @change="handleSearch"
          >
            <el-option
              v-for="item in roleTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-input
            v-model="queryParams.role_name"
            placeholder="请输入角色名称"
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
      >
        <el-table-column prop="role_name" label="角色名称" min-width="150" />
        <el-table-column label="身份" min-width="120">
          <template #default="{ row }">
            <el-tag :type="row.role_type === 1 ? 'danger' : 'info'">
              {{ row.role_type === 1 ? '管理员' : '成员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="role_desc" label="角色描述" min-width="200">
          <template #default="{ row }">
            <span class="desc-text">{{ row.role_desc || '-' }}</span>
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
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
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
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑角色' : '添加新角色'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="角色名称" prop="role_name">
          <el-input
            v-model="formData.role_name"
            placeholder="请输入角色名称"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="角色类型" prop="role_type">
          <el-radio-group v-model="formData.role_type">
            <el-radio :label="1">管理员</el-radio>
            <el-radio :label="2">成员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色描述" prop="role_desc">
          <el-input
            v-model="formData.role_desc"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
            maxlength="255"
            show-word-limit
          />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { roleApi, type Role, type RoleQueryParams } from '@/api/role'

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
const tableData = ref<Role[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive<RoleQueryParams & { pageNum: number; pageSize: number }>({
  pageNum: 1,
  pageSize: 10,
  role_id: undefined,
  role_type: undefined,
  role_name: '',
  source_type: undefined,
})

// 角色类型选项
const roleTypeOptions = [
  { label: '管理员', value: 1 },
  { label: '成员', value: 2 },
]

// 获取角色列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await roleApi.getList({
      role_id: queryParams.role_id,
      role_type: queryParams.role_type,
      role_name: queryParams.role_name,
      source_type: queryParams.source_type,
    })
    tableData.value = res.data.data || []
    total.value = res.data.data?.length || 0
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.pageNum = 1
  fetchData()
}

// 重置
const handleReset = () => {
  queryParams.role_id = undefined
  queryParams.role_type = undefined
  queryParams.role_name = ''
  queryParams.source_type = undefined
  queryParams.pageNum = 1
  fetchData()
}

// 分页
const handleSizeChange = (val: number) => {
  queryParams.pageSize = val
  fetchData()
}

const handleCurrentChange = (val: number) => {
  queryParams.pageNum = val
  fetchData()
}

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const formData = reactive<Role>({
  role_type: 2,
  role_name: '',
  role_desc: '',
})

// 表单校验规则
const formRules: FormRules = {
  role_name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { max: 255, message: '角色名称不能超过255个字符', trigger: 'blur' },
  ],
  role_type: [
    { required: true, message: '请选择角色类型', trigger: 'change' },
  ],
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  formData.role_id = undefined
  formData.role_type = 2
  formData.role_name = ''
  formData.role_desc = ''
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: Role) => {
  isEdit.value = true
  formData.role_id = row.role_id
  formData.role_type = row.role_type
  formData.role_name = row.role_name
  formData.role_desc = row.role_desc || ''
  dialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value && formData.role_id) {
          await roleApi.update(formData.role_id, formData)
          ElMessage.success('编辑成功')
        } else {
          await roleApi.create(formData)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败')
      }
    }
  })
}

// 删除
const handleDelete = (row: Role) => {
  if (!row.role_id) return
  ElMessageBox.confirm(
    `确定要删除角色 "${row.role_name}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await roleApi.delete(row.role_id!)
        ElMessage.success('删除成功')
        fetchData()
      } catch (error) {
        console.error('删除失败:', error)
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {
      // 取消删除
    })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.role-page {
  padding: 20px;

  .page-card {
    min-height: calc(100vh - 140px);

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .title {
        font-size: 18px;
        font-weight: 500;
      }
    }
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

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

  .desc-text {
    color: #606266;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
