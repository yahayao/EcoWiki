/**
 * 缓存配置类
 * 
 * 功能包括：
 * - Spring缓存功能启用
 * - 缓存策略配置管理
 * - 系统性能优化支持
 * - 缓存注解支持启用
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // Spring Boot 自动配置会处理缓存管理器的创建
    // 使用 application.properties 中的配置来设置EhCache
}
