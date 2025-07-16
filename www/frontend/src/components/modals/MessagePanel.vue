<!--
  消息通知界面组件
  
  提供用户消息的查看、发送、管理功能
  支持消息列表、对话功能、未读提醒等
-->
<template>
  <div class="message-panel">
    <!-- 消息面板头部 -->
    <div class="message-header">
      <div class="header-left">
        <div class="header-icon-wrapper">
          <IconMessage class="header-icon" />
        </div>
        <div class="header-title-section">
          <h2 class="panel-title">消息中心</h2>
          <p class="panel-subtitle">与用户的交流沟通</p>
        </div>
        <div class="unread-badge" v-if="unreadCount > 0">
          {{ unreadCount > 99 ? '99+' : unreadCount }}
        </div>
      </div>
      <div class="header-actions">
        <button 
          class="action-btn secondary" 
          @click="markAllAsRead"
          :disabled="unreadCount === 0"
          title="标记全部已读"
        >
          <IconCheck />
          <span>全部已读</span>
        </button>
        <button class="action-btn primary" @click="showSendDialog = true" title="发送消息">
          <IconSend />
          <span>发送消息</span>
        </button>
        <button class="close-btn" @click="closePanel" title="关闭">
          <IconClose />
        </button>
      </div>
    </div>

    <!-- 消息筛选标签 -->
    <div class="message-tabs">
      <div class="tabs-container">
        <button 
          v-for="(tab, index) in tabs" 
          :key="tab.key"
          class="tab-button"
          :class="{ 
            active: activeTab === tab.key,
            'drag-over': dragOverIndex === index,
            'dragging': draggedTabIndex === index
          }"
          @click="activeTab = tab.key"
          @dragstart="handleDragStart($event, index)"
          @dragenter="handleDragEnter(index)"
          @dragleave="handleDragLeave"
          @dragover.prevent="handleDragOver"
          @drop="handleDrop($event, index)"
          @dragend="handleDragEnd"
          draggable="true"
        >
          <span class="tab-label">{{ tab.label }}</span>
          <span v-if="tab.key === 'unread' && unreadCount > 0" class="tab-badge">
            {{ unreadCount > 99 ? '99+' : unreadCount }}
          </span>
        </button>
      </div>
    </div>

    <!-- 消息列表 -->
    <div class="message-list" v-if="!loading">
      <div v-if="messages.length === 0" class="empty-state">
        <IconMessage class="empty-icon" />
        <p>暂无消息</p>
      </div>
      <div 
        v-for="message in messages" 
        :key="message.messageId"
        class="message-item"
        :class="{ 
          'unread': message.status === '未读',
          'sent': message.senderUserId === currentUserId,
          'expanded': expandedMessage === message.messageId
        }"
        @click="toggleMessageExpansion(message.messageId)"
      >
        <div class="message-avatar">
          <img :src="getAvatarUrl(message)" :alt="getDisplayName(message)" />
        </div>
        <div class="message-content">
          <div class="message-header-info">
            <span class="sender-name">{{ getDisplayName(message) }}</span>
            <span class="message-time">{{ formatTime(message.sendTime) }}</span>
          </div>
          <div class="message-text" :class="{ 'expanded': expandedMessage === message.messageId }">
            {{ message.content }}
          </div>
          <div class="message-status" v-if="message.status === '未读' && message.recipientUserId === currentUserId">
            <span class="unread-dot"></span>
            未读
          </div>
          
          <!-- 展开状态下的详细信息 -->
          <div v-if="expandedMessage === message.messageId" class="message-details">
            <div class="detail-row">
              <span class="detail-label">发送者：</span>
              <span class="detail-value">{{ message.senderUsername || '未知用户' }}</span>
            </div>
            <div class="detail-row" v-if="message.recipientUserId === currentUserId">
              <span class="detail-label">接收者：</span>
              <span class="detail-value">{{ message.recipientUsername || '我' }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">发送时间：</span>
              <span class="detail-value">{{ new Date(message.sendTime).toLocaleString() }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">状态：</span>
              <span class="detail-value" :class="message.status === '未读' ? 'status-unread' : 'status-read'">
                {{ message.status }}
              </span>
            </div>
            <div class="message-actions-expanded">
              <button 
                class="action-btn-expanded" 
                @click.stop="markAsRead(message.messageId)"
                v-if="message.status === '未读' && message.recipientUserId === currentUserId"
              >
                <IconCheck />
                标记已读
              </button>
              <button 
                class="action-btn-expanded delete-btn" 
                @click.stop="deleteMessage(message.messageId)"
              >
                <IconTrash />
                删除消息
              </button>
            </div>
          </div>
        </div>
        <div class="message-actions" v-if="expandedMessage !== message.messageId">
          <button 
            class="action-icon-btn" 
            @click.stop="markAsRead(message.messageId)"
            v-if="message.status === '未读' && message.recipientUserId === currentUserId"
            title="标记已读"
          >
            <IconCheck />
          </button>
          <button 
            class="action-icon-btn" 
            @click.stop="deleteMessage(message.messageId)"
            title="删除消息"
          >
            <IconTrash />
          </button>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 分页控件 -->
    <div class="pagination" v-if="totalPages > 1">
      <button 
        class="page-btn" 
        @click="changePage(currentPage - 1)"
        :disabled="currentPage === 0"
      >
        上一页
      </button>
      <span class="page-info">
        第 {{ currentPage + 1 }} 页，共 {{ totalPages }} 页
      </span>
      <button 
        class="page-btn" 
        @click="changePage(currentPage + 1)"
        :disabled="currentPage >= totalPages - 1"
      >
        下一页
      </button>
    </div>

    <!-- 发送消息对话框 -->
    <div v-if="showSendDialog" class="modal-overlay" @click="showSendDialog = false">
      <div class="send-dialog" @click.stop>
        <div class="dialog-header">
          <h3>发送消息</h3>
          <button class="close-btn" @click="showSendDialog = false">
            <IconClose />
          </button>
        </div>
        <div class="dialog-content">
          <div class="form-group">
            <label>接收用户ID：</label>
            <input 
              v-model="sendForm.recipientUserId" 
              type="number" 
              placeholder="请输入用户ID"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>消息内容：</label>
            <textarea 
              v-model="sendForm.content"
              placeholder="请输入消息内容..."
              class="form-textarea"
              rows="4"
            ></textarea>
          </div>
        </div>
        <div class="dialog-actions">
          <button class="cancel-btn" @click="showSendDialog = false">取消</button>
          <button 
            class="send-btn" 
            @click="sendMessage"
            :disabled="!sendForm.recipientUserId || !sendForm.content.trim()"
          >
            发送
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { messageApi, type MessageDto, type PageResponse } from '../../api/message'
import { useAuth } from '../../composables/useAuth'
import toast from '../../utils/toast'

// 定义事件
const emit = defineEmits<{
  close: []
}>()

// 关闭消息面板
const closePanel = () => {
  // 恢复body滚动
  document.body.style.overflow = ''
  emit('close')
}

// 直接导入图标组件
import IconMessage from '../icons/IconMessage.vue'
import IconCheck from '../icons/IconCheck.vue'
import IconSend from '../icons/IconSend.vue'
import IconTrash from '../icons/IconTrash.vue'
import IconClose from '../icons/IconClose.vue'

// 认证状态
const { user } = useAuth()
const currentUserId = computed(() => {
  // 兼容不同的字段名：userId 或 id
  return user.value?.userId || (user.value as any)?.id
})

// 响应式数据
const loading = ref(false)
const messages = ref<MessageDto[]>([])
const unreadCount = ref(0)
const activeTab = ref('all')
const currentPage = ref(0)
const totalPages = ref(0)
const showSendDialog = ref(false)
const expandedMessage = ref<number | null>(null)

// 发送消息表单
const sendForm = ref({
  recipientUserId: null as number | null,
  content: ''
})

// 标签页配置
const defaultTabs = [
  { key: 'all', label: '全部消息' },
  { key: 'received', label: '收到的' },
  { key: 'sent', label: '发送的' },
  { key: 'unread', label: '未读消息' }
]

// 从本地存储获取标签页顺序，如果没有则使用默认顺序
const getTabsOrder = (): typeof defaultTabs => {
  try {
    const savedOrder = localStorage.getItem('messageTabsOrder')
    if (savedOrder) {
      const parsedOrder = JSON.parse(savedOrder)
      // 验证保存的顺序是否有效
      if (Array.isArray(parsedOrder) && parsedOrder.length === defaultTabs.length) {
        const isValid = defaultTabs.every(defaultTab => 
          parsedOrder.some(savedTab => savedTab.key === defaultTab.key)
        )
        if (isValid) {
          return parsedOrder
        }
      }
    }
  } catch (error) {
    console.warn('读取标签页顺序失败:', error)
  }
  return [...defaultTabs]
}

// 保存标签页顺序到本地存储
const saveTabsOrder = (tabs: typeof defaultTabs) => {
  try {
    localStorage.setItem('messageTabsOrder', JSON.stringify(tabs))
  } catch (error) {
    console.warn('保存标签页顺序失败:', error)
  }
}

const tabs = ref(getTabsOrder())

// 拖拽相关状态
const draggedTabIndex = ref<number | null>(null)
const dragOverIndex = ref<number | null>(null)

// 监听标签页切换
watch(activeTab, () => {
  currentPage.value = 0
  loadMessages()
})

// 组件挂载时加载数据
onMounted(() => {
  loadMessages()
  loadUnreadCount()
  // 禁止body滚动
  document.body.style.overflow = 'hidden'
})

// 组件卸载时恢复滚动
onUnmounted(() => {
  // 恢复body滚动
  document.body.style.overflow = ''
})

/**
 * 加载消息列表
 */
const loadMessages = async () => {
  if (!currentUserId.value) {
    return
  }
  
  loading.value = true
  try {
    let response: PageResponse<MessageDto>
    
    switch (activeTab.value) {
      case 'received':
        response = await messageApi.getReceivedMessages(currentPage.value, 20)
        break
      case 'sent':
        response = await messageApi.getSentMessages(currentPage.value, 20)
        break
      case 'unread':
        const unreadMessages = await messageApi.getUnreadMessages()
        // 模拟分页格式
        response = {
          content: unreadMessages,
          totalElements: unreadMessages.length,
          totalPages: 1,
          number: 0,
          size: unreadMessages.length,
          first: true,
          last: true
        }
        break
      default:
        response = await messageApi.getAllMessages(currentPage.value, 20)
    }
    
    messages.value = response.content
    totalPages.value = response.totalPages
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '加载消息失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加载未读消息数量
 */
const loadUnreadCount = async () => {
  if (!currentUserId.value) return
  
  try {
    unreadCount.value = await messageApi.getUnreadCount()
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

/**
 * 切换页码
 */
const changePage = (page: number) => {
  currentPage.value = page
  loadMessages()
}

/**
 * 发送消息
 */
const sendMessage = async () => {
  if (!sendForm.value.recipientUserId || !sendForm.value.content.trim()) return
  
  try {
    await messageApi.sendMessage({
      recipientUserId: sendForm.value.recipientUserId,
      content: sendForm.value.content.trim()
    })
    
    toast.success('消息发送成功')
    showSendDialog.value = false
    sendForm.value = { recipientUserId: null, content: '' }
    
    // 重新加载消息列表
    loadMessages()
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '发送消息失败')
  }
}

// 监听发送对话框状态变化
watch(showSendDialog, (newVal) => {
  if (newVal) {
    // 打开对话框时禁止滚动
    document.body.style.overflow = 'hidden'
  } else {
    // 关闭对话框时恢复滚动状态（因为消息面板本身也禁止滚动）
    document.body.style.overflow = 'hidden'
  }
})

/**
 * 标记消息为已读
 */
const markAsRead = async (messageId: number) => {
  try {
    await messageApi.markAsRead(messageId)
    
    // 更新本地状态
    const messageIndex = messages.value.findIndex(m => m.messageId === messageId)
    if (messageIndex !== -1) {
      messages.value[messageIndex].status = '已读'
    }
    
    toast.success('标记已读成功')
    loadUnreadCount() // 更新未读数量
  } catch (error) {
    console.error('标记已读失败:', error)
    toast.error(error instanceof Error ? error.message : '标记已读失败')
  }
}

/**
 * 标记全部消息为已读
 */
const markAllAsRead = async () => {
  try {
    await messageApi.markAllAsRead()
    
    // 更新本地状态
    messages.value.forEach(message => {
      if (message.status === '未读') {
        message.status = '已读'
      }
    })
    
    toast.success('全部标记已读成功')
    loadUnreadCount() // 更新未读数量
  } catch (error) {
    console.error('标记全部已读失败:', error)
    toast.error(error instanceof Error ? error.message : '标记已读失败')
  }
}

/**
 * 删除消息
 */
const deleteMessage = async (messageId: number) => {
  if (!confirm('确定要删除这条消息吗？')) return
  
  try {
    await messageApi.deleteMessage(messageId)
    
    // 从本地列表中移除消息
    messages.value = messages.value.filter(m => m.messageId !== messageId)
    
    toast.success('删除成功')
    loadUnreadCount() // 更新未读数量
  } catch (error) {
    console.error('删除消息失败:', error)
    toast.error(error instanceof Error ? error.message : '删除失败')
  }
}

/**
 * 切换消息展开状态
 */
const toggleMessageExpansion = (messageId: number) => {
  if (expandedMessage.value === messageId) {
    expandedMessage.value = null
  } else {
    expandedMessage.value = messageId
  }
}

/**
 * 打开对话
 */
const openConversation = (message: MessageDto) => {
  // 这里可以实现打开对话界面的逻辑
  console.log('打开对话:', message)
}

/**
 * 获取头像URL
 */
const getAvatarUrl = (message: MessageDto): string => {
  const username = message.senderUserId === currentUserId.value 
    ? message.recipientUsername 
    : message.senderUsername
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(username || 'User')}&background=667eea&color=fff&size=40`
}

/**
 * 获取显示名称
 */
const getDisplayName = (message: MessageDto): string => {
  if (message.senderUserId === currentUserId.value) {
    return `发送给 ${message.recipientUsername || '未知用户'}`
  } else {
    return message.senderUsername || '未知用户'
  }
}

/**
 * 格式化时间
 */
const formatTime = (timeStr: string): string => {
  const time = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - time.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return time.toLocaleDateString('zh-CN')
}

/**
 * 拖拽开始
 */
const handleDragStart = (event: DragEvent, index: number) => {
  draggedTabIndex.value = index
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
    event.dataTransfer.setData('text/html', '')
  }
}

/**
 * 拖拽进入
 */
const handleDragEnter = (index: number) => {
  dragOverIndex.value = index
}

/**
 * 拖拽离开
 */
const handleDragLeave = () => {
  // 延迟清除，避免在子元素间移动时闪烁
  setTimeout(() => {
    dragOverIndex.value = null
  }, 50)
}

/**
 * 拖拽悬停
 */
const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
}

/**
 * 拖拽放置
 */
const handleDrop = (event: DragEvent, dropIndex: number) => {
  event.preventDefault()
  
  if (draggedTabIndex.value === null || draggedTabIndex.value === dropIndex) {
    draggedTabIndex.value = null
    dragOverIndex.value = null
    return
  }

  // 重新排列标签页
  const newTabs = [...tabs.value]
  const draggedTab = newTabs.splice(draggedTabIndex.value, 1)[0]
  newTabs.splice(dropIndex, 0, draggedTab)
  
  tabs.value = newTabs
  saveTabsOrder(newTabs)
  
  // 清除拖拽状态
  draggedTabIndex.value = null
  dragOverIndex.value = null
}

/**
 * 拖拽结束
 */
const handleDragEnd = () => {
  draggedTabIndex.value = null
  dragOverIndex.value = null
}
</script>

<style scoped>
/* CSS 变量定义 */
:root {
  --primary-color: #667eea;
  --primary-dark: #5a67d8;
  --secondary-color: #764ba2;
  --success-color: #10b981;
  --danger-color: #ef4444;
  --warning-color: #f59e0b;
  --text-color: #1f2937;
  --text-secondary: #6b7280;
  --background-light: #f8fafc;
  --border-color: #e5e7eb;
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

/* 通用样式重置 */
* {
  box-sizing: border-box;
}

/* 基础动画 */
@keyframes gentleFloat {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-3px);
  }
}

.message-panel * {
  outline-color: var(--primary-color);
  outline-offset: 2px;
}

.message-panel {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 800px;
  height: 600px;
  max-width: 90vw;
  max-height: 85vh;
  background: white;
  border-radius: 12px;
  box-shadow: 
    0 10px 40px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  z-index: 1000;
}

/* 背景遮罩层 */
.message-panel::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: -1;
  backdrop-filter: blur(4px);
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
  flex-shrink: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.header-icon {
  width: 20px;
  height: 20px;
  color: white;
}

.header-title-section {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.panel-title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  letter-spacing: -0.025em;
}

.panel-subtitle {
  margin: 0;
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 400;
}

.unread-badge {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn.primary {
  background: rgba(16, 185, 129, 0.9);
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.action-btn.primary:hover {
  background: rgba(5, 150, 105, 0.95);
  border-color: rgba(16, 185, 129, 0.5);
  transform: translateY(-1px);
}

.action-btn.secondary {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.action-btn.secondary:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.4);
  transform: translateY(-1px);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: rgba(239, 68, 68, 0.15);
  color: #fca5a5;
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: 6px;
  cursor: pointer;
}

.close-btn:hover {
  background: rgba(239, 68, 68, 0.25);
  border-color: rgba(239, 68, 68, 0.4);
  color: #fef2f2;
}

.close-btn svg {
  width: 16px;
  height: 16px;
}

.message-tabs {
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  padding: 0;
  flex-shrink: 0;
}

.tabs-container {
  display: flex;
  padding: 0;
  background: transparent;
  border-radius: 0;
  box-shadow: none;
}

.tab-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 16px;
  border: none;
  background: transparent;
  color: #64748b;
  font-weight: 500;
  font-size: 0.9em;
  cursor: pointer;
  border-radius: 0;
  transition: all 0.2s ease;
  position: relative;
  min-height: auto;
  flex: 1;
  justify-content: center;
  border-bottom: 2px solid transparent;
}

.tab-label {
  font-weight: 500;
}

.tab-button:hover:not(.active) {
  background: #f1f5f9;
  color: #475569;
}

.tab-button.active {
  background: white;
  color: #4f46e5;
  font-weight: 600;
  border-bottom-color: #4f46e5;
}

.tab-button.active::after {
  background: linear-gradient(90deg, #667eea, #764ba2);
  height: 3px;
  border-radius: 2px;
}

.tab-badge {
  margin-left: 4px;
  padding: 1px 6px;
  background: #ef4444;
  color: white;
  border-radius: 8px;
  font-size: 0.7rem;
  font-weight: 600;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
  background: white;
}

.message-list::-webkit-scrollbar {
  width: 6px;
}

.message-list::-webkit-scrollbar-track {
  background: #f8fafc;
}

.message-list::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
  color: var(--text-secondary);
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin-bottom: 20px;
  opacity: 0.4;
  color: var(--text-secondary);
}

.empty-state p {
  font-size: 1.1em;
  font-weight: 500;
  margin: 0;
  color: var(--text-secondary);
}

.message-item {
  display: flex;
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
  background: white;
  align-items: flex-start;
  gap: 12px;
}

.message-item:hover {
  background: #f8fafc;
}

.message-item.expanded {
  background: #f0f9ff;
  border-left: 3px solid #0ea5e9;
}

/* 未读消息特殊效果 */
.message-item.unread {
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
  border-left: 3px solid #f59e0b;
}

.message-item.unread .sender-name {
  color: #92400e;
  font-weight: 600;
}

/* 发送的消息样式 */
.message-item.sent {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-left: 3px solid #0ea5e9;
}

.message-item.sent .sender-name {
  color: #0c4a6e;
}

.message-avatar {
  flex-shrink: 0;
}

.message-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  border: 2px solid #e5e7eb;
}

.message-item:hover .message-avatar img {
  border-color: #cbd5e1;
}

.message-item.expanded .message-avatar img {
  border-color: #0ea5e9;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.sender-name {
  font-weight: 600;
  color: #1f2937;
  font-size: 0.95em;
}

.message-time {
  font-size: 0.8em;
  color: #6b7280;
  font-weight: 400;
}

.message-text {
  color: #374151;
  line-height: 1.5;
  font-size: 0.9em;
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.message-text.expanded {
  background: #f8fafc;
  padding: 12px;
  border-radius: 6px;
  border-left: 3px solid #0ea5e9;
  margin-top: 8px;
}

/* 消息详细信息 */
.message-details {
  margin-top: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #f1f5f9 0%, #f8fafc 100%);
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
    max-height: 0;
  }
  to {
    opacity: 1;
    transform: translateY(0);
    max-height: 200px;
  }
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row:hover {
  background: rgba(255, 255, 255, 0.5);
  margin: 0 -16px;
  padding: 12px 16px;
  border-radius: 8px;
}

.detail-label {
  font-weight: 600;
  color: var(--text-secondary);
  font-size: 0.9em;
  min-width: 80px;
}

.detail-value {
  color: var(--text-color);
  font-weight: 500;
  text-align: right;
  flex: 1;
}

.status-unread {
  color: #ef4444;
  font-weight: 600;
}

.status-read {
  color: #10b981;
  font-weight: 600;
}

/* 展开状态下的操作按钮 */
.message-actions-expanded {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 2px solid #e2e8f0;
  justify-content: flex-end;
}

.action-btn-expanded {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: none;
  border-radius: 10px;
  font-size: 0.9em;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  background: linear-gradient(135deg, #f1f5f9 0%, #ffffff 100%);
  color: var(--text-color);
  border: 1px solid #cbd5e1;
  min-width: 100px;
  justify-content: center;
}

.action-btn-expanded:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-color: var(--primary-color);
}

.action-btn-expanded.delete-btn {
  background: linear-gradient(135deg, #fef2f2 0%, #ffffff 100%);
  border-color: #fecaca;
  color: #dc2626;
}

.action-btn-expanded.delete-btn:hover {
  background: linear-gradient(135deg, #fee2e2 0%, #fef2f2 100%);
  border-color: #f87171;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.2);
}

/* 消息状态指示器 */
.message-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.8em;
  color: var(--primary-color);
  font-weight: 500;
  margin-top: 8px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: #ef4444;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.1);
  }
}

/* 头像优化 */
.message-avatar {
  width: 48px;
  height: 48px;
  margin-right: 16px;
  flex-shrink: 0;
}

.message-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #f1f5f9;
  transition: all 0.3s ease;
}

.message-item.expanded .message-avatar img {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
  transform: scale(1.1);
}

/* 消息头部信息 */
.message-header-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.sender-name {
  font-weight: 600;
  color: var(--text-color);
  font-size: 1em;
}

.message-time {
  font-size: 0.85em;
  color: var(--text-secondary);
  font-weight: 400;
}

.message-item.expanded .sender-name {
  color: var(--primary-color);
  font-size: 1.1em;
}

.message-item.expanded .message-time {
  background: linear-gradient(135deg, var(--primary-color), #8b5cf6);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-weight: 500;
}

/* 加载状态和空状态优化 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--text-secondary);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f1f5f9;
  border-top: 4px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 分页控件优化 */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 24px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-top: 1px solid #e5e7eb;
}

.page-btn {
  padding: 10px 16px;
  border: 1px solid #d1d5db;
  background: linear-gradient(135deg, #ffffff 0%, #f9fafb 100%);
  color: #374151;
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 80px;
}

.page-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f3f4f6;
  color: #9ca3af;
}

.page-info {
  font-size: 0.9rem;
  color: #6b7280;
  font-weight: 500;
  padding: 0 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-panel {
    width: 95vw;
    height: 80vh;
    border-radius: 8px;
  }
  
  .message-header {
    padding: 12px 16px;
  }
  
  .panel-title {
    font-size: 1.1rem;
  }
  
  .panel-subtitle {
    font-size: 0.75rem;
  }
  
  .header-actions {
    gap: 4px;
  }
  
  .action-btn span {
    display: none;
  }
  
  .tab-button {
    padding: 10px 8px;
    font-size: 0.8em;
  }
  
  .message-item {
    padding: 12px 16px;
  }
  
  .message-avatar img {
    width: 32px;
    height: 32px;
  }
  
  .action-icon-btn {
    width: 24px;
    height: 24px;
  }
  
  .action-icon-btn svg {
    width: 12px;
    height: 12px;
  }
}

/* 简化动画效果 */

/* 焦点和键盘导航支持 */
.message-item:focus {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

.tab-button:focus,
.action-btn:focus,
.action-btn-expanded:focus {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

/* 高对比度模式支持 */
@media (prefers-contrast: high) {
  .message-item {
    border: 2px solid var(--text-color);
  }
  
  .message-item.unread {
    background: #fff3cd;
    border-color: #856404;
  }
  
  .action-btn,
  .action-btn-expanded {
    border: 2px solid currentColor;
  }
}

/* 消息项操作按钮 */
.message-actions {
  display: flex;
  gap: 4px;
  margin-left: auto;
  align-self: flex-start;
}

.action-icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.action-icon-btn:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.action-icon-btn:first-child {
  color: #059669;
  border-color: rgba(5, 150, 105, 0.2);
}

.action-icon-btn:first-child:hover {
  background: #ecfdf5;
  border-color: #059669;
}

.action-icon-btn:last-child {
  color: #dc2626;
  border-color: rgba(220, 38, 38, 0.2);
}

.action-icon-btn:last-child:hover {
  background: #fef2f2;
  border-color: #dc2626;
}

.action-icon-btn svg {
  width: 14px;
  height: 14px;
}

/* 发送消息对话框优化 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(8px);
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.send-dialog {
  background: white;
  border-radius: 20px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.3),
    0 10px 30px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.dialog-header h3 {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 600;
  letter-spacing: -0.025em;
}

.dialog-header .close-btn {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.dialog-header .close-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.4);
}

.dialog-content {
  padding: 32px 28px;
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #374151;
  font-size: 0.95rem;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  font-size: 1rem;
  transition: all 0.3s ease;
  background: white;
  box-sizing: border-box;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 
    0 0 0 4px rgba(102, 126, 234, 0.1),
    0 4px 12px rgba(102, 126, 234, 0.15);
  transform: translateY(-1px);
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
  font-family: inherit;
  line-height: 1.5;
}

.dialog-actions {
  display: flex;
  gap: 12px;
  padding: 24px 28px;
  background: #f8fafc;
  border-top: 1px solid #e5e7eb;
  justify-content: flex-end;
}

.cancel-btn,
.send-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.cancel-btn {
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  color: #64748b;
  border: 1px solid #cbd5e1;
}

.cancel-btn:hover {
  background: linear-gradient(135deg, #e2e8f0 0%, #cbd5e1 100%);
  border-color: #94a3b8;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.send-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: 1px solid rgba(102, 126, 234, 0.3);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.send-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%);
  border-color: rgba(90, 103, 216, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}

/* 拖拽状态样式 */
.tab-button.dragging {
  opacity: 0.5;
  transform: scale(0.95);
}

.tab-button.drag-over {
  border-left: 3px solid #7c3aed;
  padding-left: 9px;
  transition: all 0.2s ease;
}

.tab-button[draggable="true"] {
  cursor: move;
}

.tab-button[draggable="true"]:hover {
  background: rgba(124, 58, 237, 0.1);
}
</style>
