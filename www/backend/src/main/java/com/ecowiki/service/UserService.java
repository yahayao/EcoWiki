package com.ecowiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecowiki.dto.LoginRequest;
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
    
    public User authenticateUser(LoginRequest request) {
        User user = null;
        
        // 根据提供的信息查找用户
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            user = userRepository.findByUsername(request.getUsername())
                .orElse(null);
        } else if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        }
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        return user;
    }
}