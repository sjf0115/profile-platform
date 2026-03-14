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
        path: 'project',
        name: 'Project',
        redirect: '/project/datasource',
        meta: { title: '项目中心' },
      },
      {
        path: 'project/datasource',
        name: 'DataSourceProject',
        component: () => import('@/views/datasource/index.vue'),
        meta: { title: '数据源管理' },
      },
      {
        path: 'project/label-category',
        name: 'LabelCategory',
        component: () => import('@/views/label/category.vue'),
        meta: { title: '标签类目管理' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
