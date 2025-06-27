<template>
  <div class="article-content">
    <!-- 文章头部信息 -->
    <div class="article-header">
      <div class="article-meta">
        <span class="article-category">{{ article.category }}</span>
        <span class="article-rating">⭐ {{ article.rating }}</span>
      </div>
      <h1 class="article-title">{{ article.title }}</h1>
      <div class="article-info">
        <div class="author-info">
          <span class="author-avatar">👤</span>
          <div class="author-details">
            <span class="author-name">{{ article.author }}</span>
            <span class="author-title">{{ article.authorTitle }}</span>
          </div>
        </div>
        <div class="article-stats">
          <span class="publish-date">📅 {{ formatDate(article.publishDate) }}</span>
          <span class="read-time">⏱️ 约 {{ article.readTime }} 分钟阅读</span>
          <span class="view-count">👁️ {{ article.views }} 次浏览</span>
        </div>
      </div>
    </div>

    <!-- 文章摘要 -->
    <div class="article-summary" v-if="article.summary">
      <h3>📝 内容摘要</h3>
      <p>{{ article.summary }}</p>
    </div>

    <!-- 文章正文 -->
    <div class="article-body">
      <div v-html="article.content"></div>
    </div>

    <!-- 文章标签 -->
    <div class="article-tags" v-if="article.tags && article.tags.length">
      <h4>🏷️ 相关标签</h4>
      <div class="tags-list">
        <span v-for="tag in article.tags" :key="tag" class="tag">{{ tag }}</span>
      </div>
    </div>

    <!-- 文章操作 -->
    <div class="article-actions">
      <button class="action-btn like-btn" :class="{ active: isLiked }" @click="toggleLike">
        <span class="icon">👍</span>
        <span>{{ article.likes || 0 }}</span>
      </button>
      <button class="action-btn share-btn" @click="shareArticle">
        <span class="icon">🔗</span>
        <span>分享</span>
      </button>
      <button class="action-btn bookmark-btn" :class="{ active: isBookmarked }" @click="toggleBookmark">
        <span class="icon">📚</span>
        <span>收藏</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface Article {
  id: string
  title: string
  content: string
  summary?: string
  author: string
  authorTitle: string
  category: string
  rating: number
  publishDate: string
  readTime: number
  views: number
  likes?: number
  tags?: string[]
}

const props = defineProps<{
  article: Article
}>()

const isLiked = ref(false)
const isBookmarked = ref(false)

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const toggleLike = () => {
  isLiked.value = !isLiked.value
  // 这里可以添加API调用来更新点赞状态
}

const toggleBookmark = () => {
  isBookmarked.value = !isBookmarked.value
  // 这里可以添加API调用来更新收藏状态
}

const shareArticle = () => {
  if (navigator.share) {
    navigator.share({
      title: props.article.title,
      text: props.article.summary || props.article.title,
      url: window.location.href
    })
  } else {
    // fallback: 复制链接到剪贴板
    navigator.clipboard.writeText(window.location.href)
    alert('链接已复制到剪贴板')
  }
}
</script>

<style scoped>
.article-content {
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
  padding: 32px 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(226, 232, 240, 0.8);
}

.article-header {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 2px solid #f7fafc;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.article-category {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
}

.article-rating {
  font-size: 0.9rem;
  color: #f6ad55;
  font-weight: 600;
}

.article-title {
  font-size: 2.2rem;
  color: #1a202c;
  font-weight: 700;
  line-height: 1.3;
  margin-bottom: 20px;
}

.article-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 16px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  font-size: 2rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 600;
  color: #1a202c;
  font-size: 1.1rem;
}

.author-title {
  color: #718096;
  font-size: 0.9rem;
}

.article-stats {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: #a0aec0;
  font-size: 0.9rem;
}

.article-summary {
  background: #f8fafc;
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 32px;
  border-left: 4px solid #667eea;
}

.article-summary h3 {
  color: #1a202c;
  margin-bottom: 12px;
  font-size: 1.2rem;
  font-weight: 600;
}

.article-summary p {
  color: #4a5568;
  line-height: 1.7;
  font-size: 1rem;
}

.article-body {
  line-height: 1.8;
  font-size: 1.1rem;
  color: #2d3748;
  margin-bottom: 32px;
}

.article-body :deep(h2) {
  color: #1a202c;
  font-size: 1.6rem;
  font-weight: 600;
  margin: 32px 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #e2e8f0;
}

.article-body :deep(h3) {
  color: #2d3748;
  font-size: 1.4rem;
  font-weight: 600;
  margin: 24px 0 12px 0;
}

.article-body :deep(p) {
  margin-bottom: 16px;
}

.article-body :deep(ul), .article-body :deep(ol) {
  margin: 16px 0;
  padding-left: 24px;
}

.article-body :deep(li) {
  margin-bottom: 8px;
}

.article-body :deep(blockquote) {
  background: #f8fafc;
  border-left: 4px solid #667eea;
  padding: 16px 20px;
  margin: 20px 0;
  font-style: italic;
  color: #4a5568;
}

.article-body :deep(code) {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 0.9rem;
  color: #e53e3e;
}

.article-body :deep(pre) {
  background: #1a202c;
  color: #e2e8f0;
  padding: 20px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 20px 0;
}

.article-tags {
  margin-bottom: 32px;
  padding: 20px;
  background: #fafafa;
  border-radius: 12px;
}

.article-tags h4 {
  color: #1a202c;
  margin-bottom: 12px;
  font-size: 1.1rem;
  font-weight: 600;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  background: white;
  color: #667eea;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 0.85rem;
  font-weight: 500;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.tag:hover {
  background: #667eea;
  color: white;
  cursor: pointer;
}

.article-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  padding-top: 24px;
  border-top: 2px solid #f7fafc;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: 2px solid #e2e8f0;
  background: white;
  border-radius: 25px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #4a5568;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.action-btn.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-color: #667eea;
}

.like-btn:hover:not(.active) {
  border-color: #f6ad55;
  color: #f6ad55;
}

.share-btn:hover {
  border-color: #4299e1;
  color: #4299e1;
}

.bookmark-btn:hover:not(.active) {
  border-color: #48bb78;
  color: #48bb78;
}

.icon {
  font-size: 1.1rem;
}

/* 响应式设计 */
@media (min-width: 1400px) {
  .article-content {
    max-width: 1200px;
    padding: 40px 32px;
  }
}

@media (max-width: 768px) {
  .article-content {
    padding: 24px 16px;
    margin: 0 16px;
  }
  
  .article-title {
    font-size: 1.8rem;
  }
  
  .article-info {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .article-actions {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .action-btn {
    flex: 1;
    min-width: 120px;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .article-title {
    font-size: 1.6rem;
  }
  
  .article-body {
    font-size: 1rem;
  }
}
</style>
