<template>
  <div class="user-secure">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-info">
          <h1>账号安全</h1>
          <p>管理您的账户安全设置和隐私选项</p>
        </div>
        <div class="security-score">
          <div class="score-circle">
            <div class="score-value">{{ securityScore }}</div>
            <div class="score-label">安全分数</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 安全设置网格 -->
    <div class="security-grid">
      <!-- 密码管理 -->
      <div class="security-card">
        <div class="card-header">
          <svg class="card-icon" viewBox="0 0 24 24">
            <path d="M12,17A2,2 0 0,0 14,15C14,13.89 13.1,13 12,13A2,2 0 0,0 10,15A2,2 0 0,0 12,17M18,8A2,2 0 0,1 20,10V20A2,2 0 0,1 18,22H6A2,2 0 0,1 4,20V10C4,8.89 4.9,8 6,8H7V6A5,5 0 0,1 12,1A5,5 0 0,1 17,6V8H18M12,3A3,3 0 0,0 9,6V8H15V6A3,3 0 0,0 12,3Z"/>
          </svg>
          <h3>密码管理</h3>
          <div class="status-badge" :class="passwordStrengthClass">
            {{ passwordStrengthText }}
          </div>
        </div>
        
        <div class="card-content">
          <div class="form-group">
            <label class="form-label">当前密码</label>
            <div class="input-wrapper">
              <input 
                :type="showCurrentPassword ? 'text' : 'password'"
                v-model="currentPassword"
                class="form-input"
                placeholder="输入当前密码"
              >
              <button 
                type="button"
                @click="showCurrentPassword = !showCurrentPassword"
                class="password-toggle"
              >
                <svg v-if="showCurrentPassword" viewBox="0 0 24 24">
                  <path d="M11.83,9L15,12.16C15,12.11 15,12.05 15,12A3,3 0 0,0 12,9C11.94,9 11.89,9 11.83,9M7.53,9.8L9.08,11.35C9.03,11.56 9,11.77 9,12A3,3 0 0,0 12,15C12.22,15 12.44,14.97 12.65,14.92L14.2,16.47C13.53,16.8 12.79,17 12,17A5,5 0 0,1 7,12C7,11.21 7.2,10.47 7.53,9.8M2,4.27L4.28,6.55L4.73,7C3.08,8.3 1.78,10 1,12C2.73,16.39 7,19.5 12,19.5C13.55,19.5 15.03,19.2 16.38,18.66L16.81,19.09L19.73,22L21,20.73L3.27,3M12,7A5,5 0 0,1 17,12C17,12.64 16.87,13.26 16.64,13.82L19.57,16.75C21.07,15.5 22.27,13.86 23,12C21.27,7.61 17,4.5 12,4.5C10.6,4.5 9.26,4.75 8,5.2L10.17,7.35C10.76,7.13 11.37,7 12,7Z"/>
                </svg>
                <svg v-else viewBox="0 0 24 24">
                  <path d="M12,9A3,3 0 0,0 9,12A3,3 0 0,0 12,15A3,3 0 0,0 15,12A3,3 0 0,0 12,9M12,17A5,5 0 0,1 7,12A5,5 0 0,1 12,7A5,5 0 0,1 17,12A5,5 0 0,1 12,17M12,4.5C7,4.5 2.73,7.61 1,12C2.73,16.39 7,19.5 12,19.5C17,19.5 21.27,16.39 23,12C21.27,7.61 17,4.5 12,4.5Z"/>
                </svg>
              </button>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">新密码</label>
            <div class="input-wrapper">
              <input 
                :type="showNewPassword ? 'text' : 'password'"
                v-model="newPassword"
                class="form-input"
                placeholder="输入新密码"
                @input="checkPasswordStrength"
              >
              <button 
                type="button"
                @click="showNewPassword = !showNewPassword"
                class="password-toggle"
              >
                <svg v-if="showNewPassword" viewBox="0 0 24 24">
                  <path d="M11.83,9L15,12.16C15,12.11 15,12.05 15,12A3,3 0 0,0 12,9C11.94,9 11.89,9 11.83,9M7.53,9.8L9.08,11.35C9.03,11.56 9,11.77 9,12A3,3 0 0,0 12,15C12.22,15 12.44,14.97 12.65,14.92L14.2,16.47C13.53,16.8 12.79,17 12,17A5,5 0 0,1 7,12C7,11.21 7.2,10.47 7.53,9.8M2,4.27L4.28,6.55L4.73,7C3.08,8.3 1.78,10 1,12C2.73,16.39 7,19.5 12,19.5C13.55,19.5 15.03,19.2 16.38,18.66L16.81,19.09L19.73,22L21,20.73L3.27,3M12,7A5,5 0 0,1 17,12C17,12.64 16.87,13.26 16.64,13.82L19.57,16.75C21.07,15.5 22.27,13.86 23,12C21.27,7.61 17,4.5 12,4.5C10.6,4.5 9.26,4.75 8,5.2L10.17,7.35C10.76,7.13 11.37,7 12,7Z"/>
                </svg>
                <svg v-else viewBox="0 0 24 24">
                  <path d="M12,9A3,3 0 0,0 9,12A3,3 0 0,0 12,15A3,3 0 0,0 15,12A3,3 0 0,0 12,9M12,17A5,5 0 0,1 7,12A5,5 0 0,1 12,7A5,5 0 0,1 17,12A5,5 0 0,1 12,17M12,4.5C7,4.5 2.73,7.61 1,12C2.73,16.39 7,19.5 12,19.5C17,19.5 21.27,16.39 23,12C21.27,7.61 17,4.5 12,4.5Z"/>
                </svg>
              </button>
            </div>
            <div class="password-strength" v-if="newPassword">
              <div class="strength-bar">
                <div class="strength-fill" :style="{ width: passwordStrength + '%', backgroundColor: passwordStrengthColor }"></div>
              </div>
              <div class="strength-text">{{ passwordStrengthText }}</div>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">确认新密码</label>
            <div class="input-wrapper">
              <input 
                :type="showConfirmPassword ? 'text' : 'password'"
                v-model="confirmPassword"
                class="form-input"
                placeholder="再次输入新密码"
              >
              <button 
                type="button"
                @click="showConfirmPassword = !showConfirmPassword"
                class="password-toggle"
              >
                <svg v-if="showConfirmPassword" viewBox="0 0 24 24">
                  <path d="M11.83,9L15,12.16C15,12.11 15,12.05 15,12A3,3 0 0,0 12,9C11.94,9 11.89,9 11.83,9M7.53,9.8L9.08,11.35C9.03,11.56 9,11.77 9,12A3,3 0 0,0 12,15C12.22,15 12.44,14.97 12.65,14.92L14.2,16.47C13.53,16.8 12.79,17 12,17A5,5 0 0,1 7,12C7,11.21 7.2,10.47 7.53,9.8M2,4.27L4.28,6.55L4.73,7C3.08,8.3 1.78,10 1,12C2.73,16.39 7,19.5 12,19.5C13.55,19.5 15.03,19.2 16.38,18.66L16.81,19.09L19.73,22L21,20.73L3.27,3M12,7A5,5 0 0,1 17,12C17,12.64 16.87,13.26 16.64,13.82L19.57,16.75C21.07,15.5 22.27,13.86 23,12C21.27,7.61 17,4.5 12,4.5C10.6,4.5 9.26,4.75 8,5.2L10.17,7.35C10.76,7.13 11.37,7 12,7Z"/>
                </svg>
                <svg v-else viewBox="0 0 24 24">
                  <path d="M12,9A3,3 0 0,0 9,12A3,3 0 0,0 12,15A3,3 0 0,0 15,12A3,3 0 0,0 12,9M12,17A5,5 0 0,1 7,12A5,5 0 0,1 12,7A5,5 0 0,1 17,12A5,5 0 0,1 12,17M12,4.5C7,4.5 2.73,7.61 1,12C2.73,16.39 7,19.5 12,19.5C17,19.5 21.27,16.39 23,12C21.27,7.61 17,4.5 12,4.5Z"/>
                </svg>
              </button>
            </div>
            <div v-if="confirmPassword && newPassword !== confirmPassword" class="error-message">
              密码不匹配
            </div>
          </div>

          <button @click="updatePassword" class="btn btn-primary" :disabled="!canUpdatePassword">
            <svg class="btn-icon" viewBox="0 0 24 24">
              <path d="M21,7L9,19L3.5,13.5L4.91,12.09L9,16.17L19.59,5.59L21,7Z"/>
            </svg>
            更新密码
          </button>
        </div>
      </div>

      <!-- 双因素认证 -->
      <div class="security-card">
        <div class="card-header">
          <svg class="card-icon" viewBox="0 0 24 24">
            <path d="M12,1L3,5V11C3,16.55 6.84,21.74 12,23C17.16,21.74 21,16.55 21,11V5L12,1M12,7C13.4,7 14.8,8.6 14.8,10V11.5C15.4,11.5 16,12.1 16,12.7V16.2C16,16.8 15.4,17.3 14.8,17.3H9.2C8.6,17.3 8,16.8 8,16.2V12.8C8,12.2 8.6,11.7 9.2,11.7V10.1C9.2,8.6 10.6,7 12,7M12,8.2C11.2,8.2 10.5,8.7 10.5,9.5V11.5H13.5V9.5C13.5,8.7 12.8,8.2 12,8.2Z"/>
          </svg>
          <h3>双因素认证</h3>
          <div class="status-badge" :class="twoFactorEnabled ? 'enabled' : 'disabled'">
            {{ twoFactorEnabled ? '已启用' : '已禁用' }}
          </div>
        </div>

        <div class="card-content">
          <p class="card-description">
            双因素认证为您的账户提供额外的安全保护层。
          </p>
          
          <div v-if="!twoFactorEnabled" class="two-factor-setup">
            <div class="setup-steps">
              <div class="step">
                <div class="step-number">1</div>
                <div class="step-content">
                  <h4>下载认证器应用</h4>
                  <p>推荐使用 Google Authenticator 或 Microsoft Authenticator</p>
                </div>
              </div>
              <div class="step">
                <div class="step-number">2</div>
                <div class="step-content">
                  <h4>扫描二维码</h4>
                  <div class="qr-code">
                    <div class="qr-placeholder">
                      <svg viewBox="0 0 24 24">
                        <path d="M3,11H5V13H3V11M11,5H13V9H11V5M9,11H13V15H9V11M15,11H17V13H15V11M19,11H21V13H19V11M12,15H14V17H12V15M3,5H9V9H3V5M5,7V7H7V7H5V7M3,15H9V19H3V15M5,17V17H7V17H5V17M15,5H21V9H15V5M17,7V7H19V7H17V7Z"/>
                      </svg>
                      <p>二维码将在此显示</p>
                    </div>
                  </div>
                </div>
              </div>
              <div class="step">
                <div class="step-number">3</div>
                <div class="step-content">
                  <h4>输入验证码</h4>
                  <div class="verification-input">
                    <input 
                      v-model="verificationCode"
                      class="form-input"
                      placeholder="输入6位验证码"
                      maxlength="6"
                    >
                    <button @click="enableTwoFactor" class="btn btn-success">
                      启用双因素认证
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="two-factor-enabled">
            <div class="enabled-info">
              <svg class="check-icon" viewBox="0 0 24 24">
                <path d="M21,7L9,19L3.5,13.5L4.91,12.09L9,16.17L19.59,5.59L21,7Z"/>
              </svg>
              <div>
                <h4>双因素认证已启用</h4>
                <p>您的账户受到双因素认证保护</p>
              </div>
            </div>
            
            <div class="backup-codes">
              <h4>备用恢复代码</h4>
              <p>请将这些代码保存在安全的地方。当您无法使用认证器应用时，可以使用这些代码。</p>
              <div class="codes-grid">
                <div v-for="code in backupCodes" :key="code" class="backup-code">
                  {{ code }}
                </div>
              </div>
              <div class="codes-actions">
                <button @click="generateNewCodes" class="btn btn-secondary">
                  生成新代码
                </button>
                <button @click="disableTwoFactor" class="btn btn-warning">
                  禁用双因素认证
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 隐私设置 -->
      <div class="security-card">
        <div class="card-header">
          <svg class="card-icon" viewBox="0 0 24 24">
            <path d="M12,12C10.11,12 8.5,10.39 8.5,8.5C8.5,6.61 10.11,5 12,5C13.89,5 15.5,6.61 15.5,8.5C15.5,10.39 13.89,12 12,12M12,14C16.42,14 20,15.79 20,18V20H4V18C4,15.79 7.58,14 12,14Z"/>
          </svg>
          <h3>隐私设置</h3>
        </div>

        <div class="card-content">
          <div class="privacy-options">
            <div class="privacy-item">
              <div class="privacy-info">
                <h4>公开个人资料</h4>
                <p>允许其他用户查看您的个人资料信息</p>
              </div>
              <label class="toggle-switch">
                <input type="checkbox" v-model="privacySettings.publicProfile">
                <span class="toggle-slider"></span>
              </label>
            </div>

            <div class="privacy-item">
              <div class="privacy-info">
                <h4>显示邮箱地址</h4>
                <p>在个人资料中显示您的邮箱地址</p>
              </div>
              <label class="toggle-switch">
                <input type="checkbox" v-model="privacySettings.showEmail">
                <span class="toggle-slider"></span>
              </label>
            </div>

            <div class="privacy-item">
              <div class="privacy-info">
                <h4>公开贡献记录</h4>
                <p>允许其他用户查看您的文章贡献历史</p>
              </div>
              <label class="toggle-switch">
                <input type="checkbox" v-model="privacySettings.showContributions">
                <span class="toggle-slider"></span>
              </label>
            </div>

            <div class="privacy-item">
              <div class="privacy-info">
                <h4>活动状态</h4>
                <p>显示您的在线状态和最后活动时间</p>
              </div>
              <label class="toggle-switch">
                <input type="checkbox" v-model="privacySettings.showActivity">
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>

          <button @click="updatePrivacySettings" class="btn btn-primary">
            <svg class="btn-icon" viewBox="0 0 24 24">
              <path d="M21,7L9,19L3.5,13.5L4.91,12.09L9,16.17L19.59,5.59L21,7Z"/>
            </svg>
            保存隐私设置
          </button>
        </div>
      </div>

      <!-- 登录设备 -->
      <div class="security-card">
        <div class="card-header">
          <svg class="card-icon" viewBox="0 0 24 24">
            <path d="M17,19H7V5H17M17,1H7C5.89,1 5,1.89 5,3V21C5,22.1 5.9,23 7,23H17C18.1,23 19,22.1 19,21V3C19,1.89 18.1,1 17,1Z"/>
          </svg>
          <h3>登录设备</h3>
          <div class="device-count">{{ loginDevices.length }} 台设备</div>
        </div>

        <div class="card-content">
          <div class="devices-list">
            <div v-for="device in loginDevices" :key="device.id" class="device-item">
              <div class="device-icon">
                <svg v-if="device.type === 'desktop'" viewBox="0 0 24 24">
                  <path d="M21,16H3V4H21M21,2H3C1.89,2 1,2.89 1,4V16A2,2 0 0,0 3,18H10V20H8V22H16V20H14V18H21A2,2 0 0,0 23,16V4C23,2.89 22.1,2 21,2Z"/>
                </svg>
                <svg v-else-if="device.type === 'mobile'" viewBox="0 0 24 24">
                  <path d="M17,19H7V5H17M17,1H7C5.89,1 5,1.89 5,3V21C5,22.1 5.9,23 7,23H17C18.1,23 19,22.1 19,21V3C19,1.89 18.1,1 17,1Z"/>
                </svg>
                <svg v-else viewBox="0 0 24 24">
                  <path d="M4,6H20V16H4M20,18A2,2 0 0,0 22,16V6C22,4.89 21.1,4 20,4H4C2.89,4 2,4.89 2,6V16A2,2 0 0,0 4,18H0V20H24V18H20Z"/>
                </svg>
              </div>
              <div class="device-info">
                <h4>{{ device.name }}</h4>
                <p>{{ device.browser }} • {{ device.os }}</p>
                <div class="device-details">
                  <span class="device-location">{{ device.location }}</span>
                  <span class="device-time">{{ device.lastSeen }}</span>
                  <span v-if="device.current" class="current-device">当前设备</span>
                </div>
              </div>
              <button 
                v-if="!device.current"
                @click="removeDevice(device.id)"
                class="btn btn-outline-danger"
              >
                移除
              </button>
            </div>
          </div>

          <button @click="removeAllDevices" class="btn btn-warning">
            移除所有其他设备
          </button>
        </div>
      </div>

      <!-- 安全日志 -->
      <div class="security-card full-width">
        <div class="card-header">
          <svg class="card-icon" viewBox="0 0 24 24">
            <path d="M13,9H18.5L13,3.5V9M6,2H14L20,8V20A2,2 0 0,1 18,22H6C4.89,22 4,21.1 4,20V4C4,2.89 4.89,2 6,2M15,18V16H6V18H15M18,14V12H6V14H18Z"/>
          </svg>
          <h3>安全日志</h3>
          <button @click="exportSecurityLog" class="btn btn-outline">
            导出日志
          </button>
        </div>

        <div class="card-content">
          <div class="log-filters">
            <select v-model="logFilter" class="form-select">
              <option value="all">所有活动</option>
              <option value="login">登录活动</option>
              <option value="password">密码变更</option>
              <option value="security">安全设置</option>
            </select>
            <input 
              type="date"
              v-model="logDateFilter"
              class="form-input"
            >
          </div>

          <div class="security-log">
            <div v-for="log in filteredSecurityLogs" :key="log.id" class="log-item">
              <div class="log-icon" :class="log.type">
                <svg v-if="log.type === 'login'" viewBox="0 0 24 24">
                  <path d="M10,17V14H3V10H10V7L15,12L10,17M10,2H19A2,2 0 0,1 21,4V20A2,2 0 0,1 19,22H10A2,2 0 0,1 8,20V18H10V20H19V4H10V6H8V4A2,2 0 0,1 10,2Z"/>
                </svg>
                <svg v-else-if="log.type === 'password'" viewBox="0 0 24 24">
                  <path d="M12,17A2,2 0 0,0 14,15C14,13.89 13.1,13 12,13A2,2 0 0,0 10,15A2,2 0 0,0 12,17M18,8A2,2 0 0,1 20,10V20A2,2 0 0,1 18,22H6A2,2 0 0,1 4,20V10C4,8.89 4.9,8 6,8H7V6A5,5 0 0,1 12,1A5,5 0 0,1 17,6V8H18M12,3A3,3 0 0,0 9,6V8H15V6A3,3 0 0,0 12,3Z"/>
                </svg>
                <svg v-else viewBox="0 0 24 24">
                  <path d="M12,1L3,5V11C3,16.55 6.84,21.74 12,23C17.16,21.74 21,16.55 21,11V5L12,1M12,7C13.4,7 14.8,8.6 14.8,10V11.5C15.4,11.5 16,12.1 16,12.7V16.2C16,16.8 15.4,17.3 14.8,17.3H9.2C8.6,17.3 8,16.8 8,16.2V12.8C8,12.2 8.6,11.7 9.2,11.7V10.1C9.2,8.6 10.6,7 12,7M12,8.2C11.2,8.2 10.5,8.7 10.5,9.5V11.5H13.5V9.5C13.5,8.7 12.8,8.2 12,8.2Z"/>
                </svg>
              </div>
              <div class="log-content">
                <div class="log-action">{{ log.action }}</div>
                <div class="log-details">
                  <span class="log-time">{{ log.timestamp }}</span>
                  <span class="log-ip">IP: {{ log.ipAddress }}</span>
                  <span class="log-location">{{ log.location }}</span>
                </div>
              </div>
              <div class="log-status" :class="log.status">
                {{ log.status === 'success' ? '成功' : '失败' }}
              </div>
            </div>
          </div>

          <div class="log-pagination">
            <button 
              @click="loadMoreLogs"
              class="btn btn-outline"
              :disabled="loadingLogs"
            >
              {{ loadingLogs ? '加载中...' : '加载更多' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useAuth } from '@/composables/useAuth'

const { user } = useAuth()

// 安全分数
const securityScore = ref(85)

// 密码管理
const currentPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const showCurrentPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)
const passwordStrength = ref(0)

