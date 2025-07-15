<!-- UserPage.vue -->
<template>
  <div class="user-page-container">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z" />
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">我的主页</h1>
          <p class="page-subtitle">自定义您的个人主页展示</p>
        </div>
      </div>
      <div class="header-actions">
        <button class="preview-btn" @click="previewPage">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M12,4.5C7,4.5 2.73,7.61 1,12C2.73,16.39 7,19.5 12,19.5C17,19.5 21.27,16.39 23,12C21.27,7.61 17,4.5 12,4.5M12,17C9.24,17 7,14.76 7,12C7,9.24 9.24,7 12,7C14.76,7 17,9.24 17,12C17,14.76 14.76,17 12,17M12,9C10.34,9 9,10.34 9,12C9,13.66 10.34,15 12,15C13.66,15 15,13.66 15,12C15,10.34 13.66,9 12,9Z"/>
          </svg>
          预览
        </button>
      </div>
    </div>

    <!-- 状态提示 -->
    <div v-if="pendingReview" class="status-alert">
      <div class="alert-icon">
        <svg viewBox="0 0 24 24" class="icon">
          <path d="M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2M12,7A2,2 0 0,0 10,9C10,10.11 10.9,11 12,11C13.11,11 14,10.11 14,9A2,2 0 0,0 12,7M12,17C9.33,17 7.08,15.42 6.12,13.03C6.08,12.89 6.08,12.74 6.12,12.6C7.08,10.21 9.33,8.63 12,8.63C14.67,8.63 16.92,10.21 17.88,12.6C17.92,12.74 17.92,12.89 17.88,13.03C16.92,15.42 14.67,17 12,17Z"/>
        </svg>
      </div>
      <div class="alert-content">
        <h4>内容审核中</h4>
        <p>您的自定义主页内容正在审核中，审核通过后将自动发布。</p>
      </div>
    </div>
    
    <!-- 编辑器区域 -->
    <div class="editor-container">
      <!-- 编辑器工具栏 -->
      <div class="editor-toolbar">
        <div class="toolbar-left">
          <button 
            class="mode-btn" 
            :class="{ active: mode === 'template' }" 
            @click="switchMode('template')"
          >
            <svg viewBox="0 0 24 24" class="icon">
              <path d="M5,3C3.89,3 3,3.89 3,5V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V5C21,3.89 20.1,3 19,3H5M5,5H19V19H5V5M7,7V9H9V7H7M11,7V9H17V7H11M7,11V13H9V11H7M11,11V13H17V11H11M7,15V17H9V15H7M11,15V17H17V15H11Z"/>
            </svg>
            模板编辑
          </button>
          <button 
            class="mode-btn" 
            :class="{ active: mode === 'custom' }" 
            @click="switchMode('custom')"
          >
            <svg viewBox="0 0 24 24" class="icon">
              <path d="M8,3A2,2 0 0,0 6,5V9A2,2 0 0,1 4,11H3V13H4A2,2 0 0,1 6,15V19A2,2 0 0,0 8,21H10V19H8V14A2,2 0 0,0 6,12A2,2 0 0,0 8,10V5H10V3M16,3A2,2 0 0,1 18,5V9A2,2 0 0,0 20,11H21V13H20A2,2 0 0,0 18,15V19A2,2 0 0,1 16,21H14V19H16V14A2,2 0 0,1 18,12A2,2 0 0,1 16,10V5H14V3H16Z"/>
            </svg>
            自定义代码
          </button>
        </div>
        
        <div class="toolbar-right">
          <button class="save-btn" @click="savePage">
            <svg viewBox="0 0 24 24" class="icon">
              <path d="M15,9H5V5H15M12,19A3,3 0 0,1 9,16A3,3 0 0,1 12,13A3,3 0 0,1 15,16A3,3 0 0,1 12,19M17,3H5C3.89,3 3,3.9 3,5V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V7L17,3Z"/>
            </svg>
            保存
          </button>
        </div>
      </div>
      
      <!-- 模板编辑模式 -->
      <div v-if="mode === 'template'" class="template-editor">
        <div class="editor-split">
          <!-- 左侧：编辑区域 -->
          <div class="edit-panel">
            <div class="panel-header">
              <h3>编辑内容</h3>
            </div>
            
            <div class="form-section">
              <label class="form-label">个人简介</label>
              <textarea 
                v-model="templateData.bio" 
                class="form-textarea" 
                placeholder="介绍一下您自己..."
                rows="4"
              ></textarea>
            </div>
            
            <div class="form-section">
              <label class="form-label">专业领域</label>
              <div class="tag-input">
                <div v-for="(tag, index) in templateData.tags" :key="index" class="tag-item">
                  <span>{{ tag }}</span>
                  <button @click="removeTag(index)" class="tag-remove">
                    <svg viewBox="0 0 24 24" class="icon">
                      <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z"/>
                    </svg>
                  </button>
                </div>
                <input 
                  v-model="newTag" 
                  @keyup.enter="addTag"
                  placeholder="添加标签..."
                  class="tag-input-field"
                />
              </div>
            </div>
            
            <div class="form-section">
              <label class="form-label">社交链接</label>
              <div class="social-links">
                <div v-for="(link, index) in templateData.socialLinks" :key="index" class="social-item">
                  <select v-model="link.platform" class="social-select">
                    <option value="github">GitHub</option>
                    <option value="twitter">Twitter</option>
                    <option value="linkedin">LinkedIn</option>
                    <option value="website">个人网站</option>
                  </select>
                  <input v-model="link.url" placeholder="链接地址" class="social-input"/>
                  <button @click="removeSocialLink(index)" class="social-remove">
                    <svg viewBox="0 0 24 24" class="icon">
                      <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z"/>
                    </svg>
                  </button>
                </div>
                <button @click="addSocialLink" class="add-social-btn">
                  <svg viewBox="0 0 24 24" class="icon">
                    <path d="M19,13H13V19H11V13H5V11H11V5H13V11H19V13Z"/>
                  </svg>
                  添加社交链接
                </button>
              </div>
            </div>
          </div>
          
          <!-- 右侧：预览区域 -->
          <div class="preview-panel">
            <div class="panel-header">
              <h3>实时预览</h3>
            </div>
            
            <div class="template-preview">
              <div class="user-header">
                <div class="user-avatar-large">
                  <img :src="user?.avatar || '/default-avatar.png'" alt="用户头像" />
                </div>
                <h1 class="user-name">{{ user?.username || '用户名' }}</h1>
                <div class="user-tags">
                  <span v-for="tag in templateData.tags" :key="tag" class="tag">{{ tag }}</span>
                </div>
              </div>
              
              <div class="user-bio">
                <p>{{ templateData.bio || '这里是您的个人简介...' }}</p>
              </div>
              
              <div class="quick-stats">
                <div class="stat-item">
                  <span class="stat-number">{{ userStats.created }}</span>
                  <span class="stat-label">创建页面</span>
                </div>
                <div class="stat-item">
                  <span class="stat-number">{{ userStats.edited }}</span>
                  <span class="stat-label">编辑次数</span>
                </div>
                <div class="stat-item">
                  <span class="stat-number">{{ userStats.points }}</span>
                  <span class="stat-label">积分</span>
                </div>
              </div>
              
              <div v-if="templateData.socialLinks.length" class="social-links-preview">
                <h4>社交链接</h4>
                <div class="social-buttons">
                  <a 
                    v-for="link in templateData.socialLinks" 
                    :key="link.platform"
                    :href="link.url"
                    target="_blank"
                    class="social-btn"
                    :class="link.platform"
                  >
                    <svg viewBox="0 0 24 24" class="icon">
                      <path :d="getSocialIcon(link.platform)"/>
                    </svg>
                    {{ getSocialLabel(link.platform) }}
                  </a>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 自定义代码模式 -->
      <div v-else class="custom-editor">
        <div class="code-editor-container">
          <div class="editor-tabs">
            <button 
              v-for="tab in codeTabs" 
              :key="tab.id"
              class="editor-tab"
              :class="{ active: activeCodeTab === tab.id }"
              @click="activeCodeTab = tab.id"
            >
              <svg viewBox="0 0 24 24" class="icon">
                <path :d="tab.icon"/>
              </svg>
              {{ tab.label }}
            </button>
          </div>
          
          <div class="code-editor">
            <textarea 
              v-model="customCode[activeCodeTab as keyof typeof customCode]"
              class="code-textarea" 
              :placeholder="getCodePlaceholder(activeCodeTab)"
            ></textarea>
          </div>
          
          <div class="code-actions">
            <div class="help-text">
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M11,9H13V7H11M12,20C7.59,20 4,16.41 4,12C4,7.59 7.59,4 12,4C16.41,4 20,7.59 20,12C20,16.41 16.41,20 12,20M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2M11,17H13V11H11V17Z"/>
              </svg>
              支持HTML、CSS和JavaScript。请遵守社区规范，禁止恶意代码。
            </div>
            
            <button class="submit-review-btn" @click="submitForReview">
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M9,20.42L2.79,14.21L5.62,11.38L9,14.77L18.88,4.88L21.71,7.71L9,20.42Z"/>
              </svg>
              提交审核
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useAuth } from '@/composables/useAuth'

