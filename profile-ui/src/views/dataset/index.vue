<template>
  <div class="dataset-management-page">
    <div class="page-header">
      <h2 class="page-title">数据集管理</h2>
    </div>

    <div class="page-card">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <div class="filter-left">
          <el-input
            v-model="queryParams.dataset_name"
            placeholder="请输入数据集名称"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="queryParams.dataset_type" placeholder="数据集类型" clearable style="width: 140px">
            <el-option label="离线数据集" :value="1" />
            <el-option label="实时数据集" :value="2" />
          </el-select>
          <el-select v-model="queryParams.dataset_status" placeholder="数据集状态" clearable style="width: 140px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
        <div class="filter-right">
          <el-button type="primary" :icon="Plus" @click="handleCreate">新建数据集</el-button>
        </div>
      </div>

      <!-- 数据集列表 -->
      <el-table
        v-loading="loading"
        :data="datasetList"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="dataset_name" label="数据集名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="dataset_desc" label="数据集描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="数据集状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" size="small">启用</el-tag>
            <el-tag v-else type="danger" size="small">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dataset_type" label="数据集类型" width="120">
          <template #default="{ row }">
            <span v-if="row.dataset_type === 1">标签数据集</span>
            <span v-else-if="row.dataset_type === 2">行为数据集</span>
            <span v-else-if="row.dataset_type === 3">统计数据集</span>
            <span v-else-if="row.dataset_type === 3">特征数据集</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="owner" label="负责人" width="120" />
        <el-table-column label="数据更新时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.gmt_modified) }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.gmt_create) }}
          </template>
        </el-table-column>
        <el-table-column prop="datasource_name" label="数据源名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="table_name" label="数据表名" min-width="150" show-overflow-tooltip />
        
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">查看</el-button>
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
                  <el-dropdown-item @click="handleRun(row)">立即执行</el-dropdown-item>
                  <el-dropdown-item @click="handleSchedule(row)">配置调度</el-dropdown-item>
                  <el-dropdown-item @click="handleBindLabel(row)">绑定标签</el-dropdown-item>
                  <el-dropdown-item @click="handleHistory(row)">调度历史</el-dropdown-item>
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
          v-model:current-page="queryParams.page_num"
          v-model:page-size="queryParams.page_size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>

  <!-- 选择创建方式弹窗 -->
  <el-dialog
    v-model="createDialogVisible"
    title="请选择创建方式"
    width="700px"
    destroy-on-close
    :close-on-click-modal="true"
  >
    <div class="create-methods">
      <div class="method-card" @click="handleCreateByMethod('label')">
        <div class="method-icon">
          <el-icon :size="32" color="#409EFF"><CollectionTag /></el-icon>
        </div>
        <div class="method-title">标签数据集</div>
        <div class="method-desc">标签数据集记录实体(用户)的一系列特征属性。通过为字段配置标签别名的方式给实体(用户)打标签，在使用标签时，实质上是在使用对应的字段。</div>
        <el-button type="primary" plain>立即创建</el-button>
      </div>

      <div class="method-card" @click="handleCreateByMethod('behavior')">
        <div class="method-icon">
          <el-icon :size="32" color="#67C23A"><Mouse /></el-icon>
        </div>
        <div class="method-title">行为数据集</div>
        <div class="method-desc">行为数据集记录用户的浏览、加购、收藏、购买等行为。</div>
        <el-button type="primary" plain>立即创建</el-button>
      </div>

      <div class="method-card" @click="handleCreateByMethod('statistics')">
        <div class="method-icon">
          <el-icon :size="32" color="#E6A23C"><DataAnalysis /></el-icon>
        </div>
        <div class="method-title">统计数据集</div>
        <div class="method-desc">统计数据集记录实体(用户)执行相关操作的统计数据。</div>
        <el-button type="primary" plain>立即创建</el-button>
      </div>

      <div class="method-card" @click="handleCreateByMethod('feature')">
        <div class="method-icon">
          <el-icon :size="32" color="#F56C6C"><Cpu /></el-icon>
        </div>
        <div class="method-title">特征数据集</div>
        <div class="method-desc">特征数据集是算法特征库。</div>
        <el-button type="primary" plain>立即创建</el-button>
      </div>
    </div>
  </el-dialog>

  <!-- 配置调度弹窗 -->
  <el-dialog
    v-model="scheduleDialogVisible"
    title="配置调度"
    width="600px"
    destroy-on-close
    :close-on-click-modal="false"
  >
    <el-form
      ref="scheduleFormRef"
      :model="scheduleForm"
      label-width="100px"
      label-position="left"
      v-loading="scheduleLoading"
    >
      <el-form-item label="调度周期" required>
        <el-select v-model="scheduleForm.schedule_period" style="width: 200px" @change="updateCron">
          <el-option label="日" value="day" />
          <el-option label="小时" value="hour" />
        </el-select>
      </el-form-item>

      <el-form-item label="计算时间" required>
        <el-time-picker
          v-model="scheduleForm.calc_time"
          format="HH:mm"
          value-format="HH:mm"
          placeholder="请选择计算时间"
          style="width: 200px"
          @change="updateCron"
        />
      </el-form-item>

      <el-form-item label="Cron 表达式">
        <el-input
          v-model="scheduleForm.trigger_cron"
          readonly
          disabled
          placeholder="选择计算时间后自动生成"
          style="width: 200px"
        />
      </el-form-item>

      <el-form-item label="生效日期" required>
        <el-radio-group v-model="scheduleForm.effective_type">
          <el-radio value="permanent">永久生效</el-radio>
          <el-radio value="custom">指定日期</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="scheduleForm.effective_type === 'custom'" label="时间区间" required>
        <el-date-picker
          v-model="scheduleForm.date_range"
          type="daterange"
          unlink-panels
          range-separator="~"
          start-placeholder="开始"
          end-placeholder="结束"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 260px"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="scheduleDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleScheduleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, More, CollectionTag, Mouse, DataAnalysis, Cpu } from '@element-plus/icons-vue'
