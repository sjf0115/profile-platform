<template>
  <el-container class="main-layout">
    <!-- 顶部导航栏 -->
    <el-header class="top-header">
      <div class="top-header-left">
        <div class="logo">
          <el-icon size="28" color="#409EFF"><DataLine /></el-icon>
          <span class="logo-text">画像平台</span>
        </div>
      </div>
      
      <!-- 顶部导航菜单 -->
      <div class="top-nav">
        <!-- 首页 - 无下拉 -->
        <div 
          class="nav-item"
          :class="{ active: activeTopNav === '/home' }"
          @click="router.push('/home')"
        >
          首页
        </div>
        
        <!-- 标签市场 - 无下拉 -->
        <div 
          class="nav-item"
          :class="{ active: activeTopNav === '/label-market' }"
          @click="router.push('/label-market')"
        >
          标签管理
        </div>
        
        <!-- 用户洞察 - 有下拉 -->
        <el-dropdown class="nav-dropdown" trigger="hover" popper-class="nav-dropdown-popper">
          <div class="nav-item" :class="{ active: activeTopNav === '/insight' }">
            用户洞察
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/insight/user-detail')">
                <el-icon><View /></el-icon>
                <span>用户细查</span>
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/insight/user-profile')">
                <el-icon><User /></el-icon>
                <span>用户画像</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <!-- 群组洞察 - 有下拉 -->
        <el-dropdown class="nav-dropdown" trigger="hover" popper-class="nav-dropdown-popper">
          <div class="nav-item" :class="{ active: activeTopNav === '/group' }">
            群组洞察
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/group/filter')">
                <el-icon><Filter /></el-icon>
                <span>群组筛选</span>
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/group/analysis')">
                <el-icon><DataAnalysis /></el-icon>
                <span>群组分析</span>
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/group/push')">
                <el-icon><Promotion /></el-icon>
                <span>群组推送</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <!-- 自助分析 - 有下拉 -->
        <el-dropdown class="nav-dropdown" trigger="hover" popper-class="nav-dropdown-popper">
          <div class="nav-item" :class="{ active: activeTopNav === '/analysis' }">
            自助分析
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/analysis/event')">
                <el-icon><TrendCharts /></el-icon>
                <span>事件分析</span>
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/analysis/retention')">
                <el-icon><Timer /></el-icon>
                <span>留存分析</span>
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/analysis/conversion')">
                <el-icon><Switch /></el-icon>
                <span>转化分析</span>
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/analysis/distribution')">
                <el-icon><PieChart /></el-icon>
                <span>分布分析</span>
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/analysis/path')">
                <el-icon><Share /></el-icon>
                <span>用户路径</span>
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/analysis/attribution')">
                <el-icon><Connection /></el-icon>
                <span>归因分析</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <!-- 项目中心 - 有下拉 -->
        <el-dropdown class="nav-dropdown" trigger="hover" popper-class="nav-dropdown-popper">
          <div class="nav-item" :class="{ active: activeTopNav === '/project' }">
            项目中心
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/project/datasource')">
                <el-icon><Coin /></el-icon>
                <span>数据源管理</span>
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/project/dataset')">
                <el-icon><FolderOpened /></el-icon>
                <span>数据集管理</span>
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/project/label-category')">
                <el-icon><CollectionTag /></el-icon>
                <span>标签类目管理</span>
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/event')">
                <el-icon><Bell /></el-icon>
                <span>事件管理</span>
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/project/entity')">
                <el-icon><User /></el-icon>
                <span>实体管理</span>
              </el-dropdown-item>
              <el-dropdown-item @click="router.push('/task')">
                <el-icon><Timer /></el-icon>
                <span>任务管理</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      
      <div class="top-header-right">
        <!-- 消息中心 -->
        <el-tooltip content="消息中心" placement="bottom">
          <div class="header-icon-btn" @click="router.push('/messages')">
            <el-icon><Bell /></el-icon>
          </div>
        </el-tooltip>
        
        <!-- 帮助文档 -->
        <el-tooltip content="帮助文档" placement="bottom">
          <div class="header-icon-btn" @click="openHelp">
            <el-icon><QuestionFilled /></el-icon>
          </div>
        </el-tooltip>
        
        <!-- 设置按钮 -->
        <el-dropdown class="settings-dropdown" trigger="click" popper-class="settings-dropdown-popper">
          <span class="header-icon-btn">
            <el-icon><Setting /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu class="settings-menu">
              <div class="settings-group">
                <div class="settings-group-title">项目管理</div>
                <el-dropdown-item @click="router.push('/settings/general')">
                  <el-icon><Grid /></el-icon>
                  <span>通用配置</span>
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/settings/engine')">
                  <el-icon><Cpu /></el-icon>
                  <span>计算引擎</span>
                </el-dropdown-item>
              </div>
              <el-dropdown-item divided />
              <div class="settings-group">
                <div class="settings-group-title">权限管理</div>
                <el-dropdown-item @click="router.push('/settings/users')">
                  <el-icon><User /></el-icon>
                  <span>用户管理</span>
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/settings/roles')">
                  <el-icon><UserFilled /></el-icon>
                  <span>角色管理</span>
                </el-dropdown-item>
              </div>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <!-- 用户下拉 -->
        <el-dropdown>
          <span class="user-info">
            <el-avatar :size="32" :icon="UserFilled" />
            <span class="user-name">管理员</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>个人中心</el-dropdown-item>
              <el-dropdown-item divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <el-container class="main-container">
      <!-- 左侧菜单 -->
      <el-aside width="220px" class="sidebar" v-if="showSidebar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          router
          background-color="#f5f7fa"
          text-color="#303133"
          active-text-color="#409EFF"
        >
          <!-- 用户洞察子菜单 -->
          <template v-if="activeTopNav === '/insight'">
            <div class="menu-group-title">用户洞察</div>
            <el-menu-item index="/insight/user-detail">
              <el-icon><View /></el-icon>
              <span>用户细查</span>
            </el-menu-item>
            <el-menu-item index="/insight/user-profile">
              <el-icon><User /></el-icon>
              <span>用户画像</span>
            </el-menu-item>
          </template>
          
          <!-- 群组洞察子菜单 -->
          <template v-if="activeTopNav === '/group'">
            <div class="menu-group-title">群组洞察</div>
            <el-menu-item index="/group/filter">
              <el-icon><Filter /></el-icon>
              <span>群组筛选</span>
            </el-menu-item>
            <el-menu-item index="/group/analysis">
              <el-icon><DataAnalysis /></el-icon>
              <span>群组分析</span>
            </el-menu-item>
            <el-menu-item index="/group/push">
              <el-icon><Promotion /></el-icon>
              <span>群组推送</span>
            </el-menu-item>
          </template>
          
          <!-- 自助分析子菜单 -->
          <template v-if="activeTopNav === '/analysis'">
            <div class="menu-group-title">自助分析</div>
            <el-menu-item index="/analysis/event">
              <el-icon><TrendCharts /></el-icon>
              <span>事件分析</span>
            </el-menu-item>
            <el-menu-item index="/analysis/retention">
              <el-icon><Timer /></el-icon>
              <span>留存分析</span>
            </el-menu-item>
            <el-menu-item index="/analysis/conversion">
              <el-icon><Switch /></el-icon>
              <span>转化分析</span>
            </el-menu-item>
            <el-menu-item index="/analysis/distribution">
              <el-icon><PieChart /></el-icon>
              <span>分布分析</span>
            </el-menu-item>
            <el-menu-item index="/analysis/path">
              <el-icon><Share /></el-icon>
              <span>用户路径</span>
            </el-menu-item>
            <el-menu-item index="/analysis/attribution">
              <el-icon><Connection /></el-icon>
              <span>归因分析</span>
            </el-menu-item>
          </template>
          
          <!-- 项目中心子菜单 -->
          <template v-if="activeTopNav === '/project'">
            <div class="menu-group-title">项目中心</div>
            <el-menu-item index="/project/datasource">
              <el-icon><Coin /></el-icon>
              <span>数据源管理</span>
            </el-menu-item>
            <el-menu-item index="/project/dataset">
              <el-icon><FolderOpened /></el-icon>
              <span>数据集管理</span>
            </el-menu-item>
            <el-menu-item index="/project/label-category">
              <el-icon><CollectionTag /></el-icon>
              <span>标签类目管理</span>
            </el-menu-item>
            <el-menu-item index="/event">
              <el-icon><Bell /></el-icon>
              <span>事件管理</span>
            </el-menu-item>
            <el-menu-item index="/project/entity">
              <el-icon><User /></el-icon>
              <span>实体管理</span>
            </el-menu-item>
            <el-menu-item index="/task">
              <el-icon><Timer /></el-icon>
              <span>任务管理</span>
            </el-menu-item>
          </template>

          <!-- 设置子菜单 -->
          <template v-if="activeTopNav === '/settings'">
            <div class="menu-group-title">项目管理</div>
            <el-menu-item index="/settings/general">
              <el-icon><Grid /></el-icon>
              <span>通用配置</span>
            </el-menu-item>
            <el-menu-item index="/settings/engine">
              <el-icon><Cpu /></el-icon>
              <span>计算引擎</span>
            </el-menu-item>
            <div class="menu-group-title" style="margin-top: 16px;">权限管理</div>
            <el-menu-item index="/settings/users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/settings/roles">
              <el-icon><UserFilled /></el-icon>
              <span>角色管理</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  DataLine, ArrowDown, Bell, QuestionFilled, Setting, Grid, User, UserFilled,
  View, Filter, DataAnalysis, Promotion, TrendCharts, Timer, Switch, PieChart, Share, Connection,
  Coin, FolderOpened, CollectionTag, Cpu
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const activeMenu = computed(() => route.path)

