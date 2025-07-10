<template>
  <div class="version-viewer">
    <div class="viewer-header">
      <div class="header-info">
        <h3>版本内容查看</h3>
        <div class="version-info">
          <span class="version-number">版本 {{ version?.versionNumber }}</span>
          <span class="version-date">{{ formatDate(version?.createdAt) }}</span>
          <span class="version-author">作者: {{ version?.author }}</span>
        </div>
      </div>
      <div class="header-actions">
        <button @click="togglePreview" class="preview-btn" :class="{ active: showPreview }">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
            <circle cx="12" cy="12" r="3"/>
          </svg>
          {{ showPreview ? '源码' : '预览' }}
        </button>
        <button @click="$emit('close')" class="close-btn">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>
    </div>

    <div class="viewer-content" v-if="!loading && content">
      <!-- 源码视图 -->
      <div v-if="!showPreview" class="source-view">
        <pre class="source-code">{{ content }}</pre>
      </div>
      
      <!-- 预览视图 -->
      <div v-else class="preview-view">
        <div class="preview-content" v-html="renderedContent"></div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>正在加载版本内容...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-state">
      <div class="error-icon">❌</div>
      <h3>加载失败</h3>
      <p>{{ error }}</p>
      <button @click="loadContent" class="retry-btn">重试</button>
    </div>

    <!-- 底部操作栏 -->
    <div class="viewer-footer" v-if="version">
      <div class="footer-info">
        <span>存储类型: {{ getStorageTypeLabel(version.storageType) }}</span>
        <span>原始大小: {{ formatFileSize(version.originalSize) }}</span>
        <span v-if="version.compressedSize">压缩大小: {{ formatFileSize(version.compressedSize) }}</span>
        <span v-if="version.compressionAlgorithm">压缩算法: {{ version.compressionAlgorithm }}</span>
      </div>
      <div class="footer-actions">
        <button @click="copyContent" class="copy-btn">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
            <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
          </svg>
          复制内容
        </button>
        <button @click="$emit('restore', version)" class="restore-btn">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/>
            <path d="M21 3v5h-5"/>
            <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/>
            <path d="M3 21v-5h5"/>
          </svg>
          恢复此版本
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { articleApi, type ArticleVersion, type ArticleVersionContent } from '../../api/article'

interface Props {
  versionId: number | null
  visible: boolean
}

