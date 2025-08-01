<!--
文章管理组件
包含个人文章的完整管理功能：收藏文章、已发布文章、草稿文章、点赞文章
-->
<template>
  <div class="article-management">
    <header class="management-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <i class="icon-folder"></i>
            文章管理
          </h1>
          <p class="page-description">管理您的文章创作和收藏</p>
        </div>
        <button 
          @click="$router.push('/article/create')"
          class="create-article-btn"
        >
          <i class="icon-plus"></i>
          新建文章
        </button>
      </div>
    </header>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card favorites">
        <div class="stat-icon">
          <i class="icon-heart"></i>
        </div>
        <div class="stat-content">
          <h3>{{ stats.favoriteArticles }}</h3>
          <p>收藏文章</p>
        </div>
      </div>
      
      <div class="stat-card published">
        <div class="stat-icon">
          <i class="icon-published"></i>
        </div>
        <div class="stat-content">
          <h3>{{ stats.publishedArticles }}</h3>
          <p>已发布</p>
        </div>
      </div>
      
      <div class="stat-card drafts">
        <div class="stat-icon">
          <i class="icon-edit"></i>
        </div>
        <div class="stat-content">
          <h3>{{ stats.draftArticles }}</h3>
          <p>草稿</p>
        </div>
      </div>
      
      <div class="stat-card likes">
        <div class="stat-icon">
          <i class="icon-thumbs-up"></i>
        </div>
        <div class="stat-content">
          <h3>{{ stats.likedArticles }}</h3>
          <p>点赞文章</p>
        </div>
      </div>
    </div>

    <!-- 标签页导航 -->
    <div class="tabs-container">
      <div class="tabs-nav">
        <button 
          v-for="tab in tabs" 
          :key="tab.key"
          :class="['tab-btn', { active: activeTab === tab.key }]"
          @click="switchTab(tab.key)"
        >
          <i :class="tab.icon"></i>
          {{ tab.label }}
          <span v-if="getTabCount(tab.key) > 0" class="tab-count">
            {{ getTabCount(tab.key) }}
          </span>
        </button>
      </div>

      <!-- 搜索和筛选 -->
      <div class="filters">
        <div class="search-box">
          <i class="icon-search"></i>
          <input 
            v-model="searchQuery"
            @input="handleSearch"
            placeholder="搜索文章..."
            class="search-input"
          >
        </div>
        <select v-model="sortBy" @change="handleSort" class="sort-select">
          <option value="createTime">按创建时间</option>
          <option value="updateTime">按更新时间</option>
          <option value="views">按浏览量</option>
          <option value="likes">按点赞数</option>
        </select>
        <select v-model="sortOrder" @change="handleSort" class="sort-select">
          <option value="desc">降序</option>
          <option value="asc">升序</option>
        </select>
      </div>
    </div>

    <!-- 文章列表 -->
    <div class="articles-content">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="filteredArticles.length === 0" class="empty-state">
        <div class="empty-illustration">
          <i class="icon-empty"></i>
        </div>
        <h3>{{ getEmptyMessage() }}</h3>
        <p>{{ getEmptyDescription() }}</p>
        <button 
          v-if="activeTab === 'published' || activeTab === 'drafts'"
          @click="$router.push('/article/create')"
          class="create-first-btn"
        >
          创建第一篇文章
        </button>
      </div>

      <div v-else class="articles-grid">
        <ArticleCard
          v-for="article in paginatedArticles"
          :key="article.articleId"
          :article="article"
          :show-actions="true"
          :show-status="activeTab === 'published' || activeTab === 'drafts'"
          @like="handleLike"
          @unlike="handleUnlike"
          @favorite="handleFavorite"
          @unfavorite="handleUnfavorite"
          @edit="handleEdit"
          @delete="handleDelete"
          @view="handleView"
        />
      </div>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="pagination">
        <button 
          @click="currentPage > 1 && goToPage(currentPage - 1)"
          :disabled="currentPage <= 1"
          class="page-btn"
        >
          <i class="icon-chevron-left"></i>
          上一页
        </button>
        
        <div class="page-numbers">
          <button
            v-for="page in visiblePages"
            :key="page"
            :class="['page-number', { active: page === currentPage }]"
            @click="goToPage(Number(page))"
          >
            {{ page }}
          </button>
        </div>

        <button 
          @click="currentPage < totalPages && goToPage(currentPage + 1)"
          :disabled="currentPage >= totalPages"
          class="page-btn"
        >
          下一页
          <i class="icon-chevron-right"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi, type Article } from '@/api/article'
