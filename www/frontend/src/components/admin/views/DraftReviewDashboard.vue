<!--
/**
 * 草稿审核管理界面组件
 * 
 * 功能：
 * - 提供管理员文章草稿审核功能
 * - 实现草稿列表查看和筛选
 * - 支持草稿审批、拒绝操作
 * - 提供审核历史和统计信息
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
-->
<template>
  <div class="draft-review-dashboard">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z" />
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">草稿审核</h1>
          <p class="page-subtitle">管理文章审核</p>
        </div>
      </div>
      <div class="header-actions">
        <button 
          @click="refreshCache" 
          :disabled="loading"
          class="btn btn-refresh"
          title="刷新数据缓存"
        >
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M17.65,6.35C16.2,4.9 14.21,4 12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20C15.73,20 18.84,17.45 19.73,14H17.65C16.83,16.33 14.61,18 12,18A6,6 0 0,1 6,12A6,6 0 0,1 12,6C13.66,6 15.14,6.69 16.22,7.78L13,11H20V4L17.65,6.35Z" />
          </svg>
          <span>刷新缓存</span>
        </button>
      </div>
    </div>

    <!-- 可点击的统计卡片网格 -->
    <div class="stats-grid">
      <div 
        class="stat-card clickable" 
        :class="{ active: selectedStatus === 'PENDING', pending: true }"
        @click="filterByStatus('PENDING')"
      >
        <div class="stat-icon">
          <svg viewBox="0 0 24 24">
            <path d="M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2M12,17A5,5 0 0,1 7,12A5,5 0 0,1 12,7A5,5 0 0,1 17,12A5,5 0 0,1 12,17M12,9A3,3 0 0,0 9,12A3,3 0 0,0 12,15A3,3 0 0,0 15,12A3,3 0 0,0 12,9Z" />
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.pending }}</div>
          <div class="stat-label">待审核</div>
        </div>
      </div>
      
      <div 
        class="stat-card clickable" 
        :class="{ active: selectedStatus === 'APPROVED', approved: true }"
        @click="filterByStatus('APPROVED')"
      >
        <div class="stat-icon">
          <svg viewBox="0 0 24 24">
            <path d="M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2M11,16.5L6.5,12L7.91,10.59L11,13.67L16.59,8.09L18,9.5L11,16.5Z" />
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.approved }}</div>
          <div class="stat-label">已通过</div>
        </div>
      </div>
      
      <div 
        class="stat-card clickable" 
        :class="{ active: selectedStatus === 'REJECTED', rejected: true }"
        @click="filterByStatus('REJECTED')"
      >
        <div class="stat-icon">
          <svg viewBox="0 0 24 24">
            <path d="M12,2C17.53,2 22,6.47 22,12C22,17.53 17.53,22 12,22C6.47,22 2,17.53 2,12C2,6.47 6.47,2 12,2M15.59,7L12,10.59L8.41,7L7,8.41L10.59,12L7,15.59L8.41,17L12,13.41L15.59,17L17,15.59L13.41,12L17,8.41L15.59,7Z" />
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.rejected }}</div>
          <div class="stat-label">已拒绝</div>
        </div>
      </div>

      <div 
        class="stat-card clickable" 
        :class="{ active: selectedStatus === 'ALL', total: true }"
        @click="filterByStatus('ALL')"
      >
        <div class="stat-icon">
          <svg viewBox="0 0 24 24">
            <path d="M19,3H5C3.89,3 3,3.89 3,5V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V5C21,3.89 20.1,3 19,3M5,19V5H19V19H5Z" />
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.pending + stats.approved + stats.rejected }}</div>
          <div class="stat-label">全部草稿</div>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-section">
      <div class="search-group">
        <input 
          type="text" 
          v-model="searchKeyword" 
          @input="onSearchInput"
          placeholder="搜索标题或作者..."
          class="search-input"
        >
        <svg viewBox="0 0 24 24" class="search-icon">
          <path d="M9.5,3A6.5,6.5 0 0,1 16,9.5C16,11.11 15.41,12.59 14.44,13.73L14.71,14H15.5L20.5,19L19,20.5L14,15.5V14.71L13.73,14.44C12.59,15.41 11.11,16 9.5,16A6.5,6.5 0 0,1 3,9.5A6.5,6.5 0 0,1 9.5,3M9.5,5C7,5 5,7 5,9.5C5,12 7,14 9.5,14C12,14 14,12 14,9.5C14,7 12,5 9.5,5Z" />
        </svg>
        <button v-if="searchKeyword" @click="clearSearch" class="clear-search-btn">
          <svg viewBox="0 0 24 24">
            <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z" />
          </svg>
        </button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p class="loading-text">
        {{ !initialLoadComplete ? '正在初始化数据缓存...' : '正在加载草稿数据...' }}
      </p>
      <p class="loading-subtitle">
        {{ !initialLoadComplete ? '首次加载需要缓存所有数据，请稍候' : '请稍候' }}
      </p>
    </div>

    <!-- 错误提示 -->
    <div v-if="error" class="error-container">
      <p class="error-message">{{ error }}</p>
      <button @click="loadDrafts" class="retry-btn">重试</button>
    </div>

    <!-- 草稿列表 -->
    <div v-if="!loading && !error" class="drafts-list">
      <div v-if="drafts.length === 0" class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24">
            <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z" />
          </svg>
        </div>
        <h3 class="empty-title">
          {{ searchKeyword ? '未找到匹配的草稿' : getEmptyStateMessage() }}
        </h3>
        <p class="empty-description">
          {{ searchKeyword ? '请尝试调整搜索条件' : getEmptyStateDescription() }}
        </p>
        <div v-if="searchKeyword" class="empty-actions">
          <button @click="clearSearch" class="btn btn-secondary">
            清除搜索条件
          </button>
        </div>
      </div>
      
      <div class="drafts-list-container">
        <div 
          v-for="draft in drafts" 
          :key="draft.draftId" 
          class="draft-item-row"
          @click="openDraftDetailModal(draft)"
        >
          <div class="draft-left-section">
            <div class="draft-info">
              <h3 class="draft-title">{{ draft.title }}</h3>
              <div class="draft-meta">
                <UserAvatar 
                  :username="draft.editorUserName || `用户${draft.editorUserId}`"
                  :avatar-url="draft.editorUserAvatar || ''"
                  size="xs"
                  shape="circle"
                  class="author-avatar"
                />
                <span class="draft-author">{{ draft.editorUserName || `用户${draft.editorUserId}` }}</span>
                <span class="meta-separator">•</span>
                <span class="draft-category">{{ draft.category || '未分类' }}</span>
              </div>
            </div>
          </div>
          
          <div class="draft-right-section">
            <div class="draft-timestamps">
              <div class="submit-time">
                <span class="time-label">提交时间：</span>
                <span class="time-value">{{ formatDateTime(draft.submittedAt) }}</span>
              </div>
              <div v-if="draft.reviewedAt" class="update-time">
                <span class="time-label">审核时间：</span>
                <span class="time-value">{{ formatDateTime(draft.reviewedAt) }}</span>
              </div>
            </div>
            <div class="status-badge" :class="draft.reviewStatus.toLowerCase()">
              <svg v-if="draft.reviewStatus === 'PENDING'" viewBox="0 0 24 24" class="status-icon">
                <path d="M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2M12,17A5,5 0 0,1 7,12A5,5 0 0,1 12,7A5,5 0 0,1 17,12A5,5 0 0,1 12,17M12,9A3,3 0 0,0 9,12A3,3 0 0,0 12,15A3,3 0 0,0 15,12A3,3 0 0,0 12,9Z" />
              </svg>
              <svg v-else-if="draft.reviewStatus === 'APPROVED'" viewBox="0 0 24 24" class="status-icon">
                <path d="M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2M11,16.5L6.5,12L7.91,10.59L11,13.67L16.59,8.09L18,9.5L11,16.5Z" />
              </svg>
              <svg v-else viewBox="0 0 24 24" class="status-icon">
                <path d="M12,2C17.53,2 22,6.47 22,12C22,17.53 17.53,22 12,22C6.47,22 2,17.53 2,12C2,6.47 6.47,2 12,2M15.59,7L12,10.59L8.41,7L7,8.41L10.59,12L7,15.59L8.41,17L12,13.41L15.59,17L17,15.59L13.41,12L17,8.41L15.59,7Z" />
              </svg>
              <span class="status-text">
                {{ draft.reviewStatus === 'PENDING' ? '待审核' : 
                   draft.reviewStatus === 'APPROVED' ? '已通过' : '已拒绝' }}
              </span>
            </div>
          </div>
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

    <!-- 草稿详情模态框 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content modal-detail" @click.stop>
        <div class="modal-header">
          <h3>草稿详情</h3>
          <button @click="closeDetailModal" class="close-btn">×</button>
        </div>
        
        <div class="modal-body modal-detail-body">
          <div v-if="selectedDraft" class="draft-detail">
            <!-- 标题 -->
            <div class="detail-section">
              <h2 class="detail-title">{{ selectedDraft.title }}</h2>
            </div>
            
            <!-- 基本信息 -->
            <div class="detail-section">
              <div class="detail-info-grid">
                <div class="detail-info-item author-item">
                  <div class="info-label">作者</div>
                  <div class="info-value author-info">
                    <UserAvatar 
                      :username="selectedDraft.editorUserName || `用户${selectedDraft.editorUserId}`"
                      :avatar-url="selectedDraft.editorUserAvatar || ''"
                      size="sm"
                      shape="circle"
                      class="author-avatar-small"
                    />
                    <span class="author-name">{{ selectedDraft.editorUserName || `用户${selectedDraft.editorUserId}` }}</span>
                  </div>
                </div>
                <div class="detail-info-item">
                  <div class="info-label">提交时间</div>
                  <div class="info-value">{{ formatDate(selectedDraft.submittedAt) }}</div>
                </div>
                <div class="detail-info-item">
                  <div class="info-label">审核状态</div>
                  <div class="info-value">
                    <span class="status-badge" :class="selectedDraft.reviewStatus.toLowerCase()">
                      {{ getStatusText(selectedDraft.reviewStatus) }}
                    </span>
                  </div>
                </div>
                <div v-if="selectedDraft.category" class="detail-info-item">
                  <div class="info-label">分类</div>
                  <div class="info-value">{{ selectedDraft.category }}</div>
                </div>
              </div>
            </div>
            
            <!-- 审核信息 -->
            <div v-if="selectedDraft.reviewedAt" class="detail-section">
              <h4 class="section-title">审核信息</h4>
              <div class="detail-info-grid">
                <div class="detail-info-item">
                  <div class="info-label">审核时间</div>
                  <div class="info-value">{{ formatDate(selectedDraft.reviewedAt) }}</div>
                </div>
                <div v-if="selectedDraft.reviewerUserId" class="detail-info-item">
                  <div class="info-label">审核者</div>
                  <div class="info-value">{{ selectedDraft.reviewerUserName || `用户ID ${selectedDraft.reviewerUserId}` }}</div>
                </div>
              </div>
              <div v-if="selectedDraft.reviewNotes" class="review-notes-detail">
                <div class="info-label">审核备注</div>
                <div class="info-value">{{ selectedDraft.reviewNotes }}</div>
              </div>
            </div>
            
            <!-- 文章内容 -->
            <div class="detail-section">
              <h4 class="section-title">文章内容</h4>
              <div class="content-detail">
                {{ selectedDraft.content || '无内容' }}
              </div>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="closeDetailModal" class="cancel-btn">关闭</button>
          <div v-if="selectedDraft && selectedDraft.reviewStatus === 'PENDING'" class="review-buttons">
            <button @click="openReviewModal(selectedDraft, true)" class="btn btn-success">
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M21,7L9,19L3.5,13.5L4.91,12.09L9,16.17L19.59,5.59L21,7Z" />
              </svg>
              通过
            </button>
            <button @click="openReviewModal(selectedDraft, false)" class="btn btn-danger">
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z" />
              </svg>
              拒绝
            </button>
          </div>
        </div>
      </div>
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
            <div class="editor-info">
              <UserAvatar 
                v-if="reviewData.draft"
                :username="reviewData.draft.editorUserName || `用户${reviewData.draft.editorUserId}`"
                :avatar-url="reviewData.draft.editorUserAvatar || ''"
                size="sm"
                shape="circle"
                class="editor-avatar"
              />
              <span class="editor-text">编辑者：{{ reviewData.draft?.editorUserName || `用户ID ${reviewData.draft?.editorUserId}` }}</span>
            </div>
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
import { adminApi, type UserResponse } from '../../../api/user'
import UserAvatar from '../../common/UserAvatar.vue'
import toast from '../../../utils/toast'

// 响应式数据
const loading = ref(false)
const error = ref('')
const drafts = ref<ArticleDraft[]>([])
const allDrafts = ref<ArticleDraft[]>([]) // 存储所有草稿用于统计
const selectedStatus = ref<'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED'>('PENDING')
const searchKeyword = ref('')
const searchTimeout = ref<number | null>(null)

// 缓存相关
const isDataCached = ref(false) // 标记数据是否已缓存
const allDraftsCache = ref<Map<string, ArticleDraft[]>>(new Map()) // 按状态缓存草稿
const initialLoadComplete = ref(false) // 标记初始加载是否完成

// 用户信息缓存
const userCache = ref<Map<number, UserResponse>>(new Map())

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

// 详情模态框
const showDetailModal = ref(false)
const selectedDraft = ref<ArticleDraft | null>(null)

// 统计数据 - 基于所有草稿计算
const stats = computed(() => {
  const pending = allDrafts.value.filter(d => d.reviewStatus === 'PENDING').length
  const approved = allDrafts.value.filter(d => d.reviewStatus === 'APPROVED').length
  const rejected = allDrafts.value.filter(d => d.reviewStatus === 'REJECTED').length
  return { pending, approved, rejected }
})

/**
 * 获取用户信息缓存 - 优化版本，使用getAllActiveUsers API
 */
