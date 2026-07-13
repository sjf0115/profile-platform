<template>
  <div class="entity-detail-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="back-btn" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>
      <h2 class="page-title">实体详情</h2>
    </div>

    <el-card v-loading="loading" class="detail-card">
      <template v-if="entity">
        <div class="detail-section">
          <h3 class="section-title">基本信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="实体ID">
              {{ entity.entity_id }}
            </el-descriptions-item>
            <el-descriptions-item label="实体名称">
              {{ entity.entity_name }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag v-if="entity.status === 1" type="success" size="small">启用</el-tag>
              <el-tag v-else type="danger" size="small">禁用</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建方式">
              <el-tag v-if="entity.source_type === 1" type="info" size="small">系统预置</el-tag>
              <el-tag v-else type="success" size="small">自定义</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建人">
              {{ entity.creator_name || entity.creator || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="修改人">
              {{ entity.modifier_name || entity.modifier || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(entity.gmt_create) }}
            </el-descriptions-item>
            <el-descriptions-item label="修改时间">
              {{ formatDateTime(entity.gmt_modified) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </template>

      <el-empty v-else description="实体不存在或已被删除" />
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <el-button @click="handleBack">返回</el-button>
      <el-button v-if="entity && entity.source_type !== 1" type="primary" @click="handleEdit">编辑</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { entityApi, type Entity } from '@/api/entity'

const route = useRoute()
const router = useRouter()

const entityId = computed(() => route.params.id as string)

const loading = ref(false)
const entity = ref<Entity | null>(null)

// 获取实体详情
const fetchEntityDetail = async () => {
  if (!entityId.value) return
  loading.value = true
  try {
    const res = await entityApi.detail(entityId.value)
    entity.value = res.data.data
  } catch (error) {
    console.error('获取实体详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 返回
const handleBack = () => {
  router.back()
}

// 编辑
const handleEdit = () => {
  // 返回列表页并触发编辑（暂不实现独立编辑页，复用弹窗）
  router.back()
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
  fetchEntityDetail()
})
</script>

<style scoped lang="scss">
.entity-detail-page {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;

  .back-btn {
    display: flex;
    align-items: center;
    cursor: pointer;
    color: #606266;
    font-size: 14px;
    margin-right: 16px;

    &:hover {
      color: #409eff;
    }

    .el-icon {
      margin-right: 4px;
    }
  }

  .page-title {
    margin: 0;
    font-size: 20px;
    font-weight: 500;
  }
}

.detail-card {
  margin-bottom: 20px;
}

.detail-section {
  margin-bottom: 30px;

  &:last-child {
    margin-bottom: 0;
  }

  .section-title {
    font-size: 16px;
    font-weight: 500;
    margin: 0 0 16px 0;
    padding-left: 12px;
    border-left: 4px solid #409eff;
  }
}

.action-bar {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px 0;
}
</style>
