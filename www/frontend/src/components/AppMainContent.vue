<template>
  <!-- 内容区 -->
  <section class="content-area">
    <!-- 热门文章模块 -->
    <FeaturedArticles />
    
    <!-- 最近更新模块 -->
    <RecentUpdates />
  </section>
</template>

<script setup lang="ts">
import FeaturedArticles from './FeaturedArticles.vue'
import RecentUpdates from './RecentUpdates.vue'

// 主内容区相关逻辑可以在这里定义
</script>

<style scoped>
/* 内容区容器 */
.content-area {
  flex: 1;
}
</style>
