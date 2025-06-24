import { createApp, type App } from 'vue'
import Toast from '../components/Toast.vue'

export interface ToastOptions {
  duration?: number
  type?: 'success' | 'error' | 'warning' | 'info'
}

class ToastService {
  private container: HTMLElement | null = null
  private toastInstances: HTMLDivElement[] = []

  private createContainer() {
    if (!this.container) {
      this.container = document.createElement('div')
      this.container.className = 'toast-container'
      this.container.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 10000;
        pointer-events: none;
      `
      document.body.appendChild(this.container)
    }
    return this.container
  }

  show(message: string, title?: string, options: ToastOptions = {}) {
    const { duration = 3000, type = 'info' } = options
    const container = this.createContainer()

    const toast = document.createElement('div')
    toast.className = `toast toast-${type}`
    toast.style.cssText = `
      background: white;
      border-radius: 8px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
      margin-bottom: 10px;
      padding: 16px 20px;
      min-width: 300px;
      max-width: 400px;
      transform: translateX(100%);
      transition: transform 0.3s ease;
      pointer-events: auto;
      border-left: 4px solid ${this.getTypeColor(type)};
    `

    const titleElement = title ? `<div style="font-weight: 600; margin-bottom: 4px; color: #333;">${title}</div>` : ''
    toast.innerHTML = `
      ${titleElement}
      <div style="color: #666; font-size: 14px;">${message}</div>
    `

    container.appendChild(toast)

    // 动画显示
    setTimeout(() => {
      toast.style.transform = 'translateX(0)'
    }, 100)

    // 自动隐藏
    setTimeout(() => {
      toast.style.transform = 'translateX(100%)'
      setTimeout(() => {
        if (container.contains(toast)) {
          container.removeChild(toast)
        }
      }, 300)
    }, duration)

    // 点击关闭
    toast.addEventListener('click', () => {
      toast.style.transform = 'translateX(100%)'
      setTimeout(() => {
        if (container.contains(toast)) {
          container.removeChild(toast)
        }
      }, 300)
    })

    this.toastInstances.push(toast)
  }

  private getTypeColor(type: string): string {
    switch (type) {
      case 'success': return '#10b981'
      case 'error': return '#ef4444'
      case 'warning': return '#f59e0b'
      default: return '#3b82f6'
    }
  }

  success(message: string, title?: string) {
    this.show(message, title, { type: 'success' })
  }

  error(message: string, title?: string) {
    this.show(message, title, { type: 'error' })
  }

  warning(message: string, title?: string) {
    this.show(message, title, { type: 'warning' })
  }

  info(message: string, title?: string) {
    this.show(message, title, { type: 'info' })
  }

  clear() {
    this.toastInstances.forEach(toast => {
      const container = this.container
      if (container && container.contains(toast)) {
        container.removeChild(toast)
      }
    })
    this.toastInstances = []
  }
}

const toast = new ToastService()
export default toast
