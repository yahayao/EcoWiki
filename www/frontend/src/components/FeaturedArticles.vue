<template>
  <div class="featured-articles">
    <h2 class="section-title">🔥 热门内容</h2>
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>
    <div v-else-if="error" class="error-container">
      <span>❌ 加载失败: {{ error }}</span>
    </div>
    <div v-else class="article-grid">
      <div 
        v-for="article in articles" 
        :key="article.articleId"
        class="article-card" 
        @click="navigateToArticle(article.articleId.toString())"
      >
        <div class="article-header">
          <span class="article-category">{{ article.category || '未分类' }}</span>
          <span class="article-stats">👁 {{ article.views }} | 👍 {{ article.likes }}</span>
        </div>
        <h3 class="article-title">{{ article.title }}</h3>
        <p class="article-excerpt">{{ getArticleExcerpt(article.content) }}</p>
        <div class="article-meta">
          <span class="article-author">
            <span class="author-icon">👤</span>
            {{ article.author }}
          </span>
          <span class="article-date">{{ formatDate(article.publishDate) }}</span>
        </div>
        <div v-if="article.tags" class="article-tags">
          <span v-for="tag in getTagArray(article.tags)" :key="tag" class="tag">{{ tag }}</span>
        </div>
      </div>
    </div>
    <div v-if="!loading && !error && articles.length === 0" class="empty-container">
      <span>� 暂无文章</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi, type Article } from '../api/article'
import { wikiParser } from '../utils/wikiParser'

const router = useRouter()
const articles = ref<Article[]>([])
const loading = ref(true)
const error = ref('')

const navigateToArticle = (articleId: string) => {
  router.push(`/article/${articleId}`)
}

const getArticleExcerpt = (content: string): string => {
  if (!content) return '暂无内容...'
  
  // 使用wikiParser提取纯文本摘要
  const htmlContent = wikiParser.parseToHtml(content)
  const excerpt = wikiParser.extractSummary(htmlContent, 120)
  return excerpt || '暂无内容...'
}

const formatDate = (dateString: string): string => {
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN')
  } catch {
    return '未知日期'
  }
}

const getTagArray = (tags: string): string[] => {
  if (!tags) return []
  return tags.split(',').map(tag => tag.trim()).filter(tag => tag.length > 0)
}

const loadPopularArticles = async () => {
  try {
    loading.value = true
    error.value = ''
    
    // 获取热门文章（按点赞数或浏览量排序）
    const response = await articleApi.getPopularArticles(6)
    articles.value = response || []
  } catch (err) {
    console.error('加载热门文章失败:', err)
    error.value = '无法加载文章数据'
    articles.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPopularArticles()
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

.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 50px;
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

.article-stats {
  font-size: 0.75rem;
  color: #6b7280;
}

.article-tags {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tag {
  background: #e5e7eb;
  color: #374151;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: 500;
}

.article-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  padding: 24px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid rgba(226, 232, 240, 0.8);
  position: relative;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  border-color: #667eea;
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.article-category {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.article-rating {
  font-size: 0.85rem;
  color: #f6ad55;
  font-weight: 500;
}

.article-title {
  font-size: 1.2rem;
  margin: 0 0 12px 0;
  color: #1a202c;
  font-weight: 600;
  line-height: 1.4;
}

.article-excerpt {
  color: #718096;
  line-height: 1.6;
  margin-bottom: 16px;
  font-size: 0.95rem;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #a0aec0;
  font-size: 0.85rem;
}

.article-author {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.author-icon {
  font-size: 0.8rem;
}

.article-date {
  color: #cbd5e0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .article-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .article-card {
    padding: 20px;
  }
}
</style>
