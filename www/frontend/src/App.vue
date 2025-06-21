<template>
  <div class="wiki-home">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="container header-container">
        <!-- Logo和标题 -->
        <div class="logo-container">
          <div class="logo">
            <div class="logo-circle">
              <span class="logo-text">EcoWiki</span>
            </div>
          </div>
          <h1 class="site-title">EcoWiki</h1>
        </div>

        <!-- 搜索栏 -->
        <div class="search-container">
          <input type="text" class="search-input" placeholder="搜索wiki内容..." />
          <button class="search-button">
            <span class="search-icon">🔍</span>
          </button>
        </div>

        <!-- 用户区域 -->
        <div class="user-area">
          <template v-if="isAuthenticated">
            <div class="user-info">
              <img :src="userAvatar" alt="用户头像" class="user-avatar" />
              <span class="username">{{ user?.username }}</span>
            </div>
            <button class="action-button" @click="handleLogout">登出</button>
          </template>
          <template v-else>
            <button class="action-button login-button" @click="showLoginModal">登录</button>
            <button class="action-button register-button" @click="showRegisterModal">注册</button>
          </template>
        </div>
      </div>
    </header>

    <!-- 主要内容区 -->
    <main class="main-content">
      <div class="container">
        <!-- 侧边导航 -->
        <aside class="sidebar">
          <h2 class="sidebar-title">分类</h2>
          <nav class="categories-nav">
            <ul class="category-list">
              <li class="category-item">
                <a href="#" class="category-link">生态系统</a>
              </li>
              <li class="category-item">
                <a href="#" class="category-link">物种百科</a>
              </li>
              <li class="category-item">
                <a href="#" class="category-link">环境保护</a>
              </li>
              <li class="category-item">
                <a href="#" class="category-link">气候变化</a>
              </li>
              <li class="category-item">
                <a href="#" class="category-link">可持续发展</a>
              </li>
            </ul>
          </nav>
        </aside>

        <!-- 内容区 -->
        <section class="content-area">
          <div class="featured-articles">
            <h2 class="section-title">热门内容</h2>
            <div class="article-grid">
              <!-- 此处会放置文章卡片 -->
              <div class="article-card">
                <h3 class="article-title">文章标题区域</h3>
                <p class="article-excerpt">文章摘要区域，展示文章的简要内容...</p>
                <div class="article-meta">
                  <span class="article-author">作者信息</span>
                  <span class="article-date">发布日期</span>
                </div>
              </div>
              <!-- 更多文章卡片 -->
            </div>
          </div>

          <div class="recent-updates">
            <h2 class="section-title">最近更新</h2>
            <div class="updates-list">
              <!-- 此处会放置最近更新的内容列表 -->
              <div class="update-item">
                <span class="update-time">更新时间</span>
                <span class="update-title">更新内容标题</span>
                <span class="update-author">更新作者</span>
              </div>
              <!-- 更多更新项 -->
            </div>
          </div>
        </section>
      </div>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="container">
        <p class="copyright">&copy; 2025 EcoWiki. 保留所有权利。</p>
        <div class="footer-links">
          <a href="#" class="footer-link">关于我们</a>
          <a href="#" class="footer-link">使用条款</a>
          <a href="#" class="footer-link">隐私政策</a>
          <a href="#" class="footer-link">联系我们</a>
        </div>
      </div>
    </footer>

    <!-- 登录模态框 -->
    <div v-if="showLoginForm" class="auth-modal-overlay" @click.self="closeModals">
      <div class="auth-modal">
        <LoginPanel @switchToRegister="switchToRegister" />
      </div>
    </div>

    <!-- 注册模态框 -->
    <div v-if="showRegisterForm" class="auth-modal-overlay" @click.self="closeModals">
      <div class="auth-modal">
        <RegisterPanel @switchToLogin="switchToLogin" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import LoginPanel from './components/LoginPanel.vue'
import RegisterPanel from './components/RegisterPanel.vue'
import { useAuth } from './composables/useAuth'

// 获取认证状态
const { user, isAuthenticated, userAvatar, clearUser } = useAuth()

