<template>
  <div class="recent-updates">
    <h2 class="section-title">📝 最近更新</h2>
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>
    <div v-else-if="error" class="error-container">
      <span>❌ 加载失败: {{ error }}</span>
    </div>
    <div v-else class="updates-list">
      <div 
        v-for="article in recentArticles" 
        :key="article.articleId"
        class="update-item"
        @click="navigateToArticle(article.articleId.toString())"
      >
        <div class="update-icon">{{ getCategoryIcon(article.category) }}</div>
        <div class="update-content">
          <span class="update-time">{{ formatDateTime(article.updateTime || article.publishDate) }}</span>
          <span class="update-title">{{ article.title }}</span>
          <span class="update-author">{{ article.author }}</span>
          <span v-if="article.category" class="update-category">{{ article.category }}</span>
        </div>
        <div class="update-stats">
          <span class="stat-item">👁 {{ article.views }}</span>
          <span class="stat-item">👍 {{ article.likes }}</span>
        </div>
      </div>
    </div>
    <div v-if="!loading && !error && recentArticles.length === 0" class="empty-container">
      <span>📝 暂无最近更新</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi, type Article } from '../api/article'

const router = useRouter()
const recentArticles = ref<Article[]>([])
const loading = ref(true)
const error = ref('')

const navigateToArticle = (articleId: string) => {
  router.push(`/article/${articleId}`)
}

const getCategoryIcon = (category: string): string => {
  const iconMap: Record<string, string> = {
    '技术': '💻',
    '环保': '🌱',
    '教育': '📚',
    '健康': '🏥',
    '学术研究': '🔬',
    '文化历史': '🏛️',
    '艺术人文': '🎭',
    '科技创新': '🚀',
    '其他': '📄'
  }
  return iconMap[category] || '📝'
}

const formatDateTime = (dateString: string): string => {
  try {
    const date = new Date(dateString)
    const now = new Date()
    const diffMs = now.getTime() - date.getTime()
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
    
    if (diffDays === 0) {
      return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    } else if (diffDays === 1) {
      return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    } else if (diffDays < 7) {
      return `${diffDays}天前`
    } else {
      return date.toLocaleDateString('zh-CN')
    }
  } catch {
    return '未知时间'
  }
}

const loadRecentArticles = async () => {
  try {
    loading.value = true
    error.value = ''
    
    // 获取最新文章（按更新时间排序）
    const response = await articleApi.getLatestArticles(8)
    recentArticles.value = response || []
  } catch (err) {
    console.error('加载最新文章失败:', err)
    error.value = '无法加载文章数据'
    recentArticles.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRecentArticles()
})
</script>

<style scoped>
.section-title {
  font-size: 1.5rem;
  margin-top: 0;
  margin-bottom: 24px;
  color: #1a202c;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 最近更新 */
.recent-updates {
  margin-top: 50px;
}

.updates-list {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(226, 232, 240, 0.8);
}

.update-item {
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.update-item:hover {
  background-color: #f8fafc;
}

.update-item:last-child {
  border-bottom: none;
}

.loading-container, .error-container, .empty-container {
  text-align: center;
  padding: 40px 20px;
  color: #6b7280;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f3f4f6;
  border-top: 3px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 12px;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.update-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.update-category {
  font-size: 0.75rem;
  color: #6b7280;
  background: #f3f4f6;
  padding: 2px 6px;
  border-radius: 4px;
  align-self: flex-start;
}

.update-stats {
  display: flex;
  flex-direction: column;
  gap: 2px;
  text-align: right;
}

.stat-item {
  font-size: 0.75rem;
  color: #6b7280;
}

.update-icon {
  font-size: 1.2rem;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.update-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.update-time {
  color: #a0aec0;
  font-size: 0.8rem;
  font-weight: 500;
}

.update-title {
  color: #1a202c;
  font-weight: 500;
  font-size: 0.95rem;
}

.update-author {
  color: #667eea;
  font-size: 0.85rem;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .update-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .update-content {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .update-item {
    padding: 16px 20px;
  }
}
</style>
