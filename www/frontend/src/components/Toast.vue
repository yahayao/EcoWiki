<!--
  Toast 消息提示组件
  
  该组件提供了一个现代化的消息提示系统，支持多种类型的通知消息。
  使用 Vue 3 的 Teleport 功能将组件渲染到 body 元素，确保在页面最上层显示。
  
  功能特性：
  - 四种消息类型：成功、错误、警告、信息
  - 自动消失或手动关闭
  - 平滑的进入和退出动画
  - 响应式设计，适配不同屏幕尺寸
  - 可自定义标题和消息内容
  - 高 z-index 确保在最上层显示
  
  设计特点：
  - 固定在右上角显示
  - 左侧彩色边框标识消息类型
  - 圆形图标配合类型主题色
  - 卡片式设计，带阴影效果
  - 关闭按钮支持悬停效果
  
  技术实现：
  - Vue 3 Composition API
  - TypeScript 类型定义
  - CSS Transitions 动画
  - Teleport 传送功能
  
  使用场景：
  - 表单提交成功/失败提示
  - 系统状态通知
  - 用户操作反馈
  - 错误信息展示
  
  @author EcoWiki Team
  @version 1.0.0
  @since 2024-01-01
-->
<template>
  <!-- 使用 Teleport 将组件渲染到 body，确保在页面最上层 -->
  <Teleport to="body">
    <!-- 进入/退出过渡动画 -->
    <Transition name="toast">
      <div 
        v-if="visible" 
        class="toast-container"
        :class="`toast-${type}`"
      >
        <!-- 消息类型图标 -->
        <div class="toast-icon">
          <span v-if="type === 'success'">✓</span>
          <span v-else-if="type === 'error'">✗</span>
          <span v-else-if="type === 'warning'">⚠</span>
          <span v-else>ℹ</span>
        </div>
        
        <!-- 消息内容区域 -->
        <div class="toast-content">
          <!-- 可选的标题 -->
          <div class="toast-title" v-if="title">{{ title }}</div>
          <!-- 主要消息内容 -->
          <div class="toast-message">{{ message }}</div>
        </div>
        
        <!-- 手动关闭按钮 -->
        <button @click="close" class="toast-close">✕</button>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
/**
 * Toast 消息提示组件脚本
 * 
 * 实现消息提示的显示逻辑、自动关闭计时器和手动关闭功能。
 * 使用 Vue 3 Composition API 和 TypeScript 进行类型安全的开发。
 */

import { ref, onMounted } from 'vue'

/**
 * 组件属性接口定义
 * 定义了 Toast 组件接受的所有属性及其类型
 */
interface Props {
  /** 消息内容（必填） */
  message: string
  /** 消息标题（可选） */
  title?: string
  /** 消息类型（可选，默认为 info） */
  type?: 'success' | 'error' | 'warning' | 'info'
  /** 自动关闭时间，单位毫秒（可选，默认 3000ms，设为 0 表示不自动关闭） */
  duration?: number
}

/**
 * 属性接收和默认值设置
 * 为可选属性设置合理的默认值
 */
const props = withDefaults(defineProps<Props>(), {
  type: 'info',      // 默认为信息类型
  duration: 3000     // 默认 3 秒后自动关闭
})

/**
 * 组件事件定义
 * 定义组件向父组件发送的事件
 */
const emit = defineEmits(['close'])

/**
 * 显示状态控制
 * 控制 Toast 组件的显示和隐藏
 */
const visible = ref(false)

/**
 * 关闭 Toast 消息
 * 
 * 设置隐藏状态并在动画完成后触发关闭事件。
 * 延迟 300ms 等待退出动画完成，确保用户体验流畅。
 * 
 * @example
 * // 用户点击关闭按钮时调用
 * close()
 */
const close = () => {
  visible.value = false
  // 延迟触发关闭事件，等待退出动画完成
  setTimeout(() => {
    emit('close')
  }, 300)  // 与 CSS 动画时间保持一致
}

/**
 * 组件挂载时的初始化逻辑
 * 
 * 1. 显示 Toast 组件
 * 2. 如果设置了自动关闭时间，启动计时器
 */
onMounted(() => {
  // 显示 Toast
  visible.value = true
  
  // 如果设置了自动关闭时间且大于 0，启动自动关闭计时器
  if (props.duration > 0) {
    setTimeout(() => {
      close()
    }, props.duration)
  }
})
</script>

<style scoped>
/**
 * Toast 消息提示组件样式
 * 
 * 实现现代化的卡片式消息提示设计，包括类型主题色、动画效果和响应式布局。
 * 使用固定定位确保在页面最上层显示，支持平滑的进入和退出动画。
 */

