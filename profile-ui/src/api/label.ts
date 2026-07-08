import request from '@/utils/request'
import type { ApiResponse, Label } from '@/types'

// 标签配置项
export interface LabelConfig {
  id: number
  name: string
}

export interface LabelConfigResponse {
  label_type: LabelConfig[]
  data_type: LabelConfig[]
  dist_type: LabelConfig[]
  organize_type: LabelConfig[]
  produce_type: LabelConfig[]
  time_type: LabelConfig[]
}

export const labelApi = {
  // 获取标签列表
  getList: (params?: any) => {
    return request.post<ApiResponse<Label[]>>('/label/list', params || {})
  },

  // 获取未绑定数据集的标签（数据集创建/编辑场景专用）
  getUnbound: (entity_identifier_id: string, dataset_id?: string) => {
    return request.get<ApiResponse<Label[]>>('/label/unbound', {
      params: { entity_identifier_id, dataset_id }
    })
  },

  // 获取已绑定数据集的线上可用标签（群组规则/分析场景专用）
  getOnline: (entity_identifier_id: string) => {
    return request.get<ApiResponse<Label[]>>('/label/online', {
      params: { entity_identifier_id }
    })
  },

  // 获取标签详情
  getDetail: (label_id: string) => {
    return request.get<ApiResponse<Label>>('/label/detail', {
      params: { label_id }
    })
  },

  // 保存标签（新增/修改）
  save: (data: Partial<Label>) => {
    return request.post<ApiResponse<number>>('/label/save', data)
  },

  // 删除标签
  delete: (label_id: string) => {
    return request.delete<ApiResponse<number>>('/label/delete', {
      params: { label_id }
    })
  },

  // 批量删除标签
  batchDelete: (label_ids: string[]) => {
    return request.post<ApiResponse<number>>('/label/batchDelete', { label_ids })
  },

  // 更新标签
  update: (data: Label) => {
    return request.post<ApiResponse<number>>('/label/update', data)
  },

  // 获取标签配置
  getConfig: () => {
    return request.get<ApiResponse<LabelConfigResponse>>('/label/config')
  }
}
