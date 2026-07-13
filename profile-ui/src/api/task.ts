import request from '@/utils/request'
import type { Task, TaskQueryParams, TaskRequest, ApiResponse } from '@/types'

// 任务相关接口
export const taskApi = {
  // 获取任务列表
  getList: (params?: TaskQueryParams) => {
    return request.post<ApiResponse<Task[]>>('/task/list', params || {})
  },

  // 获取任务详情
  getDetail: (taskId: string) => {
    return request.get<ApiResponse<Task>>(`/task/${taskId}/detail`)
  },

  // 创建任务
  create: (data: TaskRequest) => {
    return request.post<ApiResponse<Task>>('/task', data)
  },

  // 更新任务
  update: (taskId: string, data: TaskRequest) => {
    return request.put<ApiResponse<number>>(`/task/${taskId}`, data)
  },

  // 更新任务状态（启用/停用）
  updateStatus: (taskId: string, status: number) => {
    return request.put<ApiResponse<number>>(`/task/${taskId}/status?status=${status}`)
  },

  // 删除任务
  delete: (taskId: string) => {
    return request.delete<ApiResponse<number>>(`/task/${taskId}`)
  },

  // 执行任务
  execute: (taskId: string) => {
    return request.post<ApiResponse<any>>(`/task/${taskId}/execute`)
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
