# EcoWiki 项目文件结构文档

## 项目概述
EcoWiki 是一个基于Spring Boot后端和Vue3前端的知识共享平台，支持用户注册、登录、文章管理、权限管理等功能。

## 根目录结构

```
EcoWiki-3/
├── LICENSE                                    # 项目许可证文件
├── README.md                                  # 项目说明文档
├── PROJECT_STRUCTURE.md                       # 项目文件结构文档（本文件）
├── describe/                                  # 项目文档目录
│   └── SRS.docx                              # 软件需求规格说明书
└── www/                                       # 主要代码目录
    ├── backend/                               # 后端Spring Boot项目
    ├── frontend/                              # 前端Vue3项目
    └── EcoWiki/                              # 其他资源文件
```

## 后端项目结构（Spring Boot）

```
www/backend/
├── pom.xml                                    # Maven项目配置文件，定义依赖和构建配置
├── API_TEST_GUIDE.md                          # API测试指南文档
├── ARTICLE_API_README.md                      # 文章API说明文档
├── config/                                    # 配置文件目录
├── routes/                                    # 路由配置目录
├── src/                                       # 源代码目录
│   ├── main/                                 # 主要源代码
│   │   ├── java/com/ecowiki/                # Java源代码包
│   │   │   ├── EcoWikiApplication.java       # Spring Boot主启动类
│   │   │   ├── config/                       # 配置类目录
│   │   │   │   ├── CorsConfig.java          # 跨域配置类
│   │   │   │   ├── SecurityConfig.java      # Spring Security安全配置
│   │   │   │   └── WebConfig.java           # Web MVC配置类
│   │   │   ├── controller/                   # 控制器层
│   │   │   │   └── api/                     # API控制器
│   │   │   │       ├── admin/               # 管理员相关API
│   │   │   │       │   └── AdminController.java  # 管理员功能控制器（用户管理、角色管理、系统统计）
│   │   │   │       ├── auth/                # 认证相关API
│   │   │   │       │   └── AuthController.java   # 认证控制器（登录、注册、token验证）
│   │   │   │       └── user/                # 用户相关API
│   │   │   │           └── UserController.java   # 用户控制器（用户信息管理）
│   │   │   ├── dto/                         # 数据传输对象
│   │   │   │   ├── ApiResponse.java         # 统一API响应格式
│   │   │   │   ├── LoginRequest.java        # 登录请求DTO
│   │   │   │   ├── UserRegistrationDto.java # 用户注册DTO
│   │   │   │   └── UserWithRoleDto.java     # 带角色信息的用户DTO
│   │   │   ├── entity/                      # 实体类
│   │   │   │   ├── Role.java               # 角色实体类
│   │   │   │   ├── User.java               # 用户实体类
│   │   │   │   ├── UserRole.java           # 用户角色关联实体类
│   │   │   │   └── UserRoleId.java         # 用户角色复合主键类
│   │   │   ├── exception/                   # 异常处理
│   │   │   │   └── GlobalExceptionHandler.java  # 全局异常处理器
│   │   │   ├── repository/                  # 数据访问层
│   │   │   │   ├── RoleRepository.java      # 角色数据访问接口
│   │   │   │   ├── UserRepository.java      # 用户数据访问接口
│   │   │   │   └── UserRoleRepository.java  # 用户角色关联数据访问接口
│   │   │   ├── security/                    # 安全相关
│   │   │   │   ├── JwtAuthenticationFilter.java  # JWT认证过滤器
│   │   │   │   ├── JwtUtil.java            # JWT工具类
│   │   │   │   └── UserDetailsServiceImpl.java   # 用户详情服务实现
│   │   │   └── service/                     # 业务逻辑层
│   │   │       ├── AdminService.java       # 管理员服务（系统统计、用户管理）
│   │   │       ├── PermissionService.java  # 权限服务（权限检查、角色验证）
│   │   │       └── UserService.java        # 用户服务（用户CRUD、角色管理）
│   │   └── resources/                       # 资源文件
│   │       ├── application.properties       # Spring Boot配置文件
│   │       └── META-INF/                   # 元信息目录
│   └── test/                               # 测试代码目录
│       └── java/                           # 测试Java代码
└── target/                                 # Maven构建输出目录
    ├── ecowiki-backend-0.0.1-SNAPSHOT.jar  # 打包后的JAR文件
    ├── ecowiki-backend-0.0.1-SNAPSHOT.jar.original  # 原始JAR文件
    ├── classes/                            # 编译后的class文件
    │   ├── application.properties          # 编译后的配置文件
    │   └── com/ecowiki/                   # 编译后的Java类
    ├── generated-sources/                  # 生成的源代码
    │   └── annotations/                    # 注解生成的代码
    ├── generated-test-sources/             # 生成的测试源代码
    │   └── test-annotations/               # 测试注解生成的代码
    ├── maven-archiver/                     # Maven归档信息
    │   └── pom.properties                 # Maven属性文件
    ├── maven-status/                       # Maven状态信息
    │   └── maven-compiler-plugin/          # 编译插件状态
    │       ├── compile/                    # 编译状态
    │       └── testCompile/               # 测试编译状态
    └── test-classes/                       # 测试类编译输出
```

