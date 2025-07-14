<!--
  编辑预览组件
  
  在编辑器上方显示文章预览，使用 ArticleContent 组件进行渲染。
  类似于 MediaWiki 的预览方式。
-->
<template>
  <div class="edit-preview" v-if="showPreview">
    <div class="preview-header">
      <div class="preview-title">
        <h3>📖 预览</h3>
        <p>这是您的文章在发布后的样子</p>
      </div>
      <button @click="$emit('close')" class="close-preview-btn">
        <span>✕</span>
        <span>关闭预览</span>
      </button>
    </div>
    
    <div class="preview-container">
      <ArticleContent :article="previewArticle" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ArticleContent from '../article/ArticleContent.vue'
import type { Article } from '../../api/article'

interface Props {
  showPreview: boolean
  title: string
  content: string
  category: string
  tags: string
  author: string
}

// 定义 props
const props = defineProps<Props>()

// 定义事件
defineEmits<{
  close: []
}>()

// 创建预览用的虚拟文章对象
const previewArticle = computed((): Article => {
  return {
    articleId: 0, // 预览时使用虚拟ID
    title: props.title || '预览文章',
    content: props.content,
    category: props.category || '未分类',
    tags: props.tags,
    author: props.author,
    publishDate: new Date().toISOString(),
    updateTime: new Date().toISOString(),
    views: 0,
    likes: 0,
    comments: 0
  }
})
</script>

<style scoped>
.edit-preview {
  margin-bottom: 2rem;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.preview-title h3 {
  margin: 0 0 0.25rem 0;
  font-size: 1.25rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.preview-title p {
  margin: 0;
  font-size: 0.875rem;
  opacity: 0.9;
}

.close-preview-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.close-preview-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
}

.preview-container {
  padding: 2rem;
  background: white;
}

/* 调整 ArticleContent 在预览中的显示 */
.preview-container :deep(.article-content) {
  margin: 0;
}

.preview-container :deep(.table-of-contents-sidebar) {
  display: none; /* 在预览中隐藏目录侧边栏 */
}

.preview-container :deep(.article-actions) {
  display: none; /* 在预览中隐藏操作按钮 */
}

.preview-container :deep(.floating-action-buttons) {
  display: none; /* 在预览中隐藏浮动按钮 */
}
</style>
