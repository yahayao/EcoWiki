/**
 * Service Worker 注册和管理工具
 */

interface ServiceWorkerConfig {
  enabled?: boolean
  scope?: string
  updateOnReload?: boolean
}

class ServiceWorkerManager {
  private registration: ServiceWorkerRegistration | null = null
  private updateAvailable = false

  /**
   * 注册 Service Worker
   */
  async register(config: ServiceWorkerConfig = {}): Promise<boolean> {
    const {
      enabled = true,
      scope = '/',
      updateOnReload = true
    } = config

    if (!enabled || !('serviceWorker' in navigator)) {
      console.log('Service Worker not supported or disabled')
      return false
    }

    try {
      console.log('Registering Service Worker...')
      
      this.registration = await navigator.serviceWorker.register('/sw.js', {
        scope
      })

      console.log('Service Worker registered successfully')

      // 监听更新
      this.registration.addEventListener('updatefound', () => {
        this.handleUpdate()
      })

      // 检查现有更新
      if (this.registration.waiting) {
        this.updateAvailable = true
        this.notifyUpdateAvailable()
      }

      // 监听控制变化
      navigator.serviceWorker.addEventListener('controllerchange', () => {
        if (updateOnReload) {
          window.location.reload()
        }
      })

      return true
    } catch (error) {
      console.error('Service Worker registration failed:', error)
      return false
    }
  }

  /**
   * 处理更新
   */
  private handleUpdate(): void {
    if (!this.registration) return

    const newWorker = this.registration.installing
    if (!newWorker) return

    newWorker.addEventListener('statechange', () => {
      if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
        this.updateAvailable = true
        this.notifyUpdateAvailable()
      }
    })
  }

  /**
   * 通知有更新可用
   */
  private notifyUpdateAvailable(): void {
    console.log('New version available')
    
    // 发送自定义事件
    window.dispatchEvent(new CustomEvent('sw-update-available', {
      detail: { registration: this.registration }
    }))

    // 可选：显示更新提示
    this.showUpdatePrompt()
  }

  /**
   * 显示更新提示
   */
  private showUpdatePrompt(): void {
    if (confirm('发现新版本，是否立即更新？')) {
      this.skipWaiting()
    }
  }

  /**
   * 跳过等待，立即激活新版本
   */
  skipWaiting(): void {
    if (!this.registration || !this.registration.waiting) {
      return
    }

    this.registration.waiting.postMessage({ type: 'SKIP_WAITING' })
  }

  /**
   * 手动检查更新
   */
  async checkForUpdate(): Promise<boolean> {
    if (!this.registration) {
      return false
    }

    try {
      await this.registration.update()
      return this.updateAvailable
    } catch (error) {
      console.error('Update check failed:', error)
      return false
    }
  }

  /**
   * 卸载 Service Worker
   */
  async unregister(): Promise<boolean> {
    if (!this.registration) {
      return false
    }

    try {
      const result = await this.registration.unregister()
      this.registration = null
      console.log('Service Worker unregistered')
      return result
    } catch (error) {
      console.error('Service Worker unregistration failed:', error)
      return false
    }
  }

  /**
   * 清除缓存
   */
  async clearCache(): Promise<boolean> {
    if (!navigator.serviceWorker.controller) {
      return false
    }

    return new Promise((resolve) => {
      const messageChannel = new MessageChannel()
      
      messageChannel.port1.onmessage = (event) => {
        resolve(event.data.success || false)
      }

      navigator.serviceWorker.controller!.postMessage(
        { type: 'CLEAR_CACHE' },
        [messageChannel.port2]
      )
    })
  }

  /**
   * 获取缓存大小
   */
  async getCacheSize(): Promise<number> {
    if (!('storage' in navigator) || !('estimate' in navigator.storage)) {
      return 0
    }

    try {
      const estimate = await navigator.storage.estimate()
      return estimate.usage || 0
    } catch (error) {
      console.error('Failed to get cache size:', error)
      return 0
    }
  }

  /**
   * 检查是否在线
   */
  isOnline(): boolean {
    return navigator.onLine
  }

  /**
   * 监听网络状态变化
   */
  onNetworkChange(callback: (online: boolean) => void): () => void {
    const handleOnline = () => callback(true)
    const handleOffline = () => callback(false)

    window.addEventListener('online', handleOnline)
    window.addEventListener('offline', handleOffline)

    // 返回清理函数
    return () => {
      window.removeEventListener('online', handleOnline)
      window.removeEventListener('offline', handleOffline)
    }
  }

  /**
   * 获取状态信息
   */
  getStatus() {
    return {
      registered: !!this.registration,
      updateAvailable: this.updateAvailable,
      controller: !!navigator.serviceWorker.controller,
      online: this.isOnline()
    }
  }
}

// 创建单例实例
export const swManager = new ServiceWorkerManager()

/**
 * 便捷的注册函数
 */
export async function registerServiceWorker(config?: ServiceWorkerConfig): Promise<boolean> {
  return swManager.register(config)
}

/**
 * Vue 插件
 */
export function createServiceWorkerPlugin(config?: ServiceWorkerConfig) {
  return {
    install(app: any) {
      // 注册 Service Worker
      swManager.register(config)

      // 提供全局属性
      app.config.globalProperties.$sw = swManager
      app.provide('serviceWorker', swManager)
    }
  }
}

/**
 * React Hook（如果需要）
 * 注意：这需要在 React 项目中使用，当前项目使用 Vue
 */
export function useServiceWorker() {
  // 这里是 React Hook 的示例代码
  // 在 Vue 项目中不会被使用
  const status = swManager.getStatus()

  return {
    ...status,
    skipWaiting: () => swManager.skipWaiting(),
    checkUpdate: () => swManager.checkForUpdate(),
    clearCache: () => swManager.clearCache(),
    getCacheSize: () => swManager.getCacheSize()
  }
}

export default swManager
