<!-- UserInformation.vue -->
<template>
  <div class="user-info-container">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <svg viewBox="0 0 24 24" class="icon">
            <path d="M12,4A4,4 0 0,1 16,8A4,4 0 0,1 12,12A4,4 0 0,1 8,8A4,4 0 0,1 12,4M12,14C16.42,14 20,15.79 20,18V20H4V18C4,15.79 7.58,14 12,14Z" />
          </svg>
        </div>
        <div class="header-text">
          <h1 class="page-title">用户信息</h1>
          <p class="page-subtitle">管理您的个人资料和设置</p>
        </div>
      </div>
    </div>

    <!-- 用户概览卡片 -->
    <div class="user-overview-card">
      <div class="user-profile-section">
        <!-- 使用新的头像上传组件 -->
        <AvatarUpload 
          :username="user?.username"
          :current-avatar-url="user?.avatarUrl"
          @upload-success="handleAvatarUploadSuccess"
          @upload-error="handleAvatarUploadError"
        />
        
        <div class="user-quick-info">
          <h2 class="user-name">{{ user?.username || '未知用户' }}</h2>
          <div class="user-status">
            <span class="status-badge" :class="user?.active ? 'status-active' : 'status-inactive'">
              <div class="status-dot"></div>
              {{ user?.active ? '账号正常' : '账号已停用' }}
            </span>
          </div>
          <div class="user-meta">
            <div class="meta-item">
              <svg viewBox="0 0 24 24" class="meta-icon">
                <path d="M12,15C12.81,15 13.5,14.7 14.11,14.11C14.7,13.5 15,12.81 15,12C15,11.19 14.7,10.5 14.11,9.89C13.5,9.3 12.81,9 12,9C11.19,9 10.5,9.3 9.89,9.89C9.3,10.5 9,11.19 9,12C9,12.81 9.3,13.5 9.89,14.11C10.5,14.7 11.19,15 12,15M12,2C14.21,2 16.21,2.81 17.78,4.39C19.36,5.96 20.17,7.96 20.17,10.17C20.17,12.38 19.36,14.38 17.78,15.95L12,21.5L6.22,15.95C4.64,14.38 3.83,12.38 3.83,10.17C3.83,7.96 4.64,5.96 6.22,4.39C7.79,2.81 9.79,2 12,2Z"/>
              </svg>
              <span>{{ user?.userGroup || '普通用户' }}</span>
            </div>
            <div class="meta-item">
              <svg viewBox="0 0 24 24" class="meta-icon">
                <path d="M9,10V12H7V10H9M13,10V12H11V10H13M17,10V12H15V10H17M19,3A2,2 0 0,1 21,5V19A2,2 0 0,1 19,21H5C3.89,21 3,20.1 3,19V5A2,2 0 0,1 5,3H6V1H8V3H16V1H18V3H19M19,19V8H5V19H19M19,5H5V6H19V5Z"/>
              </svg>
              <span>{{ formatDate(user?.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 详细信息卡片 -->
    <div class="info-cards-grid">
      <!-- 基本信息卡片 -->
      <div class="info-card">
        <div class="card-header">
          <div class="card-title">
            <svg viewBox="0 0 24 24" class="card-icon">
              <path d="M12,4A4,4 0 0,1 16,8A4,4 0 0,1 12,12A4,4 0 0,1 8,8A4,4 0 0,1 12,4M12,14C16.42,14 20,15.79 20,18V20H4V18C4,15.79 7.58,14 12,14Z" />
            </svg>
            <h3>基本信息</h3>
          </div>
        </div>
        
        <div class="card-content">
          <div class="info-item">
            <label class="info-label">用户名</label>
            <div class="info-value-container">
              <div v-if="!isEditing || editType !== 'username'" class="info-value">{{ user?.username || '未知用户' }}</div>
              <div v-else class="edit-container">
                <input
                  v-model="draft.username"
                  type="text"
                  class="edit-input"
                  placeholder="请输入用户名"
                  @keyup.enter="saveEdit"
                  @keyup.escape="cancelEdit"
                />
                <div class="edit-actions">
                  <button class="save-btn" @click="saveEdit">
                    <svg viewBox="0 0 24 24" class="icon">
                      <path d="M21,7L9,19L3.5,13.5L4.91,12.09L9,16.17L19.59,5.59L21,7Z"/>
                    </svg>
                  </button>
                  <button class="cancel-btn" @click="cancelEdit">
                    <svg viewBox="0 0 24 24" class="icon">
                      <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z"/>
                    </svg>
                  </button>
                </div>
              </div>
              <button v-if="!isEditing || editType !== 'username'" class="edit-btn" @click="editUsername">
                <svg viewBox="0 0 24 24" class="icon">
                  <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z"/>
                </svg>
              </button>
            </div>
          </div>

          <div class="info-item">
            <label class="info-label">全名</label>
            <div class="info-value-container">
              <div v-if="!isEditing || editType !== 'fullName'" class="info-value">{{ user?.fullName || '未设置' }}</div>
              <div v-else class="edit-container">
                <input
                  v-model="draft.fullName"
                  type="text"
                  class="edit-input"
                  placeholder="请输入全名"
                  @keyup.enter="saveEdit"
                  @keyup.escape="cancelEdit"
                />
                <div class="edit-actions">
                  <button class="save-btn" @click="saveEdit">
                    <svg viewBox="0 0 24 24" class="icon">
                      <path d="M21,7L9,19L3.5,13.5L4.91,12.09L9,16.17L19.59,5.59L21,7Z"/>
                    </svg>
                  </button>
                  <button class="cancel-btn" @click="cancelEdit">
                    <svg viewBox="0 0 24 24" class="icon">
                      <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z"/>
                    </svg>
                  </button>
                </div>
              </div>
              <button v-if="!isEditing || editType !== 'fullName'" class="edit-btn" @click="editFullName">
                <svg viewBox="0 0 24 24" class="icon">
                  <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z"/>
                </svg>
              </button>
            </div>
          </div>

          <div class="info-item">
            <label class="info-label">电子邮箱</label>
            <div class="info-value-container">
              <div v-if="!isEditing || editType !== 'email'" class="info-value">{{ user?.email || '未设置' }}</div>
              <div v-else class="edit-container">
                <input
                  v-model="draft.email"
                  type="email"
                  class="edit-input"
                  placeholder="请输入邮箱"
                  @keyup.enter="saveEdit"
                  @keyup.escape="cancelEdit"
                />
                <div class="edit-actions">
                  <button class="save-btn" @click="saveEdit">
                    <svg viewBox="0 0 24 24" class="icon">
                      <path d="M21,7L9,19L3.5,13.5L4.91,12.09L9,16.17L19.59,5.59L21,7Z"/>
                    </svg>
                  </button>
                  <button class="cancel-btn" @click="cancelEdit">
                    <svg viewBox="0 0 24 24" class="icon">
                      <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z"/>
                    </svg>
                  </button>
                </div>
              </div>
              <div v-if="!isEditing || editType !== 'email'" class="info-value-right">
                <span class="verification-badge verified">
                  <svg viewBox="0 0 24 24" class="icon">
                    <path d="M21,7L9,19L3.5,13.5L4.91,12.09L9,16.17L19.59,5.59L21,7Z"/>
                  </svg>
                  已验证
                </span>
                <button class="edit-btn" @click="editEmail">
                  <svg viewBox="0 0 24 24" class="icon">
                    <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>

          <div class="info-item">
            <label class="info-label">注册时间</label>
            <div class="info-value">{{ formatDate(user?.createdAt) }}</div>
          </div>

          <div class="info-item">
            <label class="info-label">登录密码</label>
            <div class="info-value-container">
              <div class="info-value security-hidden">••••••••••••</div>
              <button class="edit-btn" @click="openPasswordForm">
                <svg viewBox="0 0 24 24" class="icon">
                  <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z"/>
                </svg>
              </button>
            </div>
          </div>

          <div class="info-item">
            <label class="info-label">账号权限</label>
            <div class="info-value">
              <span class="role-badge">{{ user?.userGroup || '普通用户' }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 安全设置卡片 -->
      <div class="info-card">
        <div class="card-header">
          <div class="card-title">
            <svg viewBox="0 0 24 24" class="card-icon">
              <path d="M12,1L3,5V11C3,16.55 6.84,21.74 12,23C17.16,21.74 21,16.55 21,11V5L12,1M12,7C13.4,7 14.8,8.6 14.8,10V11H16V16H8V11H9.2V10C9.2,8.6 10.6,7 12,7M12,8.2C11.2,8.2 10.4,8.7 10.4,10V11H13.6V10C13.6,8.7 12.8,8.2 12,8.2Z" />
            </svg>
            <h3>安全验证设置</h3>
          </div>
        </div>

        <div class="card-content">
          <div class="info-item">
            <label class="info-label">安全验证问题</label>
            <div class="info-description">用于忘记密码时验证身份</div>
            <div class="info-value-container">
              <div class="info-value security-hidden">••••••••••••••••••••</div>
              <button class="edit-btn" @click="editSecurityQuestion">
                <svg viewBox="0 0 24 24" class="icon">
                  <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z"/>
                </svg>
              </button>
            </div>
          </div>
          
          <div class="info-item">
            <label class="info-label">安全验证答案</label>
            <div class="info-value-container">
              <div class="info-value security-hidden">••••••••••••</div>
              <button class="edit-btn" @click="editSecurityAnswer">
                <svg viewBox="0 0 24 24" class="icon">
                  <path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z"/>
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 密码修改模态框 -->
    <div v-if="showPasswordForm" class="modal-overlay" @click="closePasswordForm">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">修改密码</h3>
          <button class="modal-close" @click="closePasswordForm">
            <svg viewBox="0 0 24 24" class="icon">
              <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z"/>
            </svg>
          </button>
        </div>
        
        <form @submit.prevent="changePassword" class="password-form">
          <div class="form-group">
            <label class="form-label">当前密码</label>
            <input
              v-model="passwordForm.currentPassword"
              type="password"
              class="form-input"
              placeholder="请输入当前密码"
              required
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">新密码</label>
            <input
              v-model="passwordForm.newPassword"
              type="password"
              class="form-input"
              placeholder="请输入新密码（至少6位）"
              minlength="6"
              required
            />
          </div>
          
          <div class="form-group">
            <label class="form-label">确认新密码</label>
            <input
              v-model="passwordForm.confirmPassword"
              type="password"
              class="form-input"
              placeholder="请再次输入新密码"
              minlength="6"
              required
            />
          </div>
          
          <div class="form-actions">
            <button type="button" class="btn btn-secondary" @click="closePasswordForm">
              取消
            </button>
            <button type="submit" class="btn btn-primary">
              确认修改
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuth } from '@/composables/useAuth'
import { userApi } from '@/api/user'
import type { UserResponse } from '@/api/user'
import { ref, computed, onMounted } from 'vue'
import toast from '@/utils/toast'
import AvatarUpload from '@/components/common/AvatarUpload.vue'

function formatDate(dateString?: string) {
  if (!dateString) return '无'
  try {
    return new Date(dateString).toLocaleString('zh-CN')
  } catch {
    return '无效日期'
  }
}

const { user, userAvatar, setUser, refreshUserInfo } = useAuth()

// 编辑状态管理
const isEditing = ref(false)
const editType = ref<'username' | 'email' | 'fullName' | 'security' | ''>('')

const draft = ref({
  username: '',
  fullName: '',
  email: '',
  securityQuestion: '',
  securityAnswer: ''
})

// 密码修改相关
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const showPasswordForm = ref(false)

function startEdit() {
  if (!user.value) return
  
  draft.value = {
    username: user.value.username || '',
    fullName: user.value.fullName || '',
    email: user.value.email || '',
    securityQuestion: '',
    securityAnswer: ''
  }
}

// 头像上传成功处理
const handleAvatarUploadSuccess = async (result: {
  avatarUrl: string
  fullUrl: string
  fileName: string
  uploadTime: string
}) => {
  toast.show('头像更新成功！', '成功', { type: 'success' })
  console.log('头像上传成功:', result)
  
  // 刷新用户信息以获取最新的头像URL
  await refreshUserInfo()
}

// 头像上传失败处理
const handleAvatarUploadError = (error: string) => {
  toast.show(`头像更新失败: ${error}`, '错误', { type: 'error' })
  console.error('头像上传失败:', error)
}

// 编辑用户名
const editUsername = () => {
  if (!user.value) return
  editType.value = 'username'
  draft.value.username = user.value.username || ''
  isEditing.value = true
}

// 编辑全名
const editFullName = () => {
  if (!user.value) return
  editType.value = 'fullName'
  draft.value.fullName = user.value.fullName || ''
  isEditing.value = true
}

// 编辑邮箱
const editEmail = () => {
  if (!user.value) return
  editType.value = 'email'
  draft.value.email = user.value.email || ''
  isEditing.value = true
}

// 保存编辑
const saveEdit = async () => {
  if (!editType.value || editType.value === 'security') return
  
  try {
    const updateData: any = {}
    if (editType.value === 'username') {
      updateData.username = draft.value.username
    } else if (editType.value === 'fullName') {
      updateData.fullName = draft.value.fullName
    } else if (editType.value === 'email') {
      updateData.email = draft.value.email
    }
    
    toast.show('正在保存...', '提示', { type: 'info' })
    const updatedUser = await userApi.updateProfile(updateData)
    
    // 更新本地用户信息
    setUser(updatedUser, localStorage.getItem('token') || '', localStorage.getItem('refreshToken') || '')
    
    isEditing.value = false
    editType.value = ''
    
    toast.show('信息更新成功', '成功', { type: 'success' })
  } catch (error: any) {
    toast.show(error.message || '更新失败', '错误', { type: 'error' })
  }
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  editType.value = ''
}

// 安全设置相关
const editSecurityQuestion = () => {
  editType.value = 'security'
  isEditing.value = true
}

const editSecurityAnswer = () => {
  editType.value = 'security'
  isEditing.value = true
}

const saveSecuritySettings = async () => {
  try {
    toast.show('正在保存安全设置...', '提示', { type: 'info' })
    await userApi.updateSecuritySettings({
      securityQuestion: draft.value.securityQuestion,
      securityAnswer: draft.value.securityAnswer
    })
    
    isEditing.value = false
    editType.value = ''
    
    toast.show('安全设置更新成功', '成功', { type: 'success' })
  } catch (error: any) {
    toast.show(error.message || '安全设置更新失败', '错误', { type: 'error' })
  }
}

// 密码修改
const openPasswordForm = () => {
  showPasswordForm.value = true
  passwordForm.value = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
}

const closePasswordForm = () => {
  showPasswordForm.value = false
}

const changePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    toast.show('新密码与确认密码不匹配', '错误', { type: 'error' })
    return
  }
  
  if (passwordForm.value.newPassword.length < 6) {
    toast.show('新密码长度至少6位', '错误', { type: 'error' })
    return
  }
  
  try {
    toast.show('正在修改密码...', '提示', { type: 'info' })
    await userApi.changePassword(passwordForm.value)
    
    showPasswordForm.value = false
    toast.show('密码修改成功', '成功', { type: 'success' })
  } catch (error: any) {
    toast.show(error.message || '密码修改失败', '错误', { type: 'error' })
  }
}

// 初始化数据
onMounted(() => {
  startEdit()
})
</script>

<style scoped>
/* 主容器样式 */
.user-info-container {
  padding: 24px;
  background: #f8fafb;
  min-height: 100vh;
}

/* 页面标题区域 */
.page-header {
  margin-bottom: 32px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.header-icon .icon {
  width: 24px;
  height: 24px;
  fill: white;
}

.header-text {
  flex: 1;
}

.page-title {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
  letter-spacing: -0.5px;
}

.page-subtitle {
  margin: 4px 0 0 0;
  color: #718096;
  font-size: 16px;
  font-weight: 400;
}

/* 用户概览卡片 */
.user-overview-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
}

.user-profile-section {
  display: flex;
  align-items: center;
  gap: 32px;
}

/* 头像区域 */
.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
}

