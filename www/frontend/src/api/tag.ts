import { api } from './index'

/**
 * 统一API响应格式接口
 * 与后端ApiResponse类保持一致，确保前后端数据格式统一
 */
interface ApiResponse<T> {
  /** 操作是否成功 */
  success: boolean
  /** 响应消息 */
  message: string
  /** 响应数据 */
  data: T
  /** 错误代码（失败时） */
  errorCode?: string
}

/**
 * 标签数据传输对象
 * 用于前后端之间传递标签数据
 */
export interface TagDto {
  /** 标签ID */
  tagId: number
  /** 标签名称 */
  tagName: string
  /** 标签描述 */
  description?: string
  /** 创建时间（ISO格式字符串） */
  createdTime: string
  /** 使用此标签的文章数量 */
  articleCount: number
}

/**
 * 标签统计数据传输对象
 */
export interface TagStatisticsDto {
  /** 标签总数 */
  totalTags: number
  /** 未使用的标签数量 */
  unusedTagCount: number
  /** 标签使用统计列表，每个元素包含[标签名称, 使用次数] */
  usageStatistics: Array<[string, number]>
}

/**
 * 标签API服务类
 * 提供标签相关的HTTP请求方法
 */
export class TagApi {
  /**
   * 创建新标签
   * @param tagName 标签名称
   * @param description 标签描述（可选）
   * @returns Promise<ApiResponse<TagDto>>
   */
  static async createTag(tagName: string, description?: string): Promise<ApiResponse<TagDto>> {
    const params = new URLSearchParams()
    params.append('tagName', tagName)
    if (description) {
      params.append('description', description)
    }
    
    const response = await api.post('/tags', params)
    return response.data
  }

  /**
   * 根据ID获取标签
   * @param tagId 标签ID
   * @returns Promise<ApiResponse<TagDto>>
   */
  static async getTagById(tagId: number): Promise<ApiResponse<TagDto>> {
    const response = await api.get(`/tags/${tagId}`)
    return response.data
  }

  /**
   * 根据名称获取标签
   * @param tagName 标签名称
   * @returns Promise<ApiResponse<TagDto>>
   */
  static async getTagByName(tagName: string): Promise<ApiResponse<TagDto>> {
    const response = await api.get(`/tags/name/${encodeURIComponent(tagName)}`)
    return response.data
  }

  /**
   * 更新标签信息
   * @param tagId 标签ID
   * @param tagName 新的标签名称
   * @param description 新的标签描述
   * @returns Promise<ApiResponse<TagDto>>
   */
  static async updateTag(tagId: number, tagName: string, description?: string): Promise<ApiResponse<TagDto>> {
    const params = new URLSearchParams()
    params.append('tagName', tagName)
    if (description) {
      params.append('description', description)
    }
    
    const response = await api.put(`/tags/${tagId}`, params)
    return response.data
  }

  /**
   * 删除标签
   * @param tagId 标签ID
   * @returns Promise<ApiResponse<string>>
   */
  static async deleteTag(tagId: number): Promise<ApiResponse<string>> {
    const response = await api.delete(`/tags/${tagId}`)
    return response.data
  }

  /**
   * 获取所有标签
   * @returns Promise<ApiResponse<TagDto[]>>
   */
  static async getAllTags(): Promise<ApiResponse<TagDto[]>> {
    const response = await api.get('/tags')
    return response.data
  }

  /**
   * 搜索标签
   * @param keyword 搜索关键词
   * @param page 页码（默认0）
   * @param size 每页数量（默认10）
   * @returns Promise<ApiResponse<Page<TagDto>>>
   */
  static async searchTags(
    keyword: string,
    page: number = 0,
    size: number = 10
  ): Promise<ApiResponse<{
    content: TagDto[]
    totalElements: number
    totalPages: number
    size: number
    number: number
  }>> {
    const response = await api.get('/tags/search', {
      params: { keyword, page, size }
    })
    return response.data
  }

  /**
   * 获取热门标签
   * @param limit 返回数量限制（默认10）
   * @returns Promise<ApiResponse<TagDto[]>>
   */
  static async getPopularTags(limit: number = 10): Promise<ApiResponse<TagDto[]>> {
    const response = await api.get('/tags/popular', {
      params: { limit }
    })
    return response.data
  }

  /**
   * 获取最近创建的标签
   * @param limit 返回数量限制（默认10）
   * @returns Promise<ApiResponse<TagDto[]>>
   */
  static async getRecentTags(limit: number = 10): Promise<ApiResponse<TagDto[]>> {
    const response = await api.get('/tags/recent', {
      params: { limit }
    })
    return response.data
  }

  /**
   * 获取标签统计信息
   * @returns Promise<ApiResponse<TagStatisticsDto>>
   */
  static async getTagStatistics(): Promise<ApiResponse<TagStatisticsDto>> {
    const response = await api.get('/tags/statistics')
    return response.data
  }

  /**
   * 获取推荐标签
   * @param articleId 文章ID
   * @param limit 返回数量限制（默认5）
   * @returns Promise<ApiResponse<TagDto[]>>
   */
  static async getRecommendedTags(articleId: number, limit: number = 5): Promise<ApiResponse<TagDto[]>> {
    const response = await api.get('/tags/recommendations', {
      params: { articleId, limit }
    })
    return response.data
  }

  /**
   * 删除未使用的标签
   * @returns Promise<ApiResponse<number>>
   */
  static async deleteUnusedTags(): Promise<ApiResponse<number>> {
    const response = await api.delete('/tags/unused')
    return response.data
  }

  /**
   * 将逗号分隔的标签字符串转换为标签数组
   * @param tagsString 逗号分隔的标签字符串
   * @returns 标签名称数组
   */
  static parseTagsString(tagsString: string): string[] {
    if (!tagsString || tagsString.trim() === '') {
      return []
    }
    return tagsString.split(',').map(tag => tag.trim()).filter(tag => tag.length > 0)
  }

  /**
   * 将标签数组转换为逗号分隔的字符串
   * @param tags 标签名称数组
   * @returns 逗号分隔的标签字符串
   */
  static tagsToString(tags: string[]): string {
    return tags.join(',')
  }
}
