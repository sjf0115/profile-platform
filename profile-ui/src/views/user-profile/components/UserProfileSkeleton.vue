<template>
  <div class="user-detail-page">
    <aside class="user-info-sidebar">
      <el-card>
        <el-skeleton animated>
          <template #template>
            <div style="display: flex; flex-direction: column; align-items: center; padding: 20px 0;">
              <el-skeleton-item variant="circle" style="width: 80px; height: 80px;" />
              <el-skeleton-item variant="h3" style="width: 40%; margin-top: 16px;" />
              <el-skeleton-item variant="text" style="width: 60%; margin-top: 8px;" />
              <div style="display: flex; gap: 12px; margin-top: 20px; width: 100%; padding: 0 20px;">
                <el-skeleton-item variant="button" style="width: 50%; height: 32px;" />
                <el-skeleton-item variant="button" style="width: 50%; height: 32px;" />
              </div>
              <div style="width: 100%; margin-top: 24px; padding: 0 20px;">
                <el-skeleton-item variant="h1" style="width: 30%; margin-bottom: 12px;" />
                <el-skeleton-item variant="text" style="width: 100%; margin-bottom: 8px;" />
                <el-skeleton-item variant="text" style="width: 80%; margin-bottom: 8px;" />
                <el-skeleton-item variant="text" style="width: 90%; margin-bottom: 8px;" />
                <el-skeleton-item variant="text" style="width: 70%;" />
              </div>
            </div>
          </template>
        </el-skeleton>
      </el-card>
    </aside>
    <main class="user-content-area">
      <el-card>
        <el-skeleton animated :rows="12" />
      </el-card>
    </main>
  </div>
</template>

<style scoped lang="scss">
.user-detail-page {
  display: flex;
  gap: 16px;
  padding: 20px;

  .user-info-sidebar {
    width: 320px;
    flex-shrink: 0;
  }

  .user-content-area {
    flex: 1;
    min-width: 0;
  }
}
</style>
