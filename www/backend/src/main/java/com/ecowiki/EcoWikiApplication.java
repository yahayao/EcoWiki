package com.ecowiki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EcoWiki应用程序主启动类
 * 
 * 这是Spring Boot应用程序的入口点，负责启动整个EcoWiki后端服务。
 * EcoWiki是一个知识共享平台，支持用户注册、登录、文章管理、权限管理等功能。
 * 
 * 主要功能模块：
 * - 用户认证与授权系统
 * - 基于角色的权限管理
 * - 管理后台功能
 * - RESTful API接口
 * 
 * 技术栈：
 * - Spring Boot 3.x
 * - Spring Security (JWT认证)
 * - Spring Data JPA
 * - MySQL数据库
 * 
 * @author EcoWiki Team
 * @version 1.0
 * @since 2025-06-30
 */
@SpringBootApplication
public class EcoWikiApplication {
    
    /**
     * 应用程序主入口方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(EcoWikiApplication.class, args);
    }
}
