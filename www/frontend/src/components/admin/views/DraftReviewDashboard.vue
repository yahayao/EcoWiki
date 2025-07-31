<template>
  <div class="draft-review-dashboard">
    <div class="dashboard-header">
      <h2>文章审核管理</h2>
      <div class="stats-cards">
        <div class="stat-card pending">
          <div class="stat-number">{{ stats.pending }}</div>
          <div class="stat-label">待审核</div>
        </div>
        <div class="stat-card approved">
          <div class="stat-number">{{ stats.approved }}</div>
          <div class="stat-label">已通过</div>
        </div>
        <div class="stat-card rejected">
          <div class="stat-number">{{ stats.rejected }}</div>
          <div class="stat-label">已拒绝</div>
        </div>
      </div>
    </div>

    <!-- 筛选控件 -->
    <div class="filter-section">
      <div class="filter-group">
        <label>状态筛选：</label>
        <select v-model="selectedStatus" @change="loadDrafts" class="status-filter">
          <option value="ALL">全部</option>
          <option value="PENDING">待审核</option>
          <option value="APPROVED">已通过</option>
          <option value="REJECTED">已拒绝</option>
        </select>
      </div>
      <div class="filter-group">
        <button @click="refreshData" class="refresh-btn" :disabled="loading">
          <span v-if="loading">🔄</span>
          <span v-else>🔄</span>
          刷新
        </button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 错误提示 -->
    <div v-if="error" class="error-container">
      <p class="error-message">{{ error }}</p>
      <button @click="loadDrafts" class="retry-btn">重试</button>
    </div>

    <!-- 草稿列表 -->
    <div v-if="!loading && !error" class="drafts-list">
      <div v-if="drafts.length === 0" class="empty-state">
        <p>暂无草稿数据</p>
      </div>
      
      <div v-for="draft in drafts" :key="draft.draftId" class="draft-item">
        <div class="draft-header">
          <h3 class="draft-title">{{ draft.title }}</h3>
          <div class="draft-meta">
            <span class="draft-status" :class="draft.reviewStatus.toLowerCase()">
              {{ getStatusText(draft.reviewStatus) }}
            </span>
            <span class="draft-date">{{ formatDate(draft.submittedAt) }}</span>
          </div>
        </div>
        
        <div class="draft-info">
          <div class="info-row">
            <span class="label">编辑者：</span>
            <span class="value">用户ID {{ draft.editorUserId }}</span>
          </div>
          <div class="info-row" v-if="draft.articleId">
            <span class="label">原文章ID：</span>
            <span class="value">{{ draft.articleId }}</span>
          </div>
          <div class="info-row" v-if="draft.category">
            <span class="label">分类：</span>
            <span class="value">{{ draft.category }}</span>
          </div>
          <div class="info-row" v-if="draft.tags">
            <span class="label">标签：</span>
            <span class="value">{{ draft.tags }}</span>
          </div>
        </div>

        <div class="draft-content-preview">
          <h4>内容预览：</h4>
          <div class="content-preview">
            {{ getContentPreview(draft.content) }}
          </div>
        </div>

        <!-- 审核信息 -->
        <div v-if="draft.reviewedAt" class="review-info">
          <div class="info-row">
            <span class="label">审核时间：</span>
            <span class="value">{{ formatDate(draft.reviewedAt) }}</span>
          </div>
          <div class="info-row" v-if="draft.reviewerUserId">
            <span class="label">审核者：</span>
            <span class="value">用户ID {{ draft.reviewerUserId }}</span>
          </div>
          <div class="info-row" v-if="draft.reviewNotes">
            <span class="label">审核备注：</span>
            <span class="value">{{ draft.reviewNotes }}</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="draft-actions" v-if="draft.reviewStatus === 'PENDING'">
          <button @click="openReviewModal(draft, true)" class="approve-btn">
            ✅ 通过
          </button>
          <button @click="openReviewModal(draft, false)" class="reject-btn">
            ❌ 拒绝
          </button>
          <button @click="viewDraftDetail(draft)" class="detail-btn">
            👁️ 详情
          </button>
        </div>
        
        <div class="draft-actions" v-else>
          <button @click="viewDraftDetail(draft)" class="detail-btn">
            👁️ 详情
          </button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="pagination">
      <button 
        @click="changePage(currentPage - 1)" 
        :disabled="currentPage === 0"
        class="page-btn"
      >
        上一页
      </button>
      <span class="page-info">
        第 {{ currentPage + 1 }} 页 / 共 {{ totalPages }} 页
      </span>
      <button 
        @click="changePage(currentPage + 1)" 
        :disabled="currentPage >= totalPages - 1"
        class="page-btn"
      >
        下一页
      </button>
    </div>

    <!-- 审核模态框 -->
    <div v-if="showReviewModal" class="modal-overlay" @click="closeReviewModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ reviewData.approved ? '通过' : '拒绝' }}审核</h3>
          <button @click="closeReviewModal" class="close-btn">×</button>
        </div>
        
        <div class="modal-body">
          <div class="draft-summary">
            <h4>{{ reviewData.draft?.title }}</h4>
            <p>编辑者：用户ID {{ reviewData.draft?.editorUserId }}</p>
          </div>
          
          <div class="review-form">
            <label for="reviewNotes">审核备注：</label>
            <textarea
              id="reviewNotes"
              v-model="reviewData.notes"
              :placeholder="reviewData.approved ? '请输入通过理由（可选）' : '请输入拒绝原因'"
              rows="4"
              class="review-textarea"
            ></textarea>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="closeReviewModal" class="cancel-btn">取消</button>
          <button 
            @click="submitReview" 
            :disabled="submittingReview"
            class="submit-btn"
            :class="{ approved: reviewData.approved, rejected: !reviewData.approved }"
          >
            <span v-if="submittingReview">提交中...</span>
            <span v-else>{{ reviewData.approved ? '确认通过' : '确认拒绝' }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { draftApi, type ArticleDraft } from '../../../api/draft'
import toast from '../../../utils/toast'

// 响应式数据
const loading = ref(false)
const error = ref('')
const drafts = ref<ArticleDraft[]>([])
const selectedStatus = ref<'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED'>('ALL')

// 分页数据
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const pageSize = 10

// 审核模态框
const showReviewModal = ref(false)
const submittingReview = ref(false)
const reviewData = ref<{
  draft: ArticleDraft | null
  approved: boolean
  notes: string
}>({
  draft: null,
  approved: true,
  notes: ''
})

// 统计数据
const stats = computed(() => {
  const pending = drafts.value.filter(d => d.reviewStatus === 'PENDING').length
  const approved = drafts.value.filter(d => d.reviewStatus === 'APPROVED').length
  const rejected = drafts.value.filter(d => d.reviewStatus === 'REJECTED').length
  return { pending, approved, rejected }
})

/**
 * 加载草稿列表
 */
const loadDrafts = async () => {
  loading.value = true
  error.value = ''
  
  try {
    let response
    if (selectedStatus.value === 'ALL') {
      response = await draftApi.getAllDrafts(currentPage.value, pageSize)
    } else {
      response = await draftApi.getDraftsByStatus(selectedStatus.value, currentPage.value, pageSize)
    }
    
    drafts.value = response.content
    totalPages.value = response.totalPages
    totalElements.value = response.totalElements
  } catch (err: any) {
    error.value = err.message || '加载草稿列表失败'
    console.error('加载草稿列表失败:', err)
  } finally {
    loading.value = false
  }
}

/**
 * 刷新数据
 */
const refreshData = () => {
  currentPage.value = 0
  loadDrafts()
}

/**
 * 换页
 */
const changePage = (page: number) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    loadDrafts()
  }
}