import { userApi } from '@/api/user'
import { debounce } from '@/utils/debounce-throttle'
import ArticleCard from './ArticleCard.vue'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const activeTab = ref('favorites')
const searchQuery = ref('')
const sortBy = ref('createTime')
const sortOrder = ref('desc')
const currentPage = ref(1)
const pageSize = 12

// 文章数据
const favoriteArticles = ref<Article[]>([])
const publishedArticles = ref<Article[]>([])
const draftArticles = ref<Article[]>([])
const likedArticles = ref<Article[]>([])

// 统计数据
const stats = ref({
  favoriteArticles: 0,
  publishedArticles: 0,
  draftArticles: 0,
  likedArticles: 0
})

// 标签页配置
const tabs = [
  { key: 'favorites', label: '收藏文章', icon: 'icon-heart' },
  { key: 'published', label: '已发布', icon: 'icon-published' },
  { key: 'drafts', label: '草稿', icon: 'icon-edit' },
  { key: 'liked', label: '点赞文章', icon: 'icon-thumbs-up' }
]

// 计算属性
const currentArticles = computed(() => {
  switch (activeTab.value) {
    case 'favorites': return favoriteArticles.value
    case 'published': return publishedArticles.value
    case 'drafts': return draftArticles.value
    case 'liked': return likedArticles.value
    default: return []
  }
})

const filteredArticles = computed(() => {
  let articles = [...currentArticles.value]
  
  // 搜索筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    articles = articles.filter(article => 
      article.title.toLowerCase().includes(query) ||
      article.content?.toLowerCase().includes(query) ||
      article.author.toLowerCase().includes(query)
    )
  }
  
  // 排序
  articles.sort((a, b) => {
    let aValue, bValue
    
    switch (sortBy.value) {
      case 'createTime':
        aValue = new Date(a.publishDate).getTime()
        bValue = new Date(b.publishDate).getTime()
        break
      case 'updateTime':
        aValue = new Date(a.updateTime).getTime()
        bValue = new Date(b.updateTime).getTime()
        break
      case 'views':
        aValue = a.views
        bValue = b.views
        break
      case 'likes':
        aValue = a.likes
        bValue = b.likes
        break
      default:
        aValue = new Date(a.publishDate).getTime()
        bValue = new Date(b.publishDate).getTime()
    }
    
    return sortOrder.value === 'desc' ? bValue - aValue : aValue - bValue
  })
  
  return articles
})

const totalPages = computed(() => 
  Math.ceil(filteredArticles.value.length / pageSize)
)

const paginatedArticles = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredArticles.value.slice(start, end)
})

const visiblePages = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  const delta = 2
  
  const range = []
  const rangeWithDots = []
  
  for (let i = Math.max(2, current - delta); 
       i <= Math.min(total - 1, current + delta); 
       i++) {
    range.push(i)
  }
  
  if (current - delta > 2) {
    rangeWithDots.push(1, '...')
  } else {
    rangeWithDots.push(1)
  }
  
  rangeWithDots.push(...range)
  
  if (current + delta < total - 1) {
    rangeWithDots.push('...', total)
  } else if (total > 1) {
    rangeWithDots.push(total)
  }
  
  return rangeWithDots.filter((item, index, arr) => arr.indexOf(item) === index)
})

// 方法
const getTabCount = (tabKey: string) => {
  switch (tabKey) {
    case 'favorites': return stats.value.favoriteArticles
    case 'published': return stats.value.publishedArticles
    case 'drafts': return stats.value.draftArticles
    case 'liked': return stats.value.likedArticles
    default: return 0
  }
}

const getEmptyMessage = () => {
  switch (activeTab.value) {
    case 'favorites': return '暂无收藏文章'
    case 'published': return '暂无已发布文章'
    case 'drafts': return '暂无草稿'
    case 'liked': return '暂无点赞文章'
    default: return '暂无数据'
  }
}

const getEmptyDescription = () => {
  switch (activeTab.value) {
    case 'favorites': return '收藏感兴趣的文章，方便随时查看'
    case 'published': return '发布您的第一篇文章，分享知识与见解'
    case 'drafts': return '创建草稿，随时保存您的创作进度'
    case 'liked': return '为您喜欢的文章点赞，支持优质内容'
    default: return ''
  }
}

