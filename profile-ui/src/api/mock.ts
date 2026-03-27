import type { ApiResponse, DataSource } from '@/types'

// Mock 数据源数据
export const mockDataSources: DataSource[] = [
  {
    datasource_id: 'ds_001',
    datasource_name: 'production_mysql',
    datasource_desc: '生产环境MySQL数据库',
    datasource_type: 'mysql',
    creator: 'admin',
    modifier: 'admin',
    gmt_create: '2024-01-15 10:30:00',
    gmt_modified: '2024-03-20 14:22:00',
    config: JSON.stringify({
      host: '192.168.1.100',
      port: '3306',
      database: 'user_db',
      username: 'app_user',
    }),
  },
  {
    datasource_id: 'ds_002',
    datasource_name: 'analytics_clickhouse',
    datasource_desc: '分析型ClickHouse集群',
    datasource_type: 'clickhouse',
    creator: 'admin',
    modifier: 'zhangsan',
    gmt_create: '2024-02-01 09:00:00',
    gmt_modified: '2024-03-15 16:45:00',
    config: JSON.stringify({
      host: 'clickhouse.internal.com',
      port: '8123',
      database: 'analytics',
      username: 'analytics',
    }),
  },
  {
    datasource_id: 'ds_003',
    datasource_name: 'dw_hologres',
    datasource_desc: '数据仓库Hologres',
    datasource_type: 'hologres',
    creator: 'admin',
    modifier: 'admin',
    gmt_create: '2024-02-20 11:20:00',
    gmt_modified: '2024-02-20 11:20:00',
    config: JSON.stringify({
      host: 'hologres.cn-beijing.aliyuncs.com',
      port: '80',
      database: 'dw_db',
      username: 'holo_user',
    }),
  },
  {
    datasource_id: 'ds_004',
    datasource_name: 'bi_postgresql',
    datasource_desc: 'BI系统PostgreSQL',
    datasource_type: 'postgresql',
    creator: 'lisi',
    modifier: 'lisi',
    gmt_create: '2024-03-01 08:30:00',
    gmt_modified: '2024-03-10 10:15:00',
    config: JSON.stringify({
      host: 'pg.bi.internal',
      port: '5432',
      database: 'bi_reports',
      username: 'bi_reader',
    }),
  },
  {
    datasource_id: 'ds_005',
    datasource_name: 'odps_maxcompute',
    datasource_desc: '阿里云MaxCompute',
    datasource_type: 'maxcompute',
    creator: 'admin',
    modifier: 'admin',
    gmt_create: '2024-03-05 14:00:00',
    gmt_modified: '2024-03-05 14:00:00',
    config: JSON.stringify({
      endpoint: 'http://service.cn.maxcompute.aliyun.com/api',
      project: 'my_project',
      accessId: 'LTAI******',
    }),
  },
]

// 模拟 API 响应
export const mockResponse = <T>(data: T): ApiResponse<T> => ({
  code: 0,
  message: 'success',
  data,
})
