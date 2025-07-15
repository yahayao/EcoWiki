<!-- UserInformation.vue -->
<template>
  <div class="user-info-container">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M12,4A4,4 0 0,1 16,8A4,4 0 0,1 12,12A4,4 0 0,1 8,8A4,4 0 0,1 12,4M12,14C16.42,14 20,15.79 20,18V20H4V18C4,15.79 7.58,14 12,14Z" />
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">用户信息</h1>
          <p class="page-subtitle">管理您的个人资料和设置</p>
        </div>
      </div>
    </div>

    <!-- 用户概览卡片 -->
    <div class="user-overview-card">
      <div class="user-profile-section">
        <div class="avatar-container">
          <div class="avatar-wrapper">
            <img :src="userAvatar" alt="用户头像" class="user-avatar">
            <div class="avatar-overlay">
              <button class="avatar-edit-btn" @click="changeAvatar">
                <svg viewBox="0 0 24 24" class="icon">
                  <path d="M9,16.17L4.83,12L3.41,13.41L9,19L21,7L19.59,5.59L9,16.17Z"/>
                </svg>
              </button>
            </div>
          </div>
          <button class="change-avatar-btn" @click="changeAvatar">
            <svg viewBox="0 0 24 24" class="icon">
              <path d="M9,12A3,3 0 0,0 12,15A3,3 0 0,0 15,12A3,3 0 0,0 12,9A3,3 0 0,0 9,12M12,4.5C17,4.5 21.27,7.61 23,12C21.27,16.39 17,19.5 12,19.5C7,19.5 2.73,16.39 1,12C2.73,7.61 7,4.5 12,4.5M12,6.5C8.5,6.5 5.42,8.81 4,12C5.42,15.19 8.5,17.5 12,17.5C15.5,17.5 18.58,15.19 20,12C18.58,8.81 15.5,6.5 12,6.5Z"/>
            </svg>
            更换头像
          </button>
        </div>
        <div class="user-quick-info">
          <h2 class="user-name">{{ user?.username || '未知用户' }}</h2>
          <div class="user-status">
            <span class="status-badge" :class="user?.active ? 'status-active' : 'status-inactive'">
              <div class="status-dot"></div>
              {{ user?.active ? '账号正常' : '账号已停用' }}
            </span>
          </div>
          <div class="user-meta">
            <div class="meta-item">
              <svg viewBox="0 0 24 24" class="meta-icon">
                <path d="M12,15C12.81,15 13.5,14.7 14.11,14.11C14.7,13.5 15,12.81 15,12C15,11.19 14.7,10.5 14.11,9.89C13.5,9.3 12.81,9 12,9C11.19,9 10.5,9.3 9.89,9.89C9.3,10.5 9,11.19 9,12C9,12.81 9.3,13.5 9.89,14.11C10.5,14.7 11.19,15 12,15M12,2C14.21,2 16.21,2.81 17.78,4.39C19.36,5.96 20.17,7.96 20.17,10.17C20.17,12.38 19.36,14.38 17.78,15.95L12,21.5L6.22,15.95C4.64,14.38 3.83,12.38 3.83,10.17C3.83,7.96 4.64,5.96 6.22,4.39C7.79,2.81 9.79,2 12,2Z"/>
              </svg>
              <span>{{ user?.userGroup || '普通用户' }}</span>
            </div>
            <div class="meta-item">
              <svg viewBox="0 0 24 24" class="meta-icon">
                <path d="M9,10V12H7V10H9M13,10V12H11V10H13M17,10V12H15V10H17M19,3A2,2 0 0,1 21,5V19A2,2 0 0,1 19,21H5C3.89,21 3,20.1 3,19V5A2,2 0 0,1 5,3H6V1H8V3H16V1H18V3H19M19,19V8H5V19H19M19,5H5V6H19V5Z"/>
              </svg>
              <span>{{ formatDate(user?.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 详细信息卡片 -->
    <div class="info-cards-grid">
      <!-- 基本信息卡片 -->
      <div class="info-card">
        <div class="card-header">
          <div class="card-title">
            <svg viewBox="0 0 24 24" class="card-icon">
              <path d="M12,4A4,4 0 0,1 16,8A4,4 0 0,1 12,12A4,4 0 0,1 8,8A4,4 0 0,1 12,4M12,14C16.42,14 20,15.79 20,18V20H4V18C4,15.79 7.58,14 12,14Z" />
            </svg>
            <h3>基本信息</h3>
          </div>
        </div>
        
        <div class="card-content">
          <div class="info-item">
            <label class="info-label">用户名</label>
            <div class="info-value-container">
              <div class="info-value">{{ user?.username || '未知用户' }}</div>
              <button class="edit-btn" @click="editUsername">
                <svg viewBox="0 0 24 24" class="icon">
                  <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z"/>
                </svg>
              </button>
            </div>
          </div>

          <div class="info-item">
            <label class="info-label">电子邮箱</label>
            <div class="info-value-container">
              <div class="info-value">{{ user?.email || '未设置' }}</div>
              <span class="verification-badge verified">
                <svg viewBox="0 0 24 24" class="icon">
                  <path d="M21,7L9,19L3.5,13.5L4.91,12.09L9,16.17L19.59,5.59L21,7Z"/>
                </svg>
                已验证
              </span>
            </div>
          </div>

          <div class="info-item">
            <label class="info-label">注册时间</label>
            <div class="info-value">{{ formatDate(user?.createdAt) }}</div>
          </div>

          <div class="info-item">
            <label class="info-label">账号权限</label>
            <div class="info-value">
              <span class="role-badge">{{ user?.userGroup || '普通用户' }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 安全设置卡片 -->
      <div class="info-card">
        <div class="card-header">
          <div class="card-title">
            <svg viewBox="0 0 24 24" class="card-icon">
              <path d="M12,1L3,5V11C3,16.55 6.84,21.74 12,23C17.16,21.74 21,16.55 21,11V5L12,1M12,7C13.4,7 14.8,8.6 14.8,10V11H16V16H8V11H9.2V10C9.2,8.6 10.6,7 12,7M12,8.2C11.2,8.2 10.4,8.7 10.4,10V11H13.6V10C13.6,8.7 12.8,8.2 12,8.2Z" />
            </svg>
            <h3>安全验证设置</h3>
          </div>
        </div>

        <div class="card-content">
          <div class="info-item">
            <label class="info-label">安全验证问题</label>
            <div class="info-description">用于忘记密码时验证身份</div>
            <div class="info-value-container">
              <div class="info-value security-hidden">••••••••••••••••••••</div>
              <button class="edit-btn" @click="editSecurityQuestion">
                <svg viewBox="0 0 24 24" class="icon">
                  <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z"/>
                </svg>
              </button>
            </div>
          </div>
          
          <div class="info-item">
            <label class="info-label">安全验证答案</label>
            <div class="info-value-container">
              <div class="info-value security-hidden">••••••••••••</div>
              <button class="edit-btn" @click="editSecurityAnswer">
                <svg viewBox="0 0 24 24" class="icon">
                  <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z"/>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuth } from '@/composables/useAuth'
import { userApi } from '@/api/user'
import type { UserResponse } from '@/api/user'
import { ref, computed, onMounted } from 'vue'

function formatDate(dateString?: string) {
  if (!dateString) return '无'
  try {
    return new Date(dateString).toLocaleString('zh-CN')
  } catch {
    return '无效日期'
  }
}

const { user, userAvatar} = useAuth()

const draft = ref({
  username: '',
  fullName: '',
  email: ''
})

function startEdit() {
  if (!user.value) return
  
  draft.value = {
    username: user.value.username || '',
    fullName: user.value.fullName || '',
    email: user.value.email || ''
  }
}

// 头像相关方法
const changeAvatar = () => {
  // TODO: 实现更换头像功能
  console.log('更换头像')
}

// 编辑方法
const editUsername = () => {
  // TODO: 实现编辑用户名功能
  console.log('编辑用户名')
}

const editSecurityQuestion = () => {
  // TODO: 实现编辑安全问题功能
  console.log('编辑安全问题')
}

const editSecurityAnswer = () => {
  // TODO: 实现编辑安全答案功能
  console.log('编辑安全答案')
}

const username = ref('新用户名')
const securityQuestion = ref('您的出生地是？')
const securityAnswer = ref('Shanghai')
</script>

<style scoped>
/* 主容器样式 */
.user-info-container {
  padding: 24px;
  background: #f8fafb;
  min-height: 100vh;
}

/* 页面标题区域 */
.page-header {
  margin-bottom: 32px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.header-icon .icon {
  width: 24px;
  height: 24px;
  fill: white;
}

.header-text {
  flex: 1;
}

.page-title {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
  letter-spacing: -0.5px;
}

.page-subtitle {
  margin: 4px 0 0 0;
  color: #718096;
  font-size: 16px;
  font-weight: 400;
}

/* 用户概览卡片 */
.user-overview-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
}

.user-profile-section {
  display: flex;
  align-items: center;
  gap: 32px;
}

/* 头像区域 */
.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
}

.user-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid white;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  transition: all 0.3s ease;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-edit-btn {
  background: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.avatar-edit-btn:hover {
  transform: scale(1.1);
}

.avatar-edit-btn .icon {
  width: 20px;
  height: 20px;
  fill: #667eea;
}

.change-avatar-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.change-avatar-btn:hover {
  background: #5a67d8;
  transform: translateY(-1px);
}

.change-avatar-btn .icon {
  width: 16px;
  height: 16px;
  fill: currentColor;
}

/* 用户快速信息 */
.user-quick-info {
  flex: 1;
}

.user-name {
  margin: 0 0 12px 0;
  font-size: 28px;
  font-weight: 700;
  color: #1a202c;
  letter-spacing: -0.5px;
}

.user-status {
  margin-bottom: 16px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.status-active {
  background: #f0fff4;
  color: #22543d;
  border: 1px solid #9ae6b4;
}

.status-inactive {
  background: #fff5f5;
  color: #822727;
  border: 1px solid #feb2b2;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-active .status-dot {
  background: #48bb78;
}

.status-inactive .status-dot {
  background: #f56565;
}

.user-meta {
  display: flex;
  gap: 24px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #4a5568;
  font-size: 14px;
}

.meta-icon {
  width: 16px;
  height: 16px;
  fill: #718096;
}

/* 信息卡片网格 */
.info-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
}

/* 信息卡片 */
.info-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.card-header {
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-icon {
  width: 20px;
  height: 20px;
  fill: #667eea;
}

.card-title h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.card-content {
  padding: 24px;
}

/* 信息项 */
.info-item {
  margin-bottom: 24px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #4a5568;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-description {
  margin-bottom: 8px;
  font-size: 13px;
  color: #718096;
  line-height: 1.4;
}

.info-value-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.info-value {
  flex: 1;
  font-size: 16px;
  color: #2d3748;
  font-weight: 500;
  padding: 12px 0;
}

.security-hidden {
  font-family: monospace;
  letter-spacing: 2px;
  color: #a0aec0;
}

/* 编辑按钮 */
.edit-btn {
  background: #f7fafc;
  border: 1px solid #e2e8f0;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.edit-btn:hover {
  background: #667eea;
  border-color: #667eea;
  transform: scale(1.05);
}

.edit-btn:hover .icon {
  fill: white;
}

.edit-btn .icon {
  width: 16px;
  height: 16px;
  fill: #718096;
  transition: fill 0.2s ease;
}

/* 验证徽章 */
.verification-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.verification-badge.verified {
  background: #f0fff4;
  color: #22543d;
  border: 1px solid #9ae6b4;
}

.verification-badge .icon {
  width: 12px;
  height: 12px;
  fill: currentColor;
}

/* 角色徽章 */
.role-badge {
  display: inline-block;
  padding: 6px 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-info-container {
    padding: 16px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .user-overview-card {
    padding: 24px;
  }
  
  .user-profile-section {
    flex-direction: column;
    text-align: center;
    gap: 24px;
  }
  
  .user-meta {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .info-cards-grid {
    grid-template-columns: 1fr;
  }
  
  .card-content {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .user-avatar {
    width: 100px;
    height: 100px;
  }
  
  .user-name {
    font-size: 24px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .page-title {
    font-size: 20px;
  }
}
</style>