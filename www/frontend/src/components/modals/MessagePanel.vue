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
        <button 
          class="action-btn danger" 
          @click="deleteReadMessages"
          :disabled="readCount === 0"
          title="删除已读消息"
        >
          <IconTrash />
          <span>删除已读</span>
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
            <label>接收人：</label>
            <div class="recipient-input-container">
              <input 
                v-model="sendForm.recipientUserIds" 
                type="text" 
                placeholder="输入用户ID，多个用逗号分隔，如：1,2,3"
                class="form-input recipient-input"
                readonly
              />
              <button 
                type="button" 
                class="select-contacts-btn"
                @click="showContactsDialog = true"
              >
                <IconUsers />
                选择联系人
              </button>
            </div>
            <!-- 已选择的联系人显示 -->
            <div v-if="selectedContacts.length > 0" class="selected-contacts">
              <div 
                v-for="contact in selectedContacts" 
                :key="contact.userId"
                class="selected-contact-item"
              >
                <img :src="contact.avatarUrl || getDefaultAvatar(contact.username)" :alt="contact.username" class="contact-avatar-small" />
                <span class="contact-name">{{ contact.username }}</span>
                <button @click="removeContact(contact.userId)" class="remove-contact-btn">
                  <IconClose />
                </button>
              </div>
            </div>
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
            :disabled="selectedContacts.length === 0 || !sendForm.content.trim()"
          >
            发送
          </button>
        </div>
      </div>
    </div>

    <!-- 联系人选择对话框 -->
    <div v-if="showContactsDialog" class="modal-overlay" @click="showContactsDialog = false">
      <div class="contacts-dialog" @click.stop>
        <div class="contacts-header">
          <h3>选择联系人</h3>
          <div class="selected-count">已选: {{ tempSelectedContacts.length }} 个</div>
          <button class="close-btn" @click="showContactsDialog = false">
            <IconClose />
          </button>
        </div>
        
        <div class="contacts-content">
          <!-- 左侧：搜索和分组列表 -->
          <div class="contacts-left">
            <!-- 搜索框 -->
            <div class="contacts-search">
              <input 
                type="text" 
                v-model="contactSearchQuery"
                placeholder="搜索用户、群组、部门/域用户组"
                class="search-input"
              />
            </div>
            
            <!-- 分组导航 -->
            <div class="contacts-nav">
              <div class="nav-section">
                <div 
                  class="nav-item"
                  :class="{ active: activeContactGroup === 'all' }"
                  @click="activeContactGroup = 'all'"
                >
                  <IconUsers />
                  <span>联系人</span>
                  <IconChevronRight />
                </div>
                <div 
                  class="nav-item"
                  :class="{ active: activeContactGroup === 'organization' }"
                  @click="activeContactGroup = 'organization'"
                >
                  <IconOrganization />
                  <span>组织架构</span>
                  <IconChevronRight />
                </div>
              </div>
              
              <!-- 组织架构展开项 -->
              <div v-if="activeContactGroup === 'organization'" class="org-groups">
                <div 
                  v-for="group in userGroups" 
                  :key="group.id"
                  class="org-group-item"
                  :class="{ active: selectedGroup === group.id }"
                  @click="selectGroup(group.id)"
                >
                  <div class="group-icon">
                    <IconGroup />
                  </div>
                  <span class="group-name">{{ group.name }}</span>
                  <span class="group-count">{{ group.userCount || 0 }}</span>
                  <button v-if="group.hasSubGroups" class="expand-btn">
                    <IconChevronDown />
                  </button>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 右侧：用户列表 -->
          <div class="contacts-right">
            <div class="contacts-list">
              <div 
                v-for="user in filteredUsers" 
                :key="user.userId"
                class="contact-item"
                :class="{ selected: tempSelectedContacts.some(c => c.userId === user.userId) }"
                @click="toggleContact(user)"
              >
                <div class="contact-checkbox">
                  <input 
                    type="checkbox" 
                    :checked="tempSelectedContacts.some(c => c.userId === user.userId)"
                    @change="toggleContact(user)"
                  />
                </div>
                <div class="contact-avatar">
                  <img :src="user.avatarUrl || getDefaultAvatar(user.username)" :alt="user.username" />
                </div>
                <div class="contact-info">
                  <div class="contact-name">{{ user.username }}</div>
                  <div class="contact-role">{{ user.roleName || '用户' }}</div>
                </div>
                <div class="contact-status">
                  <span v-if="user.active" class="status-active">已启用</span>
                  <span v-else class="status-inactive">已禁用</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="contacts-actions">
          <button class="cancel-btn" @click="cancelContactSelection">取消</button>
          <button class="confirm-btn" @click="confirmContactSelection">
            确认 ({{ tempSelectedContacts.length }})
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { messageApi, type MessageDto, type PageResponse } from '../../api/message'
import { adminApi } from '../../api/user'
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
import IconUsers from '../icons/IconUsers.vue'
import IconOrganization from '../icons/IconOrganization.vue'
import IconGroup from '../icons/IconGroup.vue'
import IconChevronRight from '../icons/IconChevronRight.vue'
import IconChevronDown from '../icons/IconChevronDown.vue'

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
const readCount = computed(() => {
  return messages.value.filter(msg => msg.status === '已读').length
})
const activeTab = ref('all')
const currentPage = ref(0)
const totalPages = ref(0)
const showSendDialog = ref(false)
const expandedMessage = ref<number | null>(null)

