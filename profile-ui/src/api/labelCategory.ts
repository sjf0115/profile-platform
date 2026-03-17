import request from '@/utils/request'
import type { ApiResponse } from '@/types'

// 标签类目
export interface LabelCategory {
  category_id: string
  category_name: string
  parent_category_id?: string
  category_level?: number
  children?: LabelCategory[]
  gmt_create?: string
  gmt_modified?: string
}

export const labelCategoryApi = {
  // 获取标签类目列表
  getList: (params?: Partial<LabelCategory>) => {
    return request.post<ApiResponse<LabelCategory[]>>('/labelCategory/list', params || {})
  },

  // 获取标签类目详情
  getDetail: (category_id: string) => {
    return request.get<ApiResponse<LabelCategory>>('/labelCategory/detail', {
      params: { category_id }
    })
  },

  // 添加标签类目
  add: (category_name: string, parent_category_id?: string) => {
    return request.get<ApiResponse<number>>('/labelCategory/add', {
      params: { category_name, parent_category_id }
    })
  },

  // 删除标签类目
  delete: (category_id: string) => {
    return request.get<ApiResponse<number>>('/labelCategory/delete', {
      params: { category_id }
    })
  },

  // 重命名标签类目
  rename: (category_id: string, category_name: string) => {
    return request.get<ApiResponse<number>>('/labelCategory/rename', {
      params: { category_id, category_name }
    })
  }
}
