/**
 * EcoWiki前端应用程序入口模块
 * 
 * 功能包括：
 * - Vue3应用实例创建和配置
 * - Pinia状态管理初始化
 * - Vue Router路由系统配置
 * - 全局样式和插件加载
 * - 应用程序DOM挂载
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
// 导入全局样式文件
import './assets/main.css'

// 导入Vue3核心函数和插件
import { createApp } from 'vue'

import { createPinia } from 'pinia'

// 导入根组件和路由配置
import App from './App.vue'
import router from './router'

import CodeDiff from 'v-code-diff'
// 创建Vue应用实例
const app = createApp(App)

// 创建Pinia状态管理实例
const pinia = createPinia()

app.use(CodeDiff)

// 使用Pinia状态管理
// Pinia是Vue3推荐的状态管理库，用于管理全局状态
app.use(pinia)

// 使用Vue Router路由管理
// 处理单页面应用的路由导航
app.use(router)

// 挂载应用到DOM元素#app
// 这会将整个Vue应用渲染到index.html中的<div id="app"></div>元素
app.mount('#app')
