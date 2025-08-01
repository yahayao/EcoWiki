/**
 * API调用分析和监控工具
 * 监控API调用频率，识别优化机会
 */

interface ApiCallRecord {
  url: string
  method: string
  timestamp: number
  duration: number
  cached: boolean
  size: number
  status: number
}

interface ApiStats {
  totalCalls: number
  cachedCalls: number
  averageResponseTime: number
  totalDataTransferred: number
  mostFrequentEndpoints: Array<{
    endpoint: string
    count: number
    averageTime: number
  }>
  cacheHitRate: number
}

class ApiMonitor {
  private callHistory: ApiCallRecord[] = []
  private readonly maxHistorySize = 1000
  private stats: ApiStats | null = null
  private reportInterval: number = 30000 // 30秒

  constructor() {
    this.startPeriodicReporting()
  }

  /**
   * 记录API调用
   */
  recordCall(record: ApiCallRecord): void {
    this.callHistory.push(record)
    
    // 限制历史记录大小
    if (this.callHistory.length > this.maxHistorySize) {
      this.callHistory = this.callHistory.slice(-this.maxHistorySize)
    }
    
    // 实时检查是否有可优化的调用模式
    this.detectOptimizationOpportunities(record)
  }

  /**
   * 检测优化机会
   */
  private detectOptimizationOpportunities(record: ApiCallRecord): void {
    const recentCalls = this.getRecentCalls(5000) // 最近5秒的调用
    
    // 检测重复调用
    const duplicateCalls = recentCalls.filter(call => 
      call.url === record.url && 
      call.method === record.method &&
      !call.cached
    )
    
    if (duplicateCalls.length > 2) {
      console.warn('🔄 Detected potential duplicate API calls:', {
        endpoint: record.url,
        count: duplicateCalls.length,
        suggestion: 'Consider implementing request deduplication or better caching'
      })
    }

    // 检测频繁调用
    const frequentCalls = recentCalls.filter(call => call.url === record.url)
    if (frequentCalls.length > 5) {
      console.warn('⚡ High frequency API calls detected:', {
        endpoint: record.url,
        count: frequentCalls.length,
        suggestion: 'Consider implementing polling optimization or WebSocket'
      })
    }

    // 检测大数据传输
    if (record.size > 1024 * 1024) { // 1MB
      console.warn('📦 Large data transfer detected:', {
        endpoint: record.url,
        size: `${(record.size / 1024 / 1024).toFixed(2)}MB`,
        suggestion: 'Consider implementing pagination or data compression'
      })
    }

    // 检测慢请求
    if (record.duration > 3000) { // 3秒
      console.warn('🐌 Slow API call detected:', {
        endpoint: record.url,
        duration: `${record.duration}ms`,
        suggestion: 'Consider optimizing backend or implementing progressive loading'
      })
    }
  }

  /**
   * 获取最近的API调用
   */
  private getRecentCalls(timeWindow: number): ApiCallRecord[] {
    const now = Date.now()
    return this.callHistory.filter(call => now - call.timestamp <= timeWindow)
  }

