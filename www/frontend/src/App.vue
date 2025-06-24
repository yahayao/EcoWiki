<template>
  <div id="app">
    <!-- 登录模态框 -->
    <div v-if="showLoginForm" class="auth-modal-overlay" @click.self="closeModals">
      <div class="auth-modal">
        <LoginPanel 
          @switchToRegister="switchToRegister" 
          @loginSuccess="closeModals"
        />
        <button class="close-button" @click="closeModals">×</button>
      </div>
    </div>

    <!-- 注册模态框 -->
    <div v-if="showRegisterForm" class="auth-modal-overlay" @click.self="closeModals">
      <div class="auth-modal">
        <RegisterPanel 
          @switchToLogin="switchToLogin" 
          @registerSuccess="closeModals"
        />
        <button class="close-button" @click="closeModals">×</button>
      </div>
    </div>

    <!-- 管理员设置模态框 -->
    <div v-if="showAdminSettings" class="auth-modal-overlay" @click.self="closeModals">
      <div class="admin-modal">
        <AdminSettings />
        <button class="close-button" @click="closeModals">×</button>
      </div>
    </div>

    <!-- 主页面内容 -->
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
            <div class="title-group">
              <h1 class="site-title">EcoWiki</h1>
              <span class="site-subtitle">知识百科</span>
            </div>
          </div>

          <!-- 搜索栏 -->
          <div class="search-container">
            <div class="search-wrapper">
              <input type="text" class="search-input" placeholder="搜索知识内容..." />
              <button class="search-button">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M21 21L16.514 16.506L21 21ZM19 10.5C19 15.194 15.194 19 10.5 19C5.806 19 2 15.194 2 10.5C2 5.806 5.806 2 10.5 2C15.194 2 19 5.806 19 10.5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- 用户区域 -->
          <div class="user-area">
            <template v-if="isAuthenticated">
              <div class="user-info">
                <div class="user-avatar-wrapper">
                  <img :src="userAvatar" alt="用户头像" class="user-avatar" />
                </div>
                <span class="username">{{ user?.username }}</span>
              </div>
              
              <!-- 管理员设置按钮 -->
              <button 
                v-if="hasAdminPermission" 
                class="action-button settings-button" 
                @click="showAdminModal"
                title="系统设置"
              >
                ⚙️ 设置
              </button>
              
              <button class="action-button logout-button" @click="handleLogout">登出</button>
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
            <h2 class="sidebar-title">知识分类</h2>
            <nav class="categories-nav">
              <ul class="category-list">
                <li class="category-item">
                  <a href="#" class="category-link">
                    <span class="category-icon">📚</span>
                    学术研究
                  </a>
                </li>
                <li class="category-item">
                  <a href="#" class="category-link">
                    <span class="category-icon">💡</span>
                    科技创新
                  </a>
                </li>
                <li class="category-item">
                  <a href="#" class="category-link">
                    <span class="category-icon">🌍</span>
                    文化历史
                  </a>
                </li>
                <li class="category-item">
                  <a href="#" class="category-link">
                    <span class="category-icon">🔬</span>
                    科学发现
                  </a>
                </li>
                <li class="category-item">
                  <a href="#" class="category-link">
                    <span class="category-icon">🎨</span>
                    艺术人文
                  </a>
                </li>
                <li class="category-item">
                  <a href="#" class="category-link">
                    <span class="category-icon">⚡</span>
                    技术前沿
                  </a>
                </li>
              </ul>
            </nav>
          </aside>

          <!-- 内容区 -->
          <section class="content-area">
            <div class="featured-articles">
              <h2 class="section-title">🔥 热门内容</h2>
              <div class="article-grid">
                <!-- 示例文章卡片 -->
                <div class="article-card">
                  <div class="article-header">
                    <span class="article-category">学术研究</span>
                    <span class="article-rating">⭐ 4.8</span>
                  </div>
                  <h3 class="article-title">人工智能发展史与未来展望</h3>
                  <p class="article-excerpt">从图灵测试到ChatGPT，探索人工智能技术的发展历程及其对未来社会的深远影响...</p>
                  <div class="article-meta">
                    <span class="article-author">
                      <span class="author-icon">👤</span>
                      科技研究员
                    </span>
                    <span class="article-date">2025-01-15</span>
                  </div>
                </div>
                
                <div class="article-card">
                  <div class="article-header">
                    <span class="article-category">文化历史</span>
                    <span class="article-rating">⭐ 4.9</span>
                  </div>
                  <h3 class="article-title">古代丝绸之路的文化交流</h3>
                  <p class="article-excerpt">追溯古代丝绸之路上的商贸往来与文化交融，了解东西方文明的碰撞与交流...</p>
                  <div class="article-meta">
                    <span class="article-author">
                      <span class="author-icon">👤</span>
                      历史学者
                    </span>
                    <span class="article-date">2025-01-12</span>
                  </div>
                </div>
                
                <div class="article-card">
                  <div class="article-header">
                    <span class="article-category">科技创新</span>
                    <span class="article-rating">⭐ 4.7</span>
                  </div>
                  <h3 class="article-title">量子计算原理与应用前景</h3>
                  <p class="article-excerpt">深入浅出地解释量子计算的基本原理，探讨其在密码学、药物研发等领域的应用...</p>
                  <div class="article-meta">
                    <span class="article-author">
                      <span class="author-icon">👤</span>
                      物理学家
                    </span>
                    <span class="article-date">2025-01-10</span>
                  </div>
                </div>
                
                <div class="article-card">
                  <div class="article-header">
                    <span class="article-category">艺术人文</span>
                    <span class="article-rating">⭐ 4.6</span>
                  </div>
                  <h3 class="article-title">文艺复兴时期的艺术革新</h3>
                  <p class="article-excerpt">从达芬奇到米开朗基罗，探索文艺复兴时期艺术家们如何改变世界对美的认知...</p>
                  <div class="article-meta">
                    <span class="article-author">
                      <span class="author-icon">👤</span>
                      艺术史家
                    </span>
                    <span class="article-date">2025-01-08</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="recent-updates">
              <h2 class="section-title">📝 最近更新</h2>
              <div class="updates-list">
                <div class="update-item">
                  <div class="update-icon">🆕</div>
                  <div class="update-content">
                    <span class="update-time">2025-01-15 14:30</span>
                    <span class="update-title">新增机器学习算法详解章节</span>
                    <span class="update-author">算法工程师</span>
                  </div>
                </div>
                <div class="update-item">
                  <div class="update-icon">📊</div>
                  <div class="update-content">
                    <span class="update-time">2025-01-14 09:15</span>
                    <span class="update-title">更新全球经济数据图表</span>
                    <span class="update-author">经济分析师</span>
                  </div>
                </div>
                <div class="update-item">
                  <div class="update-icon">🔬</div>
                  <div class="update-content">
                    <span class="update-time">2025-01-13 16:45</span>
                    <span class="update-title">完善化学元素周期表介绍</span>
                    <span class="update-author">化学专家</span>
                  </div>
                </div>
                <div class="update-item">
                  <div class="update-icon">🎭</div>
                  <div class="update-content">
                    <span class="update-time">2025-01-12 11:20</span>
                    <span class="update-title">添加世界文学经典作品赏析</span>
                    <span class="update-author">文学评论家</span>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </main>

      <!-- 页脚 -->
      <footer class="footer">
        <div class="container">
          <div class="footer-content">
            <div class="footer-section">
              <h3 class="footer-title">EcoWiki</h3>
              <p class="footer-description">开放环境，共享知识的百科平台</p>
            </div>
            <div class="footer-section">
              <h4 class="footer-subtitle">快速链接</h4>
              <div class="footer-links">
                <a href="#" class="footer-link">关于我们</a>
                <a href="#" class="footer-link">使用指南</a>
                <a href="#" class="footer-link">贡献内容</a>
              </div>
            </div>
            <div class="footer-section">
              <h4 class="footer-subtitle">帮助支持</h4>
              <div class="footer-links">
                <a href="#" class="footer-link">使用条款</a>
                <a href="#" class="footer-link">隐私政策</a>
                <a href="#" class="footer-link">联系我们</a>
              </div>
            </div>
          </div>
          <div class="footer-bottom">
            <p class="copyright">&copy; 2025 EcoWiki. 保留所有权利。</p>
          </div>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import LoginPanel from './components/LoginPanel.vue'
