import request from '@/utils/request'
import type { ApiResponse, UserProfileRowVO } from '@/types'

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
}
