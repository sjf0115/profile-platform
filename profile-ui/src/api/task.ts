import request from '@/utils/request'
import type { Task, TaskQueryParams, ApiResponse } from '@/types'

export const taskApi = {
  // 获取任务列表
  getList: (params: TaskQueryParams) => {
    return request.post<ApiResponse<Task[]>>('/task/list', params)
  },

  // 获取任务详情
  detail: (taskId: string) => {
    return request.get<ApiResponse<Task>>(`/task/${taskId}/detail`)
  },

  // 创建任务
  create: (data: Task) => {
    return request.post<ApiResponse<number>>('/task', data)
  },

  // 修改任务
  update: (taskId: string, data: Task) => {
    return request.put<ApiResponse<number>>(`/task/${taskId}`, data)
  },

  // 删除任务
  delete: (taskId: string) => {
    return request.delete<ApiResponse<number>>(`/task/${taskId}`)
  },

  // 执行任务
  execute: (taskId: string) => {
    return request.post<ApiResponse<any>>(`/task/${taskId}/execute`)
  },

  // 通过关联ID执行任务
  executeByRelatedId: (relatedId: string) => {
    return request.post<ApiResponse<any>>('/task/executeByRelatedId', null, {
      params: { related_id: relatedId }
    })
  },

  // 配置任务上游依赖
  configureUpstream: (taskId: string, upstreamTaskIds: string) => {
    return request.put<ApiResponse<any>>(`/task/${taskId}/upstream`, { upstream_task_ids: upstreamTaskIds })
  },

  // 通过调度引擎触发任务
  scheduleTrigger: (taskId: string) => {
    return request.post<ApiResponse<any>>(`/task/${taskId}/schedule-trigger`)
  }
}
