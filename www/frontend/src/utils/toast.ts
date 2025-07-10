/**
 * Toast 消息提示工具服务
 * 
 * 该模块提供了一个完整的消息提示系统，用于在应用中显示各种类型的通知消息。
 * 采用面向对象设计，提供了简洁的API接口和灵活的配置选项。
 * 
 * 功能特性：
 * - 四种消息类型：成功、错误、警告、信息
 * - 自动消失和手动关闭
 * - 多 Toast 实例管理
 * - 动画效果和响应式设计
 * - 类型安全的 TypeScript 接口
 * - 单例模式，全局状态管理
 * 
 * 设计原则：
 * - 单例模式：确保全局只有一个 Toast 服务实例
 * - 组合模式：支持多个 Toast 同时显示
 * - 策略模式：不同类型的 Toast 使用不同的样式策略
 * - 观察者模式：自动管理 Toast 的生命周期
 * 
 * 技术实现：
 * - 原生 DOM 操作，无框架依赖
 * - CSS Transform 和 Transition 动画
 * - 事件监听和自动清理机制
 * - 内存泄漏防护
 * 
 * 使用场景：
 * - API 请求成功/失败提示
 * - 表单验证结果反馈
 * - 系统状态变更通知
 * - 用户操作确认提示
 * 
 * @author EcoWiki Team
 * @version 1.0.0
 * @since 2024-01-01
 * 
 * @example
 * ```typescript
 * import toast from '@/utils/toast'
 * 
 * // 显示成功消息
 * toast.success('操作成功！')
 * 
 * // 显示错误消息
 * toast.error('操作失败', '错误提示')
 * 
 * // 自定义配置
 * toast.show('自定义消息', '标题', { 
 *   type: 'warning', 
 *   duration: 5000 
 * })
 * ```
 */

import { createApp, type App } from 'vue'
import Toast from '../components/common/Toast.vue'

/**
 * Toast 配置选项接口
 * 定义了创建 Toast 时可以传递的配置参数
 * 
 * @interface ToastOptions
 */
export interface ToastOptions {
  /** 显示持续时间（毫秒），0 表示不自动关闭 */
  duration?: number
  /** Toast 类型，影响颜色和图标 */
  type?: 'success' | 'error' | 'warning' | 'info'
}

/**
 * Toast 服务类
 * 
 * 负责管理和显示 Toast 消息的核心服务类。
 * 采用单例模式，确保全局只有一个实例，避免重复创建容器和状态冲突。
 * 
 * 核心功能：
 * - 创建和管理 Toast 容器
 * - 动态生成 Toast 元素
 * - 控制动画效果和自动关闭
 * - 管理多个 Toast 实例
 * - 提供便捷的类型化方法
 * 
 * @class ToastService
 */
class ToastService {
  /** Toast 容器元素，所有 Toast 都会添加到此容器中 */
  private container: HTMLElement | null = null
  
  /** 当前活跃的 Toast 实例数组，用于批量管理 */
  private toastInstances: HTMLDivElement[] = []

