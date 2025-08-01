/**
 * API请求去重和优化工具
 * 防止重复请求，减少API调用次数
 */

interface PendingRequest {
  promise: Promise<any>
  timestamp: number
}

interface RequestConfig {
  deduplicationEnabled?: boolean
  deduplicationTTL?: number
  batchEnabled?: boolean
  batchDelay?: number
  retryConfig?: {
    maxRetries: number
    retryDelay: number
  }
}

class RequestOptimizer {
  private pendingRequests = new Map<string, PendingRequest>()
  private batchQueue: Array<{
    key: string
    config: any
    resolve: (value: any) => void
    reject: (reason: any) => void
  }> = []
  private batchTimer: ReturnType<typeof setTimeout> | null = null

  /**
   * 生成请求唯一标识
   */
  private generateRequestKey(config: any): string {
    const { method = 'GET', url = '', params, data } = config
    const paramStr = params ? JSON.stringify(params) : ''
    const dataStr = data ? JSON.stringify(data) : ''
    return `${method}_${url}_${paramStr}_${dataStr}`
  }

  /**
   * 请求去重处理
   */
  deduplicateRequest<T>(config: any, requestFn: () => Promise<T>): Promise<T> {
    const key = this.generateRequestKey(config)
    const now = Date.now()
    
    // 检查是否有相同的请求正在进行
    const pending = this.pendingRequests.get(key)
    if (pending && now - pending.timestamp < 5000) { // 5秒内的重复请求
      console.log('Request deduplicated:', key)
      return pending.promise as Promise<T>
    }

    // 创建新请求
    const promise = requestFn().finally(() => {
      // 请求完成后清理
      this.pendingRequests.delete(key)
    })

    this.pendingRequests.set(key, {
      promise,
      timestamp: now
    })

    return promise
  }

  /**
   * 批量请求处理
   */
  batchRequest<T>(
    config: any,
    requestFn: () => Promise<T>,
    batchDelay: number = 50
  ): Promise<T> {
    return new Promise((resolve, reject) => {
      const key = this.generateRequestKey(config)
      
      // 添加到批处理队列
      this.batchQueue.push({
        key,
        config,
        resolve,
        reject
      })

      // 设置批处理定时器
      if (this.batchTimer) {
        clearTimeout(this.batchTimer)
      }

      this.batchTimer = setTimeout(() => {
        this.processBatch()
      }, batchDelay)
    })
  }

  /**
   * 处理批处理队列
   */
  private processBatch(): void {
    if (this.batchQueue.length === 0) return

    // 按请求类型分组
    const groups = new Map<string, typeof this.batchQueue>()
    
    this.batchQueue.forEach(item => {
      const groupKey = `${item.config.method}_${item.config.url.split('/').slice(0, -1).join('/')}`
      if (!groups.has(groupKey)) {
        groups.set(groupKey, [])
      }
      groups.get(groupKey)!.push(item)
    })

    // 处理每个分组
    groups.forEach((items, groupKey) => {
      if (items.length > 1) {
        console.log(`Batching ${items.length} requests for ${groupKey}`)
        // 这里可以实现具体的批处理逻辑
        // 例如：将多个单个资源请求合并为一个批量请求
      }
      
      // 执行请求
      items.forEach(item => {
        this.executeRequest(item)
      })
    })

    this.batchQueue = []
    this.batchTimer = null
  }

  /**
   * 执行单个请求
   */
  private async executeRequest(item: any): Promise<void> {
    try {
      // 这里需要根据实际的请求函数来实现
      // 暂时作为示例
      const result = await fetch(item.config.url, {
        method: item.config.method,
        headers: item.config.headers,
        body: item.config.data ? JSON.stringify(item.config.data) : undefined
      }).then(res => res.json())
      
      item.resolve(result)
    } catch (error) {
      item.reject(error)
    }
  }

  /**
   * 智能重试机制
   */
  async retryRequest<T>(
    requestFn: () => Promise<T>,
    maxRetries: number = 3,
    retryDelay: number = 1000
  ): Promise<T> {
    let lastError: any
    
    for (let i = 0; i <= maxRetries; i++) {
      try {
        return await requestFn()
      } catch (error: any) {
        lastError = error
        
        // 如果是网络错误或5xx错误，则重试
        const shouldRetry = 
          !error.response || 
          error.response.status >= 500 ||
          error.code === 'NETWORK_ERROR'
        
        if (i < maxRetries && shouldRetry) {
          console.log(`Request failed, retrying in ${retryDelay}ms... (${i + 1}/${maxRetries})`)
          await new Promise(resolve => setTimeout(resolve, retryDelay * Math.pow(2, i)))
          continue
        }
        
        break
      }
    }
    
    throw lastError
  }

  /**
   * 预取数据
   */
  async prefetchData(urls: string[], priority: 'high' | 'low' = 'low'): Promise<void> {
    const promises = urls.map(url => {
      return new Promise(resolve => {
        const img = new Image()
        img.onload = img.onerror = () => resolve(null)
        img.src = url
      })
    })

    if (priority === 'high') {
      await Promise.all(promises)
    } else {
      // 低优先级预取，使用 requestIdleCallback
      promises.forEach(promise => {
        if ('requestIdleCallback' in window) {
          requestIdleCallback(() => promise)
        } else {
          setTimeout(() => promise, 100)
        }
      })
    }
  }

  /**
   * 清理过期的pending请求
   */
  cleanup(): void {
    const now = Date.now()
    const expiredKeys: string[] = []
    
    this.pendingRequests.forEach((request, key) => {
      if (now - request.timestamp > 30000) { // 30秒超时
        expiredKeys.push(key)
      }
    })
    
    expiredKeys.forEach(key => {
      this.pendingRequests.delete(key)
    })
  }

  /**
   * 获取统计信息
   */
  getStats() {
    return {
      pendingRequests: this.pendingRequests.size,
      batchQueueSize: this.batchQueue.length,
      hasBatchTimer: !!this.batchTimer
    }
  }
}

// 创建全局实例
export const requestOptimizer = new RequestOptimizer()

// 定期清理
setInterval(() => {
  requestOptimizer.cleanup()
}, 60000) // 每分钟清理一次

/**
 * 请求装饰器
 */
export function OptimizedRequest(config: RequestConfig = {}) {
  return function (target: any, propertyKey: string, descriptor: PropertyDescriptor) {
    const originalMethod = descriptor.value

    descriptor.value = async function (...args: any[]) {
      const requestFn = () => originalMethod.apply(this, args)

      // 去重处理
      if (config.deduplicationEnabled !== false) {
        return requestOptimizer.deduplicateRequest(
          { method: 'GET', url: propertyKey, params: args[0] },
          requestFn
        )
      }

      // 重试处理
      if (config.retryConfig) {
        return requestOptimizer.retryRequest(
          requestFn,
          config.retryConfig.maxRetries,
          config.retryConfig.retryDelay
        )
      }

      return requestFn()
    }

    return descriptor
  }
}

export default requestOptimizer
