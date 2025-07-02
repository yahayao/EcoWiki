<template>
  <div class="role-permissions-assignment">
    <!-- 角色选择器 -->
    <div class="role-selector">
      <h3>选择角色</h3>
      <div class="role-list">
        <div 
          v-for="role in roles" 
          :key="role.roleId"
          class="role-item"
          :class="{ 'role-item--active': selectedRole?.roleId === role.roleId }"
          @click="selectRole(role)"
        >
          <div class="role-info">
            <h4>{{ role.roleName }}</h4>
            <p v-if="role.description">{{ role.description }}</p>
          </div>
          <div class="role-stats">
            <span class="permission-count">{{ getAssignedPermissionsCount(role.roleId) }} 个权限</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 权限分配区域 -->
    <div v-if="selectedRole" class="permissions-assignment">
      <div class="assignment-header">
        <h3>为 "{{ selectedRole.roleName }}" 分配权限</h3>
        <div class="assignment-actions">
          <button class="btn btn-ghost btn-sm" @click="expandAll">
            全部展开
          </button>
          <button class="btn btn-ghost btn-sm" @click="collapseAll">
            全部收起
          </button>
          <button class="btn btn-primary" @click="saveRolePermissions" :disabled="saving">
            {{ saving ? '保存中...' : '保存权限配置' }}
          </button>
        </div>
      </div>

      <!-- 权限分组列表 -->
      <div class="permission-groups">
        <div 
          v-for="group in permissionGroups" 
          :key="group.groupId"
          class="permission-group"
          :class="{ 'permission-group--expanded': expandedGroups.has(group.groupId!) }"
        >
          <!-- 分组头部 -->
          <div class="group-header" @click="toggleGroup(group.groupId!)">
            <div class="group-info">
              <div class="group-icon">
                <svg v-if="group.icon" viewBox="0 0 24 24" class="icon">
                  <path :d="getIconPath(group.icon)" />
                </svg>
                <svg v-else viewBox="0 0 24 24" class="icon">
                  <path d="M12,1L21,5V11C21,16.55 17.16,21.74 12,23C6.84,21.74 3,16.55 3,11V5L12,1Z" />
                </svg>
              </div>
              <div class="group-text">
                <h4>{{ group.groupName }}</h4>
                <p v-if="group.groupDescription">{{ group.groupDescription }}</p>
              </div>
            </div>
            <div class="group-controls">
              <div class="group-status">
                <span class="assigned-count">
                  {{ getGroupAssignedCount(group) }}/{{ group.permissions?.length || 0 }}
                </span>
              </div>
              <label class="group-checkbox">
                <input
                  type="checkbox"
                  :checked="isGroupFullyAssigned(group)"
                  :indeterminate="isGroupPartiallyAssigned(group)"
                  @change="toggleGroupPermissions(group, $event)"
                />
                <span class="checkmark"></span>
              </label>
              <button class="expand-btn" :class="{ 'expand-btn--expanded': expandedGroups.has(group.groupId!) }">
                <svg viewBox="0 0 24 24" class="icon">
                  <path d="M7.41,8.58L12,13.17L16.59,8.58L18,10L12,16L6,10L7.41,8.58Z" />
                </svg>
              </button>
            </div>
          </div>

          <!-- 权限列表 -->
          <div v-if="expandedGroups.has(group.groupId!)" class="permissions-content">
            <div v-if="group.permissions && group.permissions.length > 0" class="permissions-list">
              <div 
                v-for="permission in group.permissions" 
                :key="permission.permissionId"
                class="permission-item"
                :class="{ 
                  'permission-item--assigned': isPermissionAssigned(permission.permissionId!),
                  'permission-item--system': permission.isSystem 
                }"
              >
                <label class="permission-label">
                  <input
                    type="checkbox"
                    :checked="isPermissionAssigned(permission.permissionId!)"
                    @change="togglePermission(permission.permissionId!, $event)"
                  />
                  <span class="checkmark"></span>
                  <div class="permission-info">
                    <div class="permission-name">{{ permission.permissionName }}</div>
                    <div class="permission-key">{{ permission.permissionKey }}</div>
                    <div v-if="permission.description" class="permission-description">
                      {{ permission.description }}
                    </div>
                  </div>
                </label>
                <div v-if="permission.isSystem" class="system-badge">
                  系统内置
                </div>
              </div>
            </div>
            <div v-else class="empty-permissions">
              <p>此分组暂无权限</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <svg viewBox="0 0 24 24" class="empty-icon">
        <path d="M12,1L21,5V11C21,16.55 17.16,21.74 12,23C6.84,21.74 3,16.55 3,11V5L12,1Z" />
      </svg>
      <h3>请选择一个角色</h3>
      <p>选择左侧的角色来配置其权限</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { userApi, permissionGroupApi } from '@/api/user'