// 当前激活的顶部导航
const activeTopNav = computed(() => {
  const path = route.path
  if (path.startsWith('/home')) return '/home'
  if (path.startsWith('/label-market')) return '/label-market'
  if (path.startsWith('/insight')) return '/insight'
  if (path.startsWith('/group')) return '/group'
  if (path.startsWith('/analysis')) return '/analysis'
  if (path.startsWith('/project')) return '/project'
  if (path.startsWith('/settings')) return '/settings'
  return '/home'
})

// 是否显示侧边栏
const showSidebar = computed(() => {
  // 首页和标签市场不显示侧边栏，其他页面显示
  return !route.path.startsWith('/home') && !route.path.startsWith('/label-market')
})

// 当前是否是设置页面
const isSettingsPage = computed(() => {
  return route.path.startsWith('/settings')
})

// 打开帮助文档
const openHelp = () => {
  window.open('https://docs.example.com', '_blank')
}
</script>

<style scoped lang="scss">
.main-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

// 顶部导航栏
.top-header {
  height: 64px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  
  .top-header-left {
    display: flex;
    align-items: center;
    gap: 24px;
    
    .logo {
      display: flex;
      align-items: center;
      
      .logo-text {
        color: #001529;
        font-size: 18px;
        font-weight: 600;
        margin-left: 12px;
      }
    }
    
    .workspace-dropdown {
      .workspace-name {
        cursor: pointer;
        color: #606266;
        font-size: 14px;
        display: flex;
        align-items: center;
        padding: 6px 12px;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        
        &:hover {
          color: #409EFF;
          border-color: #409EFF;
        }
      }
    }
  }
  
  .top-nav {
    display: flex;
    align-items: center;
    gap: 4px;
    flex: 1;
    justify-content: center;
    
    .nav-item {
      cursor: pointer;
      padding: 8px 16px;
      color: #606266;
      font-size: 14px;
      border-radius: 4px;
      display: flex;
      align-items: center;
      white-space: nowrap;
      
      &:hover {
        color: #409EFF;
        background-color: #f5f7fa;
      }
      
      &.active {
        color: #409EFF;
        font-weight: 500;
      }
    }
    
    .nav-dropdown {
      :deep(.el-tooltip__trigger) {
        outline: none;
      }
    }
  }
  
  // 下拉菜单样式 - 顶部导航二级菜单
  :deep(.el-dropdown__popper.nav-dropdown-popper) {
    .el-dropdown-menu {
      padding: 12px 0;
      min-width: 180px;
      border-radius: 8px;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      
      .el-dropdown-menu__item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 20px;
        font-size: 14px;
        line-height: 1.5;
        margin: 0 8px;
        border-radius: 6px;
        
        .el-icon {
          font-size: 18px;
          color: #909399;
        }
        
        &:hover {
          background-color: #f0f7ff;
          color: #409EFF;
          
          .el-icon {
            color: #409EFF;
          }
        }
        
        &:active {
          background-color: #e6f2ff;
        }
      }
    }
  }
  
  .top-header-right {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .header-icon-btn {
      cursor: pointer;
      padding: 8px;
      color: #606266;
      font-size: 18px;
      display: flex;
      align-items: center;
      border-radius: 4px;
      
      &:hover {
        color: #409EFF;
        background-color: #f5f7fa;
      }
    }
    
    .user-info {
      cursor: pointer;
      color: #606266;
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 4px 8px;
      border-radius: 4px;
      
      &:hover {
        background-color: #f5f7fa;
      }
      
      .user-name {
        font-size: 14px;
      }
    }
  }
}

