<!-- AvatarUpload.vue -->
<template>
  <div class="avatar-upload-container">
    <!-- 头像显示区域 -->
    <div class="avatar-display">
      <div class="avatar-wrapper" @click="triggerUpload">
        <img 
          :src="displayAvatarUrl" 
          :alt="username + '的头像'"
          class="avatar-image"
          @error="handleImageError"
        />
        <div class="avatar-overlay">
          <div class="upload-icon">
            <svg viewBox="0 0 24 24" class="icon">
              <path d="M9,16.17L4.83,12L3.41,13.41L9,19L21,7L19.59,5.59L9,16.17Z"/>
            </svg>
            <span>更换头像</span>
          </div>
        </div>
        
        <!-- 上传进度指示器 -->
        <div v-if="uploading" class="upload-progress">
          <div class="progress-circle">
            <svg viewBox="0 0 36 36" class="circular-chart">
              <path 
                class="circle-bg"
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
              />
              <path 
                class="circle"
                :stroke-dasharray="`${uploadProgress}, 100`"
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
              />
            </svg>
            <div class="percentage">{{ Math.round(uploadProgress) }}%</div>
          </div>
        </div>
      </div>
      
      <!-- 头像信息 -->
      <div class="avatar-info">
        <p class="avatar-tips">点击头像更换图片</p>
        <p class="avatar-formats">支持 JPG、PNG、GIF、WEBP 格式</p>
        <p class="avatar-size">文件大小不超过 5MB</p>
      </div>
    </div>
    
    <!-- 隐藏的文件输入框 -->
    <input
      ref="fileInput"
      type="file"
      accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
      style="display: none"
      @change="handleFileSelect"
    />
    
    <!-- 上传状态提示 -->
    <div v-if="uploadMessage" :class="['upload-message', uploadMessageType]">
      {{ uploadMessage }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuth } from '@/composables/useAuth'
import { api } from '@/api/index'

// Props
interface Props {
  username?: string
  currentAvatarUrl?: string
  size?: 'small' | 'medium' | 'large'
}

const props = withDefaults(defineProps<Props>(), {
  username: '',
  currentAvatarUrl: '',
  size: 'medium'
})

// Emits
const emit = defineEmits<{
  uploadSuccess: [result: {
    avatarUrl: string
    fullUrl: string
    fileName: string
    uploadTime: string
  }]
  uploadError: [error: string]
}>()

// Composables
const { user, setUser } = useAuth()

// Reactive data
const fileInput = ref<HTMLInputElement>()
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadMessage = ref('')
const uploadMessageType = ref<'success' | 'error' | 'info'>('info')

// 默认头像URL - 使用Vite的静态资源导入
import defaultAvatarSvg from '@/assets/images/default-avatar.svg'

const defaultAvatarUrl = defaultAvatarSvg

// 计算属性
const displayAvatarUrl = computed(() => {
  const avatarUrl = props.currentAvatarUrl || user.value?.avatarUrl
  
  if (!avatarUrl) {
    return defaultAvatarUrl
  }
  
  // 如果是完整URL，直接使用
  if (avatarUrl.startsWith('http')) {
    return avatarUrl
  }
  
  // 如果是相对路径，拼接服务器地址
  // 支持新的静态资源路径 /uploads/avatars/ 和旧的 /avatars/ 路径
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  if (avatarUrl.startsWith('/uploads/avatars/') || avatarUrl.startsWith('/avatars/')) {
    return baseUrl + avatarUrl
  } else {
    // 兼容性处理：如果只是文件名，添加完整路径
    return baseUrl + '/uploads/avatars/' + avatarUrl
  }
})

const username = computed(() => {
  return props.username || user.value?.username || '用户'
})

// 方法
const triggerUpload = () => {
  if (uploading.value) return
  fileInput.value?.click()
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file) return
  
  // 客户端验证
  const validationError = validateFile(file)
  if (validationError) {
    showMessage(validationError, 'error')
    return
  }
  
  uploadAvatar(file)
}

const validateFile = (file: File): string | null => {
  // 检查文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    return '只支持 JPG、PNG、GIF、WEBP 格式的图片'
  }
  
  // 检查文件大小 (5MB)
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    return '文件大小不能超过 5MB'
  }
  
  return null
}

const uploadAvatar = async (file: File) => {
  try {
    uploading.value = true
    uploadProgress.value = 0
    showMessage('正在上传头像...', 'info')
    
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await api.post('/avatar/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        if (progressEvent.total) {
          uploadProgress.value = (progressEvent.loaded / progressEvent.total) * 100
        }
      }
    })
    
    if (response.data.code === 200) {
      const result = response.data.data
      
      // 更新本地用户信息
      if (user.value) {
        const updatedUser = { ...user.value, avatarUrl: result.avatarUrl }
        const token = localStorage.getItem('token') || ''
        const refreshToken = localStorage.getItem('refreshToken') || ''
        setUser(updatedUser, token, refreshToken)
      }
      
      showMessage('头像上传成功！', 'success')
      emit('uploadSuccess', result)
    } else {
      throw new Error(response.data.message || '上传失败')
    }
    
  } catch (error: any) {
    const errorMessage = error.response?.data?.message || error.message || '上传失败'
    showMessage(errorMessage, 'error')
    emit('uploadError', errorMessage)
  } finally {
    uploading.value = false
    uploadProgress.value = 0
    
    // 清空文件输入框
    if (fileInput.value) {
      fileInput.value.value = ''
    }
  }
}

const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement
  target.src = defaultAvatarUrl
}

const showMessage = (message: string, type: 'success' | 'error' | 'info') => {
  uploadMessage.value = message
  uploadMessageType.value = type
  
  // 3秒后自动清除消息
  setTimeout(() => {
    uploadMessage.value = ''
  }, 3000)
}

// 生命周期
onMounted(() => {
  console.log('头像上传组件已加载')
})
</script>

<style scoped>
.avatar-upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.avatar-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 3px solid #e2e8f0;
}

.avatar-wrapper:hover {
  border-color: #667eea;
  transform: scale(1.05);
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.3s ease;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.upload-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: white;
  font-size: 12px;
  font-weight: 500;
}

.upload-icon .icon {
  width: 24px;
  height: 24px;
  fill: currentColor;
}

.upload-progress {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-circle {
  position: relative;
  width: 60px;
  height: 60px;
}

.circular-chart {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.circle-bg {
  fill: none;
  stroke: #e2e8f0;
  stroke-width: 3;
}

.circle {
  fill: none;
  stroke: #667eea;
  stroke-width: 3;
  stroke-linecap: round;
  transition: stroke-dasharray 0.2s ease;
}

.percentage {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 12px;
  font-weight: 600;
  color: #667eea;
}

.avatar-info {
  text-align: center;
  color: #64748b;
}

.avatar-tips {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  color: #374151;
}

.avatar-formats,
.avatar-size {
  font-size: 12px;
  margin-bottom: 2px;
}

.upload-message {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  text-align: center;
  min-width: 200px;
}

.upload-message.success {
  background: #d1fae5;
  color: #065f46;
  border: 1px solid #a7f3d0;
}

.upload-message.error {
  background: #fee2e2;
  color: #991b1b;
  border: 1px solid #fca5a5;
}

.upload-message.info {
  background: #dbeafe;
  color: #1e40af;
  border: 1px solid #93c5fd;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .avatar-wrapper {
    width: 100px;
    height: 100px;
  }
  
  .upload-icon {
    font-size: 10px;
  }
  
  .upload-icon .icon {
    width: 20px;
    height: 20px;
  }
}
</style>
