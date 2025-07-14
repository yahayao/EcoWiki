<!-- UserProfile.vue -->
<template>
  <div class="user-profile-layout">
    <!-- 顶部标题栏 -->
    <div class="profile-header">
      <div class="header-left">
        <div class="header-icon">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M12,4A4,4 0 0,1 16,8A4,4 0 0,1 12,12A4,4 0 0,1 8,8A4,4 0 0,1 12,4M12,14C16.42,14 20,15.79 20,18V20H4V18C4,15.79 7.58,14 12,14Z" />
          </svg>
        </div>
        <div class="header-text">
          <h1>个人中心</h1>
          <p>管理您的个人资料和设置</p>
        </div>
      </div>
      <div class="header-actions">
        <button class="btn btn-ghost" @click="refreshProfile">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M17.65,6.35C16.2,4.9 14.21,4 12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20C15.73,20 18.84,17.45 19.73,14H17.65C16.83,16.33 14.61,18 12,18A6,6 0 0,1 6,12A6,6 0 0,1 12,6C13.66,6 15.14,6.69 16.22,7.78L13,11H20V4L17.65,6.35Z" />
          </svg>
        </button>
        <button class="btn btn-ghost" @click="goBack">
          返回
        </button>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="profile-container">
      <!-- 左侧导航 -->
      <div class="profile-sidebar">
        <nav class="nav-menu">
          <div class="nav-group">
            <h3 class="nav-group-title">个人资料</h3>
            <ul class="nav-list">
              <li class="nav-item" :class="{ active: activeSection === 'info' }" @click="setActiveSection('info')">
                <div class="nav-item-content">
                  <svg viewBox="0 0 24 24" class="nav-icon">
                    <path d="M12,4A4,4 0 0,1 16,8A4,4 0 0,1 12,12A4,4 0 0,1 8,8A4,4 0 0,1 12,4M12,14C16.42,14 20,15.79 20,18V20H4V18C4,15.79 7.58,14 12,14Z" />
                  </svg>
                  <span class="nav-text">用户信息</span>
                </div>
              </li>
              <li class="nav-item" :class="{ active: activeSection === 'page' }" @click="setActiveSection('page')">
                <div class="nav-item-content">
                  <svg viewBox="0 0 24 24" class="nav-icon">
                    <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z" />
                  </svg>
                  <span class="nav-text">我的主页</span>
                </div>
              </li>
              <li class="nav-item" :class="{ active: activeSection === 'contribute' }" @click="setActiveSection('contribute')">
                <div class="nav-item-content">
                  <svg viewBox="0 0 24 24" class="nav-icon">
                    <path d="M3,22V8H7V22H3M10,22V2H14V22H10M17,22V14H21V22H17Z" />
                  </svg>
                  <span class="nav-text">贡献统计</span>
                </div>
              </li>
              <li class="nav-item" :class="{ active: activeSection === 'articles' }" @click="setActiveSection('articles')">
                <div class="nav-item-content">
                  <svg viewBox="0 0 24 24" class="nav-icon">
                    <path d="M19,3H14.82C14.4,1.84 13.3,1 12,1C10.7,1 9.6,1.84 9.18,3H5A2,2 0 0,0 3,5V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V5A2,2 0 0,0 19,3M12,3A1,1 0 0,1 13,4A1,1 0 0,1 12,5A1,1 0 0,1 11,4A1,1 0 0,1 12,3" />
                  </svg>
                  <span class="nav-text">文章管理</span>
                </div>
              </li>
              <li class="nav-item" :class="{ active: activeSection === 'secure' }" @click="setActiveSection('secure')">
                <div class="nav-item-content">
                  <svg viewBox="0 0 24 24" class="nav-icon">
                    <path d="M12,1L3,5V11C3,16.55 6.84,21.74 12,23C17.16,21.74 21,16.55 21,11V5L12,1M12,7C13.4,7 14.8,8.6 14.8,10V11H16V16H8V11H9.2V10C9.2,8.6 10.6,7 12,7M12,8.2C11.2,8.2 10.4,8.7 10.4,10V11H13.6V10C13.6,8.7 12.8,8.2 12,8.2Z" />
                  </svg>
                  <span class="nav-text">账号安全</span>
                </div>
              </li>
            </ul>
          </div>
        </nav>
      </div>

      <!-- 右侧内容区域 -->
      <div class="profile-content">
        <UserInformation v-show="activeSection === 'info'" />
        <UserPage v-show="activeSection === 'page'" />
        <UserContribute v-show="activeSection === 'contribute'" />
        <UserArticle v-show="activeSection === 'articles'" />
        <UserSecure v-show="activeSection === 'secure'" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import UserInformation from './view/UserInformation.vue'
import UserPage from './view/UserPage.vue'
import UserContribute from './view/UserContribute.vue'
import UserArticle from './view/UserArticle.vue'
import UserSecure from './view/UserSecure.vue'

const router = useRouter()
const activeSection = ref('info')


const setActiveSection = (section: string) => {
  activeSection.value = section
}

const goBack = () => {
  router.push('/');
}
const refreshProfile = () => {
  window.location.reload()
}
</script>

<style scoped>
.user-profile-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  overflow: hidden;
}

.profile-header {
  background: white;
  border-bottom: 1px solid #e9ecef;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 72px;
  flex-shrink: 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #43a047 0%, #66bb6a 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-icon .icon {
  width: 20px;
  height: 20px;
  fill: white;
}

.header-text h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
}

.header-text p {
  margin: 4px 0 0 0;
  color: #6c757d;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.btn-ghost {
  background: transparent;
  color: #6c757d;
  border: 1px solid #dee2e6;
}

.btn-ghost:hover {
  background: #f8f9fa;
  color: #495057;
}

.profile-container {
  flex: 1;
  display: flex;
  min-height: 0;
}

.profile-sidebar {
  width: 280px;
  background: white;
  border-right: 1px solid #e9ecef;
  flex-shrink: 0;
  overflow-y: auto;
}

.nav-menu {
  padding: 16px 0;
}

.nav-group {
  margin-bottom: 24px;
}

.nav-group-title {
  padding: 0 24px;
  margin: 0 0 12px 0;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  color: #6c757d;
  letter-spacing: 0.5px;
}

.nav-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav-item {
  cursor: pointer;
  transition: all 0.2s;
}

.nav-item:hover {
  background: #f8f9fa;
}

.nav-item.active {
  background: #e8f5e9;
  border-right: 3px solid #43a047;
}

.nav-item.active .nav-text {
  color: #43a047;
  font-weight: 600;
}

.nav-item-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
}

.nav-icon {
  width: 18px;
  height: 18px;
  fill: #6c757d;
  flex-shrink: 0;
}

.nav-item.active .nav-icon {
  fill: #43a047;
}

.nav-text {
  font-size: 14px;
  color: #495057;
  font-weight: 500;
}

.profile-content {
  flex: 1;
  overflow-y: auto;
  background: white;
  margin: 16px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

@media (max-width: 768px) {
  .profile-container {
    flex-direction: column;
  }
  
  .profile-sidebar {
    width: 100%;
    height: auto;
    border-right: none;
    border-bottom: 1px solid #e9ecef;
  }
}
.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.btn .icon {
  width: 16px;
  height: 16px;
}
.btn-ghost {
  background: transparent;
  color: #6c757d;
  border: 1px solid #dee2e6;
}
</style>