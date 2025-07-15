<!-- 
  文章管理组件
  功能：展示文章列表，支持文章的创建、编辑、删除等操作
  作者：EcoWiki开发团队
-->
<template>
  <div class="article-management">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z" />
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">文章管理</h1>
          <p class="page-subtitle">管理系统文章内容和发布状态</p>
        </div>
      </div>
      <div class="header-actions">
        <!-- 创建文章按钮 -->
        <button class="create-btn" @click="showCreateModal = true">
          ➕ 创建文章
        </button>
        <!-- 刷新按钮 -->
        <button class="refresh-btn" @click="loadArticles" :disabled="loading">
          <span v-if="loading" class="loading-spinner"></span>
          刷新
        </button>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-and-filter">
      <div class="search-box">
        <input
          type="text"
          v-model="searchQuery"
          placeholder="搜索文章（标题、作者、内容）"
          class="search-input"
        >
        <button class="clear-search-btn" @click="clearSearch" v-if="searchQuery">
          ✕
        </button>
      </div>
      
      <div class="filter-controls">
        <div class="sort-control">
          <label>排序方式：</label>
          <select v-model="sortBy" class="sort-select">
            <option value="publishDate">发布时间</option>
            <option value="articleId">文章ID</option>
            <option value="title">标题</option>
            <option value="author">作者</option>
            <option value="views">浏览量</option>
            <option value="likes">点赞数</option>
          </select>
          <button 
            @click="toggleSortOrder" 
            class="sort-order-btn"
            :title="sortOrder === 'asc' ? '升序' : '降序'"
          >
            {{ sortOrder === 'asc' ? '↑' : '↓' }}
          </button>
        </div>
        
        <div class="category-filter">
          <label>分类筛选：</label>
          <select v-model="categoryFilter" class="category-filter-select">
            <option value="">全部分类</option>
            <option v-for="category in categories" :key="category" :value="category">
              {{ category }}
            </option>
          </select>
        </div>
      </div>
    </div>

    <!-- 加载状态显示 -->
    <div v-if="loading" class="loading">加载中...</div>
    
    <!-- 错误状态显示 -->
    <div v-if="error" class="error">{{ error }}</div>

    <!-- 文章列表 -->
    <div v-if="!loading && !error" class="article-list">
      <div class="article-table">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>标题</th>
              <th>作者</th>
              <th>分类</th>
              <th>发布时间</th>
              <th>浏览量</th>
              <th>点赞数</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="article in filteredArticles" :key="article.articleId" class="article-row">
              <td>{{ article.articleId }}</td>
              <td class="title-cell">
                <div class="title-text" :title="article.title">{{ truncateText(article.title, 30) }}</div>
              </td>
              <td>{{ article.author }}</td>
              <td>{{ article.category || '未分类' }}</td>
              <td>{{ formatDate(article.publishDate) }}</td>
              <td>{{ article.views || 0 }}</td>
              <td>{{ article.likes || 0 }}</td>
              <td class="actions-cell">
                <button class="edit-btn" @click="editArticle(article)">编辑</button>
                <button class="delete-btn" @click="confirmDeleteArticle(article)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页控件 -->
      <div class="pagination" v-if="totalPages > 1">
        <button 
          @click="changePage(currentPage - 1)" 
          :disabled="currentPage <= 0"
          class="page-btn"
        >
          上一页
        </button>
        
        <span class="page-info">
          第 {{ currentPage + 1 }} 页，共 {{ totalPages }} 页
        </span>
        
        <button 
          @click="changePage(currentPage + 1)" 
          :disabled="currentPage >= totalPages - 1"
          class="page-btn"
        >
          下一页
        </button>
      </div>
    </div>

    <!-- 创建文章模态框 -->
    <div v-if="showCreateModal" class="modal-overlay" @click="closeCreateModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h4>创建新文章</h4>
          <button class="close-btn" @click="closeCreateModal">✕</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="createArticle">
            <div class="form-group">
              <label for="create-title">标题：</label>
              <input
                id="create-title"
                type="text"
                v-model="newArticle.title"
                required
                class="form-input"
                placeholder="请输入文章标题"
              >
            </div>
            <div class="form-group">
              <label for="create-category">分类：</label>
              <input
                id="create-category"
                type="text"
                v-model="newArticle.category"
                class="form-input"
                placeholder="请输入文章分类"
              >
            </div>
            <div class="form-group">
              <label for="create-tags">标签：</label>
              <input
                id="create-tags"
                type="text"
                v-model="newArticle.tags"
                class="form-input"
                placeholder="多个标签以逗号分隔"
              >
            </div>
            <div class="form-group">
              <label for="create-content">内容：</label>
              <textarea
                id="create-content"
                v-model="newArticle.content"
                required
                class="form-textarea"
                placeholder="请输入文章内容"
                rows="10"
              ></textarea>
            </div>
            <div class="form-actions">
              <button type="submit" class="submit-btn" :disabled="createLoading">
                {{ createLoading ? '创建中...' : '创建文章' }}
              </button>
              <button type="button" class="cancel-btn" @click="closeCreateModal">取消</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 编辑文章模态框 -->
    <div v-if="showEditModal" class="modal-overlay" @click="closeEditModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h4>编辑文章</h4>
          <button class="close-btn" @click="closeEditModal">✕</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="updateArticle">
            <div class="form-group">
              <label for="edit-title">标题：</label>
              <input
                id="edit-title"
                type="text"
                v-model="editingArticle.title"
                required
                class="form-input"
              >
            </div>
            <div class="form-group">
              <label for="edit-category">分类：</label>
              <input
                id="edit-category"
                type="text"
                v-model="editingArticle.category"
                class="form-input"
              >
            </div>
            <div class="form-group">
              <label for="edit-content">内容：</label>
              <textarea
                id="edit-content"
                v-model="editingArticle.content"
                required
                class="form-textarea"
                rows="10"
              ></textarea>
            </div>
            <div class="form-actions">
              <button type="submit" class="submit-btn" :disabled="updateLoading">
                {{ updateLoading ? '更新中...' : '更新文章' }}
              </button>
              <button type="button" class="cancel-btn" @click="closeEditModal">取消</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 删除确认模态框 -->
    <div v-if="showDeleteModal" class="modal-overlay" @click="closeDeleteModal">
      <div class="modal-content small-modal" @click.stop>
        <div class="modal-header">
          <h4>确认删除</h4>
          <button class="close-btn" @click="closeDeleteModal">✕</button>
        </div>
        <div class="modal-body">
          <p>确定要删除文章 "{{ deletingArticle?.title }}" 吗？</p>
          <p class="warning-text">此操作不可逆！</p>
          <div class="form-actions">
            <button class="delete-confirm-btn" @click="deleteArticle" :disabled="deleteLoading">
              {{ deleteLoading ? '删除中...' : '确认删除' }}
            </button>
            <button class="cancel-btn" @click="closeDeleteModal">取消</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 定义组件名称，用于缓存标识
