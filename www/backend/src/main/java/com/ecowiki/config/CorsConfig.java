package com.ecowiki.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置类
 * <p>
 * 配置EcoWiki项目的跨域资源共享(CORS)策略，允许前端应用访问后端API。
 * 适用于前后端分离架构的开发和部署环境。
 * <p>
 * <b>设计说明：</b>
 * - 允许前端开发服务器(localhost:5173)的跨域访问
 * - 支持常用HTTP方法(GET、POST、PUT、DELETE、OPTIONS)
 * - 允许所有请求头，支持凭据传递
 * - 设置预检请求缓存时间为1小时
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    /**
     * 添加跨域映射配置
     * @param registry CORS注册表
     **/
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
