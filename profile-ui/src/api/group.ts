import request from '@/utils/request'
import type { ApiResponse, Group, GroupConfigResponse, Dataset } from '@/types'

export const groupApi = {
  // 获取群组列表
  getList: (params?: any) => {
    return request.post<ApiResponse<Group[]>>('/group/list', params || {})
  },

  // 获取群组详情
  getDetail: (group_id: string) => {
    return request.get<ApiResponse<Group>>('/group/detail', {
      params: { group_id }
    })
  },

  // 保存群组（新增/修改）
  save: (data: Partial<Group>) => {
    return request.post<ApiResponse<number>>('/group/save', data)
  },

  // 删除群组
  delete: (group_id: string) => {
    return request.delete<ApiResponse<number>>('/group/delete', {
      params: { group_id }
    })
  },

  // 获取群组配置（筛选条件用）
  getConfig: () => {
    return request.get<ApiResponse<GroupConfigResponse>>('/group/config')
  },

  // 获取标签操作符配置
  getLabelConfig: () => {
    return request.get<ApiResponse<any[]>>('/group/config/label')
  },

  // 上传 CSV 文件到 MinIO
  uploadFile: (formData: FormData) => {
    return request.post<ApiResponse<{ uuid_file_key: string; file_list: string[] }>>('/group/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 删除 MinIO 中已上传的文件
  deleteUploadedFile: (file_key: string) => {
    return request.delete<ApiResponse<null>>('/group/upload/delete', {
      params: { file_key }
    })
  },

  // 取消上传（删除 MinIO 文件）
  deleteUpload: (file_key: string) => {
    return request.delete<ApiResponse<null>>('/group/cancel-upload', {
      params: { file_key }
    })
  },

  // 下载 CSV 上传模板
  downloadTemplate: () => {
    return request.get('/group/template/download', { responseType: 'blob' })
  },

  // 获取 SQL 创建可用的数据集表和字段
  getAvailableTables: (entity_identifier_id: string) => {
    return request.get<ApiResponse<Dataset[]>>('/group/available-tables', {
      params: { entity_identifier_id }
    })
  },

  // 预估群组人数
  estimate: (data: any) => {
    return request.post<ApiResponse<number>>('/group/estimate', data)
  }
}
