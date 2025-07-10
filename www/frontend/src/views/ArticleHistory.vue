<template>
  <div class="article-history-page">
    <!-- 页面头部 -->
    <header class="page-header">
      <div class="header-content">
        <button @click="goBack" class="back-button">
          <i class="fas fa-arrow-left"></i>
          返回文章
        </button>
        <div class="header-info">
          <h1 class="page-title">{{ articleTitle }} - 版本历史</h1>
          <p class="page-subtitle">共 {{ versions.length }} 个版本</p>
        </div>
      </div>
    </header>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>正在加载版本历史...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <div class="error-message">
        <i class="fas fa-exclamation-triangle"></i>
        <p>{{ error }}</p>
        <button @click="loadVersions" class="retry-button">重试</button>
      </div>
    </div>

    <!-- 版本列表 -->
    <div v-else class="history-content">
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <button 
            @click="showCompareMode = !showCompareMode" 
            class="compare-toggle"
            :class="{ active: showCompareMode }"
          >
            <i class="fas fa-columns"></i>
            {{ showCompareMode ? '退出对比' : '版本对比' }}
          </button>
        </div>
        <div class="toolbar-right">
          <select v-model="sortOrder" class="sort-select">
            <option value="desc">最新在前</option>
            <option value="asc">最旧在前</option>
          </select>
        </div>
      </div>

      <!-- 对比模式提示 -->
      <div v-if="showCompareMode" class="compare-hint">
        <i class="fas fa-info-circle"></i>
        请选择两个版本进行对比（已选择 {{ selectedVersions.length }}/2）
        <button 
          v-if="selectedVersions.length === 2" 
          @click="compareVersions"
          class="compare-button"
        >
          开始对比
        </button>
      </div>

      <!-- 版本列表 -->
      <div class="versions-list">
        <div 
          v-for="version in sortedVersions" 
          :key="version.versionId"
          class="version-item"
          :class="{ 
            selected: selectedVersions.includes(version.versionId),
            disabled: showCompareMode && selectedVersions.length >= 2 && !selectedVersions.includes(version.versionId)
          }"
          @click="handleVersionClick(version)"
        >
          <div class="version-header">
            <div class="version-info">
              <span class="version-number">v{{ version.versionNumber }}</span>
              <span class="version-type" :class="getVersionTypeClass(version.storageType)">
                {{ getVersionTypeText(version.storageType) }}
              </span>
              <span v-if="version.versionNumber === 1" class="version-badge initial">初始版本</span>
              <span v-if="isLatestVersion(version)" class="version-badge latest">最新版本</span>
            </div>
            <div class="version-actions">
              <button 
                @click.stop="viewVersion(version)"
                class="action-button view"
                title="查看此版本"
              >
                <i class="fas fa-eye"></i>
              </button>
              <button 
                v-if="!isLatestVersion(version)"
                @click.stop="restoreVersion(version)"
                class="action-button restore"
                title="恢复到此版本"
              >
                <i class="fas fa-undo"></i>
              </button>
            </div>
          </div>
          
          <div class="version-details">
            <div class="version-meta">
              <span class="author">
                <i class="fas fa-user"></i>
                {{ version.author || '未知作者' }}
              </span>
              <span class="date">
                <i class="fas fa-calendar"></i>
                {{ formatDate(version.createdAt) }}
              </span>
              <span class="size">
                <i class="fas fa-file-alt"></i>
                {{ version.originalSize ? formatSize(version.originalSize) : 'N/A' }}
                <span v-if="version.compressedSize" class="compressed">
                  (压缩后: {{ formatSize(version.compressedSize) }})
                </span>
              </span>
            </div>
            
            <div v-if="version.changeSummary" class="change-summary">
              <i class="fas fa-edit"></i>
              {{ version.changeSummary }}
            </div>
            
            <div class="version-stats">
              <span v-if="version.compressionRatio" class="compression">
                <i class="fas fa-compress"></i>
                压缩比: {{ Math.round(version.compressionRatio * 100) }}%
              </span>
              <span v-if="version.isArchived" class="archived">
                <i class="fas fa-archive"></i>
                已归档
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 版本查看器 -->
    <div v-if="viewingVersion" class="version-viewer-overlay" @click="closeViewer">
      <div class="version-viewer" @click.stop>
        <div class="viewer-header">
          <h3>版本 {{ viewingVersion.versionNumber }} - {{ formatDate(viewingVersion.createdAt) }}</h3>
          <button @click="closeViewer" class="close-button">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="viewer-content">
          <div v-if="viewingVersionContent" class="content-display" v-html="viewingVersionContent"></div>
          <div v-else class="loading-content">
            <div class="loading-spinner"></div>
            <p>正在加载版本内容...</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 版本对比器 -->
    <div v-if="comparingVersions" class="version-compare-overlay" @click="closeCompare">
      <div class="version-compare" @click.stop>
        <div class="compare-header">
          <h3>版本对比</h3>
          <button @click="closeCompare" class="close-button">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="compare-content">
          <div class="compare-panels">
            <div class="compare-panel">
              <h4>版本 {{ comparingVersions.oldVersion.versionNumber }} ({{ formatDate(comparingVersions.oldVersion.createdAt) }})</h4>
              <div class="compare-text" v-html="comparingVersions.oldContent"></div>
            </div>
            <div class="compare-panel">
              <h4>版本 {{ comparingVersions.newVersion.versionNumber }} ({{ formatDate(comparingVersions.newVersion.createdAt) }})</h4>
              <div class="compare-text" v-html="comparingVersions.newContent"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { articleApi, type ArticleVersion } from '@/api/article'

