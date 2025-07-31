<!--
/**
 * 管理后台布局组件
 * 
 * 这是管理后台的主要布局组件，提供了完整的管理界面框架。
 * 包含侧边导航、头部操作区和内容展示区域。
 * 
 * 主要功能：
 * - 提供管理后台的整体布局结构
 * - 统一的导航菜单和页面标题
 * - 全局的应用和返回按钮
 * - 支持多个子页面的嵌套路由
 * - 管理待处理变更的状态
 * 
 * 组件结构：
 * - admin-sider: 左侧导航栏
 * - admin-main: 主内容区域
 *   - admin-header: 顶部操作栏
 *   - admin-content: 内容展示区（router-view）
 * 
 * 导航菜单：
 * - 系统设置: 首页风格配置、系统统计
 * - 用户管理: 用户列表、角色分配
 * - 权限管理: 角色创建、权限配置
 * 
 * 状态管理：
 * - 监听用户角色变更
 * - 监听系统设置变更
 * - 统一的应用/撤销操作
 * 
 * 特殊功能：
 * - 返回按钮记住进入前的页面
 * - 应用按钮的智能启用/禁用
 * - 离开时确认未保存的变更
 * - 流畅的动画效果
 * 
 * @author EcoWiki Team
 * @version 2.0 (支持系统设置和角色管理)
 * @since 2025-06-30
 */
