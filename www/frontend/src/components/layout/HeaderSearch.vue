<!--
  头部搜索组件
  
  该组件提供网站头部的搜索功能，允许用户快速搜索站内的知识内容。
  采用现代化的UI设计，支持键盘操作和鼠标点击两种搜索方式。
  
  主要功能：
  - 关键词搜索：支持输入搜索关键词进行内容检索
  - 多种触发方式：支持回车键和点击按钮两种搜索触发方式
  - 实时验证：自动过滤空白搜索词，确保搜索有效性
  - 事件传递：通过emit向父组件传递搜索事件
  - 响应式设计：适配不同屏幕尺寸的显示效果
  
  交互设计：
  - 占位符提示：清晰的搜索提示文本
  - 图标按钮：直观的搜索图标视觉引导
  - 键盘操作：回车键快捷搜索支持
  - 输入验证：自动过滤空白和无效输入
  
  视觉特点：
  - 现代化设计：圆角边框和阴影效果
  - 图标集成：使用SVG图标保证清晰度
  - 悬浮反馈：按钮悬浮状态的颜色变化
  - 响应式布局：自适应容器宽度
  
  技术实现：
  - Vue 3 Composition API
  - TypeScript 类型安全
  - 事件发射机制（defineEmits）
  - 双向数据绑定（v-model）
  - 键盘事件监听（@keyup.enter）
  
  使用场景：
  - 网站头部导航栏
  - 全站内容搜索入口
  - 快速查找功能
  - 用户内容发现
  
  扩展性：
  - 支持搜索建议和自动完成
  - 可添加搜索历史记录
  - 支持高级搜索选项
  - 可集成搜索结果预览
  
  @author EcoWiki Team
  @version 1.0.0
  @since 2024-01-01
-->
<template>
  <!-- 搜索栏容器 -->
  <div class="search-container">
    <div class="search-wrapper">
      <!-- 搜索输入框 -->
      <input 
        type="text" 
        class="search-input" 
        placeholder="搜索知识内容..." 
        v-model="searchTerm"
        @keyup.enter="handleSearch"
      />
      
      <!-- 搜索按钮 -->
      <button class="search-button" @click="handleSearch">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M21 21L16.514 16.506L21 21ZM19 10.5C19 15.194 15.194 19 10.5 19C5.806 19 2 15.194 2 10.5C2 5.806 5.806 2 10.5 2C15.194 2 19 5.806 19 10.5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 头部搜索组件脚本
 * 
 * 该脚本处理搜索输入的状态管理和事件处理。
 * 提供搜索词验证和事件发射功能。
 */

import { ref } from 'vue'

// 定义组件事件类型
const emit = defineEmits<{
  search: [term: string]  // 搜索事件，传递搜索关键词
}>()

// 响应式数据定义
const searchTerm = ref('')  // 当前搜索关键词

/**
 * 处理搜索操作
 * 验证搜索词有效性并发射搜索事件
 */
const handleSearch = () => {
  // 过滤空白字符，确保搜索词有效
  if (searchTerm.value.trim()) {
    emit('search', searchTerm.value.trim())
  }
}
</script>

<style scoped>
/* 搜索栏 */
.search-container {
  flex: 1;
  max-width: 500px;
  margin: 0 20px;
}

.search-wrapper {
  position: relative;
  width: 100%;
}

.search-input {
  width: 100%;
  padding: 12px 50px 12px 20px;
  border: none;
  border-radius: 25px;
  font-size: 1rem;
  outline: none;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.search-input:focus {
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.15);
  background: white;
}

.search-input::placeholder {
  color: #718096;
}

.search-button {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  color: #667eea;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-button:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: translateY(-50%) scale(1.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-container {
    max-width: 100%;
    margin: 0;
  }
}
</style>
