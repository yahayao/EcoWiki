# EcoWiki 前端 - 注册功能实现

## 功能概述

已实现完整的用户注册功能，包含以下特性：

### 🚀 核心功能

1. **用户注册** - 支持用户名、邮箱、密码注册
2. **用户登录** - 支持用户名/邮箱登录
3. **实时验证** - 用户名和邮箱可用性实时检查
4. **表单验证** - 客户端表单验证和错误提示
5. **状态管理** - 全局用户认证状态管理
6. **优雅提示** - 替代原生alert的Toast消息组件

### 📁 新增文件结构

```
src/
├── api/
│   ├── index.ts          # HTTP客户端配置
│   └── user.ts           # 用户相关API接口
├── composables/
│   └── useAuth.ts        # 认证状态管理
├── components/
│   └── Toast.vue         # 消息提示组件
├── utils/
│   ├── validation.ts     # 表单验证工具
│   └── toast.ts          # Toast服务
└── ...
```

### 🔧 技术实现

#### API服务 (`src/api/`)

- **index.ts**: 基于axios的HTTP客户端，包含请求/响应拦截器
- **user.ts**: 用户注册、登录、验证等API接口

#### 表单验证 (`src/utils/validation.ts`)

- 邮箱格式验证
- 用户名格式验证（3-20位，字母数字下划线）
- 密码强度验证（至少8位，包含字母和数字）
- 防抖函数实现

#### 状态管理 (`src/composables/useAuth.ts`)

- 全局用户认证状态
- localStorage持久化
- 自动状态恢复

#### 消息提示 (`src/components/Toast.vue` & `src/utils/toast.ts`)

- 支持成功、错误、警告、信息四种类型
- 自动消失和手动关闭
- 优雅的动画效果

### 🎨 UI改进

#### RegisterPanel组件增强

- ✅ 实时用户名可用性检查
- ✅ 实时邮箱可用性检查  
- ✅ 表单验证错误提示
- ✅ 输入状态视觉反馈（成功/错误/检查中）
- ✅ 智能提交按钮状态
- ✅ 优雅的Toast消息提示

#### LoginPanel组件增强

- ✅ 真实API调用
- ✅ 用户状态管理
- ✅ Toast消息提示

### 🔗 API接口设计

#### 注册接口
```typescript
POST /api/auth/register
Content-Type: application/json

{
  "username": "string",
  "email": "string", 
  "password": "string"
}
```

#### 登录接口
```typescript
POST /api/auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string",
  "rememberMe": "boolean"
}
```

#### 检查用户名可用性
```typescript
GET /api/auth/check-username?username={username}
```

#### 检查邮箱可用性
```typescript
GET /api/auth/check-email?email={email}
```

### 🌍 环境配置

创建了环境配置文件：

- `.env.development` - 开发环境配置
- `.env.production` - 生产环境配置

配置API基础URL：
```
VITE_API_BASE_URL=http://localhost:8080/api
```

### 📦 依赖

新增依赖：
- `axios` - HTTP客户端

### 🚀 使用方法

1. **启动开发服务器**
   ```bash
   cd www/frontend
   npm run dev
   ```

2. **配置后端API**
   - 确保后端服务器运行在 `http://localhost:8080`
   - 或修改 `.env.development` 中的 `VITE_API_BASE_URL`

3. **测试注册功能**
   - 填写用户名（3-20位，字母数字下划线）
   - 填写有效邮箱地址
   - 设置密码（至少8位，包含字母和数字）
   - 确认密码
   - 点击注册按钮

### 🔒 安全特性

- 客户端表单验证
- 密码强度检查
- 防抖请求避免频繁API调用
- Token-based认证
- 请求拦截器自动添加认证头
- 统一错误处理

### 🎯 后续开发建议

1. **密码找回功能**
2. **邮箱验证功能**  
3. **OAuth第三方登录**
4. **用户头像上传**
5. **个人信息编辑**
6. **退出登录功能**

### 🐛 调试

如果遇到问题，请检查：

1. 后端API服务是否正常运行
2. 网络请求是否被CORS阻止
3. 浏览器控制台是否有错误信息
4. API响应格式是否符合前端期望

### 💡 开发者提示

- 所有API调用都有完整的错误处理
- Toast组件支持自定义样式和持续时间
- 表单验证可以轻松扩展新的规则
- 认证状态在页面刷新后会自动恢复
