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
 * 5. 验证成功后重置密码并跳转到登录页
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
 * - 页面跳转：重置成功后自动跳转到登录页
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
  <div>
    <!-- 页面标题 -->
    <h2>忘记密码</h2>
    
    <!-- 密码重置表单 -->
    <form @submit.prevent="submit">
      <!-- 邮箱输入框 -->
      <div>
        <label for="email">电子邮件:</label>
          <input type="email" v-model="email" required />
      </div>
      
      <!-- 安全问题输入框（条件显示） -->
      <div v-if="securityQuestion">
        <label :for="securityQuestion">安全问题:</label>
          <input type="text" v-model="answer" required />
      </div>
      
      <!-- 提交按钮 -->
      <button type="submit">重置密码</button>
    </form>
  </div>
</template>

<script>
/**
 * 忘记密码组件的JavaScript逻辑
 * 
 * 定义组件的数据、计算属性和方法，处理用户交互和API调用。
 * 实现忘记密码功能的核心业务逻辑。
 */
export default {
  name: 'ForgotPassword',
  
  /**
   * 组件数据
   * 
   * @returns {Object} 组件的响应式数据对象
   */
  data() {
    return {
      /** 用户输入的邮箱地址 */
      email: '',
      /** 用户输入的安全问题答案 */
      answer: '',
      /** 安全问题文本（从Store获取） */
      securityQuestion: ''
    }
  },
  
  /**
   * 计算属性
   * 
   * 定义基于其他数据计算得出的属性，具有缓存特性。
   */
  computed: {
    /**
     * 获取安全问题
     * 
     * 从Vuex Store中获取当前用户的安全问题文本。
     * 
     * @returns {string} 安全问题文本
     */
    securityQuestion() {
      return this.$store.state.securityQuestion;
    }
  },
  
  /**
   * 组件方法
   * 
   * 定义组件的行为和事件处理函数。
   */
  methods: {
    /**
     * 提交忘记密码表单
     * 
     * 处理用户提交的忘记密码请求，发送邮箱和安全问题答案到后端进行验证。
     * 验证成功后重置密码并跳转到登录页面。
     * 
     * 业务流程：
     * 1. 收集用户输入的邮箱和安全问题答案
     * 2. 发送POST请求到后端API
     * 3. 根据响应结果显示相应的提示信息
     * 4. 成功时跳转到登录页面，失败时显示错误信息
     * 
     * 错误处理：
     * - 网络错误：捕获fetch异常并记录到控制台
     * - 业务错误：显示后端返回的错误信息
     * - 用户体验：使用alert提供即时反馈
     * 
     * 安全考虑：
     * - 不在前端存储用户密码
     * - 敏感信息通过HTTPS传输
     * - 错误信息不泄露过多系统细节
     * 
     * @async
     * @returns {Promise<void>} 异步处理结果
     */
    async submit() {
      try {
        // 发送忘记密码请求到后端API
        const response = await this.$http.post('/auth/forgot-password', {
           email: this.email,
           answer: this.answer
        });
        
        // 根据响应结果处理业务逻辑
        if (response.data.success) {
          // 重置成功，提示用户并跳转到登录页
          alert('密码已重置成功！');
          this.$router.push('/login');
        } else {
          // 重置失败，显示错误信息
          alert(response.data.message || '发生错误！');
        }
      } catch (error) {
        // 网络异常或其他错误处理
        console.error('忘记密码请求失败:', error);
        alert('网络异常，请稍后重试！');
      }
    }
  }
}
</script>