const getUserInfo = async (userId: number): Promise<UserResponse | null> => {
  // 先检查缓存
  if (userCache.value.has(userId)) {
    return userCache.value.get(userId) || null
  }
  
  try {
    // 如果缓存为空，批量加载所有用户（一次性加载，避免重复请求）
    if (userCache.value.size === 0) {
      console.log('正在批量加载用户数据...')
      
      // 尝试使用getAllActiveUsers API，因为它返回的数据格式更适合
      const response = await adminApi.getAllActiveUsers()
      console.log('用户API响应:', response)
      
      if (response && Array.isArray(response)) {
        console.log('用户数据条数:', response.length)
        response.forEach((user: any) => {
          // 将UserContactDto转换为UserResponse格式
          const userResponse: UserResponse = {
            userId: user.userId,
            username: user.username,
            email: user.email,
            fullName: user.fullName || user.username,
            userGroup: 'user', // 默认值，因为后端没有提供
            active: user.active,
            avatarUrl: user.avatarUrl || '', // 可能为空
            createdAt: '',
            updatedAt: ''
          }
          
          console.log('缓存用户:', userResponse.userId, userResponse.username, userResponse.avatarUrl)
          userCache.value.set(userResponse.userId, userResponse)
        })
      } else {
        console.error('用户API返回数据格式错误:', response)
        // 降级到getUsers API
        const fallbackResponse = await adminApi.getUsers(0, 1000)
        if (fallbackResponse && fallbackResponse.data && fallbackResponse.data.content) {
          fallbackResponse.data.content.forEach((user: UserResponse) => {
            userCache.value.set(user.userId, user)
          })
        }
      }
    }
    
    const user = userCache.value.get(userId)
    if (user) {
      console.log(`找到用户 ${userId}:`, user.username, user.avatarUrl || '无头像')
    } else {
      console.log(`未找到用户 ${userId}`)
    }
    
    return user || null
  } catch (err) {
    console.warn(`获取用户信息失败 (ID: ${userId}):`, err)
    return null
  }
}

