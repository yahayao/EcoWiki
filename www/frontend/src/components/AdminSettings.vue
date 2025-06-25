<template>
  <div class="admin-settings">
    <!-- 标题区 居中 -->
    <div class="settings-header-center">
      <h2 class="settings-title">系统设置</h2>
      <p class="settings-subtitle">管理用户权限和系统配置</p>
    </div>

    <!-- 应用按钮单独一行，右对齐 -->
    <div class="settings-action-row">
      <div></div>
      <button class="apply-btn-fixed" :disabled="applying" @click="applySettings">
      <span v-if="applying" class="loading-spinner"></span>
      <span v-else>应用</span>
      </button>
    </div>

    <!-- 首页风格切换 -->
    <div class="home-style-switch-row">
      <label class="switch-label">
        <span>首页风格：</span>
        <span class="switch-text">{{ homeStyle === 'simple' ? '简约首页' : '经典首页' }}</span>
        <label class="switch">
          <input type="checkbox" v-model="switchChecked" />
          <span class="slider"></span>
        </label>
      </label>
    </div>

    <!-- 统计信息 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.totalUsers || 0 }}</div>
          <div class="stat-label">总用户数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">✅</div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.activeUsers || 0 }}</div>
          <div class="stat-label">活跃用户</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">👑</div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.adminCount || 0 }}</div>
          <div class="stat-label">管理员</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">🔧</div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.moderatorCount || 0 }}</div>
          <div class="stat-label">版主</div>
        </div>
      </div>
    </div>

    <!-- 用户管理 -->
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
              <tr v-for="user in users" :key="user.id">
                <td>{{ user.id }}</td>
                <td>{{ user.username }}</td>
                <td>{{ user.email }}</td>
                <td>{{ user.fullName || '-' }}</td>
                <td>
                  <select
                    :value="pendingUserChanges[user.id]?.userGroup ?? user.userGroup"
                    @change="e => onUserGroupChange(user, (e.target as HTMLSelectElement).value)"
                    :disabled="user.userGroup === 'superadmin'"
                    class="role-select"
                  >
                    <option value="user">普通用户</option>
                    <option value="moderator">版主</option>
                    <option value="admin">管理员</option>
                    <option value="superadmin">超级管理员</option>
                  </select>
                </td>
                <td>
                  <button
                    @click="onUserStatusChange(user, !user.active)"
                    :disabled="user.userGroup === 'superadmin'"
                    :class="['status-btn', (pendingUserChanges[user.id]?.active ?? user.active) ? 'active' : 'inactive']"
                  >
                    {{ (pendingUserChanges[user.id]?.active ?? user.active) ? '启用' : '禁用' }}
                  </button>
                </td>
                <td>{{ formatDate(user.createdAt) }}</td>
                <td>
                  <button 
                    @click="deleteUser(user.id)"
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

    <!-- 系统设置 -->
    
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, watch } from 'vue'
import { adminApi, USER_GROUPS, type UserResponse, type UserGroup } from '../api/user'
import { userService } from '@/services/userService'
import toast from '../utils/toast'

// 响应式数据
const users = ref<UserResponse[]>([])
const stats = ref<any>({})
const loading = ref(false)
const error = ref('')

// 系统设置
const systemSettings = ref({
  siteName: 'EcoWiki',
  siteDescription: '知识共享平台',
  allowRegistration: true,
  emailVerification: false,
  emailNotifications: true,
  maintenanceMode: false,
  autoBackup: true,
  maxFileSize: 10,
  cacheEnabled: true
})

// 用于暂存用户修改但未应用的设置
const pendingSettings = reactive({ ...systemSettings.value })

// 新增：用于暂存用户更改
const pendingUserChanges = ref<Record<number, Partial<UserResponse>>>({})

