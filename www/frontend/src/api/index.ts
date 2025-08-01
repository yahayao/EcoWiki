import axios from 'axios'
import { createCacheInterceptor } from '@/utils/api-cache'

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
 * 在每个请求发送前自动添加JWT认证头
 */
api.interceptors.request.use(
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

// 添加缓存拦截器
const cacheInterceptor = createCacheInterceptor({
  ttl: 5 * 60 * 1000, // 5分钟缓存
  storage: 'memory',
  shouldCache: (response) => {
    // 只缓存 GET 请求且状态码为 200-299 的响应
    return response.status >= 200 && 
           response.status < 300 && 
           response.config.method?.toUpperCase() === 'GET'
  },
  invalidatePatterns: ['/api/articles', '/api/users', '/api/drafts']
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
