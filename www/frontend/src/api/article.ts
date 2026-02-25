/**
 * 文章相关API接口服务模块
 * 
 * 功能：
 * - 提供完整的文章管理功能接口
 * - 支持文章CRUD操作和检索功能
 * - 实现文章互动和统计分析功能
 * - 提供Wiki语法解析和处理接口
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
import axios from 'axios'

/**
 * API基础路径配置
 * 在开发环境下会被Vite代理到后端服务器
 * 生产环境下直接使用相对路径访问同域API
 */
const BASE_URL = '/api'

// ── key 转换工具（与 index.ts 保持一致） ─────────────────────────────────────
function _toCamel(s: string) { return s.replace(/_([a-z])/g, (_, c) => c.toUpperCase()) }
function _toSnake(s: string) { return s.replace(/([A-Z])/g, c => `_${c.toLowerCase()}`) }
function _transformKeys(obj: any, fn: (k: string) => string): any {
  if (Array.isArray(obj)) return obj.map(v => _transformKeys(v, fn))
  if (obj !== null && typeof obj === 'object' && !(obj instanceof File) && !(obj instanceof FormData)) {
    return Object.fromEntries(Object.entries(obj).map(([k, v]) => [fn(k), _transformKeys(v, fn)]))
  }
  return obj
}

/**
 * 将文章或文章列表中的 tags 字段从对象数组转换为逗号分隔的字符串
 * 后端返回的 tags 格式：[{tagId, tagName, color}, ...]
 * 前端期望格式：'tag1,tag2,tag3'
 */
function _normalizeTags(data: any): void {
  if (!data) return
  const convertTags = (item: any) => {
    if (item && Array.isArray(item.tags)) {
      item.tags = item.tags.map((t: any) => t.tagName || t.tag_name || '').filter(Boolean).join(',')
    }
  }
  // 直接是文章对象
  if (data.articleId !== undefined || data.article_id !== undefined) {
    convertTags(data)
    return
  }
  // 分页结果 (PageResult)
  if (data.content && Array.isArray(data.content)) {
    data.content.forEach(convertTags)
    return
  }
  // 数组（如 popular/latest 返回的 Article[]）
  if (Array.isArray(data)) {
    data.forEach(convertTags)
  }
}

/**
 * 文章数据接口定义
 * 定义了文章实体的完整数据结构，与后端Article实体保持一致
 * 
 * @interface Article
 */
export interface Article {
  /** 文章唯一标识ID */
  articleId: number
  /** 文章标题 */
  title: string
  /** 文章作者 */
  author: string
  /** 作者头像URL */
  authorAvatar?: string
  /** 文章内容（支持Wiki语法） */
  content: string
  /** 发布日期（ISO格式字符串） */
  publishDate: string
  /** 文章分类 */
  category: string
  /** 浏览次数 */
  views: number
  /** 点赞数量 */
  likes: number
  /** 文章标签（逗号分隔的字符串） */
  tags: string
  /** 评论数量 */
  comments: number
  /** 最后更新时间（ISO格式字符串） */
  updateTime: string
  /** 贡献者列表（可选，用于预加载） */
  contributors?: Array<{
    username: string
    displayName: string
    avatarUrl: string
    editCount: number
    latestEdit: string
  }>
  /** 贡献者加载错误（可选） */
  contributorsError?: string | null
}

/**
 * 创建文章请求数据传输对象
 * 用于新建文章时的数据提交
 * 
 * @interface ArticleCreateRequest
 */
export interface ArticleCreateRequest {
  /** 文章标题（必填） */
  title: string
  /** 文章作者（必填） */
  author: string
  /** 文章内容（必填，支持Wiki语法） */
  content: string
  /** 文章分类（必填） */
  category: string
  /** 文章标签（可选，逗号分隔） */
  tags?: string
}

/**
 * 更新文章请求数据传输对象
 * 用于编辑现有文章时的数据提交
 * 注意：作者信息不可修改，articleId通过URL参数传递
 * 
 * @interface ArticleUpdateRequest
 */
