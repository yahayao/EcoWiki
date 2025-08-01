<!--
文章卡片组件
展示文章信息并提供交互功能
-->
<template>
  <div class="article-card">
    <!-- 文章封面/状态标识 -->
    <div class="card-header">
      <div v-if="showStatus && articleStatus" :class="['status-badge', articleStatus.toLowerCase()]">
        {{ getStatusText(articleStatus) }}
      </div>
      <div v-if="article.category" class="category-tag">
        {{ article.category }}
      </div>
    </div>

    <!-- 文章信息 -->
    <div class="card-content">
      <h3 class="article-title" @click="handleView">
        {{ article.title }}
      </h3>
      
      <div class="article-meta">
        <div class="author-info">
          <img 
            v-if="article.authorAvatar" 
            :src="article.authorAvatar" 
            :alt="article.author"
            class="author-avatar"
          >
          <div v-else class="author-avatar-placeholder">
            {{ article.author.charAt(0).toUpperCase() }}
          </div>
          <span class="author-name">{{ article.author }}</span>
        </div>
        
        <div class="publish-date">
          {{ formatDate(article.publishDate) }}
        </div>
      </div>

      <div class="article-excerpt">
        {{ getExcerpt(article.content) }}
      </div>

      <div class="article-tags" v-if="article.tags">
        <span 
          v-for="tag in getTagsList(article.tags)" 
          :key="tag"
          class="tag"
        >
          {{ tag }}
        </span>
      </div>
    </div>

    <!-- 统计和操作 -->
    <div class="card-footer">
      <div class="stats">
        <div class="stat-item">
          <i class="icon-eye"></i>
          <span>{{ formatNumber(article.views) }}</span>
        </div>
        <div class="stat-item">
          <i class="icon-thumbs-up"></i>
          <span>{{ formatNumber(article.likes) }}</span>
        </div>
        <div class="stat-item">
          <i class="icon-comment"></i>
          <span>{{ formatNumber(article.comments) }}</span>
        </div>
      </div>

      <div v-if="showActions" class="actions">
        <!-- 点赞按钮 -->
        <button 
          :class="['action-btn', 'like-btn', { active: isLiked }]"
          @click="handleLikeToggle"
          :title="isLiked ? '取消点赞' : '点赞'"
        >
          <i class="icon-heart"></i>
        </button>

        <!-- 收藏按钮 -->
        <button 
          :class="['action-btn', 'favorite-btn', { active: isFavorited }]"
          @click="handleFavoriteToggle"
          :title="isFavorited ? '取消收藏' : '收藏'"
        >
          <i class="icon-bookmark"></i>
        </button>

        <!-- 编辑按钮 (仅自己的文章) -->
        <button 
          v-if="canEdit"
          class="action-btn edit-btn"
          @click="handleEdit"
          title="编辑"
        >
          <i class="icon-edit"></i>
        </button>

        <!-- 删除按钮 (仅自己的文章) -->
        <button 
          v-if="canDelete"
          class="action-btn delete-btn"
          @click="handleDelete"
          title="删除"
        >
          <i class="icon-trash"></i>
        </button>

        <!-- 更多操作 -->
        <div class="dropdown" ref="dropdownRef">
          <button 
            class="action-btn more-btn"
            @click="toggleDropdown"
            title="更多操作"
          >
            <i class="icon-more"></i>
          </button>
          
          <div v-if="showDropdown" class="dropdown-menu">
            <button @click="copyLink" class="dropdown-item">
              <i class="icon-link"></i>
              复制链接
            </button>
            <button @click="shareArticle" class="dropdown-item">
              <i class="icon-share"></i>
              分享文章
            </button>
            <button @click="reportArticle" class="dropdown-item">
              <i class="icon-flag"></i>
              举报
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import type { Article } from '@/api/article'
import { articleApi } from '@/api/article'

interface Props {
  article: Article
  showActions?: boolean
  showStatus?: boolean
  canEdit?: boolean
  canDelete?: boolean
  status?: string // 文章状态，用于显示状态标签
}

