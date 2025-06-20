<template>
  <div class="register-card">
    <div class="card-content">
      <!-- 左侧内容区 -->
      <div class="content-left">
        <!-- 装饰性背景 -->
        <div class="decorative-bg">
          <div class="geometric-shape shape-1"></div>
          <div class="geometric-shape shape-2"></div>
          <div class="geometric-shape shape-3"></div>
          <div class="geometric-shape shape-4"></div>
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
        <div class="register-header">
          <h1 class="register-title">加入我们！</h1>
          <p class="register-subtitle">开启探索之旅</p>
        </div>
      </div>
      
      <!-- 右侧表单区 -->
      <div class="content-right">
        <div class="form-container">
          <form @submit.prevent="handleRegister" class="register-form">
            <div class="form-group">
              <input
                id="username"
                v-model="formData.username"
                type="text"
                class="form-input"
                placeholder="用户名"
                required
              />
            </div>
            
            <div class="form-group">
              <input
                id="email"
                v-model="formData.email"
                type="email"
                class="form-input"
                placeholder="邮箱"
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
            
            <div class="form-group">
              <input
                id="confirmPassword"
                v-model="formData.confirmPassword"
                type="password"
                class="form-input"
                placeholder="确认密码"
                required
              />
            </div>
            
            <button
              type="submit"
              class="register-button"
              :disabled="isLoading"
            >
              <span v-if="isLoading" class="loading-spinner"></span>
              {{ isLoading ? '创建中...' : '注册' }}
            </button>
            
            <!-- 底部链接 -->
            <div class="form-footer">
              <span class="login-prompt">已有账户？</span>
              <a href="#" @click="$emit('switchToLogin')" class="login-link">登录</a>
            </div>
          </form>
        </div>
      </div>
    </div>
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
  console.log('注册表单数据:', formData)
  
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
    const requestData = {
      username: formData.username,
      email: formData.email,
      password: formData.password
    }
    
    console.log('发送注册请求:', requestData)
    
    const response = await fetch('http://localhost:3001/api/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestData)
    })
    
    const data = await response.json()
    console.log('注册响应:', data)
    
    if (data.success) {
      alert('注册成功！请登录')
      formData.username = ''
      formData.email = ''
      formData.password = ''
      formData.confirmPassword = ''
    } else {
      alert(data.message || '注册失败')
    }
    
  } catch (error) {
    console.error('注册失败:', error)
    alert('网络错误，请检查后端服务是否启动')
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.register-card {
  background: linear-gradient(135deg, #f1f3f4 0%, #ffffff 100%);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.4s ease;
  animation: cardFadeIn 0.6s ease-out;
  max-width: 500px;
  margin: 0 auto;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.register-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.12);
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-content {
  display: flex;
  min-height: 450px;
}

.content-left {
  flex: 0.8;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 40px 30px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
  border-right: 1px solid rgba(0, 0, 0, 0.06);
}

.decorative-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.geometric-shape {
  position: absolute;
  background: linear-gradient(135deg, rgba(147, 107, 236, 0.12) 0%, rgba(168, 139, 248, 0.12) 100%);
  animation: float 15s ease-in-out infinite;
  border-radius: 12px;
}

.shape-1 {
  width: 80px;
  height: 80px;
  top: 10%;
  left: -20px;
  clip-path: polygon(50% 0%, 0% 100%, 100% 100%);
  animation-delay: 0s;
  background: rgba(147, 107, 236, 0.1);
}

.shape-2 {
  width: 60px;
  height: 60px;
  top: 50%;
  right: -15px;
  border-radius: 50%;
  animation-delay: 5s;
  background: rgba(168, 139, 248, 0.1);
}

.shape-3 {
  width: 50px;
  height: 50px;
  bottom: 15%;
  left: 35%;
  clip-path: polygon(20% 0%, 80% 0%, 100% 60%, 60% 100%, 0% 60%);
  animation-delay: 10s;
  background: rgba(196, 181, 253, 0.1);
}

.shape-4 {
  width: 40px;
  height: 40px;
  top: 25%;
  left: 60%;
  border-radius: 8px;
  animation-delay: 7s;
  background: rgba(147, 107, 236, 0.08);
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
    opacity: 0.5;
  }
  25% {
    transform: translateY(-15px) rotate(90deg);
    opacity: 0.7;
  }
  50% {
    transform: translateY(-25px) rotate(180deg);
    opacity: 0.9;
  }
  75% {
    transform: translateY(-10px) rotate(270deg);
    opacity: 0.7;
  }
}

.logo-section {
  margin-bottom: 25px;
  text-align: center;
}

.logo-circle {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #a78bfa 0%, #c084fc 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  box-shadow: 0 8px 20px rgba(167, 139, 250, 0.3);
  border: 3px solid white;
  backdrop-filter: blur(10px);
}

.logo-text {
  color: white;
  font-size: 0.9rem;
  font-weight: 700;
  letter-spacing: -0.5px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

.register-header {
  text-align: center;
}

.register-title {
  color: #1f2937;
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  background: linear-gradient(135deg, #a78bfa 0%, #c084fc 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.register-subtitle {
  color: #4b5563;
  font-size: 0.9rem;
  margin: 0;
  line-height: 1.4;
  font-weight: 500;
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

.register-form {
  width: 100%;
}

.form-group {
  margin-bottom: 18px;
}

.form-input {
  width: 100%;
  padding: 14px 0;
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
  border-bottom-color: #a78bfa;
  background: rgba(167, 139, 250, 0.02);
}

.form-input:focus::placeholder {
  color: transparent;
}

.register-button {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #a78bfa 0%, #c084fc 100%);
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
  box-shadow: 0 4px 15px rgba(167, 139, 250, 0.35);
}

.register-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s;
}

.register-button:hover::before {
  left: 100%;
}

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(167, 139, 250, 0.45);
}

.register-button:disabled {
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

.login-prompt {
  color: #718096;
}

.login-link {
  color: #a78bfa;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.login-link:hover {
  color: #c084fc;
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
  .register-card {
    max-width: 420px;
  }
  
  .card-content {
    flex-direction: column;
    min-height: auto;
  }
  
  .content-left {
    padding: 30px 25px;
    flex: none;
    border-right: none;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  }
  
  .content-right {
    padding: 0;
  }
  
  .form-container {
    padding: 30px 25px;
  }
  
  .register-title {
    font-size: 1.5rem;
  }
}

@media (max-width: 480px) {
  .register-card {
    max-width: 360px;
  }
  
  .form-container {
    padding: 25px 20px;
  }
  
  .content-left {
    padding: 25px 20px;
  }
}
</style>