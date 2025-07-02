<template>
  <div class="permission-groups-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M12,1L21,5V11C21,16.55 17.16,21.74 12,23C6.84,21.74 3,16.55 3,11V5L12,1M12,7C10.89,7 10,7.89 10,9A2,2 0 0,0 12,11A2,2 0 0,0 14,9C14,7.89 13.11,7 12,7M15,20C15,16.69 13.65,15 12,15C10.35,15 9,16.69 9,20H15Z" />
          </svg>
        </div>
        <div class="header-text">
          <h1>权限分组管理</h1>
          <p>管理权限分组和细分权限设置</p>
        </div>
      </div>
      <div class="header-actions">
        <button class="btn btn-refresh" @click="refreshData">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M17.65,6.35C16.2,4.9 14.21,4 12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20C15.73,20 18.84,17.45 19.73,14H17.65C16.83,16.33 14.61,18 12,18A6,6 0 0,1 6,12A6,6 0 0,1 12,6C13.66,6 15.14,6.69 16.22,7.78L13,11H20V4L17.65,6.35Z" />
          </svg>
        </button>
        <button class="btn btn-primary" @click="openCreateGroupDialog">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M19,13H13V19H11V13H5V11H11V5H13V11H19V13Z" />
          </svg>
          新建分组
        </button>
      </div>
    </div>

    <!-- 分组列表 -->
    <div class="groups-container">
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载权限分组中...</p>
      </div>

      <div v-else-if="permissionGroups.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" class="empty-icon">
          <path d="M12,1L21,5V11C21,16.55 17.16,21.74 12,23C6.84,21.74 3,16.55 3,11V5L12,1Z" />
        </svg>
        <h3>暂无权限分组</h3>
        <p>点击上方按钮创建第一个权限分组</p>
      </div>

      <div v-else class="groups-list">
        <div 
          v-for="group in permissionGroups" 
          :key="group.groupId"
          class="group-card"
          :class="{ 'group-card--inactive': !group.isActive }"
        >
          <!-- 分组头部 -->
          <div class="group-header">
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
                <h3>{{ group.groupName }}</h3>
                <p v-if="group.groupDescription">{{ group.groupDescription }}</p>
                <div class="group-meta">
                  <span class="group-key">{{ group.groupKey }}</span>
                  <span class="permissions-count">{{ group.permissions?.length || 0 }} 个权限</span>
                </div>
              </div>
            </div>
            <div class="group-actions">
              <button 
                class="btn btn-ghost btn-sm"
                @click="openEditGroupDialog(group)"
                title="编辑分组"
              >
                <svg viewBox="0 0 24 24" class="icon">
                  <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z" />
                </svg>
              </button>
              <button 
                class="btn btn-ghost btn-sm"
                @click="openAddPermissionDialog(group)"
                title="添加权限"
              >
                <svg viewBox="0 0 24 24" class="icon">
                  <path d="M19,13H13V19H11V13H5V11H11V5H13V11H19V13Z" />
                </svg>
              </button>
              <button 
                v-if="!group.isSystem"
                class="btn btn-ghost btn-sm btn-danger"
                @click="confirmDeleteGroup(group)"
                title="删除分组"
              >
                <svg viewBox="0 0 24 24" class="icon">
                  <path d="M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z" />
                </svg>
              </button>
            </div>
          </div>

          <!-- 权限列表 -->
          <div v-if="group.permissions && group.permissions.length > 0" class="permissions-list">
            <div class="permissions-header">
              <h4>细分权限</h4>
              <span class="permissions-count-badge">{{ group.permissions.length }}</span>
            </div>
            <div class="permissions-grid">
              <div 
                v-for="permission in group.permissions" 
                :key="permission.permissionId"
                class="permission-item"
                :class="{ 'permission-item--system': permission.isSystem }"
              >
                <div class="permission-content">
                  <div class="permission-key">{{ permission.permissionKey }}</div>
                  <div class="permission-name">{{ permission.permissionName }}</div>
                  <div v-if="permission.description" class="permission-description">
                    {{ permission.description }}
                  </div>
                </div>
                <div class="permission-actions">
                  <button 
                    v-if="!permission.isSystem"
                    class="btn btn-ghost btn-xs"
                    @click="openEditPermissionDialog(permission)"
                  >
                    <svg viewBox="0 0 24 24" class="icon">
                      <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z" />
                    </svg>
                  </button>
                  <button 
                    v-if="!permission.isSystem"
                    class="btn btn-ghost btn-xs btn-danger"
                    @click="confirmDeletePermission(permission)"
                  >
                    <svg viewBox="0 0 24 24" class="icon">
                      <path d="M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z" />
                    </svg>
                  </button>
                  <div v-if="permission.isSystem" class="system-badge">
                    系统内置
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else class="empty-permissions">
            <p>此分组暂无权限</p>
            <button class="btn btn-ghost btn-sm" @click="openAddPermissionDialog(group)">
              添加权限
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分组编辑对话框 -->
    <div v-if="showGroupDialog" class="modal-overlay" @click="closeGroupDialog">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>{{ editingGroup ? '编辑分组' : '新建分组' }}</h2>
          <button class="btn btn-ghost btn-sm" @click="closeGroupDialog">
            <svg viewBox="0 0 24 24" class="icon">
              <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z" />
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveGroup">
            <div class="form-group">
              <label>分组标识符</label>
              <input 
                v-model="groupForm.groupKey" 
                type="text" 
                class="form-control"
                :disabled="!!editingGroup"
                placeholder="例如：content, users, admin"
                required
              />
            </div>
            <div class="form-group">
              <label>分组名称</label>
              <input 
                v-model="groupForm.groupName" 
                type="text" 
                class="form-control"
                placeholder="例如：内容管理"
                required
              />
            </div>
            <div class="form-group">
              <label>分组描述</label>
              <textarea 
                v-model="groupForm.groupDescription" 
                class="form-control"
                rows="3"
                placeholder="描述此分组的用途和包含的权限类型"
              ></textarea>
            </div>
            <div class="form-group">
              <label>图标</label>
              <input 
                v-model="groupForm.icon" 
                type="text" 
                class="form-control"
                placeholder="Material Design Icons 路径"
              />
            </div>
            <div class="form-group">
              <label>排序顺序</label>
              <input 
                v-model.number="groupForm.sortOrder" 
                type="number" 
                class="form-control"
                min="0"
              />
            </div>
            <div class="form-group">
              <label class="checkbox-label">
                <input 
                  v-model="groupForm.isActive" 
                  type="checkbox"
                />
                启用此分组
              </label>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-ghost" @click="closeGroupDialog">
            取消
          </button>
          <button type="button" class="btn btn-primary" @click="saveGroup">
            {{ editingGroup ? '更新' : '创建' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 权限编辑对话框 -->
    <div v-if="showPermissionDialog" class="modal-overlay" @click="closePermissionDialog">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>{{ editingPermission ? '编辑权限' : '新建权限' }}</h2>
          <button class="btn btn-ghost btn-sm" @click="closePermissionDialog">
            <svg viewBox="0 0 24 24" class="icon">
              <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z" />
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="savePermission">
            <div class="form-group">
              <label>权限标识符</label>
              <input 
                v-model="permissionForm.permissionKey" 
                type="text" 
                class="form-control"
                placeholder="例如：read:pages, write:users"
                required
              />
            </div>
            <div class="form-group">
              <label>权限名称</label>
              <input 
                v-model="permissionForm.permissionName" 
                type="text" 
                class="form-control"
                placeholder="例如：查看页面"
                required
              />
            </div>
            <div class="form-group">
              <label>权限描述</label>
              <textarea 
                v-model="permissionForm.description" 
                class="form-control"
                rows="3"
                placeholder="详细描述此权限的作用和范围"
              ></textarea>
            </div>
            <div class="form-group">
              <label>排序顺序</label>
              <input 
                v-model.number="permissionForm.sortOrder" 
                type="number" 
                class="form-control"
                min="0"
              />
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-ghost" @click="closePermissionDialog">
            取消
          </button>
          <button type="button" class="btn btn-primary" @click="savePermission">
            {{ editingPermission ? '更新' : '创建' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { permissionGroupApi } from '@/api/user'
import type { PermissionGroup, Permission, PermissionGroupForm, PermissionForm } from '@/types/permission'

// 响应式数据
const loading = ref(false)
const permissionGroups = ref<PermissionGroup[]>([])

// 对话框状态
const showGroupDialog = ref(false)
const showPermissionDialog = ref(false)
const editingGroup = ref<PermissionGroup | null>(null)
const editingPermission = ref<Permission | null>(null)
const currentGroup = ref<PermissionGroup | null>(null)

// 表单数据
const groupForm = reactive<PermissionGroupForm>({
  groupKey: '',
  groupName: '',
  groupDescription: '',
  icon: '',
  sortOrder: 0,
  isActive: true
})

const permissionForm = reactive<PermissionForm>({
  permissionName: '',
  description: '',
  permissionKey: '',
  groupId: 0,
  sortOrder: 0
})

// 生命周期
onMounted(() => {
  loadPermissionGroups()
})

// 方法
async function loadPermissionGroups() {
  loading.value = true
  try {
    permissionGroups.value = await permissionGroupApi.getAllPermissionGroups()
  } catch (error) {
    console.error('加载权限分组失败:', error)
  } finally {
    loading.value = false
  }
}

async function refreshData() {
  await loadPermissionGroups()
}

// 分组操作
function openCreateGroupDialog() {
  editingGroup.value = null
  resetGroupForm()
  showGroupDialog.value = true
}

function openEditGroupDialog(group: PermissionGroup) {
  editingGroup.value = group
  Object.assign(groupForm, {
    groupKey: group.groupKey,
    groupName: group.groupName,
    groupDescription: group.groupDescription || '',
    icon: group.icon || '',
    sortOrder: group.sortOrder,
    isActive: group.isActive
  })
  showGroupDialog.value = true
}

function closeGroupDialog() {
  showGroupDialog.value = false
  editingGroup.value = null
  resetGroupForm()
}

function resetGroupForm() {
  Object.assign(groupForm, {
    groupKey: '',
    groupName: '',
    groupDescription: '',
    icon: '',
    sortOrder: 0,
    isActive: true
  })
}

async function saveGroup() {
  try {
    if (editingGroup.value) {
      await permissionGroupApi.updatePermissionGroup(editingGroup.value.groupId!, groupForm)
    } else {
      await permissionGroupApi.createPermissionGroup(groupForm)
    }
    closeGroupDialog()
    await loadPermissionGroups()
  } catch (error) {
    console.error('保存分组失败:', error)
  }
}

function confirmDeleteGroup(group: PermissionGroup) {
  if (confirm(`确认删除分组"${group.groupName}"吗？此操作不可恢复。`)) {
    deleteGroup(group.groupId!)
  }
}

async function deleteGroup(groupId: number) {
  try {
    await permissionGroupApi.deletePermissionGroup(groupId)
    await loadPermissionGroups()
  } catch (error) {
    console.error('删除分组失败:', error)
  }
}

// 权限操作
function openAddPermissionDialog(group: PermissionGroup) {
  currentGroup.value = group
  editingPermission.value = null
  resetPermissionForm()
  permissionForm.groupId = group.groupId!
  showPermissionDialog.value = true
}

function openEditPermissionDialog(permission: Permission) {
  editingPermission.value = permission
  Object.assign(permissionForm, {
    permissionName: permission.permissionName,
    description: permission.description || '',
    permissionKey: permission.permissionKey,
    groupId: permission.groupId!,
    sortOrder: permission.sortOrder
  })
  showPermissionDialog.value = true
}

function closePermissionDialog() {
  showPermissionDialog.value = false
  editingPermission.value = null
  currentGroup.value = null
  resetPermissionForm()
}

function resetPermissionForm() {
  Object.assign(permissionForm, {
    permissionName: '',
    description: '',
    permissionKey: '',
    groupId: 0,
    sortOrder: 0
  })
}

async function savePermission() {
  try {
    if (editingPermission.value) {
      // 更新权限的API需要单独实现
      console.log('更新权限功能待实现')
    } else {
      await permissionGroupApi.addPermissionToGroup(permissionForm.groupId, permissionForm)
    }
    closePermissionDialog()
    await loadPermissionGroups()
  } catch (error) {
    console.error('保存权限失败:', error)
  }
}

function confirmDeletePermission(permission: Permission) {
  if (confirm(`确认删除权限"${permission.permissionName}"吗？此操作不可恢复。`)) {
    // 删除权限的API需要单独实现
    console.log('删除权限功能待实现', permission)
  }
}

// 工具方法
function getIconPath(iconName: string): string {
  // 这里可以根据图标名称返回对应的SVG路径
  // 简化处理，返回默认图标
  return "M12,1L21,5V11C21,16.55 17.16,21.74 12,23C6.84,21.74 3,16.55 3,11V5L12,1Z"
}
</script>

<style scoped>
.permission-groups-management {
  padding: 24px;
  background: #f8f9fa;
  min-height: 100vh;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding: 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 48px;
  height: 48px;
  background: #2196f3;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-icon .icon {
  width: 24px;
  height: 24px;
  fill: white;
}

.header-text h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.header-text p {
  margin: 4px 0 0;
  color: #666;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
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

.btn-primary:hover {
  background: #1976d2;
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

.btn-refresh {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  padding: 8px;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 13px;
}

.btn-xs {
  padding: 4px 8px;
  font-size: 12px;
}

.btn-danger {
  color: #e53e3e;
}

.btn-danger:hover {
  background: #fed7d7;
  border-color: #e53e3e;
}

.btn .icon {
  width: 16px;
  height: 16px;
  fill: currentColor;
}

/* 加载和空状态 */
.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #2196f3;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-icon {
  width: 64px;
  height: 64px;
  fill: #ccc;
  margin-bottom: 16px;
}

/* 分组列表 */
.groups-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.group-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
  transition: box-shadow 0.2s;
}

.group-card:hover {
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

.group-card--inactive {
  opacity: 0.6;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
}

.group-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.group-icon {
  width: 40px;
  height: 40px;
  background: #e3f2fd;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.group-icon .icon {
  width: 20px;
  height: 20px;
  fill: #2196f3;
}

.group-text h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.group-text p {
  margin: 4px 0 8px;
  color: #666;
  font-size: 14px;
}

.group-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
}

.group-key {
  background: #e8f5e8;
  color: #2e7d32;
  padding: 2px 8px;
  border-radius: 4px;
  font-family: monospace;
}

.permissions-count {
  color: #666;
}

.group-actions {
  display: flex;
  gap: 8px;
}

/* 权限列表 */
.permissions-list {
  padding: 20px 24px;
}

.permissions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.permissions-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.permissions-count-badge {
  background: #e3f2fd;
  color: #2196f3;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.permissions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 12px;
}

.permission-item {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.permission-item--system {
  background: #fff3cd;
  border-color: #ffeaa7;
}

.permission-content {
  flex: 1;
}

.permission-key {
  font-family: monospace;
  font-size: 12px;
  color: #2196f3;
  font-weight: 600;
  margin-bottom: 4px;
}

.permission-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.permission-description {
  font-size: 12px;
  color: #666;
  line-height: 1.4;
}

.permission-actions {
  display: flex;
  gap: 4px;
  align-items: center;
}

.system-badge {
  background: #ffc107;
  color: #856404;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 10px;
  font-weight: 500;
}

.empty-permissions {
  text-align: center;
  padding: 40px 24px;
  color: #666;
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
}

.modal-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #eee;
}

/* 表单样式 */
.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.form-control {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-control:focus {
  outline: none;
  border-color: #2196f3;
  box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.2);
}

.checkbox-label {
  display: flex !important;
  align-items: center;
  gap: 8px;
  font-weight: normal !important;
  cursor: pointer;
}

.checkbox-label input[type="checkbox"] {
  width: auto;
}
</style>
