<template>
  <div class="article-history-page">
    <!-- 页面头部 -->
    <div class="container">
    <header class="page-header">
      <div class="page-header-content">
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
          <div v-if="viewingVersionContent" class="content-display">{{ viewingVersionContent }}</div>
          <div v-else class="loading-content">
            <div class="loading-spinner"></div>
            <p>正在加载版本内容...</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 版本对比器 -->
    <div v-if="comparingVersions || comparingLoading" class="version-compare-overlay" @click="closeCompare">
        <div class="version-compare-container" @click.stop>
          <div v-if="comparingLoading" class="loading-container">
            <div class="loading-spinner"></div>
            <p>正在加载版本内容...</p>
          </div>

          <div v-else-if="comparingVersions" class="modern-diff-container">
            <!-- 现代化头部 -->
            <div class="diff-header">
              <div class="header-left">
                <div class="header-icon">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
                  </svg>
                </div>
                <div class="diff-header-content">
                  <h2 class="diff-title">版本对比</h2>
                  <div class="version-info">
                    <span class="version-tag old-version">
                      v{{ comparingVersions.oldVersion.versionNumber }}
                    </span>
                    <svg class="arrow-icon" width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                      <path d="M7.28 3.22a.75.75 0 0 0-1.06 1.06L9.94 8l-3.72 3.72a.75.75 0 1 0 1.06 1.06L12 8.28a.75.75 0 0 0 0-1.06L7.28 3.22Z"/>
                    </svg>
                    <span class="version-tag new-version">
                      v{{ comparingVersions.newVersion.versionNumber }}
                    </span>
                  </div>
                </div>
              </div>
              
              <div class="header-actions">
                <div class="action-group">
                  <button 
                    @click="viewMode = viewMode === 'line-by-line' ? 'side-by-side' : 'line-by-line'"
                    class="action-btn"
                    :class="{ active: viewMode === 'side-by-side' }"
                  >
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                      <path d="M1 1.75A.75.75 0 0 1 1.75 1h4.5a.75.75 0 0 1 0 1.5h-4.5A.75.75 0 0 1 1 1.75Zm0 5A.75.75 0 0 1 1.75 6h4.5a.75.75 0 0 1 0 1.5h-4.5A.75.75 0 0 1 1 6.75Zm0 5A.75.75 0 0 1 1.75 11h4.5a.75.75 0 0 1 0 1.5h-4.5a.75.75 0 0 1-.75-.75ZM9.75 1a.75.75 0 0 0 0 1.5h4.5a.75.75 0 0 0 0-1.5h-4.5ZM9 6.75A.75.75 0 0 1 9.75 6h4.5a.75.75 0 0 1 0 1.5h-4.5A.75.75 0 0 1 9 6.75ZM9.75 11a.75.75 0 0 0 0 1.5h4.5a.75.75 0 0 0 0-1.5h-4.5Z"/>
                    </svg>
                    {{ viewMode === 'line-by-line' ? '分屏' : '统一' }}
                  </button>
                  
                  <button 
                    @click="diffTheme = diffTheme === 'light' ? 'dark' : 'light'"
                    class="action-btn"
                    :class="{ active: diffTheme === 'dark' }"
                  >
                    <svg v-if="diffTheme === 'light'" width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                      <path d="M8 4a4 4 0 1 0 0 8 4 4 0 0 0 0-8ZM2.5 8a5.5 5.5 0 1 1 11 0 5.5 5.5 0 0 1-11 0Z"/>
                    </svg>
                    <svg v-else width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                      <path d="M6 .278a.768.768 0 0 1 .08.858 7.208 7.208 0 0 0-.878 3.46c0 4.021 3.278 7.277 7.318 7.277.527 0 1.04-.055 1.533-.16a.787.787 0 0 1 .81.316.733.733 0 0 1-.031.893A8.349 8.349 0 0 1 8.344 16C3.734 16 0 12.286 0 7.71 0 4.266 2.114 1.312 5.124.06A.752.752 0 0 1 6 .278Z"/>
                    </svg>
                    {{ diffTheme === 'light' ? '暗色' : '亮色' }}
                  </button>
                </div>
                
                <button @click="closeCompare" class="close-btn">
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                    <path d="M2.146 2.854a.5.5 0 1 1 .708-.708L8 7.293l5.146-5.147a.5.5 0 0 1 .708.708L8.707 8l5.147 5.146a.5.5 0 0 1-.708.708L8 8.707l-5.146 5.147a.5.5 0 0 1-.708-.708L7.293 8 2.146 2.854Z"/>
                  </svg>
                </button>
              </div>
            </div>

            <!-- 代码差异显示区域 -->
            <div class="diff-content">
              <code-diff
                v-if="controllerversion"
                :old-string="comparingVersions.oldContent"
                :new-string="comparingVersions.newContent"
                :output-format="viewMode"
                :theme="diffTheme"
                :highlight="true"
                :diffStyle="'word'"
                :filename="`版本 ${comparingVersions.oldVersion.versionNumber}`"
                :newFilename="`版本 ${comparingVersions.newVersion.versionNumber}`"
                class="modern-code-diff"
              />
            </div>
          </div>
        </div>
    </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { articleApi, type ArticleVersion } from '@/api/article'

