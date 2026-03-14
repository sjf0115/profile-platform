import request from '@/utils/request'
import type { Dataset, DatasetQueryParams, ApiResponse } from '@/types'

export const datasetApi = {
  // 获取数据集列表
  list: (params: DatasetQueryParams) => {
    return request.post<ApiResponse<Dataset[]>>('/dataset/list', params)
  },

  // 获取数据集详情
  detail: (datasetId: string) => {
    return request.get<ApiResponse<Dataset>>('/dataset/detail', {
      params: { dataset_id: datasetId }
    })
  },

  // 保存数据集
  save: (data: Dataset) => {
    return request.post<ApiResponse<number>>('/dataset/save', data)
  },

  // 更新数据集
  update: (data: Dataset) => {
    return request.post<ApiResponse<number>>('/dataset/save', data)
  },

  // 删除数据集
  delete: (datasetId: string) => {
    return request.post<ApiResponse<number>>('/dataset/delete', { dataset_id: datasetId })
  },

  // 获取数据表列表
  getTables: (datasourceId: string) => {
    return request.get<ApiResponse<any[]>>('/dataset/tables', {
      params: { datasource_id: datasourceId }
    })
  },

  // 获取数据表字段
  getFields: (datasourceId: string, tableName: string, datasetId?: string) => {
    return request.get<ApiResponse<any[]>>('/dataset/fields', {
      params: { 
        datasource_id: datasourceId, 
        table_name: tableName,
        dataset_id: datasetId || ''
      }
    })
  }
}
