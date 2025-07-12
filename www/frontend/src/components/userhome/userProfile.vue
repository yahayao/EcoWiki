<template>
  <div class="profile-wrapper">
    <!-- 页面标题 -->
    <header class="profile-header">
      <h1>个人中心</h1>
      <button @click="$emit('close')" class="close-btn">×</button>
    </header>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>正在加载用户信息...</p>
    </div>

    <!-- 主要内容区 -->
    <main v-else class="profile-main">
      <!-- 左侧资料卡片 -->
      <section class="profile-card">
        <div class="avatar-box">
          <img :src="userAvatar" alt="用户头像" class="avatar" />
          <button class="upload-btn" @click="triggerUpload">更换头像</button>
          <input
            ref="avatarInput"
            type="file"
            accept="image/*"
            style="display: none"
            @change="handleAvatarChange"
          />
        </div>

        <div class="info-box">
          <h2>{{ user?.username || '未知用户' }}</h2>
          <p class="bio">{{ user?.fullName || '这个人很酷，什么也没留下。' }}</p>
          <ul class="meta">
            <li><strong>邮箱：</strong>{{ user?.email || '未设置' }}</li>
            <li><strong>用户组：</strong>{{ user?.userGroup || '普通用户' }}</li>
            <li><strong>注册时间：</strong>{{ formatDate(user?.createdAt) }}</li>
            <li><strong>最后更新：</strong>{{ formatDate(user?.updatedAt) }}</li>
            <li><strong>账户状态：</strong>
              <span :class="user?.active ? 'status-active' : 'status-inactive'">
                {{ user?.active ? '活跃' : '已停用' }}
              </span>
            </li>
          </ul>
        </div>
      </section>

      <!-- 右侧操作区 -->
      <aside class="action-panel">
        <h3>快捷操作</h3>
        <button class="btn primary" @click="editMode = !editMode">
          {{ editMode ? '取消修改' : '编辑资料' }}
        </button>
        <button class="btn secondary" @click="$emit('logout')">安全退出</button>
        <button class="btn secondary" @click="refreshProfile" :disabled="refreshing">
          {{ refreshing ? '刷新中...' : '刷新资料' }}
        </button>
      </aside>
    </main>

    <!-- 资料编辑弹窗 -->
    <transition name="fade">
      <div v-if="editMode" class="edit-modal" @click.self="editMode = false">
        <div class="edit-box">
          <h3>编辑资料</h3>
          <div class="edit-form">
            <label>
              用户名
              <input 
                v-model="draft.username" 
                type="text" 
                maxlength="20" 
                :disabled="true"
                placeholder="用户名不可修改" 
              />
            </label>
            <label>
              显示名称
              <input 
                v-model="draft.fullName" 
                type="text" 
                maxlength="50" 
                placeholder="请输入显示名称" 
              />
            </label>
            <label>
              邮箱
              <input 
                v-model="draft.email" 
                type="email" 
                maxlength="100" 
                :disabled="true"
                placeholder="邮箱不可修改" 
              />
            </label>
          </div>
          <div class="edit-actions">
            <button class="btn primary" @click="saveProfile" :disabled="saving">
              {{ saving ? '保存中...' : '保存' }}
            </button>
            <button class="btn secondary" @click="cancelEdit">取消</button>
          </div>
          
          <!-- 错误提示 -->
          <div v-if="error" class="error-message">
            {{ error }}
          </div>
          
          <!-- 成功提示 -->
          <div v-if="success" class="success-message">
            {{ success }}
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuth } from '../../composables/useAuth'
import { userApi } from '../../api/user'
import type { UserResponse } from '../../api/user'

/* ===== 事件声明 ===== */
const emit = defineEmits<{
  close: []
  logout: []
}>()

/* ===== 认证状态管理 ===== */
const { user, userAvatar, setUser, token } = useAuth()

/* ===== 响应式状态 ===== */
const refreshing = ref(false)
const editMode = ref(false)
const saving = ref(false)
const error = ref('')
const success = ref('')
const loading = ref(false)

/* ===== 编辑草稿数据 ===== */
const draft = ref({
  username: '',
  fullName: '',
  email: ''
})

