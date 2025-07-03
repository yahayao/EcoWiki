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

/**
 * JWT工具类
 * 
 * 负责JWT令牌的生成、解析、验证等核心功能。
 * 提供用户身份认证和授权的技术支持，确保API接口的安全访问。
 * 
 * 主要功能：
 * - JWT令牌生成：根据用户信息生成安全的访问令牌
 * - 令牌解析：从令牌中提取用户名、过期时间等信息
 * - 令牌验证：验证令牌的有效性和完整性
 * - 刷新令牌：支持长期有效的刷新令牌机制
 * - 声明提取：从JWT中提取自定义的声明信息
 * 
 * 安全特性：
 * - HMAC SHA算法：使用HS256算法进行签名
 * - 密钥管理：采用安全的密钥生成和管理机制
 * - 过期控制：支持访问令牌和刷新令牌的不同过期时间
 * - 签名验证：确保令牌未被篡改
 * 
 * 令牌配置：
 * - 访问令牌有效期：24小时（86400000毫秒）
 * - 刷新令牌有效期：7天（604800000毫秒）
 * - 签名算法：HS256（HMAC SHA-256）
 * - 密钥长度：符合HMAC SHA-256要求的密钥长度
 * 
 * 使用场景：
 * - 用户登录：生成访问令牌用于后续API调用
 * - 接口认证：验证请求头中的Bearer令牌
 * - 权限控制：基于令牌中的用户信息进行权限判断
 * - 会话管理：无状态的用户会话管理
 * 
 * 技术实现：
 * - JJWT库：使用io.jsonwebtoken实现JWT操作
 * - Spring组件：注册为Spring Bean，支持依赖注入
 * - 函数式编程：使用Function接口简化声明提取
 * - 异常处理：妥善处理令牌解析和验证异常
 * 
 * 注意事项：
 * - 密钥安全：生产环境应使用环境变量或配置文件管理密钥
 * - 令牌传输：建议通过HTTPS传输令牌，防止中间人攻击
 * - 过期处理：前端应监听令牌过期，及时刷新或重新登录
 * - 存储安全：客户端应安全存储令牌，避免XSS攻击
 * 
 * @author EcoWiki团队
 * @version 2.0
 * @since 2024-01-01
 */
@Component
public class JwtUtil {
    
    private static final String SECRET_KEY = "EcoWiki2025SecretKeyForJWTTokenGenerationAndValidation";
    private static final int JWT_EXPIRATION_MS = 86400000; // 24小时
    private static final int REFRESH_TOKEN_EXPIRATION_MS = 604800000; // 7天
    
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
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
}