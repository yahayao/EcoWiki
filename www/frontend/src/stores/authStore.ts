/**
 * 用户认证状态管理 Pinia Store
 *
 * 管理用户登录状态、JWT令牌和权限验证。
 *
 * @author EcoWiki开发团队
 * @version 1.0.0
 */

import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { userApi, type UserResponse, USER_GROUPS, type UserGroup } from '../api/user'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserResponse | null>(null)
  const token = ref<string | null>(null)

  const isAuthenticated = computed(() => !!(user.value && token.value))
  const isLoggedIn = isAuthenticated
  const userAvatar = computed(() => {
    if (user.value?.avatarUrl) return user.value.avatarUrl
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(user.value?.username || 'User')}&background=667eea&color=fff&size=40`
  })
  const userDisplayName = computed(() => user.value?.username || '未知用户')
  const isAdmin = computed(() => userApi.isAdmin(user.value))
  const isSuperAdmin = computed(() => userApi.isSuperAdmin(user.value))

  const initializeAuth = () => {
    const savedToken = localStorage.getItem('token')
    const savedUser = localStorage.getItem('user')
    if (savedToken && savedUser) {
      try {
        token.value = savedToken
        user.value = JSON.parse(savedUser)
      } catch {
        localStorage.removeItem('user')
        user.value = null
      }
    }
  }

  const setUser = (userData: UserResponse, authToken: string, refreshToken?: string) => {
    user.value = userData
    token.value = authToken
    localStorage.setItem('token', authToken)
    localStorage.setItem('user', JSON.stringify(userData))
    if (refreshToken) localStorage.setItem('refreshToken', refreshToken)
  }

  const clearUser = (clearSavedCredentials = false) => {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('refreshToken')
    if (clearSavedCredentials) {
      localStorage.removeItem('rememberMe')
      localStorage.removeItem('savedLoginField')
      localStorage.removeItem('savedPassword')
    }
  }

  const hasPermission = (permission: UserGroup): boolean => {
    if (!user.value) return false
    if (userApi.isSuperAdmin(user.value)) return true
    if (userApi.isAdmin(user.value) && permission !== USER_GROUPS.SUPER_ADMIN) return true
    return user.value.userGroup === permission
  }

  const refreshUserInfo = async () => {
    if (!token.value) return false
    try {
      const userData = await userApi.getCurrentUser()
      user.value = userData
      localStorage.setItem('user', JSON.stringify(userData))
      return true
    } catch { return false }
  }

  initializeAuth()

  return {
    user, token,
    isAuthenticated, isLoggedIn, userAvatar, userDisplayName, isAdmin, isSuperAdmin,
    setUser, clearUser, hasPermission, refreshUserInfo, initializeAuth
  }
})
