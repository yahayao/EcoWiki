package com.ecowiki.config;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ecowiki.entity.Role;
import com.ecowiki.entity.User;
import com.ecowiki.repository.RoleRepository;
import com.ecowiki.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;

    // 生产环境启用
    // @Autowired
    // private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 初始化基础角色数据
        initializeRoles();
        
        // 检查是否已存在超级管理员
        if (!userRepository.existsByUsername("superadmin")) {
            // 创建超级管理员账户
            User superAdmin = new User();
            superAdmin.setUsername("superadmin");
            // superAdmin.setPassword(passwordEncoder.encode("EcoWiki@2025")); 生产环境启用
            superAdmin.setPassword("EcoWiki@2025");
            superAdmin.setEmail("admin@ecowiki.com");
            superAdmin.setFullName("超级管理员");
            superAdmin.setUserGroup("superadmin"); // 使用 userGroup
            superAdmin.setActive(true);

            userRepository.save(superAdmin);

            System.out.println("=================================");
            System.out.println("超级管理员账户已创建:");
            System.out.println("用户名: superadmin");
            System.out.println("密码: EcoWiki@2025");
            System.out.println("邮箱: admin@ecowiki.com");
            System.out.println("权限组: superadmin");
            System.out.println("=================================");
        }
    }
    
    private void initializeRoles() {
        // 检查是否已有角色数据
        if (roleRepository.count() == 0) {
            // 创建基础角色
            createRole("user", "普通用户");
            createRole("moderator", "版主"); 
            createRole("admin", "管理员");
            createRole("superadmin", "超级管理员");
            
            System.out.println("基础角色数据初始化完成");
        }
    }
    
    private void createRole(String roleName, String description) {
        Role role = new Role();
        role.setRoleName(roleName);
        role.setDescription(description);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        roleRepository.save(role);
    }
}