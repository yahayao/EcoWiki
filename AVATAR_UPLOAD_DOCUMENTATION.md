# EcoWiki 头像上传系统完整文档

# 进入头像存储目录
cd "d:\桌面\EcoWiki_project\EcoWiki\www\backend\uploads\avatars"

# 查看所有头像文件
dir

# 删除特定用户的头像文件
del "username_20250725_143021_a1b2c3d4.jpg"

# 批量删除某个用户的所有头像
del "username_*"

# 删除所有头像文件（慎用！）
del "*.*"

## 📋 系统概述

EcoWiki 头像上传系统是一个完整的用户头像管理解决方案，支持安全的文件上传、存储和访问。系统采用前后端分离架构，提供 RESTful API 接口和现代化的 Vue.js 组件。

### 🔧 技术栈
- **后端**: Spring Boot 3.2.0, Spring Security, JPA/Hibernate
- **前端**: Vue 3.5.13, TypeScript 5.8.0, Composition API
- **存储**: 本地文件系统 + MySQL 数据库
- **认证**: JWT Bearer Token

### 🌟 主要特性
- ✅ 多格式支持（JPG、PNG、GIF、WEBP）
- ✅ 文件大小限制（最大 5MB）
- ✅ JWT 认证保护
- ✅ 自动文件命名和去重
- ✅ 旧文件自动清理
- ✅ 实时上传进度显示
- ✅ 拖拽上传支持
- ✅ 响应式设计

---

## 🏗️ 系统架构

### 架构图
```
前端 Vue.js 应用 (localhost:5173)
    ↓ HTTP POST /api/avatar/upload
Vite 代理服务器
    ↓ 转发请求
后端 Spring Boot (localhost:8080/api)
    ↓ AvatarUploadController
文件系统存储 (uploads/avatars/)
    ↓ 静态资源访问
WebConfig 静态资源映射 (/uploads/avatars/**)
    ↓ 数据库记录
MySQL (user.avatar_url 字段)
```

### 关键配置说明
1. **Context Path**: `server.servlet.context-path=/api`
2. **Controller 映射**: `@RequestMapping("/avatar")` → 实际路径 `/api/avatar`
3. **静态资源映射**: `/uploads/avatars/**` → `uploads/avatars/` 目录
4. **前端 API Base**: `http://localhost:8080/api`

---

## 🔧 后端实现

### 1. 核心控制器 - AvatarUploadController.java

**位置**: `src/main/java/com/ecowiki/controller/api/AvatarUploadController.java`

```java
@RestController
@RequestMapping("/avatar")  // 注意：不是 /api/avatar
public class AvatarUploadController {
    
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadAvatar(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file) {
        // 实现细节...
    }
}
```

**核心功能**:
- JWT 令牌验证
- 文件类型和大小验证
- 唯一文件名生成
- 数据库记录更新
- 旧文件清理

### 2. 配置文件 - application.properties

```properties
# 服务器配置
server.port=8080
server.servlet.context-path=/api

# 头像上传配置
avatar.upload.path=uploads/avatars/
avatar.max-size=5242880
server.base-url=http://localhost:8080

# 文件上传配置
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
spring.servlet.multipart.file-size-threshold=1MB
```

### 3. 静态资源配置 - WebConfig.java

