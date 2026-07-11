<template>
  <div class="application-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span class="title">应用管理</span>
        </div>
      </template>

      <!-- 搜索和操作区域 -->
      <div class="toolbar">
        <div class="left-actions">
          <el-button type="primary" :icon="Plus" @click="handleAdd">
            新增应用
          </el-button>
        </div>
        <div class="right-filters">
          <el-select
            v-model="queryParams.status"
            placeholder="状态"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="2" />
          </el-select>
          <el-input
            v-model="queryParams.app_name"
            placeholder="应用名称"
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
        <el-table-column label="应用" min-width="200">
          <template #default="{ row }">
            <div class="app-info">
              <div class="app-name">{{ row.app_name }}</div>
              <div class="app-desc">{{ row.app_desc || '-' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="AppKey" min-width="200">
          <template #default="{ row }">
            <div class="app-key-cell">
              <span class="app-key-text">{{ maskAppKey(row.app_key) }}</span>
              <el-button link type="primary" size="small" @click="copyAppKey(row.app_key)">
                复制
              </el-button>
            </div>
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
        <el-table-column label="创建时间" width="160">
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
                  <el-dropdown-item @click="handleResetSecret(row)">重置秘钥</el-dropdown-item>
                  <el-dropdown-item divided @click="handleDelete(row)">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>

        <!-- 空状态 -->
        <template #empty>
          <el-empty description="暂无应用">
            <el-button type="primary" @click="handleAdd">创建应用</el-button>
          </el-empty>
        </template>
      </el-table>
    </el-card>

    <!-- 创建成功弹窗 -->
    <el-dialog
      v-model="showCreateDialog"
      title="应用创建成功"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="create-success">
        <el-icon :size="48" color="#67C23A"><CircleCheckFilled /></el-icon>
        <p class="success-text">应用创建成功！请妥善保存以下凭证信息：</p>
        <div class="credential-item">
          <span class="credential-label">AppKey：</span>
          <span class="credential-value">{{ createdAppKey }}</span>
          <el-button link type="primary" @click="copyText(createdAppKey)">复制</el-button>
        </div>
        <div class="credential-item">
          <span class="credential-label">AppSecret：</span>
          <span class="credential-value">{{ createdAppSecret }}</span>
          <el-button link type="primary" @click="copyText(createdAppSecret)">复制</el-button>
        </div>
        <el-alert
          title="AppSecret 只显示一次，关闭后将无法再次查看，只能重置！"
          type="warning"
          :closable="false"
          show-icon
          style="margin-top: 16px;"
        />
      </div>
      <template #footer>
        <el-button type="primary" @click="showCreateDialog = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, CircleCheckFilled, More } from '@element-plus/icons-vue'
import type { Application, ApplicationQueryParams } from '@/types'
import { applicationApi } from '@/api/application'
import { checkLineageDeletable } from '@/api/lineage'

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
const tableData = ref<Application[]>([])

// 查询参数
const queryParams = reactive<ApplicationQueryParams>({
  status: undefined,
  app_name: '',
})

// 创建成功弹窗
const showCreateDialog = ref(false)
const createdAppKey = ref('')
const createdAppSecret = ref('')

// 获取应用列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await applicationApi.getList(queryParams)
    tableData.value = res.data.data || []
  } catch (error) {
    console.error('获取应用列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 掩码 AppKey
const maskAppKey = (appKey?: string): string => {
  if (!appKey) return '-'
  if (appKey.length <= 8) return appKey
  return appKey.substring(0, 8) + '****'
}

// 复制 AppKey
const copyAppKey = async (appKey: string) => {
  try {
    await navigator.clipboard.writeText(appKey)
    ElMessage.success('已复制完整 AppKey')
  } catch (e) {
    ElMessage.error('复制失败')
  }
}

// 复制文本
const copyText = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch (e) {
    ElMessage.error('复制失败')
  }
}

// 搜索
const handleSearch = () => {
  fetchData()
}

// 重置
const handleReset = () => {
  queryParams.status = undefined
  queryParams.app_name = ''
  fetchData()
}

// 新增
const handleAdd = () => {
  router.push('/application/create')
}

// 查看详情
const handleView = (row: Application) => {
  router.push(`/application/detail/${row.app_key}`)
}

// 编辑
const handleEdit = (row: Application) => {
  router.push(`/application/edit/${row.app_key}`)
}

// 禁用/启用
const handleToggleStatus = (row: Application) => {
  const newStatus = row.status === 1 ? 2 : 1
  const action = newStatus === 1 ? '启用' : '禁用'

  ElMessageBox.confirm(`确定要${action}应用 "${row.app_name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await applicationApi.updateStatus(row.app_key!, newStatus)
        ElMessage.success(`${action}成功`)
        fetchData()
      } catch (error) {
        console.error(`${action}失败:`, error)
      }
    })
    .catch(() => {})
}

// 重置 Secret
const handleResetSecret = (row: Application) => {
  ElMessageBox.confirm(
    `确定要重置应用 "${row.app_name}" 的 AppSecret 吗？重置后旧 Secret 将失效。`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const res = await applicationApi.resetSecret(row.app_key!)
        const newSecret = res.data.data
        ElMessageBox.alert(
          `新的 AppSecret 为：<strong>${newSecret}</strong><br/><br/>请妥善保存，关闭后将无法再次查看。`,
          '重置成功',
          {
            dangerouslyUseHTMLString: true,
            confirmButtonText: '我知道了',
            type: 'success',
          }
        )
      } catch (error) {
        console.error('重置 Secret 失败:', error)
      }
    })
    .catch(() => {})
}

// 删除
const handleDelete = async (row: Application) => {
  if (!await checkLineageDeletable('application', row.app_key!, row.app_name)) return
  ElMessageBox.confirm(
    `确定要删除应用 "${row.app_name}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await applicationApi.delete(row.app_key!)
        ElMessage.success('删除成功')
        fetchData()
      } catch (error: any) {
        ElMessage.error(error?.response?.data?.message || '删除失败')
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchData()
})

// 监听路由参数，检查是否从创建页面返回
const checkCreateResult = () => {
  const raw = sessionStorage.getItem('createdApp')
  if (raw) {
    try {
      const createdApp = JSON.parse(raw)
      createdAppKey.value = createdApp.app_key
      createdAppSecret.value = createdApp.app_secret
      showCreateDialog.value = true
    } catch {
      // 解析失败忽略
    } finally {
      // 读取后立即清除，防止刷新或路由跳转后反复弹出
      sessionStorage.removeItem('createdApp')
    }
  }
}

onMounted(() => {
  checkCreateResult()
})
</script>

<style scoped lang="scss">
.application-page {
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

  .app-info {
    .app-name {
      font-weight: 500;
      color: #303133;
      margin-bottom: 4px;
    }

    .app-desc {
      font-size: 12px;
      color: #909399;
    }
  }

  .app-key-cell {
    display: flex;
    align-items: center;
    gap: 8px;

    .app-key-text {
      font-family: monospace;
      color: #606266;
    }
  }

  .create-success {
    text-align: center;
    padding: 20px 0;

    .success-text {
      margin: 16px 0;
      font-size: 14px;
      color: #606266;
    }

    .credential-item {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      margin: 12px 0;
      padding: 12px;
      background: #f5f7fa;
      border-radius: 6px;

      .credential-label {
        color: #909399;
        font-size: 13px;
      }

      .credential-value {
        font-family: monospace;
        color: #303133;
        font-size: 13px;
        word-break: break-all;
      }
    }
  }
}
</style>
