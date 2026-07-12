import request from '@/utils/request'
import type { ApiResponse, Export, ExportQueryParams } from '@/types'

// 投递相关接口
export const exportApi = {
  // 获取投递列表
  getList: (params?: ExportQueryParams) => {
    return request.post<ApiResponse<Export[]>>('/export/list', params || {})
  },

  // 获取投递详情
  getDetail: (exportId: string) => {
    return request.get<ApiResponse<Export>>(`/export/${exportId}/detail`)
  },

  // 创建投递
  create: (data: Partial<Export>) => {
    return request.post<ApiResponse<Export>>('/export', data)
  },

  // 更新投递
  update: (exportId: string, data: Partial<Export>) => {
    return request.put<ApiResponse<number>>(`/export/${exportId}`, data)
  },

  // 更新投递状态
  updateStatus: (exportId: string, status: number) => {
    return request.put<ApiResponse<number>>(`/export/${exportId}/status`, null, {
      params: { status }
    })
  },

  // 删除投递
  delete: (exportId: string) => {
    return request.delete<ApiResponse<number>>(`/export/${exportId}`)
  },

  // 立即执行投递
  execute: (exportId: string) => {
    return request.post<ApiResponse<string>>(`/export/${exportId}/execute`)
  },
}
