import request from '@/utils/request'
import type { TaskInstance, TaskInstanceQueryParams, ApiResponse } from '@/types'

export const taskInstanceApi = {
  // 获取实例列表
  getList: (params: TaskInstanceQueryParams) => {
    return request.post<ApiResponse<TaskInstance[]>>('/instance/list', params)
  },

  // 获取实例详情
  getDetail: (instanceId: string) => {
    return request.get<ApiResponse<TaskInstance>>(`/instance/${instanceId}/detail`)
  },

  // 根据任务ID查询实例列表
  listByTaskId: (taskId: string) => {
    return request.get<ApiResponse<TaskInstance[]>>(`/instance/${taskId}/list`)
  },

  // 删除实例
  delete: (instanceId: string) => {
    return request.delete<ApiResponse<void>>(`/instance/${instanceId}`)
  }
}
