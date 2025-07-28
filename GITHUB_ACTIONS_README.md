# GitHub Actions 工作流说明

这个项目包含了完整的 CI/CD 流水线，使用 GitHub Actions 实现自动化构建、测试、安全扫描和部署。

## 🚀 工作流概览

### 1. CI/CD Pipeline (`ci-cd.yml`)
主要的持续集成和持续部署工作流，在以下情况触发：
- 推送到 `main` 或 `develop` 分支
- 向 `main` 分支创建 Pull Request

**包含的作业：**
- **Backend Test**: 构建和测试后端 Spring Boot 应用
- **Frontend Test**: 构建和测试前端 Vue 应用
- **Security Scan**: 使用 Trivy 进行安全漏洞扫描
- **Deploy Staging**: 部署到预发布环境 (develop 分支)
- **Deploy Production**: 部署到生产环境 (main 分支)

### 2. Code Quality (`code-quality.yml`)
代码质量检查工作流，包括：
- **Backend Quality**: Maven 测试、SonarCloud 分析、覆盖率报告
- **Frontend Quality**: ESLint、Prettier、TypeScript 类型检查
- **Dependency Security**: Snyk 依赖安全扫描
- **CodeQL Analysis**: GitHub 安全代码分析

### 3. Dependency Update (`dependency-update.yml`)
自动依赖更新工作流：
- 每周一自动检查并更新依赖
- 为后端 Maven 依赖和前端 npm 依赖分别创建 PR
- 支持手动触发

### 4. Release (`release.yml`)
发布工作流，在推送版本标签时触发：
- 创建 GitHub Release
- 构建并推送 Docker 镜像到 GitHub Container Registry
- 部署到生产环境
- 发送通知

## 🔧 配置要求

### GitHub Secrets
在 GitHub 仓库设置中配置以下 secrets：

#### 必需的 Secrets
```
GITHUB_TOKEN  # GitHub 自动提供，用于基本操作
```

#### 可选的 Secrets (用于增强功能)
```
SONAR_TOKEN           # SonarCloud 代码质量分析
SNYK_TOKEN           # Snyk 安全扫描
CODECOV_TOKEN        # Codecov 覆盖率报告
SLACK_WEBHOOK_URL    # Slack 通知
```

### 环境配置
在 GitHub 仓库设置中创建以下环境：
- `staging` - 预发布环境
- `production` - 生产环境

## 🐳 Docker 支持

### 本地开发
```bash
# 启动开发环境 (MySQL + Redis + 管理界面)
docker-compose -f docker-compose.dev.yml up -d

# 访问管理界面
# phpMyAdmin: http://localhost:8081
# Redis Commander: http://localhost:8082
```

### 完整应用
```bash
# 构建并启动完整应用
docker-compose up --build

# 后台运行
docker-compose up -d --build

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 生产环境
```bash
# 使用生产配置启动
docker-compose --profile production up -d
```

## 📋 工作流详细说明

### Backend Pipeline
1. **环境准备**: JDK 17, MySQL 8.0 测试数据库
2. **依赖缓存**: Maven 依赖缓存优化构建速度
3. **测试执行**: 运行单元测试和集成测试
4. **构建打包**: 生成可执行 JAR 文件
5. **Docker 构建**: 多阶段构建优化镜像大小

### Frontend Pipeline
1. **环境准备**: Node.js 18, pnpm
2. **依赖管理**: pnpm 缓存和安装
3. **代码检查**: TypeScript 类型检查、ESLint、Prettier
4. **构建打包**: Vite 生产构建
5. **Docker 构建**: Nginx 静态文件服务

### Security Features
- **Trivy**: 文件系统漏洞扫描
- **Snyk**: 依赖安全扫描
- **CodeQL**: 静态代码安全分析
- **SonarCloud**: 代码质量和安全问题检测

## 🚀 部署流程

### 分支策略
- `main`: 生产分支，自动部署到生产环境
- `develop`: 开发分支，自动部署到预发布环境
- `feature/*`: 功能分支，运行测试但不部署

### 发布流程
1. 创建版本标签: `git tag v1.0.0 && git push origin v1.0.0`
2. 自动触发发布工作流
3. 构建 Docker 镜像并推送到注册表
4. 部署到生产环境
5. 创建 GitHub Release
6. 发送通知

## 📊 监控和通知

### 健康检查
- 后端: `/actuator/health`
- 前端: `/health`
- 数据库: MySQL ping
- 缓存: Redis ping

### 通知集成
- Slack 集成用于发布通知
- GitHub 环境保护规则
- PR 状态检查

## 🛠️ 本地开发设置

### 后端开发
```bash
cd www/backend
mvn spring-boot:run
```

### 前端开发
```bash
cd www/frontend
pnpm install
pnpm run dev
```

### 数据库管理
- 使用 docker-compose.dev.yml 启动数据库
- 通过 phpMyAdmin (http://localhost:8081) 管理数据库
- 通过 Redis Commander (http://localhost:8082) 管理缓存

## 🔍 故障排除

### 常见问题
1. **构建失败**: 检查依赖版本兼容性
2. **测试失败**: 确保数据库连接正常
3. **部署失败**: 检查环境变量和 secrets 配置
4. **Docker 构建失败**: 检查 Dockerfile 和依赖文件

### 调试方法
- 查看 GitHub Actions 日志
- 使用 `docker-compose logs` 查看容器日志
- 检查健康检查端点状态

## 📚 更多资源

- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [Docker Compose 参考](https://docs.docker.com/compose/)
- [Spring Boot 文档](https://spring.io/projects/spring-boot)
- [Vue.js 文档](https://vuejs.org/)