const switchTab = (tabKey: string) => {
  activeTab.value = tabKey
  currentPage.value = 1
  loadArticles()
}

const handleSearch = debounce(() => {
  currentPage.value = 1
}, 300)

const handleSort = () => {
  currentPage.value = 1
}

const goToPage = (page: number) => {
  currentPage.value = page
}

//
const handleLike = async (article: Article) => {
  try {
    await articleApi.likeArticle(article.articleId)
    article.likes++
    // 如果在点赞文章列表中，添加到列表
    if (activeTab.value === 'liked') {
      const exists = likedArticles.value.find(a => a.articleId === article.articleId)
      if (!exists) {
        likedArticles.value.unshift(article)
        stats.value.likedArticles++
      }
    }
  } catch (error) {
    console.error('点赞失败:', error)
    alert('点赞失败，请重试')
  }
}

const handleUnlike = async (article: Article) => {
  try {
    await articleApi.unlikeArticle(article.articleId)
    article.likes--
    // 如果在点赞文章列表中，从列表移除
    if (activeTab.value === 'liked') {
      const index = likedArticles.value.findIndex(a => a.articleId === article.articleId)
      if (index > -1) {
        likedArticles.value.splice(index, 1)
        stats.value.likedArticles--
      }
    }
  } catch (error) {
    console.error('取消点赞失败:', error)
    alert('取消点赞失败，请重试')
  }
}

const handleFavorite = async (article: Article) => {
  try {
    await articleApi.favoriteArticle(article.articleId)
    // 如果在收藏文章列表中，添加到列表
    if (activeTab.value === 'favorites') {
      const exists = favoriteArticles.value.find(a => a.articleId === article.articleId)
      if (!exists) {
        favoriteArticles.value.unshift(article)
        stats.value.favoriteArticles++
      }
    }
  } catch (error) {
    console.error('收藏失败:', error)
    alert('收藏失败，请重试')
  }
}

const handleUnfavorite = async (article: Article) => {
  try {
    await articleApi.unfavoriteArticle(article.articleId)
    // 如果在收藏文章列表中，从列表移除
    if (activeTab.value === 'favorites') {
      const index = favoriteArticles.value.findIndex(a => a.articleId === article.articleId)
      if (index > -1) {
        favoriteArticles.value.splice(index, 1)
        stats.value.favoriteArticles--
      }
    }
  } catch (error) {
    console.error('取消收藏失败:', error)
    alert('取消收藏失败，请重试')
  }
}

const handleEdit = (article: Article) => {
  router.push(`/article/edit/${article.articleId}`)
}

const handleDelete = async (article: Article) => {
  if (!confirm('确定要删除这篇文章吗？此操作无法撤销。')) {
    return
  }
  
  try {
    await articleApi.deleteArticle(article.articleId)
    
    // 从所有相关列表中移除
    const removeFromList = (list: Article[]) => {
      const index = list.findIndex(a => a.articleId === article.articleId)
      if (index > -1) {
        list.splice(index, 1)
      }
    }
    
    removeFromList(publishedArticles.value)
    removeFromList(draftArticles.value)
    removeFromList(favoriteArticles.value)
    removeFromList(likedArticles.value)
    
    // 更新统计
    loadStats()
    
    alert('文章删除成功')
  } catch (error) {
    console.error('删除失败:', error)
    alert('删除失败，请重试')
  }
}

const handleView = (article: Article) => {
  router.push(`/article/${article.articleId}`)
}

