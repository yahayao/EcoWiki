<!--
/**
 * 用户管理组件
 * 
 * 功能包括：
 * - 用户列表展示和管理
 * - 用户角色分配和权限管理
 * - 用户状态切换和操作
 * - 批量用户操作处理
 * - 用户信息编辑和更新
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
-->
<template>
  <div class="user-management">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M16,4C16.88,4 17.67,4.5 18,5.26L19,7H20A2,2 0 0,1 22,9V19A2,2 0 0,1 20,21H4A2,2 0 0,1 2,19V9C2,8.62 2.1,8.26 2.21,7.93L5.74,3.58C5.95,3.22 6.33,3 6.74,3H13.26C13.67,3 14.05,3.22 14.26,3.58L15.79,6H16M16,6A2,2 0 0,0 14,8A2,2 0 0,0 16,10A2,2 0 0,0 18,8A2,2 0 0,0 16,6Z" />
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">用户管理</h1>
          <p class="page-subtitle">管理系统用户信息和角色权限</p>
        </div>
      </div>
      <div class="header-actions">
        <!-- 刷新按钮 -->
        <button class="refresh-btn" @click="loadUsers" :disabled="loading">
          <span v-if="loading" class="loading-spinner"></span>
          刷新
        </button>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-and-filter">
      <div class="search-box">
        <input
          ref="searchInput"
          type="text"
          v-model="searchQuery"
          placeholder="搜索用户（用户名、邮箱、姓名）- 按 Ctrl+F 快速搜索"
          class="search-input"
          @keydown.esc="clearSearch"
        >
        <button class="clear-search-btn" @click="clearSearch" v-if="searchQuery">
          ✕
        </button>
      </div>
      
      <div class="filter-controls">
        <div class="sort-control">
          <label>排序方式：</label>
          <select v-model="sortBy" class="sort-select">
            <option value="createdAt">注册时间</option>
            <option value="userId">用户ID</option>
            <option value="username">用户名</option>
            <option value="email">邮箱</option>
            <option value="userGroup">角色</option>
            <option value="active">状态</option>
          </select>
          <button 
            @click="toggleSortOrder" 
            class="sort-order-btn"
            :title="sortOrder === 'asc' ? '升序' : '降序'"
          >
            {{ sortOrder === 'asc' ? '↑' : '↓' }}
          </button>
        </div>
        
        <div class="role-filter">
          <label>角色筛选：</label>
          <select v-model="roleFilter" class="role-filter-select">
            <option value="">全部角色</option>
            <option v-for="role in roles" :key="role" :value="role">
              {{ getRoleDisplayName(role) }}
            </option>
          </select>
        </div>
        
        <div class="status-filter">
          <label>状态筛选：</label>
          <select v-model="statusFilter" class="status-filter-select">
            <option value="">全部状态</option>
            <option value="true">正常</option>
            <option value="false">已禁用</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 加载状态显示 -->
    <div v-if="loading" class="loading">加载中...</div>
    <!-- 错误状态显示 -->
    <div v-else-if="error" class="error">{{ error }}</div>
    <!-- 用户列表表格 -->
    <div v-else>
      <!-- 搜索结果统计 -->
      <div class="search-results-info" v-if="searchQuery || roleFilter || statusFilter">
        <span class="results-count">
          找到 {{ filteredAndSortedUsers.length }} 个用户
          <span v-if="filteredAndSortedUsers.length !== users.length">
            （共 {{ users.length }} 个用户）
          </span>
        </span>
        <button v-if="searchQuery || roleFilter || statusFilter" @click="clearAllFilters" class="clear-filters-btn">
          清除所有筛选
        </button>
      </div>
      
      <!-- 空搜索结果提示 -->
      <div v-if="filteredAndSortedUsers.length === 0" class="no-results">
        <div class="no-results-icon">🔍</div>
        <h4>没有找到匹配的用户</h4>
        <p>请尝试调整搜索条件或筛选选项</p>
        <button @click="clearAllFilters" class="clear-filters-btn">清除所有筛选</button>
      </div>
      
      <div class="users-table" v-else>
        <table>
          <thead>
            <tr>
              <th class="sortable-header" @click="setSortBy('userId')">
                用户ID
                <span v-if="sortBy === 'userId'" class="sort-indicator">
                  {{ sortOrder === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th class="sortable-header" @click="setSortBy('username')">
                用户名
                <span v-if="sortBy === 'username'" class="sort-indicator">
                  {{ sortOrder === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th class="sortable-header" @click="setSortBy('email')">
                邮箱
                <span v-if="sortBy === 'email'" class="sort-indicator">
                  {{ sortOrder === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th>姓名</th>
              <th class="sortable-header" @click="setSortBy('userGroup')">
                角色
                <span v-if="sortBy === 'userGroup'" class="sort-indicator">
                  {{ sortOrder === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th class="sortable-header" @click="setSortBy('active')">
                状态
                <span v-if="sortBy === 'active'" class="sort-indicator">
                  {{ sortOrder === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th class="sortable-header" @click="setSortBy('createdAt')">
                注册时间
                <span v-if="sortBy === 'createdAt'" class="sort-indicator">
                  {{ sortOrder === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <!-- 用户数据行循环 -->
            <tr v-for="user in filteredAndSortedUsers" :key="user.userId">
              <td>{{ user.userId }}</td>
              <td>{{ user.username }}</td>
              <td>{{ user.email }}</td>
              <td>{{ user.fullName || '-' }}</td>
              <td>
                <!-- 角色选择下拉框 -->
                <select
                  :value="pendingUserChanges[user.userId]?.userGroup ?? user.userGroup"
                  @change="e => onUserGroupChange(user, (e.target as HTMLSelectElement).value)"
                  :disabled="user.userGroup === 'superadmin' || getCurrentUser()?.username === user.username"
                  class="role-select"
                >
                  <option v-if="roles.length === 0" disabled>加载中...</option>
                  <option v-for="role in availableRoles" :key="role" :value="role">
                    {{ getRoleDisplayName(role) }}
                  </option>
                </select>
                <!-- 角色加载失败提示 -->
                <small v-if="roles.length === 0" style="color: #e53e3e;">
                  角色数据加载失败，请刷新页面
                </small>
                <!-- 不能修改自己角色的提示 -->
                <small v-if="getCurrentUser()?.username === user.username" style="color: #666; display: block;">
                  不能修改自己的角色
                </small>
              </td>
              <td>
                <!-- 用户状态指示器 -->
                <span
                  :class="['status-indicator', (pendingUserChanges[user.userId]?.active ?? user.active) ? 'status-active' : 'status-inactive']"
                >
                  {{ (pendingUserChanges[user.userId]?.active ?? user.active) ? '正常' : '已禁用' }}
                </span>
              </td>
              <td>{{ formatDate(user.createdAt) }}</td>
              <td>
                <!-- 用户状态切换按钮 -->
                <button 
                  @click="handleToggleUserStatus(user)"
                  :disabled="user.userGroup === 'superadmin' || getCurrentUser()?.username === user.username"
                  :class="['action-btn', (pendingUserChanges[user.userId]?.active ?? user.active) ? 'disable-btn' : 'restore-btn']"
                >
                  {{ (pendingUserChanges[user.userId]?.active ?? user.active) ? '禁用' : '恢复' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Vue 3 组合式 API 导入
import { onMounted, computed, ref, onUnmounted } from 'vue'
// Pinia 状态管理工具
import { storeToRefs } from 'pinia'
// 管理员用户状态管理
import { useAdminUserStore } from '../../../stores/adminUserStore'
// 用户相关类型和API
import { type UserResponse, type UserGroup, adminApi } from '../../../api/user'
// 消息提示工具
import toast from '../../../utils/toast'

/**
 * 搜索和排序相关的响应式变量
 */
