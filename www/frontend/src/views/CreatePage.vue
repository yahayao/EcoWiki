<!--
  Wiki页面创建界面
  
  提供一个简化的表单界面来创建新的Wiki页面，而不是通过URL直接访问。
  只包含必要的标题输入功能。
  
  主要功能：
  - 页面标题输入和验证
  - URL预览和验证
  - 创建后行为选择
  
  @author EcoWiki Team
  @version 2.0.0 (简化版本)
  @since 2025-07-22
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
            <input
              id="pageTitle"
              v-model="form.title"
              type="text"
              class="form-input"
              :class="{ error: titleError }"
              placeholder="请输入页面标题..."
              @input="validateTitle"
              @blur="validateTitle"
            />
            <div v-if="titleError" class="form-error">{{ titleError }}</div>
            <div v-else-if="form.title" class="form-help">
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
import toast from '@/utils/toast'

const router = useRouter()
const { isAuthenticated, user } = useAuth()

// 权限检查 - 如果用户未登录，重定向到首页
if (!isAuthenticated.value) {
  router.replace('/')
}

// 表单数据
const form = ref({
  title: '',
  openAfterCreate: true
})

// 状态管理
const creating = ref(false)
const titleError = ref('')

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

// 计算属性
const canCreate = computed(() => {
  return form.value.title.trim() && !titleError.value
})

// 验证标题
const validateTitle = () => {
  const title = form.value.title.trim()
  
  if (!title) {
    titleError.value = '页面标题不能为空'
    return
  }
  
  if (title.length < 2) {
    titleError.value = '页面标题至少需要2个字符'
    return
  }
  
  if (title.length > 100) {
    titleError.value = '页面标题不能超过100个字符'
    return
  }
  
  // 检查特殊字符
  const invalidChars = /[<>:"\/\\|?*]/
  if (invalidChars.test(title)) {
    titleError.value = '页面标题不能包含特殊字符 < > : " / \\ | ? *'
    return
  }
  
  titleError.value = ''
}

// 创建页面
const createPage = async () => {
  if (!canCreate.value) return
  
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
    
    // 调用API创建文章
    const response = await articleApi.createArticle(articleData)
    
    toast.success('页面创建成功！', '成功')
    
    // 根据用户选择决定后续操作
    if (form.value.openAfterCreate) {
      // 跳转到编辑页面
      router.push(`/edit/${encodeURIComponent(form.value.title)}`)
    } else {
      // 跳转到文章详情页
      router.push(`/wiki/${encodeURIComponent(form.value.title)}`)
    }
    
  } catch (error: any) {
    console.error('创建页面失败:', error)
    toast.error(error.message || '创建页面失败', '错误')
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
  padding: 12px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s;
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
