package com.example.demo.security;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Component;

/**
 * @author lei
 */
@Component
public class TotpAuthenticator {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    // 生成 TOTP 密钥并返回 GoogleAuthenticatorKey 对象  
    public GoogleAuthenticatorKey generateSecret() {
        return gAuth.createCredentials();
    }

    // 获取 TOTP QR 码 URL    
    public String getQRCode(GoogleAuthenticatorKey secret, String account) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(account, "SpringBootDemo", secret);
    }

    // 验证 TOTP    
    public boolean verifyTotp(String secret, int verificationCode) {
        return gAuth.authorize(secret, verificationCode);
    }
}
