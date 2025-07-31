/**
 * 文章草稿API模块
 * 
 * 提供文章草稿的提交、审核、查询等功能的API接口
 * 实现完整的文章审核流程管理
 */

import { api } from './index'

/**
 * 文章草稿数据接口
 */
export interface ArticleDraft {
  /** 草稿ID */
  draftId: number
  /** 关联的文章ID（编辑现有文章时有值，新建文章时为null） */
  articleId?: number
  /** 编辑者用户ID */
  editorUserId: number
  /** 文章标题 */
  title: string
  /** 文章内容 */
  content: string
  /** 文章分类 */
  category?: string
  /** 文章标签 */
  tags?: string
  /** 审核状态 */
  reviewStatus: 'PENDING' | 'APPROVED' | 'REJECTED'
  /** 提交时间 */
  submittedAt: string
  /** 审核时间 */
  reviewedAt?: string
  /** 审核者用户ID */
  reviewerUserId?: number
  /** 审核备注 */
  reviewNotes?: string
}

/**
 * 审核草稿请求
 */
export interface ReviewDraftRequest {
  /** 审核结果：true为通过，false为拒绝 */
  approved: boolean
  /** 审核备注 */
  reviewNotes?: string
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
 * 草稿提交结果
 */
export interface DraftSubmissionResult {
  /** 草稿ID */
  draftId: number
  /** 状态 */
  status: string
  /** 消息 */
  message: string
}

/**
 * 文章草稿API类
 */
export const draftApi = {
  /**
   * 提交新文章草稿
   * @param request 文章创建请求
   * @returns 提交结果
   */
  async submitNewArticle(request: {
    title: string
    author: string
    content: string
    category: string
    tags?: string
  }): Promise<DraftSubmissionResult> {
    const response = await api.post<ApiResponse<DraftSubmissionResult>>('/api/drafts/submit-new', request)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '提交新文章草稿失败')
    }
    return response.data.data
  },

  /**
   * 提交文章编辑草稿
   * @param articleId 文章ID
   * @param request 文章更新请求
   * @returns 提交结果
   */
  async submitArticleEdit(articleId: number, request: {
    title: string
    content: string
    category: string
    tags?: string
  }): Promise<DraftSubmissionResult> {
    const response = await api.post<ApiResponse<DraftSubmissionResult>>(
      `/api/drafts/submit-edit/${articleId}`, 
      request
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '提交文章编辑草稿失败')
    }
    return response.data.data
  },

  /**
   * 审核草稿（仅管理员）
   * @param draftId 草稿ID
   * @param request 审核请求
   * @returns 审核结果
   */
  async reviewDraft(draftId: number, request: ReviewDraftRequest): Promise<ArticleDraft> {
    const response = await api.put<ApiResponse<ArticleDraft>>(`/api/drafts/review/${draftId}`, request)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '审核草稿失败')
    }
    return response.data.data
  },

  /**
   * 获取待审核草稿列表（仅管理员）
   * @param page 页码
   * @param size 每页大小
   * @returns 待审核草稿列表
   */
  async getPendingDrafts(page = 0, size = 10): Promise<PageResponse<ArticleDraft>> {
    const response = await api.get<ApiResponse<PageResponse<ArticleDraft>>>(
      `/api/drafts/pending?page=${page}&size=${size}`
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取待审核草稿列表失败')
    }
    return response.data.data
  },

  /**
   * 获取用户的草稿列表
   * @param page 页码
   * @param size 每页大小
   * @returns 用户的草稿列表
   */
  async getMyDrafts(page = 0, size = 10): Promise<PageResponse<ArticleDraft>> {
    const response = await api.get<ApiResponse<PageResponse<ArticleDraft>>>(
      `/api/drafts/my-drafts?page=${page}&size=${size}`
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取用户草稿列表失败')
    }
    return response.data.data
  },

  /**
   * 根据状态获取草稿列表（仅管理员）
   * @param status 审核状态
   * @param page 页码
   * @param size 每页大小
   * @returns 指定状态的草稿列表
   */
  async getDraftsByStatus(
    status: 'PENDING' | 'APPROVED' | 'REJECTED', 
    page = 0, 
    size = 10
  ): Promise<PageResponse<ArticleDraft>> {
    const response = await api.get<ApiResponse<PageResponse<ArticleDraft>>>(
      `/api/drafts/by-status?status=${status}&page=${page}&size=${size}`
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取草稿列表失败')
    }
    return response.data.data
  },

  /**
   * 获取草稿详情
   * @param draftId 草稿ID
   * @returns 草稿详情
   */
  async getDraftById(draftId: number): Promise<ArticleDraft> {
    const response = await api.get<ApiResponse<ArticleDraft>>(`/api/drafts/${draftId}`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取草稿详情失败')
    }
    return response.data.data
  },

  /**
   * 删除草稿
   * @param draftId 草稿ID
   * @returns 删除结果
   */
  async deleteDraft(draftId: number): Promise<void> {
    const response = await api.delete<ApiResponse<void>>(`/api/drafts/${draftId}`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '删除草稿失败')
    }
  },

  /**
   * 获取所有草稿列表（仅管理员）
   * @param page 页码
   * @param size 每页大小
   * @returns 所有草稿列表
   */
  async getAllDrafts(page = 0, size = 10): Promise<PageResponse<ArticleDraft>> {
    const response = await api.get<ApiResponse<PageResponse<ArticleDraft>>>(
      `/api/drafts/all?page=${page}&size=${size}`
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取所有草稿列表失败')
    }
    return response.data.data
  }
}

// 导出类型和API
export type { ArticleDraft, ReviewDraftRequest, PageResponse, DraftSubmissionResult }
