import { ref } from 'vue'
import { userApi } from '@/api/user'
import type { UserProfileVO } from '@/types'
import { ElMessage } from 'element-plus'

/**
 * 用户画像数据获取与管理
 */
export function useUserProfile(userId: string) {
  const profile = ref<UserProfileVO | null>(null)
  const loading = ref(true)

  /**
   * 获取用户画像
   */
  const fetchProfile = async () => {
    loading.value = true
    try {
      const res = await userApi.getUserProfile(userId)
      profile.value = res.data.data
    } catch (error) {
      console.error('获取用户画像失败:', error)
      ElMessage.error('获取用户画像失败')
    } finally {
      loading.value = false
    }
  }

  /**
   * 添加标签
   */
  const handleAddLabel = async (labelId: string, labelValue: string) => {
    try {
      await userApi.addUserLabel(userId, { label_id: labelId, label_value: labelValue })
      ElMessage.success('标签添加成功')
      await fetchProfile()
    } catch (error) {
      console.error('添加标签失败:', error)
    }
  }

  /**
   * 删除标签
   */
  const handleDeleteLabel = async (labelId: string) => {
    try {
      await userApi.deleteUserLabel(userId, labelId)
      ElMessage.success('标签删除成功')
      await fetchProfile()
    } catch (error) {
      console.error('删除标签失败:', error)
    }
  }

  /**
   * 更新类目排序
   */
  const handleCategorySort = async (categoryIds: string[]) => {
    try {
      await userApi.updateCategorySort(userId, categoryIds)
      await fetchProfile()
    } catch (error) {
      console.error('更新类目排序失败:', error)
    }
  }

  /**
   * 删除类目
   */
  const handleDeleteCategory = async (categoryId: string) => {
    try {
      await userApi.deleteCategory(userId, categoryId)
      ElMessage.success('类目删除成功')
      await fetchProfile()
    } catch (error) {
      console.error('删除类目失败:', error)
    }
  }

  return {
    profile,
    loading,
    fetchProfile,
    handleAddLabel,
    handleDeleteLabel,
    handleCategorySort,
    handleDeleteCategory,
  }
}
