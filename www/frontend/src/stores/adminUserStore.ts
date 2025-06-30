import { defineStore } from 'pinia'
import { ref } from 'vue'
import { adminApi, type UserResponse, type UserGroup } from '../api/user'
import toast from '../utils/toast'

export const useAdminUserStore = defineStore('adminUser', () => {
  const users = ref<UserResponse[]>([])
  const loading = ref(false)
  const error = ref('')
  // 全局暂存用户更改
  const pendingUserChanges = ref<Record<number, Partial<UserResponse>>>({})

  // 加载用户列表
  const loadUsers = async () => {
    loading.value = true
    error.value = ''
    try {
      const response = await adminApi.getUsers(0, 100)
      if (response.code === 200) {
        users.value = response.data.content || []
      } else {
        throw new Error(response.message || '获取用户列表失败')
      }
    } catch (err: any) {
      error.value = err.message || '加载用户列表失败'
      toast.error(error.value)
    } finally {
      loading.value = false
    }
  }

  // 更新用户权限组
  const updateUserGroup = async (userId: number, newGroup: UserGroup) => {
    const response = await adminApi.updateUserGroup(userId, newGroup)
    if (response.code !== 200) throw new Error(response.message || '更新用户权限失败')
  }

  // 更新用户状态
  const toggleUserStatus = async (userId: number, active: boolean) => {
    const response = await adminApi.updateUserStatus(userId, active)
    if (response.code !== 200) throw new Error(response.message || '更新用户状态失败')
  }

  // 删除用户
  const deleteUser = async (userId: number) => {
    const response = await adminApi.deleteUser(userId)
    if (response.code !== 200) throw new Error(response.message || '删除用户失败')
    return response
  }

  // 批量应用所有变更
  const applyAllUserChanges = async () => {
    const changes = { ...pendingUserChanges.value }
    let hasError = false
    for (const userIdStr in changes) {
      const userId = Number(userIdStr)
      const user = users.value.find(u => u.userId === userId)
      if (!user) continue
      const change = changes[userId]
      try {
        if (change.userGroup !== undefined && change.userGroup !== user.userGroup) {
          await updateUserGroup(userId, change.userGroup as UserGroup)
        }
        if (change.active !== undefined && change.active !== user.active) {
          await toggleUserStatus(userId, change.active)
        }
      } catch (e: any) {
        hasError = true
        toast.error(`用户ID ${userId} 更新失败: ` + (e.message || '未知错误'))
      }
    }
    if (!hasError) toast.success('所有用户变更已应用')
    pendingUserChanges.value = {}
    await loadUsers()
  }

  return {
    users,
    loading,
    error,
    pendingUserChanges,
    loadUsers,
    updateUserGroup,
    toggleUserStatus,
    deleteUser,
    applyAllUserChanges
  }
})
