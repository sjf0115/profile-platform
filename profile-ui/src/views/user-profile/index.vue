<template>
  <div class="user-profile-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">用户画像</h2>
      </div>
      <div class="header-right">
        <div class="entity-selector">
          <span class="entity-label">实体</span>
          <el-select
            v-model="searchForm.entityIdentifierId"
            placeholder="请选择实体标识"
            style="width: 180px"
            class="entity-select"
          >
            <el-option
              v-for="item in entityIdentifierList"
              :key="item.entity_identifier_id"
              :label="`${item.entity_name} > ${item.entity_identifier_name}`"
              :value="item.entity_identifier_id"
            />
          </el-select>
        </div>
      </div>
    </div>

    <!-- 搜索区域 - 居中卡片 -->
    <div class="search-wrapper">
      <div class="search-card">
        <!-- 目的输入 -->
        <div class="search-purpose">
          <el-icon class="purpose-icon"><MagicStick /></el-icon>
          <el-input
            v-model="searchForm.purpose"
            placeholder="试一试！直接输入你本次细查的目的~"
            clearable
            class="purpose-input"
          />
        </div>

        <!-- 类型 + ID -->
        <div class="search-row">
          <el-select v-model="searchForm.type" placeholder="选择类型" style="width: 140px">
            <el-option label="用户" value="user" />
            <el-option label="标签" value="label" />
            <el-option label="群组" value="group" />
          </el-select>
          <el-input
            v-model="searchForm.id"
            placeholder="请输入ID"
            style="width: 220px"
            clearable
          />
        </div>

        <!-- 时间 + 高级筛选 + 搜索 -->
        <div class="search-row search-row-bottom">
          <div class="search-field">
            <el-icon class="time-icon"><Calendar /></el-icon>
            <span class="field-label">时间</span>
            <el-date-picker
              v-model="searchForm.dateRange"
              type="datetimerange"
              range-separator="-"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              style="width: 360px"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </div>
          <div class="search-field">
            <el-button class="advanced-btn" text>
              高级筛选
              <el-icon><ArrowDown /></el-icon>
            </el-button>
          </div>
          <div class="search-actions">
            <el-button type="primary" class="search-btn" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 结果表格 -->
    <div v-if="hasSearched" class="result-section">
      <h3 class="result-title">{{ resultTitle }}</h3>
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        style="width: 100%"
        empty-text="暂无数据"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="用户" min-width="180">
          <template #default="{ row }">
            <div class="user-cell" @click="goToUserDetail(row.user_id)">
              <span class="user-name">{{ row.user_name || row.user_id }}</span>
              <span class="user-id">({{ row.user_id }}...)</span>
              <el-icon class="user-play-icon"><VideoPlay /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="app" label="应用" min-width="100" show-overflow-tooltip />
        <el-table-column prop="last_access_time" label="最近访问时间" min-width="160" show-overflow-tooltip />
        <el-table-column prop="device_model" label="设备型号" min-width="100" show-overflow-tooltip>
          <template #default="{ row }">{{ row.device_model || '-' }}</template>
        </el-table-column>
        <el-table-column prop="os" label="操作系统" min-width="100" show-overflow-tooltip />
        <el-table-column prop="software_version" label="软件版本" min-width="100" show-overflow-tooltip>
          <template #default="{ row }">{{ row.software_version || '-' }}</template>
        </el-table-column>
        <el-table-column prop="channel" label="渠道" min-width="80" show-overflow-tooltip>
          <template #default="{ row }">{{ row.channel || '-' }}</template>
        </el-table-column>
        <el-table-column prop="device_brand" label="设备品牌" min-width="100" show-overflow-tooltip>
          <template #default="{ row }">{{ row.device_brand || '-' }}</template>
        </el-table-column>
        <el-table-column prop="region" label="地域" min-width="100" show-overflow-tooltip />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, VideoPlay, MagicStick, Calendar, ArrowDown } from '@element-plus/icons-vue'
import { entityIdentifierApi, type EntityIdentifier } from '@/api/entity'
import { userProfileApi } from '@/api/user-profile'
import type { UserProfileRowVO } from '@/types'
import { ElMessage } from 'element-plus'

const router = useRouter()
const hasSearched = ref(false)
const loading = ref(false)
const tableData = ref<UserProfileRowVO[]>([])
const entityIdentifierList = ref<EntityIdentifier[]>([])

const searchForm = ref({
  purpose: '',
  entityIdentifierId: '',
  type: 'user' as 'user' | 'label' | 'group',
  id: '',
  dateRange: null as [string, string] | null,
})

