<!--
  用户登录面板组件
  
  该组件提供了一个现代化的用户登录界面，支持邮箱或用户名登录。
  采用双栏布局设计，左侧为品牌展示，右侧为登录表单。
  
  功能特性：
  - 支持邮箱或用户名登录
  - 密码显示/隐藏切换
  - 记住我功能
  - 表单验证和错误提示
  - 登录状态加载指示器
  - 注册页面切换链接
  
  设计特点：
  - 渐变背景和卡片设计
  - 装饰性浮动元素动画
  - 响应式布局和移动端适配
  - 平滑的交互动画效果
  - 现代化的表单设计
  
  数据流程：
  - 自动识别输入类型（邮箱/用户名）
  - 调用认证API进行登录验证
  - 成功后保存用户信息和令牌
  - 通知父组件关闭模态框
  
  技术实现：
  - Vue 3 Composition API
  - TypeScript 类型安全
  - Reactive 表单数据管理
  - CSS 动画和渐变效果
  - 自定义 SVG 图标
  
  使用场景：
  - 用户身份验证入口
  - 会员系统登录界面
  - 受保护资源访问控制
  
  @author EcoWiki Team
  @version 1.0.0
  @since 2024-01-01
-->
<template>
  <div class="login-card">
    <div class="card-content">
      <!-- 左侧内容区 - 品牌展示和欢迎信息 -->
      <div class="content-left">
        <!-- 装饰性背景动画元素 -->
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
      
      <!-- 右侧表单区 - 登录表单 -->
      <div class="content-right">
        <div class="form-container">
          <form @submit.prevent="handleLogin" class="login-form">
            <!-- 用户名/邮箱输入框 -->
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
            
            <!-- 密码输入框（支持显示/隐藏切换） -->
            <div class="form-group password-group">
              <input
                id="password"
                v-model="formData.password"
                :type="showPassword ? 'text' : 'password'"
                class="form-input"
                placeholder="密码"
                required
              />
              <!-- 密码显示/隐藏切换按钮 -->
              <button
                type="button"
                class="password-toggle"
                @click="togglePasswordVisibility"
                :title="showPassword ? '隐藏密码' : '显示密码'"
              >
                <!-- 眼睛图标 - 显示状态 -->
                <svg v-if="showPassword" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <!-- 眼睛图标 - 隐藏状态 -->
                <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                  <line x1="1" y1="1" x2="23" y2="23"/>
                </svg>
              </button>
            </div>
            
            <!-- 记住我复选框 -->
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
            
            <!-- 登录提交按钮 -->
            <button
              type="submit"
              class="login-button"
              :disabled="isLoading"
            >
              <span v-if="isLoading" class="loading-spinner"></span>
              {{ isLoading ? '登录中...' : '登录' }}
            </button>
            <!-- 其他登录表单内容 -->
            <div class="form-footer">
              <span class="register-prompt">登不上去？</span>
            <a href="#" @click="$emit('switchToForgot')" class="register-link">忘记密码</a>
            </div>
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
/**
 * 用户登录面板组件脚本
 * 
 * 实现用户登录的核心逻辑，包括表单验证、API调用、状态管理和错误处理。
 * 支持邮箱或用户名两种登录方式，并提供完整的用户体验功能。
 */

import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '../api/user'
import { useAuth } from '../composables/useAuth'
import toast from '../utils/toast'

/**
 * 组件事件定义
 * 定义了组件向父组件发送的事件
 */
const emit = defineEmits(['switchToRegister', 'loginSuccess','switchToForgot'])

/**
 * 认证状态管理
 * 使用全局认证状态管理，处理用户信息和令牌
 */
const { setUser } = useAuth()

/**
 * 响应式状态管理
 */
// 登录加载状态
const isLoading = ref(false)
// 密码显示状态
const showPassword = ref(false)

/**
 * 登录表单数据
 * 使用 reactive 创建响应式对象，自动追踪表单字段变化
 */
const formData = reactive({
  /** 登录字段 - 支持邮箱或用户名 */
  loginField: '', 
  /** 用户密码 */
  password: '',
  /** 是否记住用户登录状态 */
  rememberMe: false
})

