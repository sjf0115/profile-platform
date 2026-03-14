import request from '@/utils/request'
import type { 
  ApiResponse, 
  Label, 
  LabelCategory,
  LabelQueryParams
} from '@/types'

// 标签相关接口
export const labelApi = {
  // 获取标签列表
  getList: (params?: LabelQueryParams) => {
    return request.post<ApiResponse<Label[]>>('/label/list', params || {})
  },

  // 获取标签详情
  getDetail: (label_id: string) => {
    return request.get<ApiResponse<Label>>('/label/detail', {
      params: { label_id }
    })
  },

  // 保存标签（新增/修改）
  save: (data: Label) => {
    return request.post<ApiResponse<number>>('/label/save', data)
  },

  // 删除标签
  delete: (label_id: string) => {
    return request.delete<ApiResponse<number>>('/label/delete', {
      params: { label_id }
    })
  },

  // 批量删除标签
  batchDelete: (label_ids: string[]) => {
    return request.post<ApiResponse<number>>('/label/batchDelete', { label_ids })
  },

  // 更新标签
  update: (data: Label) => {
    return request.post<ApiResponse<number>>('/label/update', data)
  },
}

// 标签类目相关接口
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
  },
}
