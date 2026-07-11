import request from '@/utils/request'
import type { ApiResponse } from '@/types'

/** 告警接收人条目 */
export interface AlertReceiver {
  type: string   // owner / user
  value?: string // userId
}

/** 告警配置（从 Task 中提取） */
export interface AlertConfig {
  alert_condition: string  // failure / success / finished / ''
  alert_channels: string   // 逗号分隔，如 'email'
  alert_receivers: string  // JSON 字符串
}

/** 告警历史记录 */
export interface AlertHistory {
  history_id: string
  task_id: string
  instance_id: string
  alert_condition: string
  alert_channel: string
  receivers: string
  subject: string
  content: string
  send_status: number   // 0-失败 1-成功 2-跳过
  send_message: string
  gmt_create: string
}

export const alertApi = {
  /** 获取告警配置 */
  getAlertConfig: (taskId: string) => {
    return request.get<ApiResponse<AlertConfig>>(`/task/${taskId}/alert`)
  },

  /** 保存告警配置 */
  saveAlertConfig: (taskId: string, data: AlertConfig) => {
    return request.put<ApiResponse<number>>(`/task/${taskId}/alert`, data)
  },

  /** 查询告警历史记录 */
  getAlertHistory: (taskId: string, instanceId?: string) => {
    return request.get<ApiResponse<AlertHistory[]>>(`/task/${taskId}/alert/history`, {
      params: instanceId ? { instance_id: instanceId } : {}
    })
  }
}
