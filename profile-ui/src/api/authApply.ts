import request from '@/utils/request'
import type { ApiResponse } from '@/types'

// 权限申请单
export interface AuthApply {
  apply_id: string
  applicant: string
  apply_reason: string
  status: number        // 1=待审批 2=已通过 3=已拒绝 4=已取消
  approver?: string
  approve_time?: string
  approve_remark?: string
  creator: string
  gmt_create: string
  gmt_modified?: string
}

// 权限申请明细
export interface AuthApplyItem {
  item_id: string
  apply_id: string
  resource_type: string
  resource_id: string
  action: number        // 1=READ 2=WRITE 3=EXPORT 4=MANAGE
  expire_time?: string
  creator: string
  gmt_create: string
}

// 提交申请入参
export interface SubmitApplyRequest {
  applyReason: string
  items: Array<{
    resourceType: string
    resourceId: string
    action: number
    expireTime?: number
  }>
}

export const authApplyApi = {
  // 提交权限申请
  submit: (data: SubmitApplyRequest) => {
    return request.post<ApiResponse<string>>('/authApply/submit', data)
  },

  // 我的申请列表
  mine: () => {
    return request.get<ApiResponse<AuthApply[]>>('/authApply/mine')
  },

  // 待审批列表
  todo: () => {
    return request.get<ApiResponse<AuthApply[]>>('/authApply/todo')
  },

  // 已审批列表
  approved: () => {
    return request.get<ApiResponse<AuthApply[]>>('/authApply/approved')
  },

  // 申请明细
  items: (applyId: string) => {
    return request.get<ApiResponse<AuthApplyItem[]>>('/authApply/items', {
      params: { applyId },
    })
  },

  // 审批通过
  approve: (applyId: string, approveRemark?: string) => {
    return request.post<ApiResponse<string>>('/authApply/approve', { applyId, approveRemark })
  },

  // 审批拒绝
  reject: (applyId: string, approveRemark?: string) => {
    return request.post<ApiResponse<string>>('/authApply/reject', { applyId, approveRemark })
  },

  // 撤销申请
  cancel: (applyId: string) => {
    return request.post<ApiResponse<string>>('/authApply/cancel', { applyId })
  },
}
