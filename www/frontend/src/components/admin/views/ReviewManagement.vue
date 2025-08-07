<template>
  <div class="review-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">审核管理</h1>
          <p class="page-subtitle">管理文章审核流程，高效处理审核请求</p>
        </div>
        <div class="header-actions">
          <button class="refresh-btn" @click="loadReviews" :disabled="loading">
            <svg class="icon" viewBox="0 0 20 20">
              <path d="M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H8a1 1 0 010 2H4a1 1 0 01-1-1V3a1 1 0 011-1zm.008 9.057a1 1 0 011.276.61A5.002 5.002 0 0014.001 13H12a1 1 0 110-2h4a1 1 0 011 1v4a1 1 0 11-2 0v-2.101a7.002 7.002 0 01-11.601-2.566 1 1 0 01.61-1.276z"/>
            </svg>
            刷新数据
          </button>
        </div>
      </div>
    </div>

    <!-- 数据统计面板 -->
    <div class="stats-dashboard">
      <div class="stat-card pending" @click="filterByStatus('PENDING')">
        <div class="card-header">
          <div class="stat-icon pending-bg">
            <svg viewBox="0 0 20 20">
              <path d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z"/>
            </svg>
          </div>
          <div class="trend-indicator up">
            <svg viewBox="0 0 20 20">
              <path d="M3 10l5-5m0 0l5 5m-5-5v12"/>
            </svg>
            +12%
          </div>
        </div>
        <div class="card-body">
          <h3 class="stat-number">{{ statistics.pending }}</h3>
          <p class="stat-label">待审核</p>
          <div class="stat-description">需要处理的文章</div>
        </div>
      </div>

      <div class="stat-card approved" @click="filterByStatus('APPROVED')">
        <div class="card-header">
          <div class="stat-icon approved-bg">
            <svg viewBox="0 0 20 20">
              <path d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"/>
            </svg>
          </div>
          <div class="trend-indicator up">
            <svg viewBox="0 0 20 20">
              <path d="M3 10l5-5m0 0l5 5m-5-5v12"/>
            </svg>
            +8%
          </div>
        </div>
        <div class="card-body">
          <h3 class="stat-number">{{ statistics.approved }}</h3>
          <p class="stat-label">已通过</p>
          <div class="stat-description">审核通过的文章</div>
        </div>
      </div>

      <div class="stat-card rejected" @click="filterByStatus('REJECTED')">
        <div class="card-header">
          <div class="stat-icon rejected-bg">
            <svg viewBox="0 0 20 20">
              <path d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"/>
            </svg>
          </div>
          <div class="trend-indicator down">
            <svg viewBox="0 0 20 20">
              <path d="M17 10l-5 5m0 0l-5-5m5 5V3"/>
            </svg>
            -3%
          </div>
        </div>
        <div class="card-body">
          <h3 class="stat-number">{{ statistics.rejected }}</h3>
          <p class="stat-label">已拒绝</p>
          <div class="stat-description">审核未通过</div>
        </div>
      </div>

      <div class="stat-card total" @click="filterByStatus('')">
        <div class="card-header">
          <div class="stat-icon total-bg">
            <svg viewBox="0 0 20 20">
              <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
          </div>
          <div class="progress-ring">
            <svg class="progress-ring-svg" width="40" height="40">
              <circle class="progress-ring-circle-bg" cx="20" cy="20" r="15"/>
              <circle class="progress-ring-circle" cx="20" cy="20" r="15" 
                :stroke-dasharray="94" :stroke-dashoffset="94 - (statistics.approved / statistics.total * 94)"/>
            </svg>
          </div>
        </div>
        <div class="card-body">
          <h3 class="stat-number">{{ statistics.total }}</h3>
          <p class="stat-label">总计</p>
          <div class="stat-description">全部审核请求</div>
        </div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="search-section">
        <div class="search-input-wrapper">
          <svg class="search-icon" viewBox="0 0 20 20">
            <path d="M9 9a2 2 0 114 0 2 2 0 01-4 0z"/>
            <path d="M9 1a8 8 0 105.293 14.293l4.421 4.421a1 1 0 001.414-1.414l-4.421-4.421A8 8 0 009 1z"/>
          </svg>
          <input 
            type="text" 
            v-model="searchQuery" 
            placeholder="搜索文章标题、作者或分类..."
            class="search-input"
            @input="() => currentPage = 1"
          />
          <button v-if="searchQuery" @click="clearSearch" class="clear-search">
            <svg viewBox="0 0 20 20">
              <path d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"/>
            </svg>
          </button>
        </div>
      </div>
      
      <div class="filter-section">
        <div class="filter-dropdown">
          <select v-model="selectedStatus" @change="() => currentPage = 1" class="status-filter">
            <option value="">全部状态</option>
            <option value="PENDING">待审核</option>
            <option value="APPROVED">已通过</option>
            <option value="REJECTED">已拒绝</option>
          </select>
        </div>
        
        <div class="view-toggle">
          <button 
            :class="['view-btn', { active: viewMode === 'list' }]"
            @click="setViewMode('list')"
          >
            <svg viewBox="0 0 20 20">
              <path d="M3 4a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 16a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1z"/>
            </svg>
          </button>
          <button 
            :class="['view-btn', { active: viewMode === 'grid' }]"
            @click="setViewMode('grid')"
          >
            <svg viewBox="0 0 20 20">
              <path d="M5 3a2 2 0 00-2 2v2a2 2 0 002 2h2a2 2 0 002-2V5a2 2 0 00-2-2H5zM5 11a2 2 0 00-2 2v2a2 2 0 002 2h2a2 2 0 002-2v-2a2 2 0 00-2-2H5zM11 5a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V5zM11 13a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 审核列表 -->
    <div class="review-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner">
          <div class="spinner"></div>
        </div>
        <p class="loading-text">正在加载审核数据...</p>
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="filteredReviews.length === 0" class="empty-state">
        <div class="empty-illustration">
          <svg viewBox="0 0 24 24" class="empty-icon">
            <path d="M9 11H7v8a2 2 0 002 2h8a2 2 0 002-2v-8h-2m-6 0V9a3 3 0 116 0v2m-6 0h6"/>
          </svg>
        </div>
        <h3 class="empty-title">暂无审核内容</h3>
        <p class="empty-description">
          {{ selectedStatus ? '当前筛选条件下没有找到审核记录' : '还没有文章提交审核' }}
        </p>
        <button @click="loadReviews" class="empty-action">
          <svg viewBox="0 0 20 20" class="icon">
            <path d="M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H8a1 1 0 010 2H4a1 1 0 01-1-1V3a1 1 0 011-1z"/>
          </svg>
          刷新数据
        </button>
      </div>

      <!-- 审核项目列表 -->
      <div v-else :class="['review-grid', viewMode]">
        <div 
          v-for="review in paginatedReviews" 
          :key="review.id"
          class="review-card"
          :class="['status-' + review.status.toLowerCase()]"
        >
          <!-- 卡片头部 -->
          <div class="card-header">
            <div class="status-indicator" :class="review.status.toLowerCase()">
              <svg v-if="review.status === 'PENDING'" viewBox="0 0 20 20" class="status-icon">
                <path d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z"/>
              </svg>
              <svg v-else-if="review.status === 'APPROVED'" viewBox="0 0 20 20" class="status-icon">
                <path d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"/>
              </svg>
              <svg v-else-if="review.status === 'REJECTED'" viewBox="0 0 20 20" class="status-icon">
                <path d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"/>
              </svg>
              <span class="status-text">{{ getStatusText(review.status) }}</span>
            </div>
            <div class="card-menu">
              <button class="menu-btn" @click="toggleMenu(review.id)">
                <svg viewBox="0 0 20 20">
                  <path d="M10 6a2 2 0 110-4 2 2 0 010 4zM10 12a2 2 0 110-4 2 2 0 010 4zM10 18a2 2 0 110-4 2 2 0 010 4z"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- 文章信息 -->
          <div class="card-content">
            <h3 class="article-title" @click="viewArticle(review.articleId)">
              {{ review.articleTitle }}
            </h3>
            
            <div class="article-meta">
              <div class="meta-item">
                <svg viewBox="0 0 20 20" class="meta-icon">
                  <path d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z"/>
                </svg>
                <span>{{ review.authorName }}</span>
              </div>
              <div class="meta-item">
                <svg viewBox="0 0 20 20" class="meta-icon">
                  <path d="M7 3a1 1 0 000 2h6a1 1 0 100-2H7zM4 7a1 1 0 011-1h10a1 1 0 110 2H5a1 1 0 01-1-1zM2 11a2 2 0 012-2h12a2 2 0 012 2v4a2 2 0 01-2 2H4a2 2 0 01-2-2v-4z"/>
                </svg>
                <span>{{ review.category }}</span>
              </div>
              <div class="meta-item">
                <svg viewBox="0 0 20 20" class="meta-icon">
                  <path d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zM6 7a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zM6 11a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1z"/>
                </svg>
                <span>{{ formatRelativeTime(review.submitTime) }}</span>
              </div>
            </div>

            <div class="article-summary" v-if="review.content">
              {{ review.content }}
            </div>

            <!-- 审核信息 -->
            <div class="review-info" v-if="review.reviewerName">
              <div class="reviewer-badge">
                <svg viewBox="0 0 20 20" class="reviewer-icon">
                  <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
                <span>{{ review.reviewerName }}</span>
              </div>
              <div class="review-time" v-if="review.reviewTime">
                {{ formatRelativeTime(review.reviewTime) }}
              </div>
            </div>

            <!-- 拒绝原因 -->
            <div v-if="review.status === 'REJECTED' && review.reason" class="reject-reason">
              <div class="reason-header">
                <svg viewBox="0 0 20 20" class="reason-icon">
                  <path d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"/>
                </svg>
                <span class="reason-title">审核意见</span>
              </div>
              <p class="reason-text">{{ review.reason }}</p>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="card-actions">
            <button class="action-btn primary" @click="viewArticle(review.articleId)">
              <svg viewBox="0 0 20 20" class="btn-icon">
                <path d="M10 12a2 2 0 100-4 2 2 0 000 4z"/>
                <path d="M10 3C5 3 1.73 7.11 1 10c.73 2.89 4 7 9 7s8.27-4.11 9-7c-.73-2.89-4-7-9-7zM10 15a5 5 0 110-10 5 5 0 010 10z"/>
              </svg>
              查看详情
            </button>
            
            <template v-if="review.status === 'PENDING'">
              <button class="action-btn success" @click="approveReview(review.id)">
                <svg viewBox="0 0 20 20" class="btn-icon">
                  <path d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"/>
                </svg>
                通过
              </button>
              
              <button class="action-btn danger" @click="rejectReview(review)">
                <svg viewBox="0 0 20 20" class="btn-icon">
                  <path d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"/>
                </svg>
                拒绝
              </button>
            </template>
            
            <button class="action-btn secondary" @click="viewHistory">
              <svg viewBox="0 0 20 20" class="btn-icon">
                <path d="M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H8a1 1 0 010 2H4a1 1 0 01-1-1V3a1 1 0 011-1z"/>
              </svg>
              历史记录
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 高级拒绝对话框 -->
    <div v-if="showRejectModal" class="modal-overlay" @click="closeRejectDialog">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">
            <svg viewBox="0 0 20 20" class="modal-icon">
              <path d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"/>
            </svg>
            拒绝审核申请
          </h3>
          <button @click="closeRejectDialog" class="modal-close">
            <svg viewBox="0 0 20 20">
              <path d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"/>
            </svg>
          </button>
        </div>
        
        <div class="modal-body">
          <div class="article-preview" v-if="currentReview">
            <h4 class="article-title">{{ currentReview.articleTitle }}</h4>
            <p class="article-author">作者：{{ currentReview.authorName }}</p>
          </div>
          
          <div class="form-group">
            <label class="form-label">请详细说明拒绝原因：</label>
            <textarea 
              v-model="rejectReason" 
              class="form-textarea"
              placeholder="请提供具体的改进建议，帮助作者提升文章质量..."
              rows="6"
              :maxlength="500"
            ></textarea>
            <div class="character-counter">
              <span :class="{ 'text-warning': rejectReason.length < 20, 'text-success': rejectReason.length >= 20 }">
                {{ rejectReason.length }}
              </span>
              <span class="text-muted">/ 500 字符</span>
            </div>
          </div>
          
          <div class="warning-notice">
            <svg viewBox="0 0 20 20" class="warning-icon">
              <path d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"/>
            </svg>
            <div class="warning-content">
              <p class="warning-title">注意事项</p>
              <p class="warning-text">拒绝后，作者将收到包含您反馈意见的通知邮件，请确保您的意见具体且有建设性。</p>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="closeRejectDialog" class="btn-cancel">
            <svg viewBox="0 0 20 20" class="btn-icon">
              <path d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"/>
            </svg>
            取消
          </button>
          <button @click="confirmReject" class="btn-confirm" :disabled="rejectReason.length < 20">
            <svg viewBox="0 0 20 20" class="btn-icon">
              <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
            确认拒绝
          </button>
        </div>
      </div>
    </div>

    <!-- 现代化分页组件 -->
    <div class="pagination-container" v-if="totalPages > 1">
      <div class="pagination-info">
        显示 <span class="highlight">{{ (currentPage - 1) * pageSize + 1 }}-{{ Math.min(currentPage * pageSize, filteredReviews.length) }}</span> 
        项，共 <span class="highlight">{{ filteredReviews.length }}</span> 项
      </div>
      
      <div class="pagination-controls">
        <button 
          class="pagination-btn" 
          :disabled="currentPage === 1"
          @click="changePage(currentPage - 1)"
        >
          <svg viewBox="0 0 20 20" class="btn-icon">
            <path d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z"/>
          </svg>
          上一页
        </button>
        
        <div class="page-numbers">
          <button 
            v-for="page in visiblePages" 
            :key="page"
            :class="['page-number', { active: page === currentPage }]"
            @click="changePage(page)"
          >
            {{ page }}
          </button>
        </div>
        
        <button 
          class="pagination-btn" 
          :disabled="currentPage === totalPages"
          @click="changePage(currentPage + 1)"
        >
          下一页
          <svg viewBox="0 0 20 20" class="btn-icon">
            <path d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

