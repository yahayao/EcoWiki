package com.ecowiki.entity;

import java.io.Serializable;
import java.util.Objects;

public class UserRoleId implements Serializable {
    private Integer userId;
    private Integer roleId;

    // 默认构造函数
    public UserRoleId() {}

    public UserRoleId(Integer userId, Integer roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    // Getters and Setters
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRoleId that = (UserRoleId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId);
    }
}
