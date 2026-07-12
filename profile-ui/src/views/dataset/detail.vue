<template>
  <div class="dataset-detail-page">
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <h2 class="page-title">数据集详情</h2>
      </div>
      <div class="header-right">
        <el-button @click="handleEdit">编辑</el-button>
        <el-button type="primary" @click="handleRefresh">刷新字段</el-button>
      </div>
    </div>

    <div class="page-content" v-loading="loading">
      <!-- 基本信息 -->
      <el-card class="detail-card">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
          </div>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="数据集ID">{{ datasetInfo.dataset_id }}</el-descriptions-item>
          <el-descriptions-item label="数据集名称">{{ datasetInfo.dataset_name }}</el-descriptions-item>
          <el-descriptions-item label="数据集类型">
            <el-tag v-if="datasetInfo.dataset_type === 1" type="primary">标签数据集</el-tag>
            <el-tag v-else-if="datasetInfo.dataset_type === 2" type="success">行为数据集</el-tag>
            <el-tag v-else-if="datasetInfo.dataset_type === 3" type="warning">统计数据集</el-tag>
            <el-tag v-else-if="datasetInfo.dataset_type === 4" type="info">特征数据集</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="数据集状态">
            <el-tag v-if="datasetInfo.status === 1" type="success">启用</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建方式">
            <span v-if="datasetInfo.source_type === 1">内置</span>
            <span v-else-if="datasetInfo.source_type === 2">自定义</span>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="负责人">{{ datasetInfo.owner_name || datasetInfo.owner || '-' }}</el-descriptions-item>
          <el-descriptions-item label="数据源" :span="1">{{ datasetInfo.datasource_name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="数据表" :span="2">{{ datasetInfo.table_name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="数据集描述" :span="3">{{ datasetInfo.dataset_desc || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 分区配置 -->
      <el-card class="detail-card" v-if="datasetInfo.partition_field">
        <template #header>
          <div class="card-header">
            <span>分区配置</span>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="分区字段">{{ datasetInfo.partition_field }}</el-descriptions-item>
          <el-descriptions-item label="分区格式">{{ datasetInfo.partition_format }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 实体标识配置 -->
      <el-card class="detail-card">
        <template #header>
          <div class="card-header">
            <span>实体标识配置</span>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="实体标识">
            {{ entityIdentifierName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="实体字段">{{ datasetInfo.entity_field || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 字段列表 -->
      <el-card class="detail-card">
        <template #header>
          <div class="card-header">
            <span>字段列表</span>
            <span class="field-count">共 {{ fieldList.length }} 个字段</span>
          </div>
        </template>
        <el-table :data="fieldList" border style="width: 100%" max-height="500">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="field_name" label="字段名" min-width="140" show-overflow-tooltip />
          <el-table-column prop="field_desc" label="字段描述" min-width="180" show-overflow-tooltip />
          <el-table-column prop="field_type" label="字段类型" width="100" align="center" />
          <el-table-column label="导入状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.import_status === 1" type="success" size="small">导入</el-tag>
              <el-tag v-else type="info" size="small">不导入</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="字段状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.field_status === 1" type="success" size="small">新增</el-tag>
              <el-tag v-else-if="row.field_status === 2" type="warning" size="small">修改</el-tag>
              <el-tag v-else-if="row.field_status === 3" type="danger" size="small">删除</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="related_id" label="关联标签" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">
              {{ getLabelName(row.related_id) || '-' }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 任务执行信息 -->
      <el-card class="detail-card" v-if="datasetInfo.latest_instance">
        <template #header>
          <div class="card-header">
            <span>任务执行信息</span>
          </div>
        </template>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="执行状态">
            <el-tag v-if="datasetInfo.latest_instance.status === 1" type="info">未运行</el-tag>
            <el-tag v-else-if="datasetInfo.latest_instance.status === 2" type="warning">运行中</el-tag>
            <el-tag v-else-if="datasetInfo.latest_instance.status === 3" type="danger">运行失败</el-tag>
            <el-tag v-else-if="datasetInfo.latest_instance.status === 4" type="success">运行成功</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="触发模式">
            <span v-if="datasetInfo.latest_instance.trigger_mode === 1">手动触发</span>
            <span v-else-if="datasetInfo.latest_instance.trigger_mode === 2">定时调度</span>
            <span v-else-if="datasetInfo.latest_instance.trigger_mode === 3">API触发</span>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="执行时长">
            {{ formatDuration(datasetInfo.latest_instance.duration) }}
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatDateTime(datasetInfo.latest_instance.start_time) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ formatDateTime(datasetInfo.latest_instance.end_time) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 元数据信息 -->
      <el-card class="detail-card">
        <template #header>
          <div class="card-header">
            <span>元数据信息</span>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="创建人">{{ datasetInfo.creator_name || datasetInfo.creator || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(datasetInfo.gmt_create) }}</el-descriptions-item>
          <el-descriptions-item label="修改人">{{ datasetInfo.modifier_name || datasetInfo.modifier || '-' }}</el-descriptions-item>
          <el-descriptions-item label="修改时间">{{ formatDateTime(datasetInfo.gmt_modified) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import type { Dataset } from '@/types'
import { datasetApi } from '@/api/dataset'
import { entityIdentifierApi, type EntityIdentifier } from '@/api/entity'
import { labelApi } from '@/api/label'

const route = useRoute()
const router = useRouter()
const datasetId = computed(() => route.params.id as string)

const loading = ref(false)
const datasetInfo = ref<Partial<Dataset>>({})
const fieldList = ref<any[]>([])
const entityIdentifierList = ref<EntityIdentifier[]>([])
const labelList = ref<any[]>([])

// 实体标识名称
const entityIdentifierName = computed(() => {
  if (!datasetInfo.value.entity_id) return ''
  const item = entityIdentifierList.value.find(
    e => e.entity_identifier_id === datasetInfo.value.entity_id
  )
  if (item) {
    return `${item.entity_name}>${item.entity_identifier_name}`
  }
  return datasetInfo.value.entity_id
})

// 获取标签名称
const getLabelName = (labelId?: string) => {
  if (!labelId) return ''
  const label = labelList.value.find(l => l.label_id === labelId)
  return label?.label_name || labelId
}

// 格式化日期时间
const formatDateTime = (dateVal?: string | number) => {
  if (!dateVal) return '-'
  const date = new Date(dateVal)
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).format(date).replace(/\//g, '-')
}

// 格式化执行时长（毫秒转分钟）
const formatDuration = (durationMs?: number) => {
  if (!durationMs && durationMs !== 0) return '-'
  const minutes = Math.floor(durationMs / 60000)
  const seconds = Math.floor((durationMs % 60000) / 1000)
  if (minutes > 0) {
    return `${minutes}分${seconds}秒`
  }
  return `${seconds}秒`
}

// 获取数据集详情
const fetchDatasetDetail = async () => {
  if (!datasetId.value) {
    ElMessage.error('数据集ID不能为空')
    return
  }
  loading.value = true
  try {
    const res = await datasetApi.detail(datasetId.value)
    console.log('数据集详情:', res)
    datasetInfo.value = res.data.data || {}
    fieldList.value = datasetInfo.value.fields || []
    
    // 如果有实体标识，获取标签列表
    if (datasetInfo.value.entity_id) {
      await fetchLabelList(datasetInfo.value.entity_id)
    }
  } catch (error) {
    console.error('获取数据集详情失败:', error)
    ElMessage.error('获取数据集详情失败')
  } finally {
    loading.value = false
  }
}

// 获取实体标识列表
const fetchEntityIdentifierList = async () => {
  try {
    const res = await entityIdentifierApi.list()
    entityIdentifierList.value = res.data.data || []
  } catch (error) {
    console.error('获取实体标识列表失败:', error)
  }
}

// 获取标签列表
const fetchLabelList = async (entityIdentifierId: string) => {
  try {
    const res = await labelApi.getList({ entity_identifier_id: entityIdentifierId })
    labelList.value = res.data.data || []
  } catch (error) {
    console.error('获取标签列表失败:', error)
  }
}

// 返回
const goBack = () => {
  router.push('/project/dataset')
}

// 编辑
const handleEdit = () => {
  router.push(`/dataset/edit/${datasetId.value}`)
}

// 刷新字段
const handleRefresh = async () => {
  try {
    await ElMessageBox.confirm('确定要刷新数据集字段吗？这将重新从数据源获取最新的表结构。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await datasetApi.refresh(
      datasetInfo.value.datasource_id!,
      datasetId.value,
      datasetInfo.value.table_name!
    )
    console.log('刷新字段结果:', res)
    fieldList.value = res.data.data || []
    ElMessage.success('刷新字段成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('刷新字段失败:', error)
      ElMessage.error('刷新字段失败')
    }
  }
}

onMounted(() => {
  fetchEntityIdentifierList()
  fetchDatasetDetail()
})
</script>

<style scoped lang="scss">
.dataset-detail-page {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .page-title {
      margin: 0;
      font-size: 20px;
      font-weight: 500;
    }
  }
}

.page-content {
  flex: 1;
  overflow: auto;
}

.detail-card {
  margin-bottom: 20px;

  &:last-child {
    margin-bottom: 0;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 500;

    .field-count {
      font-size: 14px;
      color: #909399;
      font-weight: normal;
    }
  }
}
</style>
