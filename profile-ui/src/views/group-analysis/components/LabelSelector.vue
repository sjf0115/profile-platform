<template>
  <div class="label-selector">
    <!-- 左侧类目树 -->
    <div class="selector-left">
      <div class="left-header">标签类目</div>
      <div class="tree-list">
        <div
          v-for="cat in categoryList"
          :key="cat.category_id"
          class="tree-item"
          :class="{ active: selectedCategoryId === cat.category_id }"
          @click="handleCategoryClick(cat.category_id)"
        >
          <span class="item-icon">
            <el-icon><FolderOpened v-if="selectedCategoryId === cat.category_id" /><Folder v-else /></el-icon>
          </span>
          <span class="item-name">{{ cat.category_name }}</span>
          <span class="item-count">{{ cat.count }}</span>
        </div>
      </div>
    </div>

    <!-- 右侧标签勾选区 -->
    <div class="selector-right">
      <!-- 搜索与筛选 -->
      <div class="right-header">
        <el-input
          v-model="labelSearch"
          placeholder="搜索标签名"
          clearable
          size="default"
          style="width: 220px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="dataTypeFilter" placeholder="全部" size="default" style="width: 100px" clearable>
          <el-option label="文本型" :value="1" />
          <el-option label="数值型" :value="2" />
        </el-select>
      </div>

      <!-- 标签网格 -->
      <div class="label-grid">
        <el-checkbox-group v-model="selectedLabelIds" @change="handleSelectionChange">
          <el-checkbox
            v-for="label in filteredLabels"
            :key="label.label_id"
            :value="label.label_id"
            class="label-item"
          >
            {{ label.label_name }}
          </el-checkbox>
        </el-checkbox-group>
        <el-empty v-if="filteredLabels.length === 0" description="暂无可选标签" :image-size="50" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Search, Folder, FolderOpened } from '@element-plus/icons-vue'
import type { AnalysisLabel } from '@/types'

const props = defineProps<{
  labels: AnalysisLabel[]
  modelValue: string[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string[]]
  'change': [value: string[]]
}>()

const labelSearch = ref('')
const dataTypeFilter = ref<number | undefined>(undefined)
const selectedCategoryId = ref<string>('__all__')
const selectedLabelIds = ref<string[]>([...props.modelValue])

// 监听外部值变化（内容比较，避免循环更新）
watch(() => props.modelValue, (val) => {
  const newVal = JSON.stringify([...val].sort())
  const curVal = JSON.stringify([...selectedLabelIds.value].sort())
  if (newVal !== curVal) {
    selectedLabelIds.value = [...val]
  }
})

// 类目列表（扁平展示）
const categoryList = computed(() => {
  const catMap = new Map<string, { category_id: string; category_name: string; count: number }>()
  catMap.set('__all__', { category_id: '__all__', category_name: '全部标签', count: props.labels.length })
  for (const label of props.labels) {
    const catId = label.label_category_id || 'uncategorized'
    const catName = label.label_category_name || '未分类'
    if (!catMap.has(catId)) {
      catMap.set(catId, { category_id: catId, category_name: catName, count: 0 })
    }
    catMap.get(catId)!.count++
  }
  return Array.from(catMap.values())
})

// 过滤后的标签
const filteredLabels = computed(() => {
  return props.labels.filter(label => {
    if (labelSearch.value && !label.label_name.includes(labelSearch.value)) return false
    if (dataTypeFilter.value && label.label_data_type !== dataTypeFilter.value) return false
    if (selectedCategoryId.value && selectedCategoryId.value !== '__all__'
        && label.label_category_id !== selectedCategoryId.value) return false
    return true
  })
})

const handleCategoryClick = (categoryId: string) => {
  selectedCategoryId.value = categoryId
}

const handleSelectionChange = (val: string[]) => {
  // 关键：el-checkbox-group 原地修改数组，val 与内部 selectedLabelIds 是同一引用
  // 必须 emit 新数组拷贝，否则父组件 watch 检测不到引用变化
  const newVal = [...val]
  emit('update:modelValue', newVal)
  emit('change', newVal)
}
</script>

<style scoped lang="scss">
.label-selector {
  display: flex;
  background: #fff;
  min-height: 300px;
}

.selector-left {
  width: 200px;
  border-right: 1px solid #ebeef5;
  overflow-y: auto;
  flex-shrink: 0;

  .left-header {
    padding: 10px 14px;
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    border-bottom: 1px solid #ebeef5;
  }

  .tree-list {
    padding: 4px 0;
  }

  .tree-item {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 14px;
    cursor: pointer;
    font-size: 13px;
    color: #606266;
    transition: all 0.15s;

    &:hover {
      background: #f5f7fa;
    }

    &.active {
      background: #ecf5ff;
      color: #409eff;
      font-weight: 500;
    }

    .item-icon {
      font-size: 14px;
      color: #c0c4cc;
    }

    &.active .item-icon {
      color: #409eff;
    }

    .item-name {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .item-count {
      font-size: 12px;
      color: #c0c4cc;
      flex-shrink: 0;
    }
  }
}

.selector-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;

  .right-header {
    display: flex;
    gap: 8px;
    padding: 10px 14px;
    border-bottom: 1px solid #ebeef5;
    flex-shrink: 0;
  }

  .label-grid {
    flex: 1;
    overflow-y: auto;
    padding: 10px 14px;

    .el-checkbox-group {
      display: grid;
      grid-template-columns: repeat(6, 1fr);
      gap: 4px 10px;
    }

    .label-item {
      height: 30px;
      margin: 0 !important;
      font-size: 13px;
      overflow: hidden;

      :deep(.el-checkbox__label) {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        padding-left: 6px;
      }
    }
  }
}
</style>
