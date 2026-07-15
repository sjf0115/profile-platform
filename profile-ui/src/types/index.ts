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

// 数据源（后端 Gson 序列化为下划线命名）
export interface DataSource {
  id?: number
  status?: number
  datasource_id?: string
  datasource_name: string
  datasource_desc?: string
  datasource_type?: string  // 数据源类型（如：mysql, clickhouse）
  source_type?: number
  owner?: string
  owner_name?: string
  creator?: string
  creator_name?: string
  modifier?: string
  modifier_name?: string
  gmt_create?: string
  gmt_modified?: string
  config?: string  // 配置信息，后端存储为 JSON 字符串
}

// 数据源查询参数（后端 Gson 序列化为下划线命名）
export interface DataSourceQueryParams extends PageParams {
  datasource_type?: string  // 数据源类型过滤
  datasource_name?: string
}

// 计算引擎（后端 Gson 序列化为下划线命名）
export interface Engine {
  id?: number
  status?: number
  engine_id?: string
  engine_name: string
  engine_type: string  // 引擎类型：clickhouse, doris, spark, flink
  engine_desc?: string
  is_default?: number  // 是否默认引擎：0-否, 1-是
  source_type?: number  // 创建方式：1-系统内置, 2-自定义
  config?: string  // 引擎配置，JSON字符串
  creator?: string
  modifier?: string
  gmt_create?: string
  gmt_modified?: string
}

// 计算引擎查询参数
export interface EngineQueryParams extends PageParams {
  engine_type?: string
  engine_name?: string
}

// 引擎类型项
export interface EngineTypeItem {
  key: string
  value: string
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
  owner_name?: string
  creator?: string
  creator_name?: string
  modifier?: string
  modifier_name?: string
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
  owner?: string
}

// 数据集字段
export interface DatasetField {
  field_name: string           // 字段名
  field_desc?: string         // 字段描述
  field_status?: number   // 字段状态
  field_type?: string // 字段类型
  import_status?: number // 导入状态: 1-导入, 2-不导入
  related_id?: string // 数据集关联对象ID
  entity_field?: boolean // 是否是实体字段（VO 扩展字段）
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
  engine_table_name?: string // 引擎表名（VO 扩展字段）
  partition_field?: string
  partition_format?: string
  entity_id?: string
  entity_field?: string
  fields?: DatasetField[]
  latest_instance?: TaskInstance
  instance_id?: string
  instance_status?: number
  instance_start_time?: string
  instance_end_time?: string
  instance_msg?: number
  owner?: string
  owner_name?: string
  creator?: string
  creator_name?: string
  modifier?: string
  modifier_name?: string
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
  group_type?: number    // 1-规则筛选, 2-文件上传, 3-SQL创建
  group_rule?: string | any  // 群组规则JSON
  group_count?: number   // 群组规模
  entity_id?: string     // 群组主体ID
  entity_identifier_id?: string  // 群组主体标识ID
  entity_identifier_name?: string
  entity_name?: string
  source_type?: number   // 1-系统内置, 2-自定义
  
  // 调度任务
  task_id?: string           // 调度任务ID
  trigger_type?: number      // 1-手动触发, 2-周期调度, 3-API触发
  trigger_cron?: string      // Cron表达式
  trigger_url?: string       // API触发URL
  trigger_start_time?: string
  trigger_end_time?: string
  
  // 最新任务实例（嵌套对象）
  task_instance?: TaskInstance
  
  // 人员信息
  owner?: string
  creator?: string
  modifier?: string
  
