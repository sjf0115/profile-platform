const TOKEN_KEY = 'profile_platform_token'
const USER_KEY = 'profile_platform_user'

/**
 * 获取 Token
 */
export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置 Token
 */
export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 移除 Token
 */
export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

/**
 * 是否已登录
 */
export function isLoggedIn(): boolean {
  return !!getToken()
}

/**
 * 设置登录用户信息
 */
export function setLoginUser(user: Record<string, any>): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

/**
 * 获取登录用户信息
 */
export function getLoginUser(): Record<string, any> | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}