import type { Dataset, DatasetQueryParams, Task } from '@/types'
import { datasetApi } from '@/api/dataset'

const router = useRouter()

// 加载状态
const loading = ref(false)

// 搜索关键词
const searchKeyword = ref('')

// 数据集列表
const datasetList = ref<Dataset[]>([])

// 总条数
const total = ref(0)

// 查询参数
const queryParams = reactive<DatasetQueryParams>({
  page_num: 1,
  page_size: 10,
})

// 创建方式弹窗显示状态
const createDialogVisible = ref(false)

// 格式化日期时间
const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).replace(/\//g, '-')
}

// 获取数据集列表
const fetchDatasetList = async () => {
  loading.value = true
  try {
    const params: any = {
      page_num: queryParams.page_num,
      page_size: queryParams.page_size
    }
    
    // 只有值不为空时才添加参数
    if (queryParams.dataset_name?.trim()) {
      params.dataset_name = queryParams.dataset_name.trim()
    }
    if (queryParams.dataset_type !== undefined && queryParams.dataset_type !== null) {
      params.dataset_type = queryParams.dataset_type
    }
    if (queryParams.dataset_status !== undefined && queryParams.dataset_status !== null) {
      params.status = queryParams.dataset_status
    }
    
    console.log('请求参数:', params)
    const res = await datasetApi.list(params)
    console.log('完整响应:', res)
    console.log('响应数据:', res.data)
    datasetList.value = res.data.data || []
    total.value = res.data.data?.length || 0
    console.log('数据集列表:', datasetList.value)
  } catch (error) {
    console.error('获取数据集列表失败:', error)
    ElMessage.error('获取数据集列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page_num = 1
  fetchDatasetList()
}

// 重置
const handleReset = () => {
  queryParams.dataset_name = ''
  queryParams.dataset_type = undefined
  queryParams.dataset_status = undefined
  queryParams.page_num = 1
  fetchDatasetList()
}

// 创建数据集
const handleCreate = () => {
  createDialogVisible.value = true
}

// 选择创建方式
const handleCreateByMethod = (method: string) => {
  createDialogVisible.value = false
  
  switch (method) {
    case 'label':
      router.push('/dataset/create/label')
      break
    case 'behavior':
      ElMessage.info('行为数据集创建功能开发中')
      // router.push('/dataset/create/behavior')
      break
    case 'statistics':
      ElMessage.info('统计数据集创建功能开发中')
      // router.push('/dataset/create/statistics')
      break
    case 'feature':
      ElMessage.info('特征数据集创建功能开发中')
      // router.push('/dataset/create/feature')
      break
    default:
      break
  }
}

// 查看详情
const handleDetail = (row: Dataset) => {
  router.push(`/dataset/detail/${row.dataset_id}`)
}

// 编辑数据集
const handleEdit = (row: Dataset) => {
  router.push(`/dataset/edit/${row.dataset_id}`)
}

// 启用/禁用数据集
const handleToggleStatus = async (row: Dataset) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '禁用'
  
  try {
    await datasetApi.update({
      ...row,
      status: newStatus
    })
    ElMessage.success(`${actionText}成功`)
    fetchDatasetList()
  } catch (error) {
    console.error(`${actionText}失败:`, error)
  }
}

// 绑定标签
const handleBindLabel = (row: Dataset) => {
  ElMessage.info(`绑定标签功能开发中: ${row.dataset_name}`)
}

// 立即执行
const handleRun = async (row: Dataset) => {
  try {
    await ElMessageBox.confirm(
      `确定要立即执行数据集 "${row.dataset_name}" 的同步任务吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info',
      }
    )
    const res = await datasetApi.execute(row.dataset_id)
    ElMessage.success('任务已提交执行')
    console.log('执行结果:', res.data)
  } catch (error: any) {
    if (error === 'cancel') return
    console.error('立即执行失败:', error)
    ElMessage.error(error?.response?.data?.message || '立即执行失败')
  }
}

// 调度弹窗
const scheduleDialogVisible = ref(false)
const scheduleFormRef = ref()
const scheduleLoading = ref(false)
const currentScheduleDataset = ref<Dataset | null>(null)
const scheduleForm = reactive<{
  schedule_period: string  // 'day' | 'hour'
  calc_time: string  // HH:mm（用于生成 Cron）
  trigger_cron: string
  effective_type: string  // 'permanent' | 'custom'
  date_range: string[]  // [start, end]
}>({
  schedule_period: 'day',
  calc_time: '',
  trigger_cron: '',
  effective_type: 'permanent',
  date_range: []
})

// 根据 调度周期+计算时间 生成 Cron 表达式
const generateCron = (period: string, time: string): string => {
  if (!time) return ''
  const [hour, minute] = time.split(':')
  if (period === 'day') {
    return `0 ${minute} ${hour} * * ?`
  } else if (period === 'hour') {
    return `0 ${minute} * * * ?`
  }
  return ''
}

// 监听调度周期和计算时间变化，自动生成 Cron
const updateCron = () => {
  scheduleForm.trigger_cron = generateCron(scheduleForm.schedule_period, scheduleForm.calc_time)
}

// 格式化日期为 YYYY-MM-DD
const formatDate = (date: Date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const handleSchedule = async (row: Dataset) => {
  currentScheduleDataset.value = row
  scheduleLoading.value = true
  try {
    const res = await datasetApi.getScheduleConfig(row.dataset_id)
    const task = res.data.data
    if (task) {
      // 从 cron 反推周期和时间
      if (task.trigger_type === 2 || task.trigger_type === 3) {
        scheduleForm.schedule_period = task.trigger_type === 2 ? 'day' : 'hour'
        if (task.trigger_cron) {
          const parts = task.trigger_cron.split(' ')
          if (parts.length >= 2) {
            scheduleForm.calc_time = `${parts[2].padStart(2, '0')}:${parts[1].padStart(2, '0')}`
            scheduleForm.trigger_cron = task.trigger_cron
          }
        }
      } else {
        scheduleForm.schedule_period = 'day'
        scheduleForm.calc_time = ''
        scheduleForm.trigger_cron = ''
      }
      scheduleForm.effective_type = task.trigger_start_time ? 'custom' : 'permanent'
      if (task.trigger_start_time && task.trigger_end_time) {
        scheduleForm.date_range = [task.trigger_start_time, task.trigger_end_time]
      } else {
        scheduleForm.date_range = []
      }
    }
  } catch (error) {
    console.error('获取调度配置失败:', error)
    // 没有配置过则用默认值
    scheduleForm.schedule_period = 'day'
    scheduleForm.calc_time = ''
    scheduleForm.trigger_cron = ''
    scheduleForm.effective_type = 'permanent'
    scheduleForm.date_range = []
  } finally {
    scheduleLoading.value = false
  }
  scheduleDialogVisible.value = true
}

const handleScheduleSubmit = async () => {
  if (!currentScheduleDataset.value) return
  if (!scheduleForm.calc_time) {
    ElMessage.warning('请选择计算时间')
    return
  }

  const triggerType = scheduleForm.schedule_period === 'day' ? 2 : 3
  const triggerStartTime = scheduleForm.effective_type === 'custom'
    ? (scheduleForm.date_range?.[0] || formatDate(new Date()))
    : formatDate(new Date())
  const triggerEndTime = scheduleForm.effective_type === 'custom'
    ? (scheduleForm.date_range?.[1] || '9999-12-31')
    : '9999-12-31'

  try {
    await datasetApi.configureSchedule(currentScheduleDataset.value.dataset_id, {
      trigger_type: triggerType,
      trigger_cron: scheduleForm.trigger_cron,
      trigger_start_time: triggerStartTime,
      trigger_end_time: triggerEndTime
    })
    ElMessage.success('调度配置成功')
    scheduleDialogVisible.value = false
  } catch (error) {
    console.error('配置调度失败:', error)
    ElMessage.error('配置调度失败')
  }
}

// 历史
const handleHistory = (row: Dataset) => {
  ElMessage.info(`执行历史功能开发中: ${row.dataset_name}`)
}

// 删除数据集
const handleDelete = (row: Dataset) => {
  ElMessageBox.confirm(
    `确定要删除数据集 "${row.dataset_name}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await datasetApi.delete(row.dataset_id)
        ElMessage.success('删除成功')
        fetchDatasetList()
      } catch (error) {
        console.error('删除失败:', error)
      }
    })
    .catch(() => {
      // 取消删除
    })
}