  // 时间
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

// ==================== Connector Plugin Params 类型定义 ====================

// 表单类型
export type FormType = 'input' | 'radio' | 'select' | 'checkbox' | 'cascader' | 'textarea' | 'group'

// 验证规则
export interface ValidateRule {
  required?: boolean
  message?: string
  trigger?: string
  min?: number
  max?: number
  pattern?: string
}

// 选项
export interface ParamOption {
  label: string
  value: string | number | boolean
  disabled?: boolean
}

// 输入框属性
export interface InputProps {
  placeholder?: string
  size?: 'large' | 'default' | 'small'
  type?: 'text' | 'password' | 'number'
  rows?: number
  clearable?: boolean
  disabled?: boolean
}

// 选择框属性
export interface SelectProps {
  placeholder?: string
  size?: 'large' | 'default' | 'small'
  clearable?: boolean
  multiple?: boolean
  filterable?: boolean
}

// 单选框属性
export interface RadioProps {
  size?: 'large' | 'default' | 'small'
}

// 参数属性（根据类型不同而变化）
export type ParamProps = InputProps | SelectProps | RadioProps

// 插件参数定义（对应后端 PluginParams）
export interface PluginParam {
  field: string
  type: FormType
  title: string
  value?: any
  props?: ParamProps
  validate?: ValidateRule[]
  options?: ParamOption[]
  emit?: string[]
}

// 数据源类型项
export interface DataSourceTypeItem {
  key: string
  value: string
}

// 数据源配置响应
export interface DataSourceConfigResponse {
  params: PluginParam[]
}

// 数据库信息
export interface DatabaseInfo {
  name: string
  type?: string
}

// 数据表信息
export interface TableInfo {
  database: string
  name: string
  type?: string
  comment?: string
  owner?: string
  create_time?: string
}

// 列信息
export interface ColumnInfo {
  name: string
  type: string
  comment?: string
  primary_key?: boolean
}

// 数据表列信息
export interface TableColumnInfo {
  database?: string
  table: string
  primary_keys?: string[]
  columns: ColumnInfo[]
}

// 调度任务
export interface Task {
  id?: number
  status?: number
  task_id: string
  task_name: string
  task_desc?: string
  task_type?: number
  task_related_id?: string
  trigger_target_id?: string
  trigger_type?: number
  trigger_cron?: string
  trigger_url?: string
  trigger_start_time?: string
  trigger_end_time?: string
  schedule_id?: string
  upstream_task_ids?: string
  source_type?: number
  owner?: string
  creator?: string
  creator_name?: string
  modifier?: string
  modifier_name?: string
  gmt_create?: string
  gmt_modified?: string
}

// 调度任务查询参数
export interface TaskQueryParams extends PageParams {
  task_name?: string
  task_type?: number
  trigger_type?: number
  status?: number
}

// 调度任务请求参数
export interface TaskRequest {
  task_name?: string
  task_desc?: string
  task_type?: number
  task_related_id?: string
  trigger_target_id?: string
  trigger_type?: number
  trigger_cron?: string
  trigger_url?: string
  trigger_start_time?: string
  trigger_end_time?: string
  upstream_task_ids?: string
}

// 任务实例
export interface TaskInstance {
  id?: number
  status?: number
  instance_id: string
  instance_name: string
  task_id: string
  instance_related_id?: string
  trigger_mode?: number
  task?: Task
  start_time?: number
  end_time?: number
  duration?: number
  message?: string
  creator?: string
  creator_name?: string
  modifier?: string
  modifier_name?: string
  gmt_create?: string
  gmt_modified?: string
}

// 任务实例查询参数
export interface TaskInstanceQueryParams extends PageParams {
  task_id?: string
  instance_name?: string
  instance_related_id?: string
  status?: number
  start_time_begin?: number
  start_time_end?: number
}

// ==================== 用户细查相关类型 ====================

// 用户画像（聚合接口响应）
export interface UserProfileVO {
  user: UserVO
  label_categories: LabelCategoryVO[]
  groups: GroupVO[]
}

// 用户基础信息
export interface UserVO {
  user_id: string
  user_name: string
  email: string
  status: number
  source_type: number
  roles: RoleVO[]
  gmt_create: string
  gmt_modified: string
}

// 角色信息
export interface RoleVO {
  role_id: string
  role_name: string
  role_type: string
  role_desc: string
}

// 标签类目（用户细查）
export interface LabelCategoryVO {
  category_id: string
  category_name: string
  sort_order: number
  labels: UserLabelVO[]
}

// 用户标签（用户细查）
export interface UserLabelVO {
  label_id: string
  label_name: string
  label_value: string
}

// 群组信息（用户细查）
export interface GroupVO {
  group_id: string
  group_name: string
  group_desc: string
  group_status: number
  group_type: number
  group_count: number
}

// ==================== 用户画像主页相关类型 ====================

// 用户画像表格行（随机抽取的用户列表）
export interface UserProfileRowVO {
  user_id: string
  user_name: string
  app: string
  last_access_time: string
  device_model: string
  os: string
  software_version: string
  channel: string
  device_brand: string
  region: string
}

// 应用管理
export interface Application {
  id?: number
  status?: number
  app_name: string
  app_desc?: string
  app_key?: string
  app_secret?: string
  target_config?: string  // JSON 字符串
  webhook_url?: string
  rate_limit?: number
  ip_whitelist?: string
  source_type?: number
  owner?: string
  owner_name?: string
  creator?: string
  modifier?: string
  gmt_create?: string
  gmt_modified?: string
}

// 应用查询参数
export interface ApplicationQueryParams {
  status?: number
  app_name?: string
  source_type?: number
}

// 应用创建/编辑请求
export interface ApplicationRequest {
  app_name: string
  app_desc?: string
  owner?: string
  target_config?: string
  webhook_url?: string
  rate_limit?: number
  ip_whitelist?: string
}

// 投递
export interface Export {
  id?: number
  status?: number
  export_id?: string
  export_type?: number  // 1-群组, 2-标签
  export_name: string
  export_desc?: string
  export_mode?: number  // 投递方式：1-数据源, 2-应用
  export_config?: string  // JSON 字符串
  scheduler_type?: number  // 1-手动触发, 2-API触发, 3-日周期, 4-小时周期
  scheduler_cron?: string
  scheduler_url?: string
  scheduler_start_time?: number
  scheduler_end_time?: number
  source_type?: number
  owner?: string
  owner_name?: string
  creator?: string
  modifier?: string
  gmt_create?: string
  gmt_modified?: string
  // 最新任务实例（关联查询）
  latest_instance?: TaskInstance
}

// 投递查询参数
export interface ExportQueryParams {
  status?: number
  export_type?: number
  export_name?: string
}

// 投递配置（精简后，database/bucket/topic 等连接信息已在数据源 config 中）
export interface ExportConfig {
  // 通用配置
  group_id?: string  // 关联群组ID
  // 数据源投递配置（export_mode=1 时有效）
  datasource_id?: string
  table_name?: string
  write_mode?: string  // append-追加, upsert-覆盖
  target_column?: string  // upsert key 列
  object_path?: string  // MinIO 对象路径模板
  // 应用投递配置（export_mode=2 时有效）
  application_id?: string
}

// ==================== 群组分析相关类型 ====================

// 群组分析实体（持久化记录）
export interface GroupAnalysis {
  analysis_id?: string
  analysis_name: string
  analysis_desc?: string
  group_id: string
  group_name?: string
  group_count?: number
  entity_identifier_name?: string
  entity_name?: string
  group_type?: number
  group_status?: number
  compare_group_ids?: string[]
  label_ids?: string[]
  status?: number
  creator?: string
  modifier?: string
  gmt_create?: string
  gmt_modified?: string
}

// 群组分析查询参数
export interface GroupAnalysisQueryParams {
  analysis_name?: string
  group_id?: string
  creator?: string
}

// 群组分析 - 可分析标签
export interface AnalysisLabel {
  label_id: string
  label_name: string
  label_category_id: string
  label_category_name: string
  label_data_type: number
  dataset_id: string
  dataset_name: string
  field_name: string
  update_type: number
}

// 群组分析 - 标签分布
export interface LabelDistribution {
  label_id: string
  label_name: string
  dataset_name: string
  update_type: string
  values: DistributionItem[]
}

// 群组分析 - 分布项
export interface DistributionItem {
  value: string
  current_count: number
  current_rate: number
  compare_count?: number
  compare_rate?: number
}

// 群组分析 - 分布请求（单标签）
export interface DistributionRequest {
  group_id: string
  label_id: string
  compare_group_ids?: string[]
}
