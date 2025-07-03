<!--
  基础按钮组件
  
  这是一个高度可定制的基础按钮组件，提供了统一的按钮样式和行为。
  基于 Tailwind CSS 设计系统，支持多种变体、尺寸和状态。
  
  主要特性：
  - 四种按钮变体：primary（主要）、secondary（次要）、danger（危险）、success（成功）
  - 三种尺寸：sm（小）、md（中）、lg（大）
  - 支持禁用状态
  - 无障碍设计（焦点环、键盘导航）
  - 平滑的过渡动画
  - 插槽支持，可自定义按钮内容
  
  设计特点：
  - 圆角矩形设计，现代化外观
  - 悬停和焦点状态的视觉反馈
  - 禁用状态的透明度降低
  - 响应式设计，适配不同屏幕尺寸
  
  使用场景：
  - 表单提交按钮
  - 操作确认按钮
  - 导航按钮
  - 工具栏按钮
  
  @author EcoWiki Team
  @version 1.0.0
  @since 2024-01-01
  
  @example
  <BaseButton variant="primary" size="md" @click="handleSubmit">
    提交表单
  </BaseButton>
  
  <BaseButton variant="danger" size="sm" :disabled="loading">
    删除项目
  </BaseButton>
-->
<template>
  <!-- 基础按钮元素 -->
  <button 
    :class="buttonClass"
    :disabled="disabled"
    @click="$emit('click', $event)"
  >
    <!-- 插槽内容，支持自定义按钮内容 -->
    <slot />
  </button>
</template>

<script setup lang="ts">
/**
 * 基础按钮组件脚本
 * 
 * 实现按钮的样式计算、属性处理和事件发射功能。
 * 使用 Vue 3 Composition API 和 TypeScript 进行类型安全的开发。
 */

import { computed } from 'vue'

/**
 * 组件属性接口定义
 * 定义了按钮组件接受的所有属性及其类型
 */
interface Props {
  /** 按钮变体类型 */
  variant?: 'primary' | 'secondary' | 'danger' | 'success'
  /** 按钮尺寸 */
  size?: 'sm' | 'md' | 'lg'
  /** 是否禁用按钮 */
  disabled?: boolean
}

/**
 * 属性接收和默认值设置
 * 为所有可选属性设置合理的默认值
 */
const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',  // 默认为主要按钮
  size: 'md',          // 默认为中等尺寸
  disabled: false      // 默认为可用状态
})

/**
 * 组件事件定义
 * 定义按钮组件向父组件发送的事件
 */
defineEmits<{
  /** 点击事件 */
  click: [event: MouseEvent]
}>()

/**
 * 计算按钮样式类名
 * 
 * 根据传入的属性动态生成按钮的完整CSS类名。
 * 结合基础样式、变体样式和尺寸样式。
 * 
 * @returns {string} 完整的CSS类名字符串
 */
const buttonClass = computed(() => {
  // 基础样式类 - 适用于所有按钮
  const baseClass = 'inline-flex items-center justify-center rounded-md font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:opacity-50 disabled:pointer-events-none'
  
  // 变体样式映射 - 不同按钮类型的颜色方案
  const variantClass = {
    primary: 'bg-blue-600 text-white hover:bg-blue-700',      // 主要按钮：蓝色背景
    secondary: 'bg-gray-200 text-gray-900 hover:bg-gray-300', // 次要按钮：灰色背景
    danger: 'bg-red-600 text-white hover:bg-red-700',         // 危险按钮：红色背景
    success: 'bg-green-600 text-white hover:bg-green-700'     // 成功按钮：绿色背景
  }
  
  // 尺寸样式映射 - 不同按钮尺寸的高度和内边距
  const sizeClass = {
    sm: 'h-8 px-3 text-sm',    // 小按钮：32px高度，小字体
    md: 'h-10 px-4 py-2',      // 中按钮：40px高度，标准字体
    lg: 'h-12 px-6 text-lg'    // 大按钮：48px高度，大字体
  }
  
  // 组合所有样式类
  return `${baseClass} ${variantClass[props.variant]} ${sizeClass[props.size]}`
})
</script>