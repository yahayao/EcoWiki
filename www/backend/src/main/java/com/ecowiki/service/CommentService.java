package com.ecowiki.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecowiki.dto.CommentDTO;
import com.ecowiki.dto.ReplyDTO;
import com.ecowiki.entity.Comment;
import com.ecowiki.entity.CommentLike;
import com.ecowiki.entity.User;
import com.ecowiki.repository.CommentLikeRepository;
import com.ecowiki.repository.CommentRepository;
import com.ecowiki.repository.UserRepository;

/**
 * 评论服务类
 * 
 * 提供评论相关的业务逻辑，匹配实际数据库结构
 * 
 * @author EcoWiki Team
 * @version 1.0.0
 * @since 2025-01-21
 */
@Service
@Transactional
public class CommentService {
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private CommentLikeRepository commentLikeRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 根据文章ID分页查询评论
     * 
     * @param articleId 文章ID
     * @param page 页码
     * @param size 每页大小
     * @param sort 排序方式
     * @param currentUser 当前用户（用于判断点赞状态）
     * @return 评论分页结果
     */
    public Page<CommentDTO> getCommentsByArticleId(Long articleId, int page, int size, String sort, String currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> commentPage;
        
        // 根据排序方式获取评论
        switch (sort.toLowerCase()) {
            case "hot":
                commentPage = commentRepository.findHotCommentsByArticleId(articleId, pageable);
                break;
            case "oldest":
                commentPage = commentRepository.findOldestCommentsByArticleId(articleId, pageable);
                break;
            case "newest":
            default:
                commentPage = commentRepository.findNewestCommentsByArticleId(articleId, pageable);
                break;
        }
        
        return commentPage.map(comment -> convertToDTO(comment, currentUser));
    }
    
    /**
     * 创建评论
     * 
     * @param articleId 文章ID
     * @param content 评论内容
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 创建的评论DTO
     */
    @Transactional
    public CommentDTO createComment(Long articleId, String content, String username, String ipAddress, String userAgent) {
        // 根据用户名获取用户ID
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(user.getUserId());
        comment.setContent(content);
        comment.setCommentTime(LocalDateTime.now());
        comment.setLikes(0);
        comment.setDeleted(false);
        
        Comment savedComment = commentRepository.save(comment);
        
        return convertToDTO(savedComment, username);
    }
    
    /**
     * 创建回复
     * 
     * @param parentCommentId 父评论ID
     * @param content 回复内容
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 创建的回复DTO
     */
    @Transactional
    public ReplyDTO createReply(Long parentCommentId, String content, String username, String ipAddress, String userAgent) {
        // 验证父评论存在
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("父评论不存在"));
        
        // 根据用户名获取用户ID
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        Comment reply = new Comment();
        reply.setArticleId(parentComment.getArticleId());
        reply.setUserId(user.getUserId());
        reply.setParentCommentId(parentCommentId);
        reply.setContent(content);
        reply.setCommentTime(LocalDateTime.now());
        reply.setLikes(0);
        reply.setDeleted(false);
        
        Comment savedReply = commentRepository.save(reply);
        
