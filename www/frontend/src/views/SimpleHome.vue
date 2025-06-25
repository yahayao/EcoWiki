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
defineEmits(['show-login', 'show-register', 'show-admin'])
</script>

<style scoped>
.simple-home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #181f2a !important; /* 深色背景，强制覆盖全局 */
}
h1 {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
  color: #fff;
}
.subtitle {
  color: #b3b8c5;
  margin-bottom: 2rem;
}
.nav-btns {
  display: flex;
  gap: 1.5rem;
}
.nav-btn {
  padding: 0.7rem 2rem;
  background: #fff;
  color: #181f2a;
  border-radius: 8px;
  text-decoration: none;
  font-size: 1.1rem;
  transition: background 0.2s, color 0.2s;
  border: none;
  font-weight: bold;
  box-shadow: none;
}
.nav-btn:hover {
  background: #4f8cff;
  color: #fff;
}
</style>