/**
 * 切换密码显示状态
 * 
 * 切换密码输入框的文本显示和隐藏状态，提升用户体验。
 * 
 * @example
 * // 点击眼睛图标时调用
 * togglePasswordVisibility()
 */
const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value
}

/**
 * 判断输入的是邮箱还是用户名
 * 
 * 使用正则表达式验证输入内容的格式，自动识别登录方式。
 * 支持标准邮箱格式验证。
 * 
 * @param {string} input - 用户输入的登录字段
 * @returns {boolean} true 表示邮箱格式，false 表示用户名格式
 * 
 * @example
 * isEmail('user@example.com')  // true
 * isEmail('username')          // false
 */
const isEmail = (input: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(input)
}

/**
 * 用户登录处理函数
 * 
 * 处理用户登录的核心逻辑，包括表单验证、API调用、成功处理和错误处理。
 * 自动识别登录方式（邮箱或用户名），调用相应的API接口。
 * 
 * 处理流程：
 * 1. 表单验证 - 检查必填字段
 * 2. 设置加载状态 - 显示登录进度
 * 3. 识别登录方式 - 邮箱或用户名
 * 4. 调用登录API - 发送认证请求
 * 5. 处理成功响应 - 保存用户信息和令牌
 * 6. 显示成功提示 - 用户反馈
 * 7. 通知父组件 - 关闭登录模态框
 * 8. 错误处理 - 显示错误信息
 * 
 * @async
 * @returns {Promise<void>} 异步处理登录逻辑
 * 
 * @example
 * // 表单提交时自动调用
 * <form @submit.prevent="handleLogin">
 */
const handleLogin = async () => {
  // 1. 表单验证
  if (!formData.loginField || !formData.password) {
    toast.warning('请填写完整的登录信息')
    return
  }
  
  // 2. 设置加载状态
  isLoading.value = true
  
  try {
    // 3. 根据输入内容判断登录方式并构建请求数据
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
    
    // 4. 调用登录API
    const response = await userApi.login(loginData)
    
    // 5. 登录成功，保存用户信息和token
    setUser(response.user, response.token, response.refreshToken)
    
    // 6. 显示成功提示
    toast.success(`欢迎回来，${response.user.username}！`, '登录成功')
    
    // 7. 通知父组件登录成功，关闭模态框
    emit('loginSuccess')
    
    // 调试信息
    console.log('登录成功，用户信息:', response.user)
    
  } catch (error: any) {
    // 8. 错误处理
    console.error('登录失败:', error)
    toast.error(error.message || '登录失败，请检查登录信息和密码', '登录失败')
  } finally {
    // 重置加载状态
    isLoading.value = false
  }
}
const router = useRouter()

</script>

<style scoped>
/**
 * 用户登录面板样式
 * 
 * 实现现代化的登录界面设计，包括渐变背景、卡片布局、表单样式和动画效果。
 * 采用双栏设计，左侧品牌展示，右侧登录表单，提供优雅的用户体验。
 */

/* 登录卡片主容器 */
.login-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);  /* 渐变背景 */
  border-radius: 20px;                    /* 大圆角设计 */
  box-shadow: 0 20px 40px rgba(102, 126, 234, 0.3);  /* 深度阴影 */
  overflow: hidden;                       /* 隐藏溢出内容 */
  transition: all 0.4s ease;             /* 平滑过渡动画 */
  animation: cardFadeIn 0.6s ease-out;   /* 进入动画 */
  max-width: 480px;                      /* 最大宽度限制 */
  margin: 0 auto;                        /* 居中显示 */
}

/* 卡片进入动画 */
@keyframes cardFadeIn {
  from {
    opacity: 0;                           /* 初始透明 */
    transform: translateY(20px) scale(0.98);  /* 向上移动并缩放 */
  }
  to {
    opacity: 1;                           /* 完全不透明 */
    transform: translateY(0) scale(1);    /* 回到原位置和大小 */
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

.password-group {
  position: relative;
}

.password-toggle {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #a0aec0;
  cursor: pointer;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.3s ease;
  z-index: 10;
}

.password-toggle:hover {
  color: #667eea;
}

.password-toggle:focus {
  outline: none;
  color: #667eea;
}

.password-toggle svg {
  width: 20px;
  height: 20px;
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

.password-group .form-input {
  padding-right: 40px;
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