export interface ArticleUpdateRequest {
  /** 文章标题（必填） */
  title: string
  /** 文章内容（必填，支持Wiki语法） */
  content: string
  /** 文章分类（必填） */
  category: string
  /** 文章标签（可选，逗号分隔） */
  tags?: string
}

/**
 * 统一API响应格式接口
 * 与后端ApiResponse类保持一致，确保前后端数据格式统一
 * 
 * @interface ApiResponse
 * @template T 响应数据的类型
 */
export interface ApiResponse<T> {
  /** 响应状态码（200表示成功） */
  code: number
  /** 响应数据载荷 */
  data: T
  /** 响应消息（成功或错误信息） */
  message: string
  /** 响应时间戳 */
  timestamp: number
}

/**
 * 分页响应数据接口
 * 标准的Spring Data分页响应格式，支持分页查询结果展示
 * 
 * @interface PageResponse
 * @template T 分页内容的数据类型
 */
export interface PageResponse<T> {
  /** 当前页数据内容 */
  content: T[]
  /** 总元素数量 */
  totalElements: number
  /** 总页数 */
  totalPages: number
  /** 每页大小 */
  size: number
  /** 当前页码（从0开始） */
  number: number
  /** 是否为第一页 */
  first: boolean
  /** 是否为最后一页 */
  last: boolean
}

/**
 * 文章统计信息接口
 * 用于展示文章相关的汇总统计数据
 * 
 * @interface ArticleStatistics
 */
export interface ArticleStatistics {
  /** 文章总数 */
  totalArticles: number
  /** 总浏览量 */
  totalViews: number
  /** 总点赞数 */
  totalLikes: number
}

/**
 * 文章版本历史相关接口定义
 */

/**
 * 文章版本数据接口
 */
export interface ArticleVersion {
  /** 版本ID */
  versionId: number
  /** 文章ID */
  articleId: number
  /** 版本号 */
  versionNumber: number
  /** 版本作者 */
  author: string
  /** 创建时间 */
  createdAt: string
  /** 存储类型 */
  storageType: 'FULL_CONTENT' | 'DIFF_FROM_BASE' | 'DIFF_FROM_PREV'
  /** 是否归档 */
  isArchived: boolean
  /** 变更摘要 */
  changeSummary?: string
  /** 原始大小 */
  originalSize?: number
  /** 压缩大小 */
  compressedSize?: number
  /** 压缩比率 */
  compressionRatio?: number
}

/**
 * 版本内容响应
 */
export interface VersionContentResponse {
  /** 版本内容 */
  content: string
  /** 版本号 */
  versionNumber?: number
}

/**
 * 版本统计信息
 */
export interface ArticleVersionStats {
  /** 总版本数 */
  totalVersions: number
  /** 基础版本数 */
  baseVersionsCount: number
  /** 差异版本数 */
  diffVersionsCount: number
  /** 归档版本数 */
  archivedVersionsCount: number
  /** 总存储大小 */
  totalStorageSize: number
  /** 压缩后存储大小 */
  compressedStorageSize: number
  /** 压缩比率 */
  compressionRatio: number
  /** 访问频率 */
  accessFrequency: number
  /** 最后访问时间 */
  lastAccessedAt?: string
  /** 最后优化时间 */
  lastOptimizedAt?: string
  /** 是否需要优化 */
  optimizationNeeded: boolean
  /** 需要优化判断 */
  needsOptimization: boolean
}

/**
 * 创建版本请求
 */
export interface CreateVersionRequest {
  /** 文章内容 */
  content: string
  /** 作者 */
  author: string
  /** 变更摘要 */
  changeSummary?: string
}

/**
 * 版本历史响应
 */
export interface VersionHistoryResponse {
  /** 版本列表 */
  versions: ArticleVersion[]
  /** 总元素数 */
  totalElements: number
  /** 总页数 */
  totalPages: number
  /** 当前页 */
  currentPage: number
  /** 每页大小 */
  size: number
}

