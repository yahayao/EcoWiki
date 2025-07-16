/**
 * 消息API模块
 * 
 * 提供消息发送、接收、查询等功能的API接口
 */

import { api } from './index'

/**
 * 消息数据传输对象
 */
export interface MessageDto {
  /** 消息ID */
  messageId: number
  /** 接收用户ID */
  recipientUserId: number
  /** 接收用户名 */
  recipientUsername?: string
  /** 发送用户ID */
  senderUserId: number
  /** 发送用户名 */
  senderUsername?: string
  /** 消息内容 */
  content: string
  /** 发送时间 */
  sendTime: string
  /** 消息状态：未读、已读 */
  status: string
}

/**
 * 发送消息请求对象
 */
export interface SendMessageRequest {
  /** 接收用户ID */
  recipientUserId: number
  /** 消息内容 */
  content: string
}

/**
 * 群发消息请求对象
 */
export interface BroadcastMessageRequest {
  /** 接收用户ID列表 */
  recipientUserIds: number[]
  /** 消息内容 */
  content: string
}

/**
 * 分页响应对象
 */
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
}

/**
 * API响应格式
 */
interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

/**
 * 消息API类
 */
export const messageApi = {
  /**
   * 发送消息
   * @param request 发送消息请求
   * @returns 发送结果
   */
  async sendMessage(request: SendMessageRequest): Promise<MessageDto> {
    const response = await api.post<ApiResponse<MessageDto>>('/api/messages/send', request)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '发送消息失败')
    }
    return response.data.data
  },

  /**
   * 群发消息
   * @param request 群发消息请求
   * @returns 发送结果
   */
  async broadcastMessage(request: BroadcastMessageRequest): Promise<MessageDto[]> {
    const response = await api.post<ApiResponse<MessageDto[]>>('/api/messages/broadcast', request)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '群发消息失败')
    }
    return response.data.data
  },

  /**
   * 获取收到的消息
   * @param page 页码（从0开始）
   * @param size 每页大小
   * @returns 消息分页列表
   */
  async getReceivedMessages(page: number = 0, size: number = 20): Promise<PageResponse<MessageDto>> {
    const response = await api.get<ApiResponse<PageResponse<MessageDto>>>(
      `/api/messages/received?page=${page}&size=${size}`
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取消息失败')
    }
    return response.data.data
  },

  /**
   * 获取发送的消息
   * @param page 页码（从0开始）
   * @param size 每页大小
   * @returns 消息分页列表
   */
  async getSentMessages(page: number = 0, size: number = 20): Promise<PageResponse<MessageDto>> {
    const response = await api.get<ApiResponse<PageResponse<MessageDto>>>(
      `/api/messages/sent?page=${page}&size=${size}`
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取消息失败')
    }
    return response.data.data
  },

  /**
   * 获取所有消息（收发）
   * @param page 页码（从0开始）
   * @param size 每页大小
   * @returns 消息分页列表
   */
  async getAllMessages(page: number = 0, size: number = 20): Promise<PageResponse<MessageDto>> {
    const response = await api.get<ApiResponse<PageResponse<MessageDto>>>(
      `/api/messages/all?page=${page}&size=${size}`
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取消息失败')
    }
    return response.data.data
  },

  /**
   * 获取与指定用户的对话
   * @param userId 对方用户ID
   * @param page 页码（从0开始）
   * @param size 每页大小
   * @returns 对话消息列表
   */
  async getConversation(userId: number, page: number = 0, size: number = 20): Promise<PageResponse<MessageDto>> {
    const response = await api.get<ApiResponse<PageResponse<MessageDto>>>(
      `/api/messages/conversation/${userId}?page=${page}&size=${size}`
    )
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取对话失败')
    }
    return response.data.data
  },

  /**
   * 获取未读消息数量
   * @returns 未读消息数量
   */
  async getUnreadCount(): Promise<number> {
    const response = await api.get<ApiResponse<number>>('/api/messages/unread/count')
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取未读数量失败')
    }
    return response.data.data
  },

  /**
   * 获取未读消息列表
   * @returns 未读消息列表
   */
  async getUnreadMessages(): Promise<MessageDto[]> {
    const response = await api.get<ApiResponse<MessageDto[]>>('/api/messages/unread')
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '获取未读消息失败')
    }
    return response.data.data
  },

  /**
   * 标记消息为已读
   * @param messageId 消息ID
   */
  async markAsRead(messageId: number): Promise<void> {
    const response = await api.put<ApiResponse<null>>(`/api/messages/${messageId}/read`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '标记已读失败')
    }
  },

  /**
   * 标记所有消息为已读
   */
  async markAllAsRead(): Promise<void> {
    const response = await api.put<ApiResponse<null>>('/api/messages/read-all')
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '标记全部已读失败')
    }
  },

  /**
   * 删除消息
   * @param messageId 消息ID
   */
  async deleteMessage(messageId: number): Promise<void> {
    const response = await api.delete<ApiResponse<null>>(`/api/messages/${messageId}`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '删除消息失败')
    }
  }
}
