import request from '@/utils/request'
import type { ApiResponse } from '@/types'

export interface Entity {
  entity_id: string
  entity_name: string
  entity_type_id: string
  entity_type_name: string
  entity_desc?: string
  status?: number
  gmt_create?: string
  gmt_modified?: string
}

export const entityApi = {
  // 获取实体列表
  list: (params?: Partial<Entity>) => {
    return request.post<ApiResponse<Entity[]>>('/entity/list', params || {})
  }
}