export default defineComponent({
  name: 'ArticleHistory',

  setup() {
    // 视图控制
    const viewMode = ref<'line-by-line' | 'side-by-side'>('line-by-line'); // 修改为 split/unified
    // 添加对比加载状态
    const comparingLoading = ref(false)
    const controllerversion = ref(false)
    const diffTheme =ref<'light' | 'dark'>('light')

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
        
         // 清空选择
        const tempSelected = [...selectedVersions.value]
        showCompareMode.value = false;
        selectedVersions.value = [];
        comparingLoading.value = true;

        const [oldVersionId, newVersionId] = tempSelected.sort((a, b) => a - b)
        
        const oldVersion = versions.value.find(v => v.versionId === oldVersionId)!
        const newVersion = versions.value.find(v => v.versionId === newVersionId)!
        if (!oldVersion || !newVersion) {
          throw new Error('无法找到选定的版本');
        }
        const title = route.params.title as string
        const articleId = await articleApi.getArticleIdByTitle(title)
        
        const [oldContentResponse, newContentResponse] = await Promise.all([
          articleApi.getVersionContent(articleId, oldVersion.versionNumber),
          articleApi.getVersionContent(articleId, newVersion.versionNumber)
        ])
        console.log("oldContentResponse",oldContentResponse.content)
        console.log("newContentResponse",newContentResponse.content)

        comparingVersions.value = {
          oldVersion,
          newVersion,
          oldContent: oldContentResponse.content,
          newContent: newContentResponse.content
        }
        
      } catch (err) {
        alert('对比失败: ' + (err instanceof Error ? err.message : '未知错误'))
      }finally{
        comparingLoading.value = false
        controllerversion.value = true
      }
    }
    
    const closeCompare = () => {
      comparingVersions.value = null
      selectedVersions.value = []
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

    // 监听对比模式变化，退出时清除选中状态
    watch(showCompareMode, (newValue) => {
      if (!newValue) {
        // 退出对比模式时清除选中状态
        selectedVersions.value = []
      }
    })
    
    return {
      diffTheme,
      controllerversion,
      viewMode,
      comparingLoading,
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

.article-diff-container {
  /* 保留作为备用，但被现代化样式替代 */
  display: none;
}

.header, .controls {
  /* 保留作为备用，但被现代化样式替代 */
  display: none;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .article-history-page {
    max-width: 100%;
    padding: 0 16px;
  }
  
  .version-compare-container {
    width: 98vw;
    height: 95vh;
  }
  
  .modern-diff-container {
    border-radius: 12px;
  }
  
  .diff-header {
    padding: 20px 24px;
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .header-left {
    width: 100%;
  }
  
  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .diff-title {
    font-size: 1.25rem;
  }
  
  .version-info {
    margin-top: 4px;
  }
}

@media (max-width: 768px) {
  .article-history-page {
    padding: 0 12px;
  }
  
  .version-compare-container {
    width: 100vw;
    height: 100vh;
  }
  
  .modern-diff-container {
    border-radius: 0;
  }
  
  .diff-header {
    padding: 16px 20px;
  }
  
  .header-icon {
    width: 40px;
    height: 40px;
  }
  
  .diff-title {
    font-size: 1.125rem;
  }
  
  .version-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .arrow-icon {
    transform: rotate(90deg);
  }
  
  .action-group {
    flex-direction: column;
    width: 100%;
  }
  
  .action-btn {
    justify-content: center;
    padding: 12px;
  }
  
  :deep(.d2h-code-line) {
    font-size: 12px;
    padding: 2px 12px;
  }
  
  :deep(.d2h-code-side-line) {
    padding: 2px 8px;
    min-width: 50px;
  }
  
  :deep(.d2h-file-header) {
    padding: 12px 16px;
    font-size: 0.8rem;
  }
  
  .diff-header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .page-title {
    font-size: 1.5rem;
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
}

@media (max-width: 480px) {
  .article-history-page {
    padding: 10px 8px;
  }
  
  .diff-header {
    padding: 12px 16px;
  }
  
  .header-icon {
    width: 36px;
    height: 36px;
  }
  
  .diff-title {
    font-size: 1rem;
  }
  
  .version-tag {
    padding: 4px 12px;
    font-size: 0.8rem;
  }
  
  .action-btn {
    padding: 10px;
    font-size: 0.8rem;
  }
  
  .close-btn {
    width: 36px;
    height: 36px;
  }
  
  :deep(.d2h-code-line) {
    font-size: 11px;
    padding: 1px 8px;
  }
  
  :deep(.d2h-file-header) {
    padding: 8px 12px;
    font-size: 0.75rem;
  }
  
  .page-header,
  .history-content {
    border-radius: 8px;
    padding: 16px;
  }
  
  .versions-list {
    padding: 16px;
  }
  
  .version-item {
    padding: 16px;
  }
  
  .version-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .version-actions {
    align-self: flex-end;
  }
  
  .back-button {
    padding: 8px 12px;
    font-size: 0.85rem;
  }
}

/* 现代化代码差异显示样式 */
:deep(.d2h-wrapper) {
  border: none;
  border-radius: 0;
  background: #ffffff;
  font-family: 'JetBrains Mono', 'Fira Code', 'SF Mono', 'Monaco', 'Cascadia Code', 'Roboto Mono', monospace;
  height: 100%;
  overflow: hidden;
  box-shadow: none;
}

:deep(.d2h-file-header) {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border: none;
  border-bottom: 1px solid #e2e8f0;
  padding: 16px 24px;
  color: #475569;
  font-weight: 600;
  font-size: 0.875rem;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif;
}

:deep(.d2h-file-wrapper) {
  border: none;
  border-radius: 0;
}

:deep(.d2h-code-wrapper) {
  border: none;
  max-height: calc(90vh - 200px);
  overflow-y: auto;
  background: #ffffff;
}

:deep(.d2h-code-wrapper::-webkit-scrollbar) {
  width: 8px;
}

:deep(.d2h-code-wrapper::-webkit-scrollbar-track) {
  background: #f1f5f9;
}

:deep(.d2h-code-wrapper::-webkit-scrollbar-thumb) {
  background: #cbd5e1;
  border-radius: 4px;
}

:deep(.d2h-code-wrapper::-webkit-scrollbar-thumb:hover) {
  background: #94a3b8;
}

:deep(.d2h-code-line) {
  font-family: 'JetBrains Mono', 'Fira Code', 'SF Mono', 'Monaco', 'Cascadia Code', 'Roboto Mono', monospace;
  font-size: 14px;
  line-height: 1.5;
  color: #1e293b;
  padding: 2px 16px;
  border: none;
  background: transparent;
  font-feature-settings: 'liga' 1, 'calt' 1;
}

:deep(.d2h-code-side-line) {
  color: #94a3b8;
  background: #f8fafc;
  font-weight: 500;
  text-align: center;
  padding: 2px 12px;
  min-width: 60px;
  border-right: 1px solid #e2e8f0;
  font-feature-settings: 'tnum';
  user-select: none;
}

:deep(.d2h-del) {
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  border-left: 3px solid #ef4444;
  position: relative;
}

:deep(.d2h-del)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

:deep(.d2h-ins) {
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  border-left: 3px solid #22c55e;
  position: relative;
}

:deep(.d2h-ins)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(135deg, #22c55e, #16a34a);
}

:deep(.d2h-info) {
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  color: #1e40af;
  font-size: 13px;
  font-weight: 600;
  padding: 12px 24px;
  border: none;
  border-left: 3px solid #3b82f6;
  margin: 8px 0;
  border-radius: 0 8px 8px 0;
}

:deep(.d2h-code-line-ctn) {
  word-break: break-all;
  white-space: pre-wrap;
}

/* 深色主题的现代化样式 */
:deep(.d2h-dark .d2h-wrapper) {
  background: #0f172a;
  color: #e2e8f0;
}

:deep(.d2h-dark .d2h-file-header) {
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  color: #e2e8f0;
  border-bottom-color: #334155;
}

:deep(.d2h-dark .d2h-code-line) {
  color: #e2e8f0;
  background: transparent;
}

:deep(.d2h-dark .d2h-code-side-line) {
  background: #1e293b;
  color: #64748b;
  border-right-color: #334155;
}

:deep(.d2h-dark .d2h-del) {
  background: linear-gradient(135deg, rgba(220, 38, 38, 0.15) 0%, rgba(239, 68, 68, 0.1) 100%);
  border-left-color: #ef4444;
}

:deep(.d2h-dark .d2h-ins) {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.15) 0%, rgba(22, 163, 74, 0.1) 100%);
  border-left-color: #22c55e;
}

:deep(.d2h-dark .d2h-info) {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(30, 64, 175, 0.1) 100%);
  color: #60a5fa;
  border-left-color: #3b82f6;
}

:deep(.d2h-dark .d2h-code-wrapper::-webkit-scrollbar-track) {
  background: #1e293b;
}

:deep(.d2h-dark .d2h-code-wrapper::-webkit-scrollbar-thumb) {
  background: #475569;
}

:deep(.d2h-dark .d2h-code-wrapper::-webkit-scrollbar-thumb:hover) {
  background: #64748b;
}


.article-history-page {
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

.page-header {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 32px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.page-header-content {
  display: flex;
  align-items: center;
  gap: 24px;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.back-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.header-info {
  flex: 1;
}

.page-title {
  margin: 0;
  font-size: 1.75rem;
  color: #1a202c;
  font-weight: 600;
  line-height: 1.3;
}

.page-subtitle {
  margin: 8px 0 0;
  color: #718096;
  font-size: 1rem;
}

.loading-container, .error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top: 4px solid #667eea;
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
  color: #e53e3e;
}

.error-message i {
  font-size: 3rem;
  margin-bottom: 16px;
  color: #fc8181;
}

.retry-button {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 25px;
  cursor: pointer;
  margin-top: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.retry-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.history-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.compare-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.compare-toggle:hover,
.compare-toggle.active {
  background: linear-gradient(135deg, #5a67d8, #667eea);
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.2);
}

.sort-select {
  padding: 10px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  color: #4a5568;
  font-size: 0.9rem;
  outline: none;
  transition: all 0.3s ease;
}

.sort-select:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.compare-hint {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  background: linear-gradient(135deg, #f0fff4, #c6f6d5);
  color: #2f855a;
  font-size: 0.9rem;
  border-left: 4px solid #38a169;
}

.compare-button {
  background: linear-gradient(135deg, #38a169, #2f855a);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.compare-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(56, 161, 105, 0.3);
}

.versions-list {
  padding: 24px;
  height: 800px;
  min-height: 600px;
  max-height: 1000px;
  overflow-y: scroll;
}

.version-item {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  margin-bottom: 16px;
  padding: 20px;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  background: white;
}

.version-item:hover {
  border-color: #667eea;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.1);
  transform: translateY(-2px);
}

.version-item.selected {
  border-color: #667eea;
  background: linear-gradient(135deg, #f7fafc, #edf2f7);
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
}

.version-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.version-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.version-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.version-number {
  font-size: 1.25rem;
  font-weight: 600;
  color: #667eea;
}

.version-type {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.version-type.full {
  background: linear-gradient(135deg, #f0fff4, #c6f6d5);
  color: #2f855a;
}

.version-type.diff-base {
  background: linear-gradient(135deg, #fffbeb, #fef3c7);
  color: #d69e2e;
}

.version-type.diff-prev {
  background: linear-gradient(135deg, #fef5e7, #fed7aa);
  color: #dd6b20;
}

.version-badge {
  padding: 4px 10px;
  border-radius: 15px;
  font-size: 0.75rem;
  font-weight: 500;
}

.version-badge.initial {
  background: linear-gradient(135deg, #f7fafc, #edf2f7);
  color: #2c5db3;
}

.version-badge.latest {
  background: linear-gradient(135deg, #f0fff4, #c6f6d5);
  color: #2f855a;
}

.version-actions {
  display: flex;
  gap: 8px;
}

.action-button {
  width: 36px;
  height: 36px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.action-button:hover {
  background: #f7fafc;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.action-button.view {
  color: #667eea;
}

.action-button.view:hover {
  background: linear-gradient(135deg, #f7fafc, #edf2f7);
  color: #5a67d8;
}

.action-button.restore {
  color: #38a169;
}

.action-button.restore:hover {
  background: linear-gradient(135deg, #f0fff4, #c6f6d5);
  color: #2f855a;
}

.version-details {
  color: #718096;
  font-size: 0.9rem;
}

.version-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 12px;
}

.version-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.change-summary {
  margin: 12px 0;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f7fafc, #edf2f7);
  border-radius: 8px;
  font-style: italic;
  border-left: 3px solid #667eea;
}

.version-stats {
  display: flex;
  gap: 16px;
  margin-top: 12px;
}

.compression {
  color: #718096;
}

.archived {
  color: #e53e3e;
}

.compressed {
  color: #38a169;
  font-size: 0.85rem;
}

/* 版本查看器样式 */
.version-viewer-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.version-viewer {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 900px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.viewer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
  border-radius: 12px 12px 0 0;
}

.viewer-header h3 {
  margin: 0;
  color: #1a202c;
  font-size: 1.25rem;
  font-weight: 600;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #718096;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.close-button:hover {
  background: #e2e8f0;
  color: #4a5568;
}

.viewer-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.content-display {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.5;
  color: #374151;
  white-space: pre-wrap;
  word-wrap: break-word;
  background: #fafafa;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  max-height: 60vh;
  overflow-y: auto;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
}

/* 现代化版本对比器样式 */
.version-compare-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: overlay-fade-in 0.2s ease-out;
}

@keyframes overlay-fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.version-compare-container {
  width: 95vw;
  max-width: 1600px;
  height: 90vh;
  max-height: 900px;
  animation: modal-slide-up 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes modal-slide-up {
  from {
    opacity: 0;
    transform: translate3d(0, 32px, 0) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translate3d(0, 0, 0) scale(1);
  }
}

.modern-diff-container {
  width: 100%;
  height: 100%;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 
    0 32px 64px rgba(15, 23, 42, 0.2),
    0 0 0 1px rgba(148, 163, 184, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 现代化头部样式 */
.diff-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 32px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 1px solid #e2e8f0;
  position: relative;
}

.diff-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, #94a3b8, transparent);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.3);
}

.diff-header-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.diff-title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #0f172a;
  letter-spacing: -0.025em;
}

.version-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.version-tag {
  padding: 6px 16px;
  border-radius: 24px;
  font-size: 0.875rem;
  font-weight: 500;
  font-feature-settings: 'tnum';
}

.old-version {
  background: linear-gradient(135deg, #fef3c7, #fed7aa);
  color: #92400e;
  border: 1px solid #fbbf24;
}

.new-version {
  background: linear-gradient(135deg, #dcfce7, #bbf7d0);
  color: #166534;
  border: 1px solid #22c55e;
}

.arrow-icon {
  color: #64748b;
  opacity: 0.7;
}

/* 操作按钮组 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-group {
  display: flex;
  gap: 8px;
  padding: 4px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 
    0 1px 3px rgba(15, 23, 42, 0.1),
    0 0 0 1px rgba(148, 163, 184, 0.1);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: none;
  background: transparent;
  color: #64748b;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.15s ease;
}

.action-btn:hover {
  background: #f8fafc;
  color: #475569;
}

.action-btn.active {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

.close-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: #ffffff;
  color: #64748b;
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s ease;
  box-shadow: 
    0 1px 3px rgba(15, 23, 42, 0.1),
    0 0 0 1px rgba(148, 163, 184, 0.1);
}

.close-btn:hover {
  background: #f8fafc;
  color: #475569;
  transform: scale(1.05);
}

/* 内容区域 */
.diff-content {
  flex: 1;
  padding: 0;
  background: #ffffff;
  overflow: hidden;
  position: relative;
}

.modern-code-diff {
  height: 100%;
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .article-history-page {
    max-width: 100%;
    padding: 0 16px;
  }
}

@media (max-width: 768px) {
  .article-history-page {
    padding: 0 12px;
  }
  
  .page-header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .page-title {
    font-size: 1.5rem;
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
    border-bottom: 1px solid #e2e8f0;
  }
}

@media (max-width: 480px) {
  .article-history-page {
    padding: 10px 8px;
  }
  
  .page-header,
  .history-content {
    border-radius: 8px;
    padding: 16px;
  }
  
  .versions-list {
    padding: 16px;
  }
  
  .version-item {
    padding: 16px;
  }
  
  .version-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .version-actions {
    align-self: flex-end;
  }
  
  .back-button {
    padding: 8px 12px;
    font-size: 0.85rem;
  }
}
</style>