## 前端项目结构（Vue3 + TypeScript）

```
www/frontend/
├── package.json                            # Node.js项目配置文件，定义依赖和脚本
├── pnpm-lock.yaml                         # pnpm锁定文件，确保依赖版本一致
├── README.md                              # 前端项目说明文档
├── REGISTER_FEATURE.md                    # 注册功能说明文档
├── env.d.ts                               # TypeScript环境类型定义
├── index.html                             # HTML入口文件
├── tsconfig.json                          # TypeScript主配置文件
├── tsconfig.app.json                      # TypeScript应用配置文件
├── tsconfig.node.json                     # TypeScript Node.js配置文件
├── vite.config.ts                         # Vite构建工具配置文件
├── public/                                # 静态资源目录
│   └── favicon.ico                        # 网站图标
└── src/                                   # 源代码目录
    ├── App.vue                            # 根组件，管理全局模态框和路由
    ├── main.ts                            # 应用入口文件，创建Vue应用实例
    ├── api/                               # API接口定义
    │   ├── index.ts                       # API基础配置和请求拦截器
    │   ├── article.ts                     # 文章相关API接口
    │   └── user.ts                        # 用户相关API接口（登录、注册、管理员功能）
    ├── assets/                            # 静态资源
    │   ├── base.css                       # 基础CSS样式
    │   ├── main.css                       # 主要CSS样式
    │   ├── logo.svg                       # Logo图标
    │   ├── fonts/                         # 字体文件目录
    │   ├── icons/                         # 图标文件目录
    │   └── images/                        # 图片文件目录
    ├── components/                        # 组件目录
    │   ├── AppFooter.vue                  # 网站底部组件
    │   ├── AppHeader.vue                  # 网站头部组件，包含导航和用户区域
    │   ├── AppMainContent.vue             # 主内容区域组件
    │   ├── AppSidebar.vue                 # 侧边栏组件
    │   ├── AuthModals.vue                 # 认证模态框容器（登录、注册、管理后台）
    │   ├── BaseButton.vue                 # 基础按钮组件
    │   ├── FeaturedArticles.vue           # 精选文章组件
    │   ├── HeaderLogo.vue                 # 头部Logo组件
    │   ├── HeaderSearch.vue               # 头部搜索组件
    │   ├── HeaderUserArea.vue             # 头部用户区域组件（登录、注册、设置按钮）
    │   ├── LoginPanel.vue                 # 登录面板组件
    │   ├── RecentUpdates.vue              # 最近更新组件
    │   ├── RegisterPanel.vue              # 注册面板组件
    │   ├── Toast.vue                      # 消息提示组件
    │   ├── WikiEditor.vue                 # Wiki编辑器组件
    │   ├── admin/                         # 管理后台组件
    │   │   ├── AdminLayout.vue            # 管理后台布局组件，包含侧边栏和统一按钮
    │   │   └── views/                     # 管理后台页面组件
    │   │       ├── SystemSettings.vue    # 系统设置页面（首页风格、统计信息）
    │   │       ├── UserList.vue          # 用户列表管理页面
    │   │       └── RoleManagement.vue    # 角色权限管理页面
    │   ├── article/                       # 文章相关组件
    │   │   └── ArticleComments.vue        # 文章评论组件
    │   ├── common/                        # 通用组件目录
    │   ├── edit/                          # 编辑相关组件目录
    │   ├── forms/                         # 表单组件目录
    │   ├── icons/                         # 图标组件目录
    │   ├── layout/                        # 布局组件目录
    │   ├── modals/                        # 模态框组件目录
    │   └── subpages/                      # 子页面组件目录
    ├── composables/                       # Vue3组合式函数
    │   └── useAuth.ts                     # 认证相关组合函数（用户状态管理）
    ├── constants/                         # 常量定义目录
    ├── pages/                             # 页面组件目录
    ├── router/                            # 路由配置
    │   └── index.ts                       # 路由定义和守卫配置
    ├── services/                          # 服务层
    │   └── userService.ts                 # 用户服务
    ├── stores/                            # Pinia状态管理
    │   └── adminUserStore.ts              # 管理后台用户状态管理
    ├── styles/                            # 样式文件
    │   └── themes/                        # 主题样式目录
    ├── types/                             # TypeScript类型定义目录
    ├── utils/                             # 工具函数
    │   ├── toast.ts                       # 消息提示工具
    │   ├── validation.ts                  # 表单验证工具
    │   └── wikiParser.ts                  # Wiki解析工具
    └── views/                             # 视图页面组件
        ├── ArticleDetail.vue              # 文章详情页面
        ├── ArticleEdit.vue                # 文章编辑页面
        ├── ClassicHome.vue                # 经典首页布局
        ├── DynamicHome.vue                # 动态首页布局
        └── SimpleHome.vue                 # 简约首页布局
```