const searchQuery = ref('')
const sortBy = ref<keyof UserResponse>('createdAt')
const sortOrder = ref<'asc' | 'desc'>('desc')
const roleFilter = ref('')
const statusFilter = ref('')
const searchInput = ref<HTMLInputElement | null>(null)

/**
 * 管理员用户状态管理实例
 */
const adminUserStore = useAdminUserStore()

/**
 * 从状态管理中获取响应式数据
 * - users: 用户列表
 * - loading: 加载状态
 * - error: 错误信息
 * - pendingUserChanges: 待应用的用户变更
 * - roles: 角色列表
 */
const { users, loading, error, pendingUserChanges, roles } = storeToRefs(adminUserStore)

/**
 * 从状态管理中获取方法
 * - loadUsers: 加载用户列表
 * - loadRoles: 加载角色列表
 * - loadRolesDetails: 加载角色详情
 * - deleteUser: 删除用户
 * - updateUserGroup: 更新用户组
 */
const { loadUsers, loadRoles, loadRolesDetails, deleteUser, updateUserGroup } = adminUserStore

/**
 * 获取当前登录用户信息
 * 从localStorage中的token解析用户信息
 * @returns {Object|null} 包含用户名和用户ID的对象，或null
 */
const getCurrentUser = () => {
  const token = localStorage.getItem('token')
  if (token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      return { username: payload.sub, userId: payload.userId }
    } catch (e) {
      return null
    }
  }
  return null
}

