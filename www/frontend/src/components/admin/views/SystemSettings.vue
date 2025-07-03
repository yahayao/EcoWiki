<!--
/**
 * 系统设置管理组件
 * 
 * 这是EcoWiki管理后台的系统设置页面，提供核心的系统配置功能。
 * 主要负责系统级别的配置管理和统计信息展示。
 * 
 * 主要功能：
 * - 首页风格切换（简约/经典模式）
 * - 系统统计信息展示
 * - 实时配置变更检测
 * - 美观的统计卡片布局
 * 
 * 设计特点：
 * - 响应式布局，适配各种屏幕尺寸
 * - 实时数据同步和状态管理
 * - 优雅的开关动画效果
 * - 统一的卡片式设计语言
 * 
 * 数据流：
 * - LocalStorage ↔ 首页风格设置
 * - AdminAPI ↔ 系统统计数据
 * - EventBus ↔ 配置变更通知
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0 - 重构为组合式API，添加实时统计
 * @since 2024-04-01
 * @lastModified 2025-07-03
 */
-->
<template>
  <!-- 系统设置主容器 -->
  <div class="admin-settings">
    <!-- 页面标题区域 - 居中布局 -->
    <div class="settings-header-center">
      <h2 class="settings-title">系统设置</h2>
      <p class="settings-subtitle">管理用户权限和系统配置</p>
    </div>

    <!-- 首页风格切换控件 -->
    <div class="home-style-switch-row">
      <label class="switch-label">
        <span>首页风格：</span>
        <!-- 动态显示当前选中的风格 -->
        <span class="switch-text">{{ homeStyle === 'simple' ? '简约首页' : '经典首页' }}</span>
        <!-- 自定义开关组件 -->
        <label class="switch">
          <input type="checkbox" v-model="switchChecked" />
          <span class="slider"></span>
        </label>
      </label>
    </div>

    <!-- 系统统计信息网格布局 -->
    <div class="stats-grid">
      <!-- 总用户数统计卡片 -->
      <div class="stat-card">
        <div class="stat-icon"></div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.totalUsers || 0 }}</div>
          <div class="stat-label">总用户数</div>
        </div>
      </div>
      
      <!-- 活跃用户统计卡片 -->
      <div class="stat-card">
        <div class="stat-icon"></div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.activeUsers || 0 }}</div>
          <div class="stat-label">活跃用户</div>
        </div>
      </div>
      
      <!-- 管理员数量统计卡片 -->
      <div class="stat-card">
        <div class="stat-icon"></div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.adminCount || 0 }}</div>
          <div class="stat-label">管理员</div>
        </div>
      </div>
      
      <!-- 其他统计信息卡片 -->
      <div class="stat-card">
        <div class="stat-icon"></div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.moderatorCount || 0 }}</div>
          <div class="stat-label">版主</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Vue 3 响应式 API 导入
import { ref, onMounted, watch } from 'vue'
// 管理员 API 接口
import { adminApi } from '../../../api/user'
// 管理员用户状态管理
import { useAdminUserStore } from '../../../stores/adminUserStore'
// 消息提示工具
import toast from '../../../utils/toast'

/**
 * 系统统计信息响应式数据
 * 包含总用户数、活跃用户数、管理员数量、版主数量等
 */
const stats = ref<any>({})

/**
 * 系统设置表单数据
 * 包含网站基本信息、功能开关、系统参数等配置
 */
const systemSettings = ref({
  siteName: 'EcoWiki',           // 网站名称
  siteDescription: '知识共享平台', // 网站描述
  allowRegistration: true,       // 是否允许注册
  emailVerification: false,      // 是否启用邮箱验证
  emailNotifications: true,      // 是否启用邮件通知
  maintenanceMode: false,        // 是否开启维护模式
  autoBackup: true,              // 是否自动备份
  maxFileSize: 10,               // 最大文件大小(MB)
  cacheEnabled: true             // 是否启用缓存
})

/**
 * 首页风格设置
 * 管理首页显示风格：经典风格(classic) 或 简约风格(simple)
 */
const homeStyle = ref(localStorage.getItem('homeStyle') || 'classic')
const switchChecked = ref(homeStyle.value === 'simple')

/**
 * 监听首页风格切换开关状态变化
 * 当开关状态改变时，更新风格设置并保存到本地存储
 */
watch(switchChecked, val => {
  const newStyle = val ? 'simple' : 'classic'
  homeStyle.value = newStyle
  localStorage.setItem('homeStyle', newStyle)
  // 触发变更事件，让AdminLayout知道有待处理的变更
  window.dispatchEvent(new Event('ecowiki-admin-pending-changes'))
})

/**
 * 组件挂载完成后的初始化逻辑
 * 设置首页风格初始状态并加载统计信息
 */