// 多选
const handleSelectionChange = (rows: Dataset[]) => {
  console.log('选中的数据集:', rows)
}

// 分页大小变化
const handleSizeChange = (val: number) => {
  queryParams.page_size = val
  fetchDatasetList()
}

// 页码变化
const handleCurrentChange = (val: number) => {
  queryParams.page_num = val
  fetchDatasetList()
}

onMounted(() => {
  fetchDatasetList()
})
</script>

<style scoped lang="scss">
.dataset-management-page {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  margin-bottom: 20px;
  
  .page-title {
    margin: 0;
    font-size: 20px;
    font-weight: 500;
  }
}

.page-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  .filter-left {
    display: flex;
    gap: 12px;
    align-items: center;
  }
  
  .filter-right {
    display: flex;
    gap: 12px;
    align-items: center;
  }
}

.el-table {
  flex: 1;
  overflow: auto;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

// 创建方式弹窗样式
.create-methods {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  padding: 10px;
}

.method-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  }

  .method-icon {
    width: 64px;
    height: 64px;
    border-radius: 12px;
    background-color: #f5f7fa;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16px;
  }

  .method-title {
    font-size: 16px;
    font-weight: 500;
    color: #303133;
    margin-bottom: 12px;
  }

  .method-desc {
    font-size: 13px;
    color: #606266;
    line-height: 1.6;
    margin-bottom: 20px;
    min-height: 60px;
  }

  .el-button {
    width: 120px;
  }
}
</style>
