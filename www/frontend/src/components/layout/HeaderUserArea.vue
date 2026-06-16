<!--
/**
 * 头部用户区域组件
 * 
 * 功能：
 * - 管理用户认证状态和操作入口
 * - 提供登录、注册、管理功能
 * - 支持用户头像和信息展示
 * - 实现权限验证和条件渲染
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
-->

<template>
  <!-- 用户操作区域容器 -->
  <div class="user-area">
    <!-- 创建页面按钮 - 仅登录用户可见 -->
    <button 
      v-if="isAuthenticated"
      class="action-button create-button" 
      @click="navigateToCreatePage"
      title="创建新页面"
    >
      <svg viewBox="0 0 24 24" class="button-icon">
        <path d="M19,13H13V19H11V13H5V11H11V5H13V11H19V13Z"/>
      </svg>
      创建页面
    </button>

    <!-- 已登录状态 - 显示用户信息和操作按钮 -->
    <template v-if="isAuthenticated">
      <!-- 消息铃铛入口 -->
      <div class="bell-menu-wrapper" @mouseenter="openBellMenu" @mouseleave="showBellMenu = false">
        <button class="bell-btn" @click="router.push('/messages')" title="消息中心">
          <svg viewBox="0 0 24 24" class="bell-icon">
            <path d="M12,22A2,2 0 0,0 14,20H10A2,2 0 0,0 12,22M18,16V11C18,7.93 16.36,5.36 13.5,4.68V4A1.5,1.5 0 0,0 12,2.5A1.5,1.5 0 0,0 10.5,4V4.68C7.63,5.36 6,7.92 6,11V16L4,18V19H20V18L18,16Z"/>
          </svg>
          <span v-if="messageStore.unreadCount > 0" class="bell-badge">{{ messageStore.unreadCount > 99 ? '99+' : messageStore.unreadCount }}</span>
        </button>
        <transition name="fade">
          <div v-if="showBellMenu" class="bell-dropdown">
            <div class="bell-dropdown-header">
              <span>收件箱</span>
              <span v-if="messageStore.unreadCount > 0" class="bell-dropdown-count">{{ messageStore.unreadCount }}条未读</span>
            </div>
            <div v-if="messageStore.inbox.length === 0" class="bell-dropdown-empty">暂无未读消息</div>
            <template v-else>
              <div
                v-for="msg in messageStore.inbox.slice(0, 5)"
                :key="msg.messageId"
                class="bell-dropdown-item"
                @click="router.push('/messages')"
              >
                <div class="bell-dropdown-subject">{{ msg.subject || msg.content.slice(0, 20) }}</div>
                <div class="bell-dropdown-meta">
                  <span class="bell-dropdown-sender">{{ msg.senderUsername || '系统' }}</span>
                  <span class="bell-dropdown-time">{{ formatMsgTime(msg.sendTime) }}</span>
                </div>
              </div>
            </template>
            <div class="bell-dropdown-footer" @click="router.push('/messages')">查看全部消息</div>
          </div>
        </transition>
      </div>

      <!-- 用户信息展示 -->
       <div class="user-menu-wrapper" @mouseenter="showMenu = true" @mouseleave="showMenu = false">
        <div class="user-info">
          <!-- 使用新的头像组件 -->
          <UserAvatar 
            :username="user?.username"
            :avatar-url="user?.avatarUrl"
            size="sm"
            shape="circle"
          />
          <span class="username">{{ user?.username }}</span>
        </div>
        <transition name="fade">
          <div v-if="showMenu" class="menu">
            <div @click="$emit('showUserProfile')" class="menu-item">个人主页</div>
            <div 
              v-if="hasAdminPermission" 
              @click="$emit('showAdminSettings')" 
              class="menu-item"
              title="系统设置"
            >
              设置
            </div>
            <div @click="$emit('logout')" class="menu-item">登出</div>
          </div>
        </transition>
      </div>
      
    </template>
    
    <!-- 未登录状态 - 显示登录和注册按钮 -->
    <template v-else>
      <button class="action-button login-button" @click="$emit('showLogin')">登录</button>
      <button class="action-button register-button" @click="$emit('showRegister')">注册</button>
    </template>
  </div>
</template>

<script setup lang="ts">
/**
 * 头部用户区域组件脚本
 * 
 * 实现用户状态检查、权限验证和操作事件管理。
 * 集成全局认证状态，提供动态的用户界面。
 */

import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../../composables/useAuth'
import { userApi } from '../../api/user'
import { useMessageStore } from '../../stores/messageStore'
import UserAvatar from '../common/UserAvatar.vue'

/**
 * 组件事件定义
 * 定义了组件向父组件发送的所有用户操作事件
 */
defineEmits<{
  /** 显示登录模态框事件 */
  showLogin: []
  /** 显示注册模态框事件 */
  showRegister: []
  /** 显示管理员设置界面事件 */
  showAdminSettings: []
  /** 显示个人中心界面事件 */
  showUserProfile: []
  /** 用户登出事件 */
  logout: []
}>()

