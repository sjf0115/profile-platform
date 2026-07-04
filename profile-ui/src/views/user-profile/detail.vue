<template>
  <div class="user-detail-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h2 class="page-title">用户画像</h2>
    </div>

    <!-- 骨架屏 -->
    <UserProfileSkeleton v-if="loading" />

    <!-- 内容区域 -->
    <div v-else-if="profile" class="detail-content">
      <!-- 左侧：用户信息面板 -->
      <aside class="user-info-sidebar">
        <UserInfoPanel :user="profile.user" />
      </aside>

      <!-- 右侧：标签/人群面板 -->
      <main class="user-content-area">
        <UserLabelPanel
          :categories="profile.label_categories"
          :groups="profile.groups"
          @label-added="(labelId: string, labelValue: string) => handleAddLabel(labelId, labelValue)"
          @label-deleted="handleDeleteLabel"
        />
      </main>
    </div>

    <!-- 加载失败 -->
    <el-empty v-else description="用户不存在或加载失败">
      <el-button @click="fetchProfile">重试</el-button>
      <el-button type="primary" @click="goBack">返回</el-button>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useUserProfile } from '@/composables/useUserProfile'
import UserProfileSkeleton from './components/UserProfileSkeleton.vue'
import UserInfoPanel from './components/UserInfoPanel.vue'
import UserLabelPanel from './components/UserLabelPanel.vue'

const route = useRoute()
const router = useRouter()
const userId = route.params.id as string

const {
  profile,
  loading,
  fetchProfile,
  handleAddLabel,
  handleDeleteLabel,
} = useUserProfile(userId)

const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped lang="scss">
.user-detail-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 0;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: #fff;

  .page-title {
    font-size: 18px;
    font-weight: 600;
    margin: 0;
    color: var(--el-text-color-primary);
  }
}

// 内容区域
.detail-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.user-info-sidebar {
  width: 320px;
  flex-shrink: 0;
  background: #f5f7fa;
  border-right: 1px solid var(--el-border-color-lighter);
  overflow-y: auto;
  padding: 0 20px 20px;
}

.user-content-area {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
  padding: 0 20px 20px;
  background: #fff;
}
</style>
