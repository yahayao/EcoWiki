/**
 * EcoWiki前端应用程序入口文件
 * 
 * 这是Vue3应用程序的主入口点，负责创建Vue应用实例并配置全局设置。
 * 该文件初始化了应用程序的核心组件和插件。
 * 
 * 主要功能：
 * - 创建Vue3应用实例
 * - 配置Pinia状态管理
 * - 配置Vue Router路由
 * - 导入全局样式
 * - 挂载应用到DOM
 * 
 * 技术栈：
 * - Vue3 (Composition API)
 * - TypeScript
 * - Pinia (状态管理)
 * - Vue Router (路由管理)
 * - Vite (构建工具)
 * 
 * @author EcoWiki Team
 * @version 2.0
 * @since 2025-06-30
 */

// 导入全局样式文件
import './assets/main.css'

// 导入Vue3核心函数和插件
import { createApp } from 'vue'

// 导入性能监控模块
import { performanceMonitor } from './utils/performance-monitor'
import { createPinia } from 'pinia'

// 导入 Service Worker 和缓存工具
import { registerServiceWorker } from './utils/service-worker'
import { dataPreloader, createDataPreloaderPlugin } from './utils/data-preloader'

// 导入根组件和路由配置
import App from './App.vue'
import router from './router'

import CodeDiff from 'v-code-diff'
// 创建Vue应用实例
const app = createApp(App)

// 创建Pinia状态管理实例
const pinia = createPinia()

app.use(CodeDiff)

// 使用数据预加载插件
app.use(createDataPreloaderPlugin())

// 使用Pinia状态管理
// Pinia是Vue3推荐的状态管理库，用于管理全局状态
app.use(pinia)

// 使用Vue Router路由管理
// 处理单页面应用的路由导航
app.use(router)

// 挂载应用到DOM元素#app
// 这会将整个Vue应用渲染到index.html中的<div id="app"></div>元素
app.mount('#app')

// 注册 Service Worker 用于离线缓存
if ('serviceWorker' in navigator && import.meta.env.PROD) {
  registerServiceWorker({
    enabled: true,
    scope: '/',
    updateOnReload: true
  }).then((success) => {
    if (success) {
      console.log('Service Worker registered successfully')
    } else {
      console.log('Service Worker registration failed')
    }
  })
}

// 在开发环境下启用性能监控
if (import.meta.env.DEV) {
  performanceMonitor.init()
  
  // 5秒后输出性能报告
  setTimeout(() => {
    performanceMonitor.reportToConsole()
  }, 5000)
}
