<!-- filepath: c:\Users\Z9495\Desktop\EcoWiki_project\EcoWiki-3\www\frontend\src\views\SimpleHome.vue -->
<template>
  <div class="simple-home">
    <h1>EcoWiki</h1>
    <p class="subtitle">极简风格，专注知识本身</p>
    <div class="nav-btns">
      <button class="nav-btn" @click="$emit('show-login')">登录</button>
      <button class="nav-btn" @click="$emit('show-register')">注册</button>
      <button class="nav-btn" @click="$emit('show-admin')">管理设置</button>
    </div>
  </div>
</template>

<script setup lang="ts">
const emit = defineEmits(['show-login', 'show-register', 'show-admin'])
</script>

<style scoped>
.simple-home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
}
h1 {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
}
.subtitle {
  color: #718096;
  margin-bottom: 2rem;
}
.nav-btns {
  display: flex;
  gap: 1.5rem;
}
.nav-btn {
  padding: 0.7rem 2rem;
  background: #4f8cff;
  color: #fff;
  border-radius: 8px;
  text-decoration: none;
  font-size: 1.1rem;
  transition: background 0.2s;
  border: none;
  cursor: pointer;
}
.nav-btn:hover {
  background: #2563eb;
}
</style>