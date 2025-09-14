/**
 * 改进的消息数据访问层接口
 * 
 * 功能：
 * - 提供消息的数据库操作接口
 * - 支持多种查询条件和状态筛选
 * - 包含软删除和过期消息处理
 * - 提供性能优化的批量操作
 * 
 * @author EcoWiki开发团队
 * @version 2.0.0
 * @since 2025-09-14
 */
package com.ecowiki.repository.message;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ecowiki.entity.message.MessageRefactored;
import com.ecowiki.enums.message.MessageStatus;
import com.ecowiki.enums.message.MessageType;

@Repository
public interface MessageRefactoredRepository extends JpaRepository<MessageRefactored, Integer> {
    
    // ===== 基础查询方法 =====
    
    /**
     * 根据接收用户ID查询可见消息（分页）
     */
    @Query("SELECT m FROM MessageRefactored m WHERE m.recipientUserId = :recipientUserId " +
           "AND m.status != :deletedStatus AND (m.expireTime IS NULL OR m.expireTime > :now) " +
           "ORDER BY m.priority DESC, m.sendTime DESC")
    Page<MessageRefactored> findVisibleByRecipientUserId(
            @Param("recipientUserId") Integer recipientUserId, 
            @Param("deletedStatus") MessageStatus deletedStatus,
            @Param("now") LocalDateTime now,
            Pageable pageable);
    
    /**
     * 根据接收用户ID查询可见消息（分页）- 简化版本
     */
    default Page<MessageRefactored> findVisibleByRecipientUserId(Integer recipientUserId, Pageable pageable) {
        return findVisibleByRecipientUserId(recipientUserId, MessageStatus.DELETED, LocalDateTime.now(), pageable);
    }
    
    /**
     * 根据发送用户ID查询可见消息（分页）
     */
    @Query("SELECT m FROM MessageRefactored m WHERE m.senderUserId = :senderUserId " +
           "AND m.status != :deletedStatus AND (m.expireTime IS NULL OR m.expireTime > :now) " +
           "ORDER BY m.priority DESC, m.sendTime DESC")
    Page<MessageRefactored> findVisibleBySenderUserId(
            @Param("senderUserId") Integer senderUserId,
            @Param("deletedStatus") MessageStatus deletedStatus,
            @Param("now") LocalDateTime now,
            Pageable pageable);
    
    /**
     * 根据发送用户ID查询可见消息（分页）- 简化版本
     */
    default Page<MessageRefactored> findVisibleBySenderUserId(Integer senderUserId, Pageable pageable) {
        return findVisibleBySenderUserId(senderUserId, MessageStatus.DELETED, LocalDateTime.now(), pageable);
    }
    
    /**
     * 查询用户的所有可见消息（发送或接收）
     */
    @Query("SELECT m FROM MessageRefactored m WHERE (m.recipientUserId = :userId OR m.senderUserId = :userId) " +
           "AND m.status != :deletedStatus AND (m.expireTime IS NULL OR m.expireTime > :now) " +
           "ORDER BY m.priority DESC, m.sendTime DESC")
    Page<MessageRefactored> findVisibleUserMessages(
            @Param("userId") Integer userId,
            @Param("deletedStatus") MessageStatus deletedStatus,
            @Param("now") LocalDateTime now,
            Pageable pageable);
    
    /**
     * 查询用户的所有可见消息（发送或接收）- 简化版本
     */
    default Page<MessageRefactored> findVisibleUserMessages(Integer userId, Pageable pageable) {
        return findVisibleUserMessages(userId, MessageStatus.DELETED, LocalDateTime.now(), pageable);
    }
    
    /**
     * 查询两个用户之间的可见对话
     */
    @Query("SELECT m FROM MessageRefactored m WHERE " +
           "((m.recipientUserId = :userId1 AND m.senderUserId = :userId2) OR " +
           "(m.recipientUserId = :userId2 AND m.senderUserId = :userId1)) " +
           "AND m.status != :deletedStatus AND (m.expireTime IS NULL OR m.expireTime > :now) " +
           "ORDER BY m.sendTime DESC")
    Page<MessageRefactored> findVisibleConversation(
            @Param("userId1") Integer userId1, 
            @Param("userId2") Integer userId2,
            @Param("deletedStatus") MessageStatus deletedStatus,
            @Param("now") LocalDateTime now,
            Pageable pageable);
    
