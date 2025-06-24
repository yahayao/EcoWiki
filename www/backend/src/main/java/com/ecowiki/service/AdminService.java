package com.ecowiki.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecowiki.entity.User;
import com.ecowiki.repository.UserRepository;

@Service
@Transactional
public class AdminService {
    
    @Autowired
    private UserRepository userRepository;
    
    // 获取所有用户（分页）
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    // 更新用户权限组
    public User updateUserGroup(Long userId, String newUserGroup) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        user.setUserGroup(newUserGroup);
        return userRepository.save(user);
    }
    
    // 更新用户状态
    public User updateUserStatus(Long userId, Boolean active) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        user.setActive(active);
        return userRepository.save(user);
    }
    
    // 获取系统统计信息
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总用户数
        long totalUsers = userRepository.count();
        stats.put("totalUsers", totalUsers);
        
        // 活跃用户数
        long activeUsers = userRepository.countByActiveTrue();
        stats.put("activeUsers", activeUsers);
        
        // 各权限组用户数
        stats.put("userCount", userRepository.countByUserGroup("user"));
        stats.put("moderatorCount", userRepository.countByUserGroup("moderator"));
        stats.put("adminCount", userRepository.countByUserGroup("admin"));
        stats.put("superadminCount", userRepository.countByUserGroup("superadmin"));
        
        return stats;
    }
    
    // 软删除用户
    public void deleteUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        user.setActive(false);
        userRepository.save(user);
    }
}