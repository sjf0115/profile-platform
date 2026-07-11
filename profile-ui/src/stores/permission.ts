import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { permissionApi } from '@/api/permission'

export const usePermissionStore = defineStore('permission', () => {
  const permissions = ref<Set<string>>(new Set())
  const loaded = ref(false)
  /** 后端权限校验是否启用（false=灰度关闭，前端跳过所有权限检查） */
  const enabled = ref(true)

  /** 是否拥有指定权限码 */
  const hasPermission = computed(() => {
    return (code: string) => {
      // 灰度关闭时，前端不做权限检查，直接放行
      if (!enabled.value) return true
      return permissions.value.has(code)
    }
  })

  /** 加载当前用户权限码 */
  async function loadPermissions() {
    try {
      const res = await permissionApi.getUserPermissions()
      const data = res.data.data as any
      // 兼容新格式 { codes: [...], enabled: boolean }
      if (data && typeof data === 'object' && !Array.isArray(data)) {
        enabled.value = data.enabled !== false
        permissions.value = new Set(data.codes || [])
      } else {
        // 兼容旧格式（纯数组）
        permissions.value = new Set(data || [])
      }
      loaded.value = true
    } catch {
      // 接口异常时放行所有权限（降级处理）
      enabled.value = false
      loaded.value = true
    }
  }

  /** 重置权限状态（退出登录时调用） */
  function reset() {
    permissions.value = new Set()
    loaded.value = false
    enabled.value = true
  }

  return { permissions, loaded, enabled, hasPermission, loadPermissions, reset }
})
