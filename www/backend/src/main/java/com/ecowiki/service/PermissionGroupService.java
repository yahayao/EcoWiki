package com.ecowiki.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecowiki.dto.PermissionDto;
import com.ecowiki.dto.PermissionGroupDto;
import com.ecowiki.entity.Permission;
import com.ecowiki.entity.PermissionGroup;
import com.ecowiki.repository.PermissionGroupRepository;
import com.ecowiki.repository.PermissionRepository;

@Service
@Transactional
public class PermissionGroupService {

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    /**
     * 获取所有权限分组及其权限
     */
    @Transactional(readOnly = true)
    public List<PermissionGroupDto> getAllPermissionGroups() {
        List<PermissionGroup> groups = permissionGroupRepository.findAllWithPermissions();
        return groups.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取权限分组
     */
    @Transactional(readOnly = true)
    public Optional<PermissionGroupDto> getPermissionGroupById(Integer groupId) {
        return permissionGroupRepository.findByIdWithPermissions(groupId)
                .map(this::convertToDto);
    }

    /**
     * 创建权限分组
     */
    public PermissionGroupDto createPermissionGroup(PermissionGroupDto dto) {
        PermissionGroup group = convertToEntity(dto);
        group = permissionGroupRepository.save(group);
        return convertToDto(group);
    }

    /**
     * 更新权限分组
     */
    public PermissionGroupDto updatePermissionGroup(Integer groupId, PermissionGroupDto dto) {
        PermissionGroup group = permissionGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("权限分组不存在"));
        
        group.setGroupName(dto.getGroupName());
        group.setGroupDescription(dto.getGroupDescription());
        group.setIcon(dto.getIcon());
        group.setSortOrder(dto.getSortOrder());
        group.setIsActive(dto.getIsActive());
        
        group = permissionGroupRepository.save(group);
        return convertToDto(group);
    }

    /**
     * 删除权限分组
     */
    public void deletePermissionGroup(Integer groupId) {
        if (!permissionGroupRepository.existsById(groupId)) {
            throw new RuntimeException("权限分组不存在");
        }
        
        // 检查是否有权限属于此分组
        List<Permission> permissions = permissionRepository.findByGroupId(groupId);
        if (!permissions.isEmpty()) {
            throw new RuntimeException("不能删除包含权限的分组，请先移除或转移权限");
        }
        
        permissionGroupRepository.deleteById(groupId);
    }

    /**
     * 获取分组下的所有权限
     */
    @Transactional(readOnly = true)
    public List<PermissionDto> getPermissionsByGroupId(Integer groupId) {
        List<Permission> permissions = permissionRepository.findByGroupIdOrderBySortOrder(groupId);
        return permissions.stream()
                .map(this::convertPermissionToDto)
                .collect(Collectors.toList());
    }

    /**
     * 为分组添加权限
     */
    public PermissionDto addPermissionToGroup(Integer groupId, PermissionDto dto) {
        if (!permissionGroupRepository.existsById(groupId)) {
            throw new RuntimeException("权限分组不存在");
        }
        
        Permission permission = convertPermissionToEntity(dto);
        permission.setGroupId(groupId);
        permission = permissionRepository.save(permission);
        
        return convertPermissionToDto(permission);
    }

    /**
     * 批量更新分组内权限的排序
     */
    public void updatePermissionsOrder(Integer groupId, List<Integer> permissionIds) {
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        
        for (int i = 0; i < permissionIds.size(); i++) {
            Integer permissionId = permissionIds.get(i);
            final int sortOrder = i + 1;
            permissions.stream()
                    .filter(p -> p.getPermissionId().equals(permissionId))
                    .findFirst()
                    .ifPresent(p -> p.setSortOrder(sortOrder));
        }
        
        permissionRepository.saveAll(permissions);
    }

    // 转换方法
    private PermissionGroupDto convertToDto(PermissionGroup group) {
        PermissionGroupDto dto = new PermissionGroupDto();
        dto.setGroupId(group.getGroupId());
        dto.setGroupKey(group.getGroupKey());
        dto.setGroupName(group.getGroupName());
        dto.setGroupDescription(group.getGroupDescription());
        dto.setIcon(group.getIcon());
        dto.setSortOrder(group.getSortOrder());
        dto.setIsActive(group.getIsActive());
        
        if (group.getPermissions() != null) {
            List<PermissionDto> permissionDtos = group.getPermissions().stream()
                    .map(this::convertPermissionToDto)
                    .collect(Collectors.toList());
            dto.setPermissions(permissionDtos);
        }
        
        return dto;
    }

    private PermissionGroup convertToEntity(PermissionGroupDto dto) {
        PermissionGroup group = new PermissionGroup();
        group.setGroupKey(dto.getGroupKey());
        group.setGroupName(dto.getGroupName());
        group.setGroupDescription(dto.getGroupDescription());
        group.setIcon(dto.getIcon());
        group.setSortOrder(dto.getSortOrder());
        group.setIsActive(dto.getIsActive());
        return group;
    }

    private PermissionDto convertPermissionToDto(Permission permission) {
        PermissionDto dto = new PermissionDto();
        dto.setPermissionId(permission.getPermissionId());
        dto.setPermissionName(permission.getPermissionName());
        dto.setDescription(permission.getDescription());
        dto.setPermissionKey(permission.getPermissionKey());
        dto.setGroupId(permission.getGroupId());
        dto.setIsSystem(permission.getIsSystem());
        dto.setSortOrder(permission.getSortOrder());
        
        // 添加分组名称
        if (permission.getPermissionGroup() != null) {
            dto.setGroupName(permission.getPermissionGroup().getGroupName());
        }
        
        return dto;
    }

    private Permission convertPermissionToEntity(PermissionDto dto) {
        Permission permission = new Permission();
        permission.setPermissionName(dto.getPermissionName());
        permission.setDescription(dto.getDescription());
        permission.setPermissionKey(dto.getPermissionKey());
        permission.setIsSystem(dto.getIsSystem());
        permission.setSortOrder(dto.getSortOrder());
        return permission;
    }
}
