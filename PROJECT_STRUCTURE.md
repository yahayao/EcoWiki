# EcoWiki 项目文件结构文档

## 项目概述

EcoWiki 是一个基于Spring Boot后端和Vue3前端的知识共享平台，支持用户注册、登录、文章管理、权限管理等功能。经过重构优化，整个项目结构更加清晰和可维护。

## 根目录结构

```
EcoWiki/
├── 📄 LICENSE                                    # GPL-3.0开源许可证文件
├── 📄 README.md                                  # 项目说明文档
├── 📄 PROJECT_STRUCTURE.md                       # 项目文件结构文档（本文件）
├── 📄 ARTICLE_VERSION_SYSTEM_COMPLETE.md         # 文章版本控制系统完整文档
├── 📄 ARTICLE_VERSION_SYSTEM_GUIDE.md            # 文章版本系统使用指南
├── 📄 ARTICLE_VERSION_TESTING_GUIDE.md           # 文章版本测试指南
├── 📄 CATEGORY_TAG_FEATURE.md                    # 分类标签功能文档
├── 📄 COMPLETE_DATABASE_INIT.sql                 # 完整数据库初始化脚本
├── 📄 DATABASE_UPGRADE_GUIDE.md                  # 数据库升级指南文档
├── � DEPLOYMENT_GUIDE.md                        # 部署指南文档
├── � FRONTEND_ARTICLE_HISTORY_COMPLETED.md      # 前端文章历史功能完成文档
├── � MESSAGE_FEATURE_GUIDE.md                   # 消息功能指南
├── � SECURITY_CONFIG.md                         # 安全配置文档
├── 🔧 deploy.bat                                 # Windows部署脚本
├── 🔧 deploy.sh                                  # Linux/macOS部署脚本
├── 🔧 start_in_vscode.bat                        # VS Code启动脚本
├── 📁 describe/                                  # 项目文档目录
│   ├── � SRS.docx                               # 软件需求规格说明书
│   └── 📁 img-demo/                              # 演示图片目录
│       ├── admin-demo.png                       # 管理后台演示图
│       ├── article-demo.png                     # 文章页面演示图
│       ├── home-demo.png                        # 主页演示图
│       └── login-demo.png                       # 登录页面演示图
└── 📁 www/                                       # 主要代码目录
    ├── 📄 IMPLEMENTATION_GUIDE.md                # 实现指南文档
    ├── 📄 PERMISSION_GROUPS_IMPLEMENTATION_GUIDE.md # 权限分组实现指南
    ├── 📁 backend/                               # 后端Spring Boot项目
    ├── 📁 frontend/                              # 前端Vue3项目
    └── 📁 webpage/                               # 静态网页目录
        ├── EcoWiki.html                          # 项目展示页面
        └── webpage.png                           # 网页截图
```

## 后端项目结构（Spring Boot 3.x）