/**
 * 检查是否存在有效的token
 * @returns {boolean} 是否有token
 */
const hasToken = computed(() => !!localStorage.getItem('token'))

/**
 * 计算可用角色列表
 * 过滤掉superadmin角色，普通管理员不能分配超级管理员权限
 * @returns {Array} 可用角色列表
 */
const availableRoles = computed(() => {
  return roles.value.filter(role => role !== 'superadmin')
})

/**
 * 检查是否有待应用的变更
 * @returns {boolean} 是否有待应用的变更
 */
const hasPendingChanges = computed(() => {
  return Object.keys(pendingUserChanges.value).length > 0
})

/**
 * 计算待应用变更的数量
 * @returns {number} 待应用变更的数量
 */
const pendingChangesCount = computed(() => {
  return Object.values(pendingUserChanges.value).filter(change => 
    change.userGroup !== undefined || change.active !== undefined
  ).length
})

/**
 * 过滤和排序后的用户列表
 * @returns {UserResponse[]} 过滤和排序后的用户列表
 */
const filteredAndSortedUsers = computed(() => {
  let filtered = users.value.filter(user => {
    // 搜索过滤
    const searchLower = searchQuery.value.toLowerCase()
    const matchesSearch = !searchQuery.value || 
      user.username.toLowerCase().includes(searchLower) ||
      user.email.toLowerCase().includes(searchLower) ||
      (user.fullName && user.fullName.toLowerCase().includes(searchLower))
    
    // 角色过滤
    const matchesRole = !roleFilter.value || user.userGroup === roleFilter.value
    
    // 状态过滤
    const matchesStatus = !statusFilter.value || 
      user.active.toString() === statusFilter.value
    
    return matchesSearch && matchesRole && matchesStatus
  })
  
  // 排序
  filtered.sort((a, b) => {
    const aValue = a[sortBy.value]
    const bValue = b[sortBy.value]
    
    let comparison = 0
    
    if (typeof aValue === 'string' && typeof bValue === 'string') {
      comparison = aValue.localeCompare(bValue)
    } else if (typeof aValue === 'number' && typeof bValue === 'number') {
      comparison = aValue - bValue
    } else if (typeof aValue === 'boolean' && typeof bValue === 'boolean') {
      comparison = Number(aValue) - Number(bValue)
    } else {
      comparison = String(aValue).localeCompare(String(bValue))
    }
    
    return sortOrder.value === 'asc' ? comparison : -comparison
  })
  
  return filtered
})

/**
 * 清空搜索条件
 */
const clearSearch = () => {
  searchQuery.value = ''
}

/**
 * 清除所有筛选条件
 */
const clearAllFilters = () => {
  searchQuery.value = ''
  roleFilter.value = ''
  statusFilter.value = ''
  sortBy.value = 'createdAt'
  sortOrder.value = 'desc'
}

/**
 * 切换排序顺序
 */
const toggleSortOrder = () => {
  sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
}

/**
 * 设置排序字段
 * @param {keyof UserResponse} field 排序字段
 */