import type { PermissionGroup, Permission } from '@/types/permission'
import toast from '@/utils/toast'

// 接口定义
interface Role {
  roleId: number
  roleName: string
  description?: string
}

interface RolePermission {
  roleId: number
  permissionId: number
}

// 响应式数据
const roles = ref<Role[]>([])
const permissionGroups = ref<PermissionGroup[]>([])
const selectedRole = ref<Role | null>(null)
const rolePermissions = ref<RolePermission[]>([])
const saving = ref(false)

// UI状态
const expandedGroups = reactive(new Set<number>())
const assignedPermissions = reactive(new Set<number>())

// 生命周期
onMounted(async () => {
  await Promise.all([
    loadRoles(),
    loadPermissionGroups()
  ])
})

// 计算属性
const getAssignedPermissionsCount = computed(() => {
  return (roleId: number) => {
    return rolePermissions.value.filter(rp => rp.roleId === roleId).length
  }
})

// 方法
async function loadRoles() {
  try {
    // 这里需要实现获取角色列表的API
    roles.value = await userApi.getRoles()
  } catch (error) {
    console.error('加载角色失败:', error)
    toast.error('加载角色失败')
  }
}

async function loadPermissionGroups() {
  try {
    permissionGroups.value = await permissionGroupApi.getAllPermissionGroups()
  } catch (error) {
    console.error('加载权限分组失败:', error)
    toast.error('加载权限分组失败')
  }
}

async function selectRole(role: Role) {
  selectedRole.value = role
  await loadRolePermissions(role.roleId)
  
  // 默认展开所有分组
  expandAll()
}

async function loadRolePermissions(roleId: number) {
  try {
    // 这里需要实现获取角色权限的API
    const permissions = await userApi.getRolePermissions(roleId)
    assignedPermissions.clear()
    permissions.forEach((permission: any) => {
      assignedPermissions.add(permission.permissionId)
    })
  } catch (error) {
    console.error('加载角色权限失败:', error)
    toast.error('加载角色权限失败')
  }
}

function expandAll() {
  permissionGroups.value.forEach(group => {
    if (group.groupId) {
      expandedGroups.add(group.groupId)
    }
  })
}

function collapseAll() {
  expandedGroups.clear()
}

function toggleGroup(groupId: number) {
  if (expandedGroups.has(groupId)) {
    expandedGroups.delete(groupId)
  } else {
    expandedGroups.add(groupId)
  }
}

function getGroupAssignedCount(group: PermissionGroup): number {
  if (!group.permissions) return 0
  return group.permissions.filter(p => isPermissionAssigned(p.permissionId!)).length
}

function isGroupFullyAssigned(group: PermissionGroup): boolean {
  if (!group.permissions || group.permissions.length === 0) return false
  return group.permissions.every(p => isPermissionAssigned(p.permissionId!))
}

function isGroupPartiallyAssigned(group: PermissionGroup): boolean {
  if (!group.permissions || group.permissions.length === 0) return false
  const assignedCount = getGroupAssignedCount(group)
  return assignedCount > 0 && assignedCount < group.permissions.length
}

function isPermissionAssigned(permissionId: number): boolean {
  return assignedPermissions.has(permissionId)
}

function toggleGroupPermissions(group: PermissionGroup, event: Event) {
  const isChecked = (event.target as HTMLInputElement).checked
  if (!group.permissions) return
  
  group.permissions.forEach(permission => {
    if (permission.permissionId) {
      if (isChecked) {
        assignedPermissions.add(permission.permissionId)
      } else {
        assignedPermissions.delete(permission.permissionId)
      }
    }
  })
}

function togglePermission(permissionId: number, event: Event) {
  const isChecked = (event.target as HTMLInputElement).checked
  if (isChecked) {
    assignedPermissions.add(permissionId)
  } else {
    assignedPermissions.delete(permissionId)
  }
}

async function saveRolePermissions() {
  if (!selectedRole.value) return
  
  saving.value = true
  try {
    const permissionIds = Array.from(assignedPermissions)
    await userApi.updateRolePermissions(selectedRole.value.roleId, permissionIds)
    toast.success('权限配置保存成功')
  } catch (error) {
    console.error('保存权限配置失败:', error)
    toast.error('保存权限配置失败')
  } finally {
    saving.value = false
  }
}

