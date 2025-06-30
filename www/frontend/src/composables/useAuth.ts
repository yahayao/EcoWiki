import { ref, computed, readonly } from 'vue'
import type { UserResponse } from '../api/user'
import { USER_GROUPS, userApi, type UserGroup } from '../api/user'

// 全局用户状态管理
const user = ref<UserResponse | null>(null)
const token = ref<string | null>(null)

// 从localStorage恢复用户状态
const initializeAuth = () => {
  const savedToken = localStorage.getItem('token')
  const savedUser = localStorage.getItem('user')
  
  if (savedToken && savedUser) {
    try {
      token.value = savedToken
      user.value = JSON.parse(savedUser)
      console.log('恢复用户认证状态:', user.value?.username, user.value?.userGroup)
    } catch (error) {
      console.error('恢复用户状态失败:', error)
      clearInvalidAuthData()
    }
  }
}

// 清除无效的认证数据
const clearInvalidAuthData = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('refreshToken')
  user.value = null
  token.value = null
}

// 设置用户状态
const setUser = (userData: UserResponse, authToken: string, refreshToken?: string) => {
  user.value = userData
  token.value = authToken
  
  localStorage.setItem('token', authToken)
  localStorage.setItem('user', JSON.stringify(userData))
  
  if (refreshToken) {
    localStorage.setItem('refreshToken', refreshToken)
  }
  
  console.log('设置用户认证状态:', userData.username, userData.userGroup)
}

// 清除用户状态
const clearUser = () => {
  const username = user.value?.username || 'unknown'
  
  user.value = null
  token.value = null
  
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('refreshToken')
  
  console.log('清除用户认证状态:', username)
}

// 检查是否已登录
const isAuthenticated = computed(() => {
  return !!(user.value && token.value)
})

// 获取用户头像
const userAvatar = computed(() => {
  if (user.value?.avatar) {
    return user.value.avatar
  }
  
  const username = user.value?.username || 'User'
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(username)}&background=667eea&color=fff&size=40`
})

// 获取用户显示名称
const userDisplayName = computed(() => {
  return user.value?.username || '未知用户'
})

// 检查用户是否有特定权限
const hasPermission = (permission: UserGroup): boolean => {
  if (!user.value) return false
  
  // 超级管理员拥有所有权限
  if (userApi.isSuperAdmin(user.value)) return true
  
  // 管理员拥有除超级管理员外的所有权限
  if (userApi.isAdmin(user.value) && permission !== USER_GROUPS.SUPER_ADMIN) return true
  
  // 检查用户组是否匹配
  return user.value.userGroup === permission
}

// 检查是否为管理员
const isAdmin = computed(() => {
  return userApi.isAdmin(user.value)
})

// 检查是否为超级管理员
const isSuperAdmin = computed(() => {
  return userApi.isSuperAdmin(user.value)
})

// 初始化认证状态
initializeAuth()

export function useAuth() {
  return {
    // 状态
    user: readonly(user),
    token: readonly(token),
    isAuthenticated,
    isLoggedIn: isAuthenticated, // 添加 isLoggedIn 别名
    userAvatar,
    userDisplayName,
    isAdmin,
    isSuperAdmin,
    
    // 方法
    setUser,
    clearUser,
    hasPermission,
    
    // 常量
    USER_GROUPS
  }
}
