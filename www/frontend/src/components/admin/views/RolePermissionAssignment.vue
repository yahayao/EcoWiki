<template>
  <div class="role-permission-assignment">
    <div class="admin-header">
      <div class="admin-header-content">
        <h1 class="admin-title">角色权限分配</h1>
        <p class="admin-subtitle">管理角色的权限分配</p>
      </div>
      <div class="admin-actions">
        <button 
          class="btn btn-primary"
          @click="showCreateRoleModal = true"
        >
          <i class="icon-plus"></i>
          创建角色
        </button>
      </div>
    </div>

    <div class="admin-content">
      <!-- 角色选择卡片 -->
      <div class="role-cards">
        <div
          v-for="role in roles"
          :key="role.roleId"
          class="role-card"
          :class="{ active: selectedRole?.roleId === role.roleId }"
          @click="selectRole(role)"
        >
          <div class="role-card-header">
            <h3>{{ role.roleName }}</h3>
            <div class="role-actions">
              <button 
                class="btn btn-sm btn-secondary"
                @click.stop="editRole(role)"
                title="编辑角色"
              >
                <i class="icon-edit"></i>
              </button>
              <button 
                class="btn btn-sm btn-danger"
                @click.stop="deleteRole(role)"
                title="删除角色"
                :disabled="role.roleName === 'admin' || role.roleName === 'user'"
              >
                <i class="icon-delete"></i>
              </button>
            </div>
          </div>
          <p class="role-description">{{ role.description || '无描述' }}</p>
          <div class="role-stats">
            <span class="permission-count">
              {{ getRolePermissionCount(role.roleId) }} 个权限
            </span>
          </div>
        </div>
      </div>

      <!-- 权限分配区域 -->
      <div v-if="selectedRole" class="permission-assignment">
        <div class="assignment-header">
          <h2>为角色 "{{ selectedRole.roleName }}" 分配权限</h2>
          <div class="assignment-actions">
            <button 
              class="btn btn-success"
              @click="savePermissions"
              :disabled="!hasChanges"
            >
              <i class="icon-save"></i>
              保存更改
            </button>
            <button 
              class="btn btn-secondary"
              @click="resetPermissions"
              :disabled="!hasChanges"
            >
              <i class="icon-refresh"></i>
              重置
            </button>
          </div>
        </div>

        <!-- 权限分组 -->
        <div class="permission-groups">
          <div 
            v-for="group in permissionGroups"
            :key="group.category"
            class="permission-group"
          >
            <div class="group-header">
              <label class="group-checkbox">
                <input
                  type="checkbox"
                  :checked="isGroupFullySelected(group.category)"
                  :indeterminate="isGroupPartiallySelected(group.category)"
                  @change="toggleGroupPermissions(group.category, ($event.target as HTMLInputElement)?.checked || false)"
                >
                <span class="checkmark"></span>
                <span class="group-title">{{ group.categoryDisplay }}</span>
              </label>
              <span class="group-count">
                {{ getSelectedPermissionsInGroup(group.category) }} / {{ group.permissions.length }}
              </span>
            </div>
            
            <div class="group-permissions">
              <label 
                v-for="permission in group.permissions"
                :key="permission.permissionId"
                class="permission-item"
              >
                <input
                  type="checkbox"
                  :value="permission.permissionId"
                  v-model="selectedPermissionIds"
                  @change="onPermissionChange"
                >
                <span class="checkmark"></span>
                <div class="permission-info">
                  <span class="permission-name">{{ permission.permissionName }}</span>
                  <span class="permission-description">{{ permission.description || '无描述' }}</span>
                </div>
              </label>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-icon">
          <i class="icon-shield"></i>
        </div>
        <h3>选择一个角色</h3>
        <p>请从上方选择一个角色来配置其权限</p>
      </div>
    </div>

    <!-- 创建/编辑角色模态框 -->
    <div v-if="showCreateRoleModal || showEditRoleModal" class="modal-overlay" @click="closeModals">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>{{ showCreateRoleModal ? '创建角色' : '编辑角色' }}</h3>
          <button class="modal-close" @click="closeModals">
            <i class="icon-close"></i>
          </button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveRole">
            <div class="form-group">
              <label for="roleName">角色名称 *</label>
              <input
                id="roleName"
                type="text"
                v-model="roleForm.roleName"
                required
                :disabled="editingRole?.roleName === 'admin' || editingRole?.roleName === 'user'"
                placeholder="输入角色名称"
              >
            </div>
            <div class="form-group">
              <label for="roleDescription">描述</label>
              <textarea
                id="roleDescription"
                v-model="roleForm.description"
                placeholder="输入角色描述"
                rows="3"
              ></textarea>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-secondary" @click="closeModals">
                取消
              </button>
              <button type="submit" class="btn btn-primary">
                {{ showCreateRoleModal ? '创建' : '保存' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Toast 消息 -->
    <Toast 
      v-if="toast.show"
      :message="toast.message"
      :type="toast.type"
      @close="toast.show = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { adminApi, rolePermissionApi } from '@/api/user'
import Toast from '@/components/Toast.vue'
import type { Role, Permission, RolePermission, RoleForm } from '@/types/permission'

// 响应式数据
const roles = ref<Role[]>([])
const permissions = ref<Permission[]>([])
const rolePermissions = ref<RolePermission[]>([])
const selectedRole = ref<Role | null>(null)
const selectedPermissionIds = ref<number[]>([])
const originalPermissionIds = ref<number[]>([])

// 模态框状态
const showCreateRoleModal = ref(false)
const showEditRoleModal = ref(false)
const editingRole = ref<Role | null>(null)

// 表单数据
const roleForm = reactive<RoleForm>({
  roleName: '',
  description: ''
})

// Toast 消息
const toast = reactive({
  show: false,
  message: '',
  type: 'success' as 'success' | 'error' | 'warning'
})

// 计算属性
const hasChanges = computed(() => {
  if (!selectedRole.value) return false
  const currentIds = [...selectedPermissionIds.value].sort()
  const originalIds = [...originalPermissionIds.value].sort()
  return JSON.stringify(currentIds) !== JSON.stringify(originalIds)
})

const permissionGroups = computed(() => {
  const groups = new Map<string, Permission[]>()
  
  permissions.value.forEach(permission => {
    // 使用 permissionName 的前缀作为分类
    const category = permission.permissionName.split(':')[0] || 'other'
    if (!groups.has(category)) {
      groups.set(category, [])
    }
    groups.get(category)!.push(permission)
  })

  return Array.from(groups.entries()).map(([category, perms]) => ({
    category,
    categoryDisplay: getCategoryDisplay(category),
    permissions: perms.sort((a, b) => a.permissionName.localeCompare(b.permissionName))
  }))
})

// 工具函数
function getCategoryDisplay(category: string): string {
  const displayMap: Record<string, string> = {
    'system': '系统管理',
    'user': '用户管理',
    'content': '内容管理',
    'article': '文章管理',
    'admin': '管理功能',
    'manage': '管理功能',
    'read': '读取权限',
    'write': '写入权限',
    'delete': '删除权限',
    'other': '其他权限'
  }
  return displayMap[category] || category
}

function getRolePermissionCount(roleId: number): number {
  return rolePermissions.value.filter(rp => rp.roleId === roleId).length
}

function getSelectedPermissionsInGroup(category: string): number {
  const groupPermissions = permissions.value.filter(p => {
    const permCategory = p.permissionName.split(':')[0] || 'other'
    return permCategory === category
  })
  return groupPermissions.filter(p => selectedPermissionIds.value.includes(p.permissionId)).length
}

function isGroupFullySelected(category: string): boolean {
  const groupPermissions = permissions.value.filter(p => {
    const permCategory = p.permissionName.split(':')[0] || 'other'
    return permCategory === category
  })
  return groupPermissions.length > 0 && 
         groupPermissions.every(p => selectedPermissionIds.value.includes(p.permissionId))
}

function isGroupPartiallySelected(category: string): boolean {
  const groupPermissions = permissions.value.filter(p => {
    const permCategory = p.permissionName.split(':')[0] || 'other'
    return permCategory === category
  })
  const selectedCount = groupPermissions.filter(p => selectedPermissionIds.value.includes(p.permissionId)).length
  return selectedCount > 0 && selectedCount < groupPermissions.length
}

// 事件处理
function selectRole(role: Role) {
  if (hasChanges.value) {
    if (!confirm('有未保存的更改，确定要切换角色吗？')) {
      return
    }
  }
  
  selectedRole.value = role
  loadRolePermissions(role.roleId)
}

function toggleGroupPermissions(category: string, checked: boolean) {
  const groupPermissions = permissions.value.filter(p => {
    const permCategory = p.permissionName.split(':')[0] || 'other'
    return permCategory === category
  })
  
  if (checked) {
    // 添加该分组的所有权限
    groupPermissions.forEach(p => {
      if (!selectedPermissionIds.value.includes(p.permissionId)) {
        selectedPermissionIds.value.push(p.permissionId)
      }
    })
  } else {
    // 移除该分组的所有权限
    groupPermissions.forEach(p => {
      const index = selectedPermissionIds.value.indexOf(p.permissionId)
      if (index > -1) {
        selectedPermissionIds.value.splice(index, 1)
      }
    })
  }
}

function onPermissionChange() {
  // 权限变更时的处理（如果需要）
}

function editRole(role: Role) {
  editingRole.value = role
  roleForm.roleName = role.roleName
  roleForm.description = role.description || ''
  showEditRoleModal.value = true
}

function deleteRole(role: Role) {
  if (role.roleName === 'admin' || role.roleName === 'user') {
    showToast('系统默认角色不能删除', 'warning')
    return
  }
  
  if (confirm(`确定要删除角色 "${role.roleName}" 吗？此操作不可撤销。`)) {
    performDeleteRole(role)
  }
}

function closeModals() {
  showCreateRoleModal.value = false
  showEditRoleModal.value = false
  editingRole.value = null
  roleForm.roleName = ''
  roleForm.description = ''
}

async function saveRole() {
  try {
    if (showCreateRoleModal.value) {
      await rolePermissionApi.createRole(roleForm)
      showToast('角色创建成功', 'success')
    } else {
      await rolePermissionApi.updateRole(editingRole.value!.roleId, roleForm)
      showToast('角色更新成功', 'success')
    }
    
    closeModals()
    await loadRoles()
  } catch (error) {
    console.error('保存角色失败:', error)
    showToast('保存角色失败', 'error')
  }
}

async function performDeleteRole(role: Role) {
  try {
    await rolePermissionApi.deleteRole(role.roleId)
    showToast('角色删除成功', 'success')
    
    if (selectedRole.value?.roleId === role.roleId) {
      selectedRole.value = null
      selectedPermissionIds.value = []
      originalPermissionIds.value = []
    }
    
    await loadRoles()
  } catch (error) {
    console.error('删除角色失败:', error)
    showToast('删除角色失败', 'error')
  }
}

async function savePermissions() {
  if (!selectedRole.value) return
  
  try {
    await rolePermissionApi.assignPermissions(selectedRole.value.roleId, selectedPermissionIds.value)
    originalPermissionIds.value = [...selectedPermissionIds.value]
    showToast('权限分配保存成功', 'success')
    await loadRolePermissions(selectedRole.value.roleId)
  } catch (error) {
    console.error('保存权限分配失败:', error)
    showToast('保存权限分配失败', 'error')
  }
}

function resetPermissions() {
  selectedPermissionIds.value = [...originalPermissionIds.value]
}

function showToast(message: string, type: 'success' | 'error' | 'warning' = 'success') {
  toast.message = message
  toast.type = type
  toast.show = true
}

// 数据加载
async function loadRoles() {
  try {
    const response = await rolePermissionApi.getRoles()
    roles.value = response.data || []
  } catch (error) {
    console.error('加载角色失败:', error)
    showToast('加载角色失败', 'error')
  }
}

async function loadPermissions() {
  try {
    const response = await adminApi.getAllPermissions()
    permissions.value = response.data || []
  } catch (error) {
    console.error('加载权限失败:', error)
    showToast('加载权限失败', 'error')
  }
}

async function loadRolePermissions(roleId: number) {
  try {
    const rolePerms = await rolePermissionApi.getRolePermissions(roleId)
    selectedPermissionIds.value = rolePerms.map((rp: Permission) => rp.permissionId)
    originalPermissionIds.value = [...selectedPermissionIds.value]
  } catch (error) {
    console.error('加载角色权限失败:', error)
    showToast('加载角色权限失败', 'error')
  }
}

async function loadAllRolePermissions() {
  try {
    const response = await rolePermissionApi.getAllRolePermissions()
    rolePermissions.value = response.data || []
  } catch (error) {
    console.error('加载所有角色权限失败:', error)
  }
}

// 生命周期
onMounted(async () => {
  await Promise.all([
    loadRoles(),
    loadPermissions(),
    loadAllRolePermissions()
  ])
})

// 监听器
watch(selectedRole, (newRole) => {
  if (newRole) {
    loadRolePermissions(newRole.roleId)
  }
})
</script>

<style scoped>
.role-permission-assignment {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  background: white;
  border-bottom: 1px solid #e9ecef;
}

.admin-header-content h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
}

.admin-subtitle {
  margin: 4px 0 0 0;
  color: #64748b;
  font-size: 14px;
}

.admin-content {
  flex: 1;
  padding: 24px 32px;
  overflow-y: auto;
}

/* 角色卡片 */
.role-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.role-card {
  background: white;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
}

.role-card:hover {
  border-color: #0ea5e9;
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.15);
}

