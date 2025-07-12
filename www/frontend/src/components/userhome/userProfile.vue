<template>
  <div class="user-profile">
    <div class="profile-header">
      <h2>用户个人资料</h2>
      <button @click="$emit('close')" class="close-btn">×</button>
    </div>
    <div class="profile-content">
      <div class="avatar-section">
        <div class="avatar">
          <img :src="userInfo.avatar || '/default-avatar.png'" alt="用户头像" />
        </div>
        <button class="change-avatar-btn">更换头像</button>
      </div>
      <div class="info-section">
        <div class="info-item">
          <label>用户名:</label>
          <span>{{ userInfo.username || '未设置' }}</span>
        </div>
        <div class="info-item">
          <label>邮箱:</label>
          <span>{{ userInfo.email || '未设置' }}</span>
        </div>
        <div class="info-item">
          <label>注册时间:</label>
          <span>{{ formatDate(userInfo.createdAt) }}</span>
        </div>
        <div class="info-item">
          <label>最后更新:</label>
          <span>{{ formatDate(userInfo.updatedAt) }}</span>
        </div>
      </div>
    </div>
    <div class="profile-actions">
      <button class="edit-btn">编辑资料</button>
      <button class="change-password-btn">修改密码</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuth } from '../../composables/useAuth'

// 获取用户认证状态
const { user } = useAuth()

// 用户信息
const userInfo = ref({
  username: '',
  email: '',
  avatar: '',
  createdAt: '',
  updatedAt: ''
})

// 定义事件
const emit = defineEmits<{
  close: []
}>()

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '无'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 组件挂载时获取用户信息
onMounted(() => {
  if (user.value) {
    userInfo.value = {
      username: user.value.username || '',
      email: user.value.email || '',
      avatar: user.value.avatar || '',
      createdAt: user.value.createdAt || '',
      updatedAt: user.value.updatedAt || ''
    }
  }
})
</script>

<style scoped>
.user-profile {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  width: 600px;
  max-width: 90vw;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
}

.profile-header h2 {
  margin: 0;
  color: #1a202c;
  font-size: 20px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #64748b;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #e2e8f0;
  color: #1a202c;
}

.profile-content {
  padding: 24px;
  display: flex;
  gap: 24px;
  flex: 1;
  overflow-y: auto;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #e2e8f0;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.change-avatar-btn {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}

.change-avatar-btn:hover {
  background: #2563eb;
}

.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-item label {
  font-weight: 600;
  color: #374151;
  min-width: 80px;
}

.info-item span {
  color: #6b7280;
}

.profile-actions {
  padding: 20px 24px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.edit-btn, .change-password-btn {
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  border: none;
}

.edit-btn {
  background: #10b981;
  color: white;
}

.edit-btn:hover {
  background: #059669;
}

.change-password-btn {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.change-password-btn:hover {
  background: #e5e7eb;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-profile {
    width: 95vw;
    height: 90vh;
  }
  
  .profile-content {
    flex-direction: column;
    text-align: center;
  }
  
  .avatar-section {
    align-items: center;
  }
  
  .info-item {
    flex-direction: column;
    gap: 4px;
    text-align: center;
  }
  
  .info-item label {
    min-width: auto;
  }
  
  .profile-actions {
    flex-direction: column;
  }
}
</style>