<template>
  <div class="review-dashboard">
    <div class="header-section">
      <h2>审核管理面板</h2>
      <button 
        class="btn btn-primary"
        @click="showCreateDialog = true"
        v-if="canCreateReview"
      >
        创建审核
      </button>
    </div>

    <!-- 统计信息 -->
    <div class="statistics-grid">
      <div class="stat-card">
        <div class="stat-number">{{ statistics.pendingCount || 0 }}</div>
        <div class="stat-label">待审核</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ statistics.totalReviews || 0 }}</div>
        <div class="stat-label">总审核数</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ statistics.approvedCount || 0 }}</div>
        <div class="stat-label">已通过</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ statistics.rejectedCount || 0 }}</div>
        <div class="stat-label">已拒绝</div>
      </div>
    </div>

    <!-- 筛选和操作 -->
    <div class="filter-section">
      <select v-model="filterStatus" class="filter-select">
        <option value="">所有状态</option>
        <option
          v-for="option in REVIEW_STATUS_OPTIONS"
          :key="option.value"
          :value="option.value"
        >
          {{ option.label }}
        </option>
      </select>
      <button @click="loadReviews" :disabled="loading" class="btn btn-secondary">
        {{ loading ? '加载中...' : '刷新' }}
      </button>
    </div>

    <!-- 审核列表 -->
    <div class="review-table">
      <table>
        <thead>
          <tr>
            <th>审核ID</th>
            <th>文章ID</th>
            <th>审核类型</th>
            <th>状态</th>
            <th>优先级</th>
            <th>提交时间</th>
            <th>截止时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="review in reviews" :key="review.reviewId">
            <td>{{ review.reviewId }}</td>
            <td>{{ review.articleId }}</td>
            <td>
              <span :class="'tag tag-' + getReviewTypeTag(review.reviewType)">
                {{ getReviewTypeLabel(review.reviewType) }}
              </span>
            </td>
            <td>
              <span :class="'tag tag-' + getStatusTag(review.status)">
                {{ getStatusLabel(review.status) }}
              </span>
            </td>
            <td>
              <span :class="'tag tag-' + getPriorityTag(review.priorityLevel)">
                {{ getPriorityLabel(review.priorityLevel) }}
              </span>
            </td>
            <td>{{ formatDateTime(review.submitTime) }}</td>
            <td :class="{ 'overdue': isOverdue(review) }">
              {{ formatDateTime(review.reviewDeadline) }}
            </td>
            <td class="actions">
              <button 
                class="btn btn-small"
                @click="viewDetail(review)"
              >
                详情
              </button>
              <button 
                class="btn btn-small btn-primary"
                @click="processReview(review)"
                v-if="canProcess(review)"
              >
                处理
              </button>
              <button 
                class="btn btn-small btn-warning"
                @click="assignReviewer(review)"
                v-if="canAssign(review)"
              >
                分配
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <button 
        @click="prevPage" 
        :disabled="currentPage <= 1"
        class="btn btn-secondary"
      >
        上一页
      </button>
      <span class="page-info">
        第 {{ currentPage }} 页，共 {{ totalPages }} 页，总计 {{ total }} 条
      </span>
      <button 
        @click="nextPage" 
        :disabled="currentPage >= totalPages"
        class="btn btn-secondary"
      >
        下一页
      </button>
    </div>

    <!-- 创建审核对话框 -->
    <div v-if="showCreateDialog" class="dialog-overlay" @click="closeCreateDialog">
      <div class="dialog" @click.stop>
        <div class="dialog-header">
          <h3>创建审核申请</h3>
          <button @click="closeCreateDialog" class="close-btn">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>文章ID *</label>
            <input 
              type="number" 
              v-model="createForm.articleId" 
              placeholder="请输入文章ID"
              min="1"
            />
          </div>
          <div class="form-group">
            <label>审核类型 *</label>
            <select v-model="createForm.reviewType">
              <option value="">请选择审核类型</option>
              <option
                v-for="option in REVIEW_TYPE_OPTIONS"
                :key="option.value"
                :value="option.value"
              >
                {{ option.label }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>内容快照</label>
            <textarea 
              v-model="createForm.contentSnapshot"
              placeholder="请输入内容快照（可选）"
              rows="4"
            ></textarea>
          </div>
          <div class="form-group">
            <label>
              <input 
                type="checkbox" 
                v-model="createForm.autoPublish"
              />
              审核通过后自动发布
            </label>
          </div>
        </div>
        <div class="dialog-footer">
          <button @click="closeCreateDialog" class="btn btn-secondary">取消</button>
          <button @click="handleCreateSubmit" class="btn btn-primary" :disabled="submitting">
            {{ submitting ? '创建中...' : '创建审核' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 处理审核对话框 -->
    <div v-if="showProcessDialog" class="dialog-overlay" @click="closeProcessDialog">
      <div class="dialog" @click.stop>
        <div class="dialog-header">
          <h3>处理审核</h3>
          <button @click="closeProcessDialog" class="close-btn">&times;</button>
        </div>
        <div class="dialog-body">
          <div class="review-info">
            <p><strong>审核ID:</strong> {{ selectedReview?.reviewId }}</p>
            <p><strong>文章ID:</strong> {{ selectedReview?.articleId }}</p>
            <p><strong>审核类型:</strong> {{ getReviewTypeLabel(selectedReview?.reviewType || '') }}</p>
          </div>
          <div class="form-group">
            <label>审核结果 *</label>
            <div class="radio-group">
              <label>
                <input type="radio" v-model="processForm.approved" :value="true" />
                通过
              </label>
              <label>
                <input type="radio" v-model="processForm.approved" :value="false" />
                拒绝
              </label>
            </div>
          </div>
          <div class="form-group">
            <label>审核理由</label>
            <textarea 
              v-model="processForm.reason"
              placeholder="请输入审核理由"
              rows="3"
            ></textarea>
          </div>
        </div>
        <div class="dialog-footer">
          <button @click="closeProcessDialog" class="btn btn-secondary">取消</button>
          <button @click="handleProcessSubmit" class="btn btn-primary" :disabled="submitting">
            {{ submitting ? '处理中...' : '提交审核' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { reviewApi } from '@/api/review';
import type { ArticleReview, ReviewStatistics } from '@/types/review';
import { REVIEW_STATUS_OPTIONS, REVIEW_TYPE_OPTIONS, PRIORITY_OPTIONS, ReviewType } from '@/types/review';

// 响应式数据
const loading = ref(false);
const submitting = ref(false);
const reviews = ref<ArticleReview[]>([]);
const selectedReview = ref<ArticleReview | null>(null);
const statistics = reactive<ReviewStatistics>({
  totalReviews: 0,
  approvedCount: 0,
  rejectedCount: 0,
  avgReviewTimeHours: 0,
  pendingCount: 0
});

// 对话框状态
const showCreateDialog = ref(false);
const showProcessDialog = ref(false);

// 表单数据
const createForm = reactive({
  articleId: null as number | null,
  reviewType: '' as ReviewType | '',
  contentSnapshot: '',
  autoPublish: false,
  submitterId: 1 // 临时硬编码
});

const processForm = reactive({
  approved: null as boolean | null,
  reason: '',
  reviewerId: 1 // 临时硬编码
});

// 筛选和分页
const filterStatus = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 计算属性
const totalPages = computed(() => Math.ceil(total.value / pageSize.value));

const canCreateReview = computed(() => {
  return true; // 根据实际权限判断
});

// 生命周期
onMounted(() => {
  loadReviews();
  loadStatistics();
});

// 方法
const loadReviews = async () => {
  try {
    loading.value = true;
    const response = await reviewApi.getAllPending(currentPage.value - 1, pageSize.value);
    
    if (response.success && response.data) {
      reviews.value = response.data.content;
      total.value = response.data.totalElements;
    }
  } catch (error) {
    console.error('加载审核列表失败:', error);
    alert('加载审核列表失败');
  } finally {
    loading.value = false;
  }
};

const loadStatistics = async () => {
  try {
    const userId = 1; // 临时硬编码
    const response = await reviewApi.getStatistics(userId);
    
    if (response.success && response.data) {
      Object.assign(statistics, response.data);
    }
  } catch (error) {
    console.error('加载统计信息失败:', error);
  }
};

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
    loadReviews();
  }
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
    loadReviews();
  }
};

const viewDetail = (review: ArticleReview) => {
  alert(`查看审核详情: ${review.reviewId}`);
};

const processReview = (review: ArticleReview) => {
  selectedReview.value = review;
  processForm.approved = null;
  processForm.reason = '';
  showProcessDialog.value = true;
};

const assignReviewer = (review: ArticleReview) => {
  alert(`分配审核员: ${review.reviewId}`);
};

const canProcess = (review: ArticleReview) => {
  return review.status === 'PENDING' && review.reviewerId;
};

const canAssign = (review: ArticleReview) => {
  return review.status === 'PENDING' && !review.reviewerId;
};

// 对话框操作
const closeCreateDialog = () => {
  showCreateDialog.value = false;
  createForm.articleId = null;
  createForm.reviewType = '';
  createForm.contentSnapshot = '';
  createForm.autoPublish = false;
};

const closeProcessDialog = () => {
  showProcessDialog.value = false;
  selectedReview.value = null;
};

const handleCreateSubmit = async () => {
  if (!createForm.articleId || !createForm.reviewType) {
    alert('请填写必填项');
    return;
  }

  try {
    submitting.value = true;
    const response = await reviewApi.create({
      articleId: createForm.articleId,
      submitterId: createForm.submitterId,
      reviewType: createForm.reviewType as ReviewType,
      contentSnapshot: createForm.contentSnapshot || undefined,
      autoPublish: createForm.autoPublish
    });

    if (response.success) {
      alert('审核创建成功');
      closeCreateDialog();
      loadReviews();
      loadStatistics();
    } else {
      throw new Error(response.message || '创建失败');
    }
  } catch (error: any) {
    console.error('创建审核申请失败:', error);
    alert(error.message || '创建审核申请失败');
  } finally {
    submitting.value = false;
  }
};

const handleProcessSubmit = async () => {
  if (!selectedReview.value || processForm.approved === null) {
    alert('请选择审核结果');
    return;
  }

  try {
    submitting.value = true;
    const response = await reviewApi.process(selectedReview.value.reviewId, {
      reviewerId: processForm.reviewerId,
      approved: processForm.approved,
      reason: processForm.reason || undefined
    });

    if (response.success) {
      alert('审核处理成功');
      closeProcessDialog();
      loadReviews();
      loadStatistics();
    } else {
      throw new Error(response.message || '处理失败');
    }
  } catch (error: any) {
    console.error('处理审核失败:', error);
    alert(error.message || '处理审核失败');
  } finally {
    submitting.value = false;
  }
};

// 工具函数
const formatDateTime = (dateTime: string | undefined) => {
  if (!dateTime) return '-';
  return new Date(dateTime).toLocaleString('zh-CN');
};

const isOverdue = (review: ArticleReview) => {
  if (!review.reviewDeadline) return false;
  return new Date(review.reviewDeadline) < new Date();
};

const getReviewTypeLabel = (type: string) => {
  const option = REVIEW_TYPE_OPTIONS.find(opt => opt.value === type);
  return option?.label || type;
};

const getReviewTypeTag = (type: string) => {
  switch (type) {
    case 'CREATE': return 'default';
    case 'UPDATE': return 'warning';
    case 'DELETE': return 'danger';
    default: return 'default';
  }
};

const getStatusLabel = (status: string) => {
  const option = REVIEW_STATUS_OPTIONS.find(opt => opt.value === status);
  return option?.label || status;
};

const getStatusTag = (status: string) => {
  switch (status) {
    case 'PENDING': return 'warning';
    case 'APPROVED': return 'success';
    case 'REJECTED': return 'danger';
    case 'CANCELLED': return 'default';
    default: return 'default';
  }
};

const getPriorityLabel = (level: number) => {
  const option = PRIORITY_OPTIONS.find(opt => opt.value === level);
  return option?.label || level.toString();
};

const getPriorityTag = (level: number) => {
  switch (level) {
    case 1: return 'default';
    case 2: return 'success';
    case 3: return 'warning';
    case 4: return 'danger';
    case 5: return 'danger';
    default: return 'default';
  }
};
</script>

<style scoped>
.review-dashboard {
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-section h2 {
  margin: 0;
  color: #333;
}

.statistics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.filter-section {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: white;
}

.review-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.review-table table {
  width: 100%;
  border-collapse: collapse;
}

.review-table th,
.review-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ebeef5;
}

.review-table th {
  background: #f5f7fa;
  font-weight: 600;
  color: #333;
}

.actions {
  display: flex;
  gap: 8px;
}

.tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.tag-default { background: #f4f4f5; color: #909399; }
.tag-success { background: #f0f9ff; color: #67c23a; }
.tag-warning { background: #fdf6ec; color: #e6a23c; }
.tag-danger { background: #fef0f0; color: #f56c6c; }

.overdue {
  color: #f56c6c;
  font-weight: bold;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
}

.page-info {
  color: #666;
  font-size: 14px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: #409eff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #337ecc;
}

.btn-secondary {
  background: #909399;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background: #73767a;
}

.btn-warning {
  background: #e6a23c;
  color: white;
}

.btn-small {
  padding: 4px 8px;
  font-size: 12px;
}

.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.dialog {
  background: white;
  border-radius: 8px;
  min-width: 500px;
  max-width: 800px;
  max-height: 80vh;
  overflow-y: auto;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

.dialog-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.dialog-body {
  padding: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 20px;
  border-top: 1px solid #ebeef5;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #409eff;
}

.radio-group {
  display: flex;
  gap: 20px;
}

.radio-group label {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 0;
}

.review-info {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.review-info p {
  margin: 5px 0;
}
</style>
