<!--
  经典首页布局组件
  
  这是EcoWiki的经典风格首页，提供传统的Wiki网站布局和丰富的功能区域。
  采用经典的三列布局设计，具有完整的导航体系和内容展示功能。
  
  主要功能：
  - 完整的顶部导航栏，包含Logo、搜索、用户操作区域
  - 左侧边栏导航，提供分类浏览和快速链接
  - 中央主内容区，展示精选文章和最新更新
  - 底部信息栏，包含版权信息和相关链接
  
  布局特点：
  - 传统三列布局：侧边栏 + 主内容 + 可选右侧栏
  - 响应式设计：移动端自动调整为单列布局
  - 卡片式设计：现代化的圆角卡片和阴影效果
  - 渐变背景：优雅的色彩搭配和视觉层次
  
  设计理念：
  - 信息密度高：适合展示大量内容和导航选项
  - 功能完整：提供完整的Wiki功能和用户操作
  - 专业感强：适合知识管理和学术研究场景
  - 易于扩展：支持添加更多功能模块
  
  用户体验：
  - 清晰的信息层次和视觉引导
  - 便捷的导航和搜索功能
  - 丰富的内容发现机制
  - 完整的用户操作流程
  
  技术特性：
  - Vue 3 组合式API架构
  - 组件化设计，易于维护
  - 事件透传机制，统一状态管理
  - CSS Flexbox布局，响应式适配
  
  @author EcoWiki Team
  @version 1.0.0
  @since 2024-01-01
  
  @example
  <!-- 在路由中使用 --
  <ClassicHome 
    @show-login="handleShowLogin"
    @show-register="handleShowRegister"
    @show-admin="handleShowAdmin"
    @logout="handleLogout"
  />
-->
<template>
  <!-- 经典首页主容器 -->
  <div class="wiki-home">
    <!-- 顶部导航栏：包含Logo、搜索、用户操作等 -->
    <AppHeader
      @showLogin="$emit('show-login')"
      @showRegister="$emit('show-register')"
      @showAdminSettings="$emit('show-admin')"
      @showUserProfile="$emit('showUserProfile')"
      @logout="$emit('logout')"
    />

    <!-- 主要内容区域：三列布局的核心区域 -->
    <main class="main-content">
      <div class="container">
        <!-- 左侧边栏：导航菜单和快速链接 -->
        <AppSidebar />
        
        <!-- 中央主内容区：精选文章、最新更新等 -->
        <AppMainContent />
      </div>
    </main>

    <!-- 底部信息栏：版权信息、相关链接等 -->
    <AppFooter />
  </div>
</template>

<script setup lang="ts">
/**
 * 经典首页布局组件脚本
 * 
 * 负责组合各个子组件，形成完整的经典首页布局。
 * 通过事件透传机制将用户操作传递给父组件处理。
 */

// 导入子组件
import { onMounted } from 'vue'

import AppHeader from '../components/layout/AppHeader.vue'      // 顶部导航栏组件
import AppSidebar from '../components/layout/AppSidebar.vue'    // 左侧边栏组件
import AppMainContent from '../components/layout/AppMainContent.vue'  // 主内容区组件
import AppFooter from '../components/layout/AppFooter.vue'      // 底部信息栏组件
import { useRouter } from 'vue-router';
const router = useRouter();
// ✅ 重置状态函数
const resetHomeState = () => {
  // 清除本地存储（如首页缓存、临时数据等）
  localStorage.removeItem('home-cache')
  localStorage.removeItem('pending-changes')

  // 强制刷新用户状态（从服务器重新获取）
  // 如果你有 useAuth().fetchUser()，可以在这里调用
  // useAuth().fetchUser()

  // 重置滚动位置
  window.scrollTo(0, 0)

  console.log('首页状态已重置')
}

// ✅ 在首页挂载时执行
onMounted(() => {
  resetHomeState()
})

router.beforeEach((to, from, next) => {
  if (to.path === '/' && from.path.startsWith('/user-profile')) {
    window.location.reload();
  } else {
    next();
  }
});
/**
 * 组件事件定义
 * 定义经典首页向父组件发送的事件，与其他首页风格保持一致的接口
 */
defineEmits(['show-login', 'show-register', 'show-admin', 'logout', 'showUserProfile'])
</script>

<style scoped>
/**
 * 经典首页布局样式
 * 
 * 实现传统Wiki网站的三列布局设计，包括响应式适配和现代化视觉效果。
 * 使用Flexbox布局确保在不同屏幕尺寸下的良好表现。
 */

/* 首页主容器 - 全屏高度的垂直布局 */
.wiki-home {
  min-height: 100vh;          /* 最小高度为视窗高度 */
  display: flex;              /* Flexbox垂直布局 */
  flex-direction: column;     /* 垂直排列子元素 */
  background: #f8fafc;        /* 浅灰色背景，营造层次感 */
}

/* 卡片式设计 - 为主要内容区域添加现代化视觉效果 */
.main-content, .sidebar, .content-area {
  border-radius: 16px;                           /* 大圆角，现代化设计 */
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.08);  /* 柔和阴影效果 */
  background: #fff;                              /* 白色背景 */
}

/* 通用容器样式 - 限制最大宽度并居中显示 */
.container {
  width: 100%;                /* 占满父容器宽度 */
  max-width: 1200px;          /* 最大宽度限制，保证阅读体验 */
  margin: 0 auto;             /* 水平居中 */
  padding: 0 20px;            /* 左右内边距，防止内容贴边 */
}

/* 主内容区布局 - 三列布局的实现 */
.main-content {
  flex: 1;                    /* 占据剩余空间，确保底部固定 */
  padding: 40px 0;            /* 上下内边距，与头部底部保持距离 */
}

.main-content .container {
  display: flex;              /* Flexbox水平布局 */
  gap: 30px;                  /* 子元素间距，分离侧边栏和主内容 */
}

/* 响应式设计 - 平板设备适配 */
@media (max-width: 1024px) {
  .main-content .container {
    flex-direction: column;   /* 改为垂直布局 */
    gap: 24px;                /* 减小间距 */
  }
}

/* 响应式设计 - 移动设备适配 */
@media (max-width: 480px) {
  .container {
    padding: 0 15px;          /* 减小左右内边距 */
  }
  
  .main-content {
    padding: 20px 0;          /* 减小上下内边距 */
  }
}
</style>