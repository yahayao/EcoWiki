/**
 * API基础配置模块
 * 
 * 功能：
 * - 提供全局HTTP客户端配置和统一的API实例
 * - 实现请求/响应拦截器和认证处理
 * - 支持自动token刷新和错误处理机制
 * - 集成API缓存、监控和请求优化功能
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
import axios from 'axios'
import { createCacheInterceptor } from '@/utils/api-cache'
import { requestOptimizer } from '@/utils/request-optimizer'
import { setupApiMonitoring } from '@/utils/api-monitor'

// ── snake_case ↔ camelCase 转换工具 ──────────────────────────────────────────
function toCamel(s: string): string {
  return s.replace(/_([a-z])/g, (_, c) => c.toUpperCase())
}
function toSnake(s: string): string {
  return s.replace(/([A-Z])/g, c => `_${c.toLowerCase()}`)
}
function transformKeys(obj: any, fn: (k: string) => string): any {
  if (Array.isArray(obj)) return obj.map(v => transformKeys(v, fn))
  if (obj !== null && typeof obj === 'object' && !(obj instanceof File) && !(obj instanceof FormData) && !(obj instanceof Blob)) {
    return Object.fromEntries(Object.entries(obj).map(([k, v]) => [fn(k), transformKeys(v, fn)]))
  }
  return obj
}
export const camelizeKeys = (obj: any) => transformKeys(obj, toCamel)
export const snakerizeKeys = (obj: any) => transformKeys(obj, toSnake)


// token刷新相关状态
let isRefreshing = false
let failedQueue: Array<{
  resolve: (value: any) => void
  reject: (reason: any) => void
  config: any
}> = []

/**
 * 处理失败队列
 * @param error 错误信息
 * @param token 新token（如果刷新成功）
 */
const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach(({ resolve, reject, config }) => {
    if (error) {
      reject(error)
    } else {
      config.headers.Authorization = `Bearer ${token}`
      resolve(api(config))
    }
  })
  
  failedQueue = []
}

/**
 * 创建axios实例
 * 配置基础URL、超时时间和默认请求头
 */
const api = axios.create({
  baseURL: 'http://localhost:8080', // 后端服务器地址，不包含/api因为后端已设置context-path
  timeout: 15000, // 增加请求超时时间：15秒
  headers: {
    'Content-Type': 'application/json',
  },
  // 性能优化配置
  maxRedirects: 3, // 最大重定向次数
  maxContentLength: 50000000, // 50MB 最大内容长度
  maxBodyLength: 50000000,   // 50MB 最大请求体长度
})

