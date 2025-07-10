# EcoWiki 项目文件结构文档

## 项目概述
EcoWiki 是一个基于Spring Boot后端和Vue3前端的知识共享平台，支持用户注册、登录、文章管理、权限管理等功能。经过重构优化，整个项目结构更加清晰和可维护。

## 根目录结构

```
EcoWiki-3/
├── 📄 LICENSE                                    # 项目许可证文件
├── 📄 README.md                                  # 项目说明文档
├── 📄 PROJECT_STRUCTURE.md                       # 项目文件结构文档（本文件）
├── 📄 CATEGORY_TAG_FEATURE.md                    # 分类标签功能文档
├── 📄 DATABASE_UPGRADE_GUIDE.md                  # 数据库升级指南文档
├── 🗄️ PERMISSION_GROUPS_INIT.sql                 # 权限分组初始化SQL脚本
├── 🗄️ PERMISSION_ROLE_INIT.sql                   # 权限角色初始化SQL脚本
├── 🗄️ SIMPLE_PERMISSION_UPGRADE.sql              # 简单权限升级SQL脚本
├── 🗄️ UPGRADE_PERMISSION_HIERARCHY.sql           # 权限层次升级SQL脚本
├── 🔧 fastRestart.bat                            # 快速重启脚本（Windows批处理）
├── 🔧 fastRestart.ps1                            # 快速重启脚本（PowerShell）
├── 🔧 start_in_vscode.bat                        # VS Code启动脚本
├── 📁 .git/                                      # Git版本控制目录
├── 📁 .vscode/                                   # VS Code配置目录
├── 📁 describe/                                  # 项目文档目录
│   └── 📄 SRS.docx                               # 软件需求规格说明书
└── 📁 www/                                       # 主要代码目录
    ├── 📄 IMPLEMENTATION_GUIDE.md                # 实现指南文档
    ├── 🗄️ MANUAL_PERMISSION_HIERARCHY_SETUP.sql  # 手动权限层次设置SQL脚本
    ├── 📄 PERMISSION_GROUPS_IMPLEMENTATION_GUIDE.md # 权限分组实现指南
    ├── 📁 backend/                               # 后端Spring Boot项目
    └── 📁 frontend/                              # 前端Vue3项目
```

## 后端项目结构（Spring Boot）

```
www/backend/
├── pom.xml                                    # Maven项目配置文件，定义依赖和构建配置
├── API_TEST_GUIDE.md                          # API测试指南文档
├── ARTICLE_API_README.md                      # 文章API说明文档
├── temp_login.json                            # 临时登录测试数据
├── temp_token.json                            # 临时令牌测试数据
├── config/                                    # 配置文件目录
├── routes/                                    # 路由配置目录
├── src/                                       # 源代码目录
│   ├── main/                                 # 主要源代码
│   │   ├── java/com/ecowiki/                # Java源代码包
│   │   │   ├── EcoWikiApplication.java       # Spring Boot主启动类
│   │   │   ├── config/                       # 配置类目录
│   │   │   │   ├── CorsConfig.java          # 跨域配置类
│   │   │   │   ├── DataInitializer.java     # 数据初始化配置类（创建默认管理员账户）
│   │   │   │   └── SecurityConfig.java      # Spring Security安全配置
│   │   │   ├── controller/                   # 控制器层
│   │   │   │   └── api/                     # API控制器
│   │   │   │       ├── admin/               # 管理员相关API
│   │   │   │       │   └── AdminController.java  # 管理员功能控制器（用户管理、角色管理、权限管理、系统统计）
│   │   │   │       ├── auth/                # 认证相关API
│   │   │   │       │   └── AuthController.java   # 认证控制器（登录、注册、token验证）
│   │   │   │       ├── ApiController.java   # API信息控制器（API文档和元信息）
│   │   │   │       └── ArticleController.java     # 文章控制器（文章CRUD、搜索、分类）
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
│   │   │   ├── entity/                      # 实体类
│   │   │   │   ├── Article.java            # 文章实体类
│   │   │   │   ├── Articles.java           # 文章辅助实体类
│   │   │   │   ├── Permission.java         # 权限实体类
│   │   │   │   ├── Role.java               # 角色实体类
│   │   │   │   ├── RolePermission.java     # 角色权限关联实体类
│   │   │   │   ├── RolePermissionId.java   # 角色权限复合主键类
│   │   │   │   ├── User.java               # 用户实体类
│   │   │   │   ├── UserRole.java           # 用户角色关联实体类
│   │   │   │   └── UserRoleId.java         # 用户角色复合主键类
│   │   │   ├── repository/                  # 数据访问层
│   │   │   │   ├── ArticleRepository.java   # 文章数据访问接口
│   │   │   │   ├── PermissionRepository.java # 权限数据访问接口
│   │   │   │   ├── RolePermissionRepository.java # 角色权限关联数据访问接口
│   │   │   │   ├── RoleRepository.java      # 角色数据访问接口
│   │   │   │   ├── UserRepository.java      # 用户数据访问接口
│   │   │   │   └── UserRoleRepository.java  # 用户角色关联数据访问接口
│   │   │   ├── security/                    # 安全相关
│   │   │   │   ├── JwtAuthenticationFilter.java  # JWT认证过滤器
│   │   │   │   ├── JwtUtil.java            # JWT工具类
│   │   │   │   └── UserDetailsServiceImpl.java   # 用户详情服务实现
│   │   │   ├── service/                     # 业务逻辑层
│   │   │   │   ├── AdminService.java       # 管理员服务（系统统计、用户管理）
│   │   │   │   ├── ArticleService.java     # 文章服务（文章业务逻辑）
│   │   │   │   ├── PermissionService.java  # 权限服务（权限检查、角色验证）
│   │   │   │   └── UserService.java        # 用户服务（用户CRUD、角色管理）
│   │   │   └── util/                        # 工具类目录
│   │   └── resources/                       # 资源文件
│   │       ├── application.properties       # Spring Boot配置文件
│   │       └── db/                         # 数据库相关文件
│   └── test/                               # 测试代码目录
│       └── java/                           # 测试Java代码
└── target/                                 # Maven构建输出目录
    ├── classes/                            # 编译后的class文件
    │   ├── application.properties          # 编译后的配置文件
    │   └── com/ecowiki/                   # 编译后的Java类
    ├── generated-sources/                  # 生成的源代码
    │   └── annotations/                    # 注解生成的代码
    ├── generated-test-sources/             # 生成的测试源代码
    │   └── test-annotations/               # 测试注解生成的代码
    ├── maven-status/                       # Maven状态信息
    │   └── maven-compiler-plugin/          # 编译插件状态
    │       ├── compile/                    # 编译状态
    │       └── testCompile/               # 测试编译状态
    └── test-classes/                       # 测试类编译输出
```

