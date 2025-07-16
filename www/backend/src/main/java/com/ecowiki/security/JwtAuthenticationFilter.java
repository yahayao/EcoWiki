package com.ecowiki.security;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT认证过滤器
 * 
 * 继承OncePerRequestFilter，在每个HTTP请求中执行一次JWT令牌的验证和认证。
 * 从请求头中提取JWT令牌，验证其有效性，并设置Spring Security上下文。
 * 
 * 主要功能：
 * - 令牌提取：从Authorization请求头中提取Bearer Token
 * - 令牌验证：使用JwtUtil验证令牌的有效性和完整性
 * - 用户认证：根据有效令牌设置Spring Security认证上下文
 * - 请求过滤：拦截所有HTTP请求进行身份验证
 * 
 * 工作流程：
 * 1. 从请求头中提取Authorization字段
 * 2. 检查是否包含"Bearer "前缀的JWT令牌
 * 3. 使用JwtUtil验证令牌有效性
 * 4. 提取用户名并创建认证对象
 * 5. 设置Spring Security上下文
 * 6. 继续执行后续过滤器链
 * 
 * 安全特性：
 * - 无状态认证：不依赖服务器端会话存储
 * - 令牌验证：确保令牌未被篡改或过期
 * - 上下文管理：正确设置和清理Security上下文
 * - 异常处理：妥善处理令牌解析异常
 * 
 * 使用场景：
 * - API接口保护：保护需要认证的REST API端点
 * - 用户身份验证：基于JWT实现用户身份验证
 * - 权限控制：为后续的权限验证提供用户身份信息
 * - 无状态服务：支持微服务架构的无状态认证
 * 
 * 技术实现：
 * - Spring Security过滤器：集成到Spring Security过滤器链
 * - 一次性过滤器：每个请求只执行一次，避免重复处理
 * - 依赖注入：使用@Autowired注入JwtUtil工具类
 * - 异常捕获：捕获并处理JWT相关异常
 * 
 * 注意事项：
 * - 过滤器顺序：应在UsernamePasswordAuthenticationFilter之前执行
 * - 性能考虑：避免在每次请求时执行重复的数据库查询
 * - 异常处理：令牌无效时应继续执行过滤器链，由后续过滤器处理
 * - 线程安全：确保SecurityContext的线程安全性
 * 
 * @author EcoWiki团队
 * @version 2.0
 * @since 2024-01-01
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    /** JWT工具类，用于令牌解析和验证 */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 过滤器内部处理逻辑
     * @param request HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     **/
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 提取JWT token
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                logger.warn("JWT Token解析失败: " + e.getMessage());
            }
        }

        // 验证token并设置认证
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtUtil.isTokenValid(jwt)) {
                    // 创建认证对象，不需要验证用户是否存在，JWT本身就是验证
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                logger.warn("JWT Token验证失败: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}