package com.ecowiki.dto;

import java.util.List;

public class PermissionGroupDto {
    private Integer groupId;
    private String groupKey;
    private String groupName;
    private String groupDescription;
    private String icon;
    private Integer sortOrder;
    private Boolean isActive;
    private List<PermissionDto> permissions;

    // Constructors
    public PermissionGroupDto() {}

    public PermissionGroupDto(Integer groupId, String groupKey, String groupName, 
                             String groupDescription, String icon, Integer sortOrder, Boolean isActive) {
        this.groupId = groupId;
        this.groupKey = groupKey;
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.icon = icon;
        this.sortOrder = sortOrder;
        this.isActive = isActive;
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

    public List<PermissionDto> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionDto> permissions) {
        this.permissions = permissions;
    }
}