.role-card.active {
  border-color: #0ea5e9;
  background: #f0f9ff;
}

.role-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.role-card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.role-actions {
  display: flex;
  gap: 8px;
}

.role-description {
  margin: 0 0 12px 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.5;
}

.role-stats {
  font-size: 12px;
  color: #0ea5e9;
  font-weight: 500;
}

/* 权限分配区域 */
.permission-assignment {
  background: white;
  border-radius: 8px;
  padding: 24px;
}

.assignment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e9ecef;
}

.assignment-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
}

.assignment-actions {
  display: flex;
  gap: 12px;
}

/* 权限分组 */
.permission-groups {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.permission-group {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  overflow: hidden;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.group-checkbox {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-weight: 600;
  color: #1e293b;
}

.group-title {
  margin-left: 12px;
  font-size: 16px;
}

.group-count {
  font-size: 14px;
  color: #64748b;
  background: #e9ecef;
  padding: 4px 8px;
  border-radius: 12px;
}

.group-permissions {
  padding: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.permission-item {
  display: flex;
  align-items: flex-start;
  cursor: pointer;
  padding: 12px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.permission-item:hover {
  background: #f8f9fa;
}

.permission-info {
  margin-left: 12px;
  flex: 1;
}

.permission-name {
  display: block;
  font-weight: 500;
  color: #1e293b;
  margin-bottom: 4px;
}

.permission-description {
  display: block;
  font-size: 12px;
  color: #64748b;
  line-height: 1.4;
}

/* 复选框样式 */
input[type="checkbox"] {
  position: relative;
  width: 18px;
  height: 18px;
  cursor: pointer;
  appearance: none;
  border: 2px solid #d1d5db;
  border-radius: 3px;
  background: white;
  transition: all 0.2s;
}

input[type="checkbox"]:checked {
  background: #0ea5e9;
  border-color: #0ea5e9;
}

input[type="checkbox"]:checked::after {
  content: '✓';
  position: absolute;
  top: -2px;
  left: 2px;
  color: white;
  font-size: 12px;
  font-weight: bold;
}

input[type="checkbox"]:indeterminate {
  background: #64748b;
  border-color: #64748b;
}

input[type="checkbox"]:indeterminate::after {
  content: '−';
  position: absolute;
  top: -3px;
  left: 3px;
  color: white;
  font-size: 14px;
  font-weight: bold;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #64748b;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 500;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
}

.btn-primary {
  background: #0ea5e9;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #0284c7;
}

.btn-secondary {
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.btn-secondary:hover:not(:disabled) {
  background: #e2e8f0;
}

.btn-success {
  background: #10b981;
  color: white;
}

.btn-success:hover:not(:disabled) {
  background: #059669;
}

.btn-danger {
  background: #ef4444;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background: #dc2626;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 8px;
  width: 500px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e9ecef;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.modal-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #64748b;
  padding: 4px;
}

.modal-body {
  padding: 24px;
}

/* 表单样式 */
.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #374151;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #0ea5e9;
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.1);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

/* 图标样式 */
.icon-plus::before { content: '+'; }
.icon-edit::before { content: '✏️'; }
.icon-delete::before { content: '🗑️'; }
.icon-save::before { content: '💾'; }
.icon-refresh::before { content: '🔄'; }
.icon-shield::before { content: '🛡️'; }
.icon-close::before { content: '×'; }
</style>
