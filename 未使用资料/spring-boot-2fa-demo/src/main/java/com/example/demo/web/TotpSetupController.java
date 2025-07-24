package com.example.demo.web;

import com.example.demo.dto.QRCodeResponse;
import com.example.demo.security.TotpAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class TotpSetupController {

    private final TotpAuthenticator totpAuthenticator;

    public TotpSetupController(TotpAuthenticator totpAuthenticator) {
        this.totpAuthenticator = totpAuthenticator;
    }

    // 设置 TOTP 密钥并返回 QR 码 URL    
    @GetMapping("/totp-setup")
    public Map<String, String> setupTotp(@RequestParam String username) {
        // 写死一个 TOTP 密钥  
        String hardCodedSecret = "OZSNQGV44RGY63BL";
        GoogleAuthenticatorKey googleAuthenticatorKey = new GoogleAuthenticatorKey.Builder(hardCodedSecret).build();
        String qrCodeUrl = totpAuthenticator.getQRCode(googleAuthenticatorKey, username);

        Map<String, String> response = new HashMap<>();
        response.put("secret", hardCodedSecret);
        response.put("qrCodeUrl", qrCodeUrl);

        return response;
    }

    // 设置 TOTP 密钥并返回 QR 码 URL    
    @GetMapping("/totp-setup1")
    public QRCodeResponse setupTotp1(@RequestParam String username) {
        GoogleAuthenticatorKey googleAuthenticatorKey = totpAuthenticator.generateSecret();
        // 保存密钥与用户名的关联关系，可以使用数据库等存储  
        // 这里只是示例，没有实际存储  

        String qrCodeUrl = totpAuthenticator.getQRCode(googleAuthenticatorKey, username);
        return new QRCodeResponse(googleAuthenticatorKey.getKey(), qrCodeUrl);
    }
}
