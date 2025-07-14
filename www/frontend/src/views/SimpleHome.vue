<!--
  极简首页布局组件
  
  这是EcoWiki的极简风格首页，提供最简洁纯净的用户体验。
  采用中心化布局设计，专注于核心功能和视觉美感。
  
  主要功能：
  - 中心化的品牌展示，突出EcoWiki标识
  - 极简的操作入口，只提供核心功能按钮
  - 纯净的视觉设计，减少干扰元素
  - 渐变背景和装饰元素，营造现代感
  
  设计特点：
  - 单页面设计：所有内容集中在一个视窗内
  - 中心对齐：所有元素围绕页面中心布局
  - 渐变背景：使用柔和的蓝紫色渐变
  - 装饰元素：半透明圆形背景增加视觉层次
  - 按钮特效：悬停动画和光泽特效
  
  设计理念：
  - 极简主义：去除冗余，专注核心
  - 直观易用：一目了然的操作入口
  - 视觉愉悦：现代化的色彩和动效
  - 快速上手：新用户友好的引导设计
  
  用户体验：
  - 零学习成本的直观界面
  - 响应式按钮交互反馈
  - 清晰的视觉层次和引导
  - 专注于快速完成核心任务
  
  适用场景：
  - 新用户首次访问
  - 移动设备浏览
  - 专注模式下的快速操作
  - 极简主义用户偏好
  
  技术特性：
  - 纯CSS动画效果
  - 响应式设计适配
  - 高性能渲染优化
  - 无依赖的轻量实现
  
  @author EcoWiki Team
  @version 1.0.0
  @since 2024-01-01
  
  @example
  <!-- 在路由中使用 
  <SimpleHome 
    @show-login="handleShowLogin"
    @show-register="handleShowRegister"
    @show-admin="handleShowAdmin"
  />
-->
<template>
  <!-- 极简首页主容器 -->
  <div class="simple-home">
    <!-- 背景装饰元素：四个渐变圆形，增加视觉层次 -->
    <div class="bg-circle bg-circle1"></div>  <!-- 左上角装饰圆 -->
    <div class="bg-circle bg-circle2"></div>  <!-- 右上角装饰圆 -->
    <div class="bg-circle bg-circle3"></div>  <!-- 左下角装饰圆 -->
    <div class="bg-circle bg-circle4"></div>  <!-- 右下角装饰圆 -->
    
    <!-- 主标题：EcoWiki品牌标识 -->
    <h1>EcoWiki</h1>
    
    <!-- 副标题：简洁的理念描述 -->
    <p class="subtitle">极简风格，专注知识本身</p>
    
    <!-- 导航按钮组：核心功能入口 -->
    <div class="nav-btns">
      <button class="nav-btn" @click="$emit('show-login')">登录</button>
      <button class="nav-btn" @click="$emit('show-register')">注册</button>
      <button class="nav-btn" @click="showGame">太空射击</button>
      <button class="nav-btn" @click="showGameB">横版跑酷</button>
      <button class="nav-btn" @click="$emit('show-admin')">管理设置</button>
    </div>
    
    <!-- 太空射击游戏组件 -->
    <SpaceShooterGame 
      :visible="gameVisible"
      @close="closeGame"
    />

    <!-- 塔防游戏组件 -->
    <TowerDefenseGame 
      :visible="gameVisibleB"
      @close="closeGameB"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * 极简首页布局组件脚本
 * 
 * 实现最简化的首页功能，只提供核心的用户操作入口。
 * 通过事件发射机制将用户操作传递给父组件处理。
 */

import { ref } from 'vue'
import SpaceShooterGame from '../spgame/SpaceShooterGame.vue'
import TowerDefenseGame from '../spgame/TowerDefenseGame.vue'

/**
 * 游戏状态管理
 */
const gameVisible = ref(false)
const gameVisibleB = ref(false)

/**
 * 显示游戏
 */
function showGame() {
  gameVisible.value = true
}
function showGameB() {
  gameVisibleB.value = true
}

/**
 * 关闭游戏
 */
function closeGame() {
  gameVisible.value = false
}
function closeGameB() {
  gameVisibleB.value = false
}

/**
 * 组件事件定义
 * 定义极简首页向父组件发送的事件，保持与其他首页风格的接口一致性
 */
defineEmits(['show-login', 'show-register', 'show-admin'])
</script>

<style scoped>
/**
 * 极简首页样式设计
 * 
 * 实现现代化的极简设计风格，包括渐变背景、装饰元素和交互动效。
 * 使用CSS3的高级特性创造优雅的视觉体验。
 */

