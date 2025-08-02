/**
 * 用户认证状态管理组合函数
 * 
 * 这是一个Vue3组合式函数，用于管理用户的认证状态和权限信息。
 * 提供了用户登录、登出、权限检查等核心功能。
 * 
 * 主要功能：
 * - 管理用户登录状态和JWT令牌
 * - 提供用户权限检查方法
 * - 处理用户信息的本地存储
 * - 自动恢复用户认证状态
 * - 支持管理员权限验证
 * 
 * 状态管理：
 * - user: 当前登录用户信息
 * - token: JWT认证令牌
 * - isAuthenticated: 是否已认证
 * - hasAdminPermission: 是否具有管理员权限
 * 
 * 存储机制：
 * - 使用localStorage持久化用户状态
 * - 支持页面刷新后状态恢复
 * - 自动清理无效认证数据
 * 
 * @author EcoWiki Team
 * @version 2.0 (支持基于user_roles的权限系统)
 * @since 2025-06-30
 */

import { ref, computed, readonly } from 'vue'
import type { UserResponse } from '../api/user'
import { USER_GROUPS, userApi, type UserGroup } from '../api/user'

// ======================== 响应式状态 ========================

/**
 * 当前登录用户信息
 * 包含用户基本信息和角色权限
 */
const user = ref<UserResponse | null>(null)

/**
 * JWT认证令牌
 * 用于API请求的身份验证
 */
const token = ref<string | null>(null)

// ======================== 状态初始化 ========================

/**
 * 从localStorage恢复用户认证状态
 * 
 * 在应用启动时调用，从本地存储中恢复用户的登录状态。
 * 如果恢复失败，会自动清理无效的认证数据。
 */
const initializeAuth = () => {
  const savedToken = localStorage.getItem('token')
  const savedUser = localStorage.getItem('user')
  const savedRefreshToken = localStorage.getItem('refreshToken')
  
  console.log('🔄 初始化认证状态...')
  console.log('localStorage中的token:', !!savedToken)
  console.log('localStorage中的user:', !!savedUser) 
  console.log('localStorage中的refreshToken:', !!savedRefreshToken)
  
  if (savedToken && savedUser) {
    try {
      token.value = savedToken
      user.value = JSON.parse(savedUser)
      console.log('✅ 恢复用户认证状态:', user.value?.username, user.value?.userGroup)
      
      // 检查refresh token状态
      if (savedRefreshToken) {
        console.log('✅ 发现已保存的refresh token')
      } else {
        console.warn('⚠️ 警告：用户已登录但没有refresh token，可能导致自动续期失败')
      }
    } catch (error) {
      console.error('❌ 恢复用户状态失败:', error)
      // 只清除损坏的用户数据，保留token以便尝试refresh
      localStorage.removeItem('user')
      user.value = null
      // 不要清除token和refreshToken，让API拦截器处理
    }
  } else {
    console.log('ℹ️ 没有找到已保存的认证信息')
  }
}

/**
 * 清除无效的认证数据
 * 
 * 当认证数据损坏或过期时，清理所有相关的本地存储数据，
 * 并重置内存中的用户状态。
 */
const clearInvalidAuthData = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('rememberMe')
  localStorage.removeItem('savedLoginField')
  localStorage.removeItem('savedPassword')
  user.value = null
  token.value = null
}

/**
 * 调试函数：检查当前认证状态
 */
const debugAuthState = () => {
  const currentToken = localStorage.getItem('token')
  const currentRefreshToken = localStorage.getItem('refreshToken')
  const currentUser = localStorage.getItem('user')
  
  console.log('🔍 当前认证状态检查:')
  console.log('  - token存在:', !!currentToken)
  console.log('  - refreshToken存在:', !!currentRefreshToken)
  console.log('  - user存在:', !!currentUser)
  console.log('  - 内存中的user:', !!user.value)
  console.log('  - 内存中的token:', !!token.value)
  console.log('  - isAuthenticated:', isAuthenticated.value)
  
  return {
    hasToken: !!currentToken,
    hasRefreshToken: !!currentRefreshToken,
    hasUser: !!currentUser,
    memoryUser: !!user.value,
    memoryToken: !!token.value,
    isAuth: isAuthenticated.value
  }
}

// ======================== 状态管理方法 ========================

/**
 * 设置用户认证状态
 * 
 * 在用户登录成功后调用，保存用户信息和令牌到内存和本地存储。
 * 
 * @param userData 用户信息对象
 * @param authToken JWT认证令牌
 * @param refreshToken 可选的刷新令牌
 */
