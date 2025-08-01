import axios from 'axios'
import { createCacheInterceptor } from '@/utils/api-cache'
import { requestOptimizer } from '@/utils/request-optimizer'
import { setupApiMonitoring } from '@/utils/api-monitor'

/**
 * API基础配置模块
 * <p>
 * 提供全局的HTTP客户端配置，包括请求/响应拦截器、认证处理、错误处理等。
 * 基于axios创建统一的API实例，确保前后端通信的一致性和安全性。
 * <p>
 * <b>功能特性：</b>
 * - 统一的baseURL和超时配置
 * - 自动添加JWT认证头
 * - 全局错误处理和401状态码处理
 * - 自动清理过期token并重定向
 * 
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */

/**
 * 创建axios实例
 * 配置基础URL、超时时间和默认请求头
 */
const api = axios.create({
  baseURL: 'http://localhost:8080/api', // 后端API基础地址
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
 */
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
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
  invalidatePatterns: ['/api/articles', '/api/users', '/api/drafts', '/api/comments']
})

api.interceptors.request.use(cacheInterceptor.request)

/**
 * 响应拦截器
 * 统一处理响应错误，特别是认证失败的情况
 */
api.interceptors.response.use(
  (response) => {
    // 应用缓存响应拦截器
    return cacheInterceptor.response(response)
  },
  (error) => {
    console.error('API Error:', error)
    
    // 处理401未授权错误：token过期或无效
    if (error.response?.status === 401) {
      // 清除本地存储的认证信息
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      // 刷新页面以重定向到登录状态
      window.location.reload()
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
