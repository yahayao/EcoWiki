package com.example.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;

public class TotpAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private TotpAuthenticator totpAuthenticator;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String totp = (String) authentication.getCredentials();

        // 这里应该从某处获取用户的secret，这里使用硬编码的secret作为示例
        String hardCodedSecret = "OZSNQGV44RGY63BL";
        
        // 验证 TOTP        
        if (totpAuthenticator.verifyTotp(hardCodedSecret, Integer.parseInt(totp))) {
            return new TotpAuthenticationToken(username, totp,
                    userDetailsService.loadUserByUsername(username).getAuthorities());
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return TotpAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
