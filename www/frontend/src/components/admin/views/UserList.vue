<!-- filepath: c:\Users\Z9495\Desktop\EcoWiki_project\EcoWiki-3\www\frontend\src\components\admin\views\UserList.vue -->
<template>
  <div class="user-management">
    <div class="section-header">
      <h3>👥 用户管理</h3>
      <!-- 刷新按钮 -->
      <button class="refresh-btn" @click="loadUsers" :disabled="loading">
        <span v-if="loading" class="loading-spinner"></span>
        <span v-else>🔄</span>
        刷新
      </button>
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
                <button
                  @click="onUserStatusChange(user, !(pendingUserChanges[user.userId]?.active ?? user.active))"
                  :disabled="user.userGroup === 'superadmin'"
                  :class="['status-btn', (pendingUserChanges[user.userId]?.active ?? user.active) ? 'active' : 'inactive']"
                >
                  {{ (pendingUserChanges[user.userId]?.active ?? user.active) ? '启用' : '禁用' }}
                </button>
              </td>
              <td>{{ formatDate(user.createdAt) }}</td>
              <td>
                <button 
                  @click="handleDeleteUser(user.userId)"
                  :disabled="user.userGroup === 'superadmin'"
                  class="delete-btn"
                >
                  🗑️ 删除
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
import { type UserResponse, type UserGroup } from '../../../api/user'
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

const handleDeleteUser = async (userId: number) => {
  if (!confirm('确定要删除该用户吗？此操作不可恢复。')) return
  try {
    const response = await deleteUser(userId)
    if (response.code === 200) {
      toast.success('用户删除成功')
      await loadUsers()
    } else {
      throw new Error(response.message || '删除用户失败')
    }
  } catch (err: any) {
    toast.error(err.message || '删除用户失败')
  }
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

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
  height: 38px;
  line-height: 38px;
  max-width: 120px;
  min-width: 80px;
  white-space: nowrap;
}

.refresh-btn:hover:not(:disabled) {
  background: #5a67d8;
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

.status-btn {
  padding: 4px 12px;
  border: none;
  border-radius: 4px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
}

.status-btn.active {
  background: #48bb78;
  color: white;
}

.status-btn.inactive {
  background: #fed7d7;
  color: #c53030;
}

.status-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.delete-btn {
  padding: 4px 8px;
  background: #fed7d7;
  color: #c53030;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.2s;
}

.delete-btn:hover:not(:disabled) {
  background: #feb2b2;
}

.delete-btn:disabled {
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