const setSortBy = (field: keyof UserResponse) => {
  if (sortBy.value === field) {
    // 如果点击的是当前排序字段，则切换排序顺序
    toggleSortOrder()
  } else {
    // 如果点击的是新字段，则设置为升序
    sortBy.value = field
    sortOrder.value = 'asc'
  }
}

/**
 * 组件挂载时的初始化逻辑
 * 按顺序加载角色列表、角色详情和用户列表
 */
onMounted(async () => {
  await loadRoles()       // 先加载角色列表
  await loadRolesDetails() // 加载角色详情
  await loadUsers()       // 再加载用户列表
  
  // 添加键盘快捷键监听
  document.addEventListener('keydown', handleKeyboardShortcuts)
})

/**
 * 组件卸载时清理事件监听
 */
onUnmounted(() => {
  document.removeEventListener('keydown', handleKeyboardShortcuts)
})

/**
 * 键盘快捷键处理函数
 * @param {KeyboardEvent} event 键盘事件
 */
const handleKeyboardShortcuts = (event: KeyboardEvent) => {
  // Ctrl+F 或 Cmd+F 聚焦搜索框
  if ((event.ctrlKey || event.metaKey) && event.key === 'f') {
    event.preventDefault()
    searchInput.value?.focus()
    searchInput.value?.select()
  }
}

/**
 * 用户角色变更处理函数
 * @param {UserResponse} user 用户对象
 * @param {string} newGroup 新的角色组
 */
const onUserGroupChange = (user: UserResponse, newGroup: string) => {
  // 防止修改自己的角色
  const currentUser = getCurrentUser()
  if (currentUser?.username === user.username) {
    toast.error('不能修改自己的角色')
    return
  }
  
  // 暂存角色变更，等待用户点击"应用"按钮
  if (!pendingUserChanges.value[user.userId]) {
    pendingUserChanges.value[user.userId] = {}
  }
  pendingUserChanges.value[user.userId].userGroup = newGroup as UserGroup
}

/**
 * 用户状态变更处理函数
 * @param {UserResponse} user 用户对象
 * @param {boolean} newStatus 新的状态
 */
const onUserStatusChange = (user: UserResponse, newStatus: boolean) => {
  if (!pendingUserChanges.value[user.userId]) {
    pendingUserChanges.value[user.userId] = {}
  }
  pendingUserChanges.value[user.userId].active = newStatus
}

/**
 * 应用所有待处理的变更
 * 批量处理角色变更和状态变更
 */
const applyChanges = async () => {
  const changes = { ...pendingUserChanges.value }
  let hasError = false
  let successCount = 0
  
  // 遍历所有待处理的变更
  for (const userIdStr in changes) {
    const userId = Number(userIdStr)
    const user = users.value.find(u => u.userId === userId)
    if (!user) continue
    
    const change = changes[userId]
    
    try {
      // 处理角色变更
      if (change.userGroup !== undefined && change.userGroup !== user.userGroup) {
        await adminApi.updateUserGroup(userId, change.userGroup)
        successCount++
      }
      
      // 处理状态变更
      if (change.active !== undefined && change.active !== user.active) {
        if (change.active) {
          // 恢复用户
          await adminApi.restoreUser(userId)
        } else {
          // 禁用用户
          await adminApi.deleteUser(userId)
        }
        successCount++
      }
    } catch (err: any) {
      hasError = true
      toast.error(`用户 ${user.username} 更新失败: ${err.message || '未知错误'}`)
    }
  }
  
  // 显示处理结果
  if (successCount > 0 && !hasError) {
    toast.success(`成功应用 ${successCount} 项变更`)
  } else if (successCount > 0 && hasError) {
    toast.warning(`部分变更已应用，共 ${successCount} 项成功`)
  }
  
  // 清空待变更列表
  pendingUserChanges.value = {}
  
  // 重新加载用户列表
  await loadUsers()
}

/**
 * 切换用户状态处理函数
 * @param {UserResponse} user 用户对象
 */
const handleToggleUserStatus = (user: UserResponse) => {
  const currentStatus = pendingUserChanges.value[user.userId]?.active ?? user.active
  const newStatus = !currentStatus
  
  // 暂存状态变更，等待用户点击"应用"按钮
  if (!pendingUserChanges.value[user.userId]) {
    pendingUserChanges.value[user.userId] = {}
  }
  pendingUserChanges.value[user.userId].active = newStatus
}

