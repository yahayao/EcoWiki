import axios from 'axios';
import type {
  ArticleReview,
  ReviewDetail,
  ReviewStatistics,
  CreateReviewRequest,
  AssignReviewerRequest,
  ProcessReviewRequest,
  ApiResponse,
  PageResponse
} from '@/types/review';

const API_BASE_URL = '/api/review';

/**
 * 文章审核 API 服务
 */
export class ReviewApiService {
  
  /**
   * 创建审核申请
   * @param request 创建审核请求参数
   * @returns 创建的审核记录
   */
  static async createReview(request: CreateReviewRequest): Promise<ApiResponse<ArticleReview>> {
    try {
      const response = await axios.post(`${API_BASE_URL}/create`, request);
      return response.data;
    } catch (error) {
      console.error('创建审核申请失败:', error);
      throw error;
    }
  }

  /**
   * 分配审核员
   * @param reviewId 审核ID
   * @param request 分配审核员请求参数
   * @returns 分配结果
   */
  static async assignReviewer(
    reviewId: number, 
    request: AssignReviewerRequest
  ): Promise<ApiResponse> {
    try {
      const response = await axios.post(`${API_BASE_URL}/${reviewId}/assign`, request);
      return response.data;
    } catch (error) {
      console.error('分配审核员失败:', error);
      throw error;
    }
  }

  /**
   * 处理审核
   * @param reviewId 审核ID
   * @param request 处理审核请求参数
   * @returns 审核结果
   */
  static async processReview(
    reviewId: number, 
    request: ProcessReviewRequest
  ): Promise<ApiResponse<ArticleReview>> {
    try {
      const response = await axios.post(`${API_BASE_URL}/${reviewId}/process`, request);
      return response.data;
    } catch (error) {
      console.error('处理审核失败:', error);
      throw error;
    }
  }

  /**
   * 获取审核详情
   * @param reviewId 审核ID
   * @returns 审核详情
   */
  static async getReviewDetail(reviewId: number): Promise<ApiResponse<ReviewDetail>> {
    try {
      const response = await axios.get(`${API_BASE_URL}/${reviewId}`);
      return response.data;
    } catch (error) {
      console.error('获取审核详情失败:', error);
      throw error;
    }
  }

  /**
   * 获取用户待审核列表
   * @param reviewerId 审核员ID
   * @param page 页码
   * @param size 页大小
   * @returns 待审核列表
   */
  static async getPendingReviews(
    reviewerId: number, 
    page = 0, 
    size = 10
  ): Promise<ApiResponse<PageResponse<ArticleReview>>> {
    try {
      const response = await axios.get(`${API_BASE_URL}/pending/${reviewerId}`, {
        params: { page, size }
      });
      return response.data;
    } catch (error) {
      console.error('获取待审核列表失败:', error);
      throw error;
    }
  }

  /**
   * 获取所有待审核列表（管理员）
   * @param page 页码
   * @param size 页大小
   * @returns 待审核列表
   */
  static async getAllPendingReviews(
    page = 0, 
    size = 10
  ): Promise<ApiResponse<PageResponse<ArticleReview>>> {
    try {
      const response = await axios.get(`${API_BASE_URL}/pending`, {
        params: { page, size }
      });
      return response.data;
    } catch (error) {
      console.error('获取所有待审核列表失败:', error);
      throw error;
    }
  }

  /**
   * 获取审核统计
   * @param reviewerId 审核员ID
   * @param days 统计天数
   * @returns 统计信息
   */
  static async getReviewStatistics(
    reviewerId: number, 
    days = 30
  ): Promise<ApiResponse<ReviewStatistics>> {
    try {
      const response = await axios.get(`${API_BASE_URL}/statistics/${reviewerId}`, {
        params: { days }
      });
      return response.data;
    } catch (error) {
      console.error('获取审核统计失败:', error);
      throw error;
    }
  }

  /**
   * 批量操作审核
   * @param reviewIds 审核ID列表
   * @param action 操作类型 ('approve' | 'reject' | 'cancel')
   * @param reason 操作原因
   * @returns 操作结果
   */
  static async batchProcessReviews(
    reviewIds: number[], 
    action: 'approve' | 'reject' | 'cancel',
    reason?: string
  ): Promise<ApiResponse> {
    try {
      const response = await axios.post(`${API_BASE_URL}/batch`, {
        reviewIds,
        action,
        reason
      });
      return response.data;
    } catch (error) {
      console.error('批量操作审核失败:', error);
      throw error;
    }
  }

  /**
   * 搜索审核记录
   * @param params 搜索参数
   * @returns 搜索结果
   */
  static async searchReviews(params: {
    keyword?: string;
    status?: string;
    reviewType?: string;
    submitterId?: number;
    reviewerId?: number;
    startDate?: string;
    endDate?: string;
    page?: number;
    size?: number;
  }): Promise<ApiResponse<PageResponse<ArticleReview>>> {
    try {
      const response = await axios.get(`${API_BASE_URL}/search`, { params });
      return response.data;
    } catch (error) {
      console.error('搜索审核记录失败:', error);
      throw error;
    }
  }

  /**
   * 导出审核数据
   * @param params 导出参数
   * @returns 导出文件
   */
  static async exportReviews(params: {
    startDate?: string;
    endDate?: string;
    status?: string;
    reviewType?: string;
    format?: 'xlsx' | 'csv';
  }): Promise<Blob> {
    try {
      const response = await axios.get(`${API_BASE_URL}/export`, {
        params,
        responseType: 'blob'
      });
      return response.data;
    } catch (error) {
      console.error('导出审核数据失败:', error);
      throw error;
    }
  }

  /**
   * 获取审核配置
   * @returns 审核配置信息
   */
  static async getReviewConfig(): Promise<ApiResponse> {
    try {
      const response = await axios.get(`${API_BASE_URL}/config`);
      return response.data;
    } catch (error) {
      console.error('获取审核配置失败:', error);
      throw error;
    }
  }

  /**
   * 更新审核配置
   * @param config 配置参数
   * @returns 更新结果
   */
  static async updateReviewConfig(config: any): Promise<ApiResponse> {
    try {
      const response = await axios.put(`${API_BASE_URL}/config`, config);
      return response.data;
    } catch (error) {
      console.error('更新审核配置失败:', error);
      throw error;
    }
  }
}

/**
 * 审核 API 工具函数
 */
export const reviewApi = {
  // 基础操作
  create: ReviewApiService.createReview,
  assign: ReviewApiService.assignReviewer,
  process: ReviewApiService.processReview,
  getDetail: ReviewApiService.getReviewDetail,
  
  // 查询操作
  getPending: ReviewApiService.getPendingReviews,
  getAllPending: ReviewApiService.getAllPendingReviews,
  getStatistics: ReviewApiService.getReviewStatistics,
  search: ReviewApiService.searchReviews,
  
  // 批量操作
  batchProcess: ReviewApiService.batchProcessReviews,
  export: ReviewApiService.exportReviews,
  
  // 配置管理
  getConfig: ReviewApiService.getReviewConfig,
  updateConfig: ReviewApiService.updateReviewConfig
};

export default reviewApi;