interface Review {
  id: number
  articleId: number
  articleTitle: string
  authorId: number
  authorName: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  submitTime: string
  reviewTime?: string
  reviewerId?: number
  reviewerName?: string
  content: string
  category: string
  tags: string[]
  reason?: string
  priority?: 'HIGH' | 'MEDIUM' | 'LOW'
  showMenu?: boolean
}

// 响应式数据
const loading = ref<boolean>(false)
const selectedStatus = ref<string>('')
const searchQuery = ref<string>('')
const currentPage = ref<number>(1)
const pageSize = ref<number>(10)
const reviews = ref<Review[]>([])
const showRejectModal = ref<boolean>(false)
const currentReview = ref<Review | null>(null)
const rejectReason = ref<string>('')
const viewMode = ref<'list' | 'grid'>('grid')

// 计算属性
const filteredReviews = computed(() => {
  let filtered = reviews.value

  // 按状态筛选
  if (selectedStatus.value) {
    filtered = filtered.filter(review => review.status === selectedStatus.value)
  }

  // 按搜索关键词筛选
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(review => 
      review.articleTitle.toLowerCase().includes(query) ||
      review.authorName.toLowerCase().includes(query) ||
      review.category.toLowerCase().includes(query)
    )
  }

  return filtered
})

const paginatedReviews = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredReviews.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(filteredReviews.value.length / pageSize.value)
})

