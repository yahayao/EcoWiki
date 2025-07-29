<template>
  <div class="review-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>审核管理</h1>
      <p class="description">管理文章审核流程，查看审核状态和处理审核请求</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card pending">
        <div class="stat-icon">⏳</div>
        <div class="stat-info">
          <h3>{{ stats.pending }}</h3>
          <p>待审核</p>
        </div>
      </div>
      <div class="stat-card approved">
        <div class="stat-icon">✅</div>
        <div class="stat-info">
          <h3>{{ stats.approved }}</h3>
          <p>已通过</p>
        </div>
      </div>
      <div class="stat-card rejected">
        <div class="stat-icon">❌</div>
        <div class="stat-info">
          <h3>{{ stats.rejected }}</h3>
          <p>已拒绝</p>
        </div>
      </div>
      <div class="stat-card total">
        <div class="stat-icon">📊</div>
        <div class="stat-info">
          <h3>{{ stats.total }}</h3>
          <p>总计</p>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filters">
      <div class="filter-group">
        <label>状态筛选:</label>
        <select v-model="selectedStatus" @change="filterReviews">
          <option value="">全部状态</option>
          <option value="PENDING">待审核</option>
          <option value="APPROVED">已通过</option>
          <option value="REJECTED">已拒绝</option>
        </select>
      </div>
      <div class="filter-group">
        <label>搜索:</label>
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索文章标题或作者..."
          @input="filterReviews"
        />
      </div>
      <button class="refresh-btn" @click="loadReviews">
        🔄 刷新
      </button>
    </div>

    <!-- 审核列表 -->
    <div class="review-list">
      <div v-if="loading" class="loading">
        <div class="loader"></div>
        <p>加载中...</p>
      </div>
      
      <div v-else-if="filteredReviews.length === 0" class="empty-state">
        <div class="empty-icon">📝</div>
        <h3>暂无审核内容</h3>
        <p>{{ selectedStatus ? '当前筛选条件下没有找到审核记录' : '还没有文章提交审核' }}</p>
      </div>

      <div v-else class="review-items">
        <div 
          v-for="review in filteredReviews" 
          :key="review.id"
          class="review-item"
          :class="review.status.toLowerCase()"
        >
          <!-- 文章信息 -->
          <div class="review-content">
            <div class="article-info">
              <h3 class="article-title">{{ review.articleTitle }}</h3>
              <div class="article-meta">
                <span class="author">👤 {{ review.authorName }}</span>
                <span class="category">🏷️ {{ review.category }}</span>
                <span class="submit-time">⏰ {{ formatDate(review.submitTime) }}</span>
              </div>
              <div class="article-summary" v-if="review.summary">
                {{ review.summary }}
              </div>
            </div>

            <!-- 审核状态 -->
            <div class="review-status">
              <span class="status-badge" :class="review.status.toLowerCase()">
                <span v-if="review.status === 'PENDING'">⏳ 待审核</span>
                <span v-else-if="review.status === 'APPROVED'">✅ 已通过</span>
                <span v-else-if="review.status === 'REJECTED'">❌ 已拒绝</span>
              </span>
              <div class="reviewer-info" v-if="review.reviewerName">
                <small>审核人: {{ review.reviewerName }}</small>
                <small v-if="review.reviewTime">{{ formatDate(review.reviewTime) }}</small>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="review-actions">
            <button class="btn-primary" @click="viewArticle(review)">
              📖 查看文章
            </button>
            <button 
              v-if="review.status === 'PENDING'" 
              class="btn-success" 
              @click="approveReview(review)"
            >
              ✅ 通过
            </button>
            <button 
              v-if="review.status === 'PENDING'" 
              class="btn-danger" 
              @click="showRejectDialog(review)"
            >
              ❌ 拒绝
            </button>
            <button class="btn-secondary" @click="viewHistory(review)">
              📋 历史记录
            </button>
          </div>

          <!-- 拒绝原因显示 -->
          <div v-if="review.status === 'REJECTED' && review.rejectReason" class="reject-reason">
            <strong>拒绝原因:</strong> {{ review.rejectReason }}
          </div>
        </div>
      </div>
    </div>

    <!-- 拒绝对话框 -->
    <div v-if="showRejectModal" class="modal-overlay" @click="closeRejectDialog">
      <div class="modal-content" @click.stop>
        <h3>拒绝审核</h3>
        <p>请输入拒绝原因:</p>
        <textarea 
          v-model="rejectReason" 
          placeholder="请详细说明拒绝的原因，帮助作者改进..."
          rows="4"
        ></textarea>
        <div class="modal-actions">
          <button class="btn-secondary" @click="closeRejectDialog">取消</button>
          <button class="btn-danger" @click="confirmReject">确认拒绝</button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="totalPages > 1">
      <button 
        class="page-btn" 
        :disabled="currentPage === 1"
        @click="changePage(currentPage - 1)"
      >
        ⬅️ 上一页
      </button>
      <span class="page-info">
        第 {{ currentPage }} 页，共 {{ totalPages }} 页
      </span>
      <button 
        class="page-btn" 
        :disabled="currentPage === totalPages"
        @click="changePage(currentPage + 1)"
      >
        下一页 ➡️
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const reviews = ref<any[]>([])
const selectedStatus = ref('')
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(1)

