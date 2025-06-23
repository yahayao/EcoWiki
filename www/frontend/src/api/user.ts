import api from './index'

// 用户相关接口类型定义
export interface RegisterRequest {
  username: string
  email: string
  password: string
  fullName?: string  // 可选字段
  userGroup?: string // 可选字段，默认为 "user"
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
  timestamp?: number
}

// 用户API服务
export const userApi = {
  // 用户登录 - 暂未实现
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    try {
      // 暂时返回模拟数据，等后端实现
      throw new Error('登录功能暂未实现，请联系管理员')
    } catch (error) {
      throw error
    }
  },

  // 检查用户名是否可用
  checkUsername: async (username: string): Promise<boolean> => {
    try {
      const response = await api.get(`/auth/check-username?username=${username}`) as ApiResponse<{available: boolean}>
      if (response.code === 200 && response.data) {
        return response.data.available
      }
      throw new Error(response.message || '检查用户名失败')
    } catch (error) {
      console.error('检查用户名失败:', error)
      throw error
    }
  },

  // 检查邮箱是否可用
  checkEmail: async (email: string): Promise<boolean> => {
    try {
      const response = await api.get(`/auth/check-email?email=${email}`) as ApiResponse<{available: boolean}>
      if (response.code === 200 && response.data) {
        return response.data.available
      }
      throw new Error(response.message || '检查邮箱失败')
    } catch (error) {
      console.error('检查邮箱失败:', error)
      throw error
    }
  },

  // 用户注册
  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    try {
      const response = await api.post('/auth/register', {
        username: data.username,
        email: data.email,
        password: data.password,
        fullName: data.fullName || '', // 如果没有提供则为空字符串
        userGroup: data.userGroup || 'user' // 默认用户组
      }) as ApiResponse<{userId: number, username: string, email: string, fullName: string, userGroup: string, createdAt: string}>
      
      if (response.code === 200 && response.data) {
        // 构造前端期望的响应格式
        return {
          user: {
            id: response.data.userId,
            username: response.data.username,
            email: response.data.email,
            avatar: '', // 暂时为空
            createdAt: response.data.createdAt,
            updatedAt: response.data.createdAt // 创建时两个时间相同
          },
          token: `mock-token-${response.data.userId}`,
          refreshToken: `mock-refresh-token-${response.data.userId}`
        }
      }
      throw new Error(response.message || '注册失败')
    } catch (error) {
      console.error('注册失败:', error)
      throw error
    }
  },

  // 获取当前用户信息 - 暂未实现
  getCurrentUser: async (): Promise<UserResponse> => {
    try {
      throw new Error('获取用户信息功能暂未实现')
    } catch (error) {
      throw error
    }
  },

  // 用户登出
  logout: async (): Promise<void> => {
    try {
      // 暂时只清除本地存储，等后端实现登出接口
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
    } catch (error) {
      throw error
    }
  }
}

export default userApi
