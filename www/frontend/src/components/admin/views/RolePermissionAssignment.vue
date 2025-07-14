<template>
  <!-- 角色权限分配主容器 -->
  <div class="role-permission-assignment">
    <!-- 页面头部区域 -->
    <div class="admin-header">
      <!-- 标题内容区域 -->
      <div class="admin-header-content">
        <h1 class="admin-title">角色权限分配</h1>
        <p class="admin-subtitle">管理角色的权限分配</p>
      </div>
      <!-- 操作按钮区域 -->
      <div class="admin-actions">
        <!-- 创建角色按钮，点击显示创建模态框 -->
        <button 
          class="btn btn-primary"
          @click="showCreateRoleModal = true"
        >
          <i class="icon-plus"></i>
          创建角色
        </button>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="admin-content">
      <!-- 角色卡片网格展示区域 -->
      <div class="role-cards">
        <!-- 遍历所有角色，生成角色卡片 -->
        <div
          v-for="role in roles"
          :key="role.roleId"
          class="role-card"
          :class="{ active: selectedRole?.roleId === role.roleId }"
          @click="selectRole(role)"
        >
          <!-- 角色卡片头部：包含名称和操作按钮 -->
          <div class="role-card-header">
            <h3>{{ role.roleName }}</h3>
            <div class="role-actions">
              <!-- 编辑角色按钮，使用事件修饰符阻止冒泡 -->
              <button 
                class="btn btn-sm btn-secondary"
                @click.stop="editRole(role)"
                title="编辑角色"
              >
                <i class="icon-edit"></i>
              </button>
              <!-- 删除角色按钮，系统默认角色(admin/superadmin)禁用删除 -->
              <button 
                class="btn btn-sm btn-danger"
                @click.stop="deleteRole(role)"
                title="删除角色"
                :disabled="role.roleName === 'admin' || role.roleName === 'superadmin' "
              >
                <i class="icon-delete"></i>
              </button>
            </div>
          </div>
          <!-- 角色描述信息 -->
          <p class="role-description">{{ role.description || '无描述' }}</p>
          <!-- 角色统计信息：显示权限数量 -->
          <div class="role-stats">
            <span class="permission-count">
              {{ getRolePermissionCount(role.roleId) }} 个权限
            </span>
          </div>
        </div>
      </div>

      <!-- 权限分配区域：只有选中角色时才显示 -->
      <div v-if="selectedRole" class="permission-assignment">
        <!-- 权限分配头部区域 -->
        <div class="assignment-header">
          <h2>为角色 "{{ selectedRole.roleName }}" 分配权限</h2>
          <div class="assignment-actions">
            <!-- 保存权限配置按钮，只有有变更时才启用 -->
            <button 
              class="btn btn-success"
              @click="savePermissions"
              :disabled="!hasChanges"
            >
              <i class="icon-save"></i>
              保存更改
            </button>
            <!-- 重置权限配置按钮，只有有变更时才启用 -->
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

        <!-- 权限分组展示区域 -->
        <div class="permission-groups">
          <!-- 遍历权限分组，每个分组显示一个折叠面板 -->
          <div 
            v-for="group in permissionGroups"
            :key="group.category"
            class="permission-group"
          >
            <!-- 分组头部：包含全选复选框和统计信息 -->
            <div class="group-header">
              <label class="group-checkbox">
                <!-- 分组全选复选框，支持半选状态 -->
                <input
                  type="checkbox"
                  :checked="isGroupFullySelected(group.category)"
                  :indeterminate="isGroupPartiallySelected(group.category)"
                  @change="toggleGroupPermissions(group.category, ($event.target as HTMLInputElement)?.checked || false)"
                >
                <span class="checkmark"></span>
                <span class="group-title">{{ group.categoryDisplay }}</span>
              </label>
              <!-- 显示已选择数量/总数量 -->
              <span class="group-count">
                {{ getSelectedPermissionsInGroup(group.category) }} / {{ group.permissions.length }}
              </span>
            </div>
            
            <!-- 分组内权限列表 -->
            <div class="group-permissions">
              <!-- 遍历分组内的每个权限 -->
              <label 
                v-for="permission in group.permissions"
                :key="permission.permissionId"
                class="permission-item"
              >
                <!-- 权限复选框，绑定到selectedPermissionIds数组 -->
                <input
                  type="checkbox"
                  :value="permission.permissionId"
                  v-model="selectedPermissionIds"
                  @change="onPermissionChange"
                >
                <span class="checkmark"></span>
                <!-- 权限详细信息 -->
                <div class="permission-info">
                  <span class="permission-name">{{ permission.permissionName }}</span>
                  <span class="permission-description">{{ permission.description || '无描述' }}</span>
                </div>
              </label>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态：未选择角色时的提示信息 -->
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
        <!-- 模态框头部 -->
        <div class="modal-header">
          <h3>{{ showCreateRoleModal ? '创建角色' : '编辑角色' }}</h3>
          <!-- 关闭按钮 -->
          <button class="modal-close" @click="closeModals">
            <i class="icon-close"></i>
          </button>
        </div>
        <!-- 模态框主体内容 -->
        <div class="modal-body">
          <!-- 角色表单 -->
          <form @submit.prevent="saveRole">
            <!-- 角色名称输入框 -->
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
            <!-- 角色描述输入框 -->
            <div class="form-group">
              <label for="roleDescription">描述</label>
              <textarea
                id="roleDescription"
                v-model="roleForm.description"
                placeholder="输入角色描述"
                rows="3"
              ></textarea>
            </div>
            <!-- 表单操作按钮 -->
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

    <!-- Toast 消息提示组件 -->
    <Toast 
      v-if="toast.show"
      :message="toast.message"
      :type="toast.type"
      @close="toast.show = false"
    />
  </div>
