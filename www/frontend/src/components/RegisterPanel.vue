<template>
  <div class="register-card">
    <div class="register-header">
      <h1 class="register-title">注册 EcoWiki 账户</h1>
      <p class="register-subtitle">创建您的新账户</p>
    </div>
      
    <form @submit.prevent="handleRegister" class="register-form">
      <div class="form-group">
        <label for="username" class="form-label">用户名</label>
        <input
          id="username"
          v-model="formData.username"
          type="text"
          class="form-input"
          placeholder="请输入用户名"
          required
        />
      </div>
      
      <div class="form-group">
        <label for="email" class="form-label">邮箱</label>
        <input
          id="email"
          v-model="formData.email"
          type="email"
          class="form-input"
          placeholder="请输入邮箱地址"
          required
        />
      </div>
      
      <div class="form-group">
        <label for="password" class="form-label">密码</label>
        <input
          id="password"
          v-model="formData.password"
          type="password"
          class="form-input"
          placeholder="请输入密码"
          required
        />
      </div>
      
      <div class="form-group">
        <label for="confirmPassword" class="form-label">确认密码</label>
        <input
          id="confirmPassword"
          v-model="formData.confirmPassword"
          type="password"
          class="form-input"
          placeholder="请再次输入密码"
          required
        />
      </div>
      
      <button
        type="submit"
        class="register-button"
        :disabled="isLoading"
      >
        <span v-if="isLoading" class="loading-spinner"></span>
        {{ isLoading ? '注册中...' : '注册' }}
      </button>
      
      <div class="register-footer">
        <p>已有账户？<a href="#" @click="$emit('switchToLogin')" class="login-link">立即登录</a></p>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'

// 定义 emits
defineEmits(['switchToLogin'])

// 响应式数据
const isLoading = ref(false)
const formData = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 注册处理函数
const handleRegister = async () => {
  if (!formData.username || !formData.email || !formData.password || !formData.confirmPassword) {
    alert('请填写完整的注册信息')
    return
  }
  
  if (formData.password !== formData.confirmPassword) {
    alert('两次输入的密码不一致')
    return
  }
  
  isLoading.value = true
  
  try {
    // 模拟注册请求
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    console.log('注册信息:', formData)
    alert('注册成功！')
    
  } catch (error) {
    console.error('注册失败:', error)
    alert('注册失败，请重试')
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
/* 复用 LoginPanel 的样式，稍作调整 */
.register-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
  backdrop-filter: blur(10px);
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.register-subtitle {
  color: #666;
  font-size: 16px;
  margin: 0;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.form-input {
  padding: 12px 16px;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  font-size: 16px;
  transition: all 0.3s ease;
  background: #f8f9fa;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.register-button {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 14px 20px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.register-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.register-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.register-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e1e5e9;
}

.register-footer p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.login-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
  cursor: pointer;
}

.login-link:hover {
  color: #5a6fd8;
  text-decoration: underline;
}

@media (max-width: 480px) {
  .register-card {
    padding: 24px;
    margin: 10px;
  }
  
  .register-title {
    font-size: 24px;
  }
}
</style>