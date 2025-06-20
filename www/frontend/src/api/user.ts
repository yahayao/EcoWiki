import api from './index'

// 用户相关接口类型定义
export interface RegisterRequest {
  username: string
  email: string
  password: string
}

export interface LoginRequest {
  username: string
  password: string
  rememberMe?: boolean
}

export interface UserResponse {
  id: number
  username: string
  email: string
  avatar?: string
  createdAt: string
  updatedAt: string
}

export interface AuthResponse {
  user: UserResponse
  token: string
  refreshToken?: string
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data?: T
}

// 用户API服务
export const userApi = {
  // 用户注册
  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    try {
      const response = await api.post('/auth/register', data) as ApiResponse<AuthResponse>
      if (response.code === 200 && response.data) {
        return response.data
      }
      throw new Error(response.message || '注册失败')
    } catch (error) {
      throw error
    }
  },

  // 用户登录
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    try {
      const response = await api.post('/auth/login', data) as ApiResponse<AuthResponse>
      if (response.code === 200 && response.data) {
        return response.data
      }
      throw new Error(response.message || '登录失败')
    } catch (error) {
      throw error
    }
  },

  // 检查用户名是否可用
  checkUsername: async (username: string): Promise<boolean> => {
    try {
      const response = await api.get(`/auth/check-username?username=${username}`) as ApiResponse<{ available: boolean }>
      if (response.code === 200 && response.data) {
        return response.data.available
      }
      return false
    } catch (error) {
      throw error
    }
  },

  // 检查邮箱是否可用
  checkEmail: async (email: string): Promise<boolean> => {
    try {
      const response = await api.get(`/auth/check-email?email=${email}`) as ApiResponse<{ available: boolean }>
      if (response.code === 200 && response.data) {
        return response.data.available
      }
      return false
    } catch (error) {
      throw error
    }
  },

  // 获取当前用户信息
  getCurrentUser: async (): Promise<UserResponse> => {
    try {
      const response = await api.get('/user/profile') as ApiResponse<UserResponse>
      if (response.code === 200 && response.data) {
        return response.data
      }
      throw new Error(response.message || '获取用户信息失败')
    } catch (error) {
      throw error
    }
  },

  // 用户登出
  logout: async (): Promise<void> => {
    try {
      await api.post('/auth/logout')
      // 清除本地存储的token
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
    } catch (error) {
      // 即使请求失败也要清除本地token
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
      throw error
    }
  }
}

export default userApi
