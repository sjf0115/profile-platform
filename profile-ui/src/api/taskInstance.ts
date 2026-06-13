import request from '@/utils/request'
import type { TaskInstance, TaskInstanceQueryParams, ApiResponse } from '@/types'

export const taskInstanceApi = {
  // 获取实例列表
  getList: (params: TaskInstanceQueryParams) => {
    return request.post<ApiResponse<TaskInstance[]>>('/instance/list', params)
  },

  // 获取实例详情
  detail: (instanceId: string) => {
    return request.get<ApiResponse<TaskInstance>>('/instance/detail', {
      params: { instance_id: instanceId }
    })
  },

  // 根据任务ID查询实例列表
  listByTaskId: (taskId: string) => {
    return request.get<ApiResponse<TaskInstance[]>>('/instance/listByTaskId', {
      params: { task_id: taskId }
    })
  }
}