const { user } = useAuth()

// 模式控制
const mode = ref<'template' | 'custom'>('template')
const pendingReview = ref(false)

// 模板数据
const templateData = reactive({
  bio: '欢迎来到我的主页！我是一名热衷于环保知识的编辑者，致力于分享可持续发展的相关知识。',
  tags: ['环保', '可持续发展', '气候变化'],
  socialLinks: [
    { platform: 'github', url: 'https://github.com/username' },
    { platform: 'website', url: 'https://mywebsite.com' }
  ]
})

// 用户统计数据
const userStats = ref({
  created: 42,
  edited: 156,
  points: 3245
})

// 新标签输入
const newTag = ref('')

// 代码编辑器
const activeCodeTab = ref('html')
const codeTabs = [
  { id: 'html', label: 'HTML', icon: 'M12,17.56L16.07,16.43L16.62,10.33H9.38L9.2,8.3H16.8L17,6.31H7L7.56,12.32H14.45L14.22,14.9L12,15.5L9.78,14.9L9.64,13.24H7.64L7.93,16.43L12,17.56M4.07,3H19.93L18.5,19.2L12,21L5.5,19.2L4.07,3Z' },
  { id: 'css', label: 'CSS', icon: 'M5,3L4.35,6.34H17.94L17.5,8.5H3.92L3.26,11.83H16.85L16.09,15.64L10.61,17.45L5.86,15.64L6.19,14H2.85L2.06,18L9.91,21L18.96,18L20.16,11.97L20.4,10.76L21.94,3H5Z' },
  { id: 'js', label: 'JavaScript', icon: 'M3,3H21V21H3V3M7.73,18.04C8.13,18.89 8.92,19.59 10.27,19.59C11.77,19.59 12.8,18.79 12.8,17.04V11.26H11.1V17C11.1,17.86 10.75,18.08 10.2,18.08C9.62,18.08 9.38,17.68 9.11,17.21L7.73,18.04M13.71,17.86C14.21,18.84 15.22,19.59 16.8,19.59C18.4,19.59 19.6,18.76 19.6,17.23C19.6,15.82 18.79,15.19 17.35,14.57L16.93,14.39C16.2,14.08 15.89,13.87 15.89,13.37C15.89,12.96 16.2,12.64 16.7,12.64C17.18,12.64 17.5,12.85 17.79,13.37L19.1,12.5C18.55,11.54 17.77,11.17 16.7,11.17C15.19,11.17 14.22,12.13 14.22,13.4C14.22,14.78 15.03,15.43 16.25,15.95L16.67,16.13C17.45,16.47 17.91,16.68 17.91,17.26C17.91,17.74 17.46,18.09 16.76,18.09C15.93,18.09 15.45,17.66 15.09,17.06L13.71,17.86Z' }
]