/* ===== 工具函数 ===== */
function formatDate(dateString?: string) {
  if (!dateString) return '无'
  try {
    return new Date(dateString).toLocaleString('zh-CN')
  } catch {
    return '无效日期'
  }
}

/* ===== 头像上传 ===== */
const avatarInput = ref<HTMLInputElement>()
function triggerUpload() {
  avatarInput.value?.click()
}

async function handleAvatarChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  
  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    error.value = '请选择图片文件'
    return
  }
  
  // 检查文件大小（限制2MB）
  if (file.size > 2 * 1024 * 1024) {
    error.value = '图片大小不能超过2MB'
    return
  }
  
  try {
    // TODO: 实现真实的头像上传API
    // const formData = new FormData()
    // formData.append('avatar', file)
    // const response = await userApi.uploadAvatar(formData)
    
    // 临时使用本地预览
    const previewUrl = URL.createObjectURL(file)
    console.log('头像上传功能待实现，当前仅预览:', previewUrl)
    
    success.value = '头像上传功能开发中...'
    setTimeout(() => { success.value = '' }, 3000)
  } catch (err: any) {
    error.value = err.message || '头像上传失败'
  }
}

/* ===== 编辑资料 ===== */
function startEdit() {
  if (!user.value) return
  
  draft.value = {
    username: user.value.username || '',
    fullName: user.value.fullName || '',
    email: user.value.email || ''
  }
  editMode.value = true
  error.value = ''
  success.value = ''
}

async function saveProfile() {
  if (!user.value) return
  
  saving.value = true
  error.value = ''
  success.value = ''
  
  try {
    // TODO: 实现真实的用户信息更新API
    // const updatedUser = await userApi.updateProfile({
    //   fullName: draft.value.fullName
    // })
    
    // 临时模拟更新
    const updatedUser: UserResponse = {
      ...user.value,
      fullName: draft.value.fullName,
      updatedAt: new Date().toISOString()
    }
    
    // 更新本地用户状态
    if (token.value) {
      setUser(updatedUser, token.value)
    }
    
    success.value = '资料更新成功'
    setTimeout(() => {
      editMode.value = false
      success.value = ''
    }, 1500)
    
  } catch (err: any) {
    error.value = err.message || '保存失败，请重试'
  } finally {
    saving.value = false
  }
}

function cancelEdit() {
  editMode.value = false
  error.value = ''
  success.value = ''
}

/* ===== 刷新用户资料 ===== */
async function refreshProfile() {
  if (!token.value) {
    error.value = '未登录，无法刷新用户信息'
    return
  }
  
  refreshing.value = true
  error.value = ''
  
  try {
    console.log('开始刷新用户资料...')
    
    // 调用真实的API获取最新用户信息
    const freshUser = await userApi.getCurrentUser()
    
    console.log('获取到最新用户信息:', freshUser)
    
    // 更新本地用户状态
    setUser(freshUser, token.value)
    
    success.value = '用户资料刷新成功'
    setTimeout(() => { success.value = '' }, 2000)
    
  } catch (err: any) {
    console.error('刷新用户资料失败:', err)
    error.value = err.message || '刷新失败，请重试'
    setTimeout(() => { error.value = '' }, 5000)
  } finally {
    refreshing.value = false
  }
}

/* ===== 初始化加载用户资料 ===== */
async function loadUserProfile() {
  if (!token.value) {
    console.log('未登录，跳过加载用户资料')
    return
  }
  
  loading.value = true
  error.value = ''
  
  try {
    console.log('初始化加载用户资料...')
    
    // 调用真实的API获取最新用户信息
    const freshUser = await userApi.getCurrentUser()
    
    console.log('初始化获取用户信息:', freshUser)
    
    // 更新本地用户状态
    setUser(freshUser, token.value)
    
  } catch (err: any) {
    console.error('加载用户资料失败:', err)
    // 如果获取失败，仍然使用本地缓存的用户信息
    console.log('使用本地缓存的用户信息')
  } finally {
    loading.value = false
  }
}

/* ===== 生命周期 ===== */
onMounted(async () => {
  // 加载最新的用户资料
  await loadUserProfile()
  
  // 初始化编辑草稿
  if (user.value) {
    startEdit()
    editMode.value = false  // 默认不开启编辑模式
  }
})
</script>