-->
<template>
  <div class="admin-layout">
    <!-- 顶部标题栏 -->
    <div class="admin-header">
      <div class="header-left">
        <div class="header-icon">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M12,15.5A3.5,3.5 0 0,1 8.5,12A3.5,3.5 0 0,1 12,8.5A3.5,3.5 0 0,1 15.5,12A3.5,3.5 0 0,1 12,15.5M19.43,12.97C19.47,12.65 19.5,12.33 19.5,12C19.5,11.67 19.47,11.34 19.43,11L21.54,9.37C21.73,9.22 21.78,8.95 21.66,8.73L19.66,5.27C19.54,5.05 19.27,4.96 19.05,5.05L16.56,6.05C16.04,5.66 15.5,5.32 14.87,5.07L14.5,2.42C14.46,2.18 14.25,2 14,2H10C9.75,2 9.54,2.18 9.5,2.42L9.13,5.07C8.5,5.32 7.96,5.66 7.44,6.05L4.95,5.05C4.73,4.96 4.46,5.05 4.34,5.27L2.34,8.73C2.22,8.95 2.27,9.22 2.46,9.37L4.57,11C4.53,11.34 4.5,11.67 4.5,12C4.5,12.33 4.53,12.65 4.57,12.97L2.46,14.63C2.27,14.78 2.22,15.05 2.34,15.27L4.34,18.73C4.46,18.95 4.73,19.03 4.95,18.95L7.44,17.94C7.96,18.34 8.5,18.68 9.13,18.93L9.5,21.58C9.54,21.82 9.75,22 10,22H14C14.25,22 14.46,21.82 14.5,21.58L14.87,18.93C15.5,18.68 16.04,18.34 16.56,17.94L19.05,18.95C19.27,19.03 19.54,18.95 19.66,18.73L21.66,15.27C21.78,15.05 21.73,14.78 21.54,14.63L19.43,12.97Z" />
          </svg>
        </div>
        <div class="header-text">
          <h1>EcoWiki 管理后台</h1>
          <p>管理系统配置和权限设置</p>
        </div>
      </div>
      <div class="header-actions">
        <button class="btn btn-refresh" @click="refreshSettings">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M17.65,6.35C16.2,4.9 14.21,4 12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20C15.73,20 18.84,17.45 19.73,14H17.65C16.83,16.33 14.61,18 12,18A6,6 0 0,1 6,12A6,6 0 0,1 12,6C13.66,6 15.14,6.69 16.22,7.78L13,11H20V4L17.65,6.35Z" />
          </svg>
        </button>
        <button class="btn btn-ghost" @click="goBack">
          返回
        </button>
        <button 
          class="btn btn-primary apply-btn-global" 
          @click="applyAllSettings" 
          :disabled="!hasPendingChanges || applying"
        >
          <span v-if="applying" class="loading-spinner"></span>
          {{ applying ? '应用中...' : '应用' }}
        </button>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="admin-container">
      <!-- 左侧导航 -->
      <div class="admin-sidebar">
        <nav class="nav-menu">
          <div class="nav-group">
            <h3 class="nav-group-title">系统管理</h3>
            <ul class="nav-list">
              <li class="nav-item" :class="{ active: activeSection === 'settings' }" @click="setActiveSection('settings')">
                <div class="nav-item-content">
                  <svg viewBox="0 0 24 24" class="nav-icon">
                    <path d="M12,15.5A3.5,3.5 0 0,1 8.5,12A3.5,3.5 0 0,1 12,8.5A3.5,3.5 0 0,1 15.5,12A3.5,3.5 0 0,1 12,15.5M19.43,12.97C19.47,12.65 19.5,12.33 19.5,12C19.5,11.67 19.47,11.34 19.43,11L21.54,9.37C21.73,9.22 21.78,8.95 21.66,8.73L19.66,5.27C19.54,5.05 19.27,4.96 19.05,5.05L16.56,6.05C16.04,5.66 15.5,5.32 14.87,5.07L14.5,2.42C14.46,2.18 14.25,2 14,2H10C9.75,2 9.54,2.18 9.5,2.42L9.13,5.07C8.5,5.32 7.96,5.66 7.44,6.05L4.95,5.05C4.73,4.96 4.46,5.05 4.34,5.27L2.34,8.73C2.22,8.95 2.27,9.22 2.46,9.37L4.57,11C4.53,11.34 4.5,11.67 4.5,12C4.5,12.33 4.53,12.65 4.57,12.97L2.46,14.63C2.27,14.78 2.22,15.05 2.34,15.27L4.34,18.73C4.46,18.95 4.73,19.03 4.95,18.95L7.44,17.94C7.96,18.34 8.5,18.68 9.13,18.93L9.5,21.58C9.54,21.82 9.75,22 10,22H14C14.25,22 14.46,21.82 14.5,21.58L14.87,18.93C15.5,18.68 16.04,18.34 16.56,17.94L19.05,18.95C19.27,19.03 19.54,18.95 19.66,18.73L21.66,15.27C21.78,15.05 21.73,14.78 21.54,14.63L19.43,12.97Z" />
                  </svg>
                  <span class="nav-text">系统设置</span>
                </div>
              </li>
            </ul>
          </div>
          
          <div class="nav-group">
            <h3 class="nav-group-title">内容管理</h3>
            <ul class="nav-list">
              <li class="nav-item" :class="{ active: activeSection === 'articles' }" @click="setActiveSection('articles')">
                <div class="nav-item-content">
                  <svg viewBox="0 0 24 24" class="nav-icon">
                    <path d="M19,3H5C3.89,3 3,3.89 3,5V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V5C21,3.89 20.1,3 19,3M5,19V5H19V19H5Z" />
                  </svg>
                  <span class="nav-text">文章管理</span>
                </div>
              </li>
              <li class="nav-item" :class="{ active: activeSection === 'drafts' }" @click="setActiveSection('drafts')">
                <div class="nav-item-content">
                  <svg viewBox="0 0 24 24" class="nav-icon">
                    <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z" />
                  </svg>
                  <span class="nav-text">草稿审核</span>
                </div>
              </li>
              <li class="nav-item" :class="{ active: activeSection === 'reviews' }" @click="setActiveSection('reviews')">
                <div class="nav-item-content">
                  <svg viewBox="0 0 24 24" class="nav-icon">
                    <path d="M9,20.42L2.79,14.21L5.62,11.38L9,14.77L18.88,4.88L21.71,7.71L9,20.42Z" />
                  </svg>
                  <span class="nav-text">审核管理</span>
                </div>
              </li>
            </ul>
          </div>
          
          <div class="nav-group">
            <h3 class="nav-group-title">用户管理</h3>
            <ul class="nav-list">
              <li class="nav-item" :class="{ active: activeSection === 'users' }" @click="setActiveSection('users')">
                <div class="nav-item-content">
                  <svg viewBox="0 0 24 24" class="nav-icon">
                    <path d="M16,4C18.2,4 20,5.8 20,8C20,10.2 18.2,12 16,12C13.8,12 12,10.2 12,8C12,5.8 13.8,4 16,4M16,14C20.4,14 24,15.8 24,18V20H8V18C8,15.8 11.6,14 16,14M8.5,11L5.5,14L4.08,12.58L2,14.66L5.5,18.16L10.92,12.74L8.5,11Z" />
                  </svg>
                  <span class="nav-text">用户管理</span>
                </div>
              </li>
            </ul>
          </div>
          
          <div class="nav-group">
            <h3 class="nav-group-title">权限管理</h3>
            <ul class="nav-list">
              <li class="nav-item" :class="{ active: activeSection === 'permissions' }" @click="setActiveSection('permissions')">
                <div class="nav-item-content">
                  <svg viewBox="0 0 24 24" class="nav-icon">
                    <path d="M12,1L21,5V11C21,16.55 17.16,21.74 12,23C6.84,21.74 3,16.55 3,11V5L12,1M12,7C10.89,7 10,7.89 10,9A2,2 0 0,0 12,11A2,2 0 0,0 14,9C14,7.89 13.11,7 12,7Z" />
                  </svg>
                  <span class="nav-text">权限管理</span>
                </div>
              </li>
              <li class="nav-item" :class="{ active: activeSection === 'roles' }" @click="setActiveSection('roles')">
                <div class="nav-item-content">
                  <svg viewBox="0 0 24 24" class="nav-icon">
                    <path d="M12,1L3,5V11C3,16.55 6.84,21.74 12,23C17.16,21.74 21,16.55 21,11V5L12,1M10,17L6,13L7.41,11.59L10,14.17L16.59,7.58L18,9L10,17Z" />
                  </svg>
                  <span class="nav-text">角色权限</span>
                </div>
              </li>
            </ul>
          </div>
        </nav>
      </div>

      <!-- 右侧内容区域 -->
      <div class="admin-content">
        <!-- 使用 v-show 控制显示，ref 控制缓存 -->
        <SystemSettings v-show="activeSection === 'settings'" ref="systemSettingsRef" />
        <ArticleManagement v-show="activeSection === 'articles'" ref="articleManagementRef" />
        <DraftReviewDashboard v-show="activeSection === 'drafts'" ref="draftReviewDashboardRef" />
        <ReviewManagement v-show="activeSection === 'reviews'" ref="reviewManagementRef" />
        <UserList v-show="activeSection === 'users'" ref="userListRef" />
        <PermissionManagement v-show="activeSection === 'permissions'" ref="permissionManagementRef" />
        <RolePermissionAssignment v-show="activeSection === 'roles'" ref="rolePermissionAssignmentRef" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 管理后台布局组件的脚本部分
 * 
 * 主要功能：
 * - 导航菜单的切换和状态管理
 * - 用户变更和系统设置的监听
 * - 全局应用按钮的状态控制
 * - 返回功能的智能处理
 * - 未保存变更的提醒机制
 */

