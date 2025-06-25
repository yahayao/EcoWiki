package com.ecowiki.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ecowiki.entity.User;
import com.ecowiki.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    // 生产环境启用
    // @Autowired
    // private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
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
}