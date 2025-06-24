<!-- filepath: c:\Users\Z9495\Desktop\EcoWiki_project\EcoWiki-3\www\frontend\src\views\Home.vue -->
<template>
  <div class="home">
    <h1>EcoWiki 首页</h1>
    <div class="navigation">
      <router-link to="/login" class="nav-link">登录</router-link>
      <router-link to="/register" class="nav-link">注册</router-link>
      <router-link to="/admin" class="nav-link">管理设置</router-link>
    </div>
  </div>
</template>

<style scoped>
.nav-link {
  display: inline-block;
  margin: 0 10px;
  padding: 8px 16px;
  background: #007bff;
  color: white;
  text-decoration: none;
  border-radius: 4px;
}
.nav-link:hover {
  background: #0056b3;
}
</style>