import request from '@/utils/request'
import type { ApiResponse } from '@/types'

// 角色类型定义
export interface Role {
  id?: number
  role_id?: string
  role_type: number
  role_name: string
  role_desc?: string
  source_type?: number
  creator?: string
  modifier?: string
  gmt_create?: string
  gmt_modified?: string
}

// 角色查询参数
export interface RoleQueryParams {
  role_id?: string
  role_type?: number
  role_name?: string
  source_type?: number
}

// 角色相关接口
export const roleApi = {
  // 获取角色列表
  getList: (params?: RoleQueryParams) => {
    return request.post<ApiResponse<Role[]>>('/role/list', params || {})
  },

  // 获取角色详情
  getDetail: (roleId: string) => {
    return request.get<ApiResponse<Role>>(`/role/${roleId}/detail`)
  },

  // 创建角色
  create: (data: Role) => {
    return request.post<ApiResponse<number>>('/role', data)
  },

  // 更新角色
  update: (roleId: string, data: Role) => {
    return request.put<ApiResponse<number>>(`/role/${roleId}`, data)
  },

  // 删除角色
  delete: (roleId: string) => {
    return request.delete<ApiResponse<number>>(`/role/${roleId}`)
  },
}