        return convertReplyToDTO(savedReply, username);
    }
    
    /**
     * 切换评论点赞状态
     * 
     * @param commentId 评论ID
     * @param username 用户名
     * @return 点赞结果
     */
    @Transactional
    public LikeResult toggleCommentLike(Long commentId, String username) {
        // 验证评论存在
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
        
        // 根据用户名获取用户ID
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        Optional<CommentLike> existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, user.getUserId());
        
        boolean isLiked;
        if (existingLike.isPresent()) {
            // 取消点赞
            commentLikeRepository.delete(existingLike.get());
            comment.decrementLikes();
            isLiked = false;
        } else {
            // 添加点赞
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(user.getUserId());
            like.setLikedAt(LocalDateTime.now());
            commentLikeRepository.save(like);
            comment.incrementLikes();
            isLiked = true;
        }
        
        commentRepository.save(comment);
        
        return new LikeResult(isLiked, comment.getLikes());
    }
    
    /**
     * 删除评论（软删除）
     * 
     * @param commentId 评论ID
     * @param username 用户名
     */
    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
        
        // 根据用户名获取用户ID
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        // 验证删除权限（只能删除自己的评论）
        if (!comment.getUserId().equals(user.getUserId())) {
            throw new RuntimeException("无权限删除此评论");
        }
        
        comment.softDelete();
        commentRepository.save(comment);
    }
    
    /**
     * 删除回复（软删除）
     * 
     * @param replyId 回复ID
     * @param username 用户名
     */
    @Transactional
    public void deleteReply(Long replyId, String username) {
        deleteComment(replyId, username); // 回复也是评论，使用相同的删除逻辑
    }
    
    /**
     * 获取评论统计信息
     * 
     * @param articleId 文章ID
     * @return 评论统计
     */
    public CommentStats getCommentStats(Long articleId) {
        long totalComments = commentRepository.countAllCommentsByArticleId(articleId);
        long topLevelComments = commentRepository.countTopLevelCommentsByArticleId(articleId);
        
        return new CommentStats(totalComments, topLevelComments);
    }
    
    // 私有辅助方法
    
    private CommentDTO convertToDTO(Comment comment, String currentUser) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getCommentId().toString());
        dto.setArticleId(comment.getArticleId());
        
        // 根据用户ID获取用户名
        Optional<User> userOpt = userRepository.findByUserId(comment.getUserId());
        String author = userOpt.map(User::getUsername).orElse("匿名用户");
        dto.setAuthor(author);
        
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCommentTime().toString());
        dto.setLikes(comment.getLikes());
        
        // 判断当前用户是否点赞了这条评论
        boolean isLiked = false;
        if (currentUser != null) {
            Optional<User> currentUserOpt = userService.findByUsername(currentUser);
            if (currentUserOpt.isPresent()) {
                isLiked = commentLikeRepository.findByCommentIdAndUserId(
                    comment.getCommentId(), currentUserOpt.get().getUserId()).isPresent();
            }
        }
        dto.setIsLiked(isLiked);
        
        // 加载回复
        List<Comment> replies = commentRepository.findRepliesByParentId(comment.getCommentId());
        List<ReplyDTO> replyDTOs = replies.stream()
                .map(reply -> convertReplyToDTO(reply, currentUser))
                .collect(Collectors.toList());
        dto.setReplies(replyDTOs);
        
        return dto;
    }
    
    private ReplyDTO convertReplyToDTO(Comment reply, String currentUser) {
        ReplyDTO dto = new ReplyDTO();
        dto.setId(reply.getCommentId().toString());
        dto.setCommentId(reply.getParentCommentId().toString());
        
        // 根据用户ID获取用户名
        Optional<User> userOpt = userRepository.findByUserId(reply.getUserId());
        String author = userOpt.map(User::getUsername).orElse("匿名用户");
        dto.setAuthor(author);
        
        dto.setContent(reply.getContent());
        dto.setCreatedAt(reply.getCommentTime().toString());
        dto.setLikes(reply.getLikes());
        
        // 判断当前用户是否点赞了这条回复
        boolean isLiked = false;
        if (currentUser != null) {
            Optional<User> currentUserOpt = userService.findByUsername(currentUser);
            if (currentUserOpt.isPresent()) {
                isLiked = commentLikeRepository.findByCommentIdAndUserId(
                    reply.getCommentId(), currentUserOpt.get().getUserId()).isPresent();
            }
        }
        dto.setIsLiked(isLiked);
        
        return dto;
    }
    
    /**
     * 点赞结果内部类
     */
    public static class LikeResult {
        private final Boolean liked;
        private final Integer likeCount;
        
        public LikeResult(Boolean liked, Integer likeCount) {
            this.liked = liked;
            this.likeCount = likeCount;
        }
        
        public Boolean isLiked() {
            return liked;
        }
        
        public Integer getLikeCount() {
            return likeCount;
        }
    }
    
    /**
     * 评论统计内部类
     */
    public static class CommentStats {
        private final long totalComments;
        private final long topLevelComments;
        
        public CommentStats(long totalComments, long topLevelComments) {
            this.totalComments = totalComments;
            this.topLevelComments = topLevelComments;
        }
        
        public long getTotalComments() {
            return totalComments;
        }
        
        public long getTopLevelComments() {
            return topLevelComments;
        }
    }
}
