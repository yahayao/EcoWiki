package com.ecowiki.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ecowiki.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    // 权限相关查询
    long countByActiveTrue();
    long countByUserGroup(String userGroup);
    
    @Query("SELECT u FROM User u WHERE u.active = true")
    java.util.List<User> findAllActiveUsers();
    
    @Query("SELECT u FROM User u WHERE u.userGroup = ?1")
    java.util.List<User> findByUserGroup(String userGroup);

}