// 双因素认证
const twoFactorEnabled = ref(false)
const verificationCode = ref('')
const backupCodes = ref([
  'ABC123DEF',
  'GHI456JKL',
  'MNO789PQR',
  'STU012VWX',
  'YZA345BCD',
  'EFG678HIJ'
])

// 隐私设置
const privacySettings = reactive({
  publicProfile: true,
  showEmail: false,
  showContributions: true,
  showActivity: true
})

// 登录设备
const loginDevices = ref([
  {
    id: 1,
    name: 'Windows PC',
    type: 'desktop',
    browser: 'Chrome 119',
    os: 'Windows 11',
    location: '北京, 中国',
    lastSeen: '刚刚',
    current: true
  },
  {
    id: 2,
    name: 'iPhone 15',
    type: 'mobile',
    browser: 'Safari',
    os: 'iOS 17',
    location: '上海, 中国',
    lastSeen: '2小时前',
    current: false
  },
  {
    id: 3,
    name: 'MacBook Pro',
    type: 'desktop',
    browser: 'Firefox 120',
    os: 'macOS 14',
    location: '深圳, 中国',
    lastSeen: '昨天',
    current: false
  }
])

// 安全日志
const securityLogs = ref([
  {
    id: 1,
    type: 'login',
    action: '登录成功',
    timestamp: '2024-01-15 14:30:25',
    ipAddress: '192.168.1.100',
    location: '北京, 中国',
    status: 'success'
  },
  {
    id: 2,
    type: 'password',
    action: '密码修改',
    timestamp: '2024-01-14 10:15:00',
    ipAddress: '192.168.1.100',
    location: '北京, 中国',
    status: 'success'
  },
  {
    id: 3,
    type: 'security',
    action: '启用双因素认证',
    timestamp: '2024-01-13 16:45:30',
    ipAddress: '192.168.1.100',
    location: '北京, 中国',
    status: 'success'
  },
  {
    id: 4,
    type: 'login',
    action: '登录失败',
    timestamp: '2024-01-13 09:20:12',
    ipAddress: '203.0.113.42',
    location: '未知',
    status: 'failed'
  }
])

