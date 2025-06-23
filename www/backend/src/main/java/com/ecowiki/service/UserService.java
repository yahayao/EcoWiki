package com.ecowiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecowiki.dto.UserRegistrationDto;
import com.ecowiki.entity.User;
import com.ecowiki.repository.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
    
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
    
    public User registerUser(UserRegistrationDto dto) {
        // 验证用户名和邮箱是否可用
        if (!isUsernameAvailable(dto.getUsername())) {
            throw new RuntimeException("用户名已被使用");
        }
        if (!isEmailAvailable(dto.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // 加密密码
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setUserGroup(dto.getUserGroup() != null ? dto.getUserGroup() : "user"); // 默认用户组
        
        // 保存到数据库
        return userRepository.save(user);
    }
}