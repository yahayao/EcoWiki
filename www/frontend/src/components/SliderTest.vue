<template>
  <div class="slider-test-page">
    <div class="container">
      <h1>滑块验证测试</h1>
      
      <div class="test-section">
        <h2>新版滑块（严丝合缝版本）</h2>
        <div class="slider-wrapper">
          <SliderCaptcha 
            ref="sliderRef"
            @success="handleSuccess"
            @reset="handleReset"
          />
        </div>
        
        <div class="status-info">
          <p>状态: {{ status }}</p>
          <p>验证次数: {{ attemptCount }}</p>
          <p v-if="debugInfo.trackWidth">轨道宽度: {{ debugInfo.trackWidth }}px</p>
          <p v-if="debugInfo.maxPosition">最大位置: {{ debugInfo.maxPosition }}px</p>
          <p v-if="debugInfo.buttonSize">按钮大小: {{ debugInfo.buttonSize }}px</p>
          <p v-if="debugInfo.currentPosition">当前位置: {{ debugInfo.currentPosition }}px</p>
          <p v-if="debugInfo.fillWidth">填充宽度: {{ debugInfo.fillWidth }}px</p>
        </div>
        
        <div class="button-group">
          <button @click="resetSlider" class="reset-btn">重置滑块</button>
          <button @click="showModal = true" class="test-modal-btn">在弹窗中测试</button>
        </div>
      </div>
      
      <div class="test-section">
        <h2>使用说明</h2>
        <ul class="feature-list">
          <li>✅ 滑块与轨道严丝合缝，无间隙</li>
          <li>✅ 滑动更加顺滑，支持鼠标和触摸</li>
          <li>✅ 简洁的设计，优雅的动画效果</li>
          <li>✅ 响应式设计，适配各种屏幕尺寸</li>
          <li>✅ 自动重置功能，验证失败时平滑回弹</li>
          <li>✅ 成功验证后的反馈动画</li>
        </ul>
      </div>
    </div>
    
    <!-- 测试弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>弹窗中的滑块测试</h3>
          <button @click="closeModal" class="close-btn">×</button>
        </div>
        <div class="modal-body">
          <p>这里测试滑块在弹窗中的表现：</p>
          <SliderCaptcha 
            ref="modalSliderRef"
            @success="handleModalSuccess"
            @reset="handleModalReset"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import SliderCaptcha from './SliderCaptcha.vue'

const sliderRef = ref<InstanceType<typeof SliderCaptcha> | null>(null)
const modalSliderRef = ref<InstanceType<typeof SliderCaptcha> | null>(null)
const status = ref('等待验证')
const attemptCount = ref(0)
const showModal = ref(false)
const debugInfo = ref({
  trackWidth: 0,
  maxPosition: 0,
  buttonSize: 0,
  currentPosition: 0,
  fillWidth: 0
})

const handleSuccess = () => {
  status.value = '验证成功！'
  attemptCount.value++
  console.log('滑块验证成功')
  
  // 3秒后自动重置
  setTimeout(() => {
    resetSlider()
  }, 3000)
}

const handleReset = () => {
  status.value = '验证重置，请重新拖动'
  console.log('滑块已重置')
}

const resetSlider = () => {
  if (sliderRef.value) {
    sliderRef.value.reset()
    status.value = '等待验证'
  }
}

const closeModal = () => {
  showModal.value = false
  if (modalSliderRef.value) {
    modalSliderRef.value.reset()
  }
}

const handleModalSuccess = () => {
  console.log('弹窗中的滑块验证成功')
  setTimeout(() => {
    closeModal()
  }, 1500)
}

const handleModalReset = () => {
  console.log('弹窗中的滑块已重置')
}
</script>

<style scoped>
.slider-test-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.container {
  max-width: 800px;
  margin: 0 auto;
}

h1 {
  text-align: center;
  color: white;
  font-size: 2.5rem;
  margin-bottom: 40px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.test-section {
  background: white;
  border-radius: 16px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.test-section h2 {
  color: #1a202c;
  font-size: 1.5rem;
  margin-bottom: 20px;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 10px;
}

.slider-wrapper {
  margin: 30px 0;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.status-info {
  background: #f0f9ff;
  border: 1px solid #0ea5e9;
  border-radius: 8px;
  padding: 15px;
  margin: 20px 0;
}

.status-info p {
  margin: 5px 0;
  color: #0c4a6e;
  font-weight: 500;
}

.button-group {
  display: flex;
  gap: 15px;
  margin-top: 20px;
}

.reset-btn, .test-modal-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.reset-btn {
  background: #ef4444;
  color: white;
}

.reset-btn:hover {
  background: #dc2626;
  transform: translateY(-1px);
}

.test-modal-btn {
  background: #3b82f6;
  color: white;
}

.test-modal-btn:hover {
  background: #2563eb;
  transform: translateY(-1px);
}

.feature-list {
  list-style: none;
  padding: 0;
}

.feature-list li {
  padding: 8px 0;
  color: #374151;
  font-size: 1.1rem;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 16px;
  max-width: 500px;
  width: 90%;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.modal-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.2rem;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.2s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.modal-body {
  padding: 30px;
}

.modal-body p {
  margin-bottom: 20px;
  color: #374151;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .container {
    padding: 0 10px;
  }
  
  h1 {
    font-size: 2rem;
  }
  
  .test-section {
    padding: 20px;
  }
  
  .button-group {
    flex-direction: column;
  }
  
  .modal-content {
    width: 95%;
  }
  
  .modal-body {
    padding: 20px;
  }
}
</style>
