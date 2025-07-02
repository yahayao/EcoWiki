package com.ecowiki.controller.api.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecowiki.dto.PermissionDto;
import com.ecowiki.dto.PermissionGroupDto;
import com.ecowiki.service.PermissionGroupService;

@RestController
@RequestMapping("/api/admin/permission-groups")
public class PermissionGroupController {

    @Autowired
    private PermissionGroupService permissionGroupService;

    /**
     * 获取所有权限分组及其权限
     */
    @GetMapping
    public ResponseEntity<List<PermissionGroupDto>> getAllPermissionGroups() {
        try {
            List<PermissionGroupDto> groups = permissionGroupService.getAllPermissionGroups();
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据ID获取权限分组
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<PermissionGroupDto> getPermissionGroupById(@PathVariable Integer groupId) {
        try {
            return permissionGroupService.getPermissionGroupById(groupId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 创建权限分组
     */
    @PostMapping
    public ResponseEntity<PermissionGroupDto> createPermissionGroup(@RequestBody PermissionGroupDto dto) {
        try {
            PermissionGroupDto created = permissionGroupService.createPermissionGroup(dto);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新权限分组
     */
    @PutMapping("/{groupId}")
    public ResponseEntity<PermissionGroupDto> updatePermissionGroup(
            @PathVariable Integer groupId, 
            @RequestBody PermissionGroupDto dto) {
        try {
            PermissionGroupDto updated = permissionGroupService.updatePermissionGroup(groupId, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除权限分组
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deletePermissionGroup(@PathVariable Integer groupId) {
        try {
            permissionGroupService.deletePermissionGroup(groupId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取分组下的所有权限
     */
    @GetMapping("/{groupId}/permissions")
    public ResponseEntity<List<PermissionDto>> getPermissionsByGroupId(@PathVariable Integer groupId) {
        try {
            List<PermissionDto> permissions = permissionGroupService.getPermissionsByGroupId(groupId);
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 为分组添加权限
     */
    @PostMapping("/{groupId}/permissions")
    public ResponseEntity<PermissionDto> addPermissionToGroup(
            @PathVariable Integer groupId, 
            @RequestBody PermissionDto dto) {
        try {
            PermissionDto created = permissionGroupService.addPermissionToGroup(groupId, dto);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 批量更新分组内权限的排序
     */
    @PutMapping("/{groupId}/permissions/order")
    public ResponseEntity<Void> updatePermissionsOrder(
            @PathVariable Integer groupId, 
            @RequestBody List<Integer> permissionIds) {
        try {
            permissionGroupService.updatePermissionsOrder(groupId, permissionIds);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
