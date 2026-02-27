<template>
  <div class="messages-layout">
    <!-- ── 左侧：文件夹导航 ────────────────────────────────────────────── -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <span class="sidebar-title">消息中心</span>
        <button class="compose-btn" @click="openCompose" title="撰写消息">
          <svg viewBox="0 0 24 24" class="icon"><path d="M20.71,7.04C21.1,6.65 21.1,6 20.71,5.63L18.37,3.29C18,2.9 17.35,2.9 16.96,3.29L15.12,5.12L18.87,8.87L20.71,7.04M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z"/></svg>
        </button>
      </div>

      <nav class="folder-list">
        <button
          v-for="folder in folders"
          :key="folder.key"
          class="folder-item"
          :class="{ active: activeFolder === folder.key }"
          @click="switchFolder(folder.key)"
        >
          <svg viewBox="0 0 24 24" class="folder-icon"><path :d="folder.icon"/></svg>
          <span class="folder-label">{{ folder.label }}</span>
          <span v-if="folder.key === 'inbox' && unreadCount > 0" class="badge">{{ unreadCount }}</span>
        </button>
      </nav>
    </aside>

    <!-- ── 中间：消息列表 ────────────────────────────────────────────────── -->
    <section class="msg-list-pane">
      <div class="list-toolbar">
        <span class="list-title">{{ currentFolderLabel }}</span>
        <button
          v-if="activeFolder === 'inbox' && unreadCount > 0"
          class="mark-all-btn"
          @click="markAllRead"
        >
          全部已读
        </button>
      </div>

      <div v-if="loading" class="state-placeholder">加载中…</div>
      <div v-else-if="messages.length === 0" class="state-placeholder">暂无消息</div>

      <ul v-else class="msg-list">
        <li
          v-for="msg in messages"
          :key="msg.messageId"
          class="msg-item"
          :class="{
            selected: selectedMsg?.messageId === msg.messageId,
            unread: msg.status === 'UNREAD',
          }"
          @click="selectMsg(msg)"
        >
          <!-- 未读圆点 -->
          <span class="unread-dot" :class="{ visible: msg.status === 'UNREAD' }"></span>

          <div class="msg-meta">
            <span class="msg-from">{{ senderLabel(msg) }}</span>
            <span class="msg-time">{{ formatTime(msg.sendTime) }}</span>
          </div>
          <div class="msg-subject">{{ msg.subject || '（无主题）' }}</div>
          <div class="msg-preview">{{ msg.content.slice(0, 80) }}</div>
        </li>
      </ul>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="pagination">
        <button :disabled="page === 0" @click="changePage(page - 1)">‹</button>
        <span>{{ page + 1 }} / {{ totalPages }}</span>
        <button :disabled="page >= totalPages - 1" @click="changePage(page + 1)">›</button>
      </div>
    </section>

    <!-- ── 右侧：消息详情 / 撰写框 ────────────────────────────────────────── -->
    <section class="detail-pane">
      <!-- 撰写新消息 -->
      <template v-if="composing">
        <div class="detail-header">
          <span class="detail-title">撰写新消息</span>
          <button class="icon-btn" @click="composing = false" title="关闭">✕</button>
        </div>
        <div class="compose-form">
          <label class="form-row">
            <span class="form-label">收件人 ID</span>
            <input v-model.number="draft.recipientUserId" type="number" placeholder="用户 ID" class="form-input"/>
          </label>
          <label class="form-row">
            <span class="form-label">主题</span>
            <input v-model="draft.subject" type="text" placeholder="（可选）" class="form-input"/>
          </label>
          <label class="form-row form-row--full">
            <span class="form-label">内容</span>
            <textarea v-model="draft.content" rows="8" class="form-input form-textarea" placeholder="消息内容…"></textarea>
          </label>
          <div class="compose-actions">
            <button class="send-btn" :disabled="sending" @click="sendMessage">
              {{ sending ? '发送中…' : '发送' }}
            </button>
          </div>
          <p v-if="sendError" class="send-error">{{ sendError }}</p>
        </div>
      </template>

      <!-- 消息详情 -->
      <template v-else-if="selectedMsg">
        <div class="detail-header">
          <span class="detail-title">{{ selectedMsg.subject || '（无主题）' }}</span>
          <div class="detail-header-actions">
            <button class="icon-btn reply-btn" @click="startReply" title="回复"
              v-if="selectedMsg.senderUserId !== null"
            >
              <svg viewBox="0 0 24 24" class="icon"><path d="M10,9V5L3,12L10,19V14.9C15,14.9 18.5,16.5 21,20C20,15 17,10 10,9Z"/></svg>
            </button>
            <button class="icon-btn" @click="removeMsg(selectedMsg.messageId)" title="删除">
              <svg viewBox="0 0 24 24" class="icon"><path d="M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z"/></svg>
            </button>
          </div>
        </div>

        <div class="detail-info">
          <span>发件人：{{ selectedMsg.senderUsername ?? '系统' }}</span>
          <span>时间：{{ formatFullTime(selectedMsg.sendTime) }}</span>
        </div>

        <div class="detail-content">{{ selectedMsg.content }}</div>

        <!-- 快速回复框 -->
        <div v-if="replying" class="reply-box">
          <textarea
            v-model="replyContent"
            rows="4"
            class="form-input form-textarea"
            placeholder="回复内容…"
          ></textarea>
          <div class="compose-actions">
            <button class="send-btn" :disabled="sending" @click="sendReply">
              {{ sending ? '发送中…' : '回复' }}
            </button>
            <button class="cancel-btn" @click="replying = false">取消</button>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <template v-else>
        <div class="empty-detail">
          <svg viewBox="0 0 24 24" class="empty-icon"><path d="M20,8L12,13L4,8V6L12,11L20,6M20,4H4C2.89,4 2,4.89 2,6V18A2,2 0 0,0 4,20H20A2,2 0 0,0 22,18V6C22,4.89 21.1,4 20,4Z"/></svg>
          <p>选择一条消息查看详情</p>
        </div>
      </template>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { messageApi, type Message, type SendMessageRequest } from '../api/message'

