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
        @click="navigateToArticle(article.title)"
      >
        <div class="related-header">
          <span class="related-category">{{ article.category }}</span>
          <span class="related-rating">⭐ {{ article.rating }}</span>
        </div>
        <h4 class="related-title">{{ article.title }}</h4>
        <p class="related-excerpt">{{ article.excerpt }}</p>
        <div class="related-meta">
          <div class="author-info">
            <UserAvatar 
              :username="article.author"
              :avatar-url="article.authorAvatar || ''"
              size="xs"
              shape="circle"
            />
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
import { articleApi, type Article } from '@/api/article'
import toast from '@/utils/toast'
import UserAvatar from '@/components/common/UserAvatar.vue'
import { useAuth } from '@/composables/useAuth'

interface RelatedArticle {
  id: number
  title: string
  excerpt: string
  author: string
  authorAvatar?: string
  category: string
  rating?: number
  publishDate: string
  views?: number
  likes?: number
  readTime?: number
}

const props = defineProps<{
  currentArticleId: number
  currentCategory?: string
  currentTags?: string[]
  maxResults?: number
  showRefreshButton?: boolean
}>()

const emit = defineEmits<{
  articleClick: [articleId: number]
}>()

const router = useRouter()

// 获取用户信息（用于头像显示）
const { user } = useAuth()

