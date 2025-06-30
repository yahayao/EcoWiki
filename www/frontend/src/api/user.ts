import axios from 'axios'
import { api } from './index'

/**
 * 用户相关API模块
 * <p>
 * 提供用户认证、管理、权限相关的API接口封装。
 * 包含登录、注册、用户管理、角色管理等功能的前端API调用。
 * <p>
 * <b>功能模块：</b>
 * - 用户认证（登录、注册、检查）
 * - 管理员用户管理（列表、权限、状态）
 * - 角色管理（查询、创建、更新、删除）
 * - 系统统计信息
 * 
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */

// 后端API基础地址
const API_BASE_URL = 'http://localhost:8080/api'

/**
 * 独立的API客户端实例
 * 用于需要特殊配置的请求
 */
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 自动添加认证头
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 用户组类型定义（支持动态角色扩展）
 */
export type UserGroup = string

/**
 * 预定义的用户角色常量
 */
export const USER_GROUPS = {
  USER: 'user',
  MODERATOR: 'moderator', 
  ADMIN: 'admin',
  SUPER_ADMIN: 'superadmin'
} as const

/**
 * 登录请求接口
 */
export interface LoginRequest {
  /** 用户名（可选） */
  username?: string
  /** 邮箱（可选） */
  email?: string
  /** 密码 */
  password: string
  /** 是否记住我 */
  rememberMe?: boolean
}

/**
 * 注册请求接口
 */
export interface RegisterRequest {
  /** 用户名 */
  username: string
  /** 邮箱 */
  email: string
  /** 密码 */
  password: string
  /** 确认密码（前端验证用） */
  confirmPassword?: string
  /** 全名（可选） */
  fullName?: string
}

/**
 * 用户响应接口
 */
export interface UserResponse {
  /** 用户ID */
  userId: number
  /** 用户名 */
  username: string
  /** 邮箱 */
  email: string
  /** 全名 */
  fullName?: string
  /** 用户组/角色 */
  userGroup: UserGroup
  /** 是否激活 */
  active: boolean
  /** 头像 */
  avatar?: string
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
}

/**
 * 角色信息接口
 */
export interface RoleResponse {
  /** 角色ID */
  roleId: number
  /** 角色名称 */
  roleName: string
  /** 角色描述 */
  description?: string
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
}

/**
 * 认证响应接口
 */
export interface AuthResponse {
  /** 用户信息 */
  user: UserResponse
  /** 访问令牌 */
  token: string
  /** 刷新令牌 */
  refreshToken: string
}

/**
 * 统一API响应格式
 */
export interface ApiResponse<T> {
  /** 状态码 */
  code: number
  /** 响应消息 */
  message: string
  /** 响应数据 */
  data: T
  /** 时间戳 */
  timestamp: number
}

/**
 * 管理员API接口集合
 */