const visiblePages = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  const pages: number[] = []
  
  if (total <= 7) {
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    if (current <= 4) {
      for (let i = 1; i <= 5; i++) pages.push(i)
      pages.push(-1) // 省略号标记
      pages.push(total)
    } else if (current >= total - 3) {
      pages.push(1)
      pages.push(-1)
      for (let i = total - 4; i <= total; i++) pages.push(i)
    } else {
      pages.push(1)
      pages.push(-1)
      for (let i = current - 1; i <= current + 1; i++) pages.push(i)
      pages.push(-1)
      pages.push(total)
    }
  }
  
  return pages
})

// 统计数据
const statistics = computed(() => {
  const total = reviews.value.length
  const pending = reviews.value.filter(r => r.status === 'PENDING').length
  const approved = reviews.value.filter(r => r.status === 'APPROVED').length
  const rejected = reviews.value.filter(r => r.status === 'REJECTED').length
  
  return {
    total,
    pending,
    approved,
    rejected,
    approvalRate: total > 0 ? Math.round((approved / total) * 100) : 0
  }
})

// 方法
const loadReviews = async () => {
  try {
    loading.value = true
    
    // 模拟API调用和数据
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    reviews.value = [
      {
        id: 1,
        articleId: 101,
        articleTitle: "可持续发展的现代农业技术",
        authorId: 1,
        authorName: "张三",
        category: "农业技术",
        status: "PENDING",
        submitTime: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
        content: "本文探讨了现代农业技术在可持续发展中的应用...",
        tags: ["农业", "可持续发展", "技术"],
        priority: "HIGH",
        showMenu: false
      },
      {
        id: 2,
        articleId: 102,
        articleTitle: "城市绿化与环境保护",
        authorId: 2,
        authorName: "李四",
        category: "环境保护",
        status: "APPROVED",
        submitTime: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(),
        reviewTime: new Date(Date.now() - 1 * 60 * 60 * 1000).toISOString(),
        reviewerName: "管理员",
        content: "分析了城市绿化对环境保护的重要作用...",
        tags: ["绿化", "环保", "城市"],
        priority: "MEDIUM",
        showMenu: false
      },
      {
        id: 3,
        articleId: 103,
        articleTitle: "新能源汽车发展趋势",
        authorId: 3,
        authorName: "王五",
        category: "新能源",
        status: "REJECTED",
        submitTime: new Date(Date.now() - 8 * 60 * 60 * 1000).toISOString(),
        reviewTime: new Date(Date.now() - 3 * 60 * 60 * 1000).toISOString(),
        reviewerName: "管理员",
        reason: "内容与环保主题关联度不够，建议重新组织内容结构",
        content: "讨论了新能源汽车的市场发展前景...",
        tags: ["新能源", "汽车", "发展"],
        priority: "LOW",
        showMenu: false
      }
    ] as Review[]
  } catch (error) {
    console.error('加载审核列表失败:', error)
    reviews.value = []
  } finally {
    loading.value = false
  }
}

