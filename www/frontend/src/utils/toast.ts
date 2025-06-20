import { createApp, type App } from 'vue'
import Toast from '../components/Toast.vue'

interface ToastOptions {
  message: string
  title?: string
  type?: 'success' | 'error' | 'warning' | 'info'
  duration?: number
}

class ToastService {
  private toastInstances: App[] = []

  show(options: ToastOptions) {
    const container = document.createElement('div')
    document.body.appendChild(container)

    const app = createApp(Toast, {
      ...options,
      onClose: () => {
        app.unmount()
        document.body.removeChild(container)
        
        const index = this.toastInstances.indexOf(app)
        if (index > -1) {
          this.toastInstances.splice(index, 1)
        }
      }
    })

    app.mount(container)
    this.toastInstances.push(app)
  }

  success(message: string, title?: string, duration = 3000) {
    this.show({
      message,
      title,
      type: 'success',
      duration
    })
  }

  error(message: string, title?: string, duration = 5000) {
    this.show({
      message,
      title,
      type: 'error',
      duration
    })
  }

  warning(message: string, title?: string, duration = 4000) {
    this.show({
      message,
      title,
      type: 'warning',
      duration
    })
  }

  info(message: string, title?: string, duration = 3000) {
    this.show({
      message,
      title,
      type: 'info',
      duration
    })
  }

  clear() {
    this.toastInstances.forEach(app => {
      app.unmount()
    })
    this.toastInstances = []
    
    // 清除所有toast容器
    const toastContainers = document.querySelectorAll('[data-toast-container]')
    toastContainers.forEach(container => {
      container.remove()
    })
  }
}

export const toast = new ToastService()
export default toast