// === Vue 3 组合式API导入 ===
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { storeToRefs } from 'pinia'  // Pinia状态管理
import { useRouter, useRoute } from 'vue-router'  // Vue Router路由管理

// === 业务逻辑导入 ===
import { useAdminUserStore } from '../../stores/adminUserStore'  // 管理用户状态
import toast from '../../utils/toast'  // 消息提示工具

// === 子组件导入 ===
import SystemSettings from './views/SystemSettings.vue'  // 系统设置页面
import ArticleManagement from './views/ArticleManagement.vue'  // 文章管理页面
import UserList from './views/UserList.vue'  // 用户列表页面
import PermissionManagement from './views/PermissionManagement.vue'  // 权限管理页面
import RolePermissionAssignment from './views/RolePermissionAssignment.vue'  // 角色权限分配页面
import ReviewManagement from './views/ReviewManagement.vue'  // 审核管理页面
import DraftReviewDashboard from './views/DraftReviewDashboard.vue'  // 草稿审核页面

// === 实例化依赖 ===
const router = useRouter()
const route = useRoute()
const adminUserStore = useAdminUserStore()
// 直接访问store属性，避免类型兼容性问题
const pendingUserChanges = computed(() => adminUserStore.pendingUserChanges)

// === 组件状态管理 ===
const applying = ref(false)  // 全局应用按钮的加载状态
const pendingChangesKey = ref(0)  // 用于强制重新计算待处理变更的响应式键

// === 子组件 ref 定义 ===
const systemSettingsRef = ref<any>(null)
const articleManagementRef = ref<any>(null)
const draftReviewDashboardRef = ref<any>(null)
const reviewManagementRef = ref<any>(null)
const userListRef = ref<any>(null)
const permissionManagementRef = ref<any>(null)
const rolePermissionAssignmentRef = ref<any>(null)

// 根据当前路由计算当前激活的导航部分
const activeSection = computed(() => {
  const path = route.path
  if (path.includes('/admin/settings')) return 'settings'
  if (path.includes('/admin/articles')) return 'articles'
  if (path.includes('/admin/drafts')) return 'drafts'
  if (path.includes('/admin/reviews')) return 'reviews'
  if (path.includes('/admin/users')) return 'users'
  if (path.includes('/admin/permissions')) return 'permissions'
  if (path.includes('/admin/role-permissions')) return 'roles'
  return 'settings' // 默认
})

