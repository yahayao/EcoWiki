<!--
  应用程序头部导航组件
  
  该组件是整个应用的顶部导航栏，包含Logo、搜索功能和用户操作区域。
  采用响应式设计，支持不同屏幕尺寸的自适应布局。
  
  功能特性：
  - Logo展示和品牌识别
  - 全局搜索功能
  - 用户登录/注册/管理入口
  - 粘性定位，始终显示在顶部
  - 渐变背景和阴影效果
  
  组件组成：
  - HeaderLogo: Logo组件
  - HeaderSearch: 搜索组件
  - HeaderUserArea: 用户操作区域组件
  
  事件传递：
  - showLogin: 显示登录模态框
  - showRegister: 显示注册模态框
  - showAdminSettings: 显示管理后台
  - logout: 用户登出
  
  响应式设计：
  - 桌面端：水平布局，三个区域并排显示
  - 移动端：垂直布局，组件堆叠显示
  
  技术栈：
  - Vue 3 Composition API
  - TypeScript 类型支持
  - CSS Grid/Flexbox 布局
  - CSS 自定义属性和渐变效果
  
  使用场景：
  - 应用主布局的头部区域
  - 全局导航和用户操作入口
  - 品牌展示和搜索功能载体
  
  @author EcoWiki Team
  @version 1.0.0
  @since 2024-01-01
-->
<template>
  <!-- 顶部导航栏容器 -->
  <header class="header">
    <div class="container header-container">
      <!-- Logo组件 - 品牌标识和首页链接 -->
      <HeaderLogo />

      <!-- 搜索组件 - 全局搜索功能 -->
      <HeaderSearch @search="handleSearch" />

      <!-- 用户区域组件 - 登录状态和用户操作 -->
      <HeaderUserArea 
        @showLogin="$emit('showLogin')"
        @showRegister="$emit('showRegister')"
        @showAdminSettings="$emit('showAdminSettings')"
        @logout="$emit('logout')"
        @showUserProfile="$emit('showUserProfile')"
        @showMessages="$emit('showMessages')"
      />
    </div>
  </header>
</template>

<script setup lang="ts">
/**
 * 应用程序头部导航组件脚本
 * 
 * 实现头部导航的交互逻辑，包括搜索处理和事件传递。
 * 使用 Vue 3 Composition API 和 TypeScript 类型定义。
 */

import HeaderLogo from './HeaderLogo.vue'
import HeaderSearch from './HeaderSearch.vue'
import HeaderUserArea from './HeaderUserArea.vue'

/**
 * 组件事件定义
 * 定义了组件向父组件传递的所有事件类型
 */
defineEmits<{
  /** 显示登录模态框事件 */
  showLogin: []
  /** 显示注册模态框事件 */
  showRegister: []
  /** 显示管理员设置界面事件 */
  showAdminSettings: []
  /** 显示用户个人资料事件 */
  showUserProfile: []
  /** 显示消息面板事件 */
  showMessages: []
  /** 用户登出事件 */
  logout: []
}>()

/**
 * 处理搜索事件
 * @param searchTerm 搜索关键词
 * 
 * 当用户在搜索框中输入关键词并触发搜索时调用。
 * 现在HeaderSearch组件已经直接处理路由跳转，
 * 这里主要用于记录搜索行为或其他全局操作。
 */
const handleSearch = (searchTerm: string) => {
  // 记录搜索行为用于调试和分析
  console.log('搜索内容:', searchTerm)
  
  // 注意：搜索路由跳转现在由HeaderSearch组件直接处理
  // 这里可以添加其他全局搜索相关的逻辑，比如：
  // - 记录搜索历史
  // - 更新全局搜索状态
  // - 发送搜索统计信息
}
</script>

<style scoped>
/**
 * 应用程序头部导航样式
 * 
 * 实现现代化的头部导航设计，包括渐变背景、阴影效果和响应式布局。
 * 使用 CSS Flexbox 进行布局，支持不同屏幕尺寸的自适应显示。
 */

/* 通用容器样式 - 限制最大宽度并居中显示 */
.container {
  width: 100%;
  max-width: 1200px;  /* 最大宽度限制 */
  margin: 0 auto;     /* 水平居中 */
  padding: 0 20px;    /* 左右内边距 */
}

/* 顶部导航栏主容器 */
.header {
  /* 渐变背景 - 从蓝紫色到深紫色的对角线渐变 */
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;                    /* 文字颜色 */
  padding: 16px 0;                /* 上下内边距 */
  
  /* 阴影效果 - 柔和的蓝色阴影增强层次感 */
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
  
  /* 粘性定位 - 滚动时始终固定在顶部 */
  position: sticky;
  top: 0;
  z-index: 100;                   /* 高层级确保在其他元素之上 */
}

/* 头部内容容器 - Flexbox 布局 */
.header-container {
  display: flex;
  justify-content: space-between; /* 子元素两端对齐 */
  align-items: center;           /* 垂直居中对齐 */
  gap: 20px;                     /* 子元素间距 */
}

/* 响应式设计 - 移动端适配 */
@media (max-width: 768px) {
  .header-container {
    flex-direction: column;       /* 移动端改为垂直布局 */
    gap: 16px;                   /* 减少间距以适应小屏幕 */
  }
  
  /* 移动端可以添加更多样式调整 */
  .container {
    padding: 0 16px;             /* 移动端减少左右内边距 */
  }
}

/* 超小屏幕适配 */
@media (max-width: 480px) {
  .header {
    padding: 12px 0;             /* 进一步减少上下内边距 */
  }
  
  .header-container {
    gap: 12px;                   /* 更小的间距 */
  }
  
  .container {
    padding: 0 12px;             /* 最小的左右内边距 */
  }
}
</style>
