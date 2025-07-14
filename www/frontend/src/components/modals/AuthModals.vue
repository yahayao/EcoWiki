<!--
  认证模态框容器组件
  
  该组件是应用认证系统的核心容器，负责管理和展示不同类型的模态框。
  包括用户登录、注册和管理员设置界面的统一入口和状态管理。
  
  功能特性：
  - 统一的模态框管理系统
  - 登录和注册表单切换
  - 管理员设置全屏界面
  - 模态框状态控制和事件传递
  - 页面滚动锁定机制
  - 背景模糊和遮罩效果
  
  组件组成：
  - LoginPanel: 登录表单组件
  - RegisterPanel: 注册表单组件
  - AdminLayout: 管理员设置界面（通过路由渲染）
  
  设计特点：
  - 模态框覆盖层统一管理
  - 点击遮罩层关闭功能
  - 响应式设计和移动端适配
  - 平滑的动画过渡效果
  - 高 z-index 确保在最上层显示
  
  状态管理：
  - 通过 props 接收显示状态
  - 通过 emit 向父组件传递事件
  - 监听状态变化控制页面行为
  - 路由集成管理管理员界面
  
  技术实现：
  - Vue 3 Composition API
  - Vue Router 集成
  - CSS 模态框和动画效果
  - TypeScript 类型安全
  - 响应式计算属性
  
  使用场景：
  - 用户身份验证入口
  - 账户注册流程
  - 管理员后台访问
  - 全局认证状态管理
  
  @author EcoWiki Team
  @version 1.0.0
  @since 2024-01-01
-->
<template>
  <!-- 认证模态框容器 - 只在有模态框需要显示时渲染 -->
  <div v-if="showAnyModal">
    <!-- 登录模态框 -->
    <div 
      v-if="showLoginForm" 
      class="auth-modal-overlay" 
      @click.self="$emit('closeModals')"
    >
      <div class="auth-modal">
        <!-- 登录表单组件 -->
        <LoginPanel 
          @switchToRegister="$emit('switchToRegister')" 
          @loginSuccess="$emit('closeModals')"
          @switchToForgot="$emit('switchToForgot')"
        />
        <!-- 模态框关闭按钮 -->
        <button class="close-button" @click="$emit('closeModals')">×</button>
      </div>
    </div>
    <!-- 忘记密码模态框 -->
    <div 
      v-if="showForgotPassword" 
      class="auth-modal-overlay" 
      @click.self="$emit('closeModals')"
    >
      <div class="auth-modal">
        <!-- 忘记密码表单组件 -->
        <Forgotpannel 
          @switchToLogin="$emit('switchToLogin')" 
          @resetSuccess="$emit('closeModals')"
        />
        <!-- 模态框关闭按钮 -->
        <button class="close-button" @click="$emit('closeModals')">×</button>
      </div>
    </div>
    <!-- 注册模态框 -->
    <div 
      v-if="showRegisterForm" 
      class="auth-modal-overlay" 
      @click.self="$emit('closeModals')"
    >
      <div class="auth-modal">
        <!-- 注册表单组件 -->
        <RegisterPanel 
          @switchToLogin="$emit('switchToLogin')" 
          @registerSuccess="$emit('closeModals')"
        />
        <!-- 模态框关闭按钮 -->
        <button class="close-button" @click="$emit('closeModals')">×</button>
      </div>
    </div>

    <!-- 用户个人资料模态框 -->
    <div 
      v-if="showUserProfile" 
      class="auth-modal-overlay admin-overlay" 
      
    >
    <!-- @click.self="$emit('closeModals')" -->
      <div class="admin-modal">
        <router-view />
        <!-- 用户个人资料组件 -->
        <!-- <UserProfile 
          @close="$emit('closeModals')"
        /> -->
      </div>
    </div>

    <!-- 管理员设置模态框 - 全屏显示 -->
    <div v-if="showAdminSettings" class="auth-modal-overlay admin-overlay">
      <div class="admin-modal">
        <!-- 通过路由渲染管理员界面组件 -->
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 认证模态框容器组件脚本
 * 
 * 实现模态框的状态管理、路由控制和页面行为控制。
 * 使用 Vue 3 Composition API 和响应式数据管理。
 */

import { computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import LoginPanel from '../forms/LoginPanel.vue'
import RegisterPanel from '../forms/RegisterPanel.vue'
import Forgotpannel from '../forms/ForgotPanel.vue'
import UserProfile from '../userhome/UserProfile.vue'

// 路由实例，用于管理员界面导航
const router = useRouter()

/**
 * 组件属性接口定义
 * 定义了组件接受的所有显示状态属性
 */
const props = defineProps<{
  /** 是否显示登录表单 */
  showLoginForm: boolean
  /** 是否显示注册表单 */
  showRegisterForm: boolean
  /** 是否显示管理员设置界面 */
  showAdminSettings: boolean
  /** 是否显示忘记密码表单 */
  showForgotPassword: boolean
  /** 是否显示用户个人资料 */
  showUserProfile: boolean
}>()

/**
 * 组件事件定义
 * 定义了组件向父组件发送的所有事件
 */
const emit = defineEmits<{
  /** 关闭所有模态框事件 */
  closeModals: []
  /** 切换到注册表单事件 */
  switchToRegister: []
  /** 切换到登录表单事件 */
  switchToLogin: []
  /** 切换到忘记密码表单事件 */
  switchToForgot: []
}>()

/**
 * 计算是否显示任何模态框
 * 
 * 响应式计算属性，当任何一个模态框需要显示时返回 true。
 * 用于控制整个模态框容器的渲染。
 * 
 * @returns {boolean} 是否有模态框需要显示
 */
const showAnyModal = computed(() => {
  return props.showLoginForm || props.showRegisterForm || props.showAdminSettings || props.showForgotPassword || props.showUserProfile
})

/**
 * 监听管理面板状态变化
 * 
 * 当管理员设置界面打开时，自动导航到系统设置路由。
 * 确保管理员界面能够正确渲染和显示。
 */
watch(() => props.showUserProfile, (isOpen) => {
  if (isOpen) {
    // 打开个人中心时，导航到个人中心页面
    router.push('/UserProfile/Information')
  }
})
watch(() => props.showAdminSettings, (isOpen) => {
  if (isOpen) {
    // 打开管理面板时，导航到系统设置页面
    router.push('/admin/settings')
  }
})

/**
 * 监听模态框状态变化
 * 
 * 控制页面滚动行为，防止模态框显示时背景页面滚动。
 * 通过添加/移除 CSS 类来控制 body 元素的滚动行为。
 */
watch(showAnyModal, (isModalOpen) => {
  if (isModalOpen) {
    // 显示模态框时禁用页面滚动
    document.body.classList.add('modal-open')
  } else {
    // 隐藏模态框时恢复页面滚动
    document.body.classList.remove('modal-open')
  }
})
</script>

<style scoped>
/**
 * 认证模态框容器样式
 * 
 * 实现现代化的模态框设计，包括遮罩层、背景模糊、动画效果和响应式布局。
 * 支持多种模态框类型，并确保良好的用户体验和视觉效果。
 */

/* 全局样式 - 防止模态框显示时页面滚动 */
:global(body.modal-open) {
  overflow: hidden;  /* 禁用页面滚动 */
}

/* 认证模态框遮罩层 */
.auth-modal-overlay {
  position: fixed;               /* 固定定位覆盖整个视口 */
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);  /* 半透明黑色遮罩 */
  display: flex;                 /* Flexbox 居中布局 */
  justify-content: center;       /* 水平居中 */
  align-items: center;           /* 垂直居中 */
  z-index: 1000;                 /* 高层级确保在最上层 */
  overflow: hidden;              /* 防止内容溢出 */
  backdrop-filter: blur(4px);    /* 背景模糊效果 */
}

/* 认证模态框容器 */
.auth-modal {
  position: relative;            /* 相对定位为子元素提供参考 */
  max-width: 90%;               /* 最大宽度限制，适配小屏幕 */
  max-height: 85vh;             /* 最大高度限制，留出顶部空间 */
  display: flex;                /* Flexbox 布局 */
  justify-content: center;      /* 内容居中 */
  align-items: center;          /* 垂直居中 */
  overflow: visible;            /* 允许内容可见，支持阴影等效果 */
}

/* 模态框关闭按钮 */
.close-button {
  position: absolute;           /* 绝对定位在模态框右上角 */
  top: 20px;
  right: 20px;
  background: rgba(0, 0, 0, 0.5);  /* 半透明黑色背景 */
  border: none;                 /* 无边框 */
  font-size: 24px;             /* 字体大小 */
  color: white;                /* 白色文字 */
  cursor: pointer;             /* 手型光标 */
  z-index: 1002;               /* 确保在模态框内容之上 */
  width: 36px;                 /* 固定宽度 */
  height: 36px;                /* 固定高度 */
  display: flex;               /* Flexbox 布局 */
  align-items: center;         /* 垂直居中 */
  justify-content: center;     /* 水平居中 */
  border-radius: 50%;          /* 圆形按钮 */
  transition: all 0.3s ease;   /* 平滑过渡动画 */
  backdrop-filter: blur(10px); /* 背景模糊效果 */
}

/* 关闭按钮悬停效果 */
.close-button:hover {
  background: rgba(0, 0, 0, 0.7);  /* 悬停时加深背景 */
  transform: scale(1.1);           /* 轻微放大效果 */
}

/* 管理员模态框遮罩层 - 全屏显示，无背景效果 */
.admin-overlay {
  background: none;            /* 无背景遮罩 */
  backdrop-filter: none;       /* 无背景模糊 */
}

/* 管理员模态框容器 - 全屏布局 */
.admin-modal {
  position: fixed;             /* 固定定位覆盖整个视口 */
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100vw;               /* 完整视口宽度 */
  height: 100vh;              /* 完整视口高度 */
  background: white;          /* 白色背景 */
  border-radius: 0;           /* 无圆角 */
  box-shadow: none;           /* 无阴影 */
  overflow: hidden;           /* 防止内容溢出 */
  z-index: 1001;              /* 高层级 */
}

/* 响应式设计 - 移动端适配 */
@media (max-width: 768px) {
  .close-button {
    top: 10px;                /* 减少距离顶部距离 */
    right: 10px;              /* 减少距离右侧距离 */
    width: 32px;              /* 减小按钮尺寸 */
    height: 32px;             /* 减小按钮尺寸 */
    font-size: 20px;          /* 减小字体大小 */
  }
  
  .auth-modal {
    max-width: 95%;           /* 移动端增加宽度使用率 */
    max-height: 90vh;         /* 移动端增加高度使用率 */
  }
}

/* 超小屏幕适配 */
@media (max-width: 480px) {
  .auth-modal-overlay {
    padding: 5px;             /* 添加内边距防止贴边 */
  }
  
  .auth-modal {
    max-width: 100%;          /* 超小屏幕占满宽度 */
    max-height: 95vh;         /* 超小屏幕最大化高度使用 */
  }
  
  .close-button {
    top: 5px;                 /* 进一步减少边距 */
    right: 5px;               /* 进一步减少边距 */
    width: 28px;              /* 更小的按钮尺寸 */
    height: 28px;             /* 更小的按钮尺寸 */
    font-size: 18px;          /* 更小的字体大小 */
  }
}
</style>
