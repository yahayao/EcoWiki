<!--
/**
 * 忘记密码组件
 * 
 * 提供用户忘记密码时的重置功能，通过邮箱和安全问题验证用户身份后重置密码。
 * 该组件是用户认证流程的重要组成部分，确保用户能够安全地恢复账户访问权限。
 * 
 * 主要功能：
 * - 邮箱验证：用户输入注册时使用的邮箱地址
 * - 安全问题：通过安全问题答案进行身份验证
 * - 密码重置：验证通过后重置用户密码
 * - 表单验证：确保输入数据的完整性和有效性
 * - 错误处理：友好的错误提示和异常处理
 * 
 * 业务流程：
 * 1. 用户输入注册邮箱地址
 * 2. 系统显示对应的安全问题（通过Store获取）
 * 3. 用户输入安全问题的答案
 * 4. 提交表单进行验证
 * 5. 验证成功后重置密码并跳转到忘记密码页
 * 
 * 安全特性：
 * - 邮箱验证：确保邮箱格式正确且已注册
 * - 安全问题：双重验证机制，提高安全性
 * - 敏感信息：不在前端存储用户密码等敏感信息
 * - 错误提示：避免泄露过多系统信息
 * 
 * 数据流：
 * - email：用户输入的邮箱地址
 * - answer：用户输入的安全问题答案
 * - securityQuestion：从Store获取的安全问题文本
 * 
 * API交互：
 * - POST /auth/forgot-password：提交忘记密码请求
 *   请求参数：{ email, answer }
 *   响应格式：{ success: boolean, message: string }
 * 
 * 状态管理：
 * - 使用Vuex Store获取安全问题信息
 * - 计算属性securityQuestion从store.state.securityQuestion获取
 * 
 * 用户体验：
 * - 表单验证：required属性确保必填字段
 * - 友好提示：成功和失败都有相应的用户提示
 * - 页面跳转：重置成功后自动跳转到忘记密码页
 * - 响应式设计：适配不同设备屏幕尺寸
 * 
 * 错误处理：
 * - try-catch捕获网络异常
 * - alert提示用户操作结果
 * - console.error记录错误日志
 * 
 * 扩展性：
 * - 可扩展短信验证、邮箱验证码等验证方式
 * - 支持多种密码重置策略
 * - 可集成更复杂的安全验证机制
 * 
 * 注意事项：
 * - 表单提交使用@submit.prevent阻止默认行为
 * - 安全问题从后端动态获取，避免前端硬编码
 * - 密码重置成功后应清除表单数据
 * - 生产环境建议使用更安全的提示方式
 * 
 * @author EcoWiki团队
 * @version 1.0
 * @since 2024-01-01
 */
-->

<template>
  <div class="forgot-card">
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
        <div class="forgot-header">
          <h1 class="forgot-title">密码忘了？</h1>
          <p class="forgot-subtitle">别担心交给我们</p>
        </div>
      </div>
      
      <!-- 右侧表单区 - 忘记密码表单 -->
      <div class="content-right">
        <div class="form-container">
          <form @submit.prevent="handleForgot" class="forgot-form">
            <!-- 用户名/邮箱输入框 -->
            <div class="form-group">
              <input
                id="username"
                v-model="formData.username"
                type="text"
                class="form-input"
                placeholder="邮箱或用户名"
                required
              />
            </div>
            <!-- 验证问题输入字段（可选） -->
            <div class="form-group">
              <input
                id="securityAnswer"
                v-model="formData.securityAnswer"
                type="text"
                class="form-input"
                placeholder="安全问题答案"
              />
            </div>
            
            <!-- 忘记密码提交按钮 -->
            <button
              type="submit"
              class="forgot-button"
              :disabled="isLoading"
            >
              <span v-if="isLoading" class="loading-spinner"></span>
              {{ isLoading ? '安全问题匹配中...' : '验证' }}
            </button>
            <div>
            <!-- 其他忘记密码表单内容 -->
            <button @click="goToForgotPassword">忘记密码</button>
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
const emit = defineEmits(['switchToRegister', 'loginSuccess'])

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
  username: '', 
  /** 用户密码 */
  password: '',
  /** 是否记住用户登录状态 */
  rememberMe: false,
  securityAnswer: '' // 安全问题答案
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
const handleForgot = async () => {
  // 1. 表单验证
  if (!formData.username || !formData.securityAnswer) {
    toast.warning('请填写完整的验证信息')
    return
  }
  
  // 2. 设置加载状态
  isLoading.value = true
/**忘记密码保存点！！！！！！！！！！！！！！！！！！！！！ */
  try {
    // 3. 根据输入内容判断登录方式并构建请求数据
    const loginData = isEmail(formData.username) 
      ? {
          email: formData.username,
          password: formData.password,
          rememberMe: formData.rememberMe
        }
      : {
          username: formData.username,
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

const goToForgotPassword = () => {
  router.push('/forgot-password')
}
</script>
<style scoped>
/**
 * 用户忘记密码面板样式
 * 
 * 实现现代化的忘记密码界面设计，包括渐变背景、卡片布局、表单样式和动画效果。
 * 采用双栏设计，左侧品牌展示，右侧忘记密码表单，提供优雅的用户体验。
 */

/* 忘记密码卡片主容器 */
.forgot-card {
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

.forgot-header {
  text-align: center;
}

.forgot-title {
  color: white;
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.forgot-subtitle {
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

.forgot-form {
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

.forgot-button {
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

.forgot-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s;
}

.forgot-button:hover::before {
  left: 100%;
}

.forgot-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
}

.forgot-button:disabled {
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
  .forgot-card {
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
  
  .forgot-title {
    font-size: 1.5rem;
  }
}

@media (max-width: 480px) {
  .forgot-card {
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