// 控制登录和注册表单的显示
const showLoginForm = ref(false)
const showRegisterForm = ref(false)

// 监听模态框状态，控制页面滚动
watch([showLoginForm, showRegisterForm], ([login, register]) => {
  const isModalOpen = login || register
  if (isModalOpen) {
    document.body.classList.add('modal-open')
  } else {
    document.body.classList.remove('modal-open')
  }
})

// 显示登录模态框
const showLoginModal = () => {
  showLoginForm.value = true
  showRegisterForm.value = false
}

// 显示注册模态框
const showRegisterModal = () => {
  showRegisterForm.value = true
  showLoginForm.value = false
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
}

// 登出处理
const handleLogout = () => {
  clearUser()
}
</script>

<style scoped>
/* 整体样式 */
.wiki-home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  color: #333;
}

.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 顶部导航栏 */
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 15px 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-container {
  display: flex;
  align-items: center;
}

.logo {
  margin-right: 15px;
}

.logo-circle {
  width: 40px;
  height: 40px;
  background-color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  color: #667eea;
  font-weight: bold;
  font-size: 12px;
}

.site-title {
  font-size: 1.5rem;
  margin: 0;
}

/* 搜索栏 */
.search-container {
  flex: 1;
  max-width: 500px;
  margin: 0 20px;
  position: relative;
}

.search-input {
  width: 100%;
  padding: 10px 15px;
  border: none;
  border-radius: 25px;
  font-size: 1rem;
  outline: none;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.search-button {
  position: absolute;
  right: 5px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
}

/* 用户区域 */
.user-area {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  margin-right: 15px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 8px;
}

.action-button {
  padding: 8px 15px;
  border: none;
  border-radius: 20px;
  font-weight: 500;
  cursor: pointer;
  margin-left: 10px;
  transition: all 0.3s ease;
}

.login-button {
  background-color: transparent;
  color: white;
  border: 1px solid white;
}

.login-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.register-button {
  background-color: white;
  color: #667eea;
}

.register-button:hover {
  background-color: #f5f5f5;
}

/* 主内容区 */
.main-content {
  flex: 1;
  padding: 30px 0;
}

.main-content .container {
  display: flex;
  gap: 30px;
}

/* 侧边栏 */
.sidebar {
  width: 250px;
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.sidebar-title {
  font-size: 1.2rem;
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.category-item {
  margin-bottom: 10px;
}

.category-link {
  display: block;
  padding: 8px 10px;
  color: #555;
  text-decoration: none;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.category-link:hover {
  background-color: #f0f4ff;
  color: #667eea;
}

/* 内容区 */
.content-area {
  flex: 1;
}

.section-title {
  font-size: 1.4rem;
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.article-card {
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  padding: 20px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.article-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.article-title {
  margin-top: 0;
  font-size: 1.2rem;
  color: #333;
}

.article-excerpt {
  color: #666;
  margin-bottom: 15px;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  color: #888;
  font-size: 0.85rem;
}

.recent-updates {
  margin-top: 40px;
}

.updates-list {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.update-item {
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
}

.update-item:last-child {
  border-bottom: none;
}

/* 页脚 */
.footer {
  background-color: #333;
  color: #ccc;
  padding: 30px 0;
  margin-top: auto;
}

.footer .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.copyright {
  margin: 0;
}

.footer-links {
  display: flex;
  gap: 20px;
}

.footer-link {
  color: #ccc;
  text-decoration: none;
  transition: color 0.2s ease;
}

.footer-link:hover {
  color: white;
}

/* 防止模态框显示时页面滚动 */
body.modal-open {
  overflow: hidden;
}

/* 认证模态框 */
.auth-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  overflow: hidden; /* 防止外层出现滚动条 */
}

.auth-modal {
  position: relative;
  max-width: 90%;
  max-height: 85vh;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: visible; /* 允许动画效果正常显示 */
}

.close-button {
  position: absolute;
  top: 10px;
  right: 10px;
  background: none;
  border: none;
  font-size: 24px;
  color: white;
  cursor: pointer;
  z-index: 1001;
}
</style>