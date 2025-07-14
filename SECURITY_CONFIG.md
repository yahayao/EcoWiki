# 环境配置说明

## 安全配置指南

本项目包含敏感的配置信息，已通过 `.gitignore` 文件进行保护。

### 后端配置

#### 数据库配置
敏感的数据库连接信息存储在 `application-local.properties` 文件中，该文件不会被提交到Git。

**首次设置：**
1. 复制 `application-local.properties.example` 为 `application-local.properties`
2. 修改其中的数据库连接信息为你的实际配置：
   ```properties
   spring.datasource.url=jdbc:mysql://your-host:port/database-name
   spring.datasource.username=your-username
   spring.datasource.password=your-password
   ```

#### 已忽略的敏感文件
- `application-local.properties` - 本地数据库配置
- `temp_*.json` - 临时测试文件
- `*token*.json` - JWT令牌文件
- `*login*.json` - 登录测试文件

### 前端配置

#### 环境变量配置
前端使用 Vite 的环境变量系统，敏感的本地配置存储在 `.env.*.local` 文件中。

**首次设置：**
1. 复制 `.env.development` 为 `.env.development.local`
2. 根据你的后端配置修改 API 地址

#### 已忽略的敏感文件
- `.env.local` - 本地环境变量
- `.env.*.local` - 特定环境的本地配置

## 开发建议

1. **永远不要提交包含真实密码、数据库连接或API密钥的文件**
2. **使用 `.example` 文件作为配置模板**
3. **定期检查 `git status` 确保敏感文件未被意外添加**
4. **团队成员应各自维护自己的本地配置文件**

## 检查命令

检查是否有敏感文件被意外跟踪：
```bash
git ls-files | Select-String -Pattern "(temp_|\.env\..*\.local|application-local\.properties)"
```

如果发现敏感文件被跟踪，使用以下命令移除：
```bash
git rm --cached path/to/sensitive/file
```
