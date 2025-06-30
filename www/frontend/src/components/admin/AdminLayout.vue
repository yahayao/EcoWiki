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
              <span class="nav-icon">🔑</span>
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
        <div class="header-actions">
          <button 
            class="return-btn" 
            @click="goBack"
          >
            ← 返回
          </button>
          <button 
            class="apply-btn-global" 
            :disabled="applying || !hasPendingChanges" 
            @click="applyAllSettings"
          >
            <span v-if="applying" class="loading-spinner"></span>
            <span v-else>应用</span>
          </button>
        </div>
      </div>
      
      <div class="admin-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useAdminUserStore } from '../../stores/adminUserStore'
import toast from '../../utils/toast'

const router = useRouter()
const adminUserStore = useAdminUserStore()
const { pendingUserChanges } = storeToRefs(adminUserStore)

const applying = ref(false)
const pendingChangesKey = ref(0) // 用于强制重新计算

// 保存进入管理后台时的原始首页风格
onMounted(() => {
  const currentHomeStyle = localStorage.getItem('homeStyle') || 'classic'
  if (!localStorage.getItem('original-homeStyle')) {
    localStorage.setItem('original-homeStyle', currentHomeStyle)
  }
  
  // 监听系统设置变更事件
  window.addEventListener('ecowiki-admin-pending-changes', handlePendingChanges)
})

// 清理函数
onUnmounted(() => {
  window.removeEventListener('ecowiki-admin-pending-changes', handlePendingChanges)
})

// 处理待处理变更
const handlePendingChanges = () => {
  pendingChangesKey.value++
}

// 计算是否有待处理的变更（始终显示按钮，但根据此状态禁用/启用）
const hasPendingChanges = computed(() => {
  // 强制重新计算
  pendingChangesKey.value
  
  // 检查用户角色变更
  const hasUserChanges = Object.keys(pendingUserChanges.value).length > 0
  
  // 检查系统设置变更（首页风格）
  const currentHomeStyle = localStorage.getItem('homeStyle') || 'classic'
  const originalHomeStyle = localStorage.getItem('original-homeStyle') || 'classic'
  const hasStyleChanges = currentHomeStyle !== originalHomeStyle
  
  return hasUserChanges || hasStyleChanges
})

// 返回到管理后台之外的最近界面
const goBack = () => {
  // 检查是否有未应用的变更
  const hasUserChanges = Object.keys(pendingUserChanges.value).length > 0
  const currentHomeStyle = localStorage.getItem('homeStyle') || 'classic'
  const originalHomeStyle = localStorage.getItem('original-homeStyle') || 'classic'
  const hasStyleChanges = currentHomeStyle !== originalHomeStyle
  
  if (hasUserChanges || hasStyleChanges) {
    if (confirm('您有未应用的变更，是否要丢弃这些变更并返回？')) {
      // 恢复原始设置
      if (hasStyleChanges) {
        localStorage.setItem('homeStyle', originalHomeStyle)
        window.dispatchEvent(new Event('ecowiki-home-style-change'))
      }
      // 清除用户变更
      adminUserStore.clearPendingChanges()
    } else {
      return // 用户取消返回
    }
  }
  
  // 清除原始设置标记
  localStorage.removeItem('original-homeStyle')
  
  // 检查localStorage中是否保存了进入管理后台前的路由
  const previousRoute = localStorage.getItem('previous-route-before-admin')
  
  // 清除保存的路由（无论是否使用）
  localStorage.removeItem('previous-route-before-admin')
  
  if (previousRoute && previousRoute !== '/admin' && !previousRoute.startsWith('/admin/')) {
    // 如果有保存的非管理后台路由，则返回到该路由
    router.push(previousRoute)
  } else {
    // 否则返回到主页
    router.push('/')
  }
}

// 应用所有设置
const applyAllSettings = async () => {
  if (applying.value) return // 防止重复点击
  
  applying.value = true
  
  try {
    // 显示开始应用的动画效果
    const applyBtn = document.querySelector('.apply-btn-global')
    if (applyBtn) {
      applyBtn.classList.add('applying-animation')
    }
    
    // 延迟2.5秒，让用户看到漂亮的加载动画
    await new Promise(resolve => setTimeout(resolve, 2500))
    
    // 应用用户管理的变更
    if (Object.keys(pendingUserChanges.value).length > 0) {
      await adminUserStore.applyAllUserChanges()
    }
    
    // 应用系统设置变更（首页风格）
    const currentHomeStyle = localStorage.getItem('homeStyle') || 'classic'
    localStorage.setItem('original-homeStyle', currentHomeStyle)
    window.dispatchEvent(new Event('ecowiki-home-style-change'))
    
    // 成功动画
    if (applyBtn) {
      applyBtn.classList.remove('applying-animation')
      applyBtn.classList.add('success-animation')
      setTimeout(() => {
        applyBtn.classList.remove('success-animation')
      }, 1000)
    }
    
    // 延迟显示成功消息，让动画完成
    setTimeout(() => {
      toast.success('🎉 所有设置已成功应用！', '应用成功')
    }, 300)
    
  } catch (e: any) {
    // 错误动画
    const applyBtn = document.querySelector('.apply-btn-global')
    if (applyBtn) {
      applyBtn.classList.remove('applying-animation')
      applyBtn.classList.add('error-animation')
      setTimeout(() => {
        applyBtn.classList.remove('error-animation')
      }, 1000)
    }
    
    setTimeout(() => {
      toast.error('❌ ' + (e.message || '应用设置失败'), '应用失败')
    }, 300)
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.return-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
  min-width: 80px;
  justify-content: center;
}

.return-btn:hover {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.apply-btn-global {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
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

/* 应用按钮动画效果 */
.applying-animation {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  transform: scale(0.95) !important;
  position: relative;
  overflow: hidden;
}

.applying-animation::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
  animation: shimmer 1.5s infinite;
}

.success-animation {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%) !important;
  transform: scale(1.05) !important;
  animation: successPulse 0.6s ease-out;
}

.error-animation {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%) !important;
  animation: shake 0.6s ease-in-out;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes shimmer {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}

@keyframes successPulse {
  0% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 0 0 10px rgba(16, 185, 129, 0);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0);
  }
}

@keyframes shake {
  0%, 100% {
    transform: translateX(0);
  }
  10%, 30%, 50%, 70%, 90% {
    transform: translateX(-3px);
  }
  20%, 40%, 60%, 80% {
    transform: translateX(3px);
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
