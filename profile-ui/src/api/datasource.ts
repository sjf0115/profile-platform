import request from '@/utils/request'
import type { 
  ApiResponse, 
  DataSource, 
  DataSourceQueryParams,
  DataSourceTypeItem,
  DataSourceConfigResponse,
  DatabaseInfo,
  TableInfo,
  TableColumnInfo
} from '@/types'
import { mockDataSources, mockResponse } from './mock'

// 是否使用 Mock 数据
const USE_MOCK = false

// 数据源相关接口
export const dataSourceApi = {
  // 获取数据源列表
  getList: (params?: DataSourceQueryParams) => {
    // 过滤掉空值，只传递有值的参数
    const filteredParams: any = {}
    if (params) {
      if (params.datasource_type) {
        filteredParams.datasource_type = params.datasource_type
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

  // 测试连接
  testConnection: (data: { type: string; data_source_param: string }) => {
    return request.post<ApiResponse<boolean>>('/datasource/test', data)
  },

  // 获取数据库列表
  getDatabases: (id: string) => {
    return request.get<ApiResponse<DatabaseInfo[]>>(`/datasource/${id}/databases`)
  },

  // 获取数据表列表
  getTables: (id: string, database: string) => {
    return request.get<ApiResponse<TableInfo[]>>(`/datasource/${id}/${database}/tables`)
  },

  // 获取数据表列信息
  getColumns: (id: string, database: string, table: string) => {
    return request.get<ApiResponse<TableColumnInfo>>(`/datasource/${id}/${database}/${table}/columns`)
  },
}

// 数据源类型相关接口（通过 Connector 插件）
export const dataSourceTypeApi = {
  // 获取所有支持的数据源类型
  getList: () => {
    return request.get<ApiResponse<DataSourceTypeItem[]>>('/datasource/type/list')
  },

  // 获取指定类型的配置表单
  getConfig: (type: string) => {
    return request.get<ApiResponse<DataSourceConfigResponse>>(`/datasource/config/${type}`)
  },
}
