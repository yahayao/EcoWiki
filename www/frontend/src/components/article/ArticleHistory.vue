<template>
  <div class="article-history">
    <!-- 历史记录头部 -->
    <div class="history-header">
      <div class="header-title">
        <h2>文章历史</h2>
        <p class="subtitle">{{ article?.title }}</p>
      </div>
      <div class="header-actions">
        <button @click="refreshHistory" class="refresh-btn" :disabled="loading">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/>
            <path d="M21 3v5h-5"/>
            <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/>
            <path d="M3 21v-5h5"/>
          </svg>
          刷新
        </button>
        <button @click="$emit('close')" class="close-btn">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- 版本统计信息 -->
    <div class="version-stats" v-if="versionStats">
      <div class="stat-item">
        <span class="stat-label">总版本数</span>
        <span class="stat-value">{{ versionStats.totalVersions }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">基础版本</span>
        <span class="stat-value">{{ versionStats.baseVersionsCount }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">差异版本</span>
        <span class="stat-value">{{ versionStats.diffVersionsCount }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">存储大小</span>
        <span class="stat-value">{{ formatFileSize(versionStats.compressedStorageSize) }}</span>
      </div>
    </div>

    <!-- 版本列表 -->
    <div class="version-list" v-if="!loading && versions.length > 0">
      <div 
        v-for="version in versions" 
        :key="version.versionId"
        class="version-item"
        :class="{ 
          'is-current': version.versionId === currentVersionId,
          'is-selected': selectedVersions.includes(version.versionId)
        }"
      >
        <div class="version-checkbox">
          <input 
            type="checkbox" 
            :value="version.versionId"
            v-model="selectedVersions"
            :disabled="selectedVersions.length >= 2 && !selectedVersions.includes(version.versionId)"
          >
        </div>
        
        <div class="version-info">
          <div class="version-header">
            <span class="version-number">版本 {{ version.versionNumber }}</span>
            <span class="version-type" :class="version.storageType.toLowerCase()">
              {{ getStorageTypeLabel(version.storageType) }}
            </span>
            <span class="version-date">{{ formatDate(version.createdAt) }}</span>
          </div>
          
          <div class="version-meta">
            <span class="version-author">作者: {{ version.author }}</span>
            <span class="version-size">{{ formatFileSize(version.originalSize) }}</span>
            <span v-if="version.isCompressed" class="compressed-badge">已压缩</span>
            <span v-if="version.isArchived" class="archived-badge">已归档</span>
          </div>
          
          <div class="version-summary" v-if="version.changeSummary">
            {{ version.changeSummary }}
          </div>
        </div>
        
        <div class="version-actions">
          <button 
            @click="viewVersion(version)" 
            class="action-btn view-btn"
            title="查看此版本"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
              <circle cx="12" cy="12" r="3"/>
            </svg>
          </button>
          
          <button 
            @click="restoreVersion(version)" 
            class="action-btn restore-btn"
            title="恢复到此版本"
            :disabled="version.versionId === currentVersionId"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/>
              <path d="M21 3v5h-5"/>
              <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/>
              <path d="M3 21v-5h5"/>
            </svg>
          </button>
          
          <button 
            @click="deleteVersion(version)" 
            class="action-btn delete-btn"
            title="删除此版本"
            :disabled="version.versionId === currentVersionId || version.storageType === 'FULL_CONTENT'"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3,6 5,6 21,6"/>
              <path d="m19,6v14a2,2 0 0,1 -2,2H7a2,2 0 0,1 -2,-2V6m3,0V4a2,2 0 0,1 2,-2h4a2,2 0 0,1 2,2v2"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>正在加载历史记录...</p>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading && versions.length === 0" class="empty-state">
      <div class="empty-icon">📄</div>
      <h3>暂无历史记录</h3>
      <p>该文章还没有历史版本记录</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-state">
      <div class="error-icon">❌</div>
      <h3>加载失败</h3>
      <p>{{ error }}</p>
      <button @click="loadVersions" class="retry-btn">重试</button>
    </div>

    <!-- 底部操作栏 -->
    <div class="bottom-actions" v-if="versions.length > 0">
      <div class="selection-info">
        <span v-if="selectedVersions.length === 0">请选择版本进行比较</span>
        <span v-else-if="selectedVersions.length === 1">已选择 1 个版本</span>
        <span v-else>已选择 {{ selectedVersions.length }} 个版本</span>
      </div>
      
      <div class="action-buttons">
        <button 
          @click="clearSelection" 
          class="clear-btn"
          :disabled="selectedVersions.length === 0"
        >
          清除选择
        </button>
        
        <button 
          @click="compareVersions" 
          class="compare-btn"
          :disabled="selectedVersions.length !== 2"
        >
          比较版本
        </button>
      </div>
    </div>

    <!-- 分页控制 -->
    <div class="pagination" v-if="totalPages > 1">
      <button 
        @click="changePage(currentPage - 1)" 
        :disabled="currentPage === 0"
        class="page-btn"
      >
        上一页
      </button>
      
      <div class="page-info">
        {{ currentPage + 1 }} / {{ totalPages }}
      </div>
      
      <button 
        @click="changePage(currentPage + 1)" 
        :disabled="currentPage === totalPages - 1"
        class="page-btn"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { articleApi, type Article, type ArticleVersion, type ArticleVersionStats } from '../../api/article'

interface Props {
  article: Article | null
  visible: boolean
}

interface Emits {
  (e: 'close'): void
  (e: 'viewVersion', version: ArticleVersion): void
  (e: 'restoreVersion', version: ArticleVersion): void
  (e: 'compareVersions', sourceId: number, targetId: number): void
  (e: 'versionDeleted', versionId: number): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 响应式状态
const loading = ref(false)
const error = ref<string | null>(null)
const versions = ref<ArticleVersion[]>([])
const versionStats = ref<ArticleVersionStats | null>(null)
const selectedVersions = ref<number[]>([])
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = ref(20)
const currentVersionId = ref<number | null>(null)

// 计算属性
const hasArticle = computed(() => props.article !== null)

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 格式化日期
const formatDate = (dateString: string): string => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取存储类型标签
const getStorageTypeLabel = (type: string): string => {
  const labels = {
    'FULL_CONTENT': '完整',
    'DIFF_FROM_BASE': '差异',
    'DIFF_FROM_PREV': '增量'
  }
  return labels[type as keyof typeof labels] || type
}

// 加载版本列表
const loadVersions = async () => {
  if (!props.article) return
  
  try {
    loading.value = true
    error.value = null
    
    const response = await articleApi.getArticleVersions(
      props.article.articleId, 
      currentPage.value, 
      pageSize.value
    )
    
    versions.value = response.content
    totalPages.value = response.totalPages
    
    // 获取版本统计信息
    versionStats.value = await articleApi.getVersionStats(props.article.articleId)
    
    // 设置当前版本ID（假设最新版本为当前版本）
    if (versions.value.length > 0) {
      currentVersionId.value = versions.value[0].versionId
    }
    
  } catch (err) {
    console.error('加载版本历史失败:', err)
    error.value = err instanceof Error ? err.message : '加载失败'
  } finally {
    loading.value = false
  }
}

// 刷新历史记录
const refreshHistory = () => {
  loadVersions()
}

// 查看版本
const viewVersion = (version: ArticleVersion) => {
  emit('viewVersion', version)
}

// 恢复版本
const restoreVersion = async (version: ArticleVersion) => {
  if (!props.article) return
  
  const confirmed = confirm(`确定要恢复到版本 ${version.versionNumber} 吗？`)
  if (!confirmed) return
  
  try {
    loading.value = true
    await articleApi.restoreVersion(props.article.articleId, version.versionId)
    emit('restoreVersion', version)
    loadVersions() // 重新加载版本列表
  } catch (err) {
    console.error('恢复版本失败:', err)
    alert('恢复版本失败: ' + (err instanceof Error ? err.message : '未知错误'))
  } finally {
    loading.value = false
  }
}

// 删除版本
const deleteVersion = async (version: ArticleVersion) => {
  const confirmed = confirm(`确定要删除版本 ${version.versionNumber} 吗？此操作无法撤销。`)
  if (!confirmed) return
  
  try {
    loading.value = true
    await articleApi.deleteVersion(version.versionId)
    emit('versionDeleted', version.versionId)
    loadVersions() // 重新加载版本列表
  } catch (err) {
    console.error('删除版本失败:', err)
    alert('删除版本失败: ' + (err instanceof Error ? err.message : '未知错误'))
  } finally {
    loading.value = false
  }
}

// 比较版本
const compareVersions = () => {
  if (selectedVersions.value.length === 2) {
    const [sourceId, targetId] = selectedVersions.value.sort((a, b) => b - a) // 降序排序
    emit('compareVersions', sourceId, targetId)
  }
}

// 清除选择
const clearSelection = () => {
  selectedVersions.value = []
}

// 切换页面
const changePage = (page: number) => {
  currentPage.value = page
  loadVersions()
}

// 监听可见性变化
onMounted(() => {
  if (props.visible && hasArticle.value) {
    loadVersions()
  }
})
</script>

<style scoped>
.article-history {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.header-title h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
}

.subtitle {
  margin: 4px 0 0 0;
  font-size: 0.9rem;
  opacity: 0.9;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.refresh-btn, .close-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 8px;
  color: white;
  cursor: pointer;
  transition: background 0.2s;
}

.refresh-btn:hover, .close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.version-stats {
  display: flex;
  justify-content: space-around;
  padding: 16px 24px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-label {
  font-size: 0.8rem;
  color: #6c757d;
}

.stat-value {
  font-size: 1.2rem;
  font-weight: 600;
  color: #495057;
}

.version-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 24px;
}

.version-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  margin-bottom: 8px;
  transition: all 0.2s;
}

.version-item:hover {
  background: #f8f9fa;
  border-color: #667eea;
}

.version-item.is-current {
  background: #e7f3ff;
  border-color: #007bff;
}

.version-item.is-selected {
  background: #fff3cd;
  border-color: #ffc107;
}

.version-checkbox input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.version-info {
  flex: 1;
}

.version-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.version-number {
  font-weight: 600;
  color: #495057;
}

.version-type {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
  color: white;
}

.version-type.full_content {
  background: #28a745;
}

.version-type.diff_from_base {
  background: #ffc107;
  color: #212529;
}

.version-type.diff_from_prev {
  background: #17a2b8;
}

.version-date {
  font-size: 0.85rem;
  color: #6c757d;
}

.version-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.version-author {
  font-size: 0.85rem;
  color: #495057;
}

.version-size {
  font-size: 0.8rem;
  color: #6c757d;
}

.compressed-badge, .archived-badge {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: 500;
}

.compressed-badge {
  background: #d4edda;
  color: #155724;
}

.archived-badge {
  background: #f8d7da;
  color: #721c24;
}

.version-summary {
  font-size: 0.85rem;
  color: #495057;
  font-style: italic;
}

.version-actions {
  display: flex;
  gap: 4px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.view-btn {
  background: #e3f2fd;
  color: #1976d2;
}

.view-btn:hover {
  background: #1976d2;
  color: white;
}

.restore-btn {
  background: #f3e5f5;
  color: #7b1fa2;
}

.restore-btn:hover {
  background: #7b1fa2;
  color: white;
}

.delete-btn {
  background: #ffebee;
  color: #d32f2f;
}

.delete-btn:hover {
  background: #d32f2f;
  color: white;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading-state, .empty-state, .error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f3f4f6;
  border-top: 3px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-icon, .error-icon {
  font-size: 3rem;
  margin-bottom: 16px;
}

.bottom-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
}

.selection-info {
  font-size: 0.9rem;
  color: #6c757d;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.clear-btn, .compare-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}

.clear-btn {
  background: #f8f9fa;
  color: #6c757d;
  border: 1px solid #dee2e6;
}

.clear-btn:hover {
  background: #e9ecef;
}

.compare-btn {
  background: #667eea;
  color: white;
}

.compare-btn:hover {
  background: #5a6fd8;
}

.clear-btn:disabled, .compare-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-top: 1px solid #e9ecef;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover {
  background: #f8f9fa;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 0.9rem;
  color: #6c757d;
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
</style>
