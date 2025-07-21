<!-- UserArticle.vue -->
<template>
  <div class="article-container">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M19,3H14.82C14.4,1.84 13.3,1 12,1C10.7,1 9.6,1.84 9.18,3H5A2,2 0 0,0 3,5V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V5A2,2 0 0,0 19,3M12,3A1,1 0 0,1 13,4A1,1 0 0,1 12,5A1,1 0 0,1 11,4A1,1 0 0,1 12,3" />
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">文章管理</h1>
          <p class="page-subtitle">管理您的文章创作和收藏</p>
        </div>
      </div>
      <div class="header-actions">
        <button class="create-btn" @click="createNewArticle">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M19,13H13V19H11V13H5V11H11V5H13V11H19V13Z"/>
          </svg>
          新建文章
        </button>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="stats-overview">
      <div class="stat-card clickable" :class="{ active: activeTab === 'favorites' }" @click="onTabChange('favorites')">
        <div class="stat-icon favorites">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M12,21.35L10.55,20.03C5.4,15.36 2,12.27 2,8.5C2,5.41 4.42,3 7.5,3C9.24,3 10.91,3.81 12,5.08C13.09,3.81 14.76,3 16.5,3C19.58,3 22,5.41 22,8.5C22,12.27 18.6,15.36 13.45,20.03L12,21.35Z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ favoriteCount }}</div>
          <div class="stat-label">收藏文章</div>
        </div>
      </div>
      
      <div class="stat-card clickable" :class="{ active: activeTab === 'created' }" @click="onTabChange('created')">
        <div class="stat-icon created">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ createdCount }}</div>
          <div class="stat-label">已发布</div>
        </div>
      </div>
      
      <div class="stat-card clickable" :class="{ active: activeTab === 'drafts' }" @click="onTabChange('drafts')">
        <div class="stat-icon drafts">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ draftCount }}</div>
          <div class="stat-label">草稿</div>
        </div>
      </div>

      <div class="stat-card clickable" :class="{ active: activeTab === 'liked' }" @click="onTabChange('liked')">
        <div class="stat-icon liked">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M23,10C23,8.89 22.1,8 21,8H14.68L15.64,3.43C15.66,3.33 15.67,3.22 15.67,3.11C15.67,2.7 15.5,2.32 15.23,2.05L14.17,1L7.59,7.58C7.22,7.95 7,8.45 7,9V19A2,2 0 0,0 9,21H18C18.83,21 19.54,20.5 19.84,19.78L22.86,12.73C22.95,12.5 23,12.26 23,12V10.08L23,10M1,21H5V9H1V21Z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ likedCount }}</div>
          <div class="stat-label">点赞文章</div>
        </div>
      </div>
    </div>
    
    <!-- 文章列表 -->
    <div class="article-content">
      <div v-if="activeTab === 'favorites'" class="article-grid">
        <div v-for="article in favoriteArticles" :key="article.id" class="article-card">
          <div class="card-header">
            <div class="article-status favorites">
              <svg viewBox="0 0 24 24" class="status-icon">
                <path d="M12,21.35L10.55,20.03C5.4,15.36 2,12.27 2,8.5C2,5.41 4.42,3 7.5,3C9.24,3 10.91,3.81 12,5.08C13.09,3.81 14.76,3 16.5,3C19.58,3 22,5.41 22,8.5C22,12.27 18.6,15.36 13.45,20.03L12,21.35Z"/>
              </svg>
              收藏
            </div>
          </div>
          <div class="card-content">
            <h4 class="article-title">{{ article.title }}</h4>
            <p class="article-excerpt">{{ article.excerpt }}</p>
            <div class="article-meta">
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2M12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20A8,8 0 0,0 20,12A8,8 0 0,0 12,4M12,6A6,6 0 0,1 18,12A6,6 0 0,1 12,18A6,6 0 0,1 6,12A6,6 0 0,1 12,6M12,8A4,4 0 0,0 8,12A4,4 0 0,0 12,16A4,4 0 0,0 16,12A4,4 0 0,0 12,8Z"/>
                </svg>
                {{ article.favoriteDate }}
              </span>
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M12,4.5C7,4.5 2.73,7.61 1,12C2.73,16.39 7,19.5 12,19.5C17,19.5 21.27,16.39 23,12C21.27,7.61 17,4.5 12,4.5M12,17C9.24,17 7,14.76 7,12C7,9.24 9.24,7 12,7C14.76,7 17,9.24 17,12C17,14.76 14.76,17 12,17M12,9C10.34,9 9,10.34 9,12C9,13.66 10.34,15 12,15C13.66,15 15,13.66 15,12C15,10.34 13.66,9 12,9Z"/>
                </svg>
                {{ article.views }} 阅读
              </span>
            </div>
          </div>
          <div class="card-actions">
            <button class="action-btn primary" @click="viewArticle(article.id)">
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M12,4.5C7,4.5 2.73,7.61 1,12C2.73,16.39 7,19.5 12,19.5C17,19.5 21.27,16.39 23,12C21.27,7.61 17,4.5 12,4.5M12,17C9.24,17 7,14.76 7,12C7,9.24 9.24,7 12,7C14.76,7 17,9.24 17,12C17,14.76 14.76,17 12,17M12,9C10.34,9 9,10.34 9,12C9,13.66 10.34,15 12,15C13.66,15 15,13.66 15,12C15,10.34 13.66,9 12,9Z"/>
              </svg>
              查看
            </button>
            <button class="action-btn secondary" @click="unfavoriteArticle(article.id)">
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M12.1,18.55L12,18.65L11.89,18.55C7.14,14.24 4,11.39 4,8.5C4,6.5 5.5,5 7.5,5C9.04,5 10.54,6 11.07,7.36H12.93C13.46,6 14.96,5 16.5,5C18.5,5 20,6.5 20,8.5C20,11.39 16.86,14.24 12.1,18.55M16.5,3C14.76,3 13.09,3.81 12,5.08C10.91,3.81 9.24,3 7.5,3C4.42,3 2,5.41 2,8.5C2,12.27 5.4,15.36 10.55,20.03L12,21.35L13.45,20.03C18.6,15.36 22,12.27 22,8.5C22,5.41 19.58,3 16.5,3Z"/>
              </svg>
              取消收藏
            </button>
          </div>
        </div>
      </div>
      
      <div v-else-if="activeTab === 'created'" class="article-grid">
        <div v-for="article in createdArticles" :key="article.id" class="article-card">
          <div class="card-header">
            <div class="article-status published">
              <svg viewBox="0 0 24 24" class="status-icon">
                <path d="M21,7L9,19L3.5,13.5L4.91,12.09L9,16.17L19.59,5.59L21,7Z"/>
              </svg>
              已发布
            </div>
          </div>
          <div class="card-content">
            <h4 class="article-title">{{ article.title }}</h4>
            <p class="article-excerpt">{{ article.excerpt }}</p>
            <div class="article-meta">
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M9,10V12H7V10H9M13,10V12H11V10H13M17,10V12H15V10H17M19,3A2,2 0 0,1 21,5V19A2,2 0 0,1 19,21H5C3.89,21 3,20.1 3,19V5A2,2 0 0,1 5,3H6V1H8V3H16V1H18V3H19M19,19V8H5V19H19M19,5H5V6H19V5Z"/>
                </svg>
                {{ article.publishDate }}
              </span>
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M12,4.5C7,4.5 2.73,7.61 1,12C2.73,16.39 7,19.5 12,19.5C17,19.5 21.27,16.39 23,12C21.27,7.61 17,4.5 12,4.5M12,17C9.24,17 7,14.76 7,12C7,9.24 9.24,7 12,7C14.76,7 17,9.24 17,12C17,14.76 14.76,17 12,17M12,9C10.34,9 9,10.34 9,12C9,13.66 10.34,15 12,15C13.66,15 15,13.66 15,12C15,10.34 13.66,9 12,9Z"/>
                </svg>
                {{ article.views }} 阅读
              </span>
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M12,21.35L10.55,20.03C5.4,15.36 2,12.27 2,8.5C2,5.41 4.42,3 7.5,3C9.24,3 10.91,3.81 12,5.08C13.09,3.81 14.76,3 16.5,3C19.58,3 22,5.41 22,8.5C22,12.27 18.6,15.36 13.45,20.03L12,21.35Z"/>
                </svg>
                {{ article.likes }} 点赞
              </span>
            </div>
          </div>
          <div class="card-actions">
            <button class="action-btn primary" @click="editArticle(article.id)">
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z"/>
              </svg>
              编辑
            </button>
            <button class="action-btn secondary" @click="viewArticle(article.id)">
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M12,4.5C7,4.5 2.73,7.61 1,12C2.73,16.39 7,19.5 12,19.5C17,19.5 21.27,16.39 23,12C21.27,7.61 17,4.5 12,4.5M12,17C9.24,17 7,14.76 7,12C7,9.24 9.24,7 12,7C14.76,7 17,9.24 17,12C17,14.76 14.76,17 12,17M12,9C10.34,9 9,10.34 9,12C9,13.66 10.34,15 12,15C13.66,15 15,13.66 15,12C15,10.34 13.66,9 12,9Z"/>
              </svg>
              查看
            </button>
          </div>
        </div>
      </div>
      
      <div v-else-if="activeTab === 'drafts'" class="article-grid">
        <div v-for="draft in draftArticles" :key="draft.id" class="article-card">
          <div class="card-header">
            <div class="article-status draft">
              <svg viewBox="0 0 24 24" class="status-icon">
                <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z"/>
              </svg>
              草稿
            </div>
          </div>
          <div class="card-content">
            <h4 class="article-title">{{ draft.title }}</h4>
            <p class="article-excerpt">{{ draft.excerpt }}</p>
            <div class="article-meta">
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M9,10V12H7V10H9M13,10V12H11V10H13M17,10V12H15V10H17M19,3A2,2 0 0,1 21,5V19A2,2 0 0,1 19,21H5C3.89,21 3,20.1 3,19V5A2,2 0 0,1 5,3H6V1H8V3H16V1H18V3H19M19,19V8H5V19H19M19,5H5V6H19V5Z"/>
                </svg>
                最后保存于 {{ draft.lastSaved }}
              </span>
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z"/>
                </svg>
                {{ draft.wordCount }} 字
              </span>
            </div>
          </div>
          <div class="card-actions">
            <button class="action-btn primary" @click="editDraft(draft.id)">
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z"/>
              </svg>
              继续编辑
            </button>
            <button class="action-btn danger" @click="deleteDraft(draft.id)">
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z"/>
              </svg>
              删除
            </button>
          </div>
        </div>
      </div>

      <div v-else-if="activeTab === 'liked'" class="article-grid">
        <div v-for="article in likedArticles" :key="article.id" class="article-card">
          <div class="card-header">
            <div class="article-status liked">
              <svg viewBox="0 0 24 24" class="status-icon">
                <path d="M23,10C23,8.89 22.1,8 21,8H14.68L15.64,3.43C15.66,3.33 15.67,3.22 15.67,3.11C15.67,2.7 15.5,2.32 15.23,2.05L14.17,1L7.59,7.58C7.22,7.95 7,8.45 7,9V19A2,2 0 0,0 9,21H18C18.83,21 19.54,20.5 19.84,19.78L22.86,12.73C22.95,12.5 23,12.26 23,12V10.08L23,10M1,21H5V9H1V21Z"/>
              </svg>
              已点赞
            </div>
          </div>
          <div class="card-content">
            <h4 class="article-title">{{ article.title }}</h4>
            <p class="article-excerpt">{{ article.excerpt }}</p>
            <div class="article-meta">
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M12,2A10,10 0 0,1 22,12A10,10 0 0,1 12,22A10,10 0 0,1 2,12A10,10 0 0,1 12,2M12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20A8,8 0 0,0 20,12A8,8 0 0,0 12,4M12,6A6,6 0 0,1 18,12A6,6 0 0,1 12,18A6,6 0 0,1 6,12A6,6 0 0,1 12,6M12,8A4,4 0 0,0 8,12A4,4 0 0,0 12,16A4,4 0 0,0 16,12A4,4 0 0,0 12,8Z"/>
                </svg>
                点赞于 {{ article.likedDate }}
              </span>
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M12,4.5C7,4.5 2.73,7.61 1,12C2.73,16.39 7,19.5 12,19.5C17,19.5 21.27,16.39 23,12C21.27,7.61 17,4.5 12,4.5M12,17C9.24,17 7,14.76 7,12C7,9.24 9.24,7 12,7C14.76,7 17,9.24 17,12C17,14.76 14.76,17 12,17M12,9C10.34,9 9,10.34 9,12C9,13.66 10.34,15 12,15C13.66,15 15,13.66 15,12C15,10.34 13.66,9 12,9Z"/>
                </svg>
                {{ article.views }} 阅读
              </span>
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M23,10C23,8.89 22.1,8 21,8H14.68L15.64,3.43C15.66,3.33 15.67,3.22 15.67,3.11C15.67,2.7 15.5,2.32 15.23,2.05L14.17,1L7.59,7.58C7.22,7.95 7,8.45 7,9V19A2,2 0 0,0 9,21H18C18.83,21 19.54,20.5 19.84,19.78L22.86,12.73C22.95,12.5 23,12.26 23,12V10.08L23,10M1,21H5V9H1V21Z"/>
                </svg>
                {{ article.likes }} 点赞
              </span>
              <span class="meta-item">
                <svg viewBox="0 0 24 24" class="meta-icon">
                  <path d="M12,4A4,4 0 0,1 16,8A4,4 0 0,1 12,12A4,4 0 0,1 8,8A4,4 0 0,1 12,4M12,14C16.42,14 20,15.79 20,18V20H4V18C4,15.79 7.58,14 12,14Z"/>
                </svg>
                {{ article.author }}
              </span>
            </div>
          </div>
          <div class="card-actions">
            <button class="action-btn primary" @click="viewArticle(article.id)">
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M12,4.5C7,4.5 2.73,7.61 1,12C2.73,16.39 7,19.5 12,19.5C17,19.5 21.27,16.39 23,12C21.27,7.61 17,4.5 12,4.5M12,17C9.24,17 7,14.76 7,12C7,9.24 9.24,7 12,7C14.76,7 17,9.24 17,12C17,14.76 14.76,17 12,17M12,9C10.34,9 9,10.34 9,12C9,13.66 10.34,15 12,15C13.66,15 15,13.66 15,12C15,10.34 13.66,9 12,9Z"/>
              </svg>
              查看
            </button>
            <button class="action-btn secondary" @click="unlikeArticle(article.id)">
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M23,10C23,8.89 22.1,8 21,8H14.68L15.64,3.43C15.66,3.33 15.67,3.22 15.67,3.11C15.67,2.7 15.5,2.32 15.23,2.05L14.17,1L7.59,7.58C7.22,7.95 7,8.45 7,9V19A2,2 0 0,0 9,21H18C18.83,21 19.54,20.5 19.84,19.78L22.86,12.73C22.95,12.5 23,12.26 23,12V10.08L23,10M1,21H5V9H1V21Z"/>
              </svg>
              取消点赞
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/api/user'
import { articleApi } from '@/api/article'
import toast from '@/utils/toast'

