<template>
  <div class="forbidden-page">
    <div class="forbidden-content">
      <el-icon class="forbidden-icon" color="#E6A23C"><WarningFilled /></el-icon>
      <h1 class="forbidden-title">403</h1>
      <p class="forbidden-desc">抱歉，您没有权限访问该页面</p>
      <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { WarningFilled } from '@element-plus/icons-vue'
</script>

<style scoped lang="scss">
.forbidden-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;

  .forbidden-content {
    text-align: center;

    .forbidden-icon {
      font-size: 72px;
    }

    .forbidden-title {
      font-size: 48px;
      color: #303133;
      margin: 16px 0 8px;
    }

    .forbidden-desc {
      font-size: 16px;
      color: #909399;
      margin-bottom: 24px;
    }
  }
}
</style>
