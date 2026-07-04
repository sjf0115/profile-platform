import request from '@/utils/request'
import type { ApiResponse, Application, ApplicationQueryParams } from '@/types'

// 应用管理相关接口
export const applicationApi = {
  // 获取应用列表
  getList: (params?: ApplicationQueryParams) => {
    return request.post<ApiResponse<Application[]>>('/application/list', params || {})
  },

  // 模糊查询
  getByKeyword: (keyword: string) => {
    return request.get<ApiResponse<Application[]>>('/application/keyword', {
      params: { keyword }
    })
  },

  // 获取应用详情
  getDetail: (id: number) => {
    return request.get<ApiResponse<Application>>('/application/detail', {
      params: { id }
    })
  },

  // 保存应用（新增/修改）
  save: (data: Partial<Application>) => {
    return request.post<ApiResponse<Application>>('/application/save', data)
  },

  // 删除应用
  delete: (id: number) => {
    return request.delete<ApiResponse<number>>('/application/delete', {
      params: { id }
    })
  },

  // 重置 AppSecret
  resetSecret: (id: number) => {
    return request.post<ApiResponse<string>>('/application/reset-secret', null, {
      params: { id }
    })
  },
}
