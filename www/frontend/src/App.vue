<!--
/**
 * EcoWiki应用程序根组件
 * 
 * 这是整个Vue应用的根组件，作为所有其他组件的容器。
 * 负责管理全局的模态框状态和处理顶层的用户交互事件。
 * 
 * 主要功能：
 * - 管理认证模态框的显示状态（登录、注册、管理后台）
 * - 处理子组件发出的全局事件
 * - 提供路由视图的渲染容器
 * - 管理用户登出功能
 * - 监听管理后台关闭事件
 * 
 * 组件结构：
 * - AuthModals: 认证相关的模态框容器
 * - router-view: 路由视图，渲染当前路由对应的页面组件
 * 
 * 状态管理：
 * - showLoginForm: 控制登录表单显示
 * - showRegisterForm: 控制注册表单显示  
 * - showAdminSettings: 控制管理后台显示
 * 
 * 事件处理：
 * - show-login: 显示登录模态框
 * - show-register: 显示注册模态框
 * - show-admin: 显示管理后台
 * - logout: 处理用户登出
 * - admin-close: 关闭管理后台
 * 
 * @author EcoWiki Team
 * @version 2.0 (支持管理后台模态框)
 * @since 2025-06-30
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
      @showMessages="showMessagesModal"
      @logout="handleLogout"
    />

    <!-- 消息面板模态框 -->
    <div v-if="showMessages" class="modal-overlay" @click="closeModals">
      <div class="message-modal-container" @click.stop>
        <MessagePanel @close="closeModals" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useAuth } from './composables/useAuth'
import AuthModals from './components/modals/AuthModals.vue'
import MessagePanel from './components/modals/MessagePanel.vue'

/**
 * 获取认证状态管理功能
 * 使用组合式函数来管理用户认证状态
 */
const { clearUser } = useAuth()

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

/**
 * 控制消息面板的显示状态
 */
const showMessages = ref(false)

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
  showMessages.value = false
}
// 显示忘记密码模态框
const showForgotPasswordModal = () => {
  showForgotPassword.value = true
  showLoginForm.value = false
  showRegisterForm.value = false
  showAdminSettings.value = false
  showMessages.value = false
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
  showMessages.value = false
}

/**
 * 显示管理员设置模态框
 * 同时隐藏其他模态框以确保只显示一个
 */
const showAdminModal = () => {
  showAdminSettings.value = true
  showLoginForm.value = false
  showRegisterForm.value = false
  showMessages.value = false
}

const showUserProfileModal = () => {
  console.log("显示用户信息");
  showUserProfile.value = true
  showLoginForm.value = false
  showRegisterForm.value = false
  showAdminSettings.value = false
  showForgotPassword.value = false
  showMessages.value = false
}

/**
 * 显示消息面板
 * 同时隐藏其他模态框以确保只显示一个
 */
const showMessagesModal = () => {
  showMessages.value = true
  showLoginForm.value = false
  showRegisterForm.value = false
  showAdminSettings.value = false
  showForgotPassword.value = false
  showUserProfile.value = false
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
  showMessages.value = false
}

// 登出处理
const handleLogout = () => {
  clearUser()
}

// 监听关闭管理后台模态框的事件
const handleCloseAdminModal = () => {
  showAdminSettings.value = false
}

// 组件挂载时添加事件监听器
onMounted(() => {
  window.addEventListener('close-admin-modal', handleCloseAdminModal)
})

// 组件卸载时移除事件监听器
onUnmounted(() => {
  window.removeEventListener('close-admin-modal', handleCloseAdminModal)
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

/* 消息模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.message-modal-container {
  width: 100%;
  max-width: 900px;
  max-height: 90vh;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  border-radius: 16px;
  overflow: hidden;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-modal-container {
    margin: 0;
    border-radius: 0;
    max-height: 100vh;
    height: 100vh;
  }
  
  .modal-overlay {
    padding: 0;
  }
}
</style>