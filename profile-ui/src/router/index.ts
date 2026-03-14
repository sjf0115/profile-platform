import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    redirect: '/datasource',
    children: [
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
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
