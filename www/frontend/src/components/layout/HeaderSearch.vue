<!--
  头部搜索组件
  
  该组件提供网站头部的搜索功能，允许用户快速搜索站内的知识内容。
  采用现代化的UI设计，支持键盘操作和鼠标点击两种搜索方式。
  
  主要功能：
  - 关键词搜索：支持输入搜索关键        <div class="dropdown-footer" v-if="hasMoreResults">
          <button class="view-more-btn" @click="loadMoreResults">
            加载更多结果 (还有 {{ totalResults - searchResults.value.length }} 个)
          </button>
        </div>检索
  - 多种触发方式：支持回车键和点击按钮两种搜索触发方式
  - 实时验证：自动过滤空白搜索词，确保搜索有效性
  - 事件传递：通过emit向父组件传递搜索事件
  - 响应式设计：适配不同屏幕尺寸的显示效果
  
  交互设计：
  - 占位符提示：清晰的搜索提示文本
  - 图标按钮：直观的搜索图标视觉引导
  - 键盘操作：回车键快捷搜索支持
  - 输入验证：自动过滤空白和无效输入
  
  视觉特点：
  - 现代化设计：圆角边框和阴影效果
  - 图标集成：使用SVG图标保证清晰度
  - 悬浮反馈：按钮悬浮状态的颜色变化
  - 响应式布局：自适应容器宽度
  
  技术实现：
  - Vue 3 Composition API
  - TypeScript 类型安全
  - 事件发射机制（defineEmits）
  - 双向数据绑定（v-model）
  - 键盘事件监听（@keyup.enter）
  
  使用场景：
  - 网站头部导航栏
  - 全站内容搜索入口
  - 快速查找功能
  - 用户内容发现
  
  扩展性：
  - 支持搜索建议和自动完成
  - 可添加搜索历史记录
  - 支持高级搜索选项
  - 可集成搜索结果预览
  
  @author EcoWiki Team
  @version 1.0.0
  @since 2024-01-01
-->
<template>
  <!-- 搜索栏容器 -->
  <div class="search-container" ref="searchContainer">
    <div class="search-wrapper">
      <!-- 搜索输入框 -->
      <input 
        type="text" 
        class="search-input" 
        placeholder="搜索知识内容..." 
        v-model="searchTerm"
        @keyup.enter="handleSearch"
        @input="handleInput"
        @focus="showDropdown = true"
        @blur="handleBlur"
      />
      
      <!-- 搜索按钮 -->
      <button class="search-button" @click="handleSearch" :disabled="!searchTerm.trim()">
        <svg v-if="!isSearching" width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M21 21L16.514 16.506L21 21ZM19 10.5C19 15.194 15.194 19 10.5 19C5.806 19 2 15.194 2 10.5C2 5.806 5.806 2 10.5 2C15.194 2 19 5.806 19 10.5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <!-- 加载动画 -->
        <div v-else class="loading-spinner"></div>
      </button>
    </div>

    <!-- 搜索结果下拉框 -->
    <div v-if="showDropdown && (searchResults.length > 0 || isSearching || showNoResults)" 
         class="search-dropdown" 
         @mousedown.prevent>
      <!-- 加载状态 -->
      <div v-if="isSearching" class="dropdown-loading">
        <div class="loading-spinner"></div>
        <span>搜索中...</span>
      </div>

      <!-- 搜索结果 -->
      <div v-else-if="searchResults.length > 0" class="search-results">
        <div class="results-header">
          <span class="results-count">找到 {{ totalResults }} 个结果</span>
        </div>
        
        <div class="results-list">
          <div 
            v-for="(article, index) in searchResults" 
            :key="article.id"
            class="result-item"
            :class="{ active: selectedIndex === index }"
            @click.stop="goToArticle(article.title)"
            @mouseenter="selectedIndex = index"
          >
            <div class="result-content">
              <h4 class="result-title">{{ article.title }}</h4>
              <p class="result-summary">{{ getArticleSummary(article.content) }}</p>
              <div class="result-meta">
                <span class="author">{{ article.author }}</span>
                <span class="date">{{ formatDate(article.publishDate) }}</span>
                <span v-if="article.category" class="category">{{ article.category }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="dropdown-footer" v-if="hasMoreResults">
          <button class="view-more-btn" @click.stop="showMoreResults" :disabled="isLoadingMore">
            <div v-if="isLoadingMore" class="loading-spinner"></div>
            <span v-if="!isLoadingMore">查看更多结果 ({{ totalResults - searchResults.length }}+)</span>
            <span v-else>加载中...</span>
          </button>
        </div>
      </div>

      <!-- 无结果提示 -->
      <div v-else-if="showNoResults" class="no-results">
        <div class="no-results-icon">🔍</div>
        <p>没有找到相关结果</p>
        <span class="no-results-tip">尝试使用不同的关键词</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 头部搜索组件脚本
 * 
 * 该脚本处理搜索输入的状态管理和事件处理。
 * 提供搜索词验证和事件发射功能。
 */

import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '../../api/article'
import type { Article } from '../../types/article'

// 定义组件事件类型
const emit = defineEmits<{
  search: [term: string]  // 搜索事件，传递搜索关键词
}>()

// 路由
const router = useRouter()

// DOM 引用
const searchContainer = ref<HTMLElement>()

// 响应式数据定义
const searchTerm = ref('')  // 当前搜索关键词
const searchResults = ref<Article[]>([])  // 搜索结果
const totalResults = ref(0)  // 总结果数
const isSearching = ref(false)  // 搜索加载状态
const isLoadingMore = ref(false)  // 加载更多结果的状态
const showDropdown = ref(false)  // 显示下拉框
const selectedIndex = ref(-1)  // 键盘选择的索引
const searchTimeout = ref<number>()  // 防抖定时器

// 计算属性
const showNoResults = computed(() => 
  searchTerm.value.trim() && !isSearching.value && searchResults.value.length === 0
)

const hasMoreResults = computed(() => 
  totalResults.value > searchResults.value.length
)

/**
 * 处理输入事件 - 实时搜索
 */
const handleInput = () => {
  const keyword = searchTerm.value.trim()
  
  // 清除之前的定时器
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }
  
  // 如果输入为空，清空结果
  if (!keyword) {
    searchResults.value = []
    totalResults.value = 0
    showDropdown.value = false
    return
  }
  
  // 防抖搜索 - 500ms 后执行
  searchTimeout.value = setTimeout(() => {
    performSearch(keyword, true)
  }, 500)
}

