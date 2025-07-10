<template>
  <div class="slider-captcha">
    <div class="slider-track" ref="sliderTrack">
      <!-- 滑块填充背景 -->
      <div class="slider-fill" :style="{ width: fillWidth + 'px' }"></div>
      
      <!-- 拖拽按钮 -->
      <div 
        class="slider-button"
        :class="{ 
          'dragging': isDragging, 
          'completed': isCompleted,
          'resetting': isResetting
        }"
        :style="{ transform: `translateX(${buttonPosition}px) translateY(-50%)` }"
        @mousedown="handleMouseDown"
        @touchstart="handleTouchStart"
      >
        <svg v-if="!isCompleted" class="slider-icon" viewBox="0 0 24 24" fill="none">
          <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <svg v-else class="slider-icon completed" viewBox="0 0 24 24" fill="none">
          <path d="M20 6L9 17L4 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      
      <!-- 提示文字 -->
      <div class="slider-text" v-if="!isCompleted">
        按住滑块，拖动到最右边
      </div>
      <div class="slider-text completed" v-else>
        验证成功
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'

// 定义事件
const emit = defineEmits<{
  success: []
  reset: []
}>()

// 响应式数据
const sliderTrack = ref<HTMLElement | null>(null)
const buttonPosition = ref(0)
const fillWidth = ref(0)
const isDragging = ref(false)
const isCompleted = ref(false)
const isResetting = ref(false)

// 拖拽相关数据
let startX = 0
let startPosition = 0
let maxPosition = 0
let trackWidth = 0
let currentButtonSize = 50 // 当前按钮大小，会根据响应式调整
const trackPadding = 2 // 轨道内边距，减小以更贴合

// 获取当前按钮大小
const getCurrentButtonSize = () => {
  if (window.innerWidth <= 480) {
    return 46
  } else if (window.innerWidth <= 768) {
    return 48
  } else {
    return 50
  }
}

// 计算轨道尺寸
const calculateDimensions = () => {
  if (!sliderTrack.value) return
  
  currentButtonSize = getCurrentButtonSize()
  trackWidth = sliderTrack.value.offsetWidth
  
  // 精确计算最大位置：
  // 滑块初始位置在left: 2px，要让滑块右边缘贴合轨道右边缘(减去2px边距)
  // 所以最大位置 = 轨道宽度 - 按钮宽度 - 4px(左右边距各2px)
  maxPosition = trackWidth - currentButtonSize - 4
  
  // 为了让用户更容易完成验证，成功阈值设为maxPosition的95%
  // 但实际显示时会snap到真正的maxPosition
}

// 更新填充宽度 - 实时填充滑过的区域
const updateFillWidth = (position: number) => {
  // 由于CSS中.slider-fill已经设置了left: 2px，
  // 这里填充宽度就是按钮位置 + 按钮宽度
  let newFillWidth = position + currentButtonSize
  
  // 确保填充宽度不超出轨道边界
  const maxFillWidth = trackWidth - 2 // 减去右边距
  if (newFillWidth > maxFillWidth) {
    newFillWidth = maxFillWidth
  }
  
  fillWidth.value = newFillWidth
}

