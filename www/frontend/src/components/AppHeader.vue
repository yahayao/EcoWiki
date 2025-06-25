<template>
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
            @click="$emit('showAdminSettings')"
            title="系统设置"
          >
            ⚙️ 设置
          </button>
          
          <button class="action-button logout-button" @click="$emit('logout')">登出</button>
        </template>
        <template v-else>
          <button class="action-button login-button" @click="$emit('showLogin')">登录</button>
          <button class="action-button register-button" @click="$emit('showRegister')">注册</button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuth } from '../composables/useAuth'
import { userApi } from '../api/user'

// 定义事件
defineEmits<{
  showLogin: []
  showRegister: []
  showAdminSettings: []
  logout: []
}>()

// 获取认证状态
const { user, isAuthenticated, userAvatar } = useAuth()

// 检查是否有管理员权限
const hasAdminPermission = computed(() => {
  return userApi.isAdmin(user.value)
})
</script>

<style scoped>
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

.settings-button {
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.settings-button:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

/* 响应式设计 */
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
}

@media (max-width: 480px) {
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
