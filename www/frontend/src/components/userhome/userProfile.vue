<template>
  <div class="profile-wrapper">
    <!-- 页面标题 -->
    <header class="profile-header">
      <h1>个人中心</h1>
    </header>

    <!-- 主要内容区 -->
    <main class="profile-main">
      <!-- 左侧资料卡片 -->
      <section class="profile-card">
        <div class="avatar-box">
          <img :src="user.avatar" alt="用户头像" class="avatar" />
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
          <h2>{{ user.username }}</h2>
          <p class="bio">{{ user.bio || '这个人很酷，什么也没留下。' }}</p>
          <ul class="meta">
            <li><strong>邮箱：</strong>{{ user.email }}</li>
            <li><strong>注册时间：</strong>{{ formatDate(user.createdAt) }}</li>
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
      </aside>
    </main>

    <!-- 资料编辑弹窗 -->
    <transition name="fade">
      <div v-if="editMode" class="edit-modal" @click.self="editMode = false">
        <div class="edit-box">
          <h3>编辑资料</h3>
          <label>
            昵称
            <input v-model="draft.username" type="text" maxlength="20" />
          </label>
          <label>
            简介
            <textarea v-model="draft.bio" rows="3" maxlength="140"></textarea>
          </label>
          <div class="edit-actions">
            <button class="btn primary" @click="saveProfile">保存</button>
            <button class="btn secondary" @click="cancelEdit">取消</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'

/* ===== 事件声明 ===== */
defineEmits<{
  logout: []
}>()

/* ===== 模拟数据 ===== */
const user = reactive({
  username: 'EcoUser',
  email: 'user@ecowiki.com',
  avatar: 'https://via.placeholder.com/120',
  bio: '热爱开源的知识分享者',
  createdAt: new Date('2024-05-01')
})

/* ===== 工具函数 ===== */
function formatDate(date: Date) {
  return date.toLocaleDateString()
}

/* ===== 头像上传 ===== */
const avatarInput = ref<HTMLInputElement>()
function triggerUpload() {
  avatarInput.value?.click()
}
async function handleAvatarChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  // TODO: 真实上传逻辑
  user.avatar = URL.createObjectURL(file)
}

/* ===== 编辑资料 ===== */
const editMode = ref(false)
const draft = reactive({ username: '', bio: '' })
function startEdit() {
  draft.username = user.username
  draft.bio = user.bio
  editMode.value = true
}
function saveProfile() {
  user.username = draft.username
  user.bio = draft.bio
  editMode.value = false
}
function cancelEdit() {
  editMode.value = false
}
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
  text-align: center;
  margin-bottom: 32px;
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
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 24px;
}
.avatar-box {
  position: relative;
  margin-bottom: 16px;
}
.avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
}
.upload-btn {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background: #667eea;
  color: #fff;
  border: none;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  cursor: pointer;
  font-size: 12px;
}
.info-box {
  text-align: center;
}
.bio {
  color: #666;
  margin: 8px 0 16px;
}
.meta {
  list-style: none;
  padding: 0;
  font-size: 0.9rem;
  color: #555;
}
.meta li + li {
  margin-top: 4px;
}

/* ===== 侧边栏 ===== */
.action-panel {
  flex: 1 1 180px;
}
.btn {
  display: block;
  width: 100%;
  padding: 10px 16px;
  margin-bottom: 12px;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s;
}
.primary {
  background: #667eea;
  color: #fff;
}
.primary:hover {
  background: #5a6fd8;
}
.secondary {
  background: #f7fafc;
  color: #333;
  border: 1px solid #ddd;
}
.secondary:hover {
  background: #edf2f7;
}

/* ===== 编辑弹窗 ===== */
.edit-modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.edit-box {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  width: 90%;
  max-width: 400px;
}
.edit-box label {
  display: block;
  margin-bottom: 12px;
  font-size: 0.9rem;
}
.edit-box input,
.edit-box textarea {
  width: 100%;
  margin-top: 4px;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.9rem;
}
.edit-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

/* ===== 动画 ===== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>