</template>

<script setup lang="ts">
// 定义组件名称，用于缓存标识
defineOptions({
  name: 'RolePermissionAssignment'
})

/**
 * 角色权限分配组件
 * 用于管理系统角色和权限的分配关系
 * 支持角色的增删改查和权限的批量分配
 */

// === Vue 3 组合式API和相关依赖导入 ===
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { rolePermissionApi } from '@/api/user'  // 角色权限相关API接口
import Toast from '../../common/Toast.vue'      // 消息提示组件
import type { Role, Permission, RolePermission, RoleForm } from '@/types/permission'

// === 核心数据状态管理 ===
const roles = ref<Role[]>([])                    // 所有角色列表数据
const permissions = ref<Permission[]>([])        // 所有权限列表数据
const rolePermissions = ref<RolePermission[]>([]) // 角色权限关联关系数据
const selectedRole = ref<Role | null>(null)      // 当前选中的角色对象
const selectedPermissionIds = ref<number[]>([])  // 当前角色已选择的权限ID数组
const originalPermissionIds = ref<number[]>([])  // 原始权限ID数组(用于变更检测)

// === UI状态管理 ===
const showCreateRoleModal = ref(false)  // 控制创建角色模态框的显示状态
const showEditRoleModal = ref(false)    // 控制编辑角色模态框的显示状态
const editingRole = ref<Role | null>(null) // 当前正在编辑的角色对象

// === 表单数据管理 ===
const roleForm = reactive<RoleForm>({
  roleName: '',     // 角色名称
  description: ''   // 角色描述
})

// === 消息提示状态管理 ===
const toast = reactive({
  show: false,      // 控制Toast显示状态
  message: '',      // Toast消息内容
  type: 'success' as 'success' | 'error' | 'warning' // Toast消息类型
})

// === 计算属性定义 ===

/**
 * 检测权限配置是否有变更
 * 用于控制保存/重置按钮的启用状态
 */
const hasChanges = computed(() => {
  if (!selectedRole.value) return false
  // 对权限ID数组进行排序后比较，确保顺序不影响比较结果
  const currentIds = [...selectedPermissionIds.value].sort()
  const originalIds = [...originalPermissionIds.value].sort()
  return JSON.stringify(currentIds) !== JSON.stringify(originalIds)
})

/**
 * 权限分组计算属性
 * 将权限按前缀分类，便于分组展示和管理
 */
