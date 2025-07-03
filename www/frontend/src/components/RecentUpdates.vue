<!--
  最近更新组件
  
  该组件展示Wiki系统中最近更新的文章列表，为用户提供快速了解最新内容的入口。
  支持实时数据获取、分类展示、状态管理和用户交互等功能。
  
  主要功能：
  - 最新文章列表展示：按更新时间排序显示最近修改的文章
  - 分类图标展示：根据文章分类显示相应的表情符号图标
  - 文章统计信息：展示浏览量、点赞数等互动数据
  - 响应式交互：支持点击跳转到文章详情页面
  - 状态管理：包含加载状态、错误处理和空数据提示
  
  数据展示：
  - 更新时间：显示文章最后更新或发布时间
  - 文章标题：可点击跳转到详情页
  - 作者信息：显示文章作者
  - 分类标签：展示文章所属分类
  - 互动统计：浏览量和点赞数实时更新
  
  视觉设计：
  - 卡片式布局：每个更新项独立展示
  - 图标化表示：使用表情符号增强视觉识别
  - 状态反馈：加载动画和错误提示
  - 悬浮效果：鼠标悬停时的视觉反馈
  
  技术实现：
  - Vue 3 Composition API
  - 异步数据加载和状态管理
  - 路由导航集成
  - 响应式设计和CSS动画
  - 时间格式化和国际化支持
  
  使用场景：
  - Wiki首页内容展示
  - 侧边栏快速导航
  - 动态内容更新提醒
  - 用户活跃度展示
  
  @author EcoWiki Team
  @version 1.0.0
  @since 2024-01-01
-->
<template>
  <!-- 最近更新容器 -->
  <div class="recent-updates">
    <!-- 区域标题 -->
    <h2 class="section-title">📝 最近更新</h2>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>
    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <span>❌ 加载失败: {{ error }}</span>
    </div>
    
    <!-- 文章列表 -->
    <div v-else class="updates-list">
      <!-- 单个文章更新项 -->
      <div 
        v-for="article in recentArticles" 
        :key="article.articleId"
        class="update-item"
        @click="navigateToArticle(article.articleId.toString())"
      >
        <!-- 分类图标 -->
        <div class="update-icon">{{ getCategoryIcon(article.category) }}</div>
        
        <!-- 文章内容信息 -->
        <div class="update-content">
          <span class="update-time">{{ formatDateTime(article.updateTime || article.publishDate) }}</span>
          <span class="update-title">{{ article.title }}</span>
          <span class="update-author">{{ article.author }}</span>
          <span v-if="article.category" class="update-category">{{ article.category }}</span>
        </div>
        
        <!-- 文章统计数据 -->
        <div class="update-stats">
          <span class="stat-item">👁 {{ article.views }}</span>
          <span class="stat-item">👍 {{ article.likes }}</span>
        </div>
      </div>
    </div>
    
    <!-- 空数据状态 -->
    <div v-if="!loading && !error && recentArticles.length === 0" class="empty-container">
      <span>📝 暂无最近更新</span>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 最近更新组件脚本
 * 
 * 该脚本处理最近更新文章的数据获取、状态管理和用户交互。
 * 使用 Composition API 管理组件状态，提供响应式的数据更新体验。
 */

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi, type Article } from '../api/article'

// 路由实例
const router = useRouter()

// 响应式数据定义
const recentArticles = ref<Article[]>([]) // 最近文章列表
const loading = ref(true)                 // 加载状态标识
const error = ref('')                     // 错误信息

/**
 * 导航到文章详情页
 * @param articleId 文章ID
 */
const navigateToArticle = (articleId: string) => {
  router.push(`/article/${articleId}`)
}

/**
 * 根据文章分类获取对应的图标
 * @param category 文章分类名称
 * @returns 对应的表情符号图标
 */
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

/**
 * 格式化日期时间显示
 * 根据时间差显示相对时间或绝对时间
 * @param dateString 日期字符串
 * @returns 格式化后的时间显示文本
 */
const formatDateTime = (dateString: string): string => {
  try {
    const date = new Date(dateString)
    const now = new Date()
    const diffMs = now.getTime() - date.getTime()
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
    
    // 今天：显示具体时间
    if (diffDays === 0) {
      return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    } 
    // 昨天：显示"昨天 + 时间"
    else if (diffDays === 1) {
      return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    } 
    // 一周内：显示"X天前"
    else if (diffDays < 7) {
      return `${diffDays}天前`
    } 
    // 一周以上：显示具体日期
    else {
      return date.toLocaleDateString('zh-CN')
    }
  } catch {
    return '未知时间'
  }
}

/**
 * 加载最近更新的文章列表
 * 从API获取最新文章数据并更新组件状态
 */
const loadRecentArticles = async () => {
  try {
    loading.value = true
    error.value = ''
    
    // 获取最新文章（按更新时间排序，限制8条）
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

// 组件挂载时加载数据
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