function getIconPath(iconName: string): string {
  // 简化处理，返回默认图标
  return "M12,1L21,5V11C21,16.55 17.16,21.74 12,23C6.84,21.74 3,16.55 3,11V5L12,1Z"
}
</script>

<style scoped>
.role-permissions-assignment {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 24px;
  height: 100vh;
  overflow: hidden;
}

/* 角色选择器 */
.role-selector {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

.role-selector h3 {
  margin: 0;
  padding: 20px 24px;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid #eee;
  background: #f8f9fa;
}

.role-list {
  max-height: calc(100vh - 80px);
  overflow-y: auto;
}

.role-item {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s;
}

.role-item:hover {
  background: #f8f9fa;
}

.role-item--active {
  background: #e3f2fd;
  border-left: 4px solid #2196f3;
}

.role-info h4 {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.role-info p {
  margin: 0;
  font-size: 12px;
  color: #666;
  line-height: 1.4;
}

.role-stats {
  margin-top: 8px;
}

.permission-count {
  font-size: 12px;
  color: #2196f3;
  background: #e3f2fd;
  padding: 2px 8px;
  border-radius: 12px;
}

/* 权限分配区域 */
.permissions-assignment {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

.assignment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
  background: #f8f9fa;
}

.assignment-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.assignment-actions {
  display: flex;
  gap: 12px;
}

.permission-groups {
  max-height: calc(100vh - 140px);
  overflow-y: auto;
}

/* 权限分组 */
.permission-group {
  border-bottom: 1px solid #f0f0f0;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  cursor: pointer;
  transition: background 0.2s;
}

.group-header:hover {
  background: #f8f9fa;
}

.group-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.group-icon {
  width: 32px;
  height: 32px;
  background: #e3f2fd;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.group-icon .icon {
  width: 16px;
  height: 16px;
  fill: #2196f3;
}

.group-text h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.group-text p {
  margin: 2px 0 0;
  font-size: 12px;
  color: #666;
}

.group-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.assigned-count {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.group-checkbox {
  position: relative;
  cursor: pointer;
}

.group-checkbox input[type="checkbox"] {
  width: 16px;
  height: 16px;
  margin: 0;
  cursor: pointer;
}

.expand-btn {
  background: none;
  border: none;
  padding: 4px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
}

.expand-btn:hover {
  background: #f0f0f0;
}

.expand-btn .icon {
  width: 16px;
  height: 16px;
  fill: #666;
  transition: transform 0.2s;
}

.expand-btn--expanded .icon {
  transform: rotate(180deg);
}

/* 权限列表 */
.permissions-content {
  background: #fafafa;
  border-top: 1px solid #f0f0f0;
}

.permissions-list {
  padding: 16px 24px;
}

.permission-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 12px 16px;
  margin-bottom: 8px;
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  transition: all 0.2s;
}

.permission-item:hover {
  border-color: #2196f3;
  box-shadow: 0 2px 8px rgba(33, 150, 243, 0.1);
}

.permission-item--assigned {
  background: #e8f5e8;
  border-color: #4caf50;
}

.permission-item--system {
  background: #fff3cd;
  border-color: #ffc107;
}

.permission-label {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  cursor: pointer;
  flex: 1;
}

.permission-label input[type="checkbox"] {
  margin-top: 2px;
  cursor: pointer;
}

.permission-info {
  flex: 1;
}

.permission-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.permission-key {
  font-family: monospace;
  font-size: 12px;
  color: #2196f3;
  background: #e3f2fd;
  padding: 2px 6px;
  border-radius: 3px;
  display: inline-block;
  margin-bottom: 4px;
}

.permission-description {
  font-size: 12px;
  color: #666;
  line-height: 1.4;
}

.system-badge {
  background: #ffc107;
  color: #856404;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 10px;
  font-weight: 500;
  white-space: nowrap;
}

.empty-permissions {
  padding: 40px 24px;
  text-align: center;
  color: #666;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  color: #666;
}

.empty-icon {
  width: 64px;
  height: 64px;
  fill: #ccc;
  margin-bottom: 16px;
}

.empty-state h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #333;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: #2196f3;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #1976d2;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-ghost {
  background: transparent;
  color: #666;
  border: 1px solid #ddd;
}

.btn-ghost:hover {
  background: #f5f5f5;
  border-color: #bbb;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 13px;
}
</style>
