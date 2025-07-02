package com.ecowiki.dto;

public class PermissionDto {
    private Integer permissionId;
    private String permissionName;
    private String description;
    private String permissionKey;
    private Integer groupId;
    private String groupName;  // 方便前端显示
    private Boolean isSystem;
    private Integer sortOrder;

    // Constructors
    public PermissionDto() {}

    public PermissionDto(Integer permissionId, String permissionName, String description, 
                        String permissionKey, Integer groupId, Boolean isSystem, Integer sortOrder) {
        this.permissionId = permissionId;
        this.permissionName = permissionName;
        this.description = description;
        this.permissionKey = permissionKey;
        this.groupId = groupId;
        this.isSystem = isSystem;
        this.sortOrder = sortOrder;
    }

    // Getters and Setters
    public Integer getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Integer permissionId) {
        this.permissionId = permissionId;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPermissionKey() {
        return permissionKey;
    }

    public void setPermissionKey(String permissionKey) {
        this.permissionKey = permissionKey;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}