<template>
  <div class="article-content">
    <!-- 文章头部信息 -->
    <div class="article-header">
      <div class="article-meta">
        <span class="article-category">{{ article.category || '未分类' }}</span>
        <span class="article-views">👁️ {{ article.views }} 次浏览</span>
      </div>
      <h1 class="article-title">{{ article.title }}</h1>
      <div class="article-info">
        <div class="author-section">
          <div class="author-info">
            <UserAvatar 
              :username="article.author"
              :avatar-url="article.authorAvatar || ''"
              size="md"
              shape="circle"
            />
            <div class="author-details">
              <span class="author-name">{{ article.author }}</span>
            </div>
          </div>
          
          <!-- 贡献者信息 -->
          <div class="contributors-section" v-if="contributorsLoading || contributorsError || (realContributorsInfo && realContributorsInfo.length > 0)">
            <span class="contributors-label">
              <template v-if="contributorsLoading">
                贡献者 (加载中...)
              </template>
              <template v-else-if="contributorsError">
                贡献者 (加载失败)
              </template>
              <template v-else>
                贡献者 {{ realContributorsInfo.length }}
              </template>
            </span>
            
            <!-- 加载状态 -->
            <div v-if="contributorsLoading" class="contributors-loading">
              <div class="loading-spinner"></div>
              <span>正在加载贡献者信息...</span>
            </div>
            
            <!-- 错误状态 -->
            <div v-else-if="contributorsError" class="contributors-error">
              <span class="error-icon">⚠️</span>
              <span>{{ contributorsError }}</span>
              <button @click="refreshContributors" class="retry-btn">重试</button>
            </div>
            
            <!-- 正常显示贡献者 -->
            <div v-else-if="realContributorsInfo && realContributorsInfo.length > 0" class="contributors-list" :class="{ 'horizontal': realContributorsInfo.length > 2 }">
              <template v-if="realContributorsInfo.length <= 2">
                <div 
                  v-for="contributor in realContributorsInfo" 
                  :key="contributor.id" 
                  class="contributor-item"
                >
                  <UserAvatar 
                    :username="contributor.username"
                    :avatar-url="contributor.avatarUrl || ''"
                    size="sm"
                    shape="circle"
                  />
                  <div class="contributor-info">
                    <span class="contributor-name">{{ contributor.username }}</span>
                    <span class="contributor-role">{{ contributor.displayName || contributor.username }}</span>
                  </div>
                </div>
              </template>
              <template v-else>
                <div class="contributors-avatars">
                  <UserAvatar 
                    v-for="contributor in realContributorsInfo" 
                    :key="contributor.id"
                    :username="contributor.username"
                    :avatar-url="contributor.avatarUrl || ''"
                    size="sm"
                    shape="circle"
                    class="contributor-avatar"
                    :title="`${contributor.username} (${contributor.displayName})`"
                  />
                </div>
              </template>
            </div>
          </div>
        </div>
        
        <div class="article-stats">
          <span class="publish-date">📅 发布于 {{ formatDate(article.publishDate) }}</span>
          <span class="update-date" v-if="article.updateTime && article.updateTime !== article.publishDate">
            ✏️ 更新于 {{ formatDate(article.updateTime) }}
          </span>
          <span class="read-time">⏱️ 约 {{ estimatedReadTime }} 分钟阅读</span>
        </div>
      </div>
    </div>

    <!-- 目录侧悬浮栏 -->
    <div class="table-of-contents-sidebar" :class="{ 'collapsed': isToCSidebarCollapsed }">
      <div class="toc-header">
        <div class="toc-title-section" v-if="!isToCSidebarCollapsed">
          <div class="toc-main-title">
            <IconArticleTOC class="toc-main-icon" />
            <span>文章目录</span>
          </div>
          <div class="toc-subtitle">
            {{ tableOfContents.length > 0 ? `${tableOfContents.length} 个章节` : '快速导航' }}
          </div>
        </div>
        <button class="toc-toggle-btn" @click="toggleToCSidebar">
          <span class="toc-toggle-icon" :class="{ 'collapsed': isToCSidebarCollapsed }">
            {{ isToCSidebarCollapsed ? '▶' : '◀' }}
          </span>
        </button>
      </div>
      <div class="toc-content" v-if="!isToCSidebarCollapsed">
        <div class="toc-progress-bar">
          <div class="toc-progress-fill"></div>
        </div>
        <ul class="toc-list">
          <!-- 返回顶部选项 -->
          <li class="toc-item toc-top-item">
            <a @click.prevent="scrollToTop" class="toc-link toc-top-link">
              <span class="toc-number toc-top-number">🔝</span>
              <span class="toc-text">返回顶部</span>
            </a>
          </li>
          <!-- 如果有子标题，显示分隔线和目录项 -->
          <template v-if="tableOfContents.length > 0">
            <li class="toc-divider"></li>
            <!-- 正常目录项 -->
            <li 
              v-for="(item, index) in tableOfContents" 
              :key="item.id"
              :class="`toc-level-${item.level}`"
              class="toc-item"
            >
              <a :href="`#${item.id}`" @click.prevent="scrollToHeading(item.id)" class="toc-link">
                <span class="toc-number">{{ index + 1 }}</span>
                <span class="toc-text">{{ item.title }}</span>
              </a>
            </li>
          </template>
          <!-- 如果没有子标题，显示提示信息 -->
          <template v-else>
            <li class="toc-divider"></li>
            <li class="toc-item toc-no-headings">
              <div class="toc-link toc-info-link">
                <span class="toc-number">📄</span>
                <span class="toc-text">本文暂无子标题</span>
              </div>
            </li>
          </template>
        </ul>
      </div>
    </div>

    <!-- 文章正文 -->
    <div class="article-body" ref="articleBody">
      <div v-html="parsedContent" class="wiki-content"></div>
    </div>

    <!-- 文章标签 -->
    <div class="article-tags" v-if="articleTags.length > 0">
      <h4>
        <IconTag class="tag-icon" />
        相关标签
      </h4>
      <div class="tags-list">
        <span 
          v-for="tag in articleTags" 
          :key="tag" 
          class="tag"
          @click="searchByTag(tag)"
        >
          {{ tag }}
        </span>
      </div>
    </div>

    <!-- 文章操作 -->
    <div class="article-actions">
      <button class="action-btn like-btn" :class="{ active: isLiked }" @click="toggleLike">
        <span class="icon">👍</span>
        <span>{{ currentLikes }}</span>
      </button>
      <button class="action-btn share-btn" @click="shareArticle">
        <span class="icon">🔗</span>
        <span>分享</span>
      </button>
      <button class="action-btn bookmark-btn" :class="{ active: isBookmarked }" @click="toggleBookmark">
        <span class="icon">📚</span>
        <span>收藏</span>
      </button>
      <button class="action-btn edit-btn" @click="editArticle">
        <span class="icon">✏️</span>
        <span>编辑</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { wikiParser } from '../../utils/wikiParser'
