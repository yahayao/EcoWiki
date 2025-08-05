/**
 * JWT工具类
 * 
 * 功能：
 * - 提供JWT令牌的生成、验证和解析功能
 * - 支持用户身份认证和授权管理
 * - 实现令牌过期时间控制和安全验证
 * - 提供令牌声明信息的提取和处理
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2025-07-01
 * @lastModified 2025-08-05
 */
package com.ecowiki.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    
    private static final int JWT_EXPIRATION_MS = 86400000; // 24小时
    private static final int REFRESH_TOKEN_EXPIRATION_MS = 604800000; // 7天
    
    // 动态生成的安全密钥，每次应用启动时重新生成
    private final Key signingKey;
    
    public JwtUtil() {
        // 使用安全的随机密钥生成，符合HMAC SHA-256要求
        this.signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }
    
    private Key getSigningKey() {
        return this.signingKey;
    }
    
    // 从token中提取用户名
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    // 从token中提取过期时间
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    // 从token中提取特定claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    // 提取所有claims
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    // 检查token是否过期
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    // 为用户生成token
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, JWT_EXPIRATION_MS);
    }
    
    // 生成refresh token
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, username, REFRESH_TOKEN_EXPIRATION_MS);
    }
    
    // 创建token
    private String createToken(Map<String, Object> claims, String subject, int expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // 验证token
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
    
    // 检查token是否有效（不检查用户名）
    public Boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    // 检查是否为refresh token
    public Boolean isRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }
    
    // 从refresh token生成新的access token
    public String refreshAccessToken(String refreshToken) {
        if (!isRefreshToken(refreshToken) || !isTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        String username = extractUsername(refreshToken);
        return generateToken(username);
    }
}