    /**
     * 查询两个用户之间的可见对话 - 简化版本
     */
    default Page<MessageRefactored> findVisibleConversation(Integer userId1, Integer userId2, Pageable pageable) {
        return findVisibleConversation(userId1, userId2, MessageStatus.DELETED, LocalDateTime.now(), pageable);
    }
    
    // ===== 按类型和状态查询 =====
    
    /**
     * 根据用户ID和消息类型查询可见消息
     */
    @Query("SELECT m FROM MessageRefactored m WHERE m.recipientUserId = :userId " +
           "AND m.messageType = :messageType " +
           "AND m.status != :deletedStatus AND (m.expireTime IS NULL OR m.expireTime > :now) " +
           "ORDER BY m.priority DESC, m.sendTime DESC")
    Page<MessageRefactored> findVisibleByUserIdAndType(
            @Param("userId") Integer userId,
            @Param("messageType") MessageType messageType,
            @Param("deletedStatus") MessageStatus deletedStatus,
            @Param("now") LocalDateTime now,
            Pageable pageable);
    
    /**
     * 根据用户ID和消息类型查询可见消息 - 简化版本
     */
    default Page<MessageRefactored> findVisibleByUserIdAndType(Integer userId, MessageType messageType, Pageable pageable) {
        return findVisibleByUserIdAndType(userId, messageType, MessageStatus.DELETED, LocalDateTime.now(), pageable);
    }
    
    /**
     * 根据接收用户ID和状态查询可见消息
     */
    @Query("SELECT m FROM MessageRefactored m WHERE m.recipientUserId = :userId " +
           "AND m.status = :status AND m.status != :deletedStatus " +
           "AND (m.expireTime IS NULL OR m.expireTime > :now) " +
           "ORDER BY m.priority DESC, m.sendTime DESC")
    Page<MessageRefactored> findVisibleByRecipientUserIdAndStatus(
            @Param("userId") Integer userId,
            @Param("status") MessageStatus status,
            @Param("deletedStatus") MessageStatus deletedStatus,
            @Param("now") LocalDateTime now,
            Pageable pageable);
    
    /**
     * 根据接收用户ID和状态查询可见消息 - 简化版本
     */
    default Page<MessageRefactored> findVisibleByRecipientUserIdAndStatus(Integer userId, MessageStatus status, Pageable pageable) {
        return findVisibleByRecipientUserIdAndStatus(userId, status, MessageStatus.DELETED, LocalDateTime.now(), pageable);
    }
    
    // ===== 统计查询方法 =====
    
    /**
     * 统计用户未读可见消息数量
     */
    @Query("SELECT COUNT(m) FROM MessageRefactored m WHERE m.recipientUserId = :recipientUserId " +
           "AND m.status = :unreadStatus AND m.status != :deletedStatus " +
           "AND (m.expireTime IS NULL OR m.expireTime > :now)")
    Long countUnreadVisibleMessages(
            @Param("recipientUserId") Integer recipientUserId,
            @Param("unreadStatus") MessageStatus unreadStatus,
            @Param("deletedStatus") MessageStatus deletedStatus,
            @Param("now") LocalDateTime now);
    
    /**
     * 统计用户未读可见消息数量 - 简化版本
     */
    default Long countUnreadVisibleMessages(Integer recipientUserId) {
        return countUnreadVisibleMessages(recipientUserId, MessageStatus.UNREAD, MessageStatus.DELETED, LocalDateTime.now());
    }
    
    /**
     * 统计用户特定类型的可见消息数量
     */
    @Query("SELECT COUNT(m) FROM MessageRefactored m WHERE m.recipientUserId = :userId " +
           "AND m.messageType = :messageType " +
           "AND m.status != :deletedStatus AND (m.expireTime IS NULL OR m.expireTime > :now)")
    Long countVisibleByUserIdAndType(
            @Param("userId") Integer userId,
            @Param("messageType") MessageType messageType,
            @Param("deletedStatus") MessageStatus deletedStatus,
            @Param("now") LocalDateTime now);
    