/* 主容器 - 全屏中心化布局 */
.simple-home {
  min-height: 100vh;              /* 最小高度为视窗高度 */
  width: 100vw;                   /* 宽度为视窗宽度 */
  position: relative;             /* 相对定位，为装饰元素提供定位基准 */
  display: flex;                  /* Flexbox布局 */
  flex-direction: column;         /* 垂直排列子元素 */
  align-items: center;            /* 水平居中 */
  justify-content: center;        /* 垂直居中 */
  background: linear-gradient(135deg, #f6f8ff 0%, #e9eafd 100%);  /* 柔和的蓝紫色渐变背景 */
  overflow: hidden;               /* 隐藏超出部分，确保装饰元素不会产生滚动条 */
}

/* 背景装饰圆形 - 增加视觉层次和现代感 */
.bg-circle {
  position: absolute;             /* 绝对定位，脱离文档流 */
  border-radius: 50%;             /* 圆形 */
  background: radial-gradient(circle, rgba(120,130,255,0.12) 0%, rgba(120,130,255,0.02) 100%);  /* 径向渐变，中心较深 */
  pointer-events: none;           /* 不响应鼠标事件，避免干扰用户操作 */
}

/* 左上角装饰圆 - 大尺寸，部分超出边界 */
.bg-circle1 {
  width: 180px;
  height: 180px;
  left: -60px;                    /* 负值让圆形部分超出左边界 */
  top: -60px;                     /* 负值让圆形部分超出上边界 */
}

/* 右上角装饰圆 - 中等尺寸 */
.bg-circle2 {
  width: 120px;
  height: 120px;
  right: -40px;                   /* 负值让圆形部分超出右边界 */
  top: 80px;                      /* 距离顶部的位置 */
}

/* 左下角装饰圆 - 小尺寸 */
.bg-circle3 {
  width: 90px;
  height: 90px;
  left: 40px;                     /* 距离左边界的位置 */
  bottom: 80px;                   /* 距离底部的位置 */
}

/* 右下角装饰圆 - 中大尺寸，部分超出边界 */
.bg-circle4 {
  width: 140px;
  height: 140px;
  right: -50px;                   /* 负值让圆形部分超出右边界 */
  bottom: -50px;                  /* 负值让圆形部分超出下边界 */
}

/* 主标题样式 - 品牌标识的视觉表现 */
h1 {
  font-size: 2.5rem;              /* 大字号，突出品牌 */
  font-weight: bold;              /* 粗体字重 */
  margin-bottom: 0.5rem;          /* 与副标题的间距 */
  color: #6a6fd7;                 /* 品牌紫色 */
  letter-spacing: 2px;            /* 字母间距，增加设计感 */
  z-index: 1;                     /* 确保在装饰元素之上 */
}

/* 副标题样式 - 理念描述文字 */
.subtitle {
  color: #4b4f6b;                 /* 深灰色，低于主标题的视觉权重 */
  margin-bottom: 2rem;            /* 与按钮组的间距 */
  font-size: 1.2rem;              /* 适中的字号 */
  letter-spacing: 1px;            /* 轻微的字母间距 */
  z-index: 1;                     /* 确保在装饰元素之上 */
}

/* 按钮组容器 - 水平排列的按钮 */
.nav-btns {
  display: flex;                  /* Flexbox水平布局 */
  gap: 1.5rem;                    /* 按钮间距 */
  z-index: 1;                     /* 确保在装饰元素之上 */
}

/* 导航按钮样式 - 现代化的渐变按钮设计 */
.nav-btn {
  padding: 1rem 2.8rem;           /* 内边距，提供足够的点击区域 */
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);  /* 蓝紫色渐变背景 */
  color: #fff;                    /* 白色文字 */
  border-radius: 25px;            /* 大圆角，现代化设计 */
  font-size: 1.25rem;             /* 适中的字号 */
  font-weight: 600;               /* 半粗体字重 */
  border: none;                   /* 无边框 */
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.35);  /* 柔和阴影 */
  cursor: pointer;                /* 手型光标 */
  transition: all 0.3s cubic-bezier(.4,2,.6,1), box-shadow 0.3s;  /* 弹性过渡动画 */
  margin-bottom: 0.5rem;          /* 底部边距 */
  position: relative;             /* 相对定位，为伪元素提供基准 */
  overflow: hidden;               /* 隐藏溢出，确保光泽特效不超出边界 */
  letter-spacing: 2px;            /* 字母间距，增加设计感 */
}

/* 按钮光泽特效 - 悬停时的光线扫过效果 */
.nav-btn::before {
  content: '';                    /* 空内容的伪元素 */
  position: absolute;             /* 绝对定位 */
  top: 0;                         /* 顶部对齐 */
  left: -100%;                    /* 初始位置在按钮左侧外部 */
  width: 100%;                    /* 与按钮同宽 */
  height: 100%;                   /* 与按钮同高 */
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.18), transparent);  /* 半透明白色渐变 */
  transition: left 0.5s;          /* 位置变化的过渡动画 */
}

/* 悬停时的光泽动画 - 光线从左到右扫过 */
.nav-btn:hover::before {
  left: 100%;                     /* 光泽移动到按钮右侧外部 */
}

/* 按钮悬停效果 - 提升和阴影变化 */
.nav-btn:hover {
  transform: translateY(-2px);    /* 向上微移，营造悬浮感 */
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.45);  /* 增强阴影效果 */
}
</style>