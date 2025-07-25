/**
 * 用户与权限管理API模块
 * 
 * 这个模块提供了EcoWiki系统中所有与用户、角色、权限相关的API接口封装。
 * 包含完整的用户生命周期管理、角色权限体系和系统统计功能。
 * 
 * 主要功能模块：
 * 1. 用户认证系统 - 登录、注册、令牌验证
 * 2. 管理员用户管理 - 用户列表、权限分配、状态管理
 * 3. 角色权限系统 - 角色CRUD、权限分配、关联管理
 * 4. 系统统计信息 - 用户数量、活跃度等数据
 * 
 * API设计特点：
 * - 统一的错误处理和响应格式
 * - 自动令牌注入和刷新
 * - 类型安全的TypeScript接口
 * - 模块化的API组织结构
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0 - 添加角色权限分离功能
 * @since 2024-04-01
 * @lastModified 2025-07-03 - 分离角色权限API逻辑
 */

// === 依赖导入 ===
import axios from 'axios'  // HTTP客户端
import { api } from './index'  // 通用API实例
import type {
  PermissionGroup,
  Permission,
  PermissionGroupForm,
  PermissionForm,
  Role,
  RoleForm,
  RolePermission
} from '@/types/permission'  // 权限相关类型定义

// === API配置 ===

/** 后端API服务的基础地址 */
const API_BASE_URL = 'http://localhost:8080/api'

/**
 * 独立的API客户端实例
 * 专门用于用户权限相关的API调用，提供独立的配置和拦截器
 */
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,  // 10秒超时
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

// === HTTP拦截器配置 ===

/**
 * 请求拦截器
 * 自动为所有请求添加JWT认证头
 */
