<template>
  <div class="app-container">
    <LoginPanel />
  </div>
</template>

<script setup>
import LoginPanel from './components/LoginPanel.vue'
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
</style>