const customCode = reactive({
  html: '<div class="custom-page">\n  <h1>我的自定义主页</h1>\n  <p>在这里添加您的内容...</p>\n</div>',
  css: '.custom-page {\n  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n  padding: 40px;\n  border-radius: 10px;\n  color: white;\n}\n\n.custom-page h1 {\n  margin-bottom: 20px;\n}',
  js: '// 在这里添加您的JavaScript代码\nconsole.log("欢迎来到我的主页！");'
})

// 方法
const switchMode = (newMode: 'template' | 'custom') => {
  mode.value = newMode
}

const previewPage = () => {
  console.log('预览页面')
  // TODO: 实现预览功能
}

const savePage = () => {
  console.log('保存页面')
  // TODO: 实现保存功能
}

const addTag = () => {
  if (newTag.value.trim() && !templateData.tags.includes(newTag.value.trim())) {
    templateData.tags.push(newTag.value.trim())
    newTag.value = ''
  }
}

const removeTag = (index: number) => {
  templateData.tags.splice(index, 1)
}

const addSocialLink = () => {
  templateData.socialLinks.push({ platform: 'website', url: '' })
}

const removeSocialLink = (index: number) => {
  templateData.socialLinks.splice(index, 1)
}

const submitForReview = () => {
  console.log('提交审核')
  pendingReview.value = true
  // TODO: 实现提交审核功能
}