export default defineComponent({
  name: 'ArticleHistory',
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    // 响应式数据
    const loading = ref(true)
    const error = ref<string | null>(null)
    const versions = ref<ArticleVersion[]>([])
    const articleTitle = ref<string>('')
    const showCompareMode = ref(false)
    const selectedVersions = ref<number[]>([])
    const sortOrder = ref<'asc' | 'desc'>('desc')
    const viewingVersion = ref<ArticleVersion | null>(null)
    const viewingVersionContent = ref<string>('')
    const comparingVersions = ref<{
      oldVersion: ArticleVersion
      newVersion: ArticleVersion
      oldContent: string
      newContent: string
    } | null>(null)
    
    // 计算属性
    const sortedVersions = computed(() => {
      const sorted = [...versions.value]
      return sorted.sort((a, b) => {
        const aTime = new Date(a.createdAt).getTime()
        const bTime = new Date(b.createdAt).getTime()
        return sortOrder.value === 'desc' ? bTime - aTime : aTime - bTime
      })
    })
    
    // 方法
    const loadVersions = async () => {
      try {
        loading.value = true
        error.value = null
        
        const title = route.params.title as string
        articleTitle.value = decodeURIComponent(title)
        
        // 获取文章ID
        const articleId = await articleApi.getArticleIdByTitle(title)
        
        // 获取版本历史
        const versionsResponse = await articleApi.getArticleVersions(articleId)
        versions.value = versionsResponse.versions
        
      } catch (err) {
        error.value = err instanceof Error ? err.message : '加载失败'
      } finally {
        loading.value = false
      }
    }
    
    const goBack = () => {
      router.push({ name: 'ArticleDetail', params: { title: route.params.title } })
    }
    
    const handleVersionClick = (version: ArticleVersion) => {
      if (showCompareMode.value) {
        if (selectedVersions.value.includes(version.versionId)) {
          // 取消选择
          selectedVersions.value = selectedVersions.value.filter(id => id !== version.versionId)
        } else if (selectedVersions.value.length < 2) {
          // 选择版本
          selectedVersions.value.push(version.versionId)
        }
      } else {
        viewVersion(version)
      }
    }
    
    const viewVersion = async (version: ArticleVersion) => {
      viewingVersion.value = version
      viewingVersionContent.value = ''
      
      try {
        const title = route.params.title as string
        const articleId = await articleApi.getArticleIdByTitle(title)
        const response = await articleApi.getVersionContent(articleId, version.versionNumber)
        viewingVersionContent.value = response.content
      } catch (err) {
        viewingVersionContent.value = '<p class="error">加载版本内容失败</p>'
      }
    }
    
    const closeViewer = () => {
      viewingVersion.value = null
      viewingVersionContent.value = ''
    }
    
    const compareVersions = async () => {
      if (selectedVersions.value.length !== 2) return
      
      try {
        const [oldVersionId, newVersionId] = selectedVersions.value.sort((a, b) => a - b)
        
        const oldVersion = versions.value.find(v => v.versionId === oldVersionId)!
        const newVersion = versions.value.find(v => v.versionId === newVersionId)!
        
        const title = route.params.title as string
        const articleId = await articleApi.getArticleIdByTitle(title)
        
        const [oldContentResponse, newContentResponse] = await Promise.all([
          articleApi.getVersionContent(articleId, oldVersion.versionNumber),
          articleApi.getVersionContent(articleId, newVersion.versionNumber)
        ])
        
        comparingVersions.value = {
          oldVersion,
          newVersion,
          oldContent: oldContentResponse.content,
          newContent: newContentResponse.content
        }
        
      } catch (err) {
        alert('对比失败: ' + (err instanceof Error ? err.message : '未知错误'))
      }
    }
    
    const closeCompare = () => {
      comparingVersions.value = null
    }
    
    const restoreVersion = async (version: ArticleVersion) => {
      if (!confirm(`确定要恢复到版本 ${version.versionNumber} 吗？这将创建一个新的版本。`)) {
        return
      }
      
      try {
        const title = route.params.title as string
        const articleId = await articleApi.getArticleIdByTitle(title)
        
        // 使用当前用户或默认作者
        const author = version.author || '用户'
        
        await articleApi.restoreToVersion(articleId, version.versionNumber, author)
        alert('版本恢复成功！已创建新版本。')
        
        // 重新加载版本列表
        loadVersions()
      } catch (err) {
        alert('恢复失败: ' + (err instanceof Error ? err.message : '未知错误'))
      }
    }
    
    const isLatestVersion = (version: ArticleVersion) => {
      return version.versionNumber === Math.max(...versions.value.map(v => v.versionNumber))
    }
    
    const getVersionTypeClass = (storageType: string) => {
      switch (storageType) {
        case 'FULL_CONTENT': return 'full'
        case 'DIFF_FROM_BASE': return 'diff-base'
        case 'DIFF_FROM_PREV': return 'diff-prev'
        default: return 'unknown'
      }
    }
    
    const getVersionTypeText = (storageType: string) => {
      switch (storageType) {
        case 'FULL_CONTENT': return '完整版本'
        case 'DIFF_FROM_BASE': return '基于基础版本的差异'
        case 'DIFF_FROM_PREV': return '基于上一版本的差异'
        default: return '未知类型'
      }
    }
    
    const formatDate = (dateString: string) => {
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    const formatSize = (bytes: number) => {
      if (bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }
    
    // 生命周期
    onMounted(() => {
      loadVersions()
    })
    
    return {
      loading,
      error,
      versions,
      articleTitle,
      showCompareMode,
      selectedVersions,
      sortOrder,
      viewingVersion,
      viewingVersionContent,
      comparingVersions,
      sortedVersions,
      loadVersions,
      goBack,
      handleVersionClick,
      viewVersion,
      closeViewer,
      compareVersions,
      closeCompare,
      restoreVersion,
      isLatestVersion,
      getVersionTypeClass,
      getVersionTypeText,
      formatDate,
      formatSize
    }
  }
})
</script>

<style scoped>
.article-history-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: #f8f9fa;
  min-height: 100vh;
}

