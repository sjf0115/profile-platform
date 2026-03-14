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
            <el-radio-group v-model="entityType" size="default">
              <el-radio-button label="entity">实体</el-radio-button>
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
          <el-table-column prop="label_name" label="标签名称" min-width="150" />
          <el-table-column v-if="visibleColumns.includes('cover_count')" prop="cover_count" label="覆盖数量" width="100" />
          <el-table-column v-if="visibleColumns.includes('cover_rate')" prop="cover_rate" label="覆盖率" width="100" />
          <el-table-column v-if="visibleColumns.includes('label_value')" prop="label_value" label="标签值" min-width="120" />
          <el-table-column v-if="visibleColumns.includes('gmt_modified')" label="更新时间" width="160">
            <template #default="{ row }">
              {{ formatDateTime(row.gmt_modified) }}
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('exec_status')" prop="exec_status" label="执行状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.exec_status === 'success'" type="success" size="small">
                <el-icon><CircleCheck /></el-icon>
                执行成功
              </el-tag>
              <el-tag v-else-if="row.exec_status === 'running'" type="warning" size="small">
                执行中
              </el-tag>
              <el-tag v-else type="info" size="small">未执行</el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('creator')" prop="creator" label="创建人" width="100" />
          <el-table-column v-if="visibleColumns.includes('label_type')" prop="label_type" label="创建方式" width="100" />
          <el-table-column v-if="visibleColumns.includes('source_type')" prop="source_type" label="标签来源" width="100" />
          <el-table-column v-if="visibleColumns.includes('gmt_create')" label="创建时间" width="160">
            <template #default="{ row }">
              {{ formatDateTime(row.gmt_create) }}
            </template>
          </el-table-column>
          <el-table-column v-if="visibleColumns.includes('label_desc')" prop="label_desc" label="标签说明" min-width="150" show-overflow-tooltip />
          <el-table-column v-if="visibleColumns.includes('heat_score')" prop="heat_score" label="总使用热度" width="100" />
          <el-table-column v-if="visibleColumns.includes('view_count')" prop="view_count" label="透视次数" width="100" />
          
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
              <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="primary" @click="handleFilter(row)">筛选</el-button>
              <el-dropdown trigger="click">
                <el-button link type="primary">
                  <el-icon><More /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleAuthorize(row)">授权</el-dropdown-item>
                    <el-dropdown-item @click="handleUpdate(row)">更新</el-dropdown-item>
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
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Search, Folder, Grid, CircleCheck, More 
} from '@element-plus/icons-vue'
import type { Label, LabelCategory, LabelQueryParams } from '@/types'
import { labelApi, labelCategoryApi } from '@/api/label'

const router = useRouter()

// 加载状态
const loading = ref(false)

// 实体类型
const entityType = ref('entity')

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

// 过滤后的类目列表
const filteredCategoryList = computed(() => {
  if (!categorySearch.value) return categoryList.value
  const search = categorySearch.value.toLowerCase()
  return categoryList.value.filter(cat => 
    cat.category_name.toLowerCase().includes(search)
  )
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

// 获取标签列表
const fetchLabelList = async () => {
  loading.value = true
  try {
    const params: LabelQueryParams = {
      ...queryParams,
      label_name: searchKeyword.value,
    }
    if (selectedCategory.value) {
      params.label_category_id = selectedCategory.value
    }
    const res = await labelApi.getList(params)
    labelList.value = res.data.data || []
    total.value = res.data.data?.length || 0
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
  // TODO: 跳转到创建标签页面
  ElMessage.info('创建标签功能开发中')
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
</style>