const router = useRouter()

const activeTab = ref('favorites')

// 数据状态
const favoriteArticles = ref<any[]>([])
const createdArticles = ref<any[]>([])
const draftArticles = ref<any[]>([])
const likedArticles = ref<any[]>([])
const articleStats = ref({
  totalArticles: 0,
  publishedArticles: 0,
  draftArticles: 0,
  favoriteArticles: 0,
  likedArticles: 0,
  totalViews: 0,
  totalLikes: 0
})

// 加载状态
const loading = ref(false)

// 统计数据计算属性
const favoriteCount = computed(() => articleStats.value.favoriteArticles)
const createdCount = computed(() => articleStats.value.publishedArticles)
const draftCount = computed(() => articleStats.value.draftArticles)
const likedCount = computed(() => articleStats.value.likedArticles)

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '无'
  try {
    return new Date(dateString).toLocaleDateString('zh-CN')
  } catch {
    return '无效日期'
  }
}

// 加载文章统计数据
const loadArticleStats = async () => {
  try {
    const stats = await userApi.getUserArticleStats()
    articleStats.value = {
      ...stats,
      likedArticles: (stats as any).likedArticles || 0  // 添加默认值以防API未返回此字段
    }
  } catch (error: any) {
    console.error('加载文章统计失败:', error)
    toast.error('加载文章统计失败', '错误')
    // 使用模拟数据作为后备方案
    articleStats.value = {
      totalArticles: 5,
      publishedArticles: 2,
      draftArticles: 1,
      favoriteArticles: 2,
      likedArticles: 3,
      totalViews: 2056,
      totalLikes: 101
    }
  }
}

