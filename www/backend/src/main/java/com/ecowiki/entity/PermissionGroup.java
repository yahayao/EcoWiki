package com.ecowiki.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "permission_group")
public class PermissionGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer groupId;

    @Column(nullable = false, unique = true, length = 50)
    private String groupKey;

    @Column(nullable = false, length = 100)
    private String groupName;

    @Column(columnDefinition = "TEXT")
    private String groupDescription;

    @Column(length = 50)
    private String icon;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "permissionGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<Permission> permissions;

    // Constructors
    public PermissionGroup() {}

    public PermissionGroup(String groupKey, String groupName, String groupDescription, String icon, Integer sortOrder) {
        this.groupKey = groupKey;
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.icon = icon;
        this.sortOrder = sortOrder;
    }

    // Getters and Setters
    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return "PermissionGroup{" +
                "groupId=" + groupId +
                ", groupKey='" + groupKey + '\'' +
                ", groupName='" + groupName + '\'' +
                ", sortOrder=" + sortOrder +
                ", isActive=" + isActive +
                '}';
    }
}
