<!--
  网站Logo和标题组件
  
  该组件展示EcoWiki的品牌标识和网站名称，是网站头部的核心视觉元素。
  采用现代化的渐变设计风格，强化品牌识别度和视觉冲击力。
  
  主要功能：
  - 品牌标识展示：显示EcoWiki的圆形Logo设计
  - 网站名称：展示网站主标题和副标题
  - 品牌一致性：维护整站的视觉品牌统一性
  - 导航入口：可作为返回首页的快捷方式
  - 响应式适配：在不同设备上保持良好的显示效果
  
  视觉设计：
  - 渐变Logo：紫色系渐变圆形背景，体现科技感
  - 层次结构：主标题和副标题的视觉层级区分
  - 边框效果：半透明边框增强立体感
  - 阴影效果：柔和的阴影提升视觉深度
  - 动画效果：悬浮状态的过渡动画
  
  组件结构：
  - Logo圆形：包含品牌文字的圆形图标
  - 标题组：网站名称和副标题的文字组合
  - 容器布局：灵活的横向排列布局
  - 间距设计：适当的元素间距保证视觉舒适度
  
  技术实现：
  - Vue 3 单文件组件
  - CSS3 渐变和阴影效果
  - Flexbox 布局系统
  - 过渡动画效果
  - 响应式设计支持
  
  使用场景：
  - 网站头部导航栏
  - 品牌标识展示
  - 页面标题区域
  - 用户界面品牌化
  
  扩展性：
  - 支持点击返回首页功能
  - 可添加Logo动画效果
  - 支持不同尺寸的Logo版本
  - 可集成品牌配色主题切换
  
  @author EcoWiki Team
  @version 1.0.0
  @since 2024-01-01
-->
<template>
  <!-- Logo和标题容器 -->
  <div class="logo-container">
    <!-- Logo圆形图标 -->
    <div class="logo">
      <div class="logo-circle">
        <span class="logo-text">EcoWiki</span>
      </div>
    </div>
    
    <!-- 网站标题组 -->
    <div class="title-group">
      <h1 class="site-title">EcoWiki</h1>
      <span class="site-subtitle">知识百科</span>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 网站Logo和标题组件脚本
 * 
 * 目前主要用于静态展示，后续可扩展以下功能：
 * - 点击返回首页的导航功能
 * - Logo动画效果
 * - 品牌主题切换
 * - 响应式Logo版本切换
 */

// Logo相关逻辑可以在这里定义
// 例如：首页导航、主题切换、动画控制等
</script>

<style scoped>
.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* Logo 样式 - 仿照图片中的设计 */
.logo-circle {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #8B5CF6 0%, #A855F7 50%, #C084FC 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(139, 92, 246, 0.3);
  position: relative;
  overflow: hidden;
}

.logo-circle::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(135deg, #8B5CF6, #A855F7, #C084FC, #8B5CF6);
  border-radius: 50%;
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.logo-circle:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 30px rgba(139, 92, 246, 0.4);
}

.logo-circle:hover::before {
  opacity: 1;
}

.logo-text {
  color: white;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: -0.5px;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  z-index: 1;
  position: relative;
}

.title-group {
  display: flex;
  flex-direction: column;
}

.site-title {
  font-size: 1.8rem;
  font-weight: 700;
  margin: 0;
  line-height: 1.2;
  letter-spacing: -0.5px;
}

.site-subtitle {
  font-size: 0.85rem;
  opacity: 0.9;
  font-weight: 400;
  margin-top: -2px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .logo-container {
    flex-direction: column;
    text-align: center;
    gap: 8px;
  }
  
  .title-group {
    align-items: center;
  }
  
  /* 移动端Logo调整 */
  .logo-circle {
    width: 48px;
    height: 48px;
  }
  
  .logo-text {
    font-size: 10px;
  }
}
</style>
