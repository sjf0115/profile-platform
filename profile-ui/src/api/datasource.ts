import request from '@/utils/request'
import type { 
  ApiResponse, 
  DataSource, 
  DataSourceSchema, 
  DataSourceQueryParams,
  DataSourceSchemaQueryParams,
  SchemaCategory 
} from '@/types'
import { mockDataSources, mockSchemas, mockResponse } from './mock'

// 是否使用 Mock 数据
const USE_MOCK = false

// 数据源相关接口
export const dataSourceApi = {
  // 获取数据源列表
  getList: (params?: DataSourceQueryParams) => {
    // 过滤掉空值，只传递有值的参数
    const filteredParams: any = {}
    if (params) {
      if (params.schema_type !== undefined && params.schema_type !== null) {
        filteredParams.schema_type = params.schema_type
      }
      if (params.datasource_name) {
        filteredParams.datasource_name = params.datasource_name
      }
      if (params.page_num !== undefined && params.page_num !== null) {
        filteredParams.page_num = params.page_num
      }
      if (params.page_size !== undefined && params.page_size !== null) {
        filteredParams.page_size = params.page_size
      }
    }
    return request.post<ApiResponse<DataSource[]>>('/datasource/list', filteredParams)
  },

  // 获取数据源详情
  getDetail: (datasource_id: string) => {
    return request.get<ApiResponse<DataSource>>('/datasource/detail', {
      params: { datasource_id }
    })
  },

  // 保存数据源（新增/修改）
  save: (data: DataSource) => {
    return request.post<ApiResponse<number>>('/datasource/save', data)
  },

  // 删除数据源
  delete: (datasource_id: string) => {
    return request.delete<ApiResponse<number>>('/datasource/delete', {
      params: { datasource_id }
    })
  },
}

// 数据源 Schema 相关接口
export const dataSourceSchemaApi = {
  // 获取 Schema 分类列表
  getCategories: () => {
    return request.get<ApiResponse<SchemaCategory[]>>('/datasource/schema/category')
  },

  // 获取 Schema 列表
  getList: (params?: DataSourceSchemaQueryParams) => {
    return request.post<ApiResponse<DataSourceSchema[]>>('/datasource/schema/list', params || {})
  },

  // 获取 Schema 详情
  getDetail: (schema_id: string) => {
    return request.get<ApiResponse<DataSourceSchema>>('/datasource/schema/detail', {
      params: { schema_id }
    })
  },

  // 保存 Schema
  save: (data: DataSourceSchema) => {
    return request.post<ApiResponse<number>>('/datasource/schema/save', data)
  },

  // 删除 Schema
  delete: (schemaId: string) => {
    return request.delete<ApiResponse<number>>('/datasource/schema/delete', {
      params: { schemaId }
    })
  },
}