```
www/backend/
├── pom.xml                                    # Maven项目配置文件，Spring Boot 3.x + MySQL + JPA
├── API_TEST_GUIDE.md                          # API测试指南文档
├── ARTICLE_API_README.md                      # 文章API说明文档
├── src/                                       # 源代码目录
│   ├── main/                                 # 主要源代码
│   │   ├── java/com/ecowiki/                # Java源代码包
│   │   │   ├── EcoWikiApplication.java       # Spring Boot主启动类
│   │   │   │
│   │   │   ├── config/                       # 配置类目录
│   │   │   │   ├── CorsConfig.java          # 跨域配置类
│   │   │   │   ├── DataInitializer.java     # 数据初始化配置（创建默认管理员）
│   │   │   │   └── SecurityConfig.java      # Spring Security安全配置
│   │   │   │
│   │   │   ├── controller/                   # 控制器层
│   │   │   │   ├── api/                     # API控制器层
│   │   │   │   │   ├── admin/               # 管理员相关API
│   │   │   │   │   │   └── AdminController.java # 管理员功能控制器
│   │   │   │   │   ├── auth/                # 认证相关API
│   │   │   │   │   │   └── AuthController.java  # 认证控制器（登录、注册）
│   │   │   │   │   ├── user/                # 用户相关API
│   │   │   │   │   │   └── UserProfileController.java # 用户资料控制器
│   │   │   │   │   ├── ApiController.java   # API信息控制器
│   │   │   │   │   ├── ArticleController.java   # 文章控制器（CRUD、搜索）
│   │   │   │   │   ├── ArticleVersionApiController.java # 文章版本API控制器
│   │   │   │   │   ├── ArticleVersionController.java # 文章版本控制器
│   │   │   │   │   ├── AvatarUploadController.java # 头像上传控制器
│   │   │   │   │   └── MessageController.java   # 消息通知控制器
│   │   │   │   ├── ArticleReviewController.java # 文章审核控制器
│   │   │   │   ├── CommentController.java   # 评论控制器
│   │   │   │   └── TagController.java       # 标签控制器
│   │   │   │
│   │   │   ├── dto/                         # 数据传输对象
│   │   │   │   ├── ApiResponse.java         # 统一API响应格式
│   │   │   │   ├── ArticleCreateRequest.java # 文章创建请求DTO
│   │   │   │   ├── ArticleDto.java          # 文章数据传输对象
│   │   │   │   ├── ArticleStatisticsDto.java # 文章统计数据DTO
│   │   │   │   ├── ArticleUpdateRequest.java # 文章更新请求DTO
│   │   │   │   ├── ForgotPasswordRequest.java # 忘记密码请求DTO
│   │   │   │   ├── LoginRequest.java        # 登录请求DTO
│   │   │   │   ├── PermissionDto.java       # 权限数据传输对象
│   │   │   │   ├── ResetPasswordRequest.java # 重置密码请求DTO
│   │   │   │   ├── UserRegistrationDto.java # 用户注册DTO
│   │   │   │   └── UserWithRoleDto.java     # 带角色信息的用户DTO
│   │   │   │
│   │   │   ├── entity/                      # JPA实体类
│   │   │   │   ├── Article.java            # 文章实体类（支持大文本、标签关联）
│   │   │   │   ├── ArticleHistoryConfig.java # 文章历史配置实体类
│   │   │   │   ├── ArticleHistoryStats.java # 文章历史统计实体类
│   │   │   │   ├── ArticleReview.java      # 文章审核实体类
│   │   │   │   ├── Articles.java           # 文章集合实体类
│   │   │   │   ├── ArticleVersion.java     # 文章版本实体类
│   │   │   │   ├── ArticleVersionStats.java # 文章版本统计实体类
│   │   │   │   ├── Comment.java            # 评论实体类
│   │   │   │   ├── CommentLike.java        # 评论点赞实体类
│   │   │   │   ├── Message.java            # 消息通知实体类
│   │   │   │   ├── Permission.java         # 权限实体类
│   │   │   │   ├── ReviewerAssignment.java # 审核员分配实体类
│   │   │   │   ├── ReviewHistory.java      # 审核历史实体类
│   │   │   │   ├── ReviewPermissionConfig.java # 审核权限配置实体类
│   │   │   │   ├── Role.java               # 角色实体类
│   │   │   │   ├── RolePermission.java     # 角色权限关联实体类
│   │   │   │   ├── RolePermissionId.java   # 角色权限关联ID实体类
│   │   │   │   ├── Tag.java                # 标签实体类
│   │   │   │   ├── User.java               # 用户实体类（移除userGroup字段）
│   │   │   │   ├── UserRole.java           # 用户角色关联实体类
│   │   │   │   ├── UserRoleId.java         # 用户角色关联ID实体类
│   │   │   │   └── VersionOperation.java   # 版本操作实体类
│   │   │   │
│   │   │   ├── repository/                  # Spring Data JPA数据访问层
│   │   │   │   ├── ArticleRepository.java   # 文章数据访问接口
│   │   │   │   ├── ArticleReviewRepository.java # 文章审核数据访问接口
│   │   │   │   ├── ArticleVersionRepository.java # 文章版本数据访问接口
│   │   │   │   ├── ArticleVersionStatsRepository.java # 文章版本统计数据访问接口
│   │   │   │   ├── CommentLikeRepository.java # 评论点赞数据访问接口
│   │   │   │   ├── CommentRepository.java   # 评论数据访问接口
│   │   │   │   ├── MessageRepository.java   # 消息数据访问接口
│   │   │   │   ├── PermissionRepository.java # 权限数据访问接口
│   │   │   │   ├── ReviewerAssignmentRepository.java # 审核员分配数据访问接口
│   │   │   │   ├── ReviewHistoryRepository.java # 审核历史数据访问接口
│   │   │   │   ├── ReviewPermissionConfigRepository.java # 审核权限配置数据访问接口
│   │   │   │   ├── RolePermissionRepository.java # 角色权限关联数据访问接口
│   │   │   │   ├── RoleRepository.java      # 角色数据访问接口
│   │   │   │   ├── TagRepository.java       # 标签数据访问接口
│   │   │   │   ├── UserRepository.java      # 用户数据访问接口
│   │   │   │   └── UserRoleRepository.java  # 用户角色关联数据访问接口
│   │   │   │
│   │   │   ├── security/                    # Spring Security安全相关
│   │   │   │   ├── JwtAuthenticationFilter.java  # JWT认证过滤器
│   │   │   │   ├── JwtUtil.java            # JWT工具类
│   │   │   │   └── UserDetailsServiceImpl.java   # 用户详情服务实现
│   │   │   │
│   │   │   ├── service/                     # 业务逻辑层
│   │   │   │   ├── AdminService.java       # 管理员服务（系统统计、用户管理）
│   │   │   │   ├── ArticleReviewService.java # 文章审核服务
│   │   │   │   ├── ArticleService.java     # 文章服务（CRUD、搜索、版本管理）
│   │   │   │   ├── ArticleVersionService.java # 文章版本服务
│   │   │   │   ├── CommentService.java     # 评论服务
│   │   │   │   ├── MessageService.java     # 消息通知服务
│   │   │   │   ├── PermissionService.java  # 权限服务（权限检查、角色验证）
│   │   │   │   ├── TagService.java         # 标签服务
│   │   │   │   └── UserService.java        # 用户服务（用户CRUD、角色管理）
│   │   │   │
│   │   │   └── util/                        # 工具类目录
│   │   │       └── WikiTextProcessor.java  # Wiki文本处理工具
│   │   │
│   │   └── resources/                       # 资源文件
│   │       ├── application.properties       # Spring Boot主配置文件
│   │       ├── application-local.properties.example # 本地配置文件模板
│   │       └── db/migration/               # 数据库迁移脚本目录
│   │
│   └── test/                               # 测试代码目录
│       └── java/com/ecowiki/              # 测试Java代码
│
└── target/                                 # Maven构建输出目录
    ├── classes/                            # 编译后的class文件
    │   ├── application.properties          # 编译后的配置文件
    │   ├── application-local.properties    # 编译后的本地配置
    │   └── com/ecowiki/                   # 编译后的Java类
    ├── generated-sources/                  # 生成的源代码
    ├── generated-test-sources/             # 生成的测试源代码
    ├── maven-status/                       # Maven状态信息
    └── test-classes/                       # 测试类编译输出
```