/**
 * 打开审核模态框
 */
const openReviewModal = (draft: ArticleDraft, approved: boolean) => {
  reviewData.value = {
    draft,
    approved,
    notes: ''
  }
  showReviewModal.value = true
}

/**
 * 关闭审核模态框
 */
const closeReviewModal = () => {
  showReviewModal.value = false
  reviewData.value = {
    draft: null,
    approved: true,
    notes: ''
  }
}

/**
 * 提交审核
 */
const submitReview = async () => {
  if (!reviewData.value.draft) return
  
  // 如果是拒绝且没有填写原因，提示用户
  if (!reviewData.value.approved && !reviewData.value.notes.trim()) {
    toast.warning('请填写拒绝原因')
    return
  }
  
  submittingReview.value = true
  
  try {
    await draftApi.reviewDraft(reviewData.value.draft.draftId, {
      approved: reviewData.value.approved,
      reviewNotes: reviewData.value.notes
    })
    
    toast.success(reviewData.value.approved ? '草稿已通过审核' : '草稿已拒绝')
    closeReviewModal()
    loadDrafts() // 重新加载列表
  } catch (err: any) {
    toast.error(err.message || '审核失败')
    console.error('审核失败:', err)
  } finally {
    submittingReview.value = false
  }
}

/**
 * 查看草稿详情
 */