/**
 * 请求拦截器
 * 在每个请求发送前自动添加JWT认证头和请求去重
 * 同时将请求 body 中的 camelCase 字段转为 snake_case
 */
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // camelCase → snake_case（仅 JSON 请求体）
    if (config.data && !(config.data instanceof FormData) && !(config.data instanceof Blob)) {
      config.data = snakerizeKeys(config.data)
    }
    // 查询参数同样转换
    if (config.params) {
      config.params = snakerizeKeys(config.params)
    }
    
    // 添加请求时间戳以便去重分析
    ;(config as any).metadata = {
      startTime: Date.now(),
      requestId: Math.random().toString(36).substr(2, 9)
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 添加缓存拦截器 - 优化配置以减少API调用
const cacheInterceptor = createCacheInterceptor({
  ttl: 15 * 60 * 1000, // 增加到15分钟缓存
  storage: 'memory',
  shouldCache: (response) => {
    const url = response.config.url || ''
    const method = response.config.method?.toUpperCase()
    
    // 只缓存 GET 请求且状态码为 200-299 的响应
    if (method !== 'GET' || response.status < 200 || response.status >= 300) {
      return false
    }
    
    // 特殊处理：不同类型的数据使用不同的缓存策略
    if (url.includes('/articles/') && !url.includes('/comments')) {
      return true // 文章内容缓存
    }
    if (url.includes('/categories') || url.includes('/tags')) {
      return true // 分类和标签长期缓存
    }
    if (url.includes('/users/profile') || url.includes('/users/info')) {
      return true // 用户信息缓存
    }
    
    return response.status >= 200 && response.status < 300
  },
  keyGenerator: (config) => {
    // 自定义缓存键生成，忽略时间戳等动态参数
    const { method = 'GET', url = '', params } = config
    const filteredParams = params ? Object.keys(params)
      .filter(key => !['_t', 'timestamp', 'cache', 'v'].includes(key))
      .reduce((obj, key) => ({ ...obj, [key]: params[key] }), {}) : {}
    
    return `api_${method}_${url}_${JSON.stringify(filteredParams)}`
  },
  invalidatePatterns: ['/articles', '/users', '/drafts', '/comments']
})

api.interceptors.request.use(cacheInterceptor.request)

/**
 * 响应拦截器
 * 统一处理响应错误，包括token自动刷新和认证失败处理
 * 同时将响应数据的 snake_case 字段转为 camelCase
 */
api.interceptors.response.use(
  (response) => {
    // snake_case → camelCase 响应数据转换
    if (response.data) {
      response.data = camelizeKeys(response.data)
    }
    // 应用缓存响应拦截器
    return cacheInterceptor.response(response)
  },
  async (error) => {
    const originalRequest = error.config
    
    console.error('API Error:', error)
    
    // 处理401未授权错误：token过期或无效
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // 如果正在刷新token，将请求加入队列
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject, config: originalRequest })
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const refreshToken = localStorage.getItem('refreshToken')
        
        if (!refreshToken) {
          console.warn('尝试刷新token但未找到refresh token')
          throw new Error('No refresh token available')
        }

        console.log('开始刷新token...')
        // 使用单独的axios实例来避免拦截器循环
        const refreshResponse = await axios.post('http://localhost:8080/api/auth/refresh', {
          refreshToken
        }, {
          headers: {
            'Content-Type': 'application/json',
          }
        })

        if (refreshResponse.data.code === 200 && refreshResponse.data.data) {
          const { token: newToken, refreshToken: newRefreshToken } = refreshResponse.data.data
          
          console.log('Token刷新成功')
          // 更新本地存储
          localStorage.setItem('token', newToken)
          localStorage.setItem('refreshToken', newRefreshToken)
          
          // 更新请求头
          originalRequest.headers.Authorization = `Bearer ${newToken}`
          
          // 处理队列中的请求
          processQueue(null, newToken)
          
          // 重试原始请求
          return api(originalRequest)
        } else {
          throw new Error('Token refresh failed')
        }
      } catch (refreshError) {
        console.error('Token refresh failed:', refreshError)
        console.error('原始错误URL:', originalRequest.url)
        
        // 处理队列中的请求（失败）
        processQueue(refreshError, null)
        
        // 只有在确实是认证失败时才清除数据和刷新页面
        if ((refreshError as any)?.message?.includes('No refresh token available') || 
            (refreshError as any)?.response?.status === 401) {
          
          console.warn('清除认证数据并准备重新登录')
          // 清除本地存储的认证信息
          localStorage.removeItem('token')
          localStorage.removeItem('refreshToken')
          localStorage.removeItem('user')
          
          // 延迟刷新页面，给用户看到错误信息的机会
          setTimeout(() => {
            window.location.reload()
          }, 100000)
        }
        
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }
    
    return Promise.reject(error.response?.data || error)
  }
)

// 设置API监控
setupApiMonitoring(api)

export { api }

// 导出各模块API
export { articleApi } from './article'
export { userApi } from './user' 
export { TagApi } from './tag'
export { messageApi } from './message'
export { commentApi } from './comment'
export { draftApi } from './draft'

// 导出主要类型接口
export type { Article, ArticleVersion, CreateVersionRequest } from './article'
export type { Comment, Reply, CreateCommentRequest } from './comment'
export type { ArticleDraft, ReviewDraftRequest, DraftSubmissionResult } from './draft'
