<!-- 
  权限管理组件
  功能：管理系统权限，支持权限的增删改查操作
  作者：EcoWiki开发团队
  文件路径：c:\Users\Z9495\Desktop\EcoWiki_project\EcoWiki-3\www\frontend\src\components\admin\views\PermissionManagement.vue
-->
<template>
  <div class="permission-management">
    <!-- 页面头部区域 -->
    <div class="page-header">
      <div class="header-content">
        <!-- 页面图标 -->
        <div class="header-icon">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M12,1L21,5V11C21,16.55 17.16,21.74 12,23C6.84,21.74 3,16.55 3,11V5L12,1M12,7C10.89,7 10,7.89 10,9A2,2 0 0,0 12,11A2,2 0 0,0 14,9C14,7.89 13.11,7 12,7Z" />
          </svg>
        </div>
        <!-- 页面标题和描述 -->
        <div class="header-text">
          <h1>权限管理</h1>
          <p>管理系统所有权限设置</p>
        </div>
      </div>
      <!-- 页面操作按钮 -->
      <div class="header-actions">
        <!-- 刷新按钮 -->
        <button class="btn btn-refresh" @click="refreshData">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M17.65,6.35C16.2,4.9 14.21,4 12,4A8,8 0 0,0 4,12A8,8 0 0,0 12,20C15.73,20 18.84,17.45 19.73,14H17.65C16.83,16.33 14.61,18 12,18A6,6 0 0,1 6,12A6,6 0 0,1 12,6C13.66,6 15.14,6.69 16.22,7.78L13,11H20V4L17.65,6.35Z" />
          </svg>
        </button>
        <!-- 新建权限按钮 -->
        <button class="btn btn-primary" @click="openCreatePermissionDialog">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M19,13H13V19H11V13H5V11H11V5H13V11H19V13Z" />
          </svg>
          新建权限
        </button>
      </div>
    </div>

    <!-- 权限列表容器 -->
    <div class="permissions-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载权限列表中...</p>
      </div>

      <!-- 错误状态 -->
      <div v-else-if="errorMessage" class="error-state">
        <svg viewBox="0 0 24 24" class="error-icon">
          <path d="M12,2C13.1,2 14,2.9 14,4C14,5.1 13.1,6 12,6C10.9,6 10,5.1 10,4C10,2.9 10.9,2 12,2M21,9V7L15,1H5C3.89,1 3,1.89 3,3V21A2,2 0 0,0 5,23H19A2,2 0 0,0 21,21V9M12,19C10.9,19 10,18.1 10,17C10,15.9 10.9,15 12,15C13.1,15 14,15.9 14,17C14,18.1 13.1,19 12,19Z" />
        </svg>
        <h3>加载失败</h3>
        <p>{{ errorMessage }}</p>
        <button class="btn btn-primary" @click="retryLoad">
          重新加载
        </button>
      </div>

      <!-- 空状态 -->
      <div v-else-if="permissions.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" class="empty-icon">
          <path d="M12,1L21,5V11C21,16.55 17.16,21.74 12,23C6.84,21.74 3,16.55 3,11V5L12,1Z" />
        </svg>
        <h3>暂无权限</h3>
        <p>点击上方按钮创建第一个权限</p>
      </div>

      <!-- 权限列表 -->
      <div v-else class="permissions-list">
        <!-- 列表标题行 -->
        <div class="list-header">
          <div class="header-cell">权限名称</div>
          <div class="header-cell">描述</div>
          <div class="header-cell">创建时间</div>
          <div class="header-cell">操作</div>
        </div>
        
        <!-- 权限数据行 -->
        <div 
          v-for="permission in permissions" 
          :key="permission.permissionId"
          class="permission-row"
        >
          <!-- 权限名称列 -->
          <div class="cell permission-name">
            <div class="permission-info">
              <h4>{{ permission.permissionName }}</h4>
            </div>
          </div>
          
          <!-- 权限描述列 -->
          <div class="cell permission-description">
            <p v-if="permission.description">{{ permission.description }}</p>
            <span v-else class="no-description">暂无描述</span>
          </div>
          
          <!-- 创建时间列 -->
          <div class="cell permission-time">
            <span class="time-text">{{ formatTime(permission.createdAt || '') }}</span>
          </div>
          
          <!-- 操作按钮列 -->
          <div class="cell permission-actions">
            <!-- 编辑按钮 -->
            <button 
              class="btn btn-ghost btn-sm"
              @click="openEditPermissionDialog(permission)"
              title="编辑权限"
            >
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z" />
              </svg>
            </button>
            <!-- 删除按钮 -->
            <button 
              class="btn btn-ghost btn-sm btn-danger"
              @click="confirmDeletePermission(permission)"
              title="删除权限"
            >
              <svg viewBox="0 0 24 24" class="icon">
                <path d="M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 权限编辑对话框 -->
    <div v-if="showPermissionDialog" class="modal-overlay" @click="closePermissionDialog">
      <div class="modal-content" @click.stop>
        <!-- 对话框标题栏 -->
        <div class="modal-header">
          <h2>{{ editingPermission ? '编辑权限' : '新建权限' }}</h2>
          <button class="btn btn-ghost btn-sm" @click="closePermissionDialog">
            <svg viewBox="0 0 24 24" class="icon">
              <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z" />
            </svg>
          </button>
        </div>
        <!-- 对话框内容区 -->
        <div class="modal-body">
          <form @submit.prevent="savePermission">
            <!-- 权限名称输入 -->
            <div class="form-group">
              <label>权限名称</label>
              <input 
                v-model="permissionForm.permissionName" 
                type="text" 
                class="form-control"
                placeholder="例如：查看文章、编辑文章"
                required
              />
            </div>
            <!-- 权限描述输入 -->
            <div class="form-group">
              <label>权限描述</label>
              <textarea 
                v-model="permissionForm.description" 
                class="form-control"
                rows="3"
                placeholder="描述此权限的具体作用和适用范围"
              ></textarea>
            </div>
          </form>
        </div>
        <!-- 对话框操作按钮 -->
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

    <!-- 删除确认对话框 -->
    <div v-if="showDeleteDialog" class="modal-overlay" @click="closeDeleteDialog">
      <div class="modal-content modal-sm" @click.stop>
        <!-- 确认对话框标题 -->
        <div class="modal-header">
          <h2>确认删除</h2>
        </div>
        <!-- 确认对话框内容 -->
        <div class="modal-body">
          <p>确定要删除权限 "{{ deletingPermission?.permissionName }}" 吗？</p>
          <p class="warning-text">此操作不可撤销，且可能影响已分配此权限的角色。</p>
        </div>
        <!-- 确认对话框操作按钮 -->
        <div class="modal-footer">
          <button type="button" class="btn btn-ghost" @click="closeDeleteDialog">
            取消
          </button>
          <button type="button" class="btn btn-danger" @click="deletePermission">
            删除
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Vue 3 组合式 API 导入
import { ref, reactive, onMounted } from 'vue'
// 管理员 API 接口
import { adminApi } from '@/api/user'
// 权限相关类型定义
import type { Permission, PermissionForm } from '@/types/permission'

