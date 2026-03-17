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
                <span class="meta-value">{{ labelData.owner || labelData.creator || '-' }}</span>
              </span>
              <span class="meta-item">
                <span class="meta-label">标签ID：</span>
                <span class="meta-value">{{ labelData.label_id }}</span>
              </span>
              <span class="meta-item">
                <span class="meta-label">标签英文名：</span>
                <span class="meta-value">{{ labelData.label_name }}</span>
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
              <el-button type="primary" size="small">开始测试</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 统计指标区 -->
      <div class="stats-section">
        <div class="stats-left">
          <div class="stat-box">
            <div class="stat-label">覆盖量</div>
            <div class="stat-value large">{{ formatNumber(labelData.cover_count) || '大于10亿' }}</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-box">
            <div class="stat-label">数据更新</div>
            <div class="stat-value">{{ getTimeTypeName(labelData.label_time_type) }}</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-box">
            <div class="stat-label">
              健康度
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </div>
            <div class="health-value">
              <span class="health-text">高</span>
              <div class="health-bars">
                <div class="bar active"></div>
                <div class="bar active"></div>
                <div class="bar active"></div>
                <div class="bar"></div>
              </div>
            </div>
          </div>
        </div>
        <div class="stats-action">
          <el-button type="primary" :icon="ShoppingCart">加入申请篮</el-button>
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
                <span class="info-value">{{ labelData.creator || '-' }}</span>
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
                <span class="info-label">来源类型</span>
                <span class="info-value">{{ labelData.source_type === 1 ? '系统内置' : '自定义' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">接入来源</span>
                <span class="info-value">odps表接入</span>
              </div>
              <div class="info-item">
                <span class="info-label">存储方式</span>
                <span class="info-value">odps</span>
              </div>
              <div class="info-item">
                <span class="info-label">标签更新类型</span>
                <span class="info-value">{{ getTimeTypeName(labelData.label_time_type) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">执行状态</span>
                <span class="info-value">{{ labelData.exec_status || '正常' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">热度评分</span>
                <span class="info-value">{{ labelData.heat_score || 0 }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">透视次数</span>
                <span class="info-value">{{ labelData.view_count || 0 }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">覆盖率</span>
                <span class="info-value">{{ labelData.cover_rate || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">标签值</span>
                <span class="info-value">{{ labelData.label_value || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">创建人</span>
                <span class="info-value">{{ labelData.creator || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">修改人</span>
                <span class="info-value">{{ labelData.modifier || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">创建时间</span>
                <span class="info-value">{{ formatDateTime(labelData.gmt_create) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">修改时间</span>
                <span class="info-value">{{ formatDateTime(labelData.gmt_modified) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">首次上架时间</span>
                <span class="info-value">{{ formatDate(labelData.gmt_create) }}</span>
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
                <span class="update-time">(数据更新时间：{{ formatDateTime(new Date().toISOString()) }})</span>
              </div>
              <div class="sample-value">
                <span class="label">标签样例：</span>
                <el-tag size="small">温州市</el-tag>
              </div>
              <el-table :data="distributionData" style="width: 100%">
                <el-table-column type="index" label="序号" width="60" align="center" />
                <el-table-column prop="value" label="取值范围" min-width="150" />
                <el-table-column prop="desc" label="值描述" min-width="150" />
                <el-table-column prop="percent" label="人次占比" width="200">
                  <template #default="{ row }">
                    <div class="percent-cell">
                      <span>{{ row.percent }}%</span>
                      <el-progress :percentage="row.percent" :show-text="false" :stroke-width="8" />
                    </div>
                  </template>
                </el-table-column>
              </el-table>
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
import { labelApi, type LabelConfigResponse } from '@/api/label'
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
  time_type: []
})

// 分布数据（模拟数据）
const distributionData = ref([
  { value: '广州市', desc: '广州市', percent: 15.2 },
  { value: '北京市', desc: '北京市', percent: 12.8 },
  { value: '上海市', desc: '上海市', percent: 11.5 },
  { value: '深圳市', desc: '深圳市', percent: 9.3 },
  { value: '杭州市', desc: '杭州市', percent: 7.6 },
  { value: '成都市', desc: '成都市', percent: 6.2 },
  { value: '武汉市', desc: '武汉市', percent: 5.8 },
])

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

// 获取标签类型名称（字符串类型）
const getLabelTypeName = (type?: string) => {
  if (!type) return '-'
  // 根据 label_type 字符串返回中文
  const typeMap: Record<string, string> = {
    'datasource': '数据源导入',
    'custom': '自定义标签',
    'sql': 'SQL',
    'upload': '上传文件'
  }
  return typeMap[type] || type
}

// 获取时效性类型名称
const getTimeTypeName = (type?: number) => {
  if (!type) return '-'
  const item = labelConfig.time_type.find(t => t.id === type)
  return item?.name || '-'
}

// 获取健康度评分
const getHealthScore = () => {
  // 模拟健康度评分
  return 4
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString()
}

// 格式化日期时间
const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString()
}

// 格式化数字
const formatNumber = (num?: number) => {
  if (!num) return ''
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
detail-header {
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
    
    .health-value {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      
      .health-text {
        font-size: 16px;
        font-weight: 500;
        color: #67c23a;
      }
      
      .health-bars {
        display: flex;
        gap: 3px;
        
        .bar {
          width: 4px;
          height: 16px;
          background-color: #e4e7ed;
          border-radius: 2px;
          
          &.active {
            background-color: #67c23a;
          }
        }
      }
    }
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
