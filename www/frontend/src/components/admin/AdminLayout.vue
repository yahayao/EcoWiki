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
          <li :class="{active: $route.name==='AdminRoles'}">
            <router-link to="/admin/roles">
              <span class="nav-icon">�</span>
              权限管理
            </router-link>
          </li>
        </ul>
      </nav>
    </aside>
    <main class="admin-main">
      <!-- 统一的应用按钮区域 -->
      <div class="admin-header">
        <div class="header-title">
          <h2 v-if="$route.name === 'AdminSettings'">系统设置</h2>
          <h2 v-else-if="$route.name === 'AdminUsers'">用户管理</h2>
          <h2 v-else-if="$route.name === 'AdminRoles'">权限管理</h2>
          <p class="header-subtitle">管理用户权限和系统配置</p>
        </div>
        <button 
          class="apply-btn-global" 
          :disabled="applying || !hasPendingChanges" 
          @click="applyAllSettings"
        >
          <span v-if="applying" class="loading-spinner"></span>
          <span v-else>应用</span>
        </button>
      </div>
      
      <div class="admin-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useAdminUserStore } from '../../stores/adminUserStore'
import toast from '../../utils/toast'

const adminUserStore = useAdminUserStore()
const { pendingUserChanges } = storeToRefs(adminUserStore)

const applying = ref(false)

// 计算是否有待处理的变更
const hasPendingChanges = computed(() => {
  return Object.keys(pendingUserChanges.value).length > 0
})

// 应用所有设置
const applyAllSettings = async () => {
  applying.value = true
  try {
    // 应用用户管理的变更
    if (Object.keys(pendingUserChanges.value).length > 0) {
      await adminUserStore.applyAllUserChanges()
    }
    
    // 如果在系统设置页面，应用系统设置
    // 触发首页风格变更事件，让系统设置页面保存设置
    const homeStyle = localStorage.getItem('homeStyle') || 'classic'
    localStorage.setItem('homeStyle', homeStyle)
    window.dispatchEvent(new Event('ecowiki-home-style-change'))
    
    toast.success('所有设置已应用')
  } catch (e: any) {
    toast.error(e.message || '应用设置失败')
  } finally {
    applying.value = false
  }
}
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

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: white;
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.header-title h2 {
  margin: 0;
  color: #1a202c;
  font-size: 1.5rem;
  font-weight: 600;
}

.header-subtitle {
  margin: 4px 0 0 0;
  color: #718096;
  font-size: 0.875rem;
}

.apply-btn-global {
  background: #4f46e5;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
  min-width: 80px;
  justify-content: center;
}

.apply-btn-global:hover:not(:disabled) {
  background: #4338ca;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.4);
}

.apply-btn-global:disabled {
  background: #d1d5db;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
  color: #9ca3af;
}

.loading-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.admin-content {
  flex: 1;
  overflow: auto;
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