/**
 * 强制刷新缓存（清除缓存并重新加载）
 */
const refreshCache = async () => {
  console.log('强制刷新缓存...')
  isDataCached.value = false
  allDraftsCache.value.clear()
  userCache.value.clear()
  await initializeCache()
}

/**
 * 批量获取用户信息映射
 */
const getUserInfoBatch = async (userIds: number[]): Promise<Map<number, UserResponse>> => {
  const userMap = new Map<number, UserResponse>()
  
  // 去重并过滤掉已缓存的用户ID
  const uniqueUserIds = [...new Set(userIds)].filter(id => !userCache.value.has(id))
  
  if (uniqueUserIds.length === 0) {
    // 所有用户都已缓存，直接从缓存返回
    userIds.forEach(id => {
      const user = userCache.value.get(id)
      if (user) userMap.set(id, user)
    })
    return userMap
  }
  
  try {
    console.log('批量获取用户信息，需要获取的用户ID:', uniqueUserIds)
    
    // 一次性获取所有活跃用户
    const response = await adminApi.getAllActiveUsers()
    console.log('获取到的用户数据数量:', response?.length || 0)
    
    if (response && Array.isArray(response)) {
      // 缓存所有获取到的用户
      response.forEach((user: any) => {
        const userInfo: UserResponse = {
          userId: user.userId,
          username: user.username,
          email: user.email,
          fullName: user.fullName,
          userGroup: user.userGroup || 'user',
          active: user.active !== false,
          avatarUrl: user.avatarUrl || '',
          createdAt: user.createdAt || '',
          updatedAt: user.updatedAt || ''
        }
        userCache.value.set(user.userId, userInfo)
      })
      
      console.log('用户缓存更新完成，缓存数量:', userCache.value.size)
    }
    
    // 返回所有请求的用户信息
    userIds.forEach(id => {
      const user = userCache.value.get(id)
      if (user) userMap.set(id, user)
    })
    
    return userMap
  } catch (err) {
    console.warn('批量获取用户信息失败:', err)
    return userMap
  }
}

