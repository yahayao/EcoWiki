package com.ecowiki.entity;

import java.io.Serializable;
import java.util.Objects;

public class RolePermissionId implements Serializable {
    private Integer roleId;
    private Integer permissionId;
    
    public RolePermissionId() {}
    
    public RolePermissionId(Integer roleId, Integer permissionId) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }
    
    public Integer getRoleId() {
        return roleId;
    }
    
    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }
    
    public Integer getPermissionId() {
        return permissionId;
    }
    
    public void setPermissionId(Integer permissionId) {
        this.permissionId = permissionId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RolePermissionId)) return false;
        RolePermissionId that = (RolePermissionId) o;
        return Objects.equals(roleId, that.roleId) && Objects.equals(permissionId, that.permissionId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(roleId, permissionId);
    }
}
