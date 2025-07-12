<!--
  动态首页组件
  
  这是一个智能的首页容器组件，根据用户设置动态切换不同的首页风格。
  支持经典风格和简洁风格之间的实时切换，提供个性化的用户体验。
  
  主要功能：
  - 动态组件加载：根据用户偏好加载不同的首页组件
  - 实时风格切换：监听设置变更事件，立即更新首页风格
  - 事件透传：将子组件事件透传给父组件
  - 本地存储集成：读取用户的首页风格偏好设置
  
  支持的首页风格：
  - Classic（经典风格）：传统的Wiki首页布局，功能丰富
  - Simple（简洁风格）：极简的首页设计，快速访问
  
  设计特点：
  - 零延迟切换：使用Vue动态组件实现即时切换
  - 事件一致性：保持不同风格组件的事件接口一致
  - 状态同步：监听全局事件确保设置变更及时生效
  - 向下兼容：默认使用经典风格，确保用户体验连续性
  
  技术实现：
  - Vue 3 动态组件（component :is）
  - 全局事件监听（window.addEventListener）
  - LocalStorage状态持久化
  - 组件事件透传机制
  
  @author EcoWiki Team
  @version 1.0.0
  @since 2024-01-01
  
  @example
  <!-- 在路由中使用 --
  <DynamicHome 
    @show-login="handleShowLogin"
    @show-register="handleShowRegister"
    @show-admin="handleShowAdmin"
    @logout="handleLogout"
  />
-->
<template>
  <!-- 动态组件容器：根据用户设置渲染不同的首页组件 -->
  <component
    :is="currentHome"
    @show-login="emit('show-login')"
    @show-register="emit('show-register')"
    @show-admin="emit('show-admin')"
    @logout="emit('logout')"
    @showUserProfile="emit('showUserProfile')"
  />
</template>

<script setup lang="ts">
/**
 * 动态首页组件脚本
 * 
 * 实现首页风格的动态切换逻辑，包括本地存储读取、事件监听和组件切换。
 * 使用Vue 3 Composition API确保响应式更新和生命周期管理。
 */

import { ref, onMounted, onUnmounted, markRaw } from 'vue'
import ClassicHome from './ClassicHome.vue'
import SimpleHome from './SimpleHome.vue'

/**
 * 组件事件定义
 * 定义动态首页组件向父组件发送的事件，与子组件事件保持一致
 */
const emit = defineEmits(['show-login', 'show-register', 'show-admin', 'logout', 'showUserProfile'])

/**
 * 获取当前首页组件
 * 
 * 根据本地存储的用户偏好设置确定应该使用哪个首页组件。
 * 使用 markRaw 包装组件以避免将其变为响应式对象，提升性能。
 * 默认使用经典风格，确保在没有设置时的良好体验。
 * 
 * @returns {Component} Vue组件对象（ClassicHome 或 SimpleHome），经过markRaw处理
 */
function getHomeComponent() {
  const homeStyle = localStorage.getItem('homeStyle')
  return markRaw(homeStyle === 'simple' ? SimpleHome : ClassicHome)
}

/**
 * 当前首页组件的响应式引用
 * 存储当前应该渲染的首页组件，用于动态组件切换
 */
const currentHome = ref(getHomeComponent())

/**
 * 更新首页风格
 * 
 * 当接收到首页风格变更事件时调用，重新读取设置并更新当前组件。
 * 使用 markRaw 确保组件不会被包装成响应式对象，避免性能警告。
 * 这个函数会在用户在管理后台修改首页设置时被触发。
 * 
 * @example
 * // 当用户在系统设置中切换首页风格时：
 * // localStorage.setItem('homeStyle', 'simple')
 * // window.dispatchEvent(new CustomEvent('ecowiki-home-style-change'))
 * // updateHome() 会被自动调用
 */
function updateHome() {
  console.log('updateHome called, homeStyle:', localStorage.getItem('homeStyle'))
  currentHome.value = getHomeComponent()
}

/**
 * 组件挂载时的初始化
 * 添加全局事件监听器，监听首页风格变更事件
 */
onMounted(() => {
  window.addEventListener('ecowiki-home-style-change', updateHome)
})

/**
 * 组件卸载时的清理
 * 移除全局事件监听器，防止内存泄漏
 */
onUnmounted(() => {
  window.removeEventListener('ecowiki-home-style-change', updateHome)
})
</script>