import request from '@/utils/request'
import type { ApiResponse, Group, GroupConfigResponse } from '@/types'

export const groupApi = {
  // 获取群组列表
  getList: (params?: any) => {
    return request.post<ApiResponse<Group[]>>('/group/list', params || {})
  },

  // 获取群组详情
  getDetail: (group_id: string) => {
    return request.get<ApiResponse<Group>>('/group/detail', {
      params: { group_id }
    })
  },

  // 保存群组（新增/修改）
  save: (data: Partial<Group>) => {
    return request.post<ApiResponse<number>>('/group/save', data)
  },

  // 删除群组
  delete: (group_id: string) => {
    return request.delete<ApiResponse<number>>('/group/delete', {
      params: { group_id }
    })
  },

  // 获取群组配置（筛选条件用）
  getConfig: () => {
    return request.get<ApiResponse<GroupConfigResponse>>('/group/config')
  },

  // 获取标签操作符配置
  getLabelConfig: () => {
    return request.get<ApiResponse<any[]>>('/group/config/label')
  }
}
