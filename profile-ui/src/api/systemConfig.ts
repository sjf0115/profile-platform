import request from '@/utils/request'
import type { ApiResponse } from '@/types'

// 系统配置相关接口
export const systemConfigApi = {
  // 获取 SMTP 表单 Schema
  getSmtpForm: () => {
    return request.get<ApiResponse<string>>('/system-config/smtp/form')
  },

  // 获取指定分组配置
  getConfigByGroup: (group: string) => {
    return request.get<ApiResponse<Record<string, string>>>(`/system-config/group/${group}`)
  },

  // 保存指定分组配置
  saveConfig: (group: string, data: Record<string, string>) => {
    return request.post<ApiResponse<string>>(`/system-config/group/${group}`, data)
  },

  // 测试 SMTP 连通性
  testSmtp: (testReceiver?: string) => {
    return request.post<ApiResponse<string>>('/system-config/smtp/test', { testReceiver })
  },
}
