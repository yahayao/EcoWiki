package com.ecowiki.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.PermissionGroup;

@Repository
public interface PermissionGroupRepository extends JpaRepository<PermissionGroup, Integer> {
    
    /**
     * 查找所有活跃的权限分组，按排序顺序
     */
    List<PermissionGroup> findByIsActiveTrueOrderBySortOrder();
    
    /**
     * 根据分组key查找权限分组
     */
    Optional<PermissionGroup> findByGroupKey(String groupKey);
    
    /**
     * 检查分组key是否存在（排除指定ID）
     */
    boolean existsByGroupKeyAndGroupIdNot(String groupKey, Integer groupId);
    
    /**
     * 查询权限分组及其权限（使用JOIN FETCH避免N+1问题）
     */
    @Query("SELECT pg FROM PermissionGroup pg LEFT JOIN FETCH pg.permissions p WHERE pg.isActive = true ORDER BY pg.sortOrder, p.sortOrder")
    List<PermissionGroup> findAllWithPermissions();
    
    /**
     * 根据ID查找权限分组及其权限
     */
    @Query("SELECT pg FROM PermissionGroup pg LEFT JOIN FETCH pg.permissions p WHERE pg.groupId = :groupId")
    Optional<PermissionGroup> findByIdWithPermissions(Integer groupId);
}