// 表格标题根据搜索类型动态变化
const resultTitle = computed(() => {
  const titles: Record<string, string> = {
    user: '用户列表',
    label: '标签关联用户列表',
    group: '群组用户列表',
  }
  return titles[searchForm.value.type] || '用户列表'
})

// 获取实体标识列表
const fetchEntityIdentifiers = async () => {
  try {
    const res = await entityIdentifierApi.list({ status: 1 })
    entityIdentifierList.value = res.data.data || []
    // 默认选择第一个实体标识
    if (entityIdentifierList.value.length > 0 && !searchForm.value.entityIdentifierId) {
      searchForm.value.entityIdentifierId = entityIdentifierList.value[0].entity_identifier_id
    }
  } catch (error) {
    console.error('获取实体标识列表失败:', error)
  }
}

// 搜索处理
const handleSearch = async () => {
  const entityIdentifierId = searchForm.value.entityIdentifierId
  const type = searchForm.value.type
  const id = searchForm.value.id

  if (!entityIdentifierId) {
    ElMessage.warning('请选择实体标识')
    return
  }

  if (!id) {
    ElMessage.warning('请输入ID')
    return
  }

  hasSearched.value = true
  loading.value = true

  try {
    // 根据类型调用不同的 API
    if (type === 'user') {
      // 用户类型：直接跳转详情页
      goToUserDetail(id)
      return
    } else if (type === 'label' || type === 'group') {
      // 标签/群组：调用随机用户接口
      const res = await userProfileApi.getRandomUsers(type, id, 50)
      tableData.value = res.data.data || []
    } else {
      ElMessage.warning('暂不支持该类型')
      hasSearched.value = false
      tableData.value = []
      return
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// 跳转到用户画像详情页
const goToUserDetail = (userId: string) => {
  router.push(`/insight/user-profile/detail/${userId}`)
}

onMounted(() => {
  fetchEntityIdentifiers()
})
</script>

<style scoped lang="scss">
.user-profile-page {
  padding: 20px 24px;
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .page-title {
    font-size: 20px;
    font-weight: 600;
    margin: 0;
    color: var(--el-text-color-primary);
  }

  .header-right {
    .entity-selector {
      display: flex;
      align-items: center;
      border: 1px solid var(--el-border-color);
      border-radius: 4px;
      overflow: hidden;
      background: #fff;

      .entity-label {
        padding: 8px 12px;
        font-size: 14px;
        color: var(--el-text-color-regular);
        border-right: 1px solid var(--el-border-color);
        background: var(--el-fill-color-light);
        white-space: nowrap;
      }

      .entity-select {
        :deep(.el-input__wrapper) {
          box-shadow: none;
          border-radius: 0;
          padding: 0 12px;
          min-height: 36px;
        }

        :deep(.el-input__inner) {
          font-size: 14px;
        }
      }
    }
  }
}

// 搜索卡片居中容器
.search-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.search-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px 32px;
  width: 100%;
  max-width: 900px;
  border: 1px solid var(--el-border-color-lighter);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.search-purpose {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);

  .purpose-icon {
    color: var(--el-color-primary);
    font-size: 16px;
    flex-shrink: 0;
  }

  .purpose-input {
    :deep(.el-input__wrapper) {
      box-shadow: none;
      padding-left: 0;
    }
  }
}

.search-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;

  &:last-child {
    margin-bottom: 0;
  }
}

.search-row-bottom {
  justify-content: space-between;
}

.search-field {
  display: flex;
  align-items: center;
  gap: 8px;

  .field-label {
    font-size: 14px;
    color: var(--el-text-color-regular);
    white-space: nowrap;
  }

  .time-icon {
    color: var(--el-color-primary);
    font-size: 14px;
  }
}

.search-actions {
  margin-left: auto;
}

.advanced-btn {
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.search-btn {
  padding: 10px 24px;
  border-radius: 6px;
}

.result-section {
  .result-title {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 16px;
    color: var(--el-text-color-primary);
  }
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;

  &:hover {
    background: var(--el-fill-color-light);
  }

  .user-name {
    color: var(--el-color-primary);
    font-weight: 500;

    &:hover {
      text-decoration: underline;
    }
  }

  .user-id {
    color: var(--el-text-color-secondary);
    font-size: 13px;
  }

  .user-play-icon {
    color: var(--el-color-primary);
    font-size: 14px;
    margin-left: 4px;
  }
}
</style>
