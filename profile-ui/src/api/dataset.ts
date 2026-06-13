import request from '@/utils/request'
import type { Dataset, DatasetQueryParams, Task, ApiResponse } from '@/types'

export const datasetApi = {
  // 获取数据集列表
  list: (params: DatasetQueryParams) => {
    return request.post<ApiResponse<Dataset[]>>('/dataset/list', params)
  },

  // 获取可用的数据源列表（根据数据集类型）
  getDatasources: (datasetType: number) => {
    return request.get<ApiResponse<any[]>>('/dataset/datasources', {
      params: { dataset_type: datasetType }
    })
  },

  // 获取数据集详情
  detail: (datasetId: string) => {
    return request.get<ApiResponse<Dataset>>('/dataset/detail', {
      params: { dataset_id: datasetId }
    })
  },

  // 保存数据集（新增/修改）
  save: (data: Dataset) => {
    return request.post<ApiResponse<number>>('/dataset/save', data)
  },

  // 删除数据集
  delete: (dataset_id: string) => {
    return request.delete<ApiResponse<number>>('/dataset/delete', {
      params: { dataset_id }
    })
  },

  // 立即执行数据同步
  execute: (datasetId: string) => {
    return request.post<ApiResponse<any>>(`/dataset/${datasetId}/execute`)
  },

  // 获取数据集调度配置
  getSchedulerConfig: (datasetId: string) => {
    return request.get<ApiResponse<Task>>(`/dataset/${datasetId}/scheduler`)
  },

  // 配置数据集调度
  configureScheduler: (datasetId: string, data: Partial<Task>) => {
    return request.put<ApiResponse<any>>(`/dataset/${datasetId}/scheduler`, data)
  }
}