defineOptions({
  name: 'ArticleManagement'
})

import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '@/api/article'

// Router
const router = useRouter()

// 响应式数据
const articles = ref<any[]>([])
const loading = ref(false)
const error = ref('')
const searchQuery = ref('')
const sortBy = ref('publishDate')
const sortOrder = ref<'asc' | 'desc'>('desc')
const categoryFilter = ref('')
const categories = ref<string[]>([])

// 分页
const currentPage = ref(0)
const pageSize = ref(10)
const totalPages = ref(0)
const totalElements = ref(0)

// 模态框状态
const showCreateModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const createLoading = ref(false)
const updateLoading = ref(false)
const deleteLoading = ref(false)

// 文章数据
const newArticle = ref({
  title: '',
  category: '',
  tags: '',
  content: ''
})

const editingArticle = ref<any>({})
const deletingArticle = ref<any>(null)

// 计算属性：过滤后的文章列表
const filteredArticles = computed(() => {
  let filtered = articles.value

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(article => 
      article.title?.toLowerCase().includes(query) ||
      article.author?.toLowerCase().includes(query) ||
      article.content?.toLowerCase().includes(query)
    )
  }

  // 分类过滤
  if (categoryFilter.value) {
    filtered = filtered.filter(article => article.category === categoryFilter.value)
  }

  return filtered
})

