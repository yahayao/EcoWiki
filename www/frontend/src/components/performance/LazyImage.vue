<!--
图片懒加载组件
支持渐进式加载和占位符
-->
<template>
  <div 
    ref="container"
    class="lazy-image-container"
    :class="{ loaded: isLoaded, loading: isLoading }"
  >
    <!-- 占位符 -->
    <div v-if="!isLoaded" class="placeholder">
      <div class="skeleton" />
    </div>
    
    <!-- 实际图片 -->
    <img
      v-show="isLoaded"
      :src="currentSrc"
      :alt="alt"
      :loading="native ? 'lazy' : 'eager'"
      @load="onLoad"
      @error="onError"
      class="lazy-image"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useIntersectionObserver } from '@vueuse/core'

interface Props {
  src: string
  alt: string
  placeholder?: string
  native?: boolean
  threshold?: number
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '',
  native: false,
  threshold: 0.1
})

const container = ref<HTMLElement>()
const isVisible = ref(false)
const isLoaded = ref(false)
const isLoading = ref(false)
const hasError = ref(false)

const currentSrc = computed(() => {
  if (!isVisible.value) return props.placeholder
  return props.src
})

// 使用 Intersection Observer
if (!props.native) {
  useIntersectionObserver(
    container,
    ([{ isIntersecting }]) => {
      if (isIntersecting && !isVisible.value) {
        isVisible.value = true
        isLoading.value = true
      }
    },
    { threshold: props.threshold }
  )
} else {
  // 如果使用原生懒加载，立即设置为可见
  onMounted(() => {
    isVisible.value = true
  })
}

const onLoad = () => {
  isLoaded.value = true
  isLoading.value = false
}

const onError = () => {
  hasError.value = true
  isLoading.value = false
}
</script>

<style scoped>
.lazy-image-container {
  position: relative;
  overflow: hidden;
  background-color: #f5f5f5;
}

.placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.skeleton {
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

.lazy-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: opacity 0.3s ease;
}

.loading .lazy-image {
  opacity: 0;
}

.loaded .lazy-image {
  opacity: 1;
}
</style>
