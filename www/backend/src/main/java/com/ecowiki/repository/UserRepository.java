package com.ecowiki.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.User;

/**
 * 用户数据访问接口
 * <p>
 * 继承JpaRepository，提供用户实体的基础CRUD操作和自定义查询方法。
 * 支持用户名、邮箱查询、存在性检查、活跃用户统计等功能。
 * <p>
 * <b>设计说明：</b>
 * - 基于Spring Data JPA，自动生成基础CRUD方法
 * - 支持方法名称查询和@Query自定义查询
 * - 适用于用户认证、权限管理、数据统计等场景
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户实体（可能为空）
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     * @param email 邮箱地址
     * @return 用户实体（可能为空）
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否已存在
     * @param email 邮箱地址
     * @return 是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 统计激活用户数量
     * @return 激活用户总数
     */
    long countByActiveTrue();
    
    /**
     * 查找所有激活用户
     * @return 激活用户列表
     */
    @Query("SELECT u FROM User u WHERE u.active = true")
    java.util.List<User> findAllActiveUsers();
    
    /**
     * 根据用户ID查找用户（无active状态过滤）
     * @param userId 用户ID
     * @return 用户实体（可能为空）
     */
    Optional<User> findByUserId(Long userId);
    
    /**
     * 查找所有激活用户（分页，用于管理界面）
     * @return 包含激活和非激活用户的分页结果
     */
    @Query("SELECT u FROM User u ORDER BY u.userId DESC")
    Page<User> findAllForAdmin(Pageable pageable);
    
    /**
     * 仅查找激活用户（分页）
     * @return 仅包含激活用户的分页结果
     */
    @Query("SELECT u FROM User u WHERE u.active = true ORDER BY u.userId DESC")
    Page<User> findAllActiveUsersPage(Pageable pageable);
    
    /**
     * 统计非激活（软删除）用户数量
     * @return 非激活用户总数
     */
    long countByActiveFalse();
}