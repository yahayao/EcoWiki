<!--
/**
 * 动态首页组件
 * 
 * 功能：
 * - 智能切换不同风格的首页布局
 * - 根据用户偏好动态加载组件
 * - 支持经典风格和简洁风格实时切换
 * - 提供个性化的用户体验
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
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