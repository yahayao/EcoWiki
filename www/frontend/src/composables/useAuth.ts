import { ref, computed, readonly } from 'vue'
import type { UserResponse } from '../api/user'

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
    } catch (error) {
      console.error('恢复用户状态失败:', error)
      // 清除无效数据
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('refreshToken')
    }
  }
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
}

// 清除用户状态
const clearUser = () => {
  user.value = null
  token.value = null
  
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('refreshToken')
}

// 检查是否已登录
const isAuthenticated = computed(() => {
  return !!(user.value && token.value)
})

// 获取用户头像
const userAvatar = computed(() => {
  return user.value?.avatar || `https://ui-avatars.com/api/?name=${user.value?.username}&background=667eea&color=fff`
})

export const useAuth = () => {
  return {
    user: readonly(user),
    token: readonly(token),
    isAuthenticated,
    userAvatar,
    setUser,
    clearUser,
    initializeAuth
  }
}

// 在应用启动时初始化
initializeAuth()
