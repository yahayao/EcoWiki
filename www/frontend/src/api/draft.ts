/**
 * 文章草稿API模块
 * 
 * 功能：
 * - 提供文章草稿的提交、审核和查询API接口
 * - 实现完整的文章审核流程管理
 * - 支持草稿状态跟踪和历史记录
 * - 提供管理员审核功能的API封装
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */

import { api } from './index'

/**
 * 文章草稿数据接口（与后端 DraftOut 对齐，字段经过 snake_case→camelCase 转换）
 */
export interface ArticleDraft {
  /** 草稿ID */
  draftId: number
  /** 关联的文章ID（编辑现有文章时有值，新建文章时为null） */
  articleId?: number | null
  /** 编辑者（作者）用户名 */
  author: string
  /** 编辑者用户ID */
  authorId: number
  /** 文章标题 */
  title: string
  /** 文章内容 */
  content: string
  /** 文章分类 */
  category?: string
  /** 草稿状态：draft | pending | approved | rejected */
  status: 'draft' | 'pending' | 'approved' | 'rejected' | string
  /** 拒绝原因（被拒绝时） */
  rejectReason?: string
  /** 创建时间 */
  createdAt?: string
  /** 更新时间 */
  updatedAt?: string
  /** 提交时间 */
  submittedAt?: string
  /** 审核时间 */
  reviewedAt?: string
  // ── 向下兼容别名 ─────────────────────────────────────
  /** @deprecated 使用 authorId */
  editorUserId: number
  /** @deprecated 使用 author */
  editorUserName: string
  /** 编辑者头像 */
  editorUserAvatar?: string
  /** 审核者用户ID */
  reviewerUserId?: number
  /** 审核者用户名 */
  reviewerUserName?: string
  /** @deprecated 使用 status */
  reviewStatus: string
  /** @deprecated 使用 rejectReason */
  reviewNotes?: string
}

/**
 * 审核草稿请求（与后端 ReviewRequest 对齐）
 */
export interface ReviewDraftRequest {
  /** 审核操作：approve | reject */
  action: 'approve' | 'reject'
  /** 审核备注 / 拒绝原因 */
  comment?: string
}

/**
 * 分页响应
 */
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
}

/**
 * API响应格式
 */
interface ApiResponse<T> {
  code: number
  message: string
  data: T
  success?: boolean
}

/**
 * 草稿提交结果（向下兼容，实际返回 ArticleDraft）
 */
export interface DraftSubmissionResult {
  /** 草稿ID */
  draftId: number
  /** 状态 */
  status: string
  /** 消息 */
  message?: string
}

// ─── 内部工具 ──────────────────────────────────────────────────────────────────

/** 将后端返回的草稿列表包装成 PageResponse 格式（后端不分页，统一适配） */
function wrapListAsPage<T>(list: T[], page: number, size: number): PageResponse<T> {
  const start = page * size
  return {
    content: list.slice(start, start + size),
    totalElements: list.length,
    totalPages: Math.ceil(list.length / size) || 1,
    number: page,
    size,
    first: page === 0,
    last: start + size >= list.length,
  }
}

/** 规范化草稿字段，补充向下兼容别名 */
function normalizeDraft(d: any): ArticleDraft {
  const normalizedStatus = (d.status || '').toString().toUpperCase()
  return {
    ...d,
    editorUserId: d.authorId,
    editorUserName: d.author,
    reviewStatus: normalizedStatus,
    reviewNotes: d.rejectReason,
    // 后端 reviewer_id → camelCase → reviewerId，映射到向下兼容字段 reviewerUserId
    reviewerUserId: d.reviewerUserId ?? d.reviewerId ?? undefined,
    reviewedAt: d.reviewedAt ?? undefined,
  }
}

// ─── API对象 ───────────────────────────────────────────────────────────────────

/**
 * 文章草稿API
 */
