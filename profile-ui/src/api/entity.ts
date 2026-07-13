import request from '@/utils/request'
import type { ApiResponse } from '@/types'

// 实体
export interface Entity {
  entity_id: string
  entity_name: string
  source_type: number  // 创建方式：1-系统预置，2-自定义
  status: number       // 1-启用, 2-停用
  creator: string
  creator_name: string
  modifier: string
  modifier_name: string
  gmt_create: string
  gmt_modified: string
}

// 实体查询参数
export interface EntityParam {
  entity_name?: string
  status?: number
}

// 实体创建/编辑请求
export interface EntityRequest {
  entity_name: string
}

export const entityApi = {
  // 获取实体列表
  list: (params?: EntityParam) => {
    return request.post<ApiResponse<Entity[]>>('/entity/list', params || {})
  },

  // 获取实体详情
  detail: (entityId: string) => {
    return request.get<ApiResponse<Entity>>(`/entity/${entityId}/detail`)
  },

  // 创建实体
  create: (data: EntityRequest) => {
    return request.post<ApiResponse<Entity>>('/entity', data)
  },

  // 更新实体
  update: (entityId: string, data: EntityRequest) => {
    return request.put<ApiResponse<number>>(`/entity/${entityId}`, data)
  },

  // 更新实体状态（启用/停用）
  updateStatus: (entityId: string, status: number) => {
    return request.put<ApiResponse<number>>(`/entity/${entityId}/status`, null, {
      params: { status }
    })
  },

  // 删除实体
  delete: (entityId: string) => {
    return request.delete<ApiResponse<number>>(`/entity/${entityId}`)
  }
}

// 实体标识
export interface EntityIdentifier {
  entity_identifier_id: string
  entity_identifier_name: string
  entity_id: string
  entity_name: string
  source_type: number  // 创建方式：1-系统预置，2-自定义
  status: number       // 1-启用, 2-停用
  creator: string
  creator_name: string
  modifier: string
  modifier_name: string
  gmt_create: string
  gmt_modified: string
}

// 实体标识查询参数
export interface EntityIdentifierParam {
  entity_identifier_name?: string
  entity_id?: string
  status?: number
}

// 实体标识创建/编辑请求
export interface EntityIdentifierRequest {
  entity_identifier_name: string
  entity_id: string
}

export const entityIdentifierApi = {
  // 获取实体标识列表
  list: (params?: EntityIdentifierParam) => {
    return request.post<ApiResponse<EntityIdentifier[]>>('/entity/identifier/list', params || {})
  },

  // 获取实体标识详情
  detail: (entityIdentifierId: string) => {
    return request.get<ApiResponse<EntityIdentifier>>(`/entity/identifier/${entityIdentifierId}/detail`)
  },

  // 创建实体标识
  create: (data: EntityIdentifierRequest) => {
    return request.post<ApiResponse<EntityIdentifier>>('/entity/identifier', data)
  },

  // 更新实体标识
  update: (entityIdentifierId: string, data: EntityIdentifierRequest) => {
    return request.put<ApiResponse<number>>(`/entity/identifier/${entityIdentifierId}`, data)
  },

  // 更新实体标识状态（启用/停用）
  updateStatus: (entityIdentifierId: string, status: number) => {
    return request.put<ApiResponse<number>>(`/entity/identifier/${entityIdentifierId}/status`, null, {
      params: { status }
    })
  },

  // 删除实体标识
  delete: (entityIdentifierId: string) => {
    return request.delete<ApiResponse<number>>(`/entity/identifier/${entityIdentifierId}`)
  }
}
