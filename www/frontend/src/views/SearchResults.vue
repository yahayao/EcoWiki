<!--
  搜索结果页面 [已废弃 - 改用下拉弹窗方式]
  
  ⚠️ 注意：此页面已被废弃，搜索功能现在通过 HeaderSearch.vue 组件的下拉弹窗实现
  
  原功能：显示用户搜索的文章结果，支持分页浏览和相关文章推荐
  新实现：HeaderSearch.vue 组件集成了实时搜索下拉框，提供更好的用户体验
  
  变更原因：
  - 下拉弹窗提供更快的搜索反馈
  - 减少页面跳转，用户体验更流畅
  - 实时搜索功能，输入即可看到结果
  - 支持键盘导航和快速访问
  
  如需恢复完整页面搜索功能，可以：
  1. 取消此页面的废弃状态
  2. 在 HeaderSearch.vue 中添加"查看全部结果"按钮
  3. 恢复路由配置中的 SearchResults 路由
  
  @author EcoWiki Team
  @version 1.0 [废弃]
  @deprecated 2025-01-27 改用 HeaderSearch 下拉弹窗方式
  @see HeaderSearch.vue 新的搜索实现
-->

<template>
  <div class="search-results">
    <!-- 搜索头部信息 -->
    <div class="search-header">
      <h1 class="search-title">搜索结果</h1>
      <div class="search-info" v-if="searchKeyword">
        <span class="keyword">关键词：{{ searchKeyword }}</span>
        <span class="count" v-if="searchResults && searchResults.content">
          找到 {{ searchResults.totalElements }} 个结果
        </span>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <div class="loading-spinner"></div>
      <p>正在搜索中...</p>
    </div>

    <!-- 搜索结果列表 -->
    <div v-else-if="searchResults && searchResults.content && searchResults.content.length > 0" class="results-list">
      <div v-for="article in searchResults.content" :key="article.id" class="result-item">
        <div class="result-header">
          <h2 class="result-title" @click="goToArticle(article.title)">
            {{ article.title }}
          </h2>
          <div class="result-meta">
            <span class="author">作者：{{ article.author }}</span>
            <span class="date">{{ formatDate(article.publishDate) }}</span>
            <span class="category" v-if="article.category">{{ article.category }}</span>
          </div>
        </div>
        
        <div class="result-content">
          <p class="summary">{{ getArticleSummary(article.content) }}</p>
        </div>
        
        <div class="result-footer">
          <div class="result-stats">
            <span class="views">浏览：{{ article.views || 0 }}</span>
            <span class="likes">点赞：{{ article.likes || 0 }}</span>
          </div>
          <div class="result-tags" v-if="article.tags && article.tags.length > 0">
            <span v-for="tag in article.tags" :key="tag.id" class="tag">
              {{ tag.tagName }}
            </span>
          </div>
        </div>
      </div>

      <!-- 分页组件 -->
      <div class="pagination" v-if="searchResults.totalPages > 1">
        <button 
          class="page-btn" 
          :disabled="currentPage === 0"
          @click="goToPage(currentPage - 1)"
        >
          上一页
        </button>
        
        <div class="page-numbers">
          <button
            v-for="page in visiblePages"
            :key="page"
            class="page-number"
            :class="{ active: page === currentPage }"
            @click="goToPage(page)"
          >
            {{ page + 1 }}
          </button>
        </div>
        
        <button 
          class="page-btn"
          :disabled="currentPage === searchResults.totalPages - 1"
          @click="goToPage(currentPage + 1)"
        >
          下一页
        </button>
      </div>
    </div>

    <!-- 空结果提示 -->
    <div v-else-if="!loading && searchKeyword" class="empty-results">
      <div class="empty-icon">🔍</div>
      <h3>没有找到相关结果</h3>
      <p>尝试使用不同的关键词，或者</p>
      <button class="back-home-btn" @click="goHome">返回首页</button>
    </div>

    <!-- 无搜索关键词提示 -->
    <div v-else-if="!loading && !searchKeyword" class="no-keyword">
      <div class="no-keyword-icon">❓</div>
      <h3>请输入搜索关键词</h3>
      <p>在上方搜索框中输入您要查找的内容</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { articleApi } from '../api/article'
import type { PageResponse, Article } from '../types/article'

// 路由和状态管理
const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const searchResults = ref<PageResponse<Article> | null>(null)
const currentPage = ref(0)
const pageSize = 10

// 计算属性
const searchKeyword = computed(() => route.query.q as string || '')

const visiblePages = computed(() => {
  if (!searchResults.value) return []
  
  const totalPages = searchResults.value.totalPages
  const current = currentPage.value
  const pages: number[] = []
  
  // 显示当前页前后2页
  const start = Math.max(0, current - 2)
  const end = Math.min(totalPages - 1, current + 2)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})

