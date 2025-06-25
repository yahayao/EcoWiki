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
          <button class="nav-btn" @click="$emit('show-login')">登录</button>
          <button class="nav-btn" @click="$emit('show-register')">注册</button>
          <button class="nav-btn" @click="$emit('show-admin')">管理设置</button>
        </div>
      </div>
    </header>

    <!-- 主要内容区 -->
    <main class="main-content">
      <div class="container main-container">
        <!-- 侧边导航 -->
        <aside class="sidebar">
          <!-- 示例：你可以在这里放分类、导航等 -->
          <div class="sidebar-title">导航</div>
          <ul class="sidebar-list">
            <li><a href="#">首页</a></li>
            <li><a href="#">分类</a></li>
            <li><a href="#">热门</a></li>
            <li><a href="#">最近更新</a></li>
          </ul>
        </aside>

        <!-- 内容区 -->
        <section class="content-area">
          <!-- 示例内容，可替换为实际内容 -->
          <div class="welcome-card">
            <h2>欢迎来到 EcoWiki</h2>
            <p>这是一个开放的知识百科平台，您可以浏览、搜索和贡献内容。</p>
          </div>
          <!-- 这里可以插入热门内容、推荐、公告等 -->
        </section>
      </div>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="container footer-container">
        <span>© 2025 EcoWiki. All rights reserved.</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
const emit = defineEmits(['show-login', 'show-register', 'show-admin'])
// 如有需要可引入相关逻辑
</script>

<style scoped>
.wiki-home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
}

/* 顶部导航栏 */
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
  padding: 0.5rem 0;
}
.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
}
.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}
.logo {
  display: flex;
  align-items: center;
}
.logo-circle {
  width: 40px;
  height: 40px;
  background: #4f8cff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.logo-text {
  color: #fff;
  font-weight: bold;
  font-size: 1.1rem;
}
.title-group {
  display: flex;
  flex-direction: column;
}
.site-title {
  font-size: 1.5rem;
  font-weight: bold;
  margin: 0;
  color: #22223b;
}
.site-subtitle {
  color: #718096;
  font-size: 1rem;
  margin-top: 2px;
}

/* 搜索栏 */
.search-container {
  flex: 1;
  display: flex;
  justify-content: center;
}
.search-wrapper {
  display: flex;
  align-items: center;
  background: #f1f5f9;
  border-radius: 8px;
  padding: 4px 10px;
}
.search-input {
  border: none;
  background: transparent;
  outline: none;
  padding: 6px 8px;
  font-size: 1rem;
  width: 180px;
}
.search-button {
  background: none;
  border: none;
  cursor: pointer;
  color: #4f8cff;
  padding: 0 4px;
  display: flex;
  align-items: center;
}

/* 用户区域 */
.user-area {
  display: flex;
  align-items: center;
  gap: 12px;
}
.nav-btn {
  padding: 0.5rem 1.2rem;
  background: #4f8cff;
  color: #fff;
  border-radius: 8px;
  text-decoration: none;
  font-size: 1rem;
  border: none;
  cursor: pointer;
  transition: background 0.2s;
}
.nav-btn:hover {
  background: #2563eb;
}

/* 主要内容区 */
.main-content {
  flex: 1;
  width: 100%;
  padding: 2rem 0;
}
.main-container {
  display: flex;
  max-width: 1200px;
  margin: 0 auto;
  gap: 2rem;
}
.sidebar {
  width: 200px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  padding: 1.2rem 1rem;
  min-height: 300px;
}
.sidebar-title {
  font-weight: bold;
  margin-bottom: 1rem;
  color: #4f8cff;
}
.sidebar-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.sidebar-list li {
  margin-bottom: 0.7rem;
}
.sidebar-list a {
  color: #22223b;
  text-decoration: none;
  transition: color 0.2s;
}
.sidebar-list a:hover {
  color: #4f8cff;
}

/* 内容区 */
.content-area {
  flex: 1;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  padding: 2rem;
  min-height: 300px;
}
.welcome-card {
  text-align: center;
  margin-bottom: 2rem;
}
.welcome-card h2 {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}
.welcome-card p {
  color: #718096;
}

/* 页脚 */
.footer {
  background: #fff;
  padding: 1rem 0;
  border-top: 1px solid #e2e8f0;
}
.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  color: #718096;
  font-size: 0.95rem;
}
</style>