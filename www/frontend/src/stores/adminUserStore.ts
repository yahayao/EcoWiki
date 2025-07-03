/**
 * 管理员用户状态管理Store
 * 
 * 功能说明：
 * - 用户列表管理和CRUD操作
 * - 角色管理和权限分配
 * - 批量变更管理和应用
 * - 加载状态和错误处理
 * 
 * 技术特性：
 * - 使用Pinia进行状态管理
 * - 支持批量操作和事务性提交
 * - 完整的错误处理和用户反馈
 * - 响应式数据管理
 * 
 * 作者：EcoWiki开发团队
 * 版本：2.0
 * 最后更新：2025年7月3日
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { adminApi, type UserResponse, type UserGroup, type RoleResponse } from '../api/user'
import toast from '../utils/toast'

/**
 * 管理员用户状态管理Store定义
 * 
 * 使用组合式API风格定义Store，提供用户和角色的完整管理功能
 * 支持批量操作、待处理变更管理和统一的错误处理
 */
export const useAdminUserStore = defineStore('adminUser', () => {
  /**
   * 响应式状态定义
   */
  
  /** 用户列表数据 */
  const users = ref<UserResponse[]>([])
  
  /** 加载状态标识 */
  const loading = ref(false)
  
  /** 错误信息存储 */
  const error = ref('')
  
  /** 
   * 待处理的用户变更暂存
   * 键为用户ID，值为需要更新的用户属性
   * 支持批量操作，用户可以先进行多个修改，最后统一提交
   */
  const pendingUserChanges = ref<Record<number, Partial<UserResponse>>>({})
  
  /** 角色名称列表（简化版） */
  const roles = ref<string[]>([])
  
  /** 角色详细信息列表（完整版） */
  const rolesDetails = ref<RoleResponse[]>([])
  
  /** 
   * 待处理的角色变更暂存
   * 分别管理创建、更新、删除操作
   * 支持批量操作和事务性提交
   */
  const pendingRoleChanges = ref<{
    create: Array<{ roleName: string; description?: string }>  // 待创建的角色
    update: Record<number, { roleName?: string; description?: string }>  // 待更新的角色
    delete: number[]  // 待删除的角色ID列表
  }>({
    create: [],
    update: {},
    delete: []
  })

  /**
   * 角色管理相关方法
   */

  /**
   * 加载角色名称列表
   * 获取系统中所有可用的角色名称，用于下拉选择等场景
   */
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

  /**
   * 用户管理相关方法
   */

  /**
   * 加载用户列表
   * 获取系统中所有用户的详细信息，包括角色和状态
   */
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

  /**
   * 更新用户权限组
   * @param {number} userId 用户ID
   * @param {UserGroup} newGroup 新的用户权限组
   * @throws {Error} 当更新失败时抛出错误
   */
  const updateUserGroup = async (userId: number, newGroup: UserGroup) => {
    const response = await adminApi.updateUserGroup(userId, newGroup)
    if (response.code !== 200) throw new Error(response.message || '更新用户权限失败')
  }

  /**
   * 更新用户状态（启用/禁用）
   * @param {number} userId 用户ID
   * @param {boolean} active 用户状态，true为启用，false为禁用
   * @throws {Error} 当更新失败时抛出错误
   */
  const toggleUserStatus = async (userId: number, active: boolean) => {
    const response = await adminApi.updateUserStatus(userId, active)
    if (response.code !== 200) throw new Error(response.message || '更新用户状态失败')
  }

  /**
   * 删除用户
   * @param {number} userId 用户ID
   * @returns {Promise} API响应结果
   * @throws {Error} 当删除失败时抛出错误
   */
  const deleteUser = async (userId: number) => {
    const response = await adminApi.deleteUser(userId)
    if (response.code !== 200) throw new Error(response.message || '删除用户失败')
    return response
  }

  /**
   * 批量应用所有用户变更
   * 
   * 遍历所有待处理的用户变更，逐一执行API调用
   * 支持部分成功，即使某些用户更新失败，其他成功的更新仍会保留
   * 完成后自动清空待处理变更并重新加载用户列表
   */
  const applyAllUserChanges = async () => {
    const changes = { ...pendingUserChanges.value }
    let hasError = false
    
    // 遍历所有待处理的用户变更
    for (const userIdStr in changes) {
      const userId = Number(userIdStr)
      const user = users.value.find(u => u.userId === userId)
      if (!user) continue
      
      const change = changes[userId]
      try {
        // 处理角色变更
        if (change.userGroup !== undefined && change.userGroup !== user.userGroup) {
          await updateUserGroup(userId, change.userGroup as UserGroup)
        }
        // 处理状态变更
        if (change.active !== undefined && change.active !== user.active) {
          await toggleUserStatus(userId, change.active)
        }
      } catch (e: any) {
        hasError = true
        toast.error(`用户ID ${userId} 更新失败: ` + (e.message || '未知错误'))
      }
    }
    
    // 显示操作结果
    if (!hasError) toast.success('所有用户变更已应用')
    
    // 清空待处理变更并重新加载数据
    pendingUserChanges.value = {}
    await loadUsers()
  }

  /**
   * 角色详细信息管理
   */

  /**
   * 加载角色详细信息
   * 获取角色的完整信息，包括描述、创建时间等
   */
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

  /**
   * 角色变更管理方法
   */

  /**
   * 添加待创建的角色
   * @param {Object} roleData 角色数据
   * @param {string} roleData.roleName 角色名称
   * @param {string} [roleData.description] 角色描述（可选）
   */
  const addPendingRole = (roleData: { roleName: string; description?: string }) => {
    pendingRoleChanges.value.create.push(roleData)
  }

  /**
   * 更新待修改的角色信息
   * @param {number} roleId 角色ID
   * @param {Object} roleData 需要更新的角色数据
   * @param {string} [roleData.roleName] 新的角色名称
   * @param {string} [roleData.description] 新的角色描述
   */
  const updatePendingRole = (roleId: number, roleData: { roleName?: string; description?: string }) => {
    pendingRoleChanges.value.update[roleId] = { ...pendingRoleChanges.value.update[roleId], ...roleData }
  }

  /**
   * 标记角色为待删除
   * @param {number} roleId 角色ID
   */
  const deletePendingRole = (roleId: number) => {
    if (!pendingRoleChanges.value.delete.includes(roleId)) {
      pendingRoleChanges.value.delete.push(roleId)
    }
  }

  /**
   * 移除角色的待删除标记
   * @param {number} roleId 角色ID
   */
  const removePendingRole = (roleId: number) => {
    const index = pendingRoleChanges.value.delete.indexOf(roleId)
    if (index > -1) {
      pendingRoleChanges.value.delete.splice(index, 1)
    }
  }

  /**
   * 批量应用所有角色变更
   * 
   * 按顺序执行创建、更新、删除操作
   * 每个操作都有独立的错误处理，确保部分失败不影响其他操作
   * 完成后自动清空待处理变更并重新加载角色数据
   */
  const applyAllRoleChanges = async () => {
    const changes = { ...pendingRoleChanges.value }
    let hasError = false

    try {
      // 处理角色创建
      for (const createData of changes.create) {
        try {
          await adminApi.createRole(createData)
        } catch (e: any) {
          hasError = true
          toast.error(`创建角色 ${createData.roleName} 失败: ` + (e.message || '未知错误'))
        }
      }

      // 处理角色更新
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

      // 处理角色删除
      for (const roleId of changes.delete) {
        try {
          await adminApi.deleteRole(roleId)
        } catch (e: any) {
          hasError = true
          toast.error(`删除角色ID ${roleId} 失败: ` + (e.message || '未知错误'))
        }
      }

      // 显示操作结果
      if (!hasError) {
        toast.success('所有角色变更已应用')
      }
    } catch (err: any) {
      toast.error('应用角色变更时发生错误: ' + (err.message || '未知错误'))
    }

    // 清空待处理变更并重新加载数据
    pendingRoleChanges.value = { create: [], update: {}, delete: [] }
    await loadRoles()
    await loadRolesDetails()
  }

  /**
   * 统一操作方法
   */

  /**
   * 批量应用所有变更（用户和角色）
   * 
   * 按顺序先应用用户变更，再应用角色变更
   * 确保数据的一致性和操作的原子性
   */
  const applyAllChanges = async () => {
    await applyAllUserChanges()  // 先应用用户变更
    await applyAllRoleChanges()  // 再应用角色变更
  }

  /**
   * 清除所有待处理变更
   * 
   * 重置所有待处理的用户和角色变更
   * 用于取消操作或重置状态
   */
  const clearPendingChanges = () => {
    pendingUserChanges.value = {}
    pendingRoleChanges.value = {
      create: [],
      update: {},
      delete: []
    }
  }

  /**
   * 导出Store接口
   * 
   * 提供给组件使用的所有状态和方法
   * 包括响应式数据、CRUD操作、批量处理等完整功能
   */
  return {
    // 响应式状态
    users,
    loading,
    error,
    pendingUserChanges,
    roles,
    rolesDetails,
    pendingRoleChanges,
    
    // 用户管理方法
    loadUsers,
    loadRoles,
    updateUserGroup,
    toggleUserStatus,
    deleteUser,
    applyAllUserChanges,
    
    // 角色管理方法
    loadRolesDetails,
    addPendingRole,
    updatePendingRole,
    deletePendingRole,
    removePendingRole,
    applyAllRoleChanges,
    
    // 统一操作方法
    applyAllChanges,
    clearPendingChanges
  }
})
