/**
 * API 缓存装饰器
 * 为 API 请求添加智能缓存功能
 */

import { cacheManager } from './cache-manager'
import type { AxiosResponse, AxiosRequestConfig, InternalAxiosRequestConfig } from 'axios'

interface CacheConfig {
  ttl?: number
  storage?: 'memory' | 'localStorage' | 'sessionStorage'
  keyGenerator?: (config: AxiosRequestConfig) => string
  shouldCache?: (response: AxiosResponse) => boolean
  invalidatePatterns?: string[]
}

interface CachedAxiosRequestConfig extends InternalAxiosRequestConfig {
  __cached?: boolean
  __cachedResponse?: AxiosResponse
}

/**
 * 生成缓存键
 */
function generateCacheKey(config: AxiosRequestConfig, keyGenerator?: (config: AxiosRequestConfig) => string): string {
  if (keyGenerator) {
    return keyGenerator(config)
  }
  
  const { method = 'GET', url = '', params, data } = config
  const paramStr = params ? JSON.stringify(params) : ''
  const dataStr = data ? JSON.stringify(data) : ''
  
  return `api_${method}_${url}_${paramStr}_${dataStr}`
}

/**
 * 检查是否应该缓存响应
 */
function shouldCacheResponse(response: AxiosResponse, shouldCache?: (response: AxiosResponse) => boolean): boolean {
  if (shouldCache) {
    return shouldCache(response)
  }
  
  // 默认只缓存成功的 GET 请求
  return response.status >= 200 && response.status < 300 && response.config.method?.toUpperCase() === 'GET'
}

/**
 * API 缓存拦截器工厂
 */
export function createCacheInterceptor(config: CacheConfig = {}) {
  const {
    ttl = 5 * 60 * 1000, // 5分钟
    storage = 'memory',
    keyGenerator,
    shouldCache,
    invalidatePatterns = []
  } = config

  return {
    request: (requestConfig: InternalAxiosRequestConfig) => {
      // 只对 GET 请求尝试从缓存获取
      if (requestConfig.method?.toUpperCase() === 'GET') {
        const cacheKey = generateCacheKey(requestConfig, keyGenerator)
        const cachedResponse = cacheManager.get<AxiosResponse>(cacheKey, storage)
        
        if (cachedResponse) {
          // 返回缓存的响应，但需要包装成 Promise
          const config = requestConfig as CachedAxiosRequestConfig
          config.__cached = true
          config.__cachedResponse = cachedResponse
          return requestConfig
        }
      }
      
      return requestConfig
    },

    response: (response: AxiosResponse) => {
      // 如果是从缓存返回的响应
      const config = response.config as CachedAxiosRequestConfig
      if (config.__cached) {
        return config.__cachedResponse!
      }

      // 检查是否应该缓存
      if (shouldCacheResponse(response, shouldCache)) {
        const cacheKey = generateCacheKey(response.config, keyGenerator)
        cacheManager.set(cacheKey, response, { ttl, storage })
      }

      // 如果是修改操作，清除相关缓存
      const method = response.config.method?.toUpperCase()
      if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method || '')) {
        invalidateCache(response.config.url || '', invalidatePatterns, storage)
      }

      return response
    }
  }
}

/**
 * 清除相关缓存
 */
function invalidateCache(url: string, patterns: string[], storage: 'memory' | 'localStorage' | 'sessionStorage') {
  // 基于 URL 模式清除缓存
  patterns.forEach(pattern => {
    if (url.includes(pattern)) {
      // 这里需要根据存储类型来清除匹配的缓存键
      // 简化实现：清除所有缓存
      cacheManager.clear(storage)
    }
  })
}

/**
 * 手动清除缓存的工具函数
 */
export const cacheUtils = {
  /**
   * 清除指定 URL 的缓存
   */
  invalidateUrl(url: string, storage: 'memory' | 'localStorage' | 'sessionStorage' = 'memory') {
    const cacheKey = generateCacheKey({ method: 'GET', url })
    cacheManager.delete(cacheKey, storage)
  },

  /**
   * 清除指定模式的缓存
   */
  invalidatePattern(pattern: string, storage: 'memory' | 'localStorage' | 'sessionStorage' = 'memory') {
    // 由于我们无法直接遍历存储中的所有键，这里简化为清除所有缓存
    cacheManager.clear(storage)
  },

  /**
   * 预热缓存
   */
  async warmup<T>(requests: Array<() => Promise<T>>, concurrency = 3) {
    const chunks = []
    for (let i = 0; i < requests.length; i += concurrency) {
      chunks.push(requests.slice(i, i + concurrency))
    }

    for (const chunk of chunks) {
      await Promise.all(chunk.map(request => request().catch(error => {
        console.warn('Cache warmup failed:', error)
        return null
      })))
    }
  }
}

/**
 * 缓存装饰器
 */
export function Cached(config: CacheConfig = {}) {
  return function (target: any, propertyKey: string, descriptor: PropertyDescriptor) {
    const originalMethod = descriptor.value

    descriptor.value = async function (...args: any[]) {
      const cacheKey = `${target.constructor.name}_${propertyKey}_${JSON.stringify(args)}`
      
      // 尝试从缓存获取
      const cached = cacheManager.get(cacheKey, config.storage)
      if (cached !== null) {
        return cached
      }

      // 执行原方法
      const result = await originalMethod.apply(this, args)
      
      // 缓存结果
      cacheManager.set(cacheKey, result, config)
      
      return result
    }

    return descriptor
  }
}

export default {
  createCacheInterceptor,
  cacheUtils,
  Cached
}
