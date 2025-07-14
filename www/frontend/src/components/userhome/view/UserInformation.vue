<!-- UserInformation.vue -->
<template>
  <div class="user-info-container">
    <h2 class="section-title">用户信息</h2>
    
    <div class="info-grid">
      <div class="info-card">
        <h3>基本信息</h3>
        <div class="avatar-section">
          <img :src="userAvatar" alt="用户头像" class="avatar-preview">
          <button class="btn btn-outline">更换头像</button>
        </div>
        
        <div class="form-group">
          <label>用户名</label>
          {{ user?.username || '未知用户' }}
          <input type="text" class="form-input" placeholder="请输入新用户名">
          <button class="btn btn-sm">修改</button>
        </div>  
        
        <div class="form-group">
          <label>电子邮箱</label>
          {{ user?.email || '未设置' }}
          <span class="badge badge-success">已验证</span>
        </div>
        
        <div class="form-group">
          <label>注册时间</label>
          {{ formatDate(user?.createdAt) }}
        </div>
        
        <div class="form-group">
          <label>账号权限</label>
          {{ user?.userGroup || '普通用户' }}
        </div>
        
        <div class="form-group">
          <label>账号状态</label>
          <span :class="user?.active ? 'status-active' : 'status-inactive'">
                {{ user?.active ? '正常' : '已停用' }}
          </span>
        </div>
      </div>
      
      <div class="info-card">
        <h3>安全验证设置</h3>
        <div class="form-group">
          <label>安全验证问题（可用于忘记密码时修改密码）</label>
          <!-- {{ user?.securityQuestion || '未设置' }} -->
          <button class="btn btn-sm">修改</button>
        </div>
        
        <div class="form-group">
          <label>安全验证答案</label>
          <input v-model="securityAnswer" type="password" class="form-input">
          <button class="btn btn-sm">修改</button>
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

const username = ref('新用户名')
const securityQuestion = ref('您的出生地是？')
const securityAnswer = ref('Shanghai')

</script>

<style scoped>
.status-active {
  color: #10b981;
  font-weight: 600;
}

.status-inactive {
  color: #ef4444;
  font-weight: 600;
}
.user-info-container {
  padding: 32px;
}

.section-title {
  margin: 0 0 24px 0;
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
}

.info-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 24px;
}

.info-card h3 {
  margin: 0 0 20px 0;
  font-size: 20px;
  color: #495057;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.avatar-preview {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #495057;
}

.form-input, .form-select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  font-size: 14px;
}

.static-info {
  margin: 0;
  padding: 8px 0;
  color: #6c757d;
}

.badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.badge-success {
  background: #d4edda;
  color: #155724;
}

.btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.btn-outline {
  border: 1px solid #43a047;
  color: #43a047;
  background: white;
}

.btn-sm {
  background: #43a047;
  color: white;
  margin-left: 8px;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>