.user-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid white;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  transition: all 0.3s ease;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-edit-btn {
  background: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.avatar-edit-btn:hover {
  transform: scale(1.1);
}

.avatar-edit-btn .icon {
  width: 20px;
  height: 20px;
  fill: #667eea;
}

.change-avatar-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.change-avatar-btn:hover {
  background: #5a67d8;
  transform: translateY(-1px);
}

.change-avatar-btn .icon {
  width: 16px;
  height: 16px;
  fill: currentColor;
}

/* 用户快速信息 */
.user-quick-info {
  flex: 1;
}

.user-name {
  margin: 0 0 12px 0;
  font-size: 28px;
  font-weight: 700;
  color: #1a202c;
  letter-spacing: -0.5px;
}

.user-status {
  margin-bottom: 16px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.status-active {
  background: #f0fff4;
  color: #22543d;
  border: 1px solid #9ae6b4;
}

.status-inactive {
  background: #fff5f5;
  color: #822727;
  border: 1px solid #feb2b2;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-active .status-dot {
  background: #48bb78;
}

.status-inactive .status-dot {
  background: #f56565;
}

.user-meta {
  display: flex;
  gap: 24px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #4a5568;
  font-size: 14px;
}

.meta-icon {
  width: 16px;
  height: 16px;
  fill: #718096;
}

/* 信息卡片网格 */
.info-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
}

/* 信息卡片 */
.info-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.card-header {
  background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-icon {
  width: 20px;
  height: 20px;
  fill: #667eea;
}

.card-title h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.card-content {
  padding: 24px;
}

/* 信息项 */
.info-item {
  margin-bottom: 24px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #4a5568;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-description {
  margin-bottom: 8px;
  font-size: 13px;
  color: #718096;
  line-height: 1.4;
}

.info-value-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.info-value {
  flex: 1;
  font-size: 16px;
  color: #2d3748;
  font-weight: 500;
  padding: 12px 0;
}

.security-hidden {
  font-family: monospace;
  letter-spacing: 2px;
  color: #a0aec0;
}

/* 编辑按钮 */
.edit-btn {
  background: #f7fafc;
  border: 1px solid #e2e8f0;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.edit-btn:hover {
  background: #667eea;
  border-color: #667eea;
  transform: scale(1.05);
}

.edit-btn:hover .icon {
  fill: white;
}

.edit-btn .icon {
  width: 16px;
  height: 16px;
  fill: #718096;
  transition: fill 0.2s ease;
}

/* 验证徽章 */
.verification-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.verification-badge.verified {
  background: #f0fff4;
  color: #22543d;
  border: 1px solid #9ae6b4;
}

.verification-badge .icon {
  width: 12px;
  height: 12px;
  fill: currentColor;
}

/* 角色徽章 */
.role-badge {
  display: inline-block;
  padding: 6px 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-info-container {
    padding: 16px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .user-overview-card {
    padding: 24px;
  }
  
  .user-profile-section {
    flex-direction: column;
    text-align: center;
    gap: 24px;
  }
  
  .user-meta {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .info-cards-grid {
    grid-template-columns: 1fr;
  }
  
  .card-content {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .user-avatar {
    width: 100px;
    height: 100px;
  }
  
  .user-name {
    font-size: 24px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .page-title {
    font-size: 20px;
  }
}

/* 编辑功能样式 */
.edit-container {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.edit-input {
  flex: 1;
  padding: 8px 12px;
  border: 2px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.edit-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.edit-actions {
  display: flex;
  gap: 4px;
}

.save-btn,
.cancel-btn {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.save-btn {
  background: #48bb78;
  color: white;
}

.save-btn:hover {
  background: #38a169;
}

.cancel-btn {
  background: #ed64a6;
  color: white;
}

.cancel-btn:hover {
  background: #d53f8c;
}

.save-btn .icon,
.cancel-btn .icon {
  width: 14px;
  height: 14px;
  fill: currentColor;
}

.info-value-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease-out;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  animation: slideIn 0.3s ease-out;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a202c;
}

.modal-close {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #718096;
  transition: all 0.2s;
}

.modal-close:hover {
  background: #f7fafc;
  color: #2d3748;
}

.modal-close .icon {
  width: 16px;
  height: 16px;
  fill: currentColor;
}

.password-form {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #2d3748;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary {
  background: #e2e8f0;
  color: #4a5568;
}

.btn-secondary:hover {
  background: #cbd5e0;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5a67d8;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>