// 鼠标按下事件
const handleMouseDown = (e: MouseEvent) => {
  if (isCompleted.value || isResetting.value) return
  
  e.preventDefault()
  
  isDragging.value = true
  startX = e.clientX
  startPosition = buttonPosition.value
  
  document.body.style.userSelect = 'none'
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

// 触摸开始事件
const handleTouchStart = (e: TouchEvent) => {
  if (isCompleted.value || isResetting.value) return
  
  e.preventDefault()
  
  isDragging.value = true
  startX = e.touches[0].clientX
  startPosition = buttonPosition.value
  
  document.body.style.userSelect = 'none'
  document.addEventListener('touchmove', handleTouchMove, { passive: false })
  document.addEventListener('touchend', handleTouchEnd)
}

// 拖拽移动事件
const handleMouseMove = (e: MouseEvent) => {
  if (!isDragging.value) return
  handleMove(e.clientX)
}

const handleTouchMove = (e: TouchEvent) => {
  if (!isDragging.value) return
  e.preventDefault()
  handleMove(e.touches[0].clientX)
}

// 统一的移动处理 - 直接同步更新，无延迟
const handleMove = (clientX: number) => {
  if (isCompleted.value) return // 已完成则不再处理移动
  
  const deltaX = clientX - startX
  let newPosition = startPosition + deltaX
  
  // 边界限制
  if (newPosition < 0) newPosition = 0
  if (newPosition > maxPosition) newPosition = maxPosition
  
  // 立即同步更新位置和填充
  buttonPosition.value = newPosition
  updateFillWidth(newPosition)
  
  // 检查是否完成（达到95%就算成功，但会snap到100%位置）
  if (newPosition >= maxPosition * 0.95) {
    // 立即将滑块snap到最右端位置
    buttonPosition.value = maxPosition
    updateFillWidth(maxPosition)
    completeVerification()
  }
}

// 鼠标释放事件
const handleMouseUp = () => {
  if (!isDragging.value || isCompleted.value) return // 增加完成状态检查
  
  isDragging.value = false
  document.body.style.userSelect = ''
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
  
  // 只有在未完成验证时才重置位置
  if (!isCompleted.value) {
    resetSlider()
  }
}

// 触摸结束事件
const handleTouchEnd = () => {
  if (!isDragging.value || isCompleted.value) return // 增加完成状态检查
  
  isDragging.value = false
  document.body.style.userSelect = ''
  document.removeEventListener('touchmove', handleTouchMove)
  document.removeEventListener('touchend', handleTouchEnd)
  
  // 只有在未完成验证时才重置位置
  if (!isCompleted.value) {
    resetSlider()
  }
}

// 完成验证
const completeVerification = () => {
  if (isCompleted.value) return // 防止重复调用
  
  isCompleted.value = true
  isDragging.value = false
  
  // 确保滑块精确位置在最右端
  buttonPosition.value = maxPosition
  fillWidth.value = trackWidth - 2 // 完全填充到轨道右边缘
  
  // 立即清理所有事件监听器，防止后续的重置
  document.body.style.userSelect = ''
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
  document.removeEventListener('touchmove', handleTouchMove)
  document.removeEventListener('touchend', handleTouchEnd)
  
  emit('success')
}

// 重置滑块
const resetSlider = () => {
  if (isResetting.value || isCompleted.value) return // 防止重复重置和已完成时重置
  
  isResetting.value = true
  const startPos = buttonPosition.value
  const startFill = fillWidth.value
  const duration = 300
  const startTime = Date.now()
  
  const animate = () => {
    // 再次检查完成状态，如果中途完成则停止重置动画
    if (isCompleted.value) {
      isResetting.value = false
      return
    }
    
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)
    
    // 使用缓动函数
    const easeProgress = 1 - Math.pow(1 - progress, 3)
    
    buttonPosition.value = startPos * (1 - easeProgress)
    fillWidth.value = startFill * (1 - easeProgress)
    
    if (progress < 1) {
      requestAnimationFrame(animate)
    } else {
      buttonPosition.value = 0
      fillWidth.value = 0
      isResetting.value = false
      emit('reset')
    }
  }
  
  requestAnimationFrame(animate)
}

// 公开的重置方法
const reset = () => {
  if (isResetting.value) return // 防止重复重置
  
  isCompleted.value = false
  resetSlider()
}

// 窗口大小变化处理
const handleResize = () => {
  calculateDimensions()
  if (!isCompleted.value) {
    buttonPosition.value = 0
    fillWidth.value = 0
  }
}

// 生命周期
onMounted(() => {
  nextTick(() => {
    calculateDimensions()
    window.addEventListener('resize', handleResize)
  })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
  document.removeEventListener('touchmove', handleTouchMove)
  document.removeEventListener('touchend', handleTouchEnd)
})

