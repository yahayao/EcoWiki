<template>
  <div class="article-edit-page">
    <div class="container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>正在加载文章数据...</p>
      </div>

      <!-- 编辑界面 -->
      <template v-else>
        <!-- 页面标题栏 -->
        <div class="edit-header">
          <div class="title-section">
            <h1>{{ isEditMode ? `编辑 "${articleForm.title || '未命名文章'}"` : '新建文章' }}</h1>
            <p class="subtitle">
              {{ isEditMode 
                  ? '您正在编辑此页面的当前版本。请在"摘要"框中描述您的更改摘要，并记录您对各条目的编辑摘要，以帮助其他编辑者和未来的自己了解您的更改。'
                  : '您正在创建一个新的文章页面。请输入文章标题和内容，在"摘要"框中描述您创建的内容。'
              }}
            </p>
          </div>
          <div class="action-buttons">
            <button @click="goBack" class="back-btn">
              <span class="back-icon">←</span>
              {{ isEditMode ? '返回文章' : '取消创建' }}
            </button>
          </div>
        </div>

        <!-- 编辑提示 -->
        <div class="edit-notice">
          <div class="notice-icon">💡</div>
          <div class="notice-content">
            <p><strong>关于页面编辑：</strong> 我们很高兴您愿意为本项目做出贡献，您也是为这一次性接受新条目的贡献，我们仅能接受作者授权条目或原创。</p>
            <p><strong>关于页面内容：</strong> 此页面作为发布到互联网的条目，所以其在流程语义信息，如有雷同工作的实体条目条目，访问者应该是对同类条目至关重要的平台。</p>
          </div>
        </div>

        <!-- 文章基本信息 -->
        <div class="article-info-form">
          <div class="form-row">
            <div class="form-group title-group">
              <label for="title">文章标题 *</label>
              <input
                id="title"
                v-model="articleForm.title"
                type="text"
                placeholder="请输入文章标题"
                class="form-input title-input"
                required
              />
            </div>
          </div>
        </div>

        <!-- 编辑工具栏和内容区域 -->
        <div class="editor-container">
          <!-- 编辑工具栏 -->
          <div class="editor-toolbar">
            <div class="toolbar-section">
              <div class="toolbar-group">
                <button @click="insertText('\'\'\'', '\'\'\'', '粗体文字')" title="粗体 (Ctrl+B)" class="toolbar-btn">
                  <strong>B</strong>
                </button>
                <button @click="insertText('\'\'', '\'\'', '斜体文字')" title="斜体 (Ctrl+I)" class="toolbar-btn">
                  <em>I</em>
                </button>
                <button @click="insertText('[[', ']]', '链接文字')" title="内部链接" class="toolbar-btn">
                  链接
                </button>
                <button @click="insertText('[', ' ]', 'http://example.com 显示文字')" title="外部链接" class="toolbar-btn">
                  外链
                </button>
                <button @click="insertText('<ref>', '</ref>', '引用内容')" title="引用" class="toolbar-btn">
                  引用
                </button>
              </div>
              
              <div class="toolbar-divider"></div>
              
              <div class="toolbar-group">
                <button @click="insertHeading(2)" title="二级标题" class="toolbar-btn">H2</button>
                <button @click="insertHeading(3)" title="三级标题" class="toolbar-btn">H3</button>
                <button @click="insertHeading(4)" title="四级标题" class="toolbar-btn">H4</button>
              </div>
              
              <div class="toolbar-divider"></div>
              
              <div class="toolbar-group">
                <button @click="insertText('* ', '', '列表项')" title="无序列表" class="toolbar-btn">
                  • 列表
                </button>
                <button @click="insertText('# ', '', '列表项')" title="有序列表" class="toolbar-btn">
                  1. 列表
                </button>
              </div>
              
              <div class="toolbar-divider"></div>
              
              <div class="toolbar-group">
                <button @click="insertTable" title="插入表格" class="toolbar-btn">
                  表格
                </button>
                <button @click="insertText('{{', '}}', '模板名称')" title="模板" class="toolbar-btn">
                  模板
                </button>
                <button @click="insertText('<nowiki>', '</nowiki>', '原始文字')" title="原始文字" class="toolbar-btn">
                  原始
                </button>
              </div>
            </div>
            
            <div class="toolbar-right">
              <button @click="togglePreview" class="toolbar-btn preview-btn" :class="{ active: showPreview }">
                {{ showPreview ? '隐藏预览' : '显示预览' }}
              </button>
            </div>
          </div>

          <!-- 编辑区域 -->
          <div class="editor-content" :class="{ 'split-view': showPreview }">
            <!-- 文本编辑器 -->
            <div class="editor-pane" v-show="!showPreview || showEditor">
              <textarea
                ref="editorTextarea"
                v-model="articleForm.content"
                class="wiki-editor"
                placeholder="请输入文章内容，支持Wiki语法..."
                @keydown="handleKeydown"
                @input="handleInput"
                rows="20"
              ></textarea>
            </div>
            
            <!-- 预览面板 -->
            <div class="preview-pane" v-show="showPreview">
              <div class="preview-header">
                <h4>预览</h4>
                <small>这是您的文章在发布后的样子</small>
              </div>
              <div class="preview-content" v-html="previewHtml"></div>
            </div>
          </div>
        </div>

        <!-- 编辑摘要和保存区域 -->
        <div class="edit-summary-section">
          <div class="summary-row">
            <div class="summary-group">
              <label for="edit-summary">摘要（描述你的更改）：</label>
              <input 
                id="edit-summary" 
                v-model="editSummary" 
                type="text" 
                class="summary-input"
                placeholder="例如：修正错别字、添加新内容、更新信息等..." 
                maxlength="200" 
              />
            </div>
          </div>

          <!-- 其他文章信息 -->
          <div class="article-meta">
            <div class="meta-row">
              <div class="meta-group">
                <label for="category">分类：</label>
                <select id="category" v-model="articleForm.category" class="form-select">
                  <option value="">选择分类</option>
                  <option value="环保">环保</option>
                  <option value="技术">技术</option>
                  <option value="教育">教育</option>
                  <option value="健康">健康</option>
                  <option value="学术研究">学术研究</option>
                  <option value="其他">其他</option>
                </select>
              </div>
              
              <div class="meta-group">
                <label for="tags">标签：</label>
                <input
                  id="tags"
                  v-model="articleForm.tags"
                  type="text"
                  placeholder="用逗号分隔，如：环保,技术,教育"
                  class="form-input"
                />
              </div>
              
              <div class="meta-group" v-if="!isEditMode">
                <label for="author">作者：</label>
                <input
                  id="author"
                  v-model="articleForm.author"
                  type="text"
                  placeholder="请输入作者姓名"
                  class="form-input"
                  required
                />
              </div>
            </div>
          </div>

          <!-- 保存按钮区域 -->
          <div class="save-section">
            <div class="save-buttons">
              <button @click="handleSave" class="save-btn primary" :disabled="saving || !canSave">
                <span v-if="saving" class="loading-spinner"></span>
                {{ saving 
                    ? (isEditMode ? '保存更改...' : '发表文章...')
                    : (isEditMode ? '保存更改' : '发表文章')
                }}
              </button>
              <button @click="togglePreview" class="preview-btn secondary">
                {{ showPreview ? '隐藏预览' : '显示预览' }}
              </button>
              <button @click="goBack" class="cancel-btn">取消</button>
            </div>
            
            <div class="save-help">
              <p>{{ isEditMode ? '保存后您的更改将立即生效' : '发表后文章将对所有用户可见' }}。请确保内容准确无误。</p>
            </div>
          </div>
        </div>
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
            <h1>{{ articleForm.title || '未命名文章' }}</h1>
            <div class="meta-info">
              <span>作者：{{ articleForm.author || '未知' }}</span>
              <span>分类：{{ articleForm.category || '未分类' }}</span>
              <span v-if="articleForm.tags">标签：{{ articleForm.tags }}</span>
            </div>
          </div>
          <div class="preview-content" v-html="previewHtml"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import WikiEditor from '../components/WikiEditor.vue'
