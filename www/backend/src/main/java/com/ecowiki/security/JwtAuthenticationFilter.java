package com.ecowiki.security;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
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
 * <p>
 * 继承OncePerRequestFilter，在每个HTTP请求中执行一次JWT令牌的验证和认证。
 * 从请求头中提取JWT令牌，验证其有效性，并设置Spring Security上下文。
 * <p>
 * <b>设计说明：</b>
 * - 从Authorization头部提取Bearer Token
 * - 验证JWT令牌的有效性和完整性
 * - 设置Spring Security认证上下文
 * - 适用于无状态的API认证机制
 *
 * @author EcoWiki
 * @version 1.0
 * @since 2024-04
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
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
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