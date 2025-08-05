/**
 * EcoWiki应用程序主启动类
 * 
 * 功能包括：
 * - Spring Boot应用程序启动入口
 * - 系统初始化和配置加载
 * - 组件扫描和依赖注入配置
 * - 数据库连接和JPA配置
 * - Web服务和API接口启动
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
