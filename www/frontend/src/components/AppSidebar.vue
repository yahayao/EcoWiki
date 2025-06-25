<template>
  <!-- 侧边导航 -->
  <aside class="sidebar">
    <h2 class="sidebar-title">知识分类</h2>
    <nav class="categories-nav">
      <ul class="category-list">
        <li class="category-item">
          <a href="#" class="category-link">
            <span class="category-icon">📚</span>
            学术研究
          </a>
        </li>
        <li class="category-item">
          <a href="#" class="category-link">
            <span class="category-icon">💡</span>
            科技创新
          </a>
        </li>
        <li class="category-item">
          <a href="#" class="category-link">
            <span class="category-icon">🌍</span>
            文化历史
          </a>
        </li>
        <li class="category-item">
          <a href="#" class="category-link">
            <span class="category-icon">🔬</span>
            科学发现
          </a>
        </li>
        <li class="category-item">
          <a href="#" class="category-link">
            <span class="category-icon">🎨</span>
            艺术人文
          </a>
        </li>
        <li class="category-item">
          <a href="#" class="category-link">
            <span class="category-icon">⚡</span>
            技术前沿
          </a>
        </li>
      </ul>
    </nav>
  </aside>
</template>

<script setup lang="ts">
// 可以在这里定义侧边栏相关的逻辑
</script>

<style scoped>
/* 侧边栏 */
.sidebar {
  width: 280px;
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  height: fit-content;
  border: 1px solid rgba(226, 232, 240, 0.8);
}

.sidebar-title {
  font-size: 1.3rem;
  margin-top: 0;
  margin-bottom: 20px;
  color: #1a202c;
  font-weight: 600;
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.category-item {
  margin-bottom: 8px;
}

.category-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  color: #4a5568;
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.2s ease;
  font-weight: 500;
}

.category-link:hover {
  background-color: #f0f4ff;
  color: #667eea;
  transform: translateX(4px);
}

.category-icon {
  font-size: 1.1rem;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .sidebar {
    width: 100%;
  }
  
  .category-list {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 12px;
  }
  
  .category-item {
    margin-bottom: 0;
  }
}

@media (max-width: 480px) {
  .sidebar {
    padding: 20px;
  }
  
  .category-list {
    grid-template-columns: 1fr;
  }
}
</style>
