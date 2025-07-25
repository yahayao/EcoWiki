<template>
  <div 
    class="user-avatar-display" 
    :class="[sizeClass, shapeClass]"
    :title="username"
  >
    <!-- 头像图片 -->
    <img 
      v-if="avatarUrl && !imageError" 
      :src="displayAvatarUrl" 
      :alt="`${username}的头像`"
      class="avatar-image"
      @error="handleImageError"
      @load="handleImageLoad"
    />
    
    <!-- 默认头像 - 首字母 -->
    <div 
      v-else
      class="avatar-fallback"
      :style="{ backgroundColor: avatarColor }"
    >
      {{ avatarInitial }}
    </div>
    
    <!-- 在线状态指示器 -->
    <div 
      v-if="showOnlineStatus && isOnline"
      class="online-indicator"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

// Props 定义
interface Props {
  username?: string
  avatarUrl?: string
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl'
  shape?: 'circle' | 'square' | 'rounded'
  showOnlineStatus?: boolean
  isOnline?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  username: '用户',
  avatarUrl: '',
  size: 'md',
  shape: 'circle',
  showOnlineStatus: false,
  isOnline: false
})

// 响应式数据
const imageError = ref(false)

// 计算属性
const displayAvatarUrl = computed(() => {
  if (!props.avatarUrl) return ''
  
  // 如果是完整URL，直接使用
  if (props.avatarUrl.startsWith('http')) {
    return props.avatarUrl
  }
  
  // 如果是相对路径，拼接服务器地址
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  
  // 支持新的静态资源路径 /uploads/avatars/ 和旧的 /avatars/ 路径
  if (props.avatarUrl.startsWith('/uploads/avatars/') || props.avatarUrl.startsWith('/avatars/')) {
    return baseUrl + props.avatarUrl
  } else {
    // 兼容性处理：如果只是文件名，添加完整路径
    return baseUrl + '/uploads/avatars/' + props.avatarUrl
  }
})

const avatarInitial = computed(() => {
  if (!props.username) return 'U'
  return props.username.charAt(0).toUpperCase()
})

const avatarColor = computed(() => {
  // 根据用户名生成一致的颜色
  if (!props.username) return '#6b7280'
  
  const colors = [
    '#ef4444', '#f97316', '#f59e0b', '#eab308', '#84cc16',
    '#22c55e', '#10b981', '#14b8a6', '#06b6d4', '#0ea5e9',
    '#3b82f6', '#6366f1', '#8b5cf6', '#a855f7', '#d946ef',
    '#ec4899', '#f43f5e'
  ]
  
  let hash = 0
  for (let i = 0; i < props.username.length; i++) {
    hash = props.username.charCodeAt(i) + ((hash << 5) - hash)
  }
  
  return colors[Math.abs(hash) % colors.length]
})

const sizeClass = computed(() => `avatar-${props.size}`)
const shapeClass = computed(() => `avatar-${props.shape}`)

// 方法
const handleImageError = () => {
  imageError.value = true
}

const handleImageLoad = () => {
  imageError.value = false
}
</script>

<style scoped>
.user-avatar-display {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
  background-color: #f3f4f6;
  border: 2px solid #e5e7eb;
  transition: all 0.2s ease;
}

.user-avatar-display:hover {
  border-color: #d1d5db;
  transform: scale(1.05);
}

/* 尺寸变体 */
.avatar-xs {
  width: 24px;
  height: 24px;
  font-size: 10px;
  border-width: 1px;
}

.avatar-sm {
  width: 32px;
  height: 32px;
  font-size: 12px;
  border-width: 1px;
}

.avatar-md {
  width: 40px;
  height: 40px;
  font-size: 14px;
  border-width: 2px;
}

.avatar-lg {
  width: 56px;
  height: 56px;
  font-size: 18px;
  border-width: 2px;
}

.avatar-xl {
  width: 80px;
  height: 80px;
  font-size: 24px;
  border-width: 3px;
}

/* 形状变体 */
.avatar-circle {
  border-radius: 50%;
}

.avatar-square {
  border-radius: 0;
}

.avatar-rounded {
  border-radius: 8px;
}

/* 头像图片 */
.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
}

/* 默认头像样式 */
.avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  text-transform: uppercase;
}

/* 在线状态指示器 */
.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 25%;
  height: 25%;
  background-color: #10b981;
  border: 2px solid white;
  border-radius: 50%;
  min-width: 8px;
  min-height: 8px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .user-avatar-display:hover {
    transform: none;
  }
}

/* 暗色主题支持 */
@media (prefers-color-scheme: dark) {
  .user-avatar-display {
    border-color: #374151;
    background-color: #1f2937;
  }
  
  .user-avatar-display:hover {
    border-color: #4b5563;
  }
  
  .online-indicator {
    border-color: #1f2937;
  }
}
</style>
