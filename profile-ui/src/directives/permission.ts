import type { Directive, DirectiveBinding } from 'vue'
import { usePermissionStore } from '@/stores/permission'

/**
 * v-permission 指令
 * 用法: <el-button v-permission="'label:edit'">编辑</el-button>
 * 无权限时直接移除 DOM 元素
 */
export const vPermission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string>) {
    const code = binding.value
    if (!code) return

    const permissionStore = usePermissionStore()
    // 权限未加载或无该权限时移除元素
    if (permissionStore.loaded && !permissionStore.hasPermission(code)) {
      el.parentNode?.removeChild(el)
    }
  },
}
