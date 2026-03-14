// 通用响应结构
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 分页参数（后端 Gson 序列化为下划线命名）
export interface PageParams {
  page_num?: number
  page_size?: number
}

// 数据源 Schema 配置项（后端使用 Gson，字段名为下划线格式）
export interface SchemaConfigItem {
  show_name: string
  key: string
  value: string
  required: number
  encrypt: number
  tip: string
}

// 数据源 Schema（后端 Gson 序列化为下划线命名）
export interface DataSourceSchema {
  id?: number
  status?: number
  schema_id: string
  schema_name: string
  schema_type: number
  jdbc_protocol?: string
  source_type: number
  creator?: string
  modifier?: string
  gmt_create?: string
  gmt_modified?: string
  config_template?: SchemaConfigItem[]
}

// 数据源（后端 Gson 序列化为下划线命名）
export interface DataSource {
  id?: number
  status?: number
  datasource_id?: string
  datasource_name: string
  datasource_desc?: string
  schema_id: string
  schema_name?: string
  schema_type?: number
  source_type?: number
  owner?: string
  creator?: string
  modifier?: string
  gmt_create?: string
  gmt_modified?: string
  config?: Record<string, any>
  config_template?: SchemaConfigItem[]
}

// 数据源查询参数（后端 Gson 序列化为下划线命名）
export interface DataSourceQueryParams extends PageParams {
  schema_type?: number
  datasource_name?: string
}

// 数据源 Schema 查询参数（后端 Gson 序列化为下划线命名）
export interface DataSourceSchemaQueryParams extends PageParams {
  schema_type?: number
  schema_name?: string
}

// 数据源分类
export interface SchemaCategory {
  id: number
  name: string
  count: number
}
