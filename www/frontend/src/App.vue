<template>
  <div id="app">
    <!-- 认证模态框 -->
    <AuthModals 
      :showLoginForm="showLoginForm"
      :showRegisterForm="showRegisterForm"
      :showAdminSettings="showAdminSettings"
      @closeModals="closeModals"
      @switchToRegister="switchToRegister"
      @switchToLogin="switchToLogin"
    />

    <!-- 主页面布局 -->
    <div class="wiki-home">
      <!-- 顶部导航栏 -->
      <AppHeader
        @showLogin="showLoginModal"
        @showRegister="showRegisterModal"
        @showAdminSettings="showAdminModal"
        @logout="handleLogout"
      />

      <!-- 主要内容区 -->
      <main class="main-content">
        <div class="container">
          <!-- 侧边导航 -->
          <AppSidebar />
          
          <!-- 内容区 -->
          <AppMainContent />
        </div>
      </main>

      <!-- 页脚 -->
      <AppFooter />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuth } from './composables/useAuth'
import AuthModals from './components/AuthModals.vue'
import AppHeader from './components/AppHeader.vue'
import AppSidebar from './components/AppSidebar.vue'
import AppMainContent from './components/AppMainContent.vue'
import AppFooter from './components/AppFooter.vue'

// 获取认证状态
const { clearUser } = useAuth()

// 控制登录和注册表单的显示
const showLoginForm = ref(false)
const showRegisterForm = ref(false)
const showAdminSettings = ref(false)

// 显示登录模态框
const showLoginModal = () => {
  showLoginForm.value = true
  showRegisterForm.value = false
  showAdminSettings.value = false
}

// 显示注册模态框
const showRegisterModal = () => {
  showRegisterForm.value = true
  showLoginForm.value = false
  showAdminSettings.value = false
}

// 显示管理员设置
const showAdminModal = () => {
  showAdminSettings.value = true
  showLoginForm.value = false
  showRegisterForm.value = false
}

// 切换到注册
const switchToRegister = () => {
  showLoginForm.value = false
  showRegisterForm.value = true
}

// 切换到登录
const switchToLogin = () => {
  showRegisterForm.value = false
  showLoginForm.value = true
}

// 关闭所有模态框
const closeModals = () => {
  showLoginForm.value = false
  showRegisterForm.value = false
  showAdminSettings.value = false
}

// 登出处理
const handleLogout = () => {
  clearUser()
}
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