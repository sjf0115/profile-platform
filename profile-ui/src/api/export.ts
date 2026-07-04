import request from '@/utils/request'
import type { ApiResponse, Export, ExportQueryParams } from '@/types'

// 投递相关接口
export const exportApi = {
  // 获取投递列表
  getList: (params?: ExportQueryParams) => {
    return request.post<ApiResponse<Export[]>>('/export/list', params || {})
  },

  // 根据名称查询
  getByName: (exportName: string) => {
    return request.get<ApiResponse<Export[]>>('/export/name', {
      params: { exportName }
    })
  },

  // 模糊查询
  getByKeyword: (keyword: string) => {
    return request.get<ApiResponse<Export[]>>('/export/keyword', {
      params: { keyword }
    })
  },

  // 获取投递详情
  getDetail: (exportId: string) => {
    return request.get<ApiResponse<Export>>('/export/detail', {
      params: { exportId }
    })
  },

  // 保存投递（新增/修改）
  save: (data: Partial<Export>) => {
    return request.post<ApiResponse<number>>('/export/save', data)
  },

  // 删除投递
  delete: (exportId: string) => {
    return request.delete<ApiResponse<number>>('/export/delete', {
      params: { exportId }
    })
  },

  // 立即执行投递
  execute: (exportId: string) => {
    return request.post<ApiResponse<string>>('/export/execute', null, {
      params: { exportId }
    })
  },
}
