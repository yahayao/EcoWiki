<!--
  文章编辑页面组件
  
  这是EcoWiki的文章编辑功能页面，支持创建新文章和编辑现有文章。
  现已重构为组件化架构，提升代码可维护性和复用性。
  
  主要功能：
  - 双模式支持：新建文章模式和编辑现有文章模式
  - 富文本编辑：集成Wiki语法编辑器，支持实时预览
  - 自动保存：定时保存草稿，防止意外丢失
  - 表单验证：完整的输入验证和错误提示
  - 分类管理：支持文章分类和标签设置
  - 作者信息：自动设置作者信息，支持手动修改
  
  组件架构：
  - EditHeader: 页面头部组件，显示标题和导航
  - EditNotice: 编辑提示组件，显示使用说明
  - EditorToolbar: 工具栏组件，提供格式化按钮
  - EditorContent: 编辑器内容组件，包含编辑器和预览
  - EditSummary: 编辑摘要和保存组件
  - LoadingState: 加载状态组件
  
  @author EcoWiki Team
  @version 3.0.0 - 组件化重构版本
  @since 2024-01-01
-->
<template>
  <div class="article-edit-page">
    <div class="container">
      <!-- 加载状态 -->
      <LoadingState 
        v-if="loading" 
        message="正在加载文章数据..." 
      />

      <!-- 编辑界面 -->
      <template v-else>
        <!-- 页面标题栏 -->
        <EditHeader
          :is-edit-mode="isEditMode"
          :current-title="currentTitle"
          @go-back="goBack"
        />

        <!-- 编辑提示 -->
        <EditNotice />

        <!-- 预览区域 -->
        <EditPreview
          :show-preview="showPreview"
          :title="currentTitle"
          :content="articleForm.content"
          :category="articleForm.category"
          :tags="articleForm.tags"
          :author="articleForm.author"
          @close="togglePreview"
        />

        <!-- 编辑工具栏和内容区域 -->
        <div class="editor-container">
          <!-- 编辑工具栏 -->
          <EditorToolbar
            :show-preview="showPreview"
            @toggle-preview="togglePreview"
            @insert-text="insertText"
            @insert-heading="insertHeading"
            @insert-table="insertTable"
          />

          <!-- 编辑区域 -->
          <EditorContent
            ref="editorContentRef"
            v-model="articleForm.content"
            @keydown="handleKeydown"
          />
        </div>

        <!-- 编辑摘要和保存区域 -->
        <EditSummary
          v-model:edit-summary="editSummary"
          v-model:category="articleForm.category"
          :display-tags="displayTags"
          :saving="saving"
          :can-save="canSave"
          :is-edit-mode="isEditMode"
          :show-preview="showPreview"
          @save="handleSave"
          @toggle-preview="togglePreview"
          @cancel="goBack"
        />
      </template>
    </div>

    <!-- 预览模态框 -->
    <div v-if="previewModalVisible" class="preview-modal" @click="closePreviewModal">
      <div class="preview-modal-content" @click.stop>
        <div class="preview-header">
          <h3>文章预览</h3>
          <button @click="closePreviewModal" class="close-btn">✕</button>
        </div>
        <div class="preview-body">
          <div class="preview-meta">
            <h1>{{ currentTitle || '未命名文章' }}</h1>
            <div class="meta-info">
              <span>分类：{{ articleForm.category || '未分类' }}</span>
              <span v-if="articleForm.tags">标签：{{ articleForm.tags }}</span>
            </div>
          </div>
          <div class="preview-content" v-html="wikiParser.parseToHtml(articleForm.content)"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 文章编辑页面组件脚本
 * 
 * 实现文章编辑页面的所有逻辑功能，包括数据管理、编辑操作、表单验证等。
 * 使用Vue 3 Composition API进行状态管理和生命周期控制。
 * 采用组件化架构，提升代码可维护性和复用性。
 */

// Vue核心功能导入
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'

// 组件导入
import EditHeader from '../components/edit/EditHeader.vue'
import EditNotice from '../components/edit/EditNotice.vue'
import EditorToolbar from '../components/edit/EditorToolbar.vue'
import EditorContent from '../components/edit/EditorContent.vue'
import EditSummary from '../components/edit/EditSummary.vue'
import EditPreview from '../components/edit/EditPreview.vue'
import LoadingState from '../components/common/LoadingState.vue'

// API和工具导入
import { articleApi, type Article, type ArticleCreateRequest, type ArticleUpdateRequest } from '../api/article'
import { draftApi, type DraftSubmissionResult } from '../api/draft'
import { wikiParser } from '../utils/wikiParser'
import toast from '../utils/toast'
import { useAuth } from '../composables/useAuth'
import { useEditorOperations } from '../composables/useEditorOperations'

