<template>
  <div class="simple-home">
    <div class="bg-circle bg-circle1"></div>
    <div class="bg-circle bg-circle2"></div>
    <div class="bg-circle bg-circle3"></div>
    <div class="bg-circle bg-circle4"></div>
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
  width: 100vw;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f6f8ff 0%, #e9eafd 100%);
  overflow: hidden;
}

/* 圆形装饰 */
.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(120,130,255,0.12) 0%, rgba(120,130,255,0.02) 100%);
  pointer-events: none;
}
.bg-circle1 {
  width: 180px;
  height: 180px;
  left: -60px;
  top: -60px;
}
.bg-circle2 {
  width: 120px;
  height: 120px;
  right: -40px;
  top: 80px;
}
.bg-circle3 {
  width: 90px;
  height: 90px;
  left: 40px;
  bottom: 80px;
}
.bg-circle4 {
  width: 140px;
  height: 140px;
  right: -50px;
  bottom: -50px;
}

/* 内容样式 */
h1 {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
  color: #6a6fd7;
  letter-spacing: 2px;
  z-index: 1;
}
.subtitle {
  color: #4b4f6b;
  margin-bottom: 2rem;
  font-size: 1.2rem;
  letter-spacing: 1px;
  z-index: 1;
}
.nav-btns {
  display: flex;
  gap: 1.5rem;
  z-index: 1;
}
.nav-btn {
  padding: 1rem 2.8rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 25px;
  font-size: 1.25rem;
  font-weight: 600;
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.35);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(.4,2,.6,1), box-shadow 0.3s;
  margin-bottom: 0.5rem;
  position: relative;
  overflow: hidden;
  letter-spacing: 2px;
}
.nav-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.18), transparent);
  transition: left 0.5s;
}
.nav-btn:hover::before {
  left: 100%;
}
.nav-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.45);
}
</style>