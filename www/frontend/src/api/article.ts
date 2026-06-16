/**
 * 文章相关 API 接口服务模块
 *
 * 修复：v2 使用共享 api 实例（./index），移除独立 axios 实例
 *
 * @author EcoWiki开发团队
 * @version 2.0.0
 */

import { api } from './index'

// ── 标签标准化（article 特有数据处理） ─────────────────────────────────────
function normalizeTags(data: any): void {
  if (!data) return
  const convert = (item: any) => {
    if (item && Array.isArray(item.tags))
      item.tags = item.tags.map((t: any) => t.tagName || t.tag_name || '').filter(Boolean).join(',')
  }
  if (data.articleId !== undefined || data.article_id !== undefined) { convert(data); return }
  if (data.content && Array.isArray(data.content)) { data.content.forEach(convert); return }
  if (Array.isArray(data)) data.forEach(convert)
}

// ── 类型定义 ──────────────────────────────────────────────────────────────────

export interface Article {
  articleId: number
  title: string
  author: string
  authorAvatar?: string
  content: string
  publishDate: string
  category: string
  views: number
  likes: number
  tags: string
  comments: number
  updateTime: string
  contributors?: Array<{ username: string; displayName: string; avatarUrl: string; editCount: number; latestEdit: string }>
  contributorsError?: string | null
}

export interface ArticleCreateRequest {
  title: string; author: string; content: string; category: string; tags?: string
}

export interface ArticleUpdateRequest {
  title: string; content: string; category: string; tags?: string
}

export interface ApiResponse<T> {
  code: number; data: T; message: string; timestamp: number
}

export interface PageResponse<T> {
  content: T[]; totalElements: number; totalPages: number; size: number; number: number; first: boolean; last: boolean
}

export interface ArticleStatistics {
  totalArticles: number; totalViews: number; totalLikes: number
}

export interface ArticleVersion {
  versionId: number; articleId: number; versionNumber: number; title: string; content: string; author: string; createdAt: string; summary: string; diff: string
}

export interface CreateVersionRequest {
  title: string; content: string; author: string; summary: string
}

// ── API 方法 ──────────────────────────────────────────────────────────────────

export const articleApi = {
  getArticles(page = 0, size = 10, category?: string, sortBy = 'latest') {
    const params: any = { page, size, sortBy }
    if (category) params.category = category
    return api.get<ApiResponse<PageResponse<Article>>>('/api/articles', { params })
  },

  getArticleByTitle(title: string) {
    return api.get<ApiResponse<Article>>(`/api/articles/${encodeURIComponent(title)}`)
  },

  createArticle(data: ArticleCreateRequest) {
    return api.post<ApiResponse<Article>>('/api/articles', data)
  },

  updateArticle(title: string, data: ArticleUpdateRequest) {
    return api.put<ApiResponse<Article>>(`/api/articles/${encodeURIComponent(title)}`, data)
  },

  deleteArticle(articleId: number) {
    return api.delete<ApiResponse<void>>(`/api/articles/${articleId}`)
  },

  searchArticles(keyword: string, page = 0, size = 10) {
    return api.get<ApiResponse<PageResponse<Article>>>('/api/articles/search', { params: { keyword, page, size } })
  },

  getPopularArticles(limit = 10) {
    return api.get<ApiResponse<Article[]>>('/api/articles/popular', { params: { limit } })
  },

  getLatestArticles(limit = 10) {
    return api.get<ApiResponse<Article[]>>('/api/articles/latest', { params: { limit } })
  },

  likeArticle(articleId: number) {
    return api.post<ApiResponse<any>>(`/api/articles/${articleId}/like`)
  },

  unlikeArticle(articleId: number) {
    return api.delete<ApiResponse<any>>(`/api/articles/${articleId}/like`)
  },

  getLikeStatus(articleId: number) {
    return api.get<ApiResponse<{ liked: boolean }>>(`/api/articles/${articleId}/like/status`)
  },

  favoriteArticle(articleId: number) {
    return api.post<ApiResponse<any>>(`/api/articles/${articleId}/favorite`)
  },

  unfavoriteArticle(articleId: number) {
    return api.delete<ApiResponse<any>>(`/api/articles/${articleId}/favorite`)
  },

  getFavoriteStatus(articleId: number) {
    return api.get<ApiResponse<{ favorited: boolean }>>(`/api/articles/${articleId}/favorite/status`)
  },

  getVersionHistory(articleId: number) {
    return api.get<ApiResponse<ArticleVersion[]>>(`/api/articles/${articleId}/versions`)
  },

  getVersionDetail(articleId: number, versionNumber: number) {
    return api.get<ApiResponse<ArticleVersion>>(`/api/articles/${articleId}/versions/${versionNumber}`)
  },

  compareVersions(articleId: number, v1: number, v2: number) {
    return api.get<ApiResponse<{ left: ArticleVersion; right: ArticleVersion }>>(`/api/articles/${articleId}/versions/compare?v1=${v1}&v2=${v2}`)
  },

  restoreToVersion(articleId: number, versionNumber: number, author: string) {
    return api.post<ApiResponse<ArticleVersion>>(`/api/articles/${articleId}/versions/${versionNumber}/restore`, { author })
  },

  getVersionStats(articleId: number) {
    return api.get<ApiResponse<{ totalVersions: number; contributors: number; lastVersion: number }>>(`/api/articles/${articleId}/versions/stats`)
  }
}

export default articleApi
export type { Article as default }