/**
 * 全局认证状态管理
 * 获取用户信息、登录状态和头像数据
 */
const { user, isAuthenticated } = useAuth()

/**
 * 路由实例
 */
const router = useRouter()

/**
 * 响应式数据
 */
const showMenu = ref(false)

/**
 * 导航到创建页面
 */
const navigateToCreatePage = () => {
  router.push('/create')
}

/**
 * 管理员权限检查
 * 
 * 计算属性，动态检查当前用户是否具有管理员权限。
 * 用于控制管理员设置按钮的显示和隐藏。
 * 通过检查用户角色来判断是否为管理员或超级管理员。
 * 
 * @returns {boolean} 是否具有管理员权限
 * 
 * @example
 * // 在模板中使用
 * <button v-if="hasAdminPermission">管理员设置</button>
 */
const hasAdminPermission = computed(() => {
  if (!user.value) return false

  // 使用官方的权限检查函数，确保只有管理员和超级管理员可以看到设置
  return userApi.isAdmin(user.value)
})

// ── 未读消息计数 + 轮询（委托 messageStore） ──────────────────────────────
const messageStore = useMessageStore()

onMounted(() => {
  messageStore.startPolling(30_000)
})

onUnmounted(() => {
  messageStore.stopPolling()
})

// ── 铃铛悬停下拉面板（委托 messageStore） ──────────────────────────────────
const showBellMenu = ref(false)

async function openBellMenu() {
  showBellMenu.value = true
  if (!isAuthenticated.value) return
  messageStore.fetchInbox()
}

function formatMsgTime(timeStr: string): string {
  const diff = Math.floor((Date.now() - new Date(timeStr).getTime()) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  return `${Math.floor(diff / 86400)}天前`
}

</script>

<style scoped>
/**
 * 头部用户区域样式
 * 
 * 实现现代化的用户操作界面设计，包括按钮样式、悬停效果和动画过渡。
 * 支持不同用户状态的视觉表现和交互反馈。
 */

/* 用户操作区域主容器 */
.user-area {
  display: flex;              /* Flexbox 布局 */
  align-items: center;        /* 垂直居中对齐 */
  gap: 12px;                  /* 子元素间距 */
}

/* 用户信息展示区域 */
.user-info {
  display: flex;              /* Flexbox 布局 */
  align-items: center;        /* 垂直居中对齐 */
  gap: 8px;                   /* 头像和用户名间距 */
}

/* 用户头像样式 */
.user-avatar {
  width: 36px;                /* 固定头像尺寸 */
  height: 36px;               /* 保持正方形 */
  border-radius: 50%;         /* 圆形头像 */
  border: 2px solid rgba(255, 255, 255, 0.3);  /* 半透明白色边框 */
  transition: all 0.3s ease;  /* 平滑过渡动画 */
}

/* 头像悬停效果 */
.user-avatar:hover {
  border-color: rgba(255, 255, 255, 0.6);  /* 悬停时加深边框 */
  transform: scale(1.05);     /* 轻微放大效果 */
}

/* 用户名文字样式 */
.username {
  font-weight: 500;           /* 中等粗细字体 */
  font-size: 0.95rem;         /* 适中的字体大小 */
}

/* 操作按钮通用样式 */
.action-button {
  padding: 10px 20px;         /* 内边距 */
  border: none;               /* 无默认边框 */
  border-radius: 22px;        /* 圆角按钮 */
  font-weight: 500;           /* 中等粗细字体 */
  cursor: pointer;            /* 手型光标 */
  transition: all 0.3s ease;  /* 平滑过渡动画 */
  font-size: 0.9rem;          /* 字体大小 */
  position: relative;         /* 为伪元素提供定位基准 */
  overflow: hidden;           /* 隐藏溢出的动画效果 */
}

/* 按钮光泽动画效果 */
.action-button::before {
  content: '';                /* 空内容 */
  position: absolute;         /* 绝对定位 */
  top: 0;
  left: -100%;               /* 初始位置在左侧外部 */
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s;      /* 左侧位置过渡动画 */
}

/* 按钮悬停时的光泽效果 */
.action-button:hover::before {
  left: 100%;                /* 光泽从左到右划过 */
}

/* 登录按钮样式 */
.login-button {
  background-color: transparent;           /* 透明背景 */
  color: white;                           /* 白色文字 */
  border: 2px solid rgba(255, 255, 255, 0.7);  /* 半透明白色边框 */
}

/* 登录按钮悬停效果 */
.login-button:hover {
  background-color: rgba(255, 255, 255, 0.1);  /* 悬停时添加背景 */
  border-color: white;                    /* 边框变为纯白色 */
  transform: translateY(-1px);            /* 向上轻微移动 */
}

/* 注册按钮样式 */
.register-button {
  background-color: white;                /* 白色背景 */
  color: #667eea;                        /* 品牌色文字 */
}

/* 注册按钮悬停效果 */
.register-button:hover {
  background-color: #f7fafc;             /* 悬停时背景稍微变色 */
  transform: translateY(-1px);           /* 向上轻微移动 */
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);  /* 添加阴影效果 */
}

