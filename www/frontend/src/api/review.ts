/**
 * 文章审核API模块
 * 
 * 说明：后端审核功能通过文章草稿系统实现。
 * 所有审核操作均重定向到 /api/articles/drafts/* 端点。
 * 
 * 新架构下审核流程：
 *   1. 用户提交草稿（submitNewArticle / submitArticleEdit）
 *   2. 管理员查看待审核草稿（getPendingDrafts）
 *   3. 管理员审核草稿（reviewDraft: {action: 'approve'|'reject', comment}）
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */

import { api } from './index'
import type { ArticleDraft, ReviewDraftRequest, PageResponse } from './draft'

// ─── 类型定义（保留向下兼容） ──────────────────────────────────────────────────

export interface ArticleReview {
  /** 草稿ID（对应原 reviewId） */
  reviewId: number
  /** 文章ID */
  articleId?: number
  /** 标题 */
  title: string
  /** 作者 */
  author: string
  /** 审核状态 */
  status: 'pending' | 'approved' | 'rejected' | string
  /** 拒绝原因 */
  rejectReason?: string
  /** 创建时间 */
  createdAt?: string
  /** 提交时间 */
  submittedAt?: string
}

export interface ReviewStatistics {
  totalReviews: number
  pendingCount: number
  approvedCount: number
  rejectedCount: number
}

export interface CreateReviewRequest {
  /** 文章标题 */
  title: string
  /** 文章内容 */
  content: string
  /** 文章分类 */
  category?: string
  /** 关联文章ID（编辑时提供） */
  articleId?: number
}

export interface AssignReviewerRequest {
  reviewerId: number
}

export interface ProcessReviewRequest {
  action: 'approve' | 'reject'
  comment?: string
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResponse2<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
}

// ─── 适配工具 ──────────────────────────────────────────────────────────────────

/** 将 ArticleDraft 转为向下兼容的 ArticleReview 格式 */
function draftToReview(draft: ArticleDraft): ArticleReview {
  return {
    reviewId: draft.draftId,
    articleId: draft.articleId ?? undefined,
    title: draft.title,
    author: draft.author,
    status: draft.status === 'pending' ? 'pending'
          : draft.status === 'approved' ? 'approved'
          : draft.status === 'rejected' ? 'rejected'
          : draft.status,
    rejectReason: draft.rejectReason,
    createdAt: draft.createdAt,
    submittedAt: draft.submittedAt,
  }
}

// ─── ReviewApiService ──────────────────────────────────────────────────────────

export class ReviewApiService {
  /**
   * 创建审核申请（提交新草稿）
   * POST /api/articles/drafts
   */
  static async createReview(request: CreateReviewRequest): Promise<ApiResponse<ArticleReview>> {
    const response = await api.post<ApiResponse<ArticleDraft>>(
      '/api/articles/drafts',
      {
        title: request.title,
        content: request.content,
        category: request.category,
        article_id: request.articleId ?? null,
      }
    )
    return {
      code: response.data.code,
      message: response.data.message,
      data: draftToReview(response.data.data),
    }
  }

  /**
   * 分配审核员（暂不支持，后端无此端点，返回成功占位）
   */
  static async assignReviewer(
    _reviewId: number,
    _request: AssignReviewerRequest
  ): Promise<ApiResponse> {
    console.warn('[reviewApi] assignReviewer: 后端不支持此操作，已忽略')
    return { code: 200, message: '操作已忽略（后端不支持分配审核员）', data: null }
  }

  /**
   * 处理审核
   * POST /api/articles/drafts/{draftId}/review
   */
  static async processReview(
    reviewId: number,
    request: ProcessReviewRequest
  ): Promise<ApiResponse<ArticleReview>> {
    const response = await api.post<ApiResponse<ArticleDraft>>(
      `/api/articles/drafts/${reviewId}/review`,
      { action: request.action, comment: request.comment ?? '' }
    )
    return {
      code: response.data.code,
      message: response.data.message,
      data: draftToReview(response.data.data),
    }
  }

  /**
   * 获取审核详情
   * GET /api/articles/drafts/{draftId}
   */
  static async getReviewDetail(reviewId: number): Promise<ApiResponse<ArticleReview>> {
    const response = await api.get<ApiResponse<ArticleDraft>>(
      `/api/articles/drafts/${reviewId}`
    )
    return {
      code: response.data.code,
      message: response.data.message,
      data: draftToReview(response.data.data),
    }
  }

  /**
   * 获取用户待审核列表（返回当前用户的全部草稿）
   * GET /api/articles/drafts/my
   */
  static async getPendingReviews(
    _reviewerId: number,
    page = 0,
    size = 10
  ): Promise<ApiResponse<PageResponse2<ArticleReview>>> {
    const response = await api.get<ApiResponse<ArticleDraft[]>>(
      '/api/articles/drafts/my'
    )
    const drafts = (response.data.data || []).filter(d => d.status === 'pending')
    const start = page * size
    const paged: PageResponse2<ArticleReview> = {
      content: drafts.slice(start, start + size).map(draftToReview),
      totalElements: drafts.length,
      totalPages: Math.ceil(drafts.length / size) || 1,
      number: page,
      size,
      first: page === 0,
      last: start + size >= drafts.length,
    }
    return { code: 200, message: 'ok', data: paged }
  }