// ── 文件夹定义 ───────────────────────────────────────────────────────────────
const folders = [
  {
    key: 'inbox',
    label: '收件箱',
    icon: 'M20,8L12,13L4,8V6L12,11L20,6M20,4H4C2.89,4 2,4.89 2,6V18A2,2 0 0,0 4,20H20A2,2 0 0,0 22,18V6C22,4.89 21.1,4 20,4Z',
  },
  {
    key: 'sent',
    label: '已发送',
    icon: 'M2,21L23,12L2,3V10L17,12L2,14V21Z',
  },
  {
    key: 'system',
    label: '系统通知',
    icon: 'M12,22A2,2 0 0,0 14,20H10A2,2 0 0,0 12,22M18,16V11C18,7.93 16.36,5.36 13.5,4.68V4A1.5,1.5 0 0,0 12,2.5A1.5,1.5 0 0,0 10.5,4V4.68C7.63,5.36 6,7.92 6,11V16L4,18V19H20V18L18,16Z',
  },
] as const

type FolderKey = typeof folders[number]['key']

// ── 状态 ────────────────────────────────────────────────────────────────────
const activeFolder  = ref<FolderKey>('inbox')
const messages      = ref<Message[]>([])
const selectedMsg   = ref<Message | null>(null)
const loading       = ref(false)
const page          = ref(0)
const totalPages    = ref(1)
const unreadCount   = ref(0)

const composing    = ref(false)
const replying     = ref(false)
const sending      = ref(false)
const sendError    = ref('')
const replyContent = ref('')

const draft = ref<SendMessageRequest>({ recipientUserId: 0, subject: '', content: '' })

// ── 计算 ────────────────────────────────────────────────────────────────────
const currentFolderLabel = computed(
  () => folders.find(f => f.key === activeFolder.value)?.label ?? ''
)