onMounted(() => {
  // 延迟执行，确保AdminLayout已经设置了original-homeStyle
  setTimeout(() => {
    const originalStyle = localStorage.getItem('original-homeStyle') || 'classic'
    // 使用原始风格来初始化界面状态，而不是当前的homeStyle
    homeStyle.value = originalStyle
    switchChecked.value = originalStyle === 'simple'
    // 确保localStorage中的homeStyle与显示状态一致
    localStorage.setItem('homeStyle', originalStyle)
  }, 100)
  
  // 加载系统统计信息
  loadStats()
})

/**
 * 获取管理员用户状态管理实例
 * 用于访问和管理管理员用户相关的全局状态
 */
const adminUserStore = useAdminUserStore()

/**
 * 加载系统统计信息
 * 从后端获取用户统计数据并更新界面显示
 */
const loadStats = async () => {
  try {
    const response = await adminApi.getSystemStats()
    if (response.code === 200) {
      stats.value = response.data || {}
    }
  } catch (err: any) {
    console.error('加载统计信息失败:', err)
  }
}
</script>

<style scoped>
/* 系统设置主容器样式 */
.admin-settings {
  padding: 24px;
  background: white;
  border-radius: 0;
  max-width: none;
  margin: 0;
  position: relative;
}

/* 标题和副标题居中样式 */
.settings-header-center {
  text-align: center;
  margin-bottom: 1.5rem;
}

.settings-title {
  font-size: 1.5rem;
  font-weight: bold;
  margin: 0;
}

.settings-subtitle {
  color: #718096;
  font-size: 1rem;
  margin-bottom: 1.5rem;
  margin-top: 0.5rem;
}

/* 首页风格切换和应用按钮同一行布局，应用按钮靠右 */
.settings-action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
  margin-top: -0.5rem;
}

/* 应用按钮样式（固定在右上角） */
.apply-btn-fixed {
  background: #4f8cff;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 0.5rem 1.5rem;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
  z-index: 10;
  position: static;
  margin-left: 1rem;
}

.apply-btn-fixed:disabled {
  background: #b3d1ff;
  cursor: not-allowed;
}

.apply-btn-fixed:not(:disabled):hover {
  background: #2563eb;
}

/* 统计信息网格布局 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

/* 统计信息卡片样式 */
.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 统计图标样式 */
.stat-icon {
  font-size: 2rem;
}

/* 统计数字样式 */
.stat-number {
  font-size: 1.8rem;
  font-weight: bold;
  margin-bottom: 4px;
}

/* 统计标签样式 */
.stat-label {
  font-size: 0.9rem;
  opacity: 0.9;
}

/* 首页风格切换行样式 */
.home-style-switch-row {
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

/* 切换开关标签样式 */
.switch-label {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  font-size: 1.1rem;
}

/* 切换开关文本样式 */
.switch-text {
  min-width: 70px;
  display: inline-block;
  text-align: center;
  color: #4f8cff;
  font-weight: 500;
}

/* 切换开关容器样式 */
.switch {
  position: relative;
  display: inline-block;
  width: 48px;
  height: 24px;
}

/* 隐藏原生checkbox输入框 */
.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

/* 切换开关滑块样式 */
.slider {
  position: absolute;
  cursor: pointer;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: #ccc;
  transition: .4s;
  border-radius: 24px;
}

/* 切换开关滑块内的圆点样式 */
.slider:before {
  position: absolute;
  content: "";
  height: 20px; width: 20px;
  left: 2px; bottom: 2px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
}

/* 切换开关选中状态的滑块背景色 */
.switch input:checked + .slider {
  background-color: #4f8cff;
}

/* 切换开关选中状态的圆点位置 */
.switch input:checked + .slider:before {
  transform: translateX(24px);
}

/* 设置卡片容器样式 */
.settings-card {
  margin-top: 2rem;
  background: #f8fafc;
  border-radius: 12px;
  padding: 24px;
}

/* 设置项网格布局 */
.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

/* 设置项标签样式 */
.setting-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #2d3748;
}

/* 设置项输入框和文本域样式 */
.setting-item input[type="text"],
.setting-item textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 0.9rem;
}

/* 设置项复选框样式 */
.setting-item input[type="checkbox"] {
  margin-right: 8px;
}

/* 加载动画旋转器样式 */
.loading-spinner {
  display: inline-block;
  width: 1em;
  height: 1em;
  border: 2px solid #fff;
  border-top: 2px solid #4f8cff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  vertical-align: middle;
  margin-right: 0.5em;
}

/* 旋转动画关键帧定义 */
@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 移动端响应式样式 */
@media (max-width: 768px) {
  .admin-settings {
    padding: 16px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .settings-grid {
    grid-template-columns: 1fr;
  }
}
</style>