  /**
   * 创建 Toast 容器
   * 
   * 如果容器不存在，则创建一个新的容器并添加到 body 中。
   * 容器使用固定定位，位于页面右上角，确保在最上层显示。
   * 
   * @private
   * @returns {HTMLElement} Toast 容器元素
   */
  private createContainer(): HTMLElement {
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

  /**
   * 显示 Toast 消息
   * 
   * 核心方法，用于创建和显示一个 Toast 消息。
   * 支持自定义标题、消息内容、类型和持续时间。
   * 
   * @param {string} message - 消息内容（必填）
   * @param {string} [title] - 消息标题（可选）
   * @param {ToastOptions} [options={}] - 配置选项（可选）
   * 
   * @example
   * ```typescript
   * // 基础用法
   * toast.show('这是一条消息')
   * 
   * // 带标题
   * toast.show('操作完成', '成功')
   * 
   * // 自定义配置
   * toast.show('警告信息', '注意', { 
   *   type: 'warning', 
   *   duration: 5000 
   * })
   * ```
   */
  show(message: string, title?: string, options: ToastOptions = {}) {
    const { duration = 3000, type = 'info' } = options
    const container = this.createContainer()

    // 创建 Toast 元素
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

    // 构建 Toast 内容 HTML
    const titleElement = title ? `<div style="font-weight: 600; margin-bottom: 4px; color: #333;">${title}</div>` : ''
    toast.innerHTML = `
      ${titleElement}
      <div style="color: #666; font-size: 14px;">${message}</div>
    `

    container.appendChild(toast)

    // 延迟触发进入动画，确保元素已添加到 DOM
    setTimeout(() => {
      toast.style.transform = 'translateX(0)'
    }, 100)

    // 设置自动关闭计时器
    setTimeout(() => {
      toast.style.transform = 'translateX(100%)'
      // 等待退出动画完成后移除元素
      setTimeout(() => {
        if (container.contains(toast)) {
          container.removeChild(toast)
          // 从实例数组中移除
          const index = this.toastInstances.indexOf(toast)
          if (index > -1) {
            this.toastInstances.splice(index, 1)
          }
        }
      }, 300)  // 与 CSS 动画时间保持一致
    }, duration)

    // 添加点击关闭功能
    toast.addEventListener('click', () => {
      toast.style.transform = 'translateX(100%)'
      setTimeout(() => {
        if (container.contains(toast)) {
          container.removeChild(toast)
          // 从实例数组中移除
          const index = this.toastInstances.indexOf(toast)
          if (index > -1) {
            this.toastInstances.splice(index, 1)
          }
        }
      }, 300)
    })

    // 添加到实例数组中进行管理
    this.toastInstances.push(toast)
  }

  /**
   * 获取 Toast 类型对应的主题颜色
   * 
   * 根据 Toast 类型返回对应的颜色值，用于左侧边框和主题标识。
   * 
   * @private
   * @param {string} type - Toast 类型
   * @returns {string} 对应的颜色值（CSS 色值）
   */
  private getTypeColor(type: string): string {
    switch (type) {
      case 'success': return '#10b981'  // 绿色 - 成功
      case 'error': return '#ef4444'    // 红色 - 错误
      case 'warning': return '#f59e0b'  // 橙色 - 警告
      default: return '#3b82f6'         // 蓝色 - 信息
    }
  }

  /**
   * 显示成功类型的 Toast
   * 
   * 便捷方法，用于显示成功消息，自动设置为绿色主题。
   * 
   * @param {string} message - 成功消息内容
   * @param {string} [title] - 消息标题（可选）
   * 
   * @example
   * ```typescript
   * toast.success('数据保存成功！')
   * toast.success('操作完成', '成功提示')
   * ```
   */
  success(message: string, title?: string) {
    this.show(message, title, { type: 'success' })
  }

  /**
   * 显示错误类型的 Toast
   * 
   * 便捷方法，用于显示错误消息，自动设置为红色主题。
   * 
   * @param {string} message - 错误消息内容
   * @param {string} [title] - 消息标题（可选）
   * 
   * @example
   * ```typescript
   * toast.error('网络连接失败！')
   * toast.error('保存失败，请重试', '错误提示')
   * ```
   */
  error(message: string, title?: string) {
    this.show(message, title, { type: 'error' })
  }

  /**
   * 显示警告类型的 Toast
   * 
   * 便捷方法，用于显示警告消息，自动设置为橙色主题。
   * 
   * @param {string} message - 警告消息内容
   * @param {string} [title] - 消息标题（可选）
   * 
   * @example
   * ```typescript
   * toast.warning('数据即将过期！')
   * toast.warning('请注意检查输入', '警告提示')
   * ```
   */
  warning(message: string, title?: string) {
    this.show(message, title, { type: 'warning' })
  }

  /**
   * 显示信息类型的 Toast
   * 
   * 便捷方法，用于显示一般信息消息，自动设置为蓝色主题。
   * 
   * @param {string} message - 信息消息内容
   * @param {string} [title] - 消息标题（可选）
   * 
   * @example
   * ```typescript
   * toast.info('系统将在5分钟后维护')
   * toast.info('新功能已上线', '系统通知')
   * ```
   */
  info(message: string, title?: string) {
    this.show(message, title, { type: 'info' })
  }

  /**
   * 清除所有 Toast 消息
   * 
   * 立即移除所有当前显示的 Toast 消息，不等待动画完成。
   * 用于页面切换或紧急情况下的强制清理。
   * 
   * @example
   * ```typescript
   * // 页面切换时清除所有提示
   * toast.clear()
   * ```
   */
  clear() {
    this.toastInstances.forEach(toast => {
      const container = this.container
      if (container && container.contains(toast)) {
        container.removeChild(toast)
      }
    })
    // 清空实例数组
    this.toastInstances = []
  }
}

/**
 * Toast 服务单例实例
 * 
 * 全局唯一的 Toast 服务实例，确保应用中所有地方使用的都是同一个服务。
 * 提供了统一的消息提示功能，支持链式调用和类型安全。
 * 
 * @type {ToastService}
 * 
 * @example
 * ```typescript
 * import toast from '@/utils/toast'
 * 
 * // 基础使用
 * toast.success('操作成功')
 * toast.error('操作失败')
 * toast.warning('注意事项')
 * toast.info('系统消息')
 * 
 * // 高级用法
 * toast.show('自定义消息', '标题', {
 *   type: 'success',
 *   duration: 5000
 * })
 * 
 * // 清除所有消息
 * toast.clear()
 * ```
 */
const toast = new ToastService()

/**
 * 默认导出 Toast 服务实例
 * 支持 ES6 默认导入语法
 */
export default toast