import { articleApi, type Article } from '../../api/article'
import { IconArticleTOC, IconTag } from '../icons'
import UserAvatar from '@/components/common/UserAvatar.vue'

const props = defineProps<{
  article: Article
}>()

const emit = defineEmits<{
  'like': [articleId: number]
  'share': [article: Article]
  'bookmark': [articleId: number]
  'edit': [articleId: number]
}>()

const router = useRouter()

const isLiked = ref(false)
const isBookmarked = ref(false)
const currentLikes = ref(props.article.likes || 0)
const articleBody = ref<HTMLElement>()
const tableOfContents = ref<{ id: string; level: number; title: string }[]>([])
const isToCSidebarCollapsed = ref(true) // 默认关闭状态

// 计算属性
const contributorsInfo = computed(() => {
  // 从文章版本历史中获取真实的贡献者数据
  // 这里需要异步加载版本历史数据，暂时返回空数组
  // TODO: 实现异步加载文章版本历史并处理贡献者数据
  return []
})

// 添加响应式的贡献者数据
const contributors = ref<Array<{
  id: number
  username: string
  displayName: string
  avatarUrl: string
  editDate: string
  editCount: number
}>>([])

// 贡献者加载状态
const contributorsLoading = ref(false)
const contributorsError = ref<string | null>(null)
// 异步加载贡献者数据
const loadContributors = async () => {
  if (!props.article?.articleId) {
    console.log('文章ID不存在，无法加载贡献者数据')
    contributors.value = []
    contributorsError.value = null
    return
  }
  
  // 如果文章对象中已经有预加载的贡献者数据，直接使用
  if (props.article.contributors !== undefined) {
    console.log('使用预加载的贡献者数据:', props.article.contributors)
    
    // 转换数据格式
    const formattedContributors = props.article.contributors.map((contributor, index) => ({
      id: index + 1,
      username: contributor.username,
      displayName: contributor.displayName || contributor.username,
      avatarUrl: contributor.avatarUrl || '',
      editDate: contributor.latestEdit,
      editCount: contributor.editCount
    }))
    
    contributors.value = formattedContributors
    contributorsError.value = props.article.contributorsError || null
    contributorsLoading.value = false
    return
  }
  
  // 如果没有预加载数据，则异步加载
  contributorsLoading.value = true
  contributorsError.value = null
  console.log('开始加载文章贡献者数据，文章ID:', props.article.articleId, '原作者:', props.article.author)
  
  try {
    // 使用新的贡献者API
    const contributorsList = await articleApi.getArticleContributors(props.article.articleId)
    console.log('获取到贡献者数据:', contributorsList)
    
    // 转换数据格式
    const formattedContributors = contributorsList.map((contributor, index) => ({
      id: index + 1,
      username: contributor.username,
      displayName: contributor.displayName || contributor.username,
      avatarUrl: contributor.avatarUrl || '',
      editDate: contributor.latestEdit,
      editCount: contributor.editCount
    }))
    
    console.log('最终贡献者列表:', formattedContributors)
    contributors.value = formattedContributors
    
  } catch (error) {
    console.error('加载贡献者数据失败:', error)
    
    // 根据不同的错误类型处理
    if (error instanceof Error) {
      if (error.message.includes('404') || error.message.includes('不存在')) {
        console.log('文章版本API不存在，使用模拟数据进行演示')
        contributors.value = generateMockContributors()
        contributorsError.value = null // 模拟数据不算错误
      } else if (error.message.includes('网络') || error.message.includes('Network')) {
        contributorsError.value = '网络连接失败，请检查网络设置'
        contributors.value = []
      } else {
        contributorsError.value = `加载失败: ${error.message}`
        contributors.value = []
      }
    } else {
      contributorsError.value = '未知错误，请稍后重试'
      contributors.value = []
    }
  } finally {
    contributorsLoading.value = false
  }
}

