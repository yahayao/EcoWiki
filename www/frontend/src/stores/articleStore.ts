/**
 * 文章状态管理 Pinia Store
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { articleApi, type Article, type ArticleCreateRequest, type ArticleUpdateRequest, type ArticleVersion } from '../api/article'

export const useArticleStore = defineStore('article', () => {
  const articles = ref<Article[]>([])
  const currentArticle = ref<Article | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const currentPage = ref(0)
  const pageSize = ref(10)
  const totalElements = ref(0)
  const totalPages = ref(0)
  const searchKeyword = ref('')
  const categoryFilter = ref('')
  const sortBy = ref<'latest' | 'popular' | 'views'>('latest')
  const versions = ref<ArticleVersion[]>([])

  const fetchArticles = async (category?: string) => {
    loading.value = true; error.value = null
    try {
      const res = await articleApi.getArticles(currentPage.value, pageSize.value, category || categoryFilter.value, sortBy.value)
      if (res.data?.code === 200) {
        const pageData = res.data.data
        articles.value = pageData.content || []
        totalElements.value = pageData.totalElements || 0
        totalPages.value = pageData.totalPages || 0
      } else throw new Error(res.data?.message || '获取文章列表失败')
    } catch (err: any) { error.value = err.message || '获取文章列表失败' }
    finally { loading.value = false }
  }

  const searchArticles = async (keyword: string, page = 0) => {
    searchKeyword.value = keyword; currentPage.value = page
    loading.value = true; error.value = null
    try {
      const res = await articleApi.searchArticles(keyword, page, pageSize.value)
      if (res.data?.code === 200) {
        const pageData = res.data.data
        articles.value = pageData.content || []
        totalElements.value = pageData.totalElements || 0
        totalPages.value = pageData.totalPages || 0
      } else throw new Error(res.data?.message || '搜索文章失败')
    } catch (err: any) { error.value = err.message || '搜索文章失败' }
    finally { loading.value = false }
  }

  const fetchArticleDetail = async (title: string) => {
    loading.value = true; error.value = null
    try {
      const res = await articleApi.getArticleByTitle(title)
      if (res.data?.code === 200) currentArticle.value = res.data.data
      else throw new Error(res.data?.message || '获取文章详情失败')
    } catch (err: any) { error.value = err.message || '获取文章详情失败'; currentArticle.value = null }
    finally { loading.value = false }
  }

  const createArticle = async (data: ArticleCreateRequest) => {
    loading.value = true; error.value = null
    try {
      const res = await articleApi.createArticle(data)
      if (res.data?.code === 200) return res.data.data
      throw new Error(res.data?.message || '创建文章失败')
    } catch (err: any) { error.value = err.message || '创建文章失败'; throw err }
    finally { loading.value = false }
  }

  const updateArticle = async (title: string, data: ArticleUpdateRequest) => {
    loading.value = true; error.value = null
    try {
      const res = await articleApi.updateArticle(title, data)
      if (res.data?.code === 200) return res.data.data
      throw new Error(res.data?.message || '更新文章失败')
    } catch (err: any) { error.value = err.message || '更新文章失败'; throw err }
    finally { loading.value = false }
  }

  const deleteArticle = async (articleId: number) => {
    loading.value = true; error.value = null
    try {
      const res = await articleApi.deleteArticle(articleId)
      if (res.data?.code !== 200) throw new Error(res.data?.message || '删除文章失败')
    } catch (err: any) { error.value = err.message || '删除文章失败'; throw err }
    finally { loading.value = false }
  }

  const fetchPopularArticles = async (limit = 10) => {
    try {
      const res = await articleApi.getPopularArticles(limit)
      return res.data?.code === 200 ? (res.data.data || []) : []
    } catch { return [] }
  }

  const fetchLatestArticles = async (limit = 10) => {
    try {
      const res = await articleApi.getLatestArticles(limit)
      return res.data?.code === 200 ? (res.data.data || []) : []
    } catch { return [] }
  }

  const setPage = (page: number) => { currentPage.value = page; fetchArticles() }
  const setSortBy = (sort: 'latest' | 'popular' | 'views') => { sortBy.value = sort; currentPage.value = 0; fetchArticles() }
  const setCategory = (category: string) => { categoryFilter.value = category; currentPage.value = 0; fetchArticles() }
  const clearSearch = () => { searchKeyword.value = ''; currentPage.value = 0; fetchArticles() }

  const fetchVersions = async (articleId: number) => {
    try {
      const res = await articleApi.getVersionHistory(articleId)
      if (res.data?.code === 200) versions.value = res.data.data || []
    } catch { versions.value = [] }
  }

  return {
    articles, currentArticle, loading, error, currentPage, pageSize,
    totalElements, totalPages, searchKeyword, categoryFilter, sortBy, versions,
    fetchArticles, searchArticles, fetchArticleDetail, createArticle,
    updateArticle, deleteArticle, fetchPopularArticles, fetchLatestArticles,
    setPage, setSortBy, setCategory, clearSearch, fetchVersions
  }
})
