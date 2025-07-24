package com.ecowiki.controller.api;

import com.ecowiki.dto.ApiResponse;
import com.ecowiki.entity.User;
import com.ecowiki.security.JwtUtil;
import com.ecowiki.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ApiProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/profile/avatar")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAvatar(
            HttpServletRequest request,
            @RequestParam("avatar") MultipartFile avatarFile) {

        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("缺少认证令牌"));
            }

            String username = jwtUtil.extractUsername(token);
            if (username == null || !jwtUtil.isTokenValid(token)) {
                return ResponseEntity.status(401).body(ApiResponse.error("认证令牌无效"));
            }

            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(ApiResponse.error("用户不存在"));
            }

            User user = userOpt.get();

            String avatarUrl = saveAvatarFile(avatarFile);
            user.setAvatarUrl(avatarUrl);
            userService.saveUser(user);

            Map<String, String> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);
            return ResponseEntity.ok(ApiResponse.success(result, "头像上传成功"));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(ApiResponse.error("头像上传失败"));
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String saveAvatarFile(MultipartFile file) throws IOException {
        String uploadDir = "uploads/avatars/";
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir + filename);
        Files.createDirectories(path.getParent());
        Files.write(path, file.getBytes());
        return "/uploads/avatars/" + filename;
    }
}