// 统计数据
const stats = reactive({
  pending: 0,
  approved: 0,
  rejected: 0,
  total: 0
})

// 拒绝对话框
const showRejectModal = ref(false)
const currentReview = ref<any>(null)
const rejectReason = ref('')

// 计算属性
const filteredReviews = computed(() => {
  let filtered = reviews.value

  if (selectedStatus.value) {
    filtered = filtered.filter(review => review.status === selectedStatus.value)
  }

  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(review => 
      review.articleTitle.toLowerCase().includes(query) ||
      review.authorName.toLowerCase().includes(query)
    )
  }

  return filtered
})

// 生命周期
onMounted(() => {
  loadReviews()
})

// 方法
const loadReviews = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 模拟数据
    reviews.value = [
      {
        id: 1,
        articleTitle: "可持续发展的现代农业技术",
        authorName: "张三",
        category: "农业技术",
        status: "PENDING",
        submitTime: new Date(Date.now() - 2 * 60 * 60 * 1000),
        summary: "本文探讨了现代农业技术在可持续发展中的应用..."
      },
      {
        id: 2,
        articleTitle: "城市绿化与环境保护",
        authorName: "李四",
        category: "环境保护",
        status: "APPROVED",
        submitTime: new Date(Date.now() - 5 * 60 * 60 * 1000),
        reviewTime: new Date(Date.now() - 1 * 60 * 60 * 1000),
        reviewerName: "管理员",
        summary: "分析了城市绿化对环境保护的重要作用..."
      },
      {
        id: 3,
        articleTitle: "新能源汽车发展趋势",
        authorName: "王五",
        category: "新能源",
        status: "REJECTED",
        submitTime: new Date(Date.now() - 8 * 60 * 60 * 1000),
        reviewTime: new Date(Date.now() - 3 * 60 * 60 * 1000),
        reviewerName: "管理员",
        rejectReason: "内容与环保主题关联度不够，建议重新组织内容结构",
        summary: "讨论了新能源汽车的市场发展前景..."
      }
    ]

    updateStats()
    totalPages.value = Math.ceil(reviews.value.length / pageSize.value)
  } catch (error) {
    console.error('加载审核列表失败:', error)
  } finally {
    loading.value = false
  }
}

const updateStats = () => {
  stats.total = reviews.value.length
  stats.pending = reviews.value.filter(r => r.status === 'PENDING').length
  stats.approved = reviews.value.filter(r => r.status === 'APPROVED').length
  stats.rejected = reviews.value.filter(r => r.status === 'REJECTED').length
}

const filterReviews = () => {
  // 触发计算属性重新计算
  currentPage.value = 1
}

const viewArticle = (review: any) => {
  // 跳转到文章详情页
  router.push(`/article/${review.id}`)
}

const approveReview = async (review: any) => {
  if (!confirm('确认通过这篇文章的审核吗？')) return
  
  try {
    // 调用API通过审核
    console.log('通过审核:', review.id)
    
    // 更新本地状态
    review.status = 'APPROVED'
    review.reviewTime = new Date()
    review.reviewerName = '当前管理员'
    
    updateStats()
  } catch (error) {
    console.error('审核通过失败:', error)
    alert('审核操作失败，请重试')
  }
}

const showRejectDialog = (review: any) => {
  currentReview.value = review
  rejectReason.value = ''
  showRejectModal.value = true
}

