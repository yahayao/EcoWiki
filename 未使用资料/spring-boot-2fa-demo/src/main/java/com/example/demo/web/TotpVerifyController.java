package com.example.demo.web;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.security.TotpAuthenticator;

@RestController
@RequestMapping("/test")
public class TotpVerifyController {

    private final TotpAuthenticator totpAuthenticator;

    public TotpVerifyController(TotpAuthenticator totpAuthenticator) {
        this.totpAuthenticator = totpAuthenticator;
    }

    @GetMapping("/totp-verify")
    public String verifyTotp(@RequestParam int totp) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // 从存储中获取与用户名关联的密钥，这里假设已获取  
        String secret = "OZSNQGV44RGY63BL";

        if (totpAuthenticator.verifyTotp(secret, totp)) {
            return "2FA 成功!";
        } else {
            return "无效的 TOTP!";
        }
    }

    @GetMapping("/test1")
    public String test() {
        return "hell1";
    }
}
