import request from '@/utils/request'
import type { ApiResponse, UserProfileVO } from '@/types'

// 用户类型定义
export interface User {
  id?: number
  user_id?: string
  user_name: string
  email?: string
  user_type?: string
  password?: string
  status?: number
  source_type?: number
  creator?: string
  modifier?: string
  gmt_create?: string
  gmt_modified?: string
  roles?: string[]
}

// 用户查询参数
export interface UserQueryParams {
  user_id?: string
  user_name?: string
  user_type?: string
  status?: number
  source_type?: number
}

// 用户概览统计
export interface UserOverview {
  total_count: number
  admin_count: number
  member_count: number
  no_permission_count: number
}

// 用户相关接口
export const userApi = {
  // 获取用户列表
  getList: (params?: UserQueryParams) => {
    return request.post<ApiResponse<User[]>>('/user/list', params || {})
  },

  // 获取用户详情
  getDetail: (userId: string) => {
    return request.get<ApiResponse<User>>(`/user/${userId}/detail`)
  },

  // 创建用户
  create: (data: User) => {
    return request.post<ApiResponse<number>>('/user', data)
  },

  // 更新用户
  update: (userId: string, data: User) => {
    return request.put<ApiResponse<number>>(`/user/${userId}`, data)
  },

  // 删除用户
  delete: (userId: string) => {
    return request.delete<ApiResponse<number>>(`/user/${userId}`)
  },

  // 获取用户概览统计
  getOverview: () => {
    return request.get<ApiResponse<UserOverview>>('/user/overview')
  },

  // ==================== 用户细查接口 ====================

  // 获取用户完整画像（聚合接口）
  getUserProfile: (userId: string) => {
    return request.get<ApiResponse<UserProfileVO>>(`/user/${userId}/profile`)
  },

  // 为用户打标签
  addUserLabel: (userId: string, data: { label_id: string; label_value: string }) => {
    return request.post<ApiResponse<number>>(`/user/${userId}/labels`, data)
  },

  // 删除用户单个标签
  deleteUserLabel: (userId: string, labelId: string) => {
    return request.delete<ApiResponse<number>>(`/user/${userId}/labels/${labelId}`)
  },

  // 更新类目排序（拖拽后调用）
  updateCategorySort: (userId: string, categoryIds: string[]) => {
    return request.put<ApiResponse<number>>(`/user/${userId}/categories/sort`, categoryIds)
  },

  // 删除类目
  deleteCategory: (userId: string, categoryId: string) => {
    return request.delete<ApiResponse<number>>(`/user/${userId}/categories/${categoryId}`)
  },
}
