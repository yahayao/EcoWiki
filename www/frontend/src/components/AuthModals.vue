<template>
  <!-- 认证模态框容器 -->
  <div v-if="showAnyModal">
    <!-- 登录模态框 -->
    <div v-if="showLoginForm" class="auth-modal-overlay" @click.self="$emit('closeModals')">
      <div class="auth-modal">
        <LoginPanel 
          @switchToRegister="$emit('switchToRegister')" 
          @loginSuccess="$emit('closeModals')"
        />
        <button class="close-button" @click="$emit('closeModals')">×</button>
      </div>
    </div>

    <!-- 注册模态框 -->
    <div v-if="showRegisterForm" class="auth-modal-overlay" @click.self="$emit('closeModals')">
      <div class="auth-modal">
        <RegisterPanel 
          @switchToLogin="$emit('switchToLogin')" 
          @registerSuccess="$emit('closeModals')"
        />
        <button class="close-button" @click="$emit('closeModals')">×</button>
      </div>
    </div>

    <!-- 管理员设置模态框 -->
    <div v-if="showAdminSettings" class="auth-modal-overlay" @click.self="$emit('closeModals')">
      <div class="admin-modal">
        <AdminSettings />
        <button class="close-button" @click="$emit('closeModals')">×</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import LoginPanel from './LoginPanel.vue'
import RegisterPanel from './RegisterPanel.vue'
import AdminSettings from './AdminSettings.vue'

// 定义props
const props = defineProps<{
  showLoginForm: boolean
  showRegisterForm: boolean
  showAdminSettings: boolean
}>()

// 定义事件
defineEmits<{
  closeModals: []
  switchToRegister: []
  switchToLogin: []
}>()

// 计算是否显示任何模态框
const showAnyModal = computed(() => {
  return props.showLoginForm || props.showRegisterForm || props.showAdminSettings
})

// 监听模态框状态，控制页面滚动
watch(showAnyModal, (isModalOpen) => {
  if (isModalOpen) {
    document.body.classList.add('modal-open')
  } else {
    document.body.classList.remove('modal-open')
  }
})
</script>

<style scoped>
/* 防止模态框显示时页面滚动 */
:global(body.modal-open) {
  overflow: hidden;
}

/* 认证模态框 */
.auth-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  overflow: hidden;
  backdrop-filter: blur(4px);
}

.auth-modal {
  position: relative;
  max-width: 90%;
  max-height: 85vh;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: visible;
}

.close-button {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.5);
  border: none;
  font-size: 24px;
  color: white;
  cursor: pointer;
  z-index: 1001;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.close-button:hover {
  background: rgba(0, 0, 0, 0.7);
  transform: scale(1.1);
}

/* 管理员模态框样式 */
.admin-modal {
  position: relative;
  min-width: 600px;
  width: 1000px;
  max-width: 95vw;
  max-height: 90vh;
  overflow: auto;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

@media (max-width: 700px) {
  .admin-modal {
    min-width: 0;
    width: 100vw;
    border-radius: 0;
  }
}

@media (max-width: 768px) {
  .admin-modal {
    max-width: 100%;
    max-height: 100vh;
    border-radius: 0;
  }
}
</style>