const permissionGroups = computed(() => {
  const groups = new Map<string, Permission[]>()
  
  // 遍历所有权限，按权限名称前缀进行分组
  permissions.value.forEach(permission => {
    // 使用权限名称中':'之前的部分作为分类标识
    const category = permission.permissionName.split(':')[0] || 'other'
    if (!groups.has(category)) {
      groups.set(category, [])
    }
    groups.get(category)!.push(permission)
  })

  // 转换为数组格式，并为每个分组添加显示名称
  return Array.from(groups.entries()).map(([category, perms]) => ({
    category,                                    // 分组标识
    categoryDisplay: getCategoryDisplay(category), // 分组显示名称
    permissions: perms.sort((a, b) => a.permissionName.localeCompare(b.permissionName)) // 权限按名称排序
  }))
})

// === 工具函数定义 ===

/**
 * 获取权限分类的中文显示名称
 * @param category 权限分类标识
 * @returns 中文显示名称
 */
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

/**
 * 计算指定角色拥有的权限数量
 * @param roleId 角色ID
 * @returns 权限数量
 */
function getRolePermissionCount(roleId: number): number {
  return rolePermissions.value.filter(rp => rp.roleId === roleId).length
}

/**
 * 计算指定分组中已选择的权限数量
 * @param category 权限分组标识
 * @returns 已选择的权限数量
 */
function getSelectedPermissionsInGroup(category: string): number {
  const groupPermissions = permissions.value.filter(p => {
    const permCategory = p.permissionName.split(':')[0] || 'other'
    return permCategory === category
  })
  return groupPermissions.filter(p => selectedPermissionIds.value.includes(p.permissionId)).length
}

/**
 * 检查指定分组是否所有权限都已选择
 * @param category 权限分组标识
 * @returns 是否全部选择
 */
function isGroupFullySelected(category: string): boolean {
  const groupPermissions = permissions.value.filter(p => {
    const permCategory = p.permissionName.split(':')[0] || 'other'
    return permCategory === category
  })
  return groupPermissions.length > 0 && 
         groupPermissions.every(p => selectedPermissionIds.value.includes(p.permissionId))
}

/**
 * 检查指定分组是否部分权限已选择(用于半选状态)
 * @param category 权限分组标识
 * @returns 是否部分选择
 */
function isGroupPartiallySelected(category: string): boolean {
  const groupPermissions = permissions.value.filter(p => {
    const permCategory = p.permissionName.split(':')[0] || 'other'
    return permCategory === category
  })
  const selectedCount = groupPermissions.filter(p => selectedPermissionIds.value.includes(p.permissionId)).length
  return selectedCount > 0 && selectedCount < groupPermissions.length
}

// === 事件处理函数 ===

/**
 * 选择角色处理函数
 * 切换选中角色并加载该角色的权限配置
 * @param role 要选择的角色对象
 */
function selectRole(role: Role) {
  // 检查是否有未保存的变更，如果有则提示用户确认
  if (hasChanges.value) {
    if (!confirm('有未保存的更改，确定要切换角色吗？')) {
      return
    }
  }
  
  selectedRole.value = role
  loadRolePermissions(role.roleId)
}

/**
 * 权限分组全选/取消全选处理函数
 * @param category 权限分组标识
 * @param checked 是否选中
 */
function toggleGroupPermissions(category: string, checked: boolean) {
  const groupPermissions = permissions.value.filter(p => {
    const permCategory = p.permissionName.split(':')[0] || 'other'
    return permCategory === category
  })
  
  if (checked) {
    // 添加该分组的所有权限到选中列表
    groupPermissions.forEach(p => {
      if (!selectedPermissionIds.value.includes(p.permissionId)) {
        selectedPermissionIds.value.push(p.permissionId)
      }
    })
  } else {
    // 从选中列表中移除该分组的所有权限
    groupPermissions.forEach(p => {
      const index = selectedPermissionIds.value.indexOf(p.permissionId)
      if (index > -1) {
        selectedPermissionIds.value.splice(index, 1)
      }
    })
  }
}

/**
 * 权限变更处理函数
 * 当单个权限选择状态改变时的回调
 */
function onPermissionChange() {
  // 权限变更时的处理逻辑（如果需要）
  // 目前暂无特殊处理，但保留接口便于扩展
}

/**
 * 编辑角色处理函数
 * 打开编辑角色模态框并填充表单数据
 * @param role 要编辑的角色对象
 */
function editRole(role: Role) {
  editingRole.value = role
  roleForm.roleName = role.roleName
  roleForm.description = role.description || ''
  showEditRoleModal.value = true
}