export const draftApi = {
  /**
   * 提交新文章草稿并立即提交审核（新建文章）
   * 步骤1: POST /api/articles/drafts → 保存草稿（status: draft）
   * 步骤2: POST /api/articles/drafts/{draftId}/submit → 提交审核队列（status: pending）
   */
  async submitNewArticle(request: {
    title: string
    author?: string
    content: string
    category: string
    tags?: string
    articleId?: number | null
  }): Promise<ArticleDraft> {
    // 步骤1：保存草稿
    const createResp = await api.post<ApiResponse<ArticleDraft>>(
      '/api/articles/drafts',
      {
        title: request.title,
        content: request.content,
        category: request.category,
        article_id: request.articleId ?? null,
      }
    )
    if (createResp.data.code !== 200) {
      throw new Error(createResp.data.message || '保存草稿失败')
    }
    const draft = normalizeDraft(createResp.data.data)

    // 步骤2：提交审核队列
    const submitResp = await api.post<ApiResponse<ArticleDraft>>(
      `/api/articles/drafts/${draft.draftId}/submit`
    )
    if (submitResp.data.code !== 200) {
      throw new Error(submitResp.data.message || '提交审核失败')
    }
    return normalizeDraft(submitResp.data.data)
  },

  /**
   * 提交文章编辑草稿并立即提交审核（修改现有文章）
   * 步骤1: POST /api/articles/{articleId}/drafts → 保存草稿（status: draft）
   * 步骤2: POST /api/articles/drafts/{draftId}/submit → 提交审核队列（status: pending）
   */
  async submitArticleEdit(articleId: number, request: {
    title: string
    content: string
    category: string
    tags?: string
  }): Promise<ArticleDraft> {
    // 步骤1：保存草稿
    const createResp = await api.post<ApiResponse<ArticleDraft>>(
      `/api/articles/${articleId}/drafts`,
      {
        title: request.title,
        content: request.content,
        category: request.category,
      }
    )
    if (createResp.data.code !== 200) {
      throw new Error(createResp.data.message || '保存草稿失败')
    }
    const draft = normalizeDraft(createResp.data.data)

    // 步骤2：提交审核队列
    const submitResp = await api.post<ApiResponse<ArticleDraft>>(
      `/api/articles/drafts/${draft.draftId}/submit`
    )
    if (submitResp.data.code !== 200) {
      throw new Error(submitResp.data.message || '提交审核失败')
    }
    return normalizeDraft(submitResp.data.data)
  },

  /**
   * 仅将草稿提交到审核队列（不含创建草稿，单独调用）
   * POST /api/articles/drafts/{draftId}/submit
   */
  async submitDraftForReview(draftId: number): Promise<ArticleDraft> {
    const response = await api.post<ApiResponse<ArticleDraft>>(
      `/api/articles/drafts/${draftId}/submit`
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '提交审核失败')
    }
    return normalizeDraft(response.data.data)
  },

  /**
   * 审核草稿（仅管理员）
   * POST /api/articles/drafts/{draftId}/review
   */
  async reviewDraft(draftId: number, request: ReviewDraftRequest): Promise<ArticleDraft> {
    const response = await api.post<ApiResponse<ArticleDraft>>(
      `/api/articles/drafts/${draftId}/review`,
      { action: request.action, comment: request.comment ?? '' }
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '审核草稿失败')
    }
    return normalizeDraft(response.data.data)
  },

  /**
   * 获取待审核草稿列表（仅管理员）
   * GET /api/articles/drafts/pending
   */
  async getPendingDrafts(page = 0, size = 10): Promise<PageResponse<ArticleDraft>> {
    const response = await api.get<ApiResponse<ArticleDraft[]>>(
      '/api/articles/drafts/pending'
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取待审核草稿列表失败')
    }
    const list = (response.data.data || []).map(normalizeDraft)
    return wrapListAsPage(list, page, size)
  },

  /**
   * 获取用户的草稿列表
   * GET /api/articles/drafts/my
   */
  async getMyDrafts(page = 0, size = 10): Promise<PageResponse<ArticleDraft>> {
    const response = await api.get<ApiResponse<ArticleDraft[]>>(
      '/api/articles/drafts/my'
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取用户草稿列表失败')
    }
    const list = (response.data.data || []).map(normalizeDraft)
    return wrapListAsPage(list, page, size)
  },

  /**
   * 根据状态获取草稿列表（管理员）
   * GET /api/articles/drafts/all?status=
   */
  async getDraftsByStatus(
    status: 'pending' | 'approved' | 'rejected' | 'draft' | string,
    page = 0,
    size = 10
  ): Promise<PageResponse<ArticleDraft>> {
    const response = await api.get<ApiResponse<ArticleDraft[]>>(
      `/api/articles/drafts/all?status=${encodeURIComponent(status)}`
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取草稿列表失败')
    }
    const list = (response.data.data || []).map(normalizeDraft)
    return wrapListAsPage(list, page, size)
  },

  /**
   * 获取草稿详情
   * GET /api/articles/drafts/{draftId}
   */
  async getDraftById(draftId: number): Promise<ArticleDraft> {
    const response = await api.get<ApiResponse<ArticleDraft>>(
      `/api/articles/drafts/${draftId}`
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取草稿详情失败')
    }
    return normalizeDraft(response.data.data)
  },

  /**
   * 删除草稿
   * DELETE /api/articles/drafts/{draftId}
   */
  async deleteDraft(draftId: number): Promise<void> {
    const response = await api.delete<ApiResponse<string>>(
      `/api/articles/drafts/${draftId}`
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '删除草稿失败')
    }
  },

  /**
   * 仅保存草稿（不提交审核）
   * 新建文章： POST /api/articles/drafts  （status=draft）
   * 编辑文章： POST /api/articles/{articleId}/drafts  （status=draft）
   */
  async saveDraftOnly(request: {
    title: string
    content: string
    category: string
    articleId?: number | null
  }): Promise<ArticleDraft> {
    const url = request.articleId
      ? `/api/articles/${request.articleId}/drafts`
      : '/api/articles/drafts'
    const body = request.articleId
      ? { content: request.content, category: request.category }
      : { title: request.title, content: request.content, category: request.category }
    const response = await api.post<ApiResponse<ArticleDraft>>(url, body)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '保存草稿失败')
    }
    return normalizeDraft(response.data.data)
  },

  /**
   * 更新被拒稿的草稿内容并重新提交审核
   * PUT /api/articles/drafts/{draftId}
   * 仅限 rejected 或 draft 状态的草稿，pending 状态不可修改
   */
  async updateDraft(draftId: number, data: { content?: string; category?: string }): Promise<ArticleDraft> {
    const response = await api.put<ApiResponse<ArticleDraft>>(
      `/api/articles/drafts/${draftId}`,
      { content: data.content, category: data.category }
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '更新草稿失败')
    }
    return normalizeDraft(response.data.data)
  },

  /**
   * 获取所有草稿列表（管理员）
   * GET /api/articles/drafts/all
   */
  async getAllDrafts(page = 0, size = 10): Promise<PageResponse<ArticleDraft>> {
    const response = await api.get<ApiResponse<ArticleDraft[]>>(
      '/api/articles/drafts/all'
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取所有草稿列表失败')
    }
    const list = (response.data.data || []).map(normalizeDraft)
    return wrapListAsPage(list, page, size)
  },
}
