<!-- UserInformation.vue -->
<template>
  <div class="user-info-container">
    <h2 class="section-title">用户信息</h2>
    
    <div class="info-grid">
      <div class="info-card">
        <h3>基本信息</h3>
        <div class="avatar-section">
          <img :src="avatar" alt="用户头像" class="avatar-preview">
          <button class="btn btn-outline">更换头像</button>
        </div>
        
        <div class="form-group">
          <label>用户名</label>
          <input v-model="username" type="text" class="form-input">
          <button class="btn btn-sm">修改</button>
        </div>
        
        <div class="form-group">
          <label>电子邮箱</label>
          <input v-model="email" type="email" class="form-input" readonly>
          <span class="badge badge-success">已验证</span>
        </div>
        
        <div class="form-group">
          <label>注册时间</label>
          <p class="static-info">2023年5月15日</p>
        </div>
        
        <div class="form-group">
          <label>账号权限</label>
          <p class="static-info">高级编辑者</p>
        </div>
        
        <div class="form-group">
          <label>账号状态</label>
          <span class="badge badge-success">正常</span>
        </div>
      </div>
      
      <div class="info-card">
        <h3>安全设置</h3>
        <div class="form-group">
          <label>安全验证问题</label>
          <select v-model="securityQuestion" class="form-select">
            <option>您的出生地是？</option>
            <option>您最喜欢的颜色？</option>
            <option>您母亲的姓名？</option>
          </select>
          <button class="btn btn-sm">修改</button>
        </div>
        
        <div class="form-group">
          <label>安全验证答案</label>
          <input v-model="securityAnswer" type="password" class="form-input">
          <button class="btn btn-sm">修改</button>
        </div>
        
        <div class="form-group">
          <label>密码</label>
          <input type="password" value="********" class="form-input" readonly>
          <button class="btn btn-sm">修改密码</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const avatar = ref('https://via.placeholder.com/150')
const username = ref('EcoEditor')
const email = ref('user@example.com')
const securityQuestion = ref('您的出生地是？')
const securityAnswer = ref('Shanghai')
</script>

<style scoped>
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