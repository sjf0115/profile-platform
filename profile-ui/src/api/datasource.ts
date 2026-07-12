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

// 数据源相关接口
export const dataSourceApi = {
  // 获取数据源列表
  getList: (params?: DataSourceQueryParams) => {
    return request.post<ApiResponse<DataSource[]>>('/datasource/list', params || {})
  },

  // 获取数据源详情
  getDetail: (datasourceId: string) => {
    return request.get<ApiResponse<DataSource>>(`/datasource/${datasourceId}/detail`)
  },

  // 创建数据源
  create: (data: DataSource) => {
    return request.post<ApiResponse<DataSource>>('/datasource', data)
  },

  // 更新数据源
  update: (datasourceId: string, data: DataSource) => {
    return request.put<ApiResponse<number>>(`/datasource/${datasourceId}`, data)
  },

  // 更新数据源状态（启用/停用）
  updateStatus: (datasourceId: string, status: number) => {
    return request.put<ApiResponse<number>>(`/datasource/${datasourceId}/status?status=${status}`)
  },

  // 删除数据源
  delete: (datasourceId: string) => {
    return request.delete<ApiResponse<number>>(`/datasource/${datasourceId}`)
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

  // 获取投递配置表单定义（根据数据源类型动态返回）
  getExportConfig: (datasourceId: string) => {
    return request.get<ApiResponse<string>>(`/datasource/export-config/${datasourceId}`)
  },

  // 简化版：获取数据表列表（后端自动提取 database）
  getTablesByDatasource: (datasourceId: string) => {
    return request.get<ApiResponse<TableInfo[]>>(`/datasource/tables/${datasourceId}`)
  },

  // 简化版：获取数据列信息（后端自动提取 database）
  getColumnsByDatasource: (datasourceId: string, table: string) => {
    return request.get<ApiResponse<TableColumnInfo>>(`/datasource/columns/${datasourceId}/${table}`)
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
