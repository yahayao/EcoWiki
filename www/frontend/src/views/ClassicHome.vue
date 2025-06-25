<!-- filepath: c:\Users\Z9495\Desktop\EcoWiki_project\EcoWiki-3\www\frontend\src\views\ClassicHome.vue -->
<template>
  <div class="wiki-home">
    <!-- 顶部导航栏 -->
    <AppHeader
      @showLogin="$emit('show-login')"
      @showRegister="$emit('show-register')"
      @showAdminSettings="$emit('show-admin')"
      @logout="$emit('logout')"
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
</template>

<script setup lang="ts">
import AppHeader from '../components/AppHeader.vue'
import AppSidebar from '../components/AppSidebar.vue'
import AppMainContent from '../components/AppMainContent.vue'
import AppFooter from '../components/AppFooter.vue'

defineEmits(['show-login', 'show-register', 'show-admin', 'logout'])
</script>

<style scoped>
.wiki-home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
}
.main-content, .sidebar, .content-area {
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.08);
  background: #fff;
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