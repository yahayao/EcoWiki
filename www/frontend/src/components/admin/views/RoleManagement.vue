<template>
  <div class="role-management">
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

    <!-- 角色列表 -->
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
          <!-- 现有角色 -->
          <tr v-for="role in filteredRoles" :key="role.roleId" 
              :class="{ 'pending-delete': pendingRoleChanges.delete.includes(role.roleId) }">
            <td>{{ role.roleId }}</td>
            <td>
              <input
                v-if="editingRole === role.roleId"
                v-model="editData.roleName"
                type="text"
                class="edit-input"
              />
              <span v-else>
                {{ pendingRoleChanges.update[role.roleId]?.roleName ?? role.roleName }}
              </span>
            </td>
            <td>
              <input
                v-if="editingRole === role.roleId"
                v-model="editData.description"
                type="text"
                class="edit-input"
              />
              <span v-else>
                {{ (pendingRoleChanges.update[role.roleId]?.description ?? role.description) || '-' }}
              </span>
            </td>
            <td>{{ formatDate(role.createdAt) }}</td>
            <td>
              <div class="action-buttons">
                <!-- 编辑按钮 -->
                <button
                  v-if="editingRole !== role.roleId && !isSystemRole(role.roleName)"
                  @click="startEdit(role)"
                  class="edit-btn"
                >
                  ✏️ 编辑
                </button>
                
                <!-- 保存/取消编辑 -->
                <template v-if="editingRole === role.roleId">
                  <button @click="saveEdit(role)" class="save-btn">💾 保存</button>
                  <button @click="cancelEdit" class="cancel-btn">❌ 取消</button>
                </template>
                
                <!-- 删除/恢复按钮 -->
                <button
                  v-if="!isSystemRole(role.roleName)"
                  @click="toggleDelete(role.roleId)"
                  :class="pendingRoleChanges.delete.includes(role.roleId) ? 'restore-btn' : 'delete-btn'"
                >
                  {{ pendingRoleChanges.delete.includes(role.roleId) ? '🔄 恢复' : '🗑️ 删除' }}
                </button>
                
                <span v-if="isSystemRole(role.roleName)" class="system-role-tag">
                  🔒 系统角色
                </span>
              </div>
            </td>
          </tr>
          
          <!-- 待添加的角色 -->
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
import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useAdminUserStore } from '../../../stores/adminUserStore'
import type { RoleResponse } from '../../../api/user'

const adminUserStore = useAdminUserStore()
const { rolesDetails, pendingRoleChanges } = storeToRefs(adminUserStore)
const { loadRolesDetails, addPendingRole, updatePendingRole, deletePendingRole, removePendingRole } = adminUserStore

// 组件状态
const showAddForm = ref(false)
const editingRole = ref<number | null>(null)
const newRole = ref({ roleName: '', description: '' })
const editData = ref({ roleName: '', description: '' })

// 过滤掉待删除的角色
const filteredRoles = computed(() => {
  return rolesDetails.value.filter(role => !pendingRoleChanges.value.delete.includes(role.roleId))
})

// 检查是否为系统角色
const isSystemRole = (roleName: string) => {
  return ['superadmin', 'admin'].includes(roleName)
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 添加角色
const addRole = () => {
  if (!newRole.value.roleName.trim()) {
    alert('请输入角色名称')
    return
  }
  
  addPendingRole({ ...newRole.value })
  newRole.value = { roleName: '', description: '' }
  showAddForm.value = false
}

// 移除待创建的角色
const removePendingCreate = (index: number) => {
  pendingRoleChanges.value.create.splice(index, 1)
}

// 开始编辑
const startEdit = (role: RoleResponse) => {
  editingRole.value = role.roleId
  editData.value = {
    roleName: pendingRoleChanges.value.update[role.roleId]?.roleName ?? role.roleName,
    description: (pendingRoleChanges.value.update[role.roleId]?.description ?? role.description) || ''
  }
}

// 保存编辑
const saveEdit = (role: RoleResponse) => {
  updatePendingRole(role.roleId, { ...editData.value })
  editingRole.value = null
}

// 取消编辑
const cancelEdit = () => {
  editingRole.value = null
  editData.value = { roleName: '', description: '' }
}

// 切换删除状态
const toggleDelete = (roleId: number) => {
  if (pendingRoleChanges.value.delete.includes(roleId)) {
    removePendingRole(roleId)
  } else {
    deletePendingRole(roleId)
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadRolesDetails()
})
</script>

<style scoped>
.role-management {
  margin-top: 2rem;
  background: #f8fafc;
  border-radius: 12px;
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h4 {
  margin: 0;
  color: #2d3748;
  font-size: 1.2rem;
}

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

.add-form {
  background: white;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 2px dashed #cbd5e0;
}

.form-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.input-field {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  font-size: 0.9rem;
}

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

.roles-table {
  overflow-x: auto;
}

.roles-table table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.roles-table th,
.roles-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
}

.roles-table th {
  background: #f7fafc;
  font-weight: 600;
  color: #2d3748;
}

.edit-input {
  width: 100%;
  padding: 4px 8px;
  border: 1px solid #4f8cff;
  border-radius: 4px;
  font-size: 0.9rem;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.edit-btn, .save-btn, .cancel-btn, .delete-btn, .restore-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.2s;
}

.edit-btn {
  background: #fbd38d;
  color: #744210;
}

.edit-btn:hover {
  background: #f6ad55;
}

.save-btn {
  background: #48bb78;
  color: white;
}

.save-btn:hover {
  background: #38a169;
}

.cancel-btn {
  background: #fed7d7;
  color: #c53030;
}

.cancel-btn:hover {
  background: #feb2b2;
}

.delete-btn {
  background: #fed7d7;
  color: #c53030;
}

.delete-btn:hover {
  background: #feb2b2;
}

.restore-btn {
  background: #c6f6d5;
  color: #22543d;
}

.restore-btn:hover {
  background: #9ae6b4;
}

.system-role-tag {
  font-size: 0.8rem;
  color: #718096;
  padding: 4px 8px;
  background: #edf2f7;
  border-radius: 4px;
}

.pending-create {
  background: rgba(72, 187, 120, 0.1);
}

.pending-delete {
  background: rgba(245, 101, 101, 0.1);
  opacity: 0.7;
}

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
