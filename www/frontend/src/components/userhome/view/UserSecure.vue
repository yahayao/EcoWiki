<!-- UserSecure.vue -->
<template>
  <div class="secure-container">
    <h2 class="section-title">账号安全</h2>
    
    <div class="secure-grid">
      <div class="secure-card">
        <h3>隐私设置</h3>
        <div class="setting-item">
          <label class="toggle-label">
            <input type="checkbox" v-model="showEmail" class="toggle-input">
            <span class="toggle-slider"></span>
            公开电子邮箱
          </label>
        </div>
        <div class="setting-item">
          <label class="toggle-label">
            <input type="checkbox" v-model="showContributions" class="toggle-input">
            <span class="toggle-slider"></span>
            公开贡献记录
          </label>
        </div>
      </div>
      
      <div class="secure-card">
        <h3>密码管理</h3>
        <div class="form-group">
          <label>当前密码</label>
          <input type="password" class="form-input" placeholder="输入当前密码">
        </div>
        <div class="form-group">
          <label>新密码</label>
          <input type="password" class="form-input" placeholder="输入新密码">
        </div>
        <div class="form-group">
          <label>确认新密码</label>
          <input type="password" class="form-input" placeholder="再次输入新密码">
        </div>
        <button class="btn btn-primary">更新密码</button>
      </div>
    </div>
    
    <div class="secure-card">
      <h3>安全日志</h3>
      <div class="log-list">
        <div class="log-item">
          <span class="log-time">2025-07-14 10:30</span>
          <span class="log-action">密码修改成功</span>
          <span class="log-ip">IP: 192.168.1.1</span>
        </div>
        <div class="log-item">
          <span class="log-time">2025-07-10 15:45</span>
          <span class="log-action">登录成功</span>
          <span class="log-ip">IP: 203.0.113.42</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const showEmail = ref(false)
const showContributions = ref(true)
</script>

<style scoped>
.secure-container {
  padding: 32px;
}

.section-title {
  margin: 0 0 24px 0;
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
}

.secure-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  margin-bottom: 32px;
}

.secure-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 24px;
}

.secure-card h3 {
  margin: 0 0 20px 0;
  color: #2c3e50;
}

.setting-item {
  margin-bottom: 16px;
}

.toggle-label {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  font-weight: 500;
}

.toggle-input {
  display: none;
}

.toggle-slider {
  width: 48px;
  height: 24px;
  background: #ccc;
  border-radius: 12px;
  position: relative;
  transition: 0.3s;
}

.toggle-slider::after {
  content: '';
  position: absolute;
  width: 20px;
  height: 20px;
  background: white;
  border-radius: 50%;
  top: 2px;
  left: 2px;
  transition: 0.3s;
}

.toggle-input:checked + .toggle-slider {
  background: #43a047;
}

.toggle-input:checked + .toggle-slider::after {
  left: 26px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #495057;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dee2e6;
  border-radius: 4px;
}

.btn-primary {
  background: #43a047;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.log-list {
  max-height: 300px;
  overflow-y: auto;
}

.log-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e9ecef;
}

.log-item:last-child {
  border-bottom: none;
}

.log-time {
  color: #6c757d;
  font-size: 14px;
}

.log-action {
  font-weight: 500;
}

.log-ip {
  color: #6c757d;
  font-size: 14px;
}

@media (max-width: 768px) {
  .secure-grid {
    grid-template-columns: 1fr;
  }
}
</style>