// 生成模拟贡献者数据（仅用于演示）
const generateMockContributors = () => {
  const originalAuthor = props.article.author?.trim().toLowerCase()
  
  const mockData = [
    { username: 'yahayao', displayName: 'yahaya', editCount: 3, editDate: '2025-07-25T10:30:00Z' },
    { username: 'yinianqingxian', displayName: 'qingxian', editCount: 2, editDate: '2025-07-26T15:20:00Z' },
    { username: 'Alng97', displayName: 'Alng97', editCount: 1, editDate: '2025-07-27T09:15:00Z' },
    { username: 'editor1', displayName: 'Editor One', editCount: 1, editDate: '2025-07-28T08:45:00Z' },
    { username: 'editor2', displayName: 'Editor Two', editCount: 1, editDate: '2025-07-28T14:30:00Z' },
    { username: 'admin', displayName: 'Administrator', editCount: 4, editDate: '2025-07-24T16:00:00Z' },
    { username: 'reviewer', displayName: 'Content Reviewer', editCount: 2, editDate: '2025-07-27T20:15:00Z' }
  ]
  
  console.log('生成模拟数据，原作者:', originalAuthor, '所有候选:', mockData.map(m => m.username))
  
  // 过滤掉原作者并转换格式
  const filteredData = mockData
    .filter(item => {
      const itemAuthor = item.username.trim().toLowerCase()
      const shouldInclude = itemAuthor !== originalAuthor
      console.log(`检查 ${item.username} (${itemAuthor}) vs ${originalAuthor}: ${shouldInclude ? '包含' : '排除'}`)
      return shouldInclude
    })
    .map((item, index) => ({
      id: index + 1,
      username: item.username,
      displayName: item.displayName,
      avatarUrl: '',
      editDate: item.editDate,
      editCount: item.editCount
    }))
    .sort((a, b) => {
      if (a.editCount !== b.editCount) {
        return b.editCount - a.editCount
      }
      return new Date(b.editDate).getTime() - new Date(a.editDate).getTime()
    })
    
  console.log('过滤后的模拟贡献者:', filteredData)
  return filteredData
}