export const adminApi = {
  /**
   * 获取用户列表（分页）
   * @param page 页码（默认0）
   * @param size 每页数量（默认10）
   * @param sortBy 排序字段（默认userId）
   * @param sortDir 排序方向（默认desc）
   * @returns 用户分页数据
   */
  getUsers: async (page = 0, size = 10, sortBy = 'userId', sortDir = 'desc') => {
    try {
      const response = await api.get(`/admin/users?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`)
      return response.data
    } catch (error: any) {
      console.error('获取用户列表失败:', error)
      throw new Error(error.response?.data?.message || error.message || '获取用户列表失败')
    }
  },
  
  /**
   * 更新用户权限组
   * @param userId 用户ID
   * @param userGroup 新的用户组
   * @returns 更新结果
   */
  updateUserGroup: async (userId: number, userGroup: UserGroup) => {
    try {
      const response = await api.put(`/admin/users/${userId}/group`, { userGroup })
      return response.data
    } catch (error: any) {
      console.error('更新用户权限失败:', error)
      throw new Error(error.response?.data?.message || error.message || '更新用户权限失败')
    }
  },
  
  /**
   * 更新用户激活状态
   * @param userId 用户ID
   * @param active 是否激活
   * @returns 更新结果
   */
  updateUserStatus: async (userId: number, active: boolean) => {
    try {
      const response = await api.put(`/admin/users/${userId}/status`, { active })
      return response.data
    } catch (error: any) {
      console.error('更新用户状态失败:', error)
      throw new Error(error.response?.data?.message || error.message || '更新用户状态失败')
    }
  },
  
  /**
   * 获取系统统计信息
   * @returns 系统统计数据
   */
  getSystemStats: async () => {
    try {
      const response = await api.get('/admin/stats')
      return response.data
    } catch (error: any) {
      console.error('获取系统统计失败:', error)
      throw new Error(error.response?.data?.message || error.message || '获取系统统计失败')
    }
  },
  
  /**
   * 获取所有角色列表
   * @returns 角色信息数组
   */
  getRoles: async () => {
    try {
      const response = await api.get('/admin/roles')
      return response.data
    } catch (error: any) {
      console.error('获取角色列表失败:', error)
      throw new Error(error.response?.data?.message || error.message || '获取角色列表失败')
    }
  },
  
  /**
   * 获取角色详细信息
   * @returns 角色详细信息数组
   */
  getRolesDetails: async () => {
    try {
      const response = await api.get('/admin/roles/details')
      return response.data
    } catch (error: any) {
      console.error('获取角色详情失败:', error)
      throw new Error(error.response?.data?.message || error.message || '获取角色详情失败')
    }
  },
  
  /**
   * 创建角色
   * @param roleData 角色数据
   * @returns 创建的角色信息
   */
  createRole: async (roleData: { roleName: string; description?: string }) => {
    try {
      const response = await api.post('/admin/roles', roleData)
      return response.data
    } catch (error: any) {
      console.error('创建角色失败:', error)
      throw new Error(error.response?.data?.message || error.message || '创建角色失败')
    }
  },
  
  /**
   * 更新角色
   * @param roleId 角色ID
   * @param roleData 更新数据
   * @returns 更新后的角色信息
   */
  updateRole: async (roleId: number, roleData: { roleName?: string; description?: string }) => {
    try {
      const response = await api.put(`/admin/roles/${roleId}`, roleData)
      return response.data
    } catch (error: any) {
      console.error('更新角色失败:', error)
      throw new Error(error.response?.data?.message || error.message || '更新角色失败')
    }
  },
  
  /**
   * 删除角色
   * @param roleId 角色ID
   * @returns 删除结果
   */
  deleteRole: async (roleId: number) => {
    try {
      const response = await api.delete(`/admin/roles/${roleId}`)
      return response.data
    } catch (error: any) {
      console.error('删除角色失败:', error)
      throw new Error(error.response?.data?.message || error.message || '删除角色失败')
    }
  },
  
  /**
   * 删除用户
   * @param userId 用户ID
   * @returns 删除结果
   */
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

/**
 * 用户API接口集合
 */
export const userApi = {
  /**
   * 用户登录
   * @param data 登录请求数据
   * @returns 认证响应数据
   */
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

  /**
   * 用户注册
   * @param data 注册请求数据
   * @returns 认证响应数据
   */
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

  /**
   * 检查用户名是否可用
   * @param username 用户名
   * @returns 是否可用
   */
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

  /**
   * 检查邮箱是否可用
   * @param email 邮箱地址
   * @returns 是否可用
   */
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

  /**
   * 获取当前用户信息
   * @returns 用户信息
   */
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

  /**
   * 用户登出
   */
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

  /**
   * 刷新token
   * @param refreshToken 刷新令牌
   * @returns 新的token和refreshToken
   */
  refreshToken: async (refreshToken: string): Promise<{ token: string; refreshToken: string }> => {
    try {
      const response = await api.post('/auth/refresh', { refreshToken })
      
      if (response.data.code === 200 && response.data.data) {
        return response.data.data
      }
      
      throw new Error(response.data.message || '刷新令牌失败')
    } catch (error: any) {
      console.error('刷新令牌失败:', error)
      throw new Error(error.response?.data?.message || error.message || '刷新令牌失败')
    }
  },

  /**
   * 检查用户是否为管理员
   * @param user 用户信息
   * @returns 是否为管理员
   */
  isAdmin: (user: UserResponse | null): boolean => {
    if (!user) return false
    return user.userGroup === USER_GROUPS.ADMIN || user.userGroup === USER_GROUPS.SUPER_ADMIN
  },

  /**
   * 检查用户是否为超级管理员
   * @param user 用户信息
   * @returns 是否为超级管理员
   */
  isSuperAdmin: (user: UserResponse | null): boolean => {
    if (!user) return false
    return user.userGroup === USER_GROUPS.SUPER_ADMIN
  }
}