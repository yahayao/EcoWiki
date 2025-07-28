<!--
  精选文章组件
  
  该组件展示Wiki系统中的热门和精选文章，为用户提供高质量内容的快速访问入口。
  采用卡片式布局设计，支持响应式显示和丰富的文章信息展示。
  
  主要功能：
  - 热门文章展示：展示高浏览量和高点赞数的精选文章
  - 文章信息预览：提供标题、摘要、作者、分类等核心信息
  - 快速导航：支持点击跳转到文章详情页面
  - 标签系统：展示文章相关标签，便于分类浏览
  - 统计信息：显示浏览量、点赞数等互动数据
  
  数据展示：
  - 文章标题：清晰的标题展示
  - 内容摘要：自动截取文章开头作为预览
  - 作者信息：显示文章作者名称
  - 发布时间：格式化的发布日期
  - 分类标签：文章所属分类和自定义标签
  - 互动统计：浏览量和点赞数实时显示
  
  视觉设计：
  - 卡片式布局：每篇文章独立的卡片容器
  - 网格排列：响应式网格布局适配不同屏幕
  - 状态反馈：加载、错误、空数据的视觉提示
  - 悬浮效果：鼠标悬停时的交互反馈
  - 图标化表示：使用表情符号增强视觉表达
  
  技术实现：
  - Vue 3 Composition API
  - 异步数据加载和状态管理
  - Wiki语法解析和内容摘要提取
  - 路由导航集成
  - 响应式设计和CSS Grid布局
  
  使用场景：
  - 首页热门内容展示
  - 推荐文章列表
  - 内容发现和导航
  - 用户浏览内容的入口
  
  扩展性：
  - 支持不同的排序方式（热度、时间、评分等）
  - 可添加文章分类筛选
  - 支持个性化推荐算法
  - 可集成收藏和分享功能
  
  @author EcoWiki Team
  @version 1.0.0
  @since 2024-01-01
-->
<template>
  <!-- 精选文章容器 -->
  <div class="featured-articles">
    <!-- 区域标题 -->
    <h2 class="section-title"><IconFire color="red"></IconFire> 热门内容</h2>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>
    
    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <span><IconCross color="red"></IconCross> 加载失败: {{ error }}</span>
    </div>
    
    <!-- 文章网格 -->
    <div v-else class="article-grid">
      <!-- 单个文章卡片 -->
      <div 
        v-for="article in articles" 
        :key="article.articleId"
        class="article-card" 
        @click="navigateToArticle(article.title.toString())"
      >
        <!-- 文章头部信息 -->
        <div class="article-header">
          <span class="article-category">{{ article.category || '未分类' }}</span>
          <span class="article-stats">👁 {{ article.views }} | 👍 {{ article.likes }}</span>
        </div>
        
        <!-- 文章标题 -->
        <h3 class="article-title">{{ article.title }}</h3>
        
        <!-- 文章摘要 -->
        <p class="article-excerpt">{{ getArticleExcerpt(article.content) }}</p>
        
        <!-- 文章标签 -->
        <div v-if="article.tags" class="article-tags">
          <span v-for="tag in getTagArray(article.tags)" :key="tag" class="tag">{{ tag }}</span>
        </div>
        
        <!-- 文章元信息 -->
        <div class="article-meta">
          <div class="article-author">
            <UserAvatar 
              :username="article.author"
              :avatar-url="article.authorAvatar || ''"
              size="xs"
              shape="circle"
            />
            <span class="author-name">{{ article.author }}</span>
          </div>
          <span class="article-date">{{ formatDate(article.publishDate) }}</span>
        </div>
      </div>
    </div>
    
    <!-- 空数据状态 -->
    <div v-if="!loading && !error && articles.length === 0" class="empty-container">
      <span>📝 暂无文章</span>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 精选文章组件脚本
 * 
 * 该脚本处理精选文章的数据获取、状态管理和用户交互。
 * 集成Wiki解析器进行内容摘要提取，提供丰富的文章预览体验。
 */

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi, type Article } from '../../api/article'
import { wikiParser } from '../../utils/wikiParser'
import { IconFire, IconCross } from '../icons'
import UserAvatar from '@/components/common/UserAvatar.vue'

// 路由实例
const router = useRouter()

// 响应式数据定义
const articles = ref<Article[]>([])  // 精选文章列表
const loading = ref(true)            // 加载状态标识
const error = ref('')                // 错误信息

/**
 * 导航到文章详情页
 * @param articleTitle 文章ID
 */
const navigateToArticle = (articleTitle: string) => {
  router.push(`/wiki/${articleTitle}`)
}

