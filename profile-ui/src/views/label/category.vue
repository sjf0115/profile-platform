<template>
  <div class="label-category-page">
    <el-card class="page-card">
      <template #header>
        <div class="card-header">
          <span class="title">标签类目管理</span>
        </div>
      </template>

      <!-- 搜索和操作区域 -->
      <div class="toolbar">
        <div class="left-actions">
          <el-button type="primary" :icon="Plus" @click="handleAdd(1)">
            新增一级类目
          </el-button>
        </div>
        <div class="right-filters">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索类目名称"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            查询
          </el-button>
        </div>
      </div>

      <!-- 类目表格 -->
      <el-table
        v-loading="loading"
        :data="filteredCategoryList"
        stripe
        border
        row-key="category_id"
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        style="width: 100%"
      >
        <el-table-column prop="category_name" label="类目名称" min-width="200">
          <template #default="{ row }">
            <el-icon style="margin-right: 8px"><Folder /></el-icon>
            {{ row.category_name }}
            <el-tag v-if="row.is_default === 1" size="small" type="info" style="margin-left: 8px">默认</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category_level" label="级别" width="100">
          <template #default="{ row }">
            {{ row.category_level }} 级
          </template>
        </el-table-column>
        <el-table-column prop="creator" label="创建人" width="120" />
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.gmt_create) }}
          </template>
        </el-table-column>
        <el-table-column label="修改时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.gmt_modified) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Plus" @click="handleAddSub(row)" v-if="row.category_level < 3">
              新增子类目
            </el-button>
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)" v-if="row.is_default !== 1">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑类目弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="400px"
      destroy-on-close
    >
      <el-form :model="formData" label-width="100px">
        <el-form-item label="类目名称" required>
          <el-input v-model="formData.category_name" placeholder="请输入类目名称" />
        </el-form-item>
        <el-form-item label="父级类目" v-if="formData.category_level && formData.category_level > 1">
          <el-input v-model="parentCategoryName" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Folder, Search, Refresh } from '@element-plus/icons-vue'
import { labelCategoryApi, type LabelCategory } from '@/api/labelCategory'

// 类目列表
const categoryList = ref<LabelCategory[]>([])

// 加载状态
const loading = ref(false)

// 搜索关键词
const searchKeyword = ref('')

// 弹窗显示状态
const dialogVisible = ref(false)
const dialogTitle = ref('新增类目')
const isEdit = ref(false)

// 表单数据
interface FormData {
  category_id: string
  category_name: string
  category_level: number
  parent_category_id: string
}

const formData = ref<FormData>({
  category_id: '',
  category_name: '',
  category_level: 1,
  parent_category_id: '',
})

// 构建树形结构的数据
const buildTreeData = (list: LabelCategory[]): any[] => {
  const map: Record<string, any> = {}
  const roots: any[] = []
  
  // 先创建所有节点的映射
  list.forEach(item => {
    map[item.category_id] = {
      ...item,
      children: [],
      hasChildren: false
    }
  })
  
  // 构建树形结构
  list.forEach(item => {
    const node = map[item.category_id]
    if (item.parent_category_id && map[item.parent_category_id]) {
      map[item.parent_category_id].children.push(node)
      map[item.parent_category_id].hasChildren = true
    } else {
      roots.push(node)
    }
  })
  
  return roots
}

// 过滤后的类目列表（树形结构）
const filteredCategoryList = computed(() => {
  const treeData = buildTreeData(categoryList.value)
  
  if (!searchKeyword.value) {
    return treeData
  }
  
  // 搜索过滤
  const keyword = searchKeyword.value.toLowerCase()
  const filterTree = (nodes: any[]): any[] => {
    return nodes.filter(node => {
      const match = node.category_name.toLowerCase().includes(keyword)
      const children = filterTree(node.children || [])
      if (children.length > 0) {
        node.children = children
        return true
      }
      return match
    })
  }
  
  return filterTree(treeData)
})

// 搜索
const handleSearch = () => {
  // 搜索通过计算属性自动触发
}

// 重置
const handleReset = () => {
  searchKeyword.value = ''
}

// 父级类目名称
const parentCategoryName = computed(() => {
  if (formData.value.category_level === 2) {
    const parent = categoryList.value.find(item => item.category_id === formData.value.parent_category_id)
    return parent?.category_name || ''
  }
  if (formData.value.category_level === 3) {
    const parent = categoryList.value.find(item => item.category_id === formData.value.parent_category_id)
    return parent?.category_name || ''
  }
  return ''
})