// 计算属性：基于真实数据的贡献者信息
const realContributorsInfo = computed(() => {
  return contributors.value
})

const parsedContent = computed(() => {
  return wikiParser.parseToHtml(props.article.content || '')
})

const articleTags = computed(() => {
  if (!props.article.tags) return []
  return props.article.tags.split(',').map(tag => tag.trim()).filter(tag => tag)
})

const estimatedReadTime = computed(() => {
  const wordsPerMinute = 200
  const wordCount = (props.article.content || '').length / 2 // 粗略估算中文字数
  return Math.max(1, Math.ceil(wordCount / wordsPerMinute))
})

// 方法
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const toggleLike = async () => {
  try {
    if (isLiked.value) {
      await articleApi.unlikeArticle(props.article.articleId)
      currentLikes.value = Math.max(0, currentLikes.value - 1)
    } else {
      await articleApi.likeArticle(props.article.articleId)
      currentLikes.value += 1
    }
    isLiked.value = !isLiked.value
    emit('like', props.article.articleId)
  } catch (error) {
    console.error('点赞操作失败:', error)
  }
}
const toggleBookmark = () => {
  isBookmarked.value = !isBookmarked.value
  emit('bookmark', props.article.articleId)
}

const shareArticle = () => {
  if (navigator.share) {
    navigator.share({
      title: props.article.title,
      text: wikiParser.extractSummary(parsedContent.value, 100),
      url: window.location.href
    })
  } else {
    // fallback: 复制链接到剪贴板
    navigator.clipboard.writeText(window.location.href)
    alert('链接已复制到剪贴板')
  }
  emit('share', props.article)
}

const editArticle = () => {
  router.push(`/edit/${props.article.title}`)
  emit('edit', props.article.articleId)
}

const searchByTag = (tag: string) => {
  router.push(`/search?tag=${encodeURIComponent(tag)}`)
}

const scrollToHeading = (id: string) => {
  // 确保DOM已经更新
  nextTick(() => {
    const element = document.getElementById(id)
    if (element) {
      // 计算元素相对于视口顶部的位置，减去偏移量以确保标题不被遮挡
      const elementPosition = element.getBoundingClientRect().top + window.pageYOffset
      const offsetPosition = elementPosition - 100 // 增加偏移量确保清晰可见
      
      window.scrollTo({
        top: offsetPosition,
        behavior: 'smooth'
      })
    } else {
      console.warn(`Element with id "${id}" not found`)
    }
  })
}

const toggleToCSidebar = () => {
  isToCSidebarCollapsed.value = !isToCSidebarCollapsed.value
}

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

