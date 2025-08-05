<!--
/**
 * 网站底部组件
 * 
 * 功能：
 * - 提供网站全局底部区域展示
 * - 支持品牌信息和快速导航链接
 * - 实现响应式布局设计
 * - 提供版权声明和法律信息展示
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
-->
<template>
  <!-- 网站底部容器 -->
  <footer class="footer">
    <div class="container">
      <!-- 主要内容区域 -->
      <div class="footer-content">
        <!-- 品牌信息区域 -->
        <div class="footer-section">
          <h3 class="footer-title">EcoWiki</h3>
          <p class="footer-description">开放环境，共享知识的百科平台</p>
        </div>
        
        <!-- 快速链接区域 -->
        <div class="footer-section">
          <h4 class="footer-subtitle">快速链接</h4>
          <div class="footer-links">
            <a href="#" class="footer-link">关于我们</a>
            <a href="#" class="footer-link">使用指南</a>
            <a href="#" class="footer-link">贡献内容</a>
          </div>
        </div>
        
        <!-- 帮助支持区域 -->
        <div class="footer-section">
          <h4 class="footer-subtitle">帮助支持</h4>
          <div class="footer-links">
            <a href="#" class="footer-link">使用条款</a>
            <a href="#" class="footer-link">隐私政策</a>
            <a href="#" class="footer-link">联系我们</a>
          </div>
        </div>
      </div>
      
      <!-- 版权声明区域 -->
      <div class="footer-bottom">
        <p class="copyright">&copy; 2025 EcoWiki. 保留所有权利。</p>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
/**
 * 网站底部组件脚本
 * 
 * 目前主要用于静态内容展示，后续可扩展以下功能：
 * - 动态版权年份计算
 * - 社交媒体链接管理
 * - 统计信息展示
 * - 多语言支持
 */

// 可以在这里定义页脚相关的逻辑
// 例如：动态年份、链接点击统计、社交媒体集成等
</script>

<style scoped>
.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 页脚 */
.footer {
  background: linear-gradient(135deg, #1a202c 0%, #2d3748 100%);
  color: #e2e8f0;
  padding: 40px 0 20px;
  margin-top: auto;
}

.footer-content {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 40px;
  margin-bottom: 30px;
}

.footer-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-title {
  font-size: 1.3rem;
  font-weight: 600;
  margin: 0;
  color: white;
}

.footer-subtitle {
  font-size: 1rem;
  font-weight: 500;
  margin: 0;
  color: white;
}

.footer-description {
  color: #a0aec0;
  font-size: 0.9rem;
  line-height: 1.5;
  margin: 0;
}

.footer-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.footer-link {
  color: #cbd5e0;
  text-decoration: none;
  transition: color 0.2s ease;
  font-size: 0.9rem;
}

.footer-link:hover {
  color: #667eea;
}

.footer-bottom {
  border-top: 1px solid #4a5568;
  padding-top: 20px;
  text-align: center;
}

.copyright {
  margin: 0;
  color: #a0aec0;
  font-size: 0.85rem;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .footer-content {
    grid-template-columns: 1fr;
    gap: 30px;
  }
}
</style>
