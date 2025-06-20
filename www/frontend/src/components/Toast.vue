<template>
  <Teleport to="body">
    <Transition name="toast">
      <div 
        v-if="visible" 
        class="toast-container"
        :class="`toast-${type}`"
      >
        <div class="toast-icon">
          <span v-if="type === 'success'">✓</span>
          <span v-else-if="type === 'error'">✗</span>
          <span v-else-if="type === 'warning'">⚠</span>
          <span v-else>ℹ</span>
        </div>
        <div class="toast-content">
          <div class="toast-title" v-if="title">{{ title }}</div>
          <div class="toast-message">{{ message }}</div>
        </div>
        <button @click="close" class="toast-close">✕</button>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Props {
  message: string
  title?: string
  type?: 'success' | 'error' | 'warning' | 'info'
  duration?: number
}

const props = withDefaults(defineProps<Props>(), {
  type: 'info',
  duration: 3000
})

const emit = defineEmits(['close'])

const visible = ref(false)

const close = () => {
  visible.value = false
  setTimeout(() => {
    emit('close')
  }, 300)
}

onMounted(() => {
  visible.value = true
  
  if (props.duration > 0) {
    setTimeout(() => {
      close()
    }, props.duration)
  }
})
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  padding: 16px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  max-width: 400px;
  z-index: 1000;
  border-left: 4px solid;
}

.toast-success {
  border-left-color: #27ae60;
}

.toast-error {
  border-left-color: #e74c3c;
}

.toast-warning {
  border-left-color: #f39c12;
}

.toast-info {
  border-left-color: #3498db;
}

.toast-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: white;
  font-size: 14px;
}

.toast-success .toast-icon {
  background: #27ae60;
}

.toast-error .toast-icon {
  background: #e74c3c;
}

.toast-warning .toast-icon {
  background: #f39c12;
}

.toast-info .toast-icon {
  background: #3498db;
}

.toast-content {
  flex: 1;
}

.toast-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  font-size: 14px;
}

.toast-message {
  color: #666;
  font-size: 13px;
  line-height: 1.4;
}

.toast-close {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 16px;
  padding: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s ease;
}

.toast-close:hover {
  background: #f5f5f5;
  color: #666;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}
</style>
