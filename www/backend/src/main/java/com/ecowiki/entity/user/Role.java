package com.ecowiki.entity.user;

/*
  角色实体类
  <p>
  对应数据库中的role表，描述系统中的权限角色（如user、moderator、admin、superadmin等）。
  支持角色名称、描述、创建/更新时间等字段。
  <p>
  <b>设计说明：</b>
  - 角色体系支持灵活扩展，便于权限分级和管理。
  - 适用于用户权限分配、后台角色管理、权限校验等场景。

  @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "role")
public class Role {
    /**
     * 角色主键ID，自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    /**
     * 角色名称（如user、admin等），不可为空
     */
    @Column(name = "role_name", nullable = false)
    private String roleName;

    /**
     * 角色描述信息
     */
    @Column(name = "description")
    private String description;

    /**
     * 创建时间（只写）
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}