const getSocialIcon = (platform: string) => {
  const icons = {
    github: 'M12,2A10,10 0 0,0 2,12C2,16.42 4.87,20.17 8.84,21.5C9.34,21.58 9.5,21.27 9.5,21C9.5,20.77 9.5,20.14 9.5,19.31C6.73,19.91 6.14,17.97 6.14,17.97C5.68,16.81 5.03,16.5 5.03,16.5C4.12,15.88 5.1,15.9 5.1,15.9C6.1,15.97 6.63,16.93 6.63,16.93C7.5,18.45 8.97,18 9.54,17.76C9.63,17.11 9.89,16.67 10.17,16.42C7.95,16.17 5.62,15.31 5.62,11.5C5.62,10.39 6,9.5 6.65,8.79C6.55,8.54 6.2,7.5 6.75,6.15C6.75,6.15 7.59,5.88 9.5,7.17C10.29,6.95 11.15,6.84 12,6.84C12.85,6.84 13.71,6.95 14.5,7.17C16.41,5.88 17.25,6.15 17.25,6.15C17.8,7.5 17.45,8.54 17.35,8.79C18,9.5 18.38,10.39 18.38,11.5C18.38,15.32 16.04,16.16 13.81,16.41C14.17,16.72 14.5,17.33 14.5,18.26C14.5,19.6 14.5,20.68 14.5,21C14.5,21.27 14.66,21.59 15.17,21.5C19.14,20.16 22,16.42 22,12A10,10 0 0,0 12,2Z',
    twitter: 'M22.46,6C21.69,6.35 20.86,6.58 20,6.69C20.88,6.16 21.56,5.32 21.88,4.31C21.05,4.81 20.13,5.16 19.16,5.36C18.37,4.5 17.26,4 16,4C13.65,4 11.73,5.92 11.73,8.29C11.73,8.63 11.77,8.96 11.84,9.27C8.28,9.09 5.11,7.38 3,4.79C2.63,5.42 2.42,6.16 2.42,6.94C2.42,8.43 3.17,9.75 4.33,10.5C3.62,10.5 2.96,10.3 2.38,10C2.38,10 2.38,10 2.38,10.03C2.38,12.11 3.86,13.85 5.82,14.24C5.46,14.34 5.08,14.39 4.69,14.39C4.42,14.39 4.15,14.36 3.89,14.31C4.43,16 6,17.26 7.89,17.29C6.43,18.45 4.58,19.13 2.56,19.13C2.22,19.13 1.88,19.11 1.54,19.07C3.44,20.29 5.7,21 8.12,21C16,21 20.33,14.46 20.33,8.79C20.33,8.6 20.33,8.42 20.32,8.23C21.16,7.63 21.88,6.87 22.46,6Z',
    linkedin: 'M19,3A2,2 0 0,1 21,5V19A2,2 0 0,1 19,21H5A2,2 0 0,1 3,19V5A2,2 0 0,1 5,3H19M18.5,18.5V13.2A3.26,3.26 0 0,0 15.24,9.94C14.39,9.94 13.4,10.46 12.92,11.24V10.13H10.13V18.5H12.92V13.57C12.92,12.8 13.54,12.17 14.31,12.17A1.4,1.4 0 0,1 15.71,13.57V18.5H18.5M6.88,8.56A1.68,1.68 0 0,0 8.56,6.88C8.56,5.95 7.81,5.19 6.88,5.19A1.69,1.69 0 0,0 5.19,6.88C5.19,7.81 5.95,8.56 6.88,8.56M8.27,18.5V10.13H5.5V18.5H8.27Z',
    website: 'M16.36,14C16.44,13.3 16.5,12.66 16.5,12C16.5,11.34 16.44,10.7 16.36,10H19.74C19.9,10.64 20,11.31 20,12C20,12.69 19.9,13.36 19.74,14M14.59,19.56C15.19,18.45 15.65,17.25 15.97,16H18.92C17.96,17.65 16.43,18.93 14.59,19.56M14.34,14H9.66C9.56,13.34 9.5,12.68 9.5,12C9.5,11.32 9.56,10.66 9.66,10H14.34C14.43,10.66 14.5,11.32 14.5,12C14.5,12.68 14.43,13.34 14.34,14M12,19.96C11.17,18.76 10.5,17.43 10.09,16H13.91C13.5,17.43 12.83,18.76 12,19.96M8,8H5.08C6.03,6.34 7.57,5.06 9.4,4.44C8.8,5.55 8.35,6.75 8,8M5.08,16H8C8.35,17.25 8.8,18.45 9.4,19.56C7.57,18.93 6.03,17.65 5.08,16M4.26,14C4.1,13.36 4,12.69 4,12C4,11.31 4.1,10.64 4.26,10H7.64C7.56,10.7 7.5,11.34 7.5,12C7.5,12.66 7.56,13.3 7.64,14M12,4.03C12.83,5.23 13.5,6.57 13.91,8H10.09C10.5,6.57 11.17,5.23 12,4.03M18.92,8H15.97C15.65,6.75 15.19,5.55 14.59,4.44C16.43,5.07 17.96,6.34 18.92,8M12,2C6.47,2 2,6.5 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2Z'
  }
  return icons[platform as keyof typeof icons] || icons.website
}