/* 创建页面按钮样式 */
.create-button {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);  /* 绿色渐变背景 */
  color: white;                          /* 白色文字 */
  border: none;                          /* 无边框 */
  box-shadow: 0 2px 10px rgba(16, 185, 129, 0.3);  /* 绿色阴影 */
  display: flex;                         /* 弹性布局 */
  align-items: center;                   /* 垂直居中 */
  gap: 6px;                             /* 图标和文字间距 */
}

/* 创建页面按钮悬停效果 */
.create-button:hover {
  background: linear-gradient(135deg, #059669 0%, #047857 100%);  /* 更深的绿色渐变 */
  transform: translateY(-1px);           /* 向上轻微移动 */
  box-shadow: 0 4px 15px rgba(16, 185, 129, 0.4);  /* 加强阴影效果 */
}

/* 按钮图标样式 */
.button-icon {
  width: 16px;                          /* 图标宽度 */
  height: 16px;                         /* 图标高度 */
  fill: currentColor;                   /* 图标颜色继承文字颜色 */
}

/* 登出按钮样式 */
.logout-button {
  background-color: rgba(255, 255, 255, 0.1);  /* 半透明白色背景 */
  color: white;                          /* 白色文字 */
  border: 1px solid rgba(255, 255, 255, 0.3);  /* 半透明白色边框 */
}

/* 登出按钮悬停效果 */
.logout-button:hover {
  background-color: rgba(255, 255, 255, 0.2);  /* 悬停时加深背景 */
}

/* 响应式设计 */
@media (max-width: 480px) {
  .user-area {
    flex-direction: column;
    gap: 8px;
    width: 100%;
  }
  
  .action-button {
    width: 100%;
    text-align: center;
  }
}
/* -----------------头像下拉菜单----------------- */
/* 最外层包裹：定位参照点，确保菜单相对头像+用户名出现 */
.user-menu-wrapper {
  position: relative;
  display: inline-block;
}
/* 头像+用户名行：横向排列，鼠标手型提示可点击 */
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}
/* 头像图片：填满圆形容器并保持比例 */
.user-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
/* 用户名文字：左侧留 8px 空隙，白色字体，14px 大小 */
.username {
  margin-left: 8px;
  font-size: 14px;
  color: #ffffff;
}
/* 下拉菜单容器：紧贴头像/用户名下方，左对齐，白色背景，圆角阴影 */
.menu {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 0px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  padding: 8px 0;
  z-index: 1000;
  min-width: 105px;
}
/* 菜单项单行：左右 16px 内边距，14px 深灰字体，hover 高亮 */
.menu-item {
  padding: 8px 16px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: background-color 0.3s ease;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.menu-item:hover {
  background-color: #f5f5f5;
}

/* Vue 过渡动画：淡入淡出 + 向下位移 10px */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* ── 铃铛悬停下拉面板 ─────────────────────────────────────────────────────── */
.bell-menu-wrapper {
  position: relative;
  display: inline-flex;
}

.bell-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  width: 280px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  z-index: 1000;
  overflow: hidden;
}

.bell-dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  font-size: 13px;
  font-weight: 600;
  color: #111827;
  border-bottom: 1px solid #f3f4f6;
}

.bell-dropdown-count {
  font-size: 11px;
  font-weight: 600;
  color: #ef4444;
  background: #fee2e2;
  padding: 2px 7px;
  border-radius: 10px;
}

.bell-dropdown-empty {
  padding: 20px 14px;
  font-size: 13px;
  color: #9ca3af;
  text-align: center;
}

.bell-dropdown-item {
  padding: 10px 14px;
  cursor: pointer;
  border-bottom: 1px solid #f9fafb;
  transition: background 0.15s;
}

.bell-dropdown-item:hover {
  background: #f9fafb;
}

.bell-dropdown-subject {
  font-size: 13px;
  color: #111827;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bell-dropdown-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 3px;
}

.bell-dropdown-sender {
  font-size: 11px;
  color: #6b7280;
}

.bell-dropdown-time {
  font-size: 11px;
  color: #9ca3af;
}

.bell-dropdown-footer {
  padding: 9px 14px;
  font-size: 12px;
  color: #667eea;
  text-align: center;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.15s;
}

.bell-dropdown-footer:hover {
  background: #f5f3ff;
}
.bell-btn {
  position: relative;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  flex-shrink: 0;
}

.bell-btn:hover { background: rgba(255, 255, 255, 0.28); }

.bell-icon {
  width: 20px;
  height: 20px;
  fill: #fff;
}

.bell-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 8px;
  background: #ef4444;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}
</style>