const props = withDefaults(defineProps<Props>(), {
  showActions: true,
  showStatus: false,
  canEdit: true,
  canDelete: true
})

const emit = defineEmits<{
  like: [article: Article]
  unlike: [article: Article]
  favorite: [article: Article]
  unfavorite: [article: Article]
  edit: [article: Article]
  delete: [article: Article]
  view: [article: Article]
}>()

const router = useRouter()
const dropdownRef = ref<HTMLElement>()
const showDropdown = ref(false)
const isLiked = ref(false)
const isFavorited = ref(false)

// 计算属性
const currentUser = computed(() => {
  // 这里应该从用户状态管理中获取当前用户
  return JSON.parse(localStorage.getItem('user') || '{}')
})

const articleStatus = computed(() => {
  return props.status || 'published'
})

// 方法
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const week = 7 * day
  const month = 30 * day
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < week) {
    return `${Math.floor(diff / day)}天前`
  } else if (diff < month) {
    return `${Math.floor(diff / week)}周前`
  } else {
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  }
}

const formatNumber = (num: number) => {
  if (num < 1000) return num.toString()
  if (num < 10000) return (num / 1000).toFixed(1) + 'k'
  return (num / 10000).toFixed(1) + 'w'
}

const getExcerpt = (content: string, maxLength = 120) => {
  // 移除HTML标签和特殊字符
  const plainText = content.replace(/<[^>]*>/g, '').replace(/[#*`]/g, '')
  if (plainText.length <= maxLength) return plainText
  return plainText.substring(0, maxLength) + '...'
}

const getTagsList = (tagsString: string): string[] => {
  if (!tagsString) return []
  return tagsString.split(',').map(tag => tag.trim()).filter(tag => tag)
}

const getStatusText = (status?: string) => {
  switch (status?.toUpperCase()) {
    case 'PUBLISHED': return '已发布'
    case 'DRAFT': return '草稿'
    case 'REVIEW': return '审核中'
    case 'REJECTED': return '已拒绝'
    default: return status
  }
}

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

const closeDropdown = (event: Event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target as Node)) {
    showDropdown.value = false
  }
}

// 文章操作
const handleView = () => {
  emit('view', props.article)
}

const handleLikeToggle = async () => {
  if (isLiked.value) {
    await handleUnlike()
  } else {
    await handleLike()
  }
}

const handleLike = async () => {
  try {
    isLiked.value = true
    emit('like', props.article)
  } catch (error) {
    isLiked.value = false
    console.error('点赞失败:', error)
  }
}

const handleUnlike = async () => {
  try {
    isLiked.value = false
    emit('unlike', props.article)
  } catch (error) {
    isLiked.value = true
    console.error('取消点赞失败:', error)
  }
}

const handleFavoriteToggle = async () => {
  if (isFavorited.value) {
    await handleUnfavorite()
  } else {
    await handleFavorite()
  }
}

const handleFavorite = async () => {
  try {
    isFavorited.value = true
    emit('favorite', props.article)
  } catch (error) {
    isFavorited.value = false
    console.error('收藏失败:', error)
  }
}

const handleUnfavorite = async () => {
  try {
    isFavorited.value = false
    emit('unfavorite', props.article)
  } catch (error) {
    isFavorited.value = true
    console.error('取消收藏失败:', error)
  }
}

const handleEdit = () => {
  emit('edit', props.article)
}

const handleDelete = () => {
  emit('delete', props.article)
}

const copyLink = async () => {
  try {
    const url = `${window.location.origin}/article/${props.article.articleId}`
    await navigator.clipboard.writeText(url)
    alert('链接已复制到剪贴板')
  } catch (error) {
    console.error('复制链接失败:', error)
    alert('复制失败，请手动复制')
  }
  showDropdown.value = false
}