.page-header {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #6c757d;
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.3s;
}

.back-button:hover {
  background: #5a6268;
}

.header-info {
  flex: 1;
}

.page-title {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.page-subtitle {
  margin: 5px 0 0;
  color: #666;
  font-size: 14px;
}

.loading-container, .error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  text-align: center;
  color: #dc3545;
}

.error-message i {
  font-size: 48px;
  margin-bottom: 16px;
}

.retry-button {
  background: #007bff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  margin-top: 16px;
}

.history-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
}

.compare-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #6c757d;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.3s;
}

.compare-toggle:hover,
.compare-toggle.active {
  background: #007bff;
}

.sort-select {
  padding: 8px 12px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  background: white;
}

.compare-hint {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: #d4edda;
  color: #155724;
  font-size: 14px;
}

.compare-button {
  background: #28a745;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.versions-list {
  padding: 20px;
}

.version-item {
  border: 1px solid #dee2e6;
  border-radius: 8px;
  margin-bottom: 16px;
  padding: 16px;
  transition: all 0.3s;
  cursor: pointer;
  position: relative;
}

.version-item:hover {
  border-color: #007bff;
  box-shadow: 0 2px 8px rgba(0,123,255,0.15);
}

.version-item.selected {
  border-color: #007bff;
  background: #e3f2fd;
}

