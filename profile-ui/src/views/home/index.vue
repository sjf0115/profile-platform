<template>
  <div class="home-page">
    <!-- 欢迎信息 -->
    <div class="welcome-section">
      <div class="welcome-text">
        <h2>下午好，管理员</h2>
        <p>数据更新时间：{{ currentDate }}</p>
      </div>
    </div>

    <div class="dashboard-content">
      <!-- 左侧内容 -->
      <div class="left-content">
        <!-- 客户概览 -->
        <el-card class="overview-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">客户概览</span>
            </div>
          </template>
          <div class="overview-stats">
            <div class="stat-item main-stat">
              <div class="stat-label">总客户数</div>
              <div class="stat-value">3,845</div>
              <div class="stat-changes">
                <span class="change-item">近一日新增 0</span>
                <span class="change-item">近一日合并 0</span>
                <span class="change-item">近一日删除 0</span>
              </div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-label">会员或潜在会员总数</div>
              <div class="stat-value">1,459</div>
              <div class="stat-sub">近一日转化 0</div>
            </div>
          </div>
          
          <!-- 趋势图 -->
          <div class="trend-chart">
            <div class="chart-header">
              <div class="legend">
                <span class="legend-item">
                  <span class="legend-dot total"></span>
                  总客户数
                </span>
                <span class="legend-item">
                  <span class="legend-dot new"></span>
                  新增客户数
                </span>
              </div>
              <div class="chart-value">新增客户数 0</div>
            </div>
            <div class="chart-placeholder">
              <div class="chart-line"></div>
            </div>
            <div class="chart-dates">
              <span>08-28</span>
              <span>08-31</span>
              <span>09-03</span>
              <span>09-06</span>
              <span>09-09</span>
              <span>09-12</span>
              <span>09-15</span>
              <span>09-18</span>
              <span>09-21</span>
              <span>09-24</span>
              <span>09-26</span>
            </div>
          </div>
        </el-card>

        <!-- ID概览 -->
        <el-card class="overview-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">ID概览</span>
            </div>
          </template>
          <div class="id-overview">
            <div class="id-stat">
              <div class="id-label">手机号</div>
              <div class="id-value">3,845</div>
            </div>
            <div class="id-stat">
              <div class="id-label">设备ID</div>
              <div class="id-value">2,156</div>
            </div>
            <div class="id-stat">
              <div class="id-label">邮箱</div>
              <div class="id-value">1,234</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧内容 -->
      <div class="right-content">
        <!-- 常用功能 -->
        <el-card class="feature-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">常用功能</span>
              <el-button link type="primary" class="edit-btn">
                编辑 <el-icon><ArrowDown /></el-icon>
              </el-button>
            </div>
          </template>
          <div class="feature-grid">
            <div class="feature-item" @click="router.push('/label')">
              <div class="feature-icon blue">
                <el-icon><CollectionTag /></el-icon>
              </div>
              <span class="feature-name">用户标签</span>
            </div>
            <div class="feature-item" @click="router.push('/insight')">
              <div class="feature-icon green">
                <el-icon><DataAnalysis /></el-icon>
              </div>
              <span class="feature-name">用户分析</span>
            </div>
            <div class="feature-item" @click="router.push('/auto-marketing')">
              <div class="feature-icon orange">
                <el-icon><Promotion /></el-icon>
              </div>
              <span class="feature-name">自动化营销</span>
            </div>
            <div class="feature-item" @click="router.push('/datasource')">
              <div class="feature-icon purple">
                <el-icon><Coin /></el-icon>
              </div>
              <span class="feature-name">数据源</span>
            </div>
          </div>
        </el-card>

        <!-- 功能上新 -->
        <el-card class="feature-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">功能上新</span>
              <el-button link type="primary">更多 ></el-button>
            </div>
          </template>
          <div class="feature-list">
            <div class="feature-news-item">
              <el-tag size="small" type="warning">功能上新</el-tag>
              <span class="news-text">用户属性管理、客户管理、行级权限控制等功能上线</span>
            </div>
            <div class="feature-news-item">
              <el-tag size="small" type="warning">功能上线</el-tag>
              <span class="news-text">标签中心、IDMapping、自动化营销流程优化</span>
            </div>
            <div class="feature-news-item">
              <el-tag size="small" type="warning">功能上线</el-tag>
              <span class="news-text">支持自定义偏好类标签</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowDown, CollectionTag, DataAnalysis, Promotion, Coin } from '@element-plus/icons-vue'