/**
 * 初始化数据缓存 - 一次性加载所有状态的草稿
 */
const initializeCache = async () => {
  loading.value = true
  error.value = ''
  
  try {
    console.log('开始初始化缓存，加载所有草稿数据...')
    
    // 并行获取所有状态的草稿
    const [pendingRes, approvedRes, rejectedRes] = await Promise.all([
      draftApi.getDraftsByStatus('PENDING', 0, 1000),
      draftApi.getDraftsByStatus('APPROVED', 0, 1000), 
      draftApi.getDraftsByStatus('REJECTED', 0, 1000)
    ])
    
    // 合并所有草稿
    const allDraftsArray = [
      ...pendingRes.content,
      ...approvedRes.content,
      ...rejectedRes.content
    ]
    
    console.log('获取到草稿总数:', allDraftsArray.length)
    
    // 提取所有需要的用户ID
    const allUserIds = new Set<number>()
    allDraftsArray.forEach((draft: ArticleDraft) => {
      allUserIds.add(draft.editorUserId)
      if (draft.reviewerUserId) {
        allUserIds.add(draft.reviewerUserId)
      }
    })
    
    console.log('需要获取的用户ID数量:', allUserIds.size)
    
    // 批量获取所有用户信息
    const userInfoMap = await getUserInfoBatch(Array.from(allUserIds))
    console.log('获取到的用户信息数量:', userInfoMap.size)
    
    // 给所有草稿附加用户信息
    const enrichedDrafts = allDraftsArray.map((draft: ArticleDraft) => {
      const editorUser = userInfoMap.get(draft.editorUserId)
      const reviewerUser = draft.reviewerUserId ? userInfoMap.get(draft.reviewerUserId) : null
      
      return {
        ...draft,
        editorUserName: editorUser?.username || editorUser?.fullName || `用户${draft.editorUserId}`,
        editorUserAvatar: editorUser?.avatarUrl || '',
        reviewerUserName: reviewerUser?.username || reviewerUser?.fullName || (draft.reviewerUserId ? `用户${draft.reviewerUserId}` : undefined)
      }
    })
    
    // 按状态分组缓存
    const pendingDrafts = enrichedDrafts.filter(d => d.reviewStatus === 'PENDING')
    const approvedDrafts = enrichedDrafts.filter(d => d.reviewStatus === 'APPROVED') 
    const rejectedDrafts = enrichedDrafts.filter(d => d.reviewStatus === 'REJECTED')
    
    allDraftsCache.value.set('PENDING', pendingDrafts)
    allDraftsCache.value.set('APPROVED', approvedDrafts)
    allDraftsCache.value.set('REJECTED', rejectedDrafts)
    allDraftsCache.value.set('ALL', enrichedDrafts)
    
    // 设置统计用的全部草稿
    allDrafts.value = enrichedDrafts
    
    // 标记缓存完成
    isDataCached.value = true
    initialLoadComplete.value = true
    
    console.log('缓存初始化完成:', {
      待审核: pendingDrafts.length,
      已通过: approvedDrafts.length,
      已拒绝: rejectedDrafts.length,
      总数: enrichedDrafts.length
    })
    
    // 显示当前选中状态的草稿
    applyCurrentFilter()
    
  } catch (err: any) {
    error.value = err.message || '初始化数据缓存失败'
    console.error('初始化缓存失败:', err)
  } finally {
    loading.value = false
  }
}

/**
 * 审核后更新缓存
 */