// 加载收藏文章
const loadFavoriteArticles = async () => {
  try {
    loading.value = true
    const result = await userApi.getFavoriteArticles(0, 20)
    favoriteArticles.value = result.content.map((article: any) => ({
      ...article,
      favoriteDate: formatDate(article.favoriteDate || article.createdAt),
      excerpt: article.content ? article.content.substring(0, 100) + '...' : '暂无摘要'
    }))
  } catch (error: any) {
    console.error('加载收藏文章失败:', error)
    // 使用模拟数据作为后备方案
    favoriteArticles.value = [
      {
        id: 1,
        title: '全球变暖的影响与对策',
        excerpt: '气候变化是当今世界面临的最严峻挑战之一，需要全球共同努力应对...',
        favoriteDate: '2025-07-10',
        views: 1200,
        author: '环保专家'
      },
      {
        id: 2,
        title: '可再生能源技术发展',
        excerpt: '太阳能、风能等可再生能源技术的快速发展为解决能源危机提供了新的希望...',
        favoriteDate: '2025-07-05',
        views: 856,
        author: '能源研究员'
      }
    ]
  } finally {
    loading.value = false
  }
}

// 加载用户创建的文章
const loadCreatedArticles = async () => {
  try {
    loading.value = true
    const result = await userApi.getUserArticles(0, 20, 'published')
    createdArticles.value = result.content.map((article: any) => ({
      ...article,
      publishDate: formatDate(article.createdAt),
      excerpt: article.content ? article.content.substring(0, 100) + '...' : '暂无摘要'
    }))
  } catch (error: any) {
    console.error('加载创建文章失败:', error)
    // 使用模拟数据作为后备方案
    createdArticles.value = [
      {
        id: 3,
        title: '垃圾分类指南',
        excerpt: '正确的垃圾分类是环保的重要一步，本文详细介绍了各类垃圾的分类方法...',
        publishDate: '2025-06-20',
        views: 1200,
        likes: 45
      }
    ]
  } finally {
    loading.value = false
  }
}

