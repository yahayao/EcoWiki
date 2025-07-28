# EcoWiki 一键部署指南

## 概述

EcoWiki 是一个现代化的知识共享平台，基于 Spring Boot 3.x + Vue 3 + TypeScript 构建。本项目提供了完整的一键部署方案，包含数据库初始化、配置自动化和环境检测。系统支持文章版本控制、权限管理、审核系统等高级功能。

## 系统架构

### 技术栈

- **后端**: Spring Boot 3.2.0 + Spring Security + Spring Data JPA
- **前端**: Vue 3.5.13 + TypeScript 5.8.0 + Vite 6.2.4 + Pinia
- **数据库**: MySQL 8.0+ / MariaDB 10.3+
- **构建工具**: Maven 3.6+ + pnpm
- **运行环境**: Java 17+ + Node.js 18+

### 核心特性

- 🔐 基于JWT的身份认证和RBAC权限控制
- 📝 文章版本控制系统，支持差异存储和历史回溯
- 🔍 全文搜索和智能分类标签系统
- 🛡️ 内容审核系统和安全防护机制
- 📊 数据统计和性能监控
- 🎨 响应式UI设计和现代化用户体验

## 文件说明

### 核心部署文件

- `COMPLETE_DATABASE_INIT.sql` - 完整的数据库初始化脚本
- `deploy.bat` - Windows 一键部署脚本
- `deploy.sh` - Linux/macOS 一键部署脚本
- `DEPLOYMENT_GUIDE.md` - 本部署指南
- `start_in_vscode.bat` - VS Code 开发环境快速启动

### 项目结构说明

- `www/backend/` - Spring Boot 后端项目
- `www/frontend/` - Vue 3 前端项目
- `describe/` - 项目文档和演示图片
- `scripts/` - 部署和维护脚本

### 已整合的功能模块

本版本已集成以下功能模块，无需单独配置：

- ✅ 用户权限管理系统 (RBAC)
- ✅ 文章版本控制系统
- ✅ 内容审核工作流
- ✅ 消息通知系统
- ✅ 头像上传功能
- ✅ 分类标签管理

## 部署步骤

### Windows 部署

1. **前置条件**
   - 确保已安装 MySQL 5.7+ 或 MariaDB 10.3+
   - 确保 MySQL 客户端在系统 PATH 中
   - 确保具有数据库创建权限

2. **执行部署**
   ```cmd
   # 在项目根目录下执行
   deploy.bat
   ```

3. **按提示输入**
   - 数据库主机地址（默认：localhost）
   - 数据库端口（默认：3306）
   - 数据库名称（默认：ecowiki）
   - 数据库用户名（默认：root）
   - 数据库密码

### Linux/macOS 部署

1. **赋予执行权限**
   ```bash
   chmod +x deploy.sh
   ```

2. **执行部署**
   ```bash
   ./deploy.sh
   ```

3. **按提示输入配置信息**

## 数据库结构

### 核心表
- `user` - 用户表
- `role` - 角色表  
- `permission` - 权限表
- `user_roles` - 用户角色关联表
- `role_permissions` - 角色权限关联表
- `articles` - 文章表
- `tags` - 标签表
- `article_tags` - 文章标签关联表

### 版本管理表（可选）
- `article_versions` - 文章版本表
- `article_version_stats` - 版本统计表
- `article_version_config` - 版本配置表

### 系统视图
- `v_user_permissions` - 用户权限汇总视图
- `v_article_stats` - 文章统计视图
- `v_article_version_summary` - 版本统计视图

## 默认数据

### 预置角色
1. **user** - 普通用户（查看文章、发表评论）
2. **moderator** - 版主（管理文章和评论）
3. **admin** - 管理员（用户和权限管理）
4. **superadmin** - 超级管理员（全部权限）

### 预置权限
- 文章管理：查看、编辑、删除文章
- 评论管理：查看、发表、删除评论
- 用户管理：查看、编辑、删除用户
- 系统管理：角色权限、系统设置、日志查看
- 内容管理：标签、分类管理

### 默认管理员账户
- **用户名**: superadmin
- **密码**: EcoWiki@2025
- **邮箱**: admin@ecowiki.com

**⚠️ 重要提醒**: 部署完成后请立即登录并修改默认密码！

## 启动服务

### 后端服务
```bash
cd www/backend
mvn spring-boot:run
```

### 前端服务
```bash
cd www/frontend
npm install  # 首次运行需要安装依赖
npm run dev
```

## 配置文件

### 后端配置
部署脚本会自动创建 `www/backend/src/main/resources/application-local.properties` 文件，包含数据库连接配置。

### 手动配置
如需手动配置，可参考以下模板：

```properties
# MySQL数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/ecowiki?useSSL=true&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8
spring.datasource.username=root
spring.datasource.password=your_password

# 连接池配置
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
```

## 故障排除

### 常见问题

1. **MySQL 连接失败**
   - 检查 MySQL 服务是否启动
   - 验证用户名密码是否正确
   - 确认防火墙设置

2. **权限不足**
   - 确保数据库用户具有创建数据库权限
   - 检查 MySQL 用户权限设置

3. **字符集问题**
   - 确保数据库字符集为 utf8mb4
   - 检查 MySQL 配置文件字符集设置

4. **端口占用**
   - 后端默认端口：8080
   - 前端默认端口：5173
   - 如有冲突请修改相应配置

### 日志查看

- **后端日志**: 控制台输出或 logs 目录
- **前端日志**: 浏览器开发者工具
- **数据库日志**: MySQL 错误日志

## 环境要求

### 系统要求

**最低要求:**

- **操作系统**: Windows 10+ / Linux / macOS
- **Java**: JDK 17+
- **Node.js**: 18+
- **pnpm**: 7+
- **MySQL**: 8.0+ 或 MariaDB 10.3+
- **内存**: 4GB+
- **磁盘**: 10GB+

**推荐配置:**

- **内存**: 8GB+
- **CPU**: 4核+
- **磁盘**: SSD 20GB+

### 开发工具推荐

- **IDE**: IntelliJ IDEA, VS Code
- **数据库客户端**: MySQL Workbench, Navicat
- **API测试**: Postman, Insomnia
- **版本控制**: Git 2.30+

## 安全建议

1. **修改默认密码**: 立即修改默认管理员密码
2. **数据库安全**: 使用专用数据库用户，避免使用 root
3. **网络安全**: 配置防火墙，限制数据库访问
4. **备份策略**: 定期备份数据库和配置文件
5. **更新维护**: 定期更新依赖和安全补丁

## 技术支持

### 故障排除指南

如遇到问题，请按以下步骤排查：

1. 查看错误日志
2. 检查网络连接
3. 验证配置文件
4. 重启相关服务
5. 查阅文档或联系开发团队

### 获取帮助

- **项目主页**: [EcoWiki GitHub](https://github.com/yahayao/EcoWiki)
- **开发博客**: https://www.edaylogy.com/1750395139677
- **文档中心**: 项目根目录下的各类文档文件
- **问题反馈**: GitHub Issues

### 开发团队

- **核心成员**: Alng、啊啊、倾仙、兔崽
- **项目状态**: 持续开发中
- **开源协议**: Apache 2.0 / GPL 3.0

---

**EcoWiki 开发团队**  
版本: 4.0  
更新日期: 2025-07-28  
文档状态: ✅ 已优化更新
