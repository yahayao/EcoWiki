/**
 * 性能优化工具模块
 * 
 * 提供前端性能优化相关的工具函数
 * 
 * @author EcoWiki Team
 * @version 1.0
 * @since 2025-08-01
 */

/**
 * 防抖函数 - 优化搜索等高频操作
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  delay: number
): (...args: Parameters<T>) => void {
  let timeoutId: ReturnType<typeof setTimeout>
  return (...args: Parameters<T>) => {
    clearTimeout(timeoutId)
    timeoutId = setTimeout(() => func(...args), delay)
  }
}

/**
 * 节流函数 - 优化滚动等高频操作
 */
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  delay: number
): (...args: Parameters<T>) => void {
  let lastCall = 0
  return (...args: Parameters<T>) => {
    const now = Date.now()
    if (now - lastCall >= delay) {
      lastCall = now
      return func(...args)
    }
  }
}

/**
 * 图片懒加载工具
 */
export function lazyLoadImage(img: HTMLImageElement, src: string) {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        img.src = src
        img.classList.remove('lazy')
        observer.unobserve(img)
      }
    })
  })
  
  observer.observe(img)
}

/**
 * 预加载关键资源
 */
export function preloadResources(urls: string[]) {
  urls.forEach(url => {
    const link = document.createElement('link')
    link.rel = 'preload'
    link.href = url
    link.as = 'script'
    document.head.appendChild(link)
  })
}

/**
 * 缓存管理工具
 */
export class CacheManager {
  private cache = new Map<string, { data: any, timestamp: number, ttl: number }>()
  
  set(key: string, data: any, ttl: number = 300000) { // 默认5分钟
    this.cache.set(key, {
      data,
      timestamp: Date.now(),
      ttl
    })
  }
  
  get(key: string) {
    const item = this.cache.get(key)
    if (!item) return null
    
    const now = Date.now()
    if (now - item.timestamp > item.ttl) {
      this.cache.delete(key)
      return null
    }
    
    return item.data
  }
  
  clear() {
    this.cache.clear()
  }
}

export const cacheManager = new CacheManager()
