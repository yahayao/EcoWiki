package com.ecowiki.service;

/**
 * 用户服务类
 *
 * 负责处理与用户相关的所有业务逻辑，包括注册、登录、用户信息管理、
 * 角色分配、权限校验等。是EcoWiki后端的核心服务之一。
 *
 * 主要功能：
 * - 用户注册与唯一性校验
 * - 用户登录与密码校验
 * - 用户信息的增删改查
 * - 用户角色的分配与查询
 * - 账号状态管理（激活/禁用）
 *
 * 依赖组件：
 * - UserRepository：用户数据访问层
 * - UserRoleRepository：用户角色关联数据访问层
 * - RoleRepository：角色数据访问层
 *
 * 设计说明：
 * - 所有用户角色分配均基于user_roles表实现
 * - 支持多角色分配和权限动态变更
 * - 密码加密功能可根据环境切换
 *
 * @author EcoWiki Team
 * @version 2.0
 * @since 2025-06-30
 */
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecowiki.dto.auth.LoginRequest;
import com.ecowiki.dto.user.UserRegistrationDto;
import com.ecowiki.entity.Role;
import com.ecowiki.entity.User;
import com.ecowiki.entity.UserRole;
import com.ecowiki.repository.RoleRepository;
import com.ecowiki.repository.UserRepository;
import com.ecowiki.repository.UserRoleRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    // 密码加密器（开发阶段注释掉，生产环境时启用）
    // @Autowired
    // private PasswordEncoder passwordEncoder;
    
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
    
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
    
    public User registerUser(UserRegistrationDto dto) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(dto.getUsername());
        
        // 加密密码（开发阶段注释掉，生产环境时启用）
        // user.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        // 开发阶段使用明文密码
        user.setPassword(dto.getPassword());
        
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setActive(true);
        
        // 保存到数据库
        User savedUser = userRepository.save(user);
        
        // 为新用户分配默认角色
        assignUserRole(savedUser.getUserId().intValue(), "user");
        
        return savedUser;
    }
    
    public User authenticateUser(LoginRequest request) {
        User user = null;
        
        // 根据提供的信息查找用户
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            user = userRepository.findByUsername(request.getUsername())
                .orElse(null);
        } else if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        }
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证密码
        // 加密密码验证（开发阶段注释掉，生产环境时启用）
        // if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        //     throw new RuntimeException("密码错误");
        // }
        
        // 开发阶段使用明文密码验证
        if (!request.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        return user;
    }
    public User resetPassword(LoginRequest request) {
        User user = null;
        
        // 根据提供的信息查找用户
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            user = userRepository.findByUsername(request.getUsername())
                .orElse(null);
        } else if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        }
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证密码
        // 加密密码验证（开发阶段注释掉，生产环境时启用）
        // if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        //     throw new RuntimeException("密码错误");
        // }
        
        // 开发阶段使用明文密码验证
        if (!request.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        return user;
        
    }


    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    // 根据角色统计用户数量（通过User_Roles表）
    public long countByUserGroup(String roleName) {
        Role role = roleRepository.findByRoleName(roleName);
        if (role == null) {
            return 0;
        }
        return userRoleRepository.countByRoleId(role.getRoleId());
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    public User findUserByEmail(String email) {
    return userRepository.findByEmail(email).orElse(null);
}

    public String getSecurityQuestion(User user) {
        // 根据用户信息返回对应的安全问题
        // 这里只是一个示例，您需要根据实际情况来实现
        return user.getSecurityQuestion();
    }
    public boolean resetPassword(User user, String newPassword) {
        user.setPassword(newPassword);
        return userRepository.save(user) != null;
    }
    
    // 用户角色管理方法
    
    /**
     * 为用户分配角色（通过角色名称）
     */
    public void assignUserRole(Integer userId, String roleName) {
        Role role = roleRepository.findByRoleName(roleName);
        if (role == null) {
            throw new RuntimeException("角色不存在: " + roleName);
        }
        
        // 检查用户角色关系是否已存在
        Optional<UserRole> existingUserRole = userRoleRepository.findByUserIdAndRoleId(userId, role.getRoleId());
        if (existingUserRole.isPresent()) {
            return; // 关系已存在，不需要重复添加
        }
        
        // 创建新的用户角色关系
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getRoleId());
        userRole.setCreatedAt(LocalDateTime.now());
        
        userRoleRepository.save(userRole);
        
        // 用户角色关系已保存，不再需要同步User表的userGroup字段
    }
    
    /**
     * 移除用户的所有角色并分配新角色
     */
    public void updateUserRole(Integer userId, String newRoleName) {
        // 先删除用户的所有角色
        userRoleRepository.deleteByUserId(userId);
        
        // 分配新角色
        assignUserRole(userId, newRoleName);
    }
    
    /**
     * 获取用户的主要角色名称
     */
    public String getUserRoleName(Integer userId) {
        List<UserRole> userRoles = userRoleRepository.findPrimaryRoleByUserId(userId);
        if (!userRoles.isEmpty()) {
            Role role = roleRepository.findById(userRoles.get(0).getRoleId()).orElse(null);
            if (role != null) {
                return role.getRoleName();
            }
        }
        return "user"; // 默认角色
    }
    
    /**
     * 获取用户的所有角色
     */
    public List<String> getUserRoleNames(Integer userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        return userRoles.stream()
                .map(ur -> {
                    Role role = roleRepository.findById(ur.getRoleId()).orElse(null);
                    return role != null ? role.getRoleName() : null;
                })
                .filter(roleName -> roleName != null)
                .toList();
    }
    
    /**
     * 删除用户时清理角色关系
     */
    public void cleanupUserRoles(Integer userId) {
        userRoleRepository.deleteByUserId(userId);
    }
    
    /**
     * 保存用户信息
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}