/* Toast 主容器 - 固定在右上角的卡片式设计 */
.toast-container {
  position: fixed;
  top: 20px;                      /* 距离顶部距离 */
  right: 20px;                    /* 距离右侧距离 */
  background: white;              /* 白色背景 */
  border-radius: 8px;             /* 圆角边框 */
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);  /* 深度阴影效果 */
  padding: 16px;                  /* 内边距 */
  display: flex;                  /* Flexbox 布局 */
  align-items: flex-start;        /* 顶部对齐 */
  gap: 12px;                      /* 子元素间距 */
  max-width: 400px;               /* 最大宽度限制 */
  z-index: 1000;                  /* 高层级确保在最上层 */
  border-left: 4px solid;         /* 左侧类型标识边框 */
}

/* 成功类型 - 绿色主题 */
.toast-success {
  border-left-color: #27ae60;     /* 绿色左边框 */
}

/* 错误类型 - 红色主题 */
.toast-error {
  border-left-color: #e74c3c;     /* 红色左边框 */
}

/* 警告类型 - 橙色主题 */
.toast-warning {
  border-left-color: #f39c12;     /* 橙色左边框 */
}

/* 信息类型 - 蓝色主题 */
.toast-info {
  border-left-color: #3498db;     /* 蓝色左边框 */
}

/* 消息类型图标容器 */
.toast-icon {
  width: 24px;                    /* 固定宽度 */
  height: 24px;                   /* 固定高度 */
  border-radius: 50%;             /* 圆形设计 */
  display: flex;                  /* Flexbox 布局 */
  align-items: center;            /* 垂直居中 */
  justify-content: center;        /* 水平居中 */
  font-weight: bold;              /* 粗体字体 */
  color: white;                   /* 白色图标 */
  font-size: 14px;                /* 图标字体大小 */
}

/* 成功图标背景色 */
.toast-success .toast-icon {
  background: #27ae60;
}

/* 错误图标背景色 */
.toast-error .toast-icon {
  background: #e74c3c;
}

/* 警告图标背景色 */
.toast-warning .toast-icon {
  background: #f39c12;
}

/* 信息图标背景色 */
.toast-info .toast-icon {
  background: #3498db;
}

/* 消息内容区域 - 占据剩余空间 */
.toast-content {
  flex: 1;                        /* 弹性增长占满剩余空间 */
}

/* 消息标题样式 */
.toast-title {
  font-weight: 600;               /* 半粗体 */
  color: #333;                    /* 深灰色文字 */
  margin-bottom: 4px;             /* 与消息内容的间距 */
  font-size: 14px;                /* 标题字体大小 */
}

/* 消息内容样式 */
.toast-message {
  color: #666;                    /* 中灰色文字 */
  font-size: 13px;                /* 消息字体大小 */
  line-height: 1.4;               /* 行高设置提高可读性 */
}

/* 关闭按钮样式 */
.toast-close {
  background: none;               /* 无背景 */
  border: none;                   /* 无边框 */
  color: #999;                    /* 浅灰色 */
  cursor: pointer;                /* 手型光标 */
  font-size: 16px;                /* 字体大小 */
  padding: 0;                     /* 无内边距 */
  width: 20px;                    /* 固定宽度 */
  height: 20px;                   /* 固定高度 */
  display: flex;                  /* Flexbox 布局 */
  align-items: center;            /* 垂直居中 */
  justify-content: center;        /* 水平居中 */
  border-radius: 50%;             /* 圆形按钮 */
  transition: all 0.2s ease;      /* 平滑过渡动画 */
}

/* 关闭按钮悬停效果 */
.toast-close:hover {
  background: #f5f5f5;            /* 浅灰背景 */
  color: #666;                    /* 深一点的文字颜色 */
}

/* Toast 进入和退出动画 */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;      /* 300ms 的平滑过渡 */
}

/* 进入动画起始状态 - 从右侧滑入 */
.toast-enter-from {
  opacity: 0;                     /* 完全透明 */
  transform: translateX(100%);    /* 向右偏移 100% */
}

/* 退出动画结束状态 - 向右侧滑出 */
.toast-leave-to {
  opacity: 0;                     /* 完全透明 */
  transform: translateX(100%);    /* 向右偏移 100% */
}

/* 响应式设计 - 移动端适配 */
@media (max-width: 480px) {
  .toast-container {
    top: 10px;                    /* 减少顶部距离 */
    right: 10px;                  /* 减少右侧距离 */
    left: 10px;                   /* 添加左侧距离，让 Toast 在小屏幕上更好显示 */
    max-width: none;              /* 移除最大宽度限制 */
  }
  
  /* 移动端进入动画调整 */
  .toast-enter-from,
  .toast-leave-to {
    transform: translateY(-100%); /* 移动端改为从上方滑入/滑出 */
  }
}
</style>