// 暴露方法给父组件
defineExpose({
  reset,
  isCompleted
})
</script>

<style scoped>
.slider-captcha {
  width: 100%;
  user-select: none;
}

.slider-track {
  position: relative;
  width: 100%;
  height: 54px; /* 增加高度以容纳正方形滑块 */
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  border-radius: 8px; /* 改为小圆角，更适合正方形滑块 */
  border: 2px solid #e2e8f0;
  overflow: hidden;
  box-shadow: 
    inset 0 2px 4px rgba(0, 0, 0, 0.06),
    0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
}

.slider-track:hover {
  border-color: #cbd5e1;
  box-shadow: 
    inset 0 2px 4px rgba(0, 0, 0, 0.06),
    0 1px 3px rgba(0, 0, 0, 0.1),
    0 0 0 3px rgba(59, 130, 246, 0.1);
}

.slider-fill {
  position: absolute;
  top: 2px; /* 与轨道边框对齐 */
  left: 2px; /* 与轨道边框对齐 */
  height: calc(100% - 4px); /* 减去边框高度 */
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 6px; /* 与轨道圆角匹配 */
  opacity: 0.9;
}

.slider-button {
  position: absolute;
  top: 50%; /* 垂直居中 */
  left: 2px; /* 初始位置与轨道边框对齐 */
  transform: translateY(-50%); /* 垂直居中偏移 */
  width: 50px; /* 正方形宽度 */
  height: 50px; /* 正方形高度 */
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 6px; /* 正方形圆角，与轨道匹配 */
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  box-shadow: 
    0 2px 8px rgba(0, 0, 0, 0.15),
    0 1px 3px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  z-index: 10;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.slider-button:hover {
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.2),
    0 2px 6px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.slider-button:hover:not(.dragging):not(.completed) {
  transform: translateY(-50%) scale(1.02);
}

.slider-button.dragging {
  cursor: grabbing;
  transition: box-shadow 0.1s ease;
  box-shadow: 
    0 6px 16px rgba(0, 0, 0, 0.25),
    0 3px 8px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.slider-button.completed {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  cursor: default;
  animation: success-bounce 0.5s ease-out;
}

.slider-button.resetting {
  cursor: default;
}

@keyframes success-bounce {
  0% { transform: translateY(-50%) scale(1); }
  50% { transform: translateY(-50%) scale(1.1); }
  100% { transform: translateY(-50%) scale(1); }
}

.slider-icon {
  width: 20px;
  height: 20px;
  color: #64748b;
  transition: all 0.2s ease;
}

.slider-button:hover .slider-icon {
  color: #3b82f6;
}

.slider-button.dragging .slider-icon {
  color: #1d4ed8;
}

.slider-icon.completed {
  color: white;
}

.slider-text {
  position: absolute;
  top: 50%;
  left: 65px; /* 调整位置以适应正方形滑块 */
  right: 20px;
  transform: translateY(-50%);
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
  pointer-events: none;
  text-align: center;
  transition: all 0.3s ease;
  z-index: 5;
}

.slider-text.completed {
  color: #10b981;
  font-weight: 600;
  left: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .slider-track {
    height: 52px;
  }
  
  .slider-button {
    width: 48px;
    height: 48px;
  }
  
  .slider-text {
    font-size: 13px;
    left: 60px;
  }
}

@media (max-width: 480px) {
  .slider-track {
    height: 50px;
  }
  
  .slider-button {
    width: 46px;
    height: 46px;
  }
  
  .slider-text {
    font-size: 12px;
    left: 55px;
  }
  
  .slider-icon {
    width: 18px;
    height: 18px;
  }
}

/* 禁用文本选择 */
.slider-captcha * {
  -webkit-touch-callout: none;
  -webkit-user-select: none;
  -khtml-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}

/* 触摸设备优化 */
@media (hover: none) and (pointer: coarse) {
  .slider-button {
    width: 50px;
    height: 50px;
  }
  
  .slider-track {
    height: 54px;
  }
}
</style>
