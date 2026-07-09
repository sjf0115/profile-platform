import request from '@/utils/request'
import type { ApiResponse, UserProfileRowVO, UserProfileVO } from '@/types'

// 用户画像相关接口
export const userProfileApi = {
  // 根据标签或群组ID随机抽取用户列表
  getRandomUsers: (type: string, id: string, limit: number = 50) => {
    return request.get<ApiResponse<UserProfileRowVO[]>>('/insight/user-profile/random-users', {
      params: { type, id, limit }
    })
  },

  // 根据实体标识ID随机抽取用户列表
  getRandomUsersByEntityIdentifier: (entityIdentifierId: string, limit: number = 50) => {
    return request.get<ApiResponse<UserProfileRowVO[]>>('/insight/user-profile/random-users', {
      params: { type: 'entity', id: entityIdentifierId, limit }
    })
  },

  // ==================== 用户细查接口 ====================

  // 获取用户完整画像（聚合接口）
  getUserProfile: (userId: string) => {
    return request.get<ApiResponse<UserProfileVO>>(`/insight/user-profile/${userId}/profile`)
  },

  // 为用户打标签
  addUserLabel: (userId: string, data: { label_id: string; label_value: string }) => {
    return request.post<ApiResponse<number>>(`/insight/user-profile/${userId}/labels`, data)
  },

  // 删除用户单个标签
  deleteUserLabel: (userId: string, labelId: string) => {
    return request.delete<ApiResponse<number>>(`/insight/user-profile/${userId}/labels/${labelId}`)
  },

  // 更新类目排序（拖拽后调用）
  updateCategorySort: (userId: string, categoryIds: string[]) => {
    return request.put<ApiResponse<number>>(`/insight/user-profile/${userId}/categories/sort`, categoryIds)
  },

  // 删除类目
  deleteCategory: (userId: string, categoryId: string) => {
    return request.delete<ApiResponse<number>>(`/insight/user-profile/${userId}/categories/${categoryId}`)
  },
}
