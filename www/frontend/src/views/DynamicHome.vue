<!-- filepath: c:\Users\Z9495\Desktop\EcoWiki_project\EcoWiki-3\www\frontend\src\views\DynamicHome.vue -->
<template>
  <component :is="currentHome"
    @show-login="$emit('show-login')"
    @show-register="$emit('show-register')"
    @show-admin="$emit('show-admin')"
  />
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import Home from './Home.vue'
import SimpleHome from './SimpleHome.vue'

function getHomeComponent() {
  return localStorage.getItem('homeStyle') === 'simple' ? SimpleHome : Home
}

const currentHome = ref(getHomeComponent())

function updateHome() {
  currentHome.value = getHomeComponent()
}

// 只在 storage 事件（多标签页同步）和自定义事件时切换
onMounted(() => {
  window.addEventListener('storage', (e) => {
    if (e.key === 'homeStyle') updateHome()
  })
  window.addEventListener('ecowiki-home-style-change', updateHome)
})
onUnmounted(() => {
  window.removeEventListener('storage', updateHome)
  window.removeEventListener('ecowiki-home-style-change', updateHome)
})
</script>