<template>
  <div class="login-card">
    <div class="card-content">
      <!-- 左侧内容区 -->
      <div class="content-left">
        <!-- 装饰性背景 -->
        <div class="decorative-bg">
          <div class="floating-circle circle-1"></div>
          <div class="floating-circle circle-2"></div>
          <div class="floating-circle circle-3"></div>
        </div>
        
        <!-- Logo 区域 -->
        <div class="logo-section">
          <div class="logo">
            <div class="logo-circle">
              <span class="logo-text">EcoWiki</span>
            </div>
          </div>
        </div>
        
        <!-- 标题区域 -->
        <div class="login-header">
          <h1 class="login-title">欢迎回来！</h1>
          <p class="login-subtitle">继续您的探索之旅</p>
        </div>
      </div>
      
      <!-- 右侧表单区 -->
      <div class="content-right">
        <div class="form-container">
          <form @submit.prevent="handleLogin" class="login-form">
            <div class="form-group">
              <input
                id="loginField"
                v-model="formData.loginField"
                type="text"
                class="form-input"
                placeholder="邮箱或用户名"
                required
              />
            </div>
            
            <div class="form-group">
              <input
                id="password"
                v-model="formData.password"
                type="password"
                class="form-input"
                placeholder="密码"
                required
              />
            </div>
            
            <div class="form-group checkbox-group">
              <label class="checkbox-label">
                <input
                  type="checkbox"
                  v-model="formData.rememberMe"
                  class="checkbox-input"
                />
                <span class="checkbox-custom"></span>
                记住我
              </label>
            </div>
            
            <button
              type="submit"
              class="login-button"
              :disabled="isLoading"
            >
              <span v-if="isLoading" class="loading-spinner"></span>
              {{ isLoading ? '登录中...' : '登录' }}
            </button>
            
            <!-- 底部链接 -->
            <div class="form-footer">
              <span class="register-prompt">还没有账户？</span>
              <a href="#" @click="$emit('switchToRegister')" class="register-link">注册</a>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { userApi } from '../api/user'
import { useAuth } from '../composables/useAuth'
import toast from '../utils/toast'

// 定义 emits
const emit = defineEmits(['switchToRegister', 'loginSuccess'])

// 使用认证状态
const { setUser } = useAuth()

// 响应式数据
const isLoading = ref(false)

const formData = reactive({
  loginField: '', // 改为通用的登录字段
  password: '',
  rememberMe: false
})

// 判断输入的是邮箱还是用户名
const isEmail = (input: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(input)
}

// 登录处理函数
const handleLogin = async () => {
  if (!formData.loginField || !formData.password) {
    toast.warning('请填写完整的登录信息')
    return
  }
  
  isLoading.value = true
  
  try {
    // 根据输入内容判断登录方式
    const loginData = isEmail(formData.loginField) 
      ? {
          email: formData.loginField,
          password: formData.password,
          rememberMe: formData.rememberMe
        }
      : {
          username: formData.loginField,
          password: formData.password,
          rememberMe: formData.rememberMe
        }
    
    // 调用登录API
    const response = await userApi.login(loginData)
    
    // 登录成功，保存用户信息和token
    setUser(response.user, response.token, response.refreshToken)
    
    toast.success(`欢迎回来，${response.user.username}！`, '登录成功')
    
    // 通知父组件登录成功，关闭模态框
    emit('loginSuccess')
    
    console.log('登录成功，用户信息:', response.user)
    
  } catch (error: any) {
    console.error('登录失败:', error)
    toast.error(error.message || '登录失败，请检查登录信息和密码', '登录失败')
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(102, 126, 234, 0.3);
  overflow: hidden;
  transition: all 0.4s ease;
  animation: cardFadeIn 0.6s ease-out;
  max-width: 480px;
  margin: 0 auto;
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.card-content {
  display: flex;
  min-height: 500px;
}

.content-left {
  flex: 1;
  background: linear-gradient(135deg, rgba(255,255,255,0.12) 0%, rgba(255,255,255,0.06) 100%);
  padding: 40px 30px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.decorative-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.floating-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  animation: float 8s ease-in-out infinite;
}

.circle-1 {
  width: 60px;
  height: 60px;
  top: 10%;
  left: -10px;
  animation-delay: 0s;
}

.circle-2 {
  width: 40px;
  height: 40px;
  top: 60%;
  right: -5px;
  animation-delay: 2.5s;
}

.circle-3 {
  width: 30px;
  height: 30px;
  bottom: 15%;
  left: 40%;
  animation-delay: 5s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.logo-section {
  margin-bottom: 25px;
  text-align: center;
}

.logo-circle {
  width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.logo-text {
  color: white;
  font-size: 0.9rem;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.login-header {
  text-align: center;
}

.login-title {
  color: white;
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.login-subtitle {
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.9rem;
  margin: 0;
  line-height: 1.4;
}

.content-right {
  flex: 1;
  background: white;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-container {
  width: 100%;
  padding: 40px 30px;
}

.login-form {
  width: 100%;
}

.form-group {
  margin-bottom: 20px;
}

.checkbox-group {
  margin-bottom: 15px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 0.9rem;
  color: #4a5568;
  user-select: none;
}

.checkbox-input {
  display: none;
}

.checkbox-custom {
  width: 18px;
  height: 18px;
  border: 2px solid #e2e8f0;
  border-radius: 3px;
  margin-right: 10px;
  position: relative;
  transition: all 0.3s ease;
}

.checkbox-input:checked + .checkbox-custom {
  background: #667eea;
  border-color: #667eea;
}

.checkbox-input:checked + .checkbox-custom::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 12px;
  font-weight: bold;
}

.form-input {
  width: 100%;
  padding: 16px 0 16px 0;
  border: none;
  border-bottom: 2px solid #e2e8f0;
  background: transparent;
  font-size: 1rem;
  color: #2d3748;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-input::placeholder {
  color: #a0aec0;
  font-weight: 400;
}

.form-input:focus {
  outline: none;
  border-bottom-color: #667eea;
  background: rgba(102, 126, 234, 0.02);
}

.form-input:focus::placeholder {
  color: transparent;
}

.login-button {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 25px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin: 25px 0 20px 0;
  position: relative;
  overflow: hidden;
}

.login-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s;
}

.login-button:hover::before {
  left: 100%;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
}

.login-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.form-footer {
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  font-size: 0.9rem;
}

.register-prompt {
  color: #718096;
}

.register-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.register-link:hover {
  color: #764ba2;
}

.loading-spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
  margin-right: 6px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  .login-card {
    max-width: 400px;
  }
  
  .card-content {
    flex-direction: column;
    min-height: auto;
  }
  
  .content-left {
    padding: 30px 25px;
    flex: none;
  }
  
  .content-right {
    padding: 0;
  }
  
  .form-container {
    padding: 30px 25px;
  }
  
  .login-title {
    font-size: 1.5rem;
  }
}

@media (max-width: 480px) {
  .login-card {
    max-width: 350px;
  }
  
  .form-container {
    padding: 25px 20px;
  }
  
  .content-left {
    padding: 25px 20px;
  }
}
</style>