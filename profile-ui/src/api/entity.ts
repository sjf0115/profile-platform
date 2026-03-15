import request from '@/utils/request'
import type { ApiResponse } from '@/types'

// 实体（原实体类型）
export interface Entity {
  entity_id: string
  entity_name: string
  entity_desc?: string
  source_type?: number  // 创建方式：1-系统预置，2-自定义
  status?: number
  gmt_create?: string
  gmt_modified?: string
  creator?: string
}

export const entityApi = {
  // 获取实体列表
  list: (params?: Partial<Entity>) => {
    return request.post<ApiResponse<Entity[]>>('/entity/list', params || {})
  },
  
  // 获取实体详情
  detail: (entityId: string) => {
    return request.get<ApiResponse<Entity>>('/entity/detail', {
      params: { entity_id: entityId }
    })
  },
  
  // 保存实体（新增/编辑）
  save: (data: Partial<Entity>) => {
    return request.post<ApiResponse<any>>('/entity/save', data)
  },
  
  // 删除实体
  delete: (entityId: string) => {
    return request.delete<ApiResponse<any>>('/entity/delete', {
      params: { entity_id: entityId }
    })
  }
}

// 实体标识（原实体）
export interface EntityIdentifier {
  entity_identifier_id: string
  entity_identifier_name: string
  entity_id: string
  entity_name: string
  entity_identifier_desc?: string
  source_type?: number  // 创建方式：1-系统预置，2-自定义
  status?: number
  gmt_create?: string
  gmt_modified?: string
  creator?: string
}

export const entityIdentifierApi = {
  // 获取实体标识列表
  list: (params?: Partial<EntityIdentifier>) => {
    return request.post<ApiResponse<EntityIdentifier[]>>('/entity/identifier/list', params || {})
  },
  
  // 获取实体标识详情
  detail: (entityIdentifierId: string) => {
    return request.get<ApiResponse<EntityIdentifier>>('/entity/identifier/detail', {
      params: { entity_identifier_id: entityIdentifierId }
    })
  },
  
  // 保存实体标识（新增/编辑）
  save: (data: Partial<EntityIdentifier>) => {
    return request.post<ApiResponse<any>>('/entity/identifier/save', data)
  },
  
  // 删除实体标识
  delete: (entityIdentifierId: string) => {
    return request.delete<ApiResponse<any>>('/entity/identifier/delete', {
      params: { entity_identifier_id: entityIdentifierId }
    })
  }
}
