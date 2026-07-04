<template>
  <div class="user-label-panel">
    <!-- 头部：Tab + 操作 -->
    <div class="panel-header">
      <el-tabs v-model="activeTab" class="panel-tabs">
        <el-tab-pane label="标签" name="labels" />
        <el-tab-pane label="人群" name="groups" />
      </el-tabs>
    </div>

    <!-- 标签内容区 -->
    <div v-if="activeTab === 'labels'" class="labels-content">
      <!-- 搜索栏 + 展示标签 + 打标签按钮 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入标签名称或标签值"
          clearable
          class="search-input"
          style="width: 280px"
        >
          <template #suffix>
            <el-icon class="search-icon"><Search /></el-icon>
          </template>
        </el-input>
        <div class="action-buttons">
          <el-button class="display-btn" @click="showDisplayLabelDialog = true">
            <el-icon><Setting /></el-icon>
            展示标签
          </el-button>
          <el-button type="primary" class="tag-btn" @click="showAddLabelDialog = true">
            打标签
          </el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="displayCategories.length === 0 && !searchKeyword"
        description="暂无展示标签，请点击展示标签按钮选择"
      >
        <el-button type="primary" @click="showDisplayLabelDialog = true">选择展示标签</el-button>
      </el-empty>

      <!-- 搜索无结果 -->
      <el-empty
        v-else-if="filteredDisplayCategories.length === 0 && searchKeyword"
        description="未找到匹配的标签"
      />

      <!-- 展示标签列表 -->
      <div v-else class="category-list">
        <div v-for="category in filteredDisplayCategories" :key="category.category_id" class="category-section">
          <div class="category-title">{{ category.category_name }}</div>
          <div class="label-tags">
            <div
              v-for="label in category.labels"
              :key="label.label_id"
              class="label-tag"
              :class="{ 'is-tagged': isLabelTagged(label.label_id) }"
            >
              <span class="label-name">{{ label.label_name }}:</span>
              <span class="label-value">{{ label.label_value }}</span>
              <el-icon v-if="isLabelTagged(label.label_id)" class="label-close" @click="handleDeleteLabel(label.label_id)">
                <Close />
              </el-icon>
            </div>
          </div>
        </div>
      </div>

    </div>

    <!-- 人群内容区 -->
    <div v-else class="groups-content">
      <el-empty v-if="!groups || groups.length === 0" description="该用户暂无所属人群" />
      <div v-else class="group-list">
        <div v-for="group in groups" :key="group.group_id" class="group-item">
          <div class="group-name">{{ group.group_name }}</div>
          <div class="group-desc">{{ group.group_desc || '暂无描述' }}</div>
          <div class="group-meta">
            <el-tag size="small" :type="group.group_status === 1 ? 'success' : 'danger'">
              {{ group.group_status === 1 ? '启用' : '停用' }}
            </el-tag>
            <span class="meta-text">{{ group.group_count || 0 }} 人</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 展示标签选择对话框 -->
    <el-dialog v-model="showDisplayLabelDialog" title="展示标签" width="800px" class="display-label-dialog">
      <div class="dialog-content">
        <!-- 左侧类目列表 -->
        <div class="category-sidebar">
          <div
            v-for="category in dialogCategories"
            :key="category.category_id"
            class="category-item"
            :class="{ active: selectedCategory === category.category_id }"
            @click="selectedCategory = category.category_id"
          >
            <el-checkbox
              :model-value="isCategorySelected(category.category_id)"
              @change="toggleCategory(category.category_id)"
              @click.stop
            />
            <span class="category-name">{{ category.category_name }}</span>
          </div>
        </div>

        <!-- 右侧标签列表 -->
        <div class="label-grid-container">
          <div class="label-grid-header">标签名称</div>
          <div class="label-grid">
            <div
              v-for="label in currentCategoryLabels"
              :key="label.label_id"
              class="label-grid-item"
              :class="{ 
                selected: isLabelSelected(label.label_id),
                tagged: isLabelTagged(label.label_id)
              }"
              @click="toggleLabel(label.label_id)"
            >
              <el-checkbox
                :model-value="isLabelSelected(label.label_id)"
                @change="toggleLabel(label.label_id)"
                @click.stop
              />
              <span class="label-grid-name">{{ label.label_name }}</span>
              <el-tag v-if="isLabelTagged(label.label_id)" size="small" type="success" class="tagged-tag">
                已打标
              </el-tag>
              <el-tag v-else size="small" type="info" class="untagged-tag">
                未打标
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <div class="footer-left">
            <span class="selected-count">已选 {{ selectedLabelIds.length }}/{{ totalLabels }}</span>
            <el-button link type="primary" @click="clearSelection">[清空]</el-button>
          </div>
          <div class="footer-right">
            <el-button @click="showDisplayLabelDialog = false">取消</el-button>
            <el-button type="primary" @click="confirmDisplayLabels">确定</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 打标签对话框 -->
    <el-dialog v-model="showAddLabelDialog" title="打标签" width="480px">
      <el-form :model="addLabelForm" label-width="80px">
        <el-form-item label="选择标签">
          <el-select
            v-model="addLabelForm.labelId"
            placeholder="请选择标签"
            filterable
            style="width: 100%"
            @change="handleLabelSelect"
          >
            <el-option
              v-for="label in availableLabels"
              :key="label.label_id"
              :label="label.label_name"
              :value="label.label_id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标签值">
          <el-input v-model="addLabelForm.labelValue" placeholder="请输入标签值" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddLabelDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAddLabel">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Close, Setting } from '@element-plus/icons-vue'
