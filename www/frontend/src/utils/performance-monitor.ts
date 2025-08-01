/**
 * 前端性能监控模块
 * 
 * 监控页面加载性能、API请求性能等关键指标
 * 
 * @author EcoWiki Team
 * @version 1.0
 * @since 2025-08-01
 */

export interface PerformanceMetrics {
  pageLoadTime: number
  domContentLoadedTime: number
  firstContentfulPaint: number
  apiResponseTimes: Record<string, number[]>
}

class PerformanceMonitor {
  private metrics: PerformanceMetrics = {
    pageLoadTime: 0,
    domContentLoadedTime: 0,
    firstContentfulPaint: 0,
    apiResponseTimes: {}
  }

  init() {
    this.measurePageLoad()
    this.measureAPIs()
  }

  private measurePageLoad() {
    // 监控页面加载时间
    window.addEventListener('load', () => {
      const perfData = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming
      this.metrics.pageLoadTime = perfData.loadEventEnd - perfData.loadEventStart
      this.metrics.domContentLoadedTime = perfData.domContentLoadedEventEnd - perfData.domContentLoadedEventStart
    })

    // 监控首次内容绘制
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          if (entry.name === 'first-contentful-paint') {
            this.metrics.firstContentfulPaint = entry.startTime
          }
        }
      })
      observer.observe({ entryTypes: ['paint'] })
    }
  }

  private measureAPIs() {
    // 监控 API 请求性能
    const originalFetch = window.fetch
    window.fetch = async (...args) => {
      const startTime = performance.now()
      const response = await originalFetch(...args)
      const endTime = performance.now()
      
      const url = args[0] as string
      const duration = endTime - startTime
      
      if (!this.metrics.apiResponseTimes[url]) {
        this.metrics.apiResponseTimes[url] = []
      }
      this.metrics.apiResponseTimes[url].push(duration)
      
      // 如果响应时间超过2秒，记录警告
      if (duration > 2000) {
        console.warn(`Slow API response: ${url} took ${duration.toFixed(2)}ms`)
      }
      
      return response
    }
  }

  getMetrics(): PerformanceMetrics {
    return { ...this.metrics }
  }

  getAverageAPITime(url: string): number {
    const times = this.metrics.apiResponseTimes[url]
    if (!times || times.length === 0) return 0
    return times.reduce((sum, time) => sum + time, 0) / times.length
  }

  reportToConsole() {
    console.group('🚀 EcoWiki Performance Metrics')
    console.log(`Page Load Time: ${this.metrics.pageLoadTime.toFixed(2)}ms`)
    console.log(`DOM Content Loaded: ${this.metrics.domContentLoadedTime.toFixed(2)}ms`)
    console.log(`First Contentful Paint: ${this.metrics.firstContentfulPaint.toFixed(2)}ms`)
    
    console.group('API Response Times (Average)')
    for (const [url, times] of Object.entries(this.metrics.apiResponseTimes)) {
      const avg = times.reduce((sum, time) => sum + time, 0) / times.length
      console.log(`${url}: ${avg.toFixed(2)}ms (${times.length} requests)`)
    }
    console.groupEnd()
    console.groupEnd()
  }
}

export const performanceMonitor = new PerformanceMonitor()
