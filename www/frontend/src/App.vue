<!--
/**
 * EcoWiki应用程序根组件
 * 
 * 功能包括：
 * - 应用程序根容器管理
 * - 全局模态框状态控制
 * - 用户认证状态管理
 * - 顶层事件处理和传递
 * - 路由视图渲染容器
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
-->

<template>
  <div id="app">
    <!-- 认证模态框组件 -->
    <!-- 包含登录、注册、管理后台三种模态框 -->
    <AuthModals 
      :showLoginForm="showLoginForm"
      :showRegisterForm="showRegisterForm"
      :showAdminSettings="showAdminSettings"
      :showUserProfile="showUserProfile"
      :showForgotPassword="showForgotPassword"
      @closeModals="closeModals"
      @switchToRegister="switchToRegister"
      @switchToLogin="switchToLogin"
      @switchToForgot="switchToForgot"
    />
    
    <!-- 主要内容区域 -->
    <!-- 路由视图，根据当前路由渲染对应的页面组件 -->
    <router-view
      @show-login="showLoginModal"
      @show-register="showRegisterModal"
      @show-admin="showAdminModal"
      @show-forgot-password="showForgotPasswordModal"
      @showUserProfile="showUserProfileModal"
      @logout="handleLogout"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from './composables/useAuth'
import AuthModals from './components/modals/AuthModals.vue'

/**
 * 获取认证状态管理功能
 * 使用组合式函数来管理用户认证状态
 */
const { clearUser } = useAuth()
const router = useRouter()

// ======================== 响应式状态 ========================

/**
 * 控制登录表单的显示状态
 */
const showLoginForm = ref(false)

/**
 * 控制注册表单的显示状态
 */
const showRegisterForm = ref(false)

/**
 * 控制用户个人资料的显示状态
 */
const showUserProfile = ref(false)
/**
 * 控制管理员设置的显示状态
 */
const showAdminSettings = ref(false)
/**
 * 控制忘记密码表单的显示状态
 */
const showForgotPassword = ref(false)

// ======================== 模态框控制方法 ========================

/**
 * 显示登录模态框
 * 同时隐藏其他模态框以确保只显示一个
 */
const showLoginModal = () => {
  showLoginForm.value = true
  showRegisterForm.value = false
  showAdminSettings.value = false
  showForgotPassword.value = false
}
// 显示忘记密码模态框
const showForgotPasswordModal = () => {
  showForgotPassword.value = true
  showLoginForm.value = false
  showRegisterForm.value = false
  showAdminSettings.value = false
}

/**
 * 显示注册模态框
 * 同时隐藏其他模态框以确保只显示一个
 */
const showRegisterModal = () => {
  showRegisterForm.value = true
  showLoginForm.value = false
  showAdminSettings.value = false
  showForgotPassword.value = false
}

/**
 * 显示管理员设置模态框
 * 同时隐藏其他模态框以确保只显示一个
 */
const showAdminModal = () => {
  showAdminSettings.value = true
  showLoginForm.value = false
  showRegisterForm.value = false
}

const showUserProfileModal = () => {
  console.log("显示用户信息");
  showUserProfile.value = true
  showLoginForm.value = false
  showRegisterForm.value = false
  showAdminSettings.value = false
  showForgotPassword.value = false
}

// 切换到注册
const switchToRegister = () => {
  showLoginForm.value = false
  showRegisterForm.value = true
  showForgotPassword.value = false
}

// 切换到登录
const switchToLogin = () => {
  showRegisterForm.value = false
  showLoginForm.value = true
  showForgotPassword.value = false
}

// 切换到忘记密码
const switchToForgot = () => {
  console.log("切换到忘记密码模态框");
  showForgotPassword.value = true
  showLoginForm.value = false
  showRegisterForm.value = false
}

// 关闭所有模态框
const closeModals = () => {
  showLoginForm.value = false
  showRegisterForm.value = false
  showAdminSettings.value = false
  showForgotPassword.value = false
  showUserProfile.value = false
}

// 登出处理
const handleLogout = () => {
  clearUser()
}

// 监听关闭管理后台模态框的事件
const handleCloseAdminModal = () => {
  showAdminSettings.value = false
}

// token 过期强制退出处理
const handleAuthExpired = () => {
  clearUser()
  closeModals()
  router.push('/')
}

// 组件挂载时添加事件监听器
onMounted(() => {
  window.addEventListener('close-admin-modal', handleCloseAdminModal)
  window.addEventListener('ecowiki-auth-expired', handleAuthExpired)
})

// 组件卸载时移除事件监听器
onUnmounted(() => {
  window.removeEventListener('close-admin-modal', handleCloseAdminModal)
  window.removeEventListener('ecowiki-auth-expired', handleAuthExpired)
})
</script>

<style scoped>
/* 整体应用样式 */
#app {
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', sans-serif;
}

/* 主页面布局 */
.wiki-home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f8fafc;
  color: #1a202c;
}

/* 通用容器样式 */
.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 主内容区布局 */
.main-content {
  flex: 1;
  padding: 40px 0;
}

.main-content .container {
  display: flex;
  gap: 30px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-content .container {
    flex-direction: column;
    gap: 24px;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 15px;
  }
  
  .main-content {
    padding: 20px 0;
  }
}

</style>