<template>
  <div class="app-container">
    <div class="view-wrapper" :class="{ 'switching': isSwitching }">
      <LoginPanel 
        v-if="currentView === 'login'" 
        @switchToRegister="switchToRegister" 
      />
      <RegisterPanel 
        v-else 
        @switchToLogin="switchToLogin" 
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import LoginPanel from './components/LoginPanel.vue'
import RegisterPanel from './components/RegisterPanel.vue'

const currentView = ref('login')
const isSwitching = ref(false)

const switchToRegister = () => {
  isSwitching.value = true
  setTimeout(() => {
    currentView.value = 'register'
    isSwitching.value = false
  }, 300) // 300ms 延时
}

const switchToLogin = () => {
  isSwitching.value = true
  setTimeout(() => {
    currentView.value = 'login'
    isSwitching.value = false
  }, 300) // 300ms 延时
}
</script>

<style scoped>
.app-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.view-wrapper {
  transition: all 0.3s ease-in-out;
  transform: scale(1);
  opacity: 1;
}

.view-wrapper.switching {
  transform: scale(0.95);
  opacity: 0;
}
</style>