const viewDraftDetail = (draft: ArticleDraft) => {
  // 这里可以跳转到详情页面或打开详情模态框
  console.log('查看草稿详情:', draft)
  // 示例：可以在这里打开一个详情模态框或跳转到详情页面
}

/**
 * 获取状态文本
 */
const getStatusText = (status: string) => {
  switch (status) {
    case 'PENDING': return '待审核'
    case 'APPROVED': return '已通过'
    case 'REJECTED': return '已拒绝'
    default: return status
  }
}

/**
 * 格式化日期
 */
const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

/**
 * 获取内容预览
 */
const getContentPreview = (content: string) => {
  if (!content) return '无内容'
  return content.length > 200 ? content.substring(0, 200) + '...' : content
}

// 组件挂载时加载数据
onMounted(() => {
  loadDrafts()
})

// 暴露方法给父组件
defineExpose({
  refreshData
})
</script>

<style scoped>
.draft-review-dashboard {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.dashboard-header {
  margin-bottom: 30px;
}

.dashboard-header h2 {
  margin-bottom: 20px;
  color: #333;
}

.stats-cards {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  text-align: center;
  min-width: 120px;
}

.stat-card.pending {
  border-left: 4px solid #f39c12;
}

.stat-card.approved {
  border-left: 4px solid #27ae60;
}

.stat-card.rejected {
  border-left: 4px solid #e74c3c;
}

.stat-number {
  font-size: 2em;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 0.9em;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-filter {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
}

.refresh-btn {
  padding: 8px 16px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.refresh-btn:hover {
  background: #2980b9;
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-container {
  text-align: center;
  padding: 50px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-container {
  text-align: center;
  padding: 50px;
}

.error-message {
  color: #e74c3c;
  margin-bottom: 20px;
}

.retry-btn {
  padding: 10px 20px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.empty-state {
  text-align: center;
  padding: 50px;
  color: #666;
}

.drafts-list {
  margin-bottom: 20px;
}

.draft-item {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.draft-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.draft-title {
  font-size: 1.2em;
  margin: 0;
  color: #333;
}

.draft-meta {
  display: flex;
  align-items: center;
  gap: 15px;
}

.draft-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8em;
  font-weight: bold;
  text-transform: uppercase;
}

.draft-status.pending {
  background: #f39c12;
  color: white;
}

.draft-status.approved {
  background: #27ae60;
  color: white;
}

.draft-status.rejected {
  background: #e74c3c;
  color: white;
}

.draft-date {
  color: #666;
  font-size: 0.9em;
}

.draft-info, .review-info {
  margin-bottom: 15px;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
}

.info-row .label {
  font-weight: bold;
  min-width: 100px;
  color: #555;
}

.info-row .value {
  color: #333;
}

.draft-content-preview {
  margin-bottom: 15px;
}

.draft-content-preview h4 {
  margin: 0 0 10px 0;
  color: #555;
}

.content-preview {
  background: #f8f9fa;
  padding: 10px;
  border-radius: 4px;
  border-left: 3px solid #3498db;
  color: #666;
  line-height: 1.5;
}

.draft-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.approve-btn, .reject-btn, .detail-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9em;
  display: flex;
  align-items: center;
  gap: 5px;
}

.approve-btn {
  background: #27ae60;
  color: white;
}

.approve-btn:hover {
  background: #229954;
}

.reject-btn {
  background: #e74c3c;
  color: white;
}

.reject-btn:hover {
  background: #c0392b;
}

.detail-btn {
  background: #95a5a6;
  color: white;
}

.detail-btn:hover {
  background: #7f8c8d;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
}

.page-btn {
  padding: 10px 20px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.page-btn:hover {
  background: #2980b9;
}

.page-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.page-info {
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
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5em;
  cursor: pointer;
  color: #999;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.draft-summary {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 4px;
}

.draft-summary h4 {
  margin: 0 0 10px 0;
  color: #333;
}

.draft-summary p {
  margin: 0;
  color: #666;
}

.review-form label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #555;
}

.review-textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
  font-family: inherit;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 20px;
  border-top: 1px solid #eee;
}

.cancel-btn, .submit-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.cancel-btn {
  background: #95a5a6;
  color: white;
}

.cancel-btn:hover {
  background: #7f8c8d;
}

.submit-btn {
  color: white;
}

.submit-btn.approved {
  background: #27ae60;
}

.submit-btn.approved:hover {
  background: #229954;
}

.submit-btn.rejected {
  background: #e74c3c;
}

.submit-btn.rejected:hover {
  background: #c0392b;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