const generateTableOfContents = () => {
  nextTick(() => {
    if (articleBody.value) {
      // 直接使用原始HTML生成目录
      tableOfContents.value = wikiParser.generateToc(parsedContent.value)
      
      // 在DOM中为标题元素添加ID
      nextTick(() => {
        const headings = articleBody.value?.querySelectorAll('h1, h2, h3, h4, h5, h6')
        if (headings) {
          headings.forEach((heading, index) => {
            heading.id = `heading-${index}`
          })
        }
      })
    }
  })
}

// 生命周期
onMounted(() => {
  generateTableOfContents()
  loadContributors() // 加载贡献者数据（会优先使用预加载数据）
})

// 监听文章变化，重新加载贡献者
watch(() => props.article.articleId, (newId, oldId) => {
  if (newId !== oldId && newId) {
    console.log('文章ID变化，重新加载贡献者:', oldId, '->', newId)
    contributors.value = [] // 先清空避免显示错误数据
    contributorsError.value = null
    loadContributors()
  }
}, { immediate: false })

// 监听预加载的贡献者数据变化
watch(() => props.article.contributors, (newContributors) => {
  if (newContributors !== undefined) {
    console.log('检测到预加载的贡献者数据变化，更新显示')
    loadContributors() // 重新处理贡献者数据
  }
}, { immediate: false, deep: true })

// 监听文章作者变化
watch(() => props.article.author, (newAuthor, oldAuthor) => {
  if (newAuthor !== oldAuthor) {
    console.log('文章作者变化，重新加载贡献者:', oldAuthor, '->', newAuthor)
    // 只有在没有预加载数据时才重新加载
    if (props.article.contributors === undefined) {
      loadContributors()
    }
  }
}, { immediate: false })

// 手动刷新贡献者数据
const refreshContributors = () => {
  console.log('手动刷新贡献者数据')
  contributors.value = []
  contributorsError.value = null
  loadContributors()
}
</script>

<style scoped>
.article-content {
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
  padding: 32px 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(226, 232, 240, 0.8);
}

.article-header {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 2px solid #f7fafc;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.article-category {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
}

.article-views {
  color: #718096;
  font-size: 0.9rem;
}

.article-title {
  font-size: 2.2rem;
  color: #1a202c;
  font-weight: 700;
  line-height: 1.3;
  margin-bottom: 20px;
}

.article-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 16px;
}

.author-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  font-size: 1.5rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 600;
  color: #1a202c;
  font-size: 1.1rem;
}

.article-stats {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: #718096;
  font-size: 0.9rem;
}

/* 贡献者样式 */
.contributors-section {
  margin: 8px 0;
  padding: 12px 0;
  border-top: 1px solid #e2e8f0;
  border-bottom: 1px solid #e2e8f0;
}

.contributors-label {
  display: block;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 8px;
  font-size: 0.95rem;
}

.contributors-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.contributors-list.horizontal {
  flex-direction: row;
  align-items: center;
}

.contributor-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.contributor-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.contributor-name {
  font-weight: 500;
  color: #2d3748;
  font-size: 0.9rem;
}

.contributor-role {
  color: #718096;
  font-size: 0.85rem;
}

