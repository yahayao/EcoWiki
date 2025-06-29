import axios from 'axios'

// 使用代理路径，在开发环境下会被Vite代理到后端
const BASE_URL = '/api'

// 文章数据接口
export interface Article {
  articleId: number
  title: string
  author: string
  content: string
  publishDate: string
  category: string
  views: number
  likes: number
  tags: string
  comments: number
  updateTime: string
}

// 创建文章请求
export interface ArticleCreateRequest {
  title: string
  author: string
  content: string
  category: string
  tags?: string
}

// 更新文章请求
export interface ArticleUpdateRequest {
  title: string
  content: string
  category: string
  tags?: string
}

// API响应格式 - 匹配后端实际返回格式
export interface ApiResponse<T> {
  code: number
  data: T
  message: string
  timestamp: number
}

// 分页响应
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

// 统计信息
export interface ArticleStatistics {
  totalArticles: number
  totalViews: number
  totalLikes: number
}

class ArticleApi {
  private api = axios.create({
    baseURL: BASE_URL,
    headers: {
      'Content-Type': 'application/json',
    },
  })

  // 创建文章
  async createArticle(article: ArticleCreateRequest): Promise<Article> {
    const response = await this.api.post<ApiResponse<Article>>('/articles', article)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  // 获取文章列表（分页）
  async getArticles(
    page = 0,
    size = 10,
    sortBy = 'publishDate',
    sortDir = 'desc'
  ): Promise<PageResponse<Article>> {
    const response = await this.api.get<ApiResponse<PageResponse<Article>>>('/articles', {
      params: { page, size, sortBy, sortDir }
    })
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  // 根据ID获取文章
  async getArticleById(id: number): Promise<Article> {
    const response = await this.api.get<ApiResponse<Article>>(`/articles/${id}`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  // 更新文章
  async updateArticle(id: number, article: ArticleUpdateRequest): Promise<Article> {
    const response = await this.api.put<ApiResponse<Article>>(`/articles/${id}`, article)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  // 删除文章
  async deleteArticle(id: number): Promise<void> {
    const response = await this.api.delete<ApiResponse<void>>(`/articles/${id}`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
  }

  // 搜索文章
  async searchArticles(keyword: string, page = 0, size = 10): Promise<PageResponse<Article>> {
    const response = await this.api.get<ApiResponse<PageResponse<Article>>>('/articles/search', {
      params: { keyword, page, size }
    })
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  // 根据分类获取文章
  async getArticlesByCategory(category: string, page = 0, size = 10): Promise<PageResponse<Article>> {
    const response = await this.api.get<ApiResponse<PageResponse<Article>>>(`/articles/category/${category}`, {
      params: { page, size }
    })
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  // 根据作者获取文章
  async getArticlesByAuthor(author: string): Promise<Article[]> {
    const response = await this.api.get<ApiResponse<Article[]>>(`/articles/author/${author}`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  // 根据标签获取文章
  async getArticlesByTag(tag: string, page = 0, size = 10): Promise<PageResponse<Article>> {
    const response = await this.api.get<ApiResponse<PageResponse<Article>>>(`/articles/tag/${tag}`, {
      params: { page, size }
    })
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  // 获取热门文章
  async getPopularArticles(limit = 10): Promise<Article[]> {
    const response = await this.api.get<ApiResponse<Article[]>>('/articles/popular', {
      params: { limit }
    })
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  // 获取最新文章
  async getLatestArticles(limit = 10): Promise<Article[]> {
    const response = await this.api.get<ApiResponse<Article[]>>('/articles/latest', {
      params: { limit }
    })
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  // 点赞文章
  async likeArticle(id: number): Promise<void> {
    const response = await this.api.post<ApiResponse<void>>(`/articles/${id}/like`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
  }

  // 取消点赞
  async unlikeArticle(id: number): Promise<void> {
    const response = await this.api.delete<ApiResponse<void>>(`/articles/${id}/like`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
  }

  // 获取统计信息
  async getStatistics(): Promise<ArticleStatistics> {
    const response = await this.api.get<ApiResponse<ArticleStatistics>>('/articles/statistics')
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }

  // 解析Wiki语法（调用后端API）
  async parseWikiText(wikiText: string): Promise<string> {
    const response = await this.api.post<ApiResponse<string>>('/articles/parse', {
      content: wikiText
    })
    if (response.data.code !== 200) {
      throw new Error(response.data.message)
    }
    return response.data.data
  }
}

export const articleApi = new ArticleApi()
export default articleApi