/**
 * 执行搜索
 */
const performSearch = async (keyword: string, isDropdown = false) => {
  if (!keyword.trim()) return
  
  isSearching.value = true
  showDropdown.value = isDropdown
  
  try {
    // 下拉框只显示前5个结果
    const pageSize = isDropdown ? 5 : 10
    const result = await articleApi.searchArticles(keyword, 0, pageSize)
    
    searchResults.value = result.content
    totalResults.value = result.totalElements
    selectedIndex.value = -1
    
    if (isDropdown) {
      showDropdown.value = true
    }
  } catch (error) {
    console.error('搜索失败:', error)
    searchResults.value = []
    totalResults.value = 0
  } finally {
    isSearching.value = false
  }
}

/**
 * 处理搜索操作 - 回车或点击搜索按钮
 */
const handleSearch = () => {
  const keyword = searchTerm.value.trim()
  if (keyword) {
    // 如果下拉框中有选中项，直接跳转到该文章
    if (selectedIndex.value >= 0 && searchResults.value[selectedIndex.value]) {
      goToArticle(searchResults.value[selectedIndex.value].title)
      return
    }
    
    // 否则跳转到搜索结果页面
    goToSearchPage()
    
    // 发射搜索事件给父组件（保持向后兼容）
    emit('search', keyword)
  }
}

/**
 * 查看全部搜索结果 - 暂时禁用
 * 因为已改用下拉弹窗方式，暂时不跳转到独立搜索页面
 */
const goToSearchPage = () => {
  // TODO: 可以考虑在下拉框中显示更多结果，或者重新启用独立搜索页面
  console.log('查看全部搜索结果功能暂时禁用')
  hideDropdown()
}

/**
 * 显示更多搜索结果到下拉框
 */
const showMoreResults = async () => {
  const keyword = searchTerm.value.trim()
  if (!keyword || isLoadingMore.value) return
  
  isLoadingMore.value = true
  
  try {
    // 计算下一页的页码
    const currentPage = Math.floor(searchResults.value.length / 5)
    const result = await articleApi.searchArticles(keyword, currentPage, 5)
    
    // 将新结果追加到现有结果中
    searchResults.value.push(...result.content)
  } catch (error) {
    console.error('加载更多结果失败:', error)
  } finally {
    isLoadingMore.value = false
  }
}

/**
 * 跳转到文章详情页
 */
const goToArticle = (title: string) => {
  router.push(`/wiki/${encodeURIComponent(title)}`)
  hideDropdown()
  searchTerm.value = ''  // 清空搜索框
}

/**
 * 隐藏下拉框
 */
const hideDropdown = () => {
  showDropdown.value = false
  selectedIndex.value = -1
}

/**
 * 处理失去焦点事件
 */
const handleBlur = (event: FocusEvent) => {
  // 如果焦点移动到下拉框内的元素，不隐藏下拉框
  const relatedTarget = event.relatedTarget as HTMLElement
  if (relatedTarget && searchContainer.value?.contains(relatedTarget)) {
    return
  }
  
  // 延迟隐藏，给用户时间点击下拉框中的选项
  setTimeout(hideDropdown, 300)
}