  /**
   * 生成统计报告
   */
  generateStats(): ApiStats {
    if (this.callHistory.length === 0) {
      return {
        totalCalls: 0,
        cachedCalls: 0,
        averageResponseTime: 0,
        totalDataTransferred: 0,
        mostFrequentEndpoints: [],
        cacheHitRate: 0
      }
    }

    const totalCalls = this.callHistory.length
    const cachedCalls = this.callHistory.filter(call => call.cached).length
    const totalDuration = this.callHistory.reduce((sum, call) => sum + call.duration, 0)
    const totalDataTransferred = this.callHistory.reduce((sum, call) => sum + call.size, 0)

    // 统计最频繁的端点
    const endpointCounts = new Map<string, { count: number, totalTime: number }>()
    
    this.callHistory.forEach(call => {
      const key = `${call.method} ${call.url.split('?')[0]}` // 忽略查询参数
      const existing = endpointCounts.get(key) || { count: 0, totalTime: 0 }
      endpointCounts.set(key, {
        count: existing.count + 1,
        totalTime: existing.totalTime + call.duration
      })
    })

    const mostFrequentEndpoints = Array.from(endpointCounts.entries())
      .map(([endpoint, data]) => ({
        endpoint,
        count: data.count,
        averageTime: Math.round(data.totalTime / data.count)
      }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 10)

    this.stats = {
      totalCalls,
      cachedCalls,
      averageResponseTime: Math.round(totalDuration / totalCalls),
      totalDataTransferred,
      mostFrequentEndpoints,
      cacheHitRate: Math.round((cachedCalls / totalCalls) * 100)
    }

    return this.stats
  }

  /**
   * 输出优化建议
   */
  generateOptimizationReport(): void {
    const stats = this.generateStats()
    
    console.group('📊 API Performance Report')
    console.log('Total API calls:', stats.totalCalls)
    console.log('Cache hit rate:', `${stats.cacheHitRate}%`)
    console.log('Average response time:', `${stats.averageResponseTime}ms`)
    console.log('Total data transferred:', `${(stats.totalDataTransferred / 1024 / 1024).toFixed(2)}MB`)
    
    console.group('🎯 Optimization Suggestions')
    
    if (stats.cacheHitRate < 30) {
      console.warn('• Low cache hit rate. Consider implementing better caching strategies.')
    }
    
    if (stats.averageResponseTime > 1000) {
      console.warn('• High average response time. Consider optimizing backend or implementing progressive loading.')
    }
    
    if (stats.totalDataTransferred > 10 * 1024 * 1024) { // 10MB
      console.warn('• High data usage. Consider implementing data compression or pagination.')
    }

    // 分析最频繁的端点
    const highFrequencyEndpoints = stats.mostFrequentEndpoints.filter(endpoint => endpoint.count > 10)
    if (highFrequencyEndpoints.length > 0) {
      console.group('🔥 High Frequency Endpoints')
      highFrequencyEndpoints.forEach(endpoint => {
        console.log(`${endpoint.endpoint}: ${endpoint.count} calls (avg: ${endpoint.averageTime}ms)`)
      })
      console.groupEnd()
    }
    
    console.groupEnd()
    console.groupEnd()
  }

  /**
   * 获取实时性能指标
   */
  getRealtimeMetrics() {
    const recent = this.getRecentCalls(60000) // 最近1分钟
    const veryRecent = this.getRecentCalls(10000) // 最近10秒
    
    return {
      callsPerMinute: recent.length,
      callsPerSecond: veryRecent.length / 10,
      recentCacheHitRate: recent.length > 0 ? 
        Math.round((recent.filter(call => call.cached).length / recent.length) * 100) : 0,
      recentAverageResponseTime: recent.length > 0 ?
        Math.round(recent.reduce((sum, call) => sum + call.duration, 0) / recent.length) : 0
    }
  }

  /**
   * 开始定期报告
   */
  private startPeriodicReporting(): void {
    if (import.meta.env.DEV) {
      setInterval(() => {
        if (this.callHistory.length > 0) {
          this.generateOptimizationReport()
        }
      }, this.reportInterval)
    }
  }

  /**
   * 导出性能数据
   */
  exportData(): string {
    return JSON.stringify({
      timestamp: new Date().toISOString(),
      stats: this.generateStats(),
      recentCalls: this.getRecentCalls(300000) // 最近5分钟
    }, null, 2)
  }

  /**
   * 清理历史数据
   */
  clearHistory(): void {
    this.callHistory = []
    this.stats = null
  }

  /**
   * 设置报告间隔
   */
  setReportInterval(interval: number): void {
    this.reportInterval = interval
  }
}

// 创建全局实例
export const apiMonitor = new ApiMonitor()

// Axios 拦截器集成
export function setupApiMonitoring(axiosInstance: any): void {
  // 请求拦截器
  axiosInstance.interceptors.request.use((config: any) => {
    config._startTime = Date.now()
    return config
  })

  // 响应拦截器
  axiosInstance.interceptors.response.use(
    (response: any) => {
      const duration = Date.now() - response.config._startTime
      const size = JSON.stringify(response.data).length
      
      apiMonitor.recordCall({
        url: response.config.url,
        method: response.config.method.toUpperCase(),
        timestamp: Date.now(),
        duration,
        cached: response.config._fromCache || false,
        size,
        status: response.status
      })
      
      return response
    },
    (error: any) => {
      if (error.config) {
        const duration = Date.now() - error.config._startTime
        
        apiMonitor.recordCall({
          url: error.config.url,
          method: error.config.method.toUpperCase(),
          timestamp: Date.now(),
          duration,
          cached: false,
          size: 0,
          status: error.response?.status || 0
        })
      }
      
      return Promise.reject(error)
    }
  )
}

export default apiMonitor
