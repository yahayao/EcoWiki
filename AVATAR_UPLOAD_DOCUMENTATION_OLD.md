# EcoWiki 头像上传系统使用文档

## 📋 系统概述

EcoWiki 头像上传系统提供了完整的用户头像管理功能，包括文件上传、存储、访问和数据库更新。系统采用前后端分离架构，支持多种图片格式，具有文件大小限制和安全验证。

## 🏗️ 系统架构

### 后端架构

- **Controller**: `AvatarUploadController` - 处理头像上传API
- **Configuration**: `WebConfig` - 配置静态资源访问
- **Entity**: `User` - 用户实体，包含avatarUrl字段
- **Service**: `UserService` - 用户业务逻辑

### 前端架构

- **Component**: `AvatarUpload.vue` - 头像上传组件
- **Integration**: 集成到`UserInformation.vue`中
- **Composable**: 使用`useAuth`管理用户状态

## 📊 数据库结构

头像URL存储在用户表的`avatar_url`字段中：

```sql
ALTER TABLE `user` 
ADD COLUMN `avatar_url` VARCHAR(255) COMMENT '头像URL';
```

**字段说明**：

- 类型：VARCHAR(255)
- 可空：是
- 存储内容：相对路径，如 `/avatars/username_20250725_143000_abc12345.jpg`
- 完整URL：`http://localhost:8080/avatars/filename`

## 🔧 配置说明

### 后端配置 (application.properties)

```properties
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

### 前端配置

在 `.env` 文件中配置：

```env
VITE_API_BASE_URL=http://localhost:8080
```

## 📡 API接口文档

### 上传头像接口

**接口地址**: `POST /api/avatar/upload`

**请求头**:

```
Authorization: Bearer {jwt_token}
Content-Type: multipart/form-data
```

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 头像图片文件 |

**文件限制**:

- 支持格式：JPG, JPEG, PNG, GIF, WEBP
- 文件大小：最大5MB
- MIME类型：image/jpeg, image/png, image/gif, image/webp

**成功响应** (200):

```json
{
  "code": 200,
  "message": "头像上传成功",
  "data": {
    "avatarUrl": "/avatars/username_20250725_143000_abc12345.jpg",
    "fullUrl": "http://localhost:8080/avatars/username_20250725_143000_abc12345.jpg",
    "fileName": "username_20250725_143000_abc12345.jpg",
    "uploadTime": "2025-07-25 14:30:00"
  }
}
```

**错误响应**:

```json
{
  "code": 400,
  "message": "只支持JPG、PNG、GIF、WEBP格式的图片文件",
  "data": null
}
```

**常见错误代码**:

- 401: 未认证或令牌无效
- 404: 用户不存在
- 400: 文件验证失败
- 500: 服务器内部错误

## 🎨 前端组件使用

### AvatarUpload 组件

**基本用法**:

```vue
<template>
  <AvatarUpload 
    :username="user?.username"
    :current-avatar-url="user?.avatarUrl"
    @upload-success="handleUploadSuccess"
    @upload-error="handleUploadError"
  />
</template>

<script setup>
import AvatarUpload from '@/components/common/AvatarUpload.vue'

const handleUploadSuccess = (result) => {
  console.log('上传成功:', result)
  // 处理上传成功逻辑
}

const handleUploadError = (error) => {
  console.error('上传失败:', error)
  // 处理上传失败逻辑
}
</script>
```

**Props**:

| 属性名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| username | string | '' | 用户名 |
| currentAvatarUrl | string | '' | 当前头像URL |
| size | 'small' \| 'medium' \| 'large' | 'medium' | 头像大小 |

**Events**:

| 事件名 | 参数 | 说明 |
|--------|------|------|
| upload-success | result: AvatarUploadResult | 上传成功时触发 |
| upload-error | error: string | 上传失败时触发 |

## 🔄 文件处理流程

### 上传流程

1. 用户选择图片文件
2. 前端验证文件类型和大小
3. 显示上传进度
4. 发送FormData到后端
5. 后端验证用户身份
6. 后端验证文件安全性
7. 删除旧头像文件（如果存在）
8. 生成唯一文件名并保存
9. 更新数据库用户记录
10. 返回新头像URL

### 文件命名规则

```
格式: {username}_{timestamp}_{uniqueId}.{extension}
示例: johndoe_20250725_143000_abc12345.jpg
```

### 文件存储结构

```
uploads/
└── avatars/
    ├── user1_20250725_143000_abc12345.jpg
    ├── user2_20250725_143100_def67890.png
    └── ...
```

## 🌐 静态资源访问

### URL映射

- **存储路径**: `uploads/avatars/filename.jpg`
- **访问URL**: `http://localhost:8080/avatars/filename.jpg`
- **数据库存储**: `/avatars/filename.jpg`

### WebConfig配置

```java
registry.addResourceHandler("/avatars/**")
        .addResourceLocations("file:" + getAbsolutePath(avatarUploadPath))
        .setCachePeriod(86400)  // 缓存24小时
        .resourceChain(false);
```

## 🔒 安全考虑

### 文件安全

- 文件类型白名单验证
- 文件大小限制（5MB）
- MIME类型检查
- 文件扩展名验证
- 唯一文件名生成（防止覆盖）

### 访问控制

- JWT令牌验证
- 用户身份验证
- 只允许用户修改自己的头像

### 防护措施

- 自动删除旧头像文件
- 错误处理和日志记录
- 路径遍历攻击防护

## 🚀 部署说明

### 开发环境部署

1. 确保后端Spring Boot应用运行在8080端口
2. 确保前端Vue应用配置正确的API地址
3. 创建头像存储目录：`uploads/avatars/`
4. 配置文件上传权限

### 生产环境部署

1. 配置Nginx代理静态文件访问
2. 设置文件存储目录权限
3. 配置HTTPS和安全头
4. 监控磁盘空间使用

### Nginx配置示例

```nginx
location /avatars/ {
    alias /path/to/uploads/avatars/;
    expires 24h;
    add_header Cache-Control "public, immutable";
}
```

## 🐛 故障排除

### 常见问题

**1. 头像上传失败，提示"系统数据信息重试"**

- 检查JWT令牌是否有效
- 确认用户是否存在
- 检查文件格式和大小

**2. 头像无法显示**

- 检查静态资源配置
- 确认文件路径正确性
- 检查服务器端口和地址

**3. 文件保存失败**

- 检查目录权限
- 确认磁盘空间充足
- 查看后端日志

### 调试技巧

1. 检查浏览器开发者工具网络面板
2. 查看后端控制台日志
3. 验证文件系统权限
4. 测试API接口独立功能

## 📝 更新日志

### 版本 1.0.0 (2025-07-25)

- 实现完整的头像上传功能
- 支持多种图片格式
- 添加文件安全验证
- 实现进度显示和错误处理
- 提供完整的前后端集成

## 🤝 开发团队

- **后端开发**: EcoWiki Team
- **前端开发**: EcoWiki Team
- **系统架构**: EcoWiki Team

---

如有问题或建议，请联系开发团队。