## 前端项目结构（Vue3 + TypeScript）

```
www/frontend/
├── 📄 package.json                            # Node.js项目配置文件，定义依赖和脚本
├── 📄 pnpm-lock.yaml                         # pnpm锁定文件，确保依赖版本一致
├── 📄 README.md                              # 前端项目说明文档
├── 📄 REGISTER_FEATURE.md                    # 注册功能说明文档
├── 📄 FINAL_STRUCTURE.md                     # 前端最终结构文档
├── 📄 env.d.ts                               # TypeScript环境类型定义
├── 📄 index.html                             # HTML入口文件
├── 📄 tsconfig.json                          # TypeScript主配置文件
├── 📄 tsconfig.app.json                      # TypeScript应用配置文件
├── 📄 tsconfig.node.json                     # TypeScript Node.js配置文件
├── 📄 vite.config.ts                         # Vite构建工具配置文件
├── 📁 public/                                # 静态资源目录
│   └── favicon.ico                          # 网站图标
└── 📁 src/                                   # 源代码目录
    ├── 📄 App.vue                            # 根组件，管理全局模态框和路由
    ├── 📄 main.ts                            # 应用入口文件，创建Vue应用实例
    │
    ├── 📁 api/                               # API接口定义
    │   ├── index.ts                         # API基础配置和请求拦截器
    │   ├── article.ts                       # 文章相关API接口
    │   ├── tag.ts                           # 标签相关API接口
    │   └── user.ts                          # 用户相关API接口
    │
    ├── 📁 assets/                            # 静态资源
    │   ├── base.css                         # 基础CSS样式
    │   ├── main.css                         # 主要CSS样式
    │   ├── logo.svg                         # Logo图标
    │   ├── 📁 fonts/                        # 字体文件目录
    │   ├── 📁 icons/                        # 图标文件目录
    │   └── 📁 images/                       # 图片文件目录
    │
    ├── 📁 components/                        # 组件目录（已重构优化）
    │   ├── 📁 admin/                         # 管理后台组件
    │   │   ├── AdminLayout.vue              # 管理后台布局组件
    │   │   └── 📁 views/                    # 管理后台页面组件
    │   │       ├── PermissionManagement.vue # 权限管理页面
    │   │       ├── RoleManagement.vue       # 角色管理页面
    │   │       ├── RolePermissionAssignment.vue # 角色权限分配页面
    │   │       ├── SystemSettings.vue       # 系统设置页面
    │   │       └── UserList.vue             # 用户列表管理页面
    │   │
    │   ├── 📁 article/                       # 文章相关组件
    │   │   ├── ArticleComments.vue          # 文章评论组件
    │   │   ├── ArticleContent.vue           # 文章内容组件
    │   │   ├── FloatingActionButtons.vue    # 浮动操作按钮
    │   │   └── RelatedArticles.vue          # 相关文章组件
    │   │
    │   ├── 📁 common/                        # 通用组件
    │   │   ├── SliderCaptcha.vue            # 滑块验证码组件（已重构优化）
    │   │   └── Toast.vue                    # 消息提示组件
    │   │
    │   ├── 📁 edit/                          # 编辑相关组件
    │   │   └── WikiEditor.vue               # Wiki编辑器组件
    │   │
    │   ├── 📁 forms/                         # 表单组件
    │   │   ├── ForgotPanel.vue              # 忘记密码表单
    │   │   ├── LoginPanel.vue               # 登录表单
    │   │   └── RegisterPanel.vue            # 注册表单（已集成滑块验证码）
    │   │
    │   ├── 📁 home/                          # 首页组件
    │   │   ├── FeaturedArticles.vue         # 精选文章组件
    │   │   └── RecentUpdates.vue            # 最近更新组件
    │   │
    │   ├── 📁 icons/                         # 图标组件
    │   │   ├── IconArticleTOC.vue           # 文章目录图标
    │   │   ├── IconCommunity.vue            # 社区图标
    │   │   ├── IconCross.vue                # 叉号图标
    │   │   ├── IconDocumentation.vue        # 文档图标
    │   │   ├── IconEcosystem.vue            # 生态系统图标
    │   │   ├── IconFire.vue                 # 火焰图标
    │   │   ├── IconSupport.vue              # 支持图标
    │   │   ├── IconTag.vue                  # 标签图标
    │   │   ├── IconTooling.vue              # 工具图标
    │   │   └── index.ts                     # 图标导出文件
    │   │
    │   ├── 📁 layout/                        # 布局组件
    │   │   ├── AppFooter.vue                # 应用底部组件
    │   │   ├── AppHeader.vue                # 应用头部组件
    │   │   ├── AppMainContent.vue           # 主内容区域组件
    │   │   ├── AppSidebar.vue               # 侧边栏组件
    │   │   ├── HeaderLogo.vue               # 头部Logo组件
    │   │   ├── HeaderSearch.vue             # 头部搜索组件
    │   │   └── HeaderUserArea.vue           # 头部用户区域组件
    │   │
    │   ├── 📁 modals/                        # 模态框组件
    │   │   └── AuthModals.vue               # 认证模态框容器
    │   │
    │   └── 📁 userhome/                      # 用户主页组件
    │       └── userProfile.vue              # 用户资料组件
    │
    ├── 📁 composables/                       # Vue3组合式函数
    │   └── useAuth.ts                       # 认证相关组合函数
    │
    ├── 📁 router/                            # 路由配置
    │   └── index.ts                         # 路由定义和守卫配置
    │
    ├── 📁 spgame/                            # 小游戏模块
    │   └── SpaceShooterGame.vue             # 太空射击游戏
    │
    ├── 📁 stores/                            # Pinia状态管理
    │   └── adminUserStore.ts                # 管理后台用户状态管理
    │
    ├── 📁 styles/                            # 样式文件
    │   └── themes/                          # 主题样式目录
    │
    ├── 📁 types/                             # TypeScript类型定义
    │   └── permission.ts                    # 权限相关类型定义
    │
    ├── 📁 utils/                             # 工具函数
    │   ├── toast.ts                         # 消息提示工具
    │   ├── validation.ts                    # 表单验证工具
    │   └── wikiParser.ts                    # Wiki解析工具
    │
    └── 📁 views/                             # 视图页面组件
        ├── ClassicHome.vue                  # 经典首页布局
        ├── DynamicHome.vue                  # 动态首页布局
        ├── SimpleHome.vue                   # 简约首页布局
        └── 📁 wiki/                         # Wiki相关页面
            ├── ArticleDetail.vue            # 文章详情页面
            └── ArticleEdit.vue              # 文章编辑页面
```

