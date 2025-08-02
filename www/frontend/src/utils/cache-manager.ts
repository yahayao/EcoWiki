/**
 * 缓存管理服务
 * 提供内存缓存、localStorage 和 sessionStorage 的统一管理
 */

interface CacheItem<T> {
  data: T
  timestamp: number
  ttl?: number
}

interface CacheOptions {
  ttl?: number // 生存时间（毫秒）
  storage?: 'memory' | 'localStorage' | 'sessionStorage'
}

class CacheManager {
  private memoryCache = new Map<string, CacheItem<any>>()
  private readonly DEFAULT_TTL = 5 * 60 * 1000 // 5分钟

  /**
   * 设置缓存
   */
  set<T>(key: string, data: T, options: CacheOptions = {}): void {
    const { ttl = this.DEFAULT_TTL, storage = 'memory' } = options
    
    const item: CacheItem<T> = {
      data,
      timestamp: Date.now(),
      ttl
    }

    switch (storage) {
      case 'memory':
        this.memoryCache.set(key, item)
        break
      case 'localStorage':
        this.setStorageItem(localStorage, key, item)
        break
      case 'sessionStorage':
        this.setStorageItem(sessionStorage, key, item)
        break
    }
  }

  /**
   * 获取缓存
   */
  get<T>(key: string, storage: 'memory' | 'localStorage' | 'sessionStorage' = 'memory'): T | null {
    let item: CacheItem<T> | null = null

    switch (storage) {
      case 'memory':
        item = this.memoryCache.get(key) || null
        break
      case 'localStorage':
        item = this.getStorageItem(localStorage, key)
        break
      case 'sessionStorage':
        item = this.getStorageItem(sessionStorage, key)
        break
    }

    if (!item) return null

    // 检查是否过期
    if (item.ttl && Date.now() - item.timestamp > item.ttl) {
      this.delete(key, storage)
      return null
    }

    return item.data
  }

  /**
   * 删除缓存
   */
  delete(key: string, storage: 'memory' | 'localStorage' | 'sessionStorage' = 'memory'): void {
    switch (storage) {
      case 'memory':
        this.memoryCache.delete(key)
        break
      case 'localStorage':
        localStorage.removeItem(key)
        break
      case 'sessionStorage':
        sessionStorage.removeItem(key)
        break
    }
  }

  /**
   * 清空缓存
   */
  clear(storage: 'memory' | 'localStorage' | 'sessionStorage' = 'memory'): void {
    switch (storage) {
      case 'memory':
        this.memoryCache.clear()
        break
      case 'localStorage':
        localStorage.clear()
        break
      case 'sessionStorage':
        sessionStorage.clear()
        break
    }
  }

  /**
   * 获取缓存或异步获取数据
   */
  async getOrFetch<T>(
    key: string,
    fetcher: () => Promise<T>,
    options: CacheOptions = {}
  ): Promise<T> {
    const cached = this.get<T>(key, options.storage)
    if (cached !== null) {
      return cached
    }

    const data = await fetcher()
    this.set(key, data, options)
    return data
  }

  /**
   * 批量设置缓存
   */
  setMultiple<T>(items: Array<{ key: string; data: T }>, options: CacheOptions = {}): void {
    items.forEach(({ key, data }) => {
      this.set(key, data, options)
    })
  }

  /**
   * 批量获取缓存
   */
  getMultiple<T>(keys: string[], storage: 'memory' | 'localStorage' | 'sessionStorage' = 'memory'): Array<T | null> {
    return keys.map(key => this.get<T>(key, storage))
  }

  /**
   * 清理过期缓存
   */
  cleanup(): void {
    const now = Date.now()
    
    console.log('🧹 开始缓存清理...')
    
    // 清理内存缓存
    const memoryItemsBefore = this.memoryCache.size
    for (const [key, item] of this.memoryCache.entries()) {
      if (item.ttl && now - item.timestamp > item.ttl) {
        this.memoryCache.delete(key)
      }
    }
    const memoryItemsAfter = this.memoryCache.size
    
    // 清理 localStorage 过期项
    const localStorageItemsBefore = localStorage.length
    this.cleanupStorage(localStorage)
    const localStorageItemsAfter = localStorage.length
    
    // 清理 sessionStorage 过期项
    const sessionStorageItemsBefore = sessionStorage.length
    this.cleanupStorage(sessionStorage)
    const sessionStorageItemsAfter = sessionStorage.length
    
    console.log('🧹 缓存清理完成:', {
      memory: `${memoryItemsBefore} -> ${memoryItemsAfter}`,
      localStorage: `${localStorageItemsBefore} -> ${localStorageItemsAfter}`,
      sessionStorage: `${sessionStorageItemsBefore} -> ${sessionStorageItemsAfter}`
    })
  }

  /**
   * 获取缓存统计信息
   */
  getStats() {
    return {
      memorySize: this.memoryCache.size,
      localStorageSize: localStorage.length,
      sessionStorageSize: sessionStorage.length
    }
  }

  private setStorageItem<T>(storage: Storage, key: string, item: CacheItem<T>): void {
    try {
      storage.setItem(key, JSON.stringify(item))
    } catch (error) {
      console.warn('Storage quota exceeded, clearing old items:', error)
      this.cleanupStorage(storage)
      try {
        storage.setItem(key, JSON.stringify(item))
      } catch (secondError) {
        console.error('Failed to set storage item after cleanup:', secondError)
      }
    }
  }

  private getStorageItem<T>(storage: Storage, key: string): CacheItem<T> | null {
    try {
      const item = storage.getItem(key)
      return item ? JSON.parse(item) : null
    } catch (error) {
      console.warn('Failed to parse storage item:', error)
      storage.removeItem(key)
      return null
    }
  }

  private cleanupStorage(storage: Storage): void {
    const now = Date.now()
    const keysToRemove: string[] = []

    // 认证相关的键，不应该被缓存清理删除
    const protectedKeys = ['token', 'refreshToken', 'user', 'rememberMe', 'savedLoginField', 'savedPassword']

    for (let i = 0; i < storage.length; i++) {
      const key = storage.key(i)
      if (!key) continue

      // 跳过受保护的认证键
      if (protectedKeys.includes(key)) {
        console.log(`🛡️ 保护认证键: ${key}`)
        continue
      }

      try {
        const item = JSON.parse(storage.getItem(key) || '{}')
        if (item.ttl && now - item.timestamp > item.ttl) {
          console.log(`🗑️ 过期缓存项: ${key}`)
          keysToRemove.push(key)
        }
      } catch (error) {
        // 只删除看起来像是缓存格式的项（以cache_开头或包含timestamp的）
        if (key.startsWith('cache_') || key.includes('_cache_')) {
          console.log(`🗑️ 无效缓存项: ${key}`)
          keysToRemove.push(key)
        } else {
          console.log(`⚠️ 跳过非缓存项: ${key}`)
        }
        // 其他解析失败的项目保留，可能是其他应用数据
      }
    }

    keysToRemove.forEach(key => {
      console.log(`❌ 删除过期项: ${key}`)
      storage.removeItem(key)
    })
  }
}

// 创建单例实例
export const cacheManager = new CacheManager()

// 定期清理过期缓存
setInterval(() => {
  cacheManager.cleanup()
}, 60000) // 每分钟清理一次

export default cacheManager
