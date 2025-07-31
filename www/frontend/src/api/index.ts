import axios from 'axios'

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
  timeout: 10000, // 请求超时时间：10秒
  headers: {
    'Content-Type': 'application/json',
  },
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

/**
 * 响应拦截器
 * 统一处理响应错误，特别是认证失败的情况
 */
api.interceptors.response.use(
  (response) => {
    return response
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
