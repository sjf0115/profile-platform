import request from '@/utils/request'
import type { ApiResponse, Application, ApplicationQueryParams, ApplicationRequest } from '@/types'

// 应用管理相关接口
export const applicationApi = {
  // 获取应用列表
  getList: (params?: ApplicationQueryParams) => {
    return request.post<ApiResponse<Application[]>>('/application/list', params || {})
  },

  // 获取应用详情
  getDetail: (appKey: string) => {
    return request.get<ApiResponse<Application>>(`/application/${appKey}/detail`)
  },

  // 创建应用
  create: (data: ApplicationRequest) => {
    return request.post<ApiResponse<Application>>('/application', data)
  },

  // 更新应用
  update: (appKey: string, data: ApplicationRequest) => {
    return request.put<ApiResponse<number>>(`/application/${appKey}`, data)
  },

  // 删除应用
  delete: (appKey: string) => {
    return request.delete<ApiResponse<number>>(`/application/${appKey}`)
  },

  // 重置 AppSecret
  resetSecret: (appKey: string) => {
    return request.post<ApiResponse<string>>(`/application/${appKey}/reset-secret`)
  },

  // 更新应用状态（启用/停用）
  updateStatus: (appKey: string, status: number) => {
    return request.put<ApiResponse<number>>(`/application/${appKey}/status`, null, {
      params: { status }
    })
  },
}