const setUser = (userData: UserResponse, authToken: string, refreshToken?: string) => {
  user.value = userData
  token.value = authToken
  
  localStorage.setItem('token', authToken)
  localStorage.setItem('user', JSON.stringify(userData))
  
  if (refreshToken) {
    localStorage.setItem('refreshToken', refreshToken)
    console.log('✅ 保存refresh token成功')
  } else {
    console.warn('⚠️ 登录时未提供refresh token')
  }
  
  console.log('设置用户认证状态:', userData.username, userData.userGroup)
  console.log('Token已保存:', !!authToken)
  console.log('RefreshToken已保存:', !!refreshToken)
}

/**
 * 清除用户认证状态（登出）
 * @param clearSavedCredentials 是否清除保存的登录凭据，默认false
 */
const clearUser = (clearSavedCredentials = false) => {
  const username = user.value?.username || 'unknown'
  
  user.value = null
  token.value = null
  
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('refreshToken')
  
  // 可选择是否清除保存的登录信息
  if (clearSavedCredentials) {
    localStorage.removeItem('rememberMe')
    localStorage.removeItem('savedLoginField')
    localStorage.removeItem('savedPassword')
  }
  
  console.log('清除用户认证状态:', username)
}

// 检查是否已登录
const isAuthenticated = computed(() => {
  return !!(user.value && token.value)
})

// 获取用户头像
const userAvatar = computed(() => {
  // 优先从用户数据中的 avatarUrl 字段获取
  if (user.value?.avatarUrl) {
    return user.value.avatarUrl
  }
  
  // 如果没有头像URL，生成默认头像
  const username = user.value?.username || 'User'
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(username)}&background=667eea&color=fff&size=40`
})

// 获取用户显示名称
const userDisplayName = computed(() => {
  return user.value?.username || '未知用户'
})
const securityAnswer = computed(() => {
  return user.value?.securityAnswer || '未设置'
})
const securityQuestion = computed(() => {
  return user.value?.securityQuestion || '未设置'
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

/**
 * 刷新用户信息
 * 
 * 从后端重新获取用户信息并更新本地缓存
 * 用于头像更新、个人信息修改后的数据同步
 */
const refreshUserInfo = async () => {
  if (!token.value) {
    console.warn('用户未登录，无法刷新用户信息')
    return false
  }
  
  try {
    // 调用后端API获取最新用户信息
    const response = await fetch('http://localhost:8080/api/auth/me', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token.value}`,
        'Content-Type': 'application/json'
      }
    })
    
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
    
    const result = await response.json()
    
    if (result.code === 200 && result.data) {
      // 更新用户信息
      user.value = {
        userId: result.data.id,
        username: result.data.username,
        email: result.data.email,
        fullName: result.data.fullName,
        userGroup: result.data.userGroup,
        active: result.data.active,
        avatarUrl: result.data.avatarUrl,
        createdAt: result.data.createdAt,
        updatedAt: result.data.updatedAt
      }
      
      // 更新本地存储
      localStorage.setItem('user', JSON.stringify(user.value))
      
      console.log('用户信息刷新成功:', user.value.username, user.value.avatarUrl)
      return true
    } else {
      throw new Error(result.message || '获取用户信息失败')
    }
  } catch (error) {
    console.error('刷新用户信息失败:', error)
    return false
  }
}

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
    securityAnswer,
    securityQuestion,
    
    // 方法
    setUser,
    clearUser,
    hasPermission,
    refreshUserInfo,
    debugAuthState,
    
    // 常量
    USER_GROUPS
  }
}

// 在开发环境下，添加全局调试函数
if (import.meta.env.DEV) {
  ;(window as any).debugAuth = () => {
    const { debugAuthState } = useAuth()
    return debugAuthState()
  }
  
  // 监控localStorage变化
  ;(window as any).monitorLocalStorage = () => {
    const originalRemoveItem = localStorage.removeItem
    localStorage.removeItem = function(key: string) {
      if (['token', 'refreshToken', 'user'].includes(key)) {
        console.error(`� 警告: ${key} 被删除！调用栈:`)
        console.trace()
      }
      return originalRemoveItem.call(this, key)
    }
    
    const originalClear = localStorage.clear
    localStorage.clear = function() {
      console.error('🚨 警告: localStorage被完全清除！调用栈:')
      console.trace()
      return originalClear.call(this)
    }
    
    console.log('📱 localStorage监控已启用')
  }
  
  console.log('�🔧 调试函数已添加:')
  console.log('  - window.debugAuth() - 检查认证状态')
  console.log('  - window.monitorLocalStorage() - 监控localStorage变化')
}
