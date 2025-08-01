package com.ecowiki.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ecowiki.security.JwtAuthenticationFilter;

/**
 * Spring Security配置类
 * <p>
 * 配置EcoWiki项目的安全策略，包括认证、授权、JWT过滤器、密码编码等。
 * 采用无状态会话管理，基于JWT进行用户认证。
 * <p>
 * <b>设计说明：</b>
 * - 禁用CSRF（适用于前后端分离的API）
 * - 采用无状态会话管理
 * - 配置JWT认证过滤器
 * - 设置不同接口的访问权限
 * - 提供BCrypt密码编码器
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /** JWT认证过滤器，使用延迟注入避免循环依赖 */
    @Autowired
    @Lazy
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    /**
     * 密码编码器Bean
     * @return BCrypt密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * 安全过滤器链配置
     * @param http HTTP安全配置
     * @return 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/uploads/**").permitAll() // 允许访问上传的静态文件
                .requestMatchers("/admin/**").authenticated() // 管理员接口需要认证
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(AbstractHttpConfigurer::disable);
        
        return http.build();
    }
}