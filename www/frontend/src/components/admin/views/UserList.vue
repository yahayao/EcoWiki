<!-- 
  用户管理组件
  功能：展示用户列表，支持用户角色管理、状态切换等操作
  作者：EcoWiki开发团队
  文件路径：c:\Users\Z9495\Desktop\EcoWiki_project\EcoWiki-3\www\frontend\src\components\admin\views\UserList.vue
-->
<template>
  <div class="user-management">
    <!-- 页面标题和操作按钮区域 -->
    <div class="section-header">
      <h3>👥 用户管理</h3>
      <div class="header-actions">
        <!-- 刷新按钮 -->
        <button class="refresh-btn" @click="loadUsers" :disabled="loading">
          <span v-if="loading" class="loading-spinner"></span>
          刷新
        </button>
      </div>
    </div>

    <!-- 加载状态显示 -->
    <div v-if="loading" class="loading">加载中...</div>
    <!-- 错误状态显示 -->
    <div v-else-if="error" class="error">{{ error }}</div>
    <!-- 用户列表表格 -->
    <div v-else>
      <div class="users-table">
        <table>
          <thead>
            <tr>
              <th>用户ID</th>
              <th>用户名</th>
              <th>邮箱</th>
              <th>姓名</th>
              <th>角色</th>
              <th>状态</th>
              <th>注册时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <!-- 用户数据行循环 -->
            <tr v-for="user in users" :key="user.userId">
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
import { onMounted, computed } from 'vue'
// Pinia 状态管理工具
import { storeToRefs } from 'pinia'
// 管理员用户状态管理
import { useAdminUserStore } from '../../../stores/adminUserStore'
// 用户相关类型和API
import { type UserResponse, type UserGroup, adminApi } from '../../../api/user'
// 消息提示工具
import toast from '../../../utils/toast'

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
 * 组件挂载时的初始化逻辑
 * 按顺序加载角色列表、角色详情和用户列表
 */
onMounted(async () => {
  await loadRoles()       // 先加载角色列表
  await loadRolesDetails() // 加载角色详情
  await loadUsers()       // 再加载用户列表
})

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
  background: white;
  padding: 24px;
  border-radius: 0;
  min-height: 100vh;
}

/* 页面标题和操作按钮区域布局 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: nowrap;
  min-width: 0;
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
  overflow-x: auto;
}

/* 用户表格样式 */
.users-table table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 表格单元格样式 */
.users-table th,
.users-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
}

/* 表格标题行样式 */
.users-table th {
  background: #f7fafc;
  font-weight: 600;
  color: #2d3748;
}

/* 角色选择下拉框样式 */
.role-select {
  padding: 4px 8px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  background: white;
  font-size: 0.9rem;
}

/* 状态指示器基础样式 */
.status-indicator {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
}

/* 激活状态指示器样式 */
.status-active {
  background: #c6f6d5;
  color: #2f855a;
}

/* 非激活状态指示器样式 */
.status-inactive {
  background: #fed7d7;
  color: #c53030;
}

/* 操作按钮基础样式 */
.action-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.2s;
}

/* 禁用按钮样式 */
.disable-btn {
  background: #fed7d7;
  color: #c53030;
}

.disable-btn:hover:not(:disabled) {
  background: #feb2b2;
}

/* 恢复按钮样式 */
.restore-btn {
  background: #c6f6d5;
  color: #2f855a;
}

.restore-btn:hover:not(:disabled) {
  background: #9ae6b4;
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
  
  .users-table {
    font-size: 0.8rem;
  }
  
  .users-table th,
  .users-table td {
    padding: 8px 4px;
  }
}
</style>