const shareArticle = () => {
  if (navigator.share) {
    navigator.share({
      title: props.article.title,
      text: getExcerpt(props.article.content, 100),
      url: `${window.location.origin}/article/${props.article.articleId}`
    })
  } else {
    copyLink()
  }
  showDropdown.value = false
}

const reportArticle = () => {
  // 这里应该打开举报对话框
  alert('举报功能开发中...')
  showDropdown.value = false
}

// 检查点赞和收藏状态
const checkInteractionStatus = async () => {
  try {
    const [likedStatus, favoritedStatus] = await Promise.all([
      articleApi.checkLikeStatus(props.article.articleId),
      articleApi.checkFavoriteStatus(props.article.articleId)
    ])
    
    isLiked.value = likedStatus
    isFavorited.value = favoritedStatus
  } catch (error) {
    console.error('检查交互状态失败:', error)
  }
}

// 生命周期
onMounted(() => {
  document.addEventListener('click', closeDropdown)
  if (props.showActions) {
    checkInteractionStatus()
  }
})

onUnmounted(() => {
  document.removeEventListener('click', closeDropdown)
})
</script>

<style scoped>
.article-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.card-header {
  position: relative;
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  min-height: 60px;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 15px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-badge.published {
  background: #d4edda;
  color: #155724;
}

.status-badge.draft {
  background: #fff3cd;
  color: #856404;
}

.status-badge.review {
  background: #cce7ff;
  color: #004085;
}

.status-badge.rejected {
  background: #f8d7da;
  color: #721c24;
}

.category-tag {
  background: #e9ecef;
  color: #495057;
  padding: 0.25rem 0.75rem;
  border-radius: 15px;
  font-size: 0.75rem;
  font-weight: 500;
}

.card-content {
  padding: 0 1rem 1rem;
}

.article-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 1rem 0;
  line-height: 1.4;
  cursor: pointer;
  transition: color 0.2s;
}

.article-title:hover {
  color: #3498db;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.author-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.author-avatar-placeholder {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #3498db;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.9rem;
}

.author-name {
  font-weight: 500;
  color: #495057;
  font-size: 0.9rem;
}

.publish-date {
  color: #6c757d;
  font-size: 0.85rem;
}

.article-excerpt {
  color: #6c757d;
  line-height: 1.6;
  margin-bottom: 1rem;
  font-size: 0.95rem;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.tag {
  background: #f8f9fa;
  color: #495057;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  border: 1px solid #dee2e6;
}

.card-footer {
  padding: 1rem;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats {
  display: flex;
  gap: 1rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  color: #6c757d;
  font-size: 0.85rem;
}

.stat-item i {
  font-size: 0.9rem;
}

.actions {
  display: flex;
  gap: 0.5rem;
  position: relative;
}

.action-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #dee2e6;
  background: white;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  color: #6c757d;
}

.action-btn:hover {
  background: #f8f9fa;
  border-color: #adb5bd;
}

.action-btn.active {
  background: #3498db;
  border-color: #3498db;
  color: white;
}

.like-btn.active {
  background: #e74c3c;
  border-color: #e74c3c;
}

.favorite-btn.active {
  background: #f39c12;
  border-color: #f39c12;
}

.edit-btn:hover {
  background: #28a745;
  border-color: #28a745;
  color: white;
}

.delete-btn:hover {
  background: #dc3545;
  border-color: #dc3545;
  color: white;
}

.dropdown {
  position: relative;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  min-width: 120px;
  margin-top: 4px;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.75rem 1rem;
  border: none;
  background: none;
  color: #495057;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.2s;
  text-align: left;
}

.dropdown-item:hover {
  background: #f8f9fa;
}

.dropdown-item:first-child {
  border-radius: 6px 6px 0 0;
}

.dropdown-item:last-child {
  border-radius: 0 0 6px 6px;
}

@media (max-width: 768px) {
  .card-footer {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .stats {
    justify-content: center;
  }

  .actions {
    justify-content: center;
  }

  .article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
}
</style>
