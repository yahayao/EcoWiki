<!-- UserArticle.vue -->
<template>
  <div class="article-container">
    <h2 class="section-title">文章管理</h2>
    
    <div class="article-tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab.key"
        class="tab-btn"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </button>
    </div>
    
    <div class="article-list">
      <div v-if="activeTab === 'favorites'" class="article-grid">
        <div class="article-card">
          <h4>全球变暖的影响与对策</h4>
          <p class="article-meta">收藏于 2025-07-10</p>
          <button class="btn btn-sm btn-outline">取消收藏</button>
        </div>
        <div class="article-card">
          <h4>可再生能源技术</h4>
          <p class="article-meta">收藏于 2025-07-05</p>
          <button class="btn btn-sm btn-outline">取消收藏</button>
        </div>
      </div>
      
      <div v-else-if="activeTab === 'created'" class="article-grid">
        <div class="article-card">
          <h4>垃圾分类指南</h4>
          <p class="article-meta">创建于 2025-06-20 · 1.2k 阅读</p>
          <button class="btn btn-sm">编辑</button>
        </div>
        <div class="article-card">
          <h4>海洋保护行动</h4>
          <p class="article-meta">创建于 2025-05-15 · 856 阅读</p>
          <button class="btn btn-sm">编辑</button>
        </div>
      </div>
      
      <div v-else-if="activeTab === 'drafts'" class="article-grid">
        <div class="article-card">
          <h4>可持续农业实践</h4>
          <p class="article-meta">最后保存于 2025-07-13</p>
          <button class="btn btn-sm">继续编辑</button>
          <button class="btn btn-sm btn-danger">删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const tabs = [
  { key: 'favorites', label: '收藏文章' },
  { key: 'created', label: '我创建的' },
  { key: 'drafts', label: '草稿箱' }
]

const activeTab = ref('favorites')
</script>

<style scoped>
.article-container {
  padding: 32px;
}

.section-title {
  margin: 0 0 24px 0;
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
}

.article-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  border-bottom: 1px solid #e9ecef;
}

.tab-btn {
  padding: 12px 24px;
  border: none;
  background: none;
  font-size: 16px;
  cursor: pointer;
  color: #6c757d;
  border-bottom: 2px solid transparent;
}

.tab-btn.active {
  color: #43a047;
  border-bottom-color: #43a047;
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.article-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
}

.article-card h4 {
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.article-meta {
  color: #6c757d;
  font-size: 14px;
  margin-bottom: 12px;
}

.btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.btn-outline {
  border: 1px solid #43a047;
  color: #43a047;
  background: white;
}

.btn-danger {
  background: #dc3545;
  color: white;
}

@media (max-width: 768px) {
  .article-grid {
    grid-template-columns: 1fr;
  }
}
</style>