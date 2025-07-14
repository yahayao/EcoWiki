<!--
  加载状态组件
  
  显示页面加载时的状态指示器。
-->
<template>
  <div class="loading-state">
    <div class="loading-spinner"></div>
    <p>{{ message || '正在加载...' }}</p>
  </div>
</template>

<script setup lang="ts">
interface Props {
  message?: string
}

defineProps<Props>()
</script>

<style scoped>
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem;
  gap: 1.5rem;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.loading-spinner {
  width: 3rem;
  height: 3rem;
  border: 4px solid #e5e7eb;
  border-top: 4px solid transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  background: conic-gradient(from 0deg, #667eea, #764ba2, #667eea);
  -webkit-mask: radial-gradient(farthest-side, transparent calc(100% - 4px), white calc(100% - 4px));
  mask: radial-gradient(farthest-side, transparent calc(100% - 4px), white calc(100% - 4px));
}

.loading-state p {
  color: #6b7280;
  font-size: 1rem;
  font-weight: 500;
  margin: 0;
  text-align: center;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
