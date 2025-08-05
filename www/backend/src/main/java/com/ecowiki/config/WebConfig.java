/**
 * Web配置类
 * 
 * 功能包括：
 * - 跨域请求配置管理
 * - 静态资源访问路径配置
 * - 文件上传目录映射设置
 * - Web MVC功能扩展配置
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.config;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * 头像上传路径
     */
    @Value("${avatar.upload.path:uploads/avatars/}")
    private String avatarUploadPath;
    
    /**
     * 服务器端口（用于生成完整URL）
     */
    @Value("${server.port:8080}")
    private String serverPort;
    
    /**
     * 配置静态资源处理器
     * 
     * 配置头像文件的HTTP访问路径，确保不与API路径冲突
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保头像上传目录存在
        ensureAvatarDirectoryExists();
        
        // 配置头像文件访问：/uploads/avatars/** → uploads/avatars/
        // 修改静态资源路径，避免与 /api/avatar/* 冲突
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + getAbsolutePath(avatarUploadPath))
                .setCachePeriod(86400)  // 缓存24小时
                .resourceChain(false);  // 禁用资源链，提高性能
        
        // API文档和静态页面
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
                
        System.out.println("✅ 头像文件访问配置完成:");
        System.out.println("   📁 头像存储目录: " + getAbsolutePath(avatarUploadPath));
        System.out.println("   🌐 头像访问地址: http://localhost:" + serverPort + "/uploads/avatars/");
        System.out.println("   ✅ API路径 /api/avatar/** 专用于接口调用");
        System.out.println("   ✅ 静态资源路径 /uploads/avatars/** 专用于文件访问");
    }
    
    /**
     * CORS跨域配置
     * 
     * 配置更详细的跨域策略，支持文件上传
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600)
                .exposedHeaders("Content-Disposition", "Content-Type", "Content-Length");
    }
    
    /**
     * 确保头像上传目录存在
     */
    private void ensureAvatarDirectoryExists() {
        try {
            // 创建头像目录
            Path avatarDir = Paths.get(avatarUploadPath);
            if (!Files.exists(avatarDir)) {
                Files.createDirectories(avatarDir);
                System.out.println("🖼️ 创建头像目录: " + avatarDir.toAbsolutePath());
            }
        } catch (java.io.IOException | java.nio.file.InvalidPathException e) {
            System.err.println("❌ 创建头像目录失败: " + e.getMessage());
            // 记录错误但不中断应用启动
        }
    }
    
    /**
     * 获取绝对路径
     * 
     * @param path 相对路径
     * @return 绝对路径字符串
     */
    private String getAbsolutePath(String path) {
        File file = new File(path);
        String absolutePath = file.getAbsolutePath();
        
        // 确保路径以斜杠结尾
        if (!absolutePath.endsWith(File.separator)) {
            absolutePath += File.separator;
        }
        
        return absolutePath;
    }
    
    /**
     * 获取文件访问的完整URL
     * 
     * @param relativePath 相对路径（如：avatars/user123.jpg）
     * @return 完整的访问URL
     */
    public String getAvatarAccessUrl(String relativePath) {
        return "http://localhost:" + serverPort + "/uploads/avatars/" + relativePath;
    }
}
