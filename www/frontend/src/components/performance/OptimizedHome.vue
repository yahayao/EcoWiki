<!--
首页性能优化组件
提供预加载、懒加载等性能优化功能
-->
<template>
  <div class="performance-optimized-home">
    <!-- 首屏关键内容 -->
    <div class="hero-section">
      <slot name="hero" />
    </div>
    
    <!-- 懒加载的次要内容 -->
    <div v-show="isVisible" class="secondary-content">
      <slot name="secondary" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useIntersectionObserver } from '@vueuse/core'
import { preloadResources } from '@/utils/performance'

const isVisible = ref(false)
const target = ref<HTMLElement>()

// 使用 Intersection Observer 进行懒加载
useIntersectionObserver(
  target,
  ([{ isIntersecting }]) => {
    if (isIntersecting) {
      isVisible.value = true
    }
  },
  { threshold: 0.1 }
)

onMounted(() => {
  // 预加载关键资源
  preloadResources([
    '/api/articles/latest',
    '/api/categories',
    '/static/images/logo.png'
  ])
  
  // 延迟加载非关键 CSS
  const link = document.createElement('link')
  link.rel = 'stylesheet'
  link.href = '/static/css/secondary.css'
  link.media = 'print'
  link.onload = () => {
    link.media = 'all'
  }
  document.head.appendChild(link)
})
</script>

<style scoped>
.performance-optimized-home {
  min-height: 100vh;
}

.hero-section {
  /* 首屏内容样式 */
  min-height: 100vh;
}

.secondary-content {
  /* 次要内容样式 */
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.secondary-content[v-show="true"] {
  opacity: 1;
  transform: translateY(0);
}
</style>