const filterByStatus = (status: string) => {
  selectedStatus.value = status
  currentPage.value = 1
}

const clearSearch = () => {
  searchQuery.value = ''
  currentPage.value = 1
}

const setViewMode = (mode: 'list' | 'grid') => {
  viewMode.value = mode
}

const toggleMenu = (reviewId: number) => {
  const review = reviews.value.find(r => r.id === reviewId)
  if (review) {
    review.showMenu = !review.showMenu
  }
  // 关闭其他菜单
  reviews.value.forEach(r => {
    if (r.id !== reviewId) {
      r.showMenu = false
    }
  })
}

const getStatusText = (status: string): string => {
  const statusMap = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return statusMap[status as keyof typeof statusMap] || status
}

const formatRelativeTime = (dateString: string): string => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (days > 0) {
    return `${days}天前`
  } else if (hours > 0) {
    return `${hours}小时前`
  } else if (minutes > 0) {
    return `${minutes}分钟前`
  } else {
    return '刚刚'
  }
}

const approveReview = async (reviewId: number) => {
  try {
    const review = reviews.value.find(r => r.id === reviewId)
    if (review) {
      review.status = 'APPROVED'
      review.reviewTime = new Date().toISOString()
      review.reviewerName = '当前管理员'
    }
    console.log('审核通过:', reviewId)
  } catch (error) {
    console.error('审核通过失败:', error)
  }
}

