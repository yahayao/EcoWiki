package com.ecowiki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EcoWiki应用程序主启动类
 * 
 * 这是Spring Boot应用程序的入口点，负责启动整个EcoWiki后端服务。
 * EcoWiki是一个现代化的知识共享平台，支持用户注册、登录、文章管理、权限管理等功能。
 * 
 * 主要功能模块：
 * - 用户认证与授权系统（JWT Token）
 * - 基于角色的权限管理
 * - 文章管理系统（增删改查、版本控制）
 * - 文章互动功能（点赞、收藏、评论）
 * - 管理后台功能
 * - 文件上传与管理
 * - RESTful API接口
 * 
 * 技术栈：
 * - Spring Boot 3.x
 * - Spring Security (JWT认证)
 * - Spring Data JPA
 * - MySQL数据库
 * - Hibernate ORM
 * 
 * 架构特点：
 * - 前后端分离设计
 * - 微服务友好架构
 * - RESTful API规范
 * - 统一异常处理
 * - 完整的日志记录
 * 
 * @author EcoWiki Development Team
 * @version 2.0 (新增文章互动功能)
 * @since 2025-01-01
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
