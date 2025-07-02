package com.ecowiki.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

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
}