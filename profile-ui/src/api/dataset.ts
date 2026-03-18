import request from '@/utils/request'
import type { Dataset, DatasetQueryParams, ApiResponse } from '@/types'

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

  // 保存数据集
  save: (data: Dataset) => {
    return request.post<ApiResponse<number>>('/dataset/save', data)
  },

  // 更新数据集
  update: (data: Dataset) => {
    return request.post<ApiResponse<number>>('/dataset/save', data)
  },

  // 删除数据集
  delete: (dataset_id: string) => {
    return request.delete<ApiResponse<number>>('/dataset/delete', {
      params: { dataset_id }
    })
  },

  // 获取数据表列表
  getTables: (datasourceId: string) => {
    return request.get<ApiResponse<any[]>>('/datasource/tables', {
      params: { datasource_id: datasourceId }
    })
  },

  // 获取数据表字段
  getFields: (datasourceId: string, tableName: string, datasetId?: string) => {
    return request.get<ApiResponse<any[]>>('/dataset/fields', {
      params: { 
        datasourceId: datasourceId, 
        tableName: tableName,
        datasetId: datasetId || ''
      }
    })
  }
}
