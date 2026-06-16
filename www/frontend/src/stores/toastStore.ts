/**
 * 全局通知队列 Pinia Store
 *
 * 提供响应式通知队列，替代 utils/toast.ts 的 DOM 操作方案。
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface ToastItem {
  id: number
  message: string
  title?: string
  type: 'success' | 'error' | 'warning' | 'info'
  duration: number
  visible: boolean
}

let nextId = 0

export const useToastStore = defineStore('toast', () => {
  const toasts = ref<ToastItem[]>([])

  const addToast = (message: string, title?: string, type: ToastItem['type'] = 'info', duration = 3000) => {
    const id = ++nextId
    toasts.value.push({ id, message, title, type, duration, visible: true })
    if (duration > 0) setTimeout(() => removeToast(id), duration)
    return id
  }

  const removeToast = (id: number) => {
    const idx = toasts.value.findIndex(t => t.id === id)
    if (idx > -1) {
      toasts.value[idx].visible = false
      setTimeout(() => { toasts.value = toasts.value.filter(t => t.id !== id) }, 300)
    }
  }

  const clearToasts = () => { toasts.value = [] }

  const success = (message: string, title?: string, duration?: number) => addToast(message, title, 'success', duration)
  const error = (message: string, title?: string, duration?: number) => addToast(message, title, 'error', duration)
  const warning = (message: string, title?: string, duration?: number) => addToast(message, title, 'warning', duration)
  const info = (message: string, title?: string, duration?: number) => addToast(message, title, 'info', duration)

  return { toasts, addToast, removeToast, clearToasts, success, error, warning, info }
})