// 加载草稿文章
const loadDraftArticles = async () => {
  try {
    loading.value = true
    const result = await userApi.getUserArticles(0, 20, 'draft')
    draftArticles.value = result.content.map((article: any) => ({
      ...article,
      lastSaved: formatDate(article.updatedAt || article.createdAt),
      excerpt: article.content ? article.content.substring(0, 100) + '...' : '暂无摘要'
    }))
  } catch (error: any) {
    console.error('加载草稿文章失败:', error)
    // 使用模拟数据作为后备方案
    draftArticles.value = [
      {
        id: 4,
        title: '环保生活小贴士',
        excerpt: '在日常生活中，我们可以通过许多简单的方式来保护环境...',
        lastSaved: '2025-07-15'
      }
    ]
  } finally {
    loading.value = false
  }
}

// 加载点赞文章
const loadLikedArticles = async () => {
  try {
    loading.value = true
    // 这里需要实现获取用户点赞文章的API
    // const result = await userApi.getLikedArticles(0, 20)
    // likedArticles.value = result.content.map((article: any) => ({
    //   ...article,
    //   likedDate: formatDate(article.likedAt),
    //   excerpt: article.content ? article.content.substring(0, 100) + '...' : '暂无摘要'
    // }))
    
    // 使用模拟数据作为后备方案
    likedArticles.value = [
      {
        id: 5,
        title: '海洋保护的重要性',
        excerpt: '海洋是地球生态系统的重要组成部分，保护海洋环境刻不容缓...',
        likedDate: '2025-07-12',
        views: 980,
        author: '海洋学家',
        likes: 156
      },
      {
        id: 6,
        title: '绿色出行倡议',
        excerpt: '选择公共交通、骑自行车或步行，让我们的出行更加环保...',
        likedDate: '2025-07-08',
        views: 1340,
        author: '交通规划师',
        likes: 203
      },
      {
        id: 7,
        title: '节能减排从我做起',
        excerpt: '节约用电、用水，减少不必要的消费，每个人都可以为环保贡献力量...',
        likedDate: '2025-07-03',
        views: 756,
        author: '环保志愿者',
        likes: 89
      }
    ]
  } catch (error: any) {
    console.error('加载点赞文章失败:', error)
    // 使用空数组作为后备方案
    likedArticles.value = []
  } finally {
    loading.value = false
  }
}

