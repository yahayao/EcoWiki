package com.ecowiki.controller;

import com.ecowiki.dto.UserRegistrationDto;
import com.ecowiki.entity.User;
import com.ecowiki.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", userService.isUsernameAvailable(username));
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody UserRegistrationDto dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.registerUser(dto);
            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("userId", user.getId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "EcoWiki API is running");
        return ResponseEntity.ok(response);
    }
}