const updateCacheAfterReview = async (reviewedDraft: ArticleDraft, approved: boolean) => {
  try {
    // 新的审核状态
    const newStatus = approved ? 'APPROVED' : 'REJECTED'
    
    // 从原状态缓存中移除
    const oldStatus = reviewedDraft.reviewStatus
    const oldCache = allDraftsCache.value.get(oldStatus) || []
    const filteredOldCache = oldCache.filter(d => d.draftId !== reviewedDraft.draftId)
    allDraftsCache.value.set(oldStatus, filteredOldCache)
    
    // 更新草稿状态
    const updatedDraft: ArticleDraft = {
      ...reviewedDraft,
      reviewStatus: newStatus as 'APPROVED' | 'REJECTED',
      reviewedAt: new Date().toISOString()
    }
    
    // 添加到新状态缓存
    const newCache = allDraftsCache.value.get(newStatus) || []
    newCache.unshift(updatedDraft) // 添加到开头
    allDraftsCache.value.set(newStatus, newCache)
    
    // 更新ALL缓存
    const allCache = allDraftsCache.value.get('ALL') || []
    const allCacheFiltered = allCache.filter(d => d.draftId !== reviewedDraft.draftId)
    allCacheFiltered.unshift(updatedDraft)
    allDraftsCache.value.set('ALL', allCacheFiltered)
    
    // 更新统计用的allDrafts
    allDrafts.value = allCacheFiltered
    
    // 重新应用当前筛选
    applyCurrentFilter()
    
    console.log(`缓存更新完成: 草稿${reviewedDraft.draftId}从${oldStatus}变为${newStatus}`)
    
  } catch (err) {
    console.error('更新缓存失败:', err)
    // 如果缓存更新失败，重新初始化缓存
    await initializeCache()
  }
}

/**
 * 应用当前筛选条件（从缓存中获取数据）
 */
const applyCurrentFilter = () => {
  if (!isDataCached.value) {
    return
  }
  
  let filteredDrafts = allDraftsCache.value.get(selectedStatus.value) || []
  
  // 如果有搜索关键词，进行前端过滤
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase().trim()
    filteredDrafts = filteredDrafts.filter((draft: any) => 
      draft.title.toLowerCase().includes(keyword) ||
      draft.content.toLowerCase().includes(keyword) ||
      draft.editorUserName.toLowerCase().includes(keyword)
    )
  }
  
  drafts.value = filteredDrafts
  
  console.log(`筛选结果 [${selectedStatus.value}]:`, filteredDrafts.length, '条记录')
}

/**
 * 加载草稿列表 - 优化版本（支持缓存）
 */
const loadDrafts = async () => {
  // 如果数据已缓存，直接从缓存获取
  if (isDataCached.value) {
    applyCurrentFilter()
    return
  }
  
  // 如果是首次加载，初始化缓存
  await initializeCache()
}

/**
 * 刷新数据
 */
const refreshData = async () => {
  currentPage.value = 0
  await initializeCache() // 重新初始化缓存数据
  await loadDrafts()
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
 * 提交审核 - 增强版本
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
    console.log('开始提交审核:', {
      draftId: reviewData.value.draft.draftId,
      approved: reviewData.value.approved,
      notes: reviewData.value.notes
    })
    
    const result = await draftApi.reviewDraft(reviewData.value.draft.draftId, {
      action: reviewData.value.approved ? 'approve' : 'reject',
      comment: reviewData.value.notes
    })
    
    console.log('审核提交成功:', result)
    
    const action = reviewData.value.approved ? '通过' : '拒绝'
    toast.success(`草稿《${reviewData.value.draft.title}》已${action}审核`)
    
    closeReviewModal()
    
    // 更新缓存：如果数据已缓存，手动更新缓存状态
    if (isDataCached.value) {
      await updateCacheAfterReview(reviewData.value.draft, reviewData.value.approved)
    } else {
      // 如果数据未缓存，重新初始化缓存
      await initializeCache()
    }
    
  } catch (err: any) {
    console.error('审核失败:', err)
    const errorMessage = err.message || '审核操作失败，请稍后重试'
    toast.error(errorMessage)
    
    // 如果是网络错误，提供重试选项
    if (err.code === 'NETWORK_ERROR' || err.message?.includes('网络')) {
      toast.error('网络连接异常，请检查网络后重试')
    }
  } finally {
    submittingReview.value = false
  }
}

/**
 * 打开草稿详情模态框
 */
const openDraftDetailModal = (draft: ArticleDraft) => {
  selectedDraft.value = draft
  showDetailModal.value = true
}

/**
 * 关闭草稿详情模态框
 */
const closeDetailModal = () => {
  showDetailModal.value = false
  selectedDraft.value = null
}

/**
 * 查看草稿详情
 */
const viewDraftDetail = (draft: ArticleDraft) => {
  openDraftDetailModal(draft)
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
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '未知时间'
  return new Date(dateStr).toLocaleString('zh-CN')
}

/**
 * 格式化日期时间 - 更紧凑的格式
 */
const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '未知时间'
  const date = new Date(dateStr)
  const now = new Date()
  const diffTime = now.getTime() - date.getTime()
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
  
  if (diffDays === 0) {
    // 今天 - 显示时间
    return date.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  } else if (diffDays === 1) {
    // 昨天
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  } else if (diffDays < 7) {
    // 一周内 - 显示星期几
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return weekdays[date.getDay()] + ' ' + date.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  } else {
    // 超过一周 - 显示日期
    return date.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}

/**
 * 获取内容预览
 */
const getContentPreview = (content: string) => {
  if (!content) return '无内容'
  return content.length > 200 ? content.substring(0, 200) + '...' : content
}

/**
 * 通过状态筛选 - 优化版本（使用缓存）
 */
