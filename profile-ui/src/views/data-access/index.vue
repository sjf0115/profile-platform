<template>
  <div class="data-access-page">
    <div class="page-header">
      <h2>数据访问控制</h2>
      <p class="page-desc">管理平台资源的访问权限，包括授权管理、权限审计等功能</p>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- Tab1: 权限申请 -->
      <el-tab-pane label="权限申请" name="apply">
        <div class="apply-section">
          <el-button type="primary" :icon="Plus" @click="showApplyDialog = true">新建申请</el-button>
          <el-table v-loading="mineLoading" :data="mineList" stripe border style="margin-top: 16px">
            <el-table-column label="申请单ID" min-width="200" prop="apply_id" />
            <el-table-column label="申请理由" min-width="200" prop="apply_reason" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="审批人" width="120" prop="approver" />
            <el-table-column label="提交时间" width="160" prop="gmt_create" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.status === 1" link type="warning" size="small" @click="handleCancel(row)">撤销</el-button>
                <el-button link type="primary" size="small" @click="handleViewItems(row.apply_id)">明细</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- Tab2: 主动授权 -->
      <el-tab-pane label="主动授权" name="grant">
        <div class="grant-section">
          <el-form :inline="true" :model="grantQuery" class="query-form">
            <el-form-item label="资源类型">
              <el-select v-model="grantQuery.resourceType" placeholder="请选择" clearable style="width: 160px">
                <el-option label="数据源" value="05" />
                <el-option label="数据集" value="06" />
                <el-option label="标签" value="08" />
                <el-option label="群组" value="09" />
                <el-option label="投递" value="10" />
                <el-option label="事件" value="12" />
                <el-option label="应用" value="21" />
                <el-option label="群组分析" value="22" />
              </el-select>
            </el-form-item>
            <el-form-item label="资源ID">
              <el-input v-model="grantQuery.resourceId" placeholder="请输入资源ID" clearable style="width: 240px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleQuery">查询</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="grantLoading" :data="grantList" stripe border>
            <el-table-column label="资源类型" width="120">
              <template #default="{ row }">
                {{ resourceTypeLabel(row.resource_type) }}
              </template>
            </el-table-column>
            <el-table-column label="资源ID" min-width="180" prop="resource_id" />
            <el-table-column label="受权者" min-width="140">
              <template #default="{ row }">
                <el-tag :type="row.grantee_type === 1 ? 'primary' : 'success'" size="small">
                  {{ row.grantee_type === 1 ? '用户' : '角色' }}
                </el-tag>
                <span style="margin-left: 6px">{{ row.grantee_id }}</span>
              </template>
            </el-table-column>
            <el-table-column label="权限" width="100">
              <template #default="{ row }">
                <el-tag :type="actionTagType(row.action)" size="small">
                  {{ actionLabel(row.action) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="过期时间" width="160">
              <template #default="{ row }">
                {{ row.expire_time || '永久' }}
              </template>
            </el-table-column>
            <el-table-column label="授权人" width="120" prop="creator" />
            <el-table-column label="创建时间" width="160" prop="gmt_create" />
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-button link type="danger" size="small" @click="handleRevoke(row)">回收</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- Tab3: 权限审批 -->
      <el-tab-pane label="权限审批" name="approval">
        <el-table v-loading="todoLoading" :data="todoList" stripe border>
          <el-table-column label="申请单ID" min-width="200" prop="apply_id" />
          <el-table-column label="申请人" width="120" prop="applicant" />
          <el-table-column label="申请理由" min-width="200" prop="apply_reason" />
          <el-table-column label="提交时间" width="160" prop="gmt_create" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleViewItems(row.apply_id)">明细</el-button>
              <el-button link type="success" size="small" @click="handleApprove(row)">通过</el-button>
              <el-button link type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Tab4: 申请记录 -->
      <el-tab-pane label="申请记录" name="applyHistory">
        <el-table v-loading="mineLoading" :data="mineList" stripe border>
          <el-table-column label="申请单ID" min-width="200" prop="apply_id" />
          <el-table-column label="申请理由" min-width="200" prop="apply_reason" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="审批人" width="120" prop="approver" />
          <el-table-column label="审批备注" min-width="150" prop="approve_remark" />
          <el-table-column label="提交时间" width="160" prop="gmt_create" />
          <el-table-column label="审批时间" width="160" prop="approve_time" />
        </el-table>
      </el-tab-pane>

      <!-- Tab5: 审批记录 -->
      <el-tab-pane label="审批记录" name="approvalHistory">
        <el-table v-loading="approvedLoading" :data="approvedList" stripe border>
          <el-table-column label="申请单ID" min-width="200" prop="apply_id" />
          <el-table-column label="申请人" width="120" prop="applicant" />
          <el-table-column label="申请理由" min-width="200" prop="apply_reason" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="审批备注" min-width="150" prop="approve_remark" />
          <el-table-column label="审批时间" width="160" prop="approve_time" />
        </el-table>
      </el-tab-pane>

      <!-- Tab6: 权限审计 -->
      <el-tab-pane label="权限审计" name="audit">
        <div class="audit-section">
          <el-form :inline="true" :model="auditQuery" class="query-form">
            <el-form-item label="资源类型">
              <el-select v-model="auditQuery.resourceType" placeholder="请选择" clearable style="width: 160px">
                <el-option label="数据源" value="05" />
                <el-option label="数据集" value="06" />
                <el-option label="标签" value="08" />
                <el-option label="群组" value="09" />
                <el-option label="投递" value="10" />
                <el-option label="事件" value="12" />
                <el-option label="应用" value="21" />
                <el-option label="群组分析" value="22" />
              </el-select>
            </el-form-item>
            <el-form-item label="资源ID">
              <el-input v-model="auditQuery.resourceId" placeholder="请输入资源ID" clearable style="width: 240px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleAuditQuery">查询</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="auditLoading" :data="auditList" stripe border>
            <el-table-column label="资源类型" width="120">
              <template #default="{ row }">
                {{ resourceTypeLabel(row.resource_type) }}
              </template>
            </el-table-column>
            <el-table-column label="资源ID" min-width="180" prop="resource_id" />
            <el-table-column label="受权者类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.grantee_type === 1 ? 'primary' : 'success'" size="small">
                  {{ row.grantee_type === 1 ? '用户' : '角色' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="受权者ID" min-width="140" prop="grantee_id" />
            <el-table-column label="权限动作" width="100">
              <template #default="{ row }">
                <el-tag :type="actionTagType(row.action)" size="small">
                  {{ actionLabel(row.action) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="授权人" width="120" prop="creator" />
            <el-table-column label="创建时间" width="160" prop="gmt_create" />
            <el-table-column label="过期时间" width="160">
              <template #default="{ row }">
                {{ row.expire_time || '永久' }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 新建申请弹窗 -->
    <el-dialog v-model="showApplyDialog" title="新建权限申请" width="560px" destroy-on-close>
      <el-form :model="applyForm" label-width="80px">
        <el-form-item label="申请理由">
          <el-input v-model="applyForm.applyReason" type="textarea" :rows="3" placeholder="请输入申请理由" />
        </el-form-item>
        <el-form-item label="资源明细">
          <div v-for="(item, idx) in applyForm.items" :key="idx" style="display:flex; gap:8px; margin-bottom:8px">
            <el-select v-model="item.resourceType" placeholder="资源类型" style="width:120px">
              <el-option label="数据源" value="05" />
              <el-option label="数据集" value="06" />
              <el-option label="标签" value="08" />
              <el-option label="群组" value="09" />
              <el-option label="投递" value="10" />
              <el-option label="事件" value="12" />
              <el-option label="应用" value="21" />
              <el-option label="群组分析" value="22" />
            </el-select>
            <el-input v-model="item.resourceId" placeholder="资源ID" style="flex:1" />
            <el-select v-model="item.action" placeholder="权限" style="width:100px">
              <el-option label="查看" :value="1" />
              <el-option label="编辑" :value="2" />
              <el-option label="导出" :value="3" />
              <el-option label="管理" :value="4" />
            </el-select>
            <el-button link type="danger" @click="removeItem(idx)">删除</el-button>
          </div>
          <el-button type="primary" link @click="addItem">+ 添加资源</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApplyDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { grantApi, type ResourceGrant } from '@/api/grant'
import { authApplyApi, type AuthApply, type AuthApplyItem } from '@/api/authApply'

const activeTab = ref('grant')

// --- 权限申请 ---
const showApplyDialog = ref(false)
const mineLoading = ref(false)
const mineList = ref<AuthApply[]>([])

const applyForm = reactive({
  applyReason: '',
  items: [{ resourceType: '', resourceId: '', action: 1 }] as Array<{ resourceType: string; resourceId: string; action: number }>,
})

const addItem = () => {
  applyForm.items.push({ resourceType: '', resourceId: '', action: 1 })
}
const removeItem = (index: number) => {
  applyForm.items.splice(index, 1)
}

const handleSubmitApply = async () => {
  if (!applyForm.applyReason) {
    ElMessage.warning('请输入申请理由')
    return
  }
  try {
    await authApplyApi.submit({ applyReason: applyForm.applyReason, items: applyForm.items })
    ElMessage.success('申请提交成功')
    showApplyDialog.value = false
    applyForm.applyReason = ''
    applyForm.items = [{ resourceType: '', resourceId: '', action: 1 }]
    fetchMine()
  } catch {
    ElMessage.error('申请提交失败')
  }
}

const fetchMine = async () => {
  mineLoading.value = true
  try {
    const res = await authApplyApi.mine()
    mineList.value = res.data.data || []
  } catch {
    // ignore
  } finally {
    mineLoading.value = false
  }
}

const handleCancel = async (row: AuthApply) => {
  try {
    await ElMessageBox.confirm('确定撤销该申请？', '提示', { type: 'warning' })
    await authApplyApi.cancel(row.apply_id)
    ElMessage.success('撤销成功')
    fetchMine()
  } catch { /* cancelled */ }
}

const handleViewItems = async (applyId: string) => {
  try {
    const res = await authApplyApi.items(applyId)
    const items = res.data.data || []
    ElMessageBox.alert(
      items.map((it: AuthApplyItem) => `资源类型:${it.resource_type} 资源ID:${it.resource_id} 权限:${actionLabel(it.action)}`).join('\n') || '无明细',
      `申请单 ${applyId} 明细`,
      { confirmButtonText: '确定' }
    )
  } catch {
    ElMessage.error('加载明细失败')
  }
}

// --- 权限审批 ---
const todoLoading = ref(false)
const todoList = ref<AuthApply[]>([])
const approvedLoading = ref(false)
const approvedList = ref<AuthApply[]>([])

const fetchTodo = async () => {
  todoLoading.value = true
  try {
    const res = await authApplyApi.todo()
    todoList.value = res.data.data || []
  } catch { /* ignore */ }
  finally { todoLoading.value = false }
}

const fetchApproved = async () => {
  approvedLoading.value = true
  try {
    const res = await authApplyApi.approved()
    approvedList.value = res.data.data || []
  } catch { /* ignore */ }
  finally { approvedLoading.value = false }
}

const handleApprove = async (row: AuthApply) => {
  try {
    await ElMessageBox.confirm(`确定通过申请 ${row.apply_id}？`, '审批通过', { type: 'success' })
    await authApplyApi.approve(row.apply_id)
    ElMessage.success('审批通过')
    fetchTodo()
    fetchApproved()
  } catch { /* cancelled */ }
}

const handleReject = async (row: AuthApply) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝理由', '审批拒绝', { type: 'warning' })
    await authApplyApi.reject(row.apply_id, value)
    ElMessage.success('已拒绝')
    fetchTodo()
    fetchApproved()
  } catch { /* cancelled */ }
}

// --- 主动授权 ---
const grantQuery = reactive({ resourceType: '', resourceId: '' })
const grantLoading = ref(false)
const grantList = ref<ResourceGrant[]>([])

const handleQuery = async () => {
  if (!grantQuery.resourceType || !grantQuery.resourceId) {
    ElMessage.warning('请选择资源类型并输入资源ID')
    return
  }
  grantLoading.value = true
  try {
    const res = await grantApi.list(grantQuery.resourceType, grantQuery.resourceId)
    grantList.value = res.data.data || []
  } catch {
    ElMessage.error('查询授权列表失败')
  } finally {
    grantLoading.value = false
  }
}

const handleRevoke = async (row: ResourceGrant) => {
  try {
    await ElMessageBox.confirm('确定回收该授权？', '提示', { type: 'warning' })
    await grantApi.revoke(row.grant_id)
    ElMessage.success('回收成功')
    handleQuery()
  } catch {
    // cancelled
  }
}

// --- 权限审计 ---
const auditQuery = reactive({ resourceType: '', resourceId: '' })
const auditLoading = ref(false)
const auditList = ref<ResourceGrant[]>([])

const handleAuditQuery = async () => {
  if (!auditQuery.resourceType || !auditQuery.resourceId) {
    ElMessage.warning('请选择资源类型并输入资源ID')
    return
  }
  auditLoading.value = true
  try {
    const res = await grantApi.list(auditQuery.resourceType, auditQuery.resourceId)
    auditList.value = res.data.data || []
  } catch {
    ElMessage.error('查询审计数据失败')
  } finally {
    auditLoading.value = false
  }
}

// --- 通用工具 ---
const resourceTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    '05': '数据源', '06': '数据集', '08': '标签', '09': '群组',
    '10': '投递', '12': '事件', '21': '应用', '22': '群组分析',
  }
  return map[type] || type
}

const actionLabel = (action: number) => {
  const map: Record<number, string> = { 1: '查看', 2: '编辑', 3: '导出', 4: '管理' }
  return map[action] || String(action)
}

const actionTagType = (action: number) => {
  const map: Record<number, string> = { 1: 'info', 2: 'warning', 3: '', 4: 'danger' }
  return map[action] || 'info'
}

const statusLabel = (status: number) => {
  const map: Record<number, string> = { 1: '待审批', 2: '已通过', 3: '已拒绝', 4: '已撤销' }
  return map[status] || String(status)
}

const statusTagType = (status: number) => {
  const map: Record<number, string> = { 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' }
  return map[status] || 'info'
}

onMounted(() => {
  fetchMine()
  fetchTodo()
  fetchApproved()
})
</script>

<style scoped lang="scss">
.data-access-page {
  .page-header {
    margin-bottom: 20px;

    h2 {
      margin: 0 0 8px;
      font-size: 20px;
      color: #303133;
    }

    .page-desc {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }

  .query-form {
    margin-bottom: 16px;
  }
}
</style>
