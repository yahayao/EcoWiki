<!--
虚拟滚动组件
用于高效渲染大量列表项
-->
<template>
  <div
    ref="containerRef"
    class="virtual-scroll-container"
    @scroll="handleScroll"
    :style="{ height: containerHeight + 'px' }"
  >
    <!-- 上方填充区域 -->
    <div :style="{ height: offsetTop + 'px' }" />
    
    <!-- 可见项目 -->
    <div
      v-for="item in visibleItems"
      :key="getItemKey(item.data)"
      :style="{ height: itemHeight + 'px' }"
      class="virtual-item"
    >
      <slot :item="item.data" :index="item.index" />
    </div>
    
    <!-- 下方填充区域 -->
    <div :style="{ height: offsetBottom + 'px' }" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

interface Props {
  items: any[]
  itemHeight: number
  containerHeight: number
  keyField?: string
  buffer?: number
}

const props = withDefaults(defineProps<Props>(), {
  keyField: 'id',
  buffer: 5
})

const containerRef = ref<HTMLElement>()
const scrollTop = ref(0)

// 计算可见范围
const visibleRange = computed(() => {
  const start = Math.floor(scrollTop.value / props.itemHeight)
  const end = Math.min(
    start + Math.ceil(props.containerHeight / props.itemHeight),
    props.items.length
  )
  
  return {
    start: Math.max(0, start - props.buffer),
    end: Math.min(props.items.length, end + props.buffer)
  }
})

// 可见项目
const visibleItems = computed(() => {
  const { start, end } = visibleRange.value
  return props.items.slice(start, end).map((item, i) => ({
    data: item,
    index: start + i
  }))
})

// 上方填充高度
const offsetTop = computed(() => {
  return visibleRange.value.start * props.itemHeight
})

// 下方填充高度
const offsetBottom = computed(() => {
  return (props.items.length - visibleRange.value.end) * props.itemHeight
})

// 获取项目的键值
const getItemKey = (item: any) => {
  return item[props.keyField] || item.id || Math.random()
}

// 处理滚动事件
const handleScroll = (event: Event) => {
  const target = event.target as HTMLElement
  scrollTop.value = target.scrollTop
}

// 滚动到指定项目
const scrollToItem = (index: number) => {
  if (containerRef.value) {
    const targetScrollTop = index * props.itemHeight
    containerRef.value.scrollTop = targetScrollTop
  }
}

// 滚动到顶部
const scrollToTop = () => {
  if (containerRef.value) {
    containerRef.value.scrollTop = 0
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (containerRef.value) {
    containerRef.value.scrollTop = props.items.length * props.itemHeight
  }
}

// 监听数据变化，自动滚动到顶部
watch(() => props.items.length, () => {
  scrollTop.value = 0
  if (containerRef.value) {
    containerRef.value.scrollTop = 0
  }
})

// 监听容器大小变化
const resizeObserver = ref<ResizeObserver>()

onMounted(() => {
  if (containerRef.value) {
    resizeObserver.value = new ResizeObserver(() => {
      // 容器大小变化时重新计算
    })
    resizeObserver.value.observe(containerRef.value)
  }
})

onUnmounted(() => {
  if (resizeObserver.value) {
    resizeObserver.value.disconnect()
  }
})

// 暴露方法
defineExpose({
  scrollToItem,
  scrollToTop,
  scrollToBottom
})
</script>

<style scoped>
.virtual-scroll-container {
  overflow-y: auto;
  overflow-x: hidden;
}

.virtual-item {
  width: 100%;
  box-sizing: border-box;
}

/* 滚动条样式优化 */
.virtual-scroll-container::-webkit-scrollbar {
  width: 8px;
}

.virtual-scroll-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.virtual-scroll-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.virtual-scroll-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
