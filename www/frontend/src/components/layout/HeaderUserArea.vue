<!--
  头部用户区域组件
  
  该组件负责显示用户认证状态和提供相关操作入口。
  根据用户登录状态动态切换显示内容，提供登录、注册、管理和登出功能。
  
  功能特性：
  - 动态显示用户登录状态
  - 用户头像和用户名展示
  - 登录/注册按钮（未登录状态）
  - 管理员设置入口（管理员权限）
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
    <!-- 已登录状态 - 显示用户信息和操作按钮 -->
    <template v-if="isAuthenticated">
      <!-- 用户信息展示 -->
       <div class="user-menu-wrapper" @mouseenter="showMenu = true" @mouseleave="showMenu = false">
        <div class="user-info">
          <div>
            <img :src="userAvatar" alt="用户头像" class="user-avatar" />
          </div>
          <span class="username">{{ user?.username }}</span>
        </div>
        <transition name="fade">
          <div v-if="showMenu" class="menu">
            <div class="menu-item">个人主页</div>
            <div class="menu-item">消息通知</div>
            <div class="menu-item">登出</div>
          </div>
        </transition>
      </div>
      <!-- <div class="avatar-dropdown" @mouseenter="showMenu = true" @mouseleave="showMenu = false">
        <div class="avatar-container">
          <img :src="userAvatar" alt="用户头像" class="user-avatar" />
          <span class="username">{{ user?.username }}</span>
        </div>
        <transition name="fade">
          <div v-if="showMenu" class="menu">
            <div class="menu-item">个人主页</div>
            <div class="menu-item">消息通知</div>
            <div class="menu-item">登出</div>
          </div>
        </transition>
      </div> -->
      
      <!-- 管理员设置按钮（仅管理员可见） -->
      <button 
        v-if="hasAdminPermission" 
        class="action-button settings-button" 
        @click="$emit('showAdminSettings')"
        title="系统设置"
      >
        ⚙️ 设置
      </button>
      
      <!-- 登出按钮 -->
      <button class="action-button logout-button" @click="$emit('logout')">登出</button>
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

import { computed, ref, defineEmits } from 'vue'
import { useAuth } from '../../composables/useAuth'
import { userApi } from '../../api/user'

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
  /** 用户登出事件 */
  logout: []
}>()

/**
 * 全局认证状态管理
 * 获取用户信息、登录状态和头像数据
 */
const { user, isAuthenticated, userAvatar } = useAuth()

/**
 * 管理员权限检查
 * 
 * 计算属性，动态检查当前用户是否具有管理员权限。
 * 用于控制管理员设置按钮的显示和隐藏。
 * 通过检查用户角色来判断是否为管理员。
 * 
 * @returns {boolean} 是否具有管理员权限
 * 
 * @example
 * // 在模板中使用
 * <button v-if="hasAdminPermission">管理员设置</button>
 */
const hasAdminPermission = computed(() => {
  if (!user.value) return false
  
  // 检查用户组是否为管理员相关角色
  const adminRoles = ['admin', 'superadmin']
  return adminRoles.includes(user.value.userGroup?.toLowerCase() || '')
})
const showMenu = ref(false)
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

/* 设置按钮样式 */
.settings-button {
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.settings-button:hover {
  background-color: rgba(255, 255, 255, 0.2);
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
</style>
