<!--
  文章详情页面组件
  
  该组件是文章详情页面的核心组件，提供完整的文章阅读体验。
  集成了文章内容展示、相关推荐、评论系统和浮动操作按钮等功能。
  
  功能特性：
  - 文章内容展示和渲染
  - 面包屑导航和返回按钮
  - 加载状态和错误处理
  - 相关文章推荐
  - 评论系统集成
  - 浮动操作按钮（查看、编辑、历史、收藏等）
  
  页面布局：
  - 导航区域：面包屑和返回按钮
  - 主内容区：文章详情展示
  - 推荐区域：相关文章推荐
  - 评论区域：用户评论和互动
  - 浮动按钮：快速操作入口
  
  状态管理：
  - 文章数据加载和缓存
  - 加载状态指示器
  - 错误状态处理和重试
  - 路由参数监听和响应
  
  用户体验：
  - 响应式设计适配不同屏幕
  - 加载动画和过渡效果
  - 错误处理和用户反馈
  - 导航便利性和页面流畅性
  
  技术实现：
  - Vue 3 Composition API
  - Vue Router 路由集成
  - TypeScript 类型安全
  - 组件化设计和复用
  - API 集成和错误处理
  
  使用场景：
  - Wiki 文章阅读页面
  - 知识库内容展示
  - 博客文章详情页
  - 内容管理系统展示
  
  @author EcoWiki Team
  @version 1.0.0
  @since 2024-01-01
-->
<template>
  <div class="article-detail-page">
    <div class="container">
      <!-- 返回导航和面包屑 -->
      <div class="navigation">
        <!-- 返回按钮 -->
        <button @click="goBack" class="back-btn">
          <span class="back-icon">←</span>
          返回首页
        </button>
        <!-- 面包屑导航 -->
        <div class="breadcrumb">
          <span>首页</span>
          <span class="separator">/</span>
          <span>{{ article?.category }}</span>
          <span class="separator">/</span>
          <span class="current">文章详情</span>
        </div>
      </div>

      <!-- 文章内容展示区 -->
      <ArticleContent 
        v-if="article" 
        :article="article" 
        class="article-section"
      />
      
      <!-- 加载状态指示器 -->
      <div v-else-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>正在加载文章内容...</p>
      </div>
      
      <!-- 错误状态和重试 -->
      <div v-else-if="error" class="error-state">
        <div class="error-icon">❌</div>
        <h3>加载失败</h3>
        <p>{{ error }}</p>
        <button @click="loadArticle" class="retry-btn">重试</button>
      </div>

      <!-- 相关文章推荐区 -->
      <RelatedArticles
        v-if="article"
        :current-article-id="article.articleId"
        :current-category="article.category"
        :current-tags="article.tags.split(',')"
        :max-results="6"
        :show-refresh-button="true"
        class="related-section"
      />

      <!-- 评论系统区 -->
      <ArticleComments 
        v-if="article" 
        :article-id="article.articleId"
        @show-login="showLoginModal"
        class="comments-section"
      />
    </div>

    <!-- 右侧浮动操作按钮 -->
    <FloatingActionButtons
      v-if="article"
      :article-id="article.articleId"
      :article-title="article.title"
      @view="handleView"
      @edit="handleEdit"
      @history="handleHistory"
      @favorite="handleFavorite"
      @more="handleMore"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * 文章详情页面组件脚本
 * 
 * 实现文章详情页面的核心逻辑，包括数据加载、状态管理、用户交互和路由处理。
 * 提供完整的文章阅读和互动体验。
 */

import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ArticleContent from '../components/article/ArticleContent.vue'
import ArticleComments from '../components/article/ArticleComments.vue'
import RelatedArticles from '../components/article/RelatedArticles.vue'
import FloatingActionButtons from '../components/article/FloatingActionButtons.vue'
import { articleApi, type Article, type ArticleVersion } from '../api/article'

// 路由实例
const route = useRoute()
const router = useRouter()

// 定义组件事件
const emit = defineEmits<{
  'show-login': []
  'show-register': []
  'show-admin': []
}>()

/**
 * 响应式状态管理
 */
// 当前文章数据
const article = ref<Article | null>(null)
// 加载状态
const loading = ref(true)
// 错误状态
const error = ref<string | null>(null)

/**
 * 加载文章数据
 * 
 * 先通过文章标题获取文章ID，然后通过ID获取文章详情数据，处理加载状态和错误情况。
 * 如果文章不存在，自动跳转到编辑页面创建新文章。
 * 支持重试机制，提供良好的用户体验。
 * 
 * @async
 * @returns {Promise<void>} 异步加载文章数据
 */
const loadArticle = async () => {
  const articleTitle = route.params.title as string
  
  // 验证文章标题的有效性
  if (!articleTitle) {
    error.value = '无效的文章标题'
    loading.value = false
    return
  }

  try {
    loading.value = true
    error.value = null
    
    // 先通过标题获取文章ID
    const articleId = await articleApi.getArticleIdByTitle(articleTitle)
    
    // 再通过ID获取文章详情
    article.value = await articleApi.getArticleById(articleId)
  } catch (err) {
    console.error('加载文章失败:', err)
    
    // 检查是否是文章不存在的错误
    const errorMessage = err instanceof Error ? err.message : '加载文章失败'
    
    // 如果文章不存在，自动跳转到新建页面
    if (errorMessage.includes('不存在') || errorMessage.includes('未找到') || errorMessage.includes('404')) {
      console.log(`文章 "${articleTitle}" 不存在，自动跳转到新建页面`)
      // 跳转到编辑页面，会自动进入新建模式
      router.push(`/edit/${articleTitle}`)
      return
    }
    
    error.value = errorMessage
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/')
}

const showLoginModal = () => {
  // 向父组件发送显示登录模态框的事件
  emit('show-login')
}

// 浮动按钮事件处理
const handleView = () => {
  // 查看历史或其他操作
  console.log('查看操作')
}

const handleEdit = () => {
  if (article.value) {
    router.push(`/edit/${article.value.title}`)
  }
}

const handleHistory = () => {
  router.push({ name: 'ArticleHistory', params: { title: route.params.title } })
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
watch(() => route.params.title, () => {
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

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}

.modal-container {
  width: 100%;
  max-width: 1200px;
  max-height: 90vh;
  overflow: hidden;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
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
