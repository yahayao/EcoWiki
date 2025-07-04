<template>
  <div class="floating-action-buttons">
    <div 
      class="action-button" 
      :class="{ active: isViewMode }"
      @click="handleView" 
      title="查看文章"
    >
      <div class="button-icon">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
          <circle cx="12" cy="12" r="3"/>
        </svg>
      </div>
      <span class="button-text">查看</span>
    </div>
    
    <div 
      class="action-button"
      :class="{ active: isEditMode, disabled: !canEdit }"
      @click="handleEdit" 
      title="编辑文章"
    >
      <div class="button-icon">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
          <path d="m18.5 2.5 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
        </svg>
      </div>
      <span class="button-text">编辑</span>
    </div>
    
    <div class="action-button" @click="handleHistory" title="查看历史">
      <div class="button-icon">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/>
          <polyline points="12,6 12,12 16,14"/>
        </svg>
      </div>
      <span class="button-text">历史</span>
    </div>
    
    <div 
      class="action-button" 
      :class="{ active: isFavorited, disabled: !isLoggedIn }"
      @click="handleFavorite" 
      title="收藏文章"
    >
      <div class="button-icon">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polygon points="12,2 15.09,8.26 22,9.27 17,14.14 18.18,21.02 12,17.77 5.82,21.02 7,14.14 2,9.27 8.91,8.26"/>
        </svg>
      </div>
      <span class="button-text">{{ isFavorited ? '已收藏' : '收藏' }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

interface Props {
  articleId: number
  articleTitle?: string
  mode?: 'view' | 'edit' | 'history'
  isFavorited?: boolean
  canEdit?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'view',
  isFavorited: false,
  canEdit: true
})

const emit = defineEmits<{
  view: []
  edit: []
  history: []
  favorite: [favorited: boolean]
}>()

const router = useRouter()
const { isLoggedIn } = useAuth()

const isViewMode = computed(() => props.mode === 'view')
const isEditMode = computed(() => props.mode === 'edit')

const handleView = () => {
  if (!isViewMode.value) {
    emit('view')
    if (props.articleTitle) {
      router.push(`/wiki/${props.articleTitle}`)
    } else {
      // 备用方案：使用文章ID
      router.push(`/wiki/${props.articleId}`)
    }
  }
}

const handleEdit = () => {
  if (props.canEdit) {
    emit('edit')
  }
}

const handleHistory = () => {
  emit('history')
  // TODO: 实现历史记录功能
  console.log('查看文章历史')
}

const handleFavorite = () => {
  if (!isLoggedIn.value) {
    return
  }
  emit('favorite', !props.isFavorited)
}
</script>

<style scoped>
.floating-action-buttons {
  position: fixed;
  right: 30px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 1000;
}

.action-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  background: #ffffff;
  border-radius: 50%;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  user-select: none;
  position: relative;
  overflow: hidden;
}

.action-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 50%;
}

.action-button:hover {
  transform: translateX(-8px) scale(1.1);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.action-button.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}

.action-button.active::before {
  opacity: 1;
}

.action-button:hover .button-icon {
  color: white;
  transform: scale(1.1);
}

.action-button:hover .button-text {
  color: white;
}

.action-button:active {
  transform: translateX(-4px) scale(1.05);
}

.action-button.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
  transform: translateX(-8px) scale(1.05);
}

.action-button.active .button-icon {
  color: white;
}

.action-button.active .button-text {
  color: white;
}

.button-icon {
  position: relative;
  z-index: 1;
  color: #4a5568;
  transition: all 0.3s ease;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.button-icon svg {
  stroke: currentColor;
}

.button-text {
  position: relative;
  z-index: 1;
  font-size: 9px;
  color: #718096;
  font-weight: 600;
  line-height: 1;
  transition: all 0.3s ease;
  text-align: center;
}

/* 特殊样式：收藏按钮 */
.action-button:nth-child(4):hover .button-icon svg {
  fill: #ffd700;
  stroke: #ffd700;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .floating-action-buttons {
    right: 20px;
  }
  
  .action-button {
    width: 50px;
    height: 50px;
  }
  
  .button-icon {
    margin-bottom: 3px;
  }
  
  .button-icon svg {
    width: 16px;
    height: 16px;
  }
  
  .button-text {
    font-size: 8px;
  }
}

@media (max-width: 768px) {
  .floating-action-buttons {
    right: 15px;
    top: auto;
    bottom: 90px;
    transform: none;
    flex-direction: row;
    justify-content: center;
    left: 50%;
    transform: translateX(-50%);
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    padding: 12px;
    border-radius: 25px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    gap: 12px;
  }
  
  .action-button {
    width: 45px;
    height: 45px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
  
  .action-button:hover {
    transform: translateY(-2px) scale(1.05);
  }
  
  .button-icon {
    margin-bottom: 2px;
  }
  
  .button-icon svg {
    width: 14px;
    height: 14px;
  }
  
  .button-text {
    font-size: 7px;
  }
}

@media (max-width: 480px) {
  .floating-action-buttons {
    padding: 10px;
    gap: 8px;
  }
  
  .action-button {
    width: 40px;
    height: 40px;
  }
  
  .button-icon svg {
    width: 12px;
    height: 12px;
  }
  
  .button-text {
    font-size: 6px;
  }
}
</style>