// ── 数据加载 ─────────────────────────────────────────────────────────────────
async function loadMessages() {
  loading.value = true
  selectedMsg.value = null
  try {
    let res
    if (activeFolder.value === 'inbox') {
      res = await messageApi.getInbox(page.value)
    } else if (activeFolder.value === 'sent') {
      res = await messageApi.getSent(page.value)
    } else {
      res = await messageApi.getInbox(page.value, 20, 'SYSTEM')
    }
    const pr = res.data.data
    messages.value   = pr.content
    totalPages.value  = pr.totalPages
  } catch {
    messages.value = []
  } finally {
    loading.value = false
  }
}

async function loadUnreadCount() {
  try {
    const res = await messageApi.getUnreadCount()
    unreadCount.value = res.data.data.count
  } catch {
    // 忽略
  }
}

// ── 操作 ─────────────────────────────────────────────────────────────────────
function switchFolder(key: FolderKey) {
  activeFolder.value = key
  page.value = 0
}

async function selectMsg(msg: Message) {
  selectedMsg.value = msg
  composing.value   = false
  replying.value    = false
  if (msg.status === 'UNREAD') {
    await messageApi.markRead(msg.messageId)
    msg.status = 'READ'
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
}

async function markAllRead() {
  await messageApi.markAllRead()
  messages.value.forEach(m => { m.status = 'READ' })
  unreadCount.value = 0
}

async function removeMsg(id: number) {
  await messageApi.remove(id)
  messages.value    = messages.value.filter(m => m.messageId !== id)
  if (selectedMsg.value?.messageId === id) selectedMsg.value = null
}

function openCompose() {
  composing.value = true
  replying.value  = false
  selectedMsg.value = null
  draft.value = { recipientUserId: 0, subject: '', content: '' }
  sendError.value = ''
}

function startReply() {
  replying.value  = true
  replyContent.value = ''
}

async function sendMessage() {
  if (!draft.value.recipientUserId || !draft.value.content.trim()) {
    sendError.value = '收件人 ID 和内容不能为空'
    return
  }
  sending.value  = true
  sendError.value = ''
  try {
    await messageApi.send(draft.value)
    composing.value = false
    if (activeFolder.value === 'sent') await loadMessages()
  } catch {
    sendError.value = '发送失败，请稍后重试'
  } finally {
    sending.value = false
  }
}

async function sendReply() {
  if (!selectedMsg.value || !replyContent.value.trim()) return
  if (selectedMsg.value.senderUserId === null) return   // 系统消息不可回复
  sending.value = true
  try {
    await messageApi.send({
      recipientUserId: selectedMsg.value.senderUserId,
      subject:         `Re: ${selectedMsg.value.subject ?? ''}`,
      content:         replyContent.value,
    })
    replying.value     = false
    replyContent.value = ''
  } catch {
    // 静默失败
  } finally {
    sending.value = false
  }
}

function changePage(p: number) {
  page.value = p
}

// ── 时间格式化 ────────────────────────────────────────────────────────────────
function formatTime(iso: string) {
  const d = new Date(iso)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 60_000)      return '刚刚'
  if (diff < 3_600_000)   return `${Math.floor(diff / 60_000)} 分钟前`
  if (diff < 86_400_000)  return `${Math.floor(diff / 3_600_000)} 小时前`
  return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

function formatFullTime(iso: string) {
  return new Date(iso).toLocaleString('zh-CN')
}

function senderLabel(msg: Message) {
  if (activeFolder.value === 'sent') return `→ ${msg.recipientUsername ?? msg.recipientUserId}`
  return msg.senderUsername ?? '系统'
}

// ── 轮询 ────────────────────────────────────────────────────────────────────
let pollTimer: ReturnType<typeof setInterval>

onMounted(() => {
  loadMessages()
  loadUnreadCount()
  pollTimer = setInterval(loadUnreadCount, 30_000)
})

onUnmounted(() => clearInterval(pollTimer))

watch([activeFolder, page], loadMessages)
</script>

<style scoped>
/* ── 整体布局 ─────────────────────────────────────────────────────────────── */
.messages-layout {
  display: grid;
  grid-template-columns: 200px 320px 1fr;
  height: calc(100vh - 72px); /* 72px = header 高度，按需调整 */
  background: #f8f9fa;
  font-size: 14px;
  color: #333;
}

/* ── 左侧 Sidebar ─────────────────────────────────────────────────────────── */
.sidebar {
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e5e7eb;
  background: #fff;
  padding: 16px 0;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px 12px;
  border-bottom: 1px solid #f0f0f0;
}

.sidebar-title {
  font-weight: 600;
  font-size: 15px;
  color: #111;
}

.compose-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: #667eea;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.compose-btn:hover { background: #5a6ee0; }

.folder-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 12px 8px 0;
}