// 方法
const performSearch = async () => {
  if (!searchKeyword.value.trim()) {
    searchResults.value = null
    return
  }
  
  loading.value = true
  try {
    const result = await articleApi.searchArticles(
      searchKeyword.value,
      currentPage.value,
      pageSize
    )
    searchResults.value = result
  } catch (error) {
    console.error('搜索失败:', error)
    searchResults.value = null
  } finally {
    loading.value = false
  }
}

const goToPage = (page: number) => {
  currentPage.value = page
  performSearch()
  
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goToArticle = (title: string) => {
  router.push(`/wiki/${encodeURIComponent(title)}`)
}

const goHome = () => {
  router.push('/')
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const getArticleSummary = (content: string) => {
  // 移除HTML标签并截取前200个字符作为摘要
  const plainText = content.replace(/<[^>]*>/g, '')
  return plainText.length > 200 ? plainText.substring(0, 200) + '...' : plainText
}

// 生命周期和监听
onMounted(() => {
  if (searchKeyword.value) {
    performSearch()
  }
})

// 监听查询参数变化
watch(() => route.query.q, (newKeyword) => {
  currentPage.value = 0
  if (newKeyword) {
    performSearch()
  } else {
    searchResults.value = null
  }
})
</script>

<style scoped>
.search-results {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 搜索头部 */
.search-header {
  margin-bottom: 30px;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 20px;
}

.search-title {
  font-size: 28px;
  color: #2c3e50;
  margin: 0 0 10px 0;
  font-weight: 600;
}

.search-info {
  display: flex;
  gap: 20px;
  align-items: center;
  color: #666;
  font-size: 14px;
}

.keyword {
  font-weight: 500;
  color: #27ae60;
}

.count {
  color: #7f8c8d;
}

/* 加载状态 */
.loading {
  text-align: center;
  padding: 60px 20px;
  color: #7f8c8d;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #27ae60;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 结果列表 */
.results-list {
  margin-bottom: 40px;
}

.result-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 20px;
  background: #fff;
  transition: all 0.3s ease;
}

.result-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #27ae60;
}

.result-header {
  margin-bottom: 12px;
}

.result-title {
  font-size: 20px;
  color: #2c3e50;
  margin: 0 0 8px 0;
  cursor: pointer;
  transition: color 0.2s ease;
}

.result-title:hover {
  color: #27ae60;
}

.result-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #7f8c8d;
  flex-wrap: wrap;
}

.author {
  font-weight: 500;
}

.category {
  background: #ecf0f1;
  padding: 2px 8px;
  border-radius: 4px;
  color: #2c3e50;
}

.result-content {
  margin: 16px 0;
}

.summary {
  color: #34495e;
  line-height: 1.6;
  margin: 0;
  font-size: 14px;
}

.result-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.result-stats {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #95a5a6;
}

.result-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag {
  background: #e8f5e8;
  color: #27ae60;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 40px;
  padding: 20px 0;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: #fff;
  color: #666;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.page-btn:hover:not(:disabled) {
  background: #27ae60;
  color: white;
  border-color: #27ae60;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 4px;
}

.page-number {
  width: 36px;
  height: 36px;
  border: 1px solid #ddd;
  background: #fff;
  color: #666;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-number:hover,
.page-number.active {
  background: #27ae60;
  color: white;
  border-color: #27ae60;
}

/* 空结果和提示 */
.empty-results,
.no-keyword {
  text-align: center;
  padding: 80px 20px;
  color: #7f8c8d;
}

.empty-icon,
.no-keyword-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-results h3,
.no-keyword h3 {
  color: #2c3e50;
  margin: 0 0 10px 0;
  font-size: 24px;
}

.empty-results p,
.no-keyword p {
  margin: 0 0 20px 0;
  color: #7f8c8d;
}

.back-home-btn {
  background: #27ae60;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s ease;
}

.back-home-btn:hover {
  background: #219a52;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-results {
    padding: 16px;
  }
  
  .search-title {
    font-size: 24px;
  }
  
  .search-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .result-item {
    padding: 16px;
  }
  
  .result-title {
    font-size: 18px;
  }
  
  .result-meta {
    gap: 12px;
  }
  
  .result-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .pagination {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .page-number {
    width: 32px;
    height: 32px;
  }
}

@media (max-width: 480px) {
  .search-results {
    padding: 12px;
  }
  
  .result-item {
    padding: 12px;
  }
  
  .result-title {
    font-size: 16px;
  }
  
  .summary {
    font-size: 13px;
  }
  
  .page-btn {
    padding: 6px 12px;
    font-size: 12px;
  }
}
</style>
