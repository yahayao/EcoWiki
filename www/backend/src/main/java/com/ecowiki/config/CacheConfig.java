package com.ecowiki.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存配置类
 * 
 * 启用Spring缓存功能，提升系统性能
 * 
 * @author EcoWiki Team
 * @version 1.0
 * @since 2025-08-01
 */
@Configuration
@EnableCaching
public class CacheConfig {
    // Spring Boot 自动配置会处理缓存管理器的创建
    // 使用 application.properties 中的配置来设置EhCache
}