// ======================== 路由和认证 ========================

/**
 * 路由实例，用于获取当前路由参数和导航
 */
const route = useRoute()

/**
 * 路由器实例，用于编程式导航
 */
const router = useRouter()

/**
 * 认证状态，获取用户信息和登录状态
 */
const { userDisplayName, isAuthenticated, user, isSuperAdmin } = useAuth()

// ======================== 响应式状态管理 ========================

/**
 * 页面加载状态
 * 控制初始数据加载时的loading显示
 */
const loading = ref(true)

/**
 * 保存状态
 * 控制保存操作时的loading状态和按钮禁用
 */
const saving = ref(false)

/**
 * 预览模式开关
 * 控制是否显示预览区域
 */
const showPreview = ref(false)

/**
 * 预览模态框显示状态
 * 控制全屏预览模态框的显示
 */
const previewModalVisible = ref(false)

/**
 * 编辑摘要
 * 用户输入的本次编辑摘要，记录变更原因
 */
const editSummary = ref('')

/**
 * 文章存在状态
 * 判断当前是编辑现有文章还是创建新文章
 */
const articleExists = ref(true)

/**
 * 编辑器引用，用于获取编辑器DOM元素
 */
const editorContentRef = ref<{ editorTextarea: HTMLTextAreaElement | null } | null>(null)

/**
 * 获取编辑器文本区域
 */
const getEditorTextarea = (): HTMLTextAreaElement | null => {
  return editorContentRef.value?.editorTextarea || null
}

// ======================== 编辑器操作 ========================

/**
 * 使用编辑器操作功能
 */
const editorOperations = useEditorOperations(
  computed({
    get: () => articleForm.value.content,
    set: (value: string) => {
      articleForm.value.content = value
    }
  }),
  getEditorTextarea,
  () => handleSave()
)

const { insertText, insertHeading, insertTable, handleKeydown } = editorOperations

/**
 * 帮助面板显示状态
 * 控制Wiki语法帮助面板的显示
 */
const showHelpPanel = ref(false)

/**
 * 来源页面路径
 * 记录用户进入编辑页面前的路径，用于返回导航
 */
const referrerPath = ref('/')

/**
 * 保存成功标记
 * 标记最近一次保存操作是否成功，用于离开页面提醒
 */
const saveSuccessful = ref(false)

// ======================== 表单数据管理 ========================

/**
 * 文章表单数据
 * 包含文章的所有可编辑字段
 */
const articleForm = ref({
  title: '',        // 文章标题
  content: '',      // 文章内容（Wiki语法）
  category: '',     // 文章分类
  tags: '',         // 文章标签（逗号分隔）
  author: ''        // 文章作者
})

/**
 * 原始文章数据
 * 存储从服务器加载的原始文章数据，用于对比变更
 */
const originalArticle = ref<Article | null>(null)

// ======================== 计算属性 ========================

/**
 * 是否为编辑模式
 * 
 * 根据文章存在状态和原始数据判断当前是编辑现有文章还是创建新文章。
 * 
 * @returns {boolean} true表示编辑现有文章，false表示创建新文章
 */
const isEditMode = computed(() => {
  return articleExists.value && originalArticle.value !== null
})

/**
 * 当前文章标题
 * 
 * 从路由参数获取文章标题，用于页面显示
 * 
 * @returns {string} 文章标题或"新文章"
 */
const currentTitle = computed(() => {
  const titleParam = route.params.title as string
  return titleParam && titleParam !== 'new' ? titleParam : '新文章'
})

/**
 * 是否可以保存
 * 
 * 检查保存条件：标题、内容非空且用户已登录。
 * 
 * @returns {boolean} true表示可以保存，false表示不满足保存条件
 */
const canSave = computed((): boolean => {
  const title = currentTitle.value
  return Boolean(title && title !== '新文章' &&
         articleForm.value.content.trim() && 
         isAuthenticated.value)
})

/**
 * 显示用的标签数组
 * 
 * 将逗号分隔的标签字符串转换为数组，用于UI显示。
 * 自动过滤空字符串和去除首尾空格。
 * 
 * @returns {string[]} 标签名称数组
 */
const displayTags = computed(() => {
  if (!articleForm.value.tags) return []
  return articleForm.value.tags
    .split(',')
    .map(tag => tag.trim())
    .filter(tag => tag.length > 0)
})

// ======================== 核心方法 ========================