/**
 * 响应式数据定义
 */
const loading = ref(false)                    // 加载状态
const permissions = ref<Permission[]>([])     // 权限列表
const errorMessage = ref<string>('')          // 错误信息

/**
 * 对话框状态控制
 */
const showPermissionDialog = ref(false)       // 权限编辑对话框显示状态
const showDeleteDialog = ref(false)           // 删除确认对话框显示状态
const editingPermission = ref<Permission | null>(null)   // 当前编辑的权限
const deletingPermission = ref<Permission | null>(null)  // 待删除的权限

/**
 * 权限表单数据
 * 用于新建和编辑权限
 */
const permissionForm = reactive<PermissionForm>({
  permissionName: '',
  description: ''
})

/**
 * 组件挂载时加载权限列表
 */
onMounted(() => {
  loadPermissions()
})

/**
 * 加载权限列表
 * 从后端获取所有权限数据并处理错误状态
 */
async function loadPermissions() {
  loading.value = true
  errorMessage.value = ''
  try {
    console.log('开始加载权限列表...')
    permissions.value = await adminApi.getAllPermissions()
    console.log('权限列表加载成功:', permissions.value)
  } catch (error: any) {
    console.error('加载权限列表失败:', error)
    
    // 根据不同错误类型设置友好的错误提示
    if (error.response) {
      if (error.response.status === 500) {
        errorMessage.value = '服务器内部错误，可能是数据库连接问题'
      } else if (error.response.status === 404) {
        errorMessage.value = 'API接口不存在'
      } else if (error.response.status === 403) {
        errorMessage.value = '权限不足，无法访问权限管理'
      } else {
        errorMessage.value = `请求失败: ${error.response.status} ${error.response.statusText}`
      }
    } else if (error.request) {
      errorMessage.value = '网络错误，无法连接到服务器'
    } else {
      errorMessage.value = `请求配置错误: ${error.message}`
    }
  } finally {
    loading.value = false
  }
}