const logFilter = ref('all')
const logDateFilter = ref('')
const loadingLogs = ref(false)

// 计算属性
const passwordStrengthColor = computed(() => {
  if (passwordStrength.value < 30) return '#e74c3c'
  if (passwordStrength.value < 60) return '#f39c12'
  if (passwordStrength.value < 80) return '#f1c40f'
  return '#27ae60'
})

const passwordStrengthText = computed(() => {
  if (passwordStrength.value < 30) return '弱'
  if (passwordStrength.value < 60) return '中等'
  if (passwordStrength.value < 80) return '强'
  return '很强'
})

const passwordStrengthClass = computed(() => {
  return passwordStrengthText.value.toLowerCase()
})

const canUpdatePassword = computed(() => {
  return currentPassword.value && 
         newPassword.value && 
         confirmPassword.value && 
         newPassword.value === confirmPassword.value &&
         passwordStrength.value >= 60
})

const filteredSecurityLogs = computed(() => {
  let filtered = securityLogs.value

  if (logFilter.value !== 'all') {
    filtered = filtered.filter(log => log.type === logFilter.value)
  }

  if (logDateFilter.value) {
    filtered = filtered.filter(log => 
      log.timestamp.startsWith(logDateFilter.value)
    )
  }

  return filtered
})

// 方法
const checkPasswordStrength = () => {
  const password = newPassword.value
  let strength = 0

  if (password.length >= 8) strength += 20
  if (password.length >= 12) strength += 10
  if (/[a-z]/.test(password)) strength += 15
  if (/[A-Z]/.test(password)) strength += 15
  if (/[0-9]/.test(password)) strength += 15
  if (/[^A-Za-z0-9]/.test(password)) strength += 25

  passwordStrength.value = Math.min(strength, 100)
}