<style scoped>
/* ===== 布局 ===== */
.profile-wrapper {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 16px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: #333;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e2e8f0;
}

.profile-header h1 {
  margin: 0;
  color: #1a202c;
  font-size: 28px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #64748b;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #f1f5f9;
  color: #1a202c;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f4f6;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-container p {
  color: #6b7280;
  font-size: 16px;
  margin: 0;
}

.profile-main {
  display: flex;
  flex-wrap: wrap;
  gap: 32px;
}

/* ===== 卡片 ===== */
.profile-card {
  flex: 1 1 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  padding: 32px 24px;
  border: 1px solid #e2e8f0;
}

.avatar-box {
  position: relative;
  margin-bottom: 24px;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #f7fafc;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.upload-btn {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: #3b82f6;
  color: #fff;
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.upload-btn:hover {
  background: #2563eb;
  transform: scale(1.05);
}

.info-box {
  text-align: center;
  width: 100%;
}

.info-box h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1a202c;
}

.bio {
  color: #64748b;
  margin: 0 0 24px 0;
  font-size: 16px;
  line-height: 1.5;
}

.meta {
  list-style: none;
  padding: 0;
  margin: 0;
  font-size: 14px;
  color: #4b5563;
  text-align: left;
}

.meta li {
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
  display: flex;
  justify-content: space-between;
}

.meta li:last-child {
  border-bottom: none;
}

.meta strong {
  color: #374151;
  font-weight: 600;
}

.status-active {
  color: #10b981;
  font-weight: 600;
}

.status-inactive {
  color: #ef4444;
  font-weight: 600;
}

/* ===== 侧边栏 ===== */
.action-panel {
  flex: 1 1 200px;
  background: #f8fafc;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e2e8f0;
  height: fit-content;
}

.action-panel h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a202c;
}

.btn {
  display: block;
  width: 100%;
  padding: 12px 16px;
  margin-bottom: 12px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.primary {
  background: #3b82f6;
  color: #fff;
}

.primary:hover:not(:disabled) {
  background: #2563eb;
  transform: translateY(-1px);
}

.secondary {
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #cbd5e1;
}

.secondary:hover:not(:disabled) {
  background: #e2e8f0;
  color: #334155;
}

/* ===== 编辑弹窗 ===== */
.edit-modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.edit-box {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.edit-box h3 {
  margin: 0 0 24px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1a202c;
}

.edit-form {
  margin-bottom: 24px;
}

.edit-box label {
  display: block;
  margin-bottom: 16px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.edit-box input,
.edit-box textarea {
  width: 100%;
  margin-top: 4px;
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.edit-box input:focus,
.edit-box textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.edit-box input:disabled {
  background-color: #f9fafb;
  color: #6b7280;
  cursor: not-allowed;
}

.edit-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.error-message,
.success-message {
  padding: 12px;
  border-radius: 6px;
  font-size: 14px;
  margin-top: 16px;
}

.error-message {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.success-message {
  background: #f0fdf4;
  color: #16a34a;
  border: 1px solid #bbf7d0;
}

/* ===== 动画 ===== */
.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

/* ===== 响应式设计 ===== */
@media (max-width: 768px) {
  .profile-wrapper {
    margin: 20px auto;
    padding: 0 12px;
  }

  .profile-header {
    margin-bottom: 24px;
  }

  .profile-header h1 {
    font-size: 24px;
  }

  .profile-main {
    flex-direction: column;
    gap: 20px;
  }

  .profile-card,
  .action-panel {
    flex: 1 1 auto;
  }

  .edit-box {
    padding: 24px;
    margin: 20px;
  }

  .meta li {
    flex-direction: column;
    gap: 4px;
    align-items: flex-start;
  }
}

@media (max-width: 480px) {
  .avatar {
    width: 100px;
    height: 100px;
  }

  .upload-btn {
    width: 32px;
    height: 32px;
    font-size: 10px;
  }

  .profile-card {
    padding: 24px 16px;
  }

  .action-panel {
    padding: 20px 16px;
  }
}
</style>