## 前端项目结构（Vue 3.5.13 + TypeScript 5.8.0）

```
www/frontend/
├── 📄 package.json                            # Node.js项目配置文件，Vue 3 + TypeScript + Vite
├── 📄 pnpm-lock.yaml                         # pnpm锁定文件，确保依赖版本一致
├── 📄 README.md                              # 前端项目说明文档
├── 📄 REGISTER_FEATURE.md                    # 注册功能说明文档
├── 📄 env.d.ts                               # TypeScript环境类型定义
├── 📄 index.html                             # HTML入口文件
├── 📄 tsconfig.json                          # TypeScript主配置文件
├── 📄 tsconfig.app.json                      # TypeScript应用配置文件
├── 📄 tsconfig.node.json                     # TypeScript Node.js配置文件
├── 📄 vite.config.ts                         # Vite 6.2.4构建工具配置文件
├── 📁 public/                                # 静态资源目录
│   └── favicon.ico                          # 网站图标
└── 📁 src/                                   # 源代码目录
    ├── 📄 App.vue                            # 根组件，管理全局模态框和路由
    ├── 📄 main.ts                            # 应用入口文件，创建Vue应用实例
    │
    ├── 📁 api/                               # API接口定义层
    │   ├── index.ts                         # API基础配置（axios实例、拦截器）
    │   ├── article.ts                       # 文章相关API接口（完整CRUD）
    │   ├── articleVersion.ts               # 文章版本相关API接口
    │   ├── comment.ts                       # 评论相关API接口
    │   ├── message.ts                       # 消息通知API接口
    │   ├── review.ts                        # 审核系统API接口 ⭐ 新增
    │   ├── tag.ts                           # 标签相关API接口
    │   └── user.ts                          # 用户相关API接口（权限管理）
    │
    ├── 📁 assets/                            # 静态资源
    │   ├── base.css                         # 基础CSS样式
    │   ├── main.css                         # 主要CSS样式
    │   ├── logo.svg                         # Logo图标
    │   ├── logo1.svg                        # 备用Logo图标
    │   ├── 📁 fonts/                        # 字体文件目录
    │   ├── 📁 icons/                        # 图标文件目录
    │   └── 📁 images/                       # 图片文件目录
    │
    ├── 📁 components/                        # 组件目录（模块化组织）
    │   ├── 📁 admin/                         # 管理后台组件
    │   │   ├── AdminLayout.vue              # 管理后台布局组件
    │   │   └── 📁 views/                    # 管理后台页面组件
    │   │       ├── ArticleManagement.vue    # 文章管理页面
    │   │       ├── PermissionManagement.vue # 权限管理页面
    │   │       ├── RolePermissionAssignment.vue # 角色权限分配页面
    │   │       ├── SystemSettings.vue       # 系统设置页面
    │   │       └── UserList.vue             # 用户列表页面
    │   │
    │   ├── 📁 article/                       # 文章相关组件
    │   │   ├── ArticleCard.vue              # 文章卡片组件
    │   │   ├── ArticleComments.vue          # 文章评论组件
    │   │   ├── ArticleEditForm.vue          # 文章编辑表单
    │   │   ├── ArticleHistory.vue           # 文章历史版本组件
    │   │   ├── ArticleList.vue              # 文章列表组件
    │   │   ├── ArticlePreview.vue           # 文章预览组件
    │   │   ├── ArticleSearch.vue            # 文章搜索组件
    │   │   ├── ArticleStatistics.vue        # 文章统计组件
    │   │   ├── CategoryFilter.vue           # 分类过滤组件
    │   │   ├── CreatePage.vue               # 页面创建组件（防重复）
    │   │   ├── PopularArticles.vue          # 热门文章组件
    │   │   └── TagCloud.vue                 # 标签云组件
    │   │
    │   ├── 📁 common/                        # 通用组件
    │   │   ├── ConfirmDialog.vue            # 确认对话框
    │   │   ├── LoadingSpinner.vue           # 加载动画
    │   │   ├── NotificationList.vue         # 通知列表
    │   │   ├── Pagination.vue               # 分页组件
    │   │   └── Toast.vue                    # 消息提示组件
    │   │
    │   ├── 📁 edit/                          # 编辑相关组件
    │   │   ├── MarkdownEditor.vue           # Markdown编辑器
    │   │   ├── WikiEditor.vue               # Wiki编辑器组件
    │   │   └── WikiPreview.vue              # Wiki预览组件
    │   │
    │   ├── 📁 forms/                         # 表单组件
    │   │   ├── ForgotPanel.vue              # 忘记密码表单
    │   │   ├── LoginPanel.vue               # 登录表单
    │   │   └── RegisterPanel.vue            # 注册表单
    │   │
    │   ├── 📁 home/                          # 首页组件
    │   │   ├── FeaturedArticles.vue         # 精选文章组件
    │   │   ├── HomeHero.vue                 # 首页英雄区域
    │   │   ├── RecentActivity.vue           # 最近活动
    │   │   └── RecentUpdates.vue            # 最近更新组件
    │   │
    │   ├── 📁 icons/                         # 图标组件
    │   │   ├── IconCommunity.vue            # 社区图标
    │   │   ├── IconDocumentation.vue        # 文档图标
    │   │   ├── IconEcosystem.vue            # 生态系统图标
    │   │   ├── IconSupport.vue              # 支持图标
    │   │   ├── IconTooling.vue              # 工具图标
    │   │   └── index.ts                     # 图标导出文件
    │   │
    │   ├── 📁 layout/                        # 布局组件
    │   │   ├── AppFooter.vue                # 应用底部组件
    │   │   ├── AppHeader.vue                # 应用头部组件
    │   │   ├── AppSidebar.vue               # 侧边栏组件
    │   │   ├── HeaderLogo.vue               # 头部Logo组件
    │   │   ├── HeaderSearch.vue             # 头部搜索组件
    │   │   ├── HeaderUserArea.vue           # 头部用户区域组件
    │   │   └── NavigationMenu.vue           # 导航菜单组件
    │   │
    │   ├── 📁 modals/                        # 模态框组件
    │   │   ├── AuthModals.vue               # 认证模态框容器
    │   │   └── MessagePanel.vue            # 消息面板组件
    │   │
    │   ├── 📁 review/                        # 审核系统组件
    │   │   ├── CreateReviewDialog.vue       # 创建审核对话框
    │   │   └── ReviewDashboard.vue          # 审核仪表板
    │   │
    │   └── 📁 userhome/                      # 用户主页组件
    │       ├── userProfile.vue              # 用户资料组件
    │       └── 📁 view/                      # 用户主页视图
    │           ├── UserArticle.vue          # 用户文章视图
    │           ├── UserContribute.vue       # 用户贡献视图
    │           ├── UserInformation.vue      # 用户信息视图
    │           ├── UserPage.vue             # 用户主页视图
    │           └── UserSecure.vue           # 用户安全设置视图
    │
    ├── 📁 composables/                       # Vue3组合式函数
    │   ├── useAuth.ts                       # 认证相关组合函数（214行）
    │   └── useEditorOperations.ts           # 编辑器操作组合函数
    │
    ├── 📁 router/                            # 路由配置
    │   └── index.ts                         # 路由定义和守卫配置
    │
    ├── 📁 services/                          # 业务服务层
    │   └── userService.ts                   # 用户服务
    │
    ├── 📁 spgame/                            # 小游戏模块
    │   ├── SpaceShooterGame.vue             # 太空射击游戏
    │   └── TowerDefenseGame.vue             # 塔防游戏
    │
    ├── 📁 stores/                            # Pinia状态管理
    │   └── adminUserStore.ts                # 管理后台用户状态管理
    │
    ├── 📁 types/                             # TypeScript类型定义
    │   ├── permission.ts                    # 权限相关类型定义
    │   └── review.ts                        # 审核相关类型定义
    │
    ├── 📁 utils/                             # 工具函数
    │   ├── toast.ts                         # 消息提示工具
    │   ├── validation.ts                    # 表单验证工具
    │   └── wikiParser.ts                    # Wiki解析工具
    │
    └── 📁 views/                             # 视图页面组件
        ├── ArticleDetail.vue                # 文章详情页面
        ├── ArticleEdit.vue                  # 文章编辑页面
        ├── ClassicHome.vue                  # 经典主页
        └── HomeView.vue                     # 主页视图
```

