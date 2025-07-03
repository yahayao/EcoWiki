<!-- 
  角色管理组件
  功能：管理系统角色，支持角色的增删改查操作
  作者：EcoWiki开发团队
  文件路径：c:\Users\Z9495\Desktop\EcoWiki_project\EcoWiki-3\www\frontend\src\components\admin\views\RoleManagement.vue
-->
<template>
  <div class="role-management">
    <!-- 页面标题和添加按钮区域 -->
    <div class="section-header">
      <h4>🔐 权限管理</h4>
      <button class="add-btn" @click="showAddForm = !showAddForm">
        <span v-if="showAddForm">❌ 取消</span>
        <span v-else">➕ 添加角色</span>
      </button>
    </div>

    <!-- 添加角色表单 -->
    <div v-if="showAddForm" class="add-form">
      <div class="form-row">
        <input
          v-model="newRole.roleName"
          type="text"
          placeholder="角色名称 (英文)"
          class="input-field"
        />
        <input
          v-model="newRole.description"
          type="text"
          placeholder="角色描述 (可选)"
          class="input-field"
        />
        <button @click="addRole" class="confirm-btn">确认添加</button>
      </div>
    </div>

    <!-- 角色列表表格 -->
    <div class="roles-table">
      <table>
        <thead>
          <tr>
            <th>角色ID</th>
            <th>角色名称</th>
            <th>角色描述</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <!-- 现有角色行 -->
          <tr v-for="role in filteredRoles" :key="role.roleId" 
              :class="{ 'pending-delete': pendingRoleChanges.delete.includes(role.roleId) }">
            <td>{{ role.roleId }}</td>
            <td>
              <!-- 编辑状态下的角色名称输入框 -->
              <input
                v-if="editingRole === role.roleId"
                v-model="editData.roleName"
                type="text"
                class="edit-input"
              />
              <!-- 普通状态下的角色名称显示 -->
              <span v-else">
                {{ pendingRoleChanges.update[role.roleId]?.roleName ?? role.roleName }}
              </span>
            </td>
            <td>
              <!-- 编辑状态下的角色描述输入框 -->
              <input
                v-if="editingRole === role.roleId"
                v-model="editData.description"
                type="text"
                class="edit-input"
              />
              <!-- 普通状态下的角色描述显示 -->
              <span v-else">
                {{ (pendingRoleChanges.update[role.roleId]?.description ?? role.description) || '-' }}
              </span>
            </td>
            <td>{{ formatDate(role.createdAt) }}</td>
            <td>
              <div class="action-buttons">
                <!-- 编辑按钮（仅对非系统角色显示） -->
                <button
                  v-if="editingRole !== role.roleId && !isSystemRole(role.roleName)"
                  @click="startEdit(role)"
                  class="edit-btn"
                >
                  ✏️ 编辑
                </button>
                
                <!-- 编辑模式下的保存/取消按钮 -->
                <template v-if="editingRole === role.roleId">
                  <button @click="saveEdit(role)" class="save-btn">💾 保存</button>
                  <button @click="cancelEdit" class="cancel-btn">❌ 取消</button>
                </template>
                
                <!-- 删除/恢复按钮（仅对非系统角色显示） -->
                <button
                  v-if="!isSystemRole(role.roleName)"
                  @click="toggleDelete(role.roleId)"
                  :class="pendingRoleChanges.delete.includes(role.roleId) ? 'restore-btn' : 'delete-btn'"
                >
                  {{ pendingRoleChanges.delete.includes(role.roleId) ? '🔄 恢复' : '🗑️ 删除' }}
                </button>
                
                <!-- 系统角色标识 -->
                <span v-if="isSystemRole(role.roleName)" class="system-role-tag">
                  🔒 系统角色
                </span>
              </div>
            </td>
          </tr>
          
          <!-- 待添加的角色行 -->
          <tr v-for="(newRole, index) in pendingRoleChanges.create" :key="'new-' + index" class="pending-create">
            <td>-</td>
            <td>{{ newRole.roleName }}</td>
            <td>{{ newRole.description || '-' }}</td>
            <td>待创建</td>
            <td>
              <button @click="removePendingCreate(index)" class="cancel-btn">
                ❌ 取消
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
// Vue 3 组合式 API 导入
import { ref, computed, onMounted } from 'vue'
// Pinia 状态管理工具
import { storeToRefs } from 'pinia'
// 管理员用户状态管理
import { useAdminUserStore } from '../../../stores/adminUserStore'
// 角色相关类型定义
import type { RoleResponse } from '../../../api/user'

/**
 * 管理员用户状态管理实例
 */
const adminUserStore = useAdminUserStore()

/**
 * 从状态管理中获取响应式数据
 * - rolesDetails: 角色详情列表
 * - pendingRoleChanges: 待应用的角色变更
 */
const { rolesDetails, pendingRoleChanges } = storeToRefs(adminUserStore)

/**
 * 从状态管理中获取方法
 * - loadRolesDetails: 加载角色详情
 * - addPendingRole: 添加待创建角色
 * - updatePendingRole: 更新待修改角色
 * - deletePendingRole: 标记待删除角色
 * - removePendingRole: 移除待删除标记
 */
const { loadRolesDetails, addPendingRole, updatePendingRole, deletePendingRole, removePendingRole } = adminUserStore

/**
 * 组件本地状态
 */
const showAddForm = ref(false)            // 是否显示添加角色表单
const editingRole = ref<number | null>(null) // 当前正在编辑的角色ID
const newRole = ref({ roleName: '', description: '' })    // 新角色表单数据
const editData = ref({ roleName: '', description: '' })   // 编辑角色表单数据

/**
 * 计算属性：过滤掉待删除的角色
 * @returns {Array} 过滤后的角色列表
 */
