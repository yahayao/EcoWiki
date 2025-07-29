package com.ecowiki.config;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ecowiki.entity.user.Role;
import com.ecowiki.entity.user.User;
import com.ecowiki.repository.RoleRepository;
import com.ecowiki.repository.UserRepository;
import com.ecowiki.service.UserService;

/**
 * 数据初始化配置类
 * 
 * 该类实现了CommandLineRunner接口，在Spring Boot应用启动时自动执行数据初始化操作。
 * 主要负责创建系统必需的基础数据，包括默认角色和超级管理员账户。
 * 
 * 主要功能：
 * - 基础角色初始化：创建系统预定义的用户角色
 * - 超级管理员创建：自动创建默认的超级管理员账户
 * - 数据完整性检查：避免重复创建已存在的数据
 * - 系统启动配置：确保系统首次运行时具备必要的基础数据
 * 
 * 初始化数据：
 * 1. 角色数据：
 *    - user: 普通用户角色
 *    - moderator: 版主角色
 *    - admin: 管理员角色
 *    - superadmin: 超级管理员角色
 * 
 * 2. 超级管理员账户：
 *    - 用户名: superadmin
 *    - 密码: EcoWiki@2025
 *    - 邮箱: admin@ecowiki.com
 *    - 全名: 超级管理员
 *    - 状态: 激活
 * 
 * 安全考虑：
 * - 生产环境密码加密：提供了密码加密的配置选项
 * - 重复创建检查：避免重复初始化造成数据冲突
 * - 明文密码警告：开发环境使用明文密码便于测试
 * - 账户状态管理：确保管理员账户处于可用状态
 * 
 * 执行时机：
 * - 应用启动时：在所有Bean初始化完成后执行
 * - 数据库连接后：确保数据库连接可用
 * - 单次执行：通过存在性检查避免重复执行
 * 
 * 使用场景：
 * - 系统首次部署：为新部署的系统创建初始数据
 * - 开发环境搭建：快速搭建具备基础数据的开发环境
 * - 测试环境准备：为测试提供一致的基础数据
 * - 数据恢复：在数据重置后快速恢复基础配置
 * 
 * 扩展建议：
 * - 权限数据初始化：可扩展添加基础权限数据的创建
 * - 配置文件驱动：支持通过配置文件定制初始化数据
 * - 环境区分：根据不同环境执行不同的初始化策略
 * - 数据版本管理：支持数据结构升级和迁移
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2024-01-01
 * @see CommandLineRunner Spring Boot启动时执行接口
 * @see Role 角色实体类
 * @see User 用户实体类
 */
@Component
public class DataInitializer implements CommandLineRunner {

    /**
     * 用户数据访问对象
     * 用于用户相关的数据库操作
     */
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 角色数据访问对象
     * 用于角色相关的数据库操作
     */
    @Autowired
    private RoleRepository roleRepository;
    
    /**
     * 用户业务服务
     * 用于用户角色分配等业务操作
     */
    @Autowired
    private UserService userService;

    // 生产环境启用密码加密
    // @Autowired
    // private PasswordEncoder passwordEncoder;

    /**
     * 应用启动时执行的初始化方法
     * 
     * 该方法在Spring Boot应用完全启动后自动执行，负责初始化系统必需的基础数据。
     * 包括角色数据的创建和超级管理员账户的设置。
     * 
     * 执行步骤：
     * 1. 初始化基础角色数据
     * 2. 检查是否存在超级管理员账户
     * 3. 创建超级管理员账户（如果不存在）
     * 4. 为超级管理员分配角色
     * 5. 输出初始化结果信息
     * 
     * @param args 命令行参数（未使用）
     * @throws Exception 初始化过程中可能抛出的异常
     */
    @Override
    public void run(String... args) throws Exception {
        // 初始化基础角色数据
        initializeRoles();
        
        // 检查是否已存在超级管理员账户，避免重复创建
        if (!userRepository.existsByUsername("superadmin")) {
            // 创建超级管理员账户
            User superAdmin = new User();
            superAdmin.setUsername("superadmin");
            // 生产环境启用：superAdmin.setPassword(passwordEncoder.encode("EcoWiki@2025"));
            superAdmin.setPassword("EcoWiki@2025");  // 开发环境使用明文密码
            superAdmin.setEmail("admin@ecowiki.com");
            superAdmin.setFullName("超级管理员");
            superAdmin.setActive(true);

            // 保存超级管理员用户信息
            User savedSuperAdmin = userRepository.save(superAdmin);
            
            // 为超级管理员分配superadmin角色
            userService.assignUserRole(savedSuperAdmin.getUserId().intValue(), "superadmin");

            // 输出超级管理员账户信息（便于首次使用）
            System.out.println("=================================");
            System.out.println("超级管理员账户已创建:");
            System.out.println("用户名: superadmin");
            System.out.println("密码: EcoWiki@2025");
            System.out.println("邮箱: admin@ecowiki.com");
            System.out.println("权限组: superadmin");
            System.out.println("=================================");
        }
    }
    
    /**
     * 初始化系统基础角色数据
     * 
     * 创建系统预定义的四个基础角色，为权限管理系统提供角色基础。
     * 通过角色数量检查避免重复创建，确保数据的一致性。
     * 
     * 角色层次：
     * - user: 普通用户，基础访问权限
     * - moderator: 版主，内容管理权限
     * - admin: 管理员，系统管理权限
     * - superadmin: 超级管理员，最高权限
     */
    private void initializeRoles() {
        // 检查数据库中是否已有角色数据，避免重复初始化
        if (roleRepository.count() == 0) {
            // 创建四个基础角色
            createRole("user", "普通用户");
            createRole("moderator", "版主"); 
            createRole("admin", "管理员");
            createRole("superadmin", "超级管理员");
            
            System.out.println("基础角色数据初始化完成");
        }
    }
    
    /**
     * 创建单个角色的辅助方法
     * 
     * 封装角色创建的通用逻辑，包括角色名称、描述和时间戳的设置。
     * 确保所有角色都具有一致的数据结构和时间记录。
     * 
     * @param roleName 角色名称，系统中的角色标识符
     * @param description 角色描述，人性化的角色功能说明
     */
    private void createRole(String roleName, String description) {
        Role role = new Role();
        role.setRoleName(roleName);
        role.setDescription(description);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        roleRepository.save(role);
    }
}