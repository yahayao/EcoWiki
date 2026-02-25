/**
 * 标签管理API模块
 * 
 * 功能：
 * - 提供标签的增删改查操作
 * - 支持标签分类和使用统计
 * - 实现热门标签查询功能
 * - 管理标签关联文章关系
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */

import { api } from './index'

/**
 * 统一API响应格式接口（与后端 ApiResponse 对齐）
 */
interface ApiResponse<T> {
  /** 状态码（200=成功） */
  code: number
  /** 响应消息 */
  message: string
  /** 响应数据 */
  data: T
  /** 时间戳 */
  timestamp?: number
}

/**
 * 标签数据传输对象（与后端 TagOut 对齐，经过 snake_case→camelCase 转换）
 * 
 * 后端字段：tag_id, tag_name, color
 * 转换后：tagId, tagName, color
 */
export interface TagDto {
  /** 标签ID */
  tagId: number
  /** 标签名称 */
  tagName: string
  /** 标签颜色（可选） */
  color?: string
  /** 创建时间（ISO格式字符串，后端可能不返回） */
  createdAt?: string
  // ── 向下兼容字段 ──────────────────────────────────────
  /** @deprecated 后端无此字段，始终为 undefined */
  description?: string
  /** @deprecated 后端无此字段 */
  createdTime?: string
  /** @deprecated 后端无此字段 */
  articleCount?: number
}

/**
 * 标签统计数据传输对象
 */
export interface TagStatisticsDto {
  /** 标签总数 */
  totalTags: number
  /** 未使用的标签数量 */
  unusedTagCount: number
  /** 标签使用统计列表 */
  usageStatistics: Array<[string, number]>
}

/**
 * 标签API服务类
 */
export class TagApi {
  /**
   * 创建新标签
   * POST /api/tags
   */
  static async createTag(tagName: string, _description?: string): Promise<ApiResponse<TagDto>> {
    const response = await api.post<ApiResponse<TagDto>>('/api/tags', { tag_name: tagName })
    return response.data
  }

  /**
   * 根据ID获取标签
   * GET /api/tags/{tagId}
   */
  static async getTagById(tagId: number): Promise<ApiResponse<TagDto>> {
    const response = await api.get<ApiResponse<TagDto>>(`/api/tags/${tagId}`)
    return response.data
  }

  /**
   * 根据名称获取标签
   * GET /api/tags/name/{tagName}
   */
  static async getTagByName(tagName: string): Promise<ApiResponse<TagDto>> {
    const response = await api.get<ApiResponse<TagDto>>(`/api/tags/name/${encodeURIComponent(tagName)}`)
    return response.data
  }

  /**
   * 更新标签信息
   * PUT /api/tags/{tagId}
   */
  static async updateTag(tagId: number, tagName: string, _description?: string): Promise<ApiResponse<TagDto>> {
    const response = await api.put<ApiResponse<TagDto>>(`/api/tags/${tagId}`, { tag_name: tagName })
    return response.data
  }

  /**
   * 删除标签
   * DELETE /api/tags/{tagId}
   */
  static async deleteTag(tagId: number): Promise<ApiResponse<string>> {
    const response = await api.delete<ApiResponse<string>>(`/api/tags/${tagId}`)
    return response.data
  }

  /**
   * 获取所有标签
   * GET /api/tags
   */
  static async getAllTags(): Promise<ApiResponse<TagDto[]>> {
    const response = await api.get<ApiResponse<TagDto[]>>('/api/tags')
    return response.data
  }

  /**
   * 搜索标签
   * GET /api/tags/search?keyword=&page=&size=
   */
  static async searchTags(
    keyword: string,
    page: number = 0,
    size: number = 20
  ): Promise<ApiResponse<TagDto[]>> {
    const response = await api.get<ApiResponse<TagDto[]>>('/api/tags/search', {
      params: { keyword, page, size }
    })
    return response.data
  }

  /**
   * 获取热门标签
   * GET /api/tags/popular?limit=
   */
  static async getPopularTags(limit: number = 10): Promise<ApiResponse<TagDto[]>> {
    const response = await api.get<ApiResponse<TagDto[]>>('/api/tags/popular', {
      params: { limit }
    })
    return response.data
  }

  /**
   * 获取最近创建的标签
   * GET /api/tags/recent?limit=
   */
  static async getRecentTags(limit: number = 10): Promise<ApiResponse<TagDto[]>> {
    const response = await api.get<ApiResponse<TagDto[]>>('/api/tags/recent', {
      params: { limit }
    })
    return response.data
  }

  /**
   * 获取标签统计信息
   * GET /api/tags/statistics
   */
  static async getTagStatistics(): Promise<ApiResponse<TagStatisticsDto>> {
    const response = await api.get<ApiResponse<TagStatisticsDto>>('/api/tags/statistics')
    return response.data
  }

  /**
   * 获取推荐标签（用文章的全部标签代替，后端暂无专用接口）
   * 降级：返回所有标签中前 limit 个
   */
  static async getRecommendedTags(_articleId: number, limit: number = 5): Promise<ApiResponse<TagDto[]>> {
    const response = await api.get<ApiResponse<TagDto[]>>('/api/tags/popular', {
      params: { limit }
    })
    return response.data
  }

  /**
   * 删除未使用的标签
   * DELETE /api/tags/unused
   */
  static async deleteUnusedTags(): Promise<ApiResponse<number>> {
    const response = await api.delete<ApiResponse<number>>('/api/tags/unused')
    return response.data
  }

  /**
   * 将逗号分隔的标签字符串转换为标签数组
   */
  static parseTagsString(tagsString: string): string[] {
    if (!tagsString || tagsString.trim() === '') return []
    return tagsString.split(',').map(t => t.trim()).filter(t => t.length > 0)
  }

  /**
   * 将标签数组转换为逗号分隔的字符串
   */
  static tagsToString(tags: string[]): string {
    return tags.join(',')
  }
}
