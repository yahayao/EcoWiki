<template>
  <div class="register-card">
    <div class="register-header">
      <h1 class="register-title">注册 EcoWiki 账户</h1>
      <p class="register-subtitle">创建您的新账户</p>
    </div>
      
    <form @submit.prevent="handleRegister" class="register-form">
      <div class="form-group">
        <label for="username" class="form-label">用户名</label>
        <div class="input-wrapper">
          <input
            id="username"
            v-model="formData.username"
            @input="onUsernameChange"
            type="text"
            class="form-input"
            :class="{ 
              'error': showErrors && errors.username,
              'success': usernameAvailable === true,
              'checking': usernameChecking
            }"
            placeholder="请输入用户名"
            required
          />
          <div class="input-status">
            <span v-if="usernameChecking" class="checking-icon">⏳</span>
            <span v-else-if="usernameAvailable === true" class="success-icon">✓</span>
            <span v-else-if="usernameAvailable === false" class="error-icon">✗</span>
          </div>
        </div>
        <div class="error-message" v-if="showErrors && errors.username">
          {{ errors.username }}
        </div>
        <div class="error-message" v-else-if="usernameAvailable === false">
          该用户名已被使用
        </div>
      </div>
      
      <div class="form-group">
        <label for="email" class="form-label">邮箱</label>
        <div class="input-wrapper">
          <input
            id="email"
            v-model="formData.email"
            @input="onEmailChange"
            type="email"
            class="form-input"
            :class="{ 
              'error': showErrors && errors.email,
              'success': emailAvailable === true,
              'checking': emailChecking
            }"
            placeholder="请输入邮箱地址"
            required
          />
          <div class="input-status">
            <span v-if="emailChecking" class="checking-icon">⏳</span>
            <span v-else-if="emailAvailable === true" class="success-icon">✓</span>
            <span v-else-if="emailAvailable === false" class="error-icon">✗</span>
          </div>
        </div>
        <div class="error-message" v-if="showErrors && errors.email">
          {{ errors.email }}
        </div>
        <div class="error-message" v-else-if="emailAvailable === false">
          该邮箱已被注册
        </div>
      </div>
      
      <div class="form-group">
        <label for="password" class="form-label">密码</label>
        <input
          id="password"
          v-model="formData.password"
          type="password"
          class="form-input"
          :class="{ 'error': showErrors && errors.password }"
          placeholder="请输入密码"
          required
        />
        <div class="error-message" v-if="showErrors && errors.password">
          {{ errors.password }}
        </div>
      </div>
      
      <div class="form-group">
        <label for="confirmPassword" class="form-label">确认密码</label>
        <input
          id="confirmPassword"
          v-model="formData.confirmPassword"
          type="password"
          class="form-input"
          :class="{ 'error': showErrors && errors.confirmPassword }"
          placeholder="请再次输入密码"
          required
        />
        <div class="error-message" v-if="showErrors && errors.confirmPassword">
          {{ errors.confirmPassword }}
        </div>
      </div>
      
      <button
        type="submit"
        class="register-button"
        :disabled="isLoading || !canSubmit"
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
import { ref, reactive, computed } from 'vue'
import { userApi } from '../api/user'
import { validateRegisterForm, debounce, type FormErrors } from '../utils/validation'
import { useAuth } from '../composables/useAuth'
import toast from '../utils/toast'

// 定义 emits
const emit = defineEmits(['switchToLogin'])

// 使用认证状态
const { setUser } = useAuth()

// 响应式数据
const isLoading = ref(false)
const errors = ref<FormErrors>({})
const showErrors = ref(false)

const formData = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 实时验证状态
const usernameChecking = ref(false)
const emailChecking = ref(false)
const usernameAvailable = ref<boolean | null>(null)
const emailAvailable = ref<boolean | null>(null)

// 计算是否可以提交表单
const canSubmit = computed(() => {
  const formErrors = validateRegisterForm(formData)
  return Object.keys(formErrors).length === 0 && 
         usernameAvailable.value === true && 
         emailAvailable.value === true
})

// 防抖检查用户名可用性
const checkUsernameAvailability = debounce(async (username: string) => {
  if (!username || username.length < 3) return
  
  usernameChecking.value = true
  try {
    usernameAvailable.value = await userApi.checkUsername(username)
  } catch (error) {
    console.error('检查用户名失败:', error)
    usernameAvailable.value = null
  } finally {
    usernameChecking.value = false
  }
}, 500)

// 防抖检查邮箱可用性
const checkEmailAvailability = debounce(async (email: string) => {
  if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return
  
  emailChecking.value = true
  try {
    emailAvailable.value = await userApi.checkEmail(email)
  } catch (error) {
    console.error('检查邮箱失败:', error)
    emailAvailable.value = null
  } finally {
    emailChecking.value = false
  }
}, 500)

// 监听用户名变化
const onUsernameChange = () => {
  usernameAvailable.value = null
  if (formData.username) {
    checkUsernameAvailability(formData.username)
  }
}

// 监听邮箱变化
const onEmailChange = () => {
  emailAvailable.value = null
  if (formData.email) {
    checkEmailAvailability(formData.email)
  }
}

// 注册处理函数
const handleRegister = async () => {
  // 显示错误提示
  showErrors.value = true
  
  // 验证表单
  errors.value = validateRegisterForm(formData)
  
  if (!canSubmit.value) {
    toast.warning('请检查并修正表单中的错误')
    return
  }
  
  isLoading.value = true
  
  try {
    // 调用注册API
    const response = await userApi.register({
      username: formData.username,
      email: formData.email,
      password: formData.password
    })
    
    // 注册成功，保存用户信息和token
    setUser(response.user, response.token, response.refreshToken)
    
    toast.success('注册成功！欢迎加入 EcoWiki', '注册成功')
    
    // 延迟跳转，让用户看到成功消息
    setTimeout(() => {
      emit('switchToLogin')
    }, 2000)
    
  } catch (error: any) {
    console.error('注册失败:', error)
    
    // 处理特定的注册错误
    if (error.message.includes('用户名')) {
      usernameAvailable.value = false
      toast.error('该用户名已被使用，请选择其他用户名')
    } else if (error.message.includes('邮箱')) {
      emailAvailable.value = false
      toast.error('该邮箱已被注册，请使用其他邮箱')
    } else {
      toast.error(error.message || '注册失败，请重试', '注册失败')
    }
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

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.form-label {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.form-input {
  padding: 12px 40px 12px 16px;
  border: 2px solid #e1e5e9;
  border-radius: 8px;
  font-size: 16px;
  transition: all 0.3s ease;
  background: #f8f9fa;
  flex: 1;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-input.error {
  border-color: #e74c3c;
  background: #ffeaea;
}

.form-input.success {
  border-color: #27ae60;
  background: #eafaf1;
}

.form-input.checking {
  border-color: #f39c12;
}

.input-status {
  position: absolute;
  right: 12px;
  display: flex;
  align-items: center;
}

.success-icon {
  color: #27ae60;
  font-weight: bold;
  font-size: 16px;
}

.error-icon {
  color: #e74c3c;
  font-weight: bold;
  font-size: 16px;
}

.checking-icon {
  font-size: 14px;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.error-message {
  color: #e74c3c;
  font-size: 12px;
  margin-top: 4px;
  min-height: 16px;
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