**位置**: `src/main/java/com/ecowiki/config/WebConfig.java`

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 头像文件访问：/uploads/avatars/** → uploads/avatars/
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + getAbsolutePath(avatarUploadPath))
                .setCachePeriod(86400);  // 24小时缓存
    }
}
```

### 4. 数据库表结构

```sql
-- 用户表中的头像字段
CREATE TABLE `user` (
  `user_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) UNIQUE NOT NULL,
  `avatar_url` VARCHAR(255) COMMENT '头像URL路径',
  -- 其他字段...
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 🎨 前端实现

### 1. 环境配置 - .env

**位置**: `frontend/.env`

```env
# API服务器地址
VITE_API_BASE_URL=http://localhost:8080

# 开发环境配置
NODE_ENV=development
VITE_APP_DEV_TOOLS=true
```

### 2. 核心组件 - AvatarUpload.vue

**位置**: `src/components/common/AvatarUpload.vue`

**主要功能**:
- 头像预览显示
- 拖拽上传支持
- 上传进度指示
- 文件验证提示
- 错误处理

**使用示例**:
```vue
<template>
  <AvatarUpload 
    :username="user?.username"
    :current-avatar-url="user?.avatarUrl"
    @upload-success="handleAvatarUploadSuccess"
    @upload-error="handleAvatarUploadError"
  />
</template>

<script setup lang="ts">
import AvatarUpload from '@/components/common/AvatarUpload.vue'

const handleAvatarUploadSuccess = (result) => {
  console.log('头像上传成功:', result)
  // 更新用户信息...
}

const handleAvatarUploadError = (error) => {
  console.error('头像上传失败:', error)
  // 显示错误信息...
}
</script>
```

### 3. API 接口调用

**位置**: `src/api/index.ts`

```typescript
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
})

// 头像上传请求示例
const uploadAvatar = async (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  
  const response = await api.post('/avatar/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: (progressEvent) => {
      // 处理上传进度...
    }
  })
  
  return response.data
}
```

---

## 🚀 部署和使用

### 1. 开发环境启动

#### 后端启动
```bash
cd EcoWiki/www/backend
mvn clean compile
mvn spring-boot:run
```
服务器启动在: `http://localhost:8080`

#### 前端启动
```bash
cd EcoWiki/www/frontend
npm install
npm run dev
```
开发服务器启动在: `http://localhost:5173`

### 2. 目录结构

启动后会自动创建以下目录结构：
```
EcoWiki/www/backend/
├── uploads/
│   └── avatars/              # 头像文件存储目录
│       ├── user1_20250725_143021_a1b2c3d4.jpg
│       ├── user2_20250725_143156_e5f6g7h8.png
│       └── ...
└── src/
    └── main/
        └── resources/
            └── application.properties
```

### 3. 文件命名规则

上传的头像文件按以下规则命名：
```
格式: {username}_{timestamp}_{uniqueId}.{extension}
示例: admin_20250725_143021_a1b2c3d4.jpg

其中:
- username: 用户名
- timestamp: 上传时间戳 (yyyyMMdd_HHmmss)
- uniqueId: 8位随机UUID
- extension: 原文件扩展名
```

---

## 🔗 API 接口文档

### POST /api/avatar/upload

**描述**: 上传用户头像

**请求方式**: `POST`

**请求路径**: `/api/avatar/upload`

**完整URL**: `http://localhost:8080/api/avatar/upload`

**请求头**:
```http
Authorization: Bearer {jwt_token}
Content-Type: multipart/form-data
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 头像图片文件 |

**文件限制**:
- 支持格式: JPG, JPEG, PNG, GIF, WEBP
- 最大大小: 5MB
- MIME类型: image/jpeg, image/png, image/gif, image/webp

**响应示例**:

**成功响应 (200)**:
```json
{
  "code": 200,
  "message": "头像上传成功",
  "data": {
    "avatarUrl": "/uploads/avatars/admin_20250725_143021_a1b2c3d4.jpg",
    "fullUrl": "http://localhost:8080/uploads/avatars/admin_20250725_143021_a1b2c3d4.jpg",
    "fileName": "admin_20250725_143021_a1b2c3d4.jpg",
    "uploadTime": "2025-07-25 14:30:21"
  }
}
```

**错误响应**:

**401 未授权**:
```json
{
  "code": 401,
  "message": "未提供认证令牌，请先登录",
  "data": null
}
```

**400 参数错误**:
```json
{
  "code": 400,
  "message": "只支持JPG、PNG、GIF、WEBP格式的图片文件",
  "data": null
}
```

**500 服务器错误**:
```json
{
  "code": 500,
  "message": "文件保存失败: IOException details",
  "data": null
}
```

---

## 🛡️ 安全机制

### 1. 认证验证
- 所有上传请求必须携带有效的 JWT 令牌
- 令牌验证失败自动返回 401 状态码
- 支持 Bearer Token 格式

### 2. 文件验证
- **扩展名检查**: 只允许 .jpg, .jpeg, .png, .gif, .webp
- **MIME类型检查**: 验证文件真实类型
- **文件大小限制**: 最大 5MB
- **文件内容检查**: 确保是有效的图片文件

### 3. 存储安全
- 自动生成唯一文件名，避免文件覆盖
- 文件存储在服务器本地，不直接暴露原始路径
- 旧文件自动删除，节省存储空间

---

## 🔍 故障排除

### 常见问题

#### 1. 上传后返回 500 错误
**可能原因**:
- 文件系统权限不足
- 上传目录不存在
- JWT 令牌无效

**解决方案**:
```bash
# 检查目录权限
ls -la uploads/avatars/

# 手动创建目录
mkdir -p uploads/avatars/

# 检查应用日志
tail -f logs/spring.log
```

#### 2. 静态资源访问 404
**可能原因**:
- WebConfig 配置错误
- 文件路径不匹配
- 静态资源映射失效

**解决方案**:
1. 检查 WebConfig.java 中的路径映射
2. 确认文件确实存在于 `uploads/avatars/` 目录
3. 重启服务器重新加载配置

#### 3. 前端无法上传
**可能原因**:
- CORS 配置问题
- API 路径错误
- 认证头丢失

**解决方案**:
1. 检查浏览器控制台错误信息
2. 确认 JWT 令牌是否有效
3. 验证 API 路径配置

### 调试日志

启用详细日志查看上传过程：

```properties
# application.properties
logging.level.com.ecowiki.controller.api.AvatarUploadController=DEBUG
logging.level.org.springframework.web.multipart=DEBUG
logging.level.org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping=TRACE
```

---

## 🔄 升级和维护

### 1. 版本兼容性
- Spring Boot 3.2.0+
- Vue.js 3.5.13+
- Java 17+
- MySQL 8.0+

### 2. 性能优化建议
- 配置 CDN 加速静态资源访问
- 启用 Gzip 压缩减少传输大小
- 配置适当的缓存策略
- 定期清理未使用的头像文件

### 3. 扩展功能计划
- [ ] 头像裁剪和缩放
- [ ] 多尺寸头像生成
- [ ] 云存储支持（阿里云OSS、AWS S3）
- [ ] 头像审核机制
- [ ] 批量上传支持

---

## 📞 技术支持

如有问题或建议，请联系：

- **项目仓库**: https://github.com/yahayao/EcoWiki
- **开发团队**: EcoWiki Team
- **当前版本**: v1.0.0
- **最后更新**: 2025年7月25日

---

*本文档将随着系统功能的更新而持续维护。*