// 数据加载
const loadStats = async () => {
  try {
    const statsData = await userApi.getUserArticleStats()
    stats.value = {
      favoriteArticles: statsData.favoriteArticles,
      publishedArticles: statsData.publishedArticles,
      draftArticles: statsData.draftArticles,
      likedArticles: 0 // 如果 API 返回这个字段，请使用 statsData.likedArticles
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadArticles = async () => {
  if (loading.value) return
  
  loading.value = true
  try {
    switch (activeTab.value) {
      case 'favorites':
        const favoritesData = await userApi.getFavoriteArticles(0, 100)
        favoriteArticles.value = favoritesData.content
        break
      case 'published':
        const publishedData = await userApi.getUserArticles(0, 100, 'published')
        publishedArticles.value = publishedData.content
        break
      case 'drafts':
        const draftsData = await userApi.getUserArticles(0, 100, 'draft')
        draftArticles.value = draftsData.content
        break
      case 'liked':
        const likedData = await userApi.getLikedArticles(0, 100)
        likedArticles.value = likedData.content
        break
    }
  } catch (error) {
    console.error('加载文章失败:', error)
  } finally {
    loading.value = false
  }
}

// 监听器
watch(activeTab, () => {
  currentPage.value = 1
})

watch([searchQuery, sortBy, sortOrder], () => {
  currentPage.value = 1
})

// 生命周期
onMounted(() => {
  loadStats()
  loadArticles()
})
</script>

<style scoped>
.article-management {
  min-height: 100vh;
  background: #f8f9fa;
}

.management-header {
  background: white;
  border-bottom: 1px solid #e9ecef;
  padding: 2rem 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section {
  flex: 1;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 0.5rem 0;
}

.page-title .icon-folder {
  color: #3498db;
  font-size: 1.8rem;
}

.page-description {
  color: #6c757d;
  font-size: 1.1rem;
  margin: 0;
}

.create-article-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #3498db;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
}

.create-article-btn:hover {
  background: #2980b9;
  transform: translateY(-1px);
}

.stats-cards {
  max-width: 1200px;
  margin: 2rem auto;
  padding: 0 2rem;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: white;
}

.stat-card.favorites .stat-icon {
  background: linear-gradient(135deg, #e74c3c, #c0392b);
}

.stat-card.published .stat-icon {
  background: linear-gradient(135deg, #27ae60, #229954);
}

.stat-card.drafts .stat-icon {
  background: linear-gradient(135deg, #f39c12, #e67e22);
}

.stat-card.likes .stat-icon {
  background: linear-gradient(135deg, #9b59b6, #8e44ad);
}

.stat-content h3 {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 0.25rem 0;
}

.stat-content p {
  color: #6c757d;
  font-size: 0.95rem;
  margin: 0;
}

.tabs-container {
  max-width: 1200px;
  margin: 0 auto 2rem;
  padding: 0 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.tabs-nav {
  display: flex;
  border-bottom: 1px solid #e9ecef;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 1.5rem;
  border: none;
  background: none;
  color: #6c757d;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.tab-btn:hover {
  color: #3498db;
}

.tab-btn.active {
  color: #3498db;
  font-weight: 600;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background: #3498db;
}

.tab-count {
  background: #3498db;
  color: white;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.25rem 0.5rem;
  border-radius: 10px;
  min-width: 1.5rem;
  text-align: center;
}

.filters {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.5rem;
  flex-wrap: wrap;
}

.search-box {
  position: relative;
  flex: 1;
  min-width: 250px;
}

.search-box .icon-search {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #6c757d;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.5rem;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  font-size: 0.95rem;
  transition: border-color 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #3498db;
}

.sort-select {
  padding: 0.75rem 1rem;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  font-size: 0.95rem;
  background: white;
  cursor: pointer;
}

.articles-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem 2rem;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e9ecef;
  border-top: 3px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-illustration {
  font-size: 4rem;
  color: #e9ecef;
  margin-bottom: 1rem;
}

.empty-state h3 {
  font-size: 1.5rem;
  color: #6c757d;
  margin: 0 0 0.5rem 0;
}

.empty-state p {
  color: #adb5bd;
  margin: 0 0 2rem 0;
}

.create-first-btn {
  background: #3498db;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.create-first-btn:hover {
  background: #2980b9;
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  margin-top: 3rem;
  padding: 2rem 0;
}

.page-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border: 1px solid #e9ecef;
  background: white;
  color: #6c757d;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.95rem;
}

.page-btn:hover:not(:disabled) {
  background: #f8f9fa;
  border-color: #3498db;
  color: #3498db;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 0.25rem;
}

.page-number {
  width: 40px;
  height: 40px;
  border: 1px solid #e9ecef;
  background: white;
  color: #6c757d;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-number:hover {
  background: #f8f9fa;
  border-color: #3498db;
  color: #3498db;
}

.page-number.active {
  background: #3498db;
  border-color: #3498db;
  color: white;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
    gap: 1rem;
  }

  .tabs-nav {
    flex-wrap: wrap;
  }

  .filters {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    min-width: unset;
  }

  .articles-grid {
    grid-template-columns: 1fr;
  }

  .pagination {
    flex-direction: column;
    gap: 1rem;
  }

  .page-numbers {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