/**
 * 重新加载权限列表
 * 用于错误状态下的重试操作
 */
async function retryLoad() {
  await loadPermissions()
}

/**
 * 刷新权限数据
 * 用于手动刷新权限列表
 */
async function refreshData() {
  await loadPermissions()
}

/**
 * 打开新建权限对话框
 * 重置表单数据并显示对话框
 */
function openCreatePermissionDialog() {
  editingPermission.value = null
  resetPermissionForm()
  showPermissionDialog.value = true
}

/**
 * 打开编辑权限对话框
 * 将权限数据填入表单并显示对话框
 * @param {Permission} permission 要编辑的权限对象
 */
function openEditPermissionDialog(permission: Permission) {
  editingPermission.value = permission
  Object.assign(permissionForm, {
    permissionName: permission.permissionName,
    description: permission.description || ''
  })
  showPermissionDialog.value = true
}

/**
 * 关闭权限编辑对话框
 * 清理状态并重置表单
 */
function closePermissionDialog() {
  showPermissionDialog.value = false
  editingPermission.value = null
  resetPermissionForm()
}

/**
 * 重置权限表单数据
 * 清空所有表单字段
 */
function resetPermissionForm() {
  Object.assign(permissionForm, {
    permissionName: '',
    description: ''
  })
}

/**
 * 保存权限
 * 根据编辑状态执行创建或更新操作
 */
async function savePermission() {
  try {
    if (editingPermission.value) {
      // 更新现有权限
      await adminApi.updatePermission(editingPermission.value.permissionId, permissionForm)
    } else {
      // 创建新权限
      await adminApi.createPermission(permissionForm)
    }
    closePermissionDialog()
    await loadPermissions()
  } catch (error) {
    console.error('保存权限失败:', error)
  }
}

/**
 * 确认删除权限
 * 显示删除确认对话框
 * @param {Permission} permission 要删除的权限对象
 */
function confirmDeletePermission(permission: Permission) {
  deletingPermission.value = permission
  showDeleteDialog.value = true
}

/**
 * 关闭删除确认对话框
 * 清理待删除权限状态
 */
function closeDeleteDialog() {
  showDeleteDialog.value = false
  deletingPermission.value = null
}

/**
 * 删除权限
 * 执行权限删除操作并刷新列表
 */
async function deletePermission() {
  if (!deletingPermission.value) return
  
  try {
    await adminApi.deletePermission(deletingPermission.value.permissionId)
    closeDeleteDialog()
    await loadPermissions()
  } catch (error) {
    console.error('删除权限失败:', error)
  }
}

/**
 * 格式化时间显示
 * 将日期字符串转换为本地化的时间格式
 * @param {string} dateString 日期字符串
 * @returns {string} 格式化后的时间字符串
 */