const rejectReview = (review: Review) => {
  currentReview.value = review
  showRejectModal.value = true
  rejectReason.value = ''
}

const confirmReject = async () => {
  if (!currentReview.value || rejectReason.value.length < 20) return
  
  try {
    currentReview.value.status = 'REJECTED'
    currentReview.value.reason = rejectReason.value
    currentReview.value.reviewTime = new Date().toISOString()
    currentReview.value.reviewerName = '当前管理员'
    
    console.log('审核拒绝:', currentReview.value.id, rejectReason.value)
    closeRejectDialog()
  } catch (error) {
    console.error('审核拒绝失败:', error)
  }
}

const closeRejectDialog = () => {
  showRejectModal.value = false
  currentReview.value = null
  rejectReason.value = ''
}

const viewArticle = (articleId: number) => {
  window.open(`/article/${articleId}`, '_blank')
}

const viewHistory = () => {
  alert('审核历史功能开发中...')
}

const changePage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

// 初始化
onMounted(() => {
  loadReviews()
})
</script>

<style scoped>
/* 现代化审核管理界面样式 */

/* 基础布局 */
.review-management {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  padding: 2rem;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  overflow: hidden;
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 3rem 2rem;
  color: white;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 100" fill="rgba(255,255,255,0.1)"><polygon points="0,0 1000,80 1000,100 0,100"/></svg>');
  background-size: cover;
}