// 方法
const loadArticles = async () => {
  try {
    loading.value = true
    error.value = ''
    
    console.log('=== 发送文章请求 ===')
    console.log('使用articleApi.getArticles方法')
    console.log('请求参数:', {
      page: currentPage.value,
      size: pageSize.value,
      sortBy: sortBy.value,
      sortDir: sortOrder.value
    })
    
    const pageData = await articleApi.getArticles(
      currentPage.value, 
      pageSize.value, 
      sortBy.value, 
      sortOrder.value
    )

    console.log('=== 前端文章数据调试 ===')
    console.log('分页数据:', pageData)
    console.log('文章数组:', pageData.content)
    console.log('文章数量:', pageData.content?.length)
    
    articles.value = pageData.content
    totalPages.value = pageData.totalPages
    totalElements.value = pageData.totalElements
    
    // 提取分类
    const uniqueCategories = [...new Set(articles.value.map(a => a.category).filter(Boolean))]
    categories.value = uniqueCategories
  } catch (err: any) {
    console.error('加载文章列表失败:', err)
    if (err.response?.status === 403) {
      error.value = '权限不足，请联系管理员'
    } else if (err.response?.status === 401) {
      error.value = '登录已过期，请重新登录'
      router.push('/login')
    } else {
      error.value = '网络错误，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}

const createArticle = async () => {
  try {
    createLoading.value = true
    
    // 添加作者信息
    const articleData = {
      ...newArticle.value,
      author: 'Admin' // 这里可以从当前用户获取
    }
    
    const article = await articleApi.createArticle(articleData)
    
    showCreateModal.value = false
    resetNewArticleForm()
    await loadArticles()
    alert('文章创建成功！')
  } catch (err: any) {
    console.error('创建文章失败:', err)
    alert(err.response?.data?.message || '创建文章失败')
  } finally {
    createLoading.value = false
  }
}

const editArticle = (article: any) => {
  editingArticle.value = { ...article }
  showEditModal.value = true
}

const updateArticle = async () => {
  try {
    updateLoading.value = true
    
    const updateData = {
      title: editingArticle.value.title,
      content: editingArticle.value.content,
      category: editingArticle.value.category
    }
    
    const article = await articleApi.updateArticle(editingArticle.value.articleId, updateData)

    showEditModal.value = false
    await loadArticles()
    alert('文章更新成功！')
  } catch (err: any) {
    console.error('更新文章失败:', err)
    alert(err.response?.data?.message || '更新文章失败')
  } finally {
    updateLoading.value = false
  }
}

const confirmDeleteArticle = (article: any) => {
  deletingArticle.value = article
  showDeleteModal.value = true
}

const deleteArticle = async () => {
  try {
    deleteLoading.value = true
    
    await articleApi.deleteArticle(deletingArticle.value.articleId)

    showDeleteModal.value = false
    await loadArticles()
    alert('文章删除成功！')
  } catch (err: any) {
    console.error('删除文章失败:', err)
    alert(err.response?.data?.message || '删除文章失败')
  } finally {
    deleteLoading.value = false
  }
}

const changePage = (newPage: number) => {
  if (newPage >= 0 && newPage < totalPages.value) {
    currentPage.value = newPage
    loadArticles()
  }
}

const toggleSortOrder = () => {
  sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  loadArticles()
}

const clearSearch = () => {
  searchQuery.value = ''
}

const closeCreateModal = () => {
  showCreateModal.value = false
  resetNewArticleForm()
}

const closeEditModal = () => {
  showEditModal.value = false
  editingArticle.value = {}
}

const closeDeleteModal = () => {
  showDeleteModal.value = false
  deletingArticle.value = null
}

const resetNewArticleForm = () => {
  newArticle.value = {
    title: '',
    category: '',
    tags: '',
    content: ''
  }
}

const truncateText = (text: string, maxLength: number) => {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('zh-CN')
}

// 监听排序变化
const watchSortBy = () => {
  loadArticles()
}

/**
 * 刷新组件数据
 * 供外部调用以重新加载所有数据
 */
const refreshData = async () => {
  await loadArticles()
}

// 生命周期
onMounted(() => {
  loadArticles()
})

// 暴露方法给父组件调用
defineExpose({
  refreshData
})
</script>

<style scoped>
.article-management {
  padding: 24px;
  background: #f8fafb;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
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

.create-btn, .refresh-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.create-btn {
  background: #4CAF50;
  color: white;
}

.create-btn:hover {
  background: #45a049;
}

.refresh-btn {
  background: #2196F3;
  color: white;
}

.refresh-btn:hover {
  background: #1976D2;
}

.refresh-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.loading-spinner {
  display: inline-block;
  width: 12px;
  height: 12px;
  border: 2px solid #ffffff;
  border-radius: 50%;
  border-top-color: transparent;
  animation: spin 1s ease-in-out infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.search-and-filter {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.search-box {
  position: relative;
  margin-bottom: 15px;
}

.search-input {
  width: 100%;
  padding: 10px 40px 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.clear-search-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: #999;
  font-size: 16px;
}

.filter-controls {
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.sort-control, .category-filter {
  display: flex;
  align-items: center;
  gap: 5px;
}

.sort-select, .category-filter-select {
  padding: 5px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.sort-order-btn {
  padding: 5px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  font-size: 16px;
}

.sort-order-btn:hover {
  background: #f0f0f0;
}

.loading, .error {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 8px;
  margin: 20px 0;
}

.error {
  color: #f44336;
}

.article-list {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

.article-table {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.title-cell {
  max-width: 200px;
}

.title-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.actions-cell {
  white-space: nowrap;
}

.edit-btn, .delete-btn {
  padding: 4px 8px;
  margin: 0 2px;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 12px;
}

.edit-btn {
  background: #2196F3;
  color: white;
}

.edit-btn:hover {
  background: #1976D2;
}

.delete-btn {
  background: #f44336;
  color: white;
}

.delete-btn:hover {
  background: #d32f2f;
}

.pagination {
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  background: #f8f9fa;
  border-top: 1px solid #eee;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  cursor: pointer;
}

.page-btn:hover:not(:disabled) {
  background: #f0f0f0;
}

.page-btn:disabled {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
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
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.small-modal {
  max-width: 400px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h4 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #999;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #333;
}

.form-input, .form-textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

.submit-btn, .cancel-btn, .delete-confirm-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.submit-btn {
  background: #4CAF50;
  color: white;
}

.submit-btn:hover:not(:disabled) {
  background: #45a049;
}

.delete-confirm-btn {
  background: #f44336;
  color: white;
}

.delete-confirm-btn:hover:not(:disabled) {
  background: #d32f2f;
}

.cancel-btn {
  background: #6c757d;
  color: white;
}

.cancel-btn:hover {
  background: #5a6268;
}

.submit-btn:disabled, .delete-confirm-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.warning-text {
  color: #f44336;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .article-management {
    padding: 10px;
  }
  
  .section-header {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .filter-controls {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .sort-control, .category-filter {
    justify-content: space-between;
  }
  
  .article-table {
    font-size: 12px;
  }
  
  th, td {
    padding: 8px 4px;
  }
  
  .modal-content {
    width: 95%;
    margin: 10px;
  }
}
</style>