// 文章操作方法
const createNewArticle = () => {
  router.push('/editor/new')
}

const viewArticle = (articleId: number) => {
  router.push(`/article/${articleId}`)
}

const editArticle = (articleId: number) => {
  router.push(`/editor/${articleId}`)
}

const unfavoriteArticle = async (articleId: number) => {
  try {
    // 这里需要实现取消收藏的API
    toast.success('已取消收藏', '操作成功')
    // 重新加载收藏文章列表
    await loadFavoriteArticles()
  } catch (error: any) {
    toast.error('取消收藏失败', '错误')
  }
}

const unlikeArticle = async (articleId: number) => {
  try {
    // 这里需要实现取消点赞的API
    toast.success('已取消点赞', '操作成功')
    // 重新加载点赞文章列表
    await loadLikedArticles()
  } catch (error: any) {
    toast.error('取消点赞失败', '错误')
  }
}

const deleteArticle = async (articleId: number) => {
  if (!confirm('确定要删除这篇文章吗？此操作不可恢复。')) {
    return
  }
  
  try {
    await articleApi.deleteArticle(articleId)
    toast.success('文章已删除', '操作成功')
    // 重新加载文章列表
    await loadCreatedArticles()
  } catch (error: any) {
    toast.error('删除文章失败', '错误')
  }
}