/**
 * 删除角色处理函数
 * 删除指定角色，系统默认角色不可删除
 * @param role 要删除的角色对象
 */
function deleteRole(role: Role) {
  // 系统默认角色不允许删除
  if (role.roleName === 'admin' || role.roleName === 'user') {
    showToast('系统默认角色不能删除', 'warning')
    return
  }
  
  // 确认删除操作
  if (confirm(`确定要删除角色 "${role.roleName}" 吗？此操作不可撤销。`)) {
    performDeleteRole(role)
  }
}

/**
 * 关闭所有模态框并重置表单
 */
function closeModals() {
  showCreateRoleModal.value = false
  showEditRoleModal.value = false
  editingRole.value = null
  roleForm.roleName = ''
  roleForm.description = ''
}

/**
 * 保存角色信息(创建或更新)
 * 根据当前模态框状态决定是创建新角色还是更新现有角色
 */
async function saveRole() {
  try {
    if (showCreateRoleModal.value) {
      // 创建新角色
      await rolePermissionApi.createRole(roleForm)
      showToast('角色创建成功', 'success')
    } else {
      // 更新现有角色
      await rolePermissionApi.updateRole(editingRole.value!.roleId, roleForm)
      showToast('角色更新成功', 'success')
    }
    
    closeModals()
    await loadRoles() // 重新加载角色列表
  } catch (error) {
    console.error('保存角色失败:', error)
    showToast('保存角色失败', 'error')
  }
}

/**
 * 执行角色删除操作
 * @param role 要删除的角色对象
 */
async function performDeleteRole(role: Role) {
  try {
    await rolePermissionApi.deleteRole(role.roleId)
    showToast('角色删除成功', 'success')
    
    // 如果删除的是当前选中的角色，清空选择状态
    if (selectedRole.value?.roleId === role.roleId) {
      selectedRole.value = null
      selectedPermissionIds.value = []
      originalPermissionIds.value = []
    }
    
    await loadRoles() // 重新加载角色列表
  } catch (error) {
    console.error('删除角色失败:', error)
    showToast('删除角色失败', 'error')
  }
}

/**
 * 保存当前角色的权限配置
 * 将选中的权限分配给当前角色
 */
async function savePermissions() {
  if (!selectedRole.value) return
  
  try {
    await rolePermissionApi.assignPermissions(selectedRole.value.roleId, selectedPermissionIds.value)
    // 更新原始状态，用于后续变更检测
    originalPermissionIds.value = [...selectedPermissionIds.value]
    showToast('权限分配保存成功', 'success')
    // 重新加载角色权限数据以确保数据一致性
    await loadRolePermissions(selectedRole.value.roleId)
  } catch (error) {
    console.error('保存权限分配失败:', error)
    showToast('保存权限分配失败', 'error')
  }
}

/**
 * 重置权限选择到原始状态
 * 撤销未保存的权限配置变更
 */
function resetPermissions() {
  selectedPermissionIds.value = [...originalPermissionIds.value]
}

/**
 * 显示Toast消息提示
 * @param message 消息内容
 * @param type 消息类型
 */
function showToast(message: string, type: 'success' | 'error' | 'warning' = 'success') {
  toast.message = message
  toast.type = type
  toast.show = true
}

// === 数据加载函数 ===

/**
 * 加载所有角色列表
 * 从后端API获取系统中的所有角色
 */
async function loadRoles() {
  try {
    const response = await rolePermissionApi.getRoles()
    roles.value = response.data || []
  } catch (error) {
    console.error('加载角色失败:', error)
    showToast('加载角色失败', 'error')
  }
}

/**
 * 加载所有权限列表
 * 从后端API获取系统中的所有可用权限
 */
async function loadPermissions() {
  try {
    const response = await rolePermissionApi.getAllPermissions()
    permissions.value = response || []
  } catch (error) {
    console.error('加载权限失败:', error)
    showToast('加载权限失败', 'error')
  }
}

/**
 * 加载指定角色的权限配置
 * @param roleId 角色ID
 */
