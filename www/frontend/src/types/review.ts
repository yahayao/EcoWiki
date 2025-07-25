// 文章审核相关的 TypeScript 接口定义

/**
 * 审核类型枚举
 */
export enum ReviewType {
  CREATE = 'CREATE',
  UPDATE = 'UPDATE',
  DELETE = 'DELETE'
}

/**
 * 审核状态枚举
 */
export enum ReviewStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  CANCELLED = 'CANCELLED'
}

/**
 * 分配状态枚举
 */
export enum AssignmentStatus {
  ACTIVE = 'ACTIVE',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED',
  CANCELLED = 'CANCELLED',
  COMPLETED = 'COMPLETED'
}

/**
 * 操作类型枚举
 */
export enum ActionType {
  CREATED = 'CREATED',
  ASSIGNED = 'ASSIGNED',
  ACCEPTED = 'ACCEPTED',
  REJECTED_ASSIGNMENT = 'REJECTED_ASSIGNMENT',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  CANCELLED = 'CANCELLED',
  MODIFIED = 'MODIFIED',
  REASSIGNED = 'REASSIGNED',
  DEADLINE_EXTENDED = 'DEADLINE_EXTENDED',
  PRIORITY_CHANGED = 'PRIORITY_CHANGED',
  COMMENT_ADDED = 'COMMENT_ADDED'
}

/**
 * 文章审核接口
 */
export interface ArticleReview {
  reviewId: number;
  articleId: number;
  reviewerId?: number;
  submitterId: number;
  reviewType: ReviewType;
  status: ReviewStatus;
  contentSnapshot?: string;
  reviewReason?: string;
  submitTime: string;
  reviewTime?: string;
  autoPublish: boolean;
  priorityLevel: number;
  reviewDeadline?: string;
}

/**
 * 审核权限配置接口
 */
export interface ReviewPermissionConfig {
  configId: number;
  roleName: string;
  reviewType: ReviewType;
  canAssignReviewer: boolean;
  canSelfReview: boolean;
  autoAssignment: boolean;
  weight: number;
  maxConcurrentReviews: number;
  priorityLevel: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * 审核员分配接口
 */
export interface ReviewerAssignment {
  assignmentId: number;
  reviewId: number;
  reviewerId: number;
  assignerId?: number;
  status: AssignmentStatus;
  assignedAt: string;
  acceptedAt?: string;
  rejectedAt?: string;
  assignmentReason?: string;
  autoAssigned: boolean;
  weightScore?: number;
  expectedCompletionTime?: string;
}

/**
 * 审核历史接口
 */
export interface ReviewHistory {
  historyId: number;
  reviewId: number;
  operatorId: number;
  actionType: ActionType;
  oldStatus?: ReviewStatus;
  newStatus?: ReviewStatus;
  actionDescription?: string;
  actionTime: string;
  ipAddress?: string;
  userAgent?: string;
  extraData?: string;
}

/**
 * 创建审核请求接口
 */
export interface CreateReviewRequest {
  articleId: number;
  submitterId: number;
  reviewType: ReviewType;
  contentSnapshot?: string;
  autoPublish?: boolean;
}

/**
 * 分配审核员请求接口
 */
export interface AssignReviewerRequest {
  reviewerId: number;
  assignerId: number;
  reason?: string;
}

/**
 * 处理审核请求接口
 */
export interface ProcessReviewRequest {
  reviewerId: number;
  approved: boolean;
  reason?: string;
}

/**
 * 审核详情接口
 */
export interface ReviewDetail {
  review: ArticleReview;
  history: ReviewHistory[];
}

/**
 * 审核统计接口
 */
export interface ReviewStatistics {
  totalReviews: number;
  approvedCount: number;
  rejectedCount: number;
  avgReviewTimeHours: number;
  pendingCount: number;
}

/**
 * 分页响应接口
 */
export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  numberOfElements: number;
  size: number;
  number: number;
  sort: {
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
  };
  empty: boolean;
}

/**
 * API 响应接口
 */
export interface ApiResponse<T = any> {
  success: boolean;
  message?: string;
  data?: T;
}

/**
 * 审核类型选项
 */
export const REVIEW_TYPE_OPTIONS = [
  { value: ReviewType.CREATE, label: '创建文章', description: '新建文章审核' },
  { value: ReviewType.UPDATE, label: '更新文章', description: '文章修改审核' },
  { value: ReviewType.DELETE, label: '删除文章', description: '文章删除审核' }
];

/**
 * 审核状态选项
 */
export const REVIEW_STATUS_OPTIONS = [
  { value: ReviewStatus.PENDING, label: '待审核', color: 'orange' },
  { value: ReviewStatus.APPROVED, label: '审核通过', color: 'green' },
  { value: ReviewStatus.REJECTED, label: '审核拒绝', color: 'red' },
  { value: ReviewStatus.CANCELLED, label: '已取消', color: 'gray' }
];

/**
 * 优先级选项
 */
export const PRIORITY_OPTIONS = [
  { value: 1, label: '低', color: 'blue' },
  { value: 2, label: '普通', color: 'green' },
  { value: 3, label: '高', color: 'orange' },
  { value: 4, label: '紧急', color: 'red' },
  { value: 5, label: '最高', color: 'purple' }
];
