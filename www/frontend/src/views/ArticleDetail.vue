<template>
  <div class="article-detail-page">
    <div class="container">
      <!-- 返回导航 -->
      <div class="navigation">
        <button @click="goBack" class="back-btn">
          <span class="back-icon">←</span>
          返回首页
        </button>
        <div class="breadcrumb">
          <span>首页</span>
          <span class="separator">/</span>
          <span>{{ article?.category }}</span>
          <span class="separator">/</span>
          <span class="current">文章详情</span>
        </div>
      </div>

      <!-- 文章内容 -->
      <ArticleContent 
        v-if="article" 
        :article="article" 
        class="article-section"
      />
      
      <!-- 加载状态 -->
      <div v-else-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>正在加载文章内容...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="error-state">
        <div class="error-icon">❌</div>
        <h3>加载失败</h3>
        <p>{{ error }}</p>
        <button @click="loadArticle" class="retry-btn">重试</button>
      </div>

      <!-- 相关文章推荐 -->
      <RelatedArticles
        v-if="article"
        :current-article-id="article.articleId"
        :current-category="article.category"
        :current-tags="article.tags.split(',')"
        :max-results="6"
        :show-refresh-button="true"
        @article-click="onRelatedArticleClick"
        class="related-section"
      />

      <!-- 评论区 -->
      <ArticleComments 
        v-if="article" 
        :article-id="article.articleId"
        @show-login="showLoginModal"
        class="comments-section"
      />
    </div>

    <!-- 右侧浮动按钮 -->
    <FloatingActionButtons
      v-if="article"
      :article-id="article.articleId"
      @view="handleView"
      @edit="handleEdit"
      @history="handleHistory"
      @favorite="handleFavorite"
      @more="handleMore"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ArticleContent from '../components/article/ArticleContent.vue'
import ArticleComments from '../components/article/ArticleComments.vue'
import RelatedArticles from '../components/article/RelatedArticles.vue'
import FloatingActionButtons from '../components/article/FloatingActionButtons.vue'
import { articleApi, type Article } from '../api/article'

const route = useRoute()
const router = useRouter()

const article = ref<Article | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

const loadArticle = async () => {
  const articleId = route.params.id as string
  if (!articleId || isNaN(Number(articleId))) {
    error.value = '无效的文章ID'
    loading.value = false
    return
  }

  try {
    loading.value = true
    error.value = null
    article.value = await articleApi.getArticleById(Number(articleId))
  } catch (err) {
    console.error('加载文章失败:', err)
    error.value = err instanceof Error ? err.message : '加载文章失败'
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/')
}

const onRelatedArticleClick = (articleId: number) => {
  router.push(`/article/${articleId}`)
}

const showLoginModal = () => {
  // 显示登录模态框的逻辑
  console.log('显示登录模态框')
}

// 浮动按钮事件处理
const handleView = () => {
  // 查看历史或其他操作
  console.log('查看操作')
}

const handleEdit = () => {
  if (article.value) {
    router.push(`/edit/${article.value.articleId}`)
  }
}

const handleHistory = () => {
  // 查看文章历史
  console.log('查看历史')
}

const handleFavorite = () => {
  // 收藏文章
  console.log('收藏文章')
}

const handleMore = () => {
  // 更多操作
  console.log('更多操作')
}

// 监听路由变化
watch(() => route.params.id, () => {
  loadArticle()
})

onMounted(() => {
  loadArticle()
})
</script>

<style scoped>
.article-detail-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px 0;
}

.container {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding: 16px 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.back-icon {
  font-size: 1.2rem;
  font-weight: bold;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #718096;
  font-size: 0.9rem;
}

.separator {
  color: #cbd5e0;
}

.current {
  color: #667eea;
  font-weight: 500;
}

.article-section {
  margin-bottom: 32px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-state p {
  color: #718096;
  font-size: 1rem;
}

.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  text-align: center;
}

.error-icon {
  font-size: 3rem;
  margin-bottom: 16px;
}

.error-state h3 {
  color: #1a202c;
  margin-bottom: 8px;
  font-size: 1.4rem;
}

.error-state p {
  color: #718096;
  margin-bottom: 24px;
  font-size: 1rem;
}

.retry-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 25px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.comments-section {
  margin-top: 0;
}

.related-section {
  margin: 32px 0 0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .container {
    max-width: 100%;
    padding: 0 16px;
  }
}

@media (max-width: 768px) {
  .container {
    padding: 0 12px;
  }
  
  .navigation {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 16px;
  }
  
  .breadcrumb {
    font-size: 0.8rem;
  }
}

@media (max-width: 480px) {
  .article-detail-page {
    padding: 10px 0;
  }
  
  .navigation {
    margin-bottom: 20px;
  }
  
  .back-btn {
    padding: 8px 12px;
    font-size: 0.85rem;
  }
}
</style>
