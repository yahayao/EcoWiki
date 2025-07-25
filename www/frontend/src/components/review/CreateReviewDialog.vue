<template>
  <div v-if="visible" class="dialog-overlay" @click="handleOverlayClick">
    <div class="dialog" @click.stop>
      <div class="dialog-header">
        <h3>创建审核申请</h3>
        <button class="close-btn" @click="handleClose">&times;</button>
      </div>
      
      <div class="dialog-body">
        <form ref="formRef" class="review-form" @submit.prevent="handleSubmit">
          <div class="form-group">
            <label for="articleId">文章ID *</label>
            <input
              id="articleId"
              v-model.number="form.articleId"
              type="number"
              min="1"
              placeholder="请输入文章ID"
              required
            />
            <span v-if="errors.articleId" class="error-text">{{ errors.articleId }}</span>
          </div>

          <div class="form-group">
            <label for="reviewType">审核类型 *</label>
            <select id="reviewType" v-model="form.reviewType" required>
              <option value="">请选择审核类型</option>
              <option
                v-for="option in REVIEW_TYPE_OPTIONS"
                :key="option.value"
                :value="option.value"
              >
                {{ option.label }} - {{ option.description }}
              </option>
            </select>
            <span v-if="errors.reviewType" class="error-text">{{ errors.reviewType }}</span>
          </div>

          <div class="form-group">
            <label for="contentSnapshot">内容快照</label>
            <textarea
              id="contentSnapshot"
              v-model="form.contentSnapshot"
              rows="4"
              placeholder="请输入内容快照（可选）"
              maxlength="1000"
            ></textarea>
            <div class="char-count">{{ form.contentSnapshot.length }}/1000</div>
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input
                v-model="form.autoPublish"
                type="checkbox"
              />
              <span class="checkmark"></span>
              审核通过后自动发布
            </label>
          </div>

          <div class="form-group">
            <div class="info-box">
              <h4>审核说明</h4>
              <ul>
                <li>创建文章：新建文章需要经过审核才能发布</li>
                <li>更新文章：修改已发布文章需要审核变更内容</li>
                <li>删除文章：删除文章需要管理员审核确认</li>
              </ul>
            </div>
          </div>
        </form>
      </div>

      <div class="dialog-footer">
        <button type="button" class="btn btn-secondary" @click="handleClose">
          取消
        </button>
        <button 
          type="button" 
          class="btn btn-primary" 
          @click="handleSubmit" 
          :disabled="submitting"
        >
          {{ submitting ? '创建中...' : '创建审核' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { reviewApi } from '@/api/review';
import { REVIEW_TYPE_OPTIONS, ReviewType, type CreateReviewRequest, type ApiResponse, type ArticleReview } from '@/types/review';

// Props
interface Props {
  modelValue: boolean;
}

// Emits
interface Emits {
  (e: 'update:modelValue', value: boolean): void;
  (e: 'success'): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

// 响应式数据
const formRef = ref<HTMLFormElement>();
const submitting = ref(false);

const form = reactive({
  articleId: null as number | null,
  reviewType: '' as ReviewType | '',
  contentSnapshot: '',
  autoPublish: false,
  submitterId: 1 // 临时硬编码，实际应该从用户状态获取
});

// 错误信息
const errors = reactive({
  articleId: '',
  reviewType: ''
});

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// 方法
const handleOverlayClick = (event: Event) => {
  if (event.target === event.currentTarget) {
    handleClose();
  }
};

const handleClose = () => {
  resetForm();
  visible.value = false;
};

const resetForm = () => {
  form.articleId = null;
  form.reviewType = '';
  form.contentSnapshot = '';
  form.autoPublish = false;
  errors.articleId = '';
  errors.reviewType = '';
};

const validateForm = (): boolean => {
  let isValid = true;
  
  // 重置错误信息
  errors.articleId = '';
  errors.reviewType = '';
  
  // 验证文章ID
  if (!form.articleId || form.articleId <= 0) {
    errors.articleId = '请输入有效的文章ID';
    isValid = false;
  }
  
  // 验证审核类型
  if (!form.reviewType) {
    errors.reviewType = '请选择审核类型';
    isValid = false;
  }
  
  return isValid;
};

const handleSubmit = async () => {
  if (!validateForm()) {
    return;
  }

  try {
    submitting.value = true;

    const response: ApiResponse<ArticleReview> = await reviewApi.create({
      articleId: form.articleId!,
      submitterId: form.submitterId,
      reviewType: form.reviewType as ReviewType,
      contentSnapshot: form.contentSnapshot || undefined,
      autoPublish: form.autoPublish
    });

    if (response.success) {
      emit('success');
      handleClose();
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
</script>

<style scoped>
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
  width: 600px;
  max-width: 90vw;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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
  font-size: 18px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
}

.close-btn:hover {
  background: #f5f5f5;
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

.review-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #409eff;
}

.form-group input[type="number"] {
  min-width: 0;
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.char-count {
  font-size: 12px;
  color: #999;
  text-align: right;
  margin-top: 4px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 14px;
  margin-bottom: 0 !important;
}

.checkbox-label input[type="checkbox"] {
  margin-right: 8px;
  width: auto;
}

.error-text {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 4px;
}

.info-box {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 4px;
  padding: 16px;
}

.info-box h4 {
  margin: 0 0 12px 0;
  color: #409eff;
  font-size: 14px;
}

.info-box ul {
  margin: 0;
  padding-left: 20px;
}

.info-box li {
  margin: 4px 0;
  font-size: 13px;
  color: #666;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  min-width: 80px;
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
</style>
