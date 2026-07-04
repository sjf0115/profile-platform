import { ref } from 'vue'
import { userProfileApi } from '@/api/user-profile'
import type { UserProfileRowVO } from '@/types'
import { ElMessage } from 'element-plus'

/**
 * 用户画像主页 - 数据获取与管理
 */
export function useUserProfileList() {
  const tableData = ref<UserProfileRowVO[]>([])
  const loading = ref(false)
  const searchType = ref<'user' | 'label' | 'group'>('user')
  const searchId = ref('')

  /**
   * 获取随机用户列表
   */
  const fetchRandomUsers = async (type: string, id: string, limit: number = 50) => {
    loading.value = true
    try {
      const res = await userProfileApi.getRandomUsers(type, id, limit)
      tableData.value = res.data.data || []
    } catch (error) {
      console.error('获取随机用户列表失败:', error)
      ElMessage.error('获取用户列表失败')
      tableData.value = []
    } finally {
      loading.value = false
    }
  }

  /**
   * 重置搜索状态
   */
  const resetSearch = () => {
    searchType.value = 'user'
    searchId.value = ''
    tableData.value = []
  }

  return {
    tableData,
    loading,
    searchType,
    searchId,
    fetchRandomUsers,
    resetSearch,
  }
}