// 联系人选择相关
const showContactsDialog = ref(false)
const selectedContacts = ref<UserContact[]>([])
const tempSelectedContacts = ref<UserContact[]>([])
const contactSearchQuery = ref('')
const activeContactGroup = ref('all')
const selectedGroup = ref<number | null>(null)
const allUsers = ref<UserContact[]>([])
const userGroups = ref<UserGroup[]>([])

// 联系人类型定义
interface UserContact {
  userId: number
  username: string
  email?: string
  fullName?: string
  avatarUrl?: string
  active: boolean
  roleId?: number
  roleName?: string
}

interface UserGroup {
  id: number
  name: string
  userCount?: number
  hasSubGroups?: boolean
}

// 发送消息表单
const sendForm = ref({
  recipientUserIds: '', // 改为字符串，支持多个ID用逗号分隔
  content: '',
  isBroadcast: false // 是否为群发
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

// 计算属性
const filteredUsers = computed(() => {
  let users = allUsers.value.filter(user => user.active) // 只显示启用的用户
  
  // 按组别筛选
  if (activeContactGroup.value === 'organization' && selectedGroup.value) {
    const selectedGroupName = userGroups.value.find(g => g.id === selectedGroup.value)?.name
    if (selectedGroupName) {
      users = users.filter(user => user.roleName === selectedGroupName)
    }
  }
  
  // 按搜索关键词筛选
  if (contactSearchQuery.value.trim()) {
    const query = contactSearchQuery.value.toLowerCase()
    users = users.filter(user => 
      user.username.toLowerCase().includes(query) ||
      (user.fullName && user.fullName.toLowerCase().includes(query)) ||
      (user.email && user.email.toLowerCase().includes(query)) ||
      (user.roleName && user.roleName.toLowerCase().includes(query))
    )
  }
  
  return users
})

// 拖拽相关状态
const draggedTabIndex = ref<number | null>(null)
const dragOverIndex = ref<number | null>(null)

// 监听标签页切换
watch(activeTab, () => {
  currentPage.value = 0
  loadMessages()
})

// 组件挂载时加载数据
onMounted(async () => {
  loadMessages()
  loadUnreadCount()
  // 先加载用户，再基于用户数据生成组别
  await loadAllUsers()
  await loadUserGroups()
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
  if (selectedContacts.value.length === 0 || !sendForm.value.content.trim()) return
  
  try {
    // 获取选中的用户ID列表
    const userIds = selectedContacts.value.map(contact => contact.userId)
    
    // 根据是否群发选择不同的发送方式
    if (sendForm.value.isBroadcast && userIds.length > 1) {
      // 群发消息
      await messageApi.broadcastMessage({
        recipientUserIds: userIds,
        content: sendForm.value.content.trim()
      })
      toast.success(`群发消息成功，已发送给 ${userIds.length} 个用户`)
    } else {
      // 单独发送给每个用户
      const promises = userIds.map(userId => 
        messageApi.sendMessage({
          recipientUserId: userId,
          content: sendForm.value.content.trim()
        })
      )
      await Promise.all(promises)
      toast.success(`消息发送成功，已发送给 ${userIds.length} 个用户`)
    }
    
    showSendDialog.value = false
    sendForm.value = { recipientUserIds: '', content: '', isBroadcast: false }
    selectedContacts.value = []
    
    // 重新加载消息列表
    loadMessages()
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '发送消息失败')
  }
}

/**
 * 加载所有启用的用户
 */
const loadAllUsers = async () => {
  try {
    // 从数据库获取真实的用户数据
    const users = await adminApi.getAllActiveUsers()
    allUsers.value = users.map((user: any) => ({
      userId: user.userId || user.id,
      username: user.username,
      email: user.email,
      fullName: user.fullName,
      avatarUrl: user.avatarUrl,
      active: user.active,
      roleId: user.roleId,
      roleName: user.roleName || '普通用户'
    }))
    
    console.log('从数据库加载的用户数据:', allUsers.value)
  } catch (error) {
    console.error('加载用户失败:', error)
    toast.error('加载联系人失败，请检查网络连接')
    // 不再使用模拟数据，让用户知道加载失败
    allUsers.value = []
  }
}

/**
 * 加载用户组
 */
const loadUserGroups = async () => {
  try {
    // 基于从数据库加载的用户数据动态生成组别
    const roleNames = [...new Set(allUsers.value.map(user => user.roleName).filter(Boolean))] as string[]
    
    if (roleNames.length > 0) {
      userGroups.value = roleNames.map((roleName, index) => ({
        id: index + 1,
        name: roleName,
        userCount: allUsers.value.filter(user => user.roleName === roleName).length
      }))
      console.log('基于数据库用户生成的组别:', userGroups.value)
    } else {
      console.log('数据库中没有用户或角色数据')
      userGroups.value = []
    }
  } catch (error) {
    console.error('加载用户组失败:', error)
    userGroups.value = []
  }
}

/**
 * 选择用户组
 */
const selectGroup = (groupId: number) => {
  selectedGroup.value = selectedGroup.value === groupId ? null : groupId
  // TODO: 根据组别筛选用户
}

/**
 * 切换联系人选择状态
 */
const toggleContact = (user: UserContact) => {
  const index = tempSelectedContacts.value.findIndex(c => c.userId === user.userId)
  if (index > -1) {
    tempSelectedContacts.value.splice(index, 1)
  } else {
    tempSelectedContacts.value.push(user)
  }
}

/**
 * 移除联系人
 */
const removeContact = (userId: number) => {
  selectedContacts.value = selectedContacts.value.filter(c => c.userId !== userId)
  updateRecipientIds()
}

/**
 * 确认联系人选择
 */
const confirmContactSelection = () => {
  selectedContacts.value = [...tempSelectedContacts.value]
  updateRecipientIds()
  showContactsDialog.value = false
}

/**
 * 取消联系人选择
 */
const cancelContactSelection = () => {
  tempSelectedContacts.value = [...selectedContacts.value]
  showContactsDialog.value = false
}

/**
 * 更新接收人ID字段
 */
const updateRecipientIds = () => {
  sendForm.value.recipientUserIds = selectedContacts.value.map(c => c.userId).join(',')
}

/**
 * 获取默认头像
 */
const getDefaultAvatar = (username: string): string => {
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(username)}&background=667eea&color=fff&size=40`
}

// 监听联系人选择对话框状态变化
watch(showContactsDialog, (newVal) => {
  if (newVal) {
    // 打开对话框时，初始化临时选择状态
    tempSelectedContacts.value = [...selectedContacts.value]
    // 禁止滚动
    document.body.style.overflow = 'hidden'
  } else {
    // 关闭对话框时恢复滚动状态（因为消息面板本身也禁止滚动）
    document.body.style.overflow = 'hidden'
  }
})

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
 * 删除已读消息
 */
const deleteReadMessages = async () => {
  try {
    // 获取已读消息的ID列表
    const readMessageIds = messages.value
      .filter(msg => msg.status === '已读' && msg.recipientUserId === currentUserId.value)
      .map(msg => msg.messageId)
    
    if (readMessageIds.length === 0) {
      toast.info('没有已读消息可删除')
      return
    }
    
    // 确认删除
    if (!confirm(`确定要删除 ${readMessageIds.length} 条已读消息吗？此操作不可撤销。`)) {
      return
    }
    
    // 批量删除已读消息
    const deletePromises = readMessageIds.map(id => messageApi.deleteMessage(id))
    await Promise.all(deletePromises)
    
    // 更新本地状态，移除已删除的消息
    messages.value = messages.value.filter(msg => !readMessageIds.includes(msg.messageId))
    
    toast.success(`成功删除 ${readMessageIds.length} 条已读消息`)
    
    // 重新加载当前页面的消息
    await loadMessages()
  } catch (error) {
    console.error('删除已读消息失败:', error)
    toast.error('删除已读消息失败')
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
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: white;
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
  padding: 20px 30px;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
  flex-shrink: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
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

.action-btn.danger {
  background: rgba(220, 53, 69, 0.1);
  border: 1px solid rgba(220, 53, 69, 0.3);
  color: #dc3545;
}

.action-btn.danger:hover:not(:disabled) {
  background: rgba(220, 53, 69, 0.2);
  border-color: rgba(220, 53, 69, 0.5);
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
  padding: 16px 24px;
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
  padding: 24px 32px;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
  background: white;
  align-items: flex-start;
  gap: 16px;
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
  line-height: 1.6;
  font-size: 1rem;
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.message-text.expanded {
  background: #f8fafc;
  padding: 16px 20px;
  border-radius: 8px;
  border-left: 3px solid #0ea5e9;
  margin-top: 12px;
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
  width: 95vw;
  max-width: 1200px;
  max-height: 95vh;
  min-height: 600px;
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
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-header .close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
}

.dialog-header .close-btn svg {
  width: 16px;
  height: 16px;
}

.dialog-content {
  padding: 48px 40px 24px 40px;
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
  min-height: 400px;
}

.form-group {
  margin-bottom: 20px;
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
  width: 100%; /* 增加宽度到100% */
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 13px;
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
  min-height: 300px;
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

/* 新增样式：群发功能相关 */
.input-hint {
        font-size: 12px;
        color: #666;
        margin-top: 4px;
        line-height: 1.4;
      }
      
      .checkbox-container {
        display: flex;
        align-items: center;
        margin-top: 8px;
        font-size: 14px;
        cursor: pointer;
      }
      
      .checkbox-container input[type="checkbox"] {
        margin-right: 8px;
        cursor: pointer;
      }
      
      .checkmark {
        color: #007acc;
      }

/* 联系人选择相关样式 */
.recipient-input-container {
  display: flex;
  gap: 8px;
  align-items: center;
}

.recipient-input {
  flex: 1;
}

.select-contacts-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.select-contacts-btn:hover {
  background: linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%);
  transform: translateY(-1px);
}

.selected-contacts {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.selected-contact-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  background: #f1f5f9;
  border-radius: 16px;
  font-size: 12px;
  border: 1px solid #e2e8f0;
}

.contact-avatar-small {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
}

.contact-name {
  font-weight: 500;
}

.remove-contact-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.2s ease;
  font-weight: bold;
}

.remove-contact-btn:hover {
  background: #dc2626;
  transform: scale(1.1);
}

/* 联系人选择对话框样式 */
.contacts-dialog {
  background: white;
  border-radius: 16px;
  width: 90vw;
  max-width: 1200px;
  height: 80vh;
  max-height: 700px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.contacts-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  flex-shrink: 0;
}

.contacts-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.selected-count {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
  background: rgba(255, 255, 255, 0.15);
  padding: 4px 12px;
  border-radius: 20px;
}

.contacts-content {
  display: flex;
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

.contacts-left {
  width: 350px;
  min-width: 350px;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
}

.contacts-search {
  padding: 12px;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
  flex-shrink: 0;
}

.search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 13px;
  outline: none;
  transition: border-color 0.2s ease;
  box-sizing: border-box;
}

.search-input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.contacts-nav {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.nav-section {
  padding: 4px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #4b5563;
  font-size: 13px;
  border-radius: 6px;
  margin: 1px 8px;
}

.nav-item:hover {
  background: #f1f5f9;
  color: #667eea;
}

.nav-item.active {
  background: #667eea;
  color: white;
  font-weight: 500;
}

.nav-item svg {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.org-groups {
  margin-left: 12px;
  max-height: 200px;
  overflow-y: auto;
}

.org-group-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #6b7280;
  font-size: 12px;
  border-radius: 4px;
  margin: 1px 6px;
}

.org-group-item:hover {
  background: #f1f5f9;
  color: #667eea;
}

.org-group-item.active {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  font-weight: 500;
}

.group-icon {
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #667eea;
  color: white;
  border-radius: 4px;
  font-size: 10px;
  flex-shrink: 0;
}

.group-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-count {
  font-size: 10px;
  color: #9ca3af;
  background: #e5e7eb;
  padding: 2px 6px;
  border-radius: 10px;
  flex-shrink: 0;
}

.org-group-item.active .group-count {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.expand-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #9ca3af;
  transition: transform 0.2s ease;
  padding: 2px;
  flex-shrink: 0;
}

.contacts-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

.contacts-list {
  flex: 1;
  overflow-y: auto;
  padding: 6px;
  min-height: 0;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
  margin-bottom: 2px;
}

.contact-item:hover {
  background: #f8fafc;
  border-color: rgba(102, 126, 234, 0.3);
}

.contact-item.selected {
  background: rgba(102, 126, 234, 0.1);
  border-color: #667eea;
}

.contact-checkbox {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.contact-checkbox input[type="checkbox"] {
  width: 14px;
  height: 14px;
  accent-color: #667eea;
  cursor: pointer;
}

.contact-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.contact-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.contact-info {
  flex: 1;
  min-width: 0;
}

.contact-info .contact-name {
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 2px;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.contact-info .contact-role {
  font-size: 11px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.contact-role {
  font-size: 12px;
  color: #6b7280;
}

.contact-status {
  margin-left: 8px;
  flex-shrink: 0;
}

.status-active {
  color: #22c55e;
  font-size: 10px;
  font-weight: 500;
}

.status-inactive {
  color: #ef4444;
  font-size: 10px;
  font-weight: 500;
}

.contacts-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid #e2e8f0;
  background: #f8fafc;
  flex-shrink: 0;
}

.contacts-actions .cancel-btn,
.contacts-actions .confirm-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 13px;
}

.contacts-actions .cancel-btn {
  background: white;
  color: #6b7280;
  border: 1px solid #d1d5db;
}

.contacts-actions .cancel-btn:hover {
  background: #f9fafb;
  border-color: #9ca3af;
}

.contacts-actions .confirm-btn {
  background: #667eea;
  color: white;
}

.contacts-actions .confirm-btn:hover {
  background: #5a67d8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.contacts-actions .confirm-btn:disabled {
  background: #d1d5db;
  color: #9ca3af;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .contacts-dialog {
    width: 98vw;
    height: 90vh;
  }
  
  .contacts-left {
    width: 240px;
    min-width: 240px;
  }
  
  .contacts-header {
    padding: 12px 16px;
  }
  
  .contacts-header h3 {
    font-size: 16px;
  }
  
  .selected-count {
    font-size: 12px;
    padding: 3px 8px;
  }
}

/* 滚动条优化 */
.contacts-nav::-webkit-scrollbar,
.contacts-list::-webkit-scrollbar,
.org-groups::-webkit-scrollbar {
  width: 4px;
}

.contacts-nav::-webkit-scrollbar-track,
.contacts-list::-webkit-scrollbar-track,
.org-groups::-webkit-scrollbar-track {
  background: #f1f5f9;
}

.contacts-nav::-webkit-scrollbar-thumb,
.contacts-list::-webkit-scrollbar-thumb,
.org-groups::-webkit-scrollbar-thumb {
  background: #cbd5e0;
  border-radius: 2px;
}

.contacts-nav::-webkit-scrollbar-thumb:hover,
.contacts-list::-webkit-scrollbar-thumb:hover,
.org-groups::-webkit-scrollbar-thumb:hover {
  background: #a0aec0;
}
</style>
