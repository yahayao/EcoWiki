import { api } from './index'

// 修改用户组类型定义 - 改为字符串常量
export const USER_GROUPS = {
  USER: 'user',
  MODERATOR: 'moderator', 
  ADMIN: 'admin',
  SUPER_ADMIN: 'superadmin'
} as const

export type UserGroup = typeof USER_GROUPS[keyof typeof USER_GROUPS]

// 接口类型定义
export interface LoginRequest {
  username?: string
  email?: string
  password: string
  rememberMe?: boolean
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  confirmPassword?: string
  fullName?: string
}

export interface UserResponse {
  id: number
  username: string
  email: string
  fullName?: string
  userGroup: UserGroup  // 改为 userGroup
  active: boolean
  avatar?: string
  createdAt: string
  updatedAt: string
}

export interface AuthResponse {
  user: UserResponse
  token: string
  refreshToken: string
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 管理员API
export const adminApi = {
  // 获取用户列表
  getUsers: async (page = 0, size = 10, sortBy = 'id', sortDir = 'desc') => {
    try {
      const response = await api.get(`/admin/users?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`)
      return response.data
    } catch (error: any) {
      console.error('获取用户列表失败:', error)
      throw new Error(error.response?.data?.message || error.message || '获取用户列表失败')
    }
  },
  
  // 更新用户权限组 - 修改API路径
  updateUserGroup: async (userId: number, userGroup: UserGroup) => {
    try {
      const response = await api.put(`/admin/users/${userId}/group`, { userGroup })
      return response.data
    } catch (error: any) {
      console.error('更新用户权限失败:', error)
      throw new Error(error.response?.data?.message || error.message || '更新用户权限失败')
    }
  },
  
  // 更新用户状态
  updateUserStatus: async (userId: number, active: boolean) => {
    try {
      const response = await api.put(`/admin/users/${userId}/status`, { active })
      return response.data
    } catch (error: any) {
      console.error('更新用户状态失败:', error)
      throw new Error(error.response?.data?.message || error.message || '更新用户状态失败')
    }
  },
  
  // 获取系统统计
  getSystemStats: async () => {
    try {
      const response = await api.get('/admin/stats')
      return response.data
    } catch (error: any) {
      console.error('获取系统统计失败:', error)
      throw new Error(error.response?.data?.message || error.message || '获取系统统计失败')
    }
  },
  
  // 删除用户
  deleteUser: async (userId: number) => {
    try {
      const response = await api.delete(`/admin/users/${userId}`)
      return response.data
    } catch (error: any) {
      console.error('删除用户失败:', error)
      throw new Error(error.response?.data?.message || error.message || '删除用户失败')
    }
  }
}

// 用户API
export const userApi = {
  // 用户登录
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    try {
      console.log('发送登录请求:', { 
        username: data.username, 
        email: data.email, 
        rememberMe: data.rememberMe 
      })
      
      const response = await api.post('/auth/login', {
        username: data.username,
        email: data.email,
        password: data.password,
        rememberMe: data.rememberMe
      })
      
      if (response.data.code === 200 && response.data.data) {
        const authData = response.data.data
        return {
          user: authData.user,
          token: authData.token,
          refreshToken: authData.refreshToken
        }
      }
      
      throw new Error(response.data.message || '登录失败')
    } catch (error: any) {
      console.error('登录失败:', error)
      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message)
      } else if (error.message) {
        throw new Error(error.message)
      } else {
        throw new Error('登录失败，请检查网络连接')
      }
    }
  },

  // 用户注册
  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    try {
      console.log('发送注册请求:', { 
        username: data.username, 
        email: data.email, 
        fullName: data.fullName 
      })
      
      const response = await api.post('/auth/register', {
        username: data.username,
        email: data.email,
        password: data.password,
        fullName: data.fullName || ''
      })
      
      if (response.data.code === 200 && response.data.data) {
        const authData = response.data.data
        return {
          user: authData.user,
          token: authData.token,
          refreshToken: authData.refreshToken
        }
      }
      
      throw new Error(response.data.message || '注册失败')
    } catch (error: any) {
      console.error('注册失败:', error)
      
      if (error.response?.data?.message) {
        throw new Error(error.response.data.message)
      } else if (error.message) {
        throw new Error(error.message)
      } else {
        throw new Error('注册失败，请检查网络连接')
      }
    }
  },

  // 检查用户名是否可用
  checkUsername: async (username: string): Promise<boolean> => {
    try {
      const response = await api.get(`/auth/check-username?username=${encodeURIComponent(username)}`)
      
      if (response.data.code === 200 && response.data.data) {
        return response.data.data.available
      }
      
      throw new Error(response.data.message || '检查用户名失败')
    } catch (error: any) {
      console.error('检查用户名失败:', error)
      
      if (error.response?.status >= 500) {
        throw new Error('服务器错误，无法检查用户名')
      }
      
      throw new Error(error.response?.data?.message || error.message || '检查用户名失败')
    }
  },

  // 检查邮箱是否可用
  checkEmail: async (email: string): Promise<boolean> => {
    try {
      const response = await api.get(`/auth/check-email?email=${encodeURIComponent(email)}`)
      
      if (response.data.code === 200 && response.data.data) {
        return response.data.data.available
      }
      
      throw new Error(response.data.message || '检查邮箱失败')
    } catch (error: any) {
      console.error('检查邮箱失败:', error)
      
      if (error.response?.status >= 500) {
        throw new Error('服务器错误，无法检查邮箱')
      }
      
      throw new Error(error.response?.data?.message || error.message || '检查邮箱失败')
    }
  },

  // 获取当前用户信息
  getCurrentUser: async (): Promise<UserResponse> => {
    try {
      const response = await api.get('/auth/me')
      
      if (response.data.code === 200 && response.data.data) {
        return response.data.data
      }
      
      throw new Error(response.data.message || '获取用户信息失败')
    } catch (error: any) {
      console.error('获取用户信息失败:', error)
      throw new Error(error.response?.data?.message || error.message || '获取用户信息失败')
    }
  },

  // 用户登出
  logout: async (): Promise<void> => {
    try {
      try {
        await api.post('/auth/logout')
      } catch (error) {
        console.warn('后端登出接口调用失败，但仍清除本地数据')
      }
      
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
    } catch (error: any) {
      console.error('登出失败:', error)
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
      throw new Error('登出失败')
    }
  },

  // 刷新token
  refreshToken: async (refreshToken: string): Promise<{ token: string; refreshToken: string }> => {
    try {
      const response = await api.post('/auth/refresh', { refreshToken })
      
      if (response.data.code === 200 && response.data.data) {
        return {
          token: response.data.data.token,
          refreshToken: response.data.data.refreshToken
        }
      }
      
      throw new Error(response.data.message || '刷新token失败')
    } catch (error: any) {
      console.error('刷新token失败:', error)
      throw new Error(error.response?.data?.message || error.message || '刷新token失败')
    }
  },

  // 检查用户权限
  hasPermission: (user: UserResponse | null, requiredGroup: UserGroup): boolean => {
    if (!user) return false
    
    const groupOrder = {
      [USER_GROUPS.USER]: 0,
      [USER_GROUPS.MODERATOR]: 1,
      [USER_GROUPS.ADMIN]: 2,
      [USER_GROUPS.SUPER_ADMIN]: 3
    }
    
    return groupOrder[user.userGroup] >= groupOrder[requiredGroup]
  },
  
  // 检查是否为管理员
  isAdmin: (user: UserResponse | null): boolean => {
    return user?.userGroup === USER_GROUPS.ADMIN || user?.userGroup === USER_GROUPS.SUPER_ADMIN
  },
  
  // 检查是否为超级管理员
  isSuperAdmin: (user: UserResponse | null): boolean => {
    return user?.userGroup === USER_GROUPS.SUPER_ADMIN
  }
}

export default userApi