// 设置下拉菜单样式
:deep(.el-dropdown__popper.settings-dropdown-popper) {
  .el-dropdown-menu.settings-menu {
    padding: 20px 0;
    min-width: 280px;
    border-radius: 12px;
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
    
    .settings-group {
      padding: 0 16px;
      
      .settings-group-title {
        font-size: 14px;
        font-weight: 600;
        color: #909399;
        padding: 10px 16px;
        margin-bottom: 6px;
      }
    }
    
    .el-dropdown-menu__item {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 14px 20px;
      font-size: 15px;
      line-height: 1.5;
      margin: 4px 12px;
      border-radius: 8px;
      
      .el-icon {
        font-size: 20px;
        color: #606266;
      }
      
      &:hover {
        background-color: #f0f7ff;
        color: #409EFF;
        
        .el-icon {
          color: #409EFF;
        }
      }
      
      &:active {
        background-color: #e6f2ff;
      }
      
      &.is-disabled {
        padding: 0;
        margin: 12px 20px;
        border-top: 1px solid #ebeef5;
        cursor: default;
        pointer-events: none;
        
        &:hover {
          background-color: transparent;
        }
      }
    }
  }
}

.main-container {
  flex: 1;
  overflow: hidden;
}

.sidebar {
  background-color: #f5f7fa;
  border-right: 1px solid #e4e7ed;
  
  .sidebar-menu {
    border-right: none;
    background-color: #f5f7fa !important;
    
    .menu-group-title {
      padding: 16px 20px 8px;
      font-size: 12px;
      color: #909399;
      font-weight: 500;
    }
    
    :deep(.el-menu-item) {
      height: 40px;
      line-height: 40px;
      margin: 4px 8px;
      border-radius: 4px;
      
      &:hover {
        background-color: #e6f2ff !important;
      }
      
      &.is-active {
        background-color: #409eff !important;
        color: #fff !important;
      }
    }
  }
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
