<!-- filepath: c:\Users\Z9495\Desktop\EcoWiki_project\EcoWiki-3\www\frontend\src\components\admin\AdminLayout.vue -->
<template>
  <div class="admin-layout">
    <aside class="admin-sider">
      <div class="logo">
        <span class="logo-icon">🌿</span>
        EcoWiki 管理后台
      </div>
      <nav class="nav-menu">
        <ul>
          <li :class="{active: $route.name==='AdminSettings'}">
            <router-link to="/admin/settings">
              <span class="nav-icon">⚙️</span>
              系统设置
            </router-link>
          </li>
          <li :class="{active: $route.name==='AdminUsers'}">
            <router-link to="/admin/users">
              <span class="nav-icon">👥</span>
              用户管理
            </router-link>
          </li>
        </ul>
      </nav>
    </aside>
    <main class="admin-main">
      <div class="admin-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
// 移除不必要的代码，简化组件
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  background: #f8fafc;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.admin-sider {
  width: 280px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0,0,0,0.1);
}

.logo {
  padding: 24px 20px;
  text-align: center;
  font-size: 1.2rem;
  font-weight: bold;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.logo-icon {
  font-size: 1.5rem;
}

.nav-menu {
  flex: 1;
  padding: 20px 0;
}

.nav-menu ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-menu li {
  margin-bottom: 4px;
}

.nav-menu a {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  color: rgba(255,255,255,0.8);
  text-decoration: none;
  transition: all 0.3s ease;
  border-radius: 0;
}

.nav-menu a:hover {
  background: rgba(255,255,255,0.1);
  color: #fff;
}

.nav-menu li.active a,
.nav-menu a.router-link-exact-active {
  background: rgba(255,255,255,0.15);
  color: #fff;
  font-weight: 600;
}

.nav-icon {
  font-size: 1.1rem;
  width: 20px;
  text-align: center;
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.admin-content {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

@media (max-width: 768px) {
  .admin-sider {
    width: 240px;
  }
  
  .logo {
    font-size: 1rem;
    padding: 16px;
  }
  
  .nav-menu a {
    padding: 10px 16px;
    font-size: 0.9rem;
  }
}
</style>