const router = useRouter()

const currentDate = computed(() => {
  const date = new Date()
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
})
</script>

<style scoped lang="scss">
.home-page {
  padding: 0;
}

.welcome-section {
  margin-bottom: 20px;
  
  .welcome-text {
    h2 {
      margin: 0 0 8px 0;
      font-size: 24px;
      font-weight: 500;
      color: #303133;
    }
    
    p {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }
}

.dashboard-content {
  display: flex;
  gap: 20px;
  
  .left-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
  
  .right-content {
    width: 360px;
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
}

.overview-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .card-title {
      font-size: 16px;
      font-weight: 500;
      color: #303133;
    }
    
    .edit-btn {
      font-size: 14px;
    }
  }
  
  .overview-stats {
    display: flex;
    padding: 20px 0;
    
    .stat-item {
      flex: 1;
      
      &.main-stat {
        flex: 2;
      }
      
      .stat-label {
        color: #606266;
        font-size: 14px;
        margin-bottom: 8px;
      }
      
      .stat-value {
        font-size: 36px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 12px;
      }
      
      .stat-changes {
        display: flex;
        flex-direction: column;
        gap: 4px;
        
        .change-item {
          font-size: 12px;
          color: #909399;
        }
      }
      
      .stat-sub {
        font-size: 12px;
        color: #909399;
      }
    }
    
    .stat-divider {
      width: 1px;
      background-color: #ebeef5;
      margin: 0 20px;
    }
  }
  
  .trend-chart {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
    
    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .legend {
        display: flex;
        gap: 16px;
        
        .legend-item {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 12px;
          color: #606266;
          
          .legend-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            
            &.total {
              background-color: #67c23a;
            }
            
            &.new {
              background-color: #409eff;
            }
          }
        }
      }
      
      .chart-value {
        font-size: 12px;
        color: #909399;
      }
    }
    
    .chart-placeholder {
      height: 120px;
      background: linear-gradient(180deg, rgba(103, 194, 58, 0.1) 0%, rgba(103, 194, 58, 0.05) 100%);
      border-radius: 4px;
      position: relative;
      margin-bottom: 12px;
      
      .chart-line {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 60%;
        background: linear-gradient(180deg, rgba(103, 194, 58, 0.3) 0%, rgba(103, 194, 58, 0.1) 100%);
        border-radius: 4px;
      }
    }
    
    .chart-dates {
      display: flex;
      justify-content: space-between;
      font-size: 12px;
      color: #909399;
    }
  }
  
  .id-overview {
    display: flex;
    gap: 40px;
    padding: 10px 0;
    
    .id-stat {
      .id-label {
        font-size: 14px;
        color: #606266;
        margin-bottom: 8px;
      }
      
      .id-value {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
      }
    }
  }
}

.feature-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .card-title {
      font-size: 16px;
      font-weight: 500;
      color: #303133;
    }
  }
  
  .feature-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    
    .feature-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        background-color: #f5f7fa;
      }
      
      .feature-icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 12px;
        
        &.blue {
          background-color: #ecf5ff;
          color: #409eff;
        }
        
        &.green {
          background-color: #f0f9eb;
          color: #67c23a;
        }
        
        &.orange {
          background-color: #fdf6ec;
          color: #e6a23c;
        }
        
        &.purple {
          background-color: #f5f0ff;
          color: #9254de;
        }
        
        .el-icon {
          font-size: 24px;
        }
      }
      
      .feature-name {
        font-size: 14px;
        color: #606266;
      }
    }
  }
  
  .feature-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    
    .feature-news-item {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      padding: 8px 0;
      
      .news-text {
        font-size: 14px;
        color: #606266;
        line-height: 1.5;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 1;
        -webkit-box-orient: vertical;
      }
    }
  }
}
</style>
