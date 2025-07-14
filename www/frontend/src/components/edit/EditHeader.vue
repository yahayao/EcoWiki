<!--
  编辑页面头部组件
  
  用于显示文章编辑页面的标题栏，包括页面标题、描述和操作按钮。
  支持新建和编辑两种模式的不同显示内容。
-->
<template>
  <div class="edit-header">
    <div class="title-section">
      <h1>{{ isEditMode ? `编辑 "${currentTitle}"` : `新建 "${currentTitle}"` }}</h1>
      <p class="subtitle">
        {{ isEditMode 
            ? '您正在编辑此页面的当前版本。请在"摘要"框中描述您的更改摘要，并记录您对各条目的编辑摘要，以帮助其他编辑者和未来的自己了解您的更改。'
            : '您正在创建一个新的文章页面。请输入文章标题和内容，在"摘要"框中描述您创建的内容。'
        }}
      </p>
    </div>
    <div class="action-buttons">
      <button @click="$emit('goBack')" class="back-btn">
        <span class="back-icon">←</span>
        {{ isEditMode ? '返回文章' : '取消创建' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  isEditMode: boolean
  currentTitle: string
}

// 定义事件
defineEmits<{
  goBack: []
}>()

// 定义 props
defineProps<Props>()
</script>

<style scoped>
.edit-header {
    display:flex;
    justify-content:space-between;
    align-items:flex-start;
    margin-bottom:24px;
    padding:24px;
    background:white;
    border-radius:12px;
    box-shadow:0 2px 10px rgba(0,0,0,0.05);
}

.title-section h1 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 0.5rem;
}

.subtitle {
  font-size: 0.875rem;
  color: #6b7280;
  max-width: 48rem;
  line-height: 1.5;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 25px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px rgba(102, 126, 234, 0.3);
}

.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
}

.back-icon {
  font-size: 1.125rem;
}
</style>