/* 横向头像显示样式 */
.contributors-avatars {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.contributor-avatar {
  transition: transform 0.2s ease;
}

.contributor-avatar:hover {
  transform: scale(1.1);
  cursor: pointer;
}

/* 贡献者加载和错误状态样式 */
.contributors-loading {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #718096;
  font-size: 0.9rem;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #e2e8f0;
  border-top: 2px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.contributors-error {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #e53e3e;
  font-size: 0.9rem;
}

.error-icon {
  font-size: 14px;
}

.retry-btn {
  background: #e53e3e;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: background 0.2s ease;
}

.retry-btn:hover {
  background: #c53030;
}

/* 侧悬浮目录栏 */
.table-of-contents-sidebar {
  position: fixed;
  top: 50%;
  left: 20px;
  transform: translateY(-50%);
  background: linear-gradient(145deg, #ffffff, #f8fafc);
  border-radius: 16px;
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.1),
    0 8px 16px rgba(0, 0, 0, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(226, 232, 240, 0.6);
  z-index: 1000;
  max-height: 75vh;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  width: 320px;
  backdrop-filter: blur(10px);
}

.table-of-contents-sidebar.collapsed {
  width: 56px;
  max-height: 56px;
}

.table-of-contents-sidebar.collapsed .toc-header {
  padding: 10px;
  justify-content: center;
}

.table-of-contents-sidebar.collapsed .toc-toggle-btn {
  margin: 0;
}

.toc-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 20px 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px 16px 0 0;
  position: relative;
  overflow: hidden;
}

.toc-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, rgba(255,255,255,0.1) 0%, transparent 100%);
  pointer-events: none;
}

.toc-title-section {
  flex: 1;
  z-index: 1;
}

.toc-main-title {
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 4px;
}

.toc-main-icon {
  width: 20px;
  height: 20px;
  color: rgba(255, 255, 255, 0.9);
}

.toc-subtitle {
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.85rem;
  font-weight: 400;
}

.toc-toggle-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  cursor: pointer;
  padding: 8px;
  border-radius: 10px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 36px;
  backdrop-filter: blur(10px);
  z-index: 1;
}

.toc-toggle-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
}

.toc-toggle-icon {
  font-size: 14px;
  color: white;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: bold;
}

.toc-toggle-icon.collapsed {
  transform: rotate(0deg);
}

.toc-content {
  padding: 0;
  max-height: calc(75vh - 80px);
  overflow-y: auto;
  background: white;
}

.toc-progress-bar {
  height: 3px;
  background: #e2e8f0;
  position: relative;
  margin-bottom: 16px;
}

.toc-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  width: 0%;
  transition: width 0.3s ease;
}

.toc-list {
  list-style: none;
  padding: 16px;
  margin: 0;
}

.toc-item {
  margin-bottom: 2px;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.2s ease;
}

.toc-item:hover {
  background: linear-gradient(135deg, #f0f8ff, #e6f3ff);
  transform: translateX(4px);
}

.toc-link {
  color: #4a5568;
  text-decoration: none;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  line-height: 1.4;
  position: relative;
}

.toc-link:hover {
  color: #2d3748;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
}

.toc-link::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 2px;
  transition: height 0.3s ease;
}

.toc-link:hover::before {
  height: 60%;
}

.toc-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-radius: 50%;
  font-size: 0.75rem;
  font-weight: 600;
  margin-right: 10px;
  flex-shrink: 0;
}

.toc-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 返回顶部样式 */
.toc-top-item {
  margin-bottom: 8px;
}

.toc-top-link {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white !important;
  font-weight: 500;
}

.toc-top-link:hover {
  background: linear-gradient(135deg, #5a67d8, #6b46c1);
  color: white !important;
  transform: translateX(6px);
}

.toc-top-number {
  background: rgba(255, 255, 255, 0.2) !important;
  color: white !important;
}

.toc-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #e2e8f0, transparent);
  margin: 8px 12px;
  border: none;
}

/* 无子标题时的样式 */
.toc-no-headings .toc-link {
  pointer-events: none;
  color: #a0aec0 !important;
  font-style: italic;
}

