/**
 * 消息中心状态管理 Pinia Store
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { messageApi, type Message, type SendMessageRequest } from '../api/message'
import { useAuthStore } from './authStore'

export const useMessageStore = defineStore('message', () => {
  const inbox = ref<Message[]>([])
  const sent = ref<Message[]>([])
  const unreadCount = ref(0)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const inboxPage = ref(0)
  const sentPage = ref(0)
  const pageSize = ref(20)
  const inboxTotal = ref(0)
  const sentTotal = ref(0)

  let pollTimer: ReturnType<typeof setInterval> | null = null

  const hasUnread = computed(() => unreadCount.value > 0)
  const unreadBadge = computed(() => unreadCount.value > 99 ? '99+' : String(unreadCount.value))

  const fetchInbox = async (page?: number, size?: number) => {
    loading.value = true; error.value = null
    try {
      const res = await messageApi.getInbox(page ?? inboxPage.value, size ?? pageSize.value)
      if (res.data?.code === 200) {
        inbox.value = res.data.data?.content || []
        inboxTotal.value = res.data.data?.totalElements || 0
        inboxPage.value = res.data.data?.page ?? inboxPage.value
      } else throw new Error(res.data?.message || '获取收件箱失败')
    } catch (err: any) { error.value = err.message || '获取收件箱失败' }
    finally { loading.value = false }
  }

  const fetchSent = async (page?: number, size?: number) => {
    loading.value = true; error.value = null
    try {
      const res = await messageApi.getSent(page ?? sentPage.value, size ?? pageSize.value)
      if (res.data?.code === 200) {
        sent.value = res.data.data?.content || []
        sentTotal.value = res.data.data?.totalElements || 0
        sentPage.value = res.data.data?.page ?? sentPage.value
      } else throw new Error(res.data?.message || '获取发件箱失败')
    } catch (err: any) { error.value = err.message || '获取发件箱失败' }
    finally { loading.value = false }
  }

  const fetchUnreadCount = async () => {
    const authStore = useAuthStore()
    if (!authStore.isAuthenticated) { unreadCount.value = 0; return }
    try {
      const res = await messageApi.getUnreadCount()
      unreadCount.value = res.data?.data?.count ?? 0
    } catch { /* ignore */ }
  }

  const sendMessage = async (data: SendMessageRequest) => {
    loading.value = true; error.value = null
    try {
      const res = await messageApi.sendMessage(data)
      if (res.data?.code !== 200) throw new Error(res.data?.message || '发送消息失败')
      await fetchSent(); return res.data
    } catch (err: any) { error.value = err.message || '发送消息失败'; throw err }
    finally { loading.value = false }
  }

  const markAsRead = async (messageId: number) => {
    try {
      await messageApi.markAsRead(messageId)
      const msg = inbox.value.find(m => m.messageId === messageId)
      if (msg) { msg.status = 'READ'; msg.readTime = new Date().toISOString() }
      await fetchUnreadCount()
    } catch { /* ignore */ }
  }

  const deleteMessage = async (messageId: number) => {
    try {
      const res = await messageApi.deleteMessage(messageId)
      if (res.data?.code === 200) {
        inbox.value = inbox.value.filter(m => m.messageId !== messageId)
        sent.value = sent.value.filter(m => m.messageId !== messageId)
        await fetchUnreadCount()
      }
    } catch (err: any) { throw err }
  }

  const fetchConversation = async (userId: number) => {
    loading.value = true; error.value = null
    try {
      const res = await messageApi.getConversation(userId)
      return res.data?.code === 200 ? (res.data.data || []) : []
    } catch (err: any) { error.value = err.message || '获取对话失败'; return [] }
    finally { loading.value = false }
  }

  const startPolling = (intervalMs = 30000) => {
    stopPolling(); fetchUnreadCount()
    pollTimer = setInterval(fetchUnreadCount, intervalMs)
  }
  const stopPolling = () => { if (pollTimer) { clearInterval(pollTimer); pollTimer = null } }

  const setInboxPage = (page: number) => { inboxPage.value = page; fetchInbox() }
  const setSentPage = (page: number) => { sentPage.value = page; fetchSent() }

  return {
    inbox, sent, unreadCount, loading, error, inboxPage, sentPage, pageSize, inboxTotal, sentTotal,
    hasUnread, unreadBadge,
    fetchInbox, fetchSent, fetchUnreadCount, sendMessage, markAsRead, deleteMessage,
    fetchConversation, startPolling, stopPolling, setInboxPage, setSentPage
  }
})