const filterByStatus = (status: 'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED') => {
  selectedStatus.value = status
  currentPage.value = 0
  
  // 如果数据已缓存，直接应用筛选
  if (isDataCached.value) {
    applyCurrentFilter()
  } else {
    // 如果数据未缓存，加载数据
    loadDrafts()
  }
}

/**
 * 清除搜索 - 优化版本（使用缓存）
 */
const clearSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 0
  
  // 如果数据已缓存，直接应用筛选
  if (isDataCached.value) {
    applyCurrentFilter()
  } else {
    loadDrafts()
  }
}

/**
 * 搜索输入处理 - 优化版本（使用缓存）
 */
const onSearchInput = () => {
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }
  
  searchTimeout.value = setTimeout(() => {
    currentPage.value = 0 // 重置到第一页
    
    // 如果数据已缓存，直接应用筛选
    if (isDataCached.value) {
      applyCurrentFilter()
    } else {
      loadDrafts()
    }
  }, 300) // 减少防抖时间，提升响应速度
}

/**
 * 处理图片加载错误
 */
const handleImageError = (event: Event, draft?: any) => {
  const img = event.target as HTMLImageElement
  console.warn('头像加载失败:', img.src)
  
  // 如果传入了draft对象，标记头像加载失败
  if (draft) {
    draft.avatarError = true
  }
  
  // 隐藏失败的图片，显示默认头像
  img.style.display = 'none'
}

/**
 * 处理图片加载成功
 */
const handleImageLoad = (draft?: any) => {
  // 如果传入了draft对象，清除错误标记
  if (draft) {
    draft.avatarError = false
  }
}

/**
 * 获取头像URL - 处理相对路径和完整URL
 */
const getAvatarUrl = (avatarPath: string | undefined): string => {
  if (!avatarPath) return ''
  
  // 如果已经是完整的URL，直接返回
  if (avatarPath.startsWith('http://') || avatarPath.startsWith('https://')) {
    return avatarPath
  }
  
  // 如果是相对路径，拼接基础URL
  const baseUrl = 'http://localhost:8080'
  if (avatarPath.startsWith('/')) {
    return `${baseUrl}${avatarPath}`
  } else {
    return `${baseUrl}/${avatarPath}`
  }
}

/**
 * 获取空状态消息
 */
const getEmptyStateMessage = () => {
  switch (selectedStatus.value) {
    case 'PENDING': return '暂无待审核的草稿'
    case 'APPROVED': return '暂无已通过的草稿'
    case 'REJECTED': return '暂无已拒绝的草稿'
    case 'ALL': return '暂无草稿数据'
    default: return '暂无草稿数据'
  }
}

/**
 * 获取空状态描述
 */
