package com.ecowiki.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "permission")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer permissionId;

    @Column(nullable = false)
    private String permissionName;

    private String description;

    @Column(name = "permission_key", length = 100)
    private String permissionKey;

    @Column(name = "group_id")
    private Integer groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private PermissionGroup permissionGroup;

    @Column(name = "is_system")
    private Boolean isSystem = false;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Getters and Setters
    public Integer getPermissionId() { return permissionId; }
    public void setPermissionId(Integer permissionId) { this.permissionId = permissionId; }

    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPermissionKey() { return permissionKey; }
    public void setPermissionKey(String permissionKey) { this.permissionKey = permissionKey; }

    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }

    public PermissionGroup getPermissionGroup() { return permissionGroup; }
    public void setPermissionGroup(PermissionGroup permissionGroup) { this.permissionGroup = permissionGroup; }

    public Boolean getIsSystem() { return isSystem; }
    public void setIsSystem(Boolean isSystem) { this.isSystem = isSystem; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}