.version-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.version-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.version-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.version-number {
  font-size: 18px;
  font-weight: bold;
  color: #007bff;
}

.version-type {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.version-type.full {
  background: #d4edda;
  color: #155724;
}

.version-type.diff-base {
  background: #fff3cd;
  color: #856404;
}

.version-type.diff-prev {
  background: #f8d7da;
  color: #721c24;
}

.version-badge {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
}

.version-badge.initial {
  background: #e2e3e5;
  color: #495057;
}

.version-badge.latest {
  background: #d4edda;
  color: #155724;
}

.version-actions {
  display: flex;
  gap: 8px;
}

.action-button {
  width: 32px;
  height: 32px;
  border: 1px solid #dee2e6;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.action-button:hover {
  background: #f8f9fa;
}

.action-button.view {
  color: #007bff;
}

.action-button.restore {
  color: #28a745;
}

.version-details {
  color: #666;
  font-size: 14px;
}

.version-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 8px;
}

.version-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.change-summary {
  margin: 8px 0;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 4px;
  font-style: italic;
}

.version-stats {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.compression {
  color: #6c757d;
}

.archived {
  color: #dc3545;
}

.compressed {
  color: #28a745;
  font-size: 12px;
}

/* 版本查看器样式 */
.version-viewer-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.version-viewer {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 800px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.viewer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #dee2e6;
}

.viewer-header h3 {
  margin: 0;
  color: #333;
}

.close-button {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6c757d;
}

.close-button:hover {
  color: #333;
}

.viewer-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.content-display {
  line-height: 1.6;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
}

/* 版本对比器样式 */
.version-compare-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.version-compare {
  background: white;
  border-radius: 8px;
  width: 95%;
  max-width: 1200px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.compare-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #dee2e6;
}

.compare-header h3 {
  margin: 0;
  color: #333;
}

.compare-content {
  flex: 1;
  overflow: hidden;
}

.compare-panels {
  display: flex;
  height: 100%;
}

.compare-panel {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.compare-panel:first-child {
  border-right: 1px solid #dee2e6;
}

.compare-panel h4 {
  margin: 0 0 16px;
  color: #333;
  font-size: 16px;
}

.compare-text {
  line-height: 1.6;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .article-history-page {
    padding: 10px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .version-meta {
    flex-direction: column;
    gap: 8px;
  }
  
  .compare-panels {
    flex-direction: column;
  }
  
  .compare-panel:first-child {
    border-right: none;
    border-bottom: 1px solid #dee2e6;
  }
}
</style>