## 数据库结构

### 核心表结构
- **users** - 用户表（存储用户基本信息，不包含角色字段）
- **roles** - 角色表（定义系统角色：user、admin、superadmin等）
- **user_roles** - 用户角色关联表（多对多关系，支持用户拥有多个角色）

### 数据表字段说明
```sql
-- 用户表
users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,  -- 用户ID
    username VARCHAR(50) UNIQUE NOT NULL,       -- 用户名
    email VARCHAR(100) UNIQUE NOT NULL,         -- 邮箱
    password_hash VARCHAR(255) NOT NULL,        -- 密码哈希
    full_name VARCHAR(100),                     -- 全名
    active BOOLEAN DEFAULT TRUE,                -- 是否激活
    avatar VARCHAR(255),                        -- 头像URL
    created_at TIMESTAMP,                       -- 创建时间
    updated_at TIMESTAMP                        -- 更新时间
)

-- 角色表
roles (
    role_id INT PRIMARY KEY AUTO_INCREMENT,     -- 角色ID
    role_name VARCHAR(50) UNIQUE NOT NULL,      -- 角色名称
    description TEXT,                           -- 角色描述
    created_at TIMESTAMP,                       -- 创建时间
    updated_at TIMESTAMP                        -- 更新时间
)

-- 用户角色关联表
user_roles (
    user_id INT,                                -- 用户ID（外键）
    role_id INT,                                -- 角色ID（外键）
    created_at TIMESTAMP,                       -- 分配时间
    PRIMARY KEY (user_id, role_id)              -- 复合主键
)
```

## 架构特点

### 后端架构
- **分层架构**: Controller → Service → Repository → Entity
- **安全机制**: JWT认证 + Spring Security
- **权限管理**: 基于角色的访问控制（RBAC）
- **数据访问**: Spring Data JPA + MySQL
- **API设计**: RESTful API设计风格

### 前端架构
- **组件化**: Vue3组合式API + TypeScript
- **状态管理**: Pinia状态管理
- **路由管理**: Vue Router + 路由守卫
- **样式系统**: Scoped CSS + 响应式设计
- **构建工具**: Vite + TypeScript

### 核心功能模块
1. **用户认证系统**: 注册、登录、JWT认证
2. **权限管理系统**: 用户角色管理、权限检查
3. **管理后台系统**: 用户管理、角色管理、系统设置
4. **首页风格系统**: 经典/简约/动态三种首页风格
5. **文章管理系统**: 文章创建、编辑、查看（待完善）

## 开发和部署

### 开发环境要求
- **后端**: Java 17+, Maven 3.6+, MySQL 8.0+
- **前端**: Node.js 16+, pnpm 7+, 现代浏览器

### 启动方式
```bash
# 后端启动
cd www/backend
mvn spring-boot:run

# 前端启动
cd www/frontend
pnpm install
pnpm dev
```

### 构建部署
```bash
# 后端打包
cd www/backend
mvn clean package

# 前端构建
cd www/frontend
pnpm build
```

## 注意事项

1. **角色系统重构**: 已从User表的userGroup字段迁移到独立的user_roles表
2. **权限检查**: 所有管理员功能都通过PermissionService统一检查权限
3. **前后端分离**: 通过JWT进行无状态认证
4. **响应式设计**: 支持桌面端和移动端自适应
5. **模块化设计**: 前后端均采用模块化架构，便于维护和扩展

---

*文档最后更新时间: 2025年6月30日*
*项目版本: EcoWiki v1.0*
