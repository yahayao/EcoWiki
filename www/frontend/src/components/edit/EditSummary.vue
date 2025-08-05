<!--
/**
 * 编辑摘要和保存组件
 * 
 * 功能包括：
 * - 编辑摘要信息输入
 * - 文章元数据设置
 * - 保存和发布操作
 * - 编辑历史记录
 * - 版本控制信息
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
-->
<template>
  <div class="edit-summary-section">
    <div class="summary-row">
      <div class="summary-group">
        <label for="edit-summary">摘要（描述你的更改）：</label>
        <input 
          id="edit-summary" 
          :value="editSummary"
          @input="updateEditSummary"
          type="text" 
          class="summary-input"
          placeholder="例如：修正错别字、添加新内容、更新信息等..." 
          maxlength="200" 
        />
      </div>
    </div>

    <!-- 其他文章信息 -->
    <div class="article-meta">
      <div class="meta-row">
        <div class="meta-group">
          <label for="category">分类：</label>
          <select id="category" :value="category" @change="updateCategory" class="form-select">
            <option value="">选择分类</option>
            <option value="环保">环保</option>
            <option value="技术">技术</option>
            <option value="教育">教育</option>
            <option value="健康">健康</option>
            <option value="学术研究">学术研究</option>
            <option value="其他">其他</option>
          </select>
        </div>
      </div>
      
      <div class="meta-row" v-if="displayTags.length > 0">
        <div class="meta-group tags-display">
          <label>自动生成的标签：</label>
          <div class="tags-container">
            <span v-for="tag in displayTags" :key="tag" class="tag-badge">{{ tag }}</span>
          </div>
          <small class="help-text">💡 通过[[分类:xxx]]语法自动生成</small>
        </div>
      </div>
    </div>

    <!-- 保存按钮区域 -->
    <div class="save-section">
      <div class="save-buttons">
        <button @click="$emit('save')" class="save-btn primary" :disabled="saving || !canSave">
          <span v-if="saving" class="loading-spinner"></span>
          {{ saving 
              ? (isEditMode ? '保存更改...' : '发表文章...')
              : (isEditMode ? '保存更改' : '发表文章')
          }}
        </button>
        <button @click="$emit('togglePreview')" class="preview-btn secondary">
          {{ showPreview ? '隐藏预览' : '显示预览' }}
        </button>
        <button @click="$emit('cancel')" class="cancel-btn">取消</button>
      </div>
      
      <div class="save-help">
        <p>{{ isEditMode ? '保存后您的更改将立即生效' : '发表后文章将对所有用户可见' }}。请确保内容准确无误。</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  editSummary: string
  category: string
  displayTags: string[]
  saving: boolean
  canSave: boolean
  isEditMode: boolean
  showPreview: boolean
}

// 定义 props
defineProps<Props>()

// 定义和获取 emit 函数
const emit = defineEmits<{
  'update:editSummary': [value: string]
  'update:category': [value: string]
  save: []
  togglePreview: []
  cancel: []
}>()

// 更新编辑摘要
const updateEditSummary = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('update:editSummary', target.value)
}

// 更新分类
const updateCategory = (event: Event) => {
  const target = event.target as HTMLSelectElement
  emit('update:category', target.value)
}
</script>

<style scoped>
.edit-summary-section {
  margin-top: 2rem;
  padding: 2rem;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.summary-row {
  margin-bottom: 1.5rem;
}

.summary-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.summary-group label {
  font-weight: 500;
  color: #374151;
  font-size: 0.875rem;
}

.summary-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  transition: border-color 0.2s ease;
}

.summary-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.article-meta {
  margin-bottom: 1.5rem;
}

.meta-row {
  margin-bottom: 1rem;
}

.meta-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.meta-group label {
  font-weight: 500;
  color: #374151;
  font-size: 0.875rem;
}

.form-select {
  padding: 0.5rem 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 0.375rem;
  font-size: 0.875rem;
  background-color: white;
  cursor: pointer;
  max-width: 200px;
}

.form-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.tags-display {
  gap: 0.75rem;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.375rem 0.875rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.3);
}

.help-text {
  color: #6b7280;
  font-size: 0.75rem;
}

.save-section {
  border-top: 1px solid #e5e7eb;
  padding-top: 1.5rem;
}

.save-buttons {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.save-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.875rem 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 25px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 3px 12px rgba(102, 126, 234, 0.3);
}

.save-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.save-btn:disabled {
  background: linear-gradient(135deg, #9ca3af 0%, #6b7280 100%);
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 2px 4px rgba(156, 163, 175, 0.2);
}

.preview-btn {
  padding: 0.875rem 1.75rem;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
}

.preview-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
}

.cancel-btn {
  padding: 0.875rem 1.75rem;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 20px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.cancel-btn:hover {
  background: linear-gradient(135deg, #e2e8f0 0%, #cbd5e1 100%);
  color: #1f2937;
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
}

.save-help {
  color: #6b7280;
  font-size: 0.875rem;
}

.loading-spinner {
  width: 1rem;
  height: 1rem;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
