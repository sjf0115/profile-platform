<template>
  <el-dialog
    v-model="dialogVisible"
    title="请选择创建方式"
    width="700px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <div class="create-type-list">
      <!-- 规则创建 -->
      <div class="create-type-card" @click="handleSelect('rule')">
        <div class="card-icon">
          <el-icon><Setting /></el-icon>
        </div>
        <div class="card-content">
          <h3 class="card-title">规则创建</h3>
          <p class="card-desc">可根据行为、属性和原有分群进行组合筛选，灵活创建分群。</p>
        </div>
        <el-button type="primary" plain>立即创建</el-button>
      </div>

      <!-- 上传文件 -->
      <div class="create-type-card" @click="handleSelect('upload')">
        <div class="card-icon upload">
          <el-icon><Upload /></el-icon>
        </div>
        <div class="card-content">
          <h3 class="card-title">上传文件</h3>
          <p class="card-desc">上传包含用户UUID的文件，可直接创建相关人群。</p>
        </div>
        <el-button type="primary" plain>立即创建</el-button>
      </div>

      <!-- SQL创建 -->
      <div class="create-type-card" @click="handleSelect('sql')">
        <div class="card-icon sql">
          <el-icon><Document /></el-icon>
        </div>
        <div class="card-content">
          <h3 class="card-title">SQL创建</h3>
          <p class="card-desc">可根据SQL自定义分群规则。</p>
        </div>
        <el-button type="primary" plain>立即创建</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Setting, Upload, Document } from '@element-plus/icons-vue'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'select': [type: string]
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const handleSelect = (type: string) => {
  emit('select', type)
}
</script>

<style scoped lang="scss">
.create-type-list {
  display: flex;
  gap: 20px;
  padding: 20px 0;
}

.create-type-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.1);
  }

  .card-icon {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background-color: #f0f9ff;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16px;

    .el-icon {
      font-size: 28px;
      color: #409eff;
    }

    &.upload {
      background-color: #f0f9ff;
      .el-icon {
        color: #67c23a;
      }
    }

    &.sql {
      background-color: #f0f9ff;
      .el-icon {
        color: #e6a23c;
      }
    }
  }

  .card-content {
    text-align: center;
    margin-bottom: 20px;

    .card-title {
      font-size: 16px;
      font-weight: 500;
      margin: 0 0 8px 0;
      color: #303133;
    }

    .card-desc {
      font-size: 13px;
      color: #909399;
      margin: 0;
      line-height: 1.5;
    }
  }
}
</style>