/**
 * 加载文章数据
 * 
 * 根据路由参数加载现有文章数据，或初始化新文章创建模式。
 */
const loadArticle = async () => {
  const articleTitle = route.params.title as string
  
  // 验证文章标题的有效性
  if (!articleTitle || articleTitle === 'new') {
    articleExists.value = false
    // 在创建模式下，设置当前登录用户为作者，不设置标题
    articleForm.value = {
      title: '', // 不设置标题，因为标题来自路由参数
      content: '',
      category: '',
      tags: '',
      author: user.value?.username || userDisplayName.value || '未知用户'
    }
    loading.value = false
    return
  }

  // 检查用户页面编辑权限
  if (articleTitle.startsWith('User:')) {
    // 超级管理员可以编辑任何用户页面
    if (!isSuperAdmin.value) {
      const currentUsername = user.value?.username || userDisplayName.value
      const expectedUserPage = `User:${currentUsername}`
      
      if (articleTitle !== expectedUserPage) {
        toast.warning('您只能编辑自己的用户页面')
        loading.value = false
        router.push(`/wiki/${articleTitle}`)
        return
      }
    }
  }

  try {
    // 先通过标题获取文章ID
    const articleId = await articleApi.getArticleIdByTitle(articleTitle)
    
    // 再通过ID获取文章详情
    const article = await articleApi.getArticleById(articleId)
    
    // 文章存在，进入编辑模式
    originalArticle.value = article
    articleExists.value = true
    
    articleForm.value = {
      title: '', // 不设置标题，因为标题来自路由参数
      content: article.content || '',
      category: article.category || '',
      tags: article.tags || '',
      author: article.author
    }
  } catch (error) {
    console.error('文章不存在，进入创建模式:', error)
    // 文章不存在，进入创建模式
    articleExists.value = false
    originalArticle.value = null
    
    // 设置默认值，作者使用当前登录用户
    // 标题从路由参数获取，不在这里设置
    articleForm.value = {
      title: '', // 不设置标题，因为标题来自路由参数
      content: '',
      category: '',
      tags: '',
      author: user.value?.username || userDisplayName.value || '未知用户'
    }
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!canSave.value) {
    toast.warning('请填写所有必填字段')
    return
  }

  // 检查用户页面编辑权限
  const articleTitle = currentTitle.value
  if (articleTitle.startsWith('User:')) {
    // 超级管理员可以编辑任何用户页面
    if (!isSuperAdmin.value) {
      const currentUsername = user.value?.username || userDisplayName.value
      const expectedUserPage = `User:${currentUsername}`
      
      if (articleTitle !== expectedUserPage) {
        toast.warning('您只能编辑自己的用户页面')
        return
      }
    }
  }

  // 在保存前提取分类作为标签
  wikiParser.clearExtractedCategories()
  wikiParser.parseToHtml(articleForm.value.content)
  const extractedCategories = wikiParser.getExtractedCategories()
  
  // 直接使用提取的分类作为标签
  articleForm.value.tags = extractedCategories.join(', ')

  try {
    saving.value = true
    saveSuccessful.value = false

    if (isEditMode.value) {
      // 提交文章编辑到草稿表等待审核
      const updateData = {
        title: currentTitle.value,
        content: articleForm.value.content.trim(),
        category: articleForm.value.category.trim(),
        tags: articleForm.value.tags.trim()
      }
      
      const result: DraftSubmissionResult = await draftApi.submitArticleEdit(originalArticle.value!.articleId, updateData)
      
      saveSuccessful.value = true
      
      toast.success('文章修改已提交审核，请耐心等待管理员审核！')
      
      // 跳转回文章详情页，并显示审核状态提示
      await router.push(`/article/${encodeURIComponent(currentTitle.value)}`)
      return
      
      // 使用setTimeout确保状态更新后再导航
      setTimeout(() => {
        router.push(`/wiki/${updated.title}`)
      }, 100)
    } else {
      // 创建文章 - 提交到草稿表等待审核
      const currentAuthor = user.value?.username || userDisplayName.value || '未知用户'
      const createData: ArticleCreateRequest = {
        title: currentTitle.value,
        content: articleForm.value.content.trim(),
        category: articleForm.value.category.trim(),
        tags: articleForm.value.tags.trim(),
        author: currentAuthor
      }
      
      const draft = await draftApi.submitNewArticle(createData)
      
      saveSuccessful.value = true
      
      toast.success('新文章已提交审核，请等待管理员审核！')
      
      // 导航到首页或个人页面
      setTimeout(() => {
        router.push('/')
      }, 100)
    }
  } catch (error) {
    console.error('保存失败:', error)
    toast.warning('保存失败，请重试')
    saveSuccessful.value = false
  } finally {
    saving.value = false
  }
}