// === 导航切换函数 ===
/**
 * 设置当前激活的导航部分
 * @param section 导航部分标识符 ('settings' | 'users' | 'permissions' | 'roles' | 'articles' | 'reviews')
 */
const setActiveSection = (section: string) => {
  let routePath = '/admin/'
  switch (section) {
    case 'settings':
      routePath += 'settings'
      break
    case 'articles':
      routePath += 'articles'
      break
    case 'drafts':
      routePath += 'drafts'
      break
    case 'reviews':
      routePath += 'reviews'
      break
    case 'users':
      routePath += 'users'
      break
    case 'permissions':
      routePath += 'permissions'
      break
    case 'roles':
      routePath += 'role-permissions'
      break
    default:
      routePath += 'settings'
  }
  
  if (route.path !== routePath) {
    router.push(routePath)
  }
}

// === 页面刷新函数 ===
/**
 * 刷新当前激活的管理页面组件
 * 通过调用组件的刷新方法来重新加载数据
 */
const refreshSettings = () => {
  const currentSection = activeSection.value
  
  // 根据当前激活的section调用对应组件的刷新方法
  switch (currentSection) {
    case 'settings':
      if (systemSettingsRef.value && typeof systemSettingsRef.value.refreshData === 'function') {
        systemSettingsRef.value.refreshData()
      }
      break
    case 'articles':
      if (articleManagementRef.value && typeof articleManagementRef.value.refreshData === 'function') {
        articleManagementRef.value.refreshData()
      }
      break
    case 'drafts':
      if (draftReviewDashboardRef.value && typeof draftReviewDashboardRef.value.refreshData === 'function') {
        draftReviewDashboardRef.value.refreshData()
      }
      break
    case 'reviews':
      if (reviewManagementRef.value && typeof reviewManagementRef.value.refreshData === 'function') {
        reviewManagementRef.value.refreshData()
      }
      break
    case 'users':
      if (userListRef.value && typeof userListRef.value.loadUsers === 'function') {
        userListRef.value.loadUsers()
      }
      break
    case 'permissions':
      if (permissionManagementRef.value && typeof permissionManagementRef.value.refreshData === 'function') {
        permissionManagementRef.value.refreshData()
      }
      break
    case 'roles':
      if (rolePermissionAssignmentRef.value && typeof rolePermissionAssignmentRef.value.refreshData === 'function') {
        rolePermissionAssignmentRef.value.refreshData()
      }
      break
  }
  
  // 显示刷新提示
  toast.success('页面已刷新', '刷新成功')
}

// === 生命周期钩子 ===

/**
 * 组件挂载时的初始化操作
 * 主要用于设置原始状态和监听器
 */
onMounted(() => {
  // 保存进入管理后台时的原始首页风格，用于取消操作时恢复
  const currentHomeStyle = localStorage.getItem('homeStyle') || 'classic'
  if (!localStorage.getItem('original-homeStyle')) {
    localStorage.setItem('original-homeStyle', currentHomeStyle)
  }
  
  // 监听系统设置变更事件，用于更新待处理变更状态
  window.addEventListener('ecowiki-admin-pending-changes', handlePendingChanges)
})

/**
 * 组件卸载时的清理操作
 * 移除事件监听器，防止内存泄漏
 */
onUnmounted(() => {
  window.removeEventListener('ecowiki-admin-pending-changes', handlePendingChanges)
})

// === 事件处理函数 ===

/**
 * 处理待处理变更事件
 * 强制重新计算待处理变更状态
 */
const handlePendingChanges = () => {
  pendingChangesKey.value++  // 增加键值，触发计算属性重新计算
}

// === 计算属性 ===

/**
 * 计算是否有待处理的变更
 * 用于控制全局应用按钮的启用/禁用状态
 * 
 * 检查的变更类型：
 * 1. 用户角色变更（来自adminUserStore）
 * 2. 系统设置变更（首页风格配置）
 * 
 * @returns {boolean} 是否存在待处理的变更
 */
