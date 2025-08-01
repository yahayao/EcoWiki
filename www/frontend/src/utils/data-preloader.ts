/**
 * 数据预加载服务
 * 智能预取用户可能需要的数据，减少后续API调用
 */

import { api } from '@/api'
import { cacheManager } from './cache-manager'

interface PreloadConfig {
  immediate?: boolean
  priority?: 'high' | 'medium' | 'low'
  dependencies?: string[]
  condition?: () => boolean
}

interface PreloadRule {
  trigger: string
  targets: Array<{
    url: string
    config?: PreloadConfig
  }>
}

class DataPreloader {
  private preloadRules: PreloadRule[] = []
  private preloadQueue: Array<{
    url: string
    config: PreloadConfig
    timestamp: number
  }> = []
  private isProcessing = false

  constructor() {
    this.initializeDefaultRules()
    this.startProcessing()
  }

  /**
   * 初始化默认预加载规则
   */
  private initializeDefaultRules(): void {
    this.preloadRules = [
      // 用户登录后预加载
      {
        trigger: 'user:login',
        targets: [
          { url: '/api/articles?page=1&size=10', config: { priority: 'high' } },
          { url: '/api/categories', config: { priority: 'high' } },
          { url: '/api/tags', config: { priority: 'medium' } },
          { url: '/api/users/me/drafts', config: { priority: 'low' } }
        ]
      },
      // 访问文章列表时预加载
      {
        trigger: 'route:articles',
        targets: [
          { url: '/api/articles?page=2&size=10', config: { priority: 'medium' } },
          { url: '/api/articles/popular', config: { priority: 'low' } }
        ]
      },
      // 查看文章详情时预加载
      {
        trigger: 'article:view',
        targets: [
          { url: '/api/articles/related', config: { priority: 'medium' } },
          { url: '/api/articles/{id}/comments', config: { priority: 'high' } }
        ]
      },
      // 访问用户中心时预加载
      {
        trigger: 'route:profile',
        targets: [
          { url: '/api/users/me/articles', config: { priority: 'high' } },
          { url: '/api/users/me/comments', config: { priority: 'medium' } },
          { url: '/api/users/me/favorites', config: { priority: 'low' } }
        ]
      }
    ]
  }

  /**
   * 触发预加载
   */
  trigger(event: string, context: any = {}): void {
    console.log('Preload trigger:', event, context)
    
    const matchingRules = this.preloadRules.filter(rule => {
      if (rule.trigger === event) return true
      if (rule.trigger.includes('*') && event.startsWith(rule.trigger.replace('*', ''))) return true
      return false
    })

    matchingRules.forEach(rule => {
      rule.targets.forEach(target => {
        let url = target.url
        
        // 替换URL中的变量
        if (context && url.includes('{')) {
          Object.keys(context).forEach(key => {
            url = url.replace(`{${key}}`, context[key])
          })
        }

        // 检查条件
        if (target.config?.condition && !target.config.condition()) {
          return
        }

        this.addToQueue(url, target.config || {})
      })
    })
  }

  /**
   * 添加到预加载队列
   */
  private addToQueue(url: string, config: PreloadConfig): void {
    // 检查是否已经在队列中或已缓存
    const cacheKey = `api_GET_${url}_`
    if (cacheManager.get(cacheKey)) {
      console.log('Data already cached, skipping preload:', url)
      return
    }

    const existing = this.preloadQueue.find(item => item.url === url)
    if (existing) {
      console.log('URL already in preload queue:', url)
      return
    }

    this.preloadQueue.push({
      url,
      config,
      timestamp: Date.now()
    })

    // 按优先级排序
    this.preloadQueue.sort((a, b) => {
      const priorityOrder = { high: 0, medium: 1, low: 2 }
      const aPriority = priorityOrder[a.config.priority || 'medium']
      const bPriority = priorityOrder[b.config.priority || 'medium']
      return aPriority - bPriority
    })
  }

  /**
   * 开始处理预加载队列
   */
  private startProcessing(): void {
    setInterval(() => {
      if (!this.isProcessing && this.preloadQueue.length > 0) {
        this.processQueue()
      }
    }, 100)
  }