import RegisterPanel from './components/RegisterPanel.vue'
import AdminSettings from './components/AdminSettings.vue'
import { useAuth } from './composables/useAuth'
import { userApi } from './api/user'

// 获取认证状态
const { user, isAuthenticated, userAvatar, clearUser } = useAuth()

// 控制登录和注册表单的显示
const showLoginForm = ref(false)
const showRegisterForm = ref(false)
const showAdminSettings = ref(false)

// 监听模态框状态，控制页面滚动
watch([showLoginForm, showRegisterForm, showAdminSettings], ([login, register, admin]) => {
  const isModalOpen = login || register || admin
  if (isModalOpen) {
    document.body.classList.add('modal-open')
  } else {
    document.body.classList.remove('modal-open')
  }
})

// 检查是否有管理员权限
const hasAdminPermission = computed(() => {
  return userApi.isAdmin(user.value)
})

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
/* 整体样式 */
#app {
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', sans-serif;
}

.wiki-home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f8fafc;
  color: #1a202c;
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
  padding: 16px 0;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* Logo 样式 - 仿照图片中的设计 */
.logo-circle {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #8B5CF6 0%, #A855F7 50%, #C084FC 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(139, 92, 246, 0.3);
  position: relative;
  overflow: hidden;
}

.logo-circle::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(135deg, #8B5CF6, #A855F7, #C084FC, #8B5CF6);
  border-radius: 50%;
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.logo-circle:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 30px rgba(139, 92, 246, 0.4);
}