import { labelApi } from '@/api/label'
import { labelCategoryApi } from '@/api/labelCategory'
import type { LabelCategoryVO, GroupVO, Label, UserLabelVO, LabelCategory } from '@/types'

const props = defineProps<{
  categories: LabelCategoryVO[]
  groups: GroupVO[]
}>()

const emit = defineEmits<{
  (e: 'label-added', labelId: string, labelValue: string): void
  (e: 'label-deleted', labelId: string): void
}>()

const activeTab = ref('labels')
const searchKeyword = ref('')
const categoryList = ref<LabelCategoryVO[]>([])
const showAddLabelDialog = ref(false)
const addLabelForm = ref({ labelId: '', labelValue: '' })
const availableLabels = ref<Label[]>([])
const labelLoading = ref(false)

// 展示标签选择相关
const showDisplayLabelDialog = ref(false)
const selectedLabelIds = ref<string[]>([])
const selectedCategory = ref('')
const tempSelectedLabelIds = ref<string[]>([])
const allLabels = ref<Label[]>([])  // 所有标签元数据
const allCategories = ref<LabelCategory[]>([])  // 所有类目

// 类目 ID 到名称的映射
const categoryMap = computed(() => {
  const map = new Map<string, string>()
  allCategories.value.forEach(cat => {
    map.set(cat.category_id, cat.category_name)
  })
  return map
})

// 展示标签的类目列表（从所有标签元数据中提取，只包含选中的标签）
const displayCategories = computed(() => {
  const categoryMap2 = new Map<string, { category_id: string; category_name: string; labels: any[] }>()
  
  // 只处理选中的标签
  const selectedLabels = allLabels.value.filter(label => 
    selectedLabelIds.value.includes(label.label_id)
  )
  
  selectedLabels.forEach(label => {
    const categoryId = label.label_category_id || 'uncategorized'
    const categoryName = categoryMap.value.get(categoryId) || '未分类'
    if (!categoryMap2.has(categoryId)) {
      categoryMap2.set(categoryId, { category_id: categoryId, category_name: categoryName, labels: [] })
    }
    // 标签值优先级：打标值 > 原始值
    const taggedValue = getTaggedValue(label.label_id)
    categoryMap2.get(categoryId)!.labels.push({
      ...label,
      label_value: taggedValue || label.label_value || '-'
    })
  })
  return Array.from(categoryMap2.values())
})

// 所有类目（用于展示标签对话框）
const dialogCategories = computed(() => {
  const categoryMap2 = new Map<string, { category_id: string; category_name: string; labels: any[] }>()
  allLabels.value.forEach(label => {
    const categoryId = label.label_category_id || 'uncategorized'
    const categoryName = categoryMap.value.get(categoryId) || '未分类'
    if (!categoryMap2.has(categoryId)) {
      categoryMap2.set(categoryId, { category_id: categoryId, category_name: categoryName, labels: [] })
    }
    categoryMap2.get(categoryId)!.labels.push(label)
  })
  return Array.from(categoryMap2.values())
})

// 当前选中的类目下的标签（用于对话框）
const currentCategoryLabels = computed(() => {
  const category = dialogCategories.value.find(c => c.category_id === selectedCategory.value)
  return category?.labels || []
})

// 总标签数
const totalLabels = computed(() => allLabels.value.length)

// 判断标签是否已打标
const isLabelTagged = (labelId: string) => {
  return categoryList.value.some(cat => cat.labels.some(l => l.label_id === labelId))
}

// 获取打标值
const getTaggedValue = (labelId: string) => {
  for (const cat of categoryList.value) {
    const label = cat.labels.find(l => l.label_id === labelId)
    if (label) return label.label_value
  }
  return null
}