const getSocialLabel = (platform: string) => {
  const labels = {
    github: 'GitHub',
    twitter: 'Twitter',
    linkedin: 'LinkedIn',
    website: '个人网站'
  }
  return labels[platform as keyof typeof labels] || '链接'
}

const getCodePlaceholder = (tab: string) => {
  const placeholders = {
    html: '在这里输入您的HTML代码...',
    css: '在这里输入您的CSS样式...',
    js: '在这里输入您的JavaScript代码...'
  }
  return placeholders[tab as keyof typeof placeholders] || ''
}
</script>

<style scoped>
.user-page-container {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 24px;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 32px;
  margin-bottom: 32px;
  color: white;
  box-shadow: 0 20px 40px rgba(102, 126, 234, 0.3);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 24px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.header-icon .icon {
  width: 24px;
  height: 24px;
  fill: currentColor;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 8px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-subtitle {
  font-size: 1.1rem;
  opacity: 0.9;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.preview-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.preview-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.preview-btn .icon {
  width: 18px;
  height: 18px;
  fill: currentColor;
}

.status-alert {
  background: linear-gradient(135deg, #ffeaa7 0%, #fab1a0 100%);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 8px 32px rgba(255, 234, 167, 0.3);
}

.alert-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.alert-icon .icon {
  width: 24px;
  height: 24px;
  fill: #2d3436;
}

.alert-content h4 {
  margin: 0 0 8px 0;
  font-size: 1.2rem;
  font-weight: 600;
  color: #2d3436;
}

.alert-content p {
  margin: 0;
  color: #636e72;
}

.editor-container {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.editor-toolbar {
  background: #f8fafc;
  padding: 20px 32px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.toolbar-left {
  display: flex;
  gap: 8px;
}

.mode-btn {
  padding: 12px 24px;
  border: 2px solid #e2e8f0;
  background: white;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}

.mode-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
}

.mode-btn:not(.active):hover {
  background: #f8f9ff;
  border-color: #cbd5e0;
}

.mode-btn .icon {
  width: 18px;
  height: 18px;
  fill: currentColor;
}

.toolbar-stats {
  display: flex;
  gap: 24px;
  align-items: center;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: 700;
  color: #667eea;
  display: block;
}

.stat-label {
  font-size: 0.875rem;
  color: #718096;
}

.template-editor {
  padding: 32px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  min-height: 600px;
}

.editor-panel {
  display: flex;
  flex-direction: column;
}

.panel-header {
  background: #f7fafc;
  padding: 16px 24px;
  border-radius: 12px 12px 0 0;
  border-bottom: 1px solid #e2e8f0;
  font-weight: 600;
  color: #2d3748;
}

.panel-content {
  flex: 1;
  padding: 24px;
  background: #f8fafc;
  border-radius: 0 0 12px 12px;
}

.form-section {
  margin-bottom: 32px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #4a5568;
  font-size: 1rem;
}

.form-textarea {
  width: 100%;
  padding: 16px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
  resize: vertical;
  min-height: 120px;
  font-family: inherit;
}

.form-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.tags-input {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  min-height: 48px;
  padding: 12px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  background: white;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
}

.tag-remove {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 0;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.8;
  transition: opacity 0.2s ease;
}

.tag-remove:hover {
  opacity: 1;
  background: rgba(255, 255, 255, 0.2);
}

.tag-remove .icon {
  width: 12px;
  height: 12px;
  fill: currentColor;
}

.tag-input-field {
  flex: 1;
  border: none;
  outline: none;
  padding: 8px;
  font-size: 0.9rem;
  min-width: 120px;
}

.social-links {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.social-item {
  display: grid;
  grid-template-columns: 120px 1fr auto;
  gap: 12px;
  align-items: center;
  padding: 16px;
  background: white;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  transition: border-color 0.3s ease;
}

.social-item:hover {
  border-color: #cbd5e0;
}

.social-select {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  font-size: 0.9rem;
  cursor: pointer;
}

.social-input {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.9rem;
  transition: border-color 0.3s ease;
}

.social-input:focus {
  outline: none;
  border-color: #667eea;
}

.social-remove {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 8px;
  background: #fed7d7;
  color: #e53e3e;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.3s ease;
}

.social-remove:hover {
  background: #feb2b2;
}

.social-remove .icon {
  width: 16px;
  height: 16px;
  fill: currentColor;
}

.add-social-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #c6f6d5;
  color: #38a169;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.3s ease;
}

.add-social-btn:hover {
  background: #9ae6b4;
}

.add-social-btn .icon {
  width: 18px;
  height: 18px;
  fill: currentColor;
}

.preview-panel {
  background: white;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  overflow: hidden;
}

.template-preview {
  padding: 24px;
}

.user-header {
  text-align: center;
  margin-bottom: 24px;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.user-name {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 8px;
  color: #2d3748;
}

.user-bio {
  color: #718096;
  line-height: 1.6;
}

.custom-editor {
  padding: 32px;
}

.code-editor-container {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.editor-tabs {
  display: flex;
  background: #f7fafc;
  border-bottom: 1px solid #e2e8f0;
}

.editor-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 0.9rem;
  color: #718096;
  transition: all 0.3s ease;
  border-bottom: 3px solid transparent;
}

.editor-tab.active {
  color: #667eea;
  border-bottom-color: #667eea;
  background: white;
}

.editor-tab:hover:not(.active) {
  color: #4a5568;
  background: #edf2f7;
}

.editor-tab .icon {
  width: 16px;
  height: 16px;
  fill: currentColor;
}

.code-editor {
  position: relative;
}

.code-textarea {
  width: 100%;
  height: 400px;
  padding: 24px;
  border: none;
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
  line-height: 1.6;
  resize: none;
  background: #1a202c;
  color: #e2e8f0;
  tab-size: 2;
}

.code-textarea:focus {
  outline: none;
}

.code-actions {
  padding: 20px 24px;
  background: #f7fafc;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.help-text {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #718096;
  font-size: 0.875rem;
}

.help-text .icon {
  width: 16px;
  height: 16px;
  fill: currentColor;
}

.submit-review-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(72, 187, 120, 0.3);
}

.submit-review-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(72, 187, 120, 0.4);
}

.submit-review-btn .icon {
  width: 18px;
  height: 18px;
  fill: currentColor;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .template-editor {
    grid-template-columns: 1fr;
    gap: 24px;
  }
}

@media (max-width: 768px) {
  .user-page-container {
    padding: 16px;
  }
  
  .page-header {
    padding: 24px;
    flex-direction: column;
    text-align: center;
  }
  
  .page-title {
    font-size: 2rem;
  }
  
  .editor-toolbar {
    flex-direction: column;
    gap: 12px;
  }
  
  .toolbar-left {
    width: 100%;
    justify-content: center;
  }
  
  .template-editor {
    padding: 24px;
  }
  
  .social-item {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  
  .code-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .help-text {
    text-align: center;
  }
}

@media (max-width: 480px) {
  .header-content {
    flex-direction: column;
    text-align: center;
  }
  
  .toolbar-stats {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .editor-tabs {
    flex-wrap: wrap;
  }
  
  .editor-tab {
    flex: 1;
    min-width: 80px;
  }
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.editor-container {
  animation: fadeInUp 0.6s ease-out;
}

.template-preview {
  animation: fadeInUp 0.6s ease-out;
  animation-delay: 0.2s;
  animation-fill-mode: both;
}

/* 滚动条样式 */
.code-textarea::-webkit-scrollbar {
  width: 8px;
}

.code-textarea::-webkit-scrollbar-track {
  background: #2d3748;
}

.code-textarea::-webkit-scrollbar-thumb {
  background: #4a5568;
  border-radius: 4px;
}

.code-textarea::-webkit-scrollbar-thumb:hover {
  background: #718096;
}
</style>