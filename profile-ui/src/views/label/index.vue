<template>
  <div class="label-management-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">标签管理</h2>
    </div>

    <div class="label-content">
      <!-- 左侧标签目录 -->
      <div class="label-sidebar">
        <div class="category-search">
          <el-input
            v-model="categorySearch"
            placeholder="请输入类目名称"
            clearable
            :prefix-icon="Search"
          />
        </div>
        
        <div class="category-tree">
          <div 
            class="category-item all-category"
            :class="{ active: selectedCategory === '' }"
            @click="selectCategory('')"
          >
            <el-icon><Folder /></el-icon>
            <span>全部标签</span>
          </div>
          
          <el-tree
            ref="categoryTreeRef"
            :data="filteredCategoryList"
            :props="{ label: 'category_name', children: 'children' }"
            node-key="category_id"
            :highlight-current="true"
            :expand-on-click-node="false"
            @node-click="handleCategoryClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node" :class="{ active: selectedCategory === data.category_id }">
                <el-icon><Folder /></el-icon>
                <span class="node-label">{{ node.label }}</span>
              </div>
            </template>
          </el-tree>
        </div>
      </div>

      <!-- 右侧标签列表 -->
      <div class="label-main">
        <!-- 筛选栏 -->
        <div class="filter-bar">
          <div class="filter-left">
            <el-radio-group v-model="entity" size="default">
              <el-radio-button label="entityIdentifier">实体</el-radio-button>
              <el-radio-button label="user">用户</el-radio-button>
            </el-radio-group>
          </div>
          
          <div class="filter-right">
            <el-select v-model="heatPeriod" placeholder="热度统计周期" style="width: 140px">
              <el-option label="最近 7 天" value="7d" />
              <el-option label="最近 30 天" value="30d" />
            </el-select>
            <el-input
              v-model="searchKeyword"
              placeholder="标签名称搜索"
              clearable
              :prefix-icon="Search"
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
            <el-button type="primary" :icon="Plus" @click="handleCreate">
              新建标签
            </el-button>
          </div>
        </div>

        <!-- 统计标签 -->
        <div class="stats-bar">
          <div class="stats-item">
            <span class="stats-label">总标签</span>
            <span class="stats-value">({{ totalCount }})</span>
          </div>
          <div class="stats-divider">|</div>
          <div class="stats-item">
            <span class="stats-label">已使用</span>
            <span class="stats-value">(0)</span>
          </div>
          <div class="stats-divider">|</div>
          <div class="stats-item">
            <span class="stats-label">未使用</span>
            <span class="stats-value">({{ totalCount }})</span>
          </div>
          <div class="stats-right">
            <el-checkbox v-model="onlyAuthorized">仅看有权限标签</el-checkbox>
            <el-dropdown trigger="click">
              <el-button link type="primary">
                <el-icon><Grid /></el-icon>
                列显示 {{ visibleColumns.length }}/{{ allColumns.length }}
              </el-button>
              <template #dropdown>
                <el-checkbox-group v-model="visibleColumns" class="column-selector">
                  <el-checkbox 
                    v-for="col in allColumns" 
                    :key="col.prop"
                    :label="col.prop"
                  >
                    {{ col.label }}
                  </el-checkbox>
                </el-checkbox-group>
              </template>
            </el-dropdown>
          </div>
        </div>

        <!-- 标签列表 -->
        <el-table
          v-loading="loading"
          :data="labelList"
          stripe
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="label_name" label="标签名称" min-width="150" show-overflow-tooltip />
          <el-table-column prop="label_desc" label="标签描述" min-width="180" show-overflow-tooltip />
          <el-table-column prop="label_status" label="标签状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.label_status === 1" type="success" size="small">启用</el-tag>
              <el-tag v-else type="danger" size="small">禁用</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="label_type" label="标签类型" width="120" />
          <el-table-column prop="label_produce_type" label="创建方式" width="100">
            <template #default="{ row }">
              <span v-if="row.label_produce_type === 1">系统生成</span>
              <span v-else-if="row.label_produce_type === 2">自定义</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="owner" label="标签负责人" width="120" />
          <el-table-column label="数据更新时间" width="160">
            <template #default="{ row }">
              {{ formatDateTime(row.gmt_modified) }}
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="160">
            <template #default="{ row }">
              {{ formatDateTime(row.gmt_create) }}
            </template>
          </el-table-column>
          <el-table-column prop="cover_count" label="覆盖量" width="100" />
          
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleDetail(row)">查看</el-button>
              <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="primary" @click="handleConfig(row)">配置</el-button>
              <el-dropdown trigger="click">
                <el-button link type="primary">
                  <el-icon><More /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleToggleStatus(row)">
                      {{ row.label_status === 1 ? '禁用' : '启用' }}
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="handleDelete(row)">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="queryParams.page_num"
            v-model:page-size="queryParams.page_size"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>

  <!-- 选择创建方式弹窗 -->
  <el-dialog
    v-model="createDialogVisible"
    title="请选择创建方式"
    width="700px"
    destroy-on-close
    :close-on-click-modal="true"
  >
    <div class="create-methods">
      <div class="method-card" @click="handleCreateByMethod('datasource')">
        <div class="method-icon">
          <el-icon :size="32" color="#409EFF"><Coin /></el-icon>
        </div>
        <div class="method-title">数据源导入</div>
        <div class="method-desc">从数据源导入画像平台时，通过为字段配置标签别名的方式给用户打标签，后续使用标签时，实质上是在使用对应的字段。</div>
        <el-button type="primary" plain>立即创建</el-button>
      </div>

      <div class="method-card" @click="handleCreateByMethod('custom')">
        <div class="method-icon">
          <el-icon :size="32" color="#67C23A"><Setting /></el-icon>
        </div>
        <div class="method-title">自定义标签</div>
        <div class="method-desc">可基于四则运算、简单函数实现标签的计算。</div>
        <el-button type="primary" plain>立即创建</el-button>
      </div>

      <div class="method-card" @click="handleCreateByMethod('sql')">
        <div class="method-icon">
          <el-icon :size="32" color="#E6A23C"><Document /></el-icon>
        </div>
        <div class="method-title">SQL</div>
        <div class="method-desc">可直接使用SQL语言进行标签的新建。</div>
        <el-button type="primary" plain>立即创建</el-button>
      </div>

      <div class="method-card" @click="handleCreateByMethod('upload')">
        <div class="method-icon">
          <el-icon :size="32" color="#F56C6C"><Upload /></el-icon>
        </div>
        <div class="method-title">上传文件</div>
        <div class="method-desc">上传包含实体ID(例如用户)的文件，可直接创建标签。</div>
        <el-button type="primary" plain>立即创建</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Search, Folder, Grid, More, Coin, Setting, Document, Upload
} from '@element-plus/icons-vue'
import { labelApi } from '@/api/label'
import { labelCategoryApi } from '@/api/labelCategory'
import type { Label, LabelCategory } from '@/types'

