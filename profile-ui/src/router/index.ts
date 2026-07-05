import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页' },
      },
      {
        path: 'project',
        name: 'Project',
        redirect: '/project/datasource',
        meta: { title: '项目中心' },
      },
      {
        path: 'project/label-category',
        name: 'LabelCategory',
        component: () => import('@/views/label/category.vue'),
        meta: { title: '标签类目管理' },
      },
      {
        path: 'datasource',
        name: 'DataSource',
        component: () => import('@/views/datasource/index.vue'),
        meta: { title: '数据源管理' },
      },
      {
        path: 'datasource/select-type',
        name: 'DataSourceSelectType',
        component: () => import('@/views/datasource/select-type.vue'),
        meta: { title: '选择数据源类型' },
      },
      {
        path: 'datasource/create',
        name: 'DataSourceCreate',
        component: () => import('@/views/datasource/create.vue'),
        meta: { title: '创建数据源' },
      },
      {
        path: 'datasource/edit/:id',
        name: 'DataSourceEdit',
        component: () => import('@/views/datasource/create.vue'),
        meta: { title: '编辑数据源' },
      },
      {
        path: 'datasource/detail/:id',
        name: 'DataSourceDetail',
        component: () => import('@/views/datasource/detail.vue'),
        meta: { title: '数据源详情' },
      },
      {
        path: 'label-market',
        name: 'LabelMarket',
        component: () => import('@/views/label/index.vue'),
        meta: { title: '标签管理' },
      },
      {
        path: 'label/create/datasource',
        name: 'CreateLabelDatasource',
        component: () => import('@/views/label/create-datasource.vue'),
        meta: { title: '创建标签 - 数据源导入' },
      },
      {
        path: 'label/edit/datasource/:id',
        name: 'EditLabelDatasource',
        component: () => import('@/views/label/create-datasource.vue'),
        meta: { title: '编辑标签 - 数据源导入' },
      },
      {
        path: 'label/create/custom',
        name: 'CreateLabelCustom',
        component: () => import('@/views/label/create-custom.vue'),
        meta: { title: '创建标签 - 自定义标签' },
      },
      {
        path: 'label/edit/custom/:id',
        name: 'EditLabelCustom',
        component: () => import('@/views/label/create-custom.vue'),
        meta: { title: '编辑标签 - 自定义标签' },
      },
      {
        path: 'label/detail/:id',
        name: 'LabelDetail',
        component: () => import('@/views/label/detail.vue'),
        meta: { title: '标签详情' },
      },      
      {
        path: 'project/datasource',
        name: 'DataSourceProject',
        component: () => import('@/views/datasource/index.vue'),
        meta: { title: '数据源管理' },
      },
      {
        path: 'project/dataset',
        name: 'Dataset',
        component: () => import('@/views/dataset/index.vue'),
        meta: { title: '数据集管理' },
      },
      {
        path: 'dataset/create/label',
        name: 'CreateLabelDataset',
        component: () => import('@/views/dataset/create-label.vue'),
        meta: { title: '创建标签数据集' },
      },
      {
        path: 'dataset/detail/:id',
        name: 'DatasetDetail',
        component: () => import('@/views/dataset/detail.vue'),
        meta: { title: '数据集详情' },
      },
      {
        path: 'dataset/edit/:id',
        name: 'DatasetEdit',
        component: () => import('@/views/dataset/create-label.vue'),
        meta: { title: '编辑标签数据集' },
      },
      {
        path: 'project/entity',
        name: 'EntityManagement',
        component: () => import('@/views/entity/index.vue'),
        meta: { title: '实体管理' },
      },
      // 设置 - 角色管理
      {
        path: 'settings/roles',
        name: 'RoleManagement',
        component: () => import('@/views/role/index.vue'),
        meta: { title: '角色管理' },
      },
      // 设置 - 用户管理
      {
        path: 'settings/users',
        name: 'UserManagement',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理' },
      },
      // 设置 - 通用配置
      {
        path: 'settings/general',
        name: 'GeneralSettings',
        component: () => import('@/views/role/index.vue'),
        meta: { title: '通用配置' },
      },
      // 设置 - 计算引擎（单例，直接显示编辑页面）
      {
        path: 'settings/engine',
        name: 'EngineManagement',
        component: () => import('@/views/engine/create.vue'),
        meta: { title: '计算引擎' },
      },
      // 群组洞察
      {
        path: 'group/detail/:id',
        name: 'GroupDetail',
        component: () => import('@/views/group/detail.vue'),
        meta: { title: '群组详情' },
      },
      {
        path: 'group/filter',
        name: 'GroupFilter',
        component: () => import('@/views/group/index.vue'),
        meta: { title: '群组筛选' },
      },
      {
        path: 'group/create/rule',
        name: 'CreateGroupRule',
        component: () => import('@/views/group/create-rule.vue'),
        meta: { title: '规则创建群组' },
      },
      {
        path: 'group/edit/rule/:id',
        name: 'EditGroupRule',
        component: () => import('@/views/group/create-rule.vue'),
        meta: { title: '编辑群组' },
      },
      {
        path: 'group/create/upload',
        name: 'CreateGroupUpload',
        component: () => import('@/views/group/create-upload.vue'),
        meta: { title: '上传文件创建群组' },
      },
      {
        path: 'group/edit/upload/:id',
        name: 'EditGroupUpload',
        component: () => import('@/views/group/create-upload.vue'),
        meta: { title: '编辑群组' },
      },
      {
        path: 'group/create/sql',
        name: 'CreateGroupSql',
        component: () => import('@/views/group/create-sql.vue'),
        meta: { title: 'SQL创建群组' },
      },
      {
        path: 'group/edit/sql/:id',
        name: 'EditGroupSql',
        component: () => import('@/views/group/create-sql.vue'),
        meta: { title: '编辑群组' },
      },
      // 群组分析
      {
        path: 'group/analysis',
        name: 'GroupAnalysis',
        component: () => import('@/views/group-analysis/index.vue'),
        meta: { title: '群组分析' },
      },
      {
        path: 'group/analysis/create',
        name: 'CreateGroupAnalysis',
        component: () => import('@/views/group-analysis/create.vue'),
        meta: { title: '创建群组分析' },
      },
      {
        path: 'group/analysis/edit/:id',
        name: 'EditGroupAnalysis',
        component: () => import('@/views/group-analysis/create.vue'),
        meta: { title: '编辑群组分析' },
      },
      {
        path: 'group/analysis/detail/:id',
        name: 'GroupAnalysisDetail',
        component: () => import('@/views/group-analysis/detail.vue'),
        meta: { title: '群组分析详情' },
      },
      {
        path: 'group/analysis/live',
        name: 'GroupAnalysisLive',
        component: () => import('@/views/group-analysis/detail.vue'),
        meta: { title: '群组分析' },
      },
      // 群组投递
      {
        path: 'group/export',
        name: 'GroupExport',
        component: () => import('@/views/export/index.vue'),
        meta: { title: '群组投递' },
      },
      {
        path: 'group/export/create',
        name: 'CreateGroupExport',
        component: () => import('@/views/export/create.vue'),
        meta: { title: '创建投递' },
      },
      {
        path: 'group/export/edit/:id',
        name: 'EditGroupExport',
        component: () => import('@/views/export/create.vue'),
        meta: { title: '编辑投递' },
      },
      {
        path: 'group/export/detail/:id',
        name: 'GroupExportDetail',
        component: () => import('@/views/export/detail.vue'),
        meta: { title: '投递详情' },
      },
      // 任务管理
      {
        path: 'task',
        name: 'TaskManagement',
        component: () => import('@/views/task/index.vue'),
        meta: { title: '任务管理' },
      },
      {
        path: 'task/detail/:id',
        name: 'TaskDetail',
        component: () => import('@/views/task/detail.vue'),
        meta: { title: '任务详情' },
      },
      {
        path: 'task/instance',
        name: 'TaskInstanceManagement',
        component: () => import('@/views/task/instance.vue'),
        meta: { title: '任务实例' },
      },
      {
        path: 'instance/detail/:id',
        name: 'InstanceDetail',
        component: () => import('@/views/task/instance-detail.vue'),
        meta: { title: '实例详情' },
      },
      // 用户画像
      {
        path: 'insight/user-profile',
        name: 'UserProfile',
        component: () => import('@/views/user-profile/index.vue'),
        meta: { title: '用户细查' },
      },
      {
        path: 'insight/user-profile/detail/:id',
        name: 'UserProfileDetail',
        component: () => import('@/views/user-profile/detail.vue'),
        meta: { title: '用户细查详情' },
      },
      // 应用管理
      {
        path: 'application',
        name: 'Application',
        component: () => import('@/views/application/index.vue'),
        meta: { title: '应用管理' },
      },
      {
        path: 'application/create',
        name: 'CreateApplication',
        component: () => import('@/views/application/create.vue'),
        meta: { title: '创建应用' },
      },
      {
        path: 'application/edit/:id',
        name: 'EditApplication',
        component: () => import('@/views/application/create.vue'),
        meta: { title: '编辑应用' },
      },
      {
        path: 'application/detail/:id',
        name: 'ApplicationDetail',
        component: () => import('@/views/application/detail.vue'),
        meta: { title: '应用详情' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
