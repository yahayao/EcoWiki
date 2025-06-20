import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 可以在这里添加token等认证信息
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

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    // 统一错误处理
    console.error('API请求错误:', error)
    
    if (error.response) {
      // 服务器返回了错误状态码
      const { status, data } = error.response
      
      switch (status) {
        case 400:
          throw new Error(data.message || '请求参数错误')
        case 401:
          throw new Error('未授权，请重新登录')
        case 403:
          throw new Error('权限不足')
        case 404:
          throw new Error('请求的资源不存在')
        case 409:
          throw new Error(data.message || '数据冲突')
        case 500:
          throw new Error('服务器内部错误')
        default:
          throw new Error(data.message || '网络请求失败')
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      throw new Error('网络连接失败，请检查网络设置')
    } else {
      // 请求配置出错
      throw new Error('请求配置错误')
    }
  }
)

export default api
