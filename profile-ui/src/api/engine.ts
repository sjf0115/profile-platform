import request from '@/utils/request'
import type { ApiResponse, Engine, EngineQueryParams, EngineTypeItem } from '@/types'
import { mockResponse } from './mock'

// 是否使用 Mock 数据
const USE_MOCK = false

// 计算引擎相关接口
export const engineApi = {
  // 获取引擎列表
  getList: (params?: EngineQueryParams) => {
    const filteredParams: any = {}
    if (params) {
      if (params.engine_type) {
        filteredParams.engine_type = params.engine_type
      }
      if (params.engine_name) {
        filteredParams.engine_name = params.engine_name
      }
      if (params.page_num !== undefined && params.page_num !== null) {
        filteredParams.page_num = params.page_num
      }
      if (params.page_size !== undefined && params.page_size !== null) {
        filteredParams.page_size = params.page_size
      }
    }
    return request.post<ApiResponse<Engine[]>>('/engine/list', filteredParams)
  },

  // 获取引擎详情
  getDetail: (engine_id: string) => {
    return request.get<ApiResponse<Engine>>('/engine/detail', {
      params: { engine_id }
    })
  },

  // 保存引擎（新增/修改）
  save: (data: Engine) => {
    return request.post<ApiResponse<number>>('/engine/save', data)
  },

  // 删除引擎
  delete: (engine_id: string) => {
    return request.delete<ApiResponse<number>>('/engine/delete', {
      params: { engine_id }
    })
  },

  // 获取默认引擎
  getDefault: () => {
    return request.get<ApiResponse<Engine | null>>('/engine/default')
  },

  // 获取引擎类型列表
  getTypeList: () => {
    return request.get<ApiResponse<EngineTypeItem[]>>('/engine/type/list')
  },
}
