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
    <div v-if="showAdminSettings" class="auth-modal-overlay admin-overlay">
      <div class="admin-modal">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import LoginPanel from './LoginPanel.vue'
import RegisterPanel from './RegisterPanel.vue'

const router = useRouter()

// 定义props
const props = defineProps<{
  showLoginForm: boolean
  showRegisterForm: boolean
  showAdminSettings: boolean
}>()

// 定义事件
const emit = defineEmits<{
  closeModals: []
  switchToRegister: []
  switchToLogin: []
}>()

// 计算是否显示任何模态框
const showAnyModal = computed(() => {
  return props.showLoginForm || props.showRegisterForm || props.showAdminSettings
})

// 监听管理面板状态，控制路由
watch(() => props.showAdminSettings, (isOpen) => {
  if (isOpen) {
    // 打开管理面板时，导航到系统设置
    router.push('/admin/settings')
  }
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
  top: 20px;
  right: 20px;
  background: rgba(0, 0, 0, 0.5);
  border: none;
  font-size: 24px;
  color: white;
  cursor: pointer;
  z-index: 1002;
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

/* 管理员模态框样式 - 全屏显示 */
.admin-overlay {
  background: none;
  backdrop-filter: none;
}

.admin-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100vw;
  height: 100vh;
  background: white;
  border-radius: 0;
  box-shadow: none;
  overflow: hidden;
  z-index: 1001;
}

@media (max-width: 768px) {
  .close-button {
    top: 10px;
    right: 10px;
    width: 32px;
    height: 32px;
    font-size: 20px;
  }
}
</style>
