<!-- filepath: c:\Users\Z9495\Desktop\EcoWiki_project\EcoWiki-3\www\frontend\src\components\admin\views\SystemSettings.vue -->
<template>
  <div class="admin-settings">
    <!-- 标题区 居中 -->
    <div class="settings-header-center">
      <h2 class="settings-title">系统设置</h2>
      <p class="settings-subtitle">管理用户权限和系统配置</p>
    </div>

    <!-- 首页风格切换 -->
    <div class="home-style-switch-row">
      <label class="switch-label">
        <span>首页风格：</span>
        <span class="switch-text">{{ homeStyle === 'simple' ? '简约首页' : '经典首页' }}</span>
        <label class="switch">
          <input type="checkbox" v-model="switchChecked" />
          <span class="slider"></span>
        </label>
      </label>
    </div>

    <!-- 统计信息 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon"></div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.totalUsers || 0 }}</div>
          <div class="stat-label">总用户数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon"></div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.activeUsers || 0 }}</div>
          <div class="stat-label">活跃用户</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon"></div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.adminCount || 0 }}</div>
          <div class="stat-label">管理员</div>
        </div>
      </div>
      
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
import { ref, onMounted, watch } from 'vue'
import { adminApi } from '../../../api/user'
import { useAdminUserStore } from '../../../stores/adminUserStore'
import toast from '../../../utils/toast'

// 统计信息
const stats = ref<any>({})

// 系统设置表单
const systemSettings = ref({
  siteName: 'EcoWiki',
  siteDescription: '知识共享平台',
  allowRegistration: true,
  emailVerification: false,
  emailNotifications: true,
  maintenanceMode: false,
  autoBackup: true,
  maxFileSize: 10,
  cacheEnabled: true
})

// 首页风格切换
const homeStyle = ref(localStorage.getItem('homeStyle') || 'classic')
const switchChecked = ref(homeStyle.value === 'simple')

watch(switchChecked, val => {
  const newStyle = val ? 'simple' : 'classic'
  homeStyle.value = newStyle
  localStorage.setItem('homeStyle', newStyle)
  // 触发变更事件，让AdminLayout知道有待处理的变更
  window.dispatchEvent(new Event('ecowiki-admin-pending-changes'))
})

// 监听AdminLayout的初始化完成
onMounted(() => {
  // 延迟一点确保AdminLayout已经设置了original-homeStyle
  setTimeout(() => {
    const originalStyle = localStorage.getItem('original-homeStyle') || 'classic'
    // 使用原始风格来初始化界面状态，而不是当前的homeStyle
    homeStyle.value = originalStyle
    switchChecked.value = originalStyle === 'simple'
    // 确保localStorage中的homeStyle与显示状态一致
    localStorage.setItem('homeStyle', originalStyle)
  }, 100)
  
  loadStats()
})

// 获取用户管理 store
const adminUserStore = useAdminUserStore()

// 加载统计信息
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
.admin-settings {
  padding: 24px;
  background: white;
  border-radius: 0;
  max-width: none;
  margin: 0;
  position: relative;
}

/* 标题和副标题居中 */
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

/* 首页风格切换和应用按钮同一行，按钮靠右 */
.settings-action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
  margin-top: -0.5rem;
}

/* 应用按钮右上角固定 */
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  font-size: 2rem;
}

.stat-number {
  font-size: 1.8rem;
  font-weight: bold;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 0.9rem;
  opacity: 0.9;
}

/* Switch风格 */
.home-style-switch-row {
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.switch-label {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  font-size: 1.1rem;
}

.switch-text {
  min-width: 70px;
  display: inline-block;
  text-align: center;
  color: #4f8cff;
  font-weight: 500;
}

.switch {
  position: relative;
  display: inline-block;
  width: 48px;
  height: 24px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: #ccc;
  transition: .4s;
  border-radius: 24px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 20px; width: 20px;
  left: 2px; bottom: 2px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
}

.switch input:checked + .slider {
  background-color: #4f8cff;
}

.switch input:checked + .slider:before {
  transform: translateX(24px);
}

.settings-card {
  margin-top: 2rem;
  background: #f8fafc;
  border-radius: 12px;
  padding: 24px;
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.setting-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #2d3748;
}

.setting-item input[type="text"],
.setting-item textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 0.9rem;
}

.setting-item input[type="checkbox"] {
  margin-right: 8px;
}

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
@keyframes spin {
  to { transform: rotate(360deg); }
}

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