## 数据库结构

### 核心表结构
- **users** - 用户表（存储用户基本信息，不包含角色字段）
- **roles** - 角色表（定义系统角色：user、moderator、admin、superadmin等）
- **user_roles** - 用户角色关联表（多对多关系，支持用户拥有多个角色）
- **permissions** - 权限表（定义系统权限：article:read、user:manage等）
- **role_permissions** - 角色权限关联表（多对多关系，支持角色拥有多个权限）
- **articles** - 文章表（存储wiki文章内容、分类、标签等信息）

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

-- 权限表
permissions (
    permission_id INT PRIMARY KEY AUTO_INCREMENT, -- 权限ID
    permission_name VARCHAR(100) UNIQUE NOT NULL, -- 权限名称
    description TEXT,                           -- 权限描述
    created_at TIMESTAMP,                       -- 创建时间
    updated_at TIMESTAMP                        -- 更新时间
)

-- 角色权限关联表
role_permissions (
    role_id INT,                                -- 角色ID（外键）
    permission_id INT,                          -- 权限ID（外键）
    created_at TIMESTAMP,                       -- 分配时间
    PRIMARY KEY (role_id, permission_id)        -- 复合主键
)

-- 文章表
articles (
    article_id BIGINT PRIMARY KEY AUTO_INCREMENT, -- 文章ID
    title VARCHAR(255) NOT NULL,               -- 文章标题
    content TEXT,                               -- 文章内容
    category VARCHAR(100),                      -- 文章分类
    tags TEXT,                                  -- 文章标签
    author VARCHAR(100),                        -- 作者
    views INT DEFAULT 0,                        -- 浏览量
    likes INT DEFAULT 0,                        -- 点赞数
    published BOOLEAN DEFAULT TRUE,             -- 是否发布
    created_at TIMESTAMP,                       -- 创建时间
    updated_at TIMESTAMP                        -- 更新时间
)
```

## 架构特点

### 后端架构
- **分层架构**: Controller → Service → Repository → Entity
- **安全机制**: JWT认证 + Spring Security
- **权限管理**: 基于角色的访问控制（RBAC）
- **数据访问**: Spring Data JPA + MySQL
- **API设计**: RESTful API设计风格

### 前端架构（已重构优化）
- **组件化**: Vue3组合式API + TypeScript
- **模块化结构**: 按功能分类组织组件（common、forms、layout、modals等）
- **状态管理**: Pinia状态管理
- **路由管理**: Vue Router + 路由守卫
- **样式系统**: Scoped CSS + 响应式设计
- **构建工具**: Vite + TypeScript
- **滑块验证码**: 自研SliderCaptcha组件，丝滑拖拽体验

### 核心功能模块
1. **用户认证系统**: 注册、登录、JWT认证、忘记密码、滑块验证码
2. **权限管理系统**: 用户角色管理、权限检查、细粒度权限控制
3. **管理后台系统**: 用户管理、角色管理、权限管理、系统设置
4. **首页风格系统**: 经典/简约/动态三种首页风格
5. **文章管理系统**: 文章创建、编辑、查看、分类、搜索
6. **自动化功能**: 作者自动设置、分类标签自动转换
7. **数据初始化**: 默认管理员账户、基础角色权限初始化
8. **小游戏模块**: 太空射击游戏，增加用户体验

## 开发和部署

### 开发环境要求
- **后端**: Java 17+, Maven 3.6+, MySQL 8.0+
- **前端**: Node.js 16+, pnpm 7+, 现代浏览器

### 启动方式
```bash
# 使用快速启动脚本（推荐）
start_in_vscode.bat     # VS Code环境启动
fastRestart.bat         # 快速重启服务

