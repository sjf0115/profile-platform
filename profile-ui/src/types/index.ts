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

// 标签类目
export interface LabelCategory {
  id?: number
  status?: number
  is_default?: number
  category_id: string
  category_name: string
  category_level?: number
  parent_category_id?: string
  category_seq?: number
  source_type?: number
  creator?: string
  modifier?: string
  gmt_create?: string
  gmt_modified?: string
  children?: LabelCategory[]
}

// 标签
export interface Label {
  id?: number
  is_valid?: number
  label_id: string
  label_name: string
  label_status?: number
  label_type?: string | number
  label_desc?: string
  label_category_id?: string
  label_data_type?: number
  label_dist_type?: number
  label_organize_type?: number
  label_produce_type?: number
  label_time_type?: number
  source_type?: number
  config?: string
  is_office?: number
  owner?: string
  creator?: string
  modifier?: string
  gmt_create?: string
  gmt_modified?: string
  // 数据源导入相关字段
  entity_identifier_id?: string
  dataset_id?: string
  dataset_field_name?: string
  // 自定义标签相关字段
  update_type?: number  // 1-手动更新, 2-周期更新
  // 扩展字段（用于展示）
  cover_count?: number
  cover_rate?: string
  label_value?: string
  exec_status?: string
  heat_score?: number
  view_count?: number
}

// 标签查询参数
export interface LabelQueryParams extends PageParams {
  label_name?: string
  label_category_id?: string
  label_status?: number
  label_type?: string
}

// 数据集字段
export interface DatasetField {
  field_name: string           // 字段名
  field_desc?: string         // 字段描述
  field_status?: number   // 字段状态
  field_type?: string // 字段类型
  import_status?: number // 导入状态: 1-导入, 2-不导入
  related_id?: string // 数据集关联对象ID
}

// 数据集
export interface Dataset {
  id?: number
  status?: number
  dataset_id: string
  dataset_name: string
  dataset_type?: number
  dataset_desc?: string
  source_type?: number
  datasource_id?: string
  datasource_name?: string  // 扩展字段，用于展示
  table_name?: string
  partition_field?: string
  partition_format?: string
  entity_id?: string
  entity_field?: string
  fields?: DatasetField[]
  instance_id?: string
  instance_status?: number
  instance_start_time?: string
  instance_end_time?: string
  instance_msg?: number
  owner?: string
  creator?: string
  modifier?: string
  gmt_create?: string
  gmt_modified?: string
}

// 数据集查询参数
export interface DatasetQueryParams extends PageParams {
  dataset_name?: string
  dataset_status?: number
  dataset_type?: number
}

// 群组
export interface Group {
  id?: number
  group_id: string
  group_name: string
  group_desc?: string
  group_status?: number  // 1-启用, 2-停用
  group_type?: number    // 1-标签筛选, 2-群组交并, 3-行为圈选, 4-行为序列圈选, 5-组合人群, 6-文件上传
  group_rule?: string    // 群组规则JSON
  group_count?: number   // 群组规模
  entity_id?: string     // 群组主体ID
  source_type?: number   // 1-系统内置, 2-自定义
  instance_id?: number
  instance_status?: number  // 1-未运行, 2-运行中, 3-运行成功, 4-运行失败
  instance_start_time?: string
  instance_end_time?: string
  instance_msg?: string
  owner?: string
  creator?: string
  modifier?: string
  gmt_create?: string
  gmt_modified?: string
}

// 群组配置响应
export interface GroupConfigResponse {
  group_type: { id: number; name: string }[]
  group_status: { id: number; name: string }[]
  instance_status: { id: number; name: string }[]
}

// 群组查询参数
export interface GroupQueryParams extends PageParams {
  group_name?: string
  group_status?: number
  group_type?: number
}
