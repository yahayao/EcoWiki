<!--
    用户注册面板组件
    
    该组件提供了一个功能完整的用户注册界面，支持实时验证和可用性检查。
    采用与登录面板相似的双栏布局设计，左侧为品牌展示，右侧为注册表单。
    
    功能特性：
    - 完整的注册表单（用户名、邮箱、密码、确认密码、姓名）
    - 实时用户名和邮箱可用性检查
    - 密码强度验证和显示/隐藏切换
    - 确认密码一致性验证
    - 表单验证和错误提示
    - 注册状态加载指示器
    - 登录页面切换链接
    
    验证功能：
    - 用户名格式和可用性检查
    - 邮箱格式和可用性检查
    - 密码强度要求验证
    - 确认密码一致性验证
    - 实时状态指示器（检查中、成功、失败）
    
    设计特点：
    - 渐变背景和卡片设计
    - 装饰性浮动元素动画
    - 响应式布局和移动端适配
    - 现代化的表单设计
    - 状态指示器和错误提示
    
    数据流程：
    - 实时验证表单字段
    - 异步检查用户名和邮箱可用性
    - 调用注册API创建新用户
    - 成功后自动登录并保存状态
    - 通知父组件关闭模态框
    
    技术实现：
    - Vue 3 Composition API
    - TypeScript 类型安全
    - Reactive 表单数据管理
    - 防抖优化API调用
    - CSS 动画和渐变效果
    
    使用场景：
    - 新用户账户创建
    - 会员系统注册流程
    - 用户身份验证扩展
    
    @author EcoWiki Team
    @version 1.0.0
    @since 2024-01-01
  -->
  <template>
    <div class="register-card">
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
          <div class="register-header">
            <h1 class="register-title">加入我们！</h1>
            <p class="register-subtitle">开启探索之旅</p>
          </div>
        </div>
        
        <!-- 右侧表单区 - 注册表单 -->
        <div class="content-right">
          <div class="form-container">
            <form @submit.prevent="handleRegister" class="register-form">
              <!-- 用户名输入框（带可用性检查） -->
              <div class="form-group">
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
                    placeholder="用户名"
                    required
                  />
                  <!-- 状态指示器 -->
                  <div class="input-status">
                    <span v-if="usernameChecking" class="checking-icon">⏳</span>
                    <span v-else-if="usernameAvailable === true" class="success-icon">✓</span>
                    <span v-else-if="usernameAvailable === false" class="error-icon">✗</span>
                  </div>
                </div>
                <!-- 错误消息显示 -->
                <div class="error-message" v-if="showErrors && errors.username">
                  {{ errors.username }}
                </div>
                <div class="error-message" v-else-if="usernameAvailable === false">
                  该用户名已被使用
                </div>
              </div>
              
              <!-- 邮箱输入框（带可用性检查） -->
              <div class="form-group">
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
                    placeholder="邮箱"
                    required
                  />
                  <!-- 状态指示器 -->
                  <div class="input-status">
                    <span v-if="emailChecking" class="checking-icon">⏳</span>
                    <span v-else-if="emailAvailable === true" class="success-icon">✓</span>
                    <span v-else-if="emailAvailable === false" class="error-icon">✗</span>
                  </div>
                </div>
                <!-- 错误消息显示 -->
                <div class="error-message" v-if="showErrors && errors.email">
                  {{ errors.email }}
                </div>
                <div class="error-message" v-else-if="emailAvailable === false">
                  该邮箱已被注册
                </div>
              </div>
              
              <!-- 全名输入字段（可选） -->
              <div class="form-group">
                <input
                  id="fullName"
                  v-model="formData.fullName"
                  type="text"
                  class="form-input"
                  placeholder="姓名（可选）"
                />
              </div>
              
              <div class="form-group">
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
                <input
                  id="confirmPassword"
                  v-model="formData.confirmPassword"
                  type="password"
                  class="form-input"
                  :class="{ 'error': showErrors && errors.confirmPassword }"
                  placeholder="确认密码"
                  required
                />
                <div class="error-message" v-if="showErrors && errors.confirmPassword">
                  {{ errors.confirmPassword }}
                </div>
              </div>
              
              <!-- 人机验证按钮/注册按钮切换 -->
              <div class="form-group slider-btn-group">
                <button
                  v-if="!sliderVerified"
                  type="button"
                  class="verify-human-btn"
                  @click="showSliderModal = true"
                  :disabled="isLoading"
                >
                  验证是否是人机
                </button>
                <button
                  v-else
                  type="submit"
                  class="register-button"
                  :disabled="isLoading || !canSubmit"
                >
                  <span v-if="isLoading" class="loading-spinner"></span>
                  {{ isLoading ? '创建中...' : '注册' }}
                </button>
              </div>

              <!-- 滑块弹窗 -->
              <div v-if="showSliderModal" class="slider-modal-mask" :class="{ active: showSliderModal }" @click="handleMaskClick">
                <div class="slider-modal">
                  <div class="slider-modal-header">
                    <div class="slider-modal-title">
                      <span class="security-icon">🛡️</span>
                      安全验证
                    </div>
                    <button class="slider-modal-close" @click="closeSliderModal">
                      <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                        <path d="M12 4L4 12M4 4L12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                      </svg>
                    </button>
                  </div>
                  
                  <div class="slider-modal-content">
                    <div class="verification-instruction">
                      <p v-if="!sliderVerified">请拖动滑块完成验证</p>
                      <p v-else class="success-text">
                        <span class="success-icon">✓</span>
                        验证成功
                      </p>
                    </div>
                    
                    <!-- 使用新的滑块组件 -->
                    <div class="new-slider-container">
                      <SliderCaptcha 
                        ref="sliderCaptchaRef"
                        @success="handleSliderSuccess"
                        @reset="handleSliderReset"
                      />
                    </div>
                  </div>
                </div>
              </div>

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
  import { ref, reactive, computed, onMounted, onBeforeUnmount, watch } from 'vue'
  import { userApi } from '../api/user'
  import { validateRegisterForm, debounce, type FormErrors } from '../utils/validation'
  import { useAuth } from '../composables/useAuth'
  import toast from '../utils/toast'
  import SliderCaptcha from './SliderCaptcha.vue'

  // 定义 emits
  const emit = defineEmits(['switchToLogin', 'registerSuccess'])

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
    confirmPassword: '',
    fullName: '' // 添加全名字段
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
      return
    }
    
    isLoading.value = true
    
    try {
      // 调用注册API
      const response = await userApi.register({
        username: formData.username,
        email: formData.email,
        password: formData.password,
        fullName: formData.fullName // 传递全名
      })
      
      // 注册成功，保存用户信息和token
      setUser(response.user, response.token, response.refreshToken)
      
      toast.success('注册成功！欢迎加入EcoWiki', '注册成功')
      
      // 通知父组件注册成功，关闭模态框
      emit('registerSuccess')
      
    } catch (error: any) {
      console.error('注册失败:', error)
      
      // 处理特定的注册错误
      if (error.message.includes('用户名')) {
        usernameAvailable.value = false
      } else if (error.message.includes('邮箱')) {
        emailAvailable.value = false
      }
      
      toast.error(error.message || '注册失败，请重试', '注册失败')
    } finally {
      isLoading.value = false
    }
  }

  // 新的滑块验证相关
  const sliderVerified = ref(false)
  const showSliderModal = ref(false)
  const sliderCaptchaRef = ref<InstanceType<typeof SliderCaptcha> | null>(null)

  // 处理遮罩点击
  const handleMaskClick = (e: MouseEvent) => {
    if (e.target === e.currentTarget) {
      closeSliderModal()
    }
  }

  // 滑块验证成功处理
  const handleSliderSuccess = () => {
    sliderVerified.value = true
    console.log('滑块验证成功')
    
    // 延迟关闭弹窗
    setTimeout(() => {
      showSliderModal.value = false
    }, 1500)
  }

  // 滑块重置处理
  const handleSliderReset = () => {
    console.log('滑块已重置')
  }

  // 关闭滑块弹窗
  const closeSliderModal = () => {
    showSliderModal.value = false
    // 重置滑块状态
    if (sliderCaptchaRef.value) {
      sliderCaptchaRef.value.reset()
    }
    sliderVerified.value = false
  }

  // 监听弹窗显示状态，重置滑块
  watch(showSliderModal, (val) => {
    if (val) {
      // 弹窗打开时重置滑块
      setTimeout(() => {
        if (sliderCaptchaRef.value) {
          sliderCaptchaRef.value.reset()
        }
        sliderVerified.value = false
      }, 100)
    }
  })

  onMounted(() => {
    // 移除旧的事件监听器，新滑块组件自己处理
  })
  
  onBeforeUnmount(() => {
    // 清理工作
  })
  </script>

  <style scoped>
  .register-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 20px;
    box-shadow: 0 20px 40px rgba(102, 126, 234, 0.3);
    overflow: hidden;
    transition: all 0.4s ease;
    animation: cardFadeIn 0.6s ease-out;
    max-width: 500px;
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
    min-height: 600px;
  }

  .content-left {
    flex: 1;
    background: linear-gradient(135deg, rgba(255,255,255,0.95) 0%, rgba(248,250,252,0.98) 100%);
    padding: 40px 30px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    position: relative;
    overflow: hidden;
    backdrop-filter: blur(20px);
    border-right: 1px solid rgba(255, 255, 255, 0.2);
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
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.08) 0%, rgba(118, 75, 162, 0.12) 100%);
    animation: float 8s ease-in-out infinite;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(102, 126, 234, 0.1);
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
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 15px;
    box-shadow: 0 8px 25px rgba(102, 126, 234, 0.25);
    border: 3px solid rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    position: relative;
  }

  .logo-circle::before {
    content: '';
    position: absolute;
    top: -3px;
    left: -3px;
    right: -3px;
    bottom: -3px;
    background: linear-gradient(135deg, #667eea, #764ba2);
    border-radius: 50%;
    z-index: -1;
    opacity: 0.6;
    filter: blur(8px);
  }

  .logo-text {
    color: white;
    font-size: 0.9rem;
    font-weight: 700;
    letter-spacing: -0.5px;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    z-index: 1;
    position: relative;
  }

  .register-header {
    text-align: center;
  }

  .register-title {
    color: #1a202c;
    font-size: 1.8rem;
    font-weight: 700;
    margin-bottom: 8px;
    letter-spacing: -0.5px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    text-shadow: 0 2px 4px rgba(0,0,0,0.05);
  }

  .register-subtitle {
    color: #4a5568;
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

  .input-wrapper {
    position: relative;
    display: flex;
    align-items: center;
  }

  .form-input {
    width: 100%;
    padding: 14px 40px 14px 0;
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

  .form-input.error {
    border-bottom-color: #e74c3c;
    background: rgba(231, 76, 60, 0.02);
  }

  .form-input.success {
    border-bottom-color: #27ae60;
    background: rgba(39, 174, 96, 0.02);
  }

  .form-input.checking {
    border-bottom-color: #f39c12;
  }

  .input-status {
    position: absolute;
    right: 0;
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
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.35);
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
    box-shadow: 0 10px 25px rgba(102, 126, 234, 0.45);
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
    color: #667eea;
    text-decoration: none;
    font-weight: 600;
    transition: color 0.3s ease;
  }

  .login-link:hover {
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



  .slider-btn-group {
    margin-bottom: 18px;
    text-align: center;
  }
  .verify-human-btn {
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
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.35);
    display: block;
  }
  .verify-human-btn::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
    transition: left 0.5s;
  }
  .verify-human-btn:hover::before {
    left: 100%;
  }
  .verify-human-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 25px rgba(102, 126, 234, 0.45);
  }
  .verify-human-btn:disabled {
    opacity: 0.7;
    cursor: not-allowed;
    transform: none;
  }

  /* 滑块弹窗美化 */
  .slider-modal-mask {
    position: fixed;
    left: 0; top: 0; right: 0; bottom: 0;
    background: rgba(0, 0, 0, 0.25);
    z-index: 10000;
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(12px) saturate(1.3);
    -webkit-backdrop-filter: blur(12px) saturate(1.3);
    opacity: 0;
    visibility: hidden;
    transition: all 0.3s ease;
  }

  .slider-modal-mask.active {
    opacity: 1;
    visibility: visible;
  }

  .slider-modal {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 24px;
    width: 480px;
    max-width: 90vw;
    box-shadow: 
      0 20px 60px 0 rgba(102, 126, 234, 0.15),
      0 8px 32px 0 rgba(118, 75, 162, 0.1),
      0 0 0 1px rgba(255, 255, 255, 0.8) inset;
    position: relative;
    animation: modalSlideIn 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
    backdrop-filter: blur(20px) saturate(1.8);
    -webkit-backdrop-filter: blur(20px) saturate(1.8);
    border: 1px solid rgba(255, 255, 255, 0.3);
    overflow: hidden;
  }

  @keyframes modalSlideIn {
    from { 
      opacity: 0; 
      transform: scale(0.9) translateY(20px); 
    }
    to { 
      opacity: 1; 
      transform: scale(1) translateY(0); 
    }
  }

  .slider-modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24px 32px 0;
    border-bottom: 1px solid rgba(102, 126, 234, 0.08);
    margin-bottom: 32px;
  }

  .slider-modal-title {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 20px;
    font-weight: 700;
    color: #2d3748;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    letter-spacing: -0.5px;
  }

  .security-icon {
    font-size: 24px;
    filter: drop-shadow(0 2px 4px rgba(102, 126, 234, 0.2));
  }

  .slider-modal-close {
    width: 36px;
    height: 36px;
    border: none;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s ease;
    color: #6b7280;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .slider-modal-close:hover {
    background: rgba(239, 68, 68, 0.1);
    color: #ef4444;
    transform: rotate(90deg);
    box-shadow: 0 4px 12px rgba(239, 68, 68, 0.2);
  }

  .slider-modal-content {
    padding: 0 32px 32px;
  }

  .verification-instruction {
    text-align: center;
    margin-bottom: 24px;
  }

  .verification-instruction p {
    font-size: 16px;
    color: #4a5568;
    margin: 0;
    font-weight: 500;
    letter-spacing: 0.5px;
  }

  .success-text {
    color: #10b981 !important;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    font-weight: 600 !important;
  }

  .success-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    background: linear-gradient(135deg, #10b981, #059669);
    color: white;
    border-radius: 50%;
    font-size: 12px;
    font-weight: bold;
    animation: successPulse 0.6s ease-out;
  }

  @keyframes successPulse {
    0% { transform: scale(0); }
    50% { transform: scale(1.2); }
    100% { transform: scale(1); }
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
      border-bottom: 1px solid rgba(255, 255, 255, 0.2);
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

  /* 新滑块容器样式 */
  .new-slider-container {
    margin: 20px 0;
    padding: 0;
  }
  </style>
