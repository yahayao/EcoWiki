<template>
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
@media (max-width: 480px) {
  .user-area {
    flex-direction: column;
    gap: 8px;
    width: 100%;
  }
  
  .action-button {
    width: 100%;
    text-align: center;
  }
}
</style>
