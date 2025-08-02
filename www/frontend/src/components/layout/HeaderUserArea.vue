<!--
  头部用户区域组件
  
  该组件负责显示用户认证状态和提供相关操作入口。
  根据用户登录状态动态切换显示内容，提供登录、注册、管理和登出功能。
  
  功能特性：
  - 动态显示用户登录状态
  - 用户头像和用户名展示
  - 登录/注册按钮（未登录状态）
  /**
 * 全局认证状态管理
 * 获取用户信息、登录状态和头像数据
 */
const { user, isAuthenticated, userAvatar, refreshUserInfo } = useAuth()设置入口（管理员权限）
  - 登出操作按钮
  - 权限验证和条件渲染
  
  状态区分：
  - 未登录状态：显示登录和注册按钮
  - 已登录状态：显示用户信息、设置和登出按钮
  - 管理员状态：额外显示系统设置按钮
  
  设计特点：
  - 响应式按钮设计
  - 悬停动画效果
  - 权限判断和条件显示
  - 统一的视觉风格
  - 平滑的交互过渡
  
  权限系统：
  - 集成全局认证状态
  - 管理员权限检查
  - 动态内容渲染
  - 安全的操作控制
  
  技术实现：
  - Vue 3 Composition API
  - TypeScript 类型安全
  - 计算属性和响应式状态
  - CSS 动画和渐变效果
  - 权限集成和验证
  
  使用场景：
  - 应用头部用户操作区
  - 身份验证和权限管理
  - 用户状态指示器
  - 快速操作入口
  
  @author EcoWiki Team
  @version 1.0.0
  @since 2024-01-01
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
            <div @click="$emit('showMessages')" class="menu-item">
              <span>消息通知</span>
              <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
            </div>
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

import { computed, ref, onMounted, watch, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../../composables/useAuth'
import { userApi, USER_GROUPS } from '../../api/user'
import { messageApi } from '../../api/message'
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
  /** 显示消息面板事件 */
  showMessages: []
  /** 用户登出事件 */
  logout: []
}>()

/**
 * 全局认证状态管理
 * 获取用户信息、登录状态和头像数据
 */
const { user, isAuthenticated, userAvatar } = useAuth()

/**
 * 路由实例
 */
const router = useRouter()

/**
 * 响应式数据
 */
const showMenu = ref(false)
const unreadCount = ref(0)

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

/**
 * 加载未读消息数量
 */
const loadUnreadCount = async () => {
  if (!user.value) {
    console.log('⏹️ 用户未登录，跳过未读消息数量检查')
    unreadCount.value = 0
    return
  }
  
  // 检查认证状态
  const hasRefreshToken = !!localStorage.getItem('refreshToken')
  if (!hasRefreshToken) {
    console.warn('⚠️ 警告：用户已登录但没有refresh token，这可能导致API调用失败')
  }
  
  try {
    console.log('🔄 开始获取未读消息数量...')
    unreadCount.value = await messageApi.getUnreadCount()
    console.log('✅ 未读消息数量:', unreadCount.value)
  } catch (error) {
    console.error('❌ 获取未读消息数量失败:', error)
    
    // 如果是认证错误，不要继续尝试
    if ((error as any)?.response?.status === 401) {
      console.warn('🚫 认证失败，停止获取未读消息数量')
      unreadCount.value = 0
      return
    }
    
    unreadCount.value = 0
  }
}

/**
 * 监听用户登录状态变化
 */
watch(isAuthenticated, (newValue) => {
  if (newValue) {
    loadUnreadCount()
  } else {
    unreadCount.value = 0
  }
})

/**
 * 组件挂载时加载未读消息数量
 */
onMounted(() => {
  if (isAuthenticated.value) {
    loadUnreadCount()
  }
  
  // 定期更新未读消息数量（每30秒）
  const unreadCountInterval = setInterval(() => {
    if (isAuthenticated.value && user.value) {
      console.log('定时检查未读消息数量...')
      loadUnreadCount()
    } else {
      console.log('用户未登录，跳过未读消息数量检查')
    }
  }, 30000)
  
  // 组件卸载时清理定时器
  onBeforeUnmount(() => {
    if (unreadCountInterval) {
      clearInterval(unreadCountInterval)
    }
  })
})
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

/* 未读消息数量badge */
.unread-badge {
  background: #ef4444;
  color: white;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 8px;
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
</style>