const applying = ref(false)

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await adminApi.getUsers(0, 100) // 获取前100个用户
    if (response.code === 200) {
      users.value = response.data.content || []
    } else {
      throw new Error(response.message || '获取用户列表失败')
    }
  } catch (err: any) {
    console.error('加载用户列表失败:', err)
    error.value = err.message || '加载用户列表失败'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

// 加载统计信息
const loadStats = async () => {
  try {
    const response = await adminApi.getSystemStats()
    if (response.code === 200) {
      stats.value = response.data || {}
    }
  } catch (err: any) {
    console.error('加载统计信息失败:', err)
  }
}

// 更新用户权限组
const updateUserGroup = async (userId: number, newGroup: UserGroup) => {
  try {
    const response = await adminApi.updateUserGroup(userId, newGroup)
    if (response.code === 200) {
      toast.success('用户权限更新成功')
      await loadUsers() // 重新加载用户列表
      await loadStats() // 重新加载统计信息
    } else {
      throw new Error(response.message || '更新用户权限失败')
    }
  } catch (err: any) {
    console.error('更新用户权限失败:', err)
    toast.error(err.message || '更新用户权限失败')
    await loadUsers() // 重新加载以恢复原始状态
  }
}

// 切换用户状态
const toggleUserStatus = async (userId: number, active: boolean) => {
  try {
    const response = await adminApi.updateUserStatus(userId, active)
    if (response.code === 200) {
      toast.success(active ? '用户已启用' : '用户已禁用')
      await loadUsers()
      await loadStats()
    } else {
      throw new Error(response.message || '更新用户状态失败')
    }
  } catch (err: any) {
    console.error('更新用户状态失败:', err)
    toast.error(err.message || '更新用户状态失败')
    await loadUsers()
  }
}

// 删除用户
const deleteUser = async (userId: number) => {
  if (!confirm('确定要删除该用户吗？此操作不可恢复。')) {
    return
  }
  
  try {
    const response = await adminApi.deleteUser(userId)
    if (response.code === 200) {
      toast.success('用户删除成功')
      await loadUsers()
      await loadStats()
    } else {
      throw new Error(response.message || '删除用户失败')
    }
  } catch (err: any) {
    console.error('删除用户失败:', err)
    toast.error(err.message || '删除用户失败')
  }
}

// 首页风格
const homeStyle = ref(localStorage.getItem('homeStyle') || 'classic')
const switchChecked = ref(homeStyle.value === 'simple')

// 只切换本地变量
watch(switchChecked, (val) => {
  homeStyle.value = val ? 'simple' : 'classic'
})

// 应用按钮事件
const applySettings = async () => {
  applying.value = true
  try {
    // 1. 注释掉或删除这行
    // await userService.updateSystemSettings({ ...pendingSettings })

    // 2. 只应用用户更改
    const changes = Object.entries(pendingUserChanges.value)
    for (const [userIdStr, change] of changes) {
      const userId = Number(userIdStr)
      if (change.userGroup !== undefined) {
        await adminApi.updateUserGroup(userId, change.userGroup as UserGroup)
      }
      if (change.active !== undefined) {
        await adminApi.updateUserStatus(userId, change.active)
      }
    }
    // 清空更改
    pendingUserChanges.value = {}

    // 应用成功后同步到全局 systemSettings
    Object.assign(systemSettings.value, pendingSettings)
    toast.success('设置已应用')
    await loadUsers()
    await loadStats()

    // 保存首页风格设置
    localStorage.setItem('homeStyle', homeStyle.value)
    // 派发自定义事件，通知 DynamicHome.vue 主动切换
    window.dispatchEvent(new Event('ecowiki-home-style-change'))
    toast.success('首页风格已切换')
  } catch (e: any) {
    toast.error(e.message || '应用设置失败')
  } finally {
    applying.value = false
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 组件挂载时加载数据
onMounted(() => {
  loadUsers()
  loadStats()
})

const onUserGroupChange = (user: UserResponse, newGroup: string) => {
  if (!pendingUserChanges.value[user.id]) {
    pendingUserChanges.value[user.id] = {}
  }
  pendingUserChanges.value[user.id].userGroup = newGroup as UserGroup
}

const onUserStatusChange = (user: UserResponse, newStatus: boolean) => {
  if (!pendingUserChanges.value[user.id]) {
    pendingUserChanges.value[user.id] = {}
  }
  pendingUserChanges.value[user.id].active = newStatus
}
</script>

<style scoped>
.admin-settings {
  padding: 24px;
  background: white;
  border-radius: 12px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
}

/* 首页风格切换和应用按钮同一行，按钮靠右 */
.settings-action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
  margin-top: -0.5rem;
}

/* 应用按钮右上角固定 */
.apply-btn-fixed {
  background: #4f8cff;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 0.5rem 1.5rem;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
  z-index: 10;
  position: static; /* 取消绝对定位 */
  margin-left: 1rem;
}
.apply-btn-fixed:disabled {
  background: #b3d1ff;
  cursor: not-allowed;
}
.apply-btn-fixed:not(:disabled):hover {
  background: #2563eb;
}

/* 标题和副标题居中 */
.settings-header-center {
  text-align: center;
  margin-bottom: 1.5rem;
}
.settings-title {
  font-size: 1.5rem;
  font-weight: bold;
  margin: 0;
}
.settings-subtitle {
  color: #718096;
  font-size: 1rem;
  margin-bottom: 1.5rem;
  margin-top: 0.5rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  font-size: 2rem;
}

.stat-number {
  font-size: 1.8rem;
  font-weight: bold;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 0.9rem;
  opacity: 0.9;
}

.user-management {
  background: #f8fafc;
  padding: 24px;
  border-radius: 12px;
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

.refresh-btn, .apply-btn {
  height: 38px;
  line-height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  max-width: 120px;
  min-width: 80px;
  white-space: nowrap;
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

.settings-card {
  margin-top: 2rem;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
  padding: 1rem 1.5rem 1rem 1.5rem;
  position: relative;
}

.settings-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.settings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.settings-header.only-btn {
  justify-content: flex-end;
}

/* Switch风格 */
.home-style-switch-row {
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.switch-label {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  font-size: 1.1rem;
}

.switch-text {
  min-width: 70px;
  display: inline-block;
  text-align: center;
  color: #4f8cff;
  font-weight: 500;
}

.switch {
  position: relative;
  display: inline-block;
  width: 48px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: #ccc;
  transition: .4s;
  border-radius: 24px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 20px; width: 20px;
  left: 2px; bottom: 2px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
}

.switch input:checked + .slider {
  background-color: #4f8cff;
}

.switch input:checked + .slider:before {
  transform: translateX(24px);
}

.loading-spinner {
  display: inline-block;
  width: 1em;
  height: 1em;
  border: 2px solid #fff;
  border-top: 2px solid #4f8cff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  vertical-align: middle;
  margin-right: 0.5em;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .admin-settings {
    padding: 16px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
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