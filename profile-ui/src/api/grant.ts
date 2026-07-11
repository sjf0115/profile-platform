import request from '@/utils/request'
import type { ApiResponse } from '@/types'

// 资源授权记录
export interface ResourceGrant {
  grant_id: string
  resource_type: string
  resource_id: string
  grantee_type: number  // 1=用户 2=角色
  grantee_id: string
  action: number        // 1=READ 2=WRITE 3=EXPORT 4=MANAGE
  expire_time?: string
  creator: string
  gmt_create: string
  gmt_modified?: string
}

// 批量授权请求
export interface BatchGrantRequest {
  resourceType: string
  resourceIds: string[]
  granteeType: number
  granteeId: string
  actions: number[]
  expireTime?: number
}

// 授权相关接口
export const grantApi = {
  // 批量授权
  batchGrant: (data: BatchGrantRequest) => {
    return request.post<ApiResponse<number>>('/grant/batch', data)
  },

  // 授权台账（按资源查询）
  list: (resourceType: string, resourceId: string) => {
    return request.get<ApiResponse<ResourceGrant[]>>('/grant/list', {
      params: { resourceType, resourceId },
    })
  },

  // 回收授权
  revoke: (grantId: string) => {
    return request.delete<ApiResponse<number>>(`/grant/${grantId}`)
  },
}
