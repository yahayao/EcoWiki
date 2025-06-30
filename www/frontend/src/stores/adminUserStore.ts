import { defineStore } from 'pinia'
import { ref } from 'vue'
import { adminApi, type UserResponse, type UserGroup, type RoleResponse } from '../api/user'
import toast from '../utils/toast'

export const useAdminUserStore = defineStore('adminUser', () => {
  const users = ref<UserResponse[]>([])
  const loading = ref(false)
  const error = ref('')
  // 全局暂存用户更改
  const pendingUserChanges = ref<Record<number, Partial<UserResponse>>>({})
  // 角色列表
  const roles = ref<string[]>([])
  // 角色详细信息
  const rolesDetails = ref<RoleResponse[]>([])
  // 角色变更暂存
  const pendingRoleChanges = ref<{
    create: Array<{ roleName: string; description?: string }>
    update: Record<number, { roleName?: string; description?: string }>
    delete: number[]
  }>({
    create: [],
    update: {},
    delete: []
  })

  // 加载角色列表
  const loadRoles = async () => {
    try {
      const response = await adminApi.getRoles()
      if (response.code === 200) {
        roles.value = response.data || []
      } else {
        error.value = `获取角色失败: ${response.message}`
      }
    } catch (err: any) {
      console.error('加载角色列表失败:', err)
      error.value = `网络错误: ${err.message}`
    }
  }

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

  // 加载角色详细信息
  const loadRolesDetails = async () => {
    try {
      const response = await adminApi.getRolesDetails()
      if (response.code === 200) {
        rolesDetails.value = response.data || []
      }
    } catch (err: any) {
      console.error('加载角色详情失败:', err)
    }
  }

  // 角色管理方法
  const addPendingRole = (roleData: { roleName: string; description?: string }) => {
    pendingRoleChanges.value.create.push(roleData)
  }

  const updatePendingRole = (roleId: number, roleData: { roleName?: string; description?: string }) => {
    pendingRoleChanges.value.update[roleId] = { ...pendingRoleChanges.value.update[roleId], ...roleData }
  }

  const deletePendingRole = (roleId: number) => {
    if (!pendingRoleChanges.value.delete.includes(roleId)) {
      pendingRoleChanges.value.delete.push(roleId)
    }
  }

  const removePendingRole = (roleId: number) => {
    const index = pendingRoleChanges.value.delete.indexOf(roleId)
    if (index > -1) {
      pendingRoleChanges.value.delete.splice(index, 1)
    }
  }

  // 批量应用所有角色变更
  const applyAllRoleChanges = async () => {
    const changes = { ...pendingRoleChanges.value }
    let hasError = false

    try {
      // 处理创建
      for (const createData of changes.create) {
        try {
          await adminApi.createRole(createData)
        } catch (e: any) {
          hasError = true
          toast.error(`创建角色 ${createData.roleName} 失败: ` + (e.message || '未知错误'))
        }
      }

      // 处理更新
      for (const roleIdStr in changes.update) {
        const roleId = Number(roleIdStr)
        const updateData = changes.update[roleId]
        try {
          await adminApi.updateRole(roleId, updateData)
        } catch (e: any) {
          hasError = true
          toast.error(`更新角色ID ${roleId} 失败: ` + (e.message || '未知错误'))
        }
      }

      // 处理删除
      for (const roleId of changes.delete) {
        try {
          await adminApi.deleteRole(roleId)
        } catch (e: any) {
          hasError = true
          toast.error(`删除角色ID ${roleId} 失败: ` + (e.message || '未知错误'))
        }
      }

      if (!hasError) {
        toast.success('所有角色变更已应用')
      }
    } catch (err: any) {
      toast.error('应用角色变更时发生错误: ' + (err.message || '未知错误'))
    }

    // 清空待处理变更
    pendingRoleChanges.value = { create: [], update: {}, delete: [] }
    // 重新加载数据
    await loadRoles()
    await loadRolesDetails()
  }

  // 批量应用所有变更（包括用户和角色）
  const applyAllChanges = async () => {
    // 先应用用户变更
    await applyAllUserChanges()
    // 再应用角色变更
    await applyAllRoleChanges()
  }

  // 清除所有待处理变更
  const clearPendingChanges = () => {
    pendingUserChanges.value = {}
    pendingRoleChanges.value = {
      create: [],
      update: {},
      delete: []
    }
  }

  return {
    users,
    loading,
    error,
    pendingUserChanges,
    roles,
    rolesDetails,
    pendingRoleChanges,
    loadUsers,
    loadRoles,
    updateUserGroup,
    toggleUserStatus,
    deleteUser,
    applyAllUserChanges,
    loadRolesDetails,
    addPendingRole,
    updatePendingRole,
    deletePendingRole,
    removePendingRole,
    applyAllRoleChanges,
    applyAllChanges,
    clearPendingChanges
  }
})
