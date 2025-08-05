<!--
/**
 * 经典首页布局组件
 * 
 * 功能：
 * - 提供传统Wiki风格的首页布局
 * - 采用经典三列布局设计
 * - 完整的导航体系和内容展示
 * - 响应式设计适配移动端
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
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
      @showMessages="$emit('showMessages')"
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
defineEmits(['show-login', 'show-register', 'show-admin', 'logout', 'showUserProfile', 'showMessages'])
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