package com.ecowiki.util;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;


/**
 * IP 工具类，从 HttpServletRequest 中提取客户端真实 IP（支持代理）
 */
public class IpUtil {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

    /**
     * 获取客户端真实IP地址（支持多级代理）
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = null;

        // 1. X-Forwarded-For：可能包含多个IP（client, proxy1, proxy2）
        ip = request.getHeader("X-Forwarded-For");
        if (isIpValid(ip)) {
            // 取第一个非unknown的IP，即为真实客户端IP
            String[] ips = ip.split(",");
            for (String candidate : ips) {
                if (!UNKNOWN.equalsIgnoreCase(candidate.trim())) {
                    return candidate.trim();
                }
            }
        }

        // 2. X-Real-IP：Nginx专用
        ip = request.getHeader("X-Real-IP");
        if (isIpValid(ip)) {
            return ip;
        }

        // 3. Proxy-Client-IP
        ip = request.getHeader("Proxy-Client-IP");
        if (isIpValid(ip)) {
            return ip;
        }

        // 4. WL-Proxy-Client-IP
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isIpValid(ip)) {
            return ip;
        }

        // 5. HTTP_CLIENT_IP
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isIpValid(ip)) {
            return ip;
        }

        // 6. HTTP_X_FORWARDED_FOR
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isIpValid(ip)) {
            return ip;
        }

        // 7. 都没有，直接获取远程地址
        ip = request.getRemoteAddr();

        // 处理本地IPv6格式
        if (LOCALHOST_IPV6.equals(ip)) {
            return LOCALHOST_IPV4;
        }

        return ip;
    }

    private static boolean isIpValid(String ip) {
        return !StringUtils.isEmpty(ip) && !UNKNOWN.equalsIgnoreCase(ip);
    }
}