/**
 * 删除用户处理函数（已废弃）
 * @deprecated 使用 handleToggleUserStatus 替代
 * @param {number} userId 用户ID
 */
const handleDeleteUser = async (userId: number) => {
  console.warn('handleDeleteUser is deprecated, use handleToggleUserStatus instead')
}

/**
 * 格式化日期显示
 * @param {string} dateString 日期字符串
 * @returns {string} 格式化后的日期字符串
 */
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

/**
 * 获取角色的显示名称
 * @param {string} role 角色标识
 * @returns {string} 角色显示名称
 */
const getRoleDisplayName = (role: string) => {
  const roleMap: Record<string, string> = {
    'user': 'User',
    'moderator': 'Moderator', 
    'admin': 'Admin',
    'superadmin': 'Super Admin'
  }
  return roleMap[role] || role
}
</script>

<style scoped>
/* 用户管理主容器样式 */
.user-management {
  padding: 24px;
  background: #f8fafb;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

/* 页面标题区域 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.header-icon .icon {
  width: 24px;
  height: 24px;
  fill: white;
}

.header-text {
  flex: 1;
}

.page-title {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
  letter-spacing: -0.5px;
}

.page-subtitle {
  margin: 4px 0 0 0;
  color: #718096;
  font-size: 16px;
  font-weight: 400;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 搜索和筛选区域样式 */
.search-and-filter {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #e2e8f0;
}

/* 搜索框容器样式 */
.search-box {
  position: relative;
  margin-bottom: 16px;
}

/* 搜索输入框样式 */
.search-input {
  width: 100%;
  padding: 12px 16px;
  padding-right: 40px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s;
  background: white;
}

.search-input:focus {
  outline: none;
  border-color: #48bb78;
  box-shadow: 0 0 0 3px rgba(72, 187, 120, 0.1);
}

/* 清空搜索按钮样式 */
.clear-search-btn {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: #e2e8f0;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  font-size: 12px;
  transition: all 0.2s;
}

.clear-search-btn:hover {
  background: #cbd5e0;
  color: #333;
}

/* 筛选控件容器样式 */
.filter-controls {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  align-items: center;
}

/* 排序控件样式 */
.sort-control {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-control label {
  font-size: 0.9rem;
  color: #4a5568;
  font-weight: 500;
  white-space: nowrap;
}

.sort-select {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: white;
  font-size: 0.9rem;
  cursor: pointer;
  min-width: 120px;
}

.sort-order-btn {
  background: #48bb78;
  color: white;
  border: none;
  border-radius: 6px;
  width: 36px;
  height: 36px;
  cursor: pointer;
  font-size: 1.2rem;
  font-weight: bold;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sort-order-btn:hover {
  background: #38a169;
  transform: translateY(-1px);
}

/* 角色筛选样式 */
.role-filter {
  display: flex;
  align-items: center;
  gap: 8px;
}

.role-filter label {
  font-size: 0.9rem;
  color: #4a5568;
  font-weight: 500;
  white-space: nowrap;
}

.role-filter-select {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: white;
  font-size: 0.9rem;
  cursor: pointer;
  min-width: 120px;
}

/* 状态筛选样式 */
.status-filter {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-filter label {
  font-size: 0.9rem;
  color: #4a5568;
  font-weight: 500;
  white-space: nowrap;
}

.status-filter-select {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: white;
  font-size: 0.9rem;
  cursor: pointer;
  min-width: 100px;
}

/* 页面标题样式 */
.section-header h3 {
  margin: 0;
  color: #1a202c;
  font-size: 1.4rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
}

/* 操作按钮区域样式 */
.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-shrink: 0;
}

/* 应用按钮样式 */
.apply-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #48bb78;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
  height: 38px;
  line-height: 38px;
  white-space: nowrap;
}

.apply-btn:hover:not(:disabled) {
  background: #38a169;
}

.apply-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 刷新按钮样式 */
.refresh-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 16px;
  background: #48bb78;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.2s;
  height: 38px;
  min-width: 80px;
  white-space: nowrap;
  box-shadow: 0 2px 4px rgba(72, 187, 120, 0.2);
}

.refresh-btn:hover:not(:disabled) {
  background: #38a169;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(72, 187, 120, 0.3);
}

.refresh-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(72, 187, 120, 0.2);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 加载和错误状态样式 */
.loading, .error {
  text-align: center;
  padding: 40px;
  font-size: 1.1rem;
}

.error {
  color: #e53e3e;
}

/* 用户表格容器样式 */
.users-table {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

/* 用户表格样式 */
.users-table table {
  width: 100%;
  border-collapse: collapse;
}

/* 表格单元格样式 */
.users-table th,
.users-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

/* 表格标题行样式 */
.users-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

/* 可排序表头样式 */
.sortable-header {
  cursor: pointer;
  user-select: none;
  position: relative;
  transition: background-color 0.2s;
}

.sortable-header:hover {
  background: #edf2f7 !important;
}

/* 排序指示器样式 */
.sort-indicator {
  margin-left: 6px;
  font-size: 0.8rem;
  color: #48bb78;
  font-weight: bold;
}

/* 搜索结果信息样式 */
.search-results-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 8px;
}

.results-count {
  color: #0369a1;
  font-size: 0.9rem;
  font-weight: 500;
}

.clear-filters-btn {
  background: #0ea5e9;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
}

.clear-filters-btn:hover {
  background: #0284c7;
  transform: translateY(-1px);
}

/* 空搜索结果提示样式 */
.no-results {
  text-align: center;
  padding: 60px 20px;
  background: #f9fafb;
  border-radius: 8px;
  border: 2px dashed #d1d5db;
}

.no-results-icon {
  font-size: 3rem;
  margin-bottom: 16px;
  opacity: 0.6;
}

.no-results h4 {
  margin: 0 0 8px 0;
  color: #374151;
  font-size: 1.2rem;
}

.no-results p {
  margin: 0 0 20px 0;
  color: #6b7280;
  font-size: 0.9rem;
}

/* 角色选择下拉框样式 */
.role-select {
  padding: 4px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  font-size: 12px;
}

/* 状态指示器基础样式 */
.status-indicator {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

/* 激活状态指示器样式 */
.status-active {
  background: #c8e6c9;
  color: #2e7d32;
}

/* 非激活状态指示器样式 */
.status-inactive {
  background: #ffcdd2;
  color: #d32f2f;
}

/* 操作按钮基础样式 */
.action-btn {
  padding: 4px 8px;
  margin: 0 2px;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

/* 禁用按钮样式 */
.disable-btn {
  background: #f44336;
  color: white;
}

.disable-btn:hover:not(:disabled) {
  background: #d32f2f;
}

/* 恢复按钮样式 */
.restore-btn {
  background: #4caf50;
  color: white;
}

.restore-btn:hover:not(:disabled) {
  background: #388e3c;
}

/* 禁用状态的操作按钮样式 */
.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 加载动画旋转器样式 */
.loading-spinner {
  display: inline-block;
  width: 1em;
  height: 1em;
  border: 2px solid #fff;
  border-top: 2px solid #667eea;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  vertical-align: middle;
}

/* 旋转动画关键帧定义 */
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 移动端响应式样式 */
@media (max-width: 768px) {
  .user-management {
    padding: 16px;
  }
  
  .section-header {
    gap: 12px;
    align-items: center;
  }
  
  .search-and-filter {
    padding: 16px;
  }
  
  .filter-controls {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .sort-control,
  .role-filter,
  .status-filter {
    flex-direction: column;
    align-items: stretch;
    gap: 6px;
  }
  
  .sort-control {
    flex-direction: row;
    align-items: center;
  }
  
  .sort-select,
  .role-filter-select,
  .status-filter-select {
    min-width: auto;
    width: 100%;
  }
  
  .users-table {
    font-size: 0.8rem;
    overflow-x: scroll;
  }
  
  .users-table th,
  .users-table td {
    padding: 8px 4px;
    min-width: 80px;
  }
  
  .search-input {
    font-size: 16px; /* 防止iOS缩放 */
  }
}
</style>
