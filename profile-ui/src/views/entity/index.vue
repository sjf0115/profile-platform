<template>
  <div class="entity-management-page">
    <div class="page-header">
      <h2 class="page-title">实体管理</h2>
    </div>

    <div class="page-content">
      <el-tabs v-model="activeTab" class="entity-tabs">
        <el-tab-pane label="实体标识管理" name="entityIdentifier">
          <EntityIdentifierList />
        </el-tab-pane>
        <el-tab-pane label="实体管理" name="entity">
          <EntityList />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import EntityIdentifierList from './components/EntityIdentifierList.vue'
import EntityList from './components/EntityList.vue'

const activeTab = ref('entityIdentifier')
</script>

<style scoped lang="scss">
.entity-management-page {
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

.page-content {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  flex: 1;
  overflow: auto;
}

.entity-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 20px;
  }
}
</style>
