package com.ecowiki.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.crypto.password.PasswordEncoder; // 开发阶段注释掉，生产环境时启用
import org.springframework.stereotype.Service;

import com.ecowiki.dto.LoginRequest;
import com.ecowiki.dto.UserRegistrationDto;
import com.ecowiki.entity.User;
import com.ecowiki.repository.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    // 密码加密器（开发阶段注释掉，生产环境时启用）
    // @Autowired
    // private PasswordEncoder passwordEncoder;
    
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
    
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
    
    public User registerUser(UserRegistrationDto dto) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(dto.getUsername());
        
        // 加密密码（开发阶段注释掉，生产环境时启用）
        // user.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        // 开发阶段使用明文密码
        user.setPassword(dto.getPassword());
        
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setUserGroup("user"); // 设置默认权限组
        user.setActive(true);
        
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
        // 加密密码验证（开发阶段注释掉，生产环境时启用）
        // if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        //     throw new RuntimeException("密码错误");
        // }
        
        // 开发阶段使用明文密码验证
        if (!request.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        return user;
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    public User findUserByEmail(String email) {
    return userRepository.findByEmail(email).orElse(null);
}

    public String getSecurityQuestion(User user) {
        // 根据用户信息返回对应的安全问题
        // 这里只是一个示例，您需要根据实际情况来实现
        return user.getSecurityQuestion();
    }
    public boolean resetPassword(User user, String newPassword) {
        user.setPassword(newPassword);
        return userRepository.save(user) != null;
    }
}