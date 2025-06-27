<template>
  <div class="related-articles">
    <div class="section-header">
      <h3 class="section-title">📚 相关推荐</h3>
      <div class="section-actions" v-if="showRefreshButton">
        <button @click="refreshRecommendations" class="refresh-btn" :disabled="loading">
          <span class="refresh-icon" :class="{ spinning: loading }">🔄</span>
          刷新推荐
        </button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading && articles.length === 0" class="loading-state">
      <div class="loading-spinner"></div>
      <p>正在加载相关推荐...</p>
    </div>

    <!-- 推荐文章列表 -->
    <div v-else-if="articles.length > 0" class="related-grid">
      <div 
        v-for="article in articles" 
        :key="article.id"
        class="related-card"
        @click="navigateToArticle(article.id)"
      >
        <div class="related-header">
          <span class="related-category">{{ article.category }}</span>
          <span class="related-rating">⭐ {{ article.rating }}</span>
        </div>
        <h4 class="related-title">{{ article.title }}</h4>
        <p class="related-excerpt">{{ article.excerpt }}</p>
        <div class="related-meta">
          <div class="author-info">
            <span class="author-avatar">{{ article.author.charAt(0).toUpperCase() }}</span>
            <span class="related-author">{{ article.author }}</span>
          </div>
          <span class="related-date">{{ formatDate(article.publishDate) }}</span>
        </div>
        <div class="related-stats">
          <span class="stat-item">
            <span class="stat-icon">👁️</span>
            {{ formatNumber(article.views || 0) }}
          </span>
          <span class="stat-item">
            <span class="stat-icon">👍</span>
            {{ formatNumber(article.likes || 0) }}
          </span>
          <span class="stat-item">
            <span class="stat-icon">⏱️</span>
            {{ article.readTime }}分钟
          </span>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-icon">📚</div>
      <h4>暂无相关推荐</h4>
      <p>我们正在为您寻找更多精彩内容</p>
    </div>

    <!-- 查看更多按钮 -->
    <div v-if="articles.length > 0 && hasMore" class="load-more-section">
      <button @click="loadMore" :disabled="loadingMore" class="load-more-btn">
        {{ loadingMore ? '加载中...' : '查看更多推荐' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'

interface RelatedArticle {
  id: string
  title: string
  excerpt: string
  author: string
  category: string
  rating: number
  publishDate: string
  views?: number
  likes?: number
  readTime: number
}

const props = defineProps<{
  currentArticleId: string
  currentCategory?: string
  currentTags?: string[]
  maxResults?: number
  showRefreshButton?: boolean
}>()

const emit = defineEmits<{
  articleClick: [articleId: string]
}>()

const router = useRouter()

// 状态管理
const articles = ref<RelatedArticle[]>([])
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)

onMounted(() => {
  loadRelatedArticles()
})

// 监听当前文章变化，重新加载推荐
watch(() => props.currentArticleId, () => {
  if (props.currentArticleId) {
    currentPage.value = 1
    hasMore.value = true
    loadRelatedArticles()
  }
})

const loadRelatedArticles = async () => {
  try {
    loading.value = true
    
    // 模拟API调用延迟
    await new Promise(resolve => setTimeout(resolve, 800))
    
    // 模拟基于当前文章的相关推荐算法
    const mockArticles: RelatedArticle[] = generateMockRecommendations()
    
    articles.value = mockArticles.slice(0, props.maxResults || 3)
    hasMore.value = mockArticles.length > (props.maxResults || 3)
    
  } catch (error) {
    console.error('Failed to load related articles:', error)
    articles.value = []
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  try {
    loadingMore.value = true
    currentPage.value++
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 600))
    
    const moreArticles = generateMockRecommendations(currentPage.value)
    articles.value.push(...moreArticles.slice(0, 3))
    
    // 模拟没有更多数据
    if (currentPage.value >= 3) {
      hasMore.value = false
    }
    
  } catch (error) {
    console.error('Failed to load more articles:', error)
  } finally {
    loadingMore.value = false
  }
}

const refreshRecommendations = () => {
  currentPage.value = 1
  hasMore.value = true
  loadRelatedArticles()
}

const navigateToArticle = (articleId: string) => {
  emit('articleClick', articleId)
  router.push(`/article/${articleId}`)
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diffDays = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60 * 24))
  
  if (diffDays === 0) {
    return '今天'
  } else if (diffDays === 1) {
    return '昨天'
  } else if (diffDays < 7) {
    return `${diffDays}天前`
  } else {
    return date.toLocaleDateString('zh-CN', {
      month: 'numeric',
      day: 'numeric'
    })
  }
}

const formatNumber = (num: number) => {
  if (num < 1000) return num.toString()
  if (num < 10000) return (num / 1000).toFixed(1) + 'k'
  return (num / 10000).toFixed(1) + 'w'
}

