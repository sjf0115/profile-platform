import request from '@/utils/request'
import type { ApiResponse } from '@/types'

// 权限点节点
export interface PermissionNode {
  permission_id: string
  permission_name: string
  permission_code: string
  permission_type: number  // 1=MENU, 2=BUTTON, 3=API
  parent_id?: string
  sort_order?: number
  children?: PermissionNode[]
}

// 权限相关接口
export const permissionApi = {
  // 获取当前用户权限码集合
  getUserPermissions: () => {
    return request.get<ApiResponse<string[]>>('/user/permissions')
  },

  // 获取权限菜单树
  getPermissionTree: () => {
    return request.get<ApiResponse<PermissionNode[]>>('/permission/tree')
  },

  // 获取角色已勾选的权限点ID列表
  getRolePermissions: (roleId: string) => {
    return request.get<ApiResponse<string[]>>(`/role/${roleId}/permissions`)
  },

  // 保存角色权限配置
  saveRolePermissions: (roleId: string, permissionIds: string[]) => {
    return request.put<ApiResponse<number>>(`/role/${roleId}/permissions`, { permissionIds })
  },
}
