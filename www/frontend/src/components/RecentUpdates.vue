<template>
  <div class="recent-updates">
    <h2 class="section-title">📝 最近更新</h2>
    <div class="updates-list">
      <div class="update-item">
        <div class="update-icon">🆕</div>
        <div class="update-content">
          <span class="update-time">2025-01-15 14:30</span>
          <span class="update-title">新增机器学习算法详解章节</span>
          <span class="update-author">算法工程师</span>
        </div>
      </div>
      <div class="update-item">
        <div class="update-icon">📊</div>
        <div class="update-content">
          <span class="update-time">2025-01-14 09:15</span>
          <span class="update-title">更新全球经济数据图表</span>
          <span class="update-author">经济分析师</span>
        </div>
      </div>
      <div class="update-item">
        <div class="update-icon">🔬</div>
        <div class="update-content">
          <span class="update-time">2025-01-13 16:45</span>
          <span class="update-title">完善化学元素周期表介绍</span>
          <span class="update-author">化学专家</span>
        </div>
      </div>
      <div class="update-item">
        <div class="update-icon">🎭</div>
        <div class="update-content">
          <span class="update-time">2025-01-12 11:20</span>
          <span class="update-title">添加世界文学经典作品赏析</span>
          <span class="update-author">文学评论家</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 最近更新相关逻辑
// 可以在这里定义更新数据、刷新逻辑等
</script>

<style scoped>
.section-title {
  font-size: 1.5rem;
  margin-top: 0;
  margin-bottom: 24px;
  color: #1a202c;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 最近更新 */
.recent-updates {
  margin-top: 50px;
}

.updates-list {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(226, 232, 240, 0.8);
}

.update-item {
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.2s ease;
}

.update-item:hover {
  background-color: #f8fafc;
}

.update-item:last-child {
  border-bottom: none;
}

.update-icon {
  font-size: 1.2rem;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.update-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.update-time {
  color: #a0aec0;
  font-size: 0.8rem;
  font-weight: 500;
}

.update-title {
  color: #1a202c;
  font-weight: 500;
  font-size: 0.95rem;
}

.update-author {
  color: #667eea;
  font-size: 0.85rem;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .update-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .update-content {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .update-item {
    padding: 16px 20px;
  }
}
</style>
