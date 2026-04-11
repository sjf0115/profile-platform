<template>
  <div class="user-page">
    <!-- 概览卡片 -->
    <el-card class="overview-card">
      <template #header>
        <div class="card-header">
          <span class="title">概览</span>
        </div>
      </template>
      <div class="overview-stats">
        <div class="stat-item">
          <div class="stat-icon blue">
            <el-icon><UserIcon /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">已加入用户</div>
            <div class="stat-value">{{ overview.total_count || 0 }}</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon orange">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">管理员</div>
            <div class="stat-value">{{ overview.admin_count || 0 }}</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon green">
            <el-icon><UserIcon /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">成员</div>
            <div class="stat-value">{{ overview.member_count || 0 }}</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon gray">
            <el-icon><CircleClose /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">无权限用户</div>
            <div class="stat-value">{{ overview.no_permission_count || 0 }}</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span class="title">用户列表</span>
        </div>
      </template>

      <!-- 搜索和操作区域 -->
      <div class="toolbar">
        <div class="left-actions">
          <el-button type="primary" :icon="Plus" @click="handleAdd">
            邀请用户
          </el-button>
        </div>
        <div class="right-filters">
          <el-input
            v-model="queryParams.keyword"
            placeholder="输入姓名或邮箱查找"
            clearable
            style="width: 240px"
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
        <el-table-column prop="user_name" label="姓名" min-width="150" />
        <el-table-column prop="email" label="邮箱" min-width="200">
          <template #default="{ row }">
            <span>{{ row.email || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="200">
          <template #default="{ row }">
            <span>{{ formatRoles(row.roles) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已加入' : '未加入' }}
            </el-tag>
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

    <!-- 邀请用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '邀请用户'"
      width="600px"
      destroy-on-close
    >
      <el-form
        v-if="isEdit"
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="姓名" prop="user_name">
          <el-input
            v-model="formData.user_name"
            placeholder="请输入姓名"
            maxlength="100"
            show-word-limit
            disabled
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="formData.email"
            placeholder="请输入邮箱"
            maxlength="100"
            show-word-limit
            disabled
          />
        </el-form-item>
        <el-form-item label="角色" prop="roles">
          <el-select
            v-model="formData.roles"
            multiple
            placeholder="请选择角色"
            style="width: 100%"
          >
            <el-option
              v-for="item in roleOptions"
              :key="item.role_id"
              :label="item.role_name"
              :value="item.role_id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <!-- 批量邀请表单 -->
      <div v-else class="batch-invite-form">
        <div
          v-for="(item, index) in inviteList"
          :key="index"
          class="invite-item"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item
                label="邮箱"
                :prop="`inviteList.${index}.email`"
                :rules="{ required: true, message: '请输入邮箱', trigger: 'blur' }"
              >
                <el-input
                  v-model="item.email"
                  placeholder="请输入"
                  maxlength="100"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item
                label="角色"
                :prop="`inviteList.${index}.roles`"
                :rules="{ required: true, message: '请选择角色', trigger: 'change', type: 'array' }"
              >
                <el-select
                  v-model="item.roles"
                  multiple
                  placeholder="请选择"
                  style="width: 100%"
                >
                  <el-option
                    v-for="role in roleOptions"
                    :key="role.role_id"
                    :label="role.role_name"
                    :value="role.role_id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        <el-button
          link
          type="primary"
          class="add-user-btn"
          @click="addInviteItem"
        >
          <el-icon><Plus /></el-icon>
          添加新用户
        </el-button>
      </div>
      
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
import { Plus, Search, Refresh, User as UserIcon, UserFilled, CircleClose } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { userApi, type User, type UserOverview } from '@/api/user'
import { roleApi } from '@/api/role'

// 概览数据
const overview = reactive<UserOverview>({
  total_count: 0,
  admin_count: 0,
  member_count: 0,
  no_permission_count: 0,
})

// 加载状态
const loading = ref(false)

// 表格数据
const tableData = ref<User[]>([])
const total = ref(0)

// 查询参数
const queryParams = reactive<{
  page_num: number
  page_size: number
  keyword?: string
}>({
  page_num: 1,
  page_size: 10,
  keyword: '',
})

// 角色选项
const roleOptions = ref<any[]>([])

// 批量邀请列表
interface InviteItem {
  email: string
  roles: string[]
}
const inviteList = ref<InviteItem[]>([{ email: '', roles: [] }])

// 添加邀请项
const addInviteItem = () => {
  inviteList.value.push({ email: '', roles: [] })
}

// 移除邀请项
const removeInviteItem = (index: number) => {
  inviteList.value.splice(index, 1)
}

// 获取概览数据
const fetchOverview = async () => {
  try {
    const res = await userApi.getOverview()
    if (res.data.data) {
      Object.assign(overview, res.data.data)
    }
  } catch (error) {
    console.error('获取概览数据失败:', error)
  }
}

// 获取用户列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await userApi.getList({
      user_name: queryParams.keyword,
    })
    tableData.value = res.data.data || []
    total.value = res.data.data?.length || 0
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 获取角色列表
const fetchRoleOptions = async () => {
  try {
    const res = await roleApi.getList()
    roleOptions.value = res.data.data || []
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

// 格式化角色显示
const formatRoles = (roles: any[]) => {
  if (!roles || roles.length === 0) return '-'
  return roles.map((r) => r.role_name || r).join('、')
}

// 搜索
const handleSearch = () => {
  queryParams.page_num = 1
  fetchData()
}

// 重置
const handleReset = () => {
  queryParams.keyword = ''
  queryParams.page_num = 1
  fetchData()
}

// 分页
const handleSizeChange = (val: number) => {
  queryParams.page_size = val
  fetchData()
}

const handleCurrentChange = (val: number) => {
  queryParams.page_num = val
  fetchData()
}

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const formData = reactive<{
  user_id?: string
  user_name: string
  email: string
  roles: string[]
}>({
  user_name: '',
  email: '',
  roles: [],
})

// 表单校验规则
const formRules: FormRules = {
  user_name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 100, message: '姓名不能超过100个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  inviteList.value = [{ email: '', roles: [] }]
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: User) => {
  isEdit.value = true
  formData.user_id = row.user_id
  formData.user_name = row.user_name || ''
  formData.email = row.email || ''
  formData.roles = row.roles || []
  dialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  if (isEdit.value) {
    // 编辑模式
    if (!formRef.value) return
    await formRef.value.validate(async (valid) => {
      if (valid) {
        try {
          await userApi.update(formData.user_id!, formData as User)
          ElMessage.success('编辑成功')
          dialogVisible.value = false
          fetchData()
          fetchOverview()
        } catch (error) {
          console.error('保存失败:', error)
          ElMessage.error('保存失败')
        }
      }
    })
  } else {
    // 批量邀请模式
    const validItems = inviteList.value.filter(
      (item) => item.email && item.roles.length > 0
    )
    if (validItems.length === 0) {
      ElMessage.warning('请至少填写一个有效的用户信息')
      return
    }
    try {
      for (const item of validItems) {
        await userApi.create({
          user_name: item.email.split('@')[0],
          email: item.email,
          roles: item.roles,
        } as User)
      }
      ElMessage.success(`成功邀请 ${validItems.length} 个用户`)
      dialogVisible.value = false
      fetchData()
      fetchOverview()
    } catch (error) {
      console.error('邀请失败:', error)
      ElMessage.error('邀请失败')
    }
  }
}

// 删除
const handleDelete = (row: User) => {
  if (!row.user_id) return
  ElMessageBox.confirm(
    `确定要删除用户 "${row.user_name}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await userApi.delete(row.user_id!)
        ElMessage.success('删除成功')
        fetchData()
        fetchOverview()
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
  fetchOverview()
  fetchData()
  fetchRoleOptions()
})
</script>

<style scoped lang="scss">
.user-page {
  padding: 20px;

  .overview-card {
    margin-bottom: 20px;

    .card-header {
      .title {
        font-size: 16px;
        font-weight: 500;
      }
    }

    .overview-stats {
      display: flex;
      gap: 20px;

      .stat-item {
        flex: 1;
        display: flex;
        align-items: center;
        padding: 20px;
        background-color: #f5f7fa;
        border-radius: 8px;

        .stat-icon {
          width: 48px;
          height: 48px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;

          &.blue {
            background-color: #e6f2ff;
            color: #409eff;
          }

          &.orange {
            background-color: #fff3e0;
            color: #ff9800;
          }

          &.green {
            background-color: #e8f5e9;
            color: #4caf50;
          }

          &.gray {
            background-color: #f5f5f5;
            color: #909399;
          }

          .el-icon {
            font-size: 24px;
          }
        }

        .stat-info {
          .stat-label {
            font-size: 14px;
            color: #606266;
            margin-bottom: 8px;
          }

          .stat-value {
            font-size: 24px;
            font-weight: 600;
            color: #303133;
          }
        }
      }
    }
  }

  .list-card {
    .card-header {
      .title {
        font-size: 16px;
        font-weight: 500;
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

    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .batch-invite-form {
    .invite-item {
      margin-bottom: 16px;
    }

    .add-user-btn {
      margin-top: 8px;
    }
  }
}
</style>
