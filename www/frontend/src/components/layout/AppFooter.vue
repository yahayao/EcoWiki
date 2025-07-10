<!--
  网站底部组件
  
  该组件是网站的全局底部区域，提供网站信息、快速链接和版权声明等功能。
  采用响应式设计，在不同设备上都能提供良好的用户体验。
  
  主要功能：
  - 网站品牌展示：显示EcoWiki品牌信息和简介
  - 快速导航链接：提供常用页面和功能的快速入口
  - 帮助支持链接：包含使用条款、隐私政策等法律信息
  - 版权声明：显示版权信息和发布年份
  - 社交媒体链接：支持社交媒体平台链接扩展
  
  布局结构：
  - 上半部分：分为三个信息区域（品牌、快速链接、帮助支持）
  - 下半部分：版权声明和法律信息
  - 响应式布局：移动端自动调整为垂直排列
  
  视觉设计：
  - 深色主题：使用渐变背景营造专业感
  - 清晰层次：通过颜色和字体大小区分信息层级
  - 链接交互：悬浮状态提供视觉反馈
  - 品牌一致性：保持与整体网站风格统一
  
  技术实现：
  - Vue 3 单文件组件
  - CSS Grid 和 Flexbox 布局
  - 响应式媒体查询
  - CSS 变量和渐变效果
  - 悬浮动画和过渡效果
  
  使用场景：
  - 网站页面底部固定展示
  - 提供网站导航和信息入口
  - 增强用户对网站的信任感
  - 法律合规信息展示
  
  扩展性：
  - 支持添加更多链接分类
  - 可集成社交媒体图标
  - 支持多语言版本
  - 可添加统计信息和动态内容
  
  @author EcoWiki Team
  @version 1.0.0
  @since 2024-01-01
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