const hasPendingChanges = computed(() => {
  // 通过访问pendingChangesKey来强制重新计算
  pendingChangesKey.value
  
  // 检查用户角色变更：如果pendingUserChanges对象有键，说明有待处理的用户变更
  const hasUserChanges = Object.keys(pendingUserChanges.value).length > 0
  
  // 检查系统设置变更：比较当前首页风格与原始风格
  const currentHomeStyle = localStorage.getItem('homeStyle') || 'classic'
  const originalHomeStyle = localStorage.getItem('original-homeStyle') || 'classic'
  const hasStyleChanges = currentHomeStyle !== originalHomeStyle
  
  return hasUserChanges || hasStyleChanges
})

// === 核心业务函数 ===

/**
 * 返回到管理后台之外的最近界面
 * 智能处理未保存变更的提醒和路由跳转
 */
const goBack = () => {
  // 检查是否有未应用的变更
  const hasUserChanges = Object.keys(pendingUserChanges.value).length > 0
  const currentHomeStyle = localStorage.getItem('homeStyle') || 'classic'
  const originalHomeStyle = localStorage.getItem('original-homeStyle') || 'classic'
  const hasStyleChanges = currentHomeStyle !== originalHomeStyle
  
  // 如果有未保存的变更，询问用户是否要丢弃
  if (hasUserChanges || hasStyleChanges) {
    if (confirm('您有未应用的变更，是否要丢弃这些变更并返回？')) {
      // 用户确认丢弃变更，恢复原始设置
      if (hasStyleChanges) {
        localStorage.setItem('homeStyle', originalHomeStyle)
        // 触发首页风格变更事件，通知其他组件更新
        window.dispatchEvent(new Event('ecowiki-home-style-change'))
      }
      // 清除用户变更
      adminUserStore.clearPendingChanges()
    } else {
      return // 用户取消返回操作
    }
  }
  
  // 清除原始设置标记，避免下次进入时冲突
  localStorage.removeItem('original-homeStyle')
  
  // 智能路由跳转：优先返回进入管理后台前的页面
  const previousRoute = localStorage.getItem('previous-route-before-admin')
  
  // 清除保存的路由记录
  localStorage.removeItem('previous-route-before-admin')
  
  if (previousRoute && previousRoute !== '/admin' && !previousRoute.startsWith('/admin/')) {
    // 如果有保存的非管理后台路由，则返回到该路由
    router.push(previousRoute)
  } else {
    // 否则返回到主页
    router.push('/')
  }
}

/**
 * 应用所有待处理的设置变更
 * 统一处理用户管理和系统设置的变更，提供良好的用户体验
 * 
 * 应用流程：
 * 1. 防重复点击检查
 * 2. 显示加载动画
 * 3. 应用用户管理变更
 * 4. 应用系统设置变更
 * 5. 显示成功/失败反馈
 * 6. 清理状态
 */
