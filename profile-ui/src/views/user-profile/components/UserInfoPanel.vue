<template>
  <div class="user-info-panel">
    <!-- 头像区域 -->
    <div class="user-header">
      <el-avatar :size="48" class="user-avatar">
        <el-icon><User /></el-icon>
      </el-avatar>
      <div class="user-phone">
        <el-icon class="phone-icon"><Message /></el-icon>
        <span>{{ maskedEmail }}</span>
      </div>
    </div>

    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" class="info-tabs">
      <el-tab-pane label="基础信息" name="basic" />
      <el-tab-pane label="身份信息" name="identity" />
    </el-tabs>

    <!-- 提示信息 -->
    <div v-if="noticeText" class="notice-box">
      <span class="notice-text">{{ noticeText }}</span>
      <el-icon class="notice-arrow"><ArrowDown /></el-icon>
    </div>

    <!-- 基础信息列表 -->
    <div v-if="activeTab === 'basic'" class="info-list">
      <div class="info-item">
        <span class="info-label">用户ID</span>
        <span class="info-value">{{ user?.user_id }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">用户名</span>
        <span class="info-value">{{ user?.user_name }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">邮箱</span>
        <span class="info-value">{{ maskedEmail }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">创建时间</span>
        <span class="info-value">{{ formatDateTime(user?.gmt_create) }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">更新时间</span>
        <span class="info-value">{{ formatDateTime(user?.gmt_modified) }}</span>
      </div>
    </div>

    <!-- 身份信息列表 -->
    <div v-else class="info-list">
      <div class="info-item">
        <span class="info-label">用户类型</span>
        <span class="info-value">{{ user?.user_type || '普通用户' }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">来源类型</span>
        <span class="info-value">{{ sourceTypeText }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">角色数量</span>
        <span class="info-value">{{ user?.roles?.length || 0 }} 个</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { User, Message, ArrowDown } from '@element-plus/icons-vue'
import type { UserVO } from '@/types'

const props = defineProps<{
  user: UserVO | null
}>()

const activeTab = ref('basic')

// 脱敏邮箱
const maskedEmail = computed(() => {
  const email = props.user?.email || ''
  if (!email) return '-'
  const [localPart, domain] = email.split('@')
  if (localPart.length <= 2) return email
  return `${localPart.substring(0, 2)}****@${domain}`
})

// 来源类型
const sourceTypeText = computed(() => {
  const map: Record<number, string> = { 1: '系统内置', 2: '自定义' }
  return map[props.user?.source_type || 0] || '未知'
})

// 动态提示信息
const noticeText = computed(() => {
  if (!props.user?.email) return '该用户暂无邮箱信息'
  return ''
})

// 格式化时间
const formatDateTime = (dateTime?: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped lang="scss">
.user-info-panel {
  background: #fff;
  border-radius: 0 0 8px 8px;
  padding: 20px;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;

  .user-avatar {
    background-color: #409eff;
    color: #fff;
  }

  .user-phone {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    color: var(--el-text-color-regular);

    .phone-icon {
      color: var(--el-text-color-secondary);
      font-size: 14px;
    }
  }
}

.info-tabs {
  margin-bottom: 16px;

  :deep(.el-tabs__header) {
    margin-bottom: 0;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }

  :deep(.el-tabs__item) {
    font-size: 14px;
    padding: 0 16px;
    height: 36px;
    line-height: 36px;

    &.is-active {
      color: var(--el-color-primary);
      font-weight: 500;
    }
  }

  :deep(.el-tabs__active-bar) {
    height: 2px;
  }
}

.notice-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  background: var(--el-fill-color-light);
  border-radius: 4px;
  margin-bottom: 16px;
  cursor: pointer;

  .notice-text {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .notice-arrow {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-left: 8px;
  }
}

.info-list {
  .info-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px solid var(--el-fill-color-lighter);

    &:last-child {
      border-bottom: none;
    }
  }

  .info-label {
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }

  .info-value {
    font-size: 13px;
    color: var(--el-text-color-primary);
    text-align: right;
    max-width: 60%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    &.customer-id {
      display: flex;
      gap: 4px;
      align-items: center;
    }
  }
}
</style>