// 判断类目是否被选中（所有标签都被选中）
const isCategorySelected = (categoryId: string) => {
  const category = dialogCategories.value.find(c => c.category_id === categoryId)
  if (!category) return false
  return category.labels.every(label => tempSelectedLabelIds.value.includes(label.label_id))
}

// 判断标签是否被选中
const isLabelSelected = (labelId: string) => {
  return tempSelectedLabelIds.value.includes(labelId)
}

// 切换类目选中状态
const toggleCategory = (categoryId: string) => {
  const category = dialogCategories.value.find(c => c.category_id === categoryId)
  if (!category) return
  
  const allSelected = isCategorySelected(categoryId)
  category.labels.forEach(label => {
    if (allSelected) {
      tempSelectedLabelIds.value = tempSelectedLabelIds.value.filter(id => id !== label.label_id)
    } else {
      if (!tempSelectedLabelIds.value.includes(label.label_id)) {
        tempSelectedLabelIds.value.push(label.label_id)
      }
    }
  })
}

// 切换标签选中状态
const toggleLabel = (labelId: string) => {
  const index = tempSelectedLabelIds.value.indexOf(labelId)
  if (index > -1) {
    tempSelectedLabelIds.value.splice(index, 1)
  } else {
    tempSelectedLabelIds.value.push(labelId)
  }
}

// 清空选择
const clearSelection = () => {
  tempSelectedLabelIds.value = []
}

// 确认展示标签
const confirmDisplayLabels = () => {
  selectedLabelIds.value = [...tempSelectedLabelIds.value]
  showDisplayLabelDialog.value = false
  ElMessage.success('展示标签已更新')
}

// 监听对话框打开，初始化临时选择并加载所有标签
watch(showDisplayLabelDialog, async (val) => {
  if (val) {
    tempSelectedLabelIds.value = [...selectedLabelIds.value]
    await Promise.all([fetchAllLabels(), fetchAllCategories()])
    if (!selectedCategory.value && dialogCategories.value.length > 0) {
      selectedCategory.value = dialogCategories.value[0].category_id
    }
  }
})

// 获取所有标签元数据
const fetchAllLabels = async () => {
  try {
    const res = await labelApi.getList({ label_status: 1, page: 1, size: 200 })
    allLabels.value = res.data.data || []
  } catch (error) {
    console.error('获取标签列表失败:', error)
    ElMessage.error('获取标签列表失败')
  }
}

// 获取所有类目
const fetchAllCategories = async () => {
  try {
    const res = await labelCategoryApi.getList({})
    allCategories.value = res.data.data || []
  } catch (error) {
    console.error('获取类目列表失败:', error)
  }
}

// 监听对话框打开，加载可用标签
watch(showAddLabelDialog, async (val) => {
  if (val) {
    await fetchAvailableLabels()
  }
})

// 获取可用标签列表
const fetchAvailableLabels = async () => {
  labelLoading.value = true
  try {
    const res = await labelApi.getList({ label_status: 1, page: 1, size: 100 })
    availableLabels.value = res.data.data || []
  } catch (error) {
    console.error('获取标签列表失败:', error)
    ElMessage.error('获取标签列表失败')
  } finally {
    labelLoading.value = false
  }
}

// 选择标签后自动填充标签名
const handleLabelSelect = (labelId: string) => {
  const label = availableLabels.value.find(l => l.label_id === labelId)
  if (label && !addLabelForm.value.labelValue) {
    addLabelForm.value.labelValue = label.label_name
  }
}

// 监听 props.categories 变化，同步到 categoryList
watch(() => props.categories, (newVal) => {
  categoryList.value = [...newVal]
}, { immediate: true, deep: true })

// 搜索过滤（基于展示标签）
const filteredDisplayCategories = computed(() => {
  if (!searchKeyword.value) return displayCategories.value
  const keyword = searchKeyword.value.toLowerCase()
  return displayCategories.value
    .map(category => ({
      ...category,
      labels: category.labels.filter(label =>
        label.label_name.toLowerCase().includes(keyword) ||
        label.label_value.toLowerCase().includes(keyword)
      )
    }))
    .filter(category => category.labels.length > 0)
})