const filteredRoles = computed(() => {
  return rolesDetails.value.filter(role => !pendingRoleChanges.value.delete.includes(role.roleId))
})

/**
 * 检查是否为系统角色
 * 系统角色不允许删除和编辑
 * @param {string} roleName 角色名称
 * @returns {boolean} 是否为系统角色
 */
const isSystemRole = (roleName: string) => {
  return ['superadmin', 'admin'].includes(roleName)
}

/**
 * 格式化日期显示
 * @param {string} dateString 日期字符串
 * @returns {string} 格式化后的日期字符串
 */
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

/**
 * 添加新角色
 * 验证输入后将角色添加到待创建列表
 */
const addRole = () => {
  if (!newRole.value.roleName.trim()) {
    alert('请输入角色名称')
    return
  }
  
  addPendingRole({ ...newRole.value })
  newRole.value = { roleName: '', description: '' }
  showAddForm.value = false
}

/**
 * 移除待创建的角色
 * @param {number} index 待创建角色在数组中的索引
 */
const removePendingCreate = (index: number) => {
  pendingRoleChanges.value.create.splice(index, 1)
}

/**
 * 开始编辑角色
 * 将角色数据填入编辑表单
 * @param {RoleResponse} role 要编辑的角色对象
 */
const startEdit = (role: RoleResponse) => {
  editingRole.value = role.roleId
  editData.value = {
    roleName: pendingRoleChanges.value.update[role.roleId]?.roleName ?? role.roleName,
    description: (pendingRoleChanges.value.update[role.roleId]?.description ?? role.description) || ''
  }
}

/**
 * 保存角色编辑
 * 将编辑后的数据保存到待更新列表
 * @param {RoleResponse} role 被编辑的角色对象
 */
const saveEdit = (role: RoleResponse) => {
  updatePendingRole(role.roleId, { ...editData.value })
  editingRole.value = null
}

/**
 * 取消角色编辑
 * 清空编辑表单并退出编辑模式
 */
const cancelEdit = () => {
  editingRole.value = null
  editData.value = { roleName: '', description: '' }
}

/**
 * 切换角色删除状态
 * 在删除和恢复之间切换
 * @param {number} roleId 角色ID
 */
const toggleDelete = (roleId: number) => {
  if (pendingRoleChanges.value.delete.includes(roleId)) {
    removePendingRole(roleId)
  } else {
    deletePendingRole(roleId)
  }
}

/**
 * 组件挂载时加载角色详情数据
 */
onMounted(() => {
  loadRolesDetails()
})
</script>

<style scoped>
/* 角色管理主容器样式 */
.role-management {
  margin-top: 2rem;
  background: #f8fafc;
  border-radius: 12px;
  padding: 24px;
}

/* 页面标题和操作按钮区域布局 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

/* 页面标题样式 */
.section-header h4 {
  margin: 0;
  color: #2d3748;
  font-size: 1.2rem;
}

/* 添加角色按钮样式 */
.add-btn {
  padding: 8px 16px;
  background: #48bb78;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.add-btn:hover {
  background: #38a169;
}

/* 添加角色表单容器样式 */
.add-form {
  background: white;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 2px dashed #cbd5e0;
}

/* 表单行布局样式 */
.form-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 输入字段样式 */
.input-field {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 0.9rem;
}

/* 确认添加按钮样式 */
.confirm-btn {
  padding: 8px 16px;
  background: #4f8cff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  white-space: nowrap;
}

.confirm-btn:hover {
  background: #2563eb;
}

/* 角色表格容器样式 */
.roles-table {
  overflow-x: auto;
}

/* 角色表格样式 */
.roles-table table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 表格单元格样式 */
.roles-table th,
.roles-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
}

/* 表格标题行样式 */
.roles-table th {
  background: #f7fafc;
  font-weight: 600;
  color: #2d3748;
}

/* 编辑状态下的输入框样式 */
.edit-input {
  width: 100%;
  padding: 4px 8px;
  border: 1px solid #4f8cff;
  border-radius: 4px;
  font-size: 0.9rem;
}

/* 操作按钮组容器样式 */
.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 操作按钮基础样式 */
.edit-btn, .save-btn, .cancel-btn, .delete-btn, .restore-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.2s;
}

/* 编辑按钮样式 */
.edit-btn {
  background: #fbd38d;
  color: #744210;
}

.edit-btn:hover {
  background: #f6ad55;
}

/* 保存按钮样式 */
.save-btn {
  background: #48bb78;
  color: white;
}

.save-btn:hover {
  background: #38a169;
}

/* 取消按钮样式 */
.cancel-btn {
  background: #fed7d7;
  color: #c53030;
}

.cancel-btn:hover {
  background: #feb2b2;
}

/* 删除按钮样式 */
.delete-btn {
  background: #fed7d7;
  color: #c53030;
}

.delete-btn:hover {
  background: #feb2b2;
}

/* 恢复按钮样式 */
.restore-btn {
  background: #c6f6d5;
  color: #22543d;
}

.restore-btn:hover {
  background: #9ae6b4;
}

/* 系统角色标签样式 */
.system-role-tag {
  font-size: 0.8rem;
  color: #718096;
  padding: 4px 8px;
  background: #edf2f7;
  border-radius: 4px;
}

/* 待创建角色行高亮样式 */
.pending-create {
  background: rgba(72, 187, 120, 0.1);
}

/* 待删除角色行高亮样式 */
.pending-delete {
  background: rgba(245, 101, 101, 0.1);
  opacity: 0.7;
}

/* 移动端响应式样式 */
@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .roles-table {
    font-size: 0.8rem;
  }
  
  .roles-table th,
  .roles-table td {
    padding: 8px 4px;
  }
}
</style>