// 新增子类目
const handleAddSub = (row: LabelCategory) => {
  isEdit.value = false
  const level = (row.level || 1) + 1
  dialogTitle.value = `新增${level}级类目`
  formData.value = {
    category_id: '',
    category_name: '',
    category_level: level,
    parent_category_id: row.category_id,
  }
  dialogVisible.value = true
}

// 获取类目列表
const fetchCategoryList = async () => {
  loading.value = true
  try {
    const res = await labelCategoryApi.getList({})
    categoryList.value = res.data.data || []
  } catch (error) {
    console.error('获取类目列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 新增类目
const handleAdd = (level: number) => {
  isEdit.value = false
  dialogTitle.value = `新增${level}级类目`
  formData.value = {
    category_id: '',
    category_name: '',
    category_level: level,
    parent_category_id: '',
  }
  dialogVisible.value = true
}

// 编辑类目
const handleEdit = (item: LabelCategory) => {
  isEdit.value = true
  dialogTitle.value = '编辑类目'
  formData.value = {
    category_id: item.category_id,
    category_name: item.category_name,
    category_level: item.level || 1,
    parent_category_id: item.parent_category_id || '',
  }
  dialogVisible.value = true
}

// 保存类目
const handleSave = async () => {
  if (!formData.value.category_name) {
    ElMessage.warning('请输入类目名称')
    return
  }
  
  try {
    if (isEdit.value && formData.value.category_id) {
      // 编辑
      await labelCategoryApi.rename(formData.value.category_id, formData.value.category_name)
      ElMessage.success('编辑成功')
    } else {
      // 新增
      await labelCategoryApi.add(
        formData.value.category_name,
        formData.value.parent_category_id || ''
      )
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchCategoryList()
  } catch (error) {
    console.error('保存失败:', error)
  }
}

// 删除类目
const handleDelete = (item: LabelCategory) => {
  // 默认一级类目不能删除
  if (item.level === 1 && item.category_name === '未分类') {
    ElMessage.warning('默认类目不能删除')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除类目 "${item.category_name}" 吗？删除后该类目下的标签将划归到未分类类目中。`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await labelCategoryApi.delete(item.category_id)
        ElMessage.success('删除成功')
        fetchCategoryList()
      } catch (error) {
        console.error('删除失败:', error)
      }
    })
    .catch(() => {
      // 取消删除
    })
}

// 格式化日期时间
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
  fetchCategoryList()
})
</script>

<style scoped lang="scss">
.label-category-page {
  .page-card {
    min-height: calc(100vh - 104px);
  }

  .card-header {
    .title {
      font-size: 16px;
      font-weight: 600;
    }
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    flex-wrap: wrap;
    gap: 12px;

    .left-actions {
      display: flex;
      gap: 10px;
    }

    .right-filters {
      display: flex;
      gap: 10px;
      align-items: center;
    }
  }
.category-level {
  display: flex;
  align-items: flex-start;
  margin-bottom: 24px;
  min-height: 60px;
  
  &:last-of-type {
    margin-bottom: 0;
  }
  
  .level-label {
    width: 80px;
    color: #909399;
    font-size: 14px;
    padding-top: 8px;
    flex-shrink: 0;
  }
  
  .level-content {
    flex: 1;
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    min-height: 40px;
    
    .category-tag {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      padding: 8px 16px;
      background-color: #f5f7fa;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      cursor: pointer;
      transition: all 0.2s;
      
      &:hover {
        background-color: #ecf5ff;
        border-color: #b3d8ff;
      }
      
      &.active {
        background-color: #ecf5ff;
        border-color: #409eff;
        color: #409eff;
      }
      
      &.default-category {
        color: #909399;
        cursor: default;
        
        &:hover {
          background-color: #f5f7fa;
          border-color: #e4e7ed;
        }
      }
      
      .tag-name {
        font-size: 14px;
      }
      
      .tag-icon {
        font-size: 14px;
        color: #909399;
        cursor: pointer;
        transition: color 0.2s;
        
        &:hover {
          color: #409eff;
        }
        
        &.delete:hover {
          color: #f56c6c;
        }
      }
    }
    
    :deep(.el-empty) {
      padding: 20px 0;
    }
  }
  
  .level-actions {
    display: flex;
    gap: 8px;
    margin-left: 16px;
    flex-shrink: 0;
  }
}

.footer-actions {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}
}
</style>
