<template>
  <div class="resource-grant-panel">
    <div class="panel-header">
      <span class="title">授权管理</span>
      <el-button type="primary" size="small" :icon="Plus" @click="showGrantDialog = true">
        新增授权
      </el-button>
    </div>

    <el-table v-loading="loading" :data="grants" stripe border size="small">
      <el-table-column label="受权者" min-width="120">
        <template #default="{ row }">
          <el-tag :type="row.grantee_type === 1 ? 'primary' : 'success'" size="small">
            {{ row.grantee_type === 1 ? '用户' : '角色' }}
          </el-tag>
          <span style="margin-left: 8px">{{ row.grantee_id }}</span>
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
      <el-table-column label="操作" width="80">
        <template #default="{ row }">
          <el-button link type="danger" size="small" @click="handleRevoke(row)">回收</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增授权弹窗 -->
    <el-dialog v-model="showGrantDialog" title="新增授权" width="480px" destroy-on-close>
      <el-form :model="grantForm" label-width="80px">
        <el-form-item label="受权者类型">
          <el-radio-group v-model="grantForm.granteeType">
            <el-radio :label="1">用户</el-radio>
            <el-radio :label="2">角色</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="受权者ID">
          <el-input v-model="grantForm.granteeId" placeholder="请输入用户ID或角色ID" />
        </el-form-item>
        <el-form-item label="权限动作">
          <el-checkbox-group v-model="grantForm.actions">
            <el-checkbox :label="1">查看</el-checkbox>
            <el-checkbox :label="2">编辑</el-checkbox>
            <el-checkbox :label="3">导出</el-checkbox>
            <el-checkbox :label="4">管理</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showGrantDialog = false">取消</el-button>
        <el-button type="primary" @click="handleGrant">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { grantApi, type ResourceGrant } from '@/api/grant'

const props = defineProps<{
  resourceType: string
  resourceId: string
}>()

const loading = ref(false)
const grants = ref<ResourceGrant[]>([])
const showGrantDialog = ref(false)

const grantForm = reactive({
  granteeType: 1,
  granteeId: '',
  actions: [1] as number[],
})

const actionLabel = (action: number) => {
  const map: Record<number, string> = { 1: '查看', 2: '编辑', 3: '导出', 4: '管理' }
  return map[action] || String(action)
}

const actionTagType = (action: number) => {
  const map: Record<number, string> = { 1: 'info', 2: 'warning', 3: '', 4: 'danger' }
  return map[action] || 'info'
}

const fetchGrants = async () => {
  loading.value = true
  try {
    const res = await grantApi.list(props.resourceType, props.resourceId)
    grants.value = res.data.data || []
  } catch {
    ElMessage.error('加载授权列表失败')
  } finally {
    loading.value = false
  }
}

const handleGrant = async () => {
  if (!grantForm.granteeId) {
    ElMessage.warning('请输入受权者ID')
    return
  }
  if (grantForm.actions.length === 0) {
    ElMessage.warning('请选择权限动作')
    return
  }
  try {
    await grantApi.batchGrant({
      resourceType: props.resourceType,
      resourceIds: [props.resourceId],
      granteeType: grantForm.granteeType,
      granteeId: grantForm.granteeId,
      actions: grantForm.actions,
    })
    ElMessage.success('授权成功')
    showGrantDialog.value = false
    fetchGrants()
  } catch {
    ElMessage.error('授权失败')
  }
}

const handleRevoke = async (row: ResourceGrant) => {
  try {
    await ElMessageBox.confirm('确定回收该授权？', '提示', { type: 'warning' })
    await grantApi.revoke(row.grant_id)
    ElMessage.success('回收成功')
    fetchGrants()
  } catch {
    // cancelled
  }
}

onMounted(fetchGrants)
</script>

<style scoped lang="scss">
.resource-grant-panel {
  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .title {
      font-size: 16px;
      font-weight: 500;
    }
  }
}
</style>