/**
 * 获取文章内容摘要
 * 使用Wiki解析器提取文章的纯文本摘要，保持原有的换行格式
 * @param content 文章原始内容
 * @returns 格式化的摘要文本
 */
const getArticleExcerpt = (content: string): string => {
  if (!content) return '暂无内容...'
  
  // 直接处理原始内容，保持换行和格式
  let text = content
    // 移除Wiki语法标记，但保持文本结构
    .replace(/\[\[Category:[^\]]+\]\]/g, '') // 移除分类标签
    .replace(/={1,6}\s*([^=\n]+)\s*={1,6}/g, '$1') // 移除标题符号，保留标题文本
    .replace(/'''([^']+)'''/g, '$1') // 移除粗体标记
    .replace(/''([^']+)''/g, '$1') // 移除斜体标记
    .replace(/__([^_]+)__/g, '$1') // 移除下划线标记
    .replace(/\[\[([^\]|]+)(\|[^\]]+)?\]\]/g, '$1') // 简化内部链接
    .replace(/\[([^\s]+)\s+([^\]]+)\]/g, '$2') // 简化外部链接
    .replace(/<[^>]+>/g, '') // 移除HTML标签
    .replace(/\{\{[^}]+\}\}/g, '') // 移除模板
    .replace(/^\s*[\*#]+\s*/gm, '') // 移除列表标记，保持列表内容
    .trim()
  
  // 如果文本太长，截取前150个字符，但在合适的位置截断
  if (text.length > 150) {
    text = text.substring(0, 150)
    // 尝试在句号、感叹号、问号处截断
    const lastPunctuation = Math.max(
      text.lastIndexOf('。'),
      text.lastIndexOf('！'), 
      text.lastIndexOf('？'),
      text.lastIndexOf('.')
    )
    if (lastPunctuation > 100) {
      text = text.substring(0, lastPunctuation + 1)
    } else {
      text = text + '...'
    }
  }
  
  return text || '暂无内容...'
}

/**
 * 格式化日期显示
 * 将ISO日期字符串转换为中文日期格式
 * @param dateString 日期字符串
 * @returns 格式化的日期文本
 */
const formatDate = (dateString: string): string => {
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN')
  } catch {
    return '未知日期'
  }
}

/**
 * 解析标签字符串为数组
 * 将逗号分隔的标签字符串转换为标签数组
 * @param tags 标签字符串
 * @returns 标签数组
 */
const getTagArray = (tags: string): string[] => {
  if (!tags) return []
  return tags.split(',').map(tag => tag.trim()).filter(tag => tag.length > 0)
}

/**
 * 加载热门文章数据
 * 从API获取热门文章列表并更新组件状态
 */
const loadPopularArticles = async () => {
  try {
    loading.value = true
    error.value = ''
    
    // 获取热门文章（按点赞数或浏览量排序，限制6条）
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

// 组件挂载时加载数据
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
  margin: 8px 0 12px 0;
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
  padding: 20px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid rgba(226, 232, 240, 0.8);
  position: relative;
  height: 280px; /* 调整为4:3比例，约280px高度 */
  display: flex;
  flex-direction: column;
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
  margin: 0 0 8px 0; /* 减少标题和摘要之间的间距 */
  color: #1a202c;
  font-weight: 600;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 标题最多2行 */
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.8rem; /* 确保标题区域高度一致 */
}

.article-excerpt {
  color: #718096;
  line-height: 1.5;
  margin-bottom: 12px; /* 减少摘要底部间距 */
  font-size: 0.9rem;
  flex: 1; /* 让摘要占据剩余空间 */
  display: -webkit-box;
  -webkit-line-clamp: 3; /* 精确控制行数 */
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  white-space: pre-line; /* 保持换行，折叠多余空格 */
  word-wrap: break-word; /* 长单词换行 */
  overflow-wrap: break-word; /* 确保换行兼容性 */
  height: calc(1.5em * 3); /* 精确控制高度，避免半行显示 */
  max-height: calc(1.5em * 3); /* 最大高度限制 */
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #a0aec0;
  font-size: 0.85rem;
  margin-top: auto; /* 推到底部 */
}

.article-author {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.author-name {
  color: #4a5568;
}

.article-date {
  color: #cbd5e0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .article-grid {
    grid-template-columns: 1fr;
  }
  
  .article-card {
    height: 360px; /* 移动端稍微降低高度 */
  }
}

@media (max-width: 480px) {
  .article-card {
    padding: 20px;
    height: 340px; /* 小屏幕进一步降低高度 */
  }
}
</style>
