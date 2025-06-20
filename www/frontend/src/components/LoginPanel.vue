<template>
  <div class="login-card">
    <div class="login-header">
      <h1 class="login-title">欢迎来到 EcoWiki</h1>
      <p class="login-subtitle">请登录您的账户</p>
    </div>
      
      <form @submit.prevent="handleLogin" class="login-form">
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
        
        <div class="form-options">
          <label class="checkbox-label">
            <input
              v-model="formData.rememberMe"
              type="checkbox"
              class="checkbox"
            />
            记住我
          </label>
          <a href="#" class="forgot-password">忘记密码？</a>
        </div>
        
        <button
          type="submit"
          class="login-button"
          :disabled="isLoading"
        >
          <span v-if="isLoading" class="loading-spinner"></span>
          {{ isLoading ? '登录中...' : '登录' }}
        </button>        <div class="login-footer">
          <p>还没有账户？<a href="#" @click="$emit('switchToRegister')" class="register-link">立即注册</a></p>
        </div>
      </form>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { userApi } from '../api/user'
import { useAuth } from '../composables/useAuth'
import toast from '../utils/toast'

// 定义 emits
defineEmits(['switchToRegister'])

// 使用认证状态
const { setUser } = useAuth()

// 响应式数据
const isLoading = ref(false)
const formData = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// 登录处理函数
const handleLogin = async () => {
  if (!formData.username || !formData.password) {
    toast.warning('请填写完整的登录信息')
    return
  }
  
  isLoading.value = true
  
  try {
    // 调用登录API
    const response = await userApi.login({
      username: formData.username,
      password: formData.password,
      rememberMe: formData.rememberMe
    })
    
    // 登录成功，保存用户信息和token
    setUser(response.user, response.token, response.refreshToken)
    
    toast.success(`欢迎回来，${response.user.username}！`, '登录成功')
    
    // 这里可以跳转到主页面或其他页面
    console.log('登录成功，用户信息:', response.user)
    
  } catch (error: any) {
    console.error('登录失败:', error)
    toast.error(error.message || '登录失败，请检查用户名和密码', '登录失败')
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
  backdrop-filter: blur(10px);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-subtitle {
  color: #666;
  font-size: 16px;
  margin: 0;
}

.login-form {
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

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 8px 0;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
}

.checkbox {
  width: 16px;
  height: 16px;
  accent-color: #667eea;
}

.forgot-password {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.3s ease;
}

.forgot-password:hover {
  color: #5a6fd8;
  text-decoration: underline;
}

.login-button {
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

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.login-button:disabled {
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

.login-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e1e5e9;
}

.login-footer p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.register-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.register-link:hover {
  color: #5a6fd8;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-card {
    padding: 24px;
    margin: 10px;
  }
  
  .login-title {
    font-size: 24px;
  }
  
  .form-input {
    font-size: 16px; /* 防止 iOS 缩放 */
  }
}
</style>