package com.example.demo.security;  
  
import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;  
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;  
  
public class TotpAuthenticationFilter extends AbstractAuthenticationProcessingFilter {  
  
    public TotpAuthenticationFilter() {  
        super(new AntPathRequestMatcher("/totp-verify"));  
    }  
    @Override  
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)  
            throws IOException, ServletException {  
        String totp = request.getParameter("totp");  
        String username = request.getParameter("username");  
  
        // 创建 TOTP 认证令牌  
        TotpAuthenticationToken token = new TotpAuthenticationToken(username, totp);  
        return this.getAuthenticationManager().authenticate(token);  
    }  
    @Override  
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,  
                                            FilterChain chain, Authentication authResult)  
            throws IOException, ServletException {  
        SecurityContextHolder.getContext().setAuthentication(authResult);  
        chain.doFilter(request, response);  
    }
}