async function loadRolePermissions(roleId: number) {
  try {
    const rolePerms = await rolePermissionApi.getRolePermissions(roleId)
    // 提取权限ID并更新选中状态
    selectedPermissionIds.value = rolePerms.map((rp: Permission) => rp.permissionId)
    // 保存原始状态用于变更检测
    originalPermissionIds.value = [...selectedPermissionIds.value]
  } catch (error) {
    console.error('加载角色权限失败:', error)
    showToast('加载角色权限失败', 'error')
  }
}

/**
 * 加载所有角色权限关联关系
 * 用于统计每个角色的权限数量等
 */
async function loadAllRolePermissions() {
  try {
    const response = await rolePermissionApi.getAllRolePermissions()
    rolePermissions.value = response.data || []
  } catch (error) {
    console.error('加载所有角色权限失败:', error)
  }
}

// === 生命周期钩子 ===

/**
 * 组件挂载时的初始化操作
 * 并行加载所有必要的基础数据
 */
onMounted(async () => {
  await Promise.all([
    loadRoles(),              // 加载角色列表
    loadPermissions(),        // 加载权限列表
    loadAllRolePermissions()  // 加载角色权限关联
  ])
})

// === 响应式监听器 ===

/**
 * 监听选中角色的变化
 * 当用户选择不同角色时，自动加载该角色的权限配置
 */
watch(selectedRole, (newRole) => {
  if (newRole) {
    loadRolePermissions(newRole.roleId)
  }
})

/**
 * 刷新组件数据
 * 供外部调用以重新加载所有数据
 */
const refreshData = async () => {
  await loadRoles()
  await loadPermissions()
  if (selectedRole.value) {
    await loadRolePermissions(selectedRole.value.roleId)
  }
}

// 暴露方法给父组件调用
defineExpose({
  refreshData
})
</script>

<style scoped>
/* === 组件样式定义 === */

/* 主容器样式 */
.role-permission-assignment {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

/* 页面头部样式 */
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

/* 主内容区域样式 */
.admin-content {
  flex: 1;
  padding: 24px 32px;
  overflow-y: auto;
}

/* === 角色卡片样式 === */
.role-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

/* 角色卡片基础样式 */
.role-card {
  background: white;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
}

/* 角色卡片悬停效果 */
.role-card:hover {
  border-color: #0ea5e9;
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.15);
}

/* 选中角色卡片样式 */
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

/* === 权限分配区域样式 === */
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

/* === 权限分组样式 === */
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

/* === 复选框自定义样式 === */
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

/* 选中状态样式 */
input[type="checkbox"]:checked {
  background: #0ea5e9;
  border-color: #0ea5e9;
}

/* 选中状态勾选标记 */
input[type="checkbox"]:checked::after {
  content: '✓';
  position: absolute;
  top: -2px;
  left: 2px;
  color: white;
  font-size: 12px;
  font-weight: bold;
}

/* 半选状态样式 */
input[type="checkbox"]:indeterminate {
  background: #64748b;
  border-color: #64748b;
}

/* 半选状态标记 */
input[type="checkbox"]:indeterminate::after {
  content: '−';
  position: absolute;
  top: -3px;
  left: 3px;
  color: white;
  font-size: 14px;
  font-weight: bold;
}

/* === 空状态样式 === */
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

/* === 按钮样式系统 === */
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

/* 主要按钮样式 */
.btn-primary {
  background: #0ea5e9;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #0284c7;
}

/* 次要按钮样式 */
.btn-secondary {
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.btn-secondary:hover:not(:disabled) {
  background: #e2e8f0;
}

/* 成功按钮样式 */
.btn-success {
  background: #10b981;
  color: white;
}

.btn-success:hover:not(:disabled) {
  background: #059669;
}

/* 危险按钮样式 */
.btn-danger {
  background: #ef4444;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background: #dc2626;
}

/* 小号按钮样式 */
.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

/* 禁用按钮样式 */
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* === 模态框样式 === */
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

/* === 表单样式 === */
.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #374151;
}

/* 表单输入控件样式 */
.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

/* 输入控件焦点状态 */
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

/* === 图标字体样式 === */
.icon-plus::before { content: '+'; }
.icon-edit::before { content: '✏️'; }
.icon-delete::before { content: '🗑️'; }
.icon-save::before { content: '💾'; }
.icon-refresh::before { content: '🔄'; }
.icon-shield::before { content: '🛡️'; }
.icon-close::before { content: '×'; }
</style>