const applyAllSettings = async () => {
  // 防止用户快速重复点击
  if (applying.value) return
  
  applying.value = true
  
  try {
    // 显示开始应用的动画效果
    const applyBtn = document.querySelector('.apply-btn-global')
    if (applyBtn) {
      applyBtn.classList.add('applying-animation')
    }
    
    // 延迟2.5秒，让用户看到漂亮的加载动画，提升用户体验
    await new Promise(resolve => setTimeout(resolve, 2500))
    
    // 应用用户管理的变更（角色分配等）
    if (Object.keys(pendingUserChanges.value).length > 0) {
      await adminUserStore.applyAllUserChanges()
    }
    
    // 应用系统设置变更（首页风格配置等）
    const currentHomeStyle = localStorage.getItem('homeStyle') || 'classic'
    localStorage.setItem('original-homeStyle', currentHomeStyle)
    // 通知其他组件首页风格已变更
    window.dispatchEvent(new Event('ecowiki-home-style-change'))
    
    // 显示成功动画
    if (applyBtn) {
      applyBtn.classList.remove('applying-animation')
      applyBtn.classList.add('success-animation')
      // 1秒后移除成功动画
      setTimeout(() => {
        applyBtn.classList.remove('success-animation')
      }, 1000)
    }
    
    // 延迟显示成功消息，让动画完成
    setTimeout(() => {
      toast.success('所有设置已成功应用！', '应用成功')
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
/* Wiki.js风格的全屏管理界面 */
.admin-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  overflow: hidden;
}

/* 顶部标题栏 */
.admin-header {
  background: white;
  border-bottom: 1px solid #e9ecef;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 72px;
  flex-shrink: 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-icon .icon {
  width: 20px;
  height: 20px;
  fill: white;
}

.header-text h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
}

.header-text p {
  margin: 4px 0 0 0;
  color: #6c757d;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.btn .icon {
  width: 16px;
  height: 16px;
}

.btn-refresh {
  background: #f8f9fa;
  color: #6c757d;
  border: 1px solid #dee2e6;
}

.btn-refresh:hover {
  background: #e9ecef;
  color: #495057;
}

.btn-ghost {
  background: transparent;
  color: #6c757d;
  border: 1px solid #dee2e6;
}

.btn-ghost:hover {
  background: #f8f9fa;
  color: #495057;
}

.btn-primary {
  background: #1976d2;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #1565c0;
}

.btn-primary:disabled {
  background: #b3d4fc;
  cursor: not-allowed;
}

/* 主容器 */
.admin-container {
  flex: 1;
  display: flex;
  min-height: 0;
}

/* 左侧导航栏 */
.admin-sidebar {
  width: 280px;
  background: white;
  border-right: 1px solid #e9ecef;
  flex-shrink: 0;
  overflow-y: auto;
}

.nav-menu {
  padding: 16px 0;
}

.nav-group {
  margin-bottom: 24px;
}

.nav-group-title {
  padding: 0 24px;
  margin: 0 0 12px 0;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  color: #6c757d;
  letter-spacing: 0.5px;
}

.nav-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav-item {
  cursor: pointer;
  transition: all 0.2s;
}

.nav-item:hover {
  background: #f8f9fa;
}

.nav-item.active {
  background: #e3f2fd;
  border-right: 3px solid #1976d2;
}

.nav-item.active .nav-text {
  color: #1976d2;
  font-weight: 600;
}

.nav-item-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
}

.nav-icon {
  width: 18px;
  height: 18px;
  fill: #6c757d;
  flex-shrink: 0;
}

.nav-item.active .nav-icon {
  fill: #1976d2;
}

.nav-text {
  font-size: 14px;
  color: #495057;
  font-weight: 500;
}

/* 右侧内容区域 */
.admin-content {
  flex: 1;
  overflow-y: auto;
  background: white;
  margin: 16px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* 确保使用 v-show 的子组件在隐藏时不占用空间 */
.admin-content > div[style*="display: none"] {
  position: absolute;
  visibility: hidden;
  pointer-events: none;
}

/* 确保显示的子组件占据完整空间 */
.admin-content > div:not([style*="display: none"]) {
  height: 100%;
  width: 100%;
}

/* 占位内容样式 */
.placeholder-content {
  text-align: center;
  padding: 60px 20px;
  color: #6c757d;
}

.placeholder-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.placeholder-content h3 {
  margin: 0 0 12px 0;
  font-size: 20px;
  font-weight: 600;
  color: #495057;
}

.placeholder-content p {
  margin: 0;
  font-size: 14px;
  max-width: 400px;
  margin: 0 auto;
  line-height: 1.5;
}

/* 加载动画样式 */
.loading-spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid #ffffff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-right: 8px;
  vertical-align: middle;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 应用按钮的特殊动画效果 */
.apply-btn-global {
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.apply-btn-global.applying-animation {
  background: linear-gradient(45deg, #1976d2, #42a5f5, #1976d2);
  background-size: 300% 300%;
  animation: shimmer 1.5s ease-in-out infinite;
}

.apply-btn-global.success-animation {
  background: #4caf50 !important;
  animation: pulse 0.6s ease-in-out;
}

.apply-btn-global.error-animation {
  background: #f44336 !important;
  animation: shake 0.6s ease-in-out;
}

@keyframes shimmer {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-4px); }
  75% { transform: translateX(4px); }
}

/* 禁用状态时的样式优化 */
.apply-btn-global:disabled {
  background: #b3d4fc !important;
  cursor: not-allowed;
  opacity: 0.7;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .admin-sidebar {
    width: 240px;
  }
}

@media (max-width: 768px) {
  .admin-container {
    flex-direction: column;
  }
  
  .admin-sidebar {
    width: 100%;
    height: auto;
    border-right: none;
    border-bottom: 1px solid #e9ecef;
  }
  
  .nav-menu {
    padding: 8px 0;
  }
  
  .nav-group {
    margin-bottom: 16px;
  }
  
  .nav-item-content {
    padding: 8px 16px;
  }
  
  .admin-header {
    padding: 16px;
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
}

@media (max-width: 480px) {
  .admin-header {
    padding: 12px;
  }
  
  .header-text h1 {
    font-size: 20px;
  }
  
  .btn {
    padding: 6px 12px;
    font-size: 13px;
  }
}
</style>