.page-header h1 {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  position: relative;
  z-index: 1;
}

.page-header .description {
  font-size: 1.1rem;
  opacity: 0.9;
  margin: 0;
  position: relative;
  z-index: 1;
}

/* 统计卡片网格 */
.stats-dashboard {
  padding: 2rem;
  background: #f8fafc;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid #e2e8f0;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  transition: all 0.3s ease;
}

.stat-card.pending::before { background: linear-gradient(90deg, #fbbf24, #f59e0b); }
.stat-card.approved::before { background: linear-gradient(90deg, #10b981, #059669); }
.stat-card.rejected::before { background: linear-gradient(90deg, #ef4444, #dc2626); }
.stat-card.total::before { background: linear-gradient(90deg, #3b82f6, #1d4ed8); }

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.stat-icon svg {
  width: 24px;
  height: 24px;
  color: white;
}

.stat-icon.pending-bg { background: linear-gradient(135deg, #fbbf24, #f59e0b); }
.stat-icon.approved-bg { background: linear-gradient(135deg, #10b981, #059669); }
.stat-icon.rejected-bg { background: linear-gradient(135deg, #ef4444, #dc2626); }
.stat-icon.total-bg { background: linear-gradient(135deg, #3b82f6, #1d4ed8); }

.trend-indicator {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.875rem;
  font-weight: 600;
  padding: 0.25rem 0.5rem;
  border-radius: 8px;
}

.trend-indicator.up {
  color: #059669;
  background: rgba(16, 185, 129, 0.1);
}

.trend-indicator.down {
  color: #dc2626;
  background: rgba(239, 68, 68, 0.1);
}

.trend-indicator svg {
  width: 16px;
  height: 16px;
}

.card-body {
  text-align: left;
}

.stat-number {
  font-size: 2.25rem;
  font-weight: 700;
  margin: 0 0 0.25rem 0;
  color: #1f2937;
}

.stat-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #6b7280;
  margin: 0 0 0.25rem 0;
}

.stat-description {
  font-size: 0.75rem;
  color: #9ca3af;
}

.progress-ring {
  width: 40px;
  height: 40px;
}

.progress-ring-svg {
  transform: rotate(-90deg);
}

.progress-ring-circle-bg {
  fill: none;
  stroke: #e5e7eb;
  stroke-width: 2;
}

.progress-ring-circle {
  fill: none;
  stroke: #3b82f6;
  stroke-width: 2;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.5s ease;
}

/* 工具栏 */
.toolbar {
  padding: 0 2rem 1rem 2rem;
  background: #f8fafc;
}

.search-filter-bar {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 1rem;
  align-items: center;
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 1rem;
}

.search-container {
  position: relative;
  flex: 1;
  max-width: 400px;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.5rem;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  font-size: 0.875rem;
  transition: all 0.2s ease;
  background: #f9fafb;
}

.search-input:focus {
  outline: none;
  border-color: #3b82f6;
  background: white;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  color: #9ca3af;
}

.clear-search {
  position: absolute;
  right: 0.5rem;
  top: 50%;
  transform: translateY(-50%);
  background: #e5e7eb;
  border: none;
  border-radius: 6px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.clear-search:hover {
  background: #d1d5db;
}

.filter-controls {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.status-filter {
  padding: 0.75rem 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  background: #f9fafb;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.status-filter:focus {
  outline: none;
  border-color: #3b82f6;
  background: white;
}

.view-toggle {
  display: flex;
  background: #f3f4f6;
  border-radius: 8px;
  padding: 0.25rem;
}

.view-btn {
  padding: 0.5rem;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.view-btn.active {
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  color: #3b82f6;
}

.view-btn svg {
  width: 20px;
  height: 20px;
}

/* 审核列表 */
.review-container {
  padding: 0 2rem 2rem 2rem;
  background: #f8fafc;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  color: #6b7280;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #e5e7eb;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.empty-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 1.5rem;
  color: #d1d5db;
}

.empty-state h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #374151;
  margin: 0 0 0.5rem 0;
}

.empty-state p {
  color: #6b7280;
  margin: 0;
}

/* 网格布局 */
.review-grid {
  display: grid;
  gap: 1.5rem;
}

.review-grid.grid {
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
}

.review-grid.list {
  grid-template-columns: 1fr;
}

.review-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  border: 1px solid #e5e7eb;
  position: relative;
  overflow: hidden;
}

.review-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  transition: all 0.3s ease;
}

.review-card.status-pending::before { background: linear-gradient(90deg, #fbbf24, #f59e0b); }
.review-card.status-approved::before { background: linear-gradient(90deg, #10b981, #059669); }
.review-card.status-rejected::before { background: linear-gradient(90deg, #ef4444, #dc2626); }

.review-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1.5rem 1.5rem 0 1.5rem;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.status-badge.pending {
  background: rgba(251, 191, 36, 0.1);
  color: #d97706;
  border: 1px solid rgba(251, 191, 36, 0.2);
}

.status-badge.approved {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.status-badge.rejected {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.status-icon {
  width: 12px;
  height: 12px;
}

.menu-container {
  position: relative;
}

.menu-btn {
  background: none;
  border: none;
  padding: 0.5rem;
  border-radius: 6px;
  cursor: pointer;
  color: #6b7280;
  transition: all 0.2s ease;
}

.menu-btn:hover {
  background: #f3f4f6;
  color: #374151;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 10;
  min-width: 150px;
}

.dropdown-item {
  padding: 0.75rem 1rem;
  color: #374151;
  text-decoration: none;
  display: block;
  font-size: 0.875rem;
  transition: background 0.2s ease;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  cursor: pointer;
}

.dropdown-item:hover {
  background: #f3f4f6;
}

.card-content {
  padding: 1.5rem;
}

.article-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #111827;
  margin: 0 0 0.75rem 0;
  line-height: 1.4;
  cursor: pointer;
  transition: color 0.2s ease;
}

.article-title:hover {
  color: #3b82f6;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 0.75rem;
  font-size: 0.875rem;
  color: #6b7280;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.meta-item svg {
  width: 16px;
  height: 16px;
}

.article-summary {
  color: #4b5563;
  font-size: 0.875rem;
  line-height: 1.5;
  margin-bottom: 1rem;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  padding: 0 1.5rem 1.5rem 1.5rem;
}

.review-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.75rem;
  color: #9ca3af;
  margin-bottom: 1rem;
}

.reject-reason {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: 8px;
  padding: 0.75rem;
  margin-bottom: 1rem;
}

.reason-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.75rem;
  font-weight: 600;
  color: #dc2626;
  margin-bottom: 0.5rem;
}

.reason-header svg {
  width: 16px;
  height: 16px;
}

.reason-text {
  font-size: 0.875rem;
  color: #7f1d1d;
  line-height: 1.4;
  margin: 0;
}

.card-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.action-btn svg {
  width: 16px;
  height: 16px;
}

.action-btn.primary {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.action-btn.primary:hover {
  background: #e5e7eb;
  border-color: #9ca3af;
}

.action-btn.success {
  background: #10b981;
  color: white;
}

.action-btn.success:hover {
  background: #059669;
}

.action-btn.danger {
  background: #ef4444;
  color: white;
}

.action-btn.danger:hover {
  background: #dc2626;
}

.action-btn.secondary {
  background: #6b7280;
  color: white;
}

.action-btn.secondary:hover {
  background: #4b5563;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0;
}

.modal-icon {
  width: 24px;
  height: 24px;
}

.modal-close {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 8px;
  padding: 0.5rem;
  cursor: pointer;
  color: white;
  transition: background 0.2s ease;
}

.modal-close:hover {
  background: rgba(255, 255, 255, 0.3);
}

.modal-close svg {
  width: 20px;
  height: 20px;
}

.modal-body {
  padding: 1.5rem;
}

.article-preview {
  background: #f8fafc;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1.5rem;
}

.article-preview .article-title {
  font-size: 1rem;
  font-weight: 600;
  color: #111827;
  margin: 0 0 0.25rem 0;
}

.article-preview .article-author {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0;
}

.form-group {
  margin-bottom: 1rem;
}

.form-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 0.5rem;
}

.form-textarea {
  width: 100%;
  min-height: 120px;
  padding: 0.75rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-family: inherit;
  font-size: 0.875rem;
  line-height: 1.5;
  resize: vertical;
  transition: border-color 0.2s ease;
}

.form-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.character-counter {
  text-align: right;
  font-size: 0.75rem;
  margin-top: 0.5rem;
}

.text-warning { color: #d97706; }
.text-success { color: #059669; }
.text-muted { color: #9ca3af; }

.warning-notice {
  display: flex;
  gap: 0.75rem;
  padding: 1rem;
  background: rgba(251, 191, 36, 0.1);
  border: 1px solid rgba(251, 191, 36, 0.2);
  border-radius: 8px;
  margin-top: 1rem;
}

.warning-icon {
  width: 20px;
  height: 20px;
  color: #d97706;
  flex-shrink: 0;
  margin-top: 0.125rem;
}

.warning-content {
  flex: 1;
}

.warning-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: #92400e;
  margin: 0 0 0.25rem 0;
}

.warning-text {
  font-size: 0.875rem;
  color: #78350f;
  margin: 0;
  line-height: 1.4;
}

.modal-footer {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  padding: 1.5rem;
  border-top: 1px solid #e5e7eb;
  background: #f8fafc;
}

.btn-cancel {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-cancel:hover {
  background: #e5e7eb;
  border-color: #9ca3af;
}

.btn-confirm {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-confirm:hover:not(:disabled) {
  background: #dc2626;
}

.btn-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-icon {
  width: 16px;
  height: 16px;
}

/* 分页样式 */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  background: white;
  border-top: 1px solid #e5e7eb;
  border-radius: 0 0 20px 20px;
}

.pagination-info {
  font-size: 0.875rem;
  color: #6b7280;
}

.pagination-info .highlight {
  font-weight: 600;
  color: #374151;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.pagination-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: white;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pagination-btn:hover:not(:disabled) {
  background: #f3f4f6;
  border-color: #9ca3af;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 0.25rem;
  margin: 0 0.5rem;
}

.page-number {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  color: #374151;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.page-number:hover {
  background: #f3f4f6;
  border-color: #9ca3af;
}

.page-number.active {
  background: #3b82f6;
  border-color: #3b82f6;
  color: white;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  }
  
  .review-grid.grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .review-management {
    padding: 1rem;
  }
  
  .container {
    border-radius: 12px;
  }
  
  .page-header {
    padding: 2rem 1rem;
  }
  
  .page-header h1 {
    font-size: 2rem;
  }
  
  .stats-dashboard,
  .toolbar,
  .review-container {
    padding-left: 1rem;
    padding-right: 1rem;
  }
  
  .search-filter-bar {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .filter-controls {
    flex-direction: column;
    align-items: stretch;
    gap: 0.75rem;
  }
  
  .view-toggle {
    align-self: center;
  }
  
  .pagination-container {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }
  
  .pagination-controls {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .modal-container {
    margin: 1rem;
    width: auto;
  }
  
  .modal-footer {
    flex-direction: column-reverse;
    gap: 0.5rem;
  }
  
  .btn-cancel,
  .btn-confirm {
    justify-content: center;
  }
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .card-actions {
    flex-direction: column;
  }
  
  .action-btn {
    justify-content: center;
  }
  
  .page-numbers {
    display: none;
  }
}

/* 动画效果 */
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.loading-text {
  animation: pulse 1.5s ease-in-out infinite;
}

/* 深色主题支持 */
@media (prefers-color-scheme: dark) {
  .review-management {
    background: linear-gradient(135deg, #1f2937 0%, #111827 100%);
  }
  
  .container {
    background: rgba(31, 41, 55, 0.95);
    color: #f9fafb;
  }
  
  .stats-dashboard,
  .toolbar,
  .review-container {
    background: #1f2937;
  }
  
  .stat-card,
  .review-card {
    background: #374151;
    border-color: #4b5563;
    color: #f9fafb;
  }
  
  .search-input,
  .status-filter {
    background: #374151;
    border-color: #4b5563;
    color: #f9fafb;
  }
  
  .search-input:focus,
  .status-filter:focus {
    background: #4b5563;
    border-color: #3b82f6;
  }
}
</style>