  /**
   * 获取所有待审核列表（管理员）
   * GET /api/articles/drafts/pending
   */
  static async getAllPendingReviews(
    page = 0,
    size = 10
  ): Promise<ApiResponse<PageResponse2<ArticleReview>>> {
    const response = await api.get<ApiResponse<ArticleDraft[]>>(
      '/api/articles/drafts/pending'
    )
    const drafts = response.data.data || []
    const start = page * size
    const paged: PageResponse2<ArticleReview> = {
      content: drafts.slice(start, start + size).map(draftToReview),
      totalElements: drafts.length,
      totalPages: Math.ceil(drafts.length / size) || 1,
      number: page,
      size,
      first: page === 0,
      last: start + size >= drafts.length,
    }
    return { code: 200, message: 'ok', data: paged }
  }

  /**
   * 获取审核统计（从草稿数据汇总）
   */
  static async getReviewStatistics(
    _reviewerId: number,
    _days = 30
  ): Promise<ApiResponse<ReviewStatistics>> {
    const [myRes, pendingRes] = await Promise.all([
      api.get<ApiResponse<ArticleDraft[]>>('/api/articles/drafts/my'),
      api.get<ApiResponse<ArticleDraft[]>>('/api/articles/drafts/pending').catch(() => ({ data: { data: [] } })),
    ])
    const myDrafts = myRes.data.data || []
    const allPending = (pendingRes as any).data.data || []
    return {
      code: 200,
      message: 'ok',
      data: {
        totalReviews: myDrafts.length,
        pendingCount: allPending.length,
        approvedCount: myDrafts.filter((d: ArticleDraft) => d.status === 'approved').length,
        rejectedCount: myDrafts.filter((d: ArticleDraft) => d.status === 'rejected').length,
      },
    }
  }

  /**
   * 批量操作审核
   */
  static async batchProcessReviews(
    reviewIds: number[],
    action: 'approve' | 'reject' | 'cancel',
    reason?: string
  ): Promise<ApiResponse> {
    if (action === 'cancel') {
      console.warn('[reviewApi] batchProcessReviews cancel: 后端不支持取消操作')
      return { code: 200, message: '暂不支持批量取消', data: null }
    }
    const results = await Promise.allSettled(
      reviewIds.map(id =>
        api.post(`/api/articles/drafts/${id}/review`, { action, comment: reason ?? '' })
      )
    )
    const failed = results.filter(r => r.status === 'rejected').length
    return {
      code: failed === 0 ? 200 : 207,
      message: `批量处理完成，成功 ${reviewIds.length - failed}，失败 ${failed}`,
      data: null,
    }
  }

  /** 搜索：降级为查询全部草稿 */
  static async searchReviews(params: {
    keyword?: string
    status?: string
    page?: number
    size?: number
    [key: string]: any
  }): Promise<ApiResponse<PageResponse2<ArticleReview>>> {
    const url = params.status
      ? `/api/articles/drafts/all?status=${encodeURIComponent(params.status)}`
      : '/api/articles/drafts/all'
    const response = await api.get<ApiResponse<ArticleDraft[]>>(url)
    let drafts = response.data.data || []
    if (params.keyword) {
      const kw = params.keyword.toLowerCase()
      drafts = drafts.filter(d =>
        d.title?.toLowerCase().includes(kw) || d.author?.toLowerCase().includes(kw)
      )
    }
    const page = params.page ?? 0
    const size = params.size ?? 10
    const start = page * size
    return {
      code: 200,
      message: 'ok',
      data: {
        content: drafts.slice(start, start + size).map(draftToReview),
        totalElements: drafts.length,
        totalPages: Math.ceil(drafts.length / size) || 1,
        number: page,
        size,
        first: page === 0,
        last: start + size >= drafts.length,
      },
    }
  }

  /** 导出：不支持，返回空 Blob */
  static async exportReviews(_params: any): Promise<Blob> {
    console.warn('[reviewApi] exportReviews: 后端不支持导出功能')
    return new Blob([''], { type: 'application/octet-stream' })
  }

  /** 获取审核配置 */
  static async getReviewConfig(): Promise<ApiResponse> {
    return { code: 200, message: 'ok', data: {} }
  }

  /** 更新审核配置 */
  static async updateReviewConfig(_config: any): Promise<ApiResponse> {
    console.warn('[reviewApi] updateReviewConfig: 后端不支持此功能')
    return { code: 200, message: '暂不支持审核配置', data: null }
  }
}

// ─── reviewApi 快捷方式 ──────────────────────────────────────────────────────

export const reviewApi = {
  create: ReviewApiService.createReview.bind(ReviewApiService),
  assign: ReviewApiService.assignReviewer.bind(ReviewApiService),
  process: ReviewApiService.processReview.bind(ReviewApiService),
  getDetail: ReviewApiService.getReviewDetail.bind(ReviewApiService),
  getPending: ReviewApiService.getPendingReviews.bind(ReviewApiService),
  getAllPending: ReviewApiService.getAllPendingReviews.bind(ReviewApiService),
  getStatistics: ReviewApiService.getReviewStatistics.bind(ReviewApiService),
  search: ReviewApiService.searchReviews.bind(ReviewApiService),
  batchProcess: ReviewApiService.batchProcessReviews.bind(ReviewApiService),
  export: ReviewApiService.exportReviews.bind(ReviewApiService),
  getConfig: ReviewApiService.getReviewConfig.bind(ReviewApiService),
  updateConfig: ReviewApiService.updateReviewConfig.bind(ReviewApiService),
}

export default reviewApi