// 标签查询参数
interface LabelQueryParams {
  page_num: number
  page_size: number
  keyword?: string
  category_id?: string
  label_name?: string
}

const router = useRouter()

// 加载状态
const loading = ref(false)

// 实体类型
const entity = ref('entityIdentifier')

// 热度统计周期
const heatPeriod = ref('7d')

// 搜索关键词
const searchKeyword = ref('')

// 类目搜索
const categorySearch = ref('')

// 仅看有权限标签
const onlyAuthorized = ref(false)

// 选中的类目
const selectedCategory = ref('')

// 标签列表
const labelList = ref<Label[]>([])

// 标签类目列表
const categoryList = ref<LabelCategory[]>([])

// 总条数
const total = ref(0)

// 总数量
const totalCount = computed(() => labelList.value.length)

// 查询参数
const queryParams = reactive<LabelQueryParams>({
  page_num: 1,
  page_size: 10,
})

// 创建方式弹窗显示状态
const createDialogVisible = ref(false)

// 所有列
const allColumns = [
  { prop: 'cover_count', label: '覆盖数量' },
  { prop: 'cover_rate', label: '覆盖率' },
  { prop: 'label_value', label: '标签值' },
  { prop: 'gmt_modified', label: '更新时间' },
  { prop: 'exec_status', label: '执行状态' },
  { prop: 'creator', label: '创建人' },
  { prop: 'label_type', label: '创建方式' },
  { prop: 'source_type', label: '标签来源' },
  { prop: 'gmt_create', label: '创建时间' },
  { prop: 'label_desc', label: '标签说明' },
  { prop: 'heat_score', label: '总使用热度' },
  { prop: 'view_count', label: '透视次数' },
]

