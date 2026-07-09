import request from '@/utils/request'
import type { ApiResponse } from '@/types'

// 登录请求参数
export interface LoginParams {
  user_name: string
  password: string
}

// 登录响应
export interface LoginResponse {
  user: User
  token: string
}

// 用户类型定义
export interface User {
  id?: number
  user_id?: string
  user_name: string
  email?: string
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
  // 登录
  login: (data: LoginParams) => {
    return request.post<ApiResponse<LoginResponse>>('/user/login', data)
  },

  // 退出登录
  logout: () => {
    return request.patch<ApiResponse<null>>('/user/logout')
  },

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
}