.folder-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  border: none;
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
  text-align: left;
  color: #555;
  transition: background 0.15s, color 0.15s;
  font-size: 14px;
  width: 100%;
}

.folder-item:hover         { background: #f3f4f6; }
.folder-item.active        { background: #eef2ff; color: #667eea; font-weight: 500; }
.folder-item.active .folder-icon { fill: #667eea; }

.folder-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  fill: #888;
}

.folder-label { flex: 1; }

.badge {
  min-width: 20px;
  height: 20px;
  padding: 0 5px;
  border-radius: 10px;
  background: #667eea;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ── 消息列表 ─────────────────────────────────────────────────────────────── */
.msg-list-pane {
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e5e7eb;
  background: #fff;
  overflow: hidden;
}

.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.list-title { font-weight: 600; font-size: 14px; }

.mark-all-btn {
  border: none;
  background: none;
  color: #667eea;
  font-size: 13px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.15s;
}

.mark-all-btn:hover { background: #eef2ff; }

.msg-list {
  list-style: none;
  margin: 0;
  padding: 0;
  overflow-y: auto;
  flex: 1;
}

.msg-item {
  position: relative;
  padding: 12px 16px 12px 24px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.15s;
}

.msg-item:hover    { background: #f9fafb; }
.msg-item.selected { background: #eef2ff; }
.msg-item.unread   { background: #fafbff; }

.unread-dot {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #667eea;
  opacity: 0;
  transition: opacity 0.2s;
}

.unread-dot.visible { opacity: 1; }

.msg-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 3px;
}

.msg-from { font-weight: 600; font-size: 13px; color: #111; }
.msg-time { font-size: 11px; color: #999; }

.msg-subject {
  font-size: 13px;
  color: #333;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.msg-preview {
  font-size: 12px;
  color: #888;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 分页 */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 10px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.pagination button {
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  padding: 4px 10px;
  cursor: pointer;
  font-size: 14px;
}

.pagination button:disabled { opacity: 0.4; cursor: default; }

/* ── 消息详情 / 撰写 ────────────────────────────────────────────────────── */
.detail-pane {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fff;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.detail-title {
  font-weight: 600;
  font-size: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 60%;
}

.detail-header-actions { display: flex; gap: 4px; }

.icon-btn {
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #555;
  transition: background 0.15s;
}

.icon-btn:hover { background: #f3f4f6; }
.icon-btn .icon { width: 18px; height: 18px; fill: currentColor; }

.reply-btn:hover { color: #667eea; background: #eef2ff; }

.detail-info {
  display: flex;
  gap: 20px;
  padding: 10px 20px;
  font-size: 12px;
  color: #888;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.detail-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #333;
}

/* 撰写表单 */
.compose-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-label { font-size: 12px; color: #666; font-weight: 500; }

.form-input {
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 8px 10px;
  font-size: 14px;
  outline: none;
  font-family: inherit;
  transition: border-color 0.2s;
}

.form-input:focus { border-color: #667eea; }

.form-textarea {
  resize: vertical;
  min-height: 120px;
}

.compose-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.send-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 8px;
  background: #667eea;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.send-btn:hover:not(:disabled) { background: #5a6ee0; }
.send-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.cancel-btn {
  padding: 8px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.15s;
}

.cancel-btn:hover { background: #f3f4f6; }

.send-error { color: #ef4444; font-size: 13px; margin-top: 4px; }

/* 快速回复框 */
.reply-box {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}

/* 空状态 */
.state-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 120px;
  color: #aaa;
  font-size: 14px;
}

.empty-detail {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 12px;
  color: #bbb;
}

.empty-icon {
  width: 48px;
  height: 48px;
  fill: #ddd;
}
</style>