import { articleApi, type Article, type ArticleCreateRequest, type ArticleUpdateRequest } from '../api/article'
import { wikiParser } from '../utils/wikiParser'
import toast from '../utils/toast'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(true)
const saving = ref(false)
const showPreview = ref(false)
const showEditor = ref(true)
const previewModalVisible = ref(false)
const previewHtml = ref('')
const editSummary = ref('')
const articleExists = ref(true) // 用于判断文章是否存在
const editorTextarea = ref<HTMLTextAreaElement | null>(null)
const showHelpPanel = ref(false)
const referrerPath = ref('/')
const saveSuccessful = ref(false) // 标记保存是否成功

// 文章表单数据
const articleForm = ref({
  title: '',
  content: '',
  category: '',
  tags: '',
  author: ''
})

const originalArticle = ref<Article | null>(null)

// 计算属性
const isEditMode = computed(() => {
  return articleExists.value && originalArticle.value !== null
})

const canSave = computed(() => {
  return articleForm.value.title.trim() && 
         articleForm.value.content.trim() && 
         articleForm.value.author.trim()
})

// 方法
const loadArticle = async () => {
  const articleId = route.params.id as string
  
  // 如果ID无效，直接进入创建模式
  if (!articleId || isNaN(Number(articleId))) {
    articleExists.value = false
    loading.value = false
    return
  }

  try {
    const article = await articleApi.getArticleById(Number(articleId))
    // 文章存在，进入编辑模式
    originalArticle.value = article
    articleExists.value = true
    
    articleForm.value = {
      title: article.title,
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
    
    // 设置默认值
    articleForm.value = {
      title: '',
      content: '',
      category: '',
      tags: '',
      author: ''
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

  try {
    saving.value = true
    saveSuccessful.value = false

    if (isEditMode.value) {
      // 更新文章
      const updateData: ArticleUpdateRequest = {
        title: articleForm.value.title.trim(),
        content: articleForm.value.content.trim(),
        category: articleForm.value.category.trim(),
        tags: articleForm.value.tags.trim()
      }
      
      const updated = await articleApi.updateArticle(Number(route.params.id), updateData)
      
      // 更新原始文章数据，防止离开页面时显示未保存提示
      originalArticle.value = updated
      
      // 同步更新当前表单，确保完全一致
      articleForm.value = {
        title: updated.title,
        content: updated.content || '',
        category: updated.category || '',
        tags: updated.tags || '',
        author: updated.author
      }
      
      saveSuccessful.value = true
      
      toast.success('文章更新成功！')
      
      // 使用setTimeout确保状态更新后再导航
      setTimeout(() => {
        router.push(`/article/${updated.articleId}`)
      }, 100)
    } else {
      // 创建文章
      const createData: ArticleCreateRequest = {
        title: articleForm.value.title.trim(),
        content: articleForm.value.content.trim(),
        category: articleForm.value.category.trim(),
        tags: articleForm.value.tags.trim(),
        author: articleForm.value.author.trim()
      }
      
      const created = await articleApi.createArticle(createData)
      
      // 创建成功后，设置为编辑模式并更新原始数据
      originalArticle.value = created
      articleExists.value = true
      
      // 同步更新当前表单
      articleForm.value = {
        title: created.title,
        content: created.content || '',
        category: created.category || '',
        tags: created.tags || '',
        author: created.author
      }
      
      saveSuccessful.value = true
      
      toast.success('文章创建成功！')
      
      // 使用setTimeout确保状态更新后再导航
      setTimeout(() => {
        router.push(`/article/${created.articleId}`)
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
  if (showPreview.value) {
    updatePreview()
  }
}

const closePreviewModal = () => {
  previewModalVisible.value = false
}

const goBack = () => {
  if (isEditMode.value && originalArticle.value) {
    router.push(`/article/${originalArticle.value.articleId}`)
  } else {
    router.push('/')
  }
}

// 检查是否有未保存的更改
const hasUnsavedChanges = computed(() => {
  if (!isEditMode.value) {
    return articleForm.value.title || articleForm.value.content || articleForm.value.author
  }
  
  if (!originalArticle.value) return false
  
  return (
    articleForm.value.title !== originalArticle.value.title ||
    articleForm.value.content !== (originalArticle.value.content || '') ||
    articleForm.value.category !== (originalArticle.value.category || '') ||
    articleForm.value.tags !== (originalArticle.value.tags || '') ||
    articleForm.value.author !== originalArticle.value.author
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

// 监听内容变化自动生成预览
watch(() => articleForm.value.content, (newContent) => {
  if (showPreview.value) {
    updatePreview()
  }
}, { debounce: 500 })

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

const insertText = (before: string, after: string = '', placeholder: string = '') => {
  if (!editorTextarea.value) return

  const textarea = editorTextarea.value
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = textarea.value.substring(start, end)

  let insertText = before
  if (selectedText) {
    insertText += selectedText
  } else if (placeholder) {
    insertText += placeholder
  }
  insertText += after

  const newValue = textarea.value.substring(0, start) + insertText + textarea.value.substring(end)
  articleForm.value.content = newValue

  // 设置光标位置
  setTimeout(() => {
    textarea.focus()
    const newPosition = start + before.length + (selectedText || placeholder).length
    textarea.setSelectionRange(newPosition, newPosition)
  }, 0)
}

const insertHeading = (level: number) => {
  const prefix = '='.repeat(level) + ' '
  const suffix = ' ' + '='.repeat(level)
  insertText(prefix, suffix, '标题文字')
}

const handleKeydown = (event: KeyboardEvent) => {
  // 处理快捷键
  if (event.ctrlKey || event.metaKey) {
    switch (event.key) {
      case 'b':
      case 'B':
        event.preventDefault()
        insertText('\'\'\'', '\'\'\'', '粗体文字')
        break
      case 'i':
      case 'I':
        event.preventDefault()
        insertText('\'\'', '\'\'', '斜体文字')
        break
      case 's':
      case 'S':
        event.preventDefault()
        handleSave()
        break
    }
  }

  // Tab键插入缩进
  if (event.key === 'Tab') {
    event.preventDefault()
    insertText('    ')
  }
}

const handleInput = () => {
  // 如果预览开启，实时更新预览
  if (showPreview.value) {
    updatePreview()
  }
}

const updatePreview = () => {
  // 这里应该调用wiki解析器
  previewHtml.value = wikiParser.parseToHtml(articleForm.value.content)
}

const updateSelection = () => {
    // 更新选择状态
}

const saveChanges = async () => {
  // 这个函数已被 handleSave() 替代
  await handleSave()
}

const toggleHelpPanel = () => {
    showHelpPanel.value = !showHelpPanel.value
}

const insertTemplate = () => {
    insertText('{{', '}}', '模板名称')
}

const insertTable = () => {
    const tableTemplate = `{| class="wikitable"
|-
! 标题1 !! 标题2 !! 标题3
|-
| 单元格1 || 单元格2 || 单元格3
|-
| 单元格4 || 单元格5 || 单元格6
|}`
    insertText(tableTemplate)
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

.loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
    background: white;
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.loading-state .loading-spinner {
    width: 40px;
    height: 40px;
    border: 4px solid #f3f4f6;
    border-top: 4px solid #667eea;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 16px;
}

.loading-state p {
    color: #718096;
    font-size: 1rem;
}

.edit-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;
    padding: 24px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.title-section h1 {
    color: #1a202c;
    font-size: 1.8rem;
    font-weight: 600;
    margin-bottom: 8px;
}

.subtitle {
    color: #718096;
    font-size: 0.9rem;
    line-height: 1.5;
    max-width: 600px;
}

.back-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 16px;
    background: linear-gradient(135deg, #667eea, #764ba2);
    color: white;
    border: none;
    border-radius: 20px;
    font-size: 0.9rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
}

.back-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.back-icon {
    font-size: 1.2rem;
    font-weight: bold;
}

.edit-notice {
    display: flex;
    gap: 12px;
    padding: 16px;
    background: #fff3cd;
    border: 1px solid #ffeaa7;
    border-radius: 8px;
    margin-bottom: 24px;
}

.notice-icon {
    font-size: 1.2rem;
    flex-shrink: 0;
}

.notice-content p {
    margin: 4px 0;
    color: #856404;
    font-size: 0.9rem;
    line-height: 1.4;
}

.edit-toolbar {
    display: flex;
    gap: 16px;
    padding: 12px 16px;
    background: white;
    border: 1px solid #e2e8f0;
    border-radius: 8px 8px 0 0;
    flex-wrap: wrap;
}

.toolbar-group {
    display: flex;
    gap: 4px;
}

.toolbar-btn {
    padding: 6px 10px;
    background: #f8f9fa;
    border: 1px solid #dee2e6;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.2s ease;
    font-size: 0.9rem;
    min-width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.toolbar-btn:hover {
    background: #e9ecef;
    border-color: #adb5bd;
}

.toolbar-btn:active {
    background: #dee2e6;
}

.editor-container {
    background: white;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    margin-bottom: 24px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.editor-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    background: #f8fafc;
    border-bottom: 1px solid #e2e8f0;
    flex-wrap: wrap;
    gap: 8px;
}

.toolbar-section {
    display: flex;
    align-items: center;
    gap: 4px;
    flex-wrap: wrap;
}

.toolbar-group {
    display: flex;
    align-items: center;
    gap: 2px;
}

.toolbar-divider {
    width: 1px;
    height: 20px;
    background: #d1d5db;
    margin: 0 8px;
}

.toolbar-btn {
    padding: 4px 8px;
    background: white;
    color: #374151;
    border: 1px solid #d1d5db;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
    white-space: nowrap;
    min-height: 28px;
}

.toolbar-btn:hover {
    background: #f3f4f6;
    border-color: #9ca3af;
}

.toolbar-btn.active {
    background: #3b82f6;
    color: white;
    border-color: #3b82f6;
}

.toolbar-right {
    display: flex;
    align-items: center;
    gap: 8px;
}

.toolbar-btn.preview-btn {
    background: #10b981;
    color: white;
    border-color: #10b981;
    padding: 6px 12px;
}

.toolbar-btn.preview-btn:hover {
    background: #059669;
    border-color: #059669;
}

.editor-content {
    display: flex;
    min-height: 400px;
}

.editor-content.split-view .editor-pane {
    width: 50%;
    border-right: 1px solid #e2e8f0;
}

.editor-content.split-view .preview-pane {
    width: 50%;
}

.editor-pane {
    flex: 1;
    position: relative;
}

.wiki-editor {
    width: 100%;
    height: 400px;
    padding: 16px;
    border: none;
    outline: none;
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
    font-size: 14px;
    line-height: 1.5;
    background: white;
    color: #374151;
    resize: vertical;
    min-height: 400px;
}

.wiki-editor:focus {
    background: #fefefe;
}

.preview-pane {
    background: #fafafa;
    border-left: 1px solid #e2e8f0;
    overflow-y: auto;
}

.preview-header {
    padding: 12px 16px;
    background: #f1f5f9;
    border-bottom: 1px solid #e2e8f0;
}

.preview-header h4 {
    margin: 0 0 4px 0;
    font-size: 14px;
    font-weight: 600;
    color: #374151;
}

.preview-header small {
    color: #6b7280;
    font-size: 12px;
}

.preview-content {
    padding: 16px;
    background: white;
    min-height: 300px;
    font-family: system-ui, -apple-system, sans-serif;
    line-height: 1.6;
}

.edit-summary-section {
    background: white;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.summary-row {
    margin-bottom: 16px;
}

.summary-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.summary-group label {
    font-size: 14px;
    font-weight: 500;
    color: #374151;
}

.summary-input {
    width: 100%;
    padding: 8px 12px;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    font-size: 14px;
    transition: border-color 0.2s ease;
}

.summary-input:focus {
    outline: none;
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.article-meta {
    border-top: 1px solid #e2e8f0;
    padding-top: 16px;
}

.meta-row {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
}

.meta-group {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 200px;
}

.meta-group label {
    font-size: 14px;
    font-weight: 500;
    color: #374151;
    white-space: nowrap;
}

.meta-group .form-input,
.meta-group .form-select {
    flex: 1;
    padding: 6px 10px;
    border: 1px solid #d1d5db;
    border-radius: 4px;
    font-size: 14px;
}

.meta-group .form-input:focus,
.meta-group .form-select:focus {
    outline: none;
    border-color: #3b82f6;
    box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.save-section {
    border-top: 1px solid #e2e8f0;
    padding-top: 20px;
}

.save-buttons {
    display: flex;
    gap: 12px;
    align-items: center;
    margin-bottom: 12px;
    flex-wrap: wrap;
}

.save-help {
    color: #6b7280;
    font-size: 14px;
}

.save-help p {
    margin: 0;
}

.title-group {
    width: 100%;
}

.title-input {
    font-size: 18px;
    font-weight: 500;
    padding: 12px 16px;
    border: 2px solid #e2e8f0;
    border-radius: 8px;
    transition: border-color 0.2s ease;
}

.title-input:focus {
    outline: none;
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
    .editor-content.split-view {
        flex-direction: column;
    }
    
    .editor-content.split-view .editor-pane,
    .editor-content.split-view .preview-pane {
        width: 100%;
        border-right: none;
        border-left: none;
    }
    
    .editor-content.split-view .preview-pane {
        border-top: 1px solid #e2e8f0;
    }
    
    .toolbar-section {
        width: 100%;
        justify-content: flex-start;
    }
    
    .meta-row {
        flex-direction: column;
        gap: 12px;
    }
    
    .meta-group {
        min-width: auto;
        flex-direction: column;
        align-items: flex-start;
    }
    
    .wiki-editor {
        height: 300px;
        min-height: 300px;
    }
}
</style>