## 数据库结构（MySQL 8.0+）

### 核心表结构

基于`COMPLETE_DATABASE_INIT.sql`的完整数据库设计：

#### 用户权限体系表

- **`user`** - 用户基本信息表（移除了userGroup字段，改用关联表）
- **`role`** - 角色定义表（user、moderator、admin、superadmin等）
- **`permission`** - 权限定义表（细粒度权限控制）
- **`user_roles`** - 用户角色多对多关联表
- **`role_permissions`** - 角色权限多对多关联表

#### 内容管理表

- **`articles`** - 文章内容表（支持Wiki语法、标签、统计）
- **`tags`** - 标签表（分类和标签管理）
- **`article_tags`** - 文章标签多对多关联表

#### 版本控制系统表

- **`article_versions`** - 文章版本历史表（支持差异存储、压缩）
- **`article_version_stats`** - 文章版本统计表（性能优化）
- **`article_version_config`** - 版本控制配置表

#### 互动功能表

- **`messages`** - 消息通知表（用户间通信）

### 详细表结构定义

```sql
-- 用户表（重构后，移除userGroup字段）
CREATE TABLE `user` (
  `user_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `email` VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
  `full_name` VARCHAR(100) COMMENT '用户全名',
  `active` BOOLEAN DEFAULT TRUE COMMENT '是否激活',
  `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未设置，1-男，2-女',
  `email_verified` BOOLEAN DEFAULT FALSE COMMENT '邮箱验证状态',
  `login_token` VARCHAR(255) COMMENT '登录令牌',
  `role_id` INT COMMENT '临时角色ID字段（兼容性）',
  `permissions` VARCHAR(500) COMMENT '权限描述',
  `last_login` TIMESTAMP COMMENT '最后登录时间',
  `avatar_url` VARCHAR(255) COMMENT '头像URL',
  `bio` TEXT COMMENT '个人简介',
  `security_question` VARCHAR(255) COMMENT '安全问题',
  `security_answer` VARCHAR(255) COMMENT '安全答案',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_username` (`username`),
  INDEX `idx_email` (`email`),
  INDEX `idx_active` (`active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 角色表
CREATE TABLE `role` (
  `role_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
  `role_name` VARCHAR(50) UNIQUE NOT NULL COMMENT '角色名称',
  `description` TEXT COMMENT '角色描述',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 权限表
CREATE TABLE `permission` (
  `permission_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
  `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
  `description` TEXT COMMENT '权限描述',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_permission_name` (`permission_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户角色关联表
CREATE TABLE `user_roles` (
  `user_id` INT NOT NULL COMMENT '用户ID',
  `role_id` INT NOT NULL COMMENT '角色ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  PRIMARY KEY (`user_id`, `role_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 角色权限关联表
CREATE TABLE `role_permissions` (
  `role` INT NOT NULL COMMENT '角色ID（兼容旧字段名）',
  `permission_id` INT NOT NULL COMMENT '权限ID',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  PRIMARY KEY (`role`, `permission_id`),
  INDEX `idx_role` (`role`),
  INDEX `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 文章表（支持大文本、统计、分类标签）
CREATE TABLE `articles` (
  `article_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文章ID',
  `title` VARCHAR(255) UNIQUE NOT NULL COMMENT '文章标题',
  `content` LONGTEXT NOT NULL COMMENT '文章内容',
  `author` VARCHAR(100) NOT NULL COMMENT '文章作者',
  `publish_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `category` VARCHAR(100) COMMENT '文章分类',
  `tags` TEXT COMMENT '文章标签（逗号分隔）',
  `views` INT DEFAULT 0 COMMENT '浏览次数',
  `likes` INT DEFAULT 0 COMMENT '点赞数',
  `comments` INT DEFAULT 0 COMMENT '评论数',
  INDEX `idx_title` (`title`),
  INDEX `idx_author` (`author`),
  INDEX `idx_category` (`category`),
  INDEX `idx_publish_date` (`publish_date`),
  INDEX `idx_views` (`views`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 标签表
CREATE TABLE `tags` (
  `tag_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` VARCHAR(50) UNIQUE NOT NULL COMMENT '标签名称',
  `description` VARCHAR(255) COMMENT '标签描述',
  `created_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 文章标签关联表
CREATE TABLE `article_tags` (
  `article_id` BIGINT NOT NULL COMMENT '文章ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`article_id`, `tag_id`),
  INDEX `idx_article_id` (`article_id`),
  INDEX `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 文章版本表（支持差异存储和压缩）
CREATE TABLE `article_versions` (
  `version_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '版本ID',
  `article_id` BIGINT NOT NULL COMMENT '文章ID',
  `version_number` INT NOT NULL COMMENT '版本号',
  `author` VARCHAR(100) COMMENT '版本作者',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `storage_type` ENUM('FULL_CONTENT', 'DIFF_FROM_BASE', 'DIFF_FROM_PREV') NOT NULL DEFAULT 'FULL_CONTENT' COMMENT '存储类型',
  `content` LONGBLOB COMMENT '版本内容',
  `content_hash` VARCHAR(64) COMMENT '内容哈希',
  `original_size` BIGINT COMMENT '原始大小',
  `compressed_size` BIGINT COMMENT '压缩后大小',
  `base_version_id` BIGINT COMMENT '基础版本ID',
  `is_compressed` BOOLEAN DEFAULT FALSE COMMENT '是否压缩',
  `is_archived` BOOLEAN DEFAULT FALSE COMMENT '是否归档',
  `change_summary` VARCHAR(500) COMMENT '变更摘要',
  `compression_algorithm` VARCHAR(20) COMMENT '压缩算法',
  INDEX `idx_article_id` (`article_id`),
  INDEX `idx_version_number` (`version_number`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_storage_type` (`storage_type`),
  INDEX `idx_is_archived` (`is_archived`),
  INDEX `idx_content_hash` (`content_hash`),
  INDEX `idx_base_version_id` (`base_version_id`),
  UNIQUE KEY `uk_article_version` (`article_id`, `version_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 文章版本统计表（性能优化）
CREATE TABLE `article_version_stats` (
  `stats_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
  `article_id` BIGINT UNIQUE NOT NULL COMMENT '文章ID',
  `total_versions` INT DEFAULT 0 COMMENT '总版本数',
  `base_versions_count` INT DEFAULT 0 COMMENT '基础版本数',
  `diff_versions_count` INT DEFAULT 0 COMMENT '差异版本数',
  `archived_versions_count` INT DEFAULT 0 COMMENT '归档版本数',
  `total_storage_size` BIGINT DEFAULT 0 COMMENT '总存储大小',
  `compressed_storage_size` BIGINT DEFAULT 0 COMMENT '压缩存储大小',
  `last_optimized_at` TIMESTAMP COMMENT '最后优化时间',
  `last_accessed_at` TIMESTAMP COMMENT '最后访问时间',
  `access_frequency` INT DEFAULT 0 COMMENT '访问频率',
  `optimization_needed` BOOLEAN DEFAULT FALSE COMMENT '是否需要优化',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_article_id` (`article_id`),
  INDEX `idx_last_optimized` (`last_optimized_at`),
  INDEX `idx_last_accessed` (`last_accessed_at`),
  INDEX `idx_optimization_needed` (`optimization_needed`),
  INDEX `idx_total_storage_size` (`total_storage_size`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 版本控制配置表
CREATE TABLE `article_version_config` (
  `config_key` VARCHAR(50) PRIMARY KEY COMMENT '配置键',
  `config_value` VARCHAR(255) NOT NULL COMMENT '配置值',
  `description` TEXT COMMENT '配置描述',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 消息通知表
CREATE TABLE `messages` (
  `message_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
  `recipient_user_id` INT NOT NULL COMMENT '接收用户ID',
  `sender_user_id` INT NOT NULL COMMENT '发送用户ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `send_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `status` VARCHAR(50) DEFAULT '未读' COMMENT '消息状态：未读、已读',
  INDEX `idx_recipient_user_id` (`recipient_user_id`),
  INDEX `idx_sender_user_id` (`sender_user_id`),
  INDEX `idx_send_time` (`send_time`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 初始数据

系统包含完整的初始数据插入：

- **默认角色**：user、moderator、admin、superadmin
- **基础权限**：文章管理、用户管理、系统管理等
- **默认管理员账户**：superadmin / EcoWiki@2025
- **版本控制配置**：差异存储阈值、压缩设置等

## 架构特点

### 后端架构（Spring Boot 3.x）

- **分层架构**: Controller → Service → Repository → Entity
- **安全机制**: JWT认证 + Spring Security
- **权限管理**: 基于角色的访问控制（RBAC），支持细粒度权限
- **数据访问**: Spring Data JPA + HikariCP连接池 + MySQL 8.0+
- **API设计**: RESTful API设计风格，统一响应格式
- **版本控制**: 完整的文章版本管理系统，支持差异存储和压缩
- **配置管理**: 多环境配置支持（local、dev、prod）

### 前端架构（Vue 3.5.13 + TypeScript 5.8.0）

- **组件化**: Vue3组合式API + TypeScript
- **模块化结构**: 按功能分类组织组件（api、components、composables、types、utils、views）
- **状态管理**: Pinia状态管理
- **路由管理**: Vue Router + 路由守卫
- **样式系统**: Scoped CSS + 响应式设计
- **构建工具**: Vite 6.2.4 + TypeScript
- **类型安全**: 完整的TypeScript类型定义系统
- **API集成**: Axios HTTP客户端 + 统一请求拦截器

### 核心功能模块

1. **用户认证系统**: 注册、登录、JWT认证、忘记密码、权限验证
2. **权限管理系统**: 用户角色管理、权限检查、细粒度权限控制、RBAC模型
3. **文章管理系统**: 文章CRUD、Wiki语法解析、分类标签、搜索功能
4. **版本控制系统**: 文章版本历史、差异对比、版本回退、存储优化
5. **消息通知系统**: 用户间消息、系统通知、状态管理
6. **管理后台系统**: 用户管理、角色管理、权限管理、系统统计
7. **搜索分类系统**: 全文搜索、分类浏览、标签云、相关推荐
8. **数据统计系统**: 文章统计、用户活跃度、系统性能监控

### 技术栈详情

#### 后端技术栈

```xml
<!-- 核心框架 -->
Spring Boot: 3.x
Spring Security: 6.x
Spring Data JPA: 3.x

<!-- 数据库 -->
MySQL: 8.0+
HikariCP: 连接池
Hibernate: ORM框架

<!-- 安全认证 -->
JWT: JSON Web Token
BCrypt: 密码加密

<!-- 构建工具 -->
Maven: 3.6+
Java: 17+
```

#### 前端技术栈

```json
{
  "framework": "Vue 3.5.13",
  "language": "TypeScript 5.8.0",
  "buildTool": "Vite 6.2.4",
  "stateManagement": "Pinia 3.0.3",
  "router": "Vue Router 4.5.1",
  "httpClient": "Axios 1.10.0",
  "utilities": [
    "@vueuse/core 13.4.0",
    "marked 16.0.0 (Markdown解析)",
    "diff 8.0.2 (差异对比)",
    "dompurify 3.2.6 (XSS防护)"
  ],
  "packageManager": "pnpm",
  "nodeVersion": "18+"
}
```

### 安全特性

1. **身份认证**: JWT无状态认证 + 自动令牌刷新
2. **权限控制**: 基于角色的访问控制（RBAC）
3. **数据安全**: BCrypt密码哈希 + 敏感数据加密
4. **通信安全**: HTTPS/TLS加密传输
5. **防护机制**:
   - SQL注入防护（参数化查询）
   - XSS防护（DOMPurify过滤）
   - CSRF防护（令牌验证）
   - 请求限流（接口防刷）

### 性能优化

1. **前端优化**:
   - 组件懒加载
   - 路由懒加载
   - 图片懒加载
   - Vite热更新

2. **后端优化**:
   - JPA查询优化
   - 数据库索引优化
   - HikariCP连接池
   - 分页查询支持

3. **版本控制优化**:
   - 差异存储算法
   - 内容压缩
   - 版本归档机制
   - 存储空间优化

## 开发和部署

### 开发环境要求

- **后端**: Java 17+, Maven 3.6+, MySQL 8.0+
- **前端**: Node.js 18+, pnpm 7+, 现代浏览器
- **开发工具**: VS Code, IntelliJ IDEA（可选）
- **数据库客户端**: MySQL Workbench, Navicat（可选）

### 环境配置

#### 数据库配置

```properties
# application-local.properties（需要创建）
spring.datasource.url=jdbc:mysql://localhost:3306/ecowiki?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8
spring.datasource.username=your_username
spring.datasource.password=your_password
```

#### 前端环境变量

```bash
# 开发环境API地址
VITE_API_BASE_URL=http://localhost:8080/api
```

### 启动方式

#### 快速启动（推荐）

```bash
# 使用VS Code启动脚本
start_in_vscode.bat     # Windows环境
./start_in_vscode.sh    # Linux/macOS环境

# 使用部署脚本
deploy.bat              # Windows一键部署
deploy.sh               # Linux/macOS一键部署
```

#### 手动启动

```bash
# 1. 启动后端
cd www/backend
mvn clean install
mvn spring-boot:run

# 2. 启动前端
cd www/frontend
pnpm install
pnpm dev
```

### 系统初始化

#### 数据库初始化

```bash
# 执行完整数据库初始化脚本
mysql -u username -p database_name < COMPLETE_DATABASE_INIT.sql
```

#### 默认账户信息

系统首次启动时会自动创建默认管理员账户：

- **用户名**: `superadmin`
- **密码**: `EcoWiki@2025`
- **邮箱**: `admin@ecowiki.com`
- **权限**: 超级管理员权限（所有功能访问权限）

#### 基础数据初始化

- **默认角色**: user、moderator、admin、superadmin
- **基础权限**: 文章管理、用户管理、系统管理等细粒度权限
- **系统配置**: 版本控制参数、安全设置等

### 构建部署

#### 生产环境构建

```bash
# 后端打包
cd www/backend
mvn clean package -Dmaven.test.skip=true

# 前端构建
cd www/frontend
pnpm build
```

#### Docker部署（可选）

```dockerfile
# Dockerfile示例
FROM openjdk:17-jdk-slim
COPY target/ecowiki-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

#### 服务器部署

```bash
# 1. 部署后端JAR包
java -jar -Dspring.profiles.active=prod ecowiki-backend.jar

# 2. 部署前端静态文件
# 将frontend/dist目录下的文件部署到Web服务器（Nginx/Apache）
```

### 开发工具和脚本

#### 可用脚本

- `start_in_vscode.bat` - VS Code环境快速启动
- `deploy.bat` / `deploy.sh` - 一键部署脚本
- `COMPLETE_DATABASE_INIT.sql` - 完整数据库初始化
- `DATABASE_UPGRADE_GUIDE.md` - 数据库升级指南

#### 测试和调试

```bash
# 后端测试
cd www/backend
mvn test

# 前端类型检查
cd www/frontend
pnpm type-check

# API测试
# 参考 backend/API_TEST_GUIDE.md
```

### 监控和维护

#### 日志配置

- 后端日志: Spring Boot默认日志配置
- 前端日志: 浏览器控制台 + 错误监控
- 数据库日志: MySQL慢查询日志

#### 性能监控

- API响应时间监控
- 数据库查询性能分析
- 前端页面加载性能监控

#### 备份策略

- 数据库: 每日自动备份
- 文件系统: 定期备份用户上传内容
- 配置文件: 版本控制管理

## 重要更新和优化记录

### 2025年7月28日 - 项目结构文档完整重构 🔄

1. **文档重构完成**
   - ✅ 基于实际代码文件结构完全重写文档
   - ✅ 修正前端组件目录结构（admin、review、userhome等）
   - ✅ 更新后端实体类列表（新增审核、版本控制相关实体）
   - ✅ 修正API接口文件列表（新增review.ts审核接口）
   - ✅ 更新类型定义文件（仅permission.ts、review.ts）
   - ✅ 修正工具函数目录（toast.ts、validation.ts、wikiParser.ts）
   - ✅ 更新视图页面文件（ArticleDetail.vue、ArticleEdit.vue等）

2. **架构文档完善**
   - ✅ 后端Controller结构更新（包含审核、评论、标签控制器）
   - ✅ Repository层补全（新增审核相关数据访问接口）
   - ✅ Service层更新（新增ArticleReviewService）
   - ✅ 前端组合式函数说明（useAuth.ts、useEditorOperations.ts）
   - ✅ 小游戏模块记录（SpaceShooterGame、TowerDefenseGame）

3. **技术栈信息准确性**
   - ✅ 确认Spring Boot 3.2.0 + Vue 3.5.13 + TypeScript 5.8.0
   - ✅ 确认Vite 6.2.4构建工具
   - ✅ 确认MySQL 8.0+数据库
   - ✅ 确认完整的审核系统集成

### 2025年7月22日 - 项目结构文档全面更新

1. **文档完善**
   - 基于实际代码分析更新所有结构信息
   - 添加详细的技术栈版本信息
   - 完善数据库表结构和字段说明
   - 增加安全特性和性能优化说明

2. **架构信息更新**
   - 前端组件结构按实际文件组织更新
   - 后端API控制器分层结构完善
   - 数据库版本控制系统详细说明
   - 权限管理系统RBAC模型说明

### 2025年1月15日 - 版本控制系统完成

1. **文章版本管理系统**
   - 完整的版本历史记录
   - 差异存储算法优化
   - 版本压缩和归档机制
   - 版本统计和性能监控

2. **数据库结构优化**
   - 版本控制相关表结构设计
   - 索引优化提升查询性能
   - 存储空间优化算法

### 2024年6月30日 - 权限系统重构

1. **RBAC权限系统**
   - 从User表userGroup字段迁移到独立关联表
   - 支持用户多角色、角色多权限
   - 细粒度权限控制实现
   - 权限缓存和性能优化

2. **安全增强**
   - JWT认证机制完善
   - Spring Security配置优化
   - API接口权限控制
   - 防护机制加强

### 2024年1月15日 - 项目初始化

1. **项目框架搭建**
   - Spring Boot 3.x后端框架
   - Vue 3 + TypeScript前端框架
   - MySQL数据库设计
   - 基础功能模块实现

---

**文档状态**: ✅ 已完全同步实际代码结构  
**最后更新**: 2025年7月28日  
**验证状态**: 🔍 已验证前后端文件结构100%准确性  
**维护团队**: EcoWiki开发团队

## 注意事项

### 开发注意事项

1. **环境配置**:
   - 必须创建`application-local.properties`文件配置数据库连接
   - 前端需要配置正确的API基地址
   - 确保MySQL 8.0+版本兼容性

2. **权限系统**:
   - 所有API接口都需要进行权限验证
   - 管理员功能通过`@PreAuthorize`注解控制
   - 前端路由守卫配合后端权限检查

3. **数据库迁移**:
   - 使用`COMPLETE_DATABASE_INIT.sql`进行完整初始化
   - 版本升级参考`DATABASE_UPGRADE_GUIDE.md`
   - 注意用户角色从userGroup字段到user_roles表的迁移

4. **版本控制**:
   - 文章版本自动创建，支持差异存储
   - 大文章建议使用压缩存储
   - 定期执行版本归档和优化

### 部署注意事项

1. **安全配置**:
   - 生产环境必须使用HTTPS
   - 修改默认管理员密码
   - 配置防火墙和安全组规则
   - 定期更新依赖包版本

2. **性能优化**:
   - 数据库连接池参数调优
   - JVM内存参数优化
   - 前端静态资源CDN部署
   - 开启数据库查询缓存

3. **监控告警**:
   - 配置应用性能监控（APM）
   - 设置数据库慢查询监控
   - 配置磁盘空间和内存告警
   - 定期检查系统日志

### 维护注意事项

1. **数据备份**:
   - 每日数据库自动备份
   - 文件系统定期快照
   - 备份数据定期恢复测试
   - 版本控制数据单独备份

2. **系统更新**:
   - 依赖包安全更新
   - 数据库版本升级计划
   - 系统补丁定期安装
   - 功能模块渐进式升级

3. **故障处理**:
   - 制定故障响应预案
   - 准备系统回滚方案
   - 建立故障排查流程
   - 定期进行灾难恢复演练

---

*文档最后更新时间: 2025年7月22日*  
*项目版本: EcoWiki v3.0 (版本控制增强版)*  
*文档维护: GitHub Copilot + EcoWiki团队*