interface Emits {
  (e: 'close'): void
  (e: 'restore', version: ArticleVersion): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 响应式状态
const loading = ref(false)
const error = ref<string | null>(null)
const content = ref<string>('')
const version = ref<ArticleVersion | null>(null)
const showPreview = ref(false)
const renderedContent = ref<string>('')

// 计算属性
const hasVersionId = computed(() => props.versionId !== null)

// 格式化日期
const formatDate = (dateString?: string): string => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 获取存储类型标签
const getStorageTypeLabel = (type: string): string => {
  const labels = {
    'FULL_CONTENT': '完整内容',
    'DIFF_FROM_BASE': '基于基础版本的差异',
    'DIFF_FROM_PREV': '基于前一版本的差异'
  }
  return labels[type as keyof typeof labels] || type
}

// 加载版本内容
const loadContent = async () => {
  if (!props.versionId) return
  
  try {
    loading.value = true
    error.value = null
    
    const versionContent = await articleApi.getVersionContent(props.versionId)
    content.value = versionContent.content
    version.value = versionContent.version
    
    // 如果是预览模式，渲染内容
    if (showPreview.value) {
      await renderContent()
    }
    
  } catch (err) {
    console.error('加载版本内容失败:', err)
    error.value = err instanceof Error ? err.message : '加载失败'
  } finally {
    loading.value = false
  }
}

// 渲染内容
const renderContent = async () => {
  if (!content.value) return
  
  try {
    // 调用API解析Wiki文本
    renderedContent.value = await articleApi.parseWikiText(content.value)
  } catch (err) {
    console.error('渲染内容失败:', err)
    renderedContent.value = `<div class="render-error">内容渲染失败: ${err instanceof Error ? err.message : '未知错误'}</div>`
  }
}

// 切换预览模式
const togglePreview = async () => {
  showPreview.value = !showPreview.value
  
  if (showPreview.value && !renderedContent.value) {
    await renderContent()
  }
}

// 复制内容
const copyContent = async () => {
  if (!content.value) return
  
  try {
    await navigator.clipboard.writeText(content.value)
    // 这里可以添加复制成功的提示
    alert('内容已复制到剪贴板')
  } catch (err) {
    console.error('复制失败:', err)
    alert('复制失败')
  }
}

// 监听版本ID变化
watch(() => props.versionId, () => {
  if (props.visible && hasVersionId.value) {
    loadContent()
  }
})

// 监听可见性变化
watch(() => props.visible, (visible) => {
  if (visible && hasVersionId.value) {
    loadContent()
  }
})

onMounted(() => {
  if (props.visible && hasVersionId.value) {
    loadContent()
  }
})
</script>

<style scoped>
.version-viewer {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.viewer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.header-info h3 {
  margin: 0 0 8px 0;
  font-size: 1.3rem;
  font-weight: 600;
}

.version-info {
  display: flex;
  gap: 16px;
  font-size: 0.9rem;
  opacity: 0.9;
}

.version-number {
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.preview-btn, .close-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 8px;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
}

.preview-btn:hover, .close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.preview-btn.active {
  background: rgba(255, 255, 255, 0.4);
}

.viewer-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.source-view {
  flex: 1;
  overflow: auto;
  padding: 24px;
  background: #f8f9fa;
}

.source-code {
  margin: 0;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9rem;
  line-height: 1.5;
  color: #495057;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.preview-view {
  flex: 1;
  overflow: auto;
  padding: 24px;
}

.preview-content {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 24px;
  line-height: 1.6;
}

.preview-content :deep(h1),
.preview-content :deep(h2),
.preview-content :deep(h3),
.preview-content :deep(h4),
.preview-content :deep(h5),
.preview-content :deep(h6) {
  margin-top: 0;
  margin-bottom: 16px;
  color: #1a202c;
}

.preview-content :deep(p) {
  margin-bottom: 16px;
  color: #4a5568;
}

.preview-content :deep(pre) {
  background: #f7fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 16px;
  overflow-x: auto;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9rem;
}

.preview-content :deep(code) {
  background: #f7fafc;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  padding: 2px 6px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9rem;
}

.preview-content :deep(blockquote) {
  border-left: 4px solid #667eea;
  padding-left: 16px;
  margin: 16px 0;
  color: #718096;
  font-style: italic;
}

.preview-content :deep(ul),
.preview-content :deep(ol) {
  padding-left: 24px;
  margin-bottom: 16px;
}

.preview-content :deep(li) {
  margin-bottom: 8px;
}

.preview-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 16px;
}

.preview-content :deep(th),
.preview-content :deep(td) {
  border: 1px solid #e2e8f0;
  padding: 8px 12px;
  text-align: left;
}

.preview-content :deep(th) {
  background: #f7fafc;
  font-weight: 600;
}

.loading-state, .error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
  flex: 1;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-icon {
  font-size: 3rem;
  margin-bottom: 16px;
}

.retry-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.retry-btn:hover {
  background: #5a6fd8;
}

.viewer-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
}

.footer-info {
  display: flex;
  gap: 16px;
  font-size: 0.85rem;
  color: #6c757d;
}

.footer-actions {
  display: flex;
  gap: 8px;
}

.copy-btn, .restore-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.copy-btn {
  background: #e9ecef;
  color: #495057;
}

.copy-btn:hover {
  background: #dee2e6;
}

.restore-btn {
  background: #667eea;
  color: white;
}

.restore-btn:hover {
  background: #5a6fd8;
}

.render-error {
  color: #dc3545;
  background: #f8d7da;
  border: 1px solid #f5c6cb;
  border-radius: 6px;
  padding: 16px;
  margin: 16px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .viewer-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .version-info {
    flex-direction: column;
    gap: 8px;
  }
  
  .viewer-footer {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .footer-info {
    flex-direction: column;
    gap: 8px;
  }
  
  .source-view, .preview-view {
    padding: 16px;
  }
  
  .source-code {
    padding: 12px;
    font-size: 0.8rem;
  }
  
  .preview-content {
    padding: 16px;
  }
}
</style>