apiClient.interceptors.request.use(
  (config) => {
    // 从本地存储获取JWT令牌
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 统一处理API响应和错误状态
 */
apiClient.interceptors.response.use(
  (response) => {
    // 成功响应直接返回
    return response
  },
  (error) => {
    // 统一错误处理
    if (error.response) {
      // 服务器返回错误状态码
      const { status, data } = error.response

      switch (status) {
        case 401:
          // 未授权，清除本地令牌并跳转到登录页
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          // 可以在这里触发全局事件或路由跳转
          break
        case 403:
          console.error('权限不足:', data.message || '无权限访问')
          break
        case 404:
          console.error('资源不存在:', data.message || '请求的资源未找到')
          break
        case 500:
          console.error('服务器内部错误:', data.message || '服务器发生错误')
          break
        default:
          console.error('API错误:', data.message || `请求失败 (${status})`)
      }
    } else if (error.request) {
      // 网络错误
      console.error('网络连接错误:', error.message)
    } else {
      // 其他错误
      console.error('请求配置错误:', error.message)
    }

    return Promise.reject(error)
  }
)

// === 类型定义 ===

/**
 * 用户组类型定义
 * 支持动态角色扩展，允许添加新的用户角色
 */
export type UserGroup = string

/**
 * 预定义的用户角色常量
 * 系统内置的标准用户角色定义
 */
export const USER_GROUPS = {
  /** 普通用户 - 基础权限 */
  USER: 'user',
  /** 版主 - 内容管理权限 */
  MODERATOR: 'moderator',
  /** 管理员 - 系统管理权限 */
  ADMIN: 'admin',
  /** 超级管理员 - 所有权限 */
  SUPER_ADMIN: 'superadmin'
} as const

// === 请求/响应接口定义 ===

/**
 * 用户登录请求接口
 * 支持用户名或邮箱登录
 */
export interface LoginRequest {
  /** 用户名（与邮箱二选一） */
  username?: string
  /** 邮箱（与用户名二选一） */
  email?: string
  /** 用户密码 */
  password: string
  /** 是否记住登录状态 */
  rememberMe?: boolean
}
/**
 * 用户密码重置请求接口
 * 用于忘记密码时的重置流程
 */
export interface ResetPasswordRequest {
  /** 用户名（与邮箱二选一） */
  username?: string
  /** 邮箱（与用户名二选一） */
  email?: string
  /** 新密码 */
  newPassword: string
  /** 确认新密码（前端验证用，后端可忽略） */
  confirmPassword?: string
  /** 安全问题答案（如果有） */
  answer?: string
}
/**
 * 用户注册请求接口
 * 创建新用户账户所需的信息
 */
export interface RegisterRequest {
  /** 用户名（唯一） */
  username: string
  /** 邮箱地址（唯一） */
  email: string
  /** 登录密码 */
  password: string
  /** 确认密码（前端验证用，后端可忽略） */
  confirmPassword?: string
  /** 用户全名（显示名称） */
  fullName?: string
}

/**
 * 用户信息响应接口
 * 从后端获取的用户详细信息
 */
export interface UserResponse {
  /** 用户唯一标识符 */
  userId: number
  /** 用户名 */
  username: string
  /** 邮箱地址 */
  email: string
  /** 用户全名 */
  fullName?: string
  /** 用户角色/权限组 */
  userGroup: UserGroup
  /** 账户是否激活 */
  active: boolean
  /** 用户头像URL */
  avatarUrl?: string
  /** 账户创建时间 */
  createdAt: string
  /** 最后更新时间 */
  updatedAt: string
  /** 安全问题（如果设置） */
  securityQuestion?: string
  /** 安全问题答案（如果设置） */
  securityAnswer?: string
}

/**
 * 角色信息响应接口
 * 系统角色的详细信息
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

// === 核心API接口定义 ===

/**
 * 管理员API接口集合
 * 
 * 提供完整的后台管理功能API，包含用户管理、角色管理、系统统计等核心功能。
 * 所有接口都要求管理员权限，会自动携带JWT令牌进行身份验证。
 * 
 * API端点说明：
 * - 用户管理：/admin/users - 获取、更新用户信息和状态
 * - 角色管理：/admin/roles - 获取角色列表和详情
 * - 系统统计：/admin/stats - 获取系统运行统计数据
 * 
 * 特殊说明：
 * - getRoles(): 调用 /admin/roles，返回角色名称数组 string[]
 *   主要用于：用户管理页面的角色下拉选择器
 * - getRolesDetails(): 调用 /admin/roles/details，返回完整角色对象 Role[]
 *   主要用于：角色管理页面的详细信息展示和编辑
 * 
 * @since 2024-04-01
 * @version 1.2.0
 */
export const adminApi = {
  /**
   * 获取用户列表（支持分页和排序）
   * 
   * 获取系统中所有用户的分页列表，支持多种排序方式。
   * 主要用于管理后台的用户管理页面。
   * 
   * @param page 页码，从0开始（默认0）
   * @param size 每页显示数量（默认10）
   * @param sortBy 排序字段（默认'userId'，可选：username, email, createdAt等）
   * @param sortDir 排序方向（默认'desc'，可选：asc/desc）
   * @returns Promise<ApiResponse<PagedUsers>> 分页用户数据
   * @throws Error 当网络错误或权限不足时抛出异常
   * 
   * @example
   * ```typescript
   * // 获取第一页用户列表
   * const users = await adminApi.getUsers(0, 20, 'createdAt', 'desc')
   * ```
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
   * 获取所有启用的用户（用于联系人选择）
   * 
   * 获取系统中所有处于激活状态的用户列表，主要用于消息发送时的联系人选择功能。
   * 返回精简的用户信息，包含ID、用户名、邮箱、头像等基本信息。
   * 
   * @returns Promise<UserContact[]> 启用用户列表
   * @throws Error 当网络错误或权限不足时抛出异常
   * 
   * @example
   * ```typescript
   * const activeUsers = await adminApi.getAllActiveUsers()
   * // 返回: [{ userId: 1, username: 'admin', email: 'admin@example.com', active: true }]
   * ```
   */
  getAllActiveUsers: async () => {
    try {
      const response = await api.get('/admin/users/active')
      if (response.data && response.data.code === 200 && response.data.data) {
        return response.data.data
      } else {
        throw new Error(response.data?.message || '获取启用用户列表失败')
      }
    } catch (error: any) {
      console.error('获取启用用户列表失败:', error)
      throw new Error(error.response?.data?.message || error.message || '获取启用用户列表失败')
    }
  },

  /**
   * 更新用户权限组
   * 
   * 修改指定用户的角色权限组，这是用户权限管理的核心功能。
   * 支持将用户提升为管理员或降级为普通用户。
   * 
   * @param userId 目标用户的唯一标识符
   * @param userGroup 新的用户组角色（user/moderator/admin/superadmin）
   * @returns Promise<ApiResponse<UserResponse>> 更新后的用户信息
   * @throws Error 当权限不足或用户不存在时抛出异常
   * 
   * @example
   * ```typescript
   * // 将用户提升为管理员
   * await adminApi.updateUserGroup(123, 'admin')
   * ```
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
   * 
   * 启用或禁用用户账户，禁用的用户将无法登录系统。
   * 这是用户账户管理的重要安全功能。
   * 
   * @param userId 目标用户的唯一标识符
   * @param active 账户状态（true=激活，false=禁用）
   * @returns Promise<ApiResponse<UserResponse>> 更新后的用户信息
   * @throws Error 当权限不足或用户不存在时抛出异常
   * 
   * @example
   * ```typescript
   * // 禁用用户账户
   * await adminApi.updateUserStatus(123, false)
   * ```
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
   * 
   * 获取系统运行的各项统计数据，用于管理后台的仪表盘展示。
   * 包含用户数量、活跃度、增长趋势等关键指标。
   * 
   * @returns Promise<ApiResponse<SystemStats>> 系统统计数据
   * @throws Error 当权限不足或服务器错误时抛出异常
   * 
   * @example
   * ```typescript
   * const stats = await adminApi.getSystemStats()
   * console.log('总用户数:', stats.data.totalUsers)
   * ```
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
   * 获取所有角色列表（简化版）
   * 
   * 获取系统中所有角色的名称列表，返回字符串数组。
   * 主要用于用户管理页面的角色下拉选择器。
   * 
   * @returns Promise<ApiResponse<string[]>> 角色名称数组
   * @throws Error 当权限不足或服务器错误时抛出异常
   * 
   * @example
   * ```typescript
   * const roles = await adminApi.getRoles()
   * // 返回: { code: 200, data: ['user', 'admin', 'moderator'] }
   * ```
   */
  getRoles: async () => {
    try {
      const response = await api.get('/admin/roles')
      // 后端返回的是 ApiResponse<List<String>> 格式
      return response.data
    } catch (error: any) {
      console.error('获取角色列表失败:', error)
      throw new Error(error.response?.data?.message || error.message || '获取角色列表失败')
    }
  },

  /**
   * 获取角色详细信息（完整版）
   * 
   * 获取系统中所有角色的完整信息，包含角色ID、名称、描述、时间戳等。
   * 主要用于角色管理页面的详细信息展示和编辑功能。
   * 
   * @returns Promise<Role[]> 角色详细信息数组
   * @throws Error 当权限不足或服务器错误时抛出异常
   * 
   * @example
   * ```typescript
   * const roleDetails = await adminApi.getRolesDetails()
   * // 返回: [{ roleId: 1, roleName: 'admin', description: '管理员', ... }]
   * ```
   */
  getRolesDetails: async () => {
    try {
      const response = await api.get('/admin/roles/details')
      // 后端返回的是 ApiResponse<List<Role>> 格式
      if (response.data && response.data.code === 200 && response.data.data) {
        return response.data.data
      } else {
        throw new Error(response.data?.message || '获取角色详情失败')
      }
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
      // 后端返回的是 ApiResponse<Role> 格式
      if (response.data && response.data.code === 200) {
        return response.data.data
      } else {
        throw new Error(response.data?.message || '创建角色失败')
      }
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
      // 后端返回的是 ApiResponse<Role> 格式
      if (response.data && response.data.code === 200) {
        return response.data.data
      } else {
        throw new Error(response.data?.message || '更新角色失败')
      }
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
      // 后端返回的是 ApiResponse<String> 格式
      if (response.data && response.data.code === 200) {
        return response.data
      } else {
        throw new Error(response.data?.message || '删除角色失败')
      }
    } catch (error: any) {
      console.error('删除角色失败:', error)
      throw new Error(error.response?.data?.message || error.message || '删除角色失败')
    }
  },

  /**
   * 删除用户（软删除，设置为非激活状态）
   * @param userId 用户ID
   * @returns 删除结果
   */
  deleteUser: async (userId: number) => {
    try {
      const response = await api.delete(`/admin/users/${userId}`)
      return response.data
    } catch (error: any) {
      console.error('禁用用户失败:', error)
      throw new Error(error.response?.data?.message || error.message || '禁用用户失败')
    }
  },

  /**
   * 恢复用户（重新激活）
   * @param userId 用户ID
   * @returns 恢复结果
   */
  restoreUser: async (userId: number) => {
    try {
      const response = await api.put(`/admin/users/${userId}/restore`)
      return response.data
    } catch (error: any) {
      console.error('恢复用户失败:', error)
      throw new Error(error.response?.data?.message || error.message || '恢复用户失败')
    }
  },

  /**
   * 获取所有权限
   * @returns 权限列表
   */
  getAllPermissions: async () => {
    try {
      const response = await api.get('/admin/permissions')
      // 后端返回的是 ApiResponse<List<Permission>> 格式
      if (response.data && response.data.code === 200 && response.data.data) {
        return response.data.data
      } else {
        throw new Error(response.data?.message || '获取权限列表失败')
      }
    } catch (error: any) {
      console.error('获取权限列表失败:', error)
      throw new Error(error.response?.data?.message || error.message || '获取权限列表失败')
    }
  },

  /**
   * 创建权限
   * @param permissionData 权限数据
   * @returns 创建的权限信息
   */
  createPermission: async (permissionData: { permissionName: string; description?: string }) => {
    try {
      const response = await api.post('/admin/permissions', permissionData)
      // 后端返回的是 ApiResponse<Permission> 格式
      if (response.data && response.data.code === 200) {
        return response.data.data
      } else {
        throw new Error(response.data?.message || '创建权限失败')
      }
    } catch (error: any) {
      console.error('创建权限失败:', error)
      throw new Error(error.response?.data?.message || error.message || '创建权限失败')
    }
  },

  /**
   * 更新权限
   * @param permissionId 权限ID
   * @param permissionData 更新数据
   * @returns 更新后的权限信息
   */
  updatePermission: async (permissionId: number, permissionData: { permissionName?: string; description?: string }) => {
    try {
      const response = await api.put(`/admin/permissions/${permissionId}`, permissionData)
      // 后端返回的是 ApiResponse<Permission> 格式
      if (response.data && response.data.code === 200) {
        return response.data.data
      } else {
        throw new Error(response.data?.message || '更新权限失败')
      }
    } catch (error: any) {
      console.error('更新权限失败:', error)
      throw new Error(error.response?.data?.message || error.message || '更新权限失败')
    }
  },

  /**
   * 删除权限
   * @param permissionId 权限ID
   * @returns 删除结果
   */
  deletePermission: async (permissionId: number) => {
    try {
      const response = await api.delete(`/admin/permissions/${permissionId}`)
      // 后端返回的是 ApiResponse<String> 格式
      if (response.data && response.data.code === 200) {
        return response.data
      } else {
        throw new Error(response.data?.message || '删除权限失败')
      }
    } catch (error: any) {
      console.error('删除权限失败:', error)
      throw new Error(error.response?.data?.message || error.message || '删除权限失败')
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
   * 用户密码重置
   * @param data 密码重置请求数据
   * @returns 认证响应数据
   */
  resetPassword: async (data: ResetPasswordRequest): Promise<AuthResponse> => {
    try {
      console.log('发送密码重置请求:', {
        email: data.email,
        newPassword: data.newPassword,
        confirmPassword: data.confirmPassword
      })
      const response = await api.post('/auth/reset-password', {
        username: data.username,
        email: data.email,
        newPassword: data.newPassword,
        securityAnswer: data.answer
      })

      if (response.data.code === 200 && response.data.data) {
        const authData = response.data.data
        return {
          user: authData.user,
          token: authData.token,
          refreshToken: authData.refreshToken
        }
      }

      throw new Error(response.data.message || '密码重置失败')
    } catch (error: any) {
      console.error('密码重置失败:', error)

      if (error.response?.data?.message) {
        throw new Error(error.response.data.message)
      } else if (error.message) {
        throw new Error(error.message)
      } else {
        throw new Error('密码重置失败，请检查网络连接')
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
  },

  /**
   * 更新个人资料
   * @param profileData 要更新的资料数据
   * @returns 更新后的用户信息
   */
  updateProfile: async (profileData: {
    username?: string
    fullName?: string
    email?: string
  }): Promise<UserResponse> => {
    try {
      const response = await api.put('/auth/profile', profileData)

      if (response.data.code === 200 && response.data.data) {
        return response.data.data
      }

      throw new Error(response.data.message || '更新个人资料失败')
    } catch (error: any) {
      console.error('更新个人资料失败:', error)
      throw new Error(error.response?.data?.message || error.message || '更新个人资料失败')
    }
  },

  /**
   * 更换用户头像
   * @param avatarFile 头像文件
   * @returns 新的头像URL
   */
  updateAvatar: async (avatarFile: File): Promise<string> => {
    try {
      const formData = new FormData()
      formData.append('avatar', avatarFile)
      console.log('发送头像更新请求:', {
        fileName: avatarFile.name,
        fileSize: avatarFile.size,
        fileType: avatarFile.type
      })
      const response = await api.post('/profile/avatar', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })

      if (response.data.code === 200 && response.data.data) {
        return response.data.data.avatarUrl
      }

      throw new Error(response.data.message || '更换头像失败')
    } catch (error: any) {
      console.error('更换头像失败:', error)
      throw new Error(error.response?.data?.message || error.message || '更换头像失败')
    }
  },

  /**
   * 修改密码
   * @param passwordData 密码修改数据
   * @returns 修改结果
   */
  changePassword: async (passwordData: {
    currentPassword: string
    newPassword: string
    confirmPassword: string
  }): Promise<void> => {
    try {
      const response = await api.post('/auth/change-password', passwordData)

      if (response.data.code !== 200) {
        throw new Error(response.data.message || '修改密码失败')
      }
    } catch (error: any) {
      console.error('修改密码失败:', error)
      throw new Error(error.response?.data?.message || error.message || '修改密码失败')
    }
  },

  /**
   * 更新安全设置
   * @param securityData 安全设置数据
   * @returns 更新结果
   */
  updateSecuritySettings: async (securityData: {
    securityQuestion?: string
    securityAnswer?: string
  }): Promise<void> => {
    try {
      const response = await api.put('/auth/security', securityData)

      if (response.data.code !== 200) {
        throw new Error(response.data.message || '更新安全设置失败')
      }
    } catch (error: any) {
      console.error('更新安全设置失败:', error)
      throw new Error(error.response?.data?.message || error.message || '更新安全设置失败')
    }
  },

  /**
   * 获取用户文章统计
   * @returns 用户文章统计数据
   */
  getUserArticleStats: async (): Promise<{
    totalArticles: number
    publishedArticles: number
    draftArticles: number
    favoriteArticles: number
    totalViews: number
    totalLikes: number
  }> => {
    try {
      const response = await api.get('/auth/article-stats')

      if (response.data.code === 200 && response.data.data) {
        return response.data.data
      }

      throw new Error(response.data.message || '获取文章统计失败')
    } catch (error: any) {
      console.error('获取文章统计失败:', error)
      throw new Error(error.response?.data?.message || error.message || '获取文章统计失败')
    }
  },

  /**
   * 获取用户收藏文章列表
   * @param page 页码（从0开始）
   * @param size 每页大小
   * @returns 收藏文章列表
   */
  getFavoriteArticles: async (page: number = 0, size: number = 10): Promise<{
    content: any[]
    totalElements: number
    totalPages: number
    size: number
    number: number
  }> => {
    try {
      const response = await api.get('/auth/favorite-articles', {
        params: { page, size }
      })

      if (response.data.code === 200 && response.data.data) {
        return response.data.data
      }

      throw new Error(response.data.message || '获取收藏文章失败')
    } catch (error: any) {
      console.error('获取收藏文章失败:', error)
      throw new Error(error.response?.data?.message || error.message || '获取收藏文章失败')
    }
  },

  /**
   * 获取用户创建的文章列表
   * @param page 页码（从0开始）
   * @param size 每页大小
   * @param status 文章状态过滤（可选）
   * @returns 用户文章列表
   */
  getUserArticles: async (page: number = 0, size: number = 10, status?: 'published' | 'draft'): Promise<{
    content: any[]
    totalElements: number
    totalPages: number
    size: number
    number: number
  }> => {
    try {
      const params: any = { page, size }
      if (status) {
        params.status = status
      }

      const response = await api.get('/auth/my-articles', {
        params
      })

      if (response.data.code === 200 && response.data.data) {
        return response.data.data
      }

      throw new Error(response.data.message || '获取用户文章失败')
    } catch (error: any) {
      console.error('获取用户文章失败:', error)
      throw new Error(error.response?.data?.message || error.message || '获取用户文章失败')
    }
  },

  /**
   * 获取用户贡献统计
   * @returns 用户贡献统计数据
   */
  getUserContributions: async (): Promise<{
    createdPages: number
    editCount: number
    points: number
    createdThisMonth: number
    editsThisMonth: number
    pointsThisMonth: number
    achievements: any[]
    contributionCalendar: any[]
  }> => {
    try {
      const response = await api.get('/auth/contributions')

      if (response.data.code === 200 && response.data.data) {
        return response.data.data
      }

      throw new Error(response.data.message || '获取贡献统计失败')
    } catch (error: any) {
      console.error('获取贡献统计失败:', error)
      throw new Error(error.response?.data?.message || error.message || '获取贡献统计失败')
    }
  }
}

/**
 * 权限分组管理API
 */
export const permissionGroupApi = {
  /**
   * 获取所有权限分组及其权限
   */
  async getAllPermissionGroups(): Promise<PermissionGroup[]> {
    const response = await apiClient.get('/admin/permission-groups')
    return response.data
  },

  /**
   * 根据ID获取权限分组
   */
  async getPermissionGroupById(groupId: number): Promise<PermissionGroup> {
    const response = await apiClient.get(`/admin/permission-groups/${groupId}`)
    return response.data
  },

  /**
   * 创建权限分组
   */
  async createPermissionGroup(data: PermissionGroupForm): Promise<PermissionGroup> {
    const response = await apiClient.post('/admin/permission-groups', data)
    return response.data
  },

  /**
   * 更新权限分组
   */
  async updatePermissionGroup(groupId: number, data: PermissionGroupForm): Promise<PermissionGroup> {
    const response = await apiClient.put(`/admin/permission-groups/${groupId}`, data)
    return response.data
  },

  /**
   * 删除权限分组
   */
  async deletePermissionGroup(groupId: number): Promise<void> {
    await apiClient.delete(`/admin/permission-groups/${groupId}`)
  },

  /**
   * 获取分组下的所有权限
   */
  async getPermissionsByGroupId(groupId: number): Promise<Permission[]> {
    const response = await apiClient.get(`/admin/permission-groups/${groupId}/permissions`)
    return response.data
  },

  /**
   * 为分组添加权限
   */
  async addPermissionToGroup(groupId: number, data: PermissionForm): Promise<Permission> {
    const response = await apiClient.post(`/admin/permission-groups/${groupId}/permissions`, data)
    return response.data
  },

  /**
   * 批量更新分组内权限的排序
   */
  async updatePermissionsOrder(groupId: number, permissionIds: number[]): Promise<void> {
    await apiClient.put(`/admin/permission-groups/${groupId}/permissions/order`, permissionIds)
  }
}

// === 角色权限管理API ===

/**
 * 角色权限分配API接口集合
 * 
 * 专门为角色权限分配页面设计的API集合，与adminApi相区别：
 * - adminApi.getRoles(): 返回角色名称数组，用于用户管理页面的下拉选择
 * - rolePermissionApi.getRoles(): 返回完整角色对象，包含roleId等详细信息
 * 
 * 主要功能：
 * 1. 角色生命周期管理 - 创建、查询、更新、删除角色
 * 2. 权限分配管理 - 为角色分配权限、查询角色权限
 * 3. 权限系统查询 - 获取所有可用权限、权限分组
 * 
 * API设计原则：
 * - 职责单一：专注于角色权限管理功能
 * - 类型安全：所有方法都有明确的TypeScript类型
 * - 错误处理：统一的异常处理和错误提示
 * 
 * @since 2025-07-03 - 从adminApi中分离出来
 * @version 1.0.0
 */
export const rolePermissionApi = {
  /**
   * 获取所有角色（角色权限管理专用）
   * 
   * 获取系统中所有角色的完整信息，包含roleId、roleName、description等。
   * 与adminApi.getRoles()不同，此方法返回完整的角色对象，主要用于角色权限分配页面。
   * 
   * @returns Promise<{data: Role[]}> 包装格式的完整角色对象数组
   * @throws Error 当权限不足或服务器错误时抛出异常
   */
  async getRoles(): Promise<{ data: Role[] }> {
    try {
      const response = await apiClient.get('/admin/roles/details')
      if (response.data && response.data.code === 200 && response.data.data) {
        return { data: response.data.data }
      } else {
        throw new Error(response.data?.message || '获取角色列表失败')
      }
    } catch (error: any) {
      console.error('获取角色列表失败:', error)
      throw new Error(error.response?.data?.message || error.message || '获取角色列表失败')
    }
  },

  /**
   * 创建新角色
   * 
   * 在系统中创建一个新的角色，管理员可以定义角色名称和描述。
   * 创建后的角色可以被分配给用户，也可以配置相应的权限。
   * 
   * @param roleForm 角色表单数据，包含roleName和description
   * @returns Promise<Role> 创建成功的角色对象
   * @throws Error 当角色名重复、权限不足或服务器错误时抛出异常
   */
  async createRole(roleForm: RoleForm): Promise<Role> {
    const response = await apiClient.post('/admin/roles', roleForm)
    return response.data.data
  },

  /**
   * 更新现有角色
   * 
   * 修改指定角色的名称和描述信息。
   * 注意：系统内置角色（如admin、user）可能不允许修改某些属性。
   * 
   * @param roleId 要更新的角色ID
   * @param roleForm 新的角色信息
   * @returns Promise<Role> 更新后的角色对象
   * @throws Error 当角色不存在、权限不足或服务器错误时抛出异常
   */
  async updateRole(roleId: number, roleForm: RoleForm): Promise<Role> {
    const response = await apiClient.put(`/admin/roles/${roleId}`, roleForm)
    return response.data.data
  },

  /**
   * 删除角色
   * 
   * 从系统中永久删除指定的角色。
   * 注意：系统内置角色（admin、user等）不能删除，已分配给用户的角色删除前需要先解除关联。
   * 
   * @param roleId 要删除的角色ID
   * @returns Promise<void> 删除成功时无返回值
   * @throws Error 当角色不存在、仍有用户使用或权限不足时抛出异常
   */
  async deleteRole(roleId: number): Promise<void> {
    await apiClient.delete(`/admin/roles/${roleId}`)
  },

  /**
   * 获取角色的权限列表
   * 
   * 查询指定角色当前拥有的所有权限。
   * 返回的权限列表可用于权限分配页面的初始状态显示。
   * 
   * @param roleId 角色ID
   * @returns Promise<Permission[]> 角色拥有的权限列表
   * @throws Error 当角色不存在或权限不足时抛出异常
   */
  async getRolePermissions(roleId: number): Promise<Permission[]> {
    const response = await apiClient.get(`/admin/roles/${roleId}/permissions`)
    return response.data.data || []
  },

  /**
   * 更新角色的权限配置
   */
  async updateRolePermissions(roleId: number, permissionIds: number[]): Promise<void> {
    await apiClient.put(`/admin/roles/${roleId}/permissions`, { permissionIds })
  },

  /**
   * 分配权限给角色
   */
  async assignPermissions(roleId: number, permissionIds: number[]): Promise<void> {
    await this.updateRolePermissions(roleId, permissionIds)
  },

  /**
   * 获取所有权限
   * @returns 权限列表
   */
  async getAllPermissions(): Promise<Permission[]> {
    try {
      const response = await apiClient.get('/admin/permissions')
      if (response.data && response.data.code === 200 && response.data.data) {
        return response.data.data
      } else {
        throw new Error(response.data?.message || '获取权限列表失败')
      }
    } catch (error: any) {
      console.error('获取权限列表失败:', error)
      throw new Error(error.response?.data?.message || error.message || '获取权限列表失败')
    }
  },

  /**
   * 批量分配权限给多个角色
   */
  async batchAssignPermissions(assignments: { roleId: number, permissionIds: number[] }[]): Promise<void> {
    await apiClient.post('/admin/roles/batch-assign-permissions', { assignments })
  },

  /**
   * 获取所有角色权限关联
   */
  async getAllRolePermissions(): Promise<{ data: RolePermission[] }> {
    const response = await apiClient.get('/admin/role-permissions')
    return { data: response.data.data || [] }
  },

  /**
   * 获取权限的分配统计
   */
  async getPermissionAssignmentStats(): Promise<any> {
    const response = await apiClient.get('/admin/permissions/assignment-stats')
    return response.data
  }
}