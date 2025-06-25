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
  /* 渐变背景，仿照登录左侧 */
  background: linear-gradient(135deg, #7b8fff 0%, #a084ee 100%);
}
h1 {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
  color: #fff;
  letter-spacing: 2px;
}
.subtitle {
  color: #e0e7ff;
  margin-bottom: 2rem;
  font-size: 1.2rem;
  letter-spacing: 1px;
}
.nav-btns {
  display: flex;
  gap: 1.5rem;
}
.nav-btn {
  padding: 0.9rem 2.5rem;
  background: linear-gradient(180deg, #fff 80%, #e6eaff 100%);
  color: #6a6fd7;
  border-radius: 32px;
  text-decoration: none;
  font-size: 1.35rem;
  font-weight: bold;
  border: none;
  box-shadow: 0 6px 24px 0 rgba(120, 120, 200, 0.18), 0 1.5px 0 #d1d5db inset;
  transition: background 0.2s, color 0.2s, box-shadow 0.2s, transform 0.1s;
  cursor: pointer;
  letter-spacing: 2px;
  margin-bottom: 0.5rem;
}
.nav-btn:hover {
  background: linear-gradient(180deg, #f0f4ff 60%, #fff 100%);
  color: #4f4fcf;
  box-shadow: 0 12px 32px 0 rgba(120, 120, 200, 0.22), 0 2px 0 #c7d2fe inset;
  transform: translateY(-2px) scale(1.04);
}
</style>