// 删除标签（带确认）
const handleDeleteLabel = async (labelId: string) => {
  try {
    await ElMessageBox.confirm('确定要删除该标签吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    emit('label-deleted', labelId)
  } catch {
    // 用户取消
  }
}

// 确认添加标签
const confirmAddLabel = () => {
  if (!addLabelForm.value.labelId) {
    ElMessage.warning('请选择标签')
    return
  }
  if (!addLabelForm.value.labelValue) {
    ElMessage.warning('请输入标签值')
    return
  }
  emit('label-added', addLabelForm.value.labelId, addLabelForm.value.labelValue)
  // 自动加入展示配置
  if (!selectedLabelIds.value.includes(addLabelForm.value.labelId)) {
    selectedLabelIds.value.push(addLabelForm.value.labelId)
  }
  showAddLabelDialog.value = false
  addLabelForm.value = { labelId: '', labelValue: '' }
}
</script>

<style scoped lang="scss">
.user-label-panel {
  .panel-header {
    margin-bottom: 16px;

    .panel-tabs {
      :deep(.el-tabs__header) {
        margin-bottom: 0;
        border-bottom: 1px solid var(--el-border-color-lighter);
      }

      :deep(.el-tabs__nav-wrap::after) {
        display: none;
      }

      :deep(.el-tabs__item) {
        font-size: 15px;
        padding: 0 20px;
        height: 40px;
        line-height: 40px;

        &.is-active {
          color: var(--el-color-primary);
          font-weight: 500;
        }
      }

      :deep(.el-tabs__active-bar) {
        height: 2px;
      }
    }
  }
}

.labels-content {
  .search-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .search-input {
      :deep(.el-input__wrapper) {
        border-radius: 4px;
      }

      .search-icon {
        color: var(--el-text-color-secondary);
      }
    }

    .action-buttons {
      display: flex;
      gap: 12px;
    }

    .display-btn {
      padding: 10px 20px;
      border-radius: 4px;
    }

    .tag-btn {
      padding: 10px 20px;
      border-radius: 4px;
    }
  }
}

.category-list {
  .category-section {
    margin-bottom: 24px;
  }

  .category-title {
    font-size: 14px;
    color: var(--el-text-color-regular);
    margin-bottom: 12px;
    font-weight: 500;
  }

  .label-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }

  .label-tag {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 8px 14px;
    background: #ecf5ff;
    border: 1px solid #d9ecff;
    border-radius: 4px;
    font-size: 13px;
    transition: all 0.2s;

    &:hover {
      background: #e6f1fd;
      border-color: #c6e2ff;
    }

    // 已打标标签样式
    &.is-tagged {
      background: #ecf5ff;
      border-color: #d9ecff;
      
      .label-name {
        color: #409eff;
        font-weight: 500;
      }
      
      .label-value {
        color: #606266;
      }
    }

    // 未打标标签样式
    &:not(.is-tagged) {
      background: #f4f4f5;
      border-color: #e9e9eb;
      
      .label-name {
        color: #909399;
      }
      
      .label-value {
        color: #c0c4cc;
      }
    }

    .label-name {
      font-weight: 500;
    }

    .label-value {
      color: #606266;
    }

    .label-close {
      font-size: 12px;
      color: #909399;
      cursor: pointer;
      opacity: 0;
      transition: opacity 0.2s;
      margin-left: 4px;

      &:hover {
        color: #f56c6c;
      }
    }

    &:hover .label-close {
      opacity: 1;
    }
  }
}

.groups-content {
  .group-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .group-item {
    padding: 16px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;
    transition: box-shadow 0.2s;

    &:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    }

    .group-name {
      font-size: 15px;
      font-weight: 500;
      margin-bottom: 4px;
      color: var(--el-text-color-primary);
    }

    .group-desc {
      font-size: 13px;
      color: var(--el-text-color-secondary);
      margin-bottom: 8px;
    }

    .group-meta {
      display: flex;
      align-items: center;
      gap: 8px;

      .meta-text {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }
}

// 展示标签对话框样式
.display-label-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.dialog-content {
  display: flex;
  height: 400px;
}

.category-sidebar {
  width: 200px;
  border-right: 1px solid var(--el-border-color-lighter);
  overflow-y: auto;
  padding: 16px 0;

  .category-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: var(--el-fill-color-light);
    }

    &.active {
      background: var(--el-fill-color-light);
    }

    .category-name {
      font-size: 14px;
      color: var(--el-text-color-regular);
    }
  }
}

.label-grid-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .label-grid-header {
    padding: 12px 16px;
    font-size: 14px;
    font-weight: 500;
    color: var(--el-text-color-primary);
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .label-grid {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
    align-content: start;
  }

  .label-grid-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    border-radius: 4px;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: var(--el-fill-color-light);
    }

    &.selected {
      background: #ecf5ff;
    }

    .label-grid-name {
      font-size: 14px;
      color: var(--el-text-color-regular);
      flex: 1;
    }

    .tagged-tag {
      font-size: 12px;
    }

    .untagged-tag {
      font-size: 12px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;

  .footer-left {
    display: flex;
    align-items: center;
    gap: 8px;

    .selected-count {
      font-size: 14px;
      color: var(--el-text-color-regular);
    }
  }

  .footer-right {
    display: flex;
    gap: 12px;
  }
}
</style>