    /**
     * 统计用户特定类型的可见消息数量 - 简化版本
     */
    default Long countVisibleByUserIdAndType(Integer userId, MessageType messageType) {
        return countVisibleByUserIdAndType(userId, messageType, MessageStatus.DELETED, LocalDateTime.now());
    }
    
    // ===== 未读消息查询 =====
    
    /**
     * 查询用户未读可见消息列表
     */
    @Query("SELECT m FROM MessageRefactored m WHERE m.recipientUserId = :recipientUserId " +
           "AND m.status = :unreadStatus AND m.status != :deletedStatus " +
           "AND (m.expireTime IS NULL OR m.expireTime > :now) " +
           "ORDER BY m.priority DESC, m.sendTime DESC")
    List<MessageRefactored> findUnreadVisibleMessages(
            @Param("recipientUserId") Integer recipientUserId,
            @Param("unreadStatus") MessageStatus unreadStatus,
            @Param("deletedStatus") MessageStatus deletedStatus,
            @Param("now") LocalDateTime now);
    
    /**
     * 查询用户未读可见消息列表 - 简化版本
     */
    default List<MessageRefactored> findUnreadVisibleMessages(Integer recipientUserId) {
        return findUnreadVisibleMessages(recipientUserId, MessageStatus.UNREAD, MessageStatus.DELETED, LocalDateTime.now());
    }
    
    // ===== 批量操作方法 =====
    
    /**
     * 批量标记用户的所有未读可见消息为已读
     */
    @Modifying
    @Transactional
    @Query("UPDATE MessageRefactored m SET m.status = :readStatus, m.readTime = :readTime, m.updatedAt = :now " +
           "WHERE m.recipientUserId = :recipientUserId AND m.status = :unreadStatus " +
           "AND m.status != :deletedStatus AND (m.expireTime IS NULL OR m.expireTime > :now) " +
           "AND m.senderUserId > 0")
    int markAllUnreadAsRead(
            @Param("recipientUserId") Integer recipientUserId,
            @Param("readStatus") MessageStatus readStatus,
            @Param("unreadStatus") MessageStatus unreadStatus,
            @Param("deletedStatus") MessageStatus deletedStatus,
            @Param("readTime") LocalDateTime readTime,
            @Param("now") LocalDateTime now);
    
    /**
     * 批量标记用户的所有未读可见消息为已读 - 简化版本
     */
    default int markAllUnreadAsRead(Integer recipientUserId) {
        LocalDateTime now = LocalDateTime.now();
        return markAllUnreadAsRead(recipientUserId, MessageStatus.VIEWED, MessageStatus.UNREAD, 
                                 MessageStatus.DELETED, now, now);
    }
    
    // ===== 过期消息处理 =====
    
    /**
     * 查询所有过期但未删除的消息
     */
    @Query("SELECT m FROM MessageRefactored m WHERE m.expireTime IS NOT NULL " +
           "AND m.expireTime <= :now AND m.status != :deletedStatus")
    List<MessageRefactored> findExpiredMessages(
            @Param("now") LocalDateTime now,
            @Param("deletedStatus") MessageStatus deletedStatus);
    
    /**
     * 查询所有过期但未删除的消息 - 简化版本
     */
    default List<MessageRefactored> findExpiredMessages() {
        return findExpiredMessages(LocalDateTime.now(), MessageStatus.DELETED);
    }
    
    // ===== 兼容性方法（与旧版本保持兼容） =====
    
    /**
     * 根据接收用户ID查询消息（包含已删除）- 兼容旧版本
     */
    @Query("SELECT m FROM MessageRefactored m WHERE m.recipientUserId = :recipientUserId ORDER BY m.sendTime DESC")
    Page<MessageRefactored> findByRecipientUserId(@Param("recipientUserId") Integer recipientUserId, Pageable pageable);
    
    /**
     * 根据发送用户ID查询消息（包含已删除）- 兼容旧版本
     */
    @Query("SELECT m FROM MessageRefactored m WHERE m.senderUserId = :senderUserId ORDER BY m.sendTime DESC")
    Page<MessageRefactored> findBySenderUserId(@Param("senderUserId") Integer senderUserId, Pageable pageable);
}