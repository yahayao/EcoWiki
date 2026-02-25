/**
 * 评论管理API模块
 * 
 * 功能：
 * - 提供评论的增删改查操作
 * - 支持评论点赞和回复功能
 * - 实现评论审核和举报处理
 * - 提供评论统计和分析功能
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */

import { api } from './index'

/**
 * 评论数据接口
 */
export interface Comment {
  /** 评论ID */
  id: number
  /** 文章ID */
  articleId: number
  /** 评论作者 */
  author: string
  /** 评论内容 */
  content: string
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt?: string
  /** 点赞数 */
  likes: number
  /** 当前用户是否已点赞 */
  isLiked: boolean
  /** 回复列表 */
  replies?: Reply[]
  /** 父评论ID（如果是回复） */
  parentId?: number
  /** 用户头像URL */
  userAvatar?: string
  /** 用户ID */
  userId?: number
}

/**
 * 回复数据接口
 */
export interface Reply {
  /** 回复ID */
  id: number
  /** 评论ID */
  commentId: number
  /** 回复作者 */
  author: string
  /** 回复内容 */
  content: string
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt?: string
  /** 点赞数 */
  likes: number
  /** 当前用户是否已点赞 */
  isLiked: boolean
  /** 用户头像URL */
  userAvatar?: string
  /** 用户ID */
  userId?: number
  /** 被回复的用户 */
  replyToUser?: string
}

/**
 * 创建评论请求
 */
export interface CreateCommentRequest {
  /** 文章ID */
  articleId: number
  /** 评论内容 */
  content: string
  /** 父评论ID（回复时使用） */
  parentId?: number
}

/**
 * 更新评论请求
 */
export interface UpdateCommentRequest {
  /** 评论内容 */
  content: string
}

/**
 * 评论查询参数
 */
export interface CommentQueryParams {
  /** 页码 */
  page?: number
  /** 每页大小 */
  size?: number
  /** 排序方式：newest-最新，oldest-最早，hot-最热 */
  sort?: 'newest' | 'oldest' | 'hot'
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
  hasMore: boolean
}

/**
 * API响应格式
 */
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  success: boolean
}

/**
 * 点赞结果接口
 * 后端返回 { liked, likes }
 */
export interface LikeResult {
  liked: boolean
  likes: number
  likeCount?: number   // 兼容旧字段
}

/**
 * 评论统计接口
 */
export interface CommentStats {
  total: number
  totalComments?: number   // 兼容字段
  topLevelComments?: number
}

// ── 将后端 snake_case 返回映射为前端 Comment 格式 ──────────────────────────────
function _normalizeComment(raw: any): Comment {
  return {
    id:          raw.commentId ?? raw.comment_id ?? raw.id,
    articleId:   raw.articleId ?? raw.article_id,
    author:      raw.author,
    content:     raw.content,
    createdAt:   raw.createdAt ?? raw.created_at,
    updatedAt:   raw.updatedAt ?? raw.updated_at,
    likes:       raw.likes ?? 0,
    isLiked:     raw.likedByCurrent ?? raw.liked_by_current ?? raw.isLiked ?? false,
    parentId:    raw.parentId ?? raw.parent_id,
    userAvatar:  raw.avatarUrl ?? raw.avatar_url ?? raw.userAvatar,
    userId:      raw.authorId ?? raw.author_id ?? raw.userId,
    replies:     (raw.replies ?? []).map(_normalizeComment),
  }
}

/**
 * 评论API类
 */
export const commentApi = {
  async getComments(articleId: number, params: CommentQueryParams = {}): Promise<PageResponse<Comment>> {
    const { page = 0, size = 20, sort = 'newest' } = params
    const response = await api.get<ApiResponse<PageResponse<Comment>>>(
      `/api/comments/article/${articleId}?page=${page}&size=${size}&sort=${sort}`
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取评论失败')
    }
    const page_data = response.data.data as any
    return {
      ...page_data,
      content: (page_data.content ?? []).map(_normalizeComment),
    }
  },

  /**
   * 创建评论
   * @param request 创建评论请求
   * @returns 创建的评论
   */
  async createComment(request: CreateCommentRequest): Promise<Comment> {
    const response = await api.post<ApiResponse<Comment>>('/api/comments', request)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '发表评论失败')
    }
    return _normalizeComment(response.data.data)
  },

  /**
   * 删除评论
   * @param commentId 评论ID
   * @returns 删除结果
   */
  async deleteComment(commentId: number): Promise<void> {
    const response = await api.delete<ApiResponse<string>>(`/api/comments/${commentId}`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '删除评论失败')
    }
  },

  /**
   * 点赞/取消点赞评论
   * @param commentId 评论ID
   * @returns 更新后的点赞状态
   */
  async toggleCommentLike(commentId: number): Promise<LikeResult> {
    const response = await api.post<ApiResponse<LikeResult>>(
      `/api/comments/${commentId}/like`
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '点赞操作失败')
    }
    return response.data.data
  },

  /**
   * 创建回复
   * @param commentId 评论ID
   * @param content 回复内容
   * @returns 创建的回复
   */
  async createReply(commentId: number, content: string): Promise<Reply> {
    const response = await api.post<ApiResponse<Reply>>(`/api/comments/${commentId}/reply`, {
      content
    })
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '发表回复失败')
    }
    return response.data.data
  },

  /**
   * 删除回复
   * @param replyId 回复ID
   * @returns 删除结果
   */
  async deleteReply(replyId: number): Promise<void> {
    const response = await api.delete<ApiResponse<string>>(`/api/comments/${replyId}`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '删除回复失败')
    }
  },

  /**
   * 获取评论统计信息
   * @param articleId 文章ID
   * @returns 评论统计
   */
  async getCommentStats(articleId: number): Promise<CommentStats> {
    const response = await api.get<ApiResponse<CommentStats>>(
      `/api/comments/article/${articleId}/stats`
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取评论统计失败')
    }
    const d = response.data.data as any
    return {
      total: d.total ?? d.totalComments ?? 0,
      totalComments: d.total ?? d.totalComments ?? 0,
      topLevelComments: d.topLevelComments ?? d.total ?? 0,
    }
  }
}