const updatePassword = () => {
  console.log('更新密码')
  // 功能规划：集成后端密码更新API
}

const enableTwoFactor = () => {
  console.log('启用双因素认证')
  twoFactorEnabled.value = true
  verificationCode.value = ''
  // 功能规划：集成双因素认证服务
}

const disableTwoFactor = () => {
  console.log('禁用双因素认证')
  twoFactorEnabled.value = false
  // 功能规划：集成双因素认证禁用接口
}

const generateNewCodes = () => {
  console.log('生成新的备用代码')
  // 功能规划：实现备用代码生成逻辑
}

const updatePrivacySettings = () => {
  console.log('更新隐私设置', privacySettings)
  // 功能规划：连接用户隐私设置API
}

const removeDevice = (deviceId: number) => {
  console.log('移除设备', deviceId)
  loginDevices.value = loginDevices.value.filter(device => device.id !== deviceId)
  // 功能规划：实现设备会话管理
}

const removeAllDevices = () => {
  console.log('移除所有其他设备')
  loginDevices.value = loginDevices.value.filter(device => device.current)
  // 功能规划：批量会话清理功能
}

const exportSecurityLog = () => {
  console.log('导出安全日志')
  // 功能规划：安全日志导出服务
}

const loadMoreLogs = () => {
  loadingLogs.value = true
  // 模拟加载更多日志
  setTimeout(() => {
    loadingLogs.value = false
  }, 1000)
  // 功能规划：分页加载安全日志
}
</script>