// 状态管理
const articles = ref<RelatedArticle[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const currentPage = ref(0) // 添加当前页码状态

// 模拟推荐算法的文章转换
const convertToRelatedArticle = (article: Article): RelatedArticle => {
  return {
    id: article.articleId,
    title: article.title,
    excerpt: article.content.substring(0, 150) + '...',
    author: article.author,
    authorAvatar: article.authorAvatar || '', // 添加头像字段
    category: article.tags.split(',')[0] || '未分类',
    rating: 4 + Math.random(), // 模拟评分
    publishDate: article.publishDate, // 使用正确的字段名
    views: article.views,
    likes: article.likes,
    readTime: Math.ceil(article.content.length / 500) // 估算阅读时间
  }
}

// 初始化
onMounted(() => {
  loadRelatedArticles()
})

// 监听当前文章变化
watch(() => props.currentArticleId, () => {
  if (props.currentArticleId) {
    // 重置状态
    articles.value = []
    currentPage.value = 0
    hasMore.value = true
    loadRelatedArticles()
  }
})

const loadRelatedArticles = async () => {
  loading.value = true
  currentPage.value = 0 // 重置页码
  try {
    // 获取相关文章 - 基于分类、标签等进行推荐
    // 增加页面大小以获得更多候选文章用于过滤
    const response = await articleApi.getArticles(0, (props.maxResults || 6) * 2)
    
    // 过滤掉当前文章，避免推荐自己
    const filteredArticles = response.content.filter(
      (article: Article) => article.articleId !== props.currentArticleId
    )
    
    // 基于分类和标签进行智能排序
    const sortedArticles = filteredArticles.sort((a, b) => {
      let scoreA = 0
      let scoreB = 0
      
      // 相同分类的文章得分更高
      if (props.currentCategory && a.tags.includes(props.currentCategory)) {
        scoreA += 10
      }
      if (props.currentCategory && b.tags.includes(props.currentCategory)) {
        scoreB += 10
      }
      
      // 相同标签的文章得分更高
      if (props.currentTags) {
        const aTagMatches = props.currentTags.filter(tag => a.tags.includes(tag)).length
        const bTagMatches = props.currentTags.filter(tag => b.tags.includes(tag)).length
        scoreA += aTagMatches * 5
        scoreB += bTagMatches * 5
      }
      
      // 按浏览量和点赞数排序
      scoreA += (a.views || 0) * 0.01 + (a.likes || 0) * 0.1
      scoreB += (b.views || 0) * 0.01 + (b.likes || 0) * 0.1
      
      return scoreB - scoreA
    })
    
    // 取前N篇文章
    const selectedArticles = sortedArticles.slice(0, props.maxResults || 6)
    articles.value = selectedArticles.map(convertToRelatedArticle)
    
    // 判断是否还有更多数据
    hasMore.value = filteredArticles.length > (props.maxResults || 6)
  } catch (error) {
    console.error('加载相关文章失败:', error)
    toast.show('加载相关文章失败', '错误', { type: 'error' })
    // 降级到模拟数据
    loadMockData()
  } finally {
    loading.value = false
  }
}

const loadMockData = () => {
  // 降级模拟数据
  articles.value = [
    {
      id: 2,
      title: '深入理解现代Web技术架构',
      excerpt: '随着互联网技术的快速发展，现代Web应用的架构变得越来越复杂和精细。本文将带您深入了解当前主流的Web技术架构模式...',
      author: '技术专家',
      authorAvatar: '', // 添加头像字段（空字符串将显示默认头像）
      category: '技术',
      rating: 4.8,
      publishDate: '2025-01-14T15:30:00Z',
      views: 1580,
      likes: 89,
      readTime: 8
    },
    {
      id: 3,
      title: '可持续发展与绿色技术创新',
      excerpt: '在全球气候变化的背景下，可持续发展已成为各行各业关注的焦点。绿色技术作为实现可持续发展的重要手段...',
      author: '环保专家',
      authorAvatar: '', // 添加头像字段（空字符串将显示默认头像）
      category: '环保',
      rating: 4.6,
      publishDate: '2025-01-13T09:20:00Z',
      views: 2340,
      likes: 156,
      readTime: 6
    }
  ]
}

const loadMore = async () => {
  loadingMore.value = true
  try {
    // 递增页码以获取下一页数据
    currentPage.value++
    
    // 加载更多相关文章，增加页面大小以获得更多候选文章
    const response = await articleApi.getArticles(currentPage.value, 10)
    
    // 过滤掉当前文章和已存在的文章，避免重复
    const existingIds = new Set(articles.value.map(article => article.id))
    let filteredArticles = response.content.filter(
      (article: Article) => article.articleId !== props.currentArticleId && 
                           !existingIds.has(article.articleId)
    )
    
    // 如果当前页没有新文章，尝试下一页
    let retryCount = 0
    while (filteredArticles.length === 0 && retryCount < 3 && response.content.length > 0) {
      currentPage.value++
      const nextResponse = await articleApi.getArticles(currentPage.value, 10)
      if (nextResponse.content.length === 0) break
      
      filteredArticles = nextResponse.content.filter(
        (article: Article) => article.articleId !== props.currentArticleId && 
                             !existingIds.has(article.articleId)
      )
      retryCount++
    }
    
    if (filteredArticles.length > 0) {
      // 基于分类和标签进行智能排序
      const sortedArticles = filteredArticles.sort((a, b) => {
        let scoreA = 0
        let scoreB = 0
        
        // 相同分类的文章得分更高
        if (props.currentCategory && a.tags.includes(props.currentCategory)) {
          scoreA += 10
        }
        if (props.currentCategory && b.tags.includes(props.currentCategory)) {
          scoreB += 10
        }
        
        // 相同标签的文章得分更高
        if (props.currentTags) {
          const aTagMatches = props.currentTags.filter(tag => a.tags.includes(tag)).length
          const bTagMatches = props.currentTags.filter(tag => b.tags.includes(tag)).length
          scoreA += aTagMatches * 5
          scoreB += bTagMatches * 5
        }
        
        // 按浏览量和点赞数排序
        scoreA += (a.views || 0) * 0.01 + (a.likes || 0) * 0.1
        scoreB += (b.views || 0) * 0.01 + (b.likes || 0) * 0.1
        
        return scoreB - scoreA
      })
      
      // 取前3篇新文章添加到列表
      const moreArticles = sortedArticles.slice(0, 3).map(convertToRelatedArticle)
      articles.value.push(...moreArticles)
      
      // 判断是否还有更多数据 - 更智能的判断
      hasMore.value = response.content.length >= 10 || filteredArticles.length > 3
    } else {
      // 没有找到新文章，可能已经到底了
      hasMore.value = false
      toast.show('已经没有更多相关文章了', '提示', { type: 'info' })
    }
  } catch (error) {
    console.error('加载更多文章失败:', error)
    toast.show('加载更多失败', '错误', { type: 'error' })
    // 如果加载失败，回退页码
    currentPage.value = Math.max(0, currentPage.value - 1)
  } finally {
    loadingMore.value = false
  }
}

const refreshRecommendations = () => {
  // 重置状态
  articles.value = []
  currentPage.value = 0
  hasMore.value = true
  loadRelatedArticles()
}

const navigateToArticle = (articleTitle: string) => {
  // 我们不再需要emit articleId，因为我们现在使用标题
  router.push(`/wiki/${articleTitle}`)
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
