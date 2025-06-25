<!-- filepath: c:\Users\Z9495\Desktop\EcoWiki_project\EcoWiki-3\www\frontend\src\views\DynamicHome.vue -->
<template>
  <component
    :is="currentHome"
    @show-login="emit('show-login')"
    @show-register="emit('show-register')"
    @show-admin="emit('show-admin')"
    @logout="emit('logout')"
  />
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import ClassicHome from './ClassicHome.vue'
import SimpleHome from './SimpleHome.vue'

const emit = defineEmits(['show-login', 'show-register', 'show-admin', 'logout'])

function getHomeComponent() {
  return localStorage.getItem('homeStyle') === 'simple' ? SimpleHome : ClassicHome
}
const currentHome = ref(getHomeComponent())

function updateHome() {
  console.log('updateHome called, homeStyle:', localStorage.getItem('homeStyle'))
  currentHome.value = getHomeComponent()
}

onMounted(() => {
  window.addEventListener('ecowiki-home-style-change', updateHome)
})
onUnmounted(() => {
  window.removeEventListener('ecowiki-home-style-change', updateHome)
})
</script>