const closeRejectDialog = () => {
  showRejectModal.value = false
  currentReview.value = null
  rejectReason.value = ''
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    alert('请输入拒绝原因')
    return
  }

  try {
    // 调用API拒绝审核
    console.log('拒绝审核:', currentReview.value.id, rejectReason.value)
    
    // 更新本地状态
    currentReview.value.status = 'REJECTED'
    currentReview.value.rejectReason = rejectReason.value
    currentReview.value.reviewTime = new Date()
    currentReview.value.reviewerName = '当前管理员'
    
    updateStats()
    closeRejectDialog()
  } catch (error) {
    console.error('审核拒绝失败:', error)
    alert('审核操作失败，请重试')
  }
}

const viewHistory = (review: any) => {
  // 查看审核历史
  console.log('查看审核历史:', review.id)
  alert('审核历史功能开发中...')
}

const changePage = (page: number) => {
  currentPage.value = page
  loadReviews()
}

const formatDate = (date: Date) => {
  return new Date(date).toLocaleString('zh-CN')
}
</script>

<style scoped>
.review-management {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 28px;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.page-header .description {
  color: #666;
  font-size: 16px;
  margin: 0;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  font-size: 32px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.stat-card.pending .stat-icon {
  background: #fff3cd;
}

.stat-card.approved .stat-icon {
  background: #d1edff;
}

.stat-card.rejected .stat-icon {
  background: #f8d7da;
}

.stat-card.total .stat-icon {
  background: #e2e8f0;
}

.stat-info h3 {
  font-size: 24px;
  font-weight: bold;
  margin: 0 0 4px 0;
  color: #1a1a1a;
}

.stat-info p {
  color: #666;
  margin: 0;
  font-size: 14px;
}

/* 筛选器 */
.filters {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-group label {
  font-weight: 500;
  color: #374151;
}

.filter-group select,
.filter-group input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.refresh-btn {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.refresh-btn:hover {
  background: #2563eb;
}

/* 审核列表 */
.review-list {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px;
  color: #666;
}

.loader {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state h3 {
  margin: 0 0 8px 0;
  color: #374151;
}

.empty-state p {
  margin: 0;
}

/* 审核项目 */
.review-item {
  border-bottom: 1px solid #e5e7eb;
  padding: 24px;
}

.review-item:last-child {
  border-bottom: none;
}

.review-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 20px;
}

.article-info {
  flex: 1;
}

.article-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.article-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.article-meta span {
  font-size: 14px;
  color: #666;
}

.article-summary {
  color: #666;
  font-size: 14px;
  line-height: 1.4;
}

.review-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

.status-badge.pending {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.approved {
  background: #dcfce7;
  color: #166534;
}

.status-badge.rejected {
  background: #fee2e2;
  color: #991b1b;
}

.reviewer-info {
  text-align: right;
  font-size: 12px;
  color: #666;
}

.reviewer-info small {
  display: block;
}

/* 操作按钮 */
.review-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.review-actions button {
  padding: 8px 16px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover {
  background: #2563eb;
}

.btn-success {
  background: #10b981;
  color: white;
}

.btn-success:hover {
  background: #059669;
}

.btn-danger {
  background: #ef4444;
  color: white;
}

.btn-danger:hover {
  background: #dc2626;
}

.btn-secondary {
  background: #6b7280;
  color: white;
}

.btn-secondary:hover {
  background: #4b5563;
}

/* 拒绝原因 */
.reject-reason {
  margin-top: 16px;
  padding: 12px;
  background: #fee2e2;
  border-radius: 6px;
  font-size: 14px;
  color: #991b1b;
}

/* 模态框 */
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
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 24px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

.modal-content h3 {
  margin: 0 0 16px 0;
  color: #1a1a1a;
}

.modal-content p {
  margin: 0 0 12px 0;
  color: #666;
}

.modal-content textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-family: inherit;
  font-size: 14px;
  resize: vertical;
  margin-bottom: 20px;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

/* 分页 */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
  padding: 20px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #d1d5db;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.page-btn:hover:not(:disabled) {
  background: #f9fafb;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .review-management {
    padding: 16px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .filters {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group {
    flex-direction: column;
    align-items: stretch;
    gap: 4px;
  }
  
  .review-content {
    flex-direction: column;
    gap: 12px;
  }
  
  .review-status {
    align-items: flex-start;
  }
  
  .review-actions {
    justify-content: stretch;
  }
  
  .review-actions button {
    flex: 1;
  }
}
</style>