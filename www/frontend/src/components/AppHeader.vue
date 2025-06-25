<template>
  <!-- 顶部导航栏 -->
  <header class="header">
    <div class="container header-container">
      <!-- Logo组件 -->
      <HeaderLogo />

      <!-- 搜索组件 -->
      <HeaderSearch @search="handleSearch" />

      <!-- 用户区域组件 -->
      <HeaderUserArea 
        @showLogin="$emit('showLogin')"
        @showRegister="$emit('showRegister')"
        @showAdminSettings="$emit('showAdminSettings')"
        @logout="$emit('logout')"
      />
    </div>
  </header>
</template>

<script setup lang="ts">
import HeaderLogo from './HeaderLogo.vue'
import HeaderSearch from './HeaderSearch.vue'
import HeaderUserArea from './HeaderUserArea.vue'

// 定义事件
defineEmits<{
  showLogin: []
  showRegister: []
  showAdminSettings: []
  logout: []
}>()

// 处理搜索
const handleSearch = (searchTerm: string) => {
  // 这里可以实现搜索逻辑
  console.log('搜索内容:', searchTerm)
  // 可以触发路由跳转或者调用搜索API
}
</script>

<style scoped>
.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 顶部导航栏容器 */
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px 0;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-container {
    flex-direction: column;
    gap: 16px;
  }
}
</style>