const togglePreview = () => {
  showPreview.value = !showPreview.value
}

const closePreviewModal = () => {
  previewModalVisible.value = false
}

const goBack = () => {
  if (isEditMode.value && originalArticle.value) {
    router.push(`/wiki/${originalArticle.value.title}`)
  } else {
    router.push('/')
  }
}

// 检查是否有未保存的更改
const hasUnsavedChanges = computed(() => {
  if (!isEditMode.value) {
    // 在新建模式下，只要有内容就算有更改
    return articleForm.value.content.trim().length > 0
  }
  
  if (!originalArticle.value) return false
  
  return (
    articleForm.value.content !== (originalArticle.value.content || '') ||
    articleForm.value.category !== (originalArticle.value.category || '') ||
    articleForm.value.tags !== (originalArticle.value.tags || '')
  )
})

// 路由守卫
onBeforeRouteLeave((to, from, next) => {
  // 如果保存成功或者正在保存，直接允许离开
  if (saveSuccessful.value || saving.value) {
    next()
    return
  }
  
  if (hasUnsavedChanges.value) {
    const answer = window.confirm('您有未保存的更改，确定要离开此页面吗？')
    if (answer) {
      next()
    } else {
      next(false)
    }
  } else {
    next()
  }
})

// 生命周期
onMounted(() => {
  // 检查用户是否已登录
  if (!isAuthenticated.value) {
    toast.warning('请先登录后再创建或编辑文章')
    router.push('/')
    return
  }
  
  loadArticle()
  // 记录来源页面
  try {
    if (document.referrer) {
      const referrerUrl = new URL(document.referrer)
      const currentOrigin = window.location.origin
      // 只有当来源页面是同站点时才使用
      if (referrerUrl.origin === currentOrigin) {
        referrerPath.value = referrerUrl.pathname
      }
    }
  } catch (error) {
    console.warn('Failed to parse referrer:', error)
  }
  
  // 添加浏览器 beforeunload 事件监听器
  window.addEventListener('beforeunload', handleBeforeUnload)
})

// 监听内容变化（预览现在是独立的，不需要自动更新）
watch(() => articleForm.value.content, () => {
  // 内容变化时可以在这里处理其他逻辑，比如自动保存
})

onUnmounted(() => {
  // 组件卸载时移除事件监听器
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

// 处理浏览器 beforeunload 事件（刷新页面、关闭标签页等）
const handleBeforeUnload = (event: BeforeUnloadEvent) => {
  // 如果保存成功或者正在保存，不阻止页面离开
  if (saveSuccessful.value || saving.value) {
    return
  }
  
  // 检查是否有未保存的更改
  if (hasUnsavedChanges.value) {
    // 设置返回值以触发浏览器的确认对话框
    event.preventDefault()
    // 对于现代浏览器，返回值会被忽略，但设置它仍然是个好习惯
    event.returnValue = '您所做的更改可能未保存。'
    return '您所做的更改可能未保存。'
  }
}

const loadArticleForEdit = async () => {
  // 这个函数已被 loadArticle() 替代，移除冗余代码
}

const handleInput = () => {
  // 处理输入事件，现在预览是独立的，不需要特殊处理
}

const insertTemplate = () => {
    insertText('{{', '}}', '模板名称')
}
</script>

<style scoped>
.article-edit-page {
    min-height: 100vh;
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    padding: 20px 0;
}

.container {
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
}

.editor-container {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  margin-bottom: 2rem;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* 预览模态框样式 */
.preview-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  backdrop-filter: blur(4px);
}

.preview-modal-content {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 16px;
  width: 100%;
  max-width: 900px;
  max-height: 85vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.preview-modal .preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.preview-modal .preview-header h3 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: white;
}

.close-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  font-size: 1.5rem;
  color: white;
  cursor: pointer;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.preview-body {
    flex: 1;
    overflow-y: auto;
    padding: 1.5rem;
}

.preview-meta h1 {
    margin: 0 0 1rem 0;
    font-size: 1.875rem;
    font-weight: 700;
    color: #111827;
}

.meta-info {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  font-size: 0.875rem;
  color: #6b7280;
  flex-wrap: wrap;
}

.meta-info span {
  background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
    .preview-modal {
        padding: 10px;
    }
    
    .preview-modal-content {
        max-height: 90vh;
    }
    
    .preview-body {
        padding: 1rem;
    }
    
    .meta-info {
        flex-direction: column;
        gap: 0.5rem;
    }
}
</style>