# 手动启动
# 后端启动
cd www/backend
mvn spring-boot:run

# 前端启动
cd www/frontend
pnpm install
pnpm dev
```

### 默认管理员账户
系统首次启动时会自动创建默认管理员账户：
- **用户名**: superadmin
- **密码**: EcoWiki@2025
- **邮箱**: admin@ecowiki.com
- **权限**: 超级管理员权限

### 构建部署
```bash
# 后端打包
cd www/backend
mvn clean package

# 前端构建
cd www/frontend
pnpm build
```

## 重要更新和优化

### 2025年7月10日 - 重大重构完成
1. **前端结构完全重构**
   - 组件按功能分类到专门目录（common、forms、layout、modals等）
   - 删除所有冗余和未使用的组件
   - 路径引用全部修复，编译零错误

2. **滑块验证码重构**
   - 全新SliderCaptcha组件，解决所有"闪回"问题
   - 边界严丝合缝，拖拽体验丝滑
   - 已完美集成到注册流程中

3. **项目清理优化**
   - 删除过时的历史文档
   - 清理空目录和未使用文件
   - 项目结构更加清晰和专业

## 注意事项

1. **组件结构重构**: 前端组件已按功能重新组织，所有路径引用已更新
2. **滑块验证码**: 新的SliderCaptcha组件提供更好的用户体验
3. **角色系统重构**: 已从User表的userGroup字段迁移到独立的user_roles表
4. **权限检查**: 所有管理员功能都通过PermissionService统一检查权限
5. **前后端分离**: 通过JWT进行无状态认证
6. **响应式设计**: 支持桌面端和移动端自适应
7. **模块化设计**: 前后端均采用模块化架构，便于维护和扩展
8. **数据初始化**: 系统首次启动自动创建默认账户和基础数据
9. **权限体系**: 支持细粒度权限控制，角色-权限分离设计
10. **开发工具**: 提供快速启动和重启脚本，提高开发效率
11. **代码质量**: 经过全面清理和重构，达到生产级别标准

---

*文档最后更新时间: 2025年7月10日*
*项目版本: EcoWiki v2.0 (重构版)*
