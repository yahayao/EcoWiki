<template>
  <div class="featured-articles">
    <h2 class="section-title">🔥 热门内容</h2>
    <div class="article-grid">
      <!-- 示例文章卡片 -->
      <div class="article-card" @click="navigateToArticle('1')">
        <div class="article-header">
          <span class="article-category">学术研究</span>
          <span class="article-rating">⭐ 4.8</span>
        </div>
        <h3 class="article-title">人工智能发展史与未来展望</h3>
        <p class="article-excerpt">从图灵测试到ChatGPT，探索人工智能技术的发展历程及其对未来社会的深远影响...</p>
        <div class="article-meta">
          <span class="article-author">
            <span class="author-icon">👤</span>
            科技研究员
          </span>
          <span class="article-date">2025-01-15</span>
        </div>
      </div>
      
      <div class="article-card" @click="navigateToArticle('2')">
        <div class="article-header">
          <span class="article-category">文化历史</span>
          <span class="article-rating">⭐ 4.9</span>
        </div>
        <h3 class="article-title">古代丝绸之路的文化交流</h3>
        <p class="article-excerpt">追溯古代丝绸之路上的商贸往来与文化交融，了解东西方文明的碰撞与交流...</p>
        <div class="article-meta">
          <span class="article-author">
            <span class="author-icon">👤</span>
            历史学者
          </span>
          <span class="article-date">2025-01-12</span>
        </div>
      </div>
      
      <div class="article-card" @click="navigateToArticle('3')">
        <div class="article-header">
          <span class="article-category">科技创新</span>
          <span class="article-rating">⭐ 4.7</span>
        </div>
        <h3 class="article-title">量子计算原理与应用前景</h3>
        <p class="article-excerpt">深入浅出地解释量子计算的基本原理，探讨其在密码学、药物研发等领域的应用...</p>
        <div class="article-meta">
          <span class="article-author">
            <span class="author-icon">👤</span>
            物理学家
          </span>
          <span class="article-date">2025-01-10</span>
        </div>
      </div>
      
      <div class="article-card" @click="navigateToArticle('4')">
        <div class="article-header">
          <span class="article-category">艺术人文</span>
          <span class="article-rating">⭐ 4.6</span>
        </div>
        <h3 class="article-title">文艺复兴时期的艺术革新</h3>
        <p class="article-excerpt">从达芬奇到米开朗基罗，探索文艺复兴时期艺术家们如何改变世界对美的认知...</p>
        <div class="article-meta">
          <span class="article-author">
            <span class="author-icon">👤</span>
            艺术史家
          </span>
          <span class="article-date">2025-01-08</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const navigateToArticle = (articleId: string) => {
  router.push(`/article/${articleId}`)
}
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

.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  margin-bottom: 50px;
}

.article-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  padding: 24px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid rgba(226, 232, 240, 0.8);
  position: relative;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  border-color: #667eea;
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.article-category {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.article-rating {
  font-size: 0.85rem;
  color: #f6ad55;
  font-weight: 500;
}

.article-title {
  font-size: 1.2rem;
  margin: 0 0 12px 0;
  color: #1a202c;
  font-weight: 600;
  line-height: 1.4;
}

.article-excerpt {
  color: #718096;
  line-height: 1.6;
  margin-bottom: 16px;
  font-size: 0.95rem;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #a0aec0;
  font-size: 0.85rem;
}

.article-author {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.author-icon {
  font-size: 0.8rem;
}

.article-date {
  color: #cbd5e0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .article-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .article-card {
    padding: 20px;
  }
}
</style>