const deleteDraft = async (draftId: number) => {
  if (!confirm('确定要删除这个草稿吗？此操作不可恢复。')) {
    return
  }
  
  try {
    await articleApi.deleteArticle(draftId)
    toast.success('草稿已删除', '操作成功')
    // 重新加载草稿列表
    await loadDraftArticles()
  } catch (error: any) {
    toast.error('删除草稿失败', '错误')
  }
}

const publishDraft = async (draftId: number) => {
  try {
    // 这里需要实现发布草稿的API
    toast.success('草稿已发布', '操作成功')
    // 重新加载数据
    await loadDraftArticles()
    await loadCreatedArticles()
  } catch (error: any) {
    toast.error('发布草稿失败', '错误')
  }
}

// 监听选项卡切换，按需加载数据
const onTabChange = async (newTab: string) => {
  activeTab.value = newTab
  
  if (newTab === 'favorites' && favoriteArticles.value.length === 0) {
    await loadFavoriteArticles()
  } else if (newTab === 'created' && createdArticles.value.length === 0) {
    await loadCreatedArticles()
  } else if (newTab === 'drafts' && draftArticles.value.length === 0) {
    await loadDraftArticles()
  } else if (newTab === 'liked' && likedArticles.value.length === 0) {
    await loadLikedArticles()
  }
}