/**
 * 文章API服务类
 * 
 * 该类封装了所有文章相关的HTTP请求操作，提供了完整的文章管理功能。
 * 采用类单例模式，通过axios实例进行HTTP通信。
 * 
 * 核心功能：
 * - 文章CRUD操作
 * - 分页查询和搜索
 * - 分类和标签筛选
 * - 点赞互动功能
 * - 统计数据获取
 * - Wiki语法解析
 * 
 * 错误处理：
 * - 统一的响应状态码检查
 * - 自动错误信息提取和抛出
 * - 网络请求异常处理
 * 
 * @class ArticleApi
 */
class ArticleApi {
  /**
   * axios实例配置
   * 设置了基础URL、请求头等通用配置
   * 
   * @private
   * @type {axios.AxiosInstance}
   */
  private api = axios.create({
    baseURL: BASE_URL,
    headers: {
      'Content-Type': 'application/json',
    },
  })

  constructor() {
    // 请求拦截器：添加 JWT token + camelCase→snake_case 转换
    this.api.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('token')
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
        if (config.data && !(config.data instanceof FormData) && !(config.data instanceof Blob)) {
          config.data = _transformKeys(config.data, _toSnake)
        }
        if (config.params) {
          config.params = _transformKeys(config.params, _toSnake)
        }
        return config
      },
      (error) => Promise.reject(error)
    )
    // 响应拦截器：snake_case→camelCase 转换 + tags数组→字符串转换
    this.api.interceptors.response.use(
      (response) => {
        if (response.data) response.data = _transformKeys(response.data, _toCamel)
        // 将文章数据中的 tags 数组（[{tagId,tagName,color}]）转换为逗号分隔的字符串
        if (response.data?.data) {
          _normalizeTags(response.data.data)
        }
        return response
      },
      (error) => Promise.reject(error)
    )
  }

  /**
   * 创建新文章
   * 
   * 提交新文章数据到后端，创建成功后返回完整的文章信息。
   * 文章ID和时间戳等信息由后端自动生成。
   * 
   * @param {ArticleCreateRequest} article - 文章创建请求数据
   * @returns {Promise<Article>} 创建成功的文章完整信息
   * @throws {Error} 当创建失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * const newArticle = await articleApi.createArticle({
   *   title: "生态保护指南",
   *   author: "环保专家",
   *   content: "这是一篇关于生态保护的文章...",
   *   category: "环保知识",
   *   tags: "环保,生态,保护"
   * });
   * ```
   */
  async createArticle(article: ArticleCreateRequest): Promise<Article> {
    const response = await this.api.post<ApiResponse<Article>>('/articles', article)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  /**
   * 检查文章标题是否已存在
   * 
   * 在创建文章前检查标题是否重复，避免创建重复内容。
   * 可用于实时验证用户输入的标题。
   * 
   * @param {string} title - 要检查的文章标题
   * @returns {Promise<boolean>} 如果标题已存在返回true，否则返回false
   * @throws {Error} 当检查失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * const exists = await articleApi.checkTitleExists("生态保护指南");
   * if (exists) {
   *   console.log("标题已存在，请选择其他标题");
   * }
   * ```
   */
  async checkTitleExists(title: string): Promise<boolean> {
    const response = await this.api.get<ApiResponse<{exists: boolean}>>('/articles/check-title', {
      params: { title }
    })
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return (response.data.data as any)?.exists ?? false
  }

  /**
   * 获取文章列表（分页查询）
   * 
   * 支持分页、排序的文章列表查询，是文章展示的核心功能。
   * 默认按发布时间倒序排列，可自定义排序字段和方向。
   * 
   * @param {number} page - 页码（从0开始，默认为0）
   * @param {number} size - 每页大小（默认为10）
   * @param {string} sortBy - 排序字段（默认为发布时间）
   * @param {string} sortDir - 排序方向（asc/desc，默认为desc）
   * @returns {Promise<PageResponse<Article>>} 分页文章列表
   * @throws {Error} 当查询失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * // 获取第一页，每页10条，按点赞数降序
   * const articles = await articleApi.getArticles(0, 10, 'likes', 'desc');
   * ```
   */
  async getArticles(
    page = 0,
    size = 10,
    sortBy = 'publishDate',
    sortDir = 'desc'
  ): Promise<PageResponse<Article>> {
    // 后端使用单个 sort 参数：newest/oldest/views/likes
    const sortMap: Record<string, string> = {
      'publishDate-desc': 'newest', 'publishDate-asc': 'oldest',
      'views-desc': 'views', 'likes-desc': 'likes',
    }
    const sort = sortMap[`${sortBy}-${sortDir}`] ?? 'newest'
    const response = await this.api.get<ApiResponse<PageResponse<Article>>>('/articles', {
      params: { page, size, sort }
    })
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  /**
   * 根据ID获取指定文章
   * 
   * 获取单篇文章的详细信息，通常用于文章详情页面展示。
   * 同时会触发文章浏览量的增加。
   * 
   * @param {number} id - 文章ID
   * @returns {Promise<Article>} 文章详细信息
   * @throws {Error} 当文章不存在或查询失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * const article = await articleApi.getArticleById(123);
   * ```
   */
  async getArticleById(id: number): Promise<Article> {
    const response = await this.api.get<ApiResponse<Article>>(`/articles/${id}`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  /**
   * 更新现有文章
   * 
   * 更新指定文章的内容信息，需要提供文章ID和更新数据。
   * 更新时间由后端自动更新，作者信息不可修改。
   * 
   * @param {number} id - 文章ID
   * @param {ArticleUpdateRequest} article - 文章更新数据
   * @returns {Promise<Article>} 更新后的文章完整信息
   * @throws {Error} 当更新失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * const updatedArticle = await articleApi.updateArticle(123, {
   *   title: "更新后的标题",
   *   content: "更新后的内容...",
   *   category: "新分类",
   *   tags: "新标签1,新标签2"
   * });
   * ```
   */
  async updateArticle(id: number, article: ArticleUpdateRequest): Promise<Article> {
    const response = await this.api.put<ApiResponse<Article>>(`/articles/${id}`, article)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  /**
   * 删除指定文章
   * 
   * 永久删除指定的文章，该操作不可恢复。
   * 通常需要管理员权限或文章作者权限。
   * 
   * @param {number} id - 文章ID
   * @returns {Promise<void>} 删除操作无返回数据
   * @throws {Error} 当删除失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * await articleApi.deleteArticle(123);
   * ```
   */
  async deleteArticle(id: number): Promise<void> {
    const response = await this.api.delete<ApiResponse<void>>(`/articles/${id}`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
  }

  /**
   * 搜索文章
   * 
   * 根据关键词在文章标题、内容、分类、标签中进行模糊搜索。
   * 支持分页查询，搜索结果按相关度排序。
   * 
   * @param {string} keyword - 搜索关键词
   * @param {number} page - 页码（从0开始，默认为0）
   * @param {number} size - 每页大小（默认为10）
   * @returns {Promise<PageResponse<Article>>} 搜索结果分页数据
   * @throws {Error} 当搜索失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * const searchResults = await articleApi.searchArticles("环保", 0, 5);
   * ```
   */
  async searchArticles(keyword: string, page = 0, size = 10): Promise<PageResponse<Article>> {
    const response = await this.api.get<ApiResponse<PageResponse<Article>>>('/articles/search', {
      params: { keyword, page, size }
    })
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  /**
   * 根据分类获取文章
   * 
   * 获取指定分类下的所有文章，支持分页查询。
   * 通常用于分类页面的文章展示。
   * 
   * @param {string} category - 文章分类名称
   * @param {number} page - 页码（从0开始，默认为0）
   * @param {number} size - 每页大小（默认为10）
   * @returns {Promise<PageResponse<Article>>} 分类文章分页数据
   * @throws {Error} 当查询失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * const categoryArticles = await articleApi.getArticlesByCategory("环保知识");
   * ```
   */
  async getArticlesByCategory(category: string, page = 0, size = 10): Promise<PageResponse<Article>> {
    const response = await this.api.get<ApiResponse<PageResponse<Article>>>('/articles', {
      params: { page, size, category }
    })
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  /**
   * 根据作者获取文章
   * 
   * 获取指定作者的所有文章，通常用于作者页面展示。
   * 注意：该接口返回的是完整的文章列表，不支持分页。
   * 
   * @param {string} author - 作者名称
   * @returns {Promise<Article[]>} 作者的文章列表
   * @throws {Error} 当查询失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * const authorArticles = await articleApi.getArticlesByAuthor("环保专家");
   * ```
   */
  async getArticlesByAuthor(author: string): Promise<Article[]> {
    const response = await this.api.get<ApiResponse<PageResponse<Article>>>('/articles', {
      params: { author, size: 100 }
    })
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return (response.data.data as any).content ?? response.data.data
  }

  /**
   * 根据标签获取文章
   * 
   * 获取包含指定标签的所有文章，支持分页查询。
   * 标签匹配是精确匹配，用于标签页面的文章展示。
   * 
   * @param {string} tag - 标签名称
   * @param {number} page - 页码（从0开始，默认为0）
   * @param {number} size - 每页大小（默认为10）
   * @returns {Promise<PageResponse<Article>>} 标签文章分页数据
   * @throws {Error} 当查询失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * const tagArticles = await articleApi.getArticlesByTag("生态保护");
   * ```
   */
  async getArticlesByTag(tag: string, page = 0, size = 10): Promise<PageResponse<Article>> {
    const response = await this.api.get<ApiResponse<PageResponse<Article>>>('/articles', {
      params: { page, size, tags: tag }
    })
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  /**
   * 获取热门文章
   * 
   * 根据浏览量、点赞数等指标获取热门文章列表。
   * 通常用于首页推荐和侧边栏热门文章展示。
   * 
   * @param {number} limit - 返回文章数量限制（默认为10）
   * @returns {Promise<Article[]>} 热门文章列表
   * @throws {Error} 当查询失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * const popularArticles = await articleApi.getPopularArticles(5);
   * ```
   */
  async getPopularArticles(limit = 10): Promise<Article[]> {
    const response = await this.api.get<ApiResponse<Article[]>>('/articles/popular', {
      params: { limit }
    })
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  /**
   * 获取最新文章
   * 
   * 按发布时间倒序获取最新发布的文章列表。
   * 通常用于首页最新动态和侧边栏最新文章展示。
   * 
   * @param {number} limit - 返回文章数量限制（默认为10）
   * @returns {Promise<Article[]>} 最新文章列表
   * @throws {Error} 当查询失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * const latestArticles = await articleApi.getLatestArticles(5);
   * ```
   */
  async getLatestArticles(limit = 10): Promise<Article[]> {
    const response = await this.api.get<ApiResponse<Article[]>>('/articles/latest', {
      params: { limit }
    })
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  /**
   * 点赞文章
   * 
   * 为指定文章添加点赞，增加文章的点赞计数。
   * 通常需要用户登录才能执行该操作。
   * 
   * @param {number} id - 文章ID
   * @returns {Promise<void>} 点赞操作无返回数据
   * @throws {Error} 当点赞失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * await articleApi.likeArticle(123);
   * ```
   */
  async likeArticle(id: number): Promise<void> {
    const response = await this.api.post<ApiResponse<void>>(`/articles/${id}/like`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
  }

  /**
   * 取消点赞文章
   * 
   * 取消对指定文章的点赞，减少文章的点赞计数。
   * 通常需要用户登录且之前已点赞才能执行该操作。
   * 
   * @param {number} id - 文章ID
   * @returns {Promise<void>} 取消点赞操作无返回数据
   * @throws {Error} 当取消点赞失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * await articleApi.unlikeArticle(123);
   * ```
   */
  async unlikeArticle(id: number): Promise<void> {
    // 后端点赞是 toggle：再次 POST 即取消点赞
    const response = await this.api.post<ApiResponse<void>>(`/articles/${id}/like`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
  }

  /**
   * 收藏文章
   * 
   * 为指定文章添加收藏，用户可以在个人中心查看收藏的文章列表。
   * 需要用户登录才能执行该操作。
   * 
   * @param {number} id - 文章ID
   * @returns {Promise<void>} 收藏操作无返回数据
   * @throws {Error} 当收藏失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * await articleApi.favoriteArticle(123);
   * ```
   */
  async favoriteArticle(id: number): Promise<void> {
    const response = await this.api.post<ApiResponse<void>>(`/articles/${id}/favorite`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
  }

  /**
   * 取消收藏文章
   * 
   * 取消对指定文章的收藏，从用户的收藏列表中移除。
   * 需要用户登录且之前已收藏才能执行该操作。
   * 
   * @param {number} id - 文章ID
   * @returns {Promise<void>} 取消收藏操作无返回数据
   * @throws {Error} 当取消收藏失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * await articleApi.unfavoriteArticle(123);
   * ```
   */
  async unfavoriteArticle(id: number): Promise<void> {
    // 后端收藏是 toggle：再次 POST 即取消收藏
    const response = await this.api.post<ApiResponse<void>>(`/articles/${id}/favorite`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
  }

  /**
   * 检查文章点赞状态
   * 
   * 检查当前用户是否已对指定文章点赞。
   * 需要用户登录才能获取准确状态。
   * 
   * @param {number} id - 文章ID
   * @returns {Promise<boolean>} 是否已点赞
   * @throws {Error} 当检查失败时抛出错误信息
   */
  async checkLikeStatus(id: number): Promise<boolean> {
    const response = await this.api.get<ApiResponse<{ liked: boolean }>>(`/articles/${id}/like-status`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data.liked
  }

  /**
   * 检查文章收藏状态
   * 
   * 检查当前用户是否已收藏指定文章。
   * 需要用户登录才能获取准确状态。
   * 
   * @param {number} id - 文章ID
   * @returns {Promise<boolean>} 是否已收藏
   * @throws {Error} 当检查失败时抛出错误信息
   */
  async checkFavoriteStatus(id: number): Promise<boolean> {
    const response = await this.api.get<ApiResponse<{ favorited: boolean }>>(`/articles/${id}/favorite-status`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data.favorited
  }

  /**
   * 获取文章统计信息
   * 
   * 获取整个系统的文章统计数据，包括总文章数、总浏览量、总点赞数等。
   * 通常用于管理后台的数据概览和首页统计展示。
   * 
   * @returns {Promise<ArticleStatistics>} 文章统计数据
   * @throws {Error} 当获取统计信息失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * const stats = await articleApi.getStatistics();
   * console.log(`总文章数: ${stats.totalArticles}`);
   * ```
   */
  async getStatistics(): Promise<ArticleStatistics> {
    const response = await this.api.get<ApiResponse<ArticleStatistics>>('/articles/statistics')
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  /**
   * 发布文章
   * 
   * 将草稿状态的文章发布为正式文章。
   * 发布后文章将对所有用户可见。
   * 
   * @param {number} id - 文章ID
   * @returns {Promise<Article>} 发布后的文章信息
   * @throws {Error} 当发布失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * const publishedArticle = await articleApi.publishArticle(123);
   * console.log(`文章 "${publishedArticle.title}" 已发布`);
   * ```
   */
  async publishArticle(id: number): Promise<Article> {
    const response = await this.api.put<ApiResponse<Article>>(`/articles/${id}/publish`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  /**
   * 切换文章状态（通用更新状态接口）
   */
  async setArticleStatus(id: number, status: string): Promise<Article> {
    const response = await this.api.put<ApiResponse<Article>>(`/articles/${id}`, { status })
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  /**
   * 解析Wiki语法文本
   * 
   * 调用后端API将Wiki格式的文本解析为HTML格式。
   * 支持Wiki标准语法，用于文章预览和内容渲染。
   * 
   * @param {string} wikiText - Wiki格式的文本内容
   * @returns {Promise<string>} 解析后的HTML内容
   * @throws {Error} 当解析失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * const wikiText = "== 标题 ==\n这是'''粗体'''文本";
   * const htmlContent = await articleApi.parseWikiText(wikiText);
   * ```
   */
  async parseWikiText(wikiText: string): Promise<string> {
    const response = await this.api.post<ApiResponse<string>>('/articles/parse', {
      content: wikiText
    })
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  /**
   * 根据标题获取文章ID
   * 
   * 通过文章标题查找对应的文章ID，用于支持基于标题的路由。
   * 
   * @param {string} title - 文章标题
   * @returns {Promise<number>} 文章ID
   * @throws {Error} 当文章不存在或查询失败时抛出错误信息
   * 
   * @example
   * ```typescript
   * const articleId = await articleApi.getArticleIdByTitle("生态保护指南");
   * ```
   */
  async getArticleIdByTitle(title: string): Promise<number> {
    const response = await this.api.get<ApiResponse<number>>(`/articles/title/${encodeURIComponent(title)}/id`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  /**
   * 获取文章贡献者列表
   * 基于文章版本历史统计贡献者信息
   * 
   * @param {number} articleId - 文章ID
   * @returns {Promise<Array>} 贡献者列表
   */
  async getArticleContributors(articleId: number): Promise<Array<{
    username: string
    displayName: string
    avatarUrl: string
    editCount: number
    latestEdit: string
  }>> {
    try {
      // 直接调用后端的贡献者API
      const response = await this.api.get<ApiResponse<Array<{
        username: string
        displayName: string
        avatarUrl: string
        editCount: number
        latestEdit: string
      }>>>(`/articles/${articleId}/contributors`)
      
      if (response.data.code !== 200) {
        throw new Error(response.data.message)
      }
      
      return response.data.data
      
    } catch (error) {
      console.error('API获取贡献者失败，使用版本历史回退方案:', error)
      
      // 如果API失败，回退到使用版本历史
      try {
      // 获取版本历史
      const versionHistory = await this.getArticleVersions(articleId, 0, 100)
      
      if (!versionHistory.versions || versionHistory.versions.length === 0) {
        return []
      }
      
      // 获取文章信息以排除原作者
      const article = await this.getArticleById(articleId)
      const originalAuthor = article.author
      
      // 统计贡献者
      const contributorMap = new Map<string, {
        username: string
        editCount: number
        latestEdit: string
      }>()
      
      versionHistory.versions.forEach(version => {
        const author = version.author?.trim()
        
        // 排除原作者和空值
        if (!author || author.toLowerCase() === originalAuthor?.toLowerCase()) {
          return
        }
        
        if (contributorMap.has(author)) {
          const existing = contributorMap.get(author)!
          existing.editCount++
          if (new Date(version.createdAt) > new Date(existing.latestEdit)) {
            existing.latestEdit = version.createdAt
          }
        } else {
          contributorMap.set(author, {
            username: author,
            editCount: 1,
            latestEdit: version.createdAt
          })
        }
      })
      
      // 转换为数组并排序
      return Array.from(contributorMap.entries())
        .map(([username, data]) => ({
          username: data.username,
          displayName: data.username,
          avatarUrl: this.generateDefaultAvatar(data.username), // 生成默认头像
          editCount: data.editCount,
          latestEdit: data.latestEdit
        }))
        .sort((a, b) => {
          if (a.editCount !== b.editCount) {
            return b.editCount - a.editCount
          }
          return new Date(b.latestEdit).getTime() - new Date(a.latestEdit).getTime()
        })
        
      } catch (fallbackError) {
        console.error('版本历史回退方案也失败:', fallbackError)
        return []
      }
    }
  }

  /**
   * 获取文章版本历史列表
   * 
   * @param {number} articleId - 文章ID
   * @param {number} page - 页码
   * @param {number} size - 每页大小
   * @returns {Promise<VersionHistoryResponse>} 版本历史数据
   */
  async getArticleVersions(articleId: number, page = 0, size = 20): Promise<VersionHistoryResponse> {
    const response = await this.api.get<ApiResponse<any>>(`/articles/${articleId}/versions`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    // 后端返回版本列表数组，封装成 VersionHistoryResponse 格式
    const rawList: any[] = Array.isArray(response.data.data) ? response.data.data : []
    const sliced = rawList.slice(page * size, page * size + size)
    return {
      versions: sliced,
      totalElements: rawList.length,
      totalPages: Math.ceil(rawList.length / size),
      currentPage: page,
      size,
    }
  }

  /**
   * 获取指定版本的内容
   * 
   * @param {number} articleId - 文章ID
   * @param {number} versionNumber - 版本号
   * @returns {Promise<VersionContentResponse>} 版本内容
   */
  async getVersionContent(articleId: number, versionNumber: number): Promise<VersionContentResponse> {
    const response = await this.api.get<ApiResponse<any>>(`/articles/${articleId}/versions/${versionNumber}`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    const v = response.data.data
    return { content: v.content ?? '', versionNumber: v.versionNumber }
  }

  /**
   * 获取最新版本的内容
   * 
   * @param {number} articleId - 文章ID
   * @returns {Promise<VersionContentResponse>} 最新版本内容
   */
  async getLatestVersionContent(articleId: number): Promise<VersionContentResponse> {
    const response = await this.api.get<ApiResponse<any>>(`/articles/${articleId}/versions/latest`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    const v = response.data.data
    return { content: v.content ?? '', versionNumber: v.versionNumber }
  }

  /**
   * 创建新版本
   * 
   * @param {number} articleId - 文章ID
   * @param {CreateVersionRequest} request - 创建版本请求
   * @returns {Promise<ArticleVersion>} 新创建的版本
   */
  async createVersion(articleId: number, request: CreateVersionRequest): Promise<ArticleVersion> {
    const response = await this.api.post<ApiResponse<ArticleVersion>>(`/articles/${articleId}/versions`, request)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  /**
   * 获取文章版本统计信息
   * 
   * @param {number} articleId - 文章ID
   * @returns {Promise<ArticleVersionStats>} 版本统计信息
   */
  async getVersionStats(articleId: number): Promise<ArticleVersionStats> {
    const response = await this.api.get<ApiResponse<ArticleVersionStats>>(`/articles/${articleId}/versions/stats`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  /**
   * 恢复到指定版本
   * 
   * @param {number} articleId - 文章ID
   * @param {number} versionNumber - 版本号
   * @param {string} author - 恢复操作的作者
   * @returns {Promise<ArticleVersion>} 恢复后创建的新版本
   */
  async restoreToVersion(articleId: number, versionNumber: number, author: string): Promise<ArticleVersion> {
    const response = await this.api.post<ApiResponse<ArticleVersion>>(`/articles/${articleId}/versions/${versionNumber}/restore`, {})
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  /**
   * 生成默认头像URL
   * 
   * @param {string} username - 用户名
   * @returns {string} 默认头像URL
   * @private
   */
  private generateDefaultAvatar(username: string): string {
    if (!username) return ''
    
    // 使用 DiceBear API 生成默认头像
    const encodedUsername = encodeURIComponent(username)
    return `https://api.dicebear.com/7.x/initials/svg?seed=${encodedUsername}&backgroundColor=random`
  }
}

/**
 * 文章API服务实例
 * 
 * 提供统一的文章API访问接口，使用单例模式确保全局状态一致性。
 * 推荐在整个应用中使用该实例进行文章相关的API调用。
 * 
 * @type {ArticleApi}
 * @example
 * ```typescript
 * import { articleApi } from '@/api/article'
 * 
 * // 获取文章列表
 * const articles = await articleApi.getArticles()
 * 
 * // 创建新文章
 * const newArticle = await articleApi.createArticle({
 *   title: "新文章",
 *   author: "作者",
 *   content: "内容",
 *   category: "分类"
 * })
 * ```
 */
export const articleApi = new ArticleApi()

/**
 * 默认导出文章API实例
 * 支持ES6默认导入语法
 * 
 * @default articleApi
 * @example
 * ```typescript
 * import articleApi from '@/api/article'
 * ```
 */
export default articleApi