.logo-circle:hover::before {
  opacity: 1;
}

.logo-text {
  color: white;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: -0.5px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  z-index: 1;
  position: relative;
}

.title-group {
  display: flex;
  flex-direction: column;
}

.site-title {
  font-size: 1.8rem;
  font-weight: 700;
  margin: 0;
  line-height: 1.2;
  letter-spacing: -0.5px;
}

.site-subtitle {
  font-size: 0.85rem;
  opacity: 0.9;
  font-weight: 400;
  margin-top: -2px;
}

/* 搜索栏 */
.search-container {
  flex: 1;
  max-width: 500px;
  margin: 0 20px;
}

.search-wrapper {
  position: relative;
  width: 100%;
}

.search-input {
  width: 100%;
  padding: 12px 50px 12px 20px;
  border: none;
  border-radius: 25px;
  font-size: 1rem;
  outline: none;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.search-input:focus {
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.15);
  background: white;
}

.search-input::placeholder {
  color: #718096;
}

.search-button {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  color: #667eea;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-button:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: translateY(-50%) scale(1.1);
}

/* 用户区域 */
.user-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar-wrapper {
  position: relative;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.user-avatar:hover {
  border-color: rgba(255, 255, 255, 0.6);
  transform: scale(1.05);
}

.username {
  font-weight: 500;
  font-size: 0.95rem;
}

.action-button {
  padding: 10px 20px;
  border: none;
  border-radius: 22px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  position: relative;
  overflow: hidden;
}

.action-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s;
}

.action-button:hover::before {
  left: 100%;
}

.login-button {
  background-color: transparent;
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.7);
}

.login-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
  border-color: white;
  transform: translateY(-1px);
}

.register-button {
  background-color: white;
  color: #667eea;
}