// 生成模拟推荐数据（实际应该从API获取）
const generateMockRecommendations = (page = 1): RelatedArticle[] => {
  const baseArticles = [
    {
      id: '2',
      title: '量子计算原理与应用前景探析',
      excerpt: '深入浅出地解释量子计算的基本原理，探讨其在密码学、药物研发等领域的应用潜力...',
      author: '量子物理学家',
      category: '科技创新',
      rating: 4.7,
      publishDate: '2025-01-10T14:30:00Z',
      views: 2156,
      likes: 89,
      readTime: 12
    },
    {
      id: '3',
      title: '区块链技术发展现状与挑战',
      excerpt: '全面分析区块链技术的核心特性、当前发展现状以及面临的技术和监管挑战...',
      author: '区块链专家',
      category: '技术分析',
      rating: 4.5,
      publishDate: '2025-01-08T09:15:00Z',
      views: 1834,
      likes: 76,
      readTime: 10
    },
    {
      id: '4',
      title: '5G通信技术与物联网的融合发展',
      excerpt: '深入探讨5G技术如何推动物联网应用的发展，以及两者结合带来的新机遇和挑战...',
      author: '通信工程师',
      category: '通信技术',
      rating: 4.6,
      publishDate: '2025-01-05T16:45:00Z',
      views: 2467,
      likes: 102,
      readTime: 14
    },
    {
      id: '5',
      title: '机器学习算法在金融风控中的应用',
      excerpt: '介绍机器学习在金融风险控制领域的具体应用案例，包括信用评估、欺诈检测等...',
      author: '金融科技专家',
      category: '金融科技',
      rating: 4.4,
      publishDate: '2025-01-03T11:20:00Z',
      views: 1923,
      likes: 67,
      readTime: 11
    },
    {
      id: '6',
      title: '云计算架构设计最佳实践',
      excerpt: '分享云计算系统架构设计的最佳实践，包括可扩展性、可靠性和成本优化等方面...',
      author: '云架构师',
      category: '云计算',
      rating: 4.8,
      publishDate: '2025-01-01T08:30:00Z',
      views: 3102,
      likes: 134,
      readTime: 16
    },
    {
      id: '7',
      title: '数据可视化设计原则与实践',
      excerpt: '探讨有效数据可视化的设计原则，以及如何创建既美观又实用的数据展示图表...',
      author: '数据科学家',
      category: '数据科学',
      rating: 4.3,
      publishDate: '2024-12-28T14:15:00Z',
      views: 1567,
      likes: 45,
      readTime: 9
    }
  ]
  
  // 根据页数返回不同的文章
  const startIndex = (page - 1) * 3
  return baseArticles.slice(startIndex, startIndex + 6)
}
</script>

<style scoped>
.related-articles {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(226, 232, 240, 0.8);
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 24px 0;
  margin-bottom: 24px;
}

.section-title {
  font-size: 1.4rem;
  color: #1a202c;
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-actions {
  display: flex;
  gap: 12px;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.refresh-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.refresh-icon {
  font-size: 0.9rem;
  transition: transform 0.3s ease;
}

.refresh-icon.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 24px;
  color: #718096;
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

.related-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  padding: 0 24px 24px;
}

.related-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.related-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s ease;
}

.related-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border-color: #667eea;
  background: white;
}

.related-card:hover::before {
  transform: scaleX(1);
}

.related-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.related-category {
  background: linear-gradient(135deg, #4299e1, #667eea);
  color: white;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 0.75rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.related-rating {
  font-size: 0.8rem;
  color: #f6ad55;
  font-weight: 600;
}

.related-title {
  font-size: 1.1rem;
  color: #1a202c;
  font-weight: 600;
  margin: 0 0 10px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.related-excerpt {
  color: #718096;
  font-size: 0.9rem;
  line-height: 1.5;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.related-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-avatar {
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: 600;
}

.related-author {
  font-size: 0.85rem;
  color: #4a5568;
  font-weight: 500;
}

.related-date {
  color: #a0aec0;
  font-size: 0.8rem;
}

.related-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #a0aec0;
  font-size: 0.8rem;
  font-weight: 500;
}

.stat-icon {
  font-size: 0.75rem;
}

.empty-state {
  text-align: center;
  padding: 60px 24px;
  color: #718096;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 16px;
}

.empty-state h4 {
  color: #4a5568;
  margin-bottom: 8px;
  font-size: 1.2rem;
}

.empty-state p {
  font-size: 0.95rem;
}

.load-more-section {
  text-align: center;
  padding: 0 24px 24px;
  border-top: 1px solid #f7fafc;
}

.load-more-btn {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
  padding: 12px 24px;
  border-radius: 25px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 16px;
}

.load-more-btn:hover:not(:disabled) {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.load-more-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* 响应式设计 */
@media (min-width: 1400px) {
  .related-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 24px;
    padding: 0 32px 32px;
  }
  
  .related-card {
    padding: 24px;
  }
}

@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 20px 16px 0;
  }
  
  .related-grid {
    grid-template-columns: 1fr;
    padding: 0 16px 20px;
    gap: 16px;
  }
  
  .section-title {
    font-size: 1.2rem;
  }
  
  .refresh-btn {
    padding: 6px 12px;
    font-size: 0.8rem;
  }
}

@media (max-width: 480px) {
  .related-card {
    padding: 16px;
  }
  
  .related-title {
    font-size: 1rem;
  }
  
  .related-excerpt {
    font-size: 0.85rem;
  }
  
  .related-stats {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .stat-item {
    font-size: 0.75rem;
  }
}
</style>
