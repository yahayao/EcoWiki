<!--
/**
 * Wiki页面创建组件
 * 
 * 功能：
 * - 提供简化的表单界面创建新的Wiki页面
 * - 页面标题输入和实时验证
 * - URL预览和可用性检查
 * - 创建后行为选择（编辑/查看）
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
-->
<template>
  <div class="create-page">
    <!-- 页面头部 -->
    <div class="header">
      <div class="container">
        <div class="header-content">
          <button @click="goBack" class="back-btn">
            <svg viewBox="0 0 24 24" class="icon">
              <path d="M20,11V13H8L13.5,18.5L12.08,19.92L4.16,12L12.08,4.08L13.5,5.5L8,11H20Z"/>
            </svg>
            返回
          </button>
          <div class="header-text">
            <h1 class="page-title">创建新页面</h1>
            <p class="page-subtitle">为EcoWiki创建一篇新的文章</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="container">
      <div class="create-form">
        <!-- 基础信息 -->
        <div class="form-section">
          <h2 class="section-title">基础信息</h2>
          
          <!-- 页面标题 -->
          <div class="form-group">
            <label for="pageTitle" class="form-label">
              页面标题 <span class="required">*</span>
            </label>
            <div class="input-wrapper">
              <input
                id="pageTitle"
                v-model="form.title"
                type="text"
                class="form-input"
                :class="{ 
                  error: titleError,
                  success: titleAvailable === true,
                  checking: checkingTitle
                }"
                placeholder="请输入页面标题..."
                @input="debouncedValidateTitle"
                @blur="validateTitle"
              />
              <!-- 状态指示器 -->
              <div class="input-status">
                <span v-if="checkingTitle" class="checking-icon">⏳</span>
                <span v-else-if="titleAvailable === true" class="success-icon">✓</span>
                <span v-else-if="titleAvailable === false" class="error-icon">✗</span>
              </div>
            </div>
            <div v-if="titleError" class="form-error">{{ titleError }}</div>
            <div v-else-if="titleAvailable === false" class="form-error">该标题已存在，请选择其他标题</div>
            <div v-else-if="form.title && titleAvailable === true" class="form-help">
              URL预览: <code>/wiki/{{ encodeURIComponent(form.title) }}</code>
            </div>
          </div>

          <!-- 创建选项 -->
          <div class="form-group">
            <label class="checkbox-label">
              <input
                v-model="form.openAfterCreate"
                type="checkbox"
                class="checkbox"
              />
              创建后立即打开编辑器
            </label>
            <div class="form-help">创建页面后直接跳转到编辑界面</div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <button @click="goBack" class="btn btn-secondary">取消</button>
          <button 
            @click="createPage" 
            class="btn btn-primary"
            :disabled="!canCreate || creating"
          >
            {{ creating ? '创建中...' : '创建并发布' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { articleApi, type ArticleCreateRequest } from '@/api/article'
import { draftApi } from '@/api/draft'
import toast from '@/utils/toast'

const router = useRouter()
const { isAuthenticated, user } = useAuth()

// 权限检查 - 如果用户未登录，重定向到首页
if (!isAuthenticated.value) {
  router.replace('/')
}

// 防抖函数
const debounce = (func: Function, wait: number) => {
  let timeout: ReturnType<typeof setTimeout>
  return function executedFunction(...args: any[]) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

// 表单数据
const form = ref({
  title: '',
  openAfterCreate: true
})

// 状态管理
const creating = ref(false)
const titleError = ref('')
const checkingTitle = ref(false)
const titleAvailable = ref<boolean | null>(null)

// 页面模板配置 - 使用空白模板
const templates = ref([
  {
    id: 'blank',
    name: '空白页面',
    icon: '📄',
    description: '从头开始创建一个全新的页面',
    content: ''
  }
])

// 验证标题
const validateTitle = async () => {
  const title = form.value.title.trim()
  
  if (!title) {
    titleError.value = '页面标题不能为空'
    titleAvailable.value = null
    return
  }
  
  if (title.length < 2) {
    titleError.value = '页面标题至少需要2个字符'
    titleAvailable.value = null
    return
  }
  
  if (title.length > 100) {
    titleError.value = '页面标题不能超过100个字符'
    titleAvailable.value = null
    return
  }
  
  // 检查特殊字符
  const invalidChars = /[<>:"\/\\|?*]/
  if (invalidChars.test(title)) {
    titleError.value = '页面标题不能包含特殊字符 < > : " / \\ | ? *'
    titleAvailable.value = null
    return
  }
  
  // 检查标题是否已存在
  try {
    checkingTitle.value = true
    titleAvailable.value = null
    const exists = await articleApi.checkTitleExists(title)
    if (exists) {
      titleError.value = '该标题已存在，请选择其他标题'
      titleAvailable.value = false
      return
    } else {
      titleAvailable.value = true
    }
  } catch (error) {
    console.error('检查标题失败:', error)
    titleAvailable.value = null
    // 检查失败不阻止用户继续，但会在创建时再次验证
  } finally {
    checkingTitle.value = false
  }
  
  titleError.value = ''
}

// 计算属性
const canCreate = computed(() => {
  return form.value.title.trim() && !titleError.value && !checkingTitle.value && titleAvailable.value === true
})

// 创建防抖的标题验证函数
const debouncedValidateTitle = debounce(() => {
  if (!form.value.title.trim()) {
    titleError.value = ''
    titleAvailable.value = null
    checkingTitle.value = false
    return
  }
  validateTitle()
}, 500)

// 创建页面
const createPage = async () => {
  if (!canCreate.value) return
  
  // 最终验证标题可用性
  if (!titleAvailable.value) {
    await validateTitle()
    if (!titleAvailable.value) {
      toast.error('请输入有效且可用的页面标题', '创建失败')
      return
    }
  }
  
  creating.value = true
  
  try {
    // 使用空白模板内容
    const content = ''
    
    // 准备文章数据
    const articleData: ArticleCreateRequest = {
      title: form.value.title.trim(),
      content: content,
      category: '未分类',
      tags: '',
      author: user.value?.username || '当前用户'
    }
    
    // 调用API提交草稿等待审核
    const response = await draftApi.submitNewArticle(articleData)
    
    toast.success('页面已提交审核，请等待管理员审核！', '成功')
    
    // 审核制度下，新创建的页面不会立即存在，跳转到首页
    router.push('/')
    
  } catch (error: any) {
    console.error('创建页面失败:', error)
    if (error.message && error.message.includes('已存在')) {
      titleError.value = error.message
      titleAvailable.value = false
      toast.error('标题已存在，请选择其他标题', '创建失败')
    } else {
      toast.error(error.message || '创建页面失败', '错误')
    }
  } finally {
    creating.value = false
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 组件挂载
onMounted(() => {
  // 可以在这里预填一些数据
})
</script>

<style scoped>
.create-page {
  min-height: 100vh;
  background: #f8fafb;
}

.header {
  background: white;
  border-bottom: 1px solid #e2e8f0;
  padding: 20px 0;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.back-btn .icon {
  width: 16px;
  height: 16px;
  fill: currentColor;
}

.header-text {
  flex: 1;
}

.page-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #1a202c;
}

.page-subtitle {
  margin: 4px 0 0 0;
  color: #64748b;
  font-size: 16px;
}

.create-form {
  background: white;
  border-radius: 12px;
  padding: 32px;
  margin: 24px 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.form-section {
  margin-bottom: 32px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-title {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1a202c;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 8px;
}

.form-group {
  margin-bottom: 20px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #374151;
}

.required {
  color: #ef4444;
}

.form-input,
.form-textarea,
.form-select {
  width: 100%;
  padding: 12px 40px 12px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s;
}

.form-input.success {
  border-color: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

.form-input.checking {
  border-color: #f59e0b;
}

.input-status {
  position: absolute;
  right: 12px;
  display: flex;
  align-items: center;
  pointer-events: none;
}

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-input.error {
  border-color: #ef4444;
}

.form-error {
  margin-top: 4px;
  color: #ef4444;
  font-size: 12px;
}

.success-icon {
  color: #10b981;
  font-weight: bold;
  font-size: 16px;
}

.error-icon {
  color: #ef4444;
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

.form-checking {
  margin-top: 4px;
  color: #f59e0b;
  font-size: 12px;
}

.form-help {
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.form-help code {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 11px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: 500;
  color: #374151;
}

.checkbox {
  width: 16px;
  height: 16px;
  accent-color: #667eea;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
}

.btn {
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-secondary {
  background: #f1f5f9;
  color: #64748b;
}

.btn-secondary:hover {
  background: #e2e8f0;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

@media (max-width: 768px) {
  .container {
    padding: 0 16px;
  }
  
  .create-form {
    padding: 20px;
    margin: 16px 0;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .btn {
    width: 100%;
  }
}
</style>