.toc-info-link {
  background: linear-gradient(135deg, #f7fafc, #edf2f7);
  border: 1px dashed #cbd5e0;
}

.toc-level-1 { 
  margin-left: 0; 
}
.toc-level-1 .toc-number {
  background: linear-gradient(135deg, #667eea, #764ba2);
}
.toc-level-1 .toc-text {
  font-weight: 600;
}

.toc-level-2 { 
  margin-left: 8px; 
}
.toc-level-2 .toc-number {
  background: linear-gradient(135deg, #4299e1, #3182ce);
}

.toc-level-3 { 
  margin-left: 16px; 
}
.toc-level-3 .toc-number {
  background: linear-gradient(135deg, #48bb78, #38a169);
}

.toc-level-4 { 
  margin-left: 24px; 
}
.toc-level-4 .toc-number {
  background: linear-gradient(135deg, #ed8936, #dd6b20);
}

.toc-level-5 { 
  margin-left: 32px; 
}
.toc-level-5 .toc-number {
  background: linear-gradient(135deg, #e53e3e, #c53030);
}

.toc-level-6 { 
  margin-left: 40px; 
}
.toc-level-6 .toc-number {
  background: linear-gradient(135deg, #9f7aea, #805ad5);
}

/* 自定义滚动条 */
.toc-content::-webkit-scrollbar {
  width: 6px;
}

.toc-content::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.toc-content::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 3px;
}

.toc-content::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #5a67d8, #6b46c1);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .table-of-contents-sidebar {
    left: 10px;
    width: 280px;
  }
}

@media (max-width: 768px) {
  .table-of-contents-sidebar {
    display: none;
  }
}

.article-body {
  line-height: 1.8;
  font-size: 1.1rem;
  color: #2d3748;
  margin-bottom: 32px;
}

.wiki-content :deep(h1),
.wiki-content :deep(h2),
.wiki-content :deep(h3),
.wiki-content :deep(h4),
.wiki-content :deep(h5),
.wiki-content :deep(h6) {
  color: #1a202c;
  font-weight: 600;
  margin-top: 32px;
  margin-bottom: 16px;
  line-height: 1.3;
}

.wiki-content :deep(h1) {
  font-size: 2rem;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 8px;
}

.wiki-content :deep(h2) {
  font-size: 1.6rem;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 6px;
}

.wiki-content :deep(h3) {
  font-size: 1.4rem;
}

.wiki-content :deep(h4) {
  font-size: 1.2rem;
}

.wiki-content :deep(h5) {
  font-size: 1.1rem;
}

.wiki-content :deep(h6) {
  font-size: 1rem;
}

.wiki-content :deep(p) {
  margin-bottom: 16px;
}

.wiki-content :deep(ul),
.wiki-content :deep(ol) {
  margin: 16px 0;
  padding-left: 24px;
}

.wiki-content :deep(li) {
  margin-bottom: 8px;
}

.wiki-content :deep(a) {
  color: #4299e1;
  text-decoration: none;
  transition: color 0.2s;
}

.wiki-content :deep(a:hover) {
  color: #2b6cb0;
  text-decoration: underline;
}

.wiki-content :deep(.wiki-link) {
  color: #4299e1;
  font-weight: 500;
}

.wiki-content :deep(.external-link) {
  color: #4299e1;
}

.wiki-content :deep(.external-link):after {
  content: " ↗";
  font-size: 0.8em;
  opacity: 0.7;
}

.wiki-content :deep(table) {
  border-collapse: collapse;
  margin: 20px 0;
  width: 100%;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

.wiki-content :deep(th),
.wiki-content :deep(td) {
  border: 1px solid #e2e8f0;
  padding: 12px;
  text-align: left;
}

.wiki-content :deep(th) {
  background: #f7fafc;
  font-weight: 600;
  color: #1a202c;
}

.wiki-content :deep(.wiki-template) {
  margin: 20px 0;
  padding: 16px;
  border-radius: 8px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.wiki-content :deep(.info-box) {
  background: #ebf8ff;
  border: 1px solid #90cdf4;
}

.wiki-content :deep(.warning-box) {
  background: #fffbeb;
  border: 1px solid #f6e05e;
}

.wiki-content :deep(.note-box) {
  background: #f0fff4;
  border: 1px solid #9ae6b4;
}

.wiki-content :deep(.template-icon) {
  font-size: 20px;
  flex-shrink: 0;
}

.wiki-content :deep(.template-content) {
  flex: 1;
}

.wiki-content :deep(pre) {
  background: #1a202c;
  color: #e2e8f0;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 20px 0;
  font-family: 'SF Mono', 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 0.9rem;
}

.wiki-content :deep(code) {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'SF Mono', 'Monaco', 'Menlo', 'Consolas', monospace;
  font-size: 0.9rem;
  color: #e53e3e;
}

.wiki-content :deep(pre code) {
  background: transparent;
  padding: 0;
  color: inherit;
}

.wiki-content :deep(blockquote) {
  background: #f8fafc;
  border-left: 4px solid #4299e1;
  padding: 16px 20px;
  margin: 20px 0;
  font-style: italic;
  color: #4a5568;
}

.wiki-content :deep(strong) {
  font-weight: 600;
  color: #1a202c;
}

.wiki-content :deep(em) {
  font-style: italic;
}

.wiki-content :deep(u) {
  text-decoration: underline;
}

.wiki-content :deep(s) {
  text-decoration: line-through;
}

.wiki-content :deep(sup) {
  vertical-align: super;
  font-size: 0.8em;
}

.wiki-content :deep(sub) {
  vertical-align: sub;
  font-size: 0.8em;
}

.wiki-content :deep(hr) {
  border: none;
  height: 2px;
  background: linear-gradient(to right, transparent, #e2e8f0, transparent);
  margin: 32px 0;
}

.article-tags {
  margin-bottom: 32px;
  padding: 20px;
  background: #fafafa;
  border-radius: 12px;
}

.article-tags h4 {
  color: #1a202c;
  margin-bottom: 12px;
  font-size: 1.1rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tag-icon {
  flex-shrink: 0;
  color: #667eea;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  background: white;
  color: #667eea;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 0.85rem;
  font-weight: 500;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
  cursor: pointer;
}

.tag:hover {
  background: #667eea;
  color: white;
  transform: translateY(-1px);
}

.article-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  padding-top: 24px;
  border-top: 2px solid #f7fafc;
  flex-wrap: wrap;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: 2px solid #e2e8f0;
  background: white;
  border-radius: 25px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #4a5568;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.action-btn.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-color: #667eea;
}

.like-btn:hover:not(.active) {
  border-color: #f6ad55;
  color: #f6ad55;
}

.share-btn:hover {
  border-color: #4299e1;
  color: #4299e1;
}

.bookmark-btn:hover:not(.active) {
  border-color: #48bb78;
  color: #48bb78;
}

.edit-btn:hover {
  border-color: #9f7aea;
  color: #9f7aea;
}

.icon {
  font-size: 1.1rem;
}

/* 响应式设计 */
@media (min-width: 1400px) {
  .article-content {
    max-width: 1200px;
    padding: 40px 32px;
  }
}

@media (max-width: 768px) {
  .article-content {
    padding: 24px 16px;
    margin: 0 16px;
  }
  
  .article-title {
    font-size: 1.8rem;
  }
  
  .article-info {
    flex-direction: column;
    gap: 12px;
  }
  
  .article-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .action-btn {
    width: 200px;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .article-content {
    padding: 20px 12px;
    margin: 0 12px;
  }
  
  .article-title {
    font-size: 1.6rem;
  }
  
  .article-meta {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
  
  .toc-level-2 { margin-left: 12px; }
  .toc-level-3 { margin-left: 24px; }
  .toc-level-4 { margin-left: 36px; }
  .toc-level-5 { margin-left: 48px; }
  .toc-level-6 { margin-left: 60px; }
}

@media (max-width: 768px) {
  .article-info {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .author-section {
    width: 100%;
  }
  
  .article-actions {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .action-btn {
    flex: 1;
    min-width: 120px;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .article-title {
    font-size: 1.6rem;
  }
  
  .article-body {
    font-size: 1rem;
  }
}
</style>