<style scoped>
.user-secure {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 24px;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 32px;
  margin-bottom: 32px;
  color: white;
  box-shadow: 0 20px 40px rgba(102, 126, 234, 0.3);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 24px;
}

.header-info h1 {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 8px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-info p {
  font-size: 1.1rem;
  opacity: 0.9;
  margin: 0;
}

.security-score {
  display: flex;
  align-items: center;
  justify-content: center;
}

.score-circle {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  width: 100px;
  height: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.score-value {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 4px;
}

.score-label {
  font-size: 0.8rem;
  opacity: 0.8;
}

.security-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
}

.security-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.security-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.security-card.full-width {
  grid-column: 1 / -1;
}

.card-header {
  background: linear-gradient(135deg, #f8f9ff 0%, #e8eaff 100%);
  padding: 24px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-icon {
  width: 24px;
  height: 24px;
  fill: #667eea;
}

.card-header h3 {
  flex: 1;
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #2d3748;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-badge.enabled {
  background: #c6f6d5;
  color: #2f855a;
}

.status-badge.disabled {
  background: #fed7d7;
  color: #c53030;
}

.device-count {
  background: #e2e8f0;
  color: #4a5568;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.card-content {
  padding: 24px;
}

.card-description {
  color: #718096;
  margin-bottom: 20px;
  line-height: 1.6;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #4a5568;
}

.form-input,
.form-select {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-wrapper {
  position: relative;
}

.password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.2s ease;
}

.password-toggle:hover {
  background: #f7fafc;
}

.password-toggle svg {
  width: 20px;
  height: 20px;
  fill: #718096;
}

.password-strength {
  margin-top: 8px;
}

.strength-bar {
  height: 4px;
  background: #e2e8f0;
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 4px;
}

.strength-fill {
  height: 100%;
  transition: all 0.3s ease;
}

.strength-text {
  font-size: 0.8rem;
  color: #718096;
}

.error-message {
  color: #e53e3e;
  font-size: 0.875rem;
  margin-top: 4px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  font-size: 0.95rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: #e2e8f0;
  color: #4a5568;
}

.btn-secondary:hover {
  background: #cbd5e0;
}

.btn-success {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(72, 187, 120, 0.3);
}

.btn-success:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(72, 187, 120)
}

.btn-warning {
  background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(237, 137, 54, 0.3);
}

.btn-warning:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(237, 137, 54, 0.4);
}

.btn-outline {
  background: transparent;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-outline:hover {
  background: #667eea;
  color: white;
}

.btn-outline-danger {
  background: transparent;
  color: #e53e3e;
  border: 2px solid #e53e3e;
  padding: 8px 16px;
  font-size: 0.875rem;
}

.btn-outline-danger:hover {
  background: #e53e3e;
  color: white;
}

.btn-icon {
  width: 18px;
  height: 18px;
  fill: currentColor;
}

/* 其他组件的样式... */
.toggle-switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 24px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #cbd5e0;
  transition: 0.3s;
  border-radius: 24px;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.3s;
  border-radius: 50%;
}

input:checked + .toggle-slider {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

input:checked + .toggle-slider:before {
  transform: translateX(26px);
}

.privacy-options {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.privacy-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.privacy-info h4 {
  margin: 0 0 4px 0;
  color: #2d3748;
  font-size: 1rem;
}

.privacy-info p {
  margin: 0;
  color: #718096;
  font-size: 0.875rem;
}

/* 设备和日志相关样式 */
.devices-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.device-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.device-icon {
  width: 32px;
  height: 32px;
  padding: 6px;
  background: #e2e8f0;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.device-icon svg {
  width: 20px;
  height: 20px;
  fill: #4a5568;
}

.device-info {
  flex: 1;
}

.device-info h4 {
  margin: 0 0 4px 0;
  color: #2d3748;
  font-size: 1rem;
  font-weight: 600;
}

.device-info p {
  margin: 0 0 8px 0;
  color: #718096;
  font-size: 0.875rem;
}

.device-details {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.device-location,
.device-time {
  color: #a0aec0;
  font-size: 0.8rem;
}

.current-device {
  background: #c6f6d5;
  color: #2f855a;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 500;
}

.log-filters {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.log-filters .form-select,
.log-filters .form-input {
  flex: 1;
  min-width: 200px;
}

.security-log {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.log-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.log-icon {
  width: 32px;
  height: 32px;
  padding: 6px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.log-icon svg {
  width: 20px;
  height: 20px;
}

.log-icon.login {
  background: #e6fffa;
}

.log-icon.login svg {
  fill: #319795;
}

.log-icon.password {
  background: #fff5f5;
}

.log-icon.password svg {
  fill: #e53e3e;
}

.log-icon.security {
  background: #f0fff4;
}

.log-icon.security svg {
  fill: #38a169;
}

.log-content {
  flex: 1;
}

.log-action {
  color: #2d3748;
  font-weight: 600;
  margin-bottom: 4px;
}

.log-details {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.log-time,
.log-ip,
.log-location {
  color: #a0aec0;
  font-size: 0.8rem;
}

.log-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.log-status.success {
  background: #c6f6d5;
  color: #2f855a;
}

.log-status.failed {
  background: #fed7d7;
  color: #c53030;
}

.log-pagination {
  text-align: center;
}

.two-factor-setup {
  margin-top: 16px;
}

.setup-steps {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.step {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.step-number {
  width: 32px;
  height: 32px;
  background: #667eea;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 0.875rem;
  flex-shrink: 0;
}

.step-content {
  flex: 1;
}

.step-content h4 {
  margin: 0 0 8px 0;
  color: #2d3748;
  font-size: 1rem;
}

.step-content p {
  margin: 0 0 12px 0;
  color: #718096;
  font-size: 0.875rem;
}

.qr-code {
  margin: 12px 0;
}

.qr-placeholder {
  width: 200px;
  height: 200px;
  background: #f7fafc;
  border: 2px dashed #cbd5e0;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.qr-placeholder svg {
  width: 48px;
  height: 48px;
  fill: #a0aec0;
}

.qr-placeholder p {
  color: #a0aec0;
  font-size: 0.875rem;
  margin: 0;
}

.verification-input {
  display: flex;
  gap: 12px;
  align-items: center;
}

.verification-input .form-input {
  max-width: 200px;
}

.two-factor-enabled {
  margin-top: 16px;
}

.enabled-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f0fff4;
  border-radius: 12px;
  margin-bottom: 24px;
}

.check-icon {
  width: 24px;
  height: 24px;
  fill: #38a169;
}

.enabled-info h4 {
  margin: 0 0 4px 0;
  color: #2d3748;
}

.enabled-info p {
  margin: 0;
  color: #718096;
  font-size: 0.875rem;
}

.backup-codes h4 {
  margin: 0 0 8px 0;
  color: #2d3748;
}

.backup-codes p {
  margin: 0 0 16px 0;
  color: #718096;
  font-size: 0.875rem;
  line-height: 1.5;
}

.codes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.backup-code {
  padding: 8px 12px;
  background: #f7fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.875rem;
  text-align: center;
  color: #4a5568;
}

.codes-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .security-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .user-secure {
    padding: 16px;
  }
  
  .page-header {
    padding: 24px;
  }
  
  .header-content {
    flex-direction: column;
    text-align: center;
  }
  
  .header-info h1 {
    font-size: 2rem;
  }
  
  .security-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .card-header {
    padding: 20px;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .card-content {
    padding: 20px;
  }
  
  .privacy-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.security-card {
  animation: fadeInUp 0.6s ease-out;
}
</style>