const getEmptyStateDescription = () => {
  switch (selectedStatus.value) {
    case 'PENDING': return '当前没有需要审核的文章草稿'
    case 'APPROVED': return '当前没有已通过审核的文章草稿'
    case 'REJECTED': return '当前没有已拒绝的文章草稿'
    case 'ALL': return '系统中还没有任何文章草稿'
    default: return '当前状态下没有草稿数据'
  }
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
/* === 基础布局样式 === */
.draft-review-dashboard {
  padding: 0;
  max-width: 100%;
  margin: 0;
  background: #f8f9fa;
  min-height: 100vh;
}

/* === 页面标题区域 === */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32px;
  background: none;
  border: none;
  margin: 0;
  border-radius: 0;
  margin: 0;
  box-shadow: none;
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
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #1a202c;
  letter-spacing: -0.025em;
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: #718096;
  line-height: 1.4;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn {
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  text-decoration: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.btn-refresh .icon {
  width: 18px;
  height: 18px;
  fill: white;
  transition: transform 0.3s ease;
}

.btn-refresh {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  font-weight: 500;
  letter-spacing: 0.025em;
}

.btn-refresh:hover:not(:disabled) {
  background: linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

.btn-refresh:hover:not(:disabled) .icon {
  transform: rotate(180deg);
}

.btn-refresh:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-refresh:disabled .icon {
  transform: none;
}

/* === 统计卡片网格 === */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin: 32px;
  margin-bottom: 0;
}

/* === 搜索区域样式 === */
.search-section {
  margin: 32px;
  margin-top: 32px;
  margin-bottom: 24px; /* 增加与下方列表的距离 */
  display: flex;
  justify-content: flex-start;
}

.search-group {
  position: relative;
  max-width: 400px;
  flex: 1;
}

.search-input {
  width: 100%;
  padding: 12px 16px 12px 44px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  color: #4a5568;
  transition: all 0.2s ease;
  background: white;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-input::placeholder {
  color: #a0aec0;
}

.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  fill: #a0aec0;
  pointer-events: none;
}

.clear-search-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  padding: 4px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.clear-search-btn:hover {
  background: #f7fafc;
}

.clear-search-btn svg {
  width: 16px;
  height: 16px;
  fill: #a0aec0;
}

/* === 内容区域样式 === */
.drafts-list {
  padding: 24px 32px 32px 32px; /* 增加顶部padding */
}

.drafts-list-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* === 列表行样式 === */
.draft-item-row {
  background: white;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 0.2s ease;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 72px;
}

.draft-item-row:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #667eea;
}

.draft-left-section {
  display: flex;
  align-items: flex-start;
  gap: 0;
  flex: 2;
  min-width: 0;
}

.draft-right-section {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
  flex-shrink: 0;
  justify-content: flex-end;
}

.author-avatar {
  flex-shrink: 0;
  margin-right: 6px;
}

.draft-info {
  flex: 1;
  min-width: 0;
}

.draft-title {
  margin: 0 0 6px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1a202c;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.draft-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #718096;
}

.draft-author {
  font-weight: 500;
  color: #4a5568;
  font-size: 12px;
}

.meta-separator {
  color: #cbd5e0;
}

.draft-category {
  color: #718096;
  font-size: 12px;
}

.draft-timestamps {
  text-align: right;
  font-size: 12px;
  line-height: 1.4;
}

.submit-time, .update-time {
  margin-bottom: 2px;
}

.time-label {
  color: #a0aec0;
  margin-right: 4px;
}

.time-value {
  color: #4a5568;
  font-weight: 500;
}

.status-badge {
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  min-width: 80px;
  justify-content: center;
}

.status-badge.pending {
  background: linear-gradient(135deg, #fed7d7, #fbb6ce);
  color: #c53030;
}

.status-badge.approved {
  background: linear-gradient(135deg, #c6f6d5, #9ae6b4);
  color: #25543e;
}

.status-badge.rejected {
  background: linear-gradient(135deg, #fed7d7, #fc8181);
  color: #c53030;
}

.status-icon {
  width: 14px;
  height: 14px;
  fill: currentColor;
}

.status-text {
  font-size: 11px;
  font-weight: 600;
}

.loading-container, 
.error-container {
  margin: 32px;
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.loading-text {
  font-size: 16px;
  color: #4a5568;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.loading-subtitle {
  font-size: 14px;
  color: #718096;
  margin: 0;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
  margin: 0 32px 32px 32px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 14px;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.stat-card.clickable {
  cursor: pointer;
  user-select: none;
}

.stat-card.clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-card.active {
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
}

.stat-card.pending {
  border-left: 4px solid #f6ad55;
}

.stat-card.approved {
  border-left: 4px solid #68d391;
}

.stat-card.rejected {
  border-left: 4px solid #fc8181;
}

.stat-card.total {
  border-left: 4px solid #667eea;
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

.stat-card.pending .stat-icon {
  background: linear-gradient(135deg, #f6ad55, #fbd38d);
}

.stat-card.approved .stat-icon {
  background: linear-gradient(135deg, #68d391, #9ae6b4);
}

.stat-card.rejected .stat-icon {
  background: linear-gradient(135deg, #fc8181, #feb2b2);
}

.stat-card.total .stat-icon {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.stat-icon svg {
  width: 24px;
  height: 24px;
  fill: white;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #718096;
  font-weight: 500;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 24px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  color: #e53e3e;
  font-size: 16px;
  margin-bottom: 24px;
}

.retry-btn {
  padding: 12px 24px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.retry-btn:hover {
  background: #5a67d8;
  transform: translateY(-1px);
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  color: #718096;
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 24px;
  background: linear-gradient(135deg, #e2e8f0, #cbd5e0);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-icon svg {
  width: 32px;
  height: 32px;
  fill: #a0aec0;
}

.empty-title {
  margin: 0 0 12px 0;
  font-size: 20px;
  font-weight: 600;
  color: #4a5568;
}

.empty-description {
  margin: 0 0 24px 0;
  font-size: 16px;
  color: #718096;
  line-height: 1.5;
}

.empty-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

/* === 草稿卡片样式 === */
.draft-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid #e2e8f0;
}

.draft-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.draft-main {
  padding: 24px;
}

.draft-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  gap: 16px;
}

.draft-title-section {
  flex: 1;
}

.draft-title {
  margin: 0 0 12px 0;
  font-size: 20px;
  font-weight: 700;
  color: #1a202c;
  line-height: 1.3;
}

.draft-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #718096;
}

.meta-icon {
  width: 16px;
  height: 16px;
  fill: #a0aec0;
  flex-shrink: 0;
}

.draft-status-section {
  flex-shrink: 0;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.025em;
}

.status-badge.pending {
  background: linear-gradient(135deg, #fed7d7, #fbb6ce);
  color: #c53030;
}

.status-badge.approved {
  background: linear-gradient(135deg, #c6f6d5, #9ae6b4);
  color: #25543e;
}

.status-badge.rejected {
  background: linear-gradient(135deg, #fed7d7, #feb2b2);
  color: #c53030;
}

.status-icon {
  width: 14px;
  height: 14px;
  fill: currentColor;
}

.draft-content {
  margin-bottom: 20px;
}

.content-preview {
  background: #f7fafc;
  border: 1px solid #e2e8f0;
  border-left: 4px solid #667eea;
  border-radius: 8px;
  padding: 16px;
  font-size: 14px;
  line-height: 1.6;
  color: #4a5568;
}

.review-summary {
  background: #f7fafc;
  border-radius: 8px;
  padding: 16px;
  margin-top: 16px;
}

.review-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #4a5568;
  margin-bottom: 8px;
}

.review-item:last-child {
  margin-bottom: 0;
}

.review-icon {
  width: 16px;
  height: 16px;
  fill: #718096;
  flex-shrink: 0;
}

.review-notes {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 14px;
  color: #4a5568;
  background: white;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  margin-top: 8px;
}

/* === 操作按钮样式 === */
.draft-actions {
  border-top: 1px solid #e2e8f0;
  padding: 20px 24px;
  background: #f8f9fa;
}

.action-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.review-buttons {
  display: flex;
  gap: 12px;
}

.btn-secondary {
  background: #f7fafc;
  color: #4a5568;
  border: 1px solid #e2e8f0;
}

.btn-secondary:hover {
  background: #edf2f7;
  border-color: #cbd5e0;
  transform: translateY(-1px);
}

.btn-success {
  background: linear-gradient(135deg, #48bb78, #68d391);
  color: white;
  border: none;
}

.btn-success:hover {
  background: linear-gradient(135deg, #38a169, #48bb78);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(72, 187, 120, 0.3);
}

.btn-danger {
  background: linear-gradient(135deg, #f56565, #fc8181);
  color: white;
  border: none;
}

.btn-danger:hover {
  background: linear-gradient(135deg, #e53e3e, #f56565);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(245, 101, 101, 0.3);
}

.page-btn {
  padding: 12px 24px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.page-btn:hover:not(:disabled) {
  background: #5a67d8;
  transform: translateY(-1px);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  transform: none;
}

.page-info {
  font-size: 14px;
  color: #718096;
  font-weight: 500;
}

/* === 模态框样式 === */
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
}

.modal-content {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease-out;
}

.modal-detail {
  max-width: 800px;
  max-height: 90vh;
}

.modal-detail-body {
  max-height: 70vh;
  overflow-y: auto;
}

/* === 详情模态框内容样式 === */
.draft-detail {
  padding: 0;
}

.detail-section {
  margin-bottom: 32px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-title {
  margin: 0 0 24px 0;
  font-size: 24px;
  font-weight: 700;
  color: #1a202c;
  line-height: 1.3;
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 8px;
}

.detail-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.detail-info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  font-weight: 600;
  color: #718096;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.info-value {
  font-size: 14px;
  color: #2d3748;
  font-weight: 500;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-avatar-small {
  flex-shrink: 0;
}

.author-name {
  font-size: 14px;
  color: #2d3748;
  font-weight: 500;
}

.review-notes-detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
  background: #f7fafc;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.content-detail {
  background: #f7fafc;
  border: 1px solid #e2e8f0;
  border-left: 4px solid #667eea;
  border-radius: 8px;
  padding: 20px;
  font-size: 14px;
  line-height: 1.6;
  color: #4a5568;
  white-space: pre-wrap;
  max-height: 300px;
  overflow-y: auto;
}

/* 状态徽章样式（复用） */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.025em;
}

.status-badge.pending {
  background: linear-gradient(135deg, #fed7d7, #fbb6ce);
  color: #c53030;
}

.status-badge.approved {
  background: linear-gradient(135deg, #c6f6d5, #9ae6b4);
  color: #25543e;
}

.status-badge.rejected {
  background: linear-gradient(135deg, #fed7d7, #feb2b2);
  color: #c53030;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #e2e8f0;
  background: #f8f9fa;
}

.modal-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1a202c;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #a0aec0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #edf2f7;
  color: #4a5568;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  max-height: 60vh;
}

.draft-summary {
  margin-bottom: 24px;
  padding: 16px;
  background: #f7fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.draft-summary h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a202c;
}

.draft-summary p {
  margin: 0;
  font-size: 14px;
  color: #718096;
}

.editor-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}

.editor-avatar {
  flex-shrink: 0;
}

.editor-text {
  font-size: 14px;
  color: #718096;
}

.review-form label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #4a5568;
}

.review-textarea {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  resize: vertical;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.5;
  color: #4a5568;
  transition: all 0.2s ease;
}

.review-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px;
  border-top: 1px solid #e2e8f0;
  background: #f8f9fa;
}

.cancel-btn {
  padding: 12px 24px;
  background: #f7fafc;
  color: #4a5568;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cancel-btn:hover {
  background: #edf2f7;
  border-color: #cbd5e0;
}

.submit-btn {
  padding: 12px 24px;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.submit-btn.approved {
  background: linear-gradient(135deg, #48bb78, #68d391);
}

.submit-btn.approved:hover:not(:disabled) {
  background: linear-gradient(135deg, #38a169, #48bb78);
  transform: translateY(-1px);
}

.submit-btn.rejected {
  background: linear-gradient(135deg, #f56565, #fc8181);
}

.submit-btn.rejected:hover:not(:disabled) {
  background: linear-gradient(135deg, #e53e3e, #f56565);
  transform: translateY(-1px);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* === 响应式设计 === */
@media (max-width: 1024px) {
  .draft-review-dashboard {
    padding: 24px;
  }
  
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .draft-review-dashboard {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-section {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .search-group {
    max-width: none;
  }
  
  .draft-header {
    flex-direction: column;
    gap: 12px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 12px;
  }
  
  .review-buttons {
    width: 100%;
    justify-content: space-between;
  }
  
  .modal-content {
    width: 95%;
    margin: 20px;
  }
}

@media (max-width: 480px) {
  .draft-review-dashboard {
    padding: 12px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
  
  .draft-main {
    padding: 20px;
  }
  
  .draft-actions {
    padding: 16px 20px;
  }
  
  .btn {
    padding: 10px 16px;
    font-size: 13px;
  }
  
  .meta-item {
    font-size: 13px;
  }
  
  .stat-number {
    font-size: 28px;
  }
  
  /* 移动端列表布局调整 */
  .draft-item-row {
    flex-direction: column;
    align-items: stretch;
    padding: 12px 16px;
    gap: 12px;
  }
  
  .draft-left-section {
    gap: 12px;
  }
  
  .draft-right-section {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
  }
  
  .draft-timestamps {
    text-align: left;
    font-size: 11px;
  }
  
  .status-badge {
    min-width: 70px;
    padding: 6px 10px;
    font-size: 11px;
  }
  
  .status-icon {
    width: 12px;
    height: 12px;
  }
}
</style>