/**
 * 处理键盘导航
 */
const handleKeydown = (event: KeyboardEvent) => {
  if (!showDropdown.value || searchResults.value.length === 0) return
  
  switch (event.key) {
    case 'ArrowDown':
      event.preventDefault()
      selectedIndex.value = Math.min(selectedIndex.value + 1, searchResults.value.length - 1)
      break
    case 'ArrowUp':
      event.preventDefault()
      selectedIndex.value = Math.max(selectedIndex.value - 1, -1)
      break
    case 'Escape':
      hideDropdown()
      break
  }
}

/**
 * 格式化日期
 */
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric'
  })
}

/**
 * 获取文章摘要
 */
const getArticleSummary = (content: string) => {
  const plainText = content.replace(/<[^>]*>/g, '')
  return plainText.length > 100 ? plainText.substring(0, 100) + '...' : plainText
}

// 生命周期
onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }
})
</script>

<style scoped>
/* 搜索栏 */
.search-container {
  flex: 1;
  max-width: 500px;
  margin: 0 20px;
  position: relative;
  z-index: 1000;
}

.search-wrapper {
  position: relative;
  width: 100%;
}

.search-input {
  width: 100%;
  padding: 12px 50px 12px 20px;
  border: none;
  border-radius: 25px;
  font-size: 1rem;
  outline: none;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.search-input:focus {
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.15);
  background: white;
}

.search-input::placeholder {
  color: #718096;
}

.search-button {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  color: #667eea;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-button:hover:not(:disabled) {
  background: rgba(102, 126, 234, 0.1);
  transform: translateY(-50%) scale(1.1);
}

.search-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 加载动画 */
.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 搜索下拉框 */
.search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  margin-top: 8px;
  max-height: 500px;
  overflow: hidden;
  z-index: 1001;
  border: 1px solid rgba(0, 0, 0, 0.08);
}

/* 下拉框加载状态 */
.dropdown-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #667eea;
  gap: 12px;
}

.dropdown-loading .loading-spinner {
  width: 20px;
  height: 20px;
}

/* 搜索结果 */
.search-results {
  overflow: hidden;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #f8f9fa;
}

.results-count {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.view-all-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.view-all-btn:hover {
  background: #5a67d8;
}

/* 结果列表 */
.results-list {
  max-height: 350px;
  overflow-y: auto;
}

.result-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.result-item:last-child {
  border-bottom: none;
}

.result-item:hover,
.result-item.active {
  background: #f8f9ff;
  border-left: 3px solid #667eea;
  padding-left: 13px;
}

.result-content {
  width: 100%;
}

.result-title {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  margin: 0 0 4px 0;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-summary {
  font-size: 12px;
  color: #666;
  margin: 0 0 6px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.result-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 11px;
  color: #999;
}

.author {
  font-weight: 500;
  color: #667eea;
}

.date {
  color: #999;
}

.category {
  background: #e2e8f0;
  color: #4a5568;
  padding: 1px 6px;
  border-radius: 3px;
  font-size: 10px;
}

/* 下拉框底部 */
.dropdown-footer {
  padding: 8px 16px;
  border-top: 1px solid #f0f0f0;
  background: #f8f9fa;
}

.view-more-btn {
  width: 100%;
  background: #f7fafc;
  border: 1px solid #e2e8f0;
  color: #4a5568;
  padding: 8px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.view-more-btn:hover:not(:disabled) {
  background: #edf2f7;
  border-color: #cbd5e0;
}

.view-more-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.view-more-btn .loading-spinner {
  width: 14px;
  height: 14px;
}

/* 无结果状态 */
.no-results {
  text-align: center;
  padding: 32px 20px;
  color: #666;
}

.no-results-icon {
  font-size: 32px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.no-results p {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 500;
}

.no-results-tip {
  font-size: 12px;
  color: #999;
}

/* 滚动条样式 */
.results-list::-webkit-scrollbar {
  width: 4px;
}

.results-list::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.results-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.results-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-container {
    max-width: 100%;
    margin: 0;
  }
  
  .search-dropdown {
    left: -10px;
    right: -10px;
    margin-top: 4px;
  }
  
  .result-item {
    padding: 10px 12px;
  }
  
  .result-title {
    font-size: 13px;
  }
  
  .result-summary {
    font-size: 11px;
  }
  
  .results-list {
    max-height: 220px;
  }
}

@media (max-width: 480px) {
  .search-input {
    padding: 10px 45px 10px 16px;
    font-size: 14px;
  }
  
  .search-button {
    right: 6px;
  }
  
  .results-header {
    padding: 8px 12px;
  }
  
  .results-count {
    font-size: 11px;
  }
  
  .view-all-btn {
    padding: 3px 8px;
    font-size: 11px;
  }
}
</style>
