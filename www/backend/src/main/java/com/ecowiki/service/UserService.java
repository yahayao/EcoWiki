package com.ecowiki.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecowiki.dto.UserRegistrationDto;
import com.ecowiki.entity.User;
import com.ecowiki.repository.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
    
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
    
    public User registerUser(UserRegistrationDto dto) {
        if (!isUsernameAvailable(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (!isEmailAvailable(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        
        return userRepository.save(user);
    }
}