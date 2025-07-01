<!-- filepath: c:\Users\Z9495\Desktop\EcoWiki_project\EcoWiki-3\www\frontend\src\components\admin\views\UserList.vue -->
<template>
  <div class="user-management">
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

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
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
            <tr v-for="user in users" :key="user.userId">
              <td>{{ user.userId }}</td>
              <td>{{ user.username }}</td>
              <td>{{ user.email }}</td>
              <td>{{ user.fullName || '-' }}</td>
              <td>
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
                <small v-if="roles.length === 0" style="color: #e53e3e;">
                  角色数据加载失败，请刷新页面
                </small>
                <small v-if="getCurrentUser()?.username === user.username" style="color: #666; display: block;">
                  不能修改自己的角色
                </small>
              </td>
              <td>
                <span
                  :class="['status-indicator', (pendingUserChanges[user.userId]?.active ?? user.active) ? 'status-active' : 'status-inactive']"
                >
                  {{ (pendingUserChanges[user.userId]?.active ?? user.active) ? '正常' : '已禁用' }}
                </span>
              </td>
              <td>{{ formatDate(user.createdAt) }}</td>
              <td>
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
import { onMounted, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useAdminUserStore } from '../../../stores/adminUserStore'
import { type UserResponse, type UserGroup, adminApi } from '../../../api/user'
import toast from '../../../utils/toast'

const adminUserStore = useAdminUserStore()
const { users, loading, error, pendingUserChanges, roles } = storeToRefs(adminUserStore)
const { loadUsers, loadRoles, loadRolesDetails, deleteUser, updateUserGroup } = adminUserStore

// 获取当前用户信息（从token中解析）
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

// Token存在检查
const hasToken = computed(() => !!localStorage.getItem('token'))

// 计算可用角色（过滤掉superadmin，普通管理员不能分配superadmin权限）
const availableRoles = computed(() => {
  return roles.value.filter(role => role !== 'superadmin')
})

// 检查是否有待应用的变更
const hasPendingChanges = computed(() => {
  return Object.keys(pendingUserChanges.value).length > 0
})

// 计算待应用变更的数量
const pendingChangesCount = computed(() => {
  return Object.values(pendingUserChanges.value).filter(change => 
    change.userGroup !== undefined || change.active !== undefined
  ).length
})

// 组件挂载时加载数据
onMounted(async () => {
  await loadRoles() // 先加载角色列表
  await loadRolesDetails() // 加载角色详情
  await loadUsers()  // 再加载用户列表
})

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

const onUserStatusChange = (user: UserResponse, newStatus: boolean) => {
  if (!pendingUserChanges.value[user.userId]) {
    pendingUserChanges.value[user.userId] = {}
  }
  pendingUserChanges.value[user.userId].active = newStatus
}

const applyChanges = async () => {
  const changes = { ...pendingUserChanges.value }
  let hasError = false
  let successCount = 0
  
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

const handleToggleUserStatus = (user: UserResponse) => {
  const currentStatus = pendingUserChanges.value[user.userId]?.active ?? user.active
  const newStatus = !currentStatus
  
  // 暂存状态变更，等待用户点击"应用"按钮
  if (!pendingUserChanges.value[user.userId]) {
    pendingUserChanges.value[user.userId] = {}
  }
  pendingUserChanges.value[user.userId].active = newStatus
}

// 保留原删除方法但标记为废弃
const handleDeleteUser = async (userId: number) => {
  // 已废弃，使用 handleToggleUserStatus 替代
  console.warn('handleDeleteUser is deprecated, use handleToggleUserStatus instead')
}

const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 角色显示名称映射
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
.user-management {
  background: white;
  padding: 24px;
  border-radius: 0;
  min-height: 100vh;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: nowrap;
  min-width: 0;
}

.section-header h3 {
  margin: 0;
  color: #1a202c;
  font-size: 1.4rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-shrink: 0;
}

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

.loading, .error {
  text-align: center;
  padding: 40px;
  font-size: 1.1rem;
}

.error {
  color: #e53e3e;
}

.users-table {
  overflow-x: auto;
}

.users-table table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.users-table th,
.users-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
}

.users-table th {
  background: #f7fafc;
  font-weight: 600;
  color: #2d3748;
}

.role-select {
  padding: 4px 8px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  background: white;
  font-size: 0.9rem;
}

.status-indicator {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-active {
  background: #c6f6d5;
  color: #2f855a;
}

.status-inactive {
  background: #fed7d7;
  color: #c53030;
}

.action-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.2s;
}

.disable-btn {
  background: #fed7d7;
  color: #c53030;
}

.disable-btn:hover:not(:disabled) {
  background: #feb2b2;
}

.restore-btn {
  background: #c6f6d5;
  color: #2f855a;
}

.restore-btn:hover:not(:disabled) {
  background: #9ae6b4;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

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

@keyframes spin {
  to { transform: rotate(360deg); }
}

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
