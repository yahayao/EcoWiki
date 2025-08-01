package com.ecowiki.component;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web性能配置组件
 * 
 * 配置静态资源缓存、压缩等性能优化设置
 * 
 * @author EcoWiki Team
 * @version 1.0
 * @since 2025-08-01
 */
@Component
public class WebPerformanceConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源缓存
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(86400); // 缓存1天
                
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600); // 缓存1小时
    }
}