// 可见列
const visibleColumns = ref([
  'cover_count',
  'cover_rate',
  'label_value',
  'gmt_modified',
  'exec_status',
  'creator',
  'label_type',
  'source_type',
  'gmt_create',
  'label_desc',
])

// 构建树形结构
const buildCategoryTree = (list: LabelCategory[]): any[] => {
  const map: Record<string, any> = {}
  const roots: any[] = []
  
  // 先创建所有节点的映射
  list.forEach(item => {
    map[item.category_id] = {
      ...item,
      children: []
    }
  })
  
  // 构建树形结构
  list.forEach(item => {
    const node = map[item.category_id]
    if (item.parent_category_id && map[item.parent_category_id]) {
      map[item.parent_category_id].children.push(node)
    } else if (!item.parent_category_id) {
      // 没有父节点的作为根节点（一级类目）
      roots.push(node)
    }
  })
  
  return roots
}

// 过滤后的类目列表（树形结构）
const filteredCategoryList = computed(() => {
  const treeData = buildCategoryTree(categoryList.value)
  
  if (!categorySearch.value) return treeData
  
  // 搜索过滤
  const search = categorySearch.value.toLowerCase()
  const filterTree = (nodes: any[]): any[] => {
    return nodes.filter(node => {
      const match = node.category_name.toLowerCase().includes(search)
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

// 获取标签类目列表
const fetchCategoryList = async () => {
  try {
    const res = await labelCategoryApi.getList({})
    categoryList.value = res.data.data || []
  } catch (error) {
    console.error('获取标签类目失败:', error)
  }
}

// 所有标签列表（用于前端过滤）
const allLabelList = ref<Label[]>([])

// 获取标签列表
const fetchLabelList = async () => {
  loading.value = true
  try {
    const params: LabelQueryParams = {
      ...queryParams,
    }
    // 只有当搜索关键词不为空时才传递
    if (searchKeyword.value && searchKeyword.value.trim()) {
      params.label_name = searchKeyword.value.trim()
    }
    // 不传递分类ID，获取所有标签，然后在前端过滤
    const res = await labelApi.getList(params)
    allLabelList.value = res.data.data || []
    
    // 前端过滤：根据选中的分类及其子分类
    let filteredList = allLabelList.value
    if (selectedCategory.value) {
      const categoryIds = getAllCategoryIds(selectedCategory.value)
      filteredList = allLabelList.value.filter(label => 
        categoryIds.includes(label.label_category_id || '')
      )
    }
    
    labelList.value = filteredList
    total.value = filteredList.length
  } catch (error) {
    console.error('获取标签列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 选择类目
const selectCategory = (categoryId: string) => {
  selectedCategory.value = categoryId
  queryParams.page_num = 1
  fetchLabelList()
}

// 获取分类及其所有子分类的ID列表
const getAllCategoryIds = (categoryId: string): string[] => {
  const ids: string[] = [categoryId]
  
  const findChildren = (id: string) => {
    const children = categoryList.value.filter(cat => cat.parent_category_id === id)
    children.forEach(child => {
      ids.push(child.category_id)
      findChildren(child.category_id)
    })
  }
  
  findChildren(categoryId)
  return ids
}

// 点击类目树节点
const handleCategoryClick = (data: LabelCategory) => {
  selectedCategory.value = data.category_id
  queryParams.page_num = 1
  fetchLabelList()
}

// 搜索
const handleSearch = () => {
  queryParams.page_num = 1
  fetchLabelList()
}

// 创建标签
const handleCreate = () => {
  createDialogVisible.value = true
}

// 选择创建方式
const handleCreateByMethod = (method: string) => {
  createDialogVisible.value = false
  
  switch (method) {
    case 'datasource':
      router.push('/label/create/datasource')
      break
    case 'custom':
      router.push('/label/create/custom')
      break
    case 'sql':
      ElMessage.info('SQL创建标签功能开发中')
      // router.push('/label/create/sql')
      break
    case 'upload':
      ElMessage.info('上传文件创建标签功能开发中')
      // router.push('/label/create/upload')
      break
    default:
      break
  }
}

// 查看详情
const handleDetail = (row: Label) => {
  // TODO: 跳转到标签详情页
  ElMessage.info('标签详情功能开发中')
}

// 编辑标签
const handleEdit = (row: Label) => {
  // TODO: 跳转到编辑标签页面
  ElMessage.info('编辑标签功能开发中')
}

// 筛选标签
const handleFilter = (row: Label) => {
  // TODO: 打开筛选弹窗
  ElMessage.info('筛选功能开发中')
}

// 授权标签
const handleAuthorize = (row: Label) => {
  // TODO: 打开授权弹窗
  ElMessage.info('授权功能开发中')
}

// 更新标签
const handleUpdate = (row: Label) => {
  // TODO: 调用更新接口
  ElMessage.info('更新功能开发中')
}

// 删除标签
const handleDelete = (row: Label) => {
  ElMessageBox.confirm(
    `确定要删除标签 "${row.label_name}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await labelApi.delete(row.label_id)
        ElMessage.success('删除成功')
        fetchLabelList()
      } catch (error) {
        console.error('删除失败:', error)
      }
    })
    .catch(() => {
      // 取消删除
    })
}

// 配置标签
const handleConfig = (row: Label) => {
  ElMessage.info(`配置标签: ${row.label_name}`)
  // TODO: 打开配置弹窗
}

// 启用/禁用标签
const handleToggleStatus = async (row: Label) => {
  const newStatus = row.label_status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? "启用" : "禁用"
  
  try {
    await labelApi.update({
      ...row,
      label_status: newStatus
    })
    ElMessage.success(`${actionText}成功`)
    fetchLabelList()
  } catch (error) {
    console.error(`${actionText}失败:`, error)
  }
}

// 多选
const handleSelectionChange = (rows: Label[]) => {
  console.log('选中的标签:', rows)
}

// 分页大小变化
const handleSizeChange = (val: number) => {
  queryParams.page_size = val
  fetchLabelList()
}

// 页码变化
const handleCurrentChange = (val: number) => {
  queryParams.page_num = val
  fetchLabelList()
}

onMounted(() => {
  fetchCategoryList()
  fetchLabelList()
})
</script>

<style scoped lang="scss">
.label-management-page {
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

.label-content {
  display: flex;
  flex: 1;
  gap: 20px;
  overflow: hidden;
}

// 左侧边栏
.label-sidebar {
  width: 260px;
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  
  .category-search {
    margin-bottom: 16px;
  }
  
  .category-tree {
    flex: 1;
    overflow-y: auto;
    
    .all-category {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 10px 12px;
      cursor: pointer;
      border-radius: 6px;
      margin-bottom: 8px;
      
      &:hover {
        background-color: #f5f7fa;
      }
      
      &.active {
        background-color: #ecf5ff;
        color: #409EFF;
      }
    }
    
    :deep(.el-tree) {
      .el-tree-node__content {
        height: 36px;
        border-radius: 6px;
        
        &:hover {
          background-color: #f5f7fa;
        }
      }
      
      .el-tree-node.is-current > .el-tree-node__content {
        background-color: #ecf5ff;
        color: #409EFF;
      }
      
      .tree-node {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }
  }
}

// 右侧主内容
.label-main {
  flex: 1;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  
  .filter-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .filter-right {
      display: flex;
      gap: 12px;
      align-items: center;
    }
  }
  
  .stats-bar {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 16px;
    padding: 12px 0;
    border-bottom: 1px solid #ebeef5;
    
    .stats-item {
      display: flex;
      align-items: center;
      gap: 4px;
      
      .stats-label {
        color: #606266;
        font-size: 14px;
      }
      
      .stats-value {
        color: #909399;
        font-size: 14px;
      }
    }
    
    .stats-divider {
      color: #dcdfe6;
    }
    
    .stats-right {
      margin-left: auto;
      display: flex;
      align-items: center;
      gap: 16px;
    }
  }
  
  .el-table {
    flex: 1;
    overflow: auto;
  }
  
  .pagination {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}

// 列选择器
.column-selector {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 150px;
}

// 创建方式弹窗样式
.create-methods {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  padding: 10px;
}

.method-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  }

  .method-icon {
    width: 64px;
    height: 64px;
    border-radius: 12px;
    background-color: #f5f7fa;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16px;
  }

  .method-title {
    font-size: 16px;
    font-weight: 500;
    color: #303133;
    margin-bottom: 12px;
  }

  .method-desc {
    font-size: 13px;
    color: #606266;
    line-height: 1.6;
    margin-bottom: 20px;
    min-height: 60px;
  }

  .el-button {
    width: 120px;
  }
}
</style>