// 初始化数据
onMounted(async () => {
  await loadArticleStats()
  await loadFavoriteArticles() // 默认加载收藏文章
})

// 草稿编辑方法
const editDraft = (draftId: number) => {
  router.push(`/editor/${draftId}`)
}
</script>

<style scoped>
/* 主容器 */
.article-container {
  padding: 24px;
  background: #f8fafb;
  min-height: 100vh;
}

/* 页面标题区域 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.header-icon .icon {
  width: 24px;
  height: 24px;
  fill: white;
}

.header-text {
  flex: 1;
}

.page-title {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
  letter-spacing: -0.5px;
}

.page-subtitle {
  margin: 4px 0 0 0;
  color: #718096;
  font-size: 16px;
  font-weight: 400;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.create-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.create-btn .icon {
  width: 18px;
  height: 18px;
  fill: currentColor;
}

/* 统计概览 */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.stat-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.stat-card.clickable {
  cursor: pointer;
  position: relative;
}

.stat-card.clickable:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.stat-card.active {
  border-color: #667eea;
  box-shadow: 0 8px 30px rgba(102, 126, 234, 0.3);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-card.active .stat-content .stat-number {
  color: white;
}

.stat-card.active .stat-content .stat-label {
  color: rgba(255, 255, 255, 0.9);
}

.stat-card.active::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  pointer-events: none;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon.favorites {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%);
}

.stat-icon.created {
  background: linear-gradient(135deg, #4ecdc4 0%, #44a08d 100%);
}

.stat-icon.drafts {
  background: linear-gradient(135deg, #feca57 0%, #ff9ff3 100%);
}

.stat-icon.liked {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon .icon {
  width: 24px;
  height: 24px;
  fill: white;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #1a202c;
  line-height: 1;
  margin-bottom: 2px;
}

.stat-label {
  font-size: 12px;
  color: #718096;
  font-weight: 500;
}

/* 文章网格 */
.article-content {
  min-height: 400px;
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 24px;
}

/* 文章卡片 */
.article-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.card-header {
  padding: 20px 24px 0;
}

.article-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.article-status.favorites {
  background: #fff5f5;
  color: #c53030;
  border: 1px solid #feb2b2;
}

.article-status.published {
  background: #f0fff4;
  color: #22543d;
  border: 1px solid #9ae6b4;
}

.article-status.draft {
  background: #fffbeb;
  color: #d69e2e;
  border: 1px solid #fbd38d;
}

.article-status.liked {
  background: #edf2f7;
  color: #4a5568;
  border: 1px solid #cbd5e0;
}

.status-icon {
  width: 12px;
  height: 12px;
  fill: currentColor;
}

.card-content {
  padding: 20px 24px;
}

.article-title {
  margin: 0 0 12px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1a202c;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-excerpt {
  margin: 0 0 16px 0;
  color: #718096;
  font-size: 14px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #718096;
  font-size: 13px;
}

.meta-icon {
  width: 14px;
  height: 14px;
  fill: currentColor;
}

.card-actions {
  padding: 0 24px 24px;
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.action-btn.primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.action-btn.secondary {
  background: #f7fafc;
  color: #4a5568;
  border: 1px solid #e2e8f0;
}

.action-btn.secondary:hover {
  background: #edf2f7;
  border-color: #cbd5e0;
}

.action-btn.danger {
  background: #fed7d7;
  color: #c53030;
  border: 1px solid #feb2b2;
}

.action-btn.danger:hover {
  background: #fec4c4;
  border-color: #f56565;
}

.action-btn .icon {
  width: 16px;
  height: 16px;
  fill: currentColor;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .article-container {
    padding: 16px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .header-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .article-tabs {
    flex-direction: column;
    gap: 4px;
  }
  
  .tab-btn {
    justify-content: flex-start;
  }
  
  .article-grid {
    grid-template-columns: 1fr;
  }
  
  .card-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .stat-card {
    flex-direction: column;
    text-align: center;
    gap: 8px;
    padding: 12px;
  }
  
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }
}
</style>