.register-button:hover {
  background-color: #f7fafc;
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.logout-button {
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.logout-button:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

/* 主内容区 */
.main-content {
  flex: 1;
  padding: 40px 0;
}

.main-content .container {
  display: flex;
  gap: 30px;
}

/* 侧边栏 */
.sidebar {
  width: 280px;
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  height: fit-content;
  border: 1px solid rgba(226, 232, 240, 0.8);
}

.sidebar-title {
  font-size: 1.3rem;
  margin-top: 0;
  margin-bottom: 20px;
  color: #1a202c;
  font-weight: 600;
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.category-item {
  margin-bottom: 8px;
}

.category-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  color: #4a5568;
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.2s ease;
  font-weight: 500;
}

.category-link:hover {
  background-color: #f0f4ff;
  color: #667eea;
  transform: translateX(4px);
}

.category-icon {
  font-size: 1.1rem;
}

/* 内容区 */
.content-area {
  flex: 1;
}

.section-title {
  font-size: 1.5rem;
  margin-top: 0;
  margin-bottom: 24px;
  color: #1a202c;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 50px;
}

.article-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  padding: 24px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid rgba(226, 232, 240, 0.8);
  position: relative;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  border-color: #667eea;
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.article-category {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.article-rating {
  font-size: 0.85rem;
  color: #f6ad55;
  font-weight: 500;
}

.article-title {
  font-size: 1.2rem;
  margin: 0 0 12px 0;
  color: #1a202c;
  font-weight: 600;
  line-height: 1.4;
}

.article-excerpt {
  color: #718096;
  line-height: 1.6;
  margin-bottom: 16px;
  font-size: 0.95rem;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #a0aec0;
  font-size: 0.85rem;
}

.article-author {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.author-icon {
  font-size: 0.8rem;
}

.article-date {
  color: #cbd5e0;
}

/* 最近更新 */
.recent-updates {
  margin-top: 50px;
}

.updates-list {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(226, 232, 240, 0.8);
}

.update-item {
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.2s ease;
}

.update-item:hover {
  background-color: #f8fafc;
}

.update-item:last-child {
  border-bottom: none;
}

.update-icon {
  font-size: 1.2rem;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.update-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.update-time {
  color: #a0aec0;
  font-size: 0.8rem;
  font-weight: 500;
}

.update-title {
  color: #1a202c;
  font-weight: 500;
  font-size: 0.95rem;
}

.update-author {
  color: #667eea;
  font-size: 0.85rem;
  font-weight: 500;
}

/* 页脚 */
.footer {
  background: linear-gradient(135deg, #1a202c 0%, #2d3748 100%);
  color: #e2e8f0;
  padding: 40px 0 20px;
  margin-top: auto;
}

.footer-content {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 40px;
  margin-bottom: 30px;
}

.footer-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-title {
  font-size: 1.3rem;
  font-weight: 600;
  margin: 0;
  color: white;
}

.footer-subtitle {
  font-size: 1rem;
  font-weight: 500;
  margin: 0;
  color: white;
}

.footer-description {
  color: #a0aec0;
  font-size: 0.9rem;
  line-height: 1.5;
  margin: 0;
}

.footer-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.footer-link {
  color: #cbd5e0;
  text-decoration: none;
  transition: color 0.2s ease;
  font-size: 0.9rem;
}

.footer-link:hover {
  color: #667eea;
}

.footer-bottom {
  border-top: 1px solid #4a5568;
  padding-top: 20px;
  text-align: center;
}

.copyright {
  margin: 0;
  color: #a0aec0;
  font-size: 0.85rem;
}

/* 防止模态框显示时页面滚动 */
:global(body.modal-open) {
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
  overflow: hidden;
  backdrop-filter: blur(4px);
}

.auth-modal {
  position: relative;
  max-width: 90%;
  max-height: 85vh;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: visible;
}

.close-button {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.5);
  border: none;
  font-size: 24px;
  color: white;
  cursor: pointer;
  z-index: 1001;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.close-button:hover {
  background: rgba(0, 0, 0, 0.7);
  transform: scale(1.1);
}

/* 新增管理员模态框样式 */
.admin-modal {
  position: relative;
  max-width: 95%;
  max-height: 90vh;
  overflow: auto;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

/* 设置按钮样式 */
.settings-button {
  background: linear-gradient(135deg, #38b2ac, #319795);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.settings-button:hover {
  background: linear-gradient(135deg, #319795, #2c7a7b);
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(56, 178, 172, 0.3);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-content .container {
    flex-direction: column;
    gap: 24px;
  }
  
  .sidebar {
    width: 100%;
  }
  
  .category-list {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 12px;
  }
  
  .category-item {
    margin-bottom: 0;
  }
  
  .footer-content {
    grid-template-columns: 1fr;
    gap: 30px;
  }
}

@media (max-width: 768px) {
  .header-container {
    flex-direction: column;
    gap: 16px;
  }
  
  .logo-container {
    order: 1;
  }
  
  .search-container {
    order: 2;
    max-width: 100%;
    margin: 0;
  }
  
  .user-area {
    order: 3;
  }
  
  .article-grid {
    grid-template-columns: 1fr;
  }
  
  .update-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .update-content {
    width: 100%;
  }
  
  .admin-modal {
    max-width: 100%;
    max-height: 100vh;
    border-radius: 0;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 15px;
  }
  
  .logo-container {
    flex-direction: column;
    text-align: center;
    gap: 8px;
  }
  
  .title-group {
    align-items: center;
  }
  
  .user-area {
    flex-direction: column;
    gap: 8px;
    width: 100%;
  }
  
  .action-button {
    width: 100%;
    text-align: center;
  }
  
  .sidebar {
    padding: 20px;
  }
  
  .category-list {
    grid-template-columns: 1fr;
  }
  
  .article-card {
    padding: 20px;
  }
  
  .update-item {
    padding: 16px 20px;
  }
  
  /* 移动端Logo调整 */
  .logo-circle {
    width: 48px;
    height: 48px;
  }
  
  .logo-text {
    font-size: 10px;
  }
}
</style>