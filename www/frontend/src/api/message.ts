import { api } from './index'

// ── 类型定义 ──────────────────────────────────────────────────────────────────

/** 与项目其他 API 模块保持一致的通用响应包装器 */
export interface ApiResponse<T> {
  code: number
  success: boolean
  message: string
  data: T
}

export interface Message {
  messageId: number
  senderUserId: number | null
  recipientUserId: number
  subject: string | null
  content: string
  messageType: string
  priority: number
  status: 'UNREAD' | 'READ' | 'DELETED'
  sendTime: string
  readTime: string | null
  senderUsername: string | null
  recipientUsername: string | null
}

export interface SendMessageRequest {
  recipientUserId: number
  subject?: string
  content: string
  messageType?: string
  priority?: number
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
  numberOfElements: number
}

// ── API 方法 ──────────────────────────────────────────────────────────────────

export const messageApi = {
  /** 获取收件箱（分页） */
  getInbox(page = 0, size = 20, msgType?: string) {
    return api.get<ApiResponse<PageResult<Message>>>('/api/messages', {
      params: { page, size, msg_type: msgType },
    })
  },

  /** 获取已发送（分页） */
  getSent(page = 0, size = 20) {
    return api.get<ApiResponse<PageResult<Message>>>('/api/messages/sent', {
      params: { page, size },
    })
  },

  /** 获取未读消息数 */
  getUnreadCount() {
    return api.get<ApiResponse<{ count: number }>>('/api/messages/unread/count')
  },

  /** 获取未读消息列表 */
  getUnreadList() {
    return api.get<ApiResponse<Message[]>>('/api/messages/unread')
  },

  /** 发送消息 */
  send(body: SendMessageRequest) {
    return api.post<ApiResponse<Message>>('/api/messages', body)
  },

  /** 标记单条已读 */
  markRead(messageId: number) {
    return api.put<ApiResponse<null>>(`/api/messages/${messageId}/read`)
  },

  /** 全部标记已读 */
  markAllRead() {
    return api.put<ApiResponse<null>>('/api/messages/read-all')
  },

  /** 删除消息（软删除） */
  remove(messageId: number) {
    return api.delete<ApiResponse<null>>(`/api/messages/${messageId}`)
  },

  /** 获取与某用户的对话 */
  getConversation(userId: number, page = 0, size = 20) {
    return api.get<ApiResponse<PageResult<Message>>>(`/api/messages/conversation/${userId}`, {
      params: { page, size },
    })
  },
}
