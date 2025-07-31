package com.ecowiki.controller.upload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecowiki.dto.ApiResponse;
import com.ecowiki.entity.user.User;
import com.ecowiki.security.JwtUtil;
import com.ecowiki.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 头像上传API控制器
 * 
 * 专门处理用户头像上传功能，提供安全的文件上传服务
 * 支持多种图片格式，自动生成唯一文件名，并更新用户数据库记录
 * 
 * 主要功能：
 * - 头像文件上传和验证
 * - 自动文件命名和存储
 * - 数据库头像URL更新
 * - 文件安全检查
 * 
 * 注意：由于 server.servlet.context-path=/api，所以这里的映射路径为 /avatar
 * 实际访问路径为 /api/avatar/upload
 * 
 * @author EcoWiki Team
 * @version 1.0.0
 * @since 2025-07-25
 */
@RestController
@RequestMapping("/avatar")
public class AvatarUploadController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 头像上传目录路径
     */
    @Value("${avatar.upload.path:uploads/avatars/}")
    private String avatarUploadPath;

    /**
     * 头像文件最大大小（字节）
     */
    @Value("${avatar.max-size:5242880}")
    private long maxAvatarSize;

    /**
     * 服务器基础URL
     */
    @Value("${server.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 上传用户头像
     * 
     * @param request HTTP请求对象，包含JWT令牌
     * @param file 头像文件
     * @return 上传结果，包含新的头像URL
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadAvatar(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file) {

        try {
            // 1. 身份验证
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("未提供认证令牌，请先登录"));
            }

            String username = jwtUtil.extractUsername(token);
            if (username == null || !jwtUtil.isTokenValid(token)) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("认证令牌无效，请重新登录"));
            }

            // 2. 查找用户
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(ApiResponse.error("用户不存在"));
            }

            User user = userOpt.get();

            // 3. 文件验证
            String validationError = validateAvatarFile(file);
            if (validationError != null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(validationError));
            }

            // 4. 删除旧头像文件（如果存在）
            deleteOldAvatar(user.getAvatarUrl());

            // 5. 保存新头像文件
            String avatarUrl = saveAvatarFile(file, username);

            // 6. 更新数据库
            user.setAvatarUrl(avatarUrl);
            userService.saveUser(user);

            // 7. 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);
            result.put("fullUrl", baseUrl + avatarUrl);
            result.put("fileName", getFileNameFromUrl(avatarUrl));
            result.put("uploadTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            return ResponseEntity.ok(ApiResponse.success(result, "头像上传成功"));

        } catch (IOException e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("文件保存失败: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("系统错误: " + e.getMessage()));
        }
    }

    /**
     * 从HTTP请求中提取JWT令牌
     * 
     * @param request HTTP请求对象
     * @return JWT令牌字符串，如果不存在则返回null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 验证头像文件
     * 
     * @param file 上传的文件
     * @return 错误信息，如果验证通过则返回null
     */
    private String validateAvatarFile(MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            return "请选择要上传的头像文件";
        }

        // 检查文件大小
        if (file.getSize() > maxAvatarSize) {
            return String.format("头像文件大小不能超过%.1fMB", maxAvatarSize / 1024.0 / 1024.0);
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            return "只支持JPG、PNG、GIF、WEBP格式的图片文件";
        }

        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasValidImageExtension(originalFilename)) {
            return "文件扩展名不正确，只支持.jpg、.jpeg、.png、.gif、.webp格式";
        }

        return null; // 验证通过
    }

    /**
     * 检查是否为有效的图片MIME类型
     */
    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp");
    }

    /**
     * 检查是否有有效的图片文件扩展名
     */
    private boolean hasValidImageExtension(String filename) {
        String lowerName = filename.toLowerCase();
        return lowerName.endsWith(".jpg") ||
               lowerName.endsWith(".jpeg") ||
               lowerName.endsWith(".png") ||
               lowerName.endsWith(".gif") ||
               lowerName.endsWith(".webp");
    }

    /**
     * 保存头像文件
     * 
     * @param file 上传的文件
     * @param username 用户名
     * @return 头像访问URL
     * @throws IOException 文件操作异常
     */
    private String saveAvatarFile(MultipartFile file, String username) throws IOException {
        // 确保上传目录存在
        Path uploadDir = Paths.get(avatarUploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名：用户名_时间戳_随机UUID.扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String filename = String.format("%s_%s_%s%s", username, timestamp, uniqueId, extension);

        // 保存文件
        Path filePath = uploadDir.resolve(filename);
        Files.write(filePath, file.getBytes());

        // 返回相对URL路径，与WebConfig中的静态资源映射一致
        return "/uploads/avatars/" + filename;
    }

    /**
     * 删除旧的头像文件
     * 
     * @param oldAvatarUrl 旧头像URL
     */
    private void deleteOldAvatar(String oldAvatarUrl) {
        if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
            try {
                // 从URL中提取文件名
                String fileName = getFileNameFromUrl(oldAvatarUrl);
                if (fileName != null) {
                    Path oldFilePath = Paths.get(avatarUploadPath, fileName);
                    if (Files.exists(oldFilePath)) {
                        Files.delete(oldFilePath);
                        // 旧头像文件已删除
                    }
                }
            } catch (IOException e) {
                // 不抛出异常，因为删除旧文件失败不应该影响新文件上传
                // 可以记录到日志中，但不影响主流程
            }
        }
    }

    /**
     * 从URL中提取文件名
     * 
     * @param url 文件URL
     * @return 文件名，如果提取失败则返回null
     */
    private String getFileNameFromUrl(String url) {
        if (url != null && url.contains("/")) {
            return url.substring(url.lastIndexOf("/") + 1);
        }
        return null;
    }

    /**
     * 获取文件扩展名
     * 
     * @param filename 文件名
     * @return 文件扩展名（包括点号），如果没有扩展名则返回空字符串
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}
