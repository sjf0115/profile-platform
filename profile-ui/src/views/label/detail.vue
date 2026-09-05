<template>
  <div class="label-detail-page">
    <!-- 顶部信息区 -->
    <div class="detail-header" v-loading="loading">
      <div class="header-main">
        <div class="header-left">
          <el-button link @click="goBack" class="back-btn">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <div class="title-section">
            <div class="main-title">
              <el-icon class="title-icon"><CollectionTag /></el-icon>
              <h2>{{ labelData.label_name }}</h2>
            </div>
            <div class="meta-info">
              <span class="meta-item">
                <span class="meta-label">负责人：</span>
                <span class="meta-value">{{ labelData.owner_name || labelData.creator_name || '-' }}</span>
              </span>
              <span class="meta-item">
                <span class="meta-label">标签ID：</span>
                <span class="meta-value">{{ labelData.label_id }}</span>
              </span>
              <span class="meta-item">
                <span class="meta-label">状态：</span>
                <el-tag :type="statusTagType(labelData.label_status)" size="small">{{ statusName(labelData.label_status) }}</el-tag>
              </span>
            </div>
            <div class="desc-info" v-if="labelData.label_desc">
              <span class="desc-label">标签描述：</span>
              <span class="desc-value">{{ labelData.label_desc }}</span>
            </div>
          </div>
        </div>
        <div class="header-right">
          <div class="test-card">
            <div class="test-title">测一测</div>
            <div class="test-subtitle">TESTING</div>
            <div class="test-bottom">
              <span class="test-desc">使用个人数据开始测试</span>
              <el-button type="primary" size="small" @click="handleDeveloping">开始测试</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 统计指标区 -->
      <div class="stats-section">
        <div class="stats-left">
          <div class="stat-box">
            <div class="stat-label">覆盖量</div>
            <div class="stat-value large">{{ distribution.has_data ? (formatNumber(distribution.cover_count) || '-') : '-' }}</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-box">
            <div class="stat-label">覆盖率</div>
            <div class="stat-value large">{{ distribution.has_data && distribution.cover_rate != null ? distribution.cover_rate + '%' : '-' }}</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-box">
            <div class="stat-label">数据更新</div>
            <div class="stat-value">{{ getTimeTypeName(labelData.label_time_type) }}</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-box health-box" @click="handleDeveloping">
            <div class="stat-label">
              健康度
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </div>
            <div class="stat-value">-</div>
          </div>
        </div>
        <div class="stats-action">
          <el-button type="primary" :icon="ShoppingCart" @click="handleDeveloping">加入申请篮</el-button>
        </div>
      </div>
    </div>

    <div class="page-content">

      <!-- 主要内容区 -->
      <div class="main-content">
        <!-- 左侧基础信息 -->
        <div class="left-panel">
          <div class="section">
            <div class="section-title">基础信息</div>
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">标签类目</span>
                <span class="info-value">{{ getCategoryName(labelData.label_category_id) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">标签归属</span>
                <span class="info-value">{{ labelData.owner_name || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">主键ID</span>
                <span class="info-value">{{ labelData.label_id }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">数据类型</span>
                <span class="info-value">{{ getDataTypeName(labelData.label_data_type) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">数据分布类型</span>
                <span class="info-value">{{ getDistTypeName(labelData.label_dist_type) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">数据组织类型</span>
                <span class="info-value">{{ getOrganizeTypeName(labelData.label_organize_type) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">加工方式</span>
                <span class="info-value">{{ getProduceTypeName(labelData.label_produce_type) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">关联实体</span>
                <span class="info-value">{{ labelData.entity_identifier_name || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">绑定数据集</span>
                <span class="info-value">
                  <template v-if="labelData.dataset_id">
                    <el-link type="primary" :underline="false" @click="goDatasetDetail">{{ labelData.dataset_name || labelData.dataset_id }}</el-link>
                    <span v-if="labelData.dataset_field_name" class="field-suffix">（{{ labelData.dataset_field_name }}）</span>
                  </template>
                  <template v-else>-</template>
                </span>
              </div>
            </div>
          </div>

          <div class="section">
            <div class="section-title">更多信息</div>
            <div class="info-list">
              <div class="info-item">
                <span class="info-label">标签类型</span>
                <span class="info-value">{{ getLabelTypeName(labelData.label_type) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">创建方式</span>
                <span class="info-value">{{ getSourceTypeName(labelData.source_type) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">接入来源</span>
                <span class="info-value">{{ getAccessSource() }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">标签更新类型</span>
                <span class="info-value">{{ getTimeTypeName(labelData.label_time_type) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">创建人</span>
                <span class="info-value">{{ labelData.creator_name || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">修改人</span>
                <span class="info-value">{{ labelData.modifier_name || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">创建时间</span>
                <span class="info-value">{{ formatDateTime(labelData.gmt_create) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">修改时间</span>
                <span class="info-value">{{ formatDateTime(labelData.gmt_modified) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧标签分布 -->
        <div class="right-panel">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="取值分布" name="distribution">
              <div class="distribution-header">
                <span class="title">取值分布 Top10</span>
                <span class="update-time" v-if="labelData.gmt_modified">(数据更新时间：{{ formatDateTime(labelData.gmt_modified) }})</span>
              </div>
              <div class="sample-value" v-if="distribution.sample_value">
                <span class="label">标签样例：</span>
                <el-tag size="small">{{ distribution.sample_value }}</el-tag>
              </div>
              <el-table v-if="distribution.has_data && distribution.values.length > 0" :data="distribution.values" v-loading="distLoading" style="width: 100%">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column prop="value" label="取值范围" min-width="150" />
                <el-table-column prop="count" label="计数" min-width="120" />
                <el-table-column prop="percent" label="人次占比" width="200">
                  <template #default="{ row }">
                    <div class="percent-cell">
                      <span>{{ row.percent }}%</span>
                      <el-progress :percentage="Math.min(row.percent, 100)" :show-text="false" :stroke-width="8" />
                    </div>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-else :description="distLoading ? '加载中...' : '暂无分布数据（标签未绑定或引擎表未就绪）'" />
            </el-tab-pane>
            <el-tab-pane label="覆盖量信息" name="coverage">
              <div class="placeholder-content">覆盖量信息开发中</div>
            </el-tab-pane>
            <el-tab-pane label="产出信息" name="output">
              <div class="placeholder-content">产出信息开发中</div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight, CollectionTag, ShoppingCart } from '@element-plus/icons-vue'
import { labelApi, type LabelConfigResponse, type LabelValueDistribution } from '@/api/label'
import { labelCategoryApi, type LabelCategory } from '@/api/labelCategory'
import type { Label } from '@/types'

const route = useRoute()
const router = useRouter()
const labelId = route.params.id as string

const loading = ref(false)
const activeTab = ref('distribution')

// 标签数据
const labelData = ref<Label>({} as Label)

// 类目列表
const categoryList = ref<LabelCategory[]>([])

// 标签配置
const labelConfig = reactive<LabelConfigResponse>({
  label_type: [],
  data_type: [],
  dist_type: [],
  organize_type: [],
  produce_type: [],
  time_type: [],
  source_type: []
})

// 分布数据
const distLoading = ref(false)
const distribution = ref<LabelValueDistribution>({
  label_id: '',
  has_data: false,
  values: []
})

// 获取标签取值分布与覆盖量
const fetchDistribution = async () => {
  distLoading.value = true
  try {
    const res = await labelApi.getDistribution(labelId)
    if (res.data.data) {
      distribution.value = res.data.data
    }
  } catch (error) {
    console.error('获取标签分布失败:', error)
  } finally {
    distLoading.value = false
  }
}

// 获取标签详情
const fetchLabelDetail = async () => {
  loading.value = true
  try {
    const res = await labelApi.getDetail(labelId)
    labelData.value = res.data.data || ({} as Label)
  } catch (error) {
    console.error('获取标签详情失败:', error)
    ElMessage.error('获取标签详情失败')
  } finally {
    loading.value = false
  }
}

// 获取类目列表
const fetchCategoryList = async () => {
  try {
    const res = await labelCategoryApi.getList({})
    categoryList.value = res.data.data || []
  } catch (error) {
    console.error('获取类目列表失败:', error)
  }
}

// 获取标签配置
const fetchLabelConfig = async () => {
  try {
    const res = await labelApi.getConfig()
    const data = res.data.data
    if (data) {
      labelConfig.label_type = data.label_type || []
      labelConfig.data_type = data.data_type || []
      labelConfig.dist_type = data.dist_type || []
      labelConfig.organize_type = data.organize_type || []
      labelConfig.produce_type = data.produce_type || []
      labelConfig.time_type = data.time_type || []
      labelConfig.source_type = data.source_type || []
    }
  } catch (error) {
    console.error('获取标签配置失败:', error)
  }
}

// 获取类目名称
const getCategoryName = (categoryId?: string) => {
  if (!categoryId) return '-'
  const category = categoryList.value.find(c => c.category_id === categoryId)
  return category?.category_name || '-'
}

// 获取数据类型名称
const getDataTypeName = (type?: number) => {
  if (!type) return '-'
  const item = labelConfig.data_type.find(t => t.id === type)
  return item?.name || '-'
}

// 获取分布类型名称
const getDistTypeName = (type?: number) => {
  if (!type) return '-'
  const item = labelConfig.dist_type.find(t => t.id === type)
  return item?.name || '-'
}

// 获取组织类型名称
const getOrganizeTypeName = (type?: number) => {
  if (!type) return '-'
  const item = labelConfig.organize_type.find(t => t.id === type)
  return item?.name || '-'
}

// 获取加工类型名称
const getProduceTypeName = (type?: number) => {
  if (!type) return '-'
  const item = labelConfig.produce_type.find(t => t.id === type)
  return item?.name || '-'
}

// 获取创建方式名称
const getSourceTypeName = (type?: number) => {
  if (!type) return '-'
  const item = labelConfig.source_type.find(t => t.id === type)
  return item?.name || '-'
}

// 获取标签类型名称
const getLabelTypeName = (type?: string | number) => {
  if (!type) return '-'
  const item = labelConfig.label_type.find(t => t.id.toString() === type.toString())
  return item?.name || '-'
}

// 获取时效性类型名称
const getTimeTypeName = (type?: number) => {
  if (!type) return '-'
  const item = labelConfig.time_type.find(t => t.id === type)
  return item?.name || '-'
}

// 获取接入来源（由创建方式推导）
const getAccessSource = () => {
  const sourceType = labelData.value.source_type
  if (sourceType === 2) {
    return labelData.value.dataset_name ? `数据集导入 · ${labelData.value.dataset_name}` : '数据集导入'
  }
  return getSourceTypeName(sourceType)
}

// 标签状态名称
const statusName = (status?: number) => {
  const map: Record<number, string> = { 0: '未绑定', 1: '已启用', 2: '已停用' }
  return status != null ? (map[status] || '-') : '-'
}

// 标签状态标签颜色
const statusTagType = (status?: number) => {
  const map: Record<number, string> = { 0: 'info', 1: 'success', 2: 'danger' }
  return (status != null ? map[status] : 'info') as 'info' | 'success' | 'danger'
}

// 暂不实现的功能统一提示
const handleDeveloping = () => {
  ElMessage.info('功能开发中')
}

// 跳转绑定数据集详情
const goDatasetDetail = () => {
  if (labelData.value.dataset_id) {
    router.push(`/dataset/detail/${labelData.value.dataset_id}`)
  }
}

// 格式化日期时间
const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString()
}

// 格式化数字
const formatNumber = (num?: number) => {
  if (num == null) return ''
  if (num >= 100000000) {
    return (num / 100000000).toFixed(1) + '亿'
  }
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toString()
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchLabelDetail()
  fetchDistribution()
  fetchCategoryList()
  fetchLabelConfig()
})
</script>

<style scoped lang="scss">
.label-detail-page {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

// 顶部详情头部
.detail-header {
  background-color: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.header-main {
  display: flex;
  justify-content: space-between;
  margin-bottom: 24px;
  
  .header-left {
    display: flex;
    gap: 16px;
    flex: 1;
    
    .back-btn {
      padding: 0;
      height: auto;
    }
    
    .title-section {
      .main-title {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 12px;
        
        .title-icon {
          color: #ff6b35;
          font-size: 24px;
        }
        
        h2 {
          margin: 0;
          font-size: 20px;
          font-weight: 600;
          color: #303133;
        }
      }
      
      .meta-info {
        display: flex;
        gap: 24px;
        margin-bottom: 8px;
        
        .meta-item {
          font-size: 13px;
          
          .meta-label {
            color: #909399;
          }
          
          .meta-value {
            color: #606266;
          }
        }
      }
      
      .desc-info {
        font-size: 13px;
        color: #606266;
        
        .desc-label {
          color: #909399;
        }
      }
    }
  }
  
  .header-right {
    .test-card {
      background: linear-gradient(135deg, #ff8c69 0%, #ff6b35 100%);
      border-radius: 8px;
      padding: 16px 20px;
      width: 220px;
      color: #fff;
      
      .test-title {
        font-size: 18px;
        font-weight: 600;
        margin-bottom: 2px;
      }
      
      .test-subtitle {
        font-size: 11px;
        opacity: 0.7;
        margin-bottom: 16px;
      }
      
      .test-bottom {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
        
        .test-desc {
          font-size: 12px;
          opacity: 0.9;
          white-space: nowrap;
        }
        
        .el-button {
          background-color: #fff;
          border-color: #fff;
          color: #ff6b35;
          font-weight: 500;
          flex-shrink: 0;
          
          &:hover {
            background-color: #f5f5f5;
            border-color: #f5f5f5;
          }
        }
      }
    }
  }
}

// 统计指标区
.stats-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fafafa;
  border-radius: 8px;
  padding: 16px 24px;
  
  .stats-left {
    display: flex;
    align-items: center;
    flex: 1;
  }
  
  .stat-box {
    flex: 1;
    text-align: center;
    
    .stat-label {
      font-size: 13px;
      color: #909399;
      margin-bottom: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 4px;
      
      .arrow-icon {
        font-size: 12px;
        color: #c0c4cc;
      }
    }
    
    .stat-value {
      font-size: 16px;
      font-weight: 500;
      color: #303133;
      
      &.large {
        font-size: 24px;
        font-weight: 600;
      }
    }
  }
  
  .health-box {
    cursor: pointer;
  }
  
  .stat-divider {
    width: 1px;
    height: 40px;
    background-color: #e4e7ed;
    margin: 0 24px;
  }
  
  .stats-action {
    margin-left: 24px;
  }
}

.page-content {
  flex: 1;
  overflow: auto;
}

.stats-cards {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  
  .stat-card {
    background-color: #fff;
    border-radius: 8px;
    padding: 20px;
    min-width: 150px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    
    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-bottom: 8px;
    }
    
    .stat-value {
      font-size: 24px;
      font-weight: 500;
      color: #303133;
    }
  }
}

.main-content {
  display: flex;
  gap: 20px;
  
  .left-panel {
    width: 320px;
    flex-shrink: 0;
    
    .section {
      background-color: #fff;
      border-radius: 8px;
      padding: 20px;
      margin-bottom: 20px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
      
      .section-title {
        font-size: 16px;
        font-weight: 500;
        margin-bottom: 16px;
        padding-bottom: 12px;
        border-bottom: 1px solid #ebeef5;
      }
      
      .info-list {
        .info-item {
          display: flex;
          justify-content: space-between;
          padding: 10px 0;
          border-bottom: 1px solid #f5f7fa;
          
          &:last-child {
            border-bottom: none;
          }
          
          .info-label {
            font-size: 14px;
            color: #909399;
          }
          
          .info-value {
            font-size: 14px;
            color: #303133;
            font-weight: 500;
            text-align: right;
            
            .field-suffix {
              font-size: 12px;
              color: #909399;
              font-weight: 400;
            }
          }
        }
      }
    }
  }
  
  .right-panel {
    flex: 1;
    background-color: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    
    .distribution-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .title {
        font-size: 16px;
        font-weight: 500;
      }
      
      .update-time {
        font-size: 12px;
        color: #909399;
      }
    }
    
    .sample-value {
      margin-bottom: 16px;
      
      .label {
        font-size: 14px;
        color: #606266;
        margin-right: 8px;
      }
    }
    
    .percent-cell {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .el-progress {
        flex: 1;
      }
    }
    
    .placeholder-content {
      padding: 40px;
      text-align: center;
      color: #909399;
    }
  }
}
</style>