  /**
   * 处理预加载队列
   */
  private async processQueue(): Promise<void> {
    if (this.isProcessing || this.preloadQueue.length === 0) return

    this.isProcessing = true

    try {
      const item = this.preloadQueue.shift()!
      const { url, config } = item

      // 检查网络状态和性能
      if (!navigator.onLine) {
        console.log('Offline, skipping preload:', url)
        return
      }

      // 检查是否在空闲时间执行
      if (config.priority === 'low') {
        await this.waitForIdleTime()
      }

      // 执行预加载
      await this.preloadData(url)
      
      console.log('Preloaded:', url)

      // 高优先级项目之间添加小延迟，避免阻塞主线程
      if (config.priority === 'high') {
        await new Promise(resolve => setTimeout(resolve, 50))
      } else {
        await new Promise(resolve => setTimeout(resolve, 200))
      }

    } catch (error) {
      console.warn('Preload failed:', error)
    } finally {
      this.isProcessing = false
    }
  }

  /**
   * 等待浏览器空闲时间
   */
  private waitForIdleTime(): Promise<void> {
    return new Promise(resolve => {
      if ('requestIdleCallback' in window) {
        requestIdleCallback(() => resolve())
      } else {
        setTimeout(resolve, 100)
      }
    })
  }

  /**
   * 执行数据预加载
   */
  private async preloadData(url: string): Promise<void> {
    try {
      const response = await api.get(url)
      // 数据会自动被缓存拦截器缓存
      console.log('Preloaded data for:', url, 'Size:', JSON.stringify(response.data).length, 'bytes')
    } catch (error: any) {
      // 静默处理预加载错误，不影响用户体验
      if (error.response?.status !== 404) {
        console.warn('Preload request failed:', url, error.message)
      }
    }
  }

  /**
   * 添加自定义预加载规则
   */
  addRule(rule: PreloadRule): void {
    this.preloadRules.push(rule)
  }

  /**
   * 移除预加载规则
   */
  removeRule(trigger: string): void {
    this.preloadRules = this.preloadRules.filter(rule => rule.trigger !== trigger)
  }

  /**
   * 清空预加载队列
   */
  clearQueue(): void {
    this.preloadQueue = []
  }

  /**
   * 获取队列状态
   */
  getQueueStatus() {
    return {
      queueSize: this.preloadQueue.length,
      isProcessing: this.isProcessing,
      nextItem: this.preloadQueue[0]?.url
    }
  }

  /**
   * 智能预测用户行为
   */
  predictAndPreload(currentRoute: string, userHistory: string[]): void {
    // 基于用户历史行为预测可能访问的页面
    const predictions = this.analyzeUserBehavior(currentRoute, userHistory)
    
    predictions.forEach(prediction => {
      this.trigger(prediction.event, prediction.context)
    })
  }

  /**
   * 分析用户行为模式
   */
  private analyzeUserBehavior(currentRoute: string, history: string[]): Array<{event: string, context: any}> {
    const predictions: Array<{event: string, context: any}> = []

    // 简单的模式识别
    if (currentRoute === '/articles' && history.includes('/articles/detail')) {
      predictions.push({ event: 'route:articles', context: {} })
    }

    if (currentRoute.startsWith('/articles/') && !currentRoute.includes('/edit')) {
      const articleId = currentRoute.split('/')[2]
      predictions.push({ event: 'article:view', context: { id: articleId } })
    }

    return predictions
  }
}

// 创建全局实例
export const dataPreloader = new DataPreloader()

// Vue 插件
export function createDataPreloaderPlugin() {
  return {
    install(app: any) {
      app.config.globalProperties.$preloader = dataPreloader
      app.provide('dataPreloader', dataPreloader)

      // 监听路由变化
      if (app.config.globalProperties.$router) {
        app.config.globalProperties.$router.afterEach((to: any) => {
          dataPreloader.trigger(`route:${to.name || to.path.split('/')[1]}`, {
            route: to.path,
            params: to.params,
            query: to.query
          })
        })
      }
    }
  }
}

export default dataPreloader
