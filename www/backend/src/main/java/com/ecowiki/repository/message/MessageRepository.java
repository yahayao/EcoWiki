package com.ecowiki.repository.message;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ecowiki.entity.message.Message;

/**
 * 消息数据访问接口
 * 
 * 提供消息的CRUD操作和查询功能
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    
    /**
     * 根据接收用户ID查询消息（分页）
     * @param recipientUserId 接收用户ID
     * @param pageable 分页参数
     * @return 消息分页列表
     */
    @Query("SELECT m FROM Message m WHERE m.recipientUserId = :recipientUserId ORDER BY m.sendTime DESC")
    Page<Message> findByRecipientUserId(@Param("recipientUserId") Integer recipientUserId, Pageable pageable);
    
    /**
     * 根据发送用户ID查询消息（分页）
     * @param senderUserId 发送用户ID
     * @param pageable 分页参数
     * @return 消息分页列表
     */
    @Query("SELECT m FROM Message m WHERE m.senderUserId = :senderUserId ORDER BY m.sendTime DESC")
    Page<Message> findBySenderUserId(@Param("senderUserId") Integer senderUserId, Pageable pageable);
    
    /**
     * 查询用户的对话（发送或接收的消息）
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 消息分页列表
     */
    @Query("SELECT m FROM Message m WHERE m.recipientUserId = :userId OR m.senderUserId = :userId ORDER BY m.sendTime DESC")
    Page<Message> findUserMessages(@Param("userId") Integer userId, Pageable pageable);
    
    /**
     * 查询两个用户之间的对话
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @param pageable 分页参数
     * @return 消息分页列表
     */
    @Query("SELECT m FROM Message m WHERE " +
           "(m.recipientUserId = :userId1 AND m.senderUserId = :userId2) OR " +
           "(m.recipientUserId = :userId2 AND m.senderUserId = :userId1) " +
           "ORDER BY m.sendTime DESC")
    Page<Message> findConversation(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2, Pageable pageable);
    
    /**
     * 统计用户未读消息数量
     * @param recipientUserId 接收用户ID
     * @return 未读消息数量
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipientUserId = :recipientUserId AND m.status = '未读'")
    Long countUnreadMessages(@Param("recipientUserId") Integer recipientUserId);
    
    /**
     * 查询用户未读消息列表
     * @param recipientUserId 接收用户ID
     * @return 未读消息列表
     */
    @Query("SELECT m FROM Message m WHERE m.recipientUserId = :recipientUserId AND m.status = '未读' ORDER BY m.sendTime DESC")
    List<Message> findUnreadMessages(@Param("recipientUserId") Integer recipientUserId);
    
    /**
     * 标记消息为已读
     * @param messageId 消息ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = '已读' WHERE m.messageId = :messageId")
    void markAsRead(@Param("messageId") Integer messageId);
    
    /**
     * 批量标记用户的所有未读消息为已读
     * @param recipientUserId 接收用户ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = '已读' WHERE m.recipientUserId = :recipientUserId AND m.status = '未读'")
    void markAllAsRead(@Param("recipientUserId") Integer recipientUserId);
    
    /**
     * 删除用户的所有消息（软删除或硬删除）
     * @param userId 用户ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Message m WHERE m.recipientUserId = :userId OR m.senderUserId = :userId")
    void deleteUserMessages(@Param("userId") Integer userId);
}