function formatTime(dateString: string): string {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 暴露方法给父组件调用
defineExpose({
  refreshData
})
</script>

<style scoped>
/* 权限管理主容器样式 */
.permission-management {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

/* 页面头部样式 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  background: white;
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

/* 头部内容区域样式 */
.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 头部图标样式 */
.header-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.header-icon .icon {
  width: 24px;
  height: 24px;
  fill: currentColor;
}

/* 头部文字样式 */
.header-text h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.header-text p {
  margin: 4px 0 0 0;
  color: #6b7280;
  font-size: 14px;
}

/* 头部操作按钮区域样式 */
.header-actions {
  display: flex;
  gap: 12px;
}

/* 权限列表容器样式 */
.permissions-container {
  flex: 1;
  padding: 24px 32px;
  overflow: auto;
}

/* 加载、错误和空状态样式 */
.loading-state, .error-state, .empty-state {
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

/* 加载动画旋转器样式 */
.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e5e7eb;
  border-top: 3px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

/* 旋转动画关键帧定义 */
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 空状态和错误状态图标样式 */
.empty-icon, .error-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 16px;
  color: #9ca3af;
}

.error-icon {
  color: #ef4444;
}

/* 权限列表样式 */
.permissions-list {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

/* 列表标题行样式 */
.list-header {
  display: grid;
  grid-template-columns: 1fr 2fr 1fr 120px;
  gap: 16px;
  padding: 16px 24px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  font-weight: 600;
  color: #374151;
  font-size: 14px;
}

/* 权限数据行样式 */
.permission-row {
  display: grid;
  grid-template-columns: 1fr 2fr 1fr 120px;
  gap: 16px;
  padding: 20px 24px;
  border-bottom: 1px solid #f3f4f6;
  transition: background-color 0.2s;
}

.permission-row:hover {
  background: #f9fafb;
}

.permission-row:last-child {
  border-bottom: none;
}

/* 单元格基础样式 */
.cell {
  display: flex;
  align-items: center;
}

/* 权限名称样式 */
.permission-info h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

/* 权限描述样式 */
.permission-description p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.no-description {
  color: #9ca3af;
  font-style: italic;
  font-size: 14px;
}

/* 时间显示样式 */
.time-text {
  color: #6b7280;
  font-size: 14px;
}

/* 操作按钮区域样式 */
.permission-actions {
  gap: 8px;
}

/* 按钮基础样式 */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  text-decoration: none;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 主要按钮样式 */
.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
}

/* 透明按钮样式 */
.btn-ghost {
  background: transparent;
  color: #6b7280;
  border: 1px solid #e5e7eb;
}

.btn-ghost:hover:not(:disabled) {
  background: #f9fafb;
  color: #374151;
}

/* 危险按钮样式 */
.btn-danger {
  background: #ef4444;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background: #dc2626;
}

/* 刷新按钮样式 */
.btn-refresh {
  background: transparent;
  color: #6b7280;
  border: 1px solid #e5e7eb;
  padding: 8px;
}

.btn-refresh:hover {
  background: #f9fafb;
  color: #374151;
}

/* 小尺寸按钮样式 */
.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

/* 按钮图标样式 */
.btn .icon {
  width: 16px;
  height: 16px;
  fill: currentColor;
}

/* 模态对话框遮罩层样式 */
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

/* 模态对话框内容样式 */
.modal-content {
  background: white;
  border-radius: 8px;
  min-width: 480px;
  max-width: 90vw;
  max-height: 90vh;
  overflow: auto;
}

/* 小尺寸模态对话框样式 */
.modal-sm {
  min-width: 400px;
}

/* 模态对话框标题栏样式 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 24px 0 24px;
}

.modal-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

/* 模态对话框内容区域样式 */
.modal-body {
  padding: 24px;
}

/* 模态对话框底部按钮区域样式 */
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 0 24px 24px 24px;
}

/* 表单组样式 */
.form-group {
  margin-bottom: 20px;
}

/* 表单标签样式 */
.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

/* 表单控件样式 */
.form-control {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-control:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 警告文本样式 */
.warning